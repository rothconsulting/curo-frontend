package ch.umb.curo.starter.helper.camunda.mock

import ch.umb.curo.starter.helper.camunda.CamundaVariableDefinition
import ch.umb.curo.starter.helper.camunda.CamundaVariableListDefinition
import org.camunda.bpm.engine.delegate.VariableScope
import org.slf4j.Logger

interface CamundaVariableHelperMockInterface {

    val logger: Logger
    val variableScope: VariableScope
    val logging: Boolean

    /**
     * Mock a variables which is access via the CamundaVariableHelper
     */
    fun mock(variable: CamundaVariableDefinition<*>, value: Any?)

    /**
     * Mock a variables with JSON value which is access via the CamundaVariableHelper
     */
    fun mockWithJson(variable: CamundaVariableDefinition<*>, value: String)

    /**
     * Mock a variables which is access via the CamundaVariableHelper
     */
    fun mock(variable: CamundaVariableListDefinition<*>, value: List<*>?)

    /**
     * Mock a variables with JSON value which is access via the CamundaVariableHelper
     */
    fun mockWithJson(variable: CamundaVariableListDefinition<*>, value: String)

}
