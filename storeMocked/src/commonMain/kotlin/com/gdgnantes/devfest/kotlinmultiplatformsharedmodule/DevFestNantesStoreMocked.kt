package com.gdgnantes.devfest.kotlinmultiplatformsharedmodule

import com.gdgnantes.devfest.kotlinmultiplatformsharedmodule.stubs.*
import com.gdgnantes.devfest.store.DevFestNantesStore
import com.gdgnantes.devfest.store.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlin.random.Random

class DevFestNantesStoreMocked : DevFestNantesStore {

    private var bookmarks = MutableStateFlow<Set<String>>(setOf())

    override fun getVenue(id: String): Flow<Result<Venue>> =
        flow { emit(Result.success(buildVenueStub())) }

    override fun getSpeaker(id: String): Flow<Result<Speaker>> =
        flow { emit(Result.success(buildSpeakerStub())) }

    override fun getRoom(id: String): Flow<Result<Room>> =
        flow { emit(Result.success(buildRoomStub())) }

    override fun getSession(id: String): Flow<Result<Session>> =
        flow { emit(Result.success(buildSessionStub())) }

    override fun getPartners(): Flow<Result<List<Partner>>> =
        flow {
            emit(
                Result.success(
                    MutableList(Random.nextInt(10, MAX_PARTNERS)) {
                        buildPartner()
                    }
                )
            )
        }

    override fun getSessions(): Flow<Result<List<Session>>> =
        flow {
            emit(
                Result.success(
                    MutableList(Random.nextInt(10, MAX_SESSIONS)) {
                        buildSessionStub()
                    }
                )
            )
        }

    override fun getSpeakers(): Flow<Result<List<Speaker>>> =
        flow {
            emit(
                Result.success(
                    MutableList(Random.nextInt(1, MAX_SPEAKERS)) {
                        buildSpeakerStub()
                    }
                )
            )
        }

    override fun getRooms(): Flow<Result<List<Room>>> =
        flow {
            emit(
                Result.success(
                    MutableList(Random.nextInt(1, MAX_ROOMS)) {
                        buildRoomStub()
                    }
                )
            )
        }

    override fun getBookmarks(userId: String): Flow<Result<Set<String>>> =
        bookmarks.map { bookmark -> Result.success(bookmark) }

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
}