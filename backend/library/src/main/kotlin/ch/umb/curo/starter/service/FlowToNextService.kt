package ch.umb.curo.starter.service

import ch.umb.curo.starter.models.FlowToNextResult
import ch.umb.curo.starter.property.CuroProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.camunda.bpm.engine.runtime.ProcessInstance
import org.camunda.bpm.engine.task.Task
import org.springframework.stereotype.Service


@Service
class FlowToNextService(private val properties: CuroProperties,
                        private val taskService: TaskService,
                        private val runtimeService: RuntimeService) {

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

    fun searchNextTask(processInstanceId: String,
                       assignee: String?): FlowToNextResult {
        val processEnded: Boolean
        val possibleTaskIds: List<String>
        val possibleProcessInstanceIds = arrayListOf(processInstanceId)
        var superProcessInstance: ProcessInstance?
        do {
            superProcessInstance = runtimeService.createProcessInstanceQuery().subProcessInstanceId(processInstanceId).singleResult()
            if (superProcessInstance != null) {
                possibleProcessInstanceIds.add(superProcessInstance.id)
            }
        } while (superProcessInstance != null)

        processEnded = superProcessInstance?.isEnded == true

        possibleTaskIds = if (assignee != null) {
            taskService.createTaskQuery().processInstanceIdIn(*possibleProcessInstanceIds.toTypedArray()).taskAssignee(assignee).list()
        } else {
            taskService.createTaskQuery().processInstanceIdIn(*possibleProcessInstanceIds.toTypedArray()).list()
        }.map { it.id }

        return FlowToNextResult(possibleTaskIds, flowToEnd = processEnded)
    }

}
