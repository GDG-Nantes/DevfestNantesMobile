//
//  AgendaContent.swift
//  iosApp
//
//  Created by Stéphane Rihet on 15/09/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared

struct AgendaContent {
    struct Session: Hashable, Identifiable {
        let id: String
        let abstract: String
        let category: shared.Category?
        let language: SessionLanguage?
        let complexity: Complexity?
        let openFeedbackFormId: String
        let speakers: [Speaker]
        let room: String
        let date: Date
        let startDate: String
        let endDate: String
        let durationAndLanguage: String
        let title: String
    }
    
    struct Section: Hashable {
        let date: Date
        let day: String
        var sessions: [Session]
    }
    
    var sections: [Section]
}

 extension AgendaContent.Session {
    
    init(from session: Session) {
        let newFormatter = ISO8601DateFormatter()
        self.init(id: session.id, abstract: session.abstract, category: session.category, language: session.language, complexity: session.complexity, openFeedbackFormId: session.openFeedbackFormId, speakers: session.speakers, room: session.room?.name ?? "", date: newFormatter.date(from: session.scheduleSlot.startDate) ?? Date(), startDate: session.scheduleSlot.startDate, endDate: session.scheduleSlot.endDate,durationAndLanguage: session.getDurationAndLanguageString(), title: session.title)
    }
}
