package ch.umb.curo.starter

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springdoc.core.GroupedOpenApi
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnClass(OpenAPI::class)
open class OpenApiConfiguration {

    @Bean
    open fun publicApi(): GroupedOpenApi {
        return GroupedOpenApi.builder()
            .packagesToScan("ch.umb.curo.starter.controller")
            .group("curo-api")
            .pathsToMatch("/curo-api/**")
            .build()
    }

    @Bean
    open fun curoOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(Info().title("Curo API")
                      .description("")
                      .version("v0.0.1"))
            .components(
                Components()
                    .addSecuritySchemes("CuroBasic",
                                        SecurityScheme()
                                            .name("CuroBasic")
                                            .type(SecurityScheme.Type.HTTP)
                                            .scheme("basic"))
                       )
    }

}
