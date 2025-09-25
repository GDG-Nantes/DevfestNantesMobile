//
//  CategoryView.swift
//  iosApp
//
//  Created by Stéphane Rihet on 14/09/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI

struct CategoryView: View {
    var categoryLabel: String
    
    var body: some View {
        Group {
            if #available(iOS 26.0, *) {
                GlassEffectContainer {
                    Text(categoryLabel)
                        .font(.system(size: 12))
                        .padding(.vertical, 6)
                        .padding(.horizontal, 10)
                        .glassEffect(.regular, in: .capsule)
                        .overlay(
                            Capsule()
                                .fill(Color(Asset.devFestYellow.color).opacity(0.5))
                        )
                        .overlay(
                            Capsule()
                                .stroke(Color(Asset.devFestYellow.color), lineWidth: 1)
                        )
                        .foregroundStyle(.primary)
                        .contentShape(Capsule())
                }
            } else {
                Text(categoryLabel)
                    .font(.system(size: 12))
                    .colorInvert()
                    .padding(.vertical, 5)
                    .padding(.horizontal, 8)
                    .background(Color(Asset.devFestYellow.color))
                    .cornerRadius(20)
                    .overlay(
                        RoundedRectangle(cornerRadius: 20)
                            .stroke(Color(Asset.devFestYellow.color), lineWidth: 1)
                    )
            }
        }
    }
}
