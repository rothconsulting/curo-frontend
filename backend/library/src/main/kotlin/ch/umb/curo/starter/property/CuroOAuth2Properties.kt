
package ch.umb.curo.starter.property

import org.springframework.boot.context.properties.NestedConfigurationProperty

class CuroOAuth2Properties {

    /**
     * Allowed issuer (iss)
     */
    var allowedIssuers: ArrayList<String> = arrayListOf()

    /**
     * Should Curo check the signature against the defined public key
     */
    var verifyJwt: Boolean = true

    /**
     * JWK Url to use for signature checking
     */
    var jwkUrl: String = ""

    /**
     * Claim which is used to check against Camunda (user id)
     * Common claims: email, preferred_username
     */
    var userIdClaim: String = "email"

    /**
     * Print warnings for wrong tokens and verification failures
     */
    var printErrorsToLog: Boolean = false

    /**
     * Curo user federation
     */
    @NestedConfigurationProperty
    var userFederation: CuroUserFederationProperties = CuroUserFederationProperties()

}
