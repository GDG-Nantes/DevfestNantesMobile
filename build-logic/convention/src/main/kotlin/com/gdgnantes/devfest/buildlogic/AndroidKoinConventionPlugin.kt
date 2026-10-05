package com.gdgnantes.devfest.buildlogic

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

/**
 * `devfest.android.koin` — adds the standard set of Koin dependencies for Android
 * modules: `koin-android` and `koin-androidx-compose` (which brings
 * `koinViewModel()`), plus `koin-test` and `junit` for per-module graph
 * verification tests. Applies no Gradle plugin and no KSP: Koin is plain DSL
 * (DI-V2-01 defers annotations / the compiler plugin). Applied directly by
 * `:androidApp` and transitively by `devfest.android.feature` for every
 * feature module.
 */
class AndroidKoinConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                add("implementation", libs.findLibrary("koin-android").get())
                add("implementation", libs.findLibrary("koin-androidx-compose").get())
                add("testImplementation", libs.findLibrary("koin-test").get())
                add("testImplementation", libs.findLibrary("junit").get())
            }
        }
    }
}
