package ch.umb.curo.starter.exception

class CamundaVariableException : RuntimeException {

    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)
}
