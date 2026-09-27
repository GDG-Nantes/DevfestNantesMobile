package com.gdgnantes.devfest.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * `devfest.android.hilt` — applies Dagger Hilt + KSP and the standard set of
 * Hilt dependencies (main + androidTest + test variants), plus
 * `hilt-navigation-compose` (D-02). Applied directly by `:androidApp` and
 * `:core:ui` (which hosts a `@HiltViewModel`), and transitively by
 * `devfest.android.feature` for every feature module.
 */
class AndroidHiltConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.google.dagger.hilt.android")
            pluginManager.apply("com.google.devtools.ksp")

            dependencies {
                add("implementation", libs.findLibrary("dagger-hilt-android").get())
                add("ksp", libs.findLibrary("dagger-hilt-compiler").get())
                add("androidTestImplementation", libs.findLibrary("dagger-hilt-android-testing").get())
                add("kspAndroidTest", libs.findLibrary("dagger-hilt-compiler").get())
                add("testImplementation", libs.findLibrary("dagger-hilt-android-testing").get())
                add("kspTest", libs.findLibrary("dagger-hilt-compiler").get())
                add("implementation", libs.findLibrary("androidx-hilt-navigation-compose").get())
            }
        }
    }
}
