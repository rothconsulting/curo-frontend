package ch.umb.curo.starter.exception

import ch.umb.curo.starter.property.CuroProperties
import org.joda.time.DateTime
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.io.PrintWriter
import java.io.StringWriter
import javax.servlet.http.HttpServletRequest

@ControllerAdvice
class GlobalExceptionHandler {

    @Autowired
    lateinit var properties: CuroProperties

    @ExceptionHandler(ApiException::class)
    fun handleApiException(apiException: ApiException, request: HttpServletRequest): ResponseEntity<DefaultErrorModel?> {
        return ResponseEntity(DefaultErrorModel(
                DateTime.now(),
                apiException.errorCode.httpMapping,
                apiException.errorCode.defaultMessage,
                if (properties.printStacktrace) apiException.cause?.let { stackTraceToString(it) } ?: "" else "",
                apiException.message ?: "",
                request.servletPath
        ), HttpStatus.valueOf(apiException.errorCode.httpMapping))
    }

    private fun stackTraceToString(ex: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        ex.printStackTrace(pw)
        return sw.toString()
    }

    class DefaultErrorModel(val timestamp: DateTime,
                            val status: Int,
                            val error: String,
                            val exception: String,
                            val message: String,
                            val path: String)
}
