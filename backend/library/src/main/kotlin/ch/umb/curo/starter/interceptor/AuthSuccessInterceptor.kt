package ch.umb.curo.starter.interceptor

import com.auth0.jwt.interfaces.DecodedJWT
import javax.servlet.http.HttpServletRequest

/**
 * AuthSuccessInterceptors are called on the /curo-api/auth/success endpoint.
 */
interface AuthSuccessInterceptor {

    val name: String
    val async: Boolean
    /**
     * Execution is ordered from lowest to highest
     */
    val order: Int

    fun onIntercept(jwt: DecodedJWT, jwtRaw: String, request: HttpServletRequest): Boolean
}
