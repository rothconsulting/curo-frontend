package ch.umb.curo.starter.controller

import ch.umb.curo.starter.models.response.CuroUser
import ch.umb.curo.starter.models.response.CuroUserResponse
import org.camunda.bpm.engine.IdentityService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletResponse

@RestController
@ConditionalOnMissingClass
class DefaultUserController(
    private val identityService: IdentityService,
) : UserController {

    override fun getUsers(attributes: ArrayList<String>, response: HttpServletResponse): CuroUserResponse {
        val users = identityService.createUserQuery().list()

        val result = CuroUserResponse()
        result.addAll(users.map { CuroUser.fromCamundaUser(it) })

        if (attributes.isNotEmpty()) {
            //Filter attributes
            result.map { curoUser ->
                val attrDefinitions = CuroUser::class.java.declaredFields
                attrDefinitions.forEach { field ->
                    if (field.name !in attributes && field.name != "Companion") {
                        field.isAccessible = true
                        field.set(curoUser, null)
                    }
                }
            }
        }

        result.sortBy { it.id }

        return result
    }
}
