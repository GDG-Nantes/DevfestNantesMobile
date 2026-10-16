package com.gdgnantes.devfest.feature.sessiondetail

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Plain JVM test: [FeedbackFormViewModel] depends on the [FeedbackFlags] seam (D-23), so no
 * Firebase initialization and no Robolectric are needed. The ViewModel launches no coroutine, so
 * no dispatcher rule is needed either.
 */
class FeedbackFormViewModelTest {
    private val enabledKey = "openfeedback_enabled"
    private val fallbackKey = "openfeedback_fallback_requested_android"

    private fun viewModel(
        flags: FakeFeedbackFlags,
        enabled: Boolean = true
    ) = FeedbackFormViewModel(flags, OpenFeedbackConfig(enabled, "devfest-test"))

    private fun flags(
        openFeedback: Boolean,
        fallback: Boolean
    ) = FakeFeedbackFlags(mutableMapOf(enabledKey to openFeedback, fallbackKey to fallback))

    @Test
    fun config_disabled_turns_both_flags_off() {
        val viewModel = viewModel(flags(openFeedback = true, fallback = true), enabled = false)

        assertFalse(viewModel.isOpenFeedbackEnabled.value)
        assertFalse(viewModel.isOpenfeedbackFallbackRequested.value)
    }

    @Test
    fun enabled_and_remote_on_turns_openfeedback_on_without_fallback() {
        val viewModel = viewModel(flags(openFeedback = true, fallback = false))

        assertTrue(viewModel.isOpenFeedbackEnabled.value)
        assertFalse(viewModel.isOpenfeedbackFallbackRequested.value)
    }

    @Test
    fun enabled_and_both_remote_flags_on_requests_the_fallback() {
        val viewModel = viewModel(flags(openFeedback = true, fallback = true))

        assertTrue(viewModel.isOpenFeedbackEnabled.value)
        assertTrue(viewModel.isOpenfeedbackFallbackRequested.value)
    }

    // Characterizes current behavior: the fallback flag is read independently of the openfeedback flag.
    @Test
    fun fallback_flag_does_not_require_the_openfeedback_flag() {
        val viewModel = viewModel(flags(openFeedback = false, fallback = true))

        assertFalse(viewModel.isOpenFeedbackEnabled.value)
        assertTrue(viewModel.isOpenfeedbackFallbackRequested.value)
    }

    @Test
    fun missing_remote_keys_read_as_false() {
        val viewModel = viewModel(FakeFeedbackFlags(mutableMapOf()))

        assertFalse(viewModel.isOpenFeedbackEnabled.value)
        assertFalse(viewModel.isOpenfeedbackFallbackRequested.value)
    }

    @Test
    fun reads_the_production_remote_keys() {
        val flags = flags(openFeedback = true, fallback = true)

        viewModel(flags)

        assertEquals(setOf("openfeedback_enabled", "openfeedback_fallback_requested_android"), flags.readKeys.toSet())
    }

    @Test
    fun init_starts_exactly_one_fetch() {
        val flags = flags(openFeedback = true, fallback = true)

        viewModel(flags)

        assertEquals(1, flags.fetchCount)
    }

    @Test
    fun successful_fetch_reapplies_the_remote_values() {
        val flags = flags(openFeedback = false, fallback = false)
        val viewModel = viewModel(flags)
        assertFalse(viewModel.isOpenFeedbackEnabled.value)
        assertFalse(viewModel.isOpenfeedbackFallbackRequested.value)

        flags.values[enabledKey] = true
        flags.values[fallbackKey] = true
        flags.completeFetch(successful = true)

        assertTrue(viewModel.isOpenFeedbackEnabled.value)
        assertTrue(viewModel.isOpenfeedbackFallbackRequested.value)
    }

    @Test
    fun failed_fetch_keeps_the_values_read_at_init() {
        val flags = flags(openFeedback = true, fallback = false)
        val viewModel = viewModel(flags)

        flags.values[enabledKey] = false
        flags.values[fallbackKey] = true
        flags.completeFetch(successful = false)

        assertTrue(viewModel.isOpenFeedbackEnabled.value)
        assertFalse(viewModel.isOpenfeedbackFallbackRequested.value)
    }

    // T-5-16: the local kill switch wins over any remote value, also after a fetch.
    @Test
    fun disabled_config_stays_off_after_a_successful_fetch() {
        val flags = flags(openFeedback = true, fallback = true)
        val viewModel = viewModel(flags, enabled = false)

        flags.completeFetch(successful = true)

        assertFalse(viewModel.isOpenFeedbackEnabled.value)
        assertFalse(viewModel.isOpenfeedbackFallbackRequested.value)
    }

    @Test
    fun project_id_is_passed_through() {
        val viewModel = viewModel(flags(openFeedback = true, fallback = true))

        assertEquals("devfest-test", viewModel.openFeedbackProjectId)
    }

    private class FakeFeedbackFlags(
        val values: MutableMap<String, Boolean>
    ) : FeedbackFlags {
        val readKeys = mutableListOf<String>()
        var fetchCount = 0
            private set
        private var onComplete: ((Boolean, Boolean) -> Unit)? = null

        override fun getBoolean(key: String): Boolean {
            readKeys += key
            return values[key] ?: false
        }

        override fun fetchAndActivate(onComplete: (successful: Boolean, activated: Boolean) -> Unit) {
            fetchCount++
            this.onComplete = onComplete
        }

        fun completeFetch(successful: Boolean) {
            checkNotNull(onComplete) { "no fetch was started" }(successful, successful)
        }
    }
}
