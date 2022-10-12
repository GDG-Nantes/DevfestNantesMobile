import SwiftUI
import shared
import Combine
import KMPNativeCoroutinesCore
import KMPNativeCoroutinesCombine
import NSLogger
import FirebaseCore
import FirebaseCrashlytics
import FirebaseRemoteConfig

var remoteConfig = RemoteConfig.remoteConfig()

@main
struct iOSApp: App {
    
    init() {
        //NSLogger
        LoggerSetupBonjourForBuildUser()
        
        //Firebase
        FirebaseApp.configure()
        _ = RCValues.sharedInstance
        
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
