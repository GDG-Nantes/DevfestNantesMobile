package com.gdgnantes.devfest.utils

import com.gdgnantes.devfest.model.Session
import com.gdgnantes.devfest.model.SessionLanguage

fun Session.getDurationInMinutes(): String {
    val millisecondsDuration =
        scheduleSlot.endDateAsEpochMilliseconds - scheduleSlot.startDateAsEpochMilliseconds
    val minutesDuration = millisecondsDuration / 60000
    return minutesDuration.toString()
}

fun Session.getLanguageEmoji(): String? {
    return when (language) {
        SessionLanguage.ENGLISH -> "\uD83C\uDDEC\uD83C\uDDE7"
        SessionLanguage.FRENCH -> "\uD83C\uDDEB\uD83C\uDDF7"
        else -> null
    }
}

fun Session.getDurationAndLanguageString(): String {
    val languageEmoji = getLanguageEmoji()
    return getDurationInMinutes() + " minutes" + if (languageEmoji != null) " / $languageEmoji" else ""
}