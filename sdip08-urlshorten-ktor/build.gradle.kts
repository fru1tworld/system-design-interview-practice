plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktor)
    application
}

group = "fru1t"
version = "0.0.1-SNAPSHOT"

application {
    mainClass.set("urlshorten.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    // Ktor Server
    implementation(libs.bundles.ktor.server)

    // Exposed ORM
    implementation(libs.bundles.exposed)

    // Kotlinx
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.serialization.json)

    // Database
    runtimeOnly(libs.mysql.connector)

    // Logging
    implementation(libs.logback.classic)

    // Testing
    testImplementation(libs.ktor.server.test.host)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.h2.database)
}

tasks.test {
    useJUnitPlatform()
}
