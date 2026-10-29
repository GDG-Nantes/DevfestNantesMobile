package com.gdgnantes.devfest.core.data

import com.gdgnantes.devfest.core.model.SessionFilter
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

/**
 * Encodes a [SessionFilter] as one JSON string, exactly like the v1.0 Android filters service: the
 * default [Json] instance, so the string is `{"type":"LANGUAGE","value":"FRENCH"}`. Both platforms
 * persist a set of these strings (D-15).
 *
 * No logging here (common code has no logger): the Android store keeps its error lines by catching
 * around [decode].
 */
internal object SessionFilterCodec {
    fun encode(filter: SessionFilter): String = Json.encodeToString(SessionFilter.serializer(), filter)

    /** Decodes [raw], throwing [SerializationException] or [IllegalArgumentException] if malformed. */
    fun decode(raw: String): SessionFilter = Json.decodeFromString<SessionFilter>(raw)

    /** Like [decode], but returns null for a malformed or unknown entry instead of throwing. */
    fun decodeOrNull(raw: String): SessionFilter? =
        try {
            decode(raw)
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
}
