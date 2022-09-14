//
//  VenueViewmodel.swift
//  iosApp
//
//  Created by Stéphane Rihet on 13/09/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared
import Combine
import KMPNativeCoroutinesCore
import KMPNativeCoroutinesAsync
import NSLogger

class VenueViewModel: ObservableObject, Identifiable {
    
    struct Content {
        let address: String
        let descriptionEn: String
        let descriptionFr: String
        let latitude: Double
        let longitude: Double
        let imageUrl: String
        let name: String
    }
    
    let store : DevFestNantesStore
    @Published var content: Content?
    
    
    init() {
        self.store = DevFestNantesStoreMocked()
    }
    
    func observVenue() async {
        do {
            let stream = asyncStream(for: store.venueNative)
            for try await data in stream {
                self.content = Content(from: data)
            }
        } catch {
            print("Failed with error: \(error)")
        }
    }

    }

private extension VenueViewModel.Content {
    init(from venue: Venue) {
        self.init(address: venue.address, descriptionEn: venue.descriptionEn, descriptionFr: venue.descriptionFr, latitude: venue.latitude, longitude: venue.longitude, imageUrl: venue.imageUrl, name: venue.name)
    }
}
