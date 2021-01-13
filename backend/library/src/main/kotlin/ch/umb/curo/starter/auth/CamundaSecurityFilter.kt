package ch.umb.curo.starter.auth

import org.camunda.bpm.engine.rest.security.auth.AuthenticationProvider
import org.camunda.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
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
open class CamundaSecurityFilter {

    @Autowired
    @Qualifier("CuroAuthenticationProvider")
    lateinit var authenticationProvider: AuthenticationProvider

    private var logger = LoggerFactory.getLogger(this::class.java)!!

    @Bean
    open fun <T : Filter> processEngineAuthenticationFilter(): FilterRegistrationBean<T> {

        logger.info("Active CuroAuthenticationProvider: ${authenticationProvider::class.java.canonicalName}")

        val registration = FilterRegistrationBean<T>()
        registration.setName("camunda-auth")
        registration.filter = getProcessEngineAuthenticationFilter() as T
        registration.addInitParameter("authentication-provider", authenticationProvider::class.java.canonicalName)
        registration.addUrlPatterns("/rest/*", "/curo-api/*")
        return registration
    }

    @Bean
    open fun getProcessEngineAuthenticationFilter(): Filter {
        return ProcessEngineAuthenticationFilter()
    }
}
