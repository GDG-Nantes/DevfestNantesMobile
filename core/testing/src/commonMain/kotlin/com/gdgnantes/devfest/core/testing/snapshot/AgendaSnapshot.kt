package com.gdgnantes.devfest.core.testing.snapshot

import com.gdgnantes.devfest.core.model.Complexity
import com.gdgnantes.devfest.core.model.Room
import com.gdgnantes.devfest.core.model.ScheduleSlot
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.SessionLanguage
import com.gdgnantes.devfest.core.model.SessionType
import com.gdgnantes.devfest.core.model.Speaker
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

/**
 * Decoder of the neutral agenda snapshot committed under `core/domain/testdata/agenda/` (D-09).
 *
 * The files are written by `AgendaSnapshotTest` in `:core:data` (which turns the raw GraphQL
 * responses into models) and documented in the README next to them. Reading the files stays in
 * each test module; this decoder takes the JSON text so it works on JVM and iOS simulator alike.
 *
 * Sessions carry speakers by id only (`Speaker(id = ...)`), and no category, abstract or
 * feedback id: those are not part of the snapshot.
 */
object AgendaSnapshot {
    private val json = Json { ignoreUnknownKeys = false }

    fun sessions(json: String): List<Session> =
        this.json.decodeFromString(ListSerializer(SessionDto.serializer()), json).map { it.toModel() }

    fun speakers(json: String): List<Speaker> =
        this.json.decodeFromString(ListSerializer(SpeakerDto.serializer()), json).map { it.toModel() }

    fun rooms(json: String): List<Room> =
        this.json.decodeFromString(ListSerializer(RoomDto.serializer()), json).map { it.toModel() }

    @Serializable
    private class RoomDto(val id: String, val name: String, val sortIndex: Int) {
        fun toModel() = Room(id = id, name = name, sortIndex = sortIndex)
    }

    @Serializable
    private class SpeakerDto(val id: String, val name: String) {
        fun toModel() = Speaker(id = id, name = name)
    }

    @Serializable
    private class SessionDto(
        val id: String,
        val title: String,
        val type: String? = null,
        val language: String? = null,
        val complexity: String? = null,
        val room: RoomDto? = null,
        val startDate: String,
        val endDate: String,
        val speakerIds: List<String>,
    ) {
        fun toModel() =
            Session(
                id = id,
                title = title,
                type = type?.let { SessionType.valueOf(it) },
                language = language?.let { SessionLanguage.valueOf(it) },
                complexity = complexity?.let { Complexity.valueOf(it) },
                room = room?.toModel(),
                scheduleSlot = ScheduleSlot(endDate = endDate, startDate = startDate),
                speakers = speakerIds.map { Speaker(id = it) },
            )
    }
}
