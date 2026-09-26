package com.gdgnantes.devfest.core.testing

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class FakeStoresTest {
    @Test
    fun fakeDevFestNantesStore_sessions_not_empty() = runTest {
        val sessions = fakeDevFestNantesStore().sessions.first()
        assertTrue(sessions.isNotEmpty())
    }
}
