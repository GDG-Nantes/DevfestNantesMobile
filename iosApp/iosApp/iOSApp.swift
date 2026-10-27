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

    /// True only when this process hosts an XCTest / Swift Testing run.
    static let isRunningTests: Bool =
        ProcessInfo.processInfo.environment["XCTestConfigurationFilePath"] != nil
        || NSClassFromString("XCTestCase") != nil

    init() {
        // The test bundle is hosted by the app: skip the real start-up so tests stay hermetic.
        if Self.isRunningTests { return }

        //Firebase initialization
        FirebaseApp.configure()

        // Start the Koin graph once, before any ViewModel is created
        IosDependencies.shared.start()
        
        // Start app startup performance monitoring
        PerformanceMonitoring.startAppStartupTrace()
        
        _ = RCValues.sharedInstance
        
        // Stop app startup performance monitoring
        PerformanceMonitoring.stopAppStartupTrace()
    }
    
    var body: some Scene {
        WindowGroup {
            if Self.isRunningTests {
                Color.clear
            } else {
                ContentView()
                    .preferredColorScheme(.dark)
            }
        }
    }
}
