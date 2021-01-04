package ch.umb.curo.starter.models.request

import io.swagger.annotations.ApiModelProperty
import java.io.Serializable

class ProcessStartRequest : Serializable {

    /**
     * Process definition key for the new instance
     **/
    @ApiModelProperty("Process definition key for the new instance")
    var processDefinitionKey: String? = null


    /**
     * Business key for the new instance
     **/
    @ApiModelProperty("Business key for the new instance")
    var businessKey: String? = null


    /**
     * Initial variables for the new instance
     **/
    @ApiModelProperty("Initial variables for the new instance")
    var variables: HashMap<String, Any?>? = null

}
