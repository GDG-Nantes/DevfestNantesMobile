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
    @ObservedObject var viewModel = AgendaViewModel()

    // Données
    var session: AgendaContent.Session
    var isBookmarked = true

    var body: some View {
        GlassRowContainer(corner: 16) {
            HStack(alignment: .top, spacing: 12) {
                // Contenu à gauche
                VStack(alignment: .leading, spacing: 6) {
                    Text(session.title)
                        .foregroundColor(Color(Asset.devFestRed.color))
                        .font(.headline)
                        .multilineTextAlignment(.leading)

                    HStack(spacing: 8) {
                        if let categorylabel = session.category?.label {
                            CategoryView(categoryLabel: categorylabel)
                        }
                        Text(session.durationAndLanguage)
                            .font(.footnote)
                            .foregroundStyle(.secondary)
                    }

                    Text(session.room)
                        .font(.footnote)
                        .foregroundStyle(.secondary)

                    Text(session.speakers.map { $0.name }.joined(separator: ", "))
                        .font(.footnote)
                        .foregroundStyle(.secondary)
                        .lineLimit(1)
                }

                Spacer(minLength: 0)

                // Favori à droite
                if session.isATalk && isBookmarked {
                    Button {
                        viewModel.toggleFavorite(ofSession: session)
                        FirebaseAnalyticsService.shared.eventBookmark(
                            page: .agenda,
                            sessionId: session.id,
                            bookmarked: viewModel.favorites.contains(session.id)
                        )
                    } label: {
                        Image(systemName: viewModel.favorites.contains(session.id) ? "star.fill" : "star")
                            .font(.system(size: 18, weight: .semibold))
                            .foregroundColor(Color(Asset.devFestYellow.color))
                            .padding(8)
                            .contentShape(Rectangle())
                    }
                    .buttonStyle(.plain)
                }
            }
        }
        // densité compacte (gouttières visibles entre rows)
        .padding(Edge.Set.vertical, 4)
    }
}
