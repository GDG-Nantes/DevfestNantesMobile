# Phase 9: iOS on Shared Logic and Legacy Persistence - Pattern Map

**Mapped:** 2026-11-05
**Files analyzed:** 19 (new/modified/deleted)
**Analogs found:** 17 / 19 (all analogs git-tracked; paths are relative to repo root)

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|---|---|---|---|---|
| `shared/src/iosMain/kotlin/com/gdgnantes/devfest/shared/IosDependencies.kt` | provider | request-response | itself (`store()`) | exact |
| `shared/src/iosTest/kotlin/com/gdgnantes/devfest/shared/di/IosKoinTest.kt` | test | request-response | itself | exact |
| `iosApp/iosApp/Agenda/AgendaSource.swift` | port + Live adapter | streaming | itself, `iosApp/iosApp/UI/Speakers/SpeakersSource.swift` | exact |
| `iosApp/iosApp/Agenda/AgendaViewModel.swift` | viewmodel | streaming (continuous) | itself (`observeSessions`), `SpeakersViewModel.swift` | role-match |
| `iosApp/iosApp/Agenda/AgendaView.swift` | component | event-driven | itself | exact |
| `iosApp/iosApp/Agenda/*FilterMenu.swift`, `ClearFiltersButton.swift` | component | event-driven | `RoomFilterMenu.swift` | exact |
| `iosApp/iosApp/Agenda/AgendaDetailView.swift` | component | event-driven | itself | exact |
| `iosApp/iosApp/Model/Data/AgendaContent.swift` | model (display mapping) | transform | itself | exact |
| `iosApp/iosApp/Model/SessionFilter.swift` | model | n/a (DELETE) | n/a | n/a |
| `iosApp/iosApp/UI/Speakers/SpeakersViewModel.swift` + `SpeakersSource.swift` | viewmodel + port | streaming (first emission) | itself | exact |
| `iosApp/iosApp/UI/Venue/VenueViewModel.swift` + `VenueSource.swift` | viewmodel + port | request-response | `SpeakersViewModel.swift` / `SpeakersSource.swift` | role-match |
| `iosApp/iosApp/UI/Speakers/SpeakerDetailsViewModel.swift` (+ view) | viewmodel | streaming | `SpeakersViewModel.swift` | role-match |
| `iosApp/DevFestNantesTests/Fakes/FakeAgendaSource.swift` | test fake | streaming | itself | exact |
| `iosApp/DevFestNantesTests/AgendaViewModelTests.swift` | test | streaming | itself | exact |
| `iosApp/DevFestNantesTests/` new: mapping/golden parity test, scroll-target test, bridge spike | test | transform | `AgendaCharacterizationTests.swift`, `CharacterizationSupport.swift` | role-match |
| `iosApp/DevFestNantesTests/` delete: `SessionFilterTests`, `AgendaDateParsingTests` | test | n/a (DELETE) | n/a | n/a |
| `scripts/swift-names/*.txt`, `names-exclude.txt` | config | batch | regenerate with `scripts/swift-names-gate.sh baseline` | process |
| `documentation/ARCHITECTURE.md`, `AGENTS.md` | docs | n/a | existing iOS sections | process |
| Persistence switch commit (own commit, behind `checkpoint:decision`) | migration-class | n/a | none | no analog |

## Pattern Assignments

### `IosDependencies.kt` (provider, request-response)

**Analog:** same file, `store()` (self-starting resolve). Existing code:
```kotlin
object IosDependencies {
    private val lock = Lockable()
    fun start() {
        KoinPlatformTools.synchronized(lock) {
            if (KoinPlatform.getKoinOrNull() == null) { initKoin() }
        }
    }
    fun store(): DevFestNantesStore {
        start()
        return KoinPlatform.getKoin().get()
    }
}
```
Add `observeAgenda()`, `bookmarksStore()`, `sessionFiltersStore()`, `speakerIndex()`, `venueLanguage()`, `classifySession()` with the same shape. A reified `private inline fun <reified T : Any> resolve()` is the proposed helper (RESEARCH A2: fall back to six explicit bodies if K/N complains). Update the class KDoc ("Swift sees only start and store"). Service-locator use is allowed here per `di-gate.sh`.

### `IosKoinTest.kt` (test)

**Analog:** same file, lines 15-52. Pattern: `@AfterTest stopKoin()`, `IosDependencies.start()`, then `assertNotNull(...)`/`assertSame`. Add one test per accessor, e.g. `assertSame(IosDependencies.bookmarksStore(), IosDependencies.bookmarksStore())` for the singles, `assertNotNull` for the factories. Test names are snake_case sentences.

### `AgendaSource.swift` (port + Live adapter, streaming)

**Analog:** current `AgendaSource.swift` and `SpeakersSource.swift`.
```swift
protocol AgendaSource {
    func sessions() -> AsyncThrowingStream<[Session], Error>
    func rooms() -> AsyncThrowingStream<Set<Room>, Error>
}
struct LiveAgendaSource: AgendaSource {
    private let store: () -> DevFestNantesStore
    init(store: @escaping () -> DevFestNantesStore = { IosDependencies.shared.store() }) { self.store = store }
    func rooms() -> AsyncThrowingStream<Set<Room>, Error> {
        let resolvedStore = store()
        return KotlinStreamBridge.stream(resolvedStore.rooms)
    }
}
```
Keep: lazy resolution (never in an initializer), `KotlinStreamBridge.stream(...)` as the only flow bridge, `rooms()` unchanged. Replace `sessions()` with `agenda()` (`[DaySchedule]`), add `currentFilters`, `setFilters`, `bookmarkedIds()` stream, `setBookmarked`, `isTalk`/`isNavigable`. See RESEARCH "Live adapter" code block (lines 409-438) for the full shape, including `AgendaDays` and evaluating `Kotlinx_datetimeTimeZone.companion.currentSystemDefault()` per subscription. The `store:` init-closure style should be kept for each accessor, as injectable default arguments, so tests can still use the mocked store.

### `AgendaViewModel.swift` (viewmodel, continuous streaming)

**Analog:** current `observeSessions()` (lines 83-110) for trace, cancellation and error structure:
```swift
do {
    let sessions = try await tracing.trackDataLoad(
        traceName: PerformanceMonitoring.TRACE_AGENDA_LOAD, dataSource: "graphql"
    ) {
        ...
        try Task.checkCancellation()   // bridge ends silently on cancel
        return first ?? []
    }
    ...
} catch is CancellationError {
    return
} catch {
    DevFestLogger(category: "Agenda").log(.error, "...: \(error.localizedDescription)", error: error)
    isLoading = false
}
```
Conventions to keep: `@Observable @MainActor final class`, `@ObservationIgnored private let` for ports and tracing, ports injected through init default arguments, `observeRooms()` unchanged. Remove: `defaults:` param, `didSet` persistence, `saveFilters`, `sessionsChanged`, `getDate`, `parseDate`, `sectionDateFormatter`.
New core: RESEARCH Pattern 2 (first-emission iterator inside `trackDataLoad`, then a `while let next = try await iterator.next()` loop) and Pattern 3 (mirror with `toggleBookmark -> Bool`, `toggleFilter`, `clearFilters` writing `[]`). Keep `isLoading` initialized `true` and never reset (D-03).

### Filter menus, `ClearFiltersButton.swift`, `AgendaNavigationBarItems.swift` (component)

**Analog:** `RoomFilterMenu.swift` (lines 11-38). Current structure reads `viewModel.sessionFilters.contains { $0.type == .room && $0.value == room.id }` and builds a `SessionFilter(type: .room, value: item.id)`, then assigns `viewModel.sessionFilters = newFilters`. Change only: `.type == .room` becomes a comparison against the Kotlin `SessionFilterType` (`SessionFilterType.room`, compared with `==`, no exhaustive switch), and the toggle becomes `viewModel.toggleFilter(filter)`. Keep `Environment(AgendaViewModel.self)`, the Swift literal option lists, and `Array(rooms)` (RESEARCH Pitfall 9). Keep the favorites filter value `"bookmark"`.

### `AgendaView.swift` (component)

**Analog:** itself. Keep the `.task` structure and order (lines 57-63): `observeRooms` task, then `RCValues.sharedInstance.fetchCloudValues()` followed by the agenda call, which becomes the seed bookmarks call and then `observeAgenda()`. Replace the 5 hardcoded `"2025-10-16"`/`"2025-10-17"` literals (e.g. lines 75-76) with `AgendaDays.day1/day2`. Extract `handleInitialScrollIfNeeded` (lines 190-215) into `static func scrollTarget(slotDates:now:calendar:) -> Date?` per RESEARCH Pattern 5, with the `currentDay == day && firstAppear` guard staying in the view. Delete `sections(_:forDay:)` and `filteredSessions` logic.

### `AgendaContent.swift` (model, transform)

**Analog:** itself. Keep `Section { date, day, sessions }` and `Session`, now with stored `isATalk` and `isNavigable` and an `init(from:isATalk:isNavigable:)`. Mapping sketch is in RESEARCH lines 443-454. `SpeakerDetailsView` (lines 71-72) builds `AgendaContent.Session` directly, so route it through the VM.

### `SpeakersViewModel.swift` / `SpeakersSource.swift` and Venue equivalents

**Analog:** `SpeakersViewModel.swift` (first-emission-only pattern, lines 33-58) and `SpeakersSource.swift`. Add the use case to the port with a lazy closure, same style as `store: () -> DevFestNantesStore`. Keep `getAlphabets()` / `speakers(for:)` signatures, backed by `SpeakerIndexUseCase.letterGroups(speakers:)`. Venue: `languageFor(languageCode:)` replaces the `Locale.current.languageCode == "fr"` test.

### Tests (Swift Testing)

**Analogs:** `AgendaViewModelTests.swift` (`@MainActor @Suite(.timeLimit(.minutes(1))) struct`, private helpers, `RecordingDataLoadTracing` asserting `recordedTraces == [.init(traceName: "agenda_load", dataSource: "graphql")]`), `Fakes/FakeAgendaSource.swift` (scripted `FakeStream<Element>` served through the real bridge, `.emit([..], thenFinish:)`, `.fail`, `.hang`, `subscriptionCount`, `terminationCount`, `waitUntilSubscribed()`), `CharacterizationSupport.swift` (`TestData`, `Golden.assertMatches("agenda/golden/ios-agenda-<Zone>.json", ...)`).
- Drop `defaults:`/`Suite` helpers from the VM tests (no UserDefaults in the VM).
- `FakeAgendaSource` gains `agendaStream: FakeStream<[DaySchedule]>`, `bookmarksStream`, in-memory `filters`/`bookmarked` recording the write-through calls, and classify closures.
- Required tests per CONTEXT: re-emission, trace on first emission only (one recorded trace after several emissions), keep content on resubscribe, mid-stream error keeps content, mirror write-through, silent cancellation.
- Bridge spike (RESEARCH Pitfall 4): hermetic hosted test with `IosDependencies.shared.sessionFiltersStore().filtersFlow`, `setFilters`, expect a second element, and clean up in `defer`.
- Pass an explicit `Europe/Paris` zone and calendar (CI runs UTC).
- Delete `SessionFilterTests.swift`, `AgendaDateParsingTests.swift`; remove the `DevFest_Nantes.SessionFilter` qualifications in `AgendaCharacterizationTests`/`CharacterizationSupport`; never edit goldens.

## Shared Patterns

### Only `Live...` adapters touch Kotlin
**Source:** `AgendaSource.swift`, `SpeakersSource.swift`. `IosDependencies.shared.xxx()` is called inside closures or methods, never in an `init`, views, VMs, or `AgendaContent`.

### Task-free streaming
**Source:** `iosApp/iosApp/Model/KotlinStreamBridge.swift`. `KotlinStreamBridge.stream(kotlinFlow)` returns `AsyncThrowingStream`; no `Task {}` and no Combine in VMs.

### Error and cancellation
**Source:** `SpeakersViewModel.observeSpeakers` (lines 33-58). `catch is CancellationError { return }`, otherwise `logger.log(.error, "...", error: error)` through `DevFestLogger(category:)`. Never `print` or `os_log`.

### Observation gate
**Source:** `scripts/swift-observation-gate.sh` scans comments too. Write "the legacy observation APIs" in prose, and never name the banned symbols.

### swift-names gate
Run `bash scripts/swift-names-gate.sh baseline`, then `check` and `selftest`, in the same commit as the Swift call sites. Remove the `SessionFilter` line from `scripts/swift-names/names-exclude.txt`.

## No Analog Found

| File | Role | Reason |
|---|---|---|
| Store-switch commit (VM stops using the `Favorites` / `AgendaSessionFilters` keys) | migration-class | One-way (07 D-15). Own revertible commit behind `checkpoint:decision`; follow RESEARCH "Runtime State Inventory" |
| In-place update UAT flow (no `clearState`) | UAT | Phase 8 `.planning/phases/08-modern-swiftui-ios-17-and-observation/08-parity/parity-flow.yaml` is the closest, but it starts with `clearState: true`, so a separate flow is required (RESEARCH Pitfall 8) |
| Continuous real Kotlin flow through the bridge | streaming | Never exercised yet; see the spike above |

Open question for the planner: `BookmarksStore` has no snapshot (only `bookmarkedSessionIds` flow), so the bookmark mirror is seeded by a first-emission read in the `.task` before the collection (RESEARCH Pitfall 1 and Open Question 1).

## Metadata

**Analog search scope:** `iosApp/iosApp/Agenda`, `iosApp/iosApp/UI`, `iosApp/iosApp/Model`, `iosApp/DevFestNantesTests`, `shared/src/iosMain`, `shared/src/iosTest`
**Files scanned:** ~20
**Pattern extraction date:** 2026-11-05
