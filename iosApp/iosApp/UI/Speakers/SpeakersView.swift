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
    @ObservedObject var viewModel: DevFestViewModel
    @State private var searchText = ""

    var body: some View {
        NavigationView {
            VStack {
                ScrollViewReader { scrollProxy in
                    ZStack {
                        List {
                            ForEach(getAlphabets(), id: \.self) { letter in
                                Section(header: Text(letter).id(letter)) {
                                    ForEach(speakers(for: letter).map(SpeakerWrapper.init), id: \.id) { speakerWrapper in
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


                        VStack {
                            ForEach(getAlphabets(), id: \.self) { letter in
                                HStack {
                                    Spacer()
                                    Button(action: {
                                        if speakers(for: letter).count > 0 {
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
                }
            }
            .navigationTitle(L10n.screenSpeakers)
        }
        .task {
            await viewModel.observeSpeakers()
        }
    }

    private func getAlphabets() -> [String] {
        let letters = viewModel.speakersContent?.compactMap { $0.name.prefix(1).uppercased() }
        let uniqueLetters = Array(Set(letters ?? []))
        return uniqueLetters.sorted()
    }

    private func speakers(for letter: String) -> [Speaker] {
        return viewModel.speakersContent?.filter {
            $0.name.prefix(1).uppercased() == letter
        } ?? []
    }
}

struct SpeakersView_Previews: PreviewProvider {
    static var previews: some View {
        SpeakersView(viewModel: DevFestViewModel())
    }
}

struct SpeakerWrapper: Identifiable {
    var speaker: Speaker
    var id: String {
        speaker.id
    }
}
