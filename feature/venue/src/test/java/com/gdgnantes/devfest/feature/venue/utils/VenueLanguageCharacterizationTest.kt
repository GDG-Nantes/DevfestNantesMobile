package com.gdgnantes.devfest.feature.venue.utils

import com.gdgnantes.devfest.core.model.ContentLanguage
import org.junit.Test
import java.util.Locale
import kotlin.test.assertEquals

/**
 * D-11 characterization of the v1.0 venue language mapping, `Locale.toContentLanguage()`: French
 * locales give [ContentLanguage.FRENCH], everything else [ContentLanguage.ENGLISH].
 *
 * The `VenueViewModel` branch that emits nothing when the system locale is null cannot be produced
 * under Robolectric; it stays in the ViewModel unchanged in 07-18.
 */
class VenueLanguageCharacterizationTest {
    private val expected =
        mapOf(
            Locale.forLanguageTag("fr") to ContentLanguage.FRENCH,
            Locale.forLanguageTag("fr-FR") to ContentLanguage.FRENCH,
            Locale.forLanguageTag("fr-CA") to ContentLanguage.FRENCH,
            Locale.forLanguageTag("en") to ContentLanguage.ENGLISH,
            Locale.forLanguageTag("en-US") to ContentLanguage.ENGLISH,
            Locale.forLanguageTag("de-DE") to ContentLanguage.ENGLISH,
            Locale.ROOT to ContentLanguage.ENGLISH,
        )

    @Test
    fun toContentLanguage_maps_every_locale_of_the_table() {
        expected.forEach { (locale, language) ->
            assertEquals(language, locale.toContentLanguage(), "language of '${locale.toLanguageTag()}'")
        }
    }

    @Test
    fun only_the_french_language_selects_french() {
        val frenchTags = expected.filterValues { it == ContentLanguage.FRENCH }.keys.map { it.language }.toSet()

        assertEquals(setOf("fr"), frenchTags)
    }
}
