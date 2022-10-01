package com.gdgnantes.devfest.analytics

enum class AnalyticsPage {
    agenda,
    sessionDetails,
    venue,
    about;

    val value: String
        get() = name.lowercase()
}