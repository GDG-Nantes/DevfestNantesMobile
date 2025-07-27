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
    // register app delegate for Firebase setup
      @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    
    init() {
        // Start app startup performance monitoring
        PerformanceMonitoring.startAppStartupTrace()
        
        //Firebase initialization
        FirebaseApp.configure()
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
