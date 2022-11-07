//
//  CategoryView.swift
//  iosApp
//
//  Created by Stéphane Rihet on 14/09/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI

///SwiftUI View
struct CategoryView: View {
    //Name of category
    var categoryLabel: String
    
    //setup UI
    var body: some View {
        Text(categoryLabel)
            .font(.system(size: 12))
            .padding(.vertical, 5)
            .padding(.horizontal, 8)
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(Color(Asset.devFestRed.color), lineWidth: 1)
            )
    }
}
