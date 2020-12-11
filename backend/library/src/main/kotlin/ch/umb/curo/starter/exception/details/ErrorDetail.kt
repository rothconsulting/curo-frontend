package ch.umb.curo.starter.exception.details

/**
 * Detailed error message directed at a developer
 *
 * Must never contain sensitive information
 */
interface ErrorDetail {
    /**
     * @return a human readable error message
     */
    fun toMessage(): String
}
