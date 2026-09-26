package com.gdgnantes.devfest.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * `devfest.android.application` — applied by `:androidApp` only. Deliberately
 * does NOT set `namespace`/`applicationId` (the app keeps these explicit in
 * its own build file) or Hilt (a separate `devfest.android.hilt` application
 * is layered on top, matching how `:core:ui` also applies Hilt directly).
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
            pluginManager.apply("devfest.detekt")

            configureAndroidDetektJvmTarget()

            extensions.configure<ApplicationExtension> {
                configureAndroidCommon(this)
                defaultConfig {
                    targetSdk = AndroidSdk.target
                }
            }
        }
    }
}
