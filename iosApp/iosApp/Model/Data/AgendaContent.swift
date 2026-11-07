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

    /// Transitional: fills both flags with the v1.0 rules for the callers that have no source yet.
    /// 09-06 deletes it with its last app call site.
    init(from session: Session) {
        let isATalk: Bool
        let isNavigable: Bool
        switch session.type {
        case .none:
            isATalk = false
            isNavigable = false
        case .some(let type):
            switch type {
            case .lunch, .opening, .break_:
                isATalk = false
            default:
                isATalk = true
            }
            switch type {
            case .quickie, .conference, .codelab:
                isNavigable = true
            default:
                isNavigable = false
            }
        }
        self.init(from: session, isATalk: isATalk, isNavigable: isNavigable)
    }
}

extension Session: Identifiable { }
