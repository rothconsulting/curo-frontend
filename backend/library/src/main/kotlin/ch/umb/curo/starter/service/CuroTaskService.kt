package ch.umb.curo.starter.service

import ch.umb.curo.starter.models.FlowToNextResult
import ch.umb.curo.starter.models.request.AssigneeRequest
import ch.umb.curo.starter.models.response.CompleteTaskResponse
import ch.umb.curo.starter.models.response.CuroFilterResponse
import ch.umb.curo.starter.models.response.CuroTask
import org.springframework.http.ResponseEntity
import javax.servlet.http.HttpServletResponse

interface CuroTaskService {

    fun getTasks(
        id: String,
        query: String?,
        attributes: ArrayList<String>,
        variables: List<String>,
        offset: Int,
        maxResult: Int,
        includeFilter: Boolean
    ): CuroFilterResponse

    fun getTask(id: String, attributes: ArrayList<String>, variables: List<String>, loadFromHistoric: Boolean): CuroTask

    fun getTaskFile(id: String, file: String): ResponseEntity<ByteArray>

    fun getTaskZipFile(
        id: String,
        files: List<String>?,
        name: String,
        ignoreNotExistingFiles: Boolean
    ): ResponseEntity<ByteArray>

    fun completeTask(
        id: String,
        body: HashMap<String, Any?>?,
        returnVariables: Boolean,
        flowToNext: Boolean,
        flowToNextIgnoreAssignee: Boolean?,
        flowToNextTimeOut: Int?
    ): CompleteTaskResponse

    fun assignTask(id: String, assigneeRequest: AssigneeRequest, response: HttpServletResponse)

    fun saveVariables(id: String, body: HashMap<String, Any?>, response: HttpServletResponse)

    fun nextTask(id: String, flowToNextIgnoreAssignee: Boolean?): FlowToNextResult
}
