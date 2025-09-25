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
    @State private var showSafari = false
    
    init(url: URL, @ViewBuilder label: @escaping () -> Label) {
        self.url = url
        self.label = label
    }
    
    var body: some View {
        if #available(iOS 26.0, *) {
            Button(action: { showSafari = true }, label: label)
                .buttonStyle(LiquidGlassButtonStyle())
                .sheet(isPresented: $showSafari) { SafariView(url: url) }
        } else {
            Button(action: { showSafari = true }, label: label)
                .buttonStyle(MaterialCapsuleButtonStyle())
                .sheet(isPresented: $showSafari) { SafariView(url: url) }
        }
    }
}

private struct MaterialCapsuleButtonStyle: ButtonStyle {
    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.callout.weight(.semibold))
            .padding(.vertical, 8)
            .padding(.horizontal, 14)
            .background(.thinMaterial, in: Capsule())
            .overlay(Capsule().stroke(.white.opacity(0.15), lineWidth: 1))
            .scaleEffect(configuration.isPressed ? 0.98 : 1)
            .animation(.easeOut(duration: 0.12), value: configuration.isPressed)
    }
}

struct SafariView: UIViewControllerRepresentable {
    let url: URL
    
    func makeUIViewController(context: UIViewControllerRepresentableContext<SafariView>) -> SFSafariViewController {
        return SFSafariViewController(url: url)
    }
    
    func updateUIViewController(_ uiViewController: SFSafariViewController,
                                context: UIViewControllerRepresentableContext<SafariView>) {
    }
}