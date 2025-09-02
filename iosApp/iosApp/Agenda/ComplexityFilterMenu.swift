import SwiftUI
import shared

struct ComplexityFilterMenu: View {
    @ObservedObject var viewModel: AgendaViewModel
    var body: some View {
        Menu(L10n.sessionFiltersDrawerComplexityLabel) {
            ForEach([Complexity.beginner.rawValue, Complexity.intermediate.rawValue, Complexity.advanced.rawValue], id: \ .self) { comp in
                Button(action: {
                    var newFilters = viewModel.sessionFilters
                    let filter = SessionFilter(type: .complexity, value: comp)
                    if newFilters.contains(filter) {
                        newFilters.remove(filter)
                    } else {
                        newFilters.insert(filter)
                    }
                    viewModel.sessionFilters = newFilters
                }) {
                    HStack {
                        Image(systemName: viewModel.sessionFilters.contains { $0.type == .complexity && $0.value == comp } ? "checkmark.square" : "square")
                        Text(
                            comp == Complexity.beginner.rawValue ? L10n.complexityBeginer :
                            comp == Complexity.intermediate.rawValue ? L10n.complexityIntermediate :
                            L10n.complexityAdvanced
                        )
                    }
                }
            }
        }
    }
}
