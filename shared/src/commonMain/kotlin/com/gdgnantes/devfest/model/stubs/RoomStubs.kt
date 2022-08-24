package com.gdgnantes.devfest.model.stubs

import com.gdgnantes.devfest.model.Room
import kotlin.native.concurrent.SharedImmutable

@SharedImmutable
val roomStubs: List<Room> = listOf(
    Room(
        "jules_verne",
        "Jules Verne"
    ),
    Room(
        "titan",
        "Titan"
    ),
    Room(
        "belem",
        "Belem"
    ),
    Room(
        "tour_de_bretagne",
        "Tour de Bretagne"
    ),
    Room(
        "les_machines",
        "Les Machines"
    ),
    Room(
        "Hangar",
        "Hangar"
    ),
    Room(
        "l_atelier",
        "L'Atelier"
    )
)