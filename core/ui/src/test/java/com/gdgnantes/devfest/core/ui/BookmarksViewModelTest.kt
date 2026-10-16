package com.gdgnantes.devfest.core.ui

import app.cash.turbine.test
import com.gdgnantes.devfest.core.analytics.AnalyticsPage
import com.gdgnantes.devfest.core.model.stubs.SessionFixtures
import com.gdgnantes.devfest.core.testing.BookmarkEvent
import com.gdgnantes.devfest.core.testing.FakeAnalyticsService
import com.gdgnantes.devfest.core.testing.FakeBookmarksStore
import com.gdgnantes.devfest.core.testing.MainDispatcherRule
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import kotlin.test.Test
import kotlin.test.assertEquals

class BookmarksViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val analytics = FakeAnalyticsService()
    private val bookmarks = FakeBookmarksStore()
    private val sessionId = SessionFixtures.welcome.id

    private fun viewModel() = BookmarksViewModel(analytics, bookmarks)

    @Test
    fun subscribe_starts_false_and_follows_the_store() =
        runTest {
            val viewModel = viewModel()

            viewModel.subscribe(sessionId).test {
                assertEquals(false, awaitItem())

                bookmarks.setBookmarked(sessionId, true)
                assertEquals(true, awaitItem())

                bookmarks.setBookmarked(sessionId, false)
                assertEquals(false, awaitItem())
            }
        }

    @Test
    fun subscribe_ignores_bookmarks_of_other_sessions() =
        runTest {
            val viewModel = viewModel()

            viewModel.subscribe(sessionId).test {
                assertEquals(false, awaitItem())

                bookmarks.setBookmarked(SessionFixtures.closing.id, true)

                expectNoEvents()
            }
        }

    @Test
    fun setBookmarked_updates_the_store_and_records_one_event() {
        val viewModel = viewModel()

        viewModel.setBookmarked(sessionId, true, AnalyticsPage.AGENDA)

        assertEquals(true, bookmarks.isBookmarked(sessionId))
        assertEquals(listOf(BookmarkEvent(AnalyticsPage.AGENDA, sessionId, true)), analytics.bookmarkEvents)
    }

    @Test
    fun unbookmarking_records_a_second_event_with_false() {
        val viewModel = viewModel()

        viewModel.setBookmarked(sessionId, true, AnalyticsPage.AGENDA)
        viewModel.setBookmarked(sessionId, false, AnalyticsPage.SESSION_DETAILS)

        assertEquals(false, bookmarks.isBookmarked(sessionId))
        assertEquals(
            listOf(
                BookmarkEvent(AnalyticsPage.AGENDA, sessionId, true),
                BookmarkEvent(AnalyticsPage.SESSION_DETAILS, sessionId, false)
            ),
            analytics.bookmarkEvents
        )
    }
}
