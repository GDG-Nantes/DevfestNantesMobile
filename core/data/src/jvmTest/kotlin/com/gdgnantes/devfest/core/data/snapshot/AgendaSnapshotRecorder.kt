package com.gdgnantes.devfest.core.data.snapshot

import com.gdgnantes.devfest.core.network.graphql.GetRoomsQuery
import com.gdgnantes.devfest.core.network.graphql.GetSessionsQuery
import com.gdgnantes.devfest.core.network.graphql.GetSpeakersQuery
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.File
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Records the raw GraphQL responses the agenda characterization tests replay offline (D-09).
 *
 * Gated: it is excluded from `:core:data:jvmTest` unless `-PrecordAgendaSnapshot` is set (same
 * style as the `-PliveNetworkTests` canary), so a default test run never touches the network. Run
 * it through the documented script:
 *
 *     bash scripts/record-agenda-snapshot.sh
 *
 * Only the app's own three agenda queries (the exact `OPERATION_DOCUMENT` the app sends) and one
 * `speakersPage` id count are posted, with the public `conference` header. Nothing else is
 * recorded (T-07-03: public conference data only, no credential exists).
 */
class AgendaSnapshotRecorder {
    private val client = HttpClient.newHttpClient()

    private val prettyJson = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    }

    @Test
    fun records_the_live_agenda_responses() {
        val graphqlDir = File(System.getProperty("devfest.dataTestdata"), "graphql")
        graphqlDir.mkdirs()

        val operations = listOf(
            RecordedOperation(GetSessionsQuery.OPERATION_NAME, GetSessionsQuery.OPERATION_DOCUMENT),
            RecordedOperation(GetSpeakersQuery.OPERATION_NAME, GetSpeakersQuery.OPERATION_DOCUMENT),
            RecordedOperation(GetRoomsQuery.OPERATION_NAME, GetRoomsQuery.OPERATION_DOCUMENT),
        )
        for (operation in operations) {
            val response = post(operation.name, operation.document)
            File(graphqlDir, "${operation.name}.json").writeText(prettyJson.encodeToString(JsonElement.serializer(), response) + "\n")
        }

        // The server total of speakers (110 expected) is metadata only: the characterization pins
        // the speakers the app's own query loads (D-17, the gap is FUT-02 debt).
        val speakersPage = post("SpeakersPageTotal", SPEAKERS_PAGE_DOCUMENT)
        val total = speakersPage.jsonObject.getValue("data").jsonObject
            .getValue("speakersPage").jsonObject
            .getValue("nodes").jsonArray.size
        assertTrue(total > 0, "speakersPage returned no node")
        val meta = buildJsonObject {
            put("conference", CONFERENCE)
            put("recordedOn", LocalDate.now().toString())
            put("speakersPageTotal", total)
        }
        File(graphqlDir, "snapshot-meta.json").writeText(prettyJson.encodeToString(JsonElement.serializer(), meta) + "\n")

        val recordedMeta = Json.parseToJsonElement(File(graphqlDir, "snapshot-meta.json").readText()).jsonObject
        assertEquals(total, recordedMeta.getValue("speakersPageTotal").jsonPrimitive.int)
    }

    private fun post(operationName: String, document: String): JsonElement {
        val body = buildJsonObject {
            put("query", document)
            put("operationName", operationName)
            put("variables", JsonObject(emptyMap()))
        }
        val request = HttpRequest.newBuilder(URI.create(SERVER_URL))
            .header("Content-Type", "application/json")
            .header("conference", CONFERENCE)
            .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
            .build()
        val response = client.send(request, HttpResponse.BodyHandlers.ofString())
        assertEquals(200, response.statusCode(), "$operationName: unexpected HTTP status")
        val parsed = Json.parseToJsonElement(response.body())
        assertFalse(parsed.jsonObject.containsKey("errors"), "$operationName: the response carries GraphQL errors")
        return parsed
    }

    private data class RecordedOperation(val name: String, val document: String)

    private companion object {
        const val SERVER_URL = "https://confetti-app.dev/graphql"
        const val CONFERENCE = "devfestnantes2025"
        const val SPEAKERS_PAGE_DOCUMENT = "query SpeakersPageTotal { speakersPage(first: 200) { nodes { id } } }"
    }
}
