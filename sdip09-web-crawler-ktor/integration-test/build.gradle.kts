plugins {
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(project(":common:event"))
    implementation(project(":web-crawler-app"))
    implementation(project(":html-downloader"))

    // Ktor Test
    testImplementation(libs.bundles.ktor.test)
    testImplementation(libs.bundles.ktor.server)
    testImplementation(libs.bundles.ktor.client)

    // Exposed ORM
    testImplementation(libs.bundles.exposed)

    // Kafka
    testImplementation(libs.kafka.clients)
    testImplementation(libs.kotlinx.coroutines.core)

    // Database
    testImplementation(libs.h2.database)

    // Testing
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.kotlin.test)

    // Logging
    testImplementation(libs.logback.classic)
}

tasks.test {
    useJUnitPlatform()
    maxParallelForks = 1  // 통합 테스트는 순차 실행
    testLogging {
        events("passed", "skipped", "failed")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}
