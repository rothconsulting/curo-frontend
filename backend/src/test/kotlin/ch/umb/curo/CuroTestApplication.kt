package ch.umb.curo

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer

@SpringBootApplication(scanBasePackages = ["ch.umb.curo"])
open class CuroTestApplication : SpringBootServletInitializer() {

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            SpringApplication.run(CuroTestApplication::class.java, *args)

        }
    }

    override fun configure(application: SpringApplicationBuilder): SpringApplicationBuilder {
        return application.sources(CuroTestApplication::class.java)
    }
}
