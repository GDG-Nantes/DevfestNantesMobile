//
//  Card.swift
//  iosApp
//
//  Created by Stéphane Rihet on 21/09/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI

struct Card<Content: View>: View {
    let content: () -> Content
    
    var body: some View {
        VStack {
            content()
        }.frame(minWidth: 0, maxWidth: .infinity)
            .background(Color(Asset.secondaryBackground.color))
            .cornerRadius(8)
            .padding(8)
    }
}

