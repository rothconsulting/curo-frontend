package ch.umb.curo.starter.service

import ch.umb.curo.starter.exception.ApiException
import ch.umb.curo.starter.interceptor.AuthSuccessInterceptor
import ch.umb.curo.starter.models.request.CuroPermissionsRequest
import ch.umb.curo.starter.models.response.AuthenticationSuccessResponse
import ch.umb.curo.starter.models.response.CuroPermissionsResponse
import ch.umb.curo.starter.property.CuroProperties
import com.auth0.jwt.JWT
import org.camunda.bpm.engine.AuthorizationService
import org.camunda.bpm.engine.IdentityService
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.authorization.Permissions
import org.camunda.bpm.engine.authorization.Resources
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import javax.servlet.http.HttpServletRequest

class DefaultCuroAuthenticationService(
    val authSuccessInterceptors: List<AuthSuccessInterceptor>,
    val properties: CuroProperties,
    val identityService: IdentityService,
    val authorizationService: AuthorizationService
) : CuroAuthenticationService {

    private val BEARER_HEADER_PREFIX = "Bearer "
    private val authSuccessInterceptorThreadPool = Executors.newCachedThreadPool()
    private val logger = LoggerFactory.getLogger(this::class.java)

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

    override fun getPermissions(
        returnPermissions: Boolean,
        request: CuroPermissionsRequest?
    ): CuroPermissionsResponse {
        val response = CuroPermissionsResponse()
        val groupIds = identityService.currentAuthentication.groupIds
        response.groups.addAll(groupIds)

        val userId = identityService.currentAuthentication.userId
        response.userId = userId

        if (!returnPermissions || request == null) {
            return response
        }

        response.permissions = hashMapOf()

        request.entries.forEach { resource ->

            val resourceId = resource.key
            response.permissions!![resourceId] = hashMapOf()

            resource.value.entries.forEach { authResource ->

                if (!Resources.values().contains(authResource.key)) {
                    throw ApiException.invalidArgument400(arrayListOf("${authResource.key.resourceName()} is not a valid resource"))
                }

                var extendedPermissions: ArrayList<String> = authResource.value.toCollection(arrayListOf())
                if (authResource.value.contains("*")) {
                    extendedPermissions.addAll(Permissions.values().filter { it.types.contains(authResource.key) }
                        .map { it.name })
                    extendedPermissions = extendedPermissions
                        .filterNot { it == "*" }
                        .filterNot { it == "NONE" }
                        .toCollection(arrayListOf())
                }

                val filteredPermissions = extendedPermissions.distinct().map {
                    try {
                        Permissions.valueOf(it.toUpperCase())
                    } catch (e: Exception) {
                        throw ApiException.invalidArgument400(arrayListOf("$it is not a valid permission"))
                    }
                }

                val permissions = filteredPermissions.mapNotNull {
                    checkPermission(userId, groupIds, it, authResource.key, resourceId)
                }

                response.permissions!![resourceId]!![authResource.key] = permissions
            }
        }

        return response
    }

    /**
     * Returns permissions if user has it
     */
    private fun checkPermission(
        userId: String?,
        groupIds: List<String>?,
        permission: Permissions,
        authResource: Resources,
        resourceId: String
    ): Permissions? = try {
        if (authorizationService.isUserAuthorized(
                userId,
                groupIds,
                permission,
                authResource,
                resourceId
            )
        ) permission else null
    } catch (e: Exception) {
        logger.debug(e.localizedMessage)
        null
    }
}
