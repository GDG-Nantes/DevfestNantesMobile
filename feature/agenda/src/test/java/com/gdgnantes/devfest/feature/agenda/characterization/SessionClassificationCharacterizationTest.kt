package com.gdgnantes.devfest.feature.agenda.characterization

import com.gdgnantes.devfest.core.model.SessionType
import com.gdgnantes.devfest.feature.agenda.utils.isService
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * D-11 characterization of the v1.0 Android session classification: `SessionType.isService()` and
 * the bookmark-star condition of `AgendaRow` (`session.type?.isService() == false`).
 *
 * The navigable classification of `MainActivity.onSessionClick` is private to an Activity and is
 * pinned by the explicit table of the shared `ClassifySessionUseCaseTest` instead.
 */
class SessionClassificationCharacterizationTest {
    private val serviceTypes = setOf(SessionType.OPENING, SessionType.BREAK, SessionType.LUNCH)

    /** The star condition of `AgendaRow`, copied verbatim. */
    private fun showsBookmarkStar(type: SessionType?): Boolean = type?.isService() == false

    @Test
    fun isService_is_true_only_for_opening_break_and_lunch() {
        SessionType.entries.forEach { type ->
            assertEquals(type in serviceTypes, type.isService(), "isService() of $type")
        }
    }

    @Test
    fun the_bookmark_star_is_hidden_for_service_types_and_for_a_null_type_and_shown_for_the_others() {
        assertFalse(showsBookmarkStar(null), "null type")
        SessionType.entries.forEach { type ->
            assertEquals(type !in serviceTypes, showsBookmarkStar(type), "star of $type")
        }
    }

    @Test
    fun every_session_type_is_covered() {
        assertEquals(
            setOf("BREAK", "CODELAB", "CONFERENCE", "KEYNOTE", "LUNCH", "OPENING", "QUICKIE"),
            SessionType.entries.map { it.name }.toSet(),
            "a new SessionType needs an explicit classification decision",
        )
        assertTrue(serviceTypes.all { it in SessionType.entries })
    }
}
