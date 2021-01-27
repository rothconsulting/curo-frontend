package ch.umb.curo.starter.controller

import ch.umb.curo.starter.exception.ApiException
import ch.umb.curo.starter.models.FlowToNextResult
import ch.umb.curo.starter.models.request.ProcessStartRequest
import ch.umb.curo.starter.models.response.ProcessStartResponse
import ch.umb.curo.starter.property.CuroProperties
import ch.umb.curo.starter.service.FlowToNextService
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.AuthorizationException
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.rest.util.EngineUtil
import org.camunda.spin.impl.json.jackson.JacksonJsonNode
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.web.bind.annotation.RestController

@RestController
@ConditionalOnMissingClass
class DefaultProcessInstanceController : ProcessInstanceController {

    @Autowired
    lateinit var properties: CuroProperties

    @Autowired
    lateinit var runtimeService: RuntimeService

    @Autowired
    lateinit var flowToNextService: FlowToNextService

    @Autowired
    lateinit var repositoryService: RepositoryService

    private var logger = LoggerFactory.getLogger(this::class.java)!!

    override fun startProcess(body: ProcessStartRequest,
                              returnVariables: Boolean,
                              flowToNext: Boolean,
                              flowToNextIgnoreAssignee: Boolean?,
                              flowToNextTimeOut: Int?): ProcessStartResponse {
        try {
            val newInstance = runtimeService.startProcessInstanceByKey(body.processDefinitionKey, body.businessKey, body.variables)

            val response = ProcessStartResponse()
            response.processInstanceId = newInstance.rootProcessInstanceId
            response.businessKey = newInstance.businessKey
            if (returnVariables) {
                val variablesTyped = runtimeService.getVariablesTyped(newInstance.id)
                val variables: HashMap<String, Any?> = hashMapOf()

                variablesTyped.entries.forEach { variable ->
                    if (variable.value is JacksonJsonNode) {
                        variables[variable.key] = ObjectMapper().readValue((variable.value as JacksonJsonNode).toString(), JsonNode::class.java)
                    } else {
                        variables[variable.key] = variable.value
                    }
                }
                response.variables = variables
            }

            if (flowToNext) {
                val currentUser = EngineUtil.lookupProcessEngine(null).identityService.currentAuthentication
                val assignee = if (!(flowToNextIgnoreAssignee ?: properties.flowToNext.ignoreAssignee)) currentUser.userId else null
                val flowToNextResult = flowToNextService.getNextTask(newInstance.rootProcessInstanceId, assignee, flowToNextTimeOut ?: properties.flowToNext.defaultTimeout)
                response.flowToNext = flowToNextResult.flowToNext
                response.flowToEnd = flowToNextResult.flowToEnd
                response.flowToNextTimeoutExceeded = flowToNextResult.flowToNextTimeoutExceeded
            }

            return response
        } catch (e: ProcessEngineException) {
            throwAndPrintStackTrace(e, ApiException.curoErrorCode(ApiException.CuroErrorCode.PROCESS_DEFINITION_NOT_FOUND))
        } catch (e: AuthorizationException) {
            throwAndPrintStackTrace(e, ApiException.unauthorized403("You are not allowed to start this process"))
        }
    }

    override fun nextTask(id: String, flowToNextIgnoreAssignee: Boolean?): FlowToNextResult {
        val currentUser = EngineUtil.lookupProcessEngine(null).identityService.currentAuthentication
        val assignee = if (!(flowToNextIgnoreAssignee ?: properties.flowToNext.ignoreAssignee)) currentUser.userId else null
        return flowToNextService.searchNextTask(id, assignee)
    }

    private fun throwAndPrintStackTrace(e: Exception?, apiReturn: ApiException): Nothing {
        if(properties.printStacktrace){
            logger.error("API-Exception: ${apiReturn.errorCode} -> ${apiReturn.message}")
            e?.printStackTrace()
        }
        throw apiReturn
    }
}
