package ch.umb.curo.starter.controller

import ch.umb.curo.starter.exception.ApiException
import ch.umb.curo.starter.models.response.CuroTask
import com.google.gson.Gson
import com.google.gson.GsonBuilder
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

    override fun getTask(id: String, attributes: ArrayList<String>, variables: ArrayList<String>, loadFromHistoric: Boolean, response: HttpServletResponse): CuroTask {
        val task = taskService.createTaskQuery().taskId(id).singleResult() ?: throw ApiException.NOT_FOUND_404
        val curoTask = CuroTask.fromCamundaTask(task)

        if(attributes.isNotEmpty()) {
            //Filter attributes
            val attrDefinitions = CuroTask::class.java.fields
            attrDefinitions.forEach { field ->
                if(field.name !in attributes){
                    field.isAccessible = true
                    field.set(curoTask, null)
                }
            }
        }

        return curoTask
    }

    override fun getTaskFile(id: String, file: String, response: HttpServletResponse) {
        TODO("Not yet implemented")
    }
}
