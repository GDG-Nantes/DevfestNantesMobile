package com.gdgnantes.devfest.model

enum class ContentLanguage {
    ENGLISH,
    FRENCH;

    val apiParameter: String
        get() = when (this) {
            ENGLISH -> "en"
            FRENCH -> "fr"
        }
}