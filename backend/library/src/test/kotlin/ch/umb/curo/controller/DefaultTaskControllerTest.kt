package ch.umb.curo.controller


import ch.umb.curo.model.DataModel
import ch.umb.curo.starter.models.request.AssigneeRequest
import ch.umb.curo.starter.models.response.CompleteTaskResponse
import ch.umb.curo.starter.util.ZipUtil
import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.FilterService
import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.filter.Filter
import org.camunda.bpm.engine.rest.dto.runtime.FilterDto
import org.camunda.bpm.engine.rest.util.EngineUtil
import org.camunda.bpm.engine.variable.impl.value.FileValueImpl
import org.camunda.bpm.engine.variable.type.ValueType
import org.camunda.spin.impl.json.jackson.JacksonJsonNode
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.*
import java.util.*
import kotlin.collections.LinkedHashMap


@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
class DefaultTaskControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var mapper: ObjectMapper

    @Autowired
    lateinit var taskService: TaskService

    @Autowired
    lateinit var historyService: HistoryService

    @Autowired
    lateinit var runtimeService: RuntimeService

    @Autowired
    lateinit var filterService: FilterService

    @Value("classpath:test-image.jpg")
    lateinit var testImage: Resource

    private val basicLogin: String = Base64.getEncoder().encodeToString("demo:demo".toByteArray())

    @AfterEach
    private fun cleanProcesses() {
        val list = runtimeService.createProcessInstanceQuery().processDefinitionKey("Process_1").list()
        list.forEach {
            runtimeService.suspendProcessInstanceById(it.rootProcessInstanceId)
            runtimeService.deleteProcessInstance(it.rootProcessInstanceId, "ABORT", true, true, true, true)
        }
    }

    @Test
    fun `getTask() - loading task without authorization should not work`() {
        mockMvc.get("/curo-api/tasks/12345") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `getTask() - loading not existing task should result in 404`() {
        mockMvc.get("/curo-api/tasks/12345") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
        }.andExpect {
            status { isNotFound() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `getTask() - loading task with id should work`() {
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1")
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()

        mockMvc.get("/curo-api/tasks/${task.id}") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.id") { value(task.id) }
            jsonPath("$.processInstanceId") { value(newInstance.rootProcessInstanceId) }
            jsonPath("$.status") { value("open") }
        }
    }

    @Test
    fun `getTask() - loading task with id and selected attributes should return only selected attributes`() {
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1")
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()

        mockMvc.get("/curo-api/tasks/${task.id}") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            param("attributes", "name")
            param("attributes", "due")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.id") { doesNotExist() }
            jsonPath("$.name") { value(task.name) }
            jsonPath("$.due") { isNotEmpty() }
            jsonPath("$.variables") { doesNotExist() }
        }
    }

    @Test
    fun `getTask() - loading task with id should return variables by default`() {
        val (variables, data, obj) = getVariables(true)

        val imageFile = (variables["image"] as FileValueImpl)
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1", variables)
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()

        mockMvc.get("/curo-api/tasks/${task.id}") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.id") { value(task.id) }
            jsonPath("$.variables.name") { isString() }
            jsonPath("$.variables.name") { value(variables["name"].toString()) }
            jsonPath("$.variables.isActive") { isBoolean() }
            jsonPath("$.variables.isActive") { value(variables["isActive"] as Boolean) }
            jsonPath("$.variables.age") { isNumber() }
            jsonPath("$.variables.age") { value(variables["age"] as Int) }
            jsonPath("$.variables.data") { isMap() }
            jsonPath("$.variables.data.id") { isString() }
            jsonPath("$.variables.data.id") { value(data["id"].toString()) }
            jsonPath("$.variables.data.name") { isString() }
            jsonPath("$.variables.data.name") { value(data["name"].toString()) }
            jsonPath("$.variables.obj") { isMap() }
            jsonPath("$.variables.obj.id") { isString() }
            jsonPath("$.variables.obj.id") { value(obj.id.toString()) }
            jsonPath("$.variables.obj.name") { isString() }
            jsonPath("$.variables.obj.name") { value(obj.name.toString()) }
            jsonPath("$.variables.obj.usable") { isBoolean() }
            jsonPath("$.variables.obj.usable") { value(obj.usable as Boolean) }
            jsonPath("$.variables.image.fileName") { isString() }
            jsonPath("$.variables.image.fileName") { value(imageFile.filename.toString()) }
            jsonPath("$.variables.image.mimeType") { isString() }
            jsonPath("$.variables.image.mimeType") { value(imageFile.mimeType.toString()) }
            jsonPath("$.variables.image.encoding") { isString() }
            jsonPath("$.variables.image.encoding") { value(imageFile.encoding.toString()) }
        }.andDo { print() }
    }

    @Test
    fun `getTask() - loading task with id and selected variables should return only selected variables`() {
        val (variables, _, obj) = getVariables()

        val newInstance = runtimeService.startProcessInstanceByKey("Process_1", variables)
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()

        mockMvc.get("/curo-api/tasks/${task.id}") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            param("variables", "name")
            param("variables", "age")
            param("variables", "obj")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.id") { value(task.id) }
            jsonPath("$.variables.name") { isString() }
            jsonPath("$.variables.name") { value(variables["name"].toString()) }
            jsonPath("$.variables.age") { isNumber() }
            jsonPath("$.variables.age") { value(variables["age"] as Int) }
            jsonPath("$.variables.isActive") { doesNotExist() }
            jsonPath("$.variables.data") { doesNotExist() }
            jsonPath("$.variables.obj") { isMap() }
            jsonPath("$.variables.obj.id") { isString() }
            jsonPath("$.variables.obj.id") { value(obj.id.toString()) }
        }.andDo { print() }
    }

    @Test
    fun `getTask() - loading historic task with wrong id should result in 404`() {
        mockMvc.get("/curo-api/tasks/12345") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            param("historic", "true")
        }.andExpect {
            status { isNotFound() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `getTask() - loading task with historic id should result in 404`() {
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1")
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        taskService.complete(task.id)

        mockMvc.get("/curo-api/tasks/${task.id}") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            param("historic", "false")
        }.andExpect {
            status { isNotFound() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `getTask() - loading historic task with active id should work`() {
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1")
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()

        mockMvc.get("/curo-api/tasks/${task.id}") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            param("historic", "true")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.id") { value(task.id) }
            jsonPath("$.processInstanceId") { value(newInstance.rootProcessInstanceId) }
            jsonPath("$.status") { value("open") }
        }
    }

    @Test
    fun `getTask() - loading historic task with historic id should work`() {
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1")
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        taskService.complete(task.id)

        mockMvc.get("/curo-api/tasks/${task.id}") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            param("historic", "true")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.id") { value(task.id) }
            jsonPath("$.processInstanceId") { value(newInstance.rootProcessInstanceId) }
            jsonPath("$.status") { value("completed") }
        }
    }

    @Test
    fun `getTask() - loading historic task with historic id and selected variables should return only selected variables`() {
        val (variables, _, obj) = getVariables()

        val newInstance = runtimeService.startProcessInstanceByKey("Process_1", variables)
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        taskService.complete(task.id)

        mockMvc.get("/curo-api/tasks/${task.id}") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            param("variables", "name")
            param("variables", "age")
            param("variables", "obj")
            param("historic", "true")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.status") { value("completed") }
            jsonPath("$.id") { value(task.id) }
            jsonPath("$.variables.name") { isString() }
            jsonPath("$.variables.name") { value(variables["name"].toString()) }
            jsonPath("$.variables.age") { isNumber() }
            jsonPath("$.variables.age") { value(variables["age"] as Int) }
            jsonPath("$.variables.isActive") { doesNotExist() }
            jsonPath("$.variables.data") { doesNotExist() }
            jsonPath("$.variables.obj") { isMap() }
            jsonPath("$.variables.obj.id") { isString() }
            jsonPath("$.variables.obj.id") { value(obj.id.toString()) }
        }.andDo { print() }
    }

    @Test
    fun `completeTask() - complete task without authorization should not work`() {
        mockMvc.post("/curo-api/tasks/12345/status") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `completeTask() - complete not existing task should result in 404`() {
        mockMvc.post("/curo-api/tasks/12345/status") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isNotFound() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `completeTask() - complete task with wrong wrong user should not work`() {
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1")
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()

        mockMvc.post("/curo-api/tasks/${task.id}/status") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isForbidden() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }


    @Test
    fun `completeTask() - complete task should work`() {
        val (variables) = getVariables()
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1", variables)
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        taskService.setAssignee(task.id, "demo")

        variables["name"] = "CHANGED"

        mockMvc.post("/curo-api/tasks/${task.id}/status") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(variables)
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }

        //Check if completed
        val historicTask = historyService.createHistoricTaskInstanceQuery().taskId(task.id).singleResult()
        assert(historicTask?.deleteReason == "completed")

        val taskVariables = historyService.createHistoricVariableInstanceQuery().processInstanceId(newInstance.processInstanceId).list()
        assert(taskVariables.firstOrNull { it.name == "name" }?.value == "CHANGED")
    }

    @Test
    fun `completeTask() - complete task with return variables should work`() {
        val (variables, data, obj) = getVariables()
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1", variables)
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        taskService.setAssignee(task.id, "demo")

        mockMvc.post("/curo-api/tasks/${task.id}/status") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            param("returnVariables", "true")
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(variables)
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.variables.name") { isString() }
            jsonPath("$.variables.name") { value(variables["name"].toString()) }
            jsonPath("$.variables.isActive") { isBoolean() }
            jsonPath("$.variables.isActive") { value(variables["isActive"] as Boolean) }
            jsonPath("$.variables.age") { isNumber() }
            jsonPath("$.variables.age") { value(variables["age"] as Int) }
            jsonPath("$.variables.data") { isMap() }
            jsonPath("$.variables.data.id") { isString() }
            jsonPath("$.variables.data.id") { value(data["id"].toString()) }
            jsonPath("$.variables.data.name") { isString() }
            jsonPath("$.variables.data.name") { value(data["name"].toString()) }
            jsonPath("$.variables.obj") { isMap() }
            jsonPath("$.variables.obj.id") { isString() }
            jsonPath("$.variables.obj.id") { value(obj.id.toString()) }
            jsonPath("$.variables.obj.name") { isString() }
            jsonPath("$.variables.obj.name") { value(obj.name.toString()) }
            jsonPath("$.variables.obj.usable") { isBoolean() }
            jsonPath("$.variables.obj.usable") { value(obj.usable as Boolean) }
        }

        //Check if completed
        val historicTask = historyService.createHistoricTaskInstanceQuery().taskId(task.id).singleResult()
        assert(historicTask?.deleteReason == "completed")
    }

    @Test
    fun `completeTask() - complete task with flowToNext should work`() {
        val (variables) = getVariables()
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1", variables)
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        taskService.setAssignee(task.id, "demo")

        variables["name"] = "CHANGED"

        val result = mockMvc.post("/curo-api/tasks/${task.id}/status") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(variables)
            param("flowToNext", "true")
            param("flowToNextIgnoreAssignee", "true")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.flowToNext") { isArray() }
            jsonPath("$.flowToNext") { isNotEmpty() }
            jsonPath("$.flowToEnd") { isBoolean() }
            jsonPath("$.flowToEnd") { value(false) }
        }.andReturn()

        //Check if completed
        val historicTask = historyService.createHistoricTaskInstanceQuery().taskId(task.id).singleResult()
        assert(historicTask?.deleteReason == "completed")

        val response = mapper.readValue(result.response.contentAsString, CompleteTaskResponse::class.java)
        val nextTask = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        assert(response.flowToNext?.firstOrNull() == nextTask.id)
    }

    @Test
    fun `completeTask() - complete task with flowToEnd should work`() {
        val (variables) = getVariables()
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1", variables)
        var task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        taskService.setAssignee(task.id, "demo")
        taskService.complete(task.id)

        task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        taskService.setAssignee(task.id, "demo")

        variables["name"] = "CHANGED"

        val result = mockMvc.post("/curo-api/tasks/${task.id}/status") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(variables)
            param("flowToNext", "true")
            param("flowToNextIgnoreAssignee", "true")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.flowToNext") { isArray() }
            jsonPath("$.flowToNext") { isEmpty() }
            jsonPath("$.flowToEnd") { isBoolean() }
            jsonPath("$.flowToEnd") { value(true) }
        }.andReturn()

        //Check if completed
        val historicTask = historyService.createHistoricTaskInstanceQuery().taskId(task.id).singleResult()
        assert(historicTask?.deleteReason == "completed")
    }

    @Test
    fun `assignTask() - assign task without authorization should not work`() {
        mockMvc.put("/curo-api/tasks/12345/assignee") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `assignTask() - assign not existing task should result in 404`() {
        val assigneeRequest = AssigneeRequest()
        assigneeRequest.assignee = "demo"

        mockMvc.put("/curo-api/tasks/12345/assignee") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(assigneeRequest)
        }.andExpect {
            status { isNotFound() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `assignTask() - claim task should work`() { //currentUser.userId == assigneeRequest.assignee && (task.assignee ?: "").isBlank()
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1")
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()

        val assigneeRequest = AssigneeRequest()
        assigneeRequest.assignee = "demo"

        mockMvc.put("/curo-api/tasks/${task.id}/assignee") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(assigneeRequest)
        }.andExpect {
            status { isOk() }
        }

        val updatedTask = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        assert((updatedTask?.assignee ?: "") == "demo")

        //Check if correct method was used
        //val lastOperation = historyService.createUserOperationLogQuery().taskId(task.id).singleResult()
        //assert(lastOperation?.operationType == "Claim")
    }

    @Test
    fun `assignTask() - assign task should work`() { //currentUser.userId == assigneeRequest.assignee && (task.assignee ?: "").isNotBlank()
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1")
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        taskService.claim(task.id, "user")

        val assigneeRequest = AssigneeRequest()
        assigneeRequest.assignee = "demo"

        mockMvc.put("/curo-api/tasks/${task.id}/assignee") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(assigneeRequest)
        }.andExpect {
            status { isOk() }
        }

        val updatedTask = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        assert((updatedTask?.assignee ?: "") == "demo")

        //Check if correct method was used
        //val lastOperation = historyService.createUserOperationLogQuery().taskId(task.id).singleResult()
        //assert(lastOperation?.operationType == "Assign")
    }

    @Test
    fun `assignTask() - assign task should work (different user)`() { //currentUser.userId != assigneeRequest.assignee
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1")
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        taskService.claim(task.id, "demo")

        val assigneeRequest = AssigneeRequest()
        assigneeRequest.assignee = "user"

        mockMvc.put("/curo-api/tasks/${task.id}/assignee") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(assigneeRequest)
        }.andExpect {
            status { isOk() }
        }

        val updatedTask = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        assert((updatedTask?.assignee ?: "") == "user")

        //Check if correct method was used
        //val lastOperation = historyService.createUserOperationLogQuery().taskId(task.id).singleResult()
        //assert(lastOperation?.operationType == "Assign")
    }

    @Test
    fun `assignTask() - unclaim task should work`() { //currentUser.userId == task.assignee && (assigneeRequest.assignee ?: "").isEmpty()
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1")
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        taskService.claim(task.id, "demo")

        val assigneeRequest = AssigneeRequest()
        assigneeRequest.assignee = ""

        mockMvc.put("/curo-api/tasks/${task.id}/assignee") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(assigneeRequest)
        }.andExpect {
            status { isOk() }
        }

        val updatedTask = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        assert((updatedTask?.assignee ?: "") == "")

        //Check if correct method was used
        //val lastOperation = historyService.createUserOperationLogQuery().taskId(task.id).singleResult()
        //assert(lastOperation?.operationType == "Claim")
    }

    @Test
    fun `assignTask() - unassign task should work`() { //currentUser.userId != task.assignee && (assigneeRequest.assignee ?: "").isEmpty()
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1")
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        taskService.claim(task.id, "user")

        val assigneeRequest = AssigneeRequest()
        assigneeRequest.assignee = ""

        mockMvc.put("/curo-api/tasks/${task.id}/assignee") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(assigneeRequest)
        }.andExpect {
            status { isOk() }
        }

        val updatedTask = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        assert((updatedTask?.assignee ?: "") == "")

        //Check if correct method was used
        //val lastOperation = historyService.createUserOperationLogQuery().taskId(task.id).singleResult()
        //assert(lastOperation?.operationType == "Assign")
    }

    @Test
    fun `saveVariables() - save variables without authorization should not work`() {
        mockMvc.patch("/curo-api/tasks/12345/variables") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `saveVariables() - save variables for not existing task should result in 404`() {
        val (variables) = getVariables()

        mockMvc.patch("/curo-api/tasks/12345/variables") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(variables)
        }.andExpect {
            status { isNotFound() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `saveVariables() - save variables for task with different assignee should result in 403`() {
        val (variables) = getVariables()
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1")
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        taskService.claim(task.id, "user")

        mockMvc.patch("/curo-api/tasks/${task.id}/variables") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(variables)
        }.andExpect {
            status { isForbidden() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `saveVariables() - save variables should work`() {
        val (variables, data) = getVariables()
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1", variables)
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        taskService.claim(task.id, "demo")

        variables["name"] = "UMB AG"
        data["name"] = "NEW NAME"
        variables["data"] = data

        mockMvc.patch("/curo-api/tasks/${task.id}/variables") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(variables)
        }.andExpect {
            status { isOk() }
        }

        val taskVariables = taskService.getVariablesTyped(task.id)
        assert((taskVariables["name"] ?: "") == "UMB AG")
        assert((taskVariables["data"] as LinkedHashMap<*, *>)["name"] == "NEW NAME")
    }

    @Test
    fun `saveVariables() - save complex variables which do not exist before`() {
        val (variables) = getVariables()
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1")
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        taskService.claim(task.id, "demo")

        variables["name"] = "UMB AG"
        val newData = DataModel()
        newData.name = "NEW_DATA"
        variables["newData"] = newData

        mockMvc.patch("/curo-api/tasks/${task.id}/variables") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(variables)
        }.andExpect {
            status { isOk() }
        }

        val taskVariables = taskService.getVariablesTyped(task.id)
        assert((taskVariables["name"] ?: "") == "UMB AG")
        assert((taskVariables["newData"] as JacksonJsonNode).prop("name").stringValue() == "NEW_DATA")
    }

    @Test
    fun `nextTask() - poll for next task`() {
        val (variables) = getVariables()
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1", variables)
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        taskService.complete(task.id)

        val nextTask = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()

        mockMvc.get("/curo-api/tasks/${task.id}/next") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            param("flowToNextIgnoreAssignee", "true")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.flowToNext") { isArray() }
            jsonPath("$.flowToNext") { isNotEmpty() }
            jsonPath("$.flowToNext.[0]") { value(nextTask.id) }
            jsonPath("$.flowToEnd") { isBoolean() }
            jsonPath("$.flowToEnd") { value(false) }
        }
    }

    @Test
    fun `getTasks() - loading tasks without authorization should not work`() {
        mockMvc.get("/curo-api/tasks") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `getTasks() - loading tasks should work`() {
        val (variables) = getVariables()

        val filter = createFilter()

        val newInstance1 = runtimeService.startProcessInstanceByKey("Process_1", variables)
        val newInstance2 = runtimeService.startProcessInstanceByKey("Process_1", variables)

        variables["name"] = "UMB"
        val newInstance3 = runtimeService.startProcessInstanceByKey("Process_1", variables) // Will no be found by the filter

        mockMvc.get("/curo-api/tasks") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            param("id", filter.id)
        }.andExpect {
            status { isOk() }
            jsonPath("$.total") { isNumber() }
            jsonPath("$.total") { value(2) }
            jsonPath("$.items") { isArray() }
            jsonPath("$.items") { isNotEmpty() }
        }
    }

    @Test
    fun `getTasks() - loading tasks with filter data should work`() {
        val filter = createFilter()

        mockMvc.get("/curo-api/tasks") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            param("id", filter.id)
            param("includeFilter", "true")
        }.andExpect {
            status { isOk() }
            jsonPath("$.name") { value("Big Filter") }
            jsonPath("$.description") { value("My Tasks") }
            jsonPath("$.refresh") { value("false") }
            jsonPath("$.properties[\"color\"]") { value("#3e4d2f") }
        }

    }

    @Test
    fun `getTasks() - loading tasks with additional filter should work`() {
        val (variables) = getVariables()

        val filter = createFilter()

        runtimeService.startProcessInstanceByKey("Process_1", variables)

        variables["age"] = 100
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1", variables)

        val nextTask = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()

        mockMvc.get("/curo-api/tasks") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            param("id", filter.id)
            param("query", """{"processVariables": [
                                                {
                                                  "operator": "eq",
                                                  "value": 100,
                                                  "name": "age"
                                                }
                                              ]
                                            }""")
        }.andExpect {
            status { isOk() }
            jsonPath("$.total") { isNumber() }
            jsonPath("$.total") { value(1) }
            jsonPath("$.items") { isArray() }
            jsonPath("$.items") { isNotEmpty() }
            jsonPath("$.items.[0].id") { value(nextTask.id) }
        }
    }

    @Test
    fun `getTaskFile() - loading file without authorization should not work`() {
        mockMvc.get("/curo-api/tasks/12345/file/awesomeFile") {
            accept = MediaType.APPLICATION_OCTET_STREAM
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `getTaskFile() - loading file with wrong task id should result in 404`() {
        mockMvc.get("/curo-api/tasks/12345/file/awesomeFile") {
            accept = MediaType.APPLICATION_OCTET_STREAM
            header("Authorization", "CuroBasic $basicLogin")
        }.andExpect {
            status { isNotFound() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `getTaskFile() - loading file with wrong variable name should result in 404`() {
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1")
        val nextTask = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()

        mockMvc.get("/curo-api/tasks/${nextTask.id}/file/awesomeFile") {
            accept = MediaType.APPLICATION_OCTET_STREAM
            header("Authorization", "CuroBasic $basicLogin")
        }.andExpect {
            status { isNotFound() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `getTaskFile() - loading file should work`() {
        val (variables) = getVariables(true)
        val testImageBytes = testImage.inputStream.readAllBytes()
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1", variables)
        val nextTask = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()

        mockMvc.get("/curo-api/tasks/${nextTask.id}/file/image") {
            accept = MediaType.APPLICATION_OCTET_STREAM
            header("Authorization", "CuroBasic $basicLogin")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.parseMediaType("image/jpeg;charset=utf-8")) }
            content { bytes(testImageBytes) }
        }
    }

    @Test
    fun `getTaskZipFile() - loading zip file without authorization should not work`() {
        mockMvc.get("/curo-api/tasks/12345/zip-files") {
            accept = MediaType.APPLICATION_OCTET_STREAM
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `getTaskZipFile() - loading zip file with wrong task id should result in 404`() {
        mockMvc.get("/curo-api/tasks/12345/zip-files") {
            accept = MediaType.APPLICATION_OCTET_STREAM
            header("Authorization", "CuroBasic $basicLogin")
            param("files", "awesomeFile")
        }.andExpect {
            status { isNotFound() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `getTaskZipFile() - loading zip file without files parameters should result in 404`() {
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1")
        val nextTask = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()

        mockMvc.get("/curo-api/tasks/${nextTask.id}/zip-files") {
            accept = MediaType.APPLICATION_OCTET_STREAM
            header("Authorization", "CuroBasic $basicLogin")
        }.andExpect {
            status { isBadRequest() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `getTaskZipFile() - loading zip file with wrong variable name should result in 404`() {
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1")
        val nextTask = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()

        mockMvc.get("/curo-api/tasks/${nextTask.id}/zip-files") {
            accept = MediaType.APPLICATION_OCTET_STREAM
            header("Authorization", "CuroBasic $basicLogin")
            param("files", "awesomeFile")
        }.andExpect {
            status { isNotFound() }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `getTaskZipFile() - loading zip file should work`() {
        val (variables) = getVariables(true)
        val zippedTestImageBytes = ZipUtil.zipFiles(arrayListOf(Pair("testImage.jpeg", testImage.inputStream.readAllBytes())))
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1", variables)
        val nextTask = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()

        mockMvc.get("/curo-api/tasks/${nextTask.id}/zip-files") {
            accept = MediaType.APPLICATION_OCTET_STREAM
            header("Authorization", "CuroBasic $basicLogin")
            param("files", "image")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_OCTET_STREAM) }
            content { bytes(zippedTestImageBytes) }
        }
    }

    @Test
    fun `getTaskZipFile() - loading zip file with wrong names and ignore flag should work`() {
        val (variables) = getVariables(true)
        val zippedTestImageBytes = ZipUtil.zipFiles(arrayListOf(Pair("testImage.jpeg", testImage.inputStream.readAllBytes())))
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1", variables)
        val nextTask = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()

        mockMvc.get("/curo-api/tasks/${nextTask.id}/zip-files") {
            accept = MediaType.APPLICATION_OCTET_STREAM
            header("Authorization", "CuroBasic $basicLogin")
            param("files", "image")
            param("files", "wrongVariable")
            param("ignoreNotExistingFiles", "true")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_OCTET_STREAM) }
            content { bytes(zippedTestImageBytes) }
        }
    }

    private fun getVariables(includeFile: Boolean = false): Triple<HashMap<String, Any>, LinkedHashMap<String, Any>, DataModel> {
        val variables = hashMapOf<String, Any>()

        //String
        variables["name"] = "Fox"
        //Boolean
        variables["isActive"] = true
        //Int
        variables["age"] = 28
        //Json (Generic Object)
        val data = LinkedHashMap<String, Any>()
        data["id"] = "12345-6789-abc"
        data["name"] = "Curo"
        variables["data"] = data
        //Object
        val obj = DataModel()
        obj.id = "1b167666-ecb7-4bc2-b6c6-e206d0ac24bb"
        obj.name = "UMB AG"
        obj.usable = false
        variables["obj"] = obj

        //File
        if(includeFile) {
            val file = FileValueImpl(testImage.inputStream.readAllBytes(), ValueType.FILE, "testImage.jpeg", "image/jpeg", "utf-8")
            variables["image"] = file
        }

        return Triple(variables, data, obj)
    }

    private fun createFilter(): Filter {
        val filterDto = mapper.readValue("""{"resourceType": "Task",
                                              "name": "Big Filter",
                                              "owner": "demo",
                                              "query": {
                                                "processVariables": [
                                                  {
                                                    "name": "name",
                                                    "value": "Fox",
                                                    "operator": "eq"
                                                  }
                                                ],
                                                "sorting": [
                                                  {
                                                    "sortBy": "processVariable",
                                                    "sortOrder": "asc",
                                                    "parameters": {
                                                      "variable": "age",
                                                      "type": "Integer"
                                                    }
                                                  }
                                                ]
                                              },
                                              "properties": {
                                                "color": "#3e4d2f",
                                                "description": "My Tasks",
                                                "priority": 5,
                                                "listType": "small",
                                                "variables": [
                                                  {
                                                    "lable": "Name",
                                                    "name": "name",
                                                    "type": "text"
                                                  },
                                                  {
                                                    "lable": "Age",
                                                    "name": "age",
                                                    "type": "number"
                                                  }
                                                ]
                                              }
                                            }""".trimIndent(), FilterDto::class.java)
        val filter = filterService.newTaskFilter()
        filterDto.updateFilter(filter, EngineUtil.lookupProcessEngine(null))
        filterService.saveFilter(filter)

        return filter
    }
}
