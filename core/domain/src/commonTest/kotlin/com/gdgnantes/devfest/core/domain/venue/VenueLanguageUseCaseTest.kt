package com.gdgnantes.devfest.core.domain.venue

import com.gdgnantes.devfest.core.model.ContentLanguage
import kotlin.test.Test
import kotlin.test.assertEquals

/** The venue language table of v1.0 Android (locale language) and v1.0 iOS (language code). */
class VenueLanguageUseCaseTest {
    private val useCase = VenueLanguageUseCase()

    // Language codes of the Android locales of the 07-06 table (fr, fr-FR, fr-CA, en, en-US, de-DE,
    // the root locale) and the iOS codes of 07-08, written as literals on purpose.
    private val expected: Map<String?, ContentLanguage> =
        mapOf(
            "fr" to ContentLanguage.FRENCH,
            "en" to ContentLanguage.ENGLISH,
            "de" to ContentLanguage.ENGLISH,
            "" to ContentLanguage.ENGLISH,
            null to ContentLanguage.ENGLISH
        )

    @Test
    fun language_for_maps_every_code_of_the_table() {
        expected.forEach { (code, language) ->
            assertEquals(language, useCase.languageFor(code), "language of '$code'")
        }
    }

    @Test
    fun only_the_french_code_selects_french() {
        assertEquals(setOf<String?>("fr"), expected.filterValues { it == ContentLanguage.FRENCH }.keys)
        assertEquals(ContentLanguage.ENGLISH, useCase.languageFor("FR"))
        assertEquals(ContentLanguage.ENGLISH, useCase.languageFor("fr-FR"))
    }
}
