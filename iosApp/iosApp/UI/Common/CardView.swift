//
//  Card.swift
//  iosApp
//
//  Created by Stéphane Rihet on 21/09/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI

/// SwiftUI View
struct Card<Content: View>: View {
    let content: () -> Content
    
    var body: some View {
        Group {
            if #available(iOS 26.0, *) {
                GlassEffectContainer {
                    VStack {
                        content()
                    }
                    .frame(minWidth: 0, maxWidth: .infinity)
                    .padding(12)
                    .glassEffect(.regular, in: .rect(cornerRadius: 12))
                }
                .clipShape(RoundedRectangle(cornerRadius: 12, style: .continuous))
                .padding(8)
            } else {
                VStack {
                    content()
                }
                .frame(minWidth: 0, maxWidth: .infinity)
                .padding(12)
                .background(Color(Asset.secondaryBackground.color))
                .clipShape(RoundedRectangle(cornerRadius: 8, style: .continuous))
                .padding(8)
            }
        }
    }
}
