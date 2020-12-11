package ch.umb.curo.starter.auth

import org.camunda.bpm.engine.rest.security.auth.AuthenticationProvider

interface CuroLoginMethod: AuthenticationProvider {

    fun getId(): String
    fun getLoginMethodName(): String
    fun useUsernamePassword(): Boolean

}