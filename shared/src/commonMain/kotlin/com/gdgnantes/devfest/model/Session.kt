package com.gdgnantes.devfest.model

data class Session(
        val id: String = "",
        val abstract: String = "",
        val category: SessionCategory? = null,
        val language: SessionLanguage? = null,
        val level: SessionLevel? = null,
        val openFeedbackFormId: String = "",
        val room: Room,
        val scheduleSlot: ScheduleSlot,
        val speakers: List<Speaker> = listOf(),
        val title: String = ""
)
