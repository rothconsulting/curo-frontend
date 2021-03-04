package ch.umb.curo.starter.models.response

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.camunda.bpm.engine.identity.User

@JsonInclude(JsonInclude.Include.NON_NULL)
class CuroUser {

    /**
     * Id of the user
     **/
    @Schema(description= "Id of the user")
    var id: String? = null

    /**
     * Firstname of the user
     **/
    @Schema(description= "Firstname of the user")
    var firstname: String? = null

    /**
     * Lastname of the user
     **/
    @Schema(description= "Lastname of the user")
    var lastname: String? = null

    /**
     * Email of the user
     **/
    @Schema(description= "Email of the user")
    var email: String? = null


    companion object {
        fun fromCamundaUser(camundaUser: User): CuroUser {
            val newUser = CuroUser()
            newUser.id = camundaUser.id
            newUser.firstname = camundaUser.firstName
            newUser.lastname = camundaUser.lastName
            newUser.email = camundaUser.email

            return newUser
        }
    }
}
