//
//  SpeakerDetailsViewModel.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 13/09/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import shared
import Combine
import KMPNativeCoroutinesCombine
import NSLogger
import SwiftUI

class SpeakerDetailsViewModel: BaseViewModel {
    @Published var speaker: Speaker_?
    @Published var speakerSession: [Session_]?
    var speakerId: String
    private var cancellables = Set<AnyCancellable>()
    
    init(speakerId: String) {
        self.speakerId = speakerId
        super.init()
        
        getSpeaker(speakerId: speakerId)
        getSpeakerSession(speakerId: speakerId)
    }
    
    func getSpeaker(speakerId: String) {
        nativeSuspendToPublisher(store.getSpeaker(id: speakerId))
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { completion in
                if case let .failure(error) = completion {
                    Logger.shared.log(.network, .error, "Observe Venue error: \(error)")
                }
            }, receiveValue: { [weak self] speaker in
                self?.speaker = speaker
            })
            .store(in: &cancellables)
    }
    
    func getSpeakerSession(speakerId: String) {
        nativeSuspendToPublisher(store.getSpeakerSessions(speakerId: speakerId))
            .receive(on: DispatchQueue.main)
            .sink(receiveCompletion: { completion in
                if case let .failure(error) = completion {
                    Logger.shared.log(.network, .error, "Observe Venue error: \(error)")
                }
            }, receiveValue: { [weak self] sessions in
                self?.speakerSession = sessions
            })
            .store(in: &cancellables)
    }
}
