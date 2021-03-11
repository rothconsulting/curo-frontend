package ch.umb.curo


import ch.umb.curo.starter.service.FlowToNextService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension


@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("flowToNext")
class FlowToNextTest {

    @Autowired
    lateinit var taskService: TaskService

    @Autowired
    lateinit var runtimeService: RuntimeService

    @Autowired
    lateinit var flowToNextService: FlowToNextService

    @AfterEach
    private fun cleanProcesses() {
        val list = runtimeService.createProcessInstanceQuery().processDefinitionKey("Process_2").list()
        list.forEach {
            runtimeService.suspendProcessInstanceById(it.rootProcessInstanceId)
            runtimeService.deleteProcessInstance(it.rootProcessInstanceId, "ABORT", true, true, true, true)
        }
    }

    @Test
    fun `flowToNext() - loading next task should with processInstanceId as input`() {
        val newInstance = runtimeService.startProcessInstanceByKey("Process_2")
        val task1 = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        taskService.complete(task1.id)

        val next1 = flowToNextService.getNextTask(task1, true, 10)
        assert(!next1.flowToEnd)
        assert(!next1.flowToNextTimeoutExceeded)

        val task2 = taskService.createTaskQuery().taskId(next1.flowToNext[0]).singleResult()
        assert(task2 != null)

        taskService.complete(task2.id)

        val next2 = flowToNextService.getNextTask(newInstance.id, null, 10)
        assert(!next2.flowToEnd)
        assert(!next2.flowToNextTimeoutExceeded)

        val task3 = taskService.createTaskQuery().taskId(next2.flowToNext[0]).singleResult()
        assert(task3 != null)

        taskService.complete(task3.id)

        val next3 = flowToNextService.getNextTask(newInstance.id, null, 10)
        assert(!next3.flowToEnd)
        assert(!next3.flowToNextTimeoutExceeded)

        val task4 = taskService.createTaskQuery().taskId(next3.flowToNext[0]).singleResult()
        assert(task4 != null)

        taskService.complete(task4.id)

        val next4 = flowToNextService.getNextTask(newInstance.id, null, 10)
        assert(next4.flowToEnd)
    }

    @Test
    fun `flowToNext() - loading next task should with task as input`() {
        val newInstance = runtimeService.startProcessInstanceByKey("Process_2")
        val task1 = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()
        taskService.complete(task1.id)

        val next1 = flowToNextService.getNextTask(task1, true, 10)
        assert(!next1.flowToEnd)
        assert(!next1.flowToNextTimeoutExceeded)

        val task2 = taskService.createTaskQuery().taskId(next1.flowToNext[0]).singleResult()
        assert(task2 != null)

        taskService.complete(task2.id)

        val next2 = flowToNextService.getNextTask(task2, true, 10)
        assert(!next2.flowToEnd)
        assert(!next2.flowToNextTimeoutExceeded)

        val task3 = taskService.createTaskQuery().taskId(next2.flowToNext[0]).singleResult()
        assert(task3 != null)

        taskService.complete(task3.id)

        val next3 = flowToNextService.getNextTask(task3, true, 10)
        assert(!next3.flowToEnd)
        assert(!next3.flowToNextTimeoutExceeded)

        val task4 = taskService.createTaskQuery().taskId(next3.flowToNext[0]).singleResult()
        assert(task4 != null)

        taskService.complete(task4.id)

        val next4 = flowToNextService.getNextTask(task4, true, 10)
        assert(next4.flowToEnd)
    }
}
