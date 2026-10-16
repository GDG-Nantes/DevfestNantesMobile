package com.gdgnantes.devfest.core.testing

import com.gdgnantes.devfest.core.data.DevFestNantesStore
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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

/**
 * Controllable [DevFestNantesStore] for tests. It is seeded with the shared fixtures, so a test
 * that does nothing gets the same data as the mocked store.
 *
 * Tests push new data by assigning [sessionsState], [speakersState], [roomsState] or
 * [partnersState]; every collector sees the new value. [agenda] is derived from the sessions.
 *
 * Unlike the mocked store, these flows never complete (they are StateFlow-backed), so collect
 * them with Turbine or cancel the collecting scope.
 *
 * Set [failure] to make the store fail. It is checked when collection of a flow starts and on
 * every getter call; a flow that is already running is not interrupted.
 */
class FakeDevFestNantesStore(
    sessions: List<Session> = SessionFixtures.all,
    speakers: List<Speaker> = SpeakerFixtures.all,
    rooms: Set<Room> = roomStubs.toSet(),
    partners: Map<PartnerCategory, List<Partner>> = PartnerFixtures.byCategory
) : DevFestNantesStore {
    val sessionsState = MutableStateFlow(sessions)
    val speakersState = MutableStateFlow(speakers)
    val roomsState = MutableStateFlow(rooms)
    val partnersState = MutableStateFlow(partners)

    /** When set, collecting any flow fails with it and every getter throws it. */
    var failure: Throwable? = null

    override val agenda: Flow<Agenda> =
        failing(sessionsState).map { current ->
            Agenda.Builder().apply { this.sessions = current }.build()
        }

    override val partners: Flow<Map<PartnerCategory, List<Partner>>> = failing(partnersState)

    override val rooms: Flow<Set<Room>> = failing(roomsState)

    override val sessions: Flow<List<Session>> = failing(sessionsState)

    override val speakers: Flow<List<Speaker>> = failing(speakersState)

    override suspend fun getRoom(id: String): Room? = checked { roomsState.value.firstOrNull { it.id == id } }

    override suspend fun getSession(id: String): Session? = checked { sessionsState.value.firstOrNull { it.id == id } }

    override suspend fun getSpeaker(id: String): Speaker? = checked { speakersState.value.firstOrNull { it.id == id } }

    override suspend fun getSpeakerSessions(speakerId: String): List<Session> =
        checked { sessionsState.value.filter { session -> session.speakers.any { it.id == speakerId } } }

    override suspend fun getVenue(language: ContentLanguage): Venue = checked { buildVenueStub(language) }

    private fun <T> failing(state: Flow<T>): Flow<T> =
        flow {
            failure?.let { throw it }
            emitAll(state)
        }

    private inline fun <T> checked(block: () -> T): T {
        failure?.let { throw it }
        return block()
    }
}
