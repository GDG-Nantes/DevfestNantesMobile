import Foundation

enum FilterType: String, Codable {
    case bookmark, complexity, language, room, type
}

struct SessionFilter: Codable, Hashable, Equatable {
    let type: FilterType
    let value: String

    func hash(into hasher: inout Hasher) {
        hasher.combine(type)
        hasher.combine(value)
    }

    static func == (lhs: SessionFilter, rhs: SessionFilter) -> Bool {
        lhs.type == rhs.type && lhs.value == rhs.value
    }
}
