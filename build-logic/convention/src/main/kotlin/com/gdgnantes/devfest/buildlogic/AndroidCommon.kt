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

    extensions.configure(KotlinAndroidProjectExtension::class.java) {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.addAll(
                "-Xopt-in=kotlin.RequiresOptIn",
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

/**
 * Disables Gradle's default `failOnNoDiscoveredTests` safety net for modules that
 * legitimately have zero unit tests today (e.g. `:core:ui`, every `:feature:*` module,
 * until Phase 5 adds coverage) — otherwise `testDebugUnitTest` fails the whole module
 * graph's `./gradlew testDebugUnitTest` CI job just because a leaf module hasn't grown
 * tests yet.
 *
 * Deliberately called ONLY from [AndroidLibraryConventionPlugin] — never from
 * [AndroidApplicationConventionPlugin] — so `:androidApp`'s own unit-test suite keeps
 * Gradle's default fail-on-no-tests discoverability safety net once it exists (WR-01).
 */
internal fun Project.configureAndroidLibraryTestDefaults() {
    tasks.withType<Test>().configureEach {
        failOnNoDiscoveredTests.set(false)
    }
}
