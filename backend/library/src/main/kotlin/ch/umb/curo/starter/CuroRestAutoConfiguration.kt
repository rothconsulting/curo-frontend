package ch.umb.curo.starter

import ch.umb.curo.starter.interceptor.AuthSuccessInterceptor
import ch.umb.curo.starter.property.CuroProperties
import ch.umb.curo.starter.service.*
import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.*
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
open class CuroRestAutoConfiguration {

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
        identityService: IdentityService,
        flowToNextService: FlowToNextService,
    ): CuroProcessInstanceService {
        return DefaultCuroProcessInstanceService(
            properties,
            runtimeService,
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
    open fun defaultCuroAuthenticationService(
        authSuccessInterceptors: List<AuthSuccessInterceptor>,
        properties: CuroProperties
    ): CuroAuthenticationService {
        return DefaultCuroAuthenticationService(authSuccessInterceptors, properties)
    }

}
