package ch.umb.curo.starter

import ch.umb.curo.starter.interceptor.BasicSuccessInterceptor
import ch.umb.curo.starter.interceptor.Oauth2SuccessInterceptor
import ch.umb.curo.starter.property.CuroProperties
import ch.umb.curo.starter.service.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.*
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
open class CuroRestAutoConfiguration {

    private val logger = LoggerFactory.getLogger(this::class.java)

    @Bean
    @ConditionalOnMissingBean
    open fun defaultCuroMenuService(filterService: FilterService): CuroMenuService {
        return DefaultCuroMenuService(filterService)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun defaultCuroTaskService(
        properties: CuroProperties,
        taskService: TaskService,
        identityService: IdentityService,
        historyService: HistoryService,
        flowToNextService: FlowToNextService,
        filterService: FilterService,
        formService: FormService,
        objectMapper: ObjectMapper
    ): CuroTaskService {
        return DefaultCuroTaskService(
            properties,
            taskService,
            identityService,
            historyService,
            flowToNextService,
            filterService,
            formService,
            objectMapper
        )
    }

    @Bean
    @ConditionalOnMissingBean
    open fun defaultCuroProcessInstanceService(
        properties: CuroProperties,
        runtimeService: RuntimeService,
        repositoryService: RepositoryService,
        identityService: IdentityService,
        flowToNextService: FlowToNextService,
    ): CuroProcessInstanceService {
        return DefaultCuroProcessInstanceService(
            properties,
            runtimeService,
            repositoryService,
            flowToNextService,
            identityService
        )
    }

    @Bean
    @ConditionalOnMissingBean
    open fun defaultCuroUserService(identityService: IdentityService): CuroUserService {
        return DefaultCuroUserService(identityService)
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "curo", value = ["auth.type"], havingValue = "oauth2")
    open fun defaultCuroOauth2AuthenticationService(
        oauth2SuccessInterceptors: List<Oauth2SuccessInterceptor>,
        properties: CuroProperties,
        identityService: IdentityService,
        authorizationService: AuthorizationService,
        repositoryService: RepositoryService
    ): CuroAuthenticationService {
        logger.debug("CURO: use DefaultCuroOauth2AuthenticationService as auth type is oauth2")
        return DefaultCuroOauth2AuthenticationService(
            oauth2SuccessInterceptors,
            properties,
            identityService,
            authorizationService
        )
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "curo", value = ["auth.type"], havingValue = "basic", matchIfMissing = true)
    open fun defaultCuroBasicAuthenticationService(
        basicSuccessInterceptors: List<BasicSuccessInterceptor>,
        properties: CuroProperties,
        identityService: IdentityService,
        authorizationService: AuthorizationService,
        repositoryService: RepositoryService
    ): CuroAuthenticationService {
        logger.debug("CURO: use DefaultCuroBasicAuthenticationService as auth type is basic")
        return DefaultCuroBasicAuthenticationService(
            basicSuccessInterceptors,
            properties,
            identityService,
            authorizationService
        )
    }

}
