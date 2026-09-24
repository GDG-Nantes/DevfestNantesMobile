plugins {
    `kotlin-dsl`
}

group = "com.gdgnantes.devfest.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("devfestDetekt") {
            id = "devfest.detekt"
            implementationClass = "com.gdgnantes.devfest.buildlogic.DetektConventionPlugin"
        }
        register("devfestKmpLibrary") {
            id = "devfest.kmp.library"
            implementationClass = "com.gdgnantes.devfest.buildlogic.KmpLibraryConventionPlugin"
        }
        register("devfestAndroidLibrary") {
            id = "devfest.android.library"
            implementationClass = "com.gdgnantes.devfest.buildlogic.AndroidLibraryConventionPlugin"
        }
        register("devfestAndroidHilt") {
            id = "devfest.android.hilt"
            implementationClass = "com.gdgnantes.devfest.buildlogic.AndroidHiltConventionPlugin"
        }
        register("devfestAndroidFeature") {
            id = "devfest.android.feature"
            implementationClass = "com.gdgnantes.devfest.buildlogic.AndroidFeatureConventionPlugin"
        }
        register("devfestAndroidApplication") {
            id = "devfest.android.application"
            implementationClass = "com.gdgnantes.devfest.buildlogic.AndroidApplicationConventionPlugin"
        }
    }
}
