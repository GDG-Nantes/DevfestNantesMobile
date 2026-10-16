package com.gdgnantes.devfest.core.testing

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

/**
 * JUnit rule that replaces `Dispatchers.Main` with [dispatcher] for the duration of each test, so
 * code that launches on `viewModelScope` can run on the JVM.
 *
 * The default [UnconfinedTestDispatcher] runs launched coroutines eagerly, which suits tests that
 * only look at the final state. Pass a `StandardTestDispatcher()` when a test must observe the
 * states in between. `runTest` picks up the scheduler of the Main test dispatcher automatically,
 * so virtual time is shared with the ViewModel.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}
