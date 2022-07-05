package com.gdgnantes.devfest.model

data class Venue(
    var address: String = "",
    var descriptionEn: String = "",
    var descriptionFr: String = "",
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var imageUrl: String = "",
    var name: String = "No Venue"
)