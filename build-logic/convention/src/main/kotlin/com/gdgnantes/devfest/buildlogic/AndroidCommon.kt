package com.gdgnantes.devfest.buildlogic

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinAndroidProjectExtension

/**
 * Configuration shared by every Android module regardless of whether it is
 * `com.android.application` or `com.android.library` (`CommonExtension`
 * applies to both, AGP 9). Replicates `androidApp/build.gradle.kts`'s
 * pre-split shape exactly (ARCH-01) — `targetSdk` is app-only (see
 * [AndroidApplicationConventionPlugin]) and is deliberately NOT set here.
 *
 * Note: `CommonExtension`'s nested-block DSL sugar (`defaultConfig { }`,
 * `compileOptions { }`, ...) is a Gradle Kotlin DSL script-only synthetic
 * accessor and is unavailable from ordinary Kotlin plugin source, so the
 * properties below are configured directly.
 */
internal fun Project.configureAndroidCommon(commonExtension: CommonExtension) {
    commonExtension.compileSdk = AndroidSdk.compile

    commonExtension.defaultConfig.minSdk = AndroidSdk.min
    commonExtension.defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

    commonExtension.compileOptions.sourceCompatibility = JavaVersion.VERSION_17
    commonExtension.compileOptions.targetCompatibility = JavaVersion.VERSION_17

    commonExtension.buildFeatures.compose = true

    commonExtension.testOptions.unitTests.isReturnDefaultValues = true
    commonExtension.testOptions.unitTests.isIncludeAndroidResources = true

    // Leaf modules (e.g. :core:ui today, every future :feature:* module) legitimately have
    // zero unit tests until Phase 5 adds coverage — `testDebugUnitTest` must not fail the
    // whole module graph's `./gradlew testDebugUnitTest` CI job just because a module hasn't
    // grown tests yet (Gradle's default `failOnNoDiscoveredTests = true` would otherwise fail
    // any such leaf module the instant it applies the Hilt convention plugin).
    tasks.withType<Test>().configureEach {
        failOnNoDiscoveredTests.set(false)
    }

    extensions.configure(KotlinAndroidProjectExtension::class.java) {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                "-Xopt-in=kotlin.RequiresOptIn",
                "-Xopt-in=kotlin.Experimental",
            )
        }
    }

    dependencies {
        val composeBom = libs.findLibrary("androidx-compose-bom").get()
        add("implementation", platform(composeBom))
        add("testImplementation", platform(composeBom))
        add("androidTestImplementation", platform(composeBom))
        add("implementation", libs.findLibrary("androidx-compose-material3").get())
        add("implementation", libs.findLibrary("androidx-compose-ui-tooling-preview").get())
        add("debugImplementation", libs.findBundle("debug").get())
        add("testImplementation", libs.findBundle("test").get())
    }

    configureDependencyResolutionRules()
}
