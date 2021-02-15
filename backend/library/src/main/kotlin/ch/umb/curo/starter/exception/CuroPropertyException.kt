package ch.umb.curo.starter.exception

class CuroPropertyException(val description: String, val property: String, val value: String, val reason: String) : RuntimeException()
