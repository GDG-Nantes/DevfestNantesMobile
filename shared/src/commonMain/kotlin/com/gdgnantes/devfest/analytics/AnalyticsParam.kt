package com.gdgnantes.devfest.analytics

enum class AnalyticsParam {
    FROM_PAGE,
    PARTNER_URL,
    SESSION_ID,
    SPEAKER,
    SOCIAL_LINK;

    override fun toString(): String {
        return name.lowercase()
    }
}