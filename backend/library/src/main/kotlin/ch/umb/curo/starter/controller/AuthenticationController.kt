package ch.umb.curo.starter.controller

import ch.umb.curo.starter.models.request.CuroPermissionsRequest
import ch.umb.curo.starter.models.request.ProcessStartRequest
import ch.umb.curo.starter.models.response.AuthenticationSuccessResponse
import ch.umb.curo.starter.models.response.CuroPermissionsResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@Tag(name = "auth", description = "Curo Auth API")
@RequestMapping("/curo-api/auth")
interface AuthenticationController {

    @Operation(
        summary = "Trigger login success logic",
        operationId = "success",
        description = "",
        security = [SecurityRequirement(name = "CuroBasic")]
    )
    @PostMapping("/success", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun success(request: HttpServletRequest): AuthenticationSuccessResponse

    @Operation(
        summary = "Load permissions",
        operationId = "getPermissionsPost",
        description = "",
        security = [SecurityRequirement(name = "CuroBasic")]
    )
    @PostMapping("/permissions", consumes = [MediaType.APPLICATION_JSON_VALUE], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getPermissions(
        @Parameter(
            description = "Should the permissions field be calculated",
            required = false
        )
        @RequestParam("returnPermissions", required = false, defaultValue = "true")
        returnPermissions: Boolean = true,

        @Parameter(description = "", required = false)
        @RequestBody
        body: CuroPermissionsRequest?
    ): CuroPermissionsResponse
}
