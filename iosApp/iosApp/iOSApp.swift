import SwiftUI
import shared
import Combine
import KMPNativeCoroutinesCore
import KMPNativeCoroutinesCombine
import NSLogger
import FirebaseCore
import FirebaseCrashlytics

@main
struct iOSApp: App {
    
    init() {
        //NSLogger
        LoggerSetupBonjourForBuildUser()
        
        //Firebase
        FirebaseApp.configure()
        Crashlytics.crashlytics().log("App loaded")
        
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
