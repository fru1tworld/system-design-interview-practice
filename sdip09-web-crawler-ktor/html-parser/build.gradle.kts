plugins {
    alias(libs.plugins.ktor)
    application
}

application {
    mainClass.set("htmlparser.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(project(":common:event"))

    // Ktor Server
    implementation(libs.bundles.ktor.server)

    // Jsoup for HTML parsing
    implementation(libs.jsoup)

    // Kafka
    implementation(libs.kafka.clients)
    implementation(libs.kotlinx.coroutines.core)

    // Logging
    implementation(libs.logback.classic)

    // Testing
    testImplementation(libs.bundles.ktor.test)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.kotlin.test)
}
