package com.gdgnantes.devfest.core.testing

import app.cash.turbine.test
import com.gdgnantes.devfest.core.model.ContentLanguage
import com.gdgnantes.devfest.core.model.stubs.PartnerFixtures
import com.gdgnantes.devfest.core.model.stubs.SessionFixtures
import com.gdgnantes.devfest.core.model.stubs.SpeakerFixtures
import com.gdgnantes.devfest.core.model.stubs.buildVenueStub
import com.gdgnantes.devfest.core.model.stubs.roomStubs
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertSame

class FakeDevFestNantesStoreTest {
    @Test
    fun flows_first_emit_the_fixtures() =
        runTest {
            val store = FakeDevFestNantesStore()

            store.sessions.test { assertEquals(SessionFixtures.all, awaitItem()) }
            store.speakers.test { assertEquals(SpeakerFixtures.all, awaitItem()) }
            store.rooms.test { assertEquals(roomStubs.toSet(), awaitItem()) }
            store.partners.test { assertEquals(PartnerFixtures.byCategory, awaitItem()) }
        }

    @Test
    fun agenda_groups_the_fixture_sessions_by_day() =
        runTest {
            val store = FakeDevFestNantesStore()

            store.agenda.test {
                val days = awaitItem().days
                assertEquals(6, days.getValue(1).sessions.size)
                assertEquals(4, days.getValue(2).sessions.size)
            }
        }

    @Test
    fun pushing_sessions_re_emits_on_sessions_and_agenda() =
        runTest {
            val store = FakeDevFestNantesStore()
            val pushed = listOf(SessionFixtures.aiKeynote)

            store.sessions.test {
                assertEquals(SessionFixtures.all, awaitItem())
                store.sessionsState.value = pushed
                assertEquals(pushed, awaitItem())
            }
            store.agenda.test {
                val days = awaitItem().days
                assertEquals(0, days.getValue(1).sessions.size)
                assertEquals(1, days.getValue(2).sessions.size)
            }
        }

    @Test
    fun lookups_return_the_fixture_for_a_known_id_and_null_for_an_unknown_one() =
        runTest {
            val store = FakeDevFestNantesStore()

            assertSame(SessionFixtures.welcome, store.getSession(SessionFixtures.welcome.id))
            assertSame(SpeakerFixtures.alice, store.getSpeaker(SpeakerFixtures.alice.id))
            assertSame(roomStubs.first(), store.getRoom(roomStubs.first().id))
            assertNull(store.getSession("unknown"))
            assertNull(store.getSpeaker("unknown"))
            assertNull(store.getRoom("unknown"))
        }

    @Test
    fun speaker_sessions_are_the_sessions_the_speaker_gives() =
        runTest {
            val store = FakeDevFestNantesStore()

            assertEquals(
                listOf(SessionFixtures.composeDeepDive, SessionFixtures.webRendering),
                store.getSpeakerSessions(SpeakerFixtures.alice.id)
            )
            assertEquals(emptyList(), store.getSpeakerSessions("unknown"))
        }

    @Test
    fun venue_is_the_offline_stub() =
        runTest {
            val store = FakeDevFestNantesStore()

            assertEquals(buildVenueStub(ContentLanguage.FRENCH), store.getVenue(ContentLanguage.FRENCH))
        }

    @Test
    fun failure_makes_flows_and_getters_throw() =
        runTest {
            val store = FakeDevFestNantesStore()
            val boom = IllegalStateException("boom")
            store.failure = boom

            store.sessions.test { assertSame(boom, awaitError()) }
            store.speakers.test { assertSame(boom, awaitError()) }
            store.agenda.test { assertSame(boom, awaitError()) }
            assertSame(boom, assertFailsWith<IllegalStateException> { store.getSession("any") })
            assertSame(boom, assertFailsWith<IllegalStateException> { store.getVenue(ContentLanguage.FRENCH) })
        }
}
