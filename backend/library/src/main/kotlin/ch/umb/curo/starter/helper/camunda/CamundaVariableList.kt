package ch.umb.curo.starter.helper.camunda

import kotlin.reflect.KClass

class CamundaVariableList<T : Any> constructor(override val value: String, private val internalType: KClass<T>) : CamundaVariableListDefinition<T> {
    override val type: Class<T>
        get() = internalType.javaObjectType

    // internal makes sure one doesn't use the wrong constructor in Kotlin when using this lib
    internal constructor(value: String, type: Class<T>) : this(value, type.kotlin)
}
