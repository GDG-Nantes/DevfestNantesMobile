//
//  CategoryView.swift
//  iosApp
//
//  Created by Stéphane Rihet on 14/09/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI

struct CategoryView: View {
    var categoryLabel: String
    
    
    var body: some View {
        Text(categoryLabel)
            .font(.system(size: 12))
            .padding(.vertical, 5)
            .padding(.horizontal, 8)
            .overlay(
                RoundedRectangle(cornerRadius: 20)
                    .stroke(Color(Asset.devfestRed.color), lineWidth: 1)
            )
    }
}
