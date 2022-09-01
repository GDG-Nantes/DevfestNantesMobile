package com.gdgnantes.devfest.store

import com.gdgnantes.devfest.model.*
import com.gdgnantes.devfest.model.stubs.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.random.Random

class DevFestNantesStoreMocked : DevFestNantesStore {

    override val agenda: Flow<Agenda>
        get() = sessions.map { sessions ->
            Agenda.Builder().run {
                this.sessions = sessions
                build()
            }
        }

    override val partners: Flow<List<Partner>>
        get() = flow {
            emit(
                MutableList(Random.nextInt(10, MAX_PARTNERS)) {
                    buildPartner()
                }
            )
        }

    override suspend fun getRoom(id: String): Room? =
        roomStubs.firstOrNull { room -> room.id == id }

    override val rooms: Flow<List<Room>>
        get() = flow {
            emit(roomStubs)
        }

    override suspend fun getSession(id: String): Session =
        buildSessionStub().copy(id = id)

    override val sessions: Flow<List<Session>>
        get() = flow {
            emit(
                MutableList(Random.nextInt(10, MAX_SESSIONS)) {
                    buildSessionStub()
                }
            )
        }

    override suspend fun getSpeaker(id: String): Speaker = buildSpeakerStub().copy(id = id)

    override val speakers: Flow<List<Speaker>>
        get() = flow {
            emit(
                MutableList(Random.nextInt(1, MAX_SPEAKERS)) {
                    buildSpeakerStub()
                }
            )
        }

    override val venue: Flow<Venue>
        get() = flow {
            emit(buildVenueStub())
        }
}