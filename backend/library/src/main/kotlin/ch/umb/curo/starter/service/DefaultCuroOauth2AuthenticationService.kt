package ch.umb.curo.starter.service

import ch.umb.curo.starter.exception.ApiException
import ch.umb.curo.starter.interceptor.Oauth2SuccessInterceptor
import ch.umb.curo.starter.models.response.AuthenticationSuccessResponse
import ch.umb.curo.starter.property.CuroProperties
import ch.umb.curo.starter.util.AuthUtil
import com.auth0.jwt.JWT
import org.camunda.bpm.engine.AuthorizationService
import org.camunda.bpm.engine.IdentityService
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class DefaultCuroOauth2AuthenticationService(
    private val oauth2SuccessInterceptors: List<Oauth2SuccessInterceptor>,
    override val properties: CuroProperties,
    override val identityService: IdentityService,
    override val authorizationService: AuthorizationService
) : DefaultCuroBasicAuthenticationService(arrayListOf(), properties, identityService, authorizationService),
    CuroAuthenticationService {

    private val authSuccessInterceptorThreadPool = Executors.newCachedThreadPool()
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun success(request: HttpServletRequest): AuthenticationSuccessResponse {
        val jwtRaw = request.getHeader("Authorization")?.substring(AuthUtil.BEARER_HEADER_PREFIX.length)
            ?: throw ApiException.UNAUTHENTICATED_401.printException(properties.printStacktrace)
        val decodedJwt = JWT.decode(jwtRaw)

        val synchronousInterceptors = oauth2SuccessInterceptors.filter { !it.async }.sortedBy { it.order }
        val asynchronousInterceptors = oauth2SuccessInterceptors.filter { it.async }.sortedBy { it.order }

        val completedSteps = arrayListOf<String>()

        logger.debug("CURO: run oauth2 success interceptors")
        synchronousInterceptors.forEach {
            logger.debug("CURO:\t-> run ${it.javaClass.canonicalName}:onIntercept")
            val intercept = it.onIntercept(decodedJwt, jwtRaw, request)
            if (intercept) {
                completedSteps.add(it.name)
            }
        }

        logger.debug("CURO: run async oauth2 success interceptors")
        authSuccessInterceptorThreadPool.execute {
            asynchronousInterceptors.forEach {
                logger.debug("CURO:\t-> run async ${it.javaClass.canonicalName}:onIntercept")
                it.onIntercept(decodedJwt, jwtRaw, request)
            }
        }

        val response = AuthenticationSuccessResponse()
        response.completedSteps = completedSteps
        response.asyncSteps = asynchronousInterceptors.map { it.name }
        return response
    }

    override fun logout(request: HttpServletRequest, response: HttpServletResponse){
        response.status = 200
    }
}
