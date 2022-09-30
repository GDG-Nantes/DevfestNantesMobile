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
    @ObservedObject var viewModel: DevFestViewModel
    var session: AgendaContent.Session
    
    var body: some View {
        HStack(alignment: .top) {
            VStack(alignment: .leading) {
                Text(session.title)
                    .foregroundColor(Color(Asset.devFestRed.color))
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

                Text(session.speakers.map { $0.name }.joined(separator: ", "))
                    .font(.footnote)
                Spacer()
            }
            Spacer()
            VStack {
                Image(systemName:  viewModel.favorites.contains(session.id) ? "star.fill" : "star")
                    .foregroundColor(.yellow)
                    .padding(8)
                    .onTapGesture { self.viewModel.toggleFavorite(ofSession: session)
                        FirebaseAnalyticsService.shared.eventBookmark(page: .agenda, sessionId: session.id, fav: viewModel.favorites.contains(session.id))
                    }
                Spacer()
            }
        }
        .padding(8)
    }
}

//struct AgendaCellView_Previews: PreviewProvider {
//    static var previews: some View {
//        AgendaCellView()
//    }
//}
