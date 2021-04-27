package ch.umb.curo.starter.models.response

import com.fasterxml.jackson.annotation.JsonInclude
import org.camunda.bpm.engine.authorization.Permissions
import org.camunda.bpm.engine.authorization.Resources

@JsonInclude(JsonInclude.Include.NON_NULL)
class CuroPermissionsResponse {

    var userId: String = ""
    var groups: ArrayList<String> = arrayListOf()
    var permissions: HashMap<String, HashMap<Resources, List<Permissions>>>? = null
    var curoPermissions: Map<String, Map<Resources, List<Permissions>>>? = null
}
