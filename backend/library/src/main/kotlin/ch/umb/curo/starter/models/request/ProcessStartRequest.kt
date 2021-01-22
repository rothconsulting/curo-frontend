package ch.umb.curo.starter.models.request

import io.swagger.v3.oas.annotations.media.Schema
import java.io.Serializable

class ProcessStartRequest : Serializable {

    /**
     * Process definition key for the new instance
     **/
    @Schema(description= "Process definition key for the new instance")
    var processDefinitionKey: String? = null


    /**
     * Business key for the new instance
     **/
    @Schema(description= "Business key for the new instance")
    var businessKey: String? = null


    /**
     * Initial variables for the new instance
     **/
    @Schema(description= "Initial variables for the new instance")
    var variables: HashMap<String, Any?>? = null

}
