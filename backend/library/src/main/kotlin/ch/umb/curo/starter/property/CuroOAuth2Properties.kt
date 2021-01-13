
package ch.umb.curo.starter.property

class CuroOAuth2Properties {

    var verifyJwt: Boolean = true
    var allowedIssuers: ArrayList<String> = arrayListOf()
    var jwkUrl: String = ""
    var userIdClaim: String = "email"
    var printErrorsToLog: Boolean = false

}
