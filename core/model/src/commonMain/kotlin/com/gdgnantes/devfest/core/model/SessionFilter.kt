package com.gdgnantes.devfest.core.model

import kotlinx.serialization.Serializable

/**
 * A single agenda filter, persisted by both platforms through the shared filter codec.
 *
 * Value equality since Phase 7 (D-14): two filters with the same [type] and [value] are equal and
 * collapse to one element of a `Set`.
 */
@Serializable
data class SessionFilter(val type: SessionFilterType, val value: String)

/** The kind of criterion a [SessionFilter] applies to. Persisted by entry name. */
enum class SessionFilterType {
    BOOKMARK,
    COMPLEXITY,
    LANGUAGE,
    ROOM,
    TYPE,
}
