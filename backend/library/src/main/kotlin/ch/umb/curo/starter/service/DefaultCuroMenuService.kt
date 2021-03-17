package ch.umb.curo.starter.service

import ch.umb.curo.starter.models.response.CuroMenu
import org.camunda.bpm.engine.FilterService

class DefaultCuroMenuService(private val filterService: FilterService) : CuroMenuService {
    override fun getMenu(): CuroMenu {
        val menu = CuroMenu()
        val filters = filterService.createFilterQuery().list()
        menu.addAll(filters.map { CuroMenu.fromFilter(it) }.sortedBy { it.order })
        return menu
    }
}
