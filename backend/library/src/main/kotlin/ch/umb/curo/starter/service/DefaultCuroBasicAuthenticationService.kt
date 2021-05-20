package ch.umb.curo.starter.service

import ch.umb.curo.starter.exception.ApiException
import ch.umb.curo.starter.interceptor.BasicSuccessInterceptor
import ch.umb.curo.starter.models.request.CuroPermissionsRequest
import ch.umb.curo.starter.models.response.AuthenticationSuccessResponse
import ch.umb.curo.starter.models.response.CuroPermissionsResponse
import ch.umb.curo.starter.property.CuroProperties
import ch.umb.curo.starter.util.AuthUtil
import org.camunda.bpm.engine.AuthorizationService
import org.camunda.bpm.engine.IdentityService
import org.camunda.bpm.engine.authorization.Permissions
import org.camunda.bpm.engine.authorization.Resources
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.Executors
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

open class DefaultCuroBasicAuthenticationService(
    private val basicSuccessInterceptors: List<BasicSuccessInterceptor>,
    open val properties: CuroProperties,
    open val identityService: IdentityService,
    open val authorizationService: AuthorizationService
) : CuroAuthenticationService {

    private val authSuccessInterceptorThreadPool = Executors.newCachedThreadPool()
    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun success(request: HttpServletRequest): AuthenticationSuccessResponse {
        val username = AuthUtil.getUsernameFromRequest(request, properties)

        val synchronousInterceptors = basicSuccessInterceptors.filter { !it.async }.sortedBy { it.order }
        val asynchronousInterceptors = basicSuccessInterceptors.filter { it.async }.sortedBy { it.order }

        val completedSteps = arrayListOf<String>()

        logger.debug("CURO: run basic success interceptors")
        synchronousInterceptors.forEach {
            logger.debug("CURO:\t-> run ${it.javaClass.canonicalName}:onIntercept")
            val intercept = it.onIntercept(username, request)
            if (intercept) {
                completedSteps.add(it.name)
            }
        }

        logger.debug("CURO: run async oauth2 success interceptors")
        authSuccessInterceptorThreadPool.execute {
            asynchronousInterceptors.forEach {
                logger.debug("CURO:\t-> run async ${it.javaClass.canonicalName}:onIntercept")
                it.onIntercept(username, request)
            }
        }

        val response = AuthenticationSuccessResponse()
        response.completedSteps = completedSteps
        response.asyncSteps = asynchronousInterceptors.map { it.name }
        return response
    }

    override fun logout(request: HttpServletRequest, response: HttpServletResponse) {
        if (properties.auth.basic.useSessionCookie) {
            AuthUtil.invalidateSession(request, response, properties)
        }
        response.status = 200
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
                        Permissions.valueOf(it.uppercase(Locale.getDefault()))
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
