package com.gdgnantes.devfest.core.domain.agenda

import com.gdgnantes.devfest.core.model.Complexity
import com.gdgnantes.devfest.core.model.Room
import com.gdgnantes.devfest.core.model.ScheduleSlot
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.SessionFilter
import com.gdgnantes.devfest.core.model.SessionFilterType
import com.gdgnantes.devfest.core.model.SessionLanguage
import com.gdgnantes.devfest.core.model.SessionType
import com.gdgnantes.devfest.core.model.stubs.SessionFixtures
import com.gdgnantes.devfest.core.testing.snapshot.AgendaSnapshot
import com.gdgnantes.devfest.core.testing.snapshot.FilterCases
import com.gdgnantes.devfest.core.testing.snapshot.GoldenFiles
import com.gdgnantes.devfest.core.testing.snapshot.Goldens
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue

/**
 * The v1.0 filter matrix of `AgendaViewModelTest` (same fixtures, same expected ids), the edge rules
 * of the shared filter, and the 15 shared filter cases against the Android and iOS v1.0 goldens.
 */
class FilterSessionsUseCaseTest {
    private val useCase = FilterSessionsUseCase()

    private val french = SessionFilter(SessionFilterType.LANGUAGE, SessionLanguage.FRENCH.name)
    private val english = SessionFilter(SessionFilterType.LANGUAGE, SessionLanguage.ENGLISH.name)
    private val advanced = SessionFilter(SessionFilterType.COMPLEXITY, Complexity.ADVANCED.name)
    private val beginner = SessionFilter(SessionFilterType.COMPLEXITY, Complexity.BEGINNER.name)
    private val conference = SessionFilter(SessionFilterType.TYPE, SessionType.CONFERENCE.name)
    private val codelab = SessionFilter(SessionFilterType.TYPE, SessionType.CODELAB.name)
    private val bookmarkedOnly = SessionFilter(SessionFilterType.BOOKMARK, "")
    private val titan = SessionFilter(SessionFilterType.ROOM, requireNotNull(SessionFixtures.composeDeepDive.room).id)
    private val belem = SessionFilter(SessionFilterType.ROOM, requireNotNull(SessionFixtures.webRendering.room).id)

    private fun idsOf(vararg sessions: Session): Set<String> = sessions.map { it.id }.toSet()

    private fun ids(
        filters: Set<SessionFilter>,
        bookmarked: Set<String> = emptySet(),
        sessions: List<Session> = SessionFixtures.all
    ): Set<String> = useCase.filter(sessions, filters, bookmarked).map { it.id }.toSet()

    @Test
    fun no_filter_keeps_every_session() {
        assertEquals(SessionFixtures.all.map { it.id }.toSet(), ids(emptySet()))
    }

    @Test
    fun french_filter_keeps_the_four_french_sessions() {
        assertEquals(
            idsOf(
                SessionFixtures.welcome,
                SessionFixtures.webRendering,
                SessionFixtures.gitQuickie,
                SessionFixtures.closing
            ),
            ids(setOf(french))
        )
    }

    @Test
    fun beginner_filter_keeps_kubernetes_lab_and_closing() {
        assertEquals(idsOf(SessionFixtures.kubernetesLab, SessionFixtures.closing), ids(setOf(beginner)))
    }

    @Test
    fun conference_type_filter_excludes_the_session_without_type() {
        val result = ids(setOf(conference))

        assertEquals(
            idsOf(SessionFixtures.composeDeepDive, SessionFixtures.webRendering, SessionFixtures.securityTalk),
            result
        )
        assertTrue(SessionFixtures.closing.id !in result)
    }

    @Test
    fun titan_room_filter_keeps_compose_deep_dive_and_closing() {
        assertEquals(idsOf(SessionFixtures.composeDeepDive, SessionFixtures.closing), ids(setOf(titan)))
    }

    @Test
    fun two_room_filters_are_ored() {
        assertEquals(
            idsOf(SessionFixtures.composeDeepDive, SessionFixtures.webRendering, SessionFixtures.closing),
            ids(setOf(titan, belem))
        )
    }

    @Test
    fun two_language_filters_are_ored() {
        assertEquals(
            SessionFixtures.all.filter { it.language != null }.map { it.id }.toSet(),
            ids(setOf(french, english))
        )
    }

    @Test
    fun french_and_advanced_returns_only_les_secrets_du_rendu_web() {
        assertEquals(setOf(SessionFixtures.webRendering.id), ids(setOf(french, advanced)))
    }

    @Test
    fun codelab_and_french_returns_nothing() {
        assertEquals(emptySet(), ids(setOf(codelab, french)))
    }

    @Test
    fun bookmark_filter_keeps_only_bookmarked_sessions() {
        val bookmarked = setOf(SessionFixtures.lunch.id, SessionFixtures.closing.id)

        assertEquals(idsOf(SessionFixtures.lunch, SessionFixtures.closing), ids(setOf(bookmarkedOnly), bookmarked))
        assertEquals(
            idsOf(SessionFixtures.welcome, SessionFixtures.lunch, SessionFixtures.closing),
            ids(setOf(bookmarkedOnly), bookmarked + SessionFixtures.welcome.id)
        )
    }

    @Test
    fun bookmarks_do_not_affect_the_result_without_a_bookmark_filter() {
        assertEquals(SessionFixtures.all.map { it.id }.toSet(), ids(emptySet(), setOf(SessionFixtures.lunch.id)))
    }

    @Test
    fun language_filter_never_matches_a_session_without_language() {
        for (language in listOf(french, english)) {
            val result = ids(setOf(language))

            assertTrue(SessionFixtures.lunch.id !in result, "lunch matched ${language.value}")
            assertTrue(SessionFixtures.coffeeBreak.id !in result, "coffee break matched ${language.value}")
        }
    }

    @Test
    fun bookmark_filters_ignore_their_value() {
        val bookmarked = setOf(SessionFixtures.lunch.id)

        for (value in listOf("", "bookmark", "true")) {
            assertEquals(
                idsOf(SessionFixtures.lunch),
                ids(setOf(SessionFilter(SessionFilterType.BOOKMARK, value)), bookmarked),
                "bookmark value '$value'"
            )
        }
    }

    @Test
    fun room_filter_matches_the_room_id_and_not_the_room_name() {
        val session = sessionOf("a", room = Room(id = "room-id", name = "Room name", sortIndex = 0))

        assertEquals(setOf("a"), ids(setOf(SessionFilter(SessionFilterType.ROOM, "room-id")), sessions = listOf(session)))
        assertEquals(
            emptySet(),
            ids(setOf(SessionFilter(SessionFilterType.ROOM, "Room name")), sessions = listOf(session))
        )
    }

    @Test
    fun a_session_with_null_fields_never_matches_the_filter_of_that_type() {
        val bare = sessionOf("bare")
        val filters =
            listOf(
                SessionFilter(SessionFilterType.LANGUAGE, SessionLanguage.FRENCH.name),
                SessionFilter(SessionFilterType.COMPLEXITY, Complexity.BEGINNER.name),
                SessionFilter(SessionFilterType.TYPE, SessionType.CONFERENCE.name),
                SessionFilter(SessionFilterType.ROOM, "Titan"),
                SessionFilter(SessionFilterType.ROOM, "")
            )

        for (filter in filters) {
            assertEquals(emptySet(), ids(setOf(filter), sessions = listOf(bare)), "filter $filter")
        }
    }

    @Test
    fun the_output_keeps_the_input_order() {
        val reversed = SessionFixtures.all.reversed()

        assertEquals(reversed.map { it.id }, useCase.filter(reversed, emptySet(), emptySet()).map { it.id })
        assertEquals(
            listOf(
                SessionFixtures.closing.id,
                SessionFixtures.gitQuickie.id,
                SessionFixtures.webRendering.id,
                SessionFixtures.welcome.id
            ),
            useCase.filter(reversed, setOf(french), emptySet()).map { it.id }
        )
    }

    @Test
    fun no_filter_returns_the_input_list_unchanged() {
        assertSame(SessionFixtures.all, useCase.filter(SessionFixtures.all, emptySet(), emptySet()))
    }

    @Test
    fun empty_sessions_give_an_empty_result() {
        assertEquals(emptyList(), useCase.filter(emptyList(), setOf(french), emptySet()))
        assertEquals(emptyList(), useCase.filter(emptyList(), emptySet(), emptySet()))
    }

    @Test
    fun every_shared_filter_case_matches_the_android_and_ios_goldens() {
        val sessions = AgendaSnapshot.sessions(GoldenFiles.read("agenda/sessions.snapshot.json"))
        val cases = FilterCases.decode(GoldenFiles.read("agenda/filter-cases.json"))
        val android = Goldens.decodeFiltering(GoldenFiles.read("agenda/golden/android-filtering.json"))
        val ios = Goldens.decodeFiltering(GoldenFiles.read("agenda/golden/ios-filtering.json"))
        val bookmarked = cases.bookmarkedSessionIds.toSet()

        assertEquals(cases.cases.map { it.name }, android.cases.map { it.name })
        assertEquals(cases.cases.map { it.name }, ios.cases.map { it.name })
        cases.cases.forEachIndexed { index, case ->
            val filters = case.filters.map { SessionFilter(SessionFilterType.valueOf(it.type), it.value) }.toSet()

            val actual = useCase.filter(sessions, filters, bookmarked).map { it.id }.sorted()

            assertEquals(android.cases[index].sessionIds, actual, "android golden, case ${case.name}")
            assertEquals(ios.cases[index].sessionIds, actual, "ios golden, case ${case.name}")
        }
    }

    private fun sessionOf(id: String, room: Room? = null): Session =
        Session(
            id = id,
            room = room,
            scheduleSlot =
                ScheduleSlot(
                    startDate = "2025-10-16T08:00:00+02:00",
                    endDate = "2025-10-16T09:00:00+02:00"
                )
        )
}
