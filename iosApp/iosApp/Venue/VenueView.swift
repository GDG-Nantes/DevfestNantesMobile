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
    
    
    @ObservedObject var viewModel : DevFestViewModel
    
    var body: some View {
        ScrollView {
            VStack(alignment: .center, spacing: 8) {
                Card {
                    VStack(spacing: 16) {
                        URLImage(url: URL(string: viewModel.venueContent.imageUrl)!) { image in
                            image
                                .resizable()
                                .aspectRatio(contentMode: .fill)
                        }.frame(maxHeight: 220)
                        
                        Text(viewModel.venueContent.name)
                            .bold()
                            .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                            .padding(.horizontal, 8)
                        
                        Text(viewModel.venueContent.address)
                            .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                            .padding(.horizontal, 8)
                        
                        CustomButton(url: URL(string: "http://maps.apple.com/?daddr=\(viewModel.venueContent.latitude),\(viewModel.venueContent.longitude)")!)  {
                            Text(L10n.venueGoToButton)
                        }.foregroundColor(Color(Asset.devFestRed.color))
                        
                        Text(viewModel.venueContent.description)
                            .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                            .padding(.horizontal, 8)
                        
                        Spacer()
                    }
                }
            }
        }
        .task {
            await viewModel.observeVenue()
        }
    }
}

//struct VenueView_Previews: PreviewProvider {
//    static var previews: some View {
//        VenueView()
//    }
//}

