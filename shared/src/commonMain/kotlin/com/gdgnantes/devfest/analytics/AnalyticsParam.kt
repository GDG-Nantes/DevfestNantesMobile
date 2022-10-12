package com.gdgnantes.devfest.analytics

enum class AnalyticsParam {
    FROM_PAGE,
    OPENFEEDBACK_ID,
    PARTNER_NAME,
    SESSION_ID,
    SOCIAL_TYPE,
    SPEAKER_ID;

    override fun toString(): String {
        return name.lowercase()
    }
}