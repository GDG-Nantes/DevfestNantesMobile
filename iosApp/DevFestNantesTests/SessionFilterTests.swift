import Testing
import Foundation
@testable import DevFest_Nantes

@Suite struct SessionFilterTests {

    /// Pins the on-disk format written under the `AgendaSessionFilters` UserDefaults key:
    /// a JSON array of `{type, value}` objects.
    @Test func decodes_the_legacy_agenda_session_filters_payload() throws {
        let payload = Data(
            """
            [{"type":"room","value":"Jules Verne"},{"type":"bookmark","value":"bookmark"},{"type":"language","value":"fr"}]
            """.utf8
        )

        let decoded = try JSONDecoder().decode(Set<SessionFilter>.self, from: payload)

        #expect(
            decoded == [
                SessionFilter(type: .room, value: "Jules Verne"),
                SessionFilter(type: .bookmark, value: "bookmark"),
                SessionFilter(type: .language, value: "fr"),
            ]
        )
    }

    @Test func decodes_an_empty_legacy_payload_to_no_filters() throws {
        let decoded = try JSONDecoder().decode(Set<SessionFilter>.self, from: Data("[]".utf8))

        #expect(decoded.isEmpty)
    }

    @Test func collapses_duplicate_filters_from_a_legacy_payload() throws {
        let payload = Data(
            """
            [{"type":"room","value":"Jules Verne"},{"type":"room","value":"Jules Verne"}]
            """.utf8
        )

        let decoded = try JSONDecoder().decode(Set<SessionFilter>.self, from: payload)

        #expect(decoded == [SessionFilter(type: .room, value: "Jules Verne")])
    }

    @Test(arguments: [FilterType.bookmark, .complexity, .language, .room, .type])
    func round_trips_every_filter_type(type: FilterType) throws {
        let filters: Set<SessionFilter> = [SessionFilter(type: type, value: "value")]

        let data = try JSONEncoder().encode(filters)
        let decoded = try JSONDecoder().decode(Set<SessionFilter>.self, from: data)

        #expect(decoded == filters)
    }

    @Test func encodes_a_filter_with_the_legacy_type_and_value_keys() throws {
        let filters: Set<SessionFilter> = [SessionFilter(type: .complexity, value: "Beginner")]

        let data = try JSONEncoder().encode(filters)
        let decoded = try JSONDecoder().decode([[String: String]].self, from: data)

        #expect(decoded == [["type": "complexity", "value": "Beginner"]])
    }

    @Test func rejects_an_unknown_filter_type() {
        let payload = Data(
            """
            [{"type":"track","value":"x"}]
            """.utf8
        )

        #expect(throws: DecodingError.self) {
            try JSONDecoder().decode(Set<SessionFilter>.self, from: payload)
        }
    }
}
