package ch.umb.curo.starter.exception

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

    private fun stackTraceToString(ex: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        ex.printStackTrace(pw)
        return sw.toString()
    }

    class DefaultErrorModel(
        val timestamp: DateTime,
        val status: Int,
        val error: String,
        val errorCode: String?,
        val exception: String,
        val message: String,
        val path: String
    )
}
