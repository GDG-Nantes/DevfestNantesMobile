package com.gdgnantes.devfest.core.model

import kotlin.time.Instant

data class ScheduleSlot(
    val endDate: String = "",
    val startDate: String = ""
) : Comparable<ScheduleSlot> {
    val startDateAsEpochMilliseconds = Instant.parse(startDate).toEpochMilliseconds()
    val endDateAsEpochMilliseconds = Instant.parse(endDate).toEpochMilliseconds()

    override fun compareTo(other: ScheduleSlot): Int {
        return (startDateAsEpochMilliseconds - other.startDateAsEpochMilliseconds).toInt()
    }
}
