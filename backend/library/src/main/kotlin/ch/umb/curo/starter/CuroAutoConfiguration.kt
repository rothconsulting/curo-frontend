package ch.umb.curo.starter

import ch.umb.curo.starter.auth.CuroBasicAuthAuthentication
import ch.umb.curo.starter.auth.CuroOAuth2Authentication
import ch.umb.curo.starter.property.CuroProperties
import org.camunda.bpm.engine.ManagementService
import org.camunda.bpm.engine.authorization.Authorization
import org.camunda.bpm.engine.rest.security.auth.AuthenticationProvider
import org.camunda.bpm.engine.rest.util.EngineUtil
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.EventListener

@EnableConfigurationProperties(CuroProperties::class)
@Configuration
@ComponentScan(basePackages = ["ch.umb.curo.starter.*"])
open class CuroAutoConfiguration {

    @Autowired
    lateinit var properties: CuroProperties

    @Autowired
    lateinit var context: ConfigurableApplicationContext

    private val logger = LoggerFactory.getLogger("ch.umb.curo.starter.CuroAutoConfiguration")

    @Bean
    @ConditionalOnMissingBean
    open fun defaultAuthenticationProvider(): AuthenticationProvider {
        return when (properties.auth.type) {
            "basic" -> {
                CuroBasicAuthAuthentication()
            }
            "oauth2" -> {
                CuroOAuth2Authentication()
            }
            else -> CuroBasicAuthAuthentication()
        }
    }

    @EventListener(ApplicationStartedEvent::class)
    fun setTelemetry() {
        if (properties.camundaTelemetry != null) {
            logger.info("CURO: Set camunda telemetry to: ${properties.camundaTelemetry}")
            val engine = EngineUtil.lookupProcessEngine(null)
            val managementService: ManagementService = engine.managementService
            managementService.toggleTelemetry(properties.camundaTelemetry!!)
        }
    }

    @EventListener(ApplicationReadyEvent::class)
    fun checkAuthorization() {
        val engine = EngineUtil.lookupProcessEngine(null)
        if(!engine.processEngineConfiguration.isAuthorizationEnabled){
            logger.warn("CURO: ⚠️ Authorization is not enabled! ⚠️")
        }

        if(!engine.processEngineConfiguration.isAuthorizationEnabled){
            logger.info("CURO: Authorization for custom code is not enabled!")
        }
    }

    @EventListener(ApplicationReadyEvent::class)
    fun setStringContext() {
        SpringContext.applicationContext = context
    }

    @EventListener(ApplicationStartedEvent::class)
    fun processUserAndGroups() {
        //Set patterns
        setCamundaUserIdPattern()
        setCamundaGroupIdPattern()

        //Create groups
        createInitialGroups()

        //Create users
        createInitialUsers()
    }

    private fun setCamundaUserIdPattern() {
        val engine = EngineUtil.lookupProcessEngine(null)
        if (properties.camundaUserIdPattern != null) {
            logger.info("CURO: Set userResourceWhitelistPattern to: ${properties.camundaUserIdPattern}")
            engine.processEngineConfiguration.userResourceWhitelistPattern = properties.camundaUserIdPattern
        }

        //show warning if userIdClaim is email or mail and userResourceWhitelistPattern is default
        val isDefaultPattern = engine.processEngineConfiguration.userResourceWhitelistPattern == null ||
                engine.processEngineConfiguration.userResourceWhitelistPattern == "[a-zA-Z0-9]+|camunda-admin"
        if (properties.auth.type == "oauth2" && properties.auth.oauth2.userIdClaim in arrayListOf(
                "mail",
                "email",
                "preferred_username"
            ) && isDefaultPattern
        ) {
            logger.warn("CURO: email seems to be used as userIdClaim but camundaUserIdPattern is no set. This may result in Curo not be able to authenticate users as the camunda default pattern does not allow email addresses.")
        }
    }

    private fun setCamundaGroupIdPattern() {
        val engine = EngineUtil.lookupProcessEngine(null)
        if (properties.camundaGroupIdPattern != null) {
            logger.info("CURO: Set groupResourceWhitelistPattern to: ${properties.camundaGroupIdPattern}")
            engine.processEngineConfiguration.groupResourceWhitelistPattern = properties.camundaGroupIdPattern
        }
    }

    private fun createInitialGroups() {
        if (properties.initialGroups != null && properties.initialGroups!!.isNotEmpty()) {
            logger.info("CURO: Create initial groups: " + properties.initialGroups!!.joinToString(", ") { it.id })
            val engine = EngineUtil.lookupProcessEngine(null)
            properties.initialGroups!!.forEach { group ->
                if (engine.identityService.createGroupQuery().groupId(group.id).count() == 0L) {
                    val newGroup = engine.identityService.newGroup(group.id)
                    newGroup.name = if (group.name.isNotEmpty()) group.name else group.id
                    if (group.type.isNotEmpty()) newGroup.type = group.type
                    engine.identityService.saveGroup(newGroup)
                    logger.debug("CURO: Group '${group.id}' created")

                    group.permissions.forEach { entry ->
                        val newAuthorization =
                            engine.authorizationService.createNewAuthorization(Authorization.AUTH_TYPE_GRANT)
                        newAuthorization.groupId = newGroup.id
                        newAuthorization.setResource(entry.key)
                        newAuthorization.resourceId = "*"
                        newAuthorization.setPermissions(*entry.value.toTypedArray())
                        engine.authorizationService.saveAuthorization(newAuthorization)
                        logger.debug(
                            "CURO: Add permissions for group '${group.id}': ${entry.key.name} -> ${
                                entry.value.joinToString(
                                    ", "
                                ) { it.name }
                            }"
                        )
                    }

                } else {
                    logger.info("CURO: Group '${group.id}' does already exist")
                }
            }
        }
    }

    private fun createInitialUsers() {
        if (properties.initialUsers != null && properties.initialUsers!!.isNotEmpty()) {
            logger.info("CURO: Create initial users: " + properties.initialUsers!!.joinToString(", ") { it.id })
            val engine = EngineUtil.lookupProcessEngine(null)
            properties.initialUsers!!.forEach { userProperty ->
                if (engine.identityService.createUserQuery().userId(userProperty.id).count() == 0L) {
                    val user = engine.identityService.newUser(userProperty.id)
                    user.email = userProperty.email
                    user.firstName = userProperty.firstname
                    user.lastName = userProperty.lastname
                    if (userProperty.password != null) {
                        user.password = userProperty.password
                    }
                    engine.identityService.saveUser(user)
                    logger.debug("CURO: User '${user.id}' created")
                }

                if (userProperty.groups != null && userProperty.groups!!.isNotEmpty()) {
                    val groups = engine.identityService.createGroupQuery().list().map { it.id }
                    userProperty.groups!!.filter { it in groups }.forEach {
                        //Only add group if user does not have it
                        if (engine.identityService.createGroupQuery().groupMember(userProperty.id).groupId(it).count() == 0L) {
                            engine.identityService.createMembership(userProperty.id, it)
                            logger.debug("CURO: Added user '${userProperty.id}' to group '$it'")
                        }
                    }

                    val nonExistingGroups = userProperty.groups!!.filterNot { it in groups }
                    if(nonExistingGroups.isNotEmpty()){
                        nonExistingGroups.forEach {
                            logger.warn("CURO: Group '$it' does not exist and can therefore not be assigned to the user '${userProperty.id}'")
                        }
                    }
                }
            }
        }
    }
}
