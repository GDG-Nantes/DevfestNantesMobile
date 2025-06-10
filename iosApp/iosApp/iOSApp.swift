import SwiftUI
import shared
import Combine
import KMPNativeCoroutinesCore
import KMPNativeCoroutinesCombine
import FirebaseCore
import FirebaseCrashlytics
import FirebaseRemoteConfig

///RemoteConfig properties
var remoteConfig = RemoteConfig.remoteConfig()

@main
struct iOSApp: App {
    
    init() {
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
