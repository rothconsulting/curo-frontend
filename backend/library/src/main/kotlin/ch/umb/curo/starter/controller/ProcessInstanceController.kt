package ch.umb.curo.starter.controller

import ch.umb.curo.starter.models.FlowToNextResult
import ch.umb.curo.starter.models.request.ProcessStartRequest
import ch.umb.curo.starter.models.response.ProcessStartResponse
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.Authorization
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@Api(value = "process-instance", description = "Curo Process Instance API")
@RequestMapping("/curo-api/process-instances")
interface ProcessInstanceController {

    @ApiOperation(value = "Start new process instance", nickname = "startProcess", notes = "", tags = ["process-instance"], authorizations = [Authorization("CuroBasic")])
    @PostMapping("", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun startProcess(
        @ApiParam(value = "Process instance start model", required = false)
        @RequestBody
        body: ProcessStartRequest,

        @ApiParam(value = "Define if variables should be returned on success", required = false)
        @RequestParam("returnVariables", required = false, defaultValue = "false")
        returnVariables: Boolean = false,

        @ApiParam(value = "Define if flowToNext should be returned on success.", required = false)
        @RequestParam("flowToNext", required = false, defaultValue = "false")
        flowToNext: Boolean = false,

        @ApiParam(value = "Define if flowToNext should ignore the first task assignee.", required = false)
        @RequestParam("flowToNextIgnoreAssignee", required = false, defaultValue = "false")
        flowToNextIgnoreAssignee: Boolean = false,

        @ApiParam(value = "Define how long in seconds flowToNext should wait.", required = false)
        @RequestParam("flowToNextTimeOut", required = false, defaultValue = "30")
        flowToNextTimeOut: Int = 30): ProcessStartResponse?

    @ApiOperation(value = "Get next task", nickname = "nextTask", notes = "", tags = ["task"], authorizations = [Authorization("CuroBasic")])
    @PatchMapping("/{id}/next", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun nextTask(
        @ApiParam(value = "ID of the process instance", required = true)
        @PathVariable("id", required = true)
        id: String,

        @ApiParam(value = "Define if flowToNext should ignore task assignee.", required = false)
        @RequestParam("flowToNextIgnoreAssignee", required = false, defaultValue = "false")
        flowToNextIgnoreAssignee: Boolean = false): FlowToNextResult
}
