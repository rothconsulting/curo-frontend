package ch.umb.curo.controller


import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.FilterService
import org.camunda.bpm.engine.filter.Filter
import org.camunda.bpm.engine.rest.dto.runtime.FilterDto
import org.camunda.bpm.engine.rest.util.EngineUtil
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
@ActiveProfiles("menu")
class DefaultMenuControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var mapper: ObjectMapper

    @Autowired
    lateinit var filterService: FilterService

    private val basicLogin: String = Base64.getEncoder().encodeToString("demo:demo".toByteArray())

    @Test
    fun `getMenu() - loading menu without authorization should not work`() {
        mockMvc.get("/curo-api/menus") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `getMenu() - loading menu should work`() {

        createFilter("My Tasks", "#000", 10, "home")
        createFilter("Overdue Tasks", "#F00", 20, "clock")
        createFilter("Cool Tasks", "#0F0", 30, "car")
        createFilter("Boring Tasks", "#00F", 40, "gear")


        mockMvc.get("/curo-api/menus") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "CuroBasic $basicLogin")
        }.andExpect {
            status { isOk() }
            content { contentType(MediaType.APPLICATION_JSON) }
            jsonPath("$[0].name") { value("\uD83C\uDF0D All tasks") }
            jsonPath("$[0].order") { value(0) }
            jsonPath("$[1].name") { value("My Tasks") }
            jsonPath("$[1].order") { value(10) }
            jsonPath("$[2].name") { value("Overdue Tasks") }
            jsonPath("$[2].order") { value(20) }
            jsonPath("$[3].name") { value("Cool Tasks") }
            jsonPath("$[3].order") { value(30) }
            jsonPath("$[4].name") { value("Boring Tasks") }
            jsonPath("$[4].order") { value(40) }
        }
    }

    private fun createFilter(name: String, color: String, priority: Int, icon: String): Filter {
        val filterDto = mapper.readValue(
            """{"resourceType": "Task",
                                              "name": "$name",
                                              "owner": "demo",
                                              "query": {},
                                              "properties": {
                                                "color": "$color",
                                                "priority": $priority,
                                                "icon": "$icon"
                                              }
                                            }""".trimIndent(), FilterDto::class.java
        )
        val filter = filterService.newTaskFilter()
        filterDto.updateFilter(filter, EngineUtil.lookupProcessEngine(null))
        filterService.saveFilter(filter)

        return filter
    }
}
