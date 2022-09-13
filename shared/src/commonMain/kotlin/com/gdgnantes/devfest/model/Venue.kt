package com.gdgnantes.devfest.model

data class Venue(
    var address: String = "",
    var descriptionEn: String = "",
    var descriptionFr: String = "",
    var coordinates: String? = null,
    var imageUrl: String = "",
    var name: String = "No Venue"
)