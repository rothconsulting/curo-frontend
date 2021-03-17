package ch.umb.curo.starter.controller

import ch.umb.curo.starter.models.response.CuroUserResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam

@Tag(name = "user", description = "Curo User API")
@RequestMapping("/curo-api/users")
interface UserController {

    @Operation(
        summary = "Load list of users",
        operationId = "getUsers",
        description = "",
        security = [SecurityRequirement(name = "CuroBasic")]
    )
    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getUsers(
        @Parameter(
            description = "Define which fields should be returned. If not present, all fields are returned",
            required = false
        )
        @RequestParam("attributes", required = false, defaultValue = "")
        attributes: ArrayList<String> = arrayListOf()
    ): CuroUserResponse
}
