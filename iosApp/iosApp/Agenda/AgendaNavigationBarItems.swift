import SwiftUI
import shared

struct AgendaNavigationBarItems: View {
    @ObservedObject var viewModel: AgendaViewModel
    var body: some View {
        Menu("\(Image(systemName: "line.3.horizontal.decrease.circle"))") {
            FavoritesFilterMenu(viewModel: viewModel)
            LanguageFilterMenu(viewModel: viewModel)
            ComplexityFilterMenu(viewModel: viewModel)
            RoomFilterMenu(viewModel: viewModel)
            SessionTypeFilterMenu(viewModel: viewModel)
            ClearFiltersButton(viewModel: viewModel)
        }
    }
}
