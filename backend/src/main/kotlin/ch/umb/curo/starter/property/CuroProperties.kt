package ch.umb.curo.starter.property

import org.springframework.boot.context.properties.ConfigurationProperties


@ConfigurationProperties(CuroProperties.PREFIX)
class CuroProperties {

    companion object {
        const val PREFIX = "curo"
    }

    val frontendEnabled = true

}