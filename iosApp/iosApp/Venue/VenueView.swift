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
                URLImage(url: URL(string: viewModel.venueContent.imageUrl)!) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fit)
                }.frame(maxHeight: 220)
                
                Text(viewModel.venueContent.name)
                    .bold()
                    .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 8)
                
                Text(viewModel.venueContent.address)
                    .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 8)
                
                Button(L10n.venueGoToButton) {
                    let url = "http://maps.apple.com/?daddr=\(viewModel.venueContent.latitude),\(viewModel.venueContent.longitude)"
                    UIApplication.shared.open(URL(string: url)!)
                }.padding(.vertical, 8)
                    .padding(.horizontal, 16)
                    .foregroundColor(Color.primary)
                    .background(Color.gray)
                    .cornerRadius(8)
                
                Text(viewModel.venueContent.description)
                    .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                    .padding(.horizontal, 8)
                
                Spacer()
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

