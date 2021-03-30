package ch.umb.curo.starter

import ch.umb.curo.starter.auth.CuroBasicAuthAuthentication
import ch.umb.curo.starter.auth.CuroOAuth2Authentication
import ch.umb.curo.starter.events.PostDeployListener
import ch.umb.curo.starter.plugin.VariableInitPlugin
import ch.umb.curo.starter.property.CuroProperties
import ch.umb.curo.starter.service.DefaultFlowToNextService
import ch.umb.curo.starter.service.DefaultStartupDataCreationService
import ch.umb.curo.starter.service.FlowToNextService
import ch.umb.curo.starter.service.StartupDataCreationService
import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.impl.cfg.AbstractProcessEnginePlugin
import org.camunda.bpm.engine.rest.security.auth.AuthenticationProvider
import org.camunda.bpm.engine.spring.SpringProcessEngineConfiguration
import org.camunda.bpm.spring.boot.starter.configuration.impl.AbstractCamundaConfiguration
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener

@Configuration
@Import(CuroRestAutoConfiguration::class)
@EnableConfigurationProperties(CuroProperties::class)
@ComponentScan(basePackages = ["ch.umb.curo.starter.*"])
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
open class CuroAutoConfiguration {

    @Autowired
    lateinit var properties: CuroProperties

    @Autowired
    lateinit var context: ConfigurableApplicationContext

    @Bean
    @ConditionalOnMissingBean
    open fun defaultAuthenticationProvider(): AuthenticationProvider {
        return when (properties.auth.type) {
            "basic" -> {
                CuroBasicAuthAuthentication()
            }
            "oauth2" -> {
                CuroOAuth2Authentication()
            }
            else -> CuroBasicAuthAuthentication()
        }
    }

    @Bean
    @ConditionalOnMissingBean
    open fun defaultInitialDataCreationService(): StartupDataCreationService {
        return DefaultStartupDataCreationService(properties)
    }

    @Bean
    @ConditionalOnMissingBean
    open fun defaultFlowToNextService(
        taskService: TaskService,
        historyService: HistoryService
    ): FlowToNextService {
        return DefaultFlowToNextService(properties, taskService, historyService)
    }

    @EventListener
    fun setStringContext(event: ApplicationReadyEvent) {
        SpringContext.applicationContext = context
    }

    @Bean
    open fun curoStartupPlugin(): AbstractCamundaConfiguration {
        return object : AbstractCamundaConfiguration() {
            override fun preInit(processEngineConfiguration: SpringProcessEngineConfiguration) {
                super.preInit(processEngineConfiguration)
                    PostDeployListener(
                        properties,
                        context,
                        processEngineConfiguration
                    ).onPostDeployEvent()
            }
        }
    }

    @Bean
    open fun injectVariableInitPlugin(): AbstractProcessEnginePlugin {
        return VariableInitPlugin(context)
    }

}
