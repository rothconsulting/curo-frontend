package ch.umb.curo.starter.controller

import ch.umb.curo.starter.models.FlowToNextResult
import ch.umb.curo.starter.models.request.AssigneeRequest
import ch.umb.curo.starter.models.response.CompleteTaskResponse
import ch.umb.curo.starter.models.response.CuroFilterResponse
import ch.umb.curo.starter.models.response.CuroTask
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.camunda.bpm.engine.rest.dto.task.TaskQueryDto
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@Tag(name = "task", description = "Curo Task API")
@RequestMapping("/curo-api/tasks")
interface TaskController {

    @Operation(
        summary = "Load list of tasks",
        operationId = "getTasks",
        description = "",
        security = [SecurityRequirement(name = "CuroBasic")]
    )
    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getTasks(
        @Parameter(description = "ID of a task filter", required = true)
        @RequestParam("id", required = true)
        id: String,

        @Parameter(description = "Additional filtering for task filtering", required = false)
        @RequestParam("query", required = false)
        query: String? = "{}",

        @Parameter(
            description = "Define which fields should be returned. If not present, all fields of the filter are returned",
            required = false
        )
        @RequestParam("attributes", required = false, defaultValue = "")
        attributes: ArrayList<String> = arrayListOf(),

        @Parameter(
            description = "Define which variables should be returned. If not present, all variables of the filter are returned. *If all variables are loaded and files are present, these files will be held in-memory for the duration of the call. If you don't want the files in-memory please define all needed variables so only needed files are held in-memory.*",
            required = false
        )
        @RequestParam("variables", required = false, defaultValue = "")
        variables: List<String> = arrayListOf(),

        @Parameter(description = "Define the offset where the paginated list should start", required = false)
        @RequestParam("offset", required = false, defaultValue = "0")
        offset: Int = 0,

        @Parameter(description = "Define the max amount of results list should return", required = false)
        @RequestParam("maxResult", required = false, defaultValue = "20")
        maxResult: Int = 20,

        @Parameter(description = "Defines if additional filter data be included in the response", required = false)
        @RequestParam("includeFilter", required = false, defaultValue = "false")
        includeFilter: Boolean = false,

        response: HttpServletResponse
    ): CuroFilterResponse

    @Operation(
        summary = "Load list of tasks",
        operationId = "getTasksPost",
        description = "",
        security = [SecurityRequirement(name = "CuroBasic")]
    )
    @PostMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getTasksPost(
        @Parameter(description = "ID of a task filter", required = true)
        @RequestParam("id", required = true)
        id: String,

        @Parameter(
            description = "Additional filtering for task filtering",
            required = false,
            schema = Schema(implementation = TaskQueryDto::class)
        )
        @RequestBody
        query: String? = "{}",

        @Parameter(
            description = "Define which fields should be returned. If not present, all fields of the filter are returned",
            required = false
        )
        @RequestParam("attributes", required = false, defaultValue = "")
        attributes: ArrayList<String> = arrayListOf(),

        @Parameter(
            description = "Define which variables should be returned. If not present, all variables of the filter are returned. *If all variables are loaded and files are present, these files will be held in-memory for the duration of the call. If you don't want the files in-memory please define all needed variables so only needed files are held in-memory.*",
            required = false
        )
        @RequestParam("variables", required = false, defaultValue = "")
        variables: List<String> = arrayListOf(),

        @Parameter(description = "Define the offset where the paginated list should start", required = false)
        @RequestParam("offset", required = false, defaultValue = "0")
        offset: Int = 0,

        @Parameter(description = "Define the max amount of results list should return", required = false)
        @RequestParam("maxResult", required = false, defaultValue = "20")
        maxResult: Int = 20,

        @Parameter(description = "Defines if additional filter data be included in the response", required = false)
        @RequestParam("includeFilter", required = false, defaultValue = "false")
        includeFilter: Boolean = false,

        response: HttpServletResponse
    ): CuroFilterResponse

    @Operation(
        summary = "Load information about a single task",
        operationId = "getTask",
        description = "",
        security = [SecurityRequirement(name = "CuroBasic")]
    )
    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getTask(
        @Parameter(description = "ID of task to get information from", required = true)
        @PathVariable("id", required = true)
        id: String,

        @Parameter(
            description = "Define which fields should be returned. If not present, all fields are returned",
            required = false
        )
        @RequestParam("attributes", required = false, defaultValue = "")
        attributes: ArrayList<String> = arrayListOf(),

        @Parameter(
            description = "Define which variables should be returned. If not present, all variables are returned. *If all variables are loaded and files are present, these files will be held in-memory for the duration of the call. If you don't want the files in-memory please define all needed variables so only needed files are held in-memory.*",
            required = false
        )
        @RequestParam("variables", required = false, defaultValue = "")
        variables: List<String> = arrayListOf(),

        @Parameter(description = "Define if the values should be loaded from historic data endpoint", required = false)
        @RequestParam("historic", required = false, defaultValue = "false")
        loadFromHistoric: Boolean = false
    ): CuroTask

    @Operation(
        summary = "Load file from a task",
        operationId = "getTaskFile",
        description = "",
        security = [SecurityRequirement(name = "CuroBasic")]
    )
    @GetMapping(
        "/{id}/file/{file}",
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE]
    )
    fun getTaskFile(
        @Parameter(description = "ID of task to get the file from", required = true)
        @PathVariable("id", required = true)
        id: String,

        @Parameter(description = "Name of the variable which contains the file", required = false)
        @PathVariable("file", required = true)
        file: String
    ): ResponseEntity<ByteArray>

    @Operation(
        summary = "Load zip file from a task, based on provided variables",
        operationId = "getTaskZipFile",
        description = "",
        security = [SecurityRequirement(name = "CuroBasic")]
    )
    @GetMapping(
        "/{id}/zip-files",
        produces = [MediaType.APPLICATION_OCTET_STREAM_VALUE, MediaType.APPLICATION_JSON_VALUE]
    )
    fun getTaskZipFile(
        @Parameter(description = "ID of task to get the file from", required = true)
        @PathVariable("id", required = true)
        id: String,

        @Parameter(description = "Name of the variable which contains the file", required = true)
        @RequestParam("files", required = true)
        files: List<String>? = arrayListOf(),

        @Parameter(description = "Name of the zip file", required = false)
        @RequestParam("name", required = false, defaultValue = "files")
        name: String = "files",

        @Parameter(description = "Should non existing files be ignored ", required = false)
        @RequestParam("ignoreNotExistingFiles", required = false, defaultValue = "false")
        ignoreNotExistingFiles: Boolean = false
    ): ResponseEntity<ByteArray>

    @Operation(
        summary = "Complete the given task.",
        operationId = "completeTask",
        description = "",
        security = [SecurityRequirement(name = "CuroBasic")]
    )
    @PostMapping("/{id}/status", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun completeTask(
        @Parameter(description = "ID of task to complete.", required = true)
        @PathVariable("id", required = true)
        id: String,

        @Parameter(description = "Body including all variables.", required = false)
        @RequestBody(required = false)
        body: HashMap<String, Any?>?,

        @Parameter(description = "Define if variables should be returned on success.", required = false)
        @RequestParam("returnVariables", required = false, defaultValue = "false")
        returnVariables: Boolean = false,

        @Parameter(description = "Define if flowToNext should be returned on success.", required = false)
        @RequestParam("flowToNext", required = false, defaultValue = "false")
        flowToNext: Boolean = false,

        @Parameter(description = "Define if flowToNext should ignore task assignee.", required = false)
        @RequestParam("flowToNextIgnoreAssignee", required = false)
        flowToNextIgnoreAssignee: Boolean? = null,

        @Parameter(description = "Define how long in seconds flowToNext should wait.", required = false)
        @RequestParam("flowToNextTimeOut", required = false)
        flowToNextTimeOut: Int? = null
    ): CompleteTaskResponse

    @Operation(
        summary = "Set assignment of given task",
        operationId = "assignTask",
        description = "",
        security = [SecurityRequirement(name = "CuroBasic")]
    )
    @PutMapping("/{id}/assignee", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun assignTask(
        @Parameter(description = "ID of task to change assignment", required = true)
        @PathVariable("id", required = true)
        id: String,

        @Parameter(description = "Assigment", required = false)
        @RequestBody
        assigneeRequest: AssigneeRequest,

        response: HttpServletResponse
    )

    @Operation(
        summary = "Save variables for the given task",
        operationId = "saveVariables",
        description = "",
        security = [SecurityRequirement(name = "CuroBasic")]
    )
    @PatchMapping("/{id}/variables", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun saveVariables(

        @Parameter(description = "ID of task", required = true)
        @PathVariable("id", required = true)
        id: String,

        @Parameter(description = "Body with variables", required = false)
        @RequestBody
        body: HashMap<String, Any?>,

        response: HttpServletResponse
    )

    @Operation(
        summary = "Get next task",
        operationId = "nextTask",
        description = "",
        security = [SecurityRequirement(name = "CuroBasic")]
    )
    @GetMapping("/{id}/next", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun nextTask(
        @Parameter(description = "ID of a completed task", required = true)
        @PathVariable("id", required = true)
        id: String,

        @Parameter(description = "Define if flowToNext should ignore task assignee.", required = false)
        @RequestParam("flowToNextIgnoreAssignee", required = false)
        flowToNextIgnoreAssignee: Boolean? = null
    ): FlowToNextResult
}
