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
    // Données
    var session: AgendaContent.Session

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
            }
        }
        // densité compacte (gouttières visibles entre rows)
        .padding(Edge.Set.vertical, 4)
    }
}
