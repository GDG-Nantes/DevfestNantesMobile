package com.gdgnantes.devfest.core.domain.agenda

import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.SessionFilter
import com.gdgnantes.devfest.core.model.SessionFilterType

/**
 * Shared agenda filter rule: OR within one filter type, AND across the types present.
 *
 * A session is kept when, for every filter type that has at least one filter, it matches at least
 * one filter of that type. No filter keeps every session. The match rules are:
 *
 * - [SessionFilterType.BOOKMARK]: membership of the session id in `bookmarkedSessionIds`, whatever
 *   the filter value is.
 * - [SessionFilterType.LANGUAGE], [SessionFilterType.COMPLEXITY], [SessionFilterType.TYPE]: the
 *   filter value equals the enum name of the session field.
 * - [SessionFilterType.ROOM]: the filter value equals `room.id`.
 *
 * A null field never matches. The output keeps the input (store) order.
 *
 * This is the v1.0 Android rule (`AgendaViewModel.filterSessions`). v1.0 Android went through a
 * `HashSet`, so its output order was unspecified; the deterministic input order is a subset of that
 * behavior. iOS v1.0 compared the room filter with the room name; the two are equal on real data
 * because `room.id == room.name`.
 */
class FilterSessionsUseCase {
    fun filter(
        sessions: List<Session>,
        filters: Set<SessionFilter>,
        bookmarkedSessionIds: Set<String>
    ): List<Session> {
        if (filters.isEmpty()) return sessions
        val filtersByType = filters.groupBy { it.type }.values
        return sessions.filter { session ->
            filtersByType.all { group -> group.any { matches(session, it, bookmarkedSessionIds) } }
        }
    }

    private fun matches(
        session: Session,
        filter: SessionFilter,
        bookmarkedSessionIds: Set<String>
    ): Boolean =
        when (filter.type) {
            SessionFilterType.BOOKMARK -> session.id in bookmarkedSessionIds
            SessionFilterType.LANGUAGE -> filter.value == session.language?.name
            SessionFilterType.COMPLEXITY -> filter.value == session.complexity?.name
            SessionFilterType.ROOM -> filter.value == session.room?.id
            SessionFilterType.TYPE -> filter.value == session.type?.name
        }
}
