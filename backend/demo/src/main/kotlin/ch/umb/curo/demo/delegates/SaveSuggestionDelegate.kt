package ch.umb.curo.demo.delegates

import ch.umb.curo.demo.CamundaVariables
import ch.umb.curo.starter.helper.camunda.variableHelper
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component("saveSuggestion")
class SaveSuggestionDelegate : JavaDelegate {
    private val logger = LoggerFactory.getLogger(SaveSuggestionDelegate::class.java)!!

    override fun execute(delegateExecution: DelegateExecution) {
        logger.info("Current activity: ${delegateExecution.currentActivityName} ...")

        val title: String = delegateExecution.variableHelper()[CamundaVariables.title]
        val category: String = delegateExecution.variableHelper()[CamundaVariables.category]
        val description: String = delegateExecution.variableHelper()[CamundaVariables.description]
        val url: String = delegateExecution.variableHelper()[CamundaVariables.url]
        val comments: String = delegateExecution.variableHelper()[CamundaVariables.comments]

        logger.info("\tSave:")
        logger.info("\t\tTitle: $title")
        logger.info("\t\tCategory: $category")
        logger.info("\t\tDescription: $description")
        logger.info("\t\tURL: $url")
        logger.info("\t\tComments from software architect: $comments")
    }
}



