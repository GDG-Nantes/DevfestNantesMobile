import Testing
import Foundation
import shared
@testable import DevFest_Nantes

/// Pins the v1.0 iOS agenda behavior (grouping, filtering, classification) on the recorded snapshot
/// (core/domain/testdata/agenda). The code under test is the unchanged app code: the grouping body of
/// `AgendaViewModel.sessionsChanged`, the D-10 statics of `AgendaView`, and `AgendaContent.Session.isATalk`.
/// Never calls `observe*`, `toggleFavorite`, `clearFilters` nor assigns `sessionFilters` (side effects).
/// The app filter type is always written `DevFest_Nantes.SessionFilter` (D-13).
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

    private func assertGrouping(zoneId: String, golden: String) throws {
        TestHost.start()
        let sessions = try TestData.sessions()
        let viewModel = AgendaViewModel()
        viewModel.sectionDateFormatter.timeZone = try #require(TimeZone(identifier: zoneId))
        viewModel.sessionsChanged(sessions: sessions)

        let isoFormatter = ISO8601DateFormatter()
        var days: [GoldenDay] = []
        for (offset, tab) in Self.tabs.enumerated() {
            let sections = AgendaView.sections(viewModel.agendaContent.sections, forDay: tab)
            let slots = sections.map { section -> GoldenSlot in
                for session in section.sessions {
                    #expect(AgendaViewModel.parseDate(session.startDate) == section.date)
                }
                // Canonical order: Swift sort is not stable among equal room names.
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

    @Test func filtering_matches_the_golden_for_every_case() throws {
        let file = try TestData.filterCases()
        let all = try TestData.sessions().map { AgendaContent.Session(from: $0) }

        let cases = try file.cases.map { filterCase -> GoldenCase in
            let filters = Set(try filterCase.filters.map { filter -> DevFest_Nantes.SessionFilter in
                guard let type = DevFest_Nantes.FilterType(rawValue: filter.type.lowercased()) else {
                    throw TestDataError.unknownName(kind: "FilterType", name: filter.type)
                }
                return DevFest_Nantes.SessionFilter(type: type, value: filter.value)
            })
            let ids = AgendaView
                .filteredSessions(all, filters: filters, favorites: file.bookmarkedSessionIds)
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
            let type = try name.map(TestData.sessionType)
            let session = Session(
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
                type: type
            )
            #expect(AgendaContent.Session(from: session).isATalk == expected, "isATalk(\(name ?? "nil"))")
        }
    }
}
