package com.gdgnantes.devfest.shared

import com.gdgnantes.devfest.core.data.BookmarksStore
import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.core.data.SessionFiltersStore
import com.gdgnantes.devfest.core.domain.agenda.ClassifySessionUseCase
import com.gdgnantes.devfest.core.domain.agenda.ObserveAgendaUseCase
import com.gdgnantes.devfest.core.domain.speakers.SpeakerIndexUseCase
import com.gdgnantes.devfest.core.domain.venue.VenueLanguageUseCase
import com.gdgnantes.devfest.shared.di.initKoin
import org.koin.mp.KoinPlatform
import org.koin.mp.KoinPlatformTools
import org.koin.mp.Lockable

/**
 * Swift-facing entry to the dependency graph. Deliberately a plain object so no DI-framework type
 * appears in the Swift API: Swift sees [start], [store] and the accessors of the shared use cases and
 * iOS stores, each returning a concrete interface or class.
 *
 * Every accessor is self-starting like [store], so a ViewModel created before the app entry point has
 * called [start] (SwiftUI previews, test hosts) does not crash with an uncatchable Kotlin exception.
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
    fun store(): DevFestNantesStore = resolve()

    /** The combined agenda use case (a new instance per call; it holds no state). */
    fun observeAgenda(): ObserveAgendaUseCase = resolve()

    /** The process-wide bookmarks store single, backed by `NSUserDefaults`. */
    fun bookmarksStore(): BookmarksStore = resolve()

    /** The process-wide agenda filters store single, backed by `NSUserDefaults`. */
    fun sessionFiltersStore(): SessionFiltersStore = resolve()

    /** The speaker index use case. */
    fun speakerIndex(): SpeakerIndexUseCase = resolve()

    /** The venue language use case. */
    fun venueLanguage(): VenueLanguageUseCase = resolve()

    /** The session classification use case. */
    fun classifySession(): ClassifySessionUseCase = resolve()

    private inline fun <reified T : Any> resolve(): T {
        start()
        return KoinPlatform.getKoin().get()
    }
}
