package com.gdgnantes.devfest.core.data

import com.gdgnantes.devfest.core.model.SessionFilter
import com.gdgnantes.devfest.core.model.SessionFilterType
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SessionFilterCodecTest {
    @Test
    fun encode_produces_the_v1_0_json_literals() {
        assertEquals(
            """{"type":"LANGUAGE","value":"FRENCH"}""",
            SessionFilterCodec.encode(SessionFilter(SessionFilterType.LANGUAGE, "FRENCH")),
        )
        assertEquals(
            """{"type":"ROOM","value":"titan"}""",
            SessionFilterCodec.encode(SessionFilter(SessionFilterType.ROOM, "titan")),
        )
        assertEquals(
            """{"type":"BOOKMARK","value":""}""",
            SessionFilterCodec.encode(SessionFilter(SessionFilterType.BOOKMARK, "")),
        )
    }

    @Test
    fun decode_round_trips_every_filter_type() {
        SessionFilterType.entries.forEach { type ->
            val filter = SessionFilter(type, "value-$type")

            assertEquals(filter, SessionFilterCodec.decode(SessionFilterCodec.encode(filter)))
        }
    }

    @Test
    fun decode_reads_the_v1_0_literal() {
        assertEquals(
            SessionFilter(SessionFilterType.COMPLEXITY, "BEGINNER"),
            SessionFilterCodec.decode("""{"type":"COMPLEXITY","value":"BEGINNER"}"""),
        )
    }

    @Test
    fun decode_throws_for_malformed_input() {
        assertFailsWith<SerializationException> { SessionFilterCodec.decode("not-json") }
    }

    @Test
    fun decode_or_null_returns_null_for_malformed_input() {
        assertNull(SessionFilterCodec.decodeOrNull("not-json"))
    }

    @Test
    fun decode_or_null_returns_null_for_an_unknown_type() {
        assertNull(SessionFilterCodec.decodeOrNull("""{"type":"UNKNOWN","value":"x"}"""))
    }

    @Test
    fun decode_or_null_returns_null_for_an_extra_key() {
        assertNull(SessionFilterCodec.decodeOrNull("""{"type":"ROOM","value":"x","extra":"y"}"""))
    }

    @Test
    fun filters_with_the_same_type_and_value_collapse_in_a_set() {
        val filters =
            setOf(
                SessionFilter(SessionFilterType.LANGUAGE, "FRENCH"),
                SessionFilter(SessionFilterType.LANGUAGE, "FRENCH"),
            )

        assertEquals(1, filters.size)
    }
}
