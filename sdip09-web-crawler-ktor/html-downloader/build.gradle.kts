plugins {
    alias(libs.plugins.ktor)
    application
}

application {
    mainClass.set("htmldownloader.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(project(":common:event"))

    // Ktor Server
    implementation(libs.bundles.ktor.server)

    // Ktor Client
    implementation(libs.bundles.ktor.client)

    // Exposed ORM
    implementation(libs.bundles.exposed)

    // Kotlinx
    implementation(libs.kafka.clients)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)

    // UUID
    implementation(libs.uuid.creator)

    // Logging
    implementation(libs.logback.classic)

    // Database
    runtimeOnly(libs.h2.database)

    // Testing
    testImplementation(libs.bundles.ktor.test)
    testImplementation(libs.bundles.kotest)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.h2.database)
}
