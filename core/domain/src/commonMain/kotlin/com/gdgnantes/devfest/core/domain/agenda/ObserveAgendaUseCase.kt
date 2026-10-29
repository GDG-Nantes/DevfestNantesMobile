package com.gdgnantes.devfest.core.domain.agenda

import com.gdgnantes.devfest.core.data.BookmarksStore
import com.gdgnantes.devfest.core.data.DevFestNantesStore
import com.gdgnantes.devfest.core.data.SessionFiltersStore
import com.rickclephas.kmp.nativecoroutines.NativeCoroutines
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.datetime.TimeZone

/**
 * The continuous agenda: sessions, active filters and bookmarks combined into the filtered and
 * grouped day schedules (D-01, D-02, D-03).
 *
 * The result equals `groupSessions.group(filterSessions.filter(sessions, filters, bookmarkedIds),
 * dayAssignment, timeZone)` and is re-emitted when any of the three sources emits. It reproduces
 * the v1.0 semantics:
 *
 * - The flow is built inside each [observe] call and never cached, so every new collection reads
 *   `store.sessions` again. A refresh is "cancel the collection, collect again", which restarts
 *   the cold store flow.
 * - A store failure propagates to the collector unchanged (known v1.0 debt, kept for strict parity).
 * - Equal consecutive results are not filtered out: the ViewModel state conflates equal data.
 *
 * Data only (D-04): loading and error state, tracing, refresh and cancellation stay in the
 * ViewModel. Every platform passes its own `TimeZone.currentSystemDefault()` (D-05, D-19); Android
 * passes [UtcDayWindows], which ignores the zone by design, iOS will pass
 * [LocalDateDays]. The upstream work runs on the injected [dispatcher].
 */
class ObserveAgendaUseCase(
    private val store: DevFestNantesStore,
    private val sessionFiltersStore: SessionFiltersStore,
    private val bookmarksStore: BookmarksStore,
    private val filterSessions: FilterSessionsUseCase,
    private val groupSessions: GroupSessionsUseCase,
    private val dispatcher: CoroutineDispatcher
) {
    @NativeCoroutines
    fun observe(dayAssignment: DayAssignment, timeZone: TimeZone): Flow<List<DaySchedule>> =
        combine(
            store.sessions,
            sessionFiltersStore.filters,
            bookmarksStore.bookmarkedSessionIds
        ) { sessions, filters, bookmarkedIds ->
            groupSessions.group(filterSessions.filter(sessions, filters, bookmarkedIds), dayAssignment, timeZone)
        }.flowOn(dispatcher)
}
