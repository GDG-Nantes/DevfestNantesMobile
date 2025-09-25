//
//  SocialIcon.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 12/09/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct SocialIconsView: View {
    var socials: [SocialItem]
    var onSocialLinkClick: (SocialItem) -> Void

    private let size: CGFloat = 44
    private var corner: CGFloat { size / 2 }

    var body: some View {
        HStack(alignment: .center, spacing: 16) {
            ForEach(Array(socials.enumerated()), id: \.offset) { index, item in
                if let link = item.link, let url = URL(string: link) {
                    Button {
                        onSocialLinkClick(item)
                        UIApplication.shared.open(url)
                    } label: {
                        ZStack {
                            GlassCircleBackground(corner: corner, size: size)

                            icon(for: item.type)
                                .resizable()
                                .scaledToFit()
                                .frame(width: size * 0.55, height: size * 0.55)
                                .foregroundColor(Color(Asset.icColor.color))
                                .accessibilityHidden(true)
                        }
                        .frame(width: size, height: size)
                        .accessibilityLabel(accessibilityLabel(for: item.type))
                    }
                    .buttonStyle(.plain)
                }
            }
        }
    }

    // MARK: - Icônes


    private func icon(for type: SocialType) -> Image {
        switch type {
        case .twitter:  Image("ic_network_twitter").renderingMode(.template)
        case .github:   Image("ic_network_github").renderingMode(.template)
        case .linkedin: Image("ic_network_linkedin").renderingMode(.template)
        case .facebook: Image("ic_network_facebook").renderingMode(.template)
        case .website:  Image("ic_network_web").renderingMode(.template)
        default:        Image(systemName: "link") // fallback au cas où
        }
    }

    private func accessibilityLabel(for type: SocialType) -> String {
        switch type {
        case .twitter:  return "Twitter"
        case .github:   return "GitHub"
        case .linkedin: return "LinkedIn"
        case .facebook: return "Facebook"
        case .website:  return "Site web"
        default:        return "Lien"
        }
    }
}

// MARK: - Fond verre circulaire réutilisable

private struct GlassCircleBackground: View {
    let corner: CGFloat
    let size: CGFloat

    var body: some View {
        Group {
            if #available(iOS 26.0, *) {
                GlassEffectContainer {
                    Color.clear.glassEffect(
                        .regular.tint(.white.opacity(0.14)),
                        in: .rect(cornerRadius: corner)
                    )
                }
                .clipShape(Circle())
                .overlay(Circle().stroke(.white.opacity(0.22), lineWidth: 0.8))
                .shadow(color: .black.opacity(0.25), radius: 8, x: 0, y: 2)
            } else {
                Circle()
                    .fill(.thinMaterial)
                    .overlay(Circle().stroke(.white.opacity(0.18), lineWidth: 0.8))
                    .shadow(color: .black.opacity(0.15), radius: 6, x: 0, y: 2)
            }
        }
        .frame(width: size, height: size)
        .contentShape(Circle())
    }
}
