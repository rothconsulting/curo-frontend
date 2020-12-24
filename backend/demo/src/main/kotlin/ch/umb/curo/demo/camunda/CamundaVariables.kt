package ch.umb.curo.demo.camunda

import ch.umb.solutions.curo.libraries.sharedprocess.camunda.CamundaVariable
import ch.umb.solutions.curo.libraries.sharedprocess.camunda.CamundaVariableDefinition

class CamundaVariables {
    companion object {
        var TITLE: CamundaVariableDefinition<String> = CamundaVariable("title", String::class)
        var CATEGORY: CamundaVariableDefinition<String> = CamundaVariable("category", String::class)
        var DESCRIPTION: CamundaVariableDefinition<String> = CamundaVariable("description", String::class)
        var URL: CamundaVariableDefinition<String> = CamundaVariable("url", String::class)
        var SUGGESTION_ACCEPT: CamundaVariableDefinition<Boolean> = CamundaVariable("suggestionAccept", Boolean::class)
    }
}
