package com.gdgnantes.devfest.model

import kotlinx.datetime.Instant
import kotlinx.datetime.toInstant

data class Agenda(val days: Map<Int, AgendaDay>) {

    class Builder() {
        var sessions: List<Session> = emptyList()

        fun build(): Agenda {
            val dayOne = mutableListOf<Session>()
            val dayTwo = mutableListOf<Session>()
            sessions.sortedBy { session -> session.scheduleSlot }
                .forEach { session ->
                    val startInstant = session.scheduleSlot.startDate.toInstant()
                    if (startInstant.minus(DAY_ONE).inWholeDays == 0L) {
                        dayOne.add(session)
                    } else if (startInstant.minus(DAY_ONE).inWholeDays == 0L) {
                        dayTwo.add(session)
                    }
                }

            val days = mutableMapOf<Int, AgendaDay>()
            days[1] = AgendaDay(1, dayOne)
            days[2] = AgendaDay(2, dayTwo)

            return Agenda(days)
        }
    }

    companion object {
        val DAY_ONE = Instant.parse("2022-10-20T02:00:00.000Z")
        val DAY_TWO = Instant.parse("2022-10-21T02:00:00.000Z")
    }
}
