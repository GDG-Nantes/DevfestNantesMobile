package com.gdgnantes.devfest.feature.speakers.list

import com.gdgnantes.devfest.core.model.stubs.SpeakerFixtures
import com.gdgnantes.devfest.core.testing.snapshot.AgendaSnapshot
import com.gdgnantes.devfest.core.testing.snapshot.AndroidSpeakerIndexGolden
import com.gdgnantes.devfest.core.testing.snapshot.GoldenFiles
import com.gdgnantes.devfest.core.testing.snapshot.GoldenLetterIndex
import com.gdgnantes.devfest.core.testing.snapshot.Goldens
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * D-11 characterization of the v1.0 Android speaker A-Z index, `List<Speaker>.getMapOfFirstUniqueCharIndex()`,
 * written against the unchanged code: on the recorded snapshot speakers (store order) it yields
 * `golden/android-speaker-index.json`, the oracle the shared speaker index must reproduce.
 *
 * Known v1.0 debt, deliberately not tested: a speaker with an empty name makes `first()` throw
 * `NoSuchElementException`. No such name exists in the data. Per D-20 the shared index skips it and
 * Android reports a non-fatal instead (07-18).
 */
class SpeakerIndexCharacterizationTest {
    @Test
    fun the_snapshot_speakers_index_matches_the_golden() {
        val speakers = AgendaSnapshot.speakers(GoldenFiles.read("agenda/speakers.snapshot.json"))

        val index = speakers.getMapOfFirstUniqueCharIndex()
        val golden =
            AndroidSpeakerIndexGolden(
                source = "android-v1.0",
                firstIndexes = index.entries.map { GoldenLetterIndex(it.key.toString(), it.value) },
            )

        assertTrue(index.isNotEmpty())
        assertEquals(0, index.values.first(), "the first speaker opens the index")
        assertEquals(index.values.sorted(), index.values.toList(), "first indexes follow list order")
        GoldenFiles.assertMatches("agenda/golden/android-speaker-index.json", Goldens.encode(golden))
    }

    @Test
    fun the_fixture_speakers_index_each_first_letter_at_its_first_position() {
        val index = SpeakerFixtures.all.getMapOfFirstUniqueCharIndex()

        assertEquals(mapOf('a' to 0, 'b' to 1, 'c' to 2, 'd' to 3, 'e' to 4, 'f' to 5), index)
    }

    @Test
    fun a_repeated_first_letter_keeps_its_first_index_and_case_is_ignored() {
        val speakers =
            listOf(SpeakerFixtures.bruno, SpeakerFixtures.alice, SpeakerFixtures.bruno.copy(name = "BEA"))

        assertEquals(mapOf('b' to 0, 'a' to 1), speakers.getMapOfFirstUniqueCharIndex())
    }
}
