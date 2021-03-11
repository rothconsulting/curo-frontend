package ch.umb.curo.starter.auth

import org.camunda.bpm.engine.rest.util.EngineUtil
import java.util.concurrent.Callable

object CamundaAuthUtil {

    fun <T> Callable<T>.callWithoutAuthentication(engineName: String? = null): T {
        return runWithoutAuthentication(this, engineName)
    }

    fun <T> runWithoutAuthentication(request: Callable<T>, engineName: String? = null): T {
        val identityService = EngineUtil.lookupProcessEngine(engineName).identityService
        val currentUser = identityService.currentAuthentication
        identityService.clearAuthentication()
        val output: T = request.call()
        identityService.setAuthentication(currentUser)
        return output
    }

}
