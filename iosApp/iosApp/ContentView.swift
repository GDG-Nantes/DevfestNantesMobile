import SwiftUI
import shared

struct ContentView: View {
    @State private var selection = 0

    var body: some View {
        TabView(selection: $selection) {
            AgendaView()
                .tabItem {
                    VStack {
                        Image(systemName: "calendar")
                        Text("Agenda")
                    }
            }.tag(0)

             VenueView()
                .tabItem {
                    VStack {
                        Image(systemName: "location.circle.fill")
                        Text("Venue")
                    }
            }.tag(1)

            AboutView()
                .tabItem {
                    VStack {
                        Image(systemName: "info.circle")
                        Text("About")
                    }
            }.tag(2)
        }
    }
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
