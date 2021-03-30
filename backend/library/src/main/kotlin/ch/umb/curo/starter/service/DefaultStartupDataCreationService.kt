package ch.umb.curo.starter.service

import ch.umb.curo.starter.property.CuroProperties
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.authorization.Authorization
import org.slf4j.LoggerFactory

class DefaultStartupDataCreationService(val properties: CuroProperties) : StartupDataCreationService {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun createInitialGroups(engine: ProcessEngine) {
        if (properties.initialGroups != null && properties.initialGroups!!.isNotEmpty()) {
            logger.info("CURO: Create initial groups: " + properties.initialGroups!!.joinToString(", ") { it.id })
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
                            "CURO: -> Add permissions for group '${group.id}': ${entry.key.name} -> ${
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

    override fun createInitialUsers(engine: ProcessEngine) {
        if (properties.initialUsers != null && properties.initialUsers!!.isNotEmpty()) {
            logger.info("CURO: Create initial users: " + properties.initialUsers!!.joinToString(", ") { it.id })
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
                        if (engine.identityService.createGroupQuery().groupMember(userProperty.id).groupId(it)
                                .count() == 0L
                        ) {
                            engine.identityService.createMembership(userProperty.id, it)
                            logger.debug("CURO: -> Added user '${userProperty.id}' to group '$it'")
                        }
                    }

                    val nonExistingGroups = userProperty.groups!!.filterNot { it in groups }
                    if (nonExistingGroups.isNotEmpty()) {
                        nonExistingGroups.forEach {
                            logger.warn("CURO: Group '$it' does not exist and can therefore not be assigned to the user '${userProperty.id}'")
                        }
                    }
                }
            }
        }
    }
}
