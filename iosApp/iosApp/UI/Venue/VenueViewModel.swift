//
//  VenueViewModel.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 13/09/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import shared
import Combine
import KMPNativeCoroutinesAsync
import SwiftUI
import os

@MainActor
class VenueViewModel: BaseViewModel {
    @Published var venueContent: VenueContent?
    
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
        Task {
            do {
                let venueData = try await asyncFunction(for: store.getVenue(language: currentLanguage))
                DispatchQueue.main.async {
                    self.venueContent = VenueContent(from: venueData)
                }
            } catch {
                Logger(subsystem: Bundle.main.bundleIdentifier ?? "DevFestNantes", category: "Venue").error("Observe Venue error: \(error.localizedDescription)")
                // Handle error appropriately
            }
        }
    }
}
