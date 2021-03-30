package ch.umb.curo.starter.auth

import ch.umb.curo.starter.SpringContext
import ch.umb.curo.starter.exception.ApiException
import ch.umb.curo.starter.interceptor.JWTClaimCheckInterceptor
import ch.umb.curo.starter.models.auth.OpenidConfiguration
import ch.umb.curo.starter.property.CuroProperties
import ch.umb.curo.starter.util.JsonBodyHandler
import com.auth0.jwk.JwkProvider
import com.auth0.jwk.UrlJwkProvider
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.*
import com.auth0.jwt.interfaces.DecodedJWT
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.rest.security.auth.AuthenticationProvider
import org.camunda.bpm.engine.rest.security.auth.AuthenticationResult
import org.slf4j.LoggerFactory
import java.net.URI
import java.net.URL
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.security.interfaces.RSAPublicKey
import java.time.Instant
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * Authenticates a request against the provided process engine's identity service by applying OAuth2 Bearer authentication.
 *
 * Please not that this class is instantiated (Class.forName) by {@link org.camunda.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter#init(FilterConfig)} and therefore its not running in the spring context
 *
 * @author itsmefox
 *
 */
open class CuroOAuth2Authentication : AuthenticationProvider, CuroLoginMethod {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val BEARER_HEADER_PREFIX = "Bearer "

    override fun extractAuthenticatedUser(request: HttpServletRequest, engine: ProcessEngine): AuthenticationResult {

        val properties = SpringContext.getBean(CuroProperties::class.java) ?: return AuthenticationResult.unsuccessful()

        val jwt = resolveToken(request)
        if(jwt == null) {
            val e = ApiException.unauthorized401("The Token is empty.")
            printErrorsToLog(e.message ?: "n/a")
            throw e
        }

        val decodedJwt = try {
            if (properties.auth.oauth2.verifyJwt) {
                verifyJwt(jwt)
            } else {
                val decodedJwt = JWT.decode(jwt)
                if (decodedJwt.issuer !in properties.auth.oauth2.allowedIssuers) {
                    throw InvalidClaimException("The Claim 'iss' value doesn't match the required issuer.")
                }

                if (decodedJwt.expiresAt.before(Date.from(Instant.now()))) {
                    throw TokenExpiredException(String.format("The Token has expired on %s.", decodedJwt.expiresAt))
                }

                decodedJwt
            }
        } catch (e: Exception) {
            printErrorsToLog(e.message ?: "n/a")
            throw e
        }

        val customChecks = SpringContext.getBeans(JWTClaimCheckInterceptor::class.java)
        logger.debug("CURO: Execute JWTClaimCheckInterceptors")
        customChecks.filterNotNull().sortedBy { it.order }.forEach {
            logger.debug("CURO: -> ${it.name} (${it.order})")
            try {
                it.onIntercept(decodedJwt, jwt, request)
            }catch (e: Exception) {
                throw e
            }
        }

        val userId = decodedJwt.getClaim(properties.auth.oauth2.userIdClaim).asString()
        if(userId == null) {
            val e = ApiException.unauthorized403("The Claim '${properties.auth.oauth2.userIdClaim}' does not exist.")
            printErrorsToLog(e.message ?: "n/a")
            throw e
        }

        //Allow if /curo-api/auth/success
        if (request.requestURI == "/curo-api/auth/success" && properties.auth.oauth2.userFederation.enabled) {
            return AuthenticationResult.successful(userId)
        }

        val user = engine.identityService.createUserQuery().userId(userId).singleResult()
        return if (user == null) {
            val e = ApiException.unauthorized403("User ($userId) does not exist on Camunda.")
            printErrorsToLog(e.message ?: "n/a")
            throw e
        } else {
            AuthenticationResult.successful(userId)
        }
    }

    /**
     * Perform the verification against the given Token
     *
     * @param encodedJwt to verify.
     * @return a verified and decoded JWT.
     */
    @Throws(
        AlgorithmMismatchException::class,
        SignatureVerificationException::class,
        TokenExpiredException::class,
        InvalidClaimException::class,
        JWTVerificationException::class
    )
    fun verifyJwt(encodedJwt: String): DecodedJWT {
        val properties = SpringContext.getBean(CuroProperties::class.java)!!

        var jwt = JWT.decode(encodedJwt)
        val jwkUrl = getCertUrl(jwt.issuer) ?: throw JWTVerificationException("Jwk url is not accessible or defined")

        val provider: JwkProvider = UrlJwkProvider(URL(jwkUrl))
        // Get the kid from received JWT token
        val jwk = provider[jwt.keyId]
        val algorithm = when (jwk.algorithm) {
            "RS256" -> Algorithm.RSA256(jwk.publicKey as RSAPublicKey, null)
            "RS384" -> Algorithm.RSA384(jwk.publicKey as RSAPublicKey, null)
            "RS512" -> Algorithm.RSA512(jwk.publicKey as RSAPublicKey, null)
            else -> throw AlgorithmMismatchException("Unknown algorithm ${jwk.algorithm}")
        }

        val verifier: JWTVerifier = JWT.require(algorithm)
            .withIssuer(*properties.auth.oauth2.allowedIssuers.toTypedArray())
            .build()

        jwt = verifier.verify(encodedJwt)

        return jwt
    }

    private fun getCertUrl(iss: String): String? {
        val properties = SpringContext.getBean(CuroProperties::class.java)!!
        val url = properties.auth.oauth2.jwkUrl

        if (url.isNotEmpty()) {
            return url
        }

        return try {
            val client = HttpClient.newHttpClient()
            val request = HttpRequest.newBuilder(
                URI.create("$iss/.well-known/openid-configuration")
            )
                .header("accept", "application/json")
                .build()
            val httpResponse = client.send(request, JsonBodyHandler(OpenidConfiguration::class.java))
            if (httpResponse.statusCode() == 200) {
                val openidConfiguration: OpenidConfiguration = httpResponse.body().get()
                openidConfiguration.jwksUri
            } else {
                printErrorsToLog("Was not able to locate .well-known/openid-configuration based on issuer claim")
                null
            }
        } catch (e: Exception) {
            printErrorsToLog("Was not able to locate .well-known/openid-configuration based on issuer claim")
            null
        }
    }

    private fun resolveToken(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith(BEARER_HEADER_PREFIX)) {
            bearerToken.substring(BEARER_HEADER_PREFIX.length, bearerToken.length)
        } else null
    }

    private fun printErrorsToLog(message: String) {
        val properties = SpringContext.getBean(CuroProperties::class.java)!!
        if (properties.auth.oauth2.printErrorsToLog) {
            logger.warn("Authentication failed: $message")
        }
    }

    override fun augmentResponseByAuthenticationChallenge(response: HttpServletResponse, engine: ProcessEngine) {
    }

    override fun getId(): String {
        return "ch.umb.curo.login_methods.bearer"
    }

    override fun getLoginMethodName(): String {
        return "Oauth2 Bearer"
    }

    override fun useUsernamePassword(): Boolean {
        return false
    }

}
