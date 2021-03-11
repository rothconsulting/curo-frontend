package ch.umb.curo.starter.controller

import ch.umb.curo.starter.models.response.AuthenticationSuccessResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
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

}
