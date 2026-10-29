package com.gdgnantes.devfest.core.data

import com.gdgnantes.devfest.core.model.SessionFilter
import com.rickclephas.kmp.nativecoroutines.NativeCoroutinesState
import kotlinx.coroutines.flow.StateFlow

/**
 * Holds the agenda filters selected by the user. One instance per process, so every screen that
 * reads the filters sees the same set.
 *
 * The Android implementation persists under `SHARED_PREFERENCES_KEY_AGENDA_FILTERS` (07-16); the iOS
 * implementation persists under its own `NSUserDefaults` keys (07-11, D-15). Both encode filters with
 * the shared filter codec.
 */
interface SessionFiltersStore {
    @NativeCoroutinesState
    val filters: StateFlow<Set<SessionFilter>>

    fun setFilters(filters: Set<SessionFilter>)
}
