import SwiftUI
import shared

struct RoomFilterItem: Identifiable {
    let id: String
    let name: String
    let isSelected: Bool
}

struct RoomFilterMenu: View {
    @ObservedObject var viewModel: AgendaViewModel
    var body: some View {
        let filterItems: [RoomFilterItem] = {
            guard let rooms = viewModel.roomsContent else { return [] }
            return rooms.map { room in
                let isSelected = viewModel.sessionFilters.contains { $0.type == .room && $0.value == room.id }
                return RoomFilterItem(id: room.id, name: room.name, isSelected: isSelected)
            }
        }()
        return Menu(L10n.sessionFiltersDrawerRoomsLabel) {
            ForEach(filterItems) { item in
                Button(action: {
                    var newFilters = viewModel.sessionFilters
                    let filter = SessionFilter(type: .room, value: item.id)
                    if newFilters.contains(filter) {
                        newFilters.remove(filter)
                    } else {
                        newFilters.insert(filter)
                    }
                    viewModel.sessionFilters = newFilters
                }) {
                    HStack {
                        Image(systemName: item.isSelected ? "checkmark.square" : "square")
                        Text(item.name)
                    }
                }
            }
        }
    }
}
