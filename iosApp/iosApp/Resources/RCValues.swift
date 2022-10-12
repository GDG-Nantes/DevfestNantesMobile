//
//  RCValues.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 11/10/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import FirebaseRemoteConfig
import NSLogger

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
            ValueKey.openfeedback_enabled.rawValue : false
        ]
        RemoteConfig.remoteConfig().setDefaults(appDefaults as? [String: NSObject])
    }
    
    func fetchCloudValues() {
        let settings = RemoteConfigSettings()
        settings.minimumFetchInterval = 300
        RemoteConfig.remoteConfig().configSettings = settings
        RemoteConfig.remoteConfig().fetch { _, error in
            if let error = error {
                Logger.shared.log(.network, .error, "RemoteConfig error: \(error)")
                return
            }
            
            RemoteConfig.remoteConfig().activate { _, _ in
                Logger.shared.log(.network, .info, "Retrieved values from the cloud!")
            }
        }
    }
    
    func bool(forKey key: ValueKey) -> Bool {
        RemoteConfig.remoteConfig()[key.rawValue].boolValue
    }
}
