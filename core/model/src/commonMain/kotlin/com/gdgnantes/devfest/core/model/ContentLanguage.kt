package com.gdgnantes.devfest.core.model

enum class ContentLanguage {
    ENGLISH,
    FRENCH;

    val apiParameter: String
        get() =
            when (this) {
            ENGLISH -> "en"
            FRENCH -> "fr"
        }
}
