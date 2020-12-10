package ch.umb.curo.starter.controller

import ch.umb.curo.starter.exception.ApiException
import ch.umb.curo.starter.models.response.CuroTask
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
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
    lateinit var runtimeService: RuntimeService

    override fun getTask(id: String, attributes: ArrayList<String>, variables: ArrayList<String>, loadFromHistoric: Boolean): CuroTask {
        val task = taskService.createTaskQuery().taskId(id).initializeFormKeys().singleResult() ?: throw ApiException.NOT_FOUND_404 //TODO: add message to 404
        val curoTask = CuroTask.fromCamundaTask(task)

        if(attributes.isNotEmpty()) {
            //Filter attributes
            val attrDefinitions = CuroTask::class.java.fields
            attrDefinitions.forEach { field ->
                if(field.name !in attributes && field.name != "Companion"){
                    field.isAccessible = true
                    field.set(curoTask, null)
                }
            }
        }

        if(attributes.contains("variables")) {
            curoTask.variables = hashMapOf()

            //Load variables
            var taskVariables = runtimeService.getVariablesTyped(task.executionId) ?: throw ApiException.NOT_FOUND_404 //TODO: add message to 404

            //Map CamundaVariable to object

            //Filter files out


            if(variables.isEmpty()){
                //Add all
                curoTask.variables!!.putAll(taskVariables)
            } else {
                //Filter variables
                taskVariables.entries.forEach { variable ->
                    if(variables.contains(variable.key)){
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
