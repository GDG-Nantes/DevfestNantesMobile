package com.gdgnantes.devfest.shared

import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.shared.di.initKoin
import org.koin.mp.KoinPlatform

/**
 * Swift-facing entry to the dependency graph. Deliberately a plain object so no DI-framework type
 * appears in the Swift API; Swift sees only [start] and [store].
 */
object IosDependencies {
    /** Starts the dependency graph once; a second call is a no-op. */
    fun start() {
        if (KoinPlatform.getKoinOrNull() == null) {
            initKoin()
        }
    }

    /** The process-wide store single. */
    fun store(): DevFestNantesStore = KoinPlatform.getKoin().get()
}
