import SwiftUI
import shared
import store
import storeMocked
import KMPNativeCoroutinesCombine

@main
struct iOSApp: App {
    
    let store : DevFestNantesStore = DevFestNantesStoreMocked() as! DevFestNantesStore
    
	var body: some Scene {       
        // Create an AnyPublisher for your flow
        let publisher = createPublisher(for: store.partnersNative)

        // Now use this publisher as you would any other
        let cancellable = publisher.sink { completion in
            print("Received completion: \(completion)")
        } receiveValue: { value in
            print("Received value: \(value)")
        }
        
		WindowGroup {
			ContentView()
		}
	}
}
