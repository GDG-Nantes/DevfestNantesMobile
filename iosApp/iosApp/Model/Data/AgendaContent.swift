//
//  AgendaContent.swift
//  iosApp
//
//  Created by Stéphane Rihet on 15/09/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared

// MARK: - AgendaContent
struct AgendaContent {
    struct Session: Hashable, Identifiable {
        let id: String
        let abstract: String
        let category: shared.Category?
        let language: shared.SessionLanguage?
        let complexity: shared.Complexity?
        let openFeedbackFormId: String?
        let speakers: [Speaker]
        let room: String
        let date: Date
        let startDate: String
        let endDate: String
        let durationAndLanguage: String
        let title: String
        let sessionType: SessionType?
        let isATalk: Bool
        let isNavigable: Bool
    }
    
// MARK: - Section
    struct Section: Hashable {
        let date: Date
        let day: String
        var sessions: [Session]
    }
    
    var sections: [Section]
}

extension AgendaContent.Session {
    /// Maps a shared session with the classification already decided by the shared use case (D-07).
    init(from session: Session, isATalk: Bool, isNavigable: Bool) {
        let newFormatter = ISO8601DateFormatter()
        self.init(id: session.id, abstract: session.abstract, category: session.category, language: session.language, complexity: session.complexity, openFeedbackFormId: session.openFeedbackFormId, speakers: session.speakers, room: session.room?.name ?? "", date: newFormatter.date(from: session.scheduleSlot.startDate) ?? Date(), startDate: session.scheduleSlot.startDate, endDate: session.scheduleSlot.endDate,durationAndLanguage: session.getDurationAndLanguageString(), title: session.title, sessionType: session.type, isATalk: isATalk, isNavigable: isNavigable)
    }
}

extension AgendaContent {
    /// Thin display mapping from the shared output (D-07): one section per time slot, in the shared order.
    /// The grouping, the filtering and the room order are already decided by the shared use case, so nothing
    /// is sorted or filtered here. Inside this extension the unqualified `Session` is the nested struct.
    init(days: [DaySchedule], isTalk: (shared.Session) -> Bool, isNavigable: (shared.Session) -> Bool) {
        self.init(sections: days.flatMap { day in
            day.slots.map { slot in
                Section(
                    date: Date(timeIntervalSince1970: TimeInterval(slot.start.epochSeconds)),
                    day: day.date,
                    sessions: slot.sessions.map {
                        AgendaContent.Session(from: $0, isATalk: isTalk($0), isNavigable: isNavigable($0))
                    }
                )
            }
        })
    }
}

extension Session: Identifiable { }

// MARK: - AgendaDays

/// The conference dates (`yyyy-MM-dd`), declared once. Still hardcoded per year (07 D-07, single-source debt):
/// the day picker, the initial day and the scroll guard of `AgendaView` read these, and so does the shared
/// grouping through `LocalDateDays`.
enum AgendaDays {
    static let day1 = "2025-10-16"
    static let day2 = "2025-10-17"

    static var all: [String] { [day1, day2] }
}
