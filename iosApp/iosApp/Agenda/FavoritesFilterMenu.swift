import SwiftUI
import shared

struct FavoritesFilterMenu: View {
    @Environment(AgendaViewModel.self) private var viewModel
    var body: some View {
        let isSelected = viewModel.sessionFilters.contains { $0.type == .bookmark }
        Button(action: {
            viewModel.toggleFilter(type: .bookmark, value: "bookmark")
        }) {
            Label(L10n.filterFavorites, systemImage: isSelected ? "star.fill" : "star")
        }
    }
}
