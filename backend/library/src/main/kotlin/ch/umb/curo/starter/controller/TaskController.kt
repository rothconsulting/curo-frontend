package ch.umb.curo.starter.controller

import ch.umb.curo.starter.models.response.CompleteTaskResponse
import ch.umb.curo.starter.models.response.CuroTask
import com.fasterxml.jackson.databind.JsonNode
import io.swagger.annotations.*
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@Api(value = "task", description = "Curo Task API")
@RequestMapping("/curo-api/task")
interface TaskController {

    @ApiOperation(value = "Load information about a single task", nickname = "getTask", notes = "", response = CuroTask::class, tags = ["task"], authorizations = [Authorization("CuroBasic")])
    @GetMapping("/{id}", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getTask(
            @ApiParam(value = "ID of task to get information from", required = true)
            @PathVariable("id", required = true)
            id: String,

            @ApiParam(value = "Define which fields should be returned. If not present, all fields are returned", required = false)
            @RequestParam("attributes", required = false, defaultValue = "")
            attributes: ArrayList<String> = arrayListOf(),

            @ApiParam(value = "Define which variables should be returned. If not present, all variables are returned", required = false)
            @RequestParam("variables", required = false, defaultValue = "")
            variables: ArrayList<String> = arrayListOf(),

            @ApiParam(value = "Define if the values should be loaded from historic data endpoint", required = false)
            @RequestParam("historic", required = false, defaultValue = "false")
            loadFromHistoric: Boolean = false): CuroTask

    @ApiOperation(value = "Load file from a task", nickname = "getTaskFile", notes = "", tags = ["task"])
    @GetMapping("/{id}/file/{file}", produces = [MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_OCTET_STREAM_VALUE])
    fun getTaskFile(
            @ApiParam(value = "ID of task to get the file from", required = true)
            @PathVariable("id", required = true)
            id: String,

            @ApiParam(value = "Name of the variable which contains the file", required = false)
            @PathVariable("file", required = true)
            file: String,

            response: HttpServletResponse)

    @ApiOperation(value = "Complete the given task", nickname = "completeTask", notes = "", tags = ["task"], authorizations = [Authorization("CuroBasic")])
    @PostMapping("/{id}/status", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun completeTask(
            @ApiParam(value = "ID of task to complete", required = true)
            @PathVariable("id", required = true)
            id: String,

            @ApiParam(value = "Body including all variables", required = false)
            body: HashMap<String, Any>,

            @ApiParam(value = "Define if variables should be returned on success", required = false)
            @RequestParam("returnVariables", required = false, defaultValue = "false")
            returnVariables: Boolean = false,

            @ApiParam(value = "Define if flowToNext should be returned on success. ", required = false)
            @RequestParam("flowToNext", required = false, defaultValue = "false")
            flowToNext: Boolean = false): CompleteTaskResponse

    //fun assignTask()
}
