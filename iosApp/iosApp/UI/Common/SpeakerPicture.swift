//
//  SpeakerPicture.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 12/09/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct SpeakerPicture: View {
    var speaker: Speaker_

    private let size: CGFloat = 128
    private let edgeWidth: CGFloat = 5

    var body: some View {
        ZStack {
            photo
                .frame(width: size, height: size)
                .clipShape(Circle())

            WaterEdgeGlass(diameter: size, edgeWidth: edgeWidth)
                .allowsHitTesting(false)
        }
        .accessibilityLabel("speakerPicture")
    }

    @ViewBuilder
    private var photo: some View {
        if let urlString = speaker.photoUrl, let url = URL(string: urlString) {
            AsyncImage(url: url) { phase in
                switch phase {
                case .empty:
                    ProgressView()
                case .success(let image):
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                case .failure:
                    Image(systemName: "person.circle")
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                @unknown default:
                    EmptyView()
                }
            }
        } else {
            Image(systemName: "person.circle")
                .resizable()
                .aspectRatio(contentMode: .fit)
        }
    }
}

private struct WaterEdgeGlass: View {
    let diameter: CGFloat
    let edgeWidth: CGFloat

    var body: some View {
        Group {
            if #available(iOS 26.0, *) {
                GlassEffectContainer {
                    Color.clear
                        .glassEffect(
                            .regular
                                .tint(.white.opacity(0.12)),
                            in: .rect(cornerRadius: diameter / 2)
                        )
                }
                .frame(width: diameter, height: diameter)
                .mask(
                    Circle()
                        .stroke(style: StrokeStyle(lineWidth: edgeWidth, lineCap: .round))
                )
                .overlay(
                    Circle()
                        .stroke(.white.opacity(0.22), lineWidth: 0.8)
                        .blur(radius: 0.5)
                )
                .shadow(color: .black.opacity(0.18), radius: 6, x: 0, y: 2)
            } else {
                Circle()
                    .strokeBorder(.thinMaterial, lineWidth: edgeWidth)
                    .frame(width: diameter, height: diameter)
                    .overlay(
                        Circle().stroke(.white.opacity(0.15), lineWidth: 0.6)
                    )
                    .shadow(color: .black.opacity(0.12), radius: 4, x: 0, y: 1)
            }
        }
    }
}
