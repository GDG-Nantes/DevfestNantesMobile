package com.gdgnantes.devfest.core.testing

import com.gdgnantes.devfest.core.model.stubs.SessionFixtures
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class FakeStoresTest {
    @Test
    fun fakeDevFestNantesStore_sessions_are_the_fixed_fixtures() = runTest {
        val sessions = fakeDevFestNantesStore().sessions.first()
        assertEquals(SessionFixtures.all, sessions)
    }
}
