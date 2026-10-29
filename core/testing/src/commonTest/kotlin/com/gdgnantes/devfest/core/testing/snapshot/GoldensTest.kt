package com.gdgnantes.devfest.core.testing.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GoldensTest {

    @Test
    fun agenda_golden_round_trips_in_the_readme_format() {
        val golden =
            AgendaGolden(
                source = "android-v1.0",
                timeZone = "UTC",
                days =
                    listOf(
                        GoldenDay(
                            dayIndex = 1,
                            date = "2025-10-16T00:00:00Z",
                            slots = listOf(GoldenSlot("2025-10-16T06:00:00Z", listOf("a", "b"))),
                        ),
                    ),
            )

        val text = Goldens.encode(golden)

        assertTrue(text.endsWith("}\n"))
        assertTrue(text.contains("\n  \"source\": \"android-v1.0\""))
        assertEquals(golden, Goldens.decodeAgenda(text))
    }

    @Test
    fun filtering_golden_round_trips() {
        val golden =
            FilteringGolden(
                source = "android-v1.0",
                cases = listOf(GoldenCase("no_filter", listOf("a", "b")), GoldenCase("none", emptyList())),
            )

        assertEquals(golden, Goldens.decodeFiltering(Goldens.encode(golden)))
    }

    @Test
    fun speaker_goldens_round_trip() {
        val android =
            AndroidSpeakerIndexGolden("android-v1.0", listOf(GoldenLetterIndex("a", 0), GoldenLetterIndex("b", 3)))
        val ios = IosSpeakerIndexGolden("ios-v1.0", listOf(GoldenLetterGroup("A", listOf("s1", "s2"))))

        assertEquals(android, Goldens.decodeAndroidSpeakerIndex(Goldens.encode(android)))
        assertEquals(ios, Goldens.decodeIosSpeakerIndex(Goldens.encode(ios)))
    }

    @Test
    fun filter_cases_decode_a_two_case_sample() {
        val sample =
            """
            {
              "bookmarkedSessionIds": ["s1"],
              "cases": [
                { "name": "no_filter", "filters": [] },
                { "name": "language_french", "filters": [{ "type": "LANGUAGE", "value": "FRENCH" }] }
              ]
            }
            """.trimIndent()

        assertEquals(
            FilterCases(
                bookmarkedSessionIds = listOf("s1"),
                cases =
                    listOf(
                        FilterCase("no_filter", emptyList()),
                        FilterCase("language_french", listOf(FilterCaseFilter("LANGUAGE", "FRENCH"))),
                    ),
            ),
            FilterCases.decode(sample),
        )
    }
}
