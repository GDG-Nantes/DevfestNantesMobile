package com.gdgnantes.devfest.model

import kotlinx.datetime.Instant

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
