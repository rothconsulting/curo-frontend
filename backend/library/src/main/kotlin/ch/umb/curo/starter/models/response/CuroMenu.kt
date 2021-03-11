package ch.umb.curo.starter.models.response

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import org.camunda.bpm.engine.filter.Filter

@JsonInclude(JsonInclude.Include.NON_NULL)
@ArraySchema(schema = Schema(implementation = CuroMenu.MenuElement::class))
class CuroMenu : ArrayList<CuroMenu.MenuElement>() {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    class MenuElement(
        /**
         * Name of the menu element
         **/
        @Schema(description = "Name of the menu element")
        val name: String,

        /**
         * Icon of the menu element
         **/
        @Schema(description = "Icon of the menu element")
        val icon: String? = null,

        /**
         * Type of the menu element
         **/
        @Schema(description = "Type of the menu element")
        val type: MenuElementType = MenuElementType.TASK_FILTER,

        /**
         * Order of the menu element
         **/
        @Schema(description = "Order of the menu element")
        val order: Int,

        /**
         * Color of the menu element
         **/
        @Schema(description = "Color of the menu element")
        val color: String,

        /**
         * Link of the menu element if type is LINK
         **/
        @Schema(description = "Link of the menu element if type is LINK")
        val link: String? = null,

        /**
         * Filter id of the menu element if type is TASK_FILTER
         **/
        @Schema(description = "Filter id of the menu element if type is TASK_FILTER")
        val filterId: String? = null,

        /**
         * Sub-Elements of the menu element if the type is FOLDER
         **/
        @Schema(description = "Sub-Elements of the menu element if the type is FOLDER")
        val subElements: ArrayList<MenuElement>? = null
    )

    enum class MenuElementType {
        TASK_FILTER, LINK, FOLDER
    }

    companion object {
        fun fromFilter(filter: Filter): MenuElement {
            val properties = filter.properties
            val color = properties.getOrDefault("color", "#000000") as String
            val order = properties.getOrDefault("priority", 0) as Int
            val icon = properties.getOrDefault("icon", null) as String?

            return MenuElement(
                name = filter.name,
                icon = icon,
                type = MenuElementType.TASK_FILTER,
                order = order,
                color = color,
                filterId = filter.id
            )
        }
    }
}
