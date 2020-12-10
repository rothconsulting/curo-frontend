package ch.umb.curo.starter

import ch.umb.curo.starter.controller.TaskController
import ch.umb.curo.starter.property.CuroProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.swagger2.annotations.EnableSwagger2

@EnableConfigurationProperties(CuroProperties::class)
@Configuration
@EnableSwagger2
open class CuroAutoConfiguration {

}
