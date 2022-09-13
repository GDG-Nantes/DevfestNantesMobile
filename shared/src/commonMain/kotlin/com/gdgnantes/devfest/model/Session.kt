package com.gdgnantes.devfest.model

data class Session(
    val id: String = "",
    val abstract: String = "",
    val category: Category? = null,
    val language: SessionLanguage? = null,
    val complexity: Complexity? = null,
    val openFeedbackFormId: String = "",
    val room: Room? = null,
    val scheduleSlot: ScheduleSlot,
    val speakers: List<Speaker> = listOf(),
    val title: String = ""
)
