package ch.umb.curo.starter.interceptor

import javax.servlet.http.HttpServletRequest

/**
 * AuthSuccessInterceptors are called on the /curo-api/auth/success endpoint.
 */
interface BasicSuccessInterceptor {

    val name: String
    val async: Boolean
    /**
     * Execution is ordered from lowest to highest
     */
    val order: Int

    fun onIntercept(username: String?, request: HttpServletRequest): Boolean
}
