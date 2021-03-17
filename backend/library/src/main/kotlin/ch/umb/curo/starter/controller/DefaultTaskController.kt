package ch.umb.curo.starter.controller

import ch.umb.curo.starter.models.FlowToNextResult
import ch.umb.curo.starter.models.request.AssigneeRequest
import ch.umb.curo.starter.models.response.CompleteTaskResponse
import ch.umb.curo.starter.models.response.CuroFilterResponse
import ch.umb.curo.starter.models.response.CuroTask
import ch.umb.curo.starter.service.CuroTaskService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse


@RestController
class DefaultTaskController(private val curoTaskService: CuroTaskService) : TaskController {

    override fun getTasks(
        id: String,
        query: String?,
        attributes: ArrayList<String>,
        variables: List<String>,
        offset: Int,
        maxResult: Int,
        includeFilter: Boolean
    ): CuroFilterResponse {
        return curoTaskService.getTasks(id, query, attributes, variables, offset, maxResult, includeFilter)
    }

    override fun getTasksPost(
        id: String,
        query: String?,
        attributes: ArrayList<String>,
        variables: List<String>,
        offset: Int,
        maxResult: Int,
        includeFilter: Boolean
    ): CuroFilterResponse {
        return curoTaskService.getTasks(id, query, attributes, variables, offset, maxResult, includeFilter)
    }

    override fun getTask(
        id: String,
        attributes: ArrayList<String>,
        variables: List<String>,
        loadFromHistoric: Boolean
    ): CuroTask {
        return curoTaskService.getTask(id, attributes, variables, loadFromHistoric)
    }

    override fun getTaskFile(id: String, file: String): ResponseEntity<ByteArray> {
        return curoTaskService.getTaskFile(id, file)
    }

    override fun getTaskZipFile(
        id: String,
        files: List<String>?,
        name: String,
        ignoreNotExistingFiles: Boolean
    ): ResponseEntity<ByteArray> {
        return curoTaskService.getTaskZipFile(id, files, name, ignoreNotExistingFiles)
    }

    override fun completeTask(
        id: String,
        body: HashMap<String, Any?>?,
        returnVariables: Boolean,
        flowToNext: Boolean,
        flowToNextIgnoreAssignee: Boolean?,
        flowToNextTimeOut: Int?
    ): CompleteTaskResponse {
        return curoTaskService.completeTask(
            id,
            body,
            returnVariables,
            flowToNext,
            flowToNextIgnoreAssignee,
            flowToNextTimeOut
        )
    }

    override fun assignTask(id: String, assigneeRequest: AssigneeRequest, response: HttpServletResponse) {
        curoTaskService.assignTask(id, assigneeRequest, response)
    }

    override fun saveVariables(id: String, body: HashMap<String, Any?>, response: HttpServletResponse) {
        curoTaskService.saveVariables(id, body, response)
    }

    override fun nextTask(id: String, flowToNextIgnoreAssignee: Boolean?): FlowToNextResult {
        return curoTaskService.nextTask(id, flowToNextIgnoreAssignee)
    }
}
