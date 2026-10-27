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

    // Known parse gap (v1.0 behavior): `parseDate` delegates to a default-option
    // ISO8601DateFormatter, which rejects fractional seconds, so such a start date falls back
    // to now. Logged as debt in
    // .planning/phases/06-ios-test-foundation-and-ci-gates/deferred-items.md and not fixed in
    // this phase (parity). The expectation is derived from the platform formatter instead of
    // hard-coding its answer, so a newer iOS runtime that starts accepting fractional seconds
    // does not break CI: the test then checks the parsed instant instead of the fallback.
    @Test func matches_the_default_iso8601_formatter_for_fractional_seconds() {
        let input = "2025-10-16T09:00:00.000+02:00"
        let parsed = AgendaViewModel.parseDate(input)

        if let platformDate = ISO8601DateFormatter().date(from: input) {
            #expect(parsed == platformDate)
        } else {
            #expect(abs(parsed.timeIntervalSinceNow) < 5)
        }
    }
}
