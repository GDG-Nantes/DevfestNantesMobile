package com.gdgnantes.devfest.core.testing.snapshot

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** `golden/android-agenda.json` and `golden/ios-agenda-<Area>_<City>.json`. */
@Serializable
data class AgendaGolden(
    val source: String,
    val timeZone: String,
    val days: List<GoldenDay>,
)

@Serializable
data class GoldenDay(
    val dayIndex: Int,
    val date: String,
    val slots: List<GoldenSlot>,
)

/** [start] is a UTC ISO instant; [sessionIds] are canonicalized (see the README). */
@Serializable
data class GoldenSlot(
    val start: String,
    val sessionIds: List<String>,
)

/** `golden/android-filtering.json` and `golden/ios-filtering.json`. */
@Serializable
data class FilteringGolden(
    val source: String,
    val cases: List<GoldenCase>,
)

@Serializable
data class GoldenCase(
    val name: String,
    val sessionIds: List<String>,
)

/** `golden/android-speaker-index.json`. */
@Serializable
data class AndroidSpeakerIndexGolden(
    val source: String,
    val firstIndexes: List<GoldenLetterIndex>,
)

@Serializable
data class GoldenLetterIndex(
    val key: String,
    val index: Int,
)

/** `golden/ios-speaker-index.json`. */
@Serializable
data class IosSpeakerIndexGolden(
    val source: String,
    val letters: List<GoldenLetterGroup>,
)

@Serializable
data class GoldenLetterGroup(
    val letter: String,
    val speakerIds: List<String>,
)

/** Encodes and decodes the golden files in the README format (2-space indent, trailing newline). */
object Goldens {
    private val json =
        Json {
            prettyPrint = true
            prettyPrintIndent = "  "
            encodeDefaults = true
        }

    fun encode(golden: AgendaGolden): String = json.encodeToString(AgendaGolden.serializer(), golden) + "\n"

    fun encode(golden: FilteringGolden): String = json.encodeToString(FilteringGolden.serializer(), golden) + "\n"

    fun encode(golden: AndroidSpeakerIndexGolden): String =
        json.encodeToString(AndroidSpeakerIndexGolden.serializer(), golden) + "\n"

    fun encode(golden: IosSpeakerIndexGolden): String =
        json.encodeToString(IosSpeakerIndexGolden.serializer(), golden) + "\n"

    fun decodeAgenda(text: String): AgendaGolden = json.decodeFromString(AgendaGolden.serializer(), text)

    fun decodeFiltering(text: String): FilteringGolden = json.decodeFromString(FilteringGolden.serializer(), text)

    fun decodeAndroidSpeakerIndex(text: String): AndroidSpeakerIndexGolden =
        json.decodeFromString(AndroidSpeakerIndexGolden.serializer(), text)

    fun decodeIosSpeakerIndex(text: String): IosSpeakerIndexGolden =
        json.decodeFromString(IosSpeakerIndexGolden.serializer(), text)
}
