import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
	id("org.springframework.boot") version "2.2.4.RELEASE"
	id("io.spring.dependency-management") version "1.0.9.RELEASE"
	kotlin("jvm") version "1.3.61"
	kotlin("plugin.spring") version "1.3.61"
}

group = "ch.umb.curo"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")

	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")

	implementation("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter:3.4.0")
	implementation("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-rest:3.4.0")
	implementation("org.camunda.bpm.springboot:camunda-bpm-spring-boot-starter-webapp:3.4.0")


	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
	testImplementation("org.springframework.boot:spring-boot-starter-web")
	testImplementation("com.h2database:h2")
}

tasks.getByName<BootJar>("bootJar") {
	enabled = false
}
