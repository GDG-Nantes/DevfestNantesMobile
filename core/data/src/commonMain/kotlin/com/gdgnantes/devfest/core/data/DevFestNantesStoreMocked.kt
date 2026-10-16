package com.gdgnantes.devfest.core.data

import com.gdgnantes.devfest.core.model.Agenda
import com.gdgnantes.devfest.core.model.ContentLanguage
import com.gdgnantes.devfest.core.model.Partner
import com.gdgnantes.devfest.core.model.PartnerCategory
import com.gdgnantes.devfest.core.model.Room
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.Speaker
import com.gdgnantes.devfest.core.model.Venue
import com.gdgnantes.devfest.core.model.stubs.PartnerFixtures
import com.gdgnantes.devfest.core.model.stubs.SessionFixtures
import com.gdgnantes.devfest.core.model.stubs.SpeakerFixtures
import com.gdgnantes.devfest.core.model.stubs.buildVenueStub
import com.gdgnantes.devfest.core.model.stubs.roomStubs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class DevFestNantesStoreMocked : DevFestNantesStore {
    private val sessionsCache: List<Session> = SessionFixtures.all

    private val speakersCache: List<Speaker> = SpeakerFixtures.all

    private val partnersCache: Map<PartnerCategory, List<Partner>> = PartnerFixtures.byCategory

    override val agenda: Flow<Agenda>
        get() =
            flow {
                emit(
                    Agenda.Builder().run {
                        this.sessions = sessionsCache
                        build()
                    }
                )
            }

    override val partners: Flow<Map<PartnerCategory, List<Partner>>>
        get() =
            flow {
                emit(partnersCache)
            }

    override suspend fun getRoom(id: String): Room? =
        roomStubs.firstOrNull { room -> room.id == id }

    override val rooms: Flow<Set<Room>>
        get() =
            flow {
                emit(roomStubs.toSet())
            }

    override suspend fun getSession(id: String): Session? =
        sessionsCache.firstOrNull { session -> session.id == id }

    override val sessions: Flow<List<Session>>
        get() =
            flow {
                emit(sessionsCache)
            }

    override suspend fun getSpeaker(id: String): Speaker? =
        speakersCache.firstOrNull { speaker -> speaker.id == id }

    override suspend fun getSpeakerSessions(speakerId: String): List<Session> =
        sessionsCache.filter { session -> session.speakers.any { it.id == speakerId } }

    override val speakers: Flow<List<Speaker>>
        get() =
            flow {
                emit(speakersCache)
            }

    override suspend fun getVenue(language: ContentLanguage): Venue {
        return buildVenueStub(language)
    }
}
