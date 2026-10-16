package com.gdgnantes.devfest.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * `devfest.android.feature` — every feature module (Gradle path segment
 * `feature`) applies this and nothing else. Layers `devfest.android.library`
 * + `devfest.android.koin` (the only DI convention) and adds the standard
 * core-module dependency set every feature needs. Must NEVER add a dependency on another feature module
 * (ARCH-02 — features stay siblings, never depend on each other).
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("devfest.android.library")
            pluginManager.apply("devfest.android.koin")

            dependencies {
                add("implementation", project(":core:ui"))
                add("implementation", project(":core:data"))
                add("implementation", project(":core:model"))
                add("implementation", project(":core:analytics"))
                add("testImplementation", project(":core:testing"))
                add("androidTestImplementation", project(":core:testing"))
                add("androidTestImplementation", libs.findLibrary("androidx-compose-ui-test-junit4").get())
                add("androidTestImplementation", libs.findLibrary("androidx-test-ext-junit").get())
                add("androidTestImplementation", libs.findLibrary("androidx-test-runner").get())
                add("androidTestImplementation", libs.findLibrary("koin-test-junit4").get())
            }
        }
    }
}
