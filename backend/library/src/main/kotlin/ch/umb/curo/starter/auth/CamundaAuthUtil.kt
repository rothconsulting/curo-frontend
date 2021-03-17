package ch.umb.curo.starter.auth

import org.camunda.bpm.engine.IdentityService
import java.util.concurrent.Callable

object CamundaAuthUtil {

    fun <T> Callable<T>.callWithoutAuthentication(identityService: IdentityService): T {
        return runWithoutAuthentication(this, identityService)
    }

    fun <T> runWithoutAuthentication(request: Callable<T>, identityService: IdentityService): T {
        val currentUser = identityService.currentAuthentication
        identityService.clearAuthentication()
        val output: T = request.call()
        identityService.setAuthentication(currentUser)
        return output
    }

}
