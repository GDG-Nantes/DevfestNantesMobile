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

extension Session: Identifiable { }
