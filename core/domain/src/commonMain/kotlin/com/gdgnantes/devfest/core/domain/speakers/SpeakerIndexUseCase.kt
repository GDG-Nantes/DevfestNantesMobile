package com.gdgnantes.devfest.core.domain.speakers

import com.gdgnantes.devfest.core.model.Speaker

/** Speakers whose name starts with [letter], in input (store) order. */
data class SpeakerLetterGroup(
    val letter: String,
    val speakers: List<Speaker>
)

/** The position of the first speaker whose name starts with [letter] in the input list. */
data class SpeakerLetterIndex(
    val letter: String,
    val index: Int
)

/**
 * Shared A-Z speaker index (SHARED-04). v1.0 Android and v1.0 iOS agree on the real data, so one
 * definition serves both:
 *
 * - The letter of a speaker is `name.take(1).uppercase()`.
 * - [letterGroups] (the iOS list) returns one group per letter, sorted by letter, with the speakers
 *   of a letter kept in input order.
 * - [firstIndexes] (the Android scroller) returns, in order of first appearance, the index of the
 *   first speaker of each letter. Android derives its lowercase `Char` key from the letter.
 *
 * Edge cases absent from the data, documented rather than reproduced:
 *
 * - An empty name gives the empty letter `""`. v1.0 Android threw `NoSuchElementException` and v1.0
 *   iOS added an empty letter. The Android caller skips empty letters and reports a non-fatal
 *   exception instead of crashing (D-20, accepted behavior change).
 * - An initial outside the Basic Multilingual Plane is one UTF-16 unit here but one grapheme in
 *   Swift, so such a letter would differ between the platforms.
 */
class SpeakerIndexUseCase {
    fun letterGroups(speakers: List<Speaker>): List<SpeakerLetterGroup> =
        speakers
            .groupBy { letterOf(it) }
            .entries
            .sortedBy { it.key }
            .map { (letter, group) -> SpeakerLetterGroup(letter, group) }

    fun firstIndexes(speakers: List<Speaker>): List<SpeakerLetterIndex> {
        val seen = mutableSetOf<String>()
        val indexes = mutableListOf<SpeakerLetterIndex>()
        speakers.forEachIndexed { index, speaker ->
            val letter = letterOf(speaker)
            if (seen.add(letter)) indexes.add(SpeakerLetterIndex(letter, index))
        }
        return indexes
    }

    private fun letterOf(speaker: Speaker): String = speaker.name.take(1).uppercase()
}
