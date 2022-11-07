//
//  Complexity-extension.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 07/11/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared

//Adds string for all complexity
extension Complexity {
    var text: String {
        switch self {
        case .beginner: return L10n.complexityBeginer
        case .intermediate: return L10n.complexityIntermediate
        case .advanced: return L10n.complexityAdvanced
        default:
            return ""
        }
    }
}
