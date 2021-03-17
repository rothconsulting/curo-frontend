package ch.umb.curo.starter.service

import ch.umb.curo.starter.models.response.CuroUserResponse

interface CuroUserService {

    fun getUsers(attributes: ArrayList<String>): CuroUserResponse

}
