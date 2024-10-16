package com.gdgnantes.devfest.utils

import com.gdgnantes.devfest.model.Session
import com.gdgnantes.devfest.model.SessionLanguage

const val MILLISECONDS_IN_MINUTE = 60000

fun Session.getDurationInMinutes(): String {
    val millisecondsDuration =
        scheduleSlot.endDateAsEpochMilliseconds - scheduleSlot.startDateAsEpochMilliseconds
    val minutesDuration = millisecondsDuration / MILLISECONDS_IN_MINUTE
    return minutesDuration.toString()
}

fun Session.getLanguageEmojiString(): String? {
    return when (language) {
        SessionLanguage.ENGLISH -> "\uD83C\uDDEC\uD83C\uDDE7"
        SessionLanguage.FRENCH -> "\uD83C\uDDEB\uD83C\uDDF7"
        else -> null
    }
}

fun Session.getDurationString(): String {
    return getDurationInMinutes() + " minutes"
}

fun Session.getDurationAndLanguageString(): String {
    val languageEmoji = getLanguageEmojiString()
    return getDurationString() + if (languageEmoji != null) " / $languageEmoji" else ""
}
