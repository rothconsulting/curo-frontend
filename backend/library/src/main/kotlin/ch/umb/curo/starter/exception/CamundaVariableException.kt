package ch.umb.solutions.curo.libraries.sharedprocess.exceptions

class CamundaVariableException : RuntimeException {

    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)
}
