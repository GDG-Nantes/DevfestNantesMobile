import SwiftUI
import shared

struct FavoritesFilterMenu: View {
    @ObservedObject var viewModel: AgendaViewModel
    var body: some View {
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
            Label(L10n.filterFavorites, systemImage: viewModel.sessionFilters.contains { $0.type == .bookmark } ? "star.fill" : "star")
        }
    }
}
