package com.gdgnantes.devfest.kotlinmultiplatformsharedmodule

import com.gdgnantes.devfest.kotlinmultiplatformsharedmodule.stubs.*
import com.gdgnantes.devfest.store.DevFestNantesStore
import com.gdgnantes.devfest.store.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.random.Random

class DevFestNantesStoreMocked : DevFestNantesStore {
    override fun getVenue(id: String): Flow<Result<Venue>> {
        TODO("Not yet implemented")
    }

    override fun getSpeaker(id: String): Flow<Result<Speaker>> =
        flow { emit(Result.success(createSpeakerStub())) }

    override fun getRoom(id: String): Flow<Result<Room>> =
        flow { emit(Result.success(createRoomStub())) }

    override fun getSession(id: String): Flow<Result<Session>> =
        flow { emit(Result.success(createSessionStub())) }

    override fun getPartners(): Flow<Result<List<Partner>>> {
        TODO("Not yet implemented")
    }

    override fun getSessions(): Flow<Result<List<Session>>> =
        flow {
            emit(
                Result.success(
                    MutableList(Random.nextInt(1, MAX_SESSIONS)) {
                        createSessionStub()
                    }
                )
            )
        }

    override fun getSpeakers(): Flow<Result<List<Speaker>>> =
        flow {
            emit(
                Result.success(
                    MutableList(Random.nextInt(1, MAX_SPEAKERS)) {
                        createSpeakerStub()
                    }
                )
            )
        }

    override fun getRooms(): Flow<Result<List<Room>>> =
        flow {
            emit(
                Result.success(
                    MutableList(Random.nextInt(1, MAX_ROOMS)) {
                        createRoomStub()
                    }
                )
            )
        }

    override fun getBookmarks(userId: String): Flow<Result<Set<String>>> {
        TODO("Not yet implemented")
    }

    override suspend fun setBookmark(userId: String, sessionId: String, value: Boolean) {
        TODO("Not yet implemented")
    }
}