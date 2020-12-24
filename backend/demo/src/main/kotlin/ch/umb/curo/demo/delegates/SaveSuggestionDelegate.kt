package ch.umb.curo.demo.delegates

import ch.umb.curo.demo.camunda.CamundaVariables
import ch.umb.solutions.curo.libraries.sharedprocess.camunda.CamundaVariableHelper
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named("saveSuggestion")
class SaveSuggestionDelegate : JavaDelegate {
    private val logger = LoggerFactory.getLogger(SaveSuggestionDelegate::class.java)

    override fun execute(delegateExecution: DelegateExecution?) {
        logger.info("Current activity: " + (delegateExecution!!.currentActivityName) + "...")

        val camundaVariableHelper = CamundaVariableHelper(delegateExecution)
        val title = camundaVariableHelper[CamundaVariables.TITLE]
        val category = camundaVariableHelper.getOrNull(CamundaVariables.CATEGORY)
        val description = camundaVariableHelper.getOrNull(CamundaVariables.DESCRIPTION)
        val url = camundaVariableHelper.getOrNull(CamundaVariables.URL)

        logger.info("\tSave:")
        logger.info("\t\tTitle: $title")
        logger.info("\t\tCategory: $category")
        logger.info("\t\tDescription: $description")
        logger.info("\t\tURL: $url")
    }
}



