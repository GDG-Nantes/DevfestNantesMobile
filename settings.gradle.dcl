pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "DevFest_Nantes"
include(":androidApp")
include(":shared")
include(":core:model")
include(":core:network")
include(":core:analytics")
include(":core:data")
include(":core:testing")
include(":core:ui")
