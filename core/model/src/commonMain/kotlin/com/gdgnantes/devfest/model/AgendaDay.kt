package com.gdgnantes.devfest.model

data class AgendaDay(
    val dayIndex: Int,
    val date: String,
    val sessions: List<Session>
)
