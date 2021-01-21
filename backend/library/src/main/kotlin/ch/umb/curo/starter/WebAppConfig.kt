package ch.umb.curo.starter

import ch.umb.curo.starter.auth.CamundaSecurityFilter
import ch.umb.curo.starter.auth.CuroBasicAuthAuthentication
import org.camunda.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter
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
open class WebAppConfig : ServletContextInitializer {

    lateinit var servletContext: ServletContext

    private val logger = LoggerFactory.getLogger(WebAppConfig::class.java)!!

    override fun onStartup(servletContext: ServletContext) {
        this.servletContext = servletContext

        val filterRegistration = servletContext.addFilter("Authentication Filter", ProcessEngineAuthenticationFilter::class.java)
        filterRegistration.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, CamundaSecurityFilter.ENGINE_REST_URL, CamundaSecurityFilter.CURO_API_URL)
        filterRegistration.initParameters = hashMapOf(Pair("authentication-provider", CuroBasicAuthAuthentication::class.java.canonicalName))
        logger.debug("CURO: Filter {} for URL {} registered.", "Authentication Filter", "${CamundaSecurityFilter.ENGINE_REST_URL}, ${CamundaSecurityFilter.CURO_API_URL}")
    }
}
