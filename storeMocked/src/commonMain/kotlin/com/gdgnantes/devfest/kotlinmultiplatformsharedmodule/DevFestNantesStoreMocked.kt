package com.gdgnantes.devfest.kotlinmultiplatformsharedmodule

import com.gdgnantes.devfest.kotlinmultiplatformsharedmodule.stubs.*
import com.gdgnantes.devfest.store.DevFestNantesStore
import com.gdgnantes.devfest.store.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

class DevFestNantesStoreMocked : DevFestNantesStore {
    private var bookmarks = MutableStateFlow<Set<String>>(setOf())

    override fun getBookmarks(userId: String): Flow<Set<String>> = bookmarks.asStateFlow()

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

    override fun getRoom(id: String): Flow<Room> = flow { emit(buildRoomStub()) }

    override fun getRooms(): Flow<List<Room>> = flow {
        emit(
            MutableList(Random.nextInt(1, MAX_ROOMS)) {
                buildRoomStub()
            }
        )
    }

    override fun getSession(id: String): Flow<Session> =
        flow { emit(buildSessionStub()) }

    override fun getSessions(): Flow<List<Session>> = flow {
        emit(
            MutableList(Random.nextInt(10, MAX_SESSIONS)) {
                buildSessionStub()
            }
        )
    }

    override fun getSpeaker(id: String): Flow<Speaker> = flow { emit(buildSpeakerStub()) }

    override fun getSpeakers(): Flow<List<Speaker>> = flow {
        emit(

            MutableList(Random.nextInt(1, MAX_SPEAKERS)) {
                buildSpeakerStub()
            }
        )
    }

    override fun getVenue(id: String): Flow<Venue> = flow { emit(buildVenueStub()) }
}