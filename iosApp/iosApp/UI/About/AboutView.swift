//
//  AboutView.swift
//  iosApp
//
//  Created by Stéphane Rihet on 10/07/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import shared

struct AboutView: View {
    @ObservedObject var viewModel = AboutViewModel()
    
    var body: some View {
        NavigationView {
            ZStack {
                DevFestSiteBackground()
                ScrollView {
                    VStack {
                        Card {
                            VStack(spacing: 16) {
                                Image("ic_about_header")
                                    .resizable()
                                    .aspectRatio(contentMode: .fit)
                                Text(L10n.screenAboutHeaderBody)
                                HStack(spacing: 24) {
                                    CustomButton(url: URL(string: WebLinks.codeOfConduct.url)!) {
                                        Text(L10n.aboutCodeOfConduct)
                                    }.foregroundColor(Color(Asset.devFestRed.color))
                                        .simultaneousGesture(TapGesture().onEnded {
                                            FirebaseAnalyticsService.shared.eventLinkCodeOfConductOpened()
                                        })
                                    CustomButton(url: URL(string: WebLinks.website.url)!) {
                                        Text(L10n.aboutWebsite)
                                    }.foregroundColor(Color(Asset.devFestRed.color))
                                        .simultaneousGesture(TapGesture().onEnded {
                                            FirebaseAnalyticsService.shared.eventLinkDevFestWebsiteOpened()
                                        })
                                }.padding(8)
                            }.padding(8)
                        }
                        
                        Card {
                            VStack {
                                Text(L10n.aboutSocialTitle)
                                    .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                    .foregroundColor(Color(Asset.devFestRed.color))
                                HStack(alignment: .top, spacing: 40) {
                                    Link(destination: URL(string: WebLinks.socialFacebook.url)!) {
                                        Image("ic_network_facebook")
                                            .renderingMode(.template)
                                            .foregroundColor(Color(Asset.icColor.color))
                                    }
                                    .simultaneousGesture(TapGesture().onEnded {
                                        FirebaseAnalyticsService.shared.eventLinkFacebookOpened()
                                    })
                        
                                    Link(destination: URL(string: WebLinks.socialTwitter.url)!) {
                                        Image("ic_network_twitter")
                                            .renderingMode(.template)
                                            .foregroundColor(Color(Asset.icColor.color))
                                    }
                                    .simultaneousGesture(TapGesture().onEnded {
                                        FirebaseAnalyticsService.shared.eventLinkTwitterOpened()
                                    })

                                    Link(destination: URL(string: WebLinks.socialLinkedin.url)!) {
                                        Image("ic_network_linkedin")
                                            .renderingMode(.template)
                                            .foregroundColor(Color(Asset.icColor.color))
                                    }
                                    .simultaneousGesture(TapGesture().onEnded {
                                        FirebaseAnalyticsService.shared.eventLinkLinkedinOpened()
                                    })
                                    
                                    Link(destination: URL(string: WebLinks.socielYoutube.url)!) {
                                        Image("ic_network_youtube")
                                            .renderingMode(.template)
                                            .foregroundColor(Color(Asset.icColor.color))
                                    }
                                    .simultaneousGesture(TapGesture().onEnded {
                                        FirebaseAnalyticsService.shared.eventLinkYoutubeOpened()
                                    })
                                }.padding(8)
                            }.padding(8)
                        }
                        
                        if let content = viewModel.partnersContent {
                            Card {
                                VStack(spacing: 16) {
                                    Text(L10n.partnersTitle)
                                        .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                        .foregroundColor(Color(Asset.devFestRed.color))
                                    
                                    ForEach(content, id: \.self) { category in
                                        VStack {
                                            Text(localizedCategoryName(for: category.categoryName.name))
                                                .font(.title2)
                                                .bold()
                                                .multilineTextAlignment(.center)
                                                .frame(maxWidth: .infinity, alignment: .center)
                                                .padding(20)
                                            ForEach(category.partners, id: \.self) { partner in
                                                if let partnerUrl = partner.url {
                                                    Button(action: { UIApplication.shared.open(URL(string: partnerUrl)!)
                                                        FirebaseAnalyticsService.shared.eventLinkPartnerOpened(partnerName: partnerUrl)
                                                    }) {
                                                        GlassRowContainer(corner: 20) {
                                                            HStack {
                                                                Spacer(minLength: 0)
                                                                if let logo = partner.logoUrl, let logoUrl = URL(string: logo) {
                                                                    AsyncImage(url: logoUrl) { phase in
                                                                        switch phase {
                                                                        case .empty:
                                                                            ProgressView()
                                                                                .frame(height: 50)
                                                                        case .success(let image):
                                                                            image
                                                                                .renderingMode(.original)
                                                                                .resizable()
                                                                                .aspectRatio(contentMode: .fit)
                                                                                .frame(height: 50)
                                                                        case .failure:
                                                                            Image(systemName: "photo")
                                                                                .resizable()
                                                                                .aspectRatio(contentMode: .fit)
                                                                                .frame(height: 50)
                                                                        @unknown default:
                                                                            EmptyView()
                                                                        }
                                                                    }
                                                                }
                                                                Spacer(minLength: 0)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    Spacer(minLength: 8)
                                }.padding(8)
                            }
                        }
                        
                        Card {
                            VStack {
                                Text(L10n.localCommunitiesTitle)
                                    .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                    .foregroundColor(Color(Asset.devFestRed.color))
                                HStack(alignment: .top, spacing: 40) {
                                    Button(action: {
                                        if let url = URL(string: WebLinks.nantestechCommunities.url) {
                                            UIApplication.shared.open(url)
                                            FirebaseAnalyticsService.shared.eventLinkLocalCommunitiesOpened()
                                        }
                                    }) {
                                        Image("local_communities_logo")
                                            .padding(6)
                                            .background(
                                                RoundedRectangle(cornerRadius: 12, style: .continuous)
                                                    .fill(Color.white)
                                            )
                                    }
                                    .foregroundColor(Color(Asset.devFestRed.color))
                                }.padding(8)
                            }.padding(8)
                        }
                        
                        Card {
                            VStack(alignment: .center, spacing: 8) {
                                CustomButton(url: URL(string: WebLinks.github.url)!) {
                                    HStack(spacing: 6) {
                                        Image("ic_network_github")
                                            .renderingMode(.template)
                                            .foregroundColor(Color(Asset.icColor.color))
                                        Text("\(L10n.forkMeOnGithub)")
                                            .foregroundColor(Color(Asset.icColor.color))
                                    }
                                }
                                Text("Version \(Bundle.main.releaseVersionNumber ?? "") (\(Bundle.main.buildVersionNumber ?? ""))")
                                    .font(.footnote)
                            }
                            .frame(maxWidth: .infinity, alignment: .center)
                            .padding(8)
                        }
                    }
                    .frame(minWidth: 0, maxWidth: .infinity)
                }
                .padding(0)
            }
            .task {
                await viewModel.observePartners()
            }
        }
        .navigationViewStyle(StackNavigationViewStyle())
        .padding(0)
        .onAppear{
            FirebaseAnalyticsService.shared.pageEvent(page: AnalyticsPage.about, className: "AboutView")
        }
    }
    
    /// Maps partner category names to their localized titles
    private func localizedCategoryName(for categoryName: String) -> String {
        switch categoryName.lowercased() {
        case "pxl":
            return L10n.partnersExclusivePlatiniumTitle
        case "platinium":
            return L10n.partnersPlatiniumTitle
        case "gold":
            return L10n.partnersGoldTitle
        case "virtual":
            return L10n.partnersVirtualTitle
        case "velotype":
            return L10n.partnersVelotypeTitle
        default:
            return categoryName // Fallback to original name
        }
    }
}
