package com.gdgnantes.devfest.core.domain.venue

import com.gdgnantes.devfest.core.model.ContentLanguage

/**
 * Shared venue content language choice (SHARED-04): French for the language code `"fr"`, English
 * for everything else, including a null or empty code. The Android "no emission when the system
 * locale is null" stays in the ViewModel.
 */
class VenueLanguageUseCase {
    fun languageFor(languageCode: String?): ContentLanguage =
        if (languageCode == FRENCH_CODE) ContentLanguage.FRENCH else ContentLanguage.ENGLISH

    private companion object {
        const val FRENCH_CODE = "fr"
    }
}
