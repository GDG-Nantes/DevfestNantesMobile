package com.gdgnantes.devfest.androidapp.services

import android.content.SharedPreferences
import androidx.core.content.edit
import com.gdgnantes.devfest.androidapp.utils.SessionFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject

interface SessionFiltersService {
    val filters: StateFlow<Set<SessionFilter>>
    fun setFilters(filters: Set<SessionFilter>)
}

class SessionFiltersServiceImpl @Inject constructor(
    private val sharedPreferences: SharedPreferences
) : SessionFiltersService {
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
        val stringSet =
            filters.map {
                Json.encodeToString(SessionFilter.serializer(), it)
            }.toSet()
        sharedPreferences.edit {
            putStringSet(SHARED_PREFERENCES_KEY_FILTERS, stringSet)
        }
    }

    private fun loadFilters(): Set<SessionFilter> {
        val stringSet =
            sharedPreferences.getStringSet(SHARED_PREFERENCES_KEY_FILTERS, emptySet()) ?: emptySet()
        return stringSet.mapNotNull { filterString ->
            try {
                Json.decodeFromString<SessionFilter>(filterString)
            } catch (e: kotlinx.serialization.SerializationException) {
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
