package com.gdgnantes.devfest.core.data.snapshot

import com.gdgnantes.devfest.core.model.Room
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.Speaker
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray

/**
 * Writer of the neutral agenda snapshot (D-09). The reader lives in `:core:testing`
 * (`AgendaSnapshot`); the committed files under `core/domain/testdata/agenda/` and the README next
 * to them are the contract between the two.
 *
 * Output is pretty-printed with a 2-space indent and ends with a newline.
 */
internal object AgendaSnapshotJson {
    private val pretty = Json {
        prettyPrint = true
        prettyPrintIndent = "  "
    }

    fun sessions(list: List<Session>): String = render(
        buildJsonArray {
            list.forEach { session ->
                add(
                    buildJsonObject {
                        put("id", session.id)
                        put("title", session.title)
                        putNullable("type", session.type?.name)
                        putNullable("language", session.language?.name)
                        putNullable("complexity", session.complexity?.name)
                        val room = session.room
                        if (room == null) {
                            put("room", JsonNull)
                        } else {
                            put("room", roomObject(room))
                        }
                        put("startDate", session.scheduleSlot.startDate)
                        put("endDate", session.scheduleSlot.endDate)
                        putJsonArray("speakerIds") {
                            session.speakers.forEach { add(JsonPrimitive(it.id)) }
                        }
                    },
                )
            }
        },
    )

    fun speakers(list: List<Speaker>): String = render(
        buildJsonArray {
            list.forEach { speaker ->
                add(
                    buildJsonObject {
                        put("id", speaker.id)
                        put("name", speaker.name)
                    },
                )
            }
        },
    )

    fun rooms(list: List<Room>): String = render(
        buildJsonArray { list.forEach { add(roomObject(it)) } },
    )

    private fun roomObject(room: Room) = buildJsonObject {
        put("id", room.id)
        put("name", room.name)
        put("sortIndex", room.sortIndex)
    }

    private fun JsonObjectBuilder.putNullable(key: String, value: String?) {
        put(key, if (value == null) JsonNull else JsonPrimitive(value))
    }

    private fun render(element: JsonElement): String =
        pretty.encodeToString(JsonElement.serializer(), element) + "\n"
}
