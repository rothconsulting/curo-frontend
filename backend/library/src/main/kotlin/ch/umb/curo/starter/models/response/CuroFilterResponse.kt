package ch.umb.curo.starter.models.response

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.camunda.bpm.engine.history.HistoricTaskInstance
import org.camunda.bpm.engine.rest.dto.task.TaskDto
import org.camunda.bpm.engine.task.DelegationState
import org.camunda.bpm.engine.task.Task
import org.joda.time.DateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
class CuroFilterResponse(var name: String? = null,
                         var description: String? = null,
                         var refresh: Boolean? = null,
                         var properties: Map<String, Any?>? = null,
                         var total: Long,
                         var items: List<Any>)
