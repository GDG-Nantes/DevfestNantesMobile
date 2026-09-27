package com.gdgnantes.devfest.buildlogic

import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.api.artifacts.VersionCatalogsExtension
import org.gradle.kotlin.dsl.getByType

internal val Project.libs: VersionCatalog
    get() = extensions.getByType<VersionCatalogsExtension>().named("libs")

/**
 * Derives the module's Android namespace / Kotlin package root from its Gradle
 * path, e.g. `:core:model` -> `com.gdgnantes.devfest.core.model`,
 * `:feature:session-detail` -> `com.gdgnantes.devfest.feature.sessiondetail`.
 */
fun Project.moduleNamespace(): String =
    "com.gdgnantes.devfest." + path.removePrefix(":").replace(':', '.').replace("-", "")
