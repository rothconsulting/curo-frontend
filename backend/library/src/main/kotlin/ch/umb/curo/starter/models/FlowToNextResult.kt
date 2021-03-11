package ch.umb.curo.starter.models

import io.swagger.v3.oas.annotations.media.Schema

class FlowToNextResult(
    /**
     * Possible next tasks
     */
    @Schema(description = "Possible next tasks")
    val flowToNext: List<String> = arrayListOf(),

    /**
     * Defines if the timeout got exceeded and polling is needed
     */
    @Schema(description = "Defines if the timeout got exceeded and polling is needed")
    val flowToNextTimeoutExceeded: Boolean = false,

    /**
     * Defines if the end of the process is reached or no next task exists
     */
    @Schema(description = "Defines if the end of the process is reached or no next task exists")
    val flowToEnd: Boolean = false
)
