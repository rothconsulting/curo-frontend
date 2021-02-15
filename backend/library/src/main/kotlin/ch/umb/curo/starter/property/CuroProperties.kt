package ch.umb.curo.starter.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty


@ConfigurationProperties(CuroProperties.PREFIX)
open class CuroProperties {

    companion object {
        const val PREFIX = "curo"
    }

    var frontendEnabled = true
    var printStacktrace = true

    var ignoreObjectType = false
    var camundaTelemetry: Boolean? = null

    /**
     * Shortcut for camunda.bpm.generic-properties.properties.userResourceWhitelistPattern
     * *If email is used for id please use this pattern: '[a-zA-Z0-9-.@_]+'*
     */
    var camundaUserIdPattern: String? = null

    @NestedConfigurationProperty
    var auth: CuroAuthProperties = CuroAuthProperties()

    @NestedConfigurationProperty
    var flowToNext: CuroFlowToNextProperties = CuroFlowToNextProperties()

}
