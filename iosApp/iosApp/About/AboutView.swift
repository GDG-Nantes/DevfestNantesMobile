//
//  AboutView.swift
//  iosApp
//
//  Created by Stéphane Rihet on 10/07/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import URLImage

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
                                    CustomButton(url: URL(string: "https://devfest.gdgnantes.com/code-of-conduct")!) {
                                        Text(L10n.aboutCodeOfConduct)
                                    }.foregroundColor(Color(Asset.devFestRed.color))
                                    CustomButton(url: URL(string: "https://devfest.gdgnantes.com/")!) {
                                        Text(L10n.aboutWebsite)
                                    }.foregroundColor(Color(Asset.devFestRed.color))
                                }.padding(8)
                            }.padding(8)
                        }
                        Card {
                            VStack {
                                Text(L10n.aboutSocialTitle)
                                    .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                    .foregroundColor(Color(Asset.devFestRed.color))
                                HStack(alignment: .top, spacing: 40) {
                                    Link(destination: URL(string: "https://facebook.com/gdgnantes")!) {
                                        Image("ic_network_facebook")
                                            .renderingMode(.template)
                                            .foregroundColor(Color(Asset.icColor.color))
                                    }
                                    Link(destination: URL(string: "https://twitter.com/gdgnantes")!) {
                                        Image("ic_network_twitter")
                                            .renderingMode(.template)
                                            .foregroundColor(Color(Asset.icColor.color))
                                    }
                                    Link(destination: URL(string: "https://www.linkedin.com/in/gdg-nantes")!) {
                                        Image("ic_network_linkedin")
                                            .renderingMode(.template)
                                            .foregroundColor(Color(Asset.icColor.color))
                                    }
                                    Link(destination: URL(string: "https://www.youtube.com/c/Gdg-franceBlogspotFr")!) {
                                        Image("ic_network_web")
                                            .renderingMode(.template)
                                            .foregroundColor(Color(Asset.icColor.color))
                                    }
                                }.padding(8)
                            }.padding(8)
                        }
                        Card {
                            VStack {
                                Text(L10n.localCommunitiesTitle)
                                    .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                    .foregroundColor(Color(Asset.devFestRed.color))
                                HStack(alignment: .top, spacing: 40) {
                                    CustomButton(url: URL(string: "https://nantes.community/")!) {
                                        Text(L10n.localCommunitiesButton)
                                    }.foregroundColor(Color(Asset.devFestRed.color))
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
                                                Button(action: { UIApplication.shared.open(URL(string: partnerUrl)!) }) {
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
                                                .padding(3)
                                                .background(.white)
                                            }
                                        }
                                    }
                                }
                                Spacer(minLength: 8)
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
    }
}

//struct AboutView_Previews: PreviewProvider {
//    static var previews: some View {
//        AboutView()
//    }
//}
