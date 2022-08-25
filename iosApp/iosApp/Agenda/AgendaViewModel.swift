//
//  AgendaViewModel.swift
//  iosApp
//
//  Created by Stéphane Rihet on 11/08/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared
import Combine
import KMPNativeCoroutinesCore
import KMPNativeCoroutinesAsync
import NSLogger

extension Session: Identifiable { }

class AgendaViewModel: ObservableObject, Identifiable {
        
    let store : DevFestNantesStore
    @Published var content: Content = Content(sections: [])
    
    
    init() {
        self.store = DevFestNantesStoreMocked()
    }
    
    func getDate(date: String) -> Date {
        let newFormatter = ISO8601DateFormatter()
        return newFormatter.date(from: date) ?? Date()// replace Date String
    }
    
    var sectionDateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-d"
        return formatter
    }()
    
    func observeSessions() async {
        do {
            let stream = asyncStream(for: store.sessionsNative)
            for try await data in stream {
                self.sessionsChanged(sessions: data)
            }
        } catch {
            print("Failed with error: \(error)")
        }
    }
    
    private func sessionsChanged(sessions: [Session]) {
        let groupedSessions = Dictionary(grouping: sessions) { getDate(date: $0.scheduleSlot.startDate) }
        let sortedKeys = groupedSessions.keys.sorted()
        var sections = [Content.Section]()
        sortedKeys.forEach { date in
            let sessions = groupedSessions[date]!
                .map { Content.Session(from: $0) }
                .sorted { $0.room < $1.room }
            sections.append(Content.Section(
                date: date,
                day: self.sectionDateFormatter.string(from: date),
                sessions: sessions))
        }
        content.sections = sections
    }
    
}

extension AgendaViewModel.Content.Session {
    
    init(from session: Session) {
        let newFormatter = ISO8601DateFormatter()
        self.init(id: session.id, abstract: session.abstract, category: session.category, language: session.language, complexity: session.complexity, openFeedbackFormId: session.openFeedbackFormId, speakers: session.speakers, room: session.room.name, date: newFormatter.date(from: session.scheduleSlot.startDate) ?? Date(), startDate: session.scheduleSlot.startDate, endDate: session.scheduleSlot.endDate, title: session.title)
    }
}

extension AgendaViewModel {
    struct Content {
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
            let title: String
        }
        
        struct Section: Hashable {
            let date: Date
            let day: String
            var sessions: [Session]
        }
        
        var sections: [Section]
    }

}

