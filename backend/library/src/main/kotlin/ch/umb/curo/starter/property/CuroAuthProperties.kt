package ch.umb.curo.starter.property

import org.springframework.boot.context.properties.NestedConfigurationProperty

class CuroAuthProperties {

    var type: String = "basic"

    @NestedConfigurationProperty
    var oauth2: CuroOAuth2Properties = CuroOAuth2Properties()

}
