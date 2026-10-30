package com.gdgnantes.devfest.feature.speakers.list

import com.gdgnantes.devfest.core.domain.speakers.SpeakerIndexUseCase
import com.gdgnantes.devfest.core.model.Speaker
import com.gdgnantes.devfest.core.model.stubs.SpeakerFixtures
import com.gdgnantes.devfest.core.testing.FakeDevFestNantesStore
import com.gdgnantes.devfest.core.testing.MainDispatcherRule
import com.gdgnantes.devfest.core.testing.NoOpPerformanceMonitoring
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
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
 * `SpeakersViewModel.firstLetterIndexes` skips. The ViewModel reports a non-fatal exception through
 * Timber (the app's trees forward WARN entries that carry a throwable to Crashlytics
 * `recordException`) once per data load, never from the composition-time index read (WR-04).
 *
 * Kept apart from the v1.0 characterizations, which only had wiring edits (D-18).
 */
class SpeakerIndexEmptyNameTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule(StandardTestDispatcher())

    private val store = FakeDevFestNantesStore()

    // Created lazily so it is built after the rule has installed the test Main dispatcher.
    private val viewModel by lazy {
        SpeakersViewModel(store, NoOpPerformanceMonitoring(), SpeakerIndexUseCase())
    }

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

    /** Pushes a data load through the store and lets the ViewModel's collector process it. */
    private fun pushSpeakers(speakers: List<Speaker>) =
        runTest {
            viewModel // starts the collector on the fixture speakers
            advanceUntilIdle()
            store.speakersState.value = speakers
            advanceUntilIdle()
        }

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
    fun the_loaded_index_skips_the_empty_name() {
        pushSpeakers(speakersWithAnEmptyName)

        assertEquals(mapOf('a' to 0, 'b' to 2), viewModel.letterIndexes.value)
    }

    @Test
    fun empty_speaker_name_is_reported_as_a_non_fatal() {
        pushSpeakers(speakersWithAnEmptyName)

        val warnings = reports.filter { it.priority == WARN }
        assertEquals(1, warnings.size)
        val throwable = assertNotNull(warnings.single().throwable)
        assertIs<IllegalStateException>(throwable)
        val message = throwable.message.orEmpty()
        assertTrue("speaker-empty" in message, "the report names the empty-name speaker id")
        assertTrue("Alice" !in message && "bob" !in message, "no speaker name goes into the report")
    }

    @Test
    fun reading_the_index_again_does_not_repeat_the_report() {
        pushSpeakers(speakersWithAnEmptyName)

        // What a recomposition does: read the index again, and compute it from the same list.
        repeat(3) { viewModel.letterIndexes.value }
        repeat(3) { viewModel.firstLetterIndexes(speakersWithAnEmptyName) }

        assertEquals(1, reports.count { it.priority == WARN })
    }

    @Test
    fun a_new_data_load_with_an_empty_name_is_reported_again() {
        pushSpeakers(speakersWithAnEmptyName)
        pushSpeakers(speakersWithAnEmptyName.drop(1))

        assertEquals(2, reports.count { it.priority == WARN })
    }

    @Test
    fun a_list_without_an_empty_name_reports_nothing() {
        pushSpeakers(SpeakerFixtures.all.reversed())

        assertTrue(reports.none { it.throwable != null }, "no non-fatal for regular names")
    }

    private data class Report(val priority: Int, val throwable: Throwable?)

    private companion object {
        const val WARN = 5 // android.util.Log.WARN
    }
}
