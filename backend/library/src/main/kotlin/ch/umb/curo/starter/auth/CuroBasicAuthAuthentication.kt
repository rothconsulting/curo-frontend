package ch.umb.curo.starter.auth

import ch.umb.curo.starter.SpringContext
import ch.umb.curo.starter.property.CuroProperties
import ch.umb.curo.starter.util.AuthUtil
import org.camunda.bpm.engine.ProcessEngine
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

    override fun extractAuthenticatedUser(request: HttpServletRequest, engine: ProcessEngine): AuthenticationResult {

        val properties = SpringContext.getBean(CuroProperties::class.java) ?: return AuthenticationResult.unsuccessful()

        //This will return a username based on the current session if session cookies are allowed.
        val sessionUsername = AuthUtil.getUsernameFromSessionCookieOrNull(request, properties, true)
        if (sessionUsername != null) {
            return AuthenticationResult.successful(sessionUsername)
        }

        //Check for CuroBasic in authorization header else we use normal Basic
        val credentials = AuthUtil.getDecodedBasicAuthCredentials(request) ?: return AuthenticationResult.unsuccessful()

        return if (isAuthenticated(engine, credentials.username, credentials.password)) {
            if (properties.auth.basic.useSessionCookie) {
                AuthUtil.createOrGetSecureSession(request, properties)
                request.session.setAttribute("username", credentials.username)
            }
            AuthenticationResult.successful(credentials.username)
        } else {
            AuthenticationResult.unsuccessful(credentials.username)
        }
    }

    override fun augmentResponseByAuthenticationChallenge(
        response: HttpServletResponse, engine: ProcessEngine
    ) {
        response.setHeader(
            HttpHeaders.WWW_AUTHENTICATE,
            AuthUtil.CURO_BASIC_AUTH_HEADER_PREFIX + "realm=\"" + engine.name + "\""
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
