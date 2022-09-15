package com.gdgnantes.devfest.android.utils

class SessionFilter(val type: FilterType, val value: Any) {
    enum class FilterType {
        BOOKMARK,
        LANGUAGE,
        ROOM
    }
}