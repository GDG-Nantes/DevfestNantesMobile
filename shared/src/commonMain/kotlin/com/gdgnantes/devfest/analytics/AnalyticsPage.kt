package com.gdgnantes.devfest.analytics

enum class AnalyticsPage {
    about,
    agenda,
    DATASHARING,
    sessionDetails,
    SETTINGS,
    venue;

    val value: String
        get() = name.lowercase()
}