package ch.umb.curo.starter.exception

import org.springframework.boot.diagnostics.AbstractFailureAnalyzer
import org.springframework.boot.diagnostics.FailureAnalysis

class CuroPropertyExceptionFailureAnalyzer : AbstractFailureAnalyzer<CuroPropertyException>() {
    override fun analyze(rootFailure: Throwable, cause: CuroPropertyException): FailureAnalysis {
        val description = "${cause.description}\n\n" +
                "\tProperty: ${cause.property}\n" +
                "\tValue: ${cause.value}\n" +
                "\tReason: ${cause.reason}"
        return FailureAnalysis(description, "Update your application configuration", cause)
    }
}
