package com.gdgnantes.devfest.feature.speakers

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.gdgnantes.devfest.core.domain.di.domainModule
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.Speaker
import com.gdgnantes.devfest.core.model.stubs.SessionFixtures
import com.gdgnantes.devfest.core.model.stubs.SpeakerFixtures
import com.gdgnantes.devfest.core.testing.di.androidTestDoubles
import com.gdgnantes.devfest.core.ui.di.coreUiModule
import com.gdgnantes.devfest.feature.speakers.di.speakersModule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.test.KoinTestRule

@RunWith(AndroidJUnit4::class)
class SpeakersSmokeTest {

    @get:Rule(order = 0)
    val koinRule =
        KoinTestRule.create {
            modules(speakersModule, coreUiModule, domainModule)
            modules(androidTestDoubles)
        }

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    @Test
    fun speakers_list_renders_fixture_speakers_and_click_reports_the_speaker() {
        var clicked: Speaker? = null
        composeRule.setContent { SpeakersRoute(onSpeakerClick = { clicked = it }) }

        composeRule.waitUntil(TIMEOUT_MILLIS) {
            composeRule.onAllNodes(hasText(SpeakerFixtures.alice.name)).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText(SpeakerFixtures.alice.name).assertIsDisplayed().performClick()

        assertEquals(SpeakerFixtures.alice, clicked)
    }

    @Test
    fun speaker_detail_renders_alice_and_her_sessions_and_session_click_reports_it() {
        var clicked: Session? = null
        composeRule.setContent {
            SpeakerDetailRoute(
                speakerId = SpeakerFixtures.alice.id,
                onBackClick = {},
                onSessionClick = { clicked = it },
                onSocialLinkClick = { _, _ -> }
            )
        }

        val sessionTitle = SessionFixtures.composeDeepDive.title
        composeRule.waitUntil(TIMEOUT_MILLIS) {
            composeRule.onAllNodes(hasScrollToNodeAction()).fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNode(hasScrollToNodeAction()).performScrollToNode(hasText(sessionTitle))
        composeRule.onNodeWithText(sessionTitle).assertIsDisplayed()
        // The name is both in the top bar and on the session card speaker line.
        composeRule.onAllNodesWithText(SpeakerFixtures.alice.name).onFirst().assertExists()
        composeRule.onNodeWithText(sessionTitle).performClick()

        assertEquals(SessionFixtures.composeDeepDive, clicked)
    }

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
    }
}
