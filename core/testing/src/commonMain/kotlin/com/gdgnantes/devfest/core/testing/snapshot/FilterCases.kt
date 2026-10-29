package com.gdgnantes.devfest.core.testing.snapshot

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * `filter-cases.json`: the shared, ordered filter cases both apps and the shared domain layer are
 * checked against. Filter types stay strings; each platform maps them to its own filter type.
 */
@Serializable
data class FilterCases(
    val bookmarkedSessionIds: List<String>,
    val cases: List<FilterCase>,
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = false }

        fun decode(text: String): FilterCases = json.decodeFromString(serializer(), text)
    }
}

@Serializable
data class FilterCase(
    val name: String,
    val filters: List<FilterCaseFilter>,
)

@Serializable
data class FilterCaseFilter(
    val type: String,
    val value: String,
)
