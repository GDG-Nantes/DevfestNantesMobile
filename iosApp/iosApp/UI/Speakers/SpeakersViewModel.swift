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
import NSLogger
import SwiftUI

@MainActor
class SpeakersViewModel: BaseViewModel {
    @Published var speakersContent: [Speaker]?
    @Published var isLoading = true
    
    ///Asynchronous method to retrieve speakers
    func observeSpeakers() async {
        Task {
            do {
                let stream = asyncStream(for: store.speakersNative)
                for try await data in stream {
                    DispatchQueue.main.async {
                        self.speakersContent = Array(data)
                        self.isLoading = false
                    }
                }
            } catch {
                Logger.shared.log(.network, .error, "Observe Speakers error: \(error)")
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
    func speakers(for letter: String) -> [Speaker] {
        return speakersContent?.filter {
            $0.name.prefix(1).uppercased() == letter
        } ?? []
    }
}
