import SwiftUI
import shared

struct LanguageFilterMenu: View {
    @ObservedObject var viewModel: AgendaViewModel
    var body: some View {
        Menu(L10n.sessionFiltersDrawerLanguagesLabel) {
            ForEach([SessionLanguage.french.rawValue, SessionLanguage.english.rawValue], id: \ .self) { lang in
                Button(action: {
                    var newFilters = viewModel.sessionFilters
                    let filter = SessionFilter(type: .language, value: lang)
                    if newFilters.contains(filter) {
                        newFilters.remove(filter)
                    } else {
                        newFilters.insert(filter)
                    }
                    viewModel.sessionFilters = newFilters
                }) {
                    HStack {
                        Image(systemName: viewModel.sessionFilters.contains { $0.type == .language && $0.value == lang } ? "checkmark.square" : "square")
                        Text(lang == SessionLanguage.french.rawValue ? L10n.languageFrench : L10n.languageEnglish)
                    }
                }
            }
        }
    }
}
