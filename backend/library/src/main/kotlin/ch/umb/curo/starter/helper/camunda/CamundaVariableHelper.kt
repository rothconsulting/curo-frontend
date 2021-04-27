package ch.umb.curo.starter.helper.camunda

import ch.umb.curo.starter.exception.CamundaVariableException
import ch.umb.curo.starter.helper.camunda.annotation.InitWithEmpty
import ch.umb.curo.starter.helper.camunda.annotation.InitWithNull
import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.delegate.VariableScope
import org.camunda.bpm.engine.variable.impl.value.ObjectValueImpl
import org.camunda.bpm.engine.variable.type.ValueType
import org.camunda.bpm.engine.variable.value.ObjectValue
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.spin.impl.json.jackson.JacksonJsonNode
import org.camunda.spin.plugin.variable.type.JsonValueType
import org.camunda.spin.plugin.variable.value.JsonValue
import org.camunda.spin.plugin.variable.value.impl.JsonValueImpl
import org.jboss.logging.Logger
import org.springframework.beans.BeanUtils
import java.lang.reflect.Modifier
import java.lang.reflect.ParameterizedType
import java.util.*
import java.util.function.Supplier

open class CamundaVariableHelper(private val variableScope: VariableScope) {

    private val logger = Logger.getLogger(this::class.java)

    /**
     * @param variableDefinition CamundaVariableDefinition<T> of variable to read
     * @param defaultValue provide value here if variable is null / not defined
     * @return result of Camunda variable matching the given definition (name/type)
     * @throws CamundaVariableException if variable not found and defaultValue is null / not provided
     * @throws CamundaVariableException if variable can not be casted in defined type
     */
    @JvmOverloads
    @Throws(CamundaVariableException::class)
    operator fun <T : Any> get(variableDefinition: CamundaVariableDefinition<T>, defaultValue: T? = null): T {
        return getOrNull(variableDefinition) ?: defaultValue
        ?: throw CamundaVariableException("Camunda variable '" + variableDefinition.value + "' is not defined")
    }

    /**
     * @param variableDefinition CamundaVariableDefinition<T> of variable to read
     * @param defaultValue provide value here if variable is null / not defined
     * @return result of Camunda variable matching the given definition (name/type)
     * @throws CamundaVariableException if variable not found and defaultValue is null / not provided
     * @throws CamundaVariableException if variable can not be casted in defined type
     */
    @Throws(CamundaVariableException::class)
    operator fun <T : Any> get(variableDefinition: CamundaVariableDefinition<T>, defaultValue: Supplier<T?>): T {
        return getOrNull(variableDefinition) ?: defaultValue.get()
        ?: throw CamundaVariableException("Camunda variable '" + variableDefinition.value + "' is not defined")
    }


    /**
     * @param variableListDefinition CamundaVariableListDefinition<T> of variable list to be read
     * @param defaultValue provide value here if variable is null / not defined
     * @return result of Camunda variable matching the given definition (name/type)
     * @throws CamundaVariableException if variable not found and defaultValue is null / not provided
     * @throws CamundaVariableException if variable can not be casted in defined type
     */
    @JvmOverloads
    @Throws(CamundaVariableException::class)
    operator fun <T : Any> get(
        variableListDefinition: CamundaVariableListDefinition<T>,
        defaultValue: List<T?>? = null
    ): List<T?> {
        return getOrNull(variableListDefinition) ?: defaultValue
        ?: throw CamundaVariableException("Camunda variable '" + variableListDefinition.value + "' is not defined")
    }

    /**
     * @param variableListDefinition CamundaVariableListDefinition<T> of variable list to be read
     * @param defaultValue provide value here if variable is null / not defined
     * @return result of Camunda variable matching the given definition (name/type)
     * @throws CamundaVariableException if variable not found and defaultValue is null / not provided
     * @throws CamundaVariableException if variable can not be casted in defined type
     */
    @Throws(CamundaVariableException::class)
    operator fun <T : Any> get(
        variableListDefinition: CamundaVariableListDefinition<T>,
        defaultValue: Supplier<List<T>?>
    ): List<T?> {
        return getOrNull(variableListDefinition) ?: defaultValue.get()
        ?: throw CamundaVariableException("Camunda variable '" + variableListDefinition.value + "' is not defined")
    }

    /**
     * @param variableDefinition CamundaVariableDefinition<T> of variable to read
     * @param exception exception thrown if variable is not present or null
     * @return result of Camunda variable matching the given definition (name/type) or null if variable is null / not defined
     */
    fun <T : Any> getOrThrow(variableDefinition: CamundaVariableDefinition<T>, exception: Throwable): T {
        return getOrNull(variableDefinition) ?: throw exception
    }

    /**
     * @param variableDefinition CamundaVariableDefinition<T> of variable to read
     * @return result of Camunda variable matching the given definition (name/type) or null if variable is null / not defined
     * @throws CamundaVariableException if variable can not be casted in defined type
     */
    @Throws(CamundaVariableException::class)
    fun <T : Any> getOrNull(variableDefinition: CamundaVariableDefinition<T>): T? {
        return try {
            val raw = variableScope.getVariableTyped<TypedValue>(variableDefinition.value, true)
            if (raw != null) {
                when (raw.type) {
                    JsonValueType.JSON -> ObjectMapper().readValue(
                        (raw as JsonValue).valueSerialized,
                        variableDefinition.type
                    )
                    ValueType.OBJECT -> ObjectMapper().readValue(
                        (raw as ObjectValue).valueSerialized,
                        variableDefinition.type
                    )
                    else -> variableDefinition.type.cast(raw.value)
                }
            } else {
                null
            }
        } catch (e: Exception) {
            throw CamundaVariableException(
                "Could not cast variable '" + variableDefinition.value + "' to type " + variableDefinition.type.toString(),
                e
            )
        }
    }

    /**
     * @param variableListDefinition name of variable to read
     * @return result of Camunda variable matching the given definition (name/type) or null if variable is null / not defined
     * @throws CamundaVariableException if variable can not be casted in defined type
     */
    @Throws(CamundaVariableException::class)
    fun <T : Any> getOrNull(variableListDefinition: CamundaVariableListDefinition<T>): List<T?>? {
        return try {
            val raw = variableScope.getVariableTyped<TypedValue>(variableListDefinition.value, true)
            if (raw != null) {
                val mapper = ObjectMapper()
                when (raw.type) {
                    JsonValueType.JSON -> mapper.readValue(
                        (raw as JsonValue).valueSerialized,
                        mapper.typeFactory.constructCollectionType(List::class.java, variableListDefinition.type)
                    ) as List<T?>?
                    ValueType.OBJECT -> ObjectMapper().readValue(
                        (raw as ObjectValue).valueSerialized,
                        mapper.typeFactory.constructCollectionType(List::class.java, variableListDefinition.type)
                    ) as List<T?>?
                    else -> variableListDefinition.type.cast(raw.value) as List<T?>?
                }
            } else {
                null
            }
        } catch (e: Exception) {
            throw CamundaVariableException(
                "Could not cast variable '" + variableListDefinition.value + "' to type " + variableListDefinition.type.toString(),
                e
            )
        }
    }


    /**
     * @param variableListDefinition name of variable to read
     * @return result of Camunda variable matching the given definition (name/type) or null if variable is null / not defined
     */
    fun <T : Any> getOrThrow(variableListDefinition: CamundaVariableListDefinition<T>, exception: Throwable): List<T?> {
        return getOrNull(variableListDefinition) ?: throw exception
    }

    /**
     * @param variableListDefinition name of variable to read
     * @return result of Camunda variable matching the given definition (name/type) or an Empty List<T> if variable is null / not defined
     * @throws CamundaVariableException if variable can not be casted in defined type
     */
    @Throws(CamundaVariableException::class)
    fun <T : Any> getOrEmpty(variableListDefinition: CamundaVariableListDefinition<T>): List<T?> {
        return getOrNull(variableListDefinition) ?: ArrayList<T>()
    }

    fun getAllVariables(camundaVariables: Class<out Any>): List<CamundaVariableDefinitionConvention<out Any>> {
        return camundaVariables.declaredFields
            .filter { Modifier.isStatic(it.modifiers) }
            .map { it.isAccessible = true; it.get(null) }
            .filterIsInstance<CamundaVariableDefinitionConvention<Any>>()
    }

    fun getAllMap(camundaVariables: Class<out Any>): Map<String, Any> {
        return getAllMapWithNullables(camundaVariables).filterValues { it != null }.mapValues { it.value as Any }
    }

    fun getAllMapWithNullables(camundaVariables: Class<out Any>): Map<String, Any?> {
        return getAllVariables(camundaVariables).associate {
            Pair(
                it.value,
                when (it) {
                    is CamundaVariable -> getOrNull(it)
                    is CamundaVariableList -> getOrNull(it)
                    else -> null
                }
            )
        }
    }

    operator fun <T : Any> set(variableName: CamundaVariableDefinition<T>, value: T?) {
        variableScope.setVariable(variableName.value, value)
    }

    operator fun <T : Any> set(variableName: CamundaVariableListDefinition<T>, value: List<T?>) {
        variableScope.setVariable(variableName.value, value)
    }

    /**
     * Initialize annotated variables from the provided class. Already existing complex variables with a matching name are converted to the correct type if needed.
     *
     * @param variableClass Class with static variables definitions.
     * @param convertExistingVariables Should Curo try to convert existing complex variables to their correct type.
     */
    @JvmOverloads
    fun initVariables(variableClass: Any, convertExistingVariables: Boolean = true) {
        logger.debug("CURO: Processing initialization of '${variableClass::class.java.canonicalName}'")
        val doAllEmpty = variableClass.javaClass.isAnnotationPresent(InitWithEmpty::class.java)
        val doAllNull = variableClass.javaClass.isAnnotationPresent(InitWithNull::class.java)
        variableClass.javaClass.declaredFields
            .filter { it.name != "Companion" }
            .filter {
                it.type.isAssignableFrom(CamundaVariableDefinition::class.java) || it.type.isAssignableFrom(
                    CamundaVariableListDefinition::class.java
                )
            }
            .forEach {
                it.isAccessible = true
                val definition = it.get(variableClass) as CamundaVariableDefinitionConvention<*>

                if (convertExistingVariables && variableScope.hasVariable(definition.value)) {
                    val raw = variableScope.getVariableTyped<TypedValue>(definition.value, true)
                    if (raw.type.isPrimitiveValueType) {
                        return@forEach
                    } else {
                        try {
                            val castedVariable = ObjectMapper().readValue(
                                (raw as ObjectValue).valueSerialized,
                                definition.type
                            )
                            variableScope.setVariable(definition.value, castedVariable)
                            logger.debug("CURO: \t-> Changed variable '${definition.value}' to defined type '${definition.type}'")
                        } catch (e: Exception) {
                            logger.debug("CURO: \t-> Changed variable '${definition.value}' to defined type '${definition.type}' failed!! -> Variable got skipped")
                        } finally {
                            return@forEach
                        }
                    }
                }

                if (it.isAnnotationPresent(InitWithNull::class.java) || it.isAnnotationPresent(InitWithEmpty::class.java) || doAllEmpty || doAllNull) {
                    val emptyInit = if (it.isAnnotationPresent(InitWithEmpty::class.java)) {
                        true
                    } else {
                        doAllEmpty && !it.isAnnotationPresent(InitWithNull::class.java)
                    }

                    when {
                        (BeanUtils.isSimpleValueType((it.genericType as ParameterizedType).actualTypeArguments[0] as Class<*>)) && emptyInit -> {
                            val value =
                                if ((it.genericType as ParameterizedType).actualTypeArguments[0] as Class<*> == Boolean::class.javaObjectType) { //Set Boolean to false
                                    false
                                } else {
                                    definition.type.getConstructor().newInstance()
                                }
                            variableScope.setVariable(definition.value, value)
                            logger.debug("CURO: \t-> Initiate variable '${definition.value}' with empty value")
                        }
                        (BeanUtils.isSimpleValueType((it.genericType as ParameterizedType).actualTypeArguments[0] as Class<*>)) && !emptyInit -> {
                            variableScope.setVariable(
                                definition.value,
                                null
                            )
                            logger.debug("CURO: \t-> Initiate variable '${definition.value}' with null value")
                        }
                        (it.type == JacksonJsonNode::class.java) && emptyInit -> {
                            variableScope.setVariable(
                                definition.value,
                                JsonValueImpl(
                                    ObjectMapper().writeValueAsString(
                                        definition.type.getConstructor()
                                            .newInstance()
                                    ),
                                    "application/json"
                                )
                            )
                            logger.debug("CURO: \t-> Initiate variable '${definition.value}' with empty value")
                        }
                        (it.type == JacksonJsonNode::class.java) && !emptyInit -> {
                            variableScope.setVariable(
                                definition.value,
                                JsonValueImpl("", "application/json")
                            )
                            logger.debug("CURO: \t-> Initiate variable '${definition.value}' with null value")
                        }
                        else -> {
                            if (emptyInit) {
                                variableScope.setVariable(
                                    definition.value,
                                    ObjectValueImpl(
                                        definition.type.getConstructor().newInstance(),
                                        ObjectMapper().writeValueAsString(
                                            definition.type.getConstructor().newInstance()
                                        ),
                                        "application/json",
                                        definition.type.canonicalName,
                                        true
                                    )
                                )
                                logger.debug("CURO: \t-> Initiate variable '${definition.value}' with empty value")
                            } else {
                                variableScope.setVariable(
                                    definition.value,
                                    ObjectValueImpl(null, "", "application/json", definition.type.canonicalName, true)
                                )
                                logger.debug("CURO: \t-> Initiate variable '${definition.value}' with null value")
                            }
                        }
                    }
                }
            }
    }

    companion object {
        /**
         * Initialize annotated variables from the provided class. Already existing complex variables with a matching name are converted to the correct type if needed.
         *
         * @param variableClass Class with static variables definitions.
         * @param variableScope Scope which is used to load and save variables.
         * @param convertExistingVariables Should Curo try to convert existing complex variables to their correct type.
         */
        @JvmOverloads
        fun initVariables(variableClass: Any, variableScope: VariableScope, convertExistingVariables: Boolean = true) {
            CamundaVariableHelper(variableScope).initVariables(variableClass, convertExistingVariables)
        }
    }

}

/**
 * Get VariableHelper instance for this VariableScope.
 */
fun VariableScope.variableHelper(): CamundaVariableHelper {
    return CamundaVariableHelper(this)
}
