package ch.umb.curo.starter.helper.camunda.mock

import ch.umb.curo.starter.helper.camunda.CamundaVariableDefinition
import ch.umb.curo.starter.helper.camunda.CamundaVariableListDefinition
import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.delegate.VariableScope
import org.camunda.bpm.engine.variable.impl.value.AbstractTypedValue
import org.camunda.bpm.engine.variable.impl.value.ObjectValueImpl
import org.camunda.bpm.engine.variable.value.TypedValue
import org.camunda.spin.plugin.variable.value.impl.JsonValueImpl
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.doReturn
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * CamundaVariableHelper mock implementation for Mockito
 *
 * @see ch.umb.curo.starter.helper.camunda.mock.CamundaVariableHelperMock
 */
class CamundaVariableHelperMockito(override val variableScope: VariableScope, override val logging: Boolean = false) :
    CamundaVariableHelperMockInterface {

    override val logger: Logger = LoggerFactory.getLogger(CamundaVariableHelperMock::class.java)

    override fun mock(variable: CamundaVariableDefinition<*>, value: Any?) {
        if (logging) logger.info("add mock for variable '${variable.value}' -> $value")
        doReturn(AbstractTypedValue(value, null)).`when`(variableScope)
            .getVariableTyped<TypedValue>(eq(variable.value), eq(true))
    }

    override fun mockWithJson(variable: CamundaVariableDefinition<*>, value: String) {
        if (logging) logger.info("add mock for variable '${variable.value}' (as json) -> $value")
        doReturn(JsonValueImpl(value, "application/json")).`when`(variableScope)
            .getVariableTyped<TypedValue>(eq(variable.value), eq(true))
    }

    override fun mock(variable: CamundaVariableListDefinition<*>, value: List<*>?) {
        if (logging) logger.info("add mock for list variable '${variable.value}' -> $value")
        doReturn(
            ObjectValueImpl(
                value,
                ObjectMapper().writeValueAsString(value),
                "application/json",
                value?.let { value::class.java.canonicalName } ?: "",
                true)).`when`(
            variableScope
        ).getVariableTyped<TypedValue>(eq(variable.value), eq(true))
    }

    override fun mockWithJson(variable: CamundaVariableListDefinition<*>, value: String) {
        if (logging) logger.info("add mock for list variable '${variable.value}' (as json) -> $value")
        doReturn(JsonValueImpl(value, "application/json")).`when`(variableScope)
            .getVariableTyped<TypedValue>(eq(variable.value), eq(true))
    }
}
