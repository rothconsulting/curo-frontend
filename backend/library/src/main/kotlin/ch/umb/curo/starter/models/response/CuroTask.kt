package ch.umb.curo.starter.models.response

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.v3.oas.annotations.media.Schema
import org.camunda.bpm.engine.history.HistoricTaskInstance
import org.camunda.bpm.engine.rest.dto.task.TaskDto
import org.camunda.bpm.engine.task.DelegationState
import org.camunda.bpm.engine.task.Task
import org.joda.time.DateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
class CuroTask {

    /**
     * DB id of the task
     **/
    @Schema(description = "DB id of the task")
    var id: String? = null

    /**
     * Reference to the path of execution or null if it is not related to a process instance
     **/
    @Schema(description = "Reference to the path of execution or null if it is not related to a process instance")
    var executionId: String? = null
        private set //Not allowed to set id

    /**
     * Reference to the process instance or null if it is not related to a process instance
     **/
    @Schema(description = "Reference to the process instance or null if it is not related to a process instance")
    var processInstanceId: String? = null

    /**
     * Reference to the process definition or null if it is not related to a process
     **/
    @Schema(description = "Reference to the process definition or null if it is not related to a process")
    var processDefinitionId: String? = null

    /**
     * The id of the activity in the process defining this task or null if this is not related to a process
     **/
    @Schema(description = "The id of the activity in the process defining this task or null if this is not related to a process")
    var taskDefinitionKey: String? = null

    /**
     * Name or title of the task
     **/
    @Schema(description = "Name or title of the task")
    var name: String? = null

    /**
     * Free text description of the task
     **/
    @Schema(description = "Free text description of the task")
    var description: String? = null

    /**
     * Indication of how important/urgent this task is with a number between
     * 0 and 100 where higher values mean a higher priority and lower values mean
     * lower priority: [0..19] lowest, [20..39] low, [40..59] normal, [60..79] high,
     * [80..100] highest
     **/
    @Schema(description = "Indication of how important/urgent this task is with a number between 0 and 100 where higher values mean a higher priority and lower values mean owner priority: [0..19] lowest, [20..39] low, [40..59] normal, [60..79] high, [80..100] highest")
    var priority: Double? = null

    /**
     * Status of the task
     **/
    @Schema(description = "Status of the task")
    var status: String? = null

    /**
     * The userId of the person to which this task is assigned or delegated
     **/
    @Schema(description = "The userId of the person to which this task is assigned or delegated")
    var assignee: String? = null

    /**
     * The userId of the person that is responsible for this task. This is used when a task is delegated
     **/
    @Schema(description = "The userId of the person that is responsible for this task. This is used when a task is delegated")
    var owner: String? = null

    /**
     * The date/time when this task was created | Time when the task started
     **/
    @Schema(description = "The date/time when this task was created | Time when the task started")
    var created: DateTime? = null

    /**
     * Time when the task was deleted or completed
     **/
    @Schema(description = "Time when the task was deleted or completed")
    var endTime: DateTime? = null

    /**
     * Difference between endTime and startTime in milliseconds
     **/
    @Schema(description = "Difference between endTime and startTime in milliseconds")
    var durationInMillis: Long? = null

    /**
     * Due date of the task
     **/
    @Schema(description = "Due date of the task")
    var due: DateTime? = null

    /**
     * Follow-up date of the task
     **/
    @Schema(description = "Follow-up date of the task")
    var followUp: DateTime? = null

    /**
     * The current delegation state for this task
     **/
    @Schema(description = "The current delegation state for this task")
    var delegationState: DelegationState? = null

    /**
     * The parent task for which this task is a subtask
     **/
    @Schema(description = "The parent task for which this task is a subtask")
    var parentTaskId: String? = null

    /**
     * Indicated whether this task is suspended or not
     **/
    @Schema(description = "Indicated whether this task is suspended or not")
    var suspended: Boolean? = null

    /**
     * Provides the form key for the task
     **/
    @Schema(description = "Provides the form key for the task")
    var formKey: String? = null

    /**
     * Variables related to this task
     **/
    @Schema(description = "Variables related to this task")
    var variables: HashMap<String, Any?>? = null


    companion object {
        fun fromCamundaTask(task: Task): CuroTask {
            val curoTask = CuroTask()
            curoTask.id = task.id
            curoTask.executionId = task.executionId
            curoTask.processInstanceId = task.processInstanceId
            curoTask.processDefinitionId = task.processDefinitionId
            curoTask.taskDefinitionKey = task.taskDefinitionKey
            curoTask.name = task.name
            curoTask.priority = task.priority.toDouble()
            curoTask.status = "open"
            curoTask.assignee = task.assignee
            curoTask.owner = task.owner
            curoTask.created = DateTime(task.createTime)
            curoTask.due = DateTime(task.dueDate)
            curoTask.followUp = DateTime(task.followUpDate)
            curoTask.delegationState = task.delegationState
            curoTask.parentTaskId = task.parentTaskId
            curoTask.suspended = task.isSuspended
            curoTask.formKey = task.formKey

            return curoTask
        }

        fun fromCamundaTaskDto(task: TaskDto): CuroTask {
            val curoTask = CuroTask()
            curoTask.id = task.id
            curoTask.executionId = task.executionId
            curoTask.processInstanceId = task.processInstanceId
            curoTask.processDefinitionId = task.processDefinitionId
            curoTask.taskDefinitionKey = task.taskDefinitionKey
            curoTask.name = task.name
            curoTask.priority = task.priority.toDouble()
            curoTask.status = "open"
            curoTask.assignee = task.assignee
            curoTask.owner = task.owner
            curoTask.created = DateTime(task.created)
            curoTask.due = DateTime(task.due)
            curoTask.followUp = DateTime(task.followUp)
            curoTask.delegationState =
                if (task.delegationState != null) DelegationState.valueOf(task.delegationState) else null
            curoTask.parentTaskId = task.parentTaskId
            curoTask.suspended = task.isSuspended
            curoTask.formKey = task.formKey

            return curoTask
        }

        fun fromCamundaHistoricTask(task: HistoricTaskInstance): CuroTask {
            val curoTask = CuroTask()
            curoTask.id = task.id
            curoTask.executionId = task.executionId
            curoTask.processInstanceId = task.processInstanceId
            curoTask.processDefinitionId = task.processDefinitionId
            curoTask.taskDefinitionKey = task.taskDefinitionKey
            curoTask.name = task.name
            curoTask.priority = task.priority.toDouble()
            curoTask.status = task.deleteReason
            curoTask.assignee = task.assignee
            curoTask.owner = task.owner
            curoTask.created = DateTime(task.startTime)
            curoTask.endTime = DateTime(task.endTime)
            curoTask.durationInMillis = task.durationInMillis
            curoTask.due = DateTime(task.dueDate)
            curoTask.followUp = DateTime(task.followUpDate)
            curoTask.delegationState = null
            curoTask.parentTaskId = task.parentTaskId
            curoTask.suspended = null
            curoTask.formKey = null

            if (curoTask.status == null || curoTask.status!!.isEmpty()) {
                curoTask.status = "open"
            }

            return curoTask
        }
    }


}
