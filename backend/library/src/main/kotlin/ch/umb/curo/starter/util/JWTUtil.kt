package ch.umb.curo.starter.util

import ch.umb.curo.starter.exception.ApiException
import ch.umb.curo.starter.property.CuroProperties
import com.auth0.jwt.interfaces.DecodedJWT

object JWTUtil {

    fun getRoles(jwt: DecodedJWT, properties: CuroProperties): List<String?>{
        return when {
            (properties.auth.oauth2.userFederation.loadGroupFromRoles) -> {
                val resourceAccess = jwt.getClaim(properties.auth.oauth2.userFederation.resourceAccessClaim).asMap()
                    ?: throw ApiException.unauthorized403("The Claim '${properties.auth.oauth2.userFederation.resourceAccessClaim}' does not exist.")
                        .printException(properties.auth.oauth2.printErrorsToLog)
                try {
                    (resourceAccess[properties.auth.oauth2.userFederation.resourceName] as HashMap<String, List<String?>?>?)?.get(
                        "roles"
                    )?.filterNotNull()
                } catch (e: Exception) {
                    throw ApiException.unauthorized403("The Claim '${properties.auth.oauth2.userFederation.resourceAccessClaim}' seems not to follow the standard.")
                        .printException(properties.auth.oauth2.printErrorsToLog)
                }
            }
            (!properties.auth.oauth2.userFederation.loadGroupFromRoles) -> {
                jwt.getClaim(properties.auth.oauth2.userFederation.groupClaim).asList(String::class.java)
                    ?: throw ApiException.unauthorized403("The Claim '${properties.auth.oauth2.userFederation.groupClaim}' does not exist.")
                        .printException(properties.auth.oauth2.printErrorsToLog)
            }
            else -> arrayListOf()
        } ?: arrayListOf()
    }
}
