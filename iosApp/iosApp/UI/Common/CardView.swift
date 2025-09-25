import SwiftUI

struct Card<Content: View>: View {
    let content: () -> Content
    private let corner: CGFloat = 16
    private let inner: CGFloat = 14
    private let outer: CGFloat = 8

    var body: some View {
        if #available(iOS 26.0, *) {
            VStack(alignment: .leading, spacing: 0) {
                content()
                    .padding(inner)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .background {
                GlassEffectContainer {
                    Color.clear
                        .glassEffect(.regular, in: .rect(cornerRadius: corner))
                }
            }
            .clipShape(RoundedRectangle(cornerRadius: corner, style: .continuous))
            .padding(outer)
        } else {
            VStack(alignment: .leading, spacing: 0) {
                content().padding(inner)
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(
                RoundedRectangle(cornerRadius: corner, style: .continuous)
                    .fill(Color(Asset.secondaryBackground.color))
            )
            .clipShape(RoundedRectangle(cornerRadius: corner, style: .continuous))
            .padding(outer)
        }
    }
}