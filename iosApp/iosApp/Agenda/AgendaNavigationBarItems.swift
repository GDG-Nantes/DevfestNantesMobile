import SwiftUI
import shared

struct AgendaNavigationBarItems: View {
    var body: some View {
        Menu("\(Image(systemName: "line.3.horizontal.decrease.circle"))") {
            FavoritesFilterMenu()
            LanguageFilterMenu()
            ComplexityFilterMenu()
            RoomFilterMenu()
            SessionTypeFilterMenu()
            ClearFiltersButton()
        }
    }
}
