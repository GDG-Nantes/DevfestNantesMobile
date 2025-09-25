//
//  LiquidGlassButtonStyle.swift
//  iosApp
//
//  Created by Stéphane RIHET on 09/09/2025.
//  Copyright © 2025 orgName. All rights reserved.
//


import SwiftUI

@available(iOS 26.0, *)
struct LiquidGlassButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        GlassEffectContainer {
            configuration.label
                .padding(.vertical, 8)
                .padding(.horizontal, 14)
                .glassEffect(.regular, in: .capsule)
                .scaleEffect(configuration.isPressed ? 0.98 : 1.0)
                .animation(.easeOut(duration: 0.12), value: configuration.isPressed)
                .contentShape(Capsule())
                .shadow(color: .black.opacity(0.22), radius: 8, x: 0, y: 2)
        }
    }
}