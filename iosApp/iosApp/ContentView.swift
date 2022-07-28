import SwiftUI
import shared

struct ContentView: View {
    let greet = Greeting().greeting()
    var body: some View {
        TabView {
           AgendaView()
             .tabItem {
                Image(systemName: "calendar")
                Text("Agenda")
              }

           VenueView()
             .tabItem {
                Image(systemName: "location.circle.fill")
                Text("Venue")
              }
            
           AboutView()
              .tabItem {
                 Image(systemName: "info.circle")
                 Text("About")
               }
        }
    }
}

struct ContentView_Previews: PreviewProvider {
	static var previews: some View {
		ContentView()
	}
}
