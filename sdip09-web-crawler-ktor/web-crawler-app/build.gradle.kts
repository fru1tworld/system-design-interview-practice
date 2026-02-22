plugins {
    alias(libs.plugins.ktor)
    application
}

application {
    mainClass.set("webcrawler.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(project(":common:event"))

    // Ktor Server
    implementation(libs.bundles.ktor.server)

    // Kafka
    implementation(libs.kafka.clients)
    implementation(libs.kotlinx.coroutines.core)

    // UUID
    implementation(libs.uuid.creator)

    // Logging
    implementation(libs.logback.classic)

    // Testing
    testImplementation(libs.bundles.ktor.test)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.kotlin.test)
}
