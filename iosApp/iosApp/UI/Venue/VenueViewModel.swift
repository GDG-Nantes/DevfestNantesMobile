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
import NSLogger
import SwiftUI

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
                let stream = asyncStream(for: self.store.getVenueNative(language: currentLanguage))
                for try await data in stream {
                    DispatchQueue.main.async {
                        self.venueContent = VenueContent(from: data)
                    }
                }
            } catch {
                Logger.shared.log(.network, .error, "Observe Venue error: \(error)")
            }
        }
    }
}
