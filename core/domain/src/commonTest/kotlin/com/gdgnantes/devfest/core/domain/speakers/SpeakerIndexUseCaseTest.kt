package com.gdgnantes.devfest.core.domain.speakers

import com.gdgnantes.devfest.core.model.Speaker
import com.gdgnantes.devfest.core.model.stubs.SpeakerFixtures
import com.gdgnantes.devfest.core.testing.snapshot.AgendaSnapshot
import com.gdgnantes.devfest.core.testing.snapshot.GoldenFiles
import com.gdgnantes.devfest.core.testing.snapshot.Goldens
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The shared A-Z speaker index against the v1.0 Android and iOS goldens and the edge rules. */
class SpeakerIndexUseCaseTest {
    private val useCase = SpeakerIndexUseCase()
    private val snapshot: List<Speaker> by lazy {
        AgendaSnapshot.speakers(GoldenFiles.read("agenda/speakers.snapshot.json"))
    }

    private fun speaker(name: String): Speaker = SpeakerFixtures.alice.copy(id = "id-$name", name = name)

    @Test
    fun the_ios_letter_groups_golden_is_reproduced() {
        val golden = Goldens.decodeIosSpeakerIndex(GoldenFiles.read("agenda/golden/ios-speaker-index.json"))

        val groups = useCase.letterGroups(snapshot)

        assertTrue(groups.isNotEmpty())
        assertEquals(golden.letters.map { it.letter }, groups.map { it.letter })
        assertEquals(golden.letters.map { it.speakerIds }, groups.map { group -> group.speakers.map { it.id } })
    }

    @Test
    fun the_android_first_indexes_golden_is_reproduced_with_lowercased_letters() {
        val golden = Goldens.decodeAndroidSpeakerIndex(GoldenFiles.read("agenda/golden/android-speaker-index.json"))

        val indexes = useCase.firstIndexes(snapshot)

        assertTrue(indexes.isNotEmpty())
        assertEquals(golden.firstIndexes.map { it.key }, indexes.map { it.letter.lowercase() })
        assertEquals(golden.firstIndexes.map { it.index }, indexes.map { it.index })
    }

    @Test
    fun the_fixture_speakers_give_one_group_and_one_index_per_first_letter() {
        val groups = useCase.letterGroups(SpeakerFixtures.all)
        val indexes = useCase.firstIndexes(SpeakerFixtures.all)

        assertEquals(listOf("A", "B", "C", "D", "E", "F"), groups.map { it.letter })
        assertEquals(
            listOf("speaker-alice", "speaker-bruno", "speaker-chloe", "speaker-david", "speaker-emma", "speaker-farid"),
            groups.map { it.speakers.single().id }
        )
        assertEquals(
            listOf(
                SpeakerLetterIndex("A", 0),
                SpeakerLetterIndex("B", 1),
                SpeakerLetterIndex("C", 2),
                SpeakerLetterIndex("D", 3),
                SpeakerLetterIndex("E", 4),
                SpeakerLetterIndex("F", 5)
            ),
            indexes
        )
    }

    @Test
    fun an_empty_input_gives_empty_results() {
        assertEquals(emptyList(), useCase.letterGroups(emptyList()))
        assertEquals(emptyList(), useCase.firstIndexes(emptyList()))
    }

    @Test
    fun a_lowercase_initial_groups_with_its_uppercase_letter() {
        val speakers = listOf(speaker("bea"), speaker("Anna"), speaker("Bob"))

        val groups = useCase.letterGroups(speakers)

        assertEquals(listOf("A", "B"), groups.map { it.letter })
        assertEquals(listOf("bea", "Bob"), groups[1].speakers.map { it.name })
        assertEquals(listOf(SpeakerLetterIndex("B", 0), SpeakerLetterIndex("A", 1)), useCase.firstIndexes(speakers))
    }

    @Test
    fun letters_are_sorted_while_speakers_and_first_indexes_keep_the_input_order() {
        val speakers = listOf(speaker("Zoe"), speaker("Adam"), speaker("Zack"), speaker("Abel"))

        val groups = useCase.letterGroups(speakers)

        assertEquals(listOf("A", "Z"), groups.map { it.letter })
        assertEquals(listOf("Adam", "Abel"), groups[0].speakers.map { it.name })
        assertEquals(listOf("Zoe", "Zack"), groups[1].speakers.map { it.name })
        assertEquals(listOf(SpeakerLetterIndex("Z", 0), SpeakerLetterIndex("A", 1)), useCase.firstIndexes(speakers))
    }

    @Test
    fun an_empty_name_gives_the_empty_letter_without_throwing() {
        val speakers = listOf(speaker(""), speaker("Anna"))

        assertEquals(listOf("", "A"), useCase.letterGroups(speakers).map { it.letter })
        assertEquals(listOf(SpeakerLetterIndex("", 0), SpeakerLetterIndex("A", 1)), useCase.firstIndexes(speakers))
    }
}
