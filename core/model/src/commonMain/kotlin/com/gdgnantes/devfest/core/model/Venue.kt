package com.gdgnantes.devfest.model

data class Venue(
    var address: String = "",
    var description: String = "",
    var floorPlanUrl: String? = null,
    var latitude: Double? = 0.0,
    var longitude: Double? = 0.0,
    var imageUrl: String? = null,
    var name: String = "No Venue"
)
