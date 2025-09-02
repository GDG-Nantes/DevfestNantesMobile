import SwiftUI
import shared

struct ClearFiltersButton: View {
    @ObservedObject var viewModel: AgendaViewModel
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
