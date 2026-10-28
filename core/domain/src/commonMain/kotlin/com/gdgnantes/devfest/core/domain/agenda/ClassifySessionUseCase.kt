package com.gdgnantes.devfest.core.domain.agenda

import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.SessionType

/**
 * Shared classification of a [Session] as service slot, talk and navigable (tappable to a detail
 * screen). It mirrors what both platforms already do today:
 *
 * - Android: `SessionType.isService()` (feature:agenda `SessionTypeUtils`) and
 *   `MainActivity.onSessionClick` for navigability.
 * - iOS: `AgendaContent.Session.isATalk` and the `isNavigable` check in `AgendaView`.
 *
 * Table: service = OPENING, BREAK, LUNCH; talk = typed and not a service; navigable = QUICKIE,
 * CONFERENCE, CODELAB. A session without a type is none of the three.
 */
class ClassifySessionUseCase {
    /** True for non-content slots (opening, break, lunch). False when the type is unknown. */
    fun isService(session: Session): Boolean = session.type in SERVICE_TYPES

    /** True for a typed session that is not a service slot. False when the type is unknown. */
    fun isTalk(session: Session): Boolean = session.type != null && !isService(session)

    /** True when tapping the session opens its detail screen. */
    fun isNavigable(session: Session): Boolean = session.type in NAVIGABLE_TYPES

    private companion object {
        val SERVICE_TYPES = setOf(SessionType.OPENING, SessionType.BREAK, SessionType.LUNCH)
        val NAVIGABLE_TYPES = setOf(SessionType.QUICKIE, SessionType.CONFERENCE, SessionType.CODELAB)
    }
}
