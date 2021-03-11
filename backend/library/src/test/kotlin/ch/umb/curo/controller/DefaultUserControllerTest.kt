package ch.umb.curo.controller


import ch.umb.curo.starter.models.response.CuroUserResponse
import ch.umb.curo.starter.property.CuroProperties
import com.fasterxml.jackson.databind.ObjectMapper
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
import java.util.*


@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("users")
class DefaultUserControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var mapper: ObjectMapper

    @Autowired
    lateinit var properties: CuroProperties

    private val basicLogin: String = Base64.getEncoder().encodeToString("demo:demo".toByteArray())

    @Test
    fun `getUsers() - loading users without authorization should not work`() {
        mockMvc.get("/curo-api/users") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `getUsers() - loading users should work`() {
        properties.initialUsers!!.sortBy { it.id }

        val result = mockMvc.get("/curo-api/users") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$[0].id") { value(properties.initialUsers!![0].id) }
            jsonPath("$[0].email") { value(properties.initialUsers!![0].email) }
            jsonPath("$[0].firstname") { value(properties.initialUsers!![0].firstname) }
            jsonPath("$[0].lastname") { value(properties.initialUsers!![0].lastname) }
        }.andReturn()

        val response = mapper.readValue(result.response.contentAsString, CuroUserResponse::class.java)

        assert(response.size == 4)
        assert(response.any { it.id == "bob_tower" })
        assert(response.any { it.id == "demo" })
        assert(response.any { it.id == "sahra_doe" })
        assert(response.any { it.id == "richard_m_nunez" })
    }

    @Test
    fun `getUsers() - loading users with only id should work`() {
        properties.initialUsers!!.sortBy { it.id }

        val result = mockMvc.get("/curo-api/users") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
            param("attributes", "id")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$[0].id") { value(properties.initialUsers!![0].id) }
            jsonPath("$[0].email") { doesNotExist() }
            jsonPath("$[0].firstname") { doesNotExist() }
            jsonPath("$[0].lastname") { doesNotExist() }
        }.andReturn()

        val response = mapper.readValue(result.response.contentAsString, CuroUserResponse::class.java)

        assert(response.size == 4)
        assert(response.any { it.id == "bob_tower" })
        assert(response.any { it.id == "demo" })
        assert(response.any { it.id == "sahra_doe" })
        assert(response.any { it.id == "richard_m_nunez" })
    }
}
