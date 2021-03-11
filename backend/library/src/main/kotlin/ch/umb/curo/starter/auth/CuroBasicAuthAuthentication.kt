package ch.umb.curo.starter.auth

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64
import org.camunda.bpm.engine.rest.security.auth.AuthenticationResult
import org.camunda.bpm.engine.rest.security.auth.impl.HttpBasicAuthenticationProvider
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.core.HttpHeaders

/**
 * Authenticates a request against the provided process engine's identity service by applying http basic authentication.
 * Not only http basic authentication with keyword Basic is allowed, it is also allowed to use the CuroBasic keyword.
 * This is needed that Curo can call Camunda directly without the browser prompting the BasicAuth dialog when password or username is wrong.
 *
 * Please not that this class is instantiated (Class.forName) by {@link org.camunda.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter#init(FilterConfig)} and therefore its not running in the spring context
 *
 * @author itsmefox
 *
 */
open class CuroBasicAuthAuthentication : HttpBasicAuthenticationProvider(), CuroLoginMethod {

    private val BASIC_AUTH_HEADER_PREFIX = "Basic "
    private val CURO_BASIC_AUTH_HEADER_PREFIX = "CuroBasic "

    override fun extractAuthenticatedUser(request: HttpServletRequest, engine: ProcessEngine): AuthenticationResult {

        val authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION)?.takeIf { it.isNotBlank() }
            ?: return AuthenticationResult.unsuccessful()

        //Check for CuroBasic in authorization header else we use normal Basic
        val encodedCredentials = if (authorizationHeader.startsWith(CURO_BASIC_AUTH_HEADER_PREFIX)) {
            authorizationHeader.substring(CURO_BASIC_AUTH_HEADER_PREFIX.length)
        } else {
            authorizationHeader.substring(BASIC_AUTH_HEADER_PREFIX.length)
        }
        val decodedCredentials = String(Base64.decodeBase64(encodedCredentials))
        val firstColonIndex = decodedCredentials.indexOf(":")

        if (firstColonIndex == -1) {
            return AuthenticationResult.unsuccessful()
        } else {
            val username = decodedCredentials.substring(0, firstColonIndex)
            val password = decodedCredentials.substring(firstColonIndex + 1)
            if (isAuthenticated(engine, username, password)) {
                return AuthenticationResult.successful(username)
            } else {
                return AuthenticationResult.unsuccessful(username)
            }
        }
    }

    override fun augmentResponseByAuthenticationChallenge(
        response: HttpServletResponse, engine: ProcessEngine
    ) {
        response.setHeader(
            HttpHeaders.WWW_AUTHENTICATE,
            CURO_BASIC_AUTH_HEADER_PREFIX + "realm=\"" + engine.name + "\""
        )
    }

    override fun getId(): String {
        return "ch.umb.curo.login_methods.basic_auth"
    }

    override fun getLoginMethodName(): String {
        return "Basic Auth"
    }

    override fun useUsernamePassword(): Boolean {
        return true
    }

}
