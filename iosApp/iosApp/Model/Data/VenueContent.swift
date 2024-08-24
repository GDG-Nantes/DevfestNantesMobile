//
//  VenueContent.swift
//  iosApp
//
//  Created by Stéphane Rihet on 15/09/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared

// MARK: - VenueContent
struct VenueContent {
    let address: String
    let description: String
    let latitude: Double
    let longitude: Double
    let imageUrl: String
    let name: String
    let planUrl: String
}

extension VenueContent {
    //Initialization venue
    init(from venue: Venue_) {
        self.init(address: venue.address, description: venue.description_, latitude: venue.latitude as! Double, longitude: venue.longitude as! Double, imageUrl: venue.imageUrl ?? "https://fr.wikipedia.org/wiki/Cit%C3%A9_des_congr%C3%A8s_de_Nantes#/media/Fichier:La-cite-nantes.jpg", name: venue.name, planUrl: venue.floorPlanUrl ?? "https://raw.githubusercontent.com/GDG-Nantes/Devfest2022/master/src/images/plan-cite-blanc.png")
    }
}
