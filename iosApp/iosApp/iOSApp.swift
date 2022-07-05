import SwiftUI
import shared
import Combine
import KMPNativeCoroutinesCore
import KMPNativeCoroutinesCombine

@main
struct iOSApp: App {
    
    let store : DevFestNantesStore

    let publisher : AnyPublisher<[Session], Error>
    
    var cancellable: AnyCancellable
    
    init() {
        self.store = DevFestNantesStoreMocked()
        // Create an AnyPublisher for your flow
        self.publisher = createPublisher(for: store.sessionsNative)

        // Now use this publisher as you would any other
        self.cancellable = publisher.sink { completion in
            print("Received completion: \(completion)")
        } receiveValue: { value in
            print("Received value: \(value)")
        }
        print("coucou")
    }
    
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
