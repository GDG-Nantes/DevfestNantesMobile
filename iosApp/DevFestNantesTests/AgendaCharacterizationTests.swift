import Testing
import Foundation
import shared
@testable import DevFest_Nantes

/// Pins the shared agenda output against the v1.0 iOS goldens on the recorded snapshot
/// (core/domain/testdata/agenda). The grouping goldens are asserted on the sections that
/// `AgendaContent(days:isTalk:isNavigable:)` maps from the shared `GroupSessionsUseCase`, the filtering golden
/// on the shared `FilterSessionsUseCase`, and the `isATalk`/`isNavigable` flags come from the shared
/// `ClassifySessionUseCase` (the tables below are literal copies of its test table). The goldens are the v1.0
/// oracle and are never edited.
/// Never calls `observe*`, `toggleBookmark`, `toggleFilter` nor `clearFilters` (side effects).
/// The Kotlin filter type is always written `shared.SessionFilter`: the app still declares its own
/// `SessionFilter` struct.
@MainActor @Suite struct AgendaCharacterizationTests {

    private static let tabs = ["2025-10-16", "2025-10-17"]

    @Test func snapshot_loads_every_session() throws {
        let dtos = try JSONDecoder().decode(
            [TestData.SessionDTO].self,
            from: TestData.data("agenda/sessions.snapshot.json")
        )
        let sessions = try TestData.sessions()

        #expect(sessions.count == dtos.count)
        #expect(sessions.count == 94)
        #expect(sessions.map(\.id) == dtos.map(\.id))
    }

    @Test func grouping_in_europe_paris_matches_the_golden() throws {
        try assertGrouping(zoneId: "Europe/Paris", golden: "agenda/golden/ios-agenda-Europe_Paris.json")
    }

    // In Auckland late day-1 sessions move to the 2025-10-17 tab and late day-2 sessions fall on
    // 2025-10-18, which no tab shows: today's behavior, pinned as is.
    @Test func grouping_in_pacific_auckland_matches_the_golden() throws {
        try assertGrouping(zoneId: "Pacific/Auckland", golden: "agenda/golden/ios-agenda-Pacific_Auckland.json")
    }

    /// The content the agenda screen shows, built the way the app builds it: the shared grouping of the snapshot
    /// mapped by `AgendaContent`, with the classification decided by the shared use case.
    private func content(zoneId: String) throws -> AgendaContent {
        let classifier = ClassifySessionUseCase()
        return AgendaContent(
            days: try TestData.schedules(zoneId: zoneId),
            isTalk: { classifier.isTalk(session: $0) },
            isNavigable: { classifier.isNavigable(session: $0) }
        )
    }

    private func assertGrouping(zoneId: String, golden: String) throws {
        TestHost.start()
        let agenda = try content(zoneId: zoneId)

        let isoFormatter = ISO8601DateFormatter()
        var days: [GoldenDay] = []
        for (offset, tab) in Self.tabs.enumerated() {
            let sections = agenda.sections.filter { $0.day == tab }
            let slots = sections.map { section -> GoldenSlot in
                for session in section.sessions {
                    #expect(isoFormatter.date(from: session.startDate) == section.date)
                }
                // Canonical order: the golden does not pin the room order inside a slot.
                let ids = section.sessions
                    .sorted { ($0.room, $0.id) < ($1.room, $1.id) }
                    .map(\.id)
                return GoldenSlot(start: isoFormatter.string(from: section.date), sessionIds: ids)
            }
            #expect(!slots.isEmpty)
            days.append(GoldenDay(dayIndex: offset + 1, date: tab, slots: slots))
        }

        try Golden.assertMatches(
            golden,
            AgendaGolden(source: "ios-v1.0", timeZone: zoneId, days: days)
        )
    }

    // Edge IOSDATA-03: the mapping never re-sorts, so the order of `TimeSlot.sessions` reaches the screen as is.
    @Test func mapping_keeps_the_shared_order_inside_a_slot() throws {
        TestHost.start()
        let expected = try TestData.schedules(zoneId: "Europe/Paris").flatMap { day in
            day.slots.map { slot in slot.sessions.map(\.id) }
        }
        let actual = try content(zoneId: "Europe/Paris").sections.map { section in section.sessions.map(\.id) }

        #expect(!expected.isEmpty)
        #expect(actual == expected)
    }

    @Test func filtering_matches_the_golden_for_every_case() throws {
        let file = try TestData.filterCases()
        let all = try TestData.sessions()
        let useCase = FilterSessionsUseCase()

        let cases = try file.cases.map { filterCase -> GoldenCase in
            let filters = Set(try filterCase.filters.map { filter -> shared.SessionFilter in
                guard let type = SessionFilterType.entries.first(where: { $0.name == filter.type }) else {
                    throw TestDataError.unknownName(kind: "SessionFilterType", name: filter.type)
                }
                return shared.SessionFilter(type: type, value: filter.value)
            })
            let ids = useCase
                .filter(sessions: all, filters: filters, bookmarkedSessionIds: Set(file.bookmarkedSessionIds))
                .map(\.id)
                .sorted()
            return GoldenCase(name: filterCase.name, sessionIds: ids)
        }

        #expect(cases.count == 15)
        try Golden.assertMatches("agenda/golden/ios-filtering.json", FilteringGolden(source: "ios-v1.0", cases: cases))
    }

    // The room filter compares the stored case value (room id) to the room NAME; this only matches
    // because room.id == room.name on the snapshot.
    @Test func room_filter_values_equal_room_names_on_the_snapshot() throws {
        for session in try TestData.sessions() {
            if let room = session.room {
                #expect(room.id == room.name, "room of \(session.id)")
            }
        }
    }

    // Literal table identical to the isTalk column of ClassifySessionUseCaseTest.
    @Test func is_a_talk_matches_the_shared_table() throws {
        let table: [(String?, Bool)] = [
            ("BREAK", false),
            ("CODELAB", true),
            ("CONFERENCE", true),
            ("KEYNOTE", true),
            ("LUNCH", false),
            ("OPENING", false),
            ("QUICKIE", true),
            (nil, false),
        ]
        let template = try #require(TestData.sessions().first)

        for (name, expected) in table {
            let session = try Self.session(of: name, like: template)
            #expect(AgendaContent.Session(from: session).isATalk == expected, "isATalk(\(name ?? "nil"))")
        }
    }

    // Literal table identical to the isNavigable column of ClassifySessionUseCaseTest.
    @Test func is_navigable_matches_the_shared_table() throws {
        let table: [(String?, Bool)] = [
            ("BREAK", false),
            ("CODELAB", true),
            ("CONFERENCE", true),
            ("KEYNOTE", false),
            ("LUNCH", false),
            ("OPENING", false),
            ("QUICKIE", true),
            (nil, false),
        ]
        let template = try #require(TestData.sessions().first)

        for (name, expected) in table {
            let session = try Self.session(of: name, like: template)
            #expect(AgendaContent.Session(from: session).isNavigable == expected, "isNavigable(\(name ?? "nil"))")
        }
    }

    private static func session(of typeName: String?, like template: Session) throws -> Session {
        Session(
            id: template.id,
            abstract: "",
            category: nil,
            language: nil,
            complexity: nil,
            openFeedbackFormId: nil,
            room: nil,
            scheduleSlot: template.scheduleSlot,
            speakers: [],
            title: template.title,
            type: try typeName.map(TestData.sessionType)
        )
    }
}
