package ch.umb.curo.demo.delegates

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named("informSoftwareArchitect")
class InformSoftwareArchitectDelegate : JavaDelegate {
    private val logger = LoggerFactory.getLogger(InformSoftwareArchitectDelegate::class.java)

    override fun execute(delegateExecution: DelegateExecution?) {
        logger.info("Running current activity: " + (delegateExecution!!.currentActivityName))
    }
}
