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
import KMPNativeCoroutinesCombine
import NSLogger
import SwiftUI

@MainActor
class SpeakersViewModel: BaseViewModel {
    @Published var speakersContent: [Speaker_]?
    @Published var isLoading = true
    private var cancellables = Set<AnyCancellable>()

    func observeSpeakers() {
        let speakersPublisher: AnyPublisher<[Speaker_], Error> = KMPNativeCoroutinesCombine.createPublisher(for: store.getSpeakers())
        
        speakersPublisher
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { completion in
                switch completion {
                case .finished:
                    self.isLoading = false
                case .failure(let error):
                    Logger.shared.log(.network, .error, "Observe Speakers error: \(error)")
                    self.isLoading = false
                }
            }, receiveValue: { speakers in
                self.speakersContent = speakers
            })
            .store(in: &cancellables)
    }

    func getAlphabets() -> [String] {
        let letters = speakersContent?.compactMap { $0.name.prefix(1).uppercased() }
        let uniqueLetters = Array(Set(letters ?? []))
        return uniqueLetters.sorted()
    }

    func speakers(for letter: String) -> [Speaker_] {
        return speakersContent?.filter {
            $0.name.prefix(1).uppercased() == letter
        } ?? []
    }
}
