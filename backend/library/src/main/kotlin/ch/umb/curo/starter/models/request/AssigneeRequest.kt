package ch.umb.curo.starter.models.request

import io.swagger.v3.oas.annotations.media.Schema
import java.io.Serializable

class AssigneeRequest : Serializable {

    /**
     * Assignee
     **/
    @Schema(description = "Assignee")
    var assignee: String? = null

}
