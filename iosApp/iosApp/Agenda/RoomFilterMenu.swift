import SwiftUI
import shared

struct RoomFilterMenu: View {
    @ObservedObject var viewModel: AgendaViewModel
    var body: some View {
        if let rooms = viewModel.roomsContent {
            Menu(L10n.sessionFiltersDrawerRoomsLabel) {
                ForEach(rooms, id: \ .id) { room in
                    Button(action: {
                        var newFilters = viewModel.sessionFilters
                        let filter = SessionFilter(type: .room, value: room.id)
                        if newFilters.contains(filter) {
                            newFilters.remove(filter)
                        } else {
                            newFilters.insert(filter)
                        }
                        viewModel.sessionFilters = newFilters
                    }) {
                        HStack {
                            Image(systemName: viewModel.sessionFilters.contains { $0.type == .room && $0.value == room.id } ? "checkmark.square" : "square")
                            Text(room.name)
                        }
                    }
                }
            }
        }
    }
}
