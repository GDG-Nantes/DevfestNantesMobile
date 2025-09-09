//
//  SpeakerDetailsViewModel.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 13/09/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared
import Combine
import KMPNativeCoroutinesAsync
import SwiftUI
import os


class SpeakerDetailsViewModel: BaseViewModel {
    @Published var speaker: Speaker_?
    @Published var speakerSession: [Session_]?
    @Published var isLoading: Bool = true
    var speakerId: String
    
    private let performanceMonitoring = PerformanceMonitoring.shared
    private let logger = DevFestLogger(category: "SpeakerDetailsViewModel")
    
    init(speakerId: String) {
        self.speakerId = speakerId
        super.init()
        
        Task {
            await getSpeaker(speakerId: speakerId)
            await getSpeakerSession(speakerId: speakerId)
        }
    }
    
    
    func getSpeaker(speakerId: String) async {
        do {
            let speakerResult = try await performanceMonitoring.trackDataLoad(
                traceName: PerformanceMonitoring.TRACE_SPEAKER_DETAILS_LOAD,
                dataSource: "graphql"
            ) {
                try await asyncFunction(for: self.store.getSpeaker(id: speakerId))
            }
            
            DispatchQueue.main.async {
                self.speaker = speakerResult
                // L'état de chargement sera arrêté une fois les sessions récupérées également
            }
            
            logger.log(.info, "Speaker details loaded: \(speakerResult?.name ?? "Unknown")")
            
        } catch {
            logger.log(.error, "Get Speaker error: \(error.localizedDescription)")
            DispatchQueue.main.async {
                self.isLoading = false
            }
        }
    }
    
    func getSpeakerSession(speakerId: String) async {
        do {
            let speakerSessionResult = try await asyncFunction(for: store.getSpeakerSessions(speakerId: speakerId))
            DispatchQueue.main.async {
                self.speakerSession = speakerSessionResult
                self.isLoading = false
            }
            
            logger.log(.info, "Speaker sessions loaded: \(speakerSessionResult.count) sessions")
        } catch {
            logger.log(.error, "Get Speaker Sessions error: \(error.localizedDescription)")
            DispatchQueue.main.async {
                self.isLoading = false
            }
        }
    }
}
