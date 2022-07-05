package com.gdgnantes.devfest.store

import com.gdgnantes.devfest.model.*
import com.gdgnantes.devfest.model.stubs.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

class DevFestNantesStoreMocked : DevFestNantesStore {
    private var bookmarks = MutableStateFlow<Set<String>>(setOf())

    override suspend fun getBookmarks(userId: String): Set<String> = bookmarks.value

    override suspend fun setBookmark(userId: String, sessionId: String, value: Boolean) {
        val currentBookmarks = bookmarks.value.toMutableSet()
        if (value && !currentBookmarks.contains(sessionId)) {
            bookmarks.emit(
                with(currentBookmarks) {
                    add(sessionId)
                    toSet()
                }
            )
        } else if (!value && currentBookmarks.contains(sessionId)) {
            bookmarks.emit(
                with(currentBookmarks) {
                    remove(sessionId)
                    toSet()
                }
            )
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

    override suspend fun getRoom(id: String): Room = buildRoomStub().copy(id = id)

    override val rooms: Flow<List<Room>>
        get() = flow {
            emit(
                MutableList(Random.nextInt(1, MAX_ROOMS)) {
                    buildRoomStub()
                }
            )
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
        get() = flow { buildVenueStub() }
}