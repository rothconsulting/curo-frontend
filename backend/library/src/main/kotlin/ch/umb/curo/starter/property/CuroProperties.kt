package ch.umb.curo.starter.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.NestedConfigurationProperty

@ConfigurationProperties(CuroProperties.PREFIX)
open class CuroProperties {

    companion object {
        const val PREFIX = "curo"
    }

    /**
     * Should Curo print stacktraces to the log and rest responses
     */
    var printStacktrace = true

    /**
     * If active, Curo will override object variables with json value if the new value does not match the object type of the variable.
     */
    var ignoreObjectType = false

    /**
     * If set, Curo will override the default serialization format to application/json
     */
    var setDefaultSerializationFormat: Boolean = true

    /**
     * If set, Curo will define the camunda telemetry to that value
     */
    var camundaTelemetry: Boolean? = null

    /**
     * Define users which Curo should create on startup.
     * If the user already exists, only the groups are assigned.
     */
    var initialUsers: ArrayList<CuroInitialUserProperty>? = null

    /**
     * Define groups which Curo should create on startup.
     * If group already exists, Curo will not create it again.
     */
    var initialGroups: List<CuroInitialGroupProperty>? = null

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

    /**
     * Location in which Curo searches for initial filter json files.
     */
    var initialFilterLocation: String = "classpath*:/filters/*"

    /**
     * Defines if Curo creates filter during startup.
     */
    var createInitialFilters: Boolean = true

    /**
     * Curo authentication
     */
    @NestedConfigurationProperty
    var auth: CuroAuthProperties = CuroAuthProperties()

    /**
     * Curo FlowToNext
     */
    @NestedConfigurationProperty
    var flowToNext: CuroFlowToNextProperties = CuroFlowToNextProperties()

}
