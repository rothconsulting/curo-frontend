package ch.umb.curo.starter.helper.camunda.mock

import ch.umb.curo.starter.helper.camunda.CamundaVariableDefinition
import ch.umb.curo.starter.helper.camunda.CamundaVariableListDefinition
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * CamundaVariableHelper generic mock
 *
 * This generic implementation will use the correct MockFramework based on what is present on the classpath
 *
 * Example:
 * <blockquote><pre>
 *     private var delegateExecution: DelegateExecution = Mockito.mock(DelegateExecution::class.java)
 *     private lateinit var variableHelperMock: CamundaVariableHelperMock
 *
 *     @BeforeAll
 *     fun init(){
 *         variableHelperMock = CamundaVariableHelperMock(delegateExecution, true)
 *     }
 *
 *     @Test
 *     fun example() {
 *         val variable = CamundaVariable("awesomeVariable", String::class.java)
 *         variableHelperMock.mock(variable, "Test")
 *
 *         CamundaVariableHelper(delegateExecution)[variable]
 *     }
 * </pre></blockquote>
 *
 * @param variableScope Real or mocked delegate execution.
 * @param logging           Should Curo output log during testing
 */
class CamundaVariableHelperMock(override val variableScope: DelegateExecution, override val logging: Boolean = false) :
    CamundaVariableHelperMockInterface {

    override val logger: Logger = LoggerFactory.getLogger(CamundaVariableHelperMock::class.java)
    private val implementation: CamundaVariableHelperMockInterface

    init {
        implementation = getCorrectMockFramework()
        if (logging) logger.info("using ${implementation::class.java.name} as mock framework implementation")
    }

    override fun mock(variable: CamundaVariableDefinition<*>, value: Any?) {
        implementation.mock(variable, value)
    }

    override fun mockWithJson(variable: CamundaVariableDefinition<*>, value: String) {
        implementation.mockWithJson(variable, value)
    }

    override fun mock(variable: CamundaVariableListDefinition<*>, value: List<*>?) {
        implementation.mock(variable, value)
    }

    override fun mockWithJson(variable: CamundaVariableListDefinition<*>, value: String) {
        implementation.mockWithJson(variable, value)
    }

    private fun getCorrectMockFramework(): CamundaVariableHelperMockInterface {
        return when {
            Class.forName(
                this::class.java.classLoader.unnamedModule,
                "org.mockito.Mockito"
            ) != null -> CamundaVariableHelperMockito(variableScope, logging)
            else -> throw IllegalArgumentException("Curo was not able to find a supported mocking framework!")
        }
    }

}
