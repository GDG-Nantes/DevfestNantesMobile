package com.gdgnantes.devfest.store

import com.gdgnantes.devfest.model.Agenda
import com.gdgnantes.devfest.model.ContentLanguage
import com.gdgnantes.devfest.model.Partner
import com.gdgnantes.devfest.model.PartnerCategory
import com.gdgnantes.devfest.model.Room
import com.gdgnantes.devfest.model.Session
import com.gdgnantes.devfest.model.Speaker
import com.gdgnantes.devfest.model.Venue
import com.gdgnantes.devfest.model.stubs.MAX_PARTNERS
import com.gdgnantes.devfest.model.stubs.MAX_SESSIONS
import com.gdgnantes.devfest.model.stubs.buildPartnerStub
import com.gdgnantes.devfest.model.stubs.buildSessionStub
import com.gdgnantes.devfest.model.stubs.buildVenueStub
import com.gdgnantes.devfest.model.stubs.roomStubs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

internal class DevFestNantesStoreMocked : DevFestNantesStore {

    private val sessionsCache = MutableList(Random.nextInt(10, MAX_SESSIONS)) {
        buildSessionStub()
    }

    private val speakersCache by lazy {
        val speakersList = mutableListOf<Speaker>()
        sessionsCache.forEach { session ->
            speakersList.addAll(session.speakers)
        }
        speakersList
    }

    private val partnersCache: Map<PartnerCategory, List<Partner>> = mapOf(
        PartnerCategory.PLATINIUM to MutableList(Random.nextInt(2, MAX_PARTNERS))
        {
            buildPartnerStub()
        },
        PartnerCategory.GOLD to MutableList(Random.nextInt(2, MAX_PARTNERS))
        {
            buildPartnerStub()
        },
        PartnerCategory.VIRTUAL to MutableList(Random.nextInt(2, MAX_PARTNERS))
        {
            buildPartnerStub()
        }
    )

    override val agenda: Flow<Agenda>
        get() = flow {
            emit(
                Agenda.Builder().run {
                    this.sessions = sessionsCache
                    build()
                })
        }

    override val partners: Flow<Map<PartnerCategory, List<Partner>>>
        get() = flow {
            emit(partnersCache)
        }

    override suspend fun getRoom(id: String): Room? =
        roomStubs.firstOrNull { room -> room.id == id }

    override val rooms: Flow<Set<Room>>
        get() = flow {
            emit(roomStubs.toSet())
        }

    override suspend fun getSession(id: String): Session =
        sessionsCache.first { session -> session.id == id }

    override val sessions: Flow<List<Session>>
        get() = flow {
            emit(sessionsCache)
        }

    override suspend fun getSpeaker(id: String): Speaker =
        speakersCache.first { speaker -> speaker.id == id }

    override suspend fun getSpeakerSessions(speakerId: String): List<Session> = emptyList()

    override val speakers: Flow<List<Speaker>>
        get() = flow {
            emit(speakersCache)
        }

    override suspend fun getVenue(language: ContentLanguage): Venue {
        return buildVenueStub(language)
    }
}