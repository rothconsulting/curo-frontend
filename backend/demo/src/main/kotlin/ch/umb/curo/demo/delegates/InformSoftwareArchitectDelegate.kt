package ch.umb.curo.demo.delegates

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.inject.Named

@Component("informSoftwareArchitect")
class InformSoftwareArchitectDelegate : JavaDelegate {
    private val logger = LoggerFactory.getLogger(InformSoftwareArchitectDelegate::class.java)

    override fun execute(delegateExecution: DelegateExecution?) {
        logger.info("Current activity: " + (delegateExecution!!.currentActivityName) + "...")

        val title = delegateExecution.getVariable("title")

        logger.info("Send suggestion '$title' to software architect...")
    }
}
