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
}
