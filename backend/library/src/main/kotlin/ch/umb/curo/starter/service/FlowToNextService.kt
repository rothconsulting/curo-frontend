package ch.umb.curo.starter.service

import ch.umb.curo.starter.models.FlowToNextResult
import org.camunda.bpm.engine.task.Task

interface FlowToNextService {
    fun getNextTask(lastTask: Task, timeout: Int): FlowToNextResult
    fun getNextTask(lastTask: Task, assignee: String?, timeout: Int): FlowToNextResult
    fun getNextTask(processInstanceId: String, assignee: String?, timeout: Int): FlowToNextResult
    fun searchNextTask(
        processInstanceId: String,
        assignee: String?
    ): FlowToNextResult
}
