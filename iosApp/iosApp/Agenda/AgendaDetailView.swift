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
    //Session data
    var content : AgendaContent.Session?
    
    
    //Store an observable object instance
    @ObservedObject var viewModel = AgendaViewModel()
    var day: String
    
    //Method to convert a date in string format to ISO 8601 format
    func getDate(date: String) -> Date {
        let newFormatter = ISO8601DateFormatter()
        return newFormatter.date(from: date) ?? Date()// replace Date String
    }
    
    //Formatting the time
    var timeFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateFormat = "HH:mm"
        return formatter
    }()
    
    //Formatting the date
    var fullDateFormatter: DateFormatter = {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        return formatter
    }()
    
    //Initialization of this view with Session, global viewmodel and the day
    init(session: AgendaContent.Session, day: String) {
        self.content = session
        self.day = day
    }
    
    //Setup UI
    var body: some View {
        content.map { session in
            ScrollView {
                VStack(alignment: .leading, spacing: 8) {
                    Text(session.title)
                        .foregroundColor(Color(Asset.devFestRed.color))
                        .font(.title)
                        .padding(.bottom, 8)
                        .padding(.top, 16)
                    Text("\(fullDateFormatter.string(from: getDate(date: session.startDate))), \(timeFormatter.string(from: getDate(date: session.startDate))) - \(timeFormatter.string(from: getDate(date: session.endDate))), \(session.room)")
                        .bold()
                        .font(.headline)
                        .padding(.bottom, 8)
                    Divider().padding(.bottom, 8)
                    Text("\(session.durationAndLanguage)")
                        .font(.footnote)
                    
                    HStack {
                        if let sessionTypeLabel = session.sessionType?.name.capitalized {
                            CategoryView(categoryLabel: sessionTypeLabel)
                        }
                        if let categoryLabel = session.category?.label {
                            CategoryView(categoryLabel: categoryLabel)
                        }
                        if let complexityLabel = session.complexity?.text {
                            CategoryView(categoryLabel:  complexityLabel)
                        }
                    }
                    Divider().padding(.top, 8)
                    Text(session.abstract)
                        .font(.body)
                    Divider().padding(.top, 8)

                    ForEach(session.speakers, id: \.self) { speaker in
                        SpeakerView(speaker: speaker)
                        Divider().padding(.top, 8)
                    }
                    //Use Remote config for display openFeedback)
                    if session.isATalk && RCValues.sharedInstance.bool(forKey: .openfeedback_enabled){
                            VStack(spacing: 16) {
                                Spacer()
                                CustomButton(url: URL(string: "\(WebLinks.openFeedback.url)/\(day)/\(session.id)")!) {
                                    Text(L10n.sessionFeedbackLabel)
                                }.foregroundColor(Color(Asset.devFestRed.color))
                                    .frame(minWidth: 0, maxWidth: .infinity, alignment: .center)
                                    .simultaneousGesture(TapGesture().onEnded {
                                        FirebaseAnalyticsService.shared.eventFeedbackClicked(openFeedbackId: session.openFeedbackFormId ?? "")
                                    })
                                Text(L10n.poweredOpenfeedback)
                                    .frame(maxWidth: .infinity, alignment: .trailing)
                                    .font(.callout)
                                    .foregroundColor(Color(UIColor.placeholderText))
                                Divider().padding(.top, 8)
                            }
                    }
                }.padding(.horizontal)
            }
            .navigationBarTitle(Text(session.title), displayMode: .inline)
            .navigationBarItems(trailing:
                                    Image(systemName:  viewModel.favorites.contains(session.id) ? "star.fill" : "star")
                .foregroundColor(Color(Asset.devFestYellow.color))
                .padding(8)
                .onTapGesture { self.viewModel.toggleFavorite(ofSession: session)
                    FirebaseAnalyticsService.shared.eventBookmark(page: .sessionDetails, sessionId: session.id, bookmarked: viewModel.favorites.contains(session.id))
                })
        }
        .onAppear{
            FirebaseAnalyticsService.shared.pageEvent(page: AnalyticsPage.sessionDetails, className: "AgendaDetailView")
        }
    }
}

