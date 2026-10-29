package com.gdgnantes.devfest.feature.agenda

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasScrollToNodeAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isSelectable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.gdgnantes.devfest.core.data.SessionFiltersStore
import com.gdgnantes.devfest.core.data.SharedPreferencesSessionFiltersStore
import com.gdgnantes.devfest.core.domain.di.domainModule
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.SessionFilterType
import com.gdgnantes.devfest.core.model.SessionLanguage
import com.gdgnantes.devfest.core.model.stubs.SessionFixtures
import com.gdgnantes.devfest.core.testing.di.androidTestDoubles
import com.gdgnantes.devfest.core.ui.di.coreUiModule
import com.gdgnantes.devfest.feature.agenda.di.agendaModule
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.dsl.module
import org.koin.test.KoinTestRule

/**
 * Smoke test of the agenda on the production module, the real session filters store and the
 * shared test doubles.
 *
 * The agenda auto-scrolls by wall clock, so nothing here depends on the first row or on the initial
 * scroll position: sessions are reached with a scroll-to-node on the vertical list. The first day
 * tab is selected explicitly so the initial pager page does not matter either.
 */
@RunWith(AndroidJUnit4::class)
class AgendaSmokeTest {

    private val testPrefs: SharedPreferences =
        InstrumentationRegistry.getInstrumentation()
            .targetContext
            .getSharedPreferences("agenda-smoke-test", Context.MODE_PRIVATE)

    @get:Rule(order = 0)
    val koinRule =
        KoinTestRule.create {
            modules(agendaModule, coreUiModule, domainModule)
            modules(androidTestDoubles)
            // Last module: androidTestDoubles binds a fake SessionFiltersStore that would otherwise win
            // over the real store, so the ViewModel and the assertions share the real instance.
            modules(
                module {
                    single<SharedPreferences> { testPrefs }
                    single<SessionFiltersStore> { SharedPreferencesSessionFiltersStore(get()) }
                }
            )
        }

    @get:Rule(order = 1)
    val composeRule = createComposeRule()

    private val targetContext
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Before
    fun clearPreferences() {
        testPrefs.edit().clear().commit()
    }

    @Test
    fun agenda_renders_fixture_sessions_and_click_reports_the_session() {
        var clicked: Session? = null
        composeRule.setContent {
            AgendaRoute(
                agendaFilterDrawerState = rememberDrawerState(DrawerValue.Closed),
                onSessionClick = { clicked = it }
            )
        }

        composeRule.selectFirstDay()
        composeRule.scrollListTo(SessionFixtures.gitQuickie.title)
        composeRule.onNodeWithText(SessionFixtures.gitQuickie.title).assertIsDisplayed().performClick()

        assertEquals(SessionFixtures.gitQuickie, clicked)
    }

    @Test
    fun english_filter_from_the_drawer_is_stored_and_hides_french_sessions() {
        composeRule.setContent {
            AgendaRoute(
                agendaFilterDrawerState = rememberDrawerState(DrawerValue.Open),
                onSessionClick = {}
            )
        }

        composeRule.selectFirstDay()
        // Before the filter the French session is part of the list.
        composeRule.scrollListTo(SessionFixtures.webRendering.title)
        composeRule.onNodeWithText(SessionFixtures.webRendering.title).assertExists()

        composeRule.onNodeWithText(targetContext.getString(R.string.language_english)).performClick()

        val filters = koinRule.koin.get<SessionFiltersStore>().filters
        composeRule.waitUntil(TIMEOUT_MILLIS) { filters.value.isNotEmpty() }
        assertEquals(
            listOf(SessionFilterType.LANGUAGE to SessionLanguage.ENGLISH.name),
            filters.value.map { it.type to it.value }
        )

        composeRule.scrollListTo(SessionFixtures.composeDeepDive.title)
        composeRule.onNodeWithText(SessionFixtures.composeDeepDive.title).assertExists()
        composeRule.onNodeWithText(SessionFixtures.webRendering.title).assertDoesNotExist()
    }

    /** Day 1 is the first tab; selecting it makes the test independent of the pager's start page. */
    private fun ComposeContentTestRule.selectFirstDay() {
        waitUntil(TIMEOUT_MILLIS) { onAllNodes(isSelectable()).fetchSemanticsNodes().isNotEmpty() }
        onAllNodes(isSelectable())[0].performClick()
    }

    /** Scrolls the vertical session list (not the day pager) until [title] is composed. */
    private fun ComposeContentTestRule.scrollListTo(title: String) {
        val verticalList =
            hasScrollToNodeAction() and
                SemanticsMatcher.keyIsDefined(SemanticsProperties.VerticalScrollAxisRange)
        waitUntil(TIMEOUT_MILLIS) { onAllNodes(verticalList).fetchSemanticsNodes().isNotEmpty() }
        onNode(verticalList).performScrollToNode(hasText(title))
    }

    private companion object {
        const val TIMEOUT_MILLIS = 5_000L
    }
}
