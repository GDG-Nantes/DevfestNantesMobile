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
    }
}
