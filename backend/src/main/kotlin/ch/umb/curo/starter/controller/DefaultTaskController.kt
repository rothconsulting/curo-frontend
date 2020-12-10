package ch.umb.curo.starter.controller

import ch.umb.curo.starter.exception.ApiException
import ch.umb.curo.starter.models.response.CuroTask
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.spin.impl.json.jackson.JacksonJsonNode
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse


@RestController
@ConditionalOnMissingClass
class DefaultTaskController : TaskController {

    @Autowired
    lateinit var taskService: TaskService

    @Autowired
    lateinit var historyService: HistoryService

    @Autowired
    lateinit var runtimeService: RuntimeService

    override fun getTask(id: String, attributes: ArrayList<String>, variables: ArrayList<String>, loadFromHistoric: Boolean): CuroTask {
        val curoTask = if(loadFromHistoric) {
           val task = historyService.createHistoricTaskInstanceQuery().taskId(id).singleResult() ?: throw ApiException.NOT_FOUND_404 //TODO: add message to 404
           CuroTask.fromCamundaHistoricTask(task)
        } else {
           val task = taskService.createTaskQuery().taskId(id).initializeFormKeys().singleResult() ?: throw ApiException.NOT_FOUND_404 //TODO: add message to 404
            CuroTask.fromCamundaTask(task)
        }



        if(attributes.isNotEmpty()) {
            //Filter attributes
            val attrDefinitions = CuroTask::class.java.declaredFields
            attrDefinitions.forEach { field ->
                if(field.name !in attributes && field.name != "Companion"){
                    field.isAccessible = true
                    field.set(curoTask, null)
                }
            }
        }

        if(attributes.contains("variables") || attributes.isEmpty()) {
            curoTask.variables = hashMapOf()
            //Load variables
            var taskVariables = taskService.getVariablesTyped(curoTask.id) ?: throw ApiException.NOT_FOUND_404 //TODO: add message to 404
            //Filter files out

            taskVariables.entries.forEach { variable ->
                if(variables.isEmpty() || variables.contains(variable.key)){
                    if(variable.value is JacksonJsonNode){
                        curoTask.variables!![variable.key] = ObjectMapper().readValue((variable.value as JacksonJsonNode).toString(), JsonNode::class.java)
                    }else{
                        curoTask.variables!![variable.key] = variable.value
                    }
                }
            }
        }

        return curoTask
    }

    override fun getTaskFile(id: String, file: String, response: HttpServletResponse) {
        TODO("Not yet implemented")
    }


}
