//
//  AboutView.swift
//  iosApp
//
//  Created by Stéphane Rihet on 10/07/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI

struct AboutView: View {
    @ObservedObject var viewModel: DevFestViewModel
    
    var body: some View {
        NavigationView {
            ZStack {
                Color(UIColor.systemBackground).edgesIgnoringSafeArea(.all)
                ScrollView {
                    VStack {
                        Card {
                            VStack(spacing: 16) {
                                Image("ic_about_header")
                                    .resizable()
                                    .aspectRatio(contentMode: .fit)
                                Text(L10n.screenAboutHeaderBody)
                                    .foregroundColor(Color.black)
                                HStack(spacing: 24) {
                                    CustomButton(url: URL(string: "https://devfest.gdgnantes.com/code-of-conduct")!) {
                                        Text(L10n.aboutCodeOfConduct)
                                    }.foregroundColor(Color.red)
                                    CustomButton(url: URL(string: "https://devfest.gdgnantes.com/")!) {
                                        Text(L10n.aboutWebsite)
                                    }.foregroundColor(Color.red)
                                }.padding(8)
                            }.padding(8)
                        }
                        Card {
                            VStack {
                                Text(L10n.aboutSocialTitle)
                                    .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                    .foregroundColor(Color.red)
                                HStack(alignment: .top, spacing: 40) {
                                    Link(destination: URL(string: "https://facebook.com/gdgnantes")!) {
                                        Image("ic_network_facebook") }
                                    Link(destination: URL(string: "https://twitter.com/gdgnantes")!) {
                                        Image("ic_network_twitter") }
                                    Link(destination: URL(string: "https://www.linkedin.com/in/gdg-nantes")!) {
                                        Image("ic_network_linkedin") }
                                    Link(destination: URL(string: "https://www.youtube.com/c/Gdg-franceBlogspotFr")!) {
                                        Image("ic_network_web") }
                                }.padding(8)
                            }.padding(8)
                        }
                        Card {
                            VStack {
                                Text(L10n.localCommunitiesTitle)
                                    .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                    .foregroundColor(Color.red)
                                HStack(alignment: .top, spacing: 40) {
                                    CustomButton(url: URL(string: "https://nantes.community/")!) {
                                        Text(L10n.localCommunitiesButton)
                                    }.foregroundColor(Color.red)
                                }.padding(8)
                            }.padding(8)
                        }
                        Card {
                            VStack {
                                Text(L10n.partnersTitle)
                                    .frame(minWidth: 0, maxWidth: .infinity, alignment: .leading)
                                    .foregroundColor(Color.red)
                            }.padding(8)
                        }
                    }
                    .background(Color.gray)
                    .frame(minWidth: 0, maxWidth: .infinity)
                }
                .padding(0)
                .background(Color(UIColor.systemBackground))
            }
        }
        // must ensure that the stack navigation is used otherwise it is considered as a master view
        // and nothing is shown in the detail
        .navigationViewStyle(StackNavigationViewStyle())
        .padding(0)
    }
}

struct Card<Content: View>: View {
    let content: () -> Content

    var body: some View {
        VStack {
            content()
        }.frame(minWidth: 0, maxWidth: .infinity)
            .background(Color.white)
            .cornerRadius(8)
            .padding(8)
    }
}

//struct AboutView_Previews: PreviewProvider {
//    static var previews: some View {
//        AboutView()
//    }
//}
