package ch.umb.curo.starter.models.response

import io.swagger.annotations.ApiModelProperty

class CompleteTaskResponse {

    /**
     * Variables related to this task
     **/
    @ApiModelProperty("Variables related to this task")
    var variables: HashMap<String, Any?>? = null

    /**
     * Possible next tasks
     */
    @ApiModelProperty("Possible next tasks")
    var flowToNext: List<String>? = null

    /**
     * Defines if the end of the process is reached or no next task exists
     */
    @ApiModelProperty("Defines if the end of the process is reached or no next task exists")
    var flowToEnd: Boolean? = null

    /**
     * Defines if the timeout got exceeded and polling is needed
     */
    @ApiModelProperty("Defines if the timeout got exceeded and polling is needed")
    var flowToNextTimeoutExceeded: Boolean? = null

}
