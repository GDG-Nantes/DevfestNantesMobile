package com.gdgnantes.devfest.model

import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant

data class Agenda(val days: Map<Int, AgendaDay>) {
    class Builder {
        var sessions: List<Session> = emptyList()

        fun build(): Agenda {
            val dayOneSessions = mutableListOf<Session>()
            val dayTwoSessions = mutableListOf<Session>()
            sessions.sortedBy { session -> session.scheduleSlot.startDate }
                .forEach { session ->
                    val startInstant = session.scheduleSlot.startDate.toInstant()
                    if (startInstant.minus(DAY_ONE).inWholeDays == 0L) {
                        dayOneSessions.add(session)
                    } else if (startInstant.minus(DAY_TWO).inWholeDays == 0L) {
                        dayTwoSessions.add(session)
                    }
                }

            val days = mutableMapOf<Int, AgendaDay>()
            days[1] = AgendaDay(1, DAY_ONE_ISO, dayOneSessions)
            days[2] = AgendaDay(2, DAY_TWO_ISO, dayTwoSessions)

            return Agenda(days)
        }
    }

    companion object {
        const val DAY_ONE_ISO = "2024-10-17T00:00:00Z"
        const val DAY_TWO_ISO = "2024-10-18T00:00:00Z"
        val DAY_ONE = Instant.parse(DAY_ONE_ISO)
        val DAY_TWO = Instant.parse(DAY_TWO_ISO)
    }
}
