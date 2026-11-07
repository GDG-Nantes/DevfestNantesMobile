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
    @Environment(AgendaViewModel.self) private var viewModel
    
    @State private var day = AgendaDays.day1
    @State private var scrollToSectionID: Date? = nil
    @State private var firstAppear = true
    
    private let sectionTimeFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateFormat = "HH:mm"
        return f
    }()

    var body: some View {
        @Bindable var bindable = viewModel
        NavigationStack {
            ZStack {
                // Fond DevFest sous toute la vue
                DevFestSiteBackground()

                LoadingView(isShowing: $bindable.isLoading) {
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
            .navigationBarItems(trailing: AgendaNavigationBarItems().environment(viewModel))
            .task {
                await viewModel.observeRooms()
            }
            .task {
                RCValues.sharedInstance.fetchCloudValues()
                await viewModel.observeAgenda()
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
            Text(L10n.day1).tag(AgendaDays.day1)
            Text(L10n.day2).tag(AgendaDays.day2)
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
        let rowContent = SessionRowContent(
            title: session.title,
            categoryLabel: session.category?.label,
            durationAndLanguage: session.durationAndLanguage,
            room: session.room,
            speakersNames: session.speakers.map { $0.name }.joined(separator: ", "),
            isTalk: session.isATalk,
            isFavorite: viewModel.isBookmarked(session.id),
            isNavigable: session.isNavigable,
            favoriteTapped: {
                let bookmarked = viewModel.toggleBookmark(sessionId: session.id)
                FirebaseAnalyticsService.shared.eventBookmark(
                    page: .agenda,
                    sessionId: session.id,
                    bookmarked: bookmarked
                )
            }
        )
        
        // Cellule entière cliquable uniquement pour conference/codelab/quickie
        if session.isNavigable {
            PushLink(destination: AgendaDetailView(session: session, day: day)) {
                SessionRow(content: rowContent)
            }
        } else {
            SessionRow(content: rowContent)
        }
    }
    
    // MARK: - Filtering
    
    private func filteredSections(for day: String) -> [AgendaContent.Section] {
        Self.sections(viewModel.agendaContent.sections, forDay: day)
    }

    private func filteredSessionsForSection(_ section: AgendaContent.Section) -> [AgendaContent.Session] {
        Self.filteredSessions(section.sessions, filters: viewModel.sessionFilters, favorites: viewModel.bookmarkedIds)
    }

    /// Static and pure so tests can call it without creating a view (D-10 extraction, bodies unchanged).
    static func sections(_ sections: [AgendaContent.Section], forDay day: String) -> [AgendaContent.Section] {
        sections.filter { $0.day.contains(day) }
    }

    /// Static and pure so tests can call it without creating a view (D-10 extraction, bodies unchanged).
    static func filteredSessions(
        _ sessions: [AgendaContent.Session],
        filters: Set<shared.SessionFilter>,
        favorites: Set<String>
    ) -> [AgendaContent.Session] {
        var filtered = sessions
        let selectedLanguages = filters.filter { $0.type == .language }.map { $0.value }
        let selectedSessionTypes = filters.filter { $0.type == .type }.map { $0.value }
        let selectedRooms = filters.filter { $0.type == .room }.map { $0.value }
        let selectedComplexities = filters.filter { $0.type == .complexity }.map { $0.value }

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
        let isFavorites = filters.contains { $0.type == .bookmark }
        return isFavorites ? filtered.filter { favorites.contains($0.id) } : filtered
    }
    
    // MARK: - Initial scroll/day selection

    /// Static and pure so tests can call it without creating a view (D-11 extraction).
    static func scrollTarget(slotDates: [Date], now: Date, calendar: Calendar) -> Date? {
        let nowTotal = calendar.component(.hour, from: now) * 60 + calendar.component(.minute, from: now)

        let sorted = slotDates.sorted {
            let t1 = calendar.component(.hour, from: $0) * 60 + calendar.component(.minute, from: $0)
            let t2 = calendar.component(.hour, from: $1) * 60 + calendar.component(.minute, from: $1)
            return t1 < t2
        }

        var previous: Date?
        for date in sorted {
            let t = calendar.component(.hour, from: date) * 60 + calendar.component(.minute, from: date)
            if t <= nowTotal { previous = date } else { break }
        }
        return previous
    }

    private func handleInitialScrollIfNeeded(isLoading: Bool) {
        guard !isLoading else { return }
        let formatter = DateFormatter()
        formatter.dateFormat = "yyyy-MM-dd"
        let currentDay = formatter.string(from: Date())
        
        if currentDay == day && firstAppear {
            let slotDates = viewModel.agendaContent.sections
                .filter { $0.day.contains(day) }
                .map(\.date)
            if let target = Self.scrollTarget(slotDates: slotDates, now: Date(), calendar: Calendar.current) {
                scrollToSectionID = target
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

        if currentDay == AgendaDays.day1 {
            day = AgendaDays.day1
        } else if currentDay == AgendaDays.day2 {
            day = AgendaDays.day2
        } else {
            day = AgendaDays.day1
        }
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
