package com.gdgnantes.devfest.store

import com.gdgnantes.devfest.model.*
import com.gdgnantes.devfest.model.stubs.*
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

    private val partnersCache = MutableList(Random.nextInt(10, MAX_PARTNERS)) {
        buildPartnerStub()
    }

    private val venueCache = buildVenueStub()

    override val agenda: Flow<Agenda>
        get() = flow {
            emit(
                Agenda.Builder().run {
                    this.sessions = sessionsCache
                    build()
                })
        }

    override val partners: Flow<List<Partner>>
        get() = flow {
            emit(partnersCache)
        }

    override suspend fun getRoom(id: String): Room? =
        roomStubs.firstOrNull { room -> room.id == id }

    override val rooms: Flow<List<Room>>
        get() = flow {
            emit(roomStubs)
        }

    override suspend fun getSession(id: String): Session =
        sessionsCache.first { session -> session.id == id }

    override val sessions: Flow<List<Session>>
        get() = flow {
            emit(sessionsCache)
        }

    override suspend fun getSpeaker(id: String): Speaker =
        speakersCache.first { speaker -> speaker.id == id }

    override val speakers: Flow<List<Speaker>>
        get() = flow {
            emit(speakersCache)
        }

    override val venue: Flow<Venue>
        get() = flow {
            emit(venueCache)
        }
}