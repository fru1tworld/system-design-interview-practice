pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "web-crawler"

include("common:event")
include("web-crawler-app")
include("html-downloader")
include("html-parser")
include("integration-test")
