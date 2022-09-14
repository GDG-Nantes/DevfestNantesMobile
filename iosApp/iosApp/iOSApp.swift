import SwiftUI
import shared
import Combine
import KMPNativeCoroutinesCore
import KMPNativeCoroutinesCombine
import NSLogger

@main
struct iOSApp: App {
    
    init() {
        //NSLogger
        LoggerSetupBonjourForBuildUser()
        
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
