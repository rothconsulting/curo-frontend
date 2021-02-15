
package ch.umb.curo.starter.property

import org.springframework.boot.context.properties.NestedConfigurationProperty

class CuroOAuth2Properties {

    var verifyJwt: Boolean = true
    var allowedIssuers: ArrayList<String> = arrayListOf()
    var jwkUrl: String = ""
    var userIdClaim: String = "email"
    var printErrorsToLog: Boolean = false

    @NestedConfigurationProperty
    var userFederation: CuroUserFederationProperties = CuroUserFederationProperties()

}
