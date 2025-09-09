//
//  VenueView.swift
//  iosApp
//
//  Created by Stéphane Rihet on 10/07/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import shared

struct VenueView: View {
    //Store an observable object instance
    @ObservedObject var viewModel = VenueViewModel()
    
    //Setup UI
    var body: some View {

        NavigationView {
            LoadingView(isShowing: $viewModel.isLoading) {
                ScrollView {
                VStack {
                    if let content = viewModel.venueContent {
                        if #available(iOS 16.0, *) {
                        Card {
                            VStack(spacing: 16) {
                                if let url = URL(string: content.imageUrl) {
                                    AsyncImage(url: url) { phase in
                                        switch phase {
                                        case .empty:
                                            ProgressView()
                                                .frame(height: 200)
                                        case .success(let image):
                                            image
                                                .resizable()
                                                .aspectRatio(contentMode: .fill)
                                        case .failure:
                                            Image(systemName: "photo")
                                                .resizable()
                                                .aspectRatio(contentMode: .fit)
                                        @unknown default:
                                            EmptyView()
                                        }
                                    }
                                    .frame(height: 200)
                                    .clipped()
                                }
                                
                                Text(content.name)
                                    .bold()
                                    .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                    .padding(.horizontal, 8)
                                
                                Text(content.address)
                                    .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                    .padding(.horizontal, 8)
                                
                                CustomButton(url: URL(string: "\(WebLinks.goTo.url)\(content.latitude),\(content.longitude)")!)  {
                                    Text(L10n.venueGoToButton)
                                }.foregroundColor(Color(Asset.devFestRed.color))
                                    .simultaneousGesture(TapGesture().onEnded {
                                        FirebaseAnalyticsService.shared.eventVenueNavigationClicked()
                                    })
                                
                                Text(content.description)
                                    .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                    .padding(.horizontal, 8)
                            }
                            .padding(8)
                        }
                        } else {
                        Card {
                            VStack(spacing: 16) {
                                if let url = URL(string: content.imageUrl) {
                                    AsyncImage(url: url) { phase in
                                        switch phase {
                                        case .empty:
                                            ProgressView()
                                                .frame(height: 200)
                                        case .success(let image):
                                            image
                                                .resizable()
                                                .aspectRatio(contentMode: .fill)
                                        case .failure:
                                            Image(systemName: "photo")
                                                .resizable()
                                                .aspectRatio(contentMode: .fit)
                                        @unknown default:
                                            EmptyView()
                                        }
                                    }
                                    .frame(height: 200)
                                    .clipped()
                                }
                                
                                Text(content.name)
                                    .bold()
                                    .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                    .padding(.horizontal, 8)
                                
                                Text(content.address)
                                    .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                    .padding(.horizontal, 8)
                                
                                CustomButton(url: URL(string: "\(WebLinks.goTo.url)\(content.latitude),\(content.longitude)")!)  {
                                    Text(L10n.venueGoToButton)
                                }.foregroundColor(Color(Asset.devFestRed.color))
                                    .simultaneousGesture(TapGesture().onEnded {
                                        FirebaseAnalyticsService.shared.eventVenueNavigationClicked()
                                    })
                                
                                Text(content.description)
                                    .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                    .padding(.horizontal, 8)
                            }
                            .padding(8)
                        }
                        }
                        if #available(iOS 16.0, *) {
                        Card {
                            VStack {
                                Text(L10n.plan)
                                    .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                    .foregroundColor(Color(Asset.devFestRed.color))
                                HStack(alignment: .top, spacing: 40) {
                                    NavigationLink(destination: PhotoDetailView(image: Asset.floorplan.image)) {
                                        if let planUrl = URL(string: content.planUrl) {
                                            AsyncImage(url: planUrl) { phase in
                                                switch phase {
                                                case .empty:
                                                    ProgressView()
                                                        .frame(height: 100)
                                                case .success(let image):
                                                    image
                                                        .resizable()
                                                        .aspectRatio(contentMode: .fill)
                                                case .failure:
                                                    Image(systemName: "photo")
                                                        .resizable()
                                                        .aspectRatio(contentMode: .fit)
                                                @unknown default:
                                                    EmptyView()
                                                }
                                            }
                                            .frame(height: 150)
                                            .clipped()
                                        }
                                    }
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
                        } else {
                        Card {
                            VStack {
                                Text(L10n.plan)
                                    .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                    .foregroundColor(Color(Asset.devFestRed.color))
                                HStack(alignment: .top, spacing: 40) {
                                    NavigationLink(destination: PhotoDetailView(image: Asset.floorplan.image)) {
                                        if let planUrl = URL(string: content.planUrl) {
                                            AsyncImage(url: planUrl) {
                                                phase in
                                                switch phase {
                                                case .empty:
                                                    ProgressView()
                                                        .frame(height: 100)
                                                case .success(let image):
                                                    image
                                                        .resizable()
                                                        .aspectRatio(contentMode: .fill)
                                                case .failure:
                                                    Image(systemName: "photo")
                                                        .resizable()
                                                        .aspectRatio(contentMode: .fit)
                                                @unknown default:
                                                    EmptyView()
                                                }
                                            }
                                            .frame(height: 150)
                                            .clipped()
                                        }
                                    }
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

