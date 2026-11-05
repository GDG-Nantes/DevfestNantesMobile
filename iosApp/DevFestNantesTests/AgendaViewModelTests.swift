import Testing
import Foundation
import shared
@testable import DevFest_Nantes

/// Agenda ViewModel tests. Every test builds its own source fake, tracing fake, throwaway defaults suite
/// and ViewModel, so nothing is shared between tests and the app's standard defaults are never touched.
@MainActor @Suite(.timeLimit(.minutes(1))) struct AgendaViewModelTests {

    /// Tracer: the live adapter over the in-memory mocked Kotlin store drives the ViewModel end to end
    /// through `KotlinStreamBridge` and the store's native flows (no network, Firebase or Koin), and the
    /// sessions load goes through the tracing seam under the v1.0 trace name and data source.
    @Test func live_source_loads_the_mocked_store_sessions_and_rooms() async {
        let store = DevFestNantesStoreBuilder().setUseMockServer(useMockServer: true).build()
        let tracing = RecordingDataLoadTracing()
        let suiteName = "AgendaViewModelTests-\(UUID().uuidString)"
        let suite = UserDefaults(suiteName: suiteName)!
        defer { suite.removePersistentDomain(forName: suiteName) }
        let viewModel = AgendaViewModel(source: LiveAgendaSource(store: { store }), tracing: tracing, defaults: suite)

        await viewModel.observeSessions()
        await viewModel.observeRooms()

        #expect(!viewModel.agendaContent.sections.isEmpty)
        #expect(viewModel.isLoading == false)
        #expect(!(viewModel.roomsContent ?? []).isEmpty)
        #expect(tracing.recordedTraces == [.init(traceName: "agenda_load", dataSource: "graphql")])
    }
}
