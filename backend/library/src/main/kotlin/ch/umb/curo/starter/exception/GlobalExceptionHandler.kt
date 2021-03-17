package ch.umb.curo.starter.exception

import ch.umb.curo.starter.exception.details.BusinessErrorModel
import ch.umb.curo.starter.exception.details.DefaultErrorModel
import ch.umb.curo.starter.property.CuroProperties
import com.fasterxml.jackson.databind.ObjectMapper
import org.joda.time.DateTime
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.io.PrintWriter
import java.io.StringWriter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@ControllerAdvice
class GlobalExceptionHandler(
    private var properties: CuroProperties,
    private val mapper: ObjectMapper
) {

    @ExceptionHandler(ApiException::class)
    fun handleApiException(apiException: ApiException, request: HttpServletRequest, response: HttpServletResponse) {
        val errorResponse = DefaultErrorModel(
            DateTime.now(),
            apiException.errorCode.httpMapping,
            apiException.errorCode.defaultMessage,
            apiException.curoErrorCode?.name,
            if (properties.printStacktrace) apiException.cause?.let { stackTraceToString(it) } ?: "" else "",
            apiException.message ?: "",
            request.servletPath)

        response.status = apiException.errorCode.httpMapping
        response.contentType = "application/json"
        response.writer.write(mapper.writeValueAsString(errorResponse))
    }

    @ExceptionHandler(BusinessLogicException::class)
    fun handleBusinessLogicException(
        businessLogicException: BusinessLogicException,
        request: HttpServletRequest,
        response: HttpServletResponse
    ) {
        val errorResponse = BusinessErrorModel(
            DateTime.now(),
            businessLogicException.statusCode,
            if (businessLogicException.showOnApi) businessLogicException.internalErrorMessage else "",
            businessLogicException.errorCode,
            if (properties.printStacktrace) businessLogicException.cause?.let { stackTraceToString(it) } ?: "" else "",
            businessLogicException.execution,
            if (businessLogicException.showOnApi) businessLogicException.publicMessage else "",
            request.servletPath,
            if (businessLogicException.showOnApi) businessLogicException.data else null,
            businessLogicException.showOnApi,
            businessLogicException.showOnFrontend,
            businessLogicException.isRepeatable)

        response.status = businessLogicException.statusCode
        response.contentType = "application/json"
        response.writer.write(mapper.writeValueAsString(errorResponse))
    }

    private fun stackTraceToString(ex: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        ex.printStackTrace(pw)
        return sw.toString()
    }

}
