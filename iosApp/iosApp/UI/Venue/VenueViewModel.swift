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
    @Published var isLoading: Bool = true
    
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
        self.isLoading = true
        Task {
            do {
                let venueData = try await asyncFunction(for: store.getVenue(language: currentLanguage))
                DispatchQueue.main.async {
                    self.venueContent = VenueContent(from: venueData)
                    self.isLoading = false
                }
            } catch {
                DevFestLogger(category: "Venue").log(.error, "Observe Venue error: \(error.localizedDescription)", error: error)
                // Handle error appropriately
                DispatchQueue.main.async {
                    self.isLoading = false
                }
            }
        }
    }
}
