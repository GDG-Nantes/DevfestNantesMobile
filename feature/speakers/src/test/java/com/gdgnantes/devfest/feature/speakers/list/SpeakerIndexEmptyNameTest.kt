package com.gdgnantes.devfest.feature.speakers.list

import com.gdgnantes.devfest.core.domain.speakers.SpeakerIndexUseCase
import com.gdgnantes.devfest.core.model.stubs.SpeakerFixtures
import com.gdgnantes.devfest.core.testing.FakeDevFestNantesStore
import com.gdgnantes.devfest.core.testing.MainDispatcherRule
import com.gdgnantes.devfest.core.testing.NoOpPerformanceMonitoring
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import timber.log.Timber
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * D-20 (accepted behavior change): a speaker with an empty name crashed the v1.0 Android scroller
 * map with `NoSuchElementException`. The shared index yields an empty letter that
 * `SpeakersViewModel.firstLetterIndexes` skips, reporting a non-fatal exception through Timber (the
 * app's trees forward WARN entries that carry a throwable to Crashlytics `recordException`).
 *
 * Kept apart from the v1.0 characterizations, which only had wiring edits (D-18).
 */
class SpeakerIndexEmptyNameTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val viewModel =
        SpeakersViewModel(FakeDevFestNantesStore(), NoOpPerformanceMonitoring(), SpeakerIndexUseCase())

    private val reports = mutableListOf<Report>()

    private val recordingTree =
        object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                reports += Report(priority, t)
            }
        }

    private val speakersWithAnEmptyName =
        listOf(
            SpeakerFixtures.alice.copy(name = "Alice"),
            SpeakerFixtures.bruno.copy(id = "speaker-empty", name = ""),
            SpeakerFixtures.chloe.copy(name = "bob"),
        )

    @Before
    fun plantRecordingTree() {
        Timber.plant(recordingTree)
    }

    @After
    fun uprootRecordingTree() {
        Timber.uproot(recordingTree)
    }

    @Test
    fun empty_speaker_name_is_skipped_without_crash() {
        val indexes = viewModel.firstLetterIndexes(speakersWithAnEmptyName)

        assertEquals(mapOf('a' to 0, 'b' to 2), indexes)
        assertEquals(listOf('a', 'b'), indexes.keys.toList())
    }

    @Test
    fun empty_speaker_name_is_reported_as_a_non_fatal() {
        viewModel.firstLetterIndexes(speakersWithAnEmptyName)

        val warnings = reports.filter { it.priority == WARN }
        assertEquals(1, warnings.size)
        val throwable = assertNotNull(warnings.single().throwable)
        assertIs<IllegalStateException>(throwable)
        val message = throwable.message.orEmpty()
        assertTrue("speaker-empty" in message, "the report names the empty-name speaker id")
        assertTrue("Alice" !in message && "bob" !in message, "no speaker name goes into the report")
    }

    @Test
    fun a_list_without_an_empty_name_reports_nothing() {
        viewModel.firstLetterIndexes(SpeakerFixtures.all)

        assertTrue(reports.none { it.throwable != null }, "no non-fatal for regular names")
    }

    private data class Report(val priority: Int, val throwable: Throwable?)

    private companion object {
        const val WARN = 5 // android.util.Log.WARN
    }
}
