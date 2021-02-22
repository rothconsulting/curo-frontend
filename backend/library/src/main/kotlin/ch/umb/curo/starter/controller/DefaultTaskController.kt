package ch.umb.curo.starter.controller

import ch.umb.curo.starter.auth.CamundaAuthUtil.runWithoutAuthentication
import ch.umb.curo.starter.exception.ApiException
import ch.umb.curo.starter.models.FlowToNextResult
import ch.umb.curo.starter.models.request.AssigneeRequest
import ch.umb.curo.starter.models.response.CompleteTaskResponse
import ch.umb.curo.starter.models.response.CuroFilterResponse
import ch.umb.curo.starter.models.response.CuroTask
import ch.umb.curo.starter.property.CuroProperties
import ch.umb.curo.starter.service.CuroTaskService
import ch.umb.curo.starter.service.FlowToNextService
import ch.umb.curo.starter.util.ZipUtil
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import org.apache.commons.io.IOUtils
import org.camunda.bpm.engine.AuthorizationException
import org.camunda.bpm.engine.EntityTypes
import org.camunda.bpm.engine.FilterService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.rest.dto.task.TaskDto
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto
import org.camunda.bpm.engine.rest.sub.runtime.impl.FilterResourceImpl
import org.camunda.bpm.engine.rest.util.EngineUtil
import org.camunda.bpm.engine.variable.type.ValueType
import org.camunda.bpm.engine.variable.value.FileValue
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.spin.impl.json.jackson.JacksonJsonNode
import org.camunda.spin.plugin.variable.value.impl.JsonValueImpl
import org.slf4j.LoggerFactory
import org.springframework.beans.BeanUtils
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.http.*
import org.springframework.web.bind.annotation.RestController
import java.util.concurrent.Callable
import javax.servlet.http.HttpServletResponse


@RestController
@ConditionalOnMissingClass
class DefaultTaskController(private val properties: CuroProperties,
                            private val curoTaskService: CuroTaskService,
                            private val taskService: TaskService,
                            private val flowToNextService: FlowToNextService,
                            private val filterService: FilterService,
                            private val objectMapper: ObjectMapper) : TaskController {

    override fun getTasks(id: String,
                          query: String?,
                          attributes: ArrayList<String>,
                          variables: List<String>,
                          offset: Int,
                          maxResult: Int,
                          includeFilter: Boolean,
                          response: HttpServletResponse): CuroFilterResponse {
        val filter = filterService.getFilter(id) ?: throw ApiException.notFound404("Filter not found")

        if (filter.resourceType !in arrayListOf(EntityTypes.TASK, EntityTypes.HISTORIC_TASK)) {
            throw ApiException.invalidArgument400(arrayListOf("Filter is not of type TASK or HISTORIC_TASK"))
        }

        if (query != null && query.isNotEmpty()) {
            try {
                objectMapper.readValue(query, TaskQueryDto::class.java)
            } catch (e: Exception) {
                throw ApiException.invalidArgument400(arrayListOf("query attribute is not deserializable into TaskQueryDto."))
            }
        }

        val description = filter.properties.getOrDefault("description", "") as String
        val refresh = filter.properties.getOrDefault("refresh", false) as Boolean
        val properties = filter.properties.filterNot { it.key in arrayListOf("description", "refresh") }

        val variablesToInclude = if ((attributes.contains("variables") || attributes.isEmpty()) && variables.isEmpty()) {
            if (attributes.isNotEmpty()) {
                attributes.add("variables")
            }

            if (!(filter.properties.getOrDefault("showUndefinedVariable", false) as Boolean)) {
                @Suppress("UNCHECKED_CAST")
                val filterVariables = filter.properties["variables"] as List<HashMap<String, String>?>?
                filterVariables?.mapNotNull { it?.getOrDefault("name", null) } ?: variables
            } else {
                arrayListOf()
            }
        } else {
            variables
        }

        val camundaFilterImpl = FilterResourceImpl(null, objectMapper, id, "/")

        //TODO: Support for HISTORIC_TASK
        val count = camundaFilterImpl.queryCount(query ?: "{}")?.count ?: 0

        if (offset >= count) {
            return if (includeFilter) {
                CuroFilterResponse(filter.name, description, refresh, properties, 0, arrayListOf())
            } else {
                CuroFilterResponse(total = 0, items = arrayListOf())
            }
        }

        val result = camundaFilterImpl.queryJsonList(query ?: "{}", offset, maxResult).map { CuroTask.fromCamundaTaskDto(it as TaskDto) }

        result.map { curoTask ->
            if (attributes.contains("variables") || attributes.isEmpty()) {
                curoTask.variables = hashMapOf()
                curoTaskService.loadVariables(variablesToInclude, curoTask)
            }
        }

        if (attributes.isNotEmpty()) {
            //Filter attributes
            result.map { curoTask ->
                val attrDefinitions = CuroTask::class.java.declaredFields
                attrDefinitions.forEach { field ->
                    if (field.name !in attributes && field.name != "Companion") {
                        field.isAccessible = true
                        field.set(curoTask, null)
                    }
                }
            }
        }

        return if (includeFilter) {
            CuroFilterResponse(filter.name, description, refresh, properties, count, result)
        } else {
            CuroFilterResponse(total = count, items = result)
        }
    }

    override fun getTasksPost(id: String,
                              query: String?,
                              attributes: ArrayList<String>,
                              variables: List<String>,
                              offset: Int,
                              maxResult: Int,
                              includeFilter: Boolean,
                              response: HttpServletResponse): CuroFilterResponse {
        return getTasks(id, query, attributes, variables, offset, maxResult, includeFilter, response)
    }

    override fun getTask(id: String, attributes: ArrayList<String>, variables: List<String>, loadFromHistoric: Boolean): CuroTask {
        val curoTask = if (loadFromHistoric) {
            val task = curoTaskService.getHistoricTask(id)
            CuroTask.fromCamundaHistoricTask(task)
        } else {
            val task = curoTaskService.getTask(id)
            CuroTask.fromCamundaTask(task)
        }

        if (attributes.contains("variables") || attributes.isEmpty()) {
            curoTask.variables = hashMapOf()
            //Load variables
            curoTaskService.loadVariables(variables, curoTask, loadFromHistoric)
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

    override fun getTaskFile(id: String, file: String): ResponseEntity<ByteArray> {
        val task = getTask(id)
        val variable = taskService.getVariableTyped<TypedValue>(task.id, file) ?: throw ApiException.curoErrorCode(ApiException.CuroErrorCode.VARIABLE_NOT_FOUND)
            .throwAndPrintStackTrace(properties.printStacktrace)
        if (variable.type != ValueType.FILE) {
            throw ApiException.curoErrorCode(ApiException.CuroErrorCode.VARIABLE_IS_NO_FILE).throwAndPrintStackTrace(properties.printStacktrace)
        }
        variable as FileValue

        val headers = HttpHeaders()
        headers.contentType = MediaType.parseMediaType((variable.mimeType ?: MediaType.APPLICATION_OCTET_STREAM_VALUE) + "; charset=${variable.encoding ?: "utf-8"}")
        headers.contentDisposition = ContentDisposition.parse("""attachment; filename="${variable.filename}"""")

        return ResponseEntity(IOUtils.toByteArray(variable.value), headers, HttpStatus.OK)
    }

    override fun getTaskZipFile(id: String, files: List<String>?, name: String, ignoreNotExistingFiles: Boolean): ResponseEntity<ByteArray> {
        val task = getTask(id)

        if(files == null || files.isEmpty()){
            throw ApiException.invalidArgument400(arrayListOf("zip files has to include at least one file.")).throwAndPrintStackTrace(properties.printStacktrace)
        }

        val filesToZip = arrayListOf<Pair<String, ByteArray>>()

        files.forEach { file ->
            val variable = taskService.getVariableTyped<TypedValue>(task.id, file)
                ?: if (ignoreNotExistingFiles) {
                    return@forEach
                } else {
                    throw ApiException.curoErrorCode(ApiException.CuroErrorCode.VARIABLE_NOT_FOUND).throwAndPrintStackTrace(properties.printStacktrace)
                }
            if (variable.type != ValueType.FILE) {
                throw ApiException.curoErrorCode(ApiException.CuroErrorCode.VARIABLE_IS_NO_FILE).throwAndPrintStackTrace(properties.printStacktrace)
            }
            variable as FileValue
            filesToZip.add(Pair(variable.filename, IOUtils.toByteArray(variable.value)))
        }

        val zip = ZipUtil.zipFiles(filesToZip)

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_OCTET_STREAM
        headers.contentDisposition = ContentDisposition.parse("""attachment; filename="$name.zip"""")

        return ResponseEntity(zip, headers, HttpStatus.OK)
    }

    override fun completeTask(id: String,
                              body: HashMap<String, Any?>?,
                              returnVariables: Boolean,
                              flowToNext: Boolean,
                              flowToNextIgnoreAssignee: Boolean?,
                              flowToNextTimeOut: Int?): CompleteTaskResponse {
        val task = curoTaskService.getTask(id)
        //Check if user is assignee
        val engine = EngineUtil.lookupProcessEngine(null)
        val currentUser = engine.identityService.currentAuthentication
        if (task.assignee != currentUser.userId) {
            throw ApiException.curoErrorCode(ApiException.CuroErrorCode.NEEDS_SAME_ASSIGNEE).throwAndPrintStackTrace(properties.printStacktrace)
        }

        //Save variables
        if (body != null) {
            val taskVariables = taskService.getVariablesTyped(task.id)
            val objectVariables = taskVariables.filter { it.value != null && !BeanUtils.isSimpleValueType(it.value::class.java) }
            val objectVariablesNames = objectVariables.map { it.key }

            body.entries.forEach { entry ->
                if (entry.key in objectVariablesNames) {
                    try {
                        if (taskVariables[entry.key]!! is JacksonJsonNode) {
                            taskService.setVariable(task.id, entry.key, JsonValueImpl(ObjectMapper().writeValueAsString(entry.value), "application/json"))
                        } else {
                            val obj = ObjectMapper().convertValue(entry.value, taskVariables[entry.key]!!::class.java)
                            taskService.setVariable(task.id, entry.key, obj)
                        }
                    } catch (e: InvalidDefinitionException) {
                        if (properties.ignoreObjectType) {
                            taskService.setVariable(task.id, entry.key, JsonValueImpl(ObjectMapper().writeValueAsString(entry.value), "application/json"))
                        } else {
                            throw ApiException.curoErrorCode(ApiException.CuroErrorCode.CANT_SAVE_IN_EXISTING_OBJECT).throwAndPrintStackTrace(properties.printStacktrace, e)
                        }
                    } catch (e: UnrecognizedPropertyException) {
                        if (properties.ignoreObjectType) {
                            taskService.setVariable(task.id, entry.key, JsonValueImpl(ObjectMapper().writeValueAsString(entry.value), "application/json"))
                        } else {
                            throw ApiException.curoErrorCode(ApiException.CuroErrorCode.CANT_SAVE_IN_EXISTING_OBJECT).throwAndPrintStackTrace(properties.printStacktrace, e)
                        }
                    } catch (e: Exception) {
                        throw ApiException.curoErrorCode(ApiException.CuroErrorCode.CANT_SAVE_IN_EXISTING_OBJECT).throwAndPrintStackTrace(properties.printStacktrace, e)
                    }
                } else {
                    if (entry.value != null && !BeanUtils.isSimpleValueType(entry.value!!::class.java)) {
                        taskService.setVariable(task.id, entry.key, JsonValueImpl(ObjectMapper().writeValueAsString(entry.value), "application/json"))
                    } else {
                        taskService.setVariable(task.id, entry.key, entry.value)
                    }
                }
            }
        }
        //Complete Task
        taskService.complete(task.id)

        // add variables if needed
        val variables = if (returnVariables) {
            val curoTask = CuroTask.fromCamundaTask(task)
            curoTaskService.loadVariables(arrayListOf(), curoTask, true)
            curoTask.variables
        } else {
            null
        }

        val response = CompleteTaskResponse()

        if (flowToNext) {
            val flowToNextResult = runWithoutAuthentication({ flowToNextService.getNextTask(task, flowToNextIgnoreAssignee ?: properties.flowToNext.ignoreAssignee, flowToNextTimeOut ?: properties.flowToNext.defaultTimeout) })
            response.flowToNext = flowToNextResult.flowToNext
            response.flowToEnd = flowToNextResult.flowToEnd
            response.flowToNextTimeoutExceeded = flowToNextResult.flowToNextTimeoutExceeded
        }

        response.variables = variables
        return response
    }

    override fun assignTask(id: String, assigneeRequest: AssigneeRequest, response: HttpServletResponse) {
        val task = curoTaskService.getTask(id)
        val engine = EngineUtil.lookupProcessEngine(null)
        val currentUser = engine.identityService.currentAuthentication

        val type = when {
            currentUser.userId == assigneeRequest.assignee && (task.assignee ?: "").isBlank() -> AssignmentType.CLAIM
            currentUser.userId == assigneeRequest.assignee && (task.assignee ?: "").isNotBlank() -> AssignmentType.ASSIGN
            currentUser.userId == task.assignee && (assigneeRequest.assignee ?: "").isEmpty() -> AssignmentType.UNCLAIM
            currentUser.userId != task.assignee && (assigneeRequest.assignee ?: "").isEmpty() -> AssignmentType.UNASSIGN
            currentUser.userId != assigneeRequest.assignee -> AssignmentType.ASSIGN
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
        } catch (e: AuthorizationException) {
            ApiException.unauthorized403("User is not allowed to set assignee").throwAndPrintStackTrace(properties.printStacktrace, e)
        }

        response.status = 200
    }

    override fun saveVariables(id: String, body: HashMap<String, Any?>, response: HttpServletResponse) {
        val task = curoTaskService.getTask(id)
        //Check if user is assignee
        val engine = EngineUtil.lookupProcessEngine(null)
        val currentUser = engine.identityService.currentAuthentication
        if (task.assignee != currentUser.userId) {
            throw ApiException.curoErrorCode(ApiException.CuroErrorCode.NEEDS_SAME_ASSIGNEE).throwAndPrintStackTrace(properties.printStacktrace)
        }

        //Save variables
        val taskVariables = taskService.getVariablesTyped(task.id)
        val objectVariables = taskVariables.filter { it.value != null && !BeanUtils.isSimpleValueType(it.value::class.java) }
        val objectVariablesNames = objectVariables.map { it.key }

        body.entries.forEach { entry ->
            if (entry.key in objectVariablesNames) {
                try {
                    if (taskVariables[entry.key]!! is JacksonJsonNode) {
                        taskService.setVariable(task.id, entry.key, JsonValueImpl(ObjectMapper().writeValueAsString(entry.value), "application/json"))
                    } else {
                        val obj = ObjectMapper().convertValue(entry.value, taskVariables[entry.key]!!::class.java)
                        taskService.setVariable(task.id, entry.key, obj)
                    }
                } catch (e: InvalidDefinitionException) {
                    if (properties.ignoreObjectType) {
                        taskService.setVariable(task.id, entry.key, JsonValueImpl(ObjectMapper().writeValueAsString(entry.value), "application/json"))
                    } else {
                        throw ApiException.curoErrorCode(ApiException.CuroErrorCode.CANT_SAVE_IN_EXISTING_OBJECT).throwAndPrintStackTrace(properties.printStacktrace, e)
                    }
                } catch (e: UnrecognizedPropertyException) {
                    if (properties.ignoreObjectType) {
                        taskService.setVariable(task.id, entry.key, JsonValueImpl(ObjectMapper().writeValueAsString(entry.value), "application/json"))
                    } else {
                        throw ApiException.curoErrorCode(ApiException.CuroErrorCode.CANT_SAVE_IN_EXISTING_OBJECT).throwAndPrintStackTrace(properties.printStacktrace, e)
                    }
                } catch (e: Exception) {
                    throw ApiException.curoErrorCode(ApiException.CuroErrorCode.CANT_SAVE_IN_EXISTING_OBJECT).throwAndPrintStackTrace(properties.printStacktrace, e)
                }
            } else {
                if (entry.value != null && !BeanUtils.isSimpleValueType(entry.value!!::class.java)) {
                    try {
                        taskService.setVariable(task.id, entry.key, JsonValueImpl(ObjectMapper().writeValueAsString(entry.value), "application/json"))
                    } catch (e: UnrecognizedPropertyException) {
                        if (properties.ignoreObjectType) {
                            taskService.setVariable(task.id, entry.key, JsonValueImpl(ObjectMapper().writeValueAsString(entry.value), "application/json"))
                        } else {
                            throw ApiException.curoErrorCode(ApiException.CuroErrorCode.CANT_SAVE_IN_EXISTING_OBJECT).throwAndPrintStackTrace(properties.printStacktrace, e)
                        }
                    } catch (e: Exception) {
                        throw ApiException.curoErrorCode(ApiException.CuroErrorCode.CANT_SAVE_IN_EXISTING_OBJECT).throwAndPrintStackTrace(properties.printStacktrace, e)
                    }
                } else {
                    taskService.setVariable(task.id, entry.key, entry.value)
                }
            }
        }

        response.status = HttpStatus.OK.value()
    }

    override fun nextTask(id: String, flowToNextIgnoreAssignee: Boolean?): FlowToNextResult {
        val currentUser = EngineUtil.lookupProcessEngine(null).identityService.currentAuthentication
        val assignee = if (!(flowToNextIgnoreAssignee ?: properties.flowToNext.ignoreAssignee)) currentUser.userId else null
        val task = curoTaskService.getHistoricTask(id)
        return runWithoutAuthentication({ flowToNextService.searchNextTask(task.processInstanceId, assignee) })
    }

    enum class AssignmentType {
        CLAIM, ASSIGN, UNCLAIM, UNASSIGN
    }
}
