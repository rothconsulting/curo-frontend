package ch.umb.curo.starter.controller

import ch.umb.curo.starter.models.response.CuroUserResponse
import ch.umb.curo.starter.service.CuroUserService
import org.springframework.web.bind.annotation.RestController

@RestController
class DefaultUserController(
    private val curoUserService: CuroUserService,
) : UserController {

    override fun getUsers(emailLike: String,
                          lastnameLike: String,
                          firstnameLike: String,
                          memberOfGroup: ArrayList<String>,
                          attributes: ArrayList<String>): CuroUserResponse {
        return curoUserService.getUsers(emailLike, lastnameLike, firstnameLike, memberOfGroup, attributes)
    }
}
