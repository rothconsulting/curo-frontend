package ch.umb.curo.demo.delegates

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import javax.inject.Named

@Component("saveSuggestion")
class SaveSuggestionDelegate : JavaDelegate {
    private val logger = LoggerFactory.getLogger(SaveSuggestionDelegate::class.java)

    override fun execute(delegateExecution: DelegateExecution?) {
        logger.info("Current activity: " + (delegateExecution!!.currentActivityName) + "...")

        val title = delegateExecution.getVariable("title")
        val category = delegateExecution.getVariable("category")
        val description = delegateExecution.getVariable("description")
        val url = delegateExecution.getVariable("url")
        val comments = delegateExecution.getVariable("comments")

        logger.info("\tSave:")
        logger.info("\t\tTitle: $title")
        logger.info("\t\tCategory: $category")
        logger.info("\t\tDescription: $description")
        logger.info("\t\tURL: $url")
        logger.info("\t\tComments from software architect: $comments")
    }
}



