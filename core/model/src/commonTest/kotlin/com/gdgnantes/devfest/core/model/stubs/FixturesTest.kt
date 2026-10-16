package com.gdgnantes.devfest.core.model.stubs

import com.gdgnantes.devfest.core.model.Agenda
import com.gdgnantes.devfest.core.model.Complexity
import com.gdgnantes.devfest.core.model.PartnerCategory
import com.gdgnantes.devfest.core.model.SessionLanguage
import com.gdgnantes.devfest.core.model.SessionType
import com.gdgnantes.devfest.core.model.SocialType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class FixturesTest {
    private val sessions = SessionFixtures.all
    private val speakers = SpeakerFixtures.all

    @Test
    fun ids_are_unique_and_sessions_are_pairwise_distinct() {
        assertEquals(10, sessions.size)
        assertEquals(10, sessions.map { it.id }.toSet().size)
        assertEquals(10, sessions.toSet().size)
        assertEquals(6, speakers.size)
        assertEquals(6, speakers.map { it.id }.toSet().size)
    }

    @Test
    fun every_room_is_used_and_only_lunch_has_no_room() {
        val usedRoomIds = sessions.mapNotNull { it.room?.id }.toSet()
        assertTrue(usedRoomIds.containsAll(roomStubs.map { it.id }.toSet()))
        assertEquals(listOf(SessionFixtures.lunch), sessions.filter { it.room == null })
    }

    @Test
    fun languages_complexities_and_types_cover_every_value_and_null() {
        val languages = sessions.map { it.language }.toSet()
        assertEquals(setOf(SessionLanguage.FRENCH, SessionLanguage.ENGLISH, null), languages)

        val complexities = sessions.map { it.complexity }.toSet()
        assertEquals(Complexity.entries.toSet() + null, complexities)

        val types = sessions.map { it.type }.toSet()
        assertEquals(SessionType.entries.toSet() + null, types)

        assertTrue(sessions.any { it.category == null })
    }

    @Test
    fun partners_cover_every_category() {
        assertEquals(PartnerCategory.entries.toSet(), PartnerFixtures.byCategory.keys)
        assertTrue(PartnerFixtures.byCategory.values.all { it.isNotEmpty() })
    }

    @Test
    fun speaker_edge_cases_are_present() {
        val aliceSessions = sessions.filter { session -> session.speakers.any { it.id == SpeakerFixtures.alice.id } }
        assertEquals(listOf(SessionFixtures.composeDeepDive, SessionFixtures.webRendering), aliceSessions)
        assertTrue(sessions.any { it.speakers.size >= 2 })

        val aliceSocials = SpeakerFixtures.alice.socials.orEmpty().map { it.type }.toSet()
        assertEquals(SocialType.entries.toSet(), aliceSocials)

        val farid = SpeakerFixtures.farid
        assertNull(farid.company)
        assertNull(farid.bio)
        assertNull(farid.photoUrl)
        assertNull(farid.city)
        assertNull(farid.socials)
    }

    @Test
    fun only_lunch_has_no_open_feedback_form_id() {
        assertEquals(listOf(SessionFixtures.lunch), sessions.filter { it.openFeedbackFormId == null })
        sessions.filter { it != SessionFixtures.lunch }.forEach { session ->
            assertTrue(!session.openFeedbackFormId.isNullOrEmpty(), session.id)
        }
    }

    @Test
    fun agenda_keeps_six_sessions_on_day_one_and_four_on_day_two() {
        val agenda = Agenda.Builder().run {
            this.sessions = SessionFixtures.all
            build()
        }
        assertEquals(6, agenda.days.getValue(1).sessions.size)
        assertEquals(4, agenda.days.getValue(2).sessions.size)
    }

    @Test
    fun start_date_string_order_is_chronological_order() {
        val byString = sessions.sortedBy { it.scheduleSlot.startDate }
        val byInstant = sessions.sortedBy { it.scheduleSlot.startDateAsEpochMilliseconds }
        assertEquals(byInstant.map { it.scheduleSlot.startDateAsEpochMilliseconds },
            byString.map { it.scheduleSlot.startDateAsEpochMilliseconds })
        assertTrue(sessions.all { it.scheduleSlot.startDate.endsWith("+02:00") })
    }

    @Test
    fun french_and_advanced_selects_exactly_one_session() {
        val frenchAdvanced = sessions.filter {
            it.language == SessionLanguage.FRENCH && it.complexity == Complexity.ADVANCED
        }
        assertEquals(listOf(SessionFixtures.webRendering), frenchAdvanced)

        val frenchCodelabs = sessions.filter {
            it.language == SessionLanguage.FRENCH && it.type == SessionType.CODELAB
        }
        assertTrue(frenchCodelabs.isEmpty())
    }

    @Test
    fun stub_wrappers_are_deterministic() {
        assertEquals(buildSessionStub(), buildSessionStub())
        assertEquals(buildSpeakerStub(), buildSpeakerStub())
        assertEquals(buildPartnerStub(), buildPartnerStub())
        assertEquals(buildScheduleSlotStub(), buildScheduleSlotStub())
    }
}
