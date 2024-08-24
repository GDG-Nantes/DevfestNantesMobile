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
        let language: SessionLanguage?
        let complexity: Complexity?
        let openFeedbackFormId: String?
        let speakers: [Speaker_]
        let room: String
        let date: Date
        let startDate: String
        let endDate: String
        let durationAndLanguage: String
        let title: String
        let sessionType: SessionType?
        
        var isATalk: Bool {
            guard let sessionType = sessionType else { return false }
            switch sessionType {
            case .lunch, .opening, .break_:
                return false
            default:
                return true
            }
        }
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
    //Initialization session
    init(from session: Session_) {
        let newFormatter = ISO8601DateFormatter()
        self.init(id: session.id, abstract: session.abstract, category: session.category, language: session.language, complexity: session.complexity, openFeedbackFormId: session.openFeedbackFormId, speakers: session.speakers, room: session.room?.name ?? "", date: newFormatter.date(from: session.scheduleSlot.startDate) ?? Date(), startDate: session.scheduleSlot.startDate, endDate: session.scheduleSlot.endDate,durationAndLanguage: session.getDurationAndLanguageString(), title: session.title, sessionType: session.type)
    }
}

extension Session: Identifiable { }
