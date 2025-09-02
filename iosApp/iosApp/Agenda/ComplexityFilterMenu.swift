
import SwiftUI
import shared

struct ComplexityFilterItem: Identifiable {
    let id: String
    let label: String
    let isSelected: Bool
}

struct ComplexityFilterMenu: View {
    @ObservedObject var viewModel: AgendaViewModel
    var body: some View {
        let complexities: [Complexity] = [.beginner, .intermediate, .advanced]
        let filterItems: [ComplexityFilterItem] = complexities.map { comp in
            let label: String = {
                switch comp {
                case .beginner: return L10n.complexityBeginer
                case .intermediate: return L10n.complexityIntermediate
                case .advanced: return L10n.complexityAdvanced
                default: return comp.text
                }
            }()
            let isSelected = viewModel.sessionFilters.contains { $0.type == .complexity && $0.value == comp.rawValue }
            return ComplexityFilterItem(id: comp.rawValue, label: label, isSelected: isSelected)
        }
        Menu(L10n.sessionFiltersDrawerComplexityLabel) {
            ForEach(filterItems) { item in
                Button(action: {
                    var newFilters = viewModel.sessionFilters
                    let filter = SessionFilter(type: .complexity, value: item.id)
                    if newFilters.contains(filter) {
                        newFilters.remove(filter)
                    } else {
                        newFilters.insert(filter)
                    }
                    viewModel.sessionFilters = newFilters
                }) {
                    HStack {
                        Image(systemName: item.isSelected ? "checkmark.square" : "square")
                        Text(item.label)
                    }
                }
            }
        }
    }
}
