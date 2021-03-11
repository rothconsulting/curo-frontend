package ch.umb.curo.starter.property

import org.camunda.bpm.engine.authorization.Permissions
import org.camunda.bpm.engine.authorization.Resources
import java.util.*

class CuroInitialGroupProperty {

    /**
     * Id of the group
     */
    var id: String = ""

    /**
     * Name of the group (if empty, Curo will use the id as name)
     */
    var name: String = ""

    /**
     * Type of the group (this property is handled by Camunda just as information)
     */
    var type: String = ""

    /**
     * List of permissions
     */
    var permissions: EnumMap<Resources, ArrayList<Permissions>> = EnumMap(Resources::class.java)

}
