package com.gdgnantes.devfest.core.data

import com.gdgnantes.devfest.core.model.ContentLanguage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Structural invariants every [DevFestNantesStore] implementation honors (D-16). The mocked store
 * and the GraphQL store (over a mock server serving recorded responses) both run this contract
 * on JVM, Android host and iOS; value assertions live in the subclasses.
 *
 * Each test calls [createStore] on its own, so nothing is shared between tests.
 */
abstract class DevFestNantesStoreContractTest {

    protected abstract suspend fun createStore(): DevFestNantesStore

    /** Id of a session that exists in this implementation's data. */
    protected abstract val knownSessionId: String

    /** Id of a speaker that exists, and has at least one session, in this implementation's data. */
    protected abstract val knownSpeakerId: String

    /** Id of a room that exists in this implementation's data. */
    protected abstract val knownRoomId: String

    /** Closes whatever [createStore] opened. Called after every test. */
    protected open fun releaseStore() {}

    @AfterTest
    fun tearDownStore() {
        releaseStore()
    }

    @Test
    fun agenda_contains_both_conference_days() = runTest {
        val store = createStore()

        assertEquals(setOf(1, 2), store.agenda.first().days.keys)
    }

    @Test
    fun agenda_sessions_are_all_in_the_sessions_flow() = runTest {
        val store = createStore()

        val sessions = store.sessions.first()
        val agendaSessions = store.agenda.first().days.values.flatMap { it.sessions }

        assertTrue(agendaSessions.isNotEmpty())
        assertTrue(sessions.containsAll(agendaSessions))
    }

    @Test
    fun session_and_speaker_ids_are_unique() = runTest {
        val store = createStore()

        val sessionIds = store.sessions.first().map { it.id }
        val speakerIds = store.speakers.first().map { it.id }

        assertEquals(sessionIds.size, sessionIds.toSet().size)
        assertEquals(speakerIds.size, speakerIds.toSet().size)
    }

    @Test
    fun rooms_iterate_in_sortIndex_order() = runTest {
        val store = createStore()

        val sortIndexes = store.rooms.first().map { it.sortIndex }

        assertTrue(sortIndexes.isNotEmpty())
        assertEquals(sortIndexes.sorted(), sortIndexes)
    }

    @Test
    fun getSession_of_a_known_id_equals_the_flow_element() = runTest {
        val store = createStore()

        val fromFlow = store.sessions.first().single { it.id == knownSessionId }

        assertEquals(fromFlow, store.getSession(knownSessionId))
    }

    @Test
    fun getSession_of_an_unknown_id_is_null() = runTest {
        val store = createStore()

        assertNull(store.getSession("unknown-session-id"))
    }

    @Test
    fun getSpeaker_known_and_unknown() = runTest {
        val store = createStore()

        val fromFlow = store.speakers.first().single { it.id == knownSpeakerId }

        assertEquals(fromFlow, store.getSpeaker(knownSpeakerId))
        assertNull(store.getSpeaker("unknown-speaker-id"))
    }

    @Test
    fun getRoom_known_and_unknown() = runTest {
        val store = createStore()

        val fromFlow = store.rooms.first().single { it.id == knownRoomId }

        assertEquals(fromFlow, store.getRoom(knownRoomId))
        assertNull(store.getRoom("unknown-room-id"))
    }

    @Test
    fun getSpeakerSessions_of_a_known_speaker_equals_the_filtered_sessions() = runTest {
        val store = createStore()

        val expected = store.sessions.first().filter { session ->
            session.speakers.any { it.id == knownSpeakerId }
        }
        val actual = store.getSpeakerSessions(knownSpeakerId)

        assertTrue(expected.isNotEmpty())
        assertEquals(expected, actual)
    }

    @Test
    fun getSpeakerSessions_of_an_unknown_speaker_is_empty() = runTest {
        val store = createStore()

        assertTrue(store.getSpeakerSessions("unknown-speaker-id").isEmpty())
    }

    @Test
    fun venue_is_non_blank_in_english_and_french() = runTest {
        val store = createStore()

        for (language in listOf(ContentLanguage.ENGLISH, ContentLanguage.FRENCH)) {
            val venue = store.getVenue(language)
            assertTrue(venue.name.isNotBlank(), "venue name is blank in $language")
            assertTrue(venue.description.isNotBlank(), "venue description is blank in $language")
        }
    }
}
