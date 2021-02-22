package ch.umb.curo.starter.interceptor

import ch.umb.curo.starter.exception.ApiException
import ch.umb.curo.starter.property.CuroProperties
import com.auth0.jwt.interfaces.DecodedJWT
import org.camunda.bpm.engine.IdentityService
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest

@Component
@ConditionalOnProperty(prefix = "curo", value = ["auth.oauth2.user-federation.enabled"], havingValue = "true")
class CamundaUserFederationInterceptor(private val properties: CuroProperties,
                                       private val identityService: IdentityService) : AuthSuccessInterceptor {
    override val name: String = "Camunda:UserFederation"
    override val async: Boolean = false
    override val order: Int = 10

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun onIntercept(jwt: DecodedJWT, jwtRaw: String, request: HttpServletRequest): Boolean {
        if (properties.auth.type != "oauth2" || !properties.auth.oauth2.userFederation.enabled) {
            return false
        }

        val userId = jwt.getClaim(properties.auth.oauth2.userIdClaim).asString()
            ?: throw ApiException.invalidArgument400(arrayListOf("The Claim '${properties.auth.oauth2.userIdClaim}' does not exist."))
                .throwAndPrintStackTrace(properties.printStacktrace)

        //Check if user exists
        val user = identityService.createUserQuery().userId(userId).singleResult()
        if (!properties.auth.oauth2.userFederation.createNonExistingUsers && user == null) {
            throw ApiException.curoErrorCode(ApiException.CuroErrorCode.USER_NOT_FOUND).throwAndPrintStackTrace(properties.printStacktrace)
        }

        if (user == null) {
            //Create user
            val newUser = identityService.newUser(userId)
            newUser.email = jwt.getClaim(properties.auth.oauth2.userFederation.emailClaim).asString()
                ?: throw ApiException.invalidArgument400(arrayListOf("The Claim '${properties.auth.oauth2.userFederation.emailClaim}' does not exist."))
                    .throwAndPrintStackTrace(properties.printStacktrace)
            newUser.firstName = jwt.getClaim(properties.auth.oauth2.userFederation.firstNameClaim).asString()
                ?: throw ApiException.invalidArgument400(arrayListOf("The Claim '${properties.auth.oauth2.userFederation.firstNameClaim}' does not exist."))
                    .throwAndPrintStackTrace(properties.printStacktrace)
            newUser.lastName = jwt.getClaim(properties.auth.oauth2.userFederation.lastNameClaim).asString()
                ?: throw ApiException.invalidArgument400(arrayListOf("The Claim '${properties.auth.oauth2.userFederation.lastNameClaim}' does not exist."))
                    .throwAndPrintStackTrace(properties.printStacktrace)
            identityService.saveUser(newUser)
        }

        //Try to unlock user
        identityService.unlockUser(userId)

        //Update groups
        val jwtGroups = when {
            (properties.auth.oauth2.userFederation.loadGroupFromRoles) -> {
                val resourceAccess = jwt.getClaim(properties.auth.oauth2.userFederation.resourceAccessClaim).asMap()
                    ?: throw ApiException.invalidArgument400(arrayListOf("The Claim '${properties.auth.oauth2.userFederation.resourceAccessClaim}' does not exist."))
                        .throwAndPrintStackTrace(properties.printStacktrace)
                try {
                    (resourceAccess[properties.auth.oauth2.userFederation.resourceName] as HashMap<String, List<String?>?>?)?.get("roles")?.filterNotNull()
                }catch(e: Exception){
                    throw ApiException.invalidArgument400(arrayListOf("The Claim '${properties.auth.oauth2.userFederation.resourceAccessClaim}' seems not to follow the standard."))
                        .throwAndPrintStackTrace(properties.printStacktrace)
                }
            }
            (!properties.auth.oauth2.userFederation.loadGroupFromRoles) -> {
                jwt.getClaim(properties.auth.oauth2.userFederation.groupClaim).asList(String::class.java)
                    ?: throw ApiException.invalidArgument400(arrayListOf("The Claim '${properties.auth.oauth2.userFederation.groupClaim}' does not exist."))
                        .throwAndPrintStackTrace(properties.printStacktrace)
            }
            else -> arrayListOf()
        } ?: arrayListOf()

        val camundaGroups = identityService.createGroupQuery().list().map { it.id }
        val userGroups = identityService.createGroupQuery().groupMember(userId).list().map { it.id }


        val nonExistingGroups = jwtGroups.filterNot { it in camundaGroups }
        if (properties.auth.oauth2.userFederation.printNonExistingGroups && nonExistingGroups.isNotEmpty()) {
            logger.info("Following jwt groups do not exist on camunda:\n${nonExistingGroups.joinToString("\n") { "- $it" }}")
        }

        val groupsToAdd = jwtGroups.filterNot { it in nonExistingGroups }.filterNot { it in userGroups }.distinct()
        var groupsToRemove = userGroups.filterNot { it in jwtGroups }.distinct()

        if(groupsToRemove.contains("camunda-admin") && properties.auth.oauth2.userFederation.dontRevokeCamundaAdminGroup){
            groupsToRemove = groupsToRemove.filter { it != "camunda-admin" }
        }

        groupsToRemove.forEach {
            identityService.deleteMembership(userId, it)
        }

        groupsToAdd.forEach {
            identityService.createMembership(userId, it)
        }

        return true
    }
}
