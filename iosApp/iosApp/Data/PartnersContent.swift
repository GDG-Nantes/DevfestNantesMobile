//
//  PartnersContent.swift
//  iosApp
//
//  Created by Stéphane Rihet on 21/09/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared

extension PartnerCategory: Comparable {
    public static func < (lhs: PartnerCategory, rhs: PartnerCategory) -> Bool {
        return lhs.ordinal < rhs.ordinal
    }
}

struct PartnerContent: Hashable {
    let categoryName: PartnerCategory
    let partners: [Partner]
}

