package ch.umb.curo.starter.events

import ch.umb.curo.starter.property.CuroProperties
import ch.umb.curo.starter.service.StartupDataCreationService
import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.EntityTypes
import org.camunda.bpm.engine.FilterService
import org.camunda.bpm.engine.ManagementService
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.exception.NotValidException
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl
import org.camunda.bpm.engine.rest.dto.runtime.FilterDto
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Service

@Service
class PostDeployEventListener(
    private val properties: CuroProperties,
    private val context: ConfigurableApplicationContext,
    private val startupDataCreationService: StartupDataCreationService,
    private val processEngine: ProcessEngine,
    private val filterService: FilterService,
    private val objectMapper: ObjectMapper,
) : ApplicationListener<ApplicationStartedEvent> {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun onApplicationEvent(event: ApplicationStartedEvent) {

        checkAuthorization(processEngine)
        setTelemetry(processEngine)
        setDefaultSerializationFormat(processEngine)

        //Set patterns
        setCamundaUserIdPattern(processEngine)
        setCamundaGroupIdPattern(processEngine)

        //Create groups
        startupDataCreationService.createInitialGroups(processEngine)

        //Create users
        startupDataCreationService.createInitialUsers(processEngine)

        //Create filter
        createFilters(processEngine)
    }


    private fun setTelemetry(engine: ProcessEngine) {
        if (properties.camundaTelemetry != null) {
            logger.info("CURO: Set camunda telemetry to: ${properties.camundaTelemetry}")
            val managementService: ManagementService = engine.managementService
            managementService.toggleTelemetry(properties.camundaTelemetry!!)
        }
    }

    private fun checkAuthorization(engine: ProcessEngine) {
        if (!engine.processEngineConfiguration.isAuthorizationEnabled) {
            logger.warn("CURO: ⚠️ Authorization is not enabled! ⚠️")
        }

        if (!engine.processEngineConfiguration.isAuthorizationEnabled) {
            logger.info("CURO: Authorization for custom code is not enabled!")
        }
    }

    private fun setDefaultSerializationFormat(engine: ProcessEngine) {
        val defaultSerializationFormat = context.environment.getProperty("camunda.bpm.default-serialization-format")
            ?: (engine.processEngineConfiguration as ProcessEngineConfigurationImpl).defaultSerializationFormat
        when {
            (defaultSerializationFormat == "application/json") -> {
                logger.debug("CURO: Default serialization format is already set to 'application/json'")
            }
            (properties.setDefaultSerializationFormat && defaultSerializationFormat != "application/json") -> {
                (engine.processEngineConfiguration as ProcessEngineConfigurationImpl).defaultSerializationFormat =
                    "application/json"
                logger.info("CURO: Set default serialization format to 'application/json'")
            }
            (!properties.setDefaultSerializationFormat && defaultSerializationFormat != "application/json") -> {
                logger.warn("CURO: ⚠️ Default serialization format is set to '$defaultSerializationFormat' which is not supported by Curo ⚠️")
            }
        }
    }

    private fun createFilters(processEngine: ProcessEngine) {
        if (!properties.createInitialFilters) {
            logger.debug("CURO: Skip initial filter creation")
            return
        }
        val filterPath = properties.initialFilterLocation
        val files = context.getResources(filterPath)
        if (files.isNotEmpty()) {
            logger.info("CURO: Creating initial filters based on files in '$filterPath'")
            files.forEach {
                val filterObj = try {
                    objectMapper.readValue(it.file, FilterDto::class.java)
                } catch (e: Exception) {
                    logger.warn("CURO: Filter file '${it.file.path}' is not a valid FilterEntity json")
                    return@forEach
                }
                try {
                    val resourceType = filterObj.resourceType
                    val name = filterObj.name

                    if (filterService.createFilterQuery().filterResourceType(resourceType).filterName(name)
                            .count() > 0
                    ) {
                        logger.debug("CURO: Filter '${it.file.path}' already exists (name: $name, type: $resourceType)")
                        return@forEach
                    }

                    val filter = if (EntityTypes.TASK == resourceType) {
                        filterService.newTaskFilter()
                    } else {
                        logger.warn("CURO: Filter file '${it.file.path}' has invalid resource type '$resourceType'")
                        return@forEach
                    }

                    try {
                        filterObj.updateFilter(filter, processEngine)
                    } catch (e: NotValidException) {
                        logger.warn("CURO: Filter file '${it.file.path}' has invalid content")
                        return@forEach
                    }

                    filterService.saveFilter(filter)
                } catch (e: Exception) {
                    logger.warn("CURO: Filter file '${it.file.path}' got rejected by Camunda")
                    return@forEach
                }

                logger.debug("CURO: Create initial filter '${it.file.path}'")
            }

        }
    }

    private fun setCamundaUserIdPattern(engine: ProcessEngine) {
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

    private fun setCamundaGroupIdPattern(engine: ProcessEngine) {
        if (properties.camundaGroupIdPattern != null) {
            logger.info("CURO: Set groupResourceWhitelistPattern to: ${properties.camundaGroupIdPattern}")
            engine.processEngineConfiguration.groupResourceWhitelistPattern = properties.camundaGroupIdPattern
        }
    }
}
