package ch.umb.curo.starter.controller

import ch.umb.curo.starter.models.response.CuroMenu
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

@Tag(name = "menu", description = "Curo Menu API")
@RequestMapping("/curo-api/menus")
interface MenuController {

    @Operation(
        summary = "Get menu for the current user",
        operationId = "getMenu",
        description = "",
        security = [SecurityRequirement(name = "CuroBasic")]
    )
    @GetMapping("", produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getMenu(): CuroMenu

}
