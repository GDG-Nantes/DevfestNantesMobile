//
//  CustomButton.swift
//  iosApp
//
//  Created by Stéphane Rihet on 15/09/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import SafariServices

///SwiftUI View
struct CustomButton<Label: View>: View {
    private var label: () -> Label
    private let url: URL
    
    //Display or not the website in the app
    @State private var showSafari = false
    
    //Initialization of CustomButton
    init(url: URL, @ViewBuilder label: @escaping () -> Label) {
        self.url = url
        self.label = label
    }
    
    //setup UI
    var body: some View {
        Button(action: {
            self.showSafari = true
        }, label: label)
        .padding(.vertical, 5)
        .padding(.horizontal, 10)
        .overlay(
            RoundedRectangle(cornerRadius: 20)
                .stroke(Color(Asset.devFestBlue.color), lineWidth: 1)
        )
        .sheet(isPresented: $showSafari) { SafariView(url: self.url) }
    }
}

///Safari view in app
struct SafariView: UIViewControllerRepresentable {
    let url: URL
    
    func makeUIViewController(context: UIViewControllerRepresentableContext<SafariView>) -> SFSafariViewController {
        return SFSafariViewController(url: url)
    }
    
    func updateUIViewController(_ uiViewController: SFSafariViewController,
                                context: UIViewControllerRepresentableContext<SafariView>) {
    }
}
