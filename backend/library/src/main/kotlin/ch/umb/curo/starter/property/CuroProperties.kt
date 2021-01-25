package ch.umb.curo.starter.property

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(CuroProperties.PREFIX)
open class CuroProperties {

    companion object {
        const val PREFIX = "curo"
    }

    var frontendEnabled = true
    var printStacktrace = true

    var ignoreObjectType = false

    var auth: CuroAuthProperties = CuroAuthProperties()
    var flowToNext: CuroFlowToNextProperties = CuroFlowToNextProperties()

}
