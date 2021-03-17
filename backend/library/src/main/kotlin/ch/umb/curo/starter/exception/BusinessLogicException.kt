package ch.umb.curo.starter.exception

import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.camunda.bpm.engine.delegate.DelegateTask
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.logging.LogLevel

/**
 * Provides a base [runtime exception][RuntimeException] which represents a logical error in a process.
 * This exception will be returned over rest to the calling client if possible.
 *
 * @author itsmefox
 */
open class BusinessLogicException(
    /**
     * Internal error message.
     * This message is shown together with the errorCode in the incident.
     * Will not be visible if `showOnApi` is set to false.
     */
    val internalErrorMessage: String,
    /**
     * Public message which can be shown to the user.
     * Will not be visible if `showOnApi` is set to false.
     */
    val publicMessage: String = "",
    /**
     * The underlying cause (which is saved for later retrieval by the [Throwable.getCause()] method).
     * (A `null` value is permitted, and indicates that the cause is nonexistent or unknown.)
     */
    cause: Throwable? = null,
    /**
     * UUID of the execution.
     */
    var execution: String = "",
    /**
     * Status code which is shown if this exception is returned from the rest controller.
     */
    val statusCode: Int = 500,
    /**
     * Error code.
     * This error code is shown together with the internal error message in the incident.
     */
    val errorCode: String = "BUSINESS_ERROR",
    /**
     * Generic data set.
     * This data set will always contain a camunda key with some related data.
     * Will not be visible if `showOnApi` is set to false.
     */
    var data: Map<String, Any>? = null,
    /**
     * Defines if `internalErrorMessage`, `publicMessage`, `data` is part of the rest response.
     */
    val showOnApi: Boolean = true,
    /**
     * Defines if a frontend should show this error or not.
     */
    val showOnFrontend: Boolean = true,
    /**
     * Defines if failed action can be repeated.
     */
    val isRepeatable: Boolean = true
) : RuntimeException("$errorCode: $internalErrorMessage", cause) {

    /**
     * Prints this execution and its related data in a human readable way.
     *
     * @param printDataToLog defines if the associated data is printed to the log
     * @param printStacktrace defines if the stacktrace is printed to the log
     * @param logger the logger which is used
     * @param logLevel the log level on which the exception is logged
     * @return the exception itself
     */
    fun printException(
        printDataToLog: Boolean = true,
        printStacktrace: Boolean = true,
        logger: Logger? = null,
        logLevel: LogLevel = LogLevel.ERROR
    ): BusinessLogicException {
        @Suppress("NAME_SHADOWING")
        val logger = logger ?: LoggerFactory.getLogger(this::class.java)!!

        if (printDataToLog) {
            printOnLogLevel(
                "BusinessLogic-Exception: -> ${this.publicMessage}\n" +
                        "\tInternalErrorMessage: ${this.internalErrorMessage}\n" +
                        "\tExecution: ${this.execution}\n" +
                        "\tCode: ${this.errorCode}\n" +
                        "\tData: ${this.data?.let { ObjectMapper().writeValueAsString(this.data) }}\n" +
                        "\tshowOnApi: ${this.showOnApi}\n" +
                        "\tstatusCode: ${this.statusCode}\n" +
                        "\tshowOnFrontend: ${this.showOnFrontend}\n" +
                        "\tisRepeatable: ${this.isRepeatable}", logger, logLevel
            )
        }
        if (printStacktrace) {
            this.cause?.printStackTrace()
        }

        return this
    }

    private fun printOnLogLevel(message: String, logger: Logger, logLevel: LogLevel) {
        when (logLevel) {
            LogLevel.TRACE -> logger.trace(message)
            LogLevel.DEBUG -> logger.debug(message)
            LogLevel.INFO -> logger.info(message)
            LogLevel.WARN -> logger.warn(message)
            LogLevel.ERROR -> logger.error(message)
            LogLevel.FATAL -> logger.error(message)
            LogLevel.OFF -> {
            } //No nothing
        }
    }

    class CamundaData(
        val processDefinitionId: String,
        val processInstanceId: String,
        val taskDefinitionKey: String,
        val taskName: String
    )

}

/**
 * Throws an existing [BusinessLogicException] based on this [DelegateTask]
 *
 * @param businessLogicException businessLogicException which will be thrown
 * @param printDataToLog defines if the associated data is printed to the log
 * @param printStacktrace defines if the stacktrace is printed to the log
 * @param logger the logger which is used
 * @param logLevel the log level on which the exception is logged
 */
fun DelegateTask.throwBusinessLogicException(
    businessLogicException: BusinessLogicException,
    printDataToLog: Boolean = true,
    printStacktrace: Boolean = true,
    logger: Logger? = null,
    logLevel: LogLevel = LogLevel.ERROR
): Nothing {
    val extendedData: HashMap<String, Any> = businessLogicException.data as HashMap<String, Any>? ?: hashMapOf()

    extendedData["camunda"] = BusinessLogicException.CamundaData(
        this.processDefinitionId,
        this.processInstanceId,
        this.taskDefinitionKey,
        this.name
    )

    businessLogicException.data = extendedData
    businessLogicException.execution = this.executionId

    throw businessLogicException.printException(printDataToLog, printStacktrace, logger, logLevel)
}

/**
 * Throws a new [BusinessLogicException] based on this [DelegateTask]
 *
 * @see [BusinessLogicException]
 * @param printDataToLog defines if the associated data is printed to the log
 * @param printStacktrace defines if the stacktrace is printed to the log
 * @param logger the logger which is used
 * @param logLevel the log level on which the exception is logged
 */
fun DelegateTask.throwBusinessLogicException(
    internalErrorMessage: String,
    publicMessage: String = "",
    cause: Throwable? = null,
    statusCode: Int = 500,
    errorCode: String = "BUSINESS_ERROR",
    data: Map<String, Any>? = null,
    showOnApi: Boolean = true,
    showOnFrontend: Boolean = true,
    isRepeatable: Boolean = true,
    printDataToLog: Boolean = true,
    printStacktrace: Boolean = true,
    logger: Logger? = null,
    logLevel: LogLevel = LogLevel.ERROR
): Nothing {

    val extendedData: HashMap<String, Any> = data as HashMap<String, Any>? ?: hashMapOf()

    extendedData["camunda"] = BusinessLogicException.CamundaData(
        this.processDefinitionId,
        this.processInstanceId,
        this.taskDefinitionKey,
        this.name
    )

    throw BusinessLogicException(
        internalErrorMessage,
        publicMessage,
        cause,
        this.executionId,
        statusCode,
        errorCode,
        extendedData,
        showOnApi,
        showOnFrontend,
        isRepeatable
    ).printException(printDataToLog, printStacktrace, logger, logLevel)
}

/**
 * Throws an existing [BusinessLogicException] based on this [DelegateExecution]
 *
 * @param businessLogicException businessLogicException which will be thrown
 * @param printDataToLog defines if the associated data is printed to the log
 * @param printStacktrace defines if the stacktrace is printed to the log
 * @param logger the logger which is used
 * @param logLevel the log level on which the exception is logged
 */
fun DelegateExecution.throwBusinessLogicException(
    businessLogicException: BusinessLogicException,
    printDataToLog: Boolean = true,
    printStacktrace: Boolean = true,
    logger: Logger? = null,
    logLevel: LogLevel = LogLevel.ERROR
): Nothing {
    val extendedData: HashMap<String, Any> = businessLogicException.data as HashMap<String, Any>? ?: hashMapOf()

    extendedData["camunda"] = BusinessLogicException.CamundaData(
        this.processDefinitionId,
        this.processInstanceId,
        this.currentActivityId,
        this.currentActivityName
    )

    businessLogicException.data = extendedData
    businessLogicException.execution = this.id

    throw businessLogicException.printException(printDataToLog, printStacktrace, logger, logLevel)
}

/**
 * Throws a new [BusinessLogicException] based on this [DelegateExecution]
 *
 * @see [BusinessLogicException]
 * @param printDataToLog defines if the associated data is printed to the log
 * @param printStacktrace defines if the stacktrace is printed to the log
 * @param logger the logger which is used
 * @param logLevel the log level on which the exception is logged
 */
fun DelegateExecution.throwBusinessLogicException(
    internalErrorMessage: String,
    publicMessage: String = "",
    cause: Throwable? = null,
    statusCode: Int = 500,
    errorCode: String = "BUSINESS_ERROR",
    data: Map<String, Any>? = null,
    showOnApi: Boolean = true,
    showOnFrontend: Boolean = true,
    isRepeatable: Boolean = true,
    printDataToLog: Boolean = true,
    printStacktrace: Boolean = true,
    logger: Logger? = null,
    logLevel: LogLevel = LogLevel.ERROR
): Nothing {

    val extendedData: HashMap<String, Any> = data as HashMap<String, Any>? ?: hashMapOf()

    extendedData["camunda"] = BusinessLogicException.CamundaData(
        this.processDefinitionId,
        this.processInstanceId,
        this.currentActivityId,
        this.currentActivityName
    )

    throw BusinessLogicException(
        internalErrorMessage,
        publicMessage,
        cause,
        this.id,
        statusCode,
        errorCode,
        extendedData,
        showOnApi,
        showOnFrontend,
        isRepeatable
    ).printException(printDataToLog, printStacktrace, logger, logLevel)
}


