package com.gdgnantes.devfest.feature.agenda.utils

import com.gdgnantes.devfest.core.model.SessionType

fun SessionType.isService() =
    this == SessionType.OPENING || this == SessionType.BREAK || this == SessionType.LUNCH
