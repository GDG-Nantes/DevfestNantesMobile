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
    //Singleton
    static let sharedInstance = RCValues()
    
    //Initialization with value
    private init() {
        loadDefaultValues()
    }
    
    func loadDefaultValues() {
        let appDefaults: [String: Any?] = [
            ValueKey.openfeedback_enabled.rawValue : false
        ]
        RemoteConfig.remoteConfig().setDefaults(appDefaults as? [String: NSObject])
    }
    
    
    //Configure and fetch cloud value
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
    
    //Global method for boolean values
    func bool(forKey key: ValueKey) -> Bool {
        RemoteConfig.remoteConfig()[key.rawValue].boolValue
    }
}
