package ch.umb.curo.starter.controller

import ch.umb.curo.starter.models.response.CuroMenu
import org.camunda.bpm.engine.FilterService
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.web.bind.annotation.RestController

@RestController
@ConditionalOnMissingClass
class DefaultMenuController(private val filterService: FilterService) : MenuController {
    override fun getMenu(): CuroMenu {
        val menu = CuroMenu()
        val filters = filterService.createFilterQuery().list()
        menu.addAll(filters.map { CuroMenu.fromFilter(it) }.sortedBy { it.order })
        return menu
    }
}
