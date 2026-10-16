package com.gdgnantes.devfest.core.model.stubs

import com.gdgnantes.devfest.core.model.Category
import com.gdgnantes.devfest.core.model.Complexity
import com.gdgnantes.devfest.core.model.Room
import com.gdgnantes.devfest.core.model.ScheduleSlot
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.SessionLanguage
import com.gdgnantes.devfest.core.model.SessionType
import com.gdgnantes.devfest.core.model.Speaker

/**
 * Fixed, hand-written sessions shared by previews, the mocked store and tests.
 *
 * Derived facts that tests rely on:
 * - French: welcome, webRendering, gitQuickie, closing. English: composeDeepDive,
 *   kubernetesLab, aiKeynote, securityTalk.
 * - ADVANCED: composeDeepDive, webRendering. French + ADVANCED: webRendering only.
 *   CODELAB + French: none.
 * - Room titan: composeDeepDive, closing. Titan or belem: composeDeepDive, webRendering, closing.
 * - Day 1 has 6 sessions, day 2 has 4. All dates use the backend's +02:00 offset form, so the
 *   start date string order is the chronological order.
 */
object SessionFixtures {
    private const val DAY_ONE_DATE = "2025-10-16"
    private const val DAY_TWO_DATE = "2025-10-17"

    private fun room(id: String): Room = roomStubs.first { it.id == id }

    private fun category(id: String): Category = categoryStubs.first { it.id == id }

    private fun slot(date: String, start: String, end: String): ScheduleSlot =
        ScheduleSlot(
            endDate = "${date}T$end:00+02:00",
            startDate = "${date}T$start:00+02:00"
        )

    val welcome =
        Session(
            id = "session-welcome",
            abstract = LOREM_IPSUM,
            category = null,
            language = SessionLanguage.FRENCH,
            complexity = null,
            openFeedbackFormId = "feedback-welcome",
            room = room("jules_verne"),
            scheduleSlot = slot(DAY_ONE_DATE, "09:00", "09:30"),
            speakers = listOf(SpeakerFixtures.bruno),
            title = "Welcome to DevFest Nantes",
            type = SessionType.OPENING
        )

    val composeDeepDive =
        Session(
            id = "session-compose-deep-dive",
            abstract = LOREM_IPSUM,
            category = category("mobile_iot"),
            language = SessionLanguage.ENGLISH,
            complexity = Complexity.ADVANCED,
            openFeedbackFormId = "feedback-compose",
            room = room("titan"),
            scheduleSlot = slot(DAY_ONE_DATE, "10:00", "10:45"),
            speakers = listOf(SpeakerFixtures.alice, SpeakerFixtures.chloe),
            title = "Compose deep dive",
            type = SessionType.CONFERENCE
        )

    val webRendering =
        Session(
            id = "session-web-rendering",
            abstract = LOREM_IPSUM,
            category = category("web"),
            language = SessionLanguage.FRENCH,
            complexity = Complexity.ADVANCED,
            openFeedbackFormId = "feedback-web",
            room = room("belem"),
            scheduleSlot = slot(DAY_ONE_DATE, "10:00", "10:45"),
            speakers = listOf(SpeakerFixtures.alice),
            title = "Les secrets du rendu web",
            type = SessionType.CONFERENCE
        )

    val kubernetesLab =
        Session(
            id = "session-kubernetes-lab",
            abstract = LOREM_IPSUM,
            category = category("cloud_devops"),
            language = SessionLanguage.ENGLISH,
            complexity = Complexity.BEGINNER,
            openFeedbackFormId = "feedback-k8s",
            room = room("tour_de_bretagne"),
            scheduleSlot = slot(DAY_ONE_DATE, "10:00", "12:00"),
            speakers = listOf(SpeakerFixtures.david),
            title = "Kubernetes hands-on lab",
            type = SessionType.CODELAB
        )

    val lunch =
        Session(
            id = "session-lunch",
            abstract = LOREM_IPSUM,
            category = null,
            language = null,
            complexity = null,
            openFeedbackFormId = null,
            room = null,
            scheduleSlot = slot(DAY_ONE_DATE, "12:00", "13:30"),
            speakers = emptyList<Speaker>(),
            title = "Lunch",
            type = SessionType.LUNCH
        )

    val gitQuickie =
        Session(
            id = "session-git-quickie",
            abstract = LOREM_IPSUM,
            category = category("languages"),
            language = SessionLanguage.FRENCH,
            complexity = Complexity.INTERMEDIATE,
            openFeedbackFormId = "feedback-git",
            room = room("les_machines"),
            scheduleSlot = slot(DAY_ONE_DATE, "13:30", "13:45"),
            speakers = listOf(SpeakerFixtures.emma),
            title = "Git en 15 minutes",
            type = SessionType.QUICKIE
        )

    val aiKeynote =
        Session(
            id = "session-ai-keynote",
            abstract = LOREM_IPSUM,
            category = category("bigdata_ai"),
            language = SessionLanguage.ENGLISH,
            complexity = Complexity.INTERMEDIATE,
            openFeedbackFormId = "feedback-ai",
            room = room("jules_verne"),
            scheduleSlot = slot(DAY_TWO_DATE, "09:00", "09:45"),
            speakers = listOf(SpeakerFixtures.david, SpeakerFixtures.bruno),
            title = "AI beyond the hype",
            type = SessionType.KEYNOTE
        )

    val securityTalk =
        Session(
            id = "session-security",
            abstract = LOREM_IPSUM,
            category = category("security"),
            language = SessionLanguage.ENGLISH,
            complexity = Complexity.INTERMEDIATE,
            openFeedbackFormId = "feedback-security",
            room = room("Hangar"),
            scheduleSlot = slot(DAY_TWO_DATE, "10:00", "10:45"),
            speakers = listOf(SpeakerFixtures.chloe),
            title = "Securing mobile apps",
            type = SessionType.CONFERENCE
        )

    val coffeeBreak =
        Session(
            id = "session-coffee-break",
            abstract = LOREM_IPSUM,
            category = null,
            language = null,
            complexity = null,
            openFeedbackFormId = "feedback-break",
            room = room("l_atelier"),
            scheduleSlot = slot(DAY_TWO_DATE, "10:45", "11:00"),
            speakers = emptyList<Speaker>(),
            title = "Coffee break",
            type = SessionType.BREAK
        )

    val closing =
        Session(
            id = "session-closing",
            abstract = LOREM_IPSUM,
            category = category("ux_ui"),
            language = SessionLanguage.FRENCH,
            complexity = Complexity.BEGINNER,
            openFeedbackFormId = "feedback-closing",
            room = room("titan"),
            scheduleSlot = slot(DAY_TWO_DATE, "17:00", "17:30"),
            speakers = listOf(SpeakerFixtures.farid),
            title = "Le mot de la fin",
            type = null
        )

    val all: List<Session> =
        listOf(
            welcome,
            composeDeepDive,
            webRendering,
            kubernetesLab,
            lunch,
            gitQuickie,
            aiKeynote,
            securityTalk,
            coffeeBreak,
            closing
        )
}
