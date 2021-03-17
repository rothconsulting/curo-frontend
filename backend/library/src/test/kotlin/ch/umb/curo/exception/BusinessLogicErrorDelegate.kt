package ch.umb.curo.exception

import ch.umb.curo.starter.exception.throwBusinessLogicException
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.JavaDelegate
import org.springframework.stereotype.Component

@Component("businessLogicErrorDelegate")
class BusinessLogicErrorDelegate : JavaDelegate {
    override fun execute(execution: DelegateExecution) {
        execution.throwBusinessLogicException(
            "internalErrorMessage",
            "publicMessage",
            null,
            500,
            "ERROR_CODE",
            hashMapOf(Pair("test","123456-789"))
        )
    }
}
