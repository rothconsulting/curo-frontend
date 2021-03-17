package ch.umb.curo.starter.exception.details

import com.fasterxml.jackson.annotation.JsonInclude
import org.joda.time.DateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
class BusinessErrorModel(
    timestamp: DateTime,
    status: Int,
    error: String,
    errorCode: String?,
    exception: String,
    val execution: String,
    message: String,
    path: String,
    val data: Map<String, Any>?,
    val showOnApi: Boolean = true,
    val showOnFrontend: Boolean = true,
    val isRepeatable: Boolean = true
) : DefaultErrorModel(timestamp, status, error, errorCode, exception, message, path)
