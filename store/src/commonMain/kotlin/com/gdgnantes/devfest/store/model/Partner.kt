package com.gdgnantes.devfest.store.model

data class Partner(
    val name: String?,
    val imageUrl: String?,
    val link: String?,
    val description: String?,
    val order: Int = 0,
)