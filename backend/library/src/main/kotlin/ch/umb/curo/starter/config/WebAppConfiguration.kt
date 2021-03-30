package ch.umb.curo.starter.config

import ch.umb.curo.starter.CuroAutoConfiguration
import ch.umb.curo.starter.auth.CamundaSecurityFilter
import ch.umb.curo.starter.auth.CuroBasicAuthAuthentication
import ch.umb.curo.starter.auth.CuroProcessEngineAuthenticationFilter
import ch.umb.curo.starter.property.CuroProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication
import org.springframework.boot.web.servlet.ServletContextInitializer
import org.springframework.context.annotation.Configuration
import java.util.*
import javax.servlet.DispatcherType
import javax.servlet.ServletContext

/**
 * Registers default ProcessEngineAuthenticationFilter if Camunda Webapp is not present.
 *
 * @author itsmefox
 *
 */
@Configuration
@ConditionalOnMissingClass("org.camunda.bpm.spring.boot.starter.webapp.CamundaBpmWebappAutoConfiguration")
@ConditionalOnWebApplication
@AutoConfigureAfter(CuroAutoConfiguration::class)
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
open class WebAppConfiguration(
    val properties: CuroProperties,
    val objectMapper: ObjectMapper
) : ServletContextInitializer {

    lateinit var servletContext: ServletContext


    private val logger = LoggerFactory.getLogger(WebAppConfiguration::class.java)!!

    override fun onStartup(servletContext: ServletContext) {
        this.servletContext = servletContext

        val filterRegistration =
            servletContext.addFilter(
                "Authentication Filter",
                CuroProcessEngineAuthenticationFilter(properties, objectMapper)
            )
        filterRegistration.addMappingForUrlPatterns(
            EnumSet.of(DispatcherType.REQUEST),
            true,
            CamundaSecurityFilter.ENGINE_REST_URL,
            *CamundaSecurityFilter.CURO_API_URLS.toTypedArray()
        )
        filterRegistration.initParameters =
            hashMapOf(Pair("authentication-provider", CuroBasicAuthAuthentication::class.java.canonicalName))
        logger.debug(
            "CURO: Filter {} for URL {} registered.",
            "Authentication Filter",
            "${CamundaSecurityFilter.ENGINE_REST_URL}, ${CamundaSecurityFilter.CURO_API_URLS.joinToString(",") { it }}"
        )
    }
}
