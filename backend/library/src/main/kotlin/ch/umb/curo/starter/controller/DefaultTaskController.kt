package ch.umb.curo.starter.controller

import ch.umb.curo.starter.exception.ApiException
import ch.umb.curo.starter.models.request.AssigneeRequest
import ch.umb.curo.starter.models.response.CompleteTaskResponse
import ch.umb.curo.starter.models.response.CuroTask
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import org.camunda.bpm.engine.AuthorizationException
import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.rest.util.EngineUtil
import org.camunda.spin.impl.json.jackson.JacksonJsonNode
import org.springframework.beans.BeanUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType


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
        val curoTask = if (loadFromHistoric) {
            val task = historyService.createHistoricTaskInstanceQuery().taskId(id).singleResult() ?: throw ApiException.notFound404("Task not found in history")
            CuroTask.fromCamundaHistoricTask(task)
        } else {
            val task = taskService.createTaskQuery().taskId(id).initializeFormKeys().singleResult() ?: throw ApiException.notFound404("Task not found")
            CuroTask.fromCamundaTask(task)
        }

        if (attributes.contains("variables") || attributes.isEmpty()) {
            curoTask.variables = hashMapOf()
            //Load variables
            if (loadFromHistoric) {
                val taskVariables = historyService.createHistoricVariableInstanceQuery().processInstanceId(curoTask.processInstanceId).list()

                taskVariables.forEach { variable ->
                    if (variables.isEmpty() || variables.contains(variable.name)) {
                        if (variable.value is JacksonJsonNode) {
                            curoTask.variables!![variable.name] = ObjectMapper().readValue((variable.value as JacksonJsonNode).toString(), JsonNode::class.java)
                        } else {
                            curoTask.variables!![variable.name] = variable.value
                        }
                    }
                }
            } else {
                val taskVariables = taskService.getVariablesTyped(curoTask.id)
                //Filter files out

                taskVariables.entries.forEach { variable ->
                    if (variables.isEmpty() || variables.contains(variable.key)) {
                        if (variable.value is JacksonJsonNode) {
                            curoTask.variables!![variable.key] = ObjectMapper().readValue((variable.value as JacksonJsonNode).toString(), JsonNode::class.java)
                        } else {
                            curoTask.variables!![variable.key] = variable.value
                        }
                    }
                }
            }
        }

        if (attributes.isNotEmpty()) {
            //Filter attributes
            val attrDefinitions = CuroTask::class.java.declaredFields
            attrDefinitions.forEach { field ->
                if (field.name !in attributes && field.name != "Companion") {
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

    override fun completeTask(id: String, body: HashMap<String, Any>, returnVariables: Boolean, flowToNext: Boolean): CompleteTaskResponse {
        val task = taskService.createTaskQuery().taskId(id).initializeFormKeys().singleResult() ?: throw ApiException.curoErrorCode(ApiException.CuroErrorCode.TASK_NOT_FOUND)
        //Check if user is assignee
        val engine = EngineUtil.lookupProcessEngine(null)
        val currentUser = engine.identityService.currentAuthentication
        if(task.assignee != currentUser.userId){
            throw ApiException.curoErrorCode(ApiException.CuroErrorCode.COMPLETE_NEEDS_SAME_ASSIGNEE)
        }

        //Save variables
        val taskVariables = taskService.getVariablesTyped(task.id)
        val objectVariables = taskVariables.filter { !BeanUtils.isSimpleValueType(it.value::class.java) }
        val objectVariablesNames = objectVariables.map { it.key }

        body.entries.forEach { entry ->
            if(entry.key in objectVariablesNames){
                try {
                    val obj = ObjectMapper().convertValue(entry.value, taskVariables[entry.key]!!::class.java)
                    taskService.setVariable(task.id, entry.key, obj)
                }catch (e: InvalidDefinitionException){
                    taskService.setVariable(task.id, entry.key, ObjectMapper().writeValueAsString(entry.value))
                } catch (e: UnrecognizedPropertyException){
                    throw ApiException.curoErrorCode(ApiException.CuroErrorCode.CANT_SAVE_IN_EXISTING_OBJECT)
                }
            }else{
                if(!BeanUtils.isSimpleValueType(entry.value::class.java)){
                    taskService.setVariable(task.id, entry.key, ObjectMapper().writeValueAsString(entry.value))
                }else{
                    taskService.setVariable(task.id, entry.key, entry.value)
                }
            }
        }

        //Complete Task
        taskService.complete(task.id)


        // add variables if needed
        val variables = if(returnVariables){
            val variablesTyped = taskService.getVariablesTyped(task.id)
            val variables: HashMap<String, Any?> = hashMapOf()

            variablesTyped.entries.forEach { variable ->
                    if (variable.value is JacksonJsonNode) {
                        variables[variable.key] = ObjectMapper().readValue((variable.value as JacksonJsonNode).toString(), JsonNode::class.java)
                    } else {
                        variables[variable.key] = variable.value
                    }
            }

            variables
        } else {
            null
        }

        //TODO: add flowToNext

        val response = CompleteTaskResponse()
        response.variables = variables

        return response
    }

    override fun assignTask(id: String, assigneeRequest: AssigneeRequest, response: HttpServletResponse) {
        val task = taskService.createTaskQuery().taskId(id).initializeFormKeys().singleResult() ?: throw ApiException.curoErrorCode(ApiException.CuroErrorCode.TASK_NOT_FOUND)
        val engine = EngineUtil.lookupProcessEngine(null)
        val currentUser = engine.identityService.currentAuthentication

        val type = when {
            currentUser.userId == assigneeRequest.assignee && task.assignee?.isBlank() == true -> AssignmentType.CLAIM
            currentUser.userId == assigneeRequest.assignee && task.assignee?.isNotBlank() == true -> AssignmentType.ASSIGN
            currentUser.userId != assigneeRequest.assignee -> AssignmentType.ASSIGN
            currentUser.userId == task.assignee && assigneeRequest.assignee?.isEmpty() == true -> AssignmentType.UNCLAIM
            currentUser.userId != task.assignee && assigneeRequest.assignee?.isEmpty() == true -> AssignmentType.UNASSIGN
            else -> AssignmentType.ASSIGN
        }

        try {
            when (type) {
                AssignmentType.CLAIM -> {
                    taskService.claim(task.id, assigneeRequest.assignee)
                }
                AssignmentType.ASSIGN -> {
                    taskService.setAssignee(task.id, assigneeRequest.assignee)
                }
                AssignmentType.UNCLAIM -> {
                    taskService.claim(task.id, null)
                }
                AssignmentType.UNASSIGN -> {
                    taskService.setAssignee(task.id, null)
                }
            }
        }catch(e: AuthorizationException) {
            throw ApiException.unauthorized403("User is not allowed to set assignee")
        }

        response.status = 200
    }


    enum class AssignmentType {
        CLAIM, ASSIGN, UNCLAIM, UNASSIGN
    }
}
