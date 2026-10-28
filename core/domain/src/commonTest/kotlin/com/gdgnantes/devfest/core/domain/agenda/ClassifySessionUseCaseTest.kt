package com.gdgnantes.devfest.core.domain.agenda

import com.gdgnantes.devfest.core.model.ScheduleSlot
import com.gdgnantes.devfest.core.model.Session
import com.gdgnantes.devfest.core.model.SessionType
import kotlin.test.Test
import kotlin.test.assertEquals

class ClassifySessionUseCaseTest {
    private val useCase = ClassifySessionUseCase()

    // Expected (isService, isTalk, isNavigable) per type, written as literals on purpose.
    private val expected: Map<SessionType?, Triple<Boolean, Boolean, Boolean>> =
        mapOf(
            SessionType.BREAK to Triple(true, false, false),
            SessionType.CODELAB to Triple(false, true, true),
            SessionType.CONFERENCE to Triple(false, true, true),
            SessionType.KEYNOTE to Triple(false, true, false),
            SessionType.LUNCH to Triple(true, false, false),
            SessionType.OPENING to Triple(true, false, false),
            SessionType.QUICKIE to Triple(false, true, true),
            null to Triple(false, false, false)
        )

    private fun sessionOf(type: SessionType?): Session =
        Session(
            id = "s",
            scheduleSlot =
                ScheduleSlot(
                    startDate = "2025-10-16T08:00:00+02:00",
                    endDate = "2025-10-16T09:00:00+02:00"
                ),
            type = type
        )

    @Test
    fun every_session_type_is_covered_by_the_table() {
        assertEquals(SessionType.entries.toSet(), expected.keys.filterNotNull().toSet())
    }

    @Test
    fun is_service_matches_the_table_for_every_type_and_a_null_type() {
        expected.forEach { (type, row) ->
            assertEquals(row.first, useCase.isService(sessionOf(type)), "isService($type)")
        }
    }

    @Test
    fun is_talk_matches_the_table_for_every_type_and_a_null_type() {
        expected.forEach { (type, row) ->
            assertEquals(row.second, useCase.isTalk(sessionOf(type)), "isTalk($type)")
        }
    }

    @Test
    fun is_navigable_matches_the_table_for_every_type_and_a_null_type() {
        expected.forEach { (type, row) ->
            assertEquals(row.third, useCase.isNavigable(sessionOf(type)), "isNavigable($type)")
        }
    }
}
