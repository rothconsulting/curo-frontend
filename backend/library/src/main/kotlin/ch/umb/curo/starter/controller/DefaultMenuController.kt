package ch.umb.curo.starter.controller

import ch.umb.curo.starter.models.response.CuroMenu
import ch.umb.curo.starter.service.CuroMenuService
import org.springframework.web.bind.annotation.RestController

@RestController
class DefaultMenuController(private val curoMenuService: CuroMenuService) : MenuController {

    override fun getMenu(): CuroMenu {
        return curoMenuService.getMenu()
    }
}
