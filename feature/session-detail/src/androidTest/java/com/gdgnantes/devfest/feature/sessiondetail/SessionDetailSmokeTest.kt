package com.gdgnantes.devfest.feature.sessiondetail

import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gdgnantes.devfest.core.data.BookmarksStore
import com.gdgnantes.devfest.core.model.stubs.SessionFixtures
import com.gdgnantes.devfest.core.testing.FakeBookmarksStore
import com.gdgnantes.devfest.core.testing.di.androidTestDoubles
import com.gdgnantes.devfest.core.ui.di.coreUiModule
import com.gdgnantes.devfest.feature.sessiondetail.di.sessionDetailModule
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.module
import org.koin.test.KoinTestRule
import com.gdgnantes.devfest.core.ui.R as CoreUiR

/**
 * Deterministic [FeedbackFlags] (the D-23 seam): every flag reads false and the fetch completes
 * synchronously, so nothing remote is ever created or pending during the test.
 */
private class SmokeFeedbackFlags : FeedbackFlags {
    var fetchCount = 0
        private set

    override fun getBoolean(key: String): Boolean = false

    override fun fetchAndActivate(onComplete: (successful: Boolean, activated: Boolean) -> Unit) {
        fetchCount++
        onComplete(true, false)
    }
}

@RunWith(AndroidJUnit4::class)
class SessionDetailSmokeTest {

    private val bookmarks = FakeBookmarksStore()
    private val flags = SmokeFeedbackFlags()

    @get:Rule(order = 0)
    val koinRule =
        KoinTestRule.create {
            allowOverride(true)
            modules(sessionDetailModule, coreUiModule)
            modules(androidTestDoubles)
            modules(
                module {
                    factory { OpenFeedbackConfig(enabled = false, projectId = "smoke-test") }
                    single<BookmarksStore> { bookmarks }
                    single<FeedbackFlags> { flags }
                }
            )
        }

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private val targetContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Test
    fun session_detail_renders_the_fixture_session_and_speakers() {
        setSessionContent()

        val session = SessionFixtures.composeDeepDive
        waitForTitle()
        session.speakers.forEach { speaker ->
            // The speaker line is the full name followed by the company.
            composeRule.onAllNodes(hasText(speaker.name, substring = true)).onFirst().assertExists()
        }

        assertSame(flags, koinRule.koin.get<FeedbackFlags>())
        composeRule.waitForIdle()
        assertEquals(1, flags.fetchCount)
    }

    @Test
    fun bookmark_fab_toggles_the_bookmark() {
        setSessionContent()
        waitForTitle()
        val id = SessionFixtures.composeDeepDive.id
        val fab = targetContext.getString(CoreUiR.string.bookmarked)

        assertFalse(bookmarks.isBookmarked(id))
        composeRule.onNodeWithContentDescription(fab).performClick()
        composeRule.waitForIdle()
        assertTrue(bookmarks.isBookmarked(id))

        composeRule.onNodeWithContentDescription(fab).performClick()
        composeRule.waitForIdle()
        assertFalse(bookmarks.isBookmarked(id))
    }

    private fun setSessionContent() {
        composeRule.setContent {
            SessionDetailRoute(
                sessionId = SessionFixtures.composeDeepDive.id,
                onBackClick = {},
                onSocialLinkClick = { _, _ -> },
                onFeedbackFormFallbackLinkClick = {}
            )
        }
    }

    /** The title shows in the top bar and in the body; one node is enough to know it loaded. */
    private fun waitForTitle() {
        val title = SessionFixtures.composeDeepDive.title
        composeRule.waitUntil(TIMEOUT_MILLIS) {
            composeRule.onAllNodesWithText(title).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
    }
}
