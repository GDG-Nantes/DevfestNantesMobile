package com.gdgnantes.devfest.core.testing.snapshot

import com.gdgnantes.devfest.core.model.Complexity
import com.gdgnantes.devfest.core.model.Room
import com.gdgnantes.devfest.core.model.ScheduleSlot
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.SessionLanguage
import com.gdgnantes.devfest.core.model.SessionType
import com.gdgnantes.devfest.core.model.Speaker
import kotlin.test.Test
import kotlin.test.assertEquals

class AgendaSnapshotDecoderTest {

    private val sample = """
        [
          {
            "id": "day-1-open",
            "title": "Opening",
            "type": null,
            "language": null,
            "complexity": null,
            "room": null,
            "startDate": "2025-10-16T08:00:00+02:00",
            "endDate": "2025-10-16T09:00:00+02:00",
            "speakerIds": []
          },
          {
            "id": "talk",
            "title": "A talk",
            "type": "CONFERENCE",
            "language": "FRENCH",
            "complexity": "BEGINNER",
            "room": { "id": "Titan", "name": "Titan", "sortIndex": 1 },
            "startDate": "2025-10-16T10:00:00+02:00",
            "endDate": "2025-10-16T10:50:00+02:00",
            "speakerIds": ["s1", "s2"]
          }
        ]
    """.trimIndent()

    @Test
    fun sessions_decode_a_roomless_untyped_session_and_a_session_with_a_room() {
        val expected = listOf(
            Session(
                id = "day-1-open",
                title = "Opening",
                scheduleSlot = ScheduleSlot(
                    endDate = "2025-10-16T09:00:00+02:00",
                    startDate = "2025-10-16T08:00:00+02:00",
                ),
            ),
            Session(
                id = "talk",
                title = "A talk",
                type = SessionType.CONFERENCE,
                language = SessionLanguage.FRENCH,
                complexity = Complexity.BEGINNER,
                room = Room(id = "Titan", name = "Titan", sortIndex = 1),
                scheduleSlot = ScheduleSlot(
                    endDate = "2025-10-16T10:50:00+02:00",
                    startDate = "2025-10-16T10:00:00+02:00",
                ),
                speakers = listOf(Speaker(id = "s1"), Speaker(id = "s2")),
            ),
        )

        assertEquals(expected, AgendaSnapshot.sessions(sample))
    }

    @Test
    fun speakers_and_rooms_decode_into_models() {
        assertEquals(
            listOf(Speaker(id = "s1", name = "Ada")),
            AgendaSnapshot.speakers("""[{"id": "s1", "name": "Ada"}]"""),
        )
        assertEquals(
            listOf(Room(id = "Tour Bretagne", name = "Tour Bretagne", sortIndex = 2147483647)),
            AgendaSnapshot.rooms("""[{"id": "Tour Bretagne", "name": "Tour Bretagne", "sortIndex": 2147483647}]"""),
        )
    }
}
