package ch.umb.curo.starter.models.response

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
class CuroFilterResponse(
    var name: String? = null,
    var description: String? = null,
    var refresh: Boolean? = null,
    var properties: Map<String, Any?>? = null,
    var total: Long,
    var items: List<Any>
)
