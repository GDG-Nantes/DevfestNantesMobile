package com.gdgnantes.devfest.shared.di

import com.gdgnantes.devfest.core.data.BookmarksStore
import com.gdgnantes.devfest.core.data.SessionFiltersStore
import com.gdgnantes.devfest.core.domain.agenda.ClassifySessionUseCase
import com.gdgnantes.devfest.core.domain.agenda.ObserveAgendaUseCase
import com.gdgnantes.devfest.shared.IosDependencies
import org.koin.core.context.stopKoin
import org.koin.mp.KoinPlatform
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertSame

class IosKoinTest {
    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun start_is_idempotent_and_store_is_one_shared_instance() {
        IosDependencies.start()
        IosDependencies.start()

        val first = IosDependencies.store()
        val second = IosDependencies.store()

        assertNotNull(first)
        assertSame(first, second)
    }

    @Test
    fun domain_use_cases_resolve_after_start() {
        IosDependencies.start()

        assertNotNull(KoinPlatform.getKoin().get<ClassifySessionUseCase>())
    }

    @Test
    fun observe_agenda_use_case_resolves_after_start() {
        IosDependencies.start()

        assertNotNull(KoinPlatform.getKoin().get<ObserveAgendaUseCase>())
    }

    @Test
    fun platform_stores_resolve_as_singles_after_start() {
        IosDependencies.start()
        val koin = KoinPlatform.getKoin()

        assertSame(koin.get<BookmarksStore>(), koin.get<BookmarksStore>())
        assertSame(koin.get<SessionFiltersStore>(), koin.get<SessionFiltersStore>())
    }

    @Test
    fun observe_agenda_accessor_resolves_after_start() {
        IosDependencies.start()

        assertNotNull(IosDependencies.observeAgenda())
    }

    @Test
    fun bookmarks_store_accessor_returns_the_shared_single() {
        IosDependencies.start()

        assertSame(IosDependencies.bookmarksStore(), IosDependencies.bookmarksStore())
    }

    @Test
    fun session_filters_store_accessor_returns_the_shared_single() {
        IosDependencies.start()

        assertSame(IosDependencies.sessionFiltersStore(), IosDependencies.sessionFiltersStore())
    }

    @Test
    fun speaker_index_accessor_resolves_after_start() {
        IosDependencies.start()

        assertNotNull(IosDependencies.speakerIndex())
    }

    @Test
    fun venue_language_accessor_resolves_after_start() {
        IosDependencies.start()

        assertNotNull(IosDependencies.venueLanguage())
    }

    @Test
    fun classify_session_accessor_resolves_after_start() {
        IosDependencies.start()

        assertNotNull(IosDependencies.classifySession())
    }
}
