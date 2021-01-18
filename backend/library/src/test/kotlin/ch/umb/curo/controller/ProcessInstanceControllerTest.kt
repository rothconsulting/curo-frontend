package ch.umb.curo.controller

import ch.umb.curo.DataModel
import ch.umb.curo.starter.models.request.ProcessStartRequest
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
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.isEqualTo
import java.util.*
import kotlin.collections.LinkedHashMap

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
class ProcessInstanceControllerTest {

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
    fun `startProcess() - start process without authorization should not work`() {
        mockMvc.post("/curo-api/process-instances") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isEqualTo(401) }
        }
    }

    @Test
    fun `startProcess() - start process with wrong definition key should result in 404`() {
        val processStartRequest = ProcessStartRequest()
        processStartRequest.processDefinitionKey = "WRONG_NAME"

        mockMvc.post("/curo-api/process-instances") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(processStartRequest)
        }.andExpect {
            status { isEqualTo(404) }
            content { contentType(MediaType.APPLICATION_JSON) }
        }
    }

    @Test
    fun `startProcess() - start process should work`() {
        val processStartRequest = ProcessStartRequest()
        processStartRequest.processDefinitionKey = "Process_1"
        processStartRequest.businessKey = "12345-67890"

        mockMvc.post("/curo-api/process-instances") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(processStartRequest)
        }.andExpect {
            status { isEqualTo(200) }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.processInstanceId") { isNotEmpty }
            jsonPath("$.businessKey") { value(processStartRequest.businessKey.toString()) }
        }
    }

    @Test
    fun `startProcess() - start process with return variables should work`() {
        val (variables, data, obj) = getVariables()
        val processStartRequest = ProcessStartRequest()
        processStartRequest.processDefinitionKey = "Process_1"
        processStartRequest.businessKey = "12345-67890"
        processStartRequest.variables = variables

        mockMvc.post("/curo-api/process-instances") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            param("returnVariables", "true")
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(processStartRequest)
        }.andExpect {
            status { isEqualTo(200) }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.processInstanceId") { isNotEmpty }
            jsonPath("$.businessKey") { value(processStartRequest.businessKey.toString()) }
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
            jsonPath("$.variables.obj.usable") { value(obj.usable.toString()) }
        }
    }

    private fun getVariables(): Triple<HashMap<String, Any?>, LinkedHashMap<String, Any>, DataModel> {
        val variables = hashMapOf<String, Any?>()

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
