package ch.umb.curo.starter.helper.camunda

interface CamundaVariableDefinitionConvention<T> {

    val value: String
    val type: Class<T>

}

interface CamundaVariableListDefinition<T> : CamundaVariableDefinitionConvention<T>
interface CamundaVariableDefinition<T> : CamundaVariableDefinitionConvention<T>
