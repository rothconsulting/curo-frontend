package ch.umb.curo.starter.auth

import ch.umb.curo.starter.property.CuroProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.rest.security.auth.AuthenticationProvider
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.servlet.Filter

/**
 * Adds authentication filter to the camunda and curo api.
 *
 * @author itsmefox
 *
 */
@Configuration
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
open class CamundaSecurityFilter(
    val properties: CuroProperties,
    val objectMapper: ObjectMapper
) {

    @Autowired
    lateinit var authenticationProvider: AuthenticationProvider

    private val logger = LoggerFactory.getLogger("ch.umb.curo.starter.CamundaSecurityFilter")

    companion object {
        const val ENGINE_REST_URL: String = "/engine-rest/*"
        val CURO_API_URLS: List<String> = arrayListOf("/curo-api/*")
    }

    @Bean
    @Suppress("UNCHECKED_CAST")
    open fun <T : Filter> processEngineAuthenticationFilter(): FilterRegistrationBean<T> {

        logger.info("CURO: active CuroAuthenticationProvider: ${authenticationProvider::class.java.canonicalName}")

        val registration = FilterRegistrationBean<T>()
        registration.setName("camunda-auth")
        registration.filter = getProcessEngineAuthenticationFilter() as T
        registration.addInitParameter("authentication-provider", authenticationProvider::class.java.canonicalName)
        registration.addUrlPatterns(ENGINE_REST_URL, *CURO_API_URLS.toTypedArray())
        return registration
    }

    @Bean
    open fun getProcessEngineAuthenticationFilter(): Filter {
        return CuroProcessEngineAuthenticationFilter(properties, objectMapper)
    }
}
