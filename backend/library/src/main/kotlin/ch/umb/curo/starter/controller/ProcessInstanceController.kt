package ch.umb.curo.starter.controller

import ch.umb.curo.starter.models.request.ProcessStartRequest
import ch.umb.curo.starter.models.response.ProcessStartResponse
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import io.swagger.annotations.ApiParam
import io.swagger.annotations.Authorization
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

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
            returnVariables: Boolean = false): ProcessStartResponse?

}
