import Testing
import Foundation
@testable import DevFest_Nantes

/// Pins how `AgendaViewModel` turns a session start date string into a `Date`.
/// Calls the static `parseDate` only: creating a ViewModel would start Koin.
/// `AgendaViewModel` is main-actor isolated, so the suite runs on the main actor.
@MainActor @Suite struct AgendaDateParsingTests {

    private let expected = Date(timeIntervalSince1970: 1760598000)

    @Test func parses_an_iso8601_start_date_with_an_offset() {
        #expect(AgendaViewModel.parseDate("2025-10-16T09:00:00+02:00") == expected)
    }

    @Test func parses_an_iso8601_start_date_in_utc() {
        #expect(AgendaViewModel.parseDate("2025-10-16T07:00:00Z") == expected)
    }

    @Test func falls_back_to_now_for_an_unparseable_date() {
        let parsed = AgendaViewModel.parseDate("not a date")

        #expect(abs(parsed.timeIntervalSinceNow) < 5)
    }

    @Test func falls_back_to_now_for_an_empty_date() {
        let parsed = AgendaViewModel.parseDate("")

        #expect(abs(parsed.timeIntervalSinceNow) < 5)
    }

    // Pins v1.0 behavior: the default-option ISO8601DateFormatter rejects fractional
    // seconds, so such a start date falls back to now. Known parse gap, logged as debt in
    // .planning/phases/06-ios-test-foundation-and-ci-gates/deferred-items.md and not
    // fixed in this phase (parity).
    @Test func falls_back_to_now_for_fractional_seconds() {
        let parsed = AgendaViewModel.parseDate("2025-10-16T09:00:00.000+02:00")

        #expect(abs(parsed.timeIntervalSinceNow) < 5)
    }
}
