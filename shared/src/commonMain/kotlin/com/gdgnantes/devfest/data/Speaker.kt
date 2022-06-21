package com.gdgnantes.devfest.data

data class Speaker(
    val id: Int,
    val firstname: String?,
    val surname: String?,
    val photoUrl: String?,
    val biography: String?,
    val twitter: String?,
    val github: String?,
    val city: String
)