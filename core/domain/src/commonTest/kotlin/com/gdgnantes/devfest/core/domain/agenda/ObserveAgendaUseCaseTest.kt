package com.gdgnantes.devfest.core.domain.agenda

import app.cash.turbine.test
import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.core.model.Agenda
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.SessionFilter
import com.gdgnantes.devfest.core.model.SessionFilterType
import com.gdgnantes.devfest.core.model.stubs.SessionFixtures
import com.gdgnantes.devfest.core.testing.FakeBookmarksStore
import com.gdgnantes.devfest.core.testing.FakeDevFestNantesStore
import com.gdgnantes.devfest.core.testing.FakeSessionFiltersStore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveAgendaUseCaseTest {
    private val filterSessions = FilterSessionsUseCase()
    private val groupSessions = GroupSessionsUseCase()
    private val utcWindows = UtcDayWindows(listOf(Agenda.DAY_ONE_ISO, Agenda.DAY_TWO_ISO))
    private val localDays = LocalDateDays(listOf("2025-10-16", "2025-10-17"))
    private val paris = TimeZone.of("Europe/Paris")
    private val auckland = TimeZone.of("Pacific/Auckland")
    private val bookmarkFilter = SessionFilter(SessionFilterType.BOOKMARK, "")

    private val store = FakeDevFestNantesStore()
    private val filtersStore = FakeSessionFiltersStore()
    private val bookmarksStore = FakeBookmarksStore()

    private fun TestScope.useCase(
        source: DevFestNantesStore = store,
        dispatcher: CoroutineDispatcher = UnconfinedTestDispatcher(testScheduler)
    ) = ObserveAgendaUseCase(source, filtersStore, bookmarksStore, filterSessions, groupSessions, dispatcher)

    private fun List<DaySchedule>.sessionIds(): List<List<String>> =
        map { day -> day.slots.flatMap { slot -> slot.sessions.map { it.id } } }

    /** Counts how many times `sessions` is collected: every collection is one upstream subscription. */
    private class CountingStore(private val delegate: FakeDevFestNantesStore) : DevFestNantesStore by delegate {
        var sessionSubscriptions = 0
            private set

        override val sessions: Flow<List<Session>> =
            flow {
                sessionSubscriptions++
                emitAll(delegate.sessions)
            }
    }

    @Test
    fun the_first_emission_is_the_grouped_unfiltered_agenda() = runTest {
        val expected = groupSessions.group(filterSessions.filter(SessionFixtures.all, emptySet(), emptySet()), utcWindows, paris)

        useCase().observe(utcWindows, paris).test {
            val days = awaitItem()

            assertEquals(expected, days)
            assertEquals(listOf(1, 2), days.map { it.dayIndex })
            assertEquals(listOf(6, 4), days.sessionIds().map { it.size })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun the_time_zone_is_passed_through_to_the_day_assignment() = runTest {
        val gitQuickie = SessionFixtures.gitQuickie.id

        useCase().observe(localDays, paris).test {
            val inParis = awaitItem().sessionIds()
            cancelAndIgnoreRemainingEvents()

            assertTrue(gitQuickie in inParis[0])
        }
        useCase().observe(localDays, auckland).test {
            val inAuckland = awaitItem().sessionIds()
            cancelAndIgnoreRemainingEvents()

            assertTrue(gitQuickie in inAuckland[1], "the late day-1 session lands on day 2 in Auckland")
        }
    }

    @Test
    fun changing_the_filters_emits_the_filtered_agenda() = runTest {
        useCase().observe(utcWindows, paris).test {
            awaitItem()

            filtersStore.setFilters(setOf(SessionFilter(SessionFilterType.TYPE, "CODELAB")))

            assertEquals(
                listOf(listOf(SessionFixtures.kubernetesLab.id), emptyList()),
                awaitItem().sessionIds()
            )
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun toggling_a_bookmark_under_the_bookmark_filter_emits_the_new_membership() = runTest {
        filtersStore.setFilters(setOf(bookmarkFilter))

        useCase().observe(utcWindows, paris).test {
            assertEquals(listOf(emptyList(), emptyList()), awaitItem().sessionIds())

            bookmarksStore.setBookmarked(SessionFixtures.welcome.id, true)
            assertEquals(listOf(listOf(SessionFixtures.welcome.id), emptyList()), awaitItem().sessionIds())

            bookmarksStore.setBookmarked(SessionFixtures.welcome.id, false)
            assertEquals(listOf(emptyList(), emptyList()), awaitItem().sessionIds())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun replacing_the_store_sessions_emits_the_new_agenda() = runTest {
        useCase().observe(utcWindows, paris).test {
            awaitItem()

            store.sessionsState.value = listOf(SessionFixtures.aiKeynote)

            assertEquals(listOf(emptyList(), listOf(SessionFixtures.aiKeynote.id)), awaitItem().sessionIds())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun a_new_collection_after_a_cancelled_one_subscribes_to_the_store_again() = runTest {
        val counting = CountingStore(store)
        val useCase = useCase(counting)

        useCase.observe(utcWindows, paris).test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(1, counting.sessionSubscriptions)

        useCase.observe(utcWindows, paris).test {
            awaitItem()
            cancelAndIgnoreRemainingEvents()
        }
        assertEquals(2, counting.sessionSubscriptions)
    }

    @Test
    fun the_upstream_runs_on_the_injected_dispatcher() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)

        useCase(dispatcher = dispatcher).observe(utcWindows, paris).test {
            expectNoEvents()

            assertEquals(2, awaitItem().size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun a_store_failure_propagates_to_the_collector() = runTest {
        store.failure = IllegalStateException("store failed")

        useCase().observe(utcWindows, paris).test {
            assertEquals("store failed", awaitError().message)
        }
    }
}
