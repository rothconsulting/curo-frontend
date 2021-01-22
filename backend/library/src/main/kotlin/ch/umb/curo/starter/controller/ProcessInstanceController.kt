package ch.umb.curo.starter.controller

import ch.umb.curo.starter.models.FlowToNextResult
import ch.umb.curo.starter.models.request.ProcessStartRequest
import ch.umb.curo.starter.models.response.ProcessStartResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@Tag(name = "process-instance", description = "Curo Process Instance API")
@RequestMapping("/curo-api/process-instances")
interface ProcessInstanceController {

    @Operation(summary = "Start new process instance", operationId = "startProcess", description = "", security = [SecurityRequirement(name = "CuroBasic")])
    @PostMapping("", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun startProcess(
        @Parameter(description = "Process instance start model", required = false)
        @RequestBody
        body: ProcessStartRequest,

        @Parameter(description = "Define if variables should be returned on success", required = false)
        @RequestParam("returnVariables", required = false, defaultValue = "false")
        returnVariables: Boolean = false,

        @Parameter(description = "Define if flowToNext should be returned on success.", required = false)
        @RequestParam("flowToNext", required = false, defaultValue = "false")
        flowToNext: Boolean = false,

        @Parameter(description = "Define if flowToNext should ignore the first task assignee.", required = false)
        @RequestParam("flowToNextIgnoreAssignee", required = false, defaultValue = "false")
        flowToNextIgnoreAssignee: Boolean = false,

        @Parameter(description = "Define how long in seconds flowToNext should wait.", required = false)
        @RequestParam("flowToNextTimeOut", required = false, defaultValue = "30")
        flowToNextTimeOut: Int = 30): ProcessStartResponse?

    @Operation(summary = "Get next task", operationId = "nextTask", description = "", security = [SecurityRequirement(name = "CuroBasic")])
    @GetMapping("/{id}/next", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun nextTask(
        @Parameter(description = "ID of the process instance", required = true)
        @PathVariable("id", required = true)
        id: String,

        @Parameter(description = "Define if flowToNext should ignore task assignee.", required = false)
        @RequestParam("flowToNextIgnoreAssignee", required = false, defaultValue = "false")
        flowToNextIgnoreAssignee: Boolean = false): FlowToNextResult
}
