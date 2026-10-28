package com.gdgnantes.devfest.core.data.snapshot

import com.apollographql.apollo.ApolloClient
import com.apollographql.mockserver.MockServer
import com.apollographql.mockserver.enqueueString
import com.gdgnantes.devfest.core.data.graphql.GraphQLStore
import com.gdgnantes.devfest.core.model.Room
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.Speaker
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.time.OffsetDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

/**
 * Offline replay of the raw responses recorded by `AgendaSnapshotRecorder` (07-03) through the
 * real [GraphQLStore] over an Apollo `MockServer` (D-09). Pins the snapshot facts, and guards the
 * neutral model snapshot under `core/domain/testdata/agenda/` against drift: the default run
 * compares and fails, only `-PrecordAgendaSnapshot` (scripts/record-agenda-snapshot.sh) rewrites.
 *
 * Nothing here reaches the network: every query is served by a local `MockServer`.
 */
class AgendaSnapshotTest {

    private val graphqlDir = File(System.getProperty("devfest.dataTestdata"), "graphql")
    private val agendaDir = File(System.getProperty("devfest.domainTestdata"), "agenda")
    private val recordMode = System.getProperty("devfest.recordAgendaSnapshot") == "true"

    private suspend fun <T> replay(rawFileName: String, body: suspend (GraphQLStore) -> T): T {
        val server = MockServer()
        try {
            server.enqueueString(File(graphqlDir, rawFileName).readText(), contentType = JSON)
            val client = ApolloClient.Builder().serverUrl(server.url()).build()
            try {
                return body(GraphQLStore(client))
            } finally {
                client.close()
            }
        } finally {
            server.close()
        }
    }

    private suspend fun recordedSessions(): List<Session> =
        replay("GetSessions.json") { it.sessions.first() }

    private suspend fun recordedSpeakers(): List<Speaker> =
        replay("GetSpeakers.json") { it.speakers.first() }

    private suspend fun recordedRooms(): List<Room> =
        replay("GetRooms.json") { it.rooms.first().toList() }

    @Test
    fun recorded_snapshot_has_the_expected_shape() = runTest {
        val sessions = recordedSessions()
        val rooms = recordedRooms()
        val speakers = recordedSpeakers()

        assertEquals(94, sessions.size)
        assertEquals(17, sessions.count { it.room == null })
        assertEquals(8, rooms.size)
        assertEquals(8, sessions.mapNotNull { it.room?.id }.distinct().size)
        // D-17: the pinned speaker set is what the app loads today (GetSpeakers), not the server total.
        assertEquals(100, speakers.size)
    }

    @Test
    fun every_session_room_id_equals_its_name() = runTest {
        val sessions = recordedSessions()
        val rooms = recordedRooms()

        sessions.mapNotNull { it.room }.forEach { assertEquals(it.name, it.id) }
        rooms.forEach { assertEquals(it.name, it.id) }
    }

    @Test
    fun every_start_is_a_whole_minute() = runTest {
        recordedSessions().forEach { session ->
            listOf(session.scheduleSlot.startDate, session.scheduleSlot.endDate).forEach { date ->
                val parsed = OffsetDateTime.parse(date)
                assertEquals(0, parsed.second, "${session.id}: $date is not a whole minute")
                assertEquals(0, parsed.nano, "${session.id}: $date is not a whole minute")
            }
        }
    }

    @Test
    fun speakers_query_returns_fewer_speakers_than_the_server_total() = runTest {
        val meta = Json.parseToJsonElement(File(graphqlDir, "snapshot-meta.json").readText()).jsonObject
        val speakersPageTotal = meta.getValue("speakersPageTotal").jsonPrimitive.int

        // D-17: the 100 speakers of GetSpeakers are the pinned set; the server total (110) is only
        // metadata. This gap is FUT-02 debt documented here, not fixed.
        assertTrue(recordedSpeakers().size < speakersPageTotal)
    }

    @Test
    fun neutral_snapshot_matches_the_raw_responses() = runTest {
        val generated = mapOf(
            "sessions.snapshot.json" to AgendaSnapshotJson.sessions(recordedSessions()),
            "speakers.snapshot.json" to AgendaSnapshotJson.speakers(recordedSpeakers()),
            "rooms.snapshot.json" to AgendaSnapshotJson.rooms(recordedRooms()),
        )

        if (recordMode) {
            agendaDir.mkdirs()
            generated.forEach { (name, content) -> File(agendaDir, name).writeText(content) }
            return@runTest
        }

        generated.forEach { (name, content) ->
            val committed = File(agendaDir, name)
            if (!committed.exists() || committed.readText() != content) {
                fail("neutral snapshot drifted: run scripts/record-agenda-snapshot.sh ($name)")
            }
        }
    }

    private companion object {
        const val JSON = "application/json"
    }
}
