package ch.umb.curo.starter.controller

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/auth")
class TestController {

    @GetMapping("/loginType")
    fun getLoginType(): String {
        return "Hello World"
    }
}