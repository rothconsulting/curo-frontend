package ch.umb.curo.starter.util

import ch.umb.curo.starter.property.CuroProperties
import org.camunda.bpm.engine.impl.digest._apacheCommonsCodec.Base64
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpSession
import javax.ws.rs.core.HttpHeaders

object AuthUtil {

    const val BEARER_HEADER_PREFIX = "Bearer "
    const val BASIC_AUTH_HEADER_PREFIX = "Basic "
    const val CURO_BASIC_AUTH_HEADER_PREFIX = "CuroBasic "

    fun getUsernameFromRequest(
        request: HttpServletRequest,
        properties: CuroProperties
    ): String? {
        return if (properties.auth.basic.useSessionCookie) {
            getUsernameFromSessionCookie(request)
        } else {
            getUsernameFromBasicAuth(request)
        }
    }

    fun getUsernameFromSessionCookie(request: HttpServletRequest): String? {
        val session: HttpSession? = request.getSession(false)
        return session?.getAttribute("username") as String?
    }

    fun getUsernameFromSessionCookieOrNull(
        request: HttpServletRequest,
        properties: CuroProperties,
        invalidateSessionIfUserNull: Boolean = true
    ): String? {
        return if (properties.auth.basic.useSessionCookie) {
            val username = getUsernameFromSessionCookie(request)
            if (username != null) {
                username
            } else {
                if(invalidateSessionIfUserNull) {
                    val session: HttpSession? = request.getSession(false)
                    session?.invalidate()
                }
                null
            }
        } else {
            null
        }
    }

    fun getDecodedBasicAuthCredentials(request: HttpServletRequest): DecodedBasicAuth? {
        val authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION)?.takeIf { it.isNotBlank() } ?: return null

        //Check for CuroBasic in authorization header else we use normal Basic
        val encodedCredentials = if (authorizationHeader.startsWith(CURO_BASIC_AUTH_HEADER_PREFIX)) {
            authorizationHeader.substring(CURO_BASIC_AUTH_HEADER_PREFIX.length)
        } else {
            authorizationHeader.substring(BASIC_AUTH_HEADER_PREFIX.length)
        }
        val decodedCredentials = String(Base64.decodeBase64(encodedCredentials))
        val firstColonIndex = decodedCredentials.indexOf(":")

        return if (firstColonIndex == -1) {
            null
        } else {
            DecodedBasicAuth(
                decodedCredentials.substring(0, firstColonIndex),
                decodedCredentials.substring(firstColonIndex + 1)
            )
        }
    }

    fun createOrGetSecureSession(request: HttpServletRequest, properties: CuroProperties): HttpSession {
        val session = request.getSession(true)
        session.maxInactiveInterval = properties.auth.basic.sessionTimeout.toSeconds().toInt()

        return session
    }

    fun invalidateSession(request: HttpServletRequest, response: HttpServletResponse, properties: CuroProperties) {
        val session = request.getSession(false)
        session?.invalidate()

        val expiredCookie = Cookie(properties.auth.basic.sessionCookieName, "")
        expiredCookie.maxAge = 1
        expiredCookie.path = "/"
        expiredCookie.secure = properties.auth.basic.secureOnlySessionCookie
        response.addCookie(expiredCookie)
    }

    private fun getUsernameFromBasicAuth(request: HttpServletRequest): String? {
        val decodedCredentials = getDecodedBasicAuthCredentials(request)
        return decodedCredentials?.username
    }

    class DecodedBasicAuth(val username: String, val password: String)

}
