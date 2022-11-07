import SwiftUI
import shared
import Combine
import KMPNativeCoroutinesCore
import KMPNativeCoroutinesCombine
import NSLogger
import FirebaseCore
import FirebaseCrashlytics
import FirebaseRemoteConfig

///RemoteConfig properties
var remoteConfig = RemoteConfig.remoteConfig()

@main
struct iOSApp: App {
    
    init() {
        //NSLogger initialization
        LoggerSetupBonjourForBuildUser()
        
        //Firebase initialization
        FirebaseApp.configure()
        _ = RCValues.sharedInstance
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
