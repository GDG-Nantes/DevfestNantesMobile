package com.gdgnantes.devfest.shared

/**
 * :shared intentionally has no other Kotlin sources (D-16/D-18): every declaration lives in a
 * `core:*` leaf module and :shared exists only to assemble and export() the iOS umbrella
 * framework. Kotlin/Native's compile + link tasks (`compileKotlinIos*`, `linkDebugFramework*`,
 * `linkReleaseFramework*`) treat a source set with zero files as NO-SOURCE and skip entirely —
 * including the link step, so no shared.framework would ever be produced for iOS at all. This
 * single marker keeps the source set non-empty so the framework actually links and re-exports
 * core:model / core:analytics / core:data as declared in build.gradle.kts.
 */
internal const val SHARED_FRAMEWORK_PLACEHOLDER = true
