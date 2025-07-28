import SwiftUI
import shared
import Combine
import KMPNativeCoroutinesCore
import KMPNativeCoroutinesCombine
import FirebaseCore
import FirebaseCrashlytics
import FirebaseRemoteConfig
import Firebase

///RemoteConfig properties
var remoteConfig = RemoteConfig.remoteConfig()

@main
struct iOSApp: App {
    
    init() {
        //Firebase initialization
        FirebaseApp.configure()
        
        // Start app startup performance monitoring
        PerformanceMonitoring.startAppStartupTrace()
        
        _ = RCValues.sharedInstance
        
        // Stop app startup performance monitoring
        PerformanceMonitoring.stopAppStartupTrace()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
                .preferredColorScheme(/*@START_MENU_TOKEN@*/.dark/*@END_MENU_TOKEN@*/)
        }
    }
}
