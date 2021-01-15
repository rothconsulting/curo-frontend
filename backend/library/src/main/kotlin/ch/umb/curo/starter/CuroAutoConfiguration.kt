package ch.umb.curo.starter

import ch.umb.curo.starter.auth.CuroBasicAuthAuthentication
import ch.umb.curo.starter.auth.CuroOAuth2Authentication
import ch.umb.curo.starter.property.CuroProperties
import org.camunda.bpm.engine.rest.security.auth.AuthenticationProvider
import org.camunda.bpm.spring.boot.starter.CamundaBpmAutoConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigureAfter
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties(CuroProperties::class)
@Configuration
@ComponentScan(basePackages = ["ch.umb.curo.starter.*"])
open class CuroAutoConfiguration {

    @Autowired
    lateinit var properties: CuroProperties

    @Bean("CuroAuthenticationProvider")
    @ConditionalOnMissingBean(name = ["CuroAuthenticationProvider"])
    open fun defaultAuthenticationProvider(): AuthenticationProvider {
        when (properties.auth.type) {
            "basic" -> {
                return CuroBasicAuthAuthentication()
            }
            "oauth2" -> {
                return CuroOAuth2Authentication()
            }
            else -> return CuroBasicAuthAuthentication()
        }
    }

}
