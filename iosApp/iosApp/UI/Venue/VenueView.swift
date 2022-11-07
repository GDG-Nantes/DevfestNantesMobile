//
//  VenueView.swift
//  iosApp
//
//  Created by Stéphane Rihet on 10/07/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import URLImage
import shared


struct VenueView: View {
    //Store an observable object instance
    @ObservedObject var viewModel : DevFestViewModel
    
    //Setup UI
    var body: some View {
        NavigationView {
                ScrollView {
                        VStack {
                            Card {
                            VStack(spacing: 16) {
                                    if let imageUrl =  viewModel.venueContent.imageUrl  {
                                        URLImage(url: URL(string: imageUrl)!) { image in
                                            image
                                                .resizable()
                                                .aspectRatio(contentMode: .fill)
                                        }}
                                    
                                    Text(viewModel.venueContent.name)
                                        .bold()
                                        .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                        .padding(.horizontal, 8)
                                    
                                    Text(viewModel.venueContent.address)
                                        .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                        .padding(.horizontal, 8)
                                    
                                    CustomButton(url: URL(string: "\(WebLinks.goTo.url)\(viewModel.venueContent.latitude),\(viewModel.venueContent.longitude)")!)  {
                                        Text(L10n.venueGoToButton)
                                    }.foregroundColor(Color(Asset.devFestRed.color))
                                        .simultaneousGesture(TapGesture().onEnded {
                                            FirebaseAnalyticsService.shared.eventVenueNavigationClicked()
                                        })
                                    
                                    Text(viewModel.venueContent.description)
                                        .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                        .padding(.horizontal, 8)
                                }
                        .padding(8)
                    }
                                Card {
                                    VStack {
                                        Text(L10n.plan)
                                            .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                            .foregroundColor(Color(Asset.devFestRed.color))
                                            HStack(alignment: .top, spacing: 40) {
                                                Button(action: {
                                                    print("Plan")
                                                }, label: {
                                                    if let planUrl = viewModel.venueContent.planUrl {
                                                        let image = UIImage(url: URL(string: planUrl)!)
                                                        NavigationLink(destination: PhotoDetailView(image: image!)) {
                                                            URLImage(url: URL(string: planUrl)!) { image in
                                                                image
                                                                    .resizable()
                                                                    .aspectRatio(contentMode: .fill)
                                                            }
                                                        }
                                                    }
                                                })
                                                .padding(.vertical, 5)
                                                .padding(.horizontal, 10)
                                                .overlay(
                                                    RoundedRectangle(cornerRadius: 20)
                                                        .stroke(Color(Asset.devFestBlue.color), lineWidth: 1)
                                                )
                                                .background(
                                                    RoundedRectangle(cornerRadius: 20, style: .continuous).fill(Color.white)
                                                )
                                        }.padding(8)
                        }.padding(8)
                    }
                }
            }
            .task {
                await viewModel.observeVenue()
            }
            .onAppear{
                FirebaseAnalyticsService.shared.pageEvent(page: AnalyticsPage.venue, className: "VenueView")
            }
        }
    }
}

