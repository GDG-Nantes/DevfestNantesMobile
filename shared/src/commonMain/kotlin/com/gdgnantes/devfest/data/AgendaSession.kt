package com.gdgnantes.devfest.data

data class AgendaSession(
    val id: Int,
    val abstract: String,
    val level: AgendaSessionLevel,
    val category: AgendaSessionCategory,
    val language: AgendaSessionLanguage,
    val speakers: List<Speaker>,
    val openFeedbackFormId: Int,
    val room: Room
)