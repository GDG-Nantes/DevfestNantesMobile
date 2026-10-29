package com.gdgnantes.devfest.feature.venue

import com.gdgnantes.devfest.core.domain.venue.VenueLanguageUseCase
import com.gdgnantes.devfest.core.model.ContentLanguage
import com.gdgnantes.devfest.core.model.stubs.buildVenueStub
import com.gdgnantes.devfest.core.testing.FakeDevFestNantesStore
import com.gdgnantes.devfest.core.testing.MainDispatcherRule
import com.gdgnantes.devfest.core.ui.UiState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertNotSame

/**
 * [VenueViewModel] reads the system locale through `Resources.getSystem()`, which needs a
 * (Robolectric) Android environment. The locale is set with Robolectric configuration qualifiers.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class VenueViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val viewModel = VenueViewModel(FakeDevFestNantesStore(), VenueLanguageUseCase())

    @Test
    @Config(qualifiers = "fr-rFR")
    fun french_locale_loads_the_french_venue() =
        runTest {
            assertEquals(buildVenueStub(ContentLanguage.FRENCH), viewModel.venue.first { it != null })
            assertEquals(UiState.SUCCESS, viewModel.uiState.value)
        }

    @Test
    @Config(qualifiers = "en-rUS")
    fun english_locale_loads_the_english_venue() =
        runTest {
            assertEquals(buildVenueStub(ContentLanguage.ENGLISH), viewModel.venue.first { it != null })
            assertEquals(UiState.SUCCESS, viewModel.uiState.value)
        }

    @Test
    @Config(qualifiers = "de-rDE")
    fun unsupported_locale_falls_back_to_english() =
        runTest {
            assertEquals(buildVenueStub(ContentLanguage.ENGLISH), viewModel.venue.first { it != null })
            assertEquals(UiState.SUCCESS, viewModel.uiState.value)
        }

    @Test
    fun ui_state_starts_loading() {
        assertEquals(UiState.LOADING, viewModel.uiState.value)
    }

    // Characterizes a production quirk (deferred, D-10): `venue` is a getter that builds a new
    // StateFlow on every access instead of a single property.
    @Test
    fun venue_builds_a_new_state_flow_on_every_access() {
        assertNotSame(viewModel.venue, viewModel.venue)
    }
}
