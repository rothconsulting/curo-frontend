package ch.umb.curo.demo

import ch.umb.curo.starter.exception.ApiException
import ch.umb.curo.starter.interceptor.JWTClaimCheckInterceptor
import ch.umb.curo.starter.property.CuroProperties
import ch.umb.curo.starter.util.JWTUtil
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletRequest

@Service
class AccessGroupCheckInterceptor(val properties: CuroProperties) : JWTClaimCheckInterceptor {
    override val name: String
        get() = "AccessGroupCheck"
    override val order: Int
        get() = 100

    override fun onIntercept(jwt: DecodedJWT, jwtRaw: String, request: HttpServletRequest) {
        val jwtGroups = JWTUtil.getRoles(jwt, properties)
        if(!jwtGroups.contains("curo-admin")){
            throw ApiException.unauthorized403("Token is missing needed role to access Curo.").printException(properties.auth.oauth2.printErrorsToLog)
        }
    }
}
