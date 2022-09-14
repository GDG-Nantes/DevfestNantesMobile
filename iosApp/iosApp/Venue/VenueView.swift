//
//  VenueView.swift
//  iosApp
//
//  Created by Stéphane Rihet on 10/07/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import URLImage


struct VenueView: View {
    @ObservedObject private var viewModel = VenueViewModel()

    var body: some View {
        viewModel.content.map { content in
            ScrollView {
                VStack(alignment: .center, spacing: 16) {
                    URLImage(url: URL(string: content.imageUrl)!) { image in
                        image
                            .resizable()
                            .aspectRatio(contentMode: .fit)
                    }.frame(maxHeight: 220)

                    Text(content.name)
                        .bold()
                        .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal, 8)
                    
                    Text(content.address)
                        .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal, 8)

                    Button("Y aller") {
                        let url = "http://maps.apple.com/?daddr=\(content.latitude),\(content.longitude)"
                        UIApplication.shared.open(URL(string: url)!)
                    }.padding(.vertical, 8)
                        .padding(.horizontal, 16)
                        .foregroundColor(Color.primary)
                        .background(Color.gray)
                        .cornerRadius(8)
                    
                    Text(content.descriptionFr)
                        .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal, 8)

                    Spacer()
                }
            }
        }
    }
}

struct VenueView_Previews: PreviewProvider {
    static var previews: some View {
        VenueView()
    }
}

