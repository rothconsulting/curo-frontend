package ch.umb.curo

import ch.umb.curo.model.DataModel
import ch.umb.curo.starter.helper.camunda.CamundaVariable
import ch.umb.curo.starter.helper.camunda.CamundaVariableHelper
import ch.umb.curo.starter.helper.camunda.CamundaVariableList
import ch.umb.curo.starter.helper.camunda.mock.CamundaVariableHelperMock
import org.camunda.bpm.engine.delegate.DelegateExecution
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VariableMockTest {

    private var delegateExecution: DelegateExecution = Mockito.mock(DelegateExecution::class.java)
    private lateinit var variableHelperMock: CamundaVariableHelperMock

    @BeforeAll
    fun init() {
        variableHelperMock = CamundaVariableHelperMock(delegateExecution, true)
    }

    @Test
    fun testMock() {
        val string = CamundaVariable("string", String::class.java)
        variableHelperMock.mock(string, "Test")
        assert(CamundaVariableHelper(delegateExecution)[string] == "Test")

        val int = CamundaVariable("int", Int::class.java)
        variableHelperMock.mock(int, 14)
        assert(CamundaVariableHelper(delegateExecution)[int] == 14)

        val boolean = CamundaVariable("boolean", Boolean::class.java)
        variableHelperMock.mock(boolean, true)
        assert(CamundaVariableHelper(delegateExecution)[boolean])

        val nullValue = CamundaVariable("null", String::class.java)
        variableHelperMock.mock(nullValue, null)
        assert(CamundaVariableHelper(delegateExecution).getOrNull(nullValue) == null)

        val data = DataModel()
        data.id = "Fox"
        val obj = CamundaVariable("obj", DataModel::class.java)
        variableHelperMock.mock(obj, data)
        assert(CamundaVariableHelper(delegateExecution)[obj].id == "Fox")
    }

    @Test
    fun testListMock() {
        val list = CamundaVariableList("list", String::class.java)
        variableHelperMock.mock(list, arrayListOf("plane", "car", "boat"))
        assert(CamundaVariableHelper(delegateExecution)[list][0] == "plane")

        val dataForList = DataModel()
        dataForList.id = "Fox"
        val listObj = CamundaVariableList("listObj", DataModel::class.java)
        variableHelperMock.mock(listObj, arrayListOf(dataForList, DataModel(), DataModel()))
        assert(CamundaVariableHelper(delegateExecution)[listObj][0]?.id == "Fox")
    }

    @Test
    fun testJsonMock() {
        val json = CamundaVariable("json", DataModel::class.java)
        variableHelperMock.mockWithJson(json, """{"id":"json-id"}""")
        assert(CamundaVariableHelper(delegateExecution)[json].id == "json-id")

        val jsonList = CamundaVariableList("jsonList", DataModel::class.java)
        variableHelperMock.mockWithJson(jsonList, """[{"id":"json-id"}]""")
        assert(CamundaVariableHelper(delegateExecution)[jsonList][0]?.id == "json-id")
    }

}
