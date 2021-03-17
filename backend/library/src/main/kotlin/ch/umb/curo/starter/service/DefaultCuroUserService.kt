package ch.umb.curo.starter.service

import ch.umb.curo.starter.models.response.CuroUser
import ch.umb.curo.starter.models.response.CuroUserResponse
import org.camunda.bpm.engine.IdentityService

class DefaultCuroUserService(private val identityService: IdentityService) : CuroUserService {
    override fun getUsers(attributes: ArrayList<String>): CuroUserResponse {
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
