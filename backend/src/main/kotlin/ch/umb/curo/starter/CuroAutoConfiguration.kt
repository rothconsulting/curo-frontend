package ch.umb.curo.starter

import ch.umb.curo.starter.property.CuroProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@EnableConfigurationProperties(CuroProperties::class)
@Configuration
open class CuroAutoConfiguration {

}
