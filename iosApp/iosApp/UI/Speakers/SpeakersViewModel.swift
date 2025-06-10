//
//  SpeakersViewModel.swift
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
class SpeakersViewModel: BaseViewModel {
    @Published var speakersContent: [Speaker_]?
    @Published var isLoading = true
    
    ///Asynchronous method to retrieve speakers
    func observeSpeakers() async {
        Task {
            do {
                let speakersSequence = asyncSequence(for: store.speakers)
                for try await speakers in speakersSequence {
                    DispatchQueue.main.async {
                        self.speakersContent = speakers
                        self.isLoading = false
                    }
                }
            } catch {
                Logger(subsystem: Bundle.main.bundleIdentifier ?? "DevFestNantes", category: "Speakers").error("Observe Speakers error: \(error.localizedDescription)")
                // Handle error appropriately, e.g., show an alert to the user
            }
        }
    }

    ///Function to get the first letters of speaker names
    func getAlphabets() -> [String] {
        let letters = speakersContent?.compactMap { $0.name.prefix(1).uppercased() }
        let uniqueLetters = Array(Set(letters ?? []))
        return uniqueLetters.sorted()
    }

    ///Function to get speakers by the first letter of their name
    func speakers(for letter: String) -> [Speaker_] {
        return speakersContent?.filter {
            $0.name.prefix(1).uppercased() == letter
        } ?? []
    }
}
