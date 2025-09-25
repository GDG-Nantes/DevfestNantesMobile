//
//  AgendaView.swift
//  iosApp
//
//  Created by Stéphane Rihet on 10/07/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

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
    @State private var scrollToSectionID: Date? = nil
    @State private var firstAppear = true
    
    private let sectionTimeFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateFormat = "HH:mm"
        return f
    }()

    var body: some View {
        NavigationStack {
            ZStack {
                // Fond DevFest sous toute la vue
                DevFestSiteBackground()

                LoadingView(isShowing: $viewModel.isLoading) {
                    ScrollViewReader { proxy in
                        VStack(spacing: 12) {
                            dayPicker
                            agendaList
                                .onChange(of: scrollToSectionID) { newID in
                                    if let newID { proxy.scrollTo(newID, anchor: .top) }
                                }
                        }
                    }
                }
            }
            .onChange(of: viewModel.isLoading) { isLoading in
                handleInitialScrollIfNeeded(isLoading: isLoading)
            }
            .navigationBarTitle(L10n.screenAgenda)
            .navigationBarItems(trailing: AgendaNavigationBarItems(viewModel: viewModel))
            .task {
                RCValues.sharedInstance.fetchCloudValues()
                await viewModel.observeRooms()
                await viewModel.observeSessions()
            }
        }
        .onAppear {
            prepareInitialDayAndScroll()
            FirebaseAnalyticsService.shared.pageEvent(page: AnalyticsPage.agenda, className: "AgendaView")
        }
    }
    
    // MARK: - Subviews
    
    private var dayPicker: some View {
        Picker("What is the day?", selection: $day) {
            Text(L10n.day1).tag("2025-10-16")
            Text(L10n.day2).tag("2025-10-17")
        }
        .pickerStyle(.segmented)
        .padding(.horizontal, 12)
    }
    
    private var agendaList: some View {
        List {
            ForEach(filteredSections(for: day), id: \.date) { section in
                let sessionsToShow = filteredSessionsForSection(section)
                if !sessionsToShow.isEmpty {
                    Section(header: TimeHeader(date: section.date, formatter: sectionTimeFormatter).id(section.date)) {
                        ForEach(sessionsToShow, id: \.id) { session in
                            agendaRow(for: session)
                                .listRowBackground(Color.clear)
                                .listRowSeparator(.hidden)
                                .listRowInsets(EdgeInsets(top: 4, leading: 16, bottom: 4, trailing: 16))
                        }
                    }
                    .textCase(nil)
                }
            }
        }
        .listStyle(.plain)
        .scrollContentBackground(.hidden)
        .background(Color.clear)
    }
    
    @ViewBuilder
    private func agendaRow(for session: AgendaContent.Session) -> some View {
        let isNavigable = (session.sessionType == .conference || session.sessionType == .codelab || session.sessionType == .quickie)
        
        let rowContent = SessionRowContent(
            title: session.title,
            categoryLabel: session.category?.label,
            durationAndLanguage: session.durationAndLanguage,
            room: session.room,
            speakersNames: session.speakers.map { $0.name }.joined(separator: ", "),
            isTalk: session.isATalk,
            isFavorite: viewModel.favorites.contains(session.id),
            isNavigable: isNavigable,
            favoriteTapped: {
                viewModel.toggleFavorite(ofSession: session)
                FirebaseAnalyticsService.shared.eventBookmark(
                    page: .agenda,
                    sessionId: session.id,
                    bookmarked: viewModel.favorites.contains(session.id)
                )
            }
        )
        
        // Cellule entière cliquable uniquement pour conference/codelab/quickie
        if isNavigable {
            PushLink(destination: AgendaDetailView(session: session, day: day)) {
                SessionRow(content: rowContent)
            }
        } else {
            SessionRow(content: rowContent)
        }
    }
    
    // MARK: - Filtering
    
    private func filteredSections(for day: String) -> [AgendaContent.Section] {
        viewModel.agendaContent.sections.filter { $0.day.contains(day) }
    }
    
    private func filteredSessionsForSection(_ section: AgendaContent.Section) -> [AgendaContent.Session] {
        let base = getFilteredSessions(sessions: section.sessions)
        let isFavorites = viewModel.sessionFilters.contains { $0.type == .bookmark }
        return isFavorites ? base.filter { viewModel.favorites.contains($0.id) } : base
    }
    
    // MARK: - Initial scroll/day selection
    
    private func handleInitialScrollIfNeeded(isLoading: Bool) {
        guard !isLoading else { return }
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        let currentDay = formatter.string(from: Date())
        
        if currentDay == day && firstAppear {
            let cal = Calendar.current
            let now = Date()
            let nowTotal = cal.component(.hour, from: now) * 60 + cal.component(.minute, from: now)
            
            let sorted = viewModel.agendaContent.sections
                .filter { $0.day.contains(day) }
                .sorted {
                    let t1 = cal.component(.hour, from: $0.date) * 60 + cal.component(.minute, from: $0.date)
                    let t2 = cal.component(.hour, from: $1.date) * 60 + cal.component(.minute, from: $1.date)
                    return t1 < t2
                }
            
            var previous: AgendaContent.Section?
            for s in sorted {
                let t = cal.component(.hour, from: s.date) * 60 + cal.component(.minute, from: s.date)
                if t <= nowTotal { previous = s } else { break }
            }
            if let previous {
                DispatchQueue.main.async { scrollToSectionID = previous.date }
            }
            firstAppear = false
        }
    }
    
    private func prepareInitialDayAndScroll() {
        if scrollToSectionID == nil {
            scrollToSectionID = viewModel.agendaContent.sections.first?.date
        }
        let f = DateFormatter()
        f.dateFormat = "yyyy-MM-dd"
        let currentDay = f.string(from: Date())

        if currentDay == "2025-10-16" {
            day = "2025-10-16"
        } else if currentDay == "2025-10-17" {
            day = "2025-10-17"
        } else {
            day = "2025-10-16"
        }
    }
    
    // MARK: - Filtres
    private func getFilteredSessions(sessions: [AgendaContent.Session]) -> [AgendaContent.Session] {
        var filtered = sessions
        let selectedLanguages = viewModel.sessionFilters.filter { $0.type == .language }.map { $0.value }
        let selectedSessionTypes = viewModel.sessionFilters.filter { $0.type == .type }.map { $0.value }
        let selectedRooms = viewModel.sessionFilters.filter { $0.type == .room }.map { $0.value }
        let selectedComplexities = viewModel.sessionFilters.filter { $0.type == .complexity }.map { $0.value }

        if !selectedLanguages.isEmpty {
            filtered = filtered.filter { s in
                if let lang = s.language?.name { selectedLanguages.contains(lang) } else { false }
            }
        }
        if !selectedSessionTypes.isEmpty {
            filtered = filtered.filter { s in
                selectedSessionTypes.contains(s.sessionType?.name ?? "")
            }
        }
        if !selectedRooms.isEmpty {
            filtered = filtered.filter { s in
                selectedRooms.contains(s.room)
            }
        }
        if !selectedComplexities.isEmpty {
            filtered = filtered.filter { s in
                if let comp = s.complexity?.name { selectedComplexities.contains(comp) } else { false }
            }
        }
        return filtered
    }
}

// MARK: - Sous-vues

/// Header de section (heure) – lisible sur fond sombre
private struct TimeHeader: View {
    let date: Date
    let formatter: DateFormatter
    
    var body: some View {
        Text(formatter.string(from: date))
            .font(.footnote.weight(.semibold))
            .foregroundStyle(.secondary)
            .padding(.vertical, 4)
            .padding(.horizontal, 8)
            .background(
                Group {
                    if #available(iOS 26.0, *) {
                        GlassEffectContainer {
                            Color.clear
                                .glassEffect(.regular, in: .capsule)
                        }
                    } else {
                        Capsule().fill(.thinMaterial)
                    }
                }
            )
            .clipShape(Capsule())
    }
}

/// Lien de navigation sans chevron système (cellule entière cliquable)
private struct PushLink<Label: View, Destination: View>: View {
    @State private var isActive = false
    let destination: Destination
    let label: () -> Label

    init(destination: Destination, @ViewBuilder label: @escaping () -> Label) {
        self.destination = destination
        self.label = label
    }

    var body: some View {
        Button(action: { isActive = true }) {
            label()
                .frame(maxWidth: .infinity, alignment: .leading)
                .contentShape(Rectangle()) // toute la carte cliquable
        }
        .buttonStyle(.plain)
        .navigationDestination(isPresented: $isActive) {
            destination
        }
    }
}

// MARK: - Session Row extraction

private struct SessionRowContent {
    let title: String
    let categoryLabel: String?
    let durationAndLanguage: String
    let room: String
    let speakersNames: String
    let isTalk: Bool
    let isFavorite: Bool
    let isNavigable: Bool
    let favoriteTapped: () -> Void
}

private struct SessionRow: View {
    let content: SessionRowContent
    
    var body: some View {
        GlassRowContainer(corner: 16) {
            // Center the row vertically so the chevron is centered when present.
            HStack(alignment: .center, spacing: 12) {
                VStack(alignment: .leading, spacing: 6) {
                    Text(content.title)
                        .foregroundColor(Color(Asset.devFestRed.color))
                        .font(.headline)
                        .multilineTextAlignment(.leading)

                    HStack(spacing: 8) {
                        if let cat = content.categoryLabel {
                            CategoryView(categoryLabel: cat)
                        }
                        Text(content.durationAndLanguage)
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                    }

                    Text(content.room)
                        .font(.footnote)
                        .foregroundStyle(.secondary)

                    Text(content.speakersNames)
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }

                Spacer(minLength: 0)

                if content.isTalk {
                    Button {
                        content.favoriteTapped()
                    } label: {
                        Image(systemName: content.isFavorite ? "star.fill" : "star")
                            .foregroundColor(Color(Asset.devFestYellow.color))
                            .font(.system(size: 16, weight: .semibold))
                            .padding(.trailing, 4)
                    }
                    .buttonStyle(.plain) // n’ouvre pas la navigation
                }

                if content.isNavigable {
                    Image(systemName: "chevron.right")
                        .font(.system(size: 15, weight: .semibold))
                        .foregroundStyle(.secondary)
                        .opacity(0.85)
                        .allowsHitTesting(false) // décoratif
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading) // étend la zone
        .contentShape(Rectangle())                        // toute la carte est “tapable”
        .padding(.vertical, 4)
    }
}
