package ch.umb.curo.starter.interceptor

import com.auth0.jwt.interfaces.DecodedJWT
import javax.servlet.http.HttpServletRequest

interface AuthSuccessInterceptor {

    val name: String
    val async: Boolean
    val order: Int

    fun onIntercept(jwt: DecodedJWT, jwtRaw: String, request: HttpServletRequest): Boolean
}
