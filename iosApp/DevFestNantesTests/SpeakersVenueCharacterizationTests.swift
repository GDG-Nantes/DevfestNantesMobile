import Testing
import Foundation
import shared
@testable import DevFest_Nantes

/// Pins the v1.0 iOS speaker alphabet and venue language (unchanged app code) on the recorded snapshot.
/// ViewModels are only created through their initializers after `IosDependencies.shared.start()`;
/// no `observe*` call (network) is made.
@MainActor @Suite struct SpeakersVenueCharacterizationTests {

    @Test func speaker_alphabet_matches_the_golden() throws {
        TestHost.start()
        let viewModel = SpeakersViewModel()
        viewModel.speakersContent = try TestData.speakers()

        let letters = viewModel.getAlphabets().map { letter in
            SpeakerIndexGolden.Letter(
                letter: letter,
                speakerIds: viewModel.speakers(for: letter).map(\.id)
            )
        }

        #expect(!letters.isEmpty)
        try Golden.assertMatches(
            "agenda/golden/ios-speaker-index.json",
            SpeakerIndexGolden(source: "ios-v1.0", letters: letters)
        )
    }

    // Shared table (identical to VenueLanguageUseCaseTest): nil, "en", "de", "" -> english; "fr" -> french.
    // `Locale.current` cannot be injected without changing the app, so only the row selected by the run's
    // language code is reachable here; the other rows are pinned on the Kotlin side and by the Android table.
    @Test func venue_language_matches_the_shared_table_for_the_run_locale() {
        TestHost.start()
        let table: [(String?, ContentLanguage)] = [
            (nil, .english),
            ("fr", .french),
            ("en", .english),
            ("de", .english),
            ("", .english),
        ]
        let code = Locale.current.languageCode
        let expected = table.first { $0.0 == code }?.1 ?? .english

        #expect(VenueViewModel().currentLanguage == expected, "language code: \(code ?? "nil")")
    }
}
