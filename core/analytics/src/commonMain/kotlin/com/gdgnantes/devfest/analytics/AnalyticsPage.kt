package com.gdgnantes.devfest.analytics

enum class AnalyticsPage {
    ABOUT,
    AGENDA,
    DATASHARING,
    LEGAL,
    SESSION_DETAILS,
    SETTINGS,
    SPEAKER,
    SPEAKERS,
    VENUE;

    override fun toString(): String {
        return name.lowercase()
    }
}
