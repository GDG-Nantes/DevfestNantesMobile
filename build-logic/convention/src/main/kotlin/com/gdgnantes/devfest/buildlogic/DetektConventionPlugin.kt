package com.gdgnantes.devfest.buildlogic

import dev.detekt.gradle.Detekt
import dev.detekt.gradle.DetektCreateBaselineTask
import dev.detekt.gradle.extensions.DetektExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.withType

/**
 * Applies dev.detekt and wires it to the single repo-wide config
 * (`linters/detekt-config.yml`), replacing the block that used to be
 * duplicated near-identically in shared/build.gradle.kts and
 * androidApp/build.gradle.kts (ARCH-01).
 */
class DetektConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("dev.detekt")

            extensions.configure<DetektExtension> {
                buildUponDefaultConfig.set(true)
                allRules.set(false)
                autoCorrect.set(false)
                config.setFrom(rootProject.file("linters/detekt-config.yml"))
            }

            dependencies {
                add("detektPlugins", libs.findLibrary("detekt-fomatting").get())
            }
        }
    }
}

/**
 * KMP modules (`devfest.kmp.library`) restrict Detekt analysis to the exact
 * source-set list :shared used before the module split.
 */
internal fun Project.configureKmpDetektSources() {
    extensions.configure<DetektExtension> {
        source.setFrom(
            "src/commonMain/kotlin",
            "src/androidMain/kotlin",
            "src/iosMain/kotlin",
        )
    }
}

/**
 * Android-only modules keep default Detekt source discovery but must pin the
 * Detekt/DetektCreateBaselineTask jvmTarget to "1.8" — exactly androidApp's
 * pre-split configuration.
 */
internal fun Project.configureAndroidDetektJvmTarget() {
    tasks.withType<Detekt>().configureEach {
        jvmTarget.set("1.8")
    }
    tasks.withType<DetektCreateBaselineTask>().configureEach {
        jvmTarget.set("1.8")
    }
}
