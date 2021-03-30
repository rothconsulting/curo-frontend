package ch.umb.curo.demo

import ch.umb.curo.starter.exception.ApiException
import ch.umb.curo.starter.interceptor.JWTClaimCheckInterceptor
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.boot.logging.LogLevel
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletRequest

@Service
class ClientIdClaimCheckInterceptor(
    private val allowedClients: ArrayList<String> = arrayListOf(),
    private val printErrorsToLog: Boolean = true
) : JWTClaimCheckInterceptor {
    override val name: String
        get() = "ClientIdClaimCheck"
    override val order: Int
        get() = 100

    override fun onIntercept(jwt: DecodedJWT, jwtRaw: String, request: HttpServletRequest) {
        if (jwt.getClaim("azp").asString() !in allowedClients || allowedClients.isEmpty()) {
            throw ApiException.unauthorized403("Invalid client")
                .printException(printErrorsToLog, logLevel = LogLevel.WARN)
        }
    }
}
