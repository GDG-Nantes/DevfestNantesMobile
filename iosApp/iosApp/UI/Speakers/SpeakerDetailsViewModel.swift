//
//  SpeakerDetailsViewModel.swift
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
final class SpeakerDetailsViewModel {
    var speaker: Speaker?
    var speakerSession: [Session]?
    var isLoading: Bool = true
    let speakerId: String

    @ObservationIgnored private let source: SpeakerDetailsSource
    @ObservationIgnored private let tracing: DataLoadTracing
    @ObservationIgnored private let logger = DevFestLogger(category: "SpeakerDetailsViewModel")

    init(speakerId: String, source: SpeakerDetailsSource = LiveSpeakerDetailsSource(), tracing: DataLoadTracing = PerformanceMonitoring.shared) {
        self.speakerId = speakerId
        self.source = source
        self.tracing = tracing
    }

    /// Loads the speaker, then their sessions (same order as v1.0). Called from the view's `.task`.
    func load() async {
        await getSpeaker(speakerId: speakerId)
        if Task.isCancelled { return }
        await getSpeakerSession(speakerId: speakerId)
    }

    /// Maps a session with the shared classification (D-07, D-08). Pure and called per row during view
    /// evaluation; the speaker lists are short, so nothing is cached.
    func content(for session: Session) -> AgendaContent.Session {
        AgendaContent.Session(from: session, isATalk: source.isTalk(session), isNavigable: source.isNavigable(session))
    }

    func getSpeaker(speakerId: String) async {
        let source = self.source
        do {
            let speakerResult = try await tracing.trackDataLoad(
                traceName: PerformanceMonitoring.TRACE_SPEAKER_DETAILS_LOAD,
                dataSource: "graphql"
            ) {
                try await source.speaker(id: speakerId)
            }

            // The loading state stops once the sessions are retrieved as well.
            speaker = speakerResult

            logger.log(.info, "Speaker details loaded: \(speakerResult?.name ?? "Unknown")")
        } catch is CancellationError {
            return
        } catch {
            logger.log(.error, "Get Speaker error: \(error.localizedDescription)")
            isLoading = false
        }
    }

    func getSpeakerSession(speakerId: String) async {
        do {
            let speakerSessionResult = try await source.speakerSessions(speakerId: speakerId)
            speakerSession = speakerSessionResult
            isLoading = false

            logger.log(.info, "Speaker sessions loaded: \(speakerSessionResult.count) sessions")
        } catch is CancellationError {
            return
        } catch {
            logger.log(.error, "Get Speaker Sessions error: \(error.localizedDescription)")
            isLoading = false
        }
    }
}
