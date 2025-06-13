package com.gdgnantes.devfest.store

import com.gdgnantes.devfest.model.ContentLanguage
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertNotNull

class DevFestNantesStoreContractTest {
    private val store: DevFestNantesStore = DevFestNantesStoreMocked()

    @Test
    fun agenda_flow_emits_agenda() = runTest {
        val agenda = store.agenda.first()
        assertNotNull(agenda)
    }

    @Test
    fun partners_flow_emits_partners() = runTest {
        val partners = store.partners.first()
        assertNotNull(partners)
    }

    @Test
    fun getRoom_returns_room_or_null() = runTest {
        val room = store.getRoom("someId")
        // Accept null or Room, just check no crash
    }

    @Test
    fun rooms_flow_emits_rooms() = runTest {
        val rooms = store.rooms.first()
        assertNotNull(rooms)
    }

    @Test
    fun getSession_returns_session_or_null() = runTest {
        val session = store.getSession("someId")
        // Accept null or Session, just check no crash
    }

    @Test
    fun sessions_flow_emits_sessions() = runTest {
        val sessions = store.sessions.first()
        assertNotNull(sessions)
    }

    @Test
    fun getSpeaker_returns_speaker_or_null() = runTest {
        store.getSpeaker("someId")
        // Accept null or Speaker, just check no crash
    }

    @Test
    fun getSpeakerSessions_returns_sessions() = runTest {
        val sessions = store.getSpeakerSessions("someId")
        assertNotNull(sessions)
    }

    @Test
    fun speakers_flow_emits_speakers() = runTest {
        val speakers = store.speakers.first()
        assertNotNull(speakers)
    }

    @Test
    fun getVenue_returns_venue() = runTest {
        val venue = store.getVenue(ContentLanguage.ENGLISH)
        assertNotNull(venue)
    }
}
