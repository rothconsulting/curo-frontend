package ch.umb.curo.starter.models.response

import io.swagger.v3.oas.annotations.media.Schema

class AuthenticationSuccessResponse {

    /**
     * All successfully completed synchronous interceptors
     **/
    @Schema(description = "All successfully completed synchronous interceptors")
    var completedSteps: List<String> = arrayListOf()

    /**
     * All started asynchronous interceptors
     **/
    @Schema(description = "All started asynchronous interceptors")
    var asyncSteps: List<String> = arrayListOf()

}
