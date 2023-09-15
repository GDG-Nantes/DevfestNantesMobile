//
//  AgendaCellView.swift
//  iosApp
//
//  Created by Stéphane Rihet on 12/08/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import shared

struct AgendaCellView: View {
    //Store an observable object instance
    @ObservedObject var viewModel = AgendaViewModel()
    
    //Session data
    var session: AgendaContent.Session
    var isBookmarked = true
    
    //Setup UI
    var body: some View {
        HStack(alignment: .top) {
            VStack(alignment: .leading) {
                Text(session.title)
                    .foregroundColor(Color(Asset.devFestRed.color))
                    .multilineTextAlignment(.leading)
                    .font(.headline)
                    .padding(.bottom, 4)
                HStack {
                    if let categorylabel = session.category?.label {
                        CategoryView(categoryLabel:  categorylabel)
                    }
                    Text("\(session.durationAndLanguage)")
                        .font(.footnote)
                }
                Text("\(session.room)")
                    .font(.footnote)
                Spacer()
                Text(session.speakers.map { $0.name }.joined(separator: ", "))
                    .font(.footnote)
                Spacer()
            }
            Spacer()
            VStack {
                if session.isATalk && isBookmarked {
                    Image(systemName:  viewModel.favorites.contains(session.id) ? "star.fill" : "star")
                        .foregroundColor(.yellow)
                        .padding(8)
                        .onTapGesture { self.viewModel.toggleFavorite(ofSession: session)
                            FirebaseAnalyticsService.shared.eventBookmark(page: .agenda, sessionId: session.id, bookmarked: viewModel.favorites.contains(session.id))
                        }
                Spacer()
                }
            }
        }
        .padding(8)
    }
}
