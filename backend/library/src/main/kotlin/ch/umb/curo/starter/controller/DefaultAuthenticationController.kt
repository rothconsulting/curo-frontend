package ch.umb.curo.starter.controller

import ch.umb.curo.starter.models.response.AuthenticationSuccessResponse
import ch.umb.curo.starter.service.CuroAuthenticationService
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest

@RestController
class DefaultAuthenticationController(private val curoAuthenticationService: CuroAuthenticationService) :
    AuthenticationController {

    override fun success(request: HttpServletRequest): AuthenticationSuccessResponse {
        return curoAuthenticationService.success(request)
    }

}
