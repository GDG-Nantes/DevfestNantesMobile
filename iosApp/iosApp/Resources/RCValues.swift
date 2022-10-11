//
//  RCValues.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 11/10/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import FirebaseRemoteConfig

enum ValueKey: String {
    case openfeedback_enabled
}

class RCValues {
    static let sharedInstance = RCValues()
    private init() {
        loadDefaultValues()
    }
    
    func loadDefaultValues() {
        let appDefaults: [String: Any?] = [
            ValueKey.openfeedback_enabled.rawValue : true
        ]
        RemoteConfig.remoteConfig().setDefaults(appDefaults as? [String: NSObject])
    }
    
    func fetchCloudValues() {
        let settings = RemoteConfigSettings()
        // WARNING: Don't actually do this in production!
        settings.minimumFetchInterval = 300
        RemoteConfig.remoteConfig().configSettings = settings
        RemoteConfig.remoteConfig().fetch { _, error in
            if let error = error {
                print("Got an error fetching remote values \(error)")
                return
            }
            
            RemoteConfig.remoteConfig().activate { _, _ in
                print("Retrieved values from the cloud!")
            }
        }
    }
    
    func bool(forKey key: ValueKey) -> Bool {
        RemoteConfig.remoteConfig()[key.rawValue].boolValue
    }
    
}
