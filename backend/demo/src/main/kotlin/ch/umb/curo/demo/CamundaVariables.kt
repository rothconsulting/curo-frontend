package ch.umb.curo.demo

import ch.umb.curo.starter.helper.camunda.CamundaVariable
import ch.umb.curo.starter.helper.camunda.CamundaVariableDefinition

class CamundaVariables {
    companion object {
        val title: CamundaVariableDefinition<String> = CamundaVariable("title", String::class.java)
        val category: CamundaVariableDefinition<String> = CamundaVariable("category", String::class.java)
        val description: CamundaVariableDefinition<String> = CamundaVariable("description", String::class.java)
        val url: CamundaVariableDefinition<String> = CamundaVariable("url", String::class.java)
        val comments: CamundaVariableDefinition<String> = CamundaVariable("comments", String::class.java)
        val suggestionAccept: CamundaVariableDefinition<Boolean> = CamundaVariable("suggestionAccept", Boolean::class.java)
    }
}
