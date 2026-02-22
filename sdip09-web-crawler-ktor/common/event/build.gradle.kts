plugins {
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.datetime)
    implementation(libs.uuid.creator)
    implementation(libs.logback.classic)

    testImplementation(libs.kotlin.test)
}

tasks.named<Jar>("jar") {
    enabled = true
}
