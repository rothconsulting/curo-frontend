package ch.umb.curo.exception

import ch.umb.curo.starter.models.request.ProcessStartRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.RuntimeService
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import java.util.*

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("businessLogicError")
class BusinessLogicErrorTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var mapper: ObjectMapper

    @Autowired
    lateinit var runtimeService: RuntimeService

    private val basicLogin: String = Base64.getEncoder().encodeToString("demo:demo".toByteArray())

    @Test
    fun `checkErrorOnApi() - business logic error should be returned on Curo api`() {
        val processStartRequest = ProcessStartRequest()
        processStartRequest.processDefinitionKey = "business_error"

        mockMvc.post("/curo-api/process-instances") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(processStartRequest)
        }.andExpect {
            status { isEqualTo(500) }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.status") { value(500) }
            jsonPath("$.error") { value("internalErrorMessage") }
            jsonPath("$.errorCode") { value("ERROR_CODE") }
            jsonPath("$.message") { value("publicMessage") }
            jsonPath("$.data.test") { value("123456-789") }
            jsonPath("$.data.camunda.taskName") { value("Error Task") }
            jsonPath("$.data.camunda.taskDefinitionKey") { value("Task_03qfpr3") }
        }
    }

    @Test
    fun `checkErrorInIncident() - business logic error should be saved as incident`() {
        val newInstance = runtimeService.startProcessInstanceByKey("business_error_async")

        Thread.sleep(1000)

        val incidents = runtimeService.createIncidentQuery().list()

        assert(incidents.isNotEmpty())
        assert(incidents[0].incidentMessage == "ERROR_CODE: internalErrorMessage")
    }

}
