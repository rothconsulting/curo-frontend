package ch.umb.curo.demo.delegates

import ch.umb.curo.demo.CamundaVariables
import ch.umb.curo.starter.helper.camunda.variableHelper
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("informSoftwareArchitect")
class InformSoftwareArchitectDelegate : JavaDelegate {
    private val logger = LoggerFactory.getLogger(InformSoftwareArchitectDelegate::class.java)!!

    override fun execute(delegateExecution: DelegateExecution) {
        logger.info("Current activity: ${delegateExecution.currentActivityName} ...")

        val title = delegateExecution.variableHelper()[CamundaVariables.title]
        logger.info("Send suggestion '$title' to software architect...")
    }
}
