package com.gdgnantes.devfest.core.data

import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdgnantes.devfest.core.model.SessionFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.SerializationException
import timber.log.Timber

/**
 * The Android [SessionFiltersStore]: the agenda filters persisted as one JSON string per filter in
 * the `SHARED_PREFERENCES_KEY_AGENDA_FILTERS` string set, the same key and format as v1.0 (D-18).
 *
 * Must stay a single instance so the agenda screen and `HomeViewModel` observe the same filters.
 */
class SharedPreferencesSessionFiltersStore(
    private val sharedPreferences: SharedPreferences
) : SessionFiltersStore {
    companion object {
        private const val SHARED_PREFERENCES_KEY_FILTERS = "SHARED_PREFERENCES_KEY_AGENDA_FILTERS"
    }

    private val _filters: MutableStateFlow<Set<SessionFilter>> = MutableStateFlow(loadFilters())
    override val filters = _filters.asStateFlow()

    override fun setFilters(filters: Set<SessionFilter>) {
        _filters.value = filters
        saveFilters(filters)
    }

    private fun saveFilters(filters: Set<SessionFilter>) {
        val stringSet = filters.map { SessionFilterCodec.encode(it) }.toSet()
        sharedPreferences.edit {
            putStringSet(SHARED_PREFERENCES_KEY_FILTERS, stringSet)
        }
    }

    private fun loadFilters(): Set<SessionFilter> {
        val stringSet =
            sharedPreferences.getStringSet(SHARED_PREFERENCES_KEY_FILTERS, emptySet()) ?: emptySet()
        return stringSet.mapNotNull { filterString ->
            try {
                SessionFilterCodec.decode(filterString)
            } catch (e: SerializationException) {
                Timber.e(
                    e,
                    "SerializationException: Failed to deserialize SessionFilter: $filterString"
                )
                null
            } catch (e: IllegalArgumentException) {
                Timber.e(
                    e,
                    "IllegalArgumentException: Failed to deserialize SessionFilter: $filterString"
                )
                null
            }
        }.toSet()
    }
}
