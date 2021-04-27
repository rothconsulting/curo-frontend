package ch.umb.curo.controller

import ch.umb.curo.starter.models.request.CuroPermissionsRequest
import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.FilterService
import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.authorization.Resources
import org.camunda.bpm.engine.filter.Filter
import org.camunda.bpm.engine.rest.dto.runtime.FilterDto
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.util.*

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("auth")
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
class DefaultAuthenticationControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var mapper: ObjectMapper

    private val basicLoginBob: String = Base64.getEncoder().encodeToString("bob_tower:testPassword".toByteArray())
    private val basicLoginSahra: String = Base64.getEncoder().encodeToString("sahra_doe:testPassword".toByteArray())
    private val basicLoginRichard: String = Base64.getEncoder().encodeToString("richard_m_nunez:testPassword".toByteArray())

    @Test
    fun `getPermissions() - loading permissions without authorization should not work`() {
        mockMvc.post("/curo-api/auth/permissions") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `getPermissions() - loading permissions should work (Bob)`() {
        val request = CuroPermissionsRequest()
        request["*"] = hashMapOf(Pair(Resources.PROCESS_INSTANCE, arrayListOf("READ")))

        mockMvc.post("/curo-api/auth/permissions") {
            accept = MediaType.APPLICATION_JSON
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(request)
            header("Authorization", "CuroBasic $basicLoginBob")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.userId") { value("bob_tower") }
            jsonPath("$.groups") { value(arrayListOf("supporter")) }
            jsonPath("$.permissions.*.PROCESS_INSTANCE[*]") { value(request["*"]?.get(Resources.PROCESS_INSTANCE)) }
        }
    }

    @Test
    fun `getPermissions() - loading permissions should work (Sahra)`() {
        val request = CuroPermissionsRequest()
        request["*"] = hashMapOf(Pair(Resources.PROCESS_INSTANCE, arrayListOf("READ", "CREATE")))

        mockMvc.post("/curo-api/auth/permissions") {
            accept = MediaType.APPLICATION_JSON
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(request)
            header("Authorization", "CuroBasic $basicLoginSahra")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.userId") { value("sahra_doe") }
            jsonPath("$.groups") { value(arrayListOf("supporter","worker")) }
            jsonPath("$.permissions.*.PROCESS_INSTANCE[*]") { value(request["*"]?.get(Resources.PROCESS_INSTANCE)) }
        }
    }

    @Test
    fun `getPermissions() - loading permissions should work (Richard)`() {
        val request = CuroPermissionsRequest()
        request["*"] = hashMapOf(Pair(Resources.PROCESS_INSTANCE, arrayListOf("READ", "CREATE","UPDATE","DELETE")))

        mockMvc.post("/curo-api/auth/permissions") {
            accept = MediaType.APPLICATION_JSON
            contentType = MediaType.APPLICATION_JSON
            content = mapper.writeValueAsString(request)
            header("Authorization", "CuroBasic $basicLoginRichard")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.userId") { value("richard_m_nunez") }
            jsonPath("$.groups") { value(arrayListOf("teamlead")) }
            jsonPath("$.permissions.*.PROCESS_INSTANCE[*]") { value(request["*"]?.get(Resources.PROCESS_INSTANCE)) }
        }
    }

    @Test
    fun `getPermissions() - loading permissions without return permissions should work (Bob)`() {
        mockMvc.post("/curo-api/auth/permissions") {
            accept = MediaType.APPLICATION_JSON
            contentType = MediaType.APPLICATION_JSON
            param("returnPermissions","false")
            header("Authorization", "CuroBasic $basicLoginBob")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$.userId") { value("bob_tower") }
            jsonPath("$.groups") { value(arrayListOf("supporter")) }
            jsonPath("$.permissions") { doesNotExist() }
        }
    }
}
