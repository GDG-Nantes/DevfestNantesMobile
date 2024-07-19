package com.gdgnantes.devfest.domain

import com.gdgnantes.devfest.graphql.fragment.RoomDetails

val RoomDetails.sortIndex: Int
    get() {
        return when (id) {
            "Jules Verne" -> 0
            "Titan" -> 1
            "Belem" -> 2
            "Tour de Bretagne" -> 3
            "Les Machines" -> 4
            "Hangar" -> 5
            "L'Atelier" -> 6
            else -> Int.MAX_VALUE
        }
    }
