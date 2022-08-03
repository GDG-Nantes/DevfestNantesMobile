package com.gdgnantes.devfest.model

import kotlinx.datetime.Instant

data class ScheduleSlot(
    val endDate: String = "",
    val startDate: String = ""
) : Comparable<ScheduleSlot> {
    override fun compareTo(other: ScheduleSlot): Int {
        val startEpochMilliseconds = Instant.parse(startDate).toEpochMilliseconds()
        val otherStartEpochMilliseconds = Instant.parse(other.startDate).toEpochMilliseconds()
        return (startEpochMilliseconds - otherStartEpochMilliseconds).toInt()
    }

}