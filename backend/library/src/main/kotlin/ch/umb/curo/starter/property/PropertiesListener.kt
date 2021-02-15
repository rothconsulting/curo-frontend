package ch.umb.curo.starter.property

import ch.umb.curo.starter.exception.CuroPropertyException
import org.springframework.boot.context.event.ApplicationContextInitializedEvent
import org.springframework.context.ApplicationListener
import java.net.URL

class PropertiesListener : ApplicationListener<ApplicationContextInitializedEvent> {

    override fun onApplicationEvent(event: ApplicationContextInitializedEvent) {
        val authType = event.applicationContext.environment.getProperty("curo.auth.type") ?: "basic"

        //allowed-issuers needs to be set if oauth2 is active
        val allowedIssuer = event.applicationContext.environment.getProperty("curo.auth.oauth2.allowed-issuers[0]") ?: ""
        if (authType == "oauth2" && allowedIssuer.isBlank()) {
            throw CuroPropertyException("Curo configuration is missing a needed parameter",
                                        "curo.auth.oauth2.allowed-issuers",
                                        "",
                                        "At least one issuer needs to be allowed if oauth2 is active")
        }

        //jwk-url needs to be set if verify-jwt is true
        val verifyJwt = event.applicationContext.environment.getProperty("curo.auth.oauth2.verify-jwt", Boolean::class.java) ?: false
        val jwkUrl = event.applicationContext.environment.getProperty("curo.auth.oauth2.jwk-url") ?: ""
        if (authType == "oauth2" && verifyJwt && jwkUrl.isEmpty()) {
            throw CuroPropertyException("Curo configuration is missing a needed parameter", "curo.auth.oauth2.jwt-url", "", "jwk-url needs to be set if verify-jwt is active")
        }

        //jwk-url need to be valid
        if (authType == "oauth2" && verifyJwt && !isValidURL(jwkUrl)) {
            throw CuroPropertyException("Curo configuration has wrong parameter", "curo.auth.oauth2.jwt-url", jwkUrl, "jwk-url needs to be a valid url")
        }

        //resourceName needs to be set if user-federation & role usage is active
        val userFederationEnabled = event.applicationContext.environment.getProperty("curo.auth.oauth2.user-federation.enabled", Boolean::class.java) ?: false
        val resourceName = event.applicationContext.environment.getProperty("curo.auth.oauth2.user-federation.resource-name") ?: ""
        if (authType == "oauth2" && userFederationEnabled && resourceName.isEmpty()) {
            throw CuroPropertyException("Curo configuration is missing a needed parameter",
                                        "curo.auth.oauth2.user-federation.resource-name",
                                        "",
                                        "resource-name needs to be set if user-federation is enabled")
        }

    }

    private fun isValidURL(url: String): Boolean {
        return try {
            URL(url).toURI()
            true
        } catch (e: Exception) {
            false
        }
    }
}
