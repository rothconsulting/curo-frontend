package ch.umb.curo.starter.controller

import ch.umb.curo.starter.auth.CuroLoginMethod
import ch.umb.curo.starter.models.response.LoginMethod
import org.camunda.bpm.engine.rest.util.EngineUtil
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping("/curo-api")
class AuthenticationController {

    @PostMapping("/authenticate", consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
    fun authenticate(@RequestParam username: String, @RequestParam password: String, response: HttpServletResponse): ResponseEntity<String> {
        val engine = EngineUtil.lookupProcessEngine(null)
        val checkPassword = engine.identityService.checkPassword(username, password)
        return if (checkPassword) {
            ResponseEntity("{}", HttpStatus.OK)
        } else {
            ResponseEntity("{}", HttpStatus.UNAUTHORIZED)
        }
    }

}
