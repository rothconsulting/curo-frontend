package ch.umb.curo.starter.controller

import ch.umb.curo.starter.models.FlowToNextResult
import ch.umb.curo.starter.models.request.ProcessStartRequest
import ch.umb.curo.starter.models.response.ProcessStartResponse
import ch.umb.curo.starter.service.CuroProcessInstanceService
import org.springframework.web.bind.annotation.RestController

@RestController
class DefaultProcessInstanceController(val curoProcessInstanceService: CuroProcessInstanceService) :
    ProcessInstanceController {

    override fun startProcess(
        body: ProcessStartRequest,
        returnVariables: Boolean,
        flowToNext: Boolean,
        flowToNextIgnoreAssignee: Boolean?,
        flowToNextTimeOut: Int?
    ): ProcessStartResponse {
        return curoProcessInstanceService.startProcess(
            body,
            returnVariables,
            flowToNext,
            flowToNextIgnoreAssignee,
            flowToNextTimeOut
        )
    }

    override fun nextTask(id: String, flowToNextIgnoreAssignee: Boolean?): FlowToNextResult {
        return curoProcessInstanceService.nextTask(id, flowToNextIgnoreAssignee)
    }

}
