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
     * Define users which Curo should create on startup.
     */
    var initialUsers: ArrayList<CuroInitialUserProperty>? = null

    /**
     * Define groups which Curo should create on startup.
     * If group already exists, Curo will not create it again.
     */
    var initialGroups: List<String>? = null

    /**
     * Shortcut for camunda.bpm.generic-properties.properties.userResourceWhitelistPattern
     * *If email is used for id please use this pattern: '[a-zA-Z0-9-.@_]+'*
     */
    var camundaUserIdPattern: String? = null

    /**
     * Shortcut for camunda.bpm.generic-properties.properties.groupResourceWhitelistPattern
     * *Recommended pattern: '[a-zA-Z0-9-.@_]+'*
     */
    var camundaGroupIdPattern: String? = null

    @NestedConfigurationProperty
    var auth: CuroAuthProperties = CuroAuthProperties()

    @NestedConfigurationProperty
    var flowToNext: CuroFlowToNextProperties = CuroFlowToNextProperties()

}
