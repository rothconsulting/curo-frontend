package ch.umb.curo.starter.models.auth

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
class OpenidConfiguration(
    @JsonProperty("issuer")
    val issuer: String = "",
    @JsonProperty("jwks_uri")
    val jwksUri: String = ""
)
