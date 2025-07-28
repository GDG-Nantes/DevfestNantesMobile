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
    var speakerId: String
    
    private let performanceMonitoring = PerformanceMonitoring.shared
    
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
            }
            
            Logger(subsystem: Bundle.main.bundleIdentifier ?? "DevFestNantes", category: "SpeakerDetails")
                .info("Speaker details loaded: \(speakerResult?.name ?? "Unknown")")
                
        } catch {
            Logger(subsystem: Bundle.main.bundleIdentifier ?? "DevFestNantes", category: "SpeakerDetails")
                .error("Get Speaker error: \(error.localizedDescription)")
        }
    }
    
    func getSpeakerSession(speakerId: String) async {
        do {
            let speakerSessionResult = try await asyncFunction(for: store.getSpeakerSessions(speakerId: speakerId))
            DispatchQueue.main.async {
                self.speakerSession = speakerSessionResult
            }
            
            Logger(subsystem: Bundle.main.bundleIdentifier ?? "DevFestNantes", category: "SpeakerDetails")
                .info("Speaker sessions loaded: \(speakerSessionResult.count) sessions")
                
        } catch {
            Logger(subsystem: Bundle.main.bundleIdentifier ?? "DevFestNantes", category: "SpeakerDetails")
                .error("Get Speaker Sessions error: \(error.localizedDescription)")
        }
    }
}
