package ch.umb.curo.starter.exception

import ch.umb.curo.starter.exception.details.BadRequestDetail
import ch.umb.curo.starter.exception.details.ErrorDetail

/**
 * Provides constants and helper methods for common errors in our REST-API.
 * Typically thrown in a controller and forwarded to the client.
 *
 *
 *  Most of the time you can use a constant
 * <pre>
 * throw CuroApiException.INVALID_ARGUMENT_400
</pre> *
 * If you want to add more details use one of the helper methods. These error details must not contain any sensitive information!
 * <pre>
 * throw CuroApiException.invalidArgument400(new FieldViolation("filterquery", query, "expected nonempty query"));
</pre> *
 *
 * Thread-safe
 */
class ApiException private constructor(message: String, cause: Throwable?, val errorCode: ErrorCode, details: ErrorDetail?, val curoErrorCode: CuroErrorCode?) : Exception(message, cause, false, false) {
    private val details: ErrorDetail? = details

    private constructor(errorCode: ErrorCode) : this(errorCode.defaultMessage, null, errorCode, null, null) {}
    private constructor(errorCode: ErrorCode, details: ErrorDetail? = null) : this(details?.toMessage() ?: errorCode.defaultMessage, null, errorCode, details, null) {}
    private constructor(curoErrorCode: CuroErrorCode, details: ErrorDetail? = null) : this(curoErrorCode.defaultMessage, null, curoErrorCode.errorCode, null, curoErrorCode) {}

    /**
     * Error codes inspired by google's gRPC error model
     */
    enum class ErrorCode constructor(val httpMapping: Int, val defaultMessage: String = "") {
        INVALID_ARGUMENT(400, "Client specified an invalid argument"),
        FAILED_PRECONDITION(400, "Request can not be executed in the current system state"),
        OUT_OF_RANGE(400, "Client specified an invalid range"),
        UNAUTHENTICATED(401, "Request not authenticated due to missing, invalid, or expired OAuth token"),
        PERMISSION_DENIED(403, "Client does not have sufficient permission. This can happen because the OAuth token does not have the right scopes, the client doesn't have permission, or the API has not been enabled for the client project."),
        NOT_FOUND(404, "A specified resource is not found, or the request is rejected by undisclosed reasons, such as whitelisting."),
        METHOD_NOT_ALLOWED(405, "A request method is not supported for the requested resource, e.g. resource is readonly"),
        ABORTED(409, "Concurrency conflict, such as read-modify-write conflict"),
        ALREADY_EXISTS(409, "The resource that a client tried to create already exists"),
        RESOURCE_EXHAUSTED(429, "Either out of resource quota or reaching rate limiting"),
        CANCELLED(499, "Request cancelled by the client"),
        DATA_LOSS(500, "Unrecoverable data loss or data corruption. The client should report the error to the user"),
        UNKNOWN(500, "Unknown server error. Typically a server bug"),
        INTERNAL(500, "Internal server error. Typically a server bug"),
        NOT_IMPLEMENTED(501, "API method not implemented by the server"),
        UNAVAILABLE(503, "Service unavailable. Typically the server is down"),
        DEADLINE_EXCEEDED(504, "Request deadline exceeded. This will happen only if the caller sets a deadline that is shorter than the method's default deadline (i.e. requested deadline is not enough for the server to process the request) and the request did not finish within the deadline");
    }

    /**
     * Curo Error codes
     */
    enum class CuroErrorCode constructor(val errorCode: ErrorCode, val defaultMessage: String = "") {
        TASK_NOT_FOUND(ErrorCode.NOT_FOUND, "Task not found"),
        COMPLETE_NEEDS_SAME_ASSIGNEE(ErrorCode.PERMISSION_DENIED, "Task does not belong to the logged in user"),
        CANT_SAVE_IN_EXISTING_OBJECT(ErrorCode.INVALID_ARGUMENT, "Can't save variable because it is not castable to already exsiting variable type")
    }

    companion object {
        val INVALID_ARGUMENT_400 = ApiException(ErrorCode.INVALID_ARGUMENT)
        val OUT_OF_RANGE_400 = ApiException(ErrorCode.OUT_OF_RANGE)
        val FAILED_PRECONDITION_400 = ApiException(ErrorCode.FAILED_PRECONDITION)
        val UNAUTHENTICATED_401 = ApiException(ErrorCode.UNAUTHENTICATED)
        val PERMISSION_DENIED_403 = ApiException(ErrorCode.PERMISSION_DENIED)
        val NOT_FOUND_404 = ApiException(ErrorCode.NOT_FOUND)
        val METHOD_NOT_ALLOWED_405 = ApiException(ErrorCode.METHOD_NOT_ALLOWED)
        val ABORTED_409 = ApiException(ErrorCode.ABORTED)
        val ALREADY_EXISTS_409 = ApiException(ErrorCode.ALREADY_EXISTS)
        val RESOURCE_EXHAUSTED_429 = ApiException(ErrorCode.RESOURCE_EXHAUSTED)
        val CANCELLED_499 = ApiException(ErrorCode.CANCELLED)
        val DATA_LOSS_500 = ApiException(ErrorCode.DATA_LOSS)
        val UNKNOWN_500 = ApiException(ErrorCode.UNKNOWN)
        val INTERNAL_500 = ApiException(ErrorCode.INTERNAL)
        val NOT_IMPLEMENTED_501 = ApiException(ErrorCode.NOT_IMPLEMENTED)
        val UNAVAILABLE_503 = ApiException(ErrorCode.UNAVAILABLE)
        val DEADLINE_EXCEEDED_504 = ApiException(ErrorCode.DEADLINE_EXCEEDED)

        fun unauthenticated401(description: String): ApiException {
            return ApiException(ErrorCode.UNAUTHENTICATED, object : ErrorDetail {
                override fun toMessage(): String {
                    return description
                }
            })
        }

        fun unauthorized403(description: String): ApiException {
            return ApiException(ErrorCode.PERMISSION_DENIED, object : ErrorDetail {
                override fun toMessage(): String {
                    return description
                }
            })
        }

        fun invalidArgument400(invalidArgumentDetails: List<String>): ApiException {
            return ApiException(ErrorCode.INVALID_ARGUMENT, object : ErrorDetail {
                override fun toMessage(): String {
                    return invalidArgumentDetails.joinToString("\n") { it }
                }
            })
        }

        fun invalidArgument400(vararg invalidArgumentDetails: BadRequestDetail.FieldViolation): ApiException {
            return ApiException(ErrorCode.INVALID_ARGUMENT, BadRequestDetail(*invalidArgumentDetails))
        }

        fun outOfRange400(vararg outOfRangeDetails: BadRequestDetail.FieldViolation): ApiException {
            return ApiException(ErrorCode.OUT_OF_RANGE, BadRequestDetail(*outOfRangeDetails))
        }

        fun notFound404(description: String): ApiException {
            return ApiException(ErrorCode.NOT_FOUND, object : ErrorDetail {
                override fun toMessage(): String {
                    return description
                }
            })
        }

        fun curoErrorCode(code: CuroErrorCode): ApiException {
            return ApiException(code, object : ErrorDetail {
                override fun toMessage(): String {
                    return code.defaultMessage
                }
            })
        }
    }

}
