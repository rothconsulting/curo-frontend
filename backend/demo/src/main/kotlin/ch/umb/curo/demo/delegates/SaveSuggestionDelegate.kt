package ch.umb.curo.demo.delegates

import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.slf4j.LoggerFactory
import javax.inject.Named

@Named("saveSuggestion")
class SaveSuggestionDelegate : JavaDelegate{
    private val logger = LoggerFactory.getLogger(SaveSuggestionDelegate::class.java)

    override fun execute(delegateExecution: DelegateExecution?) {
        logger.info("Running current activity: " + (delegateExecution!!.currentActivityName))
    }
}



