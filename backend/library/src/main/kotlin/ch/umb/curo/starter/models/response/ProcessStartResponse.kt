package ch.umb.curo.starter.models.response

import io.swagger.annotations.ApiModelProperty
import java.io.Serializable

class ProcessStartResponse : Serializable {

    /**
     * Id of the process instance
     **/
    @ApiModelProperty("Id of the process instance")
    var processInstanceId: String? = null

    /**
     * Business key for the process instance
     **/
    @ApiModelProperty("Business key for the process instance")
    var businessKey: String? = null

    /**
     * Variables related to this process instance
     **/
    @ApiModelProperty("Variables related to this process instance")
    var variables: HashMap<String, Any?>? = null

}
