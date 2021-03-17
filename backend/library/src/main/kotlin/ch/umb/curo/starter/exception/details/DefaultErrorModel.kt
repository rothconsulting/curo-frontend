package ch.umb.curo.starter.exception.details

import org.joda.time.DateTime

open class DefaultErrorModel(
    val timestamp: DateTime,
    val status: Int,
    val error: String,
    val errorCode: String?,
    val exception: String,
    val message: String,
    val path: String
)
