package ch.umb.curo

import org.camunda.bpm.engine.rest.util.EngineUtil
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("init")
class InitCreationTest {

    @Test
    fun testInitCreation() {
        //Check groups
        val engine = EngineUtil.lookupProcessEngine(null)
        val groups = engine.identityService.createGroupQuery().list().map { it.id }
        assert(groups.contains("worker"))
        assert(groups.contains("supporter"))
        assert(groups.contains("teamlead"))

        //Check users
        val users = engine.identityService.createUserQuery().list()

        val bobTower = users.firstOrNull { it.id == "bob_tower" }
        assert(bobTower != null)
        assert(engine.identityService.checkPassword("bob_tower", "testPassword"))
        val bobTowerGroups = engine.identityService.createGroupQuery().groupMember("bob_tower").list().map { it.id }
        assert(bobTowerGroups.contains("camunda-admin"))

        val sahraDoe = users.firstOrNull { it.id == "sahra_doe" }
        assert(sahraDoe != null)
        val sahraDoeGroups = engine.identityService.createGroupQuery().groupMember("sahra_doe").list().map { it.id }
        assert(sahraDoeGroups.contains("worker"))
        assert(sahraDoeGroups.contains("supporter"))

        val richardMNunez = users.firstOrNull { it.id == "richard_m_nunez" }
        assert(richardMNunez != null)
        val richardMNunezGroups =
            engine.identityService.createGroupQuery().groupMember("richard_m_nunez").list().map { it.id }
        assert(richardMNunezGroups.contains("teamlead"))

        //Check updated groups on existing user
        val demo = users.firstOrNull { it.id == "demo" }
        assert(demo != null)
        val demoGroups = engine.identityService.createGroupQuery().groupMember("demo").list().map { it.id }
        assert(demoGroups.contains("camunda-admin"))
        assert(demoGroups.contains("worker"))
        assert(demoGroups.contains("supporter"))
        assert(demoGroups.contains("teamlead"))
    }

}
