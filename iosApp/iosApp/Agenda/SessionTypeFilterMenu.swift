import SwiftUI
import shared

struct SessionTypeFilterMenu: View {
    @ObservedObject var viewModel: AgendaViewModel
    struct SessionTypeItem {
        let value: String
        let label: String
    }

    var sessionTypeItems: [SessionTypeItem] {
        [
            SessionTypeItem(value: SessionType.conference.name, label: L10n.sessionTypeConference),
            SessionTypeItem(value: SessionType.quickie.name, label: L10n.sessionTypeQuickie),
            SessionTypeItem(value: SessionType.codelab.name, label: L10n.sessionTypeCodelab)
        ]
    }

    var body: some View {
        Menu(L10n.sessionFiltersDrawerTypeLabel) {
            ForEach(sessionTypeItems, id: \ .value) { item in
                let isSelected = viewModel.sessionFilters.contains { $0.type == .type && $0.value == item.value }
                Button(action: {
                    var newFilters = viewModel.sessionFilters
                    let filter = SessionFilter(type: .type, value: item.value)
                    if newFilters.contains(filter) {
                        newFilters.remove(filter)
                    } else {
                        newFilters.insert(filter)
                    }
                    viewModel.sessionFilters = newFilters
                }) {
                    HStack {
                        Image(systemName: isSelected ? "checkmark.square" : "square")
                        Text(item.label)
                    }
                }
            }
        }
    }
}
