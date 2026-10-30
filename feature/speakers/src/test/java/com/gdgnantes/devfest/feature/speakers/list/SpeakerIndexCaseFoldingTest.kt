package com.gdgnantes.devfest.feature.speakers.list

import com.gdgnantes.devfest.core.domain.speakers.SpeakerIndexUseCase
import com.gdgnantes.devfest.core.model.Speaker
import com.gdgnantes.devfest.core.model.stubs.SpeakerFixtures
import com.gdgnantes.devfest.core.testing.FakeDevFestNantesStore
import com.gdgnantes.devfest.core.testing.MainDispatcherRule
import com.gdgnantes.devfest.core.testing.NoOpPerformanceMonitoring
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

/**
 * WR-01: the scroller map and the row header check derive the letter key from the same function, so
 * a first letter whose upper-casing changes the length (`ß` becomes `SS`) neither overwrites another
 * letter's entry nor loses its own header.
 */
class SpeakerIndexCaseFoldingTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val viewModel =
        SpeakersViewModel(FakeDevFestNantesStore(), NoOpPerformanceMonitoring(), SpeakerIndexUseCase())

    private fun speakers(vararg names: String) =
        names.mapIndexed { i, name -> SpeakerFixtures.alice.copy(id = "speaker-$i", name = name) }

    /** The indexes of the rows that `SpeakersList` shows a letter header for. */
    private fun headerIndexes(speakers: List<Speaker>): List<Int> {
        val map = viewModel.firstLetterIndexes(speakers)
        return speakers.indices.filter { speakerLetterKey(speakers[it].name)?.let { key -> map[key] } == it }
    }

    @Test
    fun a_sharp_s_first_letter_keeps_its_own_key_after_an_s() {
        val speakers = speakers("Sam", "ßeta")

        assertEquals(mapOf('s' to 0, 'ß' to 1), viewModel.firstLetterIndexes(speakers))
        assertEquals(listOf(0, 1), headerIndexes(speakers))
    }

    @Test
    fun a_sharp_s_first_letter_does_not_take_the_s_entry_before_an_s() {
        val speakers = speakers("ßeta", "Sam")

        assertEquals(mapOf('ß' to 0, 's' to 1), viewModel.firstLetterIndexes(speakers))
        assertEquals(listOf(0, 1), headerIndexes(speakers))
    }

    @Test
    fun letter_keys_are_lowercased_first_characters() {
        assertEquals('a', speakerLetterKey("Alice"))
        assertEquals('ß', speakerLetterKey("ßeta"))
        assertEquals(null, speakerLetterKey(""))
    }
}
