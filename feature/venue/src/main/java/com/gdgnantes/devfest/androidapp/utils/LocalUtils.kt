package com.gdgnantes.devfest.androidapp.utils

import com.gdgnantes.devfest.core.model.ContentLanguage
import java.util.*

fun Locale.toContentLanguage(): ContentLanguage {
    return when (language) {
        "fr" -> ContentLanguage.FRENCH
        "en" -> ContentLanguage.ENGLISH
        else -> ContentLanguage.ENGLISH
    }
}
