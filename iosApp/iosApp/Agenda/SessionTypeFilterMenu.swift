import SwiftUI
import shared

struct SessionTypeFilterMenu: View {
    @ObservedObject var viewModel: AgendaViewModel
    var body: some View {
        Menu(L10n.sessionFiltersDrawerTypeLabel) {
            ForEach([SessionType.conference.rawValue, SessionType.quickie.rawValue, SessionType.codelab.rawValue], id: \ .self) { type in
                Button(action: {
                    var newFilters = viewModel.sessionFilters
                    let filter = SessionFilter(type: .type, value: type)
                    if newFilters.contains(filter) {
                        newFilters.remove(filter)
                    } else {
                        newFilters.insert(filter)
                    }
                    viewModel.sessionFilters = newFilters
                }) {
                    HStack {
                        Image(systemName: viewModel.sessionFilters.contains { $0.type == .type && $0.value == type } ? "checkmark.square" : "square")
                        Text(
                            type == SessionType.conference.rawValue ? L10n.sessionTypeConference :
                            type == SessionType.quickie.rawValue ? L10n.sessionTypeQuickie :
                            L10n.sessionTypeCodelab
                        )
                    }
                }
            }
        }
    }
}
