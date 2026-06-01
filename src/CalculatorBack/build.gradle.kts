plugins {
	java
	id("org.springframework.boot") version "4.0.6"
	id("io.spring.dependency-management") version "1.1.7"
	jacoco
	checkstyle
}

group = "ru.denis"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(25)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	runtimeOnly("org.postgresql:postgresql")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testImplementation("org.testcontainers:junit-jupiter:1.19.1")
	testImplementation("org.testcontainers:postgresql:1.19.1")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.8")
	implementation("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
}

checkstyle {
	toolVersion = "10.21.4"
	configFile = file("config/checkstyle/checkstyle.xml")
	isIgnoreFailures = true
}

// Workaround: Gradle 9 + JDK 25 fails to load classes from paths containing
// non-ASCII characters on Windows. We sync compiled outputs to an ASCII-only
// temp dir so the test worker classloader can find them.
val asciiTestOutput = file("${System.getProperty("java.io.tmpdir")}/calculator-test-out/test")
val asciiMainOutput = file("${System.getProperty("java.io.tmpdir")}/calculator-test-out/main")

val syncTestClassesToAsciiPath by tasks.registering(Sync::class) {
	dependsOn(tasks.compileTestJava)
	from(sourceSets.test.get().output.classesDirs)
	into(asciiTestOutput)
}

val syncMainClassesToAsciiPath by tasks.registering(Sync::class) {
	dependsOn(tasks.compileJava)
	from(sourceSets.main.get().output.classesDirs)
	into(asciiMainOutput)
}

tasks.withType<Test> {
	dependsOn(syncTestClassesToAsciiPath, syncMainClassesToAsciiPath)
	testClassesDirs = files(asciiTestOutput)
	classpath = files(asciiTestOutput, asciiMainOutput) +
		(classpath - sourceSets.test.get().output.classesDirs - sourceSets.main.get().output.classesDirs)
	useJUnitPlatform()
	finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required = false
		html.required = true
	}
}
