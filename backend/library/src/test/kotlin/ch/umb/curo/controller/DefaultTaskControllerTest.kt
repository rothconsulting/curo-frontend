package ch.umb.curo.controller


import ch.umb.curo.DataModel
import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
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
        mockMvc.post("/curo-api/tasks/status/12345") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isEqualTo(401) }
        }
    }

    @Test
    fun `completeTask() - complete not existing task should result in 404`() {
        mockMvc.get("/curo-api/tasks/status/12345") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
        }.andExpect {
            status { isEqualTo(404) }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
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
