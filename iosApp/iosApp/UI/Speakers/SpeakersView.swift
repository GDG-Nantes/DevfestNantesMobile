//
//  SpeakersView.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 11/09/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import URLImage
import shared

struct SpeakersView: View {
    @ObservedObject var viewModel = SpeakersViewModel()
    
    var body: some View {
        NavigationView {
            LoadingView(isShowing: $viewModel.isLoading) {
                VStack {
                    ScrollViewReader { scrollProxy in
                        ZStack {
                            List {
                                ForEach(viewModel.getAlphabets(), id: \.self) { letter in
                                    Section(header: Text(letter).id(letter)) {
                                        ForEach(viewModel.speakers(for: letter).map(SpeakerWrapper.init), id: \.id) { speakerWrapper in
                                            NavigationLink(destination: SpeakerDetails(speakerId: speakerWrapper.id)) {
                                                HStack {
                                                    if let photo = speakerWrapper.speaker.photoUrl {
                                                        URLImage(url: URL(string: photo)!) { image in
                                                            image
                                                                .resizable()
                                                                .aspectRatio(contentMode: .fill)
                                                                .clipShape(Circle())
                                                        }.frame(width: 40, height: 40)
                                                    }
                                                    
                                                    Text(speakerWrapper.speaker.name)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            
                            VStack {
                                ForEach(viewModel.getAlphabets(), id: \.self) { letter in
                                    HStack {
                                        Spacer()
                                        Button(action: {
                                            if viewModel.speakers(for: letter).count > 0 {
                                                withAnimation {
                                                    scrollProxy.scrollTo(letter)
                                                }
                                            }
                                        }, label: {
                                            Text(letter)
                                                .font(.system(size: 14))
                                                .padding(.trailing, 7)
                                        })
                                    }
                                }
                            }
                        }
                        .navigationTitle(L10n.screenSpeakers)
                    }
                    .task {
                        await viewModel.observeSpeakers()
                    }
                }
            }
        }
    }
}

struct SpeakersView_Previews: PreviewProvider {
    static var previews: some View {
        SpeakersView(viewModel: SpeakersViewModel())
    }
}

struct SpeakerWrapper: Identifiable {
    var speaker: Speaker_
    var id: String {
        speaker.id
    }
}
