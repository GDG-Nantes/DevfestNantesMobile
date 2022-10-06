//
//  AboutView.swift
//  iosApp
//
//  Created by Stéphane Rihet on 10/07/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import URLImage
import shared

struct AboutView: View {
    @ObservedObject var viewModel: DevFestViewModel
    
    var body: some View {
        NavigationView {
            ZStack {
                ScrollView {
                    VStack {
                        Card {
                            VStack(spacing: 16) {
                                Image("ic_about_header")
                                    .resizable()
                                    .aspectRatio(contentMode: .fit)
                                    .foregroundColor(Color(Asset.devFestBlue.color))
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
                                        FirebaseAnalyticsService.shared.eventLinkTwitterOpened()})

                                    Link(destination: URL(string: WebLinks.socialLinkedin.url)!) {
                                        Image("ic_network_linkedin")
                                            .renderingMode(.template)
                                            .foregroundColor(Color(Asset.icColor.color))
                                    }
                                        .simultaneousGesture(TapGesture().onEnded {
                                        FirebaseAnalyticsService.shared.eventLinkLinkedinOpened()})
                                    
                                    Link(destination: URL(string: WebLinks.socielYoutube.url)!) {
                                        Image("ic_network_youtube")
                                            .renderingMode(.template)
                                            .foregroundColor(Color(Asset.icColor.color))
                                    }
                                            .simultaneousGesture(TapGesture().onEnded {
                                        FirebaseAnalyticsService.shared.eventLinkYoutubeOpened()})

                                }.padding(8)
                            }.padding(8)
                        }

                        Card {
                            VStack(spacing: 16) {
                                Text(L10n.partnersTitle)
                                    .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                    .foregroundColor(Color(Asset.devFestRed.color))
                                
                                ForEach(self.viewModel.partnersContent, id: \.self) { category in
                                    VStack {
                                        Text(category.categoryName.name)
                                            .font(.title2)
                                            .bold()
                                            .padding(20)
                                        ForEach(category.partners, id: \.self) { partner in
                                            if let partnerUrl = partner.url {
                                                Button(action: { UIApplication.shared.open(URL(string: partnerUrl)!)
                                                    FirebaseAnalyticsService.shared.eventLinkPartnerOpened(partnerURL: partnerUrl)
                                                }) {
                                                    if let logo =  partner.logoUrl  {
                                                        URLImage(url: URL(string:logo)!) { image in
                                                            image
                                                                .renderingMode(.original)
                                                                .resizable()
                                                                .aspectRatio(contentMode: .fit)
                                                        }
                                                    }
                                                }
                                                .frame(maxHeight: 50)
                                                .padding(8)
                                                .background(
                                                    RoundedRectangle(cornerRadius: 20, style: .continuous).fill(Color.white)
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(minLength: 8)
                            }.padding(8)
                        }
                        Card {
                            VStack {
                                Text(L10n.localCommunitiesTitle)
                                    .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                    .foregroundColor(Color(Asset.devFestRed.color))
                                HStack(alignment: .top, spacing: 40) {
                                    CustomButton(url: URL(string: WebLinks.nantestechCommunities.url)!) {
                                        Image("local_communities_logo")
                                    }
                                    .background(
                                        RoundedRectangle(cornerRadius: 20, style: .continuous).fill(Color.white)
                                    )
                                    .simultaneousGesture(TapGesture().onEnded {
                                        FirebaseAnalyticsService.shared.eventLinkLocalCommunitiesOpened()
                                    })
                                    .foregroundColor(Color(Asset.devFestRed.color))
                                }.padding(8)
                            }.padding(8)
                        }
                        Card {
                            VStack {
                                CustomButton(url: URL(string: WebLinks.github.url)!) {
                                    HStack(spacing: 0) {
                                        Image("ic_network_github")
                                                    .renderingMode(.template)
                                                    .foregroundColor(Color(Asset.icColor.color))
                                        Text(" \(L10n.forkMeOnGithub)")
                                            .foregroundColor(Color(Asset.icColor.color))
                                    }
                                }
                                Text("Version \("\(Bundle.main.releaseVersionNumber ?? "") (\(Bundle.main.buildVersionNumber ?? ""))")")
                                    .font(.footnote)
                            }.padding(8)
                        }
                    }
                    .frame(minWidth: 0, maxWidth: .infinity)
                }
                .padding(0)
                .background(Color(UIColor.systemBackground))
            }
            .task {
                await viewModel.observePartners()
            }
        }
        // must ensure that the stack navigation is used otherwise it is considered as a master view
        // and nothing is shown in the detail
        .navigationViewStyle(StackNavigationViewStyle())
        .padding(0)
        .onAppear{
            FirebaseAnalyticsService.shared.pageEvent(page: AnalyticsPage.about, className: "AboutView")
        }
    }
}

//struct AboutView_Previews: PreviewProvider {
//    static var previews: some View {
//        AboutView()
//    }
//}

extension Bundle {
    var releaseVersionNumber: String? {
        return infoDictionary?["CFBundleShortVersionString"] as? String
    }
    var buildVersionNumber: String? {
        return infoDictionary?["CFBundleVersion"] as? String
    }
}
