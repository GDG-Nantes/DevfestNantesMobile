//
//  AgendaView.swift
//  iosApp
//
//  Created by Stéphane Rihet on 10/07/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import shared

struct AgendaView: View {
    @ObservedObject var viewModel = AgendaViewModel()
    
    @State private var day = "2025-10-16"
    @State private var showFavoritesOnly = false
    @State private var selectedRoom: Room_?
    @State private var selectedComplexity: Complexity?
    @State private var selectedLanguage: SessionLanguage?
    @State private var selectedSessionType: SessionType?
    @State private var scrollToSectionID: Date? = nil
    @State private var firstAppear = true
    
    var sectionTimeFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter
    }()
    
    var body: some View {
        NavigationView {
            LoadingView(isShowing: $viewModel.isLoading) {
                ScrollViewReader { proxy in
                    VStack {
                        Picker("What is the day?", selection: $day) {
                            Text(L10n.day1).tag("2025-10-16")
                            Text(L10n.day2).tag("2025-10-17")
                        }
                        .pickerStyle(SegmentedPickerStyle())

                        List {
                            ForEach(viewModel.agendaContent.sections.filter { $0.day.contains(day) }, id: \.date) { section in
                                let filteredSessions = getFilteredSessions(sessions: section.sessions)
                                let sessionsToShow = showFavoritesOnly ? filteredSessions.filter { viewModel.favorites.contains($0.id) } : filteredSessions
                                
                                if !sessionsToShow.isEmpty {
                                    Section(header: Text(sectionTimeFormatter.string(from: section.date)).id(section.date)) {
                                        ForEach(sessionsToShow, id: \.id) { session in
                                            if session.sessionType == .conference || session.sessionType == .codelab || session.sessionType == .quickie {
                                                NavigationLink(destination: AgendaDetailView(session: session, day: day)) {
                                                    AgendaCellView(session: session)
                                                }
                                            } else {
                                                AgendaCellView(session: session)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        .onChange(of: scrollToSectionID) { newID in
                            if let newID = newID {
                                proxy.scrollTo(newID, anchor: .top)
                            }
                        }
                    }
                }
            }
            .onChange(of: viewModel.isLoading) { isLoading in
                if !isLoading {
                    let formatter = DateFormatter()
                    formatter.dateFormat = "yyyy-MM-dd"
                    let currentDay = formatter.string(from: Date())
                    
                    if currentDay == day && firstAppear {
                        let calendar = Calendar.current
                        let now = Date()
                        let nowHour = calendar.component(.hour, from: now)
                        let nowMinute = calendar.component(.minute, from: now)
                        let nowTotalMinutes = nowHour * 60 + nowMinute
                        
                        let sortedSections = viewModel.agendaContent.sections
                            .filter({ $0.day.contains(day) })
                            .sorted(by: { section1, section2 in
                                let hour1 = calendar.component(.hour, from: section1.date)
                                let minute1 = calendar.component(.minute, from: section1.date)
                                let totalMinutes1 = hour1 * 60 + minute1
                                
                                let hour2 = calendar.component(.hour, from: section2.date)
                                let minute2 = calendar.component(.minute, from: section2.date)
                                let totalMinutes2 = hour2 * 60 + minute2
                                
                                return totalMinutes1 < totalMinutes2
                            })
                        
                        var previousSection: AgendaContent.Section?
                        
                        for section in sortedSections {
                            let hour = calendar.component(.hour, from: section.date)
                            let minute = calendar.component(.minute, from: section.date)
                            let sectionTotalMinutes = hour * 60 + minute
                            
                            if sectionTotalMinutes <= nowTotalMinutes {
                                previousSection = section
                            } else {
                                break
                            }
                        }
                        
                        if let previousSection = previousSection {
                            DispatchQueue.main.async {
                                scrollToSectionID = previousSection.date
                            }
                        }
                        
                        firstAppear = false
                    }
                }
            }

            .navigationBarTitle(L10n.screenAgenda)
            .navigationBarItems(trailing:
                                    Menu("\(Image(systemName: "line.3.horizontal.decrease.circle"))") {
                let selected = Binding(
                    get: { self.showFavoritesOnly },
                    set: { self.showFavoritesOnly = $0 == self.showFavoritesOnly ? false : $0 }
                )
                Picker("", selection: selected) {
                    Text(L10n.filterFavorites).tag(true)
                    Menu(L10n.sessionFiltersDrawerLanguagesLabel) {
                        let selected = Binding(
                            get: { self.selectedLanguage },
                            set: { self.selectedLanguage = $0 == self.selectedLanguage ? nil : $0 })
                        Picker(L10n.sessionFiltersDrawerLanguagesLabel, selection: selected) {
                            Text(L10n.languageFrench).tag(Optional(SessionLanguage.french))
                            Text(L10n.languageEnglish).tag(Optional(SessionLanguage.english))
                        }
                    }
                    Menu(L10n.sessionFiltersDrawerComplexityLabel) {
                        let selected = Binding(
                            get: { self.selectedComplexity },
                            set: { self.selectedComplexity = $0 == self.selectedComplexity ? nil : $0 })
                        Picker(L10n.sessionFiltersDrawerComplexityLabel, selection: selected) {
                            Text(L10n.complexityBeginer).tag(Optional(Complexity.beginner))
                            Text(L10n.complexityIntermediate).tag(Optional(Complexity.intermediate))
                            Text(L10n.complexityAdvanced).tag(Optional(Complexity.advanced))
                        }
                    }
                    if let rooms = viewModel.roomsContent {
                        Menu(L10n.sessionFiltersDrawerRoomsLabel) {
                            let selected = Binding(
                                get: { self.selectedRoom },
                                set: { self.selectedRoom = $0 == self.selectedRoom ? nil : $0 })
                            Picker(L10n.sessionFiltersDrawerRoomsLabel, selection: selected) {
                                ForEach(rooms, id: \.id) { room in
                                    Text(room.name).tag(Optional(room))
                                }
                            }
                        }}
                    Menu(L10n.sessionFiltersDrawerTypeLabel) {
                        let selected = Binding(
                            get: { self.selectedSessionType },
                            set: { self.selectedSessionType = $0 == self.selectedSessionType ? nil : $0 })
                        Picker(L10n.sessionFiltersDrawerTypeLabel, selection: selected) {
                            Text(L10n.sessionTypeConference).tag(Optional(SessionType.conference))
                            Text(L10n.sessionTypeQuickie).tag(Optional(SessionType.quickie))
                            Text(L10n.sessionTypeCodelab).tag(Optional(SessionType.codelab))
                        }
                    }
                }
                ///Adds remove button filters
                if showFavoritesOnly || selectedRoom != nil || selectedLanguage != nil || selectedComplexity != nil || selectedSessionType != nil {
                    Button(action: {
                        self.showFavoritesOnly = false
                        self.selectedRoom = nil
                        self.selectedLanguage = nil
                        self.selectedSessionType = nil
                        self.selectedComplexity = nil
                    }) {
                        Label(L10n.filterClear, systemImage: "trash")
                    }
                }
                
            })
            .task {
                RCValues.sharedInstance.fetchCloudValues()
                await viewModel.observeRooms()
                await viewModel.observeSessions()
            }
        }
        
        .onAppear {
            
            if scrollToSectionID == nil {
                scrollToSectionID = viewModel.agendaContent.sections.first?.date
            }
            
            let formatter = DateFormatter()
            formatter.dateFormat = "yyyy-MM-dd"
            let currentDay = formatter.string(from: Date())

            
            if currentDay == "2025-10-16" {
                day = "2025-10-16"
            } else if currentDay == "2025-10-17" {
                day = "2025-10-17"
            } else {
                day = "2025-10-16"
            }
            
            
            FirebaseAnalyticsService.shared.pageEvent(page: AnalyticsPage.agenda, className: "AgendaView")
        }
    }
    
    private func getFilteredSessions(sessions: [AgendaContent.Session]) -> [AgendaContent.Session] {
        var selectedSession: [AgendaContent.Session] = sessions
        if let unwrappedLanguage = selectedLanguage {
            selectedSession = selectedSession.filter { $0.language == unwrappedLanguage }
        }
        if let unwrappedSessionType = selectedSessionType {
            selectedSession = selectedSession.filter { $0.sessionType == unwrappedSessionType }
        }
        if let unwrappedRoom = selectedRoom {
            selectedSession = selectedSession.filter { unwrappedRoom.name.contains($0.room) }
        }
        if let unwrappedComplexity = selectedComplexity {
            selectedSession = selectedSession.filter { $0.complexity == unwrappedComplexity }
        }
        return selectedSession
    }
}
