//
//  AgendaDetailView.swift
//  iosApp
//
//  Created by Stéphane Rihet on 11/08/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import shared

struct AgendaDetailView: View {
    var content: AgendaContent.Session?
    @ObservedObject var viewModel = AgendaViewModel()
    var day: String

    init(session: AgendaContent.Session, day: String) {
        self.content = session
        self.day = day
    }

    private func iso(_ s: String) -> Date {
        let f = ISO8601DateFormatter()
        return f.date(from: s) ?? Date()
    }
    
    private let timeFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateFormat = "HH:mm"
        return f
    }()
    
    private let fullDateFormatter: DateFormatter = {
        let f = DateFormatter()
        f.dateStyle = .medium
        return f
    }()

    var body: some View {
        content.map { session in
            ZStack {
                DevFestSiteBackground()
                ScrollView {
                    VStack(spacing: 12) {
                        Card {
                            VStack(alignment: .leading, spacing: 14) {
                                VStack(alignment: .leading, spacing: 10) {
                                    Text(session.title)
                                        .foregroundColor(Color(Asset.devFestRed.color))
                                        .font(.title2.weight(.bold))

                                    Text("\(fullDateFormatter.string(from: iso(session.startDate))), \(timeFormatter.string(from: iso(session.startDate))) – \(timeFormatter.string(from: iso(session.endDate))), \(session.room)")
                                        .font(.subheadline.weight(.semibold))
                                        .foregroundStyle(.primary)
                                        .lineLimit(2)
                                }

                                VStack(alignment: .leading, spacing: 10) {
                                    Text(session.durationAndLanguage)
                                        .font(.footnote)
                                        .foregroundStyle(.secondary)

                                    HStack(spacing: 8) {
                                        if let type = session.sessionType?.name.capitalized {
                                            CategoryView(categoryLabel: type)
                                        }
                                        if let cat = session.category?.label {
                                            CategoryView(categoryLabel: cat)
                                        }
                                        if let complexity = session.complexity?.text {
                                            CategoryView(categoryLabel: complexity)
                                        }
                                    }
                                }

                                if !session.abstract.isEmpty {
                                    VStack(alignment: .leading, spacing: 10) {
                                        Text(session.abstract)
                                            .font(.body)
                                            .foregroundStyle(.primary)
                                    }
                                }

                                // Feedback (si activé)
                                if session.isATalk && RCValues.sharedInstance.bool(forKey: .openfeedback_enabled) {
                                    VStack(alignment: .leading, spacing: 8) {
                                        CustomButton(url: URL(string: "\(WebLinks.openFeedback.url)/\(day)/\(session.id)")!) {
                                            Text(L10n.sessionFeedbackLabel)
                                        }
                                        .foregroundColor(Color(Asset.devFestRed.color))
                                        .frame(maxWidth: .infinity)
                                        .simultaneousGesture(TapGesture().onEnded {
                                            FirebaseAnalyticsService.shared.eventFeedbackClicked(
                                                openFeedbackId: session.openFeedbackFormId ?? ""
                                            )
                                        })

                                        Text(L10n.poweredOpenfeedback)
                                            .font(.callout)
                                            .foregroundColor(Color(UIColor.placeholderText))
                                            .frame(maxWidth: .infinity, alignment: .trailing)
                                    }
                                }
                            }
                        }

                        if !session.speakers.isEmpty {
                            Card {
                                VStack(alignment: .leading, spacing: 12) {
                                    Text(L10n.screenSpeaker)
                                        .font(.headline)
                                        .foregroundColor(Color(Asset.devFestRed.color))
                                    ForEach(session.speakers, id: \.self) { speaker in
                                        SpeakerView(speaker: speaker)
                                        if speaker.id != session.speakers.last?.id {
                                            Divider().opacity(0.25)
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(minLength: 8)
                    }
                    .padding(.horizontal, 12)
                    .padding(.vertical, 8)
                }
                .navigationBarTitle(Text(session.title), displayMode: .inline)
                .toolbar {
                    ToolbarItem(placement: .navigationBarTrailing) {
                        Button {
                            viewModel.toggleFavorite(ofSession: session)
                            FirebaseAnalyticsService.shared.eventBookmark(
                                page: .sessionDetails,
                                sessionId: session.id,
                                bookmarked: viewModel.favorites.contains(session.id)
                            )
                        } label: {
                            Image(systemName: viewModel.favorites.contains(session.id) ? "star.fill" : "star")
                                .foregroundColor(Color(Asset.devFestYellow.color))
                        }
                    }
                }
            }
        }
        .onAppear {
            FirebaseAnalyticsService.shared.pageEvent(page: AnalyticsPage.sessionDetails, className: "AgendaDetailView")
        }
    }
}