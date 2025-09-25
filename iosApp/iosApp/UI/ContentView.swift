import SwiftUI
import shared
import Combine

///SwiftUI TabView
struct ContentView: View {
    @StateObject var viewModel = BaseViewModel()
    @State private var selection = 0

    var body: some View {
        ZStack {
            DevFestSiteBackground()   // ✅ fond commun vertical

            TabView(selection: $selection) {
                AgendaView()
                    .tabItem {
                        Image(systemName: "calendar")
                        Text(L10n.screenAgenda)
                    }
                    .tag(0)

                SpeakersView()
                    .tabItem {
                        Image(systemName: "person.3.fill")
                        Text(L10n.screenSpeakers)
                    }
                    .tag(1)

                VenueView()
                    .tabItem {
                        Image(systemName: "location.circle.fill")
                        Text(L10n.screenVenue)
                    }
                    .tag(2)

                AboutView()
                    .tabItem {
                        Image(systemName: "info.circle")
                        Text(L10n.screenAbout)
                    }
                    .tag(3)
            }
            .accentColor(Color(Asset.devFestRed.color))
        }
        .onAppear {
            if #available(iOS 26.0, *) {
                let appearance = UITabBarAppearance()
                appearance.configureWithDefaultBackground()
                UITabBar.appearance().standardAppearance = appearance
                UITabBar.appearance().scrollEdgeAppearance = appearance
            } else {
                let appearance = UITabBarAppearance()
                appearance.configureWithOpaqueBackground()
                UITabBar.appearance().standardAppearance = appearance
                UITabBar.appearance().scrollEdgeAppearance = appearance
            }
        }
    }
}

import SwiftUI

extension Color {
    init(hex: UInt, alpha: Double = 1.0) {
        self.init(.sRGB,
                  red:   Double((hex >> 16) & 0xFF) / 255.0,
                  green: Double((hex >> 8)  & 0xFF) / 255.0,
                  blue:  Double(hex & 0xFF)        / 255.0,
                  opacity: alpha)
    }
}

enum DevFestTheme {
    // Fond / barres (site)
    static let greenDark   = Color(hex: 0x1F3A33) // vert sapin sombre
    static let greenDarker = Color(hex: 0x0F1A18) // vert très foncé
    static let nearBlack   = Color(hex: 0x0B0E0E) // quasi noir
    // Accents
    static let orange      = Color(hex: 0xD8543F)
    static let cream       = Color(hex: 0xF5E7A7) // jaune pâle des cartes
}



import SwiftUI

struct DevFestSiteBackground: View {
    var body: some View {
        ZStack {
            // Dégradé principal : vert sombre -> quasi noir
            LinearGradient(
                colors: [
                    DevFestTheme.greenDark,
                    DevFestTheme.greenDarker,
                    DevFestTheme.nearBlack
                ],
                startPoint: .top, endPoint: .bottom
            )
            .ignoresSafeArea()

            // Un halo vert très léger en haut (rappelle la barre)
            LinearGradient(
                colors: [
                    DevFestTheme.greenDark.opacity(0.25),
                    .clear
                ],
                startPoint: .top, endPoint: .center
            )
            .ignoresSafeArea()

            // Vignette subtile pour garder le regard au centre
            LinearGradient(
                colors: [.black.opacity(0.22), .clear, .black.opacity(0.25)],
                startPoint: .top, endPoint: .bottom
            )
            .blendMode(.multiply)
            .ignoresSafeArea()

            // Texture (iOS 26 uniquement) : léger grain pour nourrir le verre
            if #available(iOS 26.0, *) {
                DevFestGrainOverlay26(opacity: 0.05) // 0.04–0.08 selon goût
            }
        }
    }
}

@available(iOS 26.0, *)
private struct DevFestGrainOverlay26: View {
    var opacity: CGFloat = 0.05
    var body: some View {
        // Une simple image SF d’emoji "noise" n’existe pas; on simule un grain tuilé
        // via un rectangle avec blend overlay + opacité faible — peu coûteux et suffisant.
        Rectangle()
            .fill(
                LinearGradient(colors: [
                    .white.opacity(opacity), .black.opacity(opacity)
                ], startPoint: .topLeading, endPoint: .bottomTrailing)
            )
            .blendMode(.overlay)
            .ignoresSafeArea()
            .allowsHitTesting(false)
    }
}
