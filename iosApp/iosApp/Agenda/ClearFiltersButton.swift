import SwiftUI
import shared

struct ClearFiltersButton: View {
    @Environment(AgendaViewModel.self) private var viewModel
    var body: some View {
        if !viewModel.sessionFilters.isEmpty {
            Button(action: {
                viewModel.clearFilters()
            }) {
                Label(L10n.filterClear, systemImage: "trash")
            }
        }
    }
}
