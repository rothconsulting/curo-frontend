package ch.umb.curo.starter.service

import ch.umb.curo.starter.exception.ApiException
import ch.umb.curo.starter.models.response.CuroFileVariable
import ch.umb.curo.starter.models.response.CuroTask
import ch.umb.curo.starter.property.CuroProperties
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.joda.JodaModule
import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.variable.type.ValueType
import org.camunda.bpm.engine.variable.value.FileValue
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.spin.impl.json.jackson.JacksonJsonNode
import org.camunda.spin.plugin.variable.type.JsonValueType
import org.springframework.stereotype.Service

@Service
open class CuroTaskService(private val properties: CuroProperties,
                           private val taskService: TaskService,
                           private val historyService: HistoryService) {

    /**
     * Get task and trow ApiException if the task does not exist
     *
     * @param id id of the task
     * @return task
     * @throws ApiException if task not found
     */
    @Throws(ApiException::class)
    fun getTask(id: String) = (taskService.createTaskQuery().taskId(id).initializeFormKeys().singleResult()
        ?: throw ApiException.curoErrorCode(ApiException.CuroErrorCode.TASK_NOT_FOUND).throwAndPrintStackTrace(properties.printStacktrace))

    /**
     * Get historic task and trow ApiException if the task does not exist
     *
     * @param id id of the historic task
     * @return historic task
     * @throws ApiException if task not found
     */
    @Throws(ApiException::class)
    fun getHistoricTask(id: String) = (historyService.createHistoricTaskInstanceQuery().taskId(id).singleResult()
        ?: throw ApiException.curoErrorCode(ApiException.CuroErrorCode.TASK_NOT_FOUND).throwAndPrintStackTrace(properties.printStacktrace))

    /**
     * Load variables for the given task.
     * Files are not added to the task object.
     */
    fun loadVariables(variables: List<String>, curoTask: CuroTask, fromHistoric: Boolean = false) {
        if (curoTask.variables == null) {
            curoTask.variables = hashMapOf()
        }

        if (!fromHistoric) {
            val taskVariables = if (variables.isEmpty()) {
                taskService.getVariablesTyped(curoTask.id)
            } else {
                taskService.getVariablesTyped(curoTask.id, variables, true)
            }

            taskVariables.entries.forEach { variable ->
                val valueInfo = taskVariables.getValueTyped<TypedValue>(variable.key)
                when (valueInfo.type) {
                    ValueType.FILE -> curoTask.variables!![variable.key] = CuroFileVariable.fromFileValue(valueInfo as FileValue)
                    JsonValueType.JSON -> curoTask.variables!![variable.key] =
                        ObjectMapper().registerModule(JodaModule()).readValue((variable.value as JacksonJsonNode).toString(), JsonNode::class.java)
                    else -> curoTask.variables!![variable.key] = variable.value
                }
            }
        } else {
            val taskVariables = historyService.createHistoricVariableInstanceQuery().processInstanceId(curoTask.processInstanceId).list()

            taskVariables.forEach { variable ->
                val valueInfo = variable.typedValue
                if (variables.isEmpty() || variables.contains(variable.name)) {
                    when {
                        valueInfo.type == ValueType.FILE -> curoTask.variables!![variable.name] = CuroFileVariable.fromFileValue(valueInfo as FileValue)
                        valueInfo.type == JsonValueType.JSON -> curoTask.variables!![variable.name] =
                            ObjectMapper().registerModule(JodaModule()).readValue((variable.value as JacksonJsonNode).toString(), JsonNode::class.java)
                        else -> curoTask.variables!![variable.name] = variable.value
                    }
                }
            }
        }
    }


}
