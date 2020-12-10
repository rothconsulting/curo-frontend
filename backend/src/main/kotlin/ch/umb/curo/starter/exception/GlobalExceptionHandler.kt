package ch.umb.curo.starter.exception

import com.google.gson.Gson
import com.google.gson.JsonElement
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import java.io.IOException
import java.io.PrintWriter
import java.io.StringWriter
import java.util.stream.Collectors
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

//@ControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ApiException::class)
    fun handleApiException(apiException: ApiException, request: HttpServletRequest, response: HttpServletResponse) {

    }

    private fun stackTraceToString(ex: Throwable): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)
        ex.printStackTrace(pw)
        return sw.toString()
    }
}
