package ch.umb.curo.starter.service

import ch.umb.curo.starter.models.response.CuroUserResponse

interface CuroUserService {

    fun getUsers(emailLike: String,
                 lastNameLike: String,
                 firstNameLike: String,
                 memberOfGroup: ArrayList<String>,
                 attributes: ArrayList<String>): CuroUserResponse

}
