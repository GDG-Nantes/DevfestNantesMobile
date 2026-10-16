package com.gdgnantes.devfest.feature.sessiondetail

import app.cash.turbine.test
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.stubs.SessionFixtures
import com.gdgnantes.devfest.core.testing.FakeDevFestNantesStore
import com.gdgnantes.devfest.core.testing.MainDispatcherRule
import com.gdgnantes.devfest.core.testing.NoOpPerformanceMonitoring
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

class SessionViewModelTest {
    // A standard dispatcher lets the test observe the empty state before the init block's load runs.
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val store = FakeDevFestNantesStore()

    private fun viewModel(sessionId: String) = SessionViewModel(store, NoOpPerformanceMonitoring(), sessionId)

    @Test
    fun loads_the_fixture_session_for_a_known_id() =
        runTest {
            val viewModel = viewModel(SessionFixtures.composeDeepDive.id)

            advanceUntilIdle()

            assertEquals(SessionFixtures.composeDeepDive, viewModel.session.value)
        }

    @Test
    fun unknown_id_yields_null() =
        runTest {
            val viewModel = viewModel("no-such-session")

            advanceUntilIdle()

            assertNull(viewModel.session.value)
        }

    @Test
    fun session_goes_from_null_to_loaded() =
        runTest {
            val viewModel = viewModel(SessionFixtures.closing.id)

            viewModel.session.test {
                assertNull(awaitItem())
                assertEquals<Session?>(SessionFixtures.closing, awaitItem())
            }
        }

    // Characterizes a production defect (deferred, D-10): SessionViewModel never catches a store
    // failure. The error escapes viewModelScope uncaught, so the screen would crash and the
    // session stays null.
    @Test
    fun store_failure_is_characterized() {
        store.failure = IllegalStateException("boom")

        val thrown =
            assertFailsWith<IllegalStateException> {
                runTest {
                    val viewModel = viewModel(SessionFixtures.composeDeepDive.id)

                    advanceUntilIdle()

                    assertNull(viewModel.session.value)
                }
            }

        assertEquals("boom", thrown.message)
    }
}
