import SwiftUI
import shared
import Combine
import KMPNativeCoroutinesCore
import KMPNativeCoroutinesCombine
import NSLogger

@main
struct iOSApp: App {
    
    let store : DevFestNantesStore

    let publisher : AnyPublisher<[Session], Error>
    
    var cancellable: AnyCancellable
    
    init() {
        //NSLogger
        LoggerSetupBonjourForBuildUser()
        
        self.store = DevFestNantesStoreBuilder().setUseMockServer(useMockServer: false).build()
        // Create an AnyPublisher for your flow
        self.publisher = createPublisher(for: store.sessionsNative)

        // Now use this publisher as you would any other
        self.cancellable = publisher.sink { completion in
            let log = "Received completion: \(completion)"
            Logger.shared.log(.service, .debug, log)
        } receiveValue: { value in
            let log = "Received value: \(value)"
            Logger.shared.log(.service, .debug, log)
        }
    }
    
	var body: some Scene {
		WindowGroup {
			ContentView()
		}
	}
}
