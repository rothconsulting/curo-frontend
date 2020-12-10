package ch.umb.curo

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ContextConfiguration

@SpringBootTest
@ContextConfiguration(classes = [CuroTestApplication::class])
class SpringBootTest {

    @Test
    fun contextLoads() {
        //Thread.sleep(Long.MAX_VALUE)
    }

}