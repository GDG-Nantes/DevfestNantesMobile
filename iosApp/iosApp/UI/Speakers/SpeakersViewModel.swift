//
//  SpeakersViewModel.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 13/09/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import Foundation
import Observation
import shared

@Observable
@MainActor
final class SpeakersViewModel {
    var speakersContent: [Speaker]? {
        // The A-Z grouping is computed once per change of the speakers, not on every view read.
        didSet { letterGroups = source.letterGroups(speakers: speakersContent ?? []) }
    }
    private(set) var letterGroups: [SpeakerLetterGroup] = []
    var isLoading = true

    @ObservationIgnored private let source: SpeakersSource
    @ObservationIgnored private let tracing: DataLoadTracing
    @ObservationIgnored private let logger = DevFestLogger(category: "SpeakersViewModel")

    init(source: SpeakersSource = LiveSpeakersSource(), tracing: DataLoadTracing = PerformanceMonitoring.shared) {
        self.source = source
        self.tracing = tracing
    }

    /// Asynchronous method to retrieve speakers
    func observeSpeakers() async {
        let source = self.source
        do {
            let speakers = try await tracing.trackDataLoad(
                traceName: PerformanceMonitoring.TRACE_SPEAKERS_LOAD,
                dataSource: "graphql"
            ) {
                var first: [Speaker]?
                // Only the first emission is used; leaving the loop terminates the upstream stream.
                for try await speakersList in source.speakers() {
                    first = speakersList
                    break
                }
                // The bridge ends silently when the load is cancelled, so cancellation is checked explicitly.
                try Task.checkCancellation()
                return first ?? []
            }

            speakersContent = speakers
            isLoading = false

            logger.log(.info, "Speakers loaded: \(speakers.count) speakers")
        } catch is CancellationError {
            return
        } catch {
            logger.log(.error, "Observe Speakers error: \(error.localizedDescription)")
            isLoading = false
        }
    }

    ///Function to get the first letters of speaker names, from the shared speaker index
    func getAlphabets() -> [String] {
        letterGroups.map(\.letter)
    }

    ///Function to get speakers by the first letter of their name, from the shared speaker index
    func speakers(for letter: String) -> [Speaker] {
        letterGroups.first { $0.letter == letter }?.speakers ?? []
    }
}
