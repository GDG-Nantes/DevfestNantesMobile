package com.gdgnantes.devfest.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * `devfest.android.feature` — every feature module (Gradle path segment
 * `feature`) applies this and nothing else. Layers `devfest.android.library`
 * + `devfest.android.hilt` and adds the standard core-module dependency set
 * every feature needs. Must NEVER add a dependency on another feature module
 * (ARCH-02 — features stay siblings, never depend on each other).
 */
class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("devfest.android.library")
            pluginManager.apply("devfest.android.hilt")

            dependencies {
                add("implementation", project(":core:ui"))
                add("implementation", project(":core:data"))
                add("implementation", project(":core:model"))
                add("implementation", project(":core:analytics"))
                add("testImplementation", project(":core:testing"))
            }
        }
    }
}
