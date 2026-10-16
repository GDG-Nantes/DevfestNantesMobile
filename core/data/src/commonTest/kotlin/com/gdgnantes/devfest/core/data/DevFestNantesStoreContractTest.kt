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
import kotlin.test.assertNull
import kotlin.test.assertTrue

class DevFestNantesStoreContractTest {
    private val store: DevFestNantesStore = DevFestNantesStoreMocked()

    @Test
    fun agenda_flow_emits_agenda() = runTest {
        val agenda = store.agenda.first()
        assertEquals(6, agenda.days.getValue(1).sessions.size)
        assertEquals(4, agenda.days.getValue(2).sessions.size)
    }

    @Test
    fun partners_flow_emits_partners() = runTest {
        assertEquals(PartnerFixtures.byCategory, store.partners.first())
    }

    @Test
    fun getRoom_returns_room_or_null() = runTest {
        assertEquals("Titan", store.getRoom("titan")?.name)
        assertNull(store.getRoom("unknown"))
    }

    @Test
    fun rooms_flow_emits_rooms() = runTest {
        assertEquals(roomStubs.toSet(), store.rooms.first())
    }

    @Test
    fun getSession_returns_session_or_null() = runTest {
        assertEquals(
            SessionFixtures.webRendering,
            store.getSession(SessionFixtures.webRendering.id)
        )
        assertNull(store.getSession("unknown"))
    }

    @Test
    fun sessions_flow_emits_sessions() = runTest {
        assertEquals(SessionFixtures.all, store.sessions.first())
    }

    @Test
    fun getSpeaker_returns_speaker_or_null() = runTest {
        assertEquals(SpeakerFixtures.alice, store.getSpeaker(SpeakerFixtures.alice.id))
        assertNull(store.getSpeaker("unknown"))
    }

    @Test
    fun getSpeakerSessions_returns_the_speakers_sessions() = runTest {
        assertEquals(
            listOf(SessionFixtures.composeDeepDive, SessionFixtures.webRendering),
            store.getSpeakerSessions(SpeakerFixtures.alice.id)
        )
        assertTrue(store.getSpeakerSessions("unknown").isEmpty())
    }

    @Test
    fun speakers_flow_emits_speakers() = runTest {
        assertEquals(SpeakerFixtures.all, store.speakers.first())
    }

    @Test
    fun getVenue_returns_venue() = runTest {
        assertEquals(
            buildVenueStub(ContentLanguage.ENGLISH),
            store.getVenue(ContentLanguage.ENGLISH)
        )
    }

    @Test
    fun two_mocked_stores_return_equal_data() = runTest {
        val other: DevFestNantesStore = DevFestNantesStoreMocked()
        assertEquals(store.sessions.first(), other.sessions.first())
        assertEquals(store.speakers.first(), other.speakers.first())
    }
}
