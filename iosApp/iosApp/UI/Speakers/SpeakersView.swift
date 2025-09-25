//
//  SpeakersView.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 11/09/2023.
//  Copyright © 2023 orgName. All rights reserved.
//

import SwiftUI
import shared

struct SpeakersView: View {
    @ObservedObject var viewModel = SpeakersViewModel()

    /// Largeur de la bande d’index et marge côté bord droit
    private let indexWidth: CGFloat  = 24
    private let indexMargin: CGFloat = 8

    var body: some View {
        NavigationView {
            LoadingView(isShowing: $viewModel.isLoading) {
                ScrollViewReader { scrollProxy in
                    ZStack {
                        // Fond sombre du thème
                        DevFestSiteBackground()

                        // Liste transparente + overlay d’index aligné à droite
                        List {
                            ForEach(viewModel.getAlphabets(), id: \.self) { letter in
                                Section(header: SectionHeader(letter: letter).id(letter)) {
                                    ForEach(viewModel.speakers(for: letter).map(SpeakerWrapper.init), id: \.id) { wrapper in

                                        PushLink(destination: SpeakerDetails(speakerId: wrapper.id)) {
                                            GlassRowContainer(corner: 18) {
                                                HStack(spacing: 12) {
                                                    Avatar(urlString: wrapper.speaker.photoUrl)

                                                    Text(wrapper.speaker.name)
                                                        .foregroundStyle(.primary)
                                                        .lineLimit(1)
                                                        .font(.system(.body, design: .rounded).weight(.medium))

                                                    Spacer(minLength: 0)

                                                    Image(systemName: "chevron.right")
                                                        .font(.system(size: 15, weight: .semibold))
                                                        .foregroundStyle(.secondary)
                                                        .opacity(0.85)
                                                }
                                            }
                                            .padding(.vertical, 3)
                                            .padding(.trailing, indexWidth + indexMargin)
                                            .contentShape(Rectangle())
                                        }
                                        .listRowBackground(Color.clear)
                                        .listRowSeparator(.hidden)
                                        .listRowInsets(EdgeInsets(top: 3, leading: 16, bottom: 3, trailing: 16))
                                    }
                                }
                                .textCase(nil)
                                .headerProminence(.increased)
                            }
                        }
                        .listStyle(.plain)
                        .scrollContentBackground(.hidden)
                        .background(Color.clear)
                        .navigationTitle(L10n.screenSpeakers)
                        .overlay(alignment: .trailing) {
                            VStack(spacing: 2) {
                                ForEach(viewModel.getAlphabets(), id: \.self) { letter in
                                    Button {
                                        if !viewModel.speakers(for: letter).isEmpty {
                                            withAnimation(.easeInOut(duration: 0.15)) {
                                                scrollProxy.scrollTo(letter, anchor: .top)
                                            }
                                        }
                                    } label: {
                                        Text(letter)
                                            .font(.system(size: 14, weight: .semibold, design: .rounded))
                                            .frame(width: indexWidth, alignment: .center)
                                            .padding(.vertical, 2)
                                    }
                                    .buttonStyle(.plain)
                                    .contentShape(Rectangle())
                                }
                            }
                            .padding(.trailing, indexMargin)
                            .background(Color.black.opacity(0.001))
                            .contentShape(Rectangle())
                            .zIndex(10)
                        }
                    }
                }
                .task { await viewModel.observeSpeakers() }
            }
        }
    }
}

// MARK: - Sous-vues / Helpers

struct SpeakerWrapper: Identifiable {
    var speaker: Speaker_
    var id: String { speaker.id }
}

/// Avatar arrondi avec fallback
private struct Avatar: View {
    let urlString: String?

    var body: some View {
        Group {
            if let urlString, let url = URL(string: urlString) {
                AsyncImage(url: url) { phase in
                    switch phase {
                    case .empty:
                        ProgressView().frame(width: 44, height: 44)
                    case .success(let image):
                        image.resizable().aspectRatio(contentMode: .fill)
                    case .failure:
                        Image(systemName: "person.circle").resizable().aspectRatio(contentMode: .fit)
                    @unknown default:
                        EmptyView()
                    }
                }
            } else {
                Image(systemName: "person.circle").resizable().aspectRatio(contentMode: .fit)
            }
        }
        .frame(width: 44, height: 44)
        .clipShape(Circle())
    }
}

/// Header de section lisible
private struct SectionHeader: View {
    let letter: String
    var body: some View {
        Text(letter)
            .font(.headline.weight(.semibold))
            .foregroundStyle(.secondary)
            .padding(.vertical, 2)
            .padding(.leading, 8)
    }
}

/// Lien de navigation sans chevron système (on met notre chevron custom dans la card)
private struct PushLink<Label: View, Destination: View>: View {
    @State private var isActive = false
    let destination: Destination
    let label: () -> Label

    init(destination: Destination, @ViewBuilder label: @escaping () -> Label) {
        self.destination = destination
        self.label = label
    }

    var body: some View {
        Button(action: { isActive = true }) {
            label()
        }
        .buttonStyle(.plain)
        .background(
            NavigationLink(isActive: $isActive) {
                destination
            } label: { EmptyView() }
            .hidden()
        )
    }
}

/// Conteneur de cellule avec fond Liquid Glass (iOS 26) ou Material (fallback)
