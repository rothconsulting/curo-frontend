package ch.umb.curo.starter.auth

import org.camunda.bpm.engine.ProcessEngine
import org.camunda.bpm.engine.rest.security.auth.ProcessEngineAuthenticationFilter
import org.springframework.boot.web.servlet.FilterRegistrationBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.util.*
import javax.servlet.Filter

@Configuration
open class CamundaSecurityFilter {

    @Bean
    open fun <T : Filter> processEngineAuthenticationFilter(): FilterRegistrationBean<T> {
        val registration = FilterRegistrationBean<T>()
        registration.setName("camunda-auth")
        registration.filter = getProcessEngineAuthenticationFilter() as T
        registration.addInitParameter("authentication-provider", CuroBasicAuthAuthentication::class.java.canonicalName)
        registration.addUrlPatterns("/rest/*", "/curo-api/*")
        return registration
    }

    @Bean
    open fun getProcessEngineAuthenticationFilter(): Filter {
        return ProcessEngineAuthenticationFilter()
    }
}
