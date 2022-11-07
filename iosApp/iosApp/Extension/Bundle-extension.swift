//
//  Bundle-extension.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 07/11/2022.
//  Copyright © 2022 orgName. All rights reserved.
//
import SwiftUI

//
extension Bundle {
    //Get the release version number
    var releaseVersionNumber: String? {
        return infoDictionary?["CFBundleShortVersionString"] as? String
    }
    //Get the build version number
    var buildVersionNumber: String? {
        return infoDictionary?["CFBundleVersion"] as? String
    }
}
