package com.gdgnantes.devfest.feature.agenda.utils

import kotlinx.serialization.Serializable

@Serializable
class SessionFilter(val type: FilterType, val value: String) {
    enum class FilterType {
        BOOKMARK,
        COMPLEXITY,
        LANGUAGE,
        ROOM,
        TYPE
    }
}
