package com.gdgnantes.devfest.buildlogic

import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension

/**
 * `devfest.kmp.library` — the convention plugin every `core:*` KMP leaf module
 * applies. Replicates the plugin/target/detekt shape that used to live only
 * in `shared/build.gradle.kts` (ARCH-01), minus anything module-specific
 * (Apollo config, framework export()) which stays in the leaf build file.
 *
 * Every KMP core module declares the SAME target set as :shared (android,
 * jvm, iosX64, iosArm64, iosSimulatorArm64) — a KMP module that declares
 * jvm() cannot depend on a KMP module lacking jvm(). No core module declares
 * binaries.framework: :shared is the only module that packages the iOS
 * umbrella framework (anti three-framework problem, D-10/D-11).
 */
class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val namespace = target.moduleNamespace()

        with(target) {
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            pluginManager.apply("com.android.kotlin.multiplatform.library")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
            pluginManager.apply("devfest.detekt")

            configureKmpDetektSources()

            extensions.configure<KotlinMultiplatformExtension> {
                targets.withType(KotlinMultiplatformAndroidLibraryTarget::class.java).configureEach {
                    this.namespace = namespace
                    compileSdk = AndroidSdk.compile
                    minSdk = AndroidSdk.min
                    compilerOptions {
                        jvmTarget.set(JvmTarget.JVM_11)
                    }
                }

                jvm()
                iosX64()
                iosArm64()
                iosSimulatorArm64()

                sourceSets.configureEach {
                    languageSettings.optIn("kotlin.experimental.ExperimentalObjCName")
                }

                sourceSets.getByName("commonTest").dependencies {
                    implementation(libs.findLibrary("kotlin-test").get())
                    implementation(libs.findLibrary("kotlinx-coroutines-test").get())
                }
            }
        }
    }
}
