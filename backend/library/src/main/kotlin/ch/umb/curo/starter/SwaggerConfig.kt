package ch.umb.curo.starter

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.BasicAuth
import springfox.documentation.service.SecurityReference
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spi.service.contexts.SecurityContext
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2
import java.util.*

@Configuration
@EnableSwagger2
open class SwaggerConfig {
    @Bean
    open fun basicAuthSecuredApi(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .groupName("Curo-Service")
                .select()
                .apis(RequestHandlerSelectors.basePackage("ch.umb.curo.starter.controller"))
                .paths(PathSelectors.any())
                .build()
                .securitySchemes(Collections.singletonList(BasicAuth("CuroBasic")))
                .securityContexts(Collections.singletonList(xBasicSecurityContext()))
    }

    private fun xBasicSecurityContext(): SecurityContext {
        return SecurityContext.builder()
                .securityReferences(Collections.singletonList(
                        SecurityReference("CuroBasic", arrayOfNulls(0))))
                .build()
    }
}
