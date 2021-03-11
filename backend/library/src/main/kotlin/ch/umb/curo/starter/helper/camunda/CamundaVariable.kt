package ch.umb.curo.starter.helper.camunda

import kotlin.reflect.KClass

class CamundaVariable<T : Any> constructor(override val value: String, private val internalType: KClass<T>) :
    CamundaVariableDefinition<T> {
    override val type: Class<T>
        get() = internalType.javaObjectType

    // internal makes sure one doesn't use the wrong constructor in Kotlin when using this lib
    constructor(value: String, type: Class<T>) : this(value, type.kotlin)
}
