package ch.umb.curo.starter.controller

import ch.umb.curo.starter.models.FlowToNextResult
import ch.umb.curo.starter.models.request.AssigneeRequest
import ch.umb.curo.starter.models.response.CompleteTaskResponse
import ch.umb.curo.starter.models.response.CuroTask
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@Tag(name = "task", description = "Curo Task API")
@RequestMapping("/curo-api/tasks")
interface TaskController {

    @Operation(summary = "Load information about a single task",
                  operationId = "getTask",
                  description = "",
                  security = [SecurityRequirement(name = "CuroBasic")])
    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getTask(
        @Parameter(description = "ID of task to get information from", required = true)
        @PathVariable("id", required = true)
        id: String,

        @Parameter(description = "Define which fields should be returned. If not present, all fields are returned", required = false)
        @RequestParam("attributes", required = false, defaultValue = "")
        attributes: ArrayList<String> = arrayListOf(),

        @Parameter(description = "Define which variables should be returned. If not present, all variables are returned", required = false)
        @RequestParam("variables", required = false, defaultValue = "")
        variables: ArrayList<String> = arrayListOf(),

        @Parameter(description = "Define if the values should be loaded from historic data endpoint", required = false)
        @RequestParam("historic", required = false, defaultValue = "false")
        loadFromHistoric: Boolean = false): CuroTask

    @Operation(summary = "Load file from a task", operationId = "getTaskFile", description = "")
    @GetMapping("/{id}/file/{file}", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun getTaskFile(
        @Parameter(description = "ID of task to get the file from", required = true)
        @PathVariable("id", required = true)
        id: String,

        @Parameter(description = "Name of the variable which contains the file", required = false)
        @PathVariable("file", required = true)
        file: String,

        response: HttpServletResponse)

    @Operation(summary = "Complete the given task.", operationId = "completeTask", description = "", security = [SecurityRequirement(name = "CuroBasic")])
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
        flowToNextTimeOut: Int? = null): CompleteTaskResponse

    @Operation(summary = "Set assignment of given task", operationId = "assignTask", description = "", security = [SecurityRequirement(name = "CuroBasic")])
    @PutMapping("/{id}/assignee", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun assignTask(
        @Parameter(description = "ID of task to change assignment", required = true)
        @PathVariable("id", required = true)
        id: String,

        @Parameter(description = "Assigment", required = false)
        @RequestBody
        assigneeRequest: AssigneeRequest,

        response: HttpServletResponse)

    @Operation(summary = "Save variables for the given task", operationId = "saveVariables", description = "", security = [SecurityRequirement(name = "CuroBasic")])
    @PatchMapping("/{id}/variables", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun saveVariables(

        @Parameter(description = "ID of task", required = true)
        @PathVariable("id", required = true)
        id: String,

        @Parameter(description = "Body with variables", required = false)
        @RequestBody
        body: HashMap<String, Any?>,

        response: HttpServletResponse)

    @Operation(summary = "Get next task", operationId = "nextTask", description = "", security = [SecurityRequirement(name = "CuroBasic")])
    @GetMapping("/{id}/next", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun nextTask(
        @Parameter(description = "ID of a completed task", required = true)
        @PathVariable("id", required = true)
        id: String,

        @Parameter(description = "Define if flowToNext should ignore task assignee.", required = false)
        @RequestParam("flowToNextIgnoreAssignee", required = false)
        flowToNextIgnoreAssignee: Boolean? = null): FlowToNextResult
}
