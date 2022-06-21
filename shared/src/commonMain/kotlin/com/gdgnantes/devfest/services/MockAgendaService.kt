package com.gdgnantes.devfest.services

import com.gdgnantes.devfest.data.AgendaDay
import com.gdgnantes.devfest.stubs.createAgendaDayStub

internal class MockAgendaService : AgendaService {
    override fun getDays(): Result<List<AgendaDay>> =
        Result.success(
            listOf(
                createAgendaDayStub(),
                createAgendaDayStub()
            )
        )
}