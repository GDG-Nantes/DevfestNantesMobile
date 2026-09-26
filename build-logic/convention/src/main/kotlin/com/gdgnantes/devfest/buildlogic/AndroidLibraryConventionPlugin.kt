package com.gdgnantes.devfest.buildlogic

import com.android.build.api.dsl.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * `devfest.android.library` — the convention every plain Android library
 * module applies (Android library + Compose + shared Detekt/SDK/compose
 * config). Feature modules layer `devfest.android.feature` on top of this
 * (which also applies `devfest.android.hilt`); `:core:ui` applies this
 * convention plus `devfest.android.hilt` directly (it hosts a
 * `@HiltViewModel` but must not get the feature convention's `:core:ui`
 * self-dependency).
 */
class AndroidLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.library")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
            pluginManager.apply("devfest.detekt")

            configureAndroidDetektJvmTarget()

            extensions.configure<LibraryExtension> {
                namespace = moduleNamespace()
                configureAndroidCommon(this)
            }
        }
    }
}
