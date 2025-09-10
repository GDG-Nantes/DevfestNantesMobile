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
    
    private let performanceMonitoring = PerformanceMonitoring.shared
    
    ///Asynchronous method to retrieve speakers
    func observeSpeakers() async {
        do {
            let speakers = try await performanceMonitoring.trackDataLoad(
                traceName: PerformanceMonitoring.TRACE_SPEAKERS_LOAD,
                dataSource: "graphql"
            ) {
                let speakersSequence = asyncSequence(for: self.store.speakers)
                var speakers: [Speaker_] = []
                
                for try await speakersList in speakersSequence {
                    speakers = speakersList
                    break // Get the first emission
                }
                
                return speakers
            }
            
            DispatchQueue.main.async {
                self.speakersContent = speakers
                self.isLoading = false
            }
            
            DevFestLogger(category: "Speakers")
                .info("Speakers loaded: \(speakers.count) speakers")
                
        } catch {
            DevFestLogger(category: "Speakers")
                .error("Observe Speakers error: \(error.localizedDescription)")
            
            DispatchQueue.main.async {
                self.isLoading = false
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
