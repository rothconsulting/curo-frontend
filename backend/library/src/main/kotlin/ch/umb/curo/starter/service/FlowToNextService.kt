package ch.umb.curo.starter.service

import ch.umb.curo.starter.models.FlowToNextResult
import ch.umb.curo.starter.property.CuroProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.history.HistoricProcessInstance
import org.camunda.bpm.engine.task.Task
import org.springframework.stereotype.Service


@Service
class FlowToNextService(
    private val properties: CuroProperties,
    private val taskService: TaskService,
    private val historyService: HistoryService
) {

    fun getNextTask(lastTask: Task, ignoreAssignee: Boolean = false, timeout: Int): FlowToNextResult {
        val assignee = if (!ignoreAssignee) lastTask.assignee else null
        return getNextTask(lastTask.processInstanceId, assignee, timeout)
    }

    fun getNextTask(processInstanceId: String, assignee: String?, timeout: Int): FlowToNextResult {
        val result = runBlocking {
            return@runBlocking withTimeoutOrNull(timeout * 1000L) {
                var possibleTaskIds: List<String> = listOf()
                var processEnded = false

                while (possibleTaskIds.isEmpty() && !processEnded) {
                    val searchResult = searchNextTask(processInstanceId, assignee)
                    possibleTaskIds = searchResult.flowToNext
                    processEnded = searchResult.flowToEnd

                    if (possibleTaskIds.isEmpty()) {
                        delay(properties.flowToNext.interval.toLong())
                    }
                }

                when {
                    possibleTaskIds.isEmpty() && processEnded -> FlowToNextResult(flowToEnd = true)
                    possibleTaskIds.isEmpty() && !processEnded -> FlowToNextResult()
                    else -> FlowToNextResult(possibleTaskIds)
                }
            }
        }

        return result ?: FlowToNextResult(flowToNextTimeoutExceeded = true)
    }

    fun searchNextTask(
        processInstanceId: String,
        assignee: String?
    ): FlowToNextResult {
        val processEnded: Boolean
        val possibleTaskIds: List<String>
        var possibleProcessInstanceIds = mutableListOf(processInstanceId)
        var rootProcessInstanceId: String = processInstanceId

        //Search for root
        var foundRoot: Boolean
        do {
            val instance = historyService.createHistoricProcessInstanceQuery().processInstanceId(rootProcessInstanceId)
                .singleResult()
            if (instance != null) {
                foundRoot = (rootProcessInstanceId == instance.rootProcessInstanceId)
                rootProcessInstanceId = instance.rootProcessInstanceId
                possibleProcessInstanceIds.add(rootProcessInstanceId)
            } else {
                foundRoot = true
            }
        } while (!foundRoot)

        //Clean up list
        possibleProcessInstanceIds = possibleProcessInstanceIds.distinct().toMutableList()

        //Search leafs
        val leafProcessInstanceIds = arrayListOf<String>()
        possibleProcessInstanceIds.forEach {
            val deepProcessInstance: List<HistoricProcessInstance>? =
                historyService.createHistoricProcessInstanceQuery().superProcessInstanceId(it).list()
            if (deepProcessInstance?.isNotEmpty() == true) {
                leafProcessInstanceIds.addAll(deepProcessInstance.map { it.id })
            }
        }

        possibleProcessInstanceIds.addAll(leafProcessInstanceIds)

        //Clean up list
        possibleProcessInstanceIds = possibleProcessInstanceIds.distinct().toMutableList()

        val possibleTaskIdsQuery =
            taskService.createTaskQuery().processInstanceIdIn(*possibleProcessInstanceIds.toTypedArray())
        possibleTaskIds =
            (if (assignee != null) possibleTaskIdsQuery.taskAssignee(assignee) else possibleTaskIdsQuery).list()
                .map { it.id }

        //Check if root is completed
        val superHistoryProcessInstance =
            historyService.createHistoricProcessInstanceQuery().processInstanceId(rootProcessInstanceId).singleResult()
        processEnded = superHistoryProcessInstance?.state == "COMPLETED" && possibleTaskIds.isEmpty()

        return FlowToNextResult(possibleTaskIds, flowToEnd = processEnded)
    }

}
