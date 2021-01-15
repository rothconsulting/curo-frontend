package ch.umb.curo.starter.auth

import ch.umb.curo.starter.SpringContext
import ch.umb.curo.starter.property.CuroProperties
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
import java.net.URL
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

    private var logger = LoggerFactory.getLogger(this::class.java)!!

    private val BEARER_HEADER_PREFIX = "Bearer "

    override fun extractAuthenticatedUser(request: HttpServletRequest, engine: ProcessEngine): AuthenticationResult {

        val properties = SpringContext.getBean(CuroProperties::class.java)!!

        val jwt = resolveToken(request)
        if (jwt == null) {
            printErrorsToLog("The Token is empty.")
            return AuthenticationResult.unsuccessful()
        }

        val decodedJwt = try {
            if (properties.auth.oauth2.verifyJwt) {
                verifyJwt(jwt)
            } else {
                val decodedJwt = JWT.decode(jwt)
                if (decodedJwt.issuer !in properties.auth.oauth2.allowedIssuers) {
                    throw InvalidClaimException("The Claim 'iss' value doesn't match the required issuer.")
                }

                if(decodedJwt.expiresAt.before(Date.from(Instant.now()))){
                    throw TokenExpiredException(String.format("The Token has expired on %s.", decodedJwt.expiresAt))
                }

                decodedJwt
            }
        } catch (e: Exception) {
            return when (e) {
                is JWTVerificationException,
                is InvalidClaimException,
                is TokenExpiredException,
                is JWTDecodeException -> {
                    printErrorsToLog(e.message ?: "n/a")
                    AuthenticationResult.unsuccessful()
                }
                else -> {
                    printErrorsToLog(e.message ?: "n/a")
                    AuthenticationResult.unsuccessful()
                }
            }
        }

        val userId = decodedJwt.getClaim(properties.auth.oauth2.userIdClaim).asString()
        if (userId == null) {
            printErrorsToLog("The Claim '${properties.auth.oauth2.userIdClaim}' does not exist.")
            return AuthenticationResult.unsuccessful()
        }

        val user = engine.identityService.createUserQuery().userId(userId).singleResult()
        return if (user == null) {
            printErrorsToLog("User ($userId) does not exist on Camunda.")
            AuthenticationResult.unsuccessful(userId)
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
            InvalidClaimException::class
    )
    fun verifyJwt(encodedJwt: String): DecodedJWT {
        val properties = SpringContext.getBean(CuroProperties::class.java)!!

        var jwt = JWT.decode(encodedJwt)

        val provider: JwkProvider = UrlJwkProvider(URL(properties.auth.oauth2.jwkUrl))
        // Get the kid from received JWT token
        val jwk = provider[jwt.keyId]
        val algorithm = Algorithm.RSA256(jwk.publicKey as RSAPublicKey, null)

        val verifier: JWTVerifier = JWT.require(algorithm)
            .withIssuer(*properties.auth.oauth2.allowedIssuers.toTypedArray())
            .build()

        jwt = verifier.verify(encodedJwt)

        return jwt
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
