package com.gdgnantes.devfest.feature.agenda.characterization

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.gdgnantes.devfest.core.model.Complexity
import com.gdgnantes.devfest.core.model.SessionFilter
import com.gdgnantes.devfest.core.model.SessionFilterType
import com.gdgnantes.devfest.core.model.SessionLanguage
import com.gdgnantes.devfest.core.model.SessionType
import com.gdgnantes.devfest.core.model.stubs.roomStubs
import com.gdgnantes.devfest.feature.agenda.AgendaFilterDrawer
import com.gdgnantes.devfest.feature.agenda.R
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import com.gdgnantes.devfest.core.ui.R as CoreUiR

/**
 * D-11 / D-14 characterization of the v1.0 [AgendaFilterDrawer], driven by Robolectric Compose.
 *
 * The drawer is hosted with its state held in the test: the callback stores the emitted set and
 * assigns it back, as `AgendaPager` does through the ViewModel. Results are asserted as
 * `(type, value)` pair sets only, so the very same assertions hold after D-14 turns `SessionFilter`
 * into a data class: the visible behavior is equality-agnostic.
 *
 * The checkbox has no semantics of its own (`onCheckedChange = null`), so the checked state shows
 * through what a click emits: adding when unchecked, removing every equal-content instance when
 * checked.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SessionFiltersDrawerCharacterizationTest {
    @get:Rule
    val composeRule = createComposeRule()

    private var filters by mutableStateOf<Set<SessionFilter>>(emptySet())
    private val emissions = mutableListOf<Set<SessionFilter>>()

    private val context: Context = ApplicationProvider.getApplicationContext()

    private fun host(initial: Set<SessionFilter> = emptySet()) {
        filters = initial
        composeRule.setContent {
            AgendaFilterDrawer(
                rooms = roomStubs.toSet(),
                sessionFilters = filters,
                onSessionFiltersChanged = {
                    emissions += it
                    filters = it
                },
            )
        }
    }

    private fun click(label: String) {
        composeRule.onNodeWithText(label).performScrollTo().performClick()
        composeRule.waitForIdle()
    }

    private fun Set<SessionFilter>.pairs(): Set<Pair<SessionFilterType, String>> = map { it.type to it.value }.toSet()

    private fun pair(type: SessionFilterType, value: String) = setOf(type to value)

    /** Clicks [label] twice on the hosted drawer: the first click adds the filter, the second removes it. */
    private fun assertToggleCycle(label: String, type: SessionFilterType, value: String) {
        click(label)
        assertEquals(pair(type, value), filters.pairs(), "$label: first click adds the filter")

        click(label)
        assertEquals(emptySet(), filters.pairs(), "$label: second click removes it")
    }

    private fun assertToggles(label: String, type: SessionFilterType, value: String) {
        host()

        assertToggleCycle(label, type, value)
        assertEquals(2, emissions.size)
    }

    @Test
    fun clicking_french_adds_language_french_and_clicking_again_removes_it() {
        assertToggles(
            context.getString(R.string.language_french),
            SessionFilterType.LANGUAGE,
            SessionLanguage.FRENCH.name
        )
    }

    @Test
    fun clicking_english_adds_language_english_and_clicking_again_removes_it() {
        assertToggles(
            context.getString(R.string.language_english),
            SessionFilterType.LANGUAGE,
            SessionLanguage.ENGLISH.name
        )
    }

    @Test
    fun the_bookmark_toggle_uses_an_empty_value() {
        assertToggles(context.getString(CoreUiR.string.bookmarked), SessionFilterType.BOOKMARK, "")
    }

    @Test
    fun every_room_toggle_uses_the_room_id_as_value() {
        host()

        roomStubs.forEach { room -> assertToggleCycle(room.name, SessionFilterType.ROOM, room.id) }
    }

    @Test
    fun every_complexity_toggle_uses_the_complexity_name_as_value() {
        host()

        mapOf(
            CoreUiR.string.complexity_beginner to Complexity.BEGINNER,
            CoreUiR.string.complexity_intermediate to Complexity.INTERMEDIATE,
            CoreUiR.string.complexity_advanced to Complexity.ADVANCED,
        ).forEach { (label, complexity) ->
            assertToggleCycle(context.getString(label), SessionFilterType.COMPLEXITY, complexity.name)
        }
    }

    @Test
    fun every_type_toggle_uses_the_session_type_name_as_value() {
        host()

        mapOf(
            CoreUiR.string.session_type_conference to SessionType.CONFERENCE,
            CoreUiR.string.session_type_quickie to SessionType.QUICKIE,
            CoreUiR.string.session_type_codelab to SessionType.CODELAB,
        ).forEach { (label, type) ->
            assertToggleCycle(context.getString(label), SessionFilterType.TYPE, type.name)
        }
    }

    @Test
    fun toggles_of_different_filters_accumulate_without_duplicates() {
        host()

        click(context.getString(R.string.language_french))
        click(context.getString(CoreUiR.string.complexity_advanced))
        click(context.getString(CoreUiR.string.session_type_codelab))
        click(context.getString(R.string.language_french))
        click(context.getString(R.string.language_french))

        assertEquals(
            setOf(
                SessionFilterType.COMPLEXITY to Complexity.ADVANCED.name,
                SessionFilterType.TYPE to SessionType.CODELAB.name,
                SessionFilterType.LANGUAGE to SessionLanguage.FRENCH.name,
            ),
            filters.pairs(),
        )
        assertEquals(3, filters.size, "no filter is held twice")
    }

    @Test
    fun the_clear_button_emits_an_empty_set() {
        host(
            setOf(
                SessionFilter(SessionFilterType.LANGUAGE, SessionLanguage.FRENCH.name),
                SessionFilter(SessionFilterType.BOOKMARK, ""),
            ),
        )

        click(context.getString(R.string.session_filters_clear))

        assertEquals(1, emissions.size)
        assertTrue(emissions.single().isEmpty())
    }

    @Test
    fun one_click_removes_both_equal_content_instances_of_a_checked_filter() {
        val french = SessionLanguage.FRENCH.name
        host(
            setOf(
                SessionFilter(SessionFilterType.LANGUAGE, french),
                SessionFilter(SessionFilterType.LANGUAGE, french),
                SessionFilter(SessionFilterType.COMPLEXITY, Complexity.ADVANCED.name),
            ),
        )

        click(context.getString(R.string.language_french))

        assertEquals(1, emissions.size)
        assertEquals(pair(SessionFilterType.COMPLEXITY, Complexity.ADVANCED.name), emissions.single().pairs())
        assertEquals(1, emissions.single().size, "both equal-content french filters are gone")
    }
}
