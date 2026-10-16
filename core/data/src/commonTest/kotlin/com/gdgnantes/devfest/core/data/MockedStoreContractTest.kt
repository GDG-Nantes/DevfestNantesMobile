package com.gdgnantes.devfest.core.data

import com.gdgnantes.devfest.core.model.ContentLanguage
import com.gdgnantes.devfest.core.model.stubs.PartnerFixtures
import com.gdgnantes.devfest.core.model.stubs.SessionFixtures
import com.gdgnantes.devfest.core.model.stubs.SpeakerFixtures
import com.gdgnantes.devfest.core.model.stubs.buildVenueStub
import com.gdgnantes.devfest.core.model.stubs.roomStubs
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

/** The shared store contract over [DevFestNantesStoreMocked], plus its fixture values. */
class MockedStoreContractTest : DevFestNantesStoreContractTest() {

    override suspend fun createStore(): DevFestNantesStore = DevFestNantesStoreMocked()

    override val knownSessionId: String = SessionFixtures.webRendering.id

    override val knownSpeakerId: String = SpeakerFixtures.alice.id

    override val knownRoomId: String = "titan"

    @Test
    fun agenda_flow_has_the_fixture_day_sizes() = runTest {
        val agenda = createStore().agenda.first()

        assertEquals(6, agenda.days.getValue(1).sessions.size)
        assertEquals(4, agenda.days.getValue(2).sessions.size)
    }

    @Test
    fun partners_flow_emits_the_fixture_partners() = runTest {
        assertEquals(PartnerFixtures.byCategory, createStore().partners.first())
    }

    @Test
    fun rooms_flow_emits_the_room_stubs() = runTest {
        val store = createStore()

        assertEquals(roomStubs.toSet(), store.rooms.first())
        assertEquals("Titan", store.getRoom("titan")?.name)
    }

    @Test
    fun sessions_flow_emits_the_fixture_sessions() = runTest {
        val store = createStore()

        assertEquals(SessionFixtures.all, store.sessions.first())
        assertEquals(SessionFixtures.webRendering, store.getSession(SessionFixtures.webRendering.id))
    }

    @Test
    fun speakers_flow_emits_the_fixture_speakers() = runTest {
        val store = createStore()

        assertEquals(SpeakerFixtures.all, store.speakers.first())
        assertEquals(SpeakerFixtures.alice, store.getSpeaker(SpeakerFixtures.alice.id))
    }

    @Test
    fun getSpeakerSessions_returns_the_speakers_fixture_sessions() = runTest {
        assertEquals(
            listOf(SessionFixtures.composeDeepDive, SessionFixtures.webRendering),
            createStore().getSpeakerSessions(SpeakerFixtures.alice.id)
        )
    }

    @Test
    fun getVenue_returns_the_offline_venue_stub() = runTest {
        val store = createStore()

        assertEquals(buildVenueStub(ContentLanguage.ENGLISH), store.getVenue(ContentLanguage.ENGLISH))
        assertEquals(buildVenueStub(ContentLanguage.FRENCH), store.getVenue(ContentLanguage.FRENCH))
    }

    @Test
    fun mocked_ids_equal_the_fixture_ids() = runTest {
        val store = createStore()

        assertEquals(SessionFixtures.all.map { it.id }, store.sessions.first().map { it.id })
        assertEquals(SpeakerFixtures.all.map { it.id }, store.speakers.first().map { it.id })
    }

    @Test
    fun two_mocked_stores_return_equal_data() = runTest {
        val first = createStore()
        val second = createStore()

        assertEquals(first.sessions.first(), second.sessions.first())
        assertEquals(first.speakers.first(), second.speakers.first())
    }
}
