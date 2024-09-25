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
import KMPNativeCoroutinesCombine
import NSLogger
import SwiftUI

@MainActor
class VenueViewModel: BaseViewModel {
    @Published var venueContent: VenueContent?
    private var cancellables = Set<AnyCancellable>()
    
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
    
    ///Method to retrieve venue using Combine
    func observeVenue() {
        nativeSuspendToPublisher(store.getVenue(language: currentLanguage))
            .receive(on: DispatchQueue.main)
            .sink(
                receiveCompletion: { completion in
                    if case let .failure(error) = completion {
                        Logger.shared.log(.network, .error, "Observe Venue error: \(error)")
                    }
                },
                receiveValue: { [weak self] venueData in
                    self?.venueContent = VenueContent(from: venueData)
                }
            )
            .store(in: &cancellables)
    }
}

