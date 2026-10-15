package com.gdgnantes.devfest.buildlogic

import com.android.build.api.dsl.ApplicationExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure

/**
 * `devfest.android.application` — applied by `:androidApp` only. Deliberately
 * does NOT set `namespace`/`applicationId` (the app keeps these explicit in
 * its own build file) or the DI framework (`devfest.android.koin` is layered
 * on top by `:androidApp`).
 */
class AndroidApplicationConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.application")
            pluginManager.apply("org.jetbrains.kotlin.plugin.compose")
            pluginManager.apply("devfest.detekt")
            pluginManager.apply("org.jetbrains.kotlinx.kover")

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
