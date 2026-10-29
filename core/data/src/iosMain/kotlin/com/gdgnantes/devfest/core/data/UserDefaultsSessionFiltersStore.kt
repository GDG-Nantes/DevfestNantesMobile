package com.gdgnantes.devfest.core.data

import com.gdgnantes.devfest.core.model.SessionFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSUserDefaults

/**
 * iOS [SessionFiltersStore] on [NSUserDefaults]: the selected filters are an array of strings under
 * [KEY_FILTERS], one [SessionFilterCodec] JSON per filter (the same bytes as Android).
 *
 * The key is new on purpose (D-15): the v1.0 iOS app stored filters under a legacy key in another
 * JSON format, which this class never reads, migrates or removes. Phase 9 wires this store into the
 * app.
 *
 * Reads return sets, so the persisted array order is not significant. Persisted values are
 * untrusted: anything that is not an array, and any entry that is not a string or does not decode,
 * is skipped. Payloads are never logged.
 */
internal class UserDefaultsSessionFiltersStore(private val defaults: NSUserDefaults) : SessionFiltersStore {
    private val state = MutableStateFlow(load())

    override val filters: StateFlow<Set<SessionFilter>> = state.asStateFlow()

    override fun setFilters(filters: Set<SessionFilter>) {
        state.value = filters
        defaults.setObject(filters.map(SessionFilterCodec::encode), forKey = KEY_FILTERS)
    }

    private fun load(): Set<SessionFilter> =
        defaults.arrayForKey(KEY_FILTERS).orEmpty()
            .mapNotNull { (it as? String)?.let(SessionFilterCodec::decodeOrNull) }
            .toSet()

    internal companion object {
        const val KEY_FILTERS = "devfest.agenda.filters"
    }
}
