package ch.umb.curo.starter.service

import ch.umb.curo.starter.models.request.CuroPermissionsRequest
import ch.umb.curo.starter.models.response.AuthenticationSuccessResponse
import ch.umb.curo.starter.models.response.CuroPermissionsResponse
import javax.servlet.http.HttpServletRequest

interface CuroAuthenticationService {

    fun success(request: HttpServletRequest): AuthenticationSuccessResponse

    fun getPermissions(
        returnPermissions: Boolean,
        request: CuroPermissionsRequest?
    ): CuroPermissionsResponse
}
