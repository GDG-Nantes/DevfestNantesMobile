package com.gdgnantes.devfest.shared

import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.shared.di.initKoin
import org.koin.mp.KoinPlatform
import org.koin.mp.KoinPlatformTools
import org.koin.mp.Lockable

/**
 * Swift-facing entry to the dependency graph. Deliberately a plain object so no DI-framework type
 * appears in the Swift API; Swift sees only [start] and [store].
 */
object IosDependencies {
    private val lock = Lockable()

    /** Starts the dependency graph once; a second call, from any thread, is a no-op. */
    fun start() {
        KoinPlatformTools.synchronized(lock) {
            if (KoinPlatform.getKoinOrNull() == null) {
                initKoin()
            }
        }
    }

    /**
     * The process-wide store single. Self-starting, so a ViewModel created before the app entry point
     * has called [start] (SwiftUI previews, test hosts) does not crash with an uncatchable Kotlin
     * exception.
     */
    fun store(): DevFestNantesStore {
        start()
        return KoinPlatform.getKoin().get()
    }
}
