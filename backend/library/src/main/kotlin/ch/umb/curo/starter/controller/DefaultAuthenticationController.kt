package ch.umb.curo.starter.controller

import ch.umb.curo.starter.models.request.CuroPermissionsRequest
import ch.umb.curo.starter.models.response.AuthenticationSuccessResponse
import ch.umb.curo.starter.models.response.CuroPermissionsResponse
import ch.umb.curo.starter.service.CuroAuthenticationService
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
class DefaultAuthenticationController(private val curoAuthenticationService: CuroAuthenticationService) :
    AuthenticationController {

    override fun success(request: HttpServletRequest): AuthenticationSuccessResponse {
        return curoAuthenticationService.success(request)
    }

    override fun logout(request: HttpServletRequest, response: HttpServletResponse) {
        curoAuthenticationService.logout(request, response)
    }

    override fun getPermissions(returnPermissions: Boolean, body: CuroPermissionsRequest?): CuroPermissionsResponse {
        return curoAuthenticationService.getPermissions(returnPermissions, body)
    }

}
