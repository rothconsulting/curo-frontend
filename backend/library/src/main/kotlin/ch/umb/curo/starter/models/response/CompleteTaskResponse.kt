package ch.umb.curo.starter.models.response

import io.swagger.annotations.ApiModelProperty

class CompleteTaskResponse {

    /**
     * Variables related to this task
     **/
    @ApiModelProperty("Variables related to this task")
    var variables: HashMap<String, Any?>? = null

}
