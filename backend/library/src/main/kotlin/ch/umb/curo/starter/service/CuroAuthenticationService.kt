package ch.umb.curo.starter.service

import ch.umb.curo.starter.models.response.AuthenticationSuccessResponse
import javax.servlet.http.HttpServletRequest

interface CuroAuthenticationService {

    fun success(request: HttpServletRequest): AuthenticationSuccessResponse

}
