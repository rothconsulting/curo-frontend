package ch.umb.curo.starter.service

import ch.umb.curo.starter.auth.CamundaAuthUtil
import ch.umb.curo.starter.exception.ApiException
import ch.umb.curo.starter.models.FlowToNextResult
import ch.umb.curo.starter.models.request.AssigneeRequest
import ch.umb.curo.starter.models.response.CompleteTaskResponse
import ch.umb.curo.starter.models.response.CuroFileVariable
import ch.umb.curo.starter.models.response.CuroFilterResponse
import ch.umb.curo.starter.models.response.CuroTask
import ch.umb.curo.starter.property.CuroProperties
import ch.umb.curo.starter.util.ZipUtil
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.exc.InvalidDefinitionException
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import com.fasterxml.jackson.datatype.joda.JodaModule
import org.apache.commons.io.IOUtils
import org.camunda.bpm.engine.*
import org.camunda.bpm.engine.filter.Filter
import org.camunda.bpm.engine.impl.identity.Authentication
import org.camunda.bpm.engine.rest.dto.task.TaskDto
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto
import org.camunda.bpm.engine.rest.sub.runtime.impl.FilterResourceImpl
import org.camunda.bpm.engine.task.Task
import org.camunda.bpm.engine.variable.VariableMap
import org.camunda.bpm.engine.variable.type.ValueType
import org.camunda.bpm.engine.variable.value.FileValue
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.spin.impl.json.jackson.JacksonJsonNode
import org.camunda.spin.json.SpinJsonException
import org.camunda.spin.plugin.variable.type.JsonValueType
import org.camunda.spin.plugin.variable.value.impl.JsonValueImpl
import org.springframework.beans.BeanUtils
import org.springframework.http.*
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletResponse

@Service
open class DefaultCuroTaskService(
    private val properties: CuroProperties,
    private val taskService: TaskService,
    private val identityService: IdentityService,
    private val historyService: HistoryService,
    private val flowToNextService: FlowToNextService,
    private val filterService: FilterService,
    private val formService: FormService,
    private val objectMapper: ObjectMapper
) : CuroTaskService {

    override fun getTasks(
        id: String,
        query: String?,
        attributes: ArrayList<String>,
        variables: List<String>,
        offset: Int,
        maxResult: Int,
        includeFilter: Boolean
    ): CuroFilterResponse {
        val filter = filterService.getFilter(id) ?: throw ApiException.notFound404("Filter not found")

        checkFilterType(filter)
        checkQuery(query)

        var areVariablesNeeded = attributes.contains("variables") || attributes.isEmpty()
        val isShowUndefinedVariable = (filter.properties.getOrDefault(
            "showUndefinedVariable",
            false
        ) as Boolean)

        val description = filter.properties.getOrDefault("description", "") as String
        val refresh = filter.properties.getOrDefault("refresh", false) as Boolean
        val properties = filter.properties.filterNot { it.key in arrayListOf("description", "refresh") }

        var variablesToInclude = listOf<String>()
        when {
            isShowUndefinedVariable -> {
                listOf<String>()
            }
            areVariablesNeeded  -> {
                @Suppress("UNCHECKED_CAST")
                val filterVariables = filter.properties["variables"] as List<HashMap<String, String>?>?
                variablesToInclude = filterVariables?.mapNotNull { it?.getOrDefault("name", null) } ?: variables
            }
            variables.isNotEmpty() -> {
                variablesToInclude = variables
            }
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

        val result = camundaFilterImpl.queryJsonList(query ?: "{}", offset, maxResult)
            .map { CuroTask.fromCamundaTaskDto(it as TaskDto) }


        if (areVariablesNeeded) {
            result.forEach { curoTask ->
                curoTask.variables = hashMapOf()
                loadVariables(variablesToInclude, curoTask)
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

    private fun checkFilterType(filter: Filter) {
        if (filter.resourceType !in arrayListOf(EntityTypes.TASK, EntityTypes.HISTORIC_TASK)) {
            throw ApiException.invalidArgument400(arrayListOf("Filter is not of type TASK or HISTORIC_TASK"))
        }
    }

    private fun checkQuery(query: String?) {
        if (query != null && query.isNotEmpty()) {
            try {
                objectMapper.readValue(query, TaskQueryDto::class.java)
            } catch (e: Exception) {
                throw ApiException.invalidArgument400(arrayListOf("query attribute is not deserializable into TaskQueryDto."))
            }
        }
    }


    override fun getTask(
        id: String,
        attributes: ArrayList<String>,
        variables: List<String>,
        loadFromHistoric: Boolean
    ): CuroTask {
        val curoTask = if (loadFromHistoric) {
            val task = getHistoricTask(id)
            val formKey = formService.getTaskFormKey(task.processDefinitionId, task.taskDefinitionKey)
            CuroTask.fromCamundaHistoricTask(task, formKey)
        } else {
            val task = getTask(id)
            CuroTask.fromCamundaTask(task)
        }

        if (attributes.contains("variables") || attributes.isEmpty()) {
            curoTask.variables = hashMapOf()
            //Load variables
            loadVariables(variables, curoTask, loadFromHistoric)
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
        val variable = taskService.getVariableTyped<TypedValue>(task.id, file) ?: throw ApiException.curoErrorCode(
            ApiException.CuroErrorCode.VARIABLE_NOT_FOUND
        )
            .printException(properties.printStacktrace)
        if (variable.type != ValueType.FILE) {
            throw ApiException.curoErrorCode(ApiException.CuroErrorCode.VARIABLE_IS_NO_FILE)
                .printException(properties.printStacktrace)
        }
        variable as FileValue

        val headers = HttpHeaders()
        headers.contentType = MediaType.parseMediaType(
            (variable.mimeType
                ?: MediaType.APPLICATION_OCTET_STREAM_VALUE) + "; charset=${variable.encoding ?: "utf-8"}"
        )
        headers.contentDisposition = ContentDisposition.parse("""attachment; filename="${variable.filename}"""")

        return ResponseEntity(IOUtils.toByteArray(variable.value), headers, HttpStatus.OK)
    }

    override fun getTaskZipFile(
        id: String,
        files: List<String>?,
        name: String,
        ignoreNotExistingFiles: Boolean
    ): ResponseEntity<ByteArray> {
        val task = getTask(id)

        if (files == null || files.isEmpty()) {
            throw ApiException.invalidArgument400(arrayListOf("zip files has to include at least one file."))
                .printException(properties.printStacktrace)
        }

        val filesToZip = arrayListOf<Pair<String, ByteArray>>()

        files.forEach { file ->
            val variable = taskService.getVariableTyped<TypedValue>(task.id, file)
                ?: if (ignoreNotExistingFiles) {
                    return@forEach
                } else {
                    throw ApiException.curoErrorCode(ApiException.CuroErrorCode.VARIABLE_NOT_FOUND)
                        .printException(properties.printStacktrace)
                }
            if (variable.type != ValueType.FILE) {
                throw ApiException.curoErrorCode(ApiException.CuroErrorCode.VARIABLE_IS_NO_FILE)
                    .printException(properties.printStacktrace)
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

    override fun completeTask(
        id: String,
        body: HashMap<String, Any?>?,
        returnVariables: Boolean,
        flowToNext: Boolean,
        flowToNextIgnoreAssignee: Boolean?,
        flowToNextTimeOut: Int?
    ): CompleteTaskResponse {
        val task = getTask(id)
        //Check if user is assignee
        val currentUser = identityService.currentAuthentication
        checkForSameAssignee(task, currentUser)

        //Save variables
        if (body != null) {
            val taskVariables = taskService.getVariablesTyped(task.id)
            val objectVariables = taskVariables
                .filterNot { it.value == null }
                .filterNot { BeanUtils.isSimpleValueType(it.value::class.java) }
            val objectVariablesNames = objectVariables.map { it.key }

            body.entries.forEach { entry ->
                saveSingleVariable(entry.key, entry.value, objectVariablesNames, taskVariables, task)
            }
        }
        //Complete Task
        taskService.complete(task.id)

        // add variables if needed
        val variables = if (returnVariables) {
            val curoTask = CuroTask.fromCamundaTask(task)
            loadVariables(arrayListOf(), curoTask, true)
            curoTask.variables
        } else {
            null
        }

        val response = CompleteTaskResponse()

        if (flowToNext) {
            val flowToNextResult = CamundaAuthUtil.runWithoutAuthentication({
                if (flowToNextIgnoreAssignee ?: properties.flowToNext.ignoreAssignee) {
                    flowToNextService.getNextTask(task, null, flowToNextTimeOut ?: properties.flowToNext.defaultTimeout)
                } else {
                    flowToNextService.getNextTask(task, flowToNextTimeOut ?: properties.flowToNext.defaultTimeout)
                }
            }, identityService)
            response.flowToNext = flowToNextResult.flowToNext
            response.flowToEnd = flowToNextResult.flowToEnd
            response.flowToNextTimeoutExceeded = flowToNextResult.flowToNextTimeoutExceeded
        }

        response.variables = variables
        return response
    }

    private fun saveSingleVariable(
        name: String,
        value: Any?,
        objectVariablesNames: List<String>,
        taskVariables: VariableMap,
        task: Task
    ) {
        if (name in objectVariablesNames) {
            try {
                if (taskVariables[name]!! is JacksonJsonNode) {
                    taskService.setVariable(
                        task.id,
                        name,
                        JsonValueImpl(ObjectMapper().writeValueAsString(value), "application/json")
                    )
                } else {
                    val obj = ObjectMapper().convertValue(value, taskVariables[name]!!::class.java)
                    taskService.setVariable(task.id, name, obj)
                }
            } catch (e: Exception) {
                when (e) {
                    is InvalidDefinitionException,
                    is UnrecognizedPropertyException -> {
                        saveIfIgnoreObjectType(task, name, value, e)
                    }
                    else -> {
                        throw ApiException.curoErrorCode(ApiException.CuroErrorCode.CANT_SAVE_IN_EXISTING_OBJECT)
                            .printException(properties.printStacktrace, e)
                    }
                }
            }
        } else {
            if (value != null && !BeanUtils.isSimpleValueType(value::class.java)) {
                try {
                    taskService.setVariable(
                        task.id,
                        name,
                        JsonValueImpl(ObjectMapper().writeValueAsString(value), "application/json")
                    )
                } catch (e: UnrecognizedPropertyException) {
                    saveIfIgnoreObjectType(task, name, value, e)
                } catch (e: Exception) {
                    throw ApiException.curoErrorCode(ApiException.CuroErrorCode.CANT_SAVE_IN_EXISTING_OBJECT)
                        .printException(properties.printStacktrace, e)
                }
            } else {
                taskService.setVariable(task.id, name, value)
            }
        }
    }

    private fun saveIfIgnoreObjectType(
        task: Task,
        name: String,
        value: Any?,
        e: java.lang.Exception
    ) {
        if (properties.ignoreObjectType) {
            taskService.setVariable(
                task.id,
                name,
                JsonValueImpl(ObjectMapper().writeValueAsString(value), "application/json")
            )
        } else {
            throw ApiException.curoErrorCode(ApiException.CuroErrorCode.CANT_SAVE_IN_EXISTING_OBJECT)
                .printException(properties.printStacktrace, e)
        }
    }

    override fun assignTask(id: String, assigneeRequest: AssigneeRequest, response: HttpServletResponse) {
        val task = getTask(id)
        val currentUser = identityService.currentAuthentication

        val type = when {
            currentUser.userId == assigneeRequest.assignee && (task.assignee
                ?: "").isBlank() -> AssignmentType.CLAIM
            currentUser.userId == assigneeRequest.assignee && (task.assignee
                ?: "").isNotBlank() -> AssignmentType.ASSIGN
            currentUser.userId == task.assignee && (assigneeRequest.assignee
                ?: "").isEmpty() -> AssignmentType.UNCLAIM
            currentUser.userId != task.assignee && (assigneeRequest.assignee
                ?: "").isEmpty() -> AssignmentType.UNASSIGN
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
            ApiException.unauthorized403("User is not allowed to set assignee")
                .printException(properties.printStacktrace, e)
        }

        response.status = HttpStatus.OK.value()
    }

    override fun saveVariables(id: String, body: HashMap<String, Any?>, response: HttpServletResponse) {
        val task = getTask(id)
        //Check if user is assignee
        val currentUser = identityService.currentAuthentication
        checkForSameAssignee(task, currentUser)

        //Save variables
        val taskVariables = taskService.getVariablesTyped(task.id)
        val objectVariables =
            taskVariables.filter { it.value != null && !BeanUtils.isSimpleValueType(it.value::class.java) }
        val objectVariablesNames = objectVariables.map { it.key }

        body.entries.forEach { entry ->
            saveSingleVariable(entry.key, entry.value, objectVariablesNames, taskVariables, task)
        }

        response.status = HttpStatus.OK.value()
    }

    private fun checkForSameAssignee(
        task: Task,
        currentUser: Authentication
    ) {
        if (task.assignee != currentUser.userId) {
            throw ApiException.curoErrorCode(ApiException.CuroErrorCode.NEEDS_SAME_ASSIGNEE)
                .printException(properties.printStacktrace)
        }
    }

    override fun nextTask(id: String, flowToNextIgnoreAssignee: Boolean?): FlowToNextResult {
        val currentUser = identityService.currentAuthentication
        val assignee =
            if (!(flowToNextIgnoreAssignee ?: properties.flowToNext.ignoreAssignee)) currentUser.userId else null
        val task = getHistoricTask(id)
        return CamundaAuthUtil.runWithoutAuthentication({
            flowToNextService.searchNextTask(
                task.processInstanceId,
                assignee
            )
        }, identityService)
    }

    /**
     * Get task and trow ApiException if the task does not exist
     *
     * @param id id of the task
     * @return task
     * @throws ApiException if task not found
     */
    @Throws(ApiException::class)
    private fun getTask(id: String) = (taskService.createTaskQuery().taskId(id).initializeFormKeys().singleResult()
        ?: throw ApiException.curoErrorCode(ApiException.CuroErrorCode.TASK_NOT_FOUND)
            .printException(properties.printStacktrace))

    /**
     * Get historic task and trow ApiException if the task does not exist
     *
     * @param id id of the historic task
     * @return historic task
     * @throws ApiException if task not found
     */
    @Throws(ApiException::class)
    private fun getHistoricTask(id: String) =
        (historyService.createHistoricTaskInstanceQuery().taskId(id).singleResult()
            ?: throw ApiException.curoErrorCode(ApiException.CuroErrorCode.TASK_NOT_FOUND)
                .printException(properties.printStacktrace))

    /**
     * Load variables for the given task.
     * Files are not added to the task object.
     */
    private fun loadVariables(variables: List<String>, curoTask: CuroTask, fromHistoric: Boolean = false) {
        curoTask.variables = curoTask.variables ?: hashMapOf()

        if (!fromHistoric) {
            try {
                val taskVariables = if (variables.isEmpty()) {
                    taskService.getVariablesTyped(curoTask.id)
                } else {
                    taskService.getVariablesTyped(curoTask.id, variables, true)
                }

                taskVariables.entries.forEach { variable ->
                    val valueInfo = taskVariables.getValueTyped<TypedValue>(variable.key)
                    convertAndAddVariable(valueInfo, curoTask, variable.key, variable.value)
                }
            } catch (e: Exception) {
                if (e.cause is SpinJsonException) {
                    throw ApiException.curoErrorCode(ApiException.CuroErrorCode.VARIABLE_IS_NOT_SERIALIZABLE)
                }
            }
        } else {
            try {
                val taskVariables = historyService
                    .createHistoricVariableInstanceQuery()
                    .processInstanceId(curoTask.processInstanceId)
                    .list()

                taskVariables.filter { variables.isEmpty() || variables.contains(it.name) }.forEach { variable ->
                    convertAndAddVariable(variable.typedValue, curoTask, variable.name, variable.value)
                }
            } catch (e: NullPointerException) {
                throw ApiException.curoErrorCode(ApiException.CuroErrorCode.VARIABLE_IS_NOT_SERIALIZABLE)
            }
        }
    }

    private fun convertAndAddVariable(
        valueInfo: TypedValue,
        curoTask: CuroTask,
        name: String,
        value: Any?
    ) {
        when (valueInfo.type) {
            ValueType.FILE -> curoTask.variables!![name] =
                CuroFileVariable.fromFileValue(valueInfo as FileValue)
            JsonValueType.JSON -> curoTask.variables!![name] =
                ObjectMapper().registerModule(JodaModule())
                    .readValue((value as JacksonJsonNode).toString(), JsonNode::class.java)
            else -> curoTask.variables!![name] = value
        }
    }

    private enum class AssignmentType {
        CLAIM, ASSIGN, UNCLAIM, UNASSIGN
    }

}
