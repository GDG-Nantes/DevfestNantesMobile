package com.gdgnantes.devfest.feature.speakers

import com.gdgnantes.devfest.core.model.stubs.SessionFixtures
import com.gdgnantes.devfest.core.model.stubs.SpeakerFixtures
import com.gdgnantes.devfest.core.testing.FakeDevFestNantesStore
import com.gdgnantes.devfest.core.testing.MainDispatcherRule
import com.gdgnantes.devfest.core.testing.NoOpPerformanceMonitoring
import com.gdgnantes.devfest.core.ui.UiState
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class SpeakerViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val store = FakeDevFestNantesStore()

    private fun viewModel(speakerId: String) = SpeakerViewModel(store, NoOpPerformanceMonitoring(), speakerId)

    @Test
    fun loads_alice_and_her_two_sessions() =
        runTest {
            val viewModel = viewModel(SpeakerFixtures.alice.id)

            assertEquals(UiState.STARTING, viewModel.uiState.value)

            advanceUntilIdle()

            assertEquals(SpeakerFixtures.alice, viewModel.speaker.value)
            assertEquals(
                listOf(SessionFixtures.composeDeepDive, SessionFixtures.webRendering),
                viewModel.sessions.value
            )
            assertEquals(UiState.SUCCESS, viewModel.uiState.value)
        }

    @Test
    fun speaker_without_profile_fields_loads_with_nulls() =
        runTest {
            val viewModel = viewModel(SpeakerFixtures.farid.id)

            advanceUntilIdle()

            assertEquals(SpeakerFixtures.farid, viewModel.speaker.value)
            assertEquals(listOf(SessionFixtures.closing), viewModel.sessions.value)
            assertEquals(UiState.SUCCESS, viewModel.uiState.value)
        }

    // Characterizes current behavior: an unknown speaker is reported as SUCCESS with a null speaker
    // and no sessions, never as an error state.
    @Test
    fun unknown_speaker_still_reports_success_with_null_speaker() =
        runTest {
            val viewModel = viewModel("no-such-speaker")

            advanceUntilIdle()

            assertNull(viewModel.speaker.value)
            assertEquals(emptyList(), viewModel.sessions.value)
            assertEquals(UiState.SUCCESS, viewModel.uiState.value)
        }
}
