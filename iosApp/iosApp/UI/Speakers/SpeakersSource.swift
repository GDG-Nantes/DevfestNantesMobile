//
//  SpeakersSource.swift
//  DevFest Nantes
//

import shared

/// What the Speakers screen reads: the list of speakers and their A-Z grouping.
protocol SpeakersSource {
    func speakers() -> AsyncThrowingStream<[Speaker], Error>

    /// The speakers grouped by the first letter of their name, sorted by letter, in input order inside a group.
    func letterGroups(speakers: [Speaker]) -> [SpeakerLetterGroup]
}

/// Live adapter over the shared store and the shared speaker index use case. Both are resolved lazily,
/// never in an initializer.
struct LiveSpeakersSource: SpeakersSource {
    private let store: () -> DevFestNantesStore
    private let speakerIndex: () -> SpeakerIndexUseCase

    init(
        store: @escaping () -> DevFestNantesStore = { IosDependencies.shared.store() },
        speakerIndex: @escaping () -> SpeakerIndexUseCase = { IosDependencies.shared.speakerIndex() }
    ) {
        self.store = store
        self.speakerIndex = speakerIndex
    }

    func speakers() -> AsyncThrowingStream<[Speaker], Error> {
        let resolvedStore = store()
        return KotlinStreamBridge.stream(resolvedStore.speakers)
    }

    func letterGroups(speakers: [Speaker]) -> [SpeakerLetterGroup] {
        speakerIndex().letterGroups(speakers: speakers)
    }
}
