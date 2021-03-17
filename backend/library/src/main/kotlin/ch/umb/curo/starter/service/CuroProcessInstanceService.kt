package ch.umb.curo.starter.service

import ch.umb.curo.starter.models.FlowToNextResult
import ch.umb.curo.starter.models.request.ProcessStartRequest
import ch.umb.curo.starter.models.response.ProcessStartResponse

interface CuroProcessInstanceService {

    fun startProcess(
        body: ProcessStartRequest,
        returnVariables: Boolean,
        flowToNext: Boolean,
        flowToNextIgnoreAssignee: Boolean?,
        flowToNextTimeOut: Int?
    ): ProcessStartResponse

    fun nextTask(id: String, flowToNextIgnoreAssignee: Boolean?): FlowToNextResult

}
