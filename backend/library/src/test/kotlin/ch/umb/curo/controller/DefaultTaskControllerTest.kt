package ch.umb.curo.controller


import ch.umb.curo.DataModel
import ch.umb.curo.starter.models.request.AssigneeRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.*
import org.springframework.test.web.servlet.result.isEqualTo
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

    private val basicLogin: String = Base64.getEncoder().encodeToString("demo:demo".toByteArray())

    @Test
    fun `getTask() - loading task without authorization should not work`() {
        mockMvc.get("/curo-api/tasks/12345") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isEqualTo(401) }
        }
    }

    @Test
    fun `getTask() - loading not existing task should result in 404`() {
        mockMvc.get("/curo-api/tasks/12345") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
        }.andExpect {
            status { isEqualTo(404) }
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
            status { isEqualTo(200) }
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
            status { isEqualTo(200) }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.id") { doesNotExist() }
            jsonPath("$.name") { value(task.name) }
            jsonPath("$.due") { isNotEmpty }
            jsonPath("$.variables") { doesNotExist() }
        }
    }

    @Test
    fun `getTask() - loading task with id should return variables by default`() {
        val (variables, data, obj) = getVariables()

        val newInstance = runtimeService.startProcessInstanceByKey("Process_1", variables)
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()

        mockMvc.get("/curo-api/tasks/${task.id}") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
        }.andExpect {
            status { isEqualTo(200) }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.id") { value(task.id) }
            jsonPath("$.variables.name") { isString }
            jsonPath("$.variables.name") { value(variables["name"].toString()) }
            jsonPath("$.variables.isActive") { isBoolean }
            jsonPath("$.variables.isActive") { value(variables["isActive"] as Boolean) }
            jsonPath("$.variables.age") { isNumber }
            jsonPath("$.variables.age") { value(variables["age"] as Int) }
            jsonPath("$.variables.data") { isMap }
            jsonPath("$.variables.data.id") { isString }
            jsonPath("$.variables.data.id") { value(data["id"].toString()) }
            jsonPath("$.variables.data.name") { isString }
            jsonPath("$.variables.data.name") { value(data["name"].toString()) }
            jsonPath("$.variables.obj") { isMap }
            jsonPath("$.variables.obj.id") { isString }
            jsonPath("$.variables.obj.id") { value(obj.id.toString()) }
            jsonPath("$.variables.obj.name") { isString }
            jsonPath("$.variables.obj.name") { value(obj.name.toString()) }
            jsonPath("$.variables.obj.usable") { isBoolean }
            jsonPath("$.variables.obj.usable") { value(obj.usable as Boolean) }
        }.andDo { print() }
    }

    @Test
    fun `getTask() - loading task with id and selected variables should return only selected variables`() {
        val (variables, data, obj) = getVariables()

        val newInstance = runtimeService.startProcessInstanceByKey("Process_1", variables)
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()

        mockMvc.get("/curo-api/tasks/${task.id}") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            param("variables", "name")
            param("variables", "age")
            param("variables", "obj")
        }.andExpect {
            status { isEqualTo(200) }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.id") { value(task.id) }
            jsonPath("$.variables.name") { isString }
            jsonPath("$.variables.name") { value(variables["name"].toString()) }
            jsonPath("$.variables.age") { isNumber }
            jsonPath("$.variables.age") { value(variables["age"] as Int) }
            jsonPath("$.variables.isActive") { doesNotExist() }
            jsonPath("$.variables.data") { doesNotExist() }
            jsonPath("$.variables.obj") { isMap }
            jsonPath("$.variables.obj.id") { isString }
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
            status { isEqualTo(404) }
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
            status { isEqualTo(404) }
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
            status { isEqualTo(200) }
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
            status { isEqualTo(200) }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.id") { value(task.id) }
            jsonPath("$.processInstanceId") { value(newInstance.rootProcessInstanceId) }
            jsonPath("$.status") { value("completed") }
        }
    }

    @Test
    fun `getTask() - loading historic task with historic id and selected variables should return only selected variables`() {
        val (variables, data, obj) = getVariables()

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
            status { isEqualTo(200) }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.status") { value("completed") }
            jsonPath("$.id") { value(task.id) }
            jsonPath("$.variables.name") { isString }
            jsonPath("$.variables.name") { value(variables["name"].toString()) }
            jsonPath("$.variables.age") { isNumber }
            jsonPath("$.variables.age") { value(variables["age"] as Int) }
            jsonPath("$.variables.isActive") { doesNotExist() }
            jsonPath("$.variables.data") { doesNotExist() }
            jsonPath("$.variables.obj") { isMap }
            jsonPath("$.variables.obj.id") { isString }
            jsonPath("$.variables.obj.id") { value(obj.id.toString()) }
        }.andDo { print() }
    }

    @Test
    fun `completeTask() - complete task without authorization should not work`() {
        mockMvc.post("/curo-api/tasks/12345/status") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isEqualTo(401) }
        }
    }

    @Test
    fun `completeTask() - complete not existing task should result in 404`() {
        mockMvc.post("/curo-api/tasks/12345/status") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            contentType = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isEqualTo(404) }
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
            status { isEqualTo(403) }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }


    @Test
    fun `completeTask() - complete task should work`() {
        val (variables, data, obj) = getVariables()
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
            status { isEqualTo(200) }
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
            status { isEqualTo(200) }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.variables.name") { isString }
            jsonPath("$.variables.name") { value(variables["name"].toString()) }
            jsonPath("$.variables.isActive") { isBoolean }
            jsonPath("$.variables.isActive") { value(variables["isActive"] as Boolean) }
            jsonPath("$.variables.age") { isNumber }
            jsonPath("$.variables.age") { value(variables["age"] as Int) }
            jsonPath("$.variables.data") { isMap }
            jsonPath("$.variables.data.id") { isString }
            jsonPath("$.variables.data.id") { value(data["id"].toString()) }
            jsonPath("$.variables.data.name") { isString }
            jsonPath("$.variables.data.name") { value(data["name"].toString()) }
            jsonPath("$.variables.obj") { isMap }
            jsonPath("$.variables.obj.id") { isString }
            jsonPath("$.variables.obj.id") { value(obj.id.toString()) }
            jsonPath("$.variables.obj.name") { isString }
            jsonPath("$.variables.obj.name") { value(obj.name.toString()) }
            jsonPath("$.variables.obj.usable") { isBoolean }
            jsonPath("$.variables.obj.usable") { value(obj.usable as Boolean) }
        }

        //Check if completed
        val historicTask = historyService.createHistoricTaskInstanceQuery().taskId(task.id).singleResult()
        assert(historicTask?.deleteReason == "completed")
    }

    @Test
    fun `assignTask() - assign task without authorization should not work`() {
        mockMvc.put("/curo-api/tasks/12345/assignee") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isEqualTo(401) }
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
            status { isEqualTo(404) }
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
            status { isEqualTo(200) }
        }

        val updatedTask = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        assert((updatedTask?.assignee ?: "") == "demo")

        //Check if correct method was used
        val lastOperation = historyService.createUserOperationLogQuery().taskId(task.id).singleResult()
        assert(lastOperation?.operationType == "Claim")
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
            status { isEqualTo(200) }
        }

        val updatedTask = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        assert((updatedTask?.assignee ?: "") == "demo")

        //Check if correct method was used
        val lastOperation = historyService.createUserOperationLogQuery().taskId(task.id).singleResult()
        assert(lastOperation?.operationType == "Assign")
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
            status { isEqualTo(200) }
        }

        val updatedTask = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        assert((updatedTask?.assignee ?: "") == "user")

        //Check if correct method was used
        val lastOperation = historyService.createUserOperationLogQuery().taskId(task.id).singleResult()
        assert(lastOperation?.operationType == "Assign")
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
            status { isEqualTo(200) }
        }

        val updatedTask = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        assert((updatedTask?.assignee ?: "") == "")

        //Check if correct method was used
        val lastOperation = historyService.createUserOperationLogQuery().taskId(task.id).singleResult()
        assert(lastOperation?.operationType == "Claim")
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
            status { isEqualTo(200) }
        }

        val updatedTask = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        assert((updatedTask?.assignee ?: "") == "")

        //Check if correct method was used
        val lastOperation = historyService.createUserOperationLogQuery().taskId(task.id).singleResult()
        assert(lastOperation?.operationType == "Assign")
    }

    @Test
    fun `saveVariables() - save variables without authorization should not work`() {
        mockMvc.patch("/curo-api/tasks/12345/variables") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isEqualTo(401) }
        }
    }

    @Test
    fun `saveVariables() - save variables for not existing task should result in 404`() {
        val (variables, data, obj) = getVariables()

        mockMvc.patch("/curo-api/tasks/12345/variables") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(variables)
        }.andExpect {
            status { isEqualTo(404) }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `saveVariables() - save variables for task with different assignee should result in 403`() {
        val (variables, data, obj) = getVariables()
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1")
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        taskService.claim(task.id, "user")

        mockMvc.patch("/curo-api/tasks/${task.id}/variables") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(variables)
        }.andExpect {
            status { isEqualTo(403) }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `saveVariables() - save variables should work`() {
        val (variables, data, obj) = getVariables()
        val newInstance = runtimeService.startProcessInstanceByKey("Process_1", variables)
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
            status { isEqualTo(200) }
        }

        val taskVariables = taskService.getVariablesTyped(task.id)
        assert((taskVariables["name"] ?:"") == "UMB AG")
        assert((mapper.readValue(taskVariables["newData"] as String, DataModel::class.java)).name == "NEW_DATA")

    }

    private fun getVariables(): Triple<HashMap<String, Any>, LinkedHashMap<String, Any>, DataModel> {
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
        return Triple(variables, data, obj)
    }
}
