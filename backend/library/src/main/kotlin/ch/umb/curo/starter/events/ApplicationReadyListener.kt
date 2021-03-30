package ch.umb.curo.starter.events

import ch.umb.curo.starter.property.CuroProperties
import ch.umb.curo.starter.service.StartupDataCreationService
import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.EntityTypes
import org.camunda.bpm.engine.ManagementService
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.exception.NotValidException
import org.camunda.bpm.engine.rest.dto.runtime.FilterDto
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationStartedEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.stereotype.Service

@Service
class ApplicationReadyListener(
    private val properties: CuroProperties,
    private val startupDataCreationService: StartupDataCreationService,
    private val processEngine: ProcessEngine,
    private val context: ConfigurableApplicationContext,
    private val objectMapper: ObjectMapper
) : ApplicationListener<ApplicationStartedEvent> {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override fun onApplicationEvent(event: ApplicationStartedEvent) {
        setTelemetry(processEngine)

        //Create groups
        startupDataCreationService.createInitialGroups(processEngine)

        //Create users
        startupDataCreationService.createInitialUsers(processEngine)

        //Create filter
        createFilters(processEngine)
    }

    private fun setTelemetry(processEngine: ProcessEngine) {
        if (properties.camundaTelemetry != null) {
            logger.info("CURO: Set camunda telemetry to: ${properties.camundaTelemetry}")
            val managementService: ManagementService = processEngine.managementService
            managementService.toggleTelemetry(properties.camundaTelemetry!!)
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
            val filterService = processEngine.filterService
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
}
