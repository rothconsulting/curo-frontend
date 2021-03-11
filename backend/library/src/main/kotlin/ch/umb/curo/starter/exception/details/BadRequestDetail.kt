package ch.umb.curo.starter.exception.details

/**
 * Provides error details for client errors such as INVALID_ARGUMENT or OUT_OF_RANGE
 */
class BadRequestDetail(vararg val fieldViolations: FieldViolation) : ErrorDetail {

    override fun toMessage(): String {
        val sb = StringBuilder()
        for (fieldViolation in fieldViolations) {
            sb.append("Request field [").append(fieldViolation.fieldName).append("] had invalid value [")
                .append(fieldViolation.value).append("], expected ").append(fieldViolation.expected)
        }
        return sb.toString()
    }

    override fun toString(): String {
        return fieldViolations.contentToString()
    }

    class FieldViolation(
        var fieldName: String? = null,
        var value: String? = null,
        var expected: String? = null
    )

}
