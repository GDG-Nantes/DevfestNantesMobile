import SwiftUI
import shared

struct FavoritesFilterMenu: View {
    @ObservedObject var viewModel: AgendaViewModel
    var body: some View {
        let isSelected = viewModel.sessionFilters.contains { $0.type == .bookmark }
        Button(action: {
            var newFilters = viewModel.sessionFilters
            let filter = SessionFilter(type: .bookmark, value: "bookmark")
            if newFilters.contains(filter) {
                newFilters.remove(filter)
            } else {
                newFilters.insert(filter)
            }
            viewModel.sessionFilters = newFilters
        }) {
            Label(L10n.filterFavorites, systemImage: isSelected ? "star.fill" : "star")
        }
    }
}
