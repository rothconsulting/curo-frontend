package ch.umb.curo

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.fasterxml.jackson.databind.ObjectMapper
import org.camunda.bpm.engine.HistoryService
import org.camunda.bpm.engine.RuntimeService
import org.camunda.bpm.engine.TaskService
import org.joda.time.DateTime
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.isEqualTo
import java.io.IOException
import java.security.GeneralSecurityException
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

/**
 * Testing of the Curo Oauth2 Authentication
 *
 * TODO:
 * [ ] verifyJwt part
 *
 * @author itsmefox
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
@ActiveProfiles("oauth2")
class CuroOAuth2AuthenticationTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var taskService: TaskService

    @Autowired
    lateinit var runtimeService: RuntimeService

    @Value("classpath:keys/private_key_pkcs8.pem")
    lateinit var privateKey: Resource

    @Value("classpath:keys/public_key.pem")
    lateinit var publicKey: Resource

    @Test
    fun `OAuth 2 - No token`() {
        mockMvc.get("/curo-api/tasks/12345") {
            accept = MediaType.APPLICATION_JSON
        }.andExpect {
            status { isEqualTo(401) }
        }
    }

    @Test
    fun `OAuth 2 - Valid token`() {
        val token = getToken("https://auth.curo.world/auth/realms/test", DateTime.now().plusHours(1), true)

        val newInstance = runtimeService.startProcessInstanceByKey("Process_1")
        val task = taskService.createTaskQuery().processInstanceId(newInstance.rootProcessInstanceId).singleResult()

        mockMvc.get("/curo-api/tasks/${task.id}") {
            accept = MediaType.APPLICATION_JSON
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isEqualTo(200) }
        }
    }

    private fun getToken(issuer: String, expires: DateTime, isAccessToken: Boolean = true): String {
        val newJWT = JWT.create()
        newJWT.withIssuer(issuer)

        if (isAccessToken) {
            newJWT.withClaim("email", "info@curo.world")
            newJWT.withClaim("preferred_username", "demo")
            newJWT.withClaim("typ", "Bearer")
            newJWT.withExpiresAt(expires.toDate())
        } else {
            newJWT.withClaim("typ", "Refresh")
            newJWT.withExpiresAt(expires.toDate())
        }

        newJWT.withIssuedAt(DateTime.now().toDate())

        val algorithm = Algorithm.RSA256(null, getPrivateKey())

        return newJWT.sign(algorithm)
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    fun getPrivateKey(): RSAPrivateKey {
        var privateKeyContent = String(privateKey.inputStream.readBytes())
        privateKeyContent = privateKeyContent.replace("\n", "").replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");
        val kf = KeyFactory.getInstance("RSA")

        val keySpecPKCS8 = PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyContent))
        val privKey = kf.generatePrivate(keySpecPKCS8) as RSAPrivateKey

        return privKey
    }

    @Throws(IOException::class, GeneralSecurityException::class)
    fun getPublicKey(): RSAPublicKey {
        var publicKeyContent = String(publicKey.inputStream.readBytes())
        publicKeyContent = publicKeyContent.replace("\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "")
        val kf = KeyFactory.getInstance("RSA")

        val keySpecX509 = X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyContent))
        val pubKey = kf.generatePublic(keySpecX509) as RSAPublicKey

        return pubKey
    }
}
