//
//  VenueViewModel.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 13/09/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import Observation
import shared

@Observable
@MainActor
final class VenueViewModel {
    var venueContent: VenueContent?
    var isLoading: Bool = true

    @ObservationIgnored private let source: VenueSource

    init(source: VenueSource = LiveVenueSource()) {
        self.source = source
    }

    ///Detect phone language
    var currentLanguage: ContentLanguage {
        guard let languageCode = Locale.current.languageCode else {
            return .english
        }
        if languageCode == "fr" {
            return .french
        } else {
            return .english
        }
    }

    ///Asynchronous method to retrieve venue
    func observeVenue() async {
        isLoading = true
        do {
            let venue = try await source.venue(language: currentLanguage)
            venueContent = VenueContent(from: venue)
            isLoading = false
        } catch is CancellationError {
            return
        } catch {
            DevFestLogger(category: "Venue").log(.error, "Observe Venue error: \(error.localizedDescription)", error: error)
            isLoading = false
        }
    }
}
