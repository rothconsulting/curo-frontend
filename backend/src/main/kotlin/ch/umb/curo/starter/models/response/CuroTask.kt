package ch.umb.curo.starter.models.response

import com.fasterxml.jackson.annotation.JsonInclude
import io.swagger.annotations.ApiModelProperty
import org.camunda.bpm.engine.task.DelegationState
import org.camunda.bpm.engine.task.Task
import org.joda.time.DateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
class CuroTask {

    /**
     * DB id of the task
     **/
    @ApiModelProperty("DB id of the task")
    var id: String? = null

    /**
     * Reference to the path of execution or null if it is not related to a process instance
     **/
    @ApiModelProperty("Reference to the path of execution or null if it is not related to a process instance")
    var executionId: String? = null
        private set //Not allowed to set id

    /**
     * Reference to the process instance or null if it is not related to a process instance
     **/
    @ApiModelProperty("Reference to the process instance or null if it is not related to a process instance")
    var processInstanceId: String? = null

    /**
     * Reference to the process definition or null if it is not related to a process
     **/
    @ApiModelProperty("Reference to the process definition or null if it is not related to a process")
    var processDefinitionId: String? = null

    /**
     * The id of the activity in the process defining this task or null if this is not related to a process
     **/
    @ApiModelProperty("The id of the activity in the process defining this task or null if this is not related to a process")
    var taskDefinitionKey: String? = null

    /**
     * Name or title of the task
     **/
    @ApiModelProperty("Name or title of the task")
    var name: String? = null

    /**
     * Free text description of the task
     **/
    @ApiModelProperty("Free text description of the task")
    var description: String? = null

    /**
     * Indication of how important/urgent this task is with a number between
     * 0 and 100 where higher values mean a higher priority and lower values mean
     * lower priority: [0..19] lowest, [20..39] low, [40..59] normal, [60..79] high,
     * [80..100] highest
     **/
    @ApiModelProperty("Indication of how important/urgent this task is with a number between 0 and 100 where higher values mean a higher priority and lower values mean owner priority: [0..19] lowest, [20..39] low, [40..59] normal, [60..79] high, [80..100] highest")
    var priority: Double? = null

    /**
     * The userId of the person to which this task is assigned or delegated
     **/
    @ApiModelProperty("The userId of the person to which this task is assigned or delegated")
    var assignee: String? = null

    /**
     * The userId of the person that is responsible for this task. This is used when a task is delegated
     **/
    @ApiModelProperty("The userId of the person that is responsible for this task. This is used when a task is delegated")
    var owner: String? = null

    /**
     * The date/time when this task was created
     **/
    @ApiModelProperty("The date/time when this task was created")
    var created: DateTime? = null

    /**
     * Due date of the task
     **/
    @ApiModelProperty("Due date of the task")
    var due: DateTime? = null

    /**
     * Follow-up date of the task
     **/
    @ApiModelProperty("Follow-up date of the task")
    var followUp: DateTime? = null

    /**
     * The current delegation state for this task
     **/
    @ApiModelProperty("The current delegation state for this task")
    var delegationState: DelegationState? = null

    /**
     * The parent task for which this task is a subtask
     **/
    @ApiModelProperty("The parent task for which this task is a subtask")
    var parentTaskId: String? = null

    /**
     * Indicated whether this task is suspended or not
     **/
    @ApiModelProperty("Indicated whether this task is suspended or not")
    var suspended: Boolean? = null

    /**
     * Provides the form key for the task
     **/
    @ApiModelProperty("Provides the form key for the task")
    var formKey: String? = null

    /**
     * Variables related to this task
     **/
    @ApiModelProperty("Variables related to this task")
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
            curoTask.assignee = task.assignee
            curoTask.owner = task.owner
            curoTask.created = DateTime(task.createTime)
            curoTask.due =  DateTime(task.dueDate)
            curoTask.followUp =  DateTime(task.followUpDate)
            curoTask.delegationState = task.delegationState
            curoTask.parentTaskId = task.parentTaskId
            curoTask.suspended = task.isSuspended
            curoTask.formKey = task.formKey

            return curoTask
        }
    }


}
