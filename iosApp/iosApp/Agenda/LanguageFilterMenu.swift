import SwiftUI
import shared

struct LanguageFilterMenu: View {
    @Environment(AgendaViewModel.self) private var viewModel
    struct LanguageItem {
        let value: String
        let label: String
    }

    var languageItems: [LanguageItem] {
        [
            LanguageItem(value: SessionLanguage.french.name, label: L10n.languageFrench),
            LanguageItem(value: SessionLanguage.english.name, label: L10n.languageEnglish)
        ]
    }

    var body: some View {
        Menu(L10n.sessionFiltersDrawerLanguagesLabel) {
            ForEach(languageItems, id: \ .value) { item in
                let isSelected = viewModel.sessionFilters.contains { $0.type == .language && $0.value == item.value }
                Button(action: {
                    viewModel.toggleFilter(type: .language, value: item.value)
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
