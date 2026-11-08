import SwiftUI
import shared

struct ComplexityFilterItem: Identifiable {
    let id: String
    let label: String
    let isSelected: Bool
}

struct ComplexityFilterMenu: View {
    @Environment(AgendaViewModel.self) private var viewModel

    private var filterItems: [ComplexityFilterItem] {
        let complexities: [Complexity] = [.beginner, .intermediate, .advanced]
        return complexities.map { comp in
            let label: String = {
                if comp == .beginner { return L10n.complexityBeginer }
                if comp == .intermediate { return L10n.complexityIntermediate }
                if comp == .advanced { return L10n.complexityAdvanced }
                return "" // fallback, should never happen
            }()
            let value = comp.name // Use uppercase enum name for filter value/id
            let isSelected = viewModel.sessionFilters.contains { $0.type == .complexity && $0.value == value }
            return ComplexityFilterItem(id: value, label: label, isSelected: isSelected)
        }
    }

    var body: some View {
        Menu(L10n.sessionFiltersDrawerComplexityLabel) {
            ForEach(filterItems) { item in
                Button(action: {
                    viewModel.toggleFilter(type: .complexity, value: item.id)
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
