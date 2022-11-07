//
//  PlanView.swift
//  DevFest Nantes
//
//  Created by Stéphane Rihet on 17/10/2022.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import PDFKit

///Use PDFView in SwiftUI for display and manipulate image
struct PhotoDetailView: UIViewRepresentable {
    let image: UIImage

    func makeUIView(context: Context) -> PDFView {
        let view = PDFView()
        view.document = PDFDocument()
        guard let page = PDFPage(image: image) else { return view }
        view.document?.insert(page, at: 0)
        view.autoScales = true
        return view
    }

    func updateUIView(_ uiView: PDFView, context: Context) {
        // empty
    }
}
