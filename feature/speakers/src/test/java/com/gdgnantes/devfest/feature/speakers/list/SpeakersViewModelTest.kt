package com.gdgnantes.devfest.feature.speakers.list

import app.cash.turbine.test
import com.gdgnantes.devfest.core.model.stubs.SpeakerFixtures
import com.gdgnantes.devfest.core.testing.FakeDevFestNantesStore
import com.gdgnantes.devfest.core.testing.MainDispatcherRule
import com.gdgnantes.devfest.core.testing.NoOpPerformanceMonitoring
import com.gdgnantes.devfest.core.ui.UiState
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class SpeakersViewModelTest {
    // A standard dispatcher lets the test observe STARTING before the init block's collection runs.
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val store = FakeDevFestNantesStore()

    private fun viewModel() = SpeakersViewModel(store, NoOpPerformanceMonitoring())

    @Test
    fun ui_state_goes_from_starting_to_success_with_the_fixture_speakers() =
        runTest {
            val viewModel = viewModel()

            viewModel.uiState.test {
                assertEquals(UiState.STARTING, awaitItem())
                assertEquals(UiState.SUCCESS, awaitItem())
            }

            assertEquals(SpeakerFixtures.all, viewModel.speakers.value)
        }

    @Test
    fun pushed_speakers_replace_the_list() =
        runTest {
            val viewModel = viewModel()

            viewModel.speakers.test {
                assertEquals(emptyList(), awaitItem())
                assertEquals(SpeakerFixtures.all, awaitItem())

                store.speakersState.value = listOf(SpeakerFixtures.alice)

                assertEquals(listOf(SpeakerFixtures.alice), awaitItem())
            }
        }

    // Characterizes a production defect (deferred, D-10): SpeakersViewModel never catches a store
    // failure. The error escapes viewModelScope uncaught, so the UI would crash rather than show
    // UiState.ERROR, and uiState never leaves STARTING.
    @Test
    fun store_failure_is_characterized() {
        store.failure = IllegalStateException("boom")

        val thrown =
            assertFailsWith<IllegalStateException> {
                runTest {
                    val viewModel = viewModel()

                    viewModel.uiState.test {
                        assertEquals(UiState.STARTING, awaitItem())
                        expectNoEvents()
                    }

                    assertEquals(UiState.STARTING, viewModel.uiState.value)
                    assertEquals(emptyList(), viewModel.speakers.value)
                }
            }

        assertEquals("boom", thrown.message)
    }
}
