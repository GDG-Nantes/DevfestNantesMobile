//
//  GlassRowContainer.swift
//  iosApp
//
//  Created by Stéphane RIHET on 11/09/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import CoreFoundation
import CoreGraphics
import SwiftUI


struct GlassRowContainer<Content: View>: View {
    let corner: CGFloat
    @ViewBuilder let content: () -> Content
    @Environment(\.colorScheme) private var colorScheme

    init(corner: CGFloat = 16, @ViewBuilder content: @escaping () -> Content) {
        self.corner = corner
        self.content = content
    }

    var body: some View {
        HStack { content() }
            .padding(.vertical, 10)
            .padding(.horizontal, 12)
            .background {
                if #available(iOS 26.0, *) {
                    GlassEffectContainer {
                        Color.clear.glassEffect(.regular, in: .rect(cornerRadius: corner))
                    }
                    .clipShape(RoundedRectangle(cornerRadius: corner, style: .continuous))
                    .shadow(color: .black.opacity(0.25), radius: 10, x: 0, y: 2)
                } else {
                    RoundedRectangle(cornerRadius: corner, style: .continuous)
                        .fill(.thinMaterial)
                        .shadow(color: .black.opacity(0.15), radius: 8, x: 0, y: 2)
                }
            }
    }
}
