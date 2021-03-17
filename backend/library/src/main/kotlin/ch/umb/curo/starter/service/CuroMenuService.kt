package ch.umb.curo.starter.service

import ch.umb.curo.starter.models.response.CuroMenu

interface CuroMenuService {

    fun getMenu(): CuroMenu

}
