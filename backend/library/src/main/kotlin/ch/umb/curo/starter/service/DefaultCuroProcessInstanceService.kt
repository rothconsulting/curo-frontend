package ch.umb.curo.starter.service

import ch.umb.curo.starter.auth.CamundaAuthUtil
import ch.umb.curo.starter.exception.ApiException
import ch.umb.curo.starter.models.FlowToNextResult
import ch.umb.curo.starter.models.request.ProcessStartRequest
import ch.umb.curo.starter.models.response.ProcessStartResponse
import ch.umb.curo.starter.property.CuroProperties
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.*
import org.camunda.spin.impl.json.jackson.JacksonJsonNode

class DefaultCuroProcessInstanceService(
    val properties: CuroProperties,
    val runtimeService: RuntimeService,
    val repositoryService: RepositoryService,
    val flowToNextService: FlowToNextService,
    val identityService: IdentityService
) : CuroProcessInstanceService {
    override fun startProcess(
        body: ProcessStartRequest,
        returnVariables: Boolean,
        flowToNext: Boolean,
        flowToNextIgnoreAssignee: Boolean?,
        flowToNextTimeOut: Int?
    ): ProcessStartResponse {
        try {

            if (repositoryService.createProcessDefinitionQuery().processDefinitionKey(body.processDefinitionKey)
                    .count() == 0L
            ) {
                throw ApiException.curoErrorCode(ApiException.CuroErrorCode.PROCESS_DEFINITION_NOT_FOUND)
                    .throwAndPrintStackTrace(properties.printStacktrace)
            }

            val newInstance =
                runtimeService.startProcessInstanceByKey(body.processDefinitionKey, body.businessKey, body.variables)

            val response = ProcessStartResponse()
            response.processInstanceId = newInstance.rootProcessInstanceId
            response.businessKey = newInstance.businessKey
            if (returnVariables) {
                val variablesTyped = runtimeService.getVariablesTyped(newInstance.id)
                val variables: HashMap<String, Any?> = hashMapOf()

                variablesTyped.entries.forEach { variable ->
                    if (variable.value is JacksonJsonNode) {
                        variables[variable.key] = ObjectMapper().readValue(
                            (variable.value as JacksonJsonNode).toString(),
                            JsonNode::class.java
                        )
                    } else {
                        variables[variable.key] = variable.value
                    }
                }
                response.variables = variables
            }

            if (flowToNext) {
                val currentUser = identityService.currentAuthentication
                val assignee = if (!(flowToNextIgnoreAssignee
                        ?: properties.flowToNext.ignoreAssignee)
                ) currentUser.userId else null
                val flowToNextResult = CamundaAuthUtil.runWithoutAuthentication({
                    flowToNextService.getNextTask(
                        newInstance.rootProcessInstanceId,
                        assignee,
                        flowToNextTimeOut ?: properties.flowToNext.defaultTimeout
                    )
                }, identityService)
                response.flowToNext = flowToNextResult.flowToNext
                response.flowToEnd = flowToNextResult.flowToEnd
                response.flowToNextTimeoutExceeded = flowToNextResult.flowToNextTimeoutExceeded
            }

            return response
        } catch (e: AuthorizationException) {
            throw ApiException.unauthorized403("You are not allowed to start this process")
                .throwAndPrintStackTrace(properties.printStacktrace, e)
        } catch (e: ProcessEngineException) {
            throw ApiException.internal500(e.localizedMessage, e).throwAndPrintStackTrace(properties.printStacktrace, e)
        }
    }

    override fun nextTask(id: String, flowToNextIgnoreAssignee: Boolean?): FlowToNextResult {
        val currentUser = identityService.currentAuthentication
        val assignee =
            if (!(flowToNextIgnoreAssignee ?: properties.flowToNext.ignoreAssignee)) currentUser.userId else null
        return CamundaAuthUtil.runWithoutAuthentication(
            { flowToNextService.searchNextTask(id, assignee) },
            identityService
        )
    }
}
