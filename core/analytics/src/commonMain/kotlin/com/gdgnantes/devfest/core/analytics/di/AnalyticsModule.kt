package com.gdgnantes.devfest.core.analytics.di

import org.koin.core.module.Module
import org.koin.dsl.module
import kotlin.native.HiddenFromObjC

/**
 * Koin bindings of `:core:analytics`. The Firebase-backed implementations live in the Android
 * actual of [analyticsPlatformModule]; the JVM and iOS actuals are empty because those targets have
 * no Kotlin analytics implementation.
 */
@HiddenFromObjC
val analyticsModule: Module =
    module {
        includes(analyticsPlatformModule)
    }

/** Platform-specific analytics bindings, one `actual` per target. */
internal expect val analyticsPlatformModule: Module
