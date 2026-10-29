import Testing
import Foundation
import shared
@testable import DevFest_Nantes

// Support for the Phase 7 characterization suites: loads the shared recorded snapshot
// (core/domain/testdata/agenda) into Kotlin model values and reads/records the iOS goldens.
// Rule (agenda/README.md): refer to the app's filter type as `DevFest_Nantes.SessionFilter` in every
// test file that imports `shared`, so the Kotlin `SessionFilter` export cannot make it ambiguous.

// MARK: - Test host

@MainActor enum TestHost {
    /// Idempotent: the host app already started Koin at launch; Kotlin starts it only when absent.
    static func start() {
        IosDependencies.shared.start()
    }
}

// MARK: - Snapshot loading

enum TestDataError: Error, CustomStringConvertible {
    case unknownName(kind: String, name: String)

    var description: String {
        switch self {
        case let .unknownName(kind, name): return "unknown \(kind) name in snapshot: \(name)"
        }
    }
}

enum TestData {
    /// Repository root: this file is <root>/iosApp/DevFestNantesTests/CharacterizationSupport.swift.
    static let repositoryRoot = URL(fileURLWithPath: #filePath)
        .deletingLastPathComponent()
        .deletingLastPathComponent()
        .deletingLastPathComponent()

    static let root = repositoryRoot.appendingPathComponent("core/domain/testdata")

    static func data(_ relativePath: String) throws -> Data {
        try Data(contentsOf: root.appendingPathComponent(relativePath))
    }

    // MARK: Snapshot DTOs (format of agenda/README.md)

    struct RoomDTO: Decodable {
        let id: String
        let name: String
        let sortIndex: Int32
    }

    struct SessionDTO: Decodable {
        let id: String
        let title: String
        let type: String?
        let language: String?
        let complexity: String?
        let room: RoomDTO?
        let startDate: String
        let endDate: String
        let speakerIds: [String]
    }

    struct SpeakerDTO: Decodable {
        let id: String
        let name: String
    }

    // MARK: Kotlin values

    static func speakers() throws -> [Speaker] {
        try JSONDecoder().decode([SpeakerDTO].self, from: data("agenda/speakers.snapshot.json")).map {
            Speaker(
                id: $0.id,
                bio: nil,
                company: nil,
                companyLogoUrl: nil,
                city: nil,
                name: $0.name,
                photoUrl: nil,
                socials: nil
            )
        }
    }

    /// Decoded sessions carry speakers by id only (no name resolution, as in the snapshot contract).
    static func sessions() throws -> [Session] {
        try JSONDecoder().decode([SessionDTO].self, from: data("agenda/sessions.snapshot.json")).map { dto in
            Session(
                id: dto.id,
                abstract: "",
                category: nil,
                language: try dto.language.map(sessionLanguage),
                complexity: try dto.complexity.map(complexity),
                openFeedbackFormId: nil,
                room: dto.room.map { Room(id: $0.id, name: $0.name, sortIndex: $0.sortIndex) },
                scheduleSlot: ScheduleSlot(endDate: dto.endDate, startDate: dto.startDate),
                speakers: dto.speakerIds.map {
                    Speaker(
                        id: $0,
                        bio: nil,
                        company: nil,
                        companyLogoUrl: nil,
                        city: nil,
                        name: "",
                        photoUrl: nil,
                        socials: nil
                    )
                },
                title: dto.title,
                type: try dto.type.map(sessionType)
            )
        }
    }

    static func sessionType(_ name: String) throws -> SessionType {
        switch name {
        case "BREAK": return .break_
        case "CODELAB": return .codelab
        case "CONFERENCE": return .conference
        case "KEYNOTE": return .keynote
        case "LUNCH": return .lunch
        case "OPENING": return .opening
        case "QUICKIE": return .quickie
        default: throw TestDataError.unknownName(kind: "SessionType", name: name)
        }
    }

    static func sessionLanguage(_ name: String) throws -> SessionLanguage {
        switch name {
        case "FRENCH": return .french
        case "ENGLISH": return .english
        default: throw TestDataError.unknownName(kind: "SessionLanguage", name: name)
        }
    }

    static func complexity(_ name: String) throws -> Complexity {
        switch name {
        case "BEGINNER": return .beginner
        case "INTERMEDIATE": return .intermediate
        case "ADVANCED": return .advanced
        default: throw TestDataError.unknownName(kind: "Complexity", name: name)
        }
    }

    // MARK: Filter cases

    struct FilterCasesFile: Decodable {
        struct Filter: Decodable {
            let type: String
            let value: String
        }

        struct Case: Decodable {
            let name: String
            let filters: [Filter]
        }

        let bookmarkedSessionIds: [String]
        let cases: [Case]
    }

    static func filterCases() throws -> FilterCasesFile {
        try JSONDecoder().decode(FilterCasesFile.self, from: data("agenda/filter-cases.json"))
    }
}

// MARK: - Golden shapes (agenda/README.md)

struct AgendaGolden: Encodable {
    let source: String
    let timeZone: String
    let days: [GoldenDay]
}

struct GoldenDay: Encodable {
    let dayIndex: Int
    let date: String
    let slots: [GoldenSlot]
}

struct GoldenSlot: Encodable {
    let start: String
    let sessionIds: [String]
}

struct FilteringGolden: Encodable {
    let source: String
    let cases: [GoldenCase]
}

struct GoldenCase: Encodable {
    let name: String
    let sessionIds: [String]
}

struct SpeakerIndexGolden: Encodable {
    struct Letter: Encodable {
        let letter: String
        let speakerIds: [String]
    }

    let source: String
    let letters: [Letter]
}

// MARK: - Golden read / compare / record

enum Golden {
    /// Explicit record mode: `TEST_RUNNER_DEVFEST_RECORD_GOLDENS=true xcodebuild ... test-without-building`
    /// reaches the hosted test process as `DEVFEST_RECORD_GOLDENS`.
    static var isRecordMode: Bool {
        ProcessInfo.processInfo.environment["DEVFEST_RECORD_GOLDENS"] == "true"
    }

    static func encode(_ value: some Encodable) throws -> String {
        let encoder = JSONEncoder()
        encoder.outputFormatting = [.prettyPrinted, .sortedKeys, .withoutEscapingSlashes]
        return String(decoding: try encoder.encode(value), as: UTF8.self) + "\n"
    }

    /// Present golden: exact string comparison. Absent golden: fails with "golden missing" and writes
    /// nothing, except in record mode where it is written once (and the test still fails with
    /// "golden recorded, re-run"). An existing golden is never overwritten.
    static func assertMatches(
        _ relativePath: String,
        _ value: some Encodable,
        sourceLocation: SourceLocation = #_sourceLocation
    ) throws {
        let url = TestData.root.appendingPathComponent(relativePath)
        let actual = try encode(value)

        if FileManager.default.fileExists(atPath: url.path) {
            let expected = try String(contentsOf: url, encoding: .utf8)
            #expect(actual == expected, "golden differs: \(relativePath)", sourceLocation: sourceLocation)
            return
        }

        guard isRecordMode else {
            Issue.record("golden missing: \(relativePath)", sourceLocation: sourceLocation)
            return
        }

        try FileManager.default.createDirectory(
            at: url.deletingLastPathComponent(),
            withIntermediateDirectories: true
        )
        try actual.write(to: url, atomically: true, encoding: .utf8)
        Issue.record("golden recorded, re-run: \(relativePath)", sourceLocation: sourceLocation)
    }
}
