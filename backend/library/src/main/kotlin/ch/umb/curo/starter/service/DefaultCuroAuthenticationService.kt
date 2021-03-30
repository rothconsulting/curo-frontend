package ch.umb.curo.starter.service

import ch.umb.curo.starter.exception.ApiException
import ch.umb.curo.starter.interceptor.AuthSuccessInterceptor
import ch.umb.curo.starter.models.response.AuthenticationSuccessResponse
import ch.umb.curo.starter.property.CuroProperties
import com.auth0.jwt.JWT
import java.util.concurrent.Executors
import javax.servlet.http.HttpServletRequest

class DefaultCuroAuthenticationService(
    val authSuccessInterceptors: List<AuthSuccessInterceptor>,
    val properties: CuroProperties
) : CuroAuthenticationService {

    private val BEARER_HEADER_PREFIX = "Bearer "
    private val authSuccessInterceptorThreadPool = Executors.newCachedThreadPool()

    override fun success(request: HttpServletRequest): AuthenticationSuccessResponse {
        val jwtRaw = request.getHeader("Authorization")?.substring(BEARER_HEADER_PREFIX.length)
            ?: throw ApiException.UNAUTHENTICATED_401.printException(properties.printStacktrace)
        val decodedJwt = JWT.decode(jwtRaw)

        val synchronousInterceptors = authSuccessInterceptors.filter { !it.async }.sortedBy { it.order }
        val asynchronousInterceptors = authSuccessInterceptors.filter { it.async }.sortedBy { it.order }

        val completedSteps = arrayListOf<String>()

        synchronousInterceptors.forEach {
            val intercept = it.onIntercept(decodedJwt, jwtRaw, request)
            if (intercept) {
                completedSteps.add(it.name)
            }
        }

        authSuccessInterceptorThreadPool.execute {
            asynchronousInterceptors.forEach { it.onIntercept(decodedJwt, jwtRaw, request) }
        }

        val response = AuthenticationSuccessResponse()
        response.completedSteps = completedSteps
        response.asyncSteps = asynchronousInterceptors.map { it.name }
        return response
    }
}
