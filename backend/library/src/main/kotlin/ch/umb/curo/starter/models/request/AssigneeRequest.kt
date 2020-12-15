package ch.umb.curo.starter.models.request

import io.swagger.annotations.ApiModelProperty
import java.io.Serializable

class AssigneeRequest : Serializable {

    /**
     * Assignee
     **/
    @ApiModelProperty("Assignee")
    var assignee: String? = null

}
