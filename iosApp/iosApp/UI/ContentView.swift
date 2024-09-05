import SwiftUI
import shared
import Combine

///SwiftUI TabView
struct ContentView: View {
    @StateObject var viewModel = BaseViewModel()
    
    @State private var selection = 0
    
    var body: some View {
        TabView(selection: $selection) {
            AgendaView()
                .tabItem {
                    VStack {
                        Image(systemName: "calendar").renderingMode(.template)
                        Text(L10n.screenAgenda)
                    }
                }.tag(0)
            
            SpeakersView()
                .tabItem {
                    VStack {
                        Image(systemName: "person.3.fill").renderingMode(.template)
                        Text(L10n.screenSpeakers)
                    }
                }.tag(1)
            
            VenueView()
                .tabItem {
                    VStack {
                        Image(systemName: "location.circle.fill").renderingMode(.template)
                        Text(L10n.screenVenue)
                    }
                }.tag(2)
            
            AboutView()
                .tabItem {
                    VStack {
                        Image(systemName: "info.circle").renderingMode(.template)
                        Text(L10n.screenAbout)
                    }
                }.tag(3)
        }
        .accentColor(Color(Asset.devFestRed.color))
        .onAppear{
            // correct the transparency bug for Tab bars
            let tabBarAppearance = UITabBarAppearance()
            tabBarAppearance.configureWithOpaqueBackground()
            UITabBar.appearance().scrollEdgeAppearance = tabBarAppearance
        }
    }
}

