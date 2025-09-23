plugins {
    java
    id("org.springframework.boot") version "3.5.5"
    id("io.spring.dependency-management") version "1.1.7"
    id("jacoco")
}

group = "edu.trincoll"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-core")
}

val isJacocoReportRequested =
    gradle.startParameter.taskNames.any {
        it.contains("jacocoTestReport", ignoreCase = true)
    }

tasks.test {
    useJUnitPlatform()
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Xshare:off")

    // Print a clear summary and per-test outcomes in the console
    testLogging {
        // Show passed/failed/skipped at lifecycle level
        events(
            org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED,
            org.gradle.api.tasks.testing.logging.TestLogEvent.SKIPPED
        )
    }

    // One-line summary after the whole test suite finishes (Kotlin DSL-friendly)
    addTestListener(object : org.gradle.api.tasks.testing.TestListener {
        override fun beforeSuite(suite: org.gradle.api.tasks.testing.TestDescriptor) {}
        override fun beforeTest(testDescriptor: org.gradle.api.tasks.testing.TestDescriptor) {}
        override fun afterTest(
            testDescriptor: org.gradle.api.tasks.testing.TestDescriptor,
            result: org.gradle.api.tasks.testing.TestResult
        ) {}
        override fun afterSuite(
            suite: org.gradle.api.tasks.testing.TestDescriptor,
            result: org.gradle.api.tasks.testing.TestResult
        ) {
            if (suite.parent == null) {
                println(
                    "Tests: ${result.testCount} passed: ${result.successfulTestCount} failed: ${result.failedTestCount} skipped: ${result.skippedTestCount}"
                )
            }
        }
    })

    if (isJacocoReportRequested) {
        // When generating JaCoCo report explicitly, allow tests to fail but still produce coverage
        ignoreFailures = true
    }
}

tasks.jacocoTestReport {
    // Only run when explicitly requested or via `check`, not after every `test`
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal()
            }
        }
        rule {
            element = "CLASS"
            includes = listOf("edu.trincoll.service.*")
            excludes = listOf("*Application", "*Config", "*Exception")
            limit {
                counter = "LINE"
                minimum = "0.80".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
    // Also produce the JaCoCo report when running `check`
    dependsOn(tasks.jacocoTestReport)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(24))
    }
}
