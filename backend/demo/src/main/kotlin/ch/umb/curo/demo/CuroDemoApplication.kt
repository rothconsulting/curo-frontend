package ch.umb.curo.demo

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer

@SpringBootApplication(scanBasePackages = ["ch.umb.curo"])
open class CuroDemoApplication : SpringBootServletInitializer() {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(CuroDemoApplication::class.java, *args)
        }
    }

    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {
        return application.sources(CuroDemoApplication::class.java)
    }
}
