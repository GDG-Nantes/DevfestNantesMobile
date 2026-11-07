import Testing
import Foundation
import shared
@testable import DevFest_Nantes

/// D-11: the scroll to the current slot is a pure function of the slot dates, the time and a calendar.
/// Slot dates come from the shared grouping of the recorded snapshot (core/domain/testdata/agenda); the time
/// is injected and the calendar is an explicit Europe/Paris one, because CI simulators run in UTC.
@MainActor @Suite struct AgendaScrollTargetTests {

    private static let paris: Calendar = {
        var calendar = Calendar(identifier: .gregorian)
        calendar.timeZone = TimeZone(identifier: "Europe/Paris")!
        return calendar
    }()

    private static let isoFormatter = ISO8601DateFormatter()

    @Test func now_before_the_first_slot_has_no_target() throws {
        let slots = try Self.slotDates(dayIndex: 1)

        #expect(try Self.target(slots, now: "2025-10-16T05:59:00Z") == nil)
    }

    @Test func now_exactly_on_a_slot_targets_that_slot() throws {
        let slots = try Self.slotDates(dayIndex: 1)

        #expect(try Self.target(slots, now: "2025-10-16T10:00:00Z") == Self.date("2025-10-16T10:00:00Z"))
    }

    @Test func now_between_two_slots_targets_the_previous_slot() throws {
        let slots = try Self.slotDates(dayIndex: 1)

        #expect(try Self.target(slots, now: "2025-10-16T10:30:00Z") == Self.date("2025-10-16T10:00:00Z"))
    }

    @Test func now_after_the_last_slot_targets_the_last_slot() throws {
        let slots = try Self.slotDates(dayIndex: 1)

        #expect(try Self.target(slots, now: "2025-10-16T21:00:00Z") == Self.date("2025-10-16T16:30:00Z"))
    }

    @Test func second_day_uses_its_own_slots() throws {
        let slots = try Self.slotDates(dayIndex: 2)

        #expect(try Self.target(slots, now: "2025-10-17T06:29:00Z") == nil)
        #expect(try Self.target(slots, now: "2025-10-17T10:00:00Z") == Self.date("2025-10-17T09:20:00Z"))
    }

    // MARK: - Helpers

    private static func slotDates(dayIndex: Int) throws -> [Date] {
        let day = try #require(TestData.schedules(zoneId: "Europe/Paris").first { $0.dayIndex == Int32(dayIndex) })
        return day.slots.map { Date(timeIntervalSince1970: TimeInterval($0.start.epochSeconds)) }
    }

    private static func target(_ slotDates: [Date], now: String) throws -> Date? {
        AgendaView.scrollTarget(slotDates: slotDates, now: try date(now), calendar: paris)
    }

    private static func date(_ iso: String) throws -> Date {
        try #require(isoFormatter.date(from: iso))
    }
}
