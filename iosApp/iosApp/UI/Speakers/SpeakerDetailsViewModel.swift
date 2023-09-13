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
    @Published var speaker: Speaker?
    @Published var speakerSession: [Session]?
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
                let stream = asyncStream(for: self.store.getSpeakerNative(id: speakerId))
                for try await data in stream {
                    DispatchQueue.main.async {
                        self.speaker = data
                    }
                    
                }
                
            } catch {
                Logger.shared.log(.network, .error, "Observe Venue error: \(error)")
            }
        }}
    
    func getSpeakerSession(speakerId: String) async {
        Task {
            do {
                let stream = asyncStream(for: self.store.getSpeakerSessionsNative(speakerId: speakerId))
                for try await data in stream {
                    DispatchQueue.main.async {
                        self.speakerSession = data
                    }
                    
                }
                
            } catch {
                Logger.shared.log(.network, .error, "Observe Venue error: \(error)")
            }
        }}
}
