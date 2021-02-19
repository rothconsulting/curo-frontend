package ch.umb.curo.starter.property

import org.springframework.boot.context.properties.NestedConfigurationProperty

class CuroAuthProperties {

    /**
     * Type of authentication
     * supported by Curo: basic, oauth2
     */
    var type: String = "basic"

    /**
     * OAuth2 configuration
     */
    @NestedConfigurationProperty
    var oauth2: CuroOAuth2Properties = CuroOAuth2Properties()

}
