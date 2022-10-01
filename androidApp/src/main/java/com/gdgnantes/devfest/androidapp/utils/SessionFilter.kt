package com.gdgnantes.devfest.androidapp.utils

class SessionFilter(val type: FilterType, val value: Any) {
    enum class FilterType {
        BOOKMARK,
        COMPLEXITY,
        LANGUAGE,
        ROOM,
        TYPE
    }
}