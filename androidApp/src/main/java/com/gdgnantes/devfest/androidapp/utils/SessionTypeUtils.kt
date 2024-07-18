package com.gdgnantes.devfest.androidapp.utils

import com.gdgnantes.devfest.model.SessionType

fun SessionType.isService() =
    this == SessionType.OPENING || this == SessionType.BREAK || this == SessionType.LUNCH
