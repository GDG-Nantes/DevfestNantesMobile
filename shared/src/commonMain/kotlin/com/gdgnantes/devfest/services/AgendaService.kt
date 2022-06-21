package com.gdgnantes.devfest.services

import com.gdgnantes.devfest.data.AgendaDay

interface AgendaService {
    fun getDays(): Result<List<AgendaDay>>
}