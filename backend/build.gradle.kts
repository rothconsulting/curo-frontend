import org.springframework.boot.gradle.tasks.bundling.BootJar
import org.gradle.language.jvm.tasks.ProcessResources

plugins {
	id("org.springframework.boot") version "2.2.4.RELEASE"
	id("io.spring.dependency-management") version "1.0.9.RELEASE"
	id("com.github.node-gradle.node") version "2.2.2"
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

node {
	download = true
	workDir = file("${project.buildDir}/resources/main/curo-webapp/node")
	npmWorkDir = file("${project.buildDir}/resources/main/curo-webapp")
	nodeModulesDir = file("${project.buildDir}/resources/main/curo-webapp")
}

sourceSets {
	main {
		java {
			exclude("${project.buildDir}/resources/main/curo-webapp")
		}
	}
}

tasks.getByName<BootJar>("bootJar") {
	enabled = false
}

tasks.getByName<Jar>("jar") {
	dependsOn("buildAngularAndCopy")
	enabled = true
	exclude("curo-webapp")
}


tasks.withType<Test> {
	dependsOn("buildAngularAndCopy")
	useJUnitPlatform()
}

tasks.register<com.moowork.gradle.node.npm.NpxTask>("buildAngular") {
	dependsOn("npmInstall")
	command = "ng"
	args = listOf("build") //["build", "--prod"]
	inputs.files("${project.buildDir}/resources/main/curo-webapp/package.json",
			"${project.buildDir}/resources/main/curo-webapp/package-lock.json",
			"${project.buildDir}/resources/main/curo-webapp/angular.json",
			"${project.buildDir}/resources/main/curo-webapp/tsconfig.json",
			"${project.buildDir}/resources/main/curo-webapp/tsconfig.app.json")
	inputs.dir("${project.buildDir}/resources/main/curo-webapp/src")
	inputs.dir(fileTree("${project.buildDir}/resources/main/curo-webapp/node_modules").exclude(".cache"))
	outputs.dir("${project.buildDir}/resources/main/curo-webapp/dist")
}

tasks.register<Copy>("buildAngularAndCopy") {
	dependsOn("buildAngular")
	mustRunAfter("processResources")
	from("${project.buildDir}/resources/main/curo-webapp/dist/curo-webapp")
	into("${project.buildDir}/resources/main/static")
}

tasks.getByName<ProcessResources>("processResources") {
	exclude("**/node_modules/**")
}