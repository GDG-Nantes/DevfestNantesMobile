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
import NSLogger
import SwiftUI


class SpeakerDetailsViewModel: BaseViewModel {
    @Published var speaker: Speaker_?
    @Published var speakerSession: [Session_]?
    var speakerId: String
    
    init(speakerId: String) {
        self.speakerId = speakerId
        super.init()
        
        Task {
            await getSpeaker(speakerId: speakerId)
            await getSpeakerSession(speakerId: speakerId)
        }        
    }
    

    func getSpeaker(speakerId: String) async {
        Task {
            do {
                let speakerReslut = try await asyncFunction(for: store.getSpeaker(id: speakerId))
                DispatchQueue.main.async {
                    self.speaker = speakerReslut
                }
            } catch {
                Logger.shared.log(.network, .error, "Observe Venue error: \(error)")
            }
        }
    }
    
    func getSpeakerSession(speakerId: String) async {
        Task {
            do {
                let speakerSessionResult = try await asyncFunction(for: store.getSpeakerSessions(speakerId: speakerId))
                DispatchQueue.main.async {
                    self.speakerSession = speakerSessionResult
                }
            } catch {
                Logger.shared.log(.network, .error, "Observe Venue error: \(error)")
            }
        }
    }
}
