package com.gdgnantes.devfest.analytics

enum class AnalyticsPage {
    ABOUT,
    AGENDA,
    DATASHARING,
    SESSION_DETAILS,
    SETTINGS,
    VENUE;

    override fun toString(): String {
        return name.lowercase()
    }
}