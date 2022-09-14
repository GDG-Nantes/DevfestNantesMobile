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
    
    let store : DevFestNantesStore
    @Published var content: Content = Content(address: "5 rue de Valmy, 44000 Nantes", description: "Située en plein cœur de ville, La Cité des Congrès de Nantes propose pour le DevFest Nantes plus de 3000m² de salles de conférences, codelabs et lieu de rencontre…", latitude: 47.21308725112951, longitude: -1.542622837466317, imageUrl: "https://devfest.gdgnantes.com/static/6328df241501c6e31393e568e5c68d7e/efc43/amphi.webp", name: "Cité des Congrès de Nantes")
    
    
    init() {
        self.store = DevFestNantesStoreBuilder().setUseMockServer(useMockServer: false).build()
    }
    
    func observeVenue() async {
        Task {
            do {
                let stream = asyncStream(for: store.getVenueNative(language: .french))
                for try await data in stream {
                    DispatchQueue.main.async {
                        self.content = Content(from: data)
                    }
                    
                }
                
            } catch {
                print("Failed with error: \(error)")
            }
        }}
    
}

extension VenueViewModel {
    struct Content {
        let address: String
        let description: String
        let latitude: Double
        let longitude: Double
        let imageUrl: String
        let name: String
    }
}

private extension VenueViewModel.Content {
    init(from venue: Venue) {
        self.init(address: venue.address, description: venue.description_, latitude: venue.latitude as! Double, longitude: venue.longitude as! Double, imageUrl: venue.imageUrl ?? "https://fakeface.rest/thumb/view", name: venue.name)
    }
}
