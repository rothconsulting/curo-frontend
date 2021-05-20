package ch.umb.curo.starter.service

import ch.umb.curo.starter.models.request.CuroPermissionsRequest
import ch.umb.curo.starter.models.response.AuthenticationSuccessResponse
import ch.umb.curo.starter.models.response.CuroPermissionsResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

interface CuroAuthenticationService {

    fun success(request: HttpServletRequest): AuthenticationSuccessResponse

    fun logout(request: HttpServletRequest, response: HttpServletResponse)

    fun getPermissions(
        returnPermissions: Boolean,
        request: CuroPermissionsRequest?
    ): CuroPermissionsResponse
}
