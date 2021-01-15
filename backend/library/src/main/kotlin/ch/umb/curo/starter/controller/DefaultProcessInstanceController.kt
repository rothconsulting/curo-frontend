package ch.umb.curo.starter.controller

import ch.umb.curo.starter.exception.ApiException
import ch.umb.curo.starter.models.request.ProcessStartRequest
import ch.umb.curo.starter.models.response.ProcessStartResponse
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.AuthorizationException
import org.camunda.bpm.engine.ProcessEngineException
import org.camunda.bpm.engine.RepositoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.spin.impl.json.jackson.JacksonJsonNode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.web.bind.annotation.RestController

@RestController
@ConditionalOnMissingClass
class DefaultProcessInstanceController : ProcessInstanceController {

    @Autowired
    lateinit var runtimeService: RuntimeService

    @Autowired
    lateinit var repositoryService: RepositoryService

    override fun startProcess(body: ProcessStartRequest, returnVariables: Boolean): ProcessStartResponse {
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
            return response
        } catch (e: ProcessEngineException) {
            throw ApiException.curoErrorCode(ApiException.CuroErrorCode.PROCESS_DEFINITION_NOT_FOUND)
        } catch (e: AuthorizationException) {
            throw ApiException.unauthorized403("You are not allowed to start this process")
        }
    }
}
