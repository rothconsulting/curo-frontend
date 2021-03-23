package ch.umb.curo.starter.service

import ch.umb.curo.starter.models.response.CuroUser
import ch.umb.curo.starter.models.response.CuroUserResponse
import org.camunda.bpm.engine.IdentityService

class DefaultCuroUserService(private val identityService: IdentityService) : CuroUserService {
    override fun getUsers(
        emailLike: String,
        lastNameLike: String,
        firstNameLike: String,
        memberOfGroup: ArrayList<String>,
        attributes: ArrayList<String>
    ): CuroUserResponse {

        val baseQuery = identityService.createUserQuery()
        baseQuery.userEmailLike(if (emailLike.isEmpty()) "%" else emailLike)
        baseQuery.userLastNameLike(if (lastNameLike.isEmpty()) "%" else lastNameLike)
        baseQuery.userFirstNameLike(if (firstNameLike.isEmpty()) "%" else firstNameLike)

        val users = if (memberOfGroup.isNotEmpty()) {
            memberOfGroup.flatMap {
                baseQuery
                    .memberOfGroup(it)
                    .list()
            }.distinctBy { it.id }
        } else {
            baseQuery.list()
        }

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
