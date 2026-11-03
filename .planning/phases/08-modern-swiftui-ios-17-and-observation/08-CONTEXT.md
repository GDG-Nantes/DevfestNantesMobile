# Phase 8: Modern SwiftUI, iOS 17 and Observation - Context

**Gathered:** 2026-11-03
**Status:** Ready for planning

<domain>
## Phase Boundary

The iOS app moves to an iOS 17.0 floor on every target configuration, and the five ViewModels
(Agenda, Speakers, SpeakerDetails, Venue, About) move from `ObservableObject`/Combine to
`@Observable @MainActor`, owned via `@State` (or environment for the shared Agenda VM), with
collection started in `.task` and cancelled on disappear. Combine, `KMPNativeCoroutinesCombine`,
`ObservableObject`, `@Published`, `objectWillChange.send()` and `DispatchQueue.main.async` leave the
iOS sources and the Combine SPM product leaves the project. Each ViewModel gets Swift Testing tests on
hand-written fakes behind narrow per-VM ports, run in the Phase 6 CI lane. No visible change from v1.0.

Out of this phase: shared filtering/grouping/persistence and continuous agenda observation (Phase 9),
explicit DI / `AppDependencies` / removal of singletons and `BaseViewModel` as a DI holder (Phase 10),
SPM packages (Phase 11), Swift 6 and warning-free build (Phase 12).

</domain>

<decisions>
## Implementation Decisions

### Agenda ViewModel instances
- **D-01:** **One shared `AgendaViewModel` instance app-wide.** The per-view `AgendaViewModel()`
  instances in `AgendaCellView` and `AgendaDetailView` (and the `@ObservedObject var viewModel =
  AgendaViewModel()` default in `AgendaView`) are removed.
- **D-02:** **`ContentView` owns it via `@State` and injects it with `.environment(agendaViewModel)`.**
  `AgendaView`, `AgendaDetailView` (reached from the Agenda tab *and* from Speakers → speaker detail →
  session) and the filter menus / `AgendaNavigationBarItems` / `ClearFiltersButton` read
  `@Environment(AgendaViewModel.self)` (with `@Bindable` where a binding is needed).
  `AgendaCellView` (only used in `SpeakerDetailsView` with `isBookmarked: false`) drops its unused VM.
- **D-03:** If the single instance makes a bookmark or filter change refresh the agenda list
  immediately where v1.0 did not (e.g. un-bookmarking from a detail while the Favorites filter is on),
  this is **accepted and recorded as a documented deviation**, not fixed back to v1.0 behavior.
  Phase 9 criterion 4 requires the single instance anyway.

### Ports and fakes
- **D-04:** **Narrow port per VM, exposing Kotlin models.** Streams as
  `AsyncThrowingStream<[Model], Error>`, one-shot calls as `async throws` (e.g.
  `protocol SpeakersSource { func speakers() -> AsyncThrowingStream<[Speaker], Error> }`,
  `func venue(language:) async throws -> Venue`). A `Live…` adapter wraps
  `asyncSequence(for:)` / `asyncFunction(for:)` on `DevFestNantesStore`. **Only live adapters call
  the KMP-NativeCoroutines bridge**; ViewModels never do. Swift value types are not introduced here.
- **D-05:** **Live wiring through an init default argument** until Phase 10:
  `init(source: SpeakersSource = LiveSpeakersSource(), ...)`, the live adapter reading
  `IosDependencies.shared.store()`. Tests pass fakes. Phase 10 removes the defaults.
- **D-06:** **Performance tracing seam:** a small protocol (e.g. `DataLoadTracing` with
  `trackDataLoad<T>(traceName:dataSource:block:)`) that `PerformanceMonitoring` conforms to; VMs take
  `tracing: DataLoadTracing = PerformanceMonitoring.shared`. Tests use a recording fake and may assert
  trace names. Mirrors the Android `PerformanceMonitoring` seam (Phase 4, 04-02). Trace names and
  `dataSource` values stay identical.
- **D-07:** **Locations:** each port protocol + its live adapter in a `<Feature>Source.swift` file next
  to its VM (ready to move into feature packages in Phase 11). Fakes in `iosApp/DevFestNantesTests/Fakes/`.
  Fakes return Kotlin values built from the recorded snapshot (`TestData` in
  `CharacterizationSupport.swift`) or small hand-built values. No mocking library.
- **D-08:** **`AgendaViewModel` persistence stays in the VM on the legacy keys**, made testable by
  injecting `defaults: UserDefaults = .standard`. Tests use a throwaway `UserDefaults(suiteName:)`.
  Keys (`Favorites`, `AgendaSessionFilters`) and formats stay byte-identical (`[String]` and Swift
  `JSONEncoder` data of `Set<SessionFilter>`). `favorites` becomes stored observable state (written
  through to `UserDefaults`) so Observation tracks it. No Swift persistence port, because Phase 9
  replaces this with the shared Kotlin stores.

### Collection lifecycle
- **D-09:** **Keep v1.0 "first emission only" semantics** for Agenda sessions, Speakers, About
  (partners) and SpeakerDetails. **Rooms stays continuous**, now collected inside the view's `.task`
  (no unstructured `Task {}`) and cancelled when the view disappears. Continuous agenda observation is
  Phase 9 (IOSDATA-03).
- **D-10:** **Reload on every appear, as in v1.0.** `.task` re-runs the load each time the view
  appears. About still resets `isLoading = true` and its content as today; the others keep their
  content until the new value arrives. Only VM recreation goes away.
- **D-11:** **Cancellation is silent.** `CancellationError` (view disappears mid-load) is not logged
  as an error and does not change `isLoading`/content; the next `.task` run starts over. Other errors
  keep today's logging and `isLoading = false` (no error UI, pinned v1.0 debt).

### Modernization scope, slicing and proof
- **D-12:** **Only what Observation touches.** Change only the lines the migration needs
  (`@State`/`@Environment`/`@Bindable`, `.task`, `onChange` sites observing VM state, removal of
  `DispatchQueue.main.async`, including the one in `AgendaView` scroll handling). Other APIs
  deprecated or soft-deprecated at iOS 17 (`navigationBarTitle`, `navigationBarItems`, `accentColor`,
  `foregroundColor`, the one-parameter `onChange` where not otherwise touched...) stay and are logged
  as debt for Phase 12.
- **D-13:** **iOS 17.0 on every target configuration** (today two configs at 15.0, four at 17.0) as
  the first plan.
- **D-14:** **Plan slicing:** (1) iOS 17 floor; (2) About; (3) Venue; (4) Speakers + SpeakerDetails;
  (5) Agenda (shared instance, environment, filter menus, rooms in `.task`); each VM plan carries its
  port, live adapter, fake and tests; (6) Combine removal: `iOSApp.swift` imports,
  `BaseViewModel` and `PerformanceMonitoring` off `ObservableObject`, unused
  `@StateObject BaseViewModel()` in `ContentView` removed, `KMPNativeCoroutinesCombine` product
  removed from `project.pbxproj`, gate wired; then the parity checkpoint. Every step green and
  shippable.
- **D-15:** **Permanent CI grep gate** `scripts/swift-observation-gate.sh` (same style as `di-gate.sh`
  / `kmp-nc-version-gate.sh`): fails on `import Combine`, `KMPNativeCoroutinesCombine`,
  `ObservableObject`, `@Published`, `objectWillChange` and `DispatchQueue.main.async` in iOS Swift
  sources (and the Combine product in `project.pbxproj`). Runs in `ios.yml` and locally.
- **D-16:** **Parity proof = paired simulator screenshots + human checkpoint** (07-19 pattern): the
  agent captures `main` vs branch for agenda (both days, each filter type, favorite toggle from list
  and from detail, favorites after relaunch), session detail, speakers + A–Z index, speaker detail,
  venue and about; a human approves the diff. Swift tests stay green. Any difference from D-03 is
  listed as a documented deviation.

### Claude's Discretion
- Exact port/adapter/fake names and method signatures; whether `Live…` adapters are structs or
  classes; how `AsyncThrowingStream` is built from `asyncSequence`.
- How `SpeakerDetailsViewModel` (takes `speakerId`) and `VenueViewModel` (reads the language) are
  created under `@State` (e.g. `State(initialValue:)` in the view's `init`), as long as init stays
  side-effect free.
- What `BaseViewModel` becomes in Phase 8 (plain class holding the store, or dropped in favor of the
  ports) as long as it is no longer `ObservableObject`; Phase 10 deletes it.
- Use of `@ObservationIgnored` for non-UI stored properties (formatters, defaults, ports).
- Test case list per VM (at minimum: loading → content, first-emission-only, error → `isLoading =
  false`, cancellation silent; Agenda: favorites toggle persisted, filters persisted/cleared, rooms
  stream).
- Exact gate script output format and whether it also scans `DevFestNantesTests`.
- Documentation updates required by the AGENTS.md rule (`documentation/ARCHITECTURE.md` line on
  "Combine / async sequences", `documentation/DEVELOPMENT.md`, `scripts/README.md`, `AGENTS.md` gate
  list) in the same commits.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Phase scope and requirements
- `.planning/ROADMAP.md` §"Phase 8: Modern SwiftUI, iOS 17 and Observation" — goal, 5 success criteria, research flag (replay the KMP-NC async bridge on CI toolchain Xcode 26.6 / Swift 6.3.3)
- `.planning/REQUIREMENTS.md` — IOSUI-01, IOSUI-02, IOSUI-03, IOSTEST-01 (and coverage notes lines ~126-128)
- `.planning/PROJECT.md` — core value (no regression), Key Decisions

### Research
- `.planning/research/SUMMARY.md` — Phase "iOS 17, `@Observable`, Combine removal" outline; pitfall 5 (Observation lifetime bugs, `.task(id:)`, CancellationError as non-error); "features never import the Kotlin framework" direction
- `.planning/research/PITFALLS.md` — Observation lifetime and Sendable pitfalls
- `.planning/research/ARCHITECTURE.md` — ports per feature, bridge-only `asyncSequence`, `AppDependencies` (Phase 10)
- `.planning/research/STACK.md` — KMP-NativeCoroutines 1.0.6 async usage, iOS 17 floor

### Prior phase decisions
- `.planning/phases/06-ios-test-foundation-and-ci-gates/06-CONTEXT.md` — app-hosted `DevFestNantesTests`, XCTest guard in `iOSApp.init` (D-02), CI lane and gates (D-04..D-10)
- `.planning/phases/07-kmp-domain-layer-and-android-adoption/07-CONTEXT.md` — D-13 (`DevFest_Nantes.SessionFilter` qualification rule), D-15 (iOS persistence moves to new keys in Phase 9; Phase 8 keeps legacy keys)

### Code and tooling
- `iosApp/iosApp/Agenda/AgendaViewModel.swift`, `iosApp/iosApp/Agenda/AgendaView.swift`, `AgendaCellView.swift`, `AgendaDetailView.swift`, filter menus — Agenda migration surface
- `iosApp/iosApp/UI/{About,Venue,Speakers}/*ViewModel.swift` and views — other VMs
- `iosApp/iosApp/Model/BaseViewModel.swift`, `iosApp/iosApp/Performance/PerformanceMonitoring.swift`, `iosApp/iosApp/iOSApp.swift`, `iosApp/iosApp/UI/ContentView.swift` — Combine/ObservableObject leftovers
- `iosApp/iosApp.xcodeproj/project.pbxproj` — deployment targets, `KMPNativeCoroutinesCombine` product
- `iosApp/DevFestNantesTests/` (`CharacterizationSupport.swift`, characterization suites) — existing Swift Testing target and snapshot loaders
- `core/domain/testdata/agenda/` — recorded snapshot used by fakes
- `scripts/di-gate.sh`, `scripts/kmp-nc-version-gate.sh`, `scripts/README.md` — gate script style
- `.github/workflows/ios.yml` — CI lane where tests and the new gate run
- `AGENTS.md` §iOS — logging (`DevFestLogger`), swift-names-gate rule, test commands
- `documentation/ARCHITECTURE.md` (line ~324 mentions Combine), `documentation/DEVELOPMENT.md`

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `TestData` / `TestHost` / `Golden` in `iosApp/DevFestNantesTests/CharacterizationSupport.swift`: build real Kotlin `Session`/`Speaker`/`Room` values from the recorded snapshot; reuse for fakes.
- `AgendaView.sections(_:forDay:)` / `AgendaView.filteredSessions(...)` statics (Phase 7 D-10 extraction): already testable, keep them unchanged.
- `AgendaViewModel.parseDate` static: pinned by `AgendaDateParsingTests`.
- `DevFestLogger`: the logging path for errors (not for cancellation, D-11).

### Established Patterns
- VMs today: `BaseViewModel: ObservableObject` holding `store = IosDependencies.shared.store()`, `@Published` state, `DispatchQueue.main.async` hops, `asyncSequence(...)` with `break` after the first emission.
- Views today own VMs with `@ObservedObject var viewModel = VM()` (recreated on parent re-render); `SpeakerDetailsView` uses `@StateObject`.
- `AgendaView` already loads in `.task` (`RCValues.sharedInstance.fetchCloudValues()`, `observeRooms()`, `observeSessions()`).
- `observeRooms()` wraps an unstructured `Task {}` that is never cancelled (to fix per IOSUI-02).
- Swift test rule: app filter type written `DevFest_Nantes.SessionFilter` in files importing `shared`.
- Characterization tests call `AgendaViewModel()` / `SpeakersViewModel()` / `VenueViewModel()` directly and must keep compiling (init defaults, D-05).

### Integration Points
- `ContentView` TabView: new owner of the shared `AgendaViewModel` (D-02).
- `SpeakerDetailsView` → `AgendaDetailView`: reads the environment Agenda VM.
- `ios.yml`: add `scripts/swift-observation-gate.sh` step (D-15).
- `project.pbxproj`: deployment targets (D-13), Combine product removal (D-14).

</code_context>

<specifics>
## Specific Ideas

- Follow the Android analogues already in the repo: `PerformanceMonitoring` interface seam (04-02) for D-06, paired screenshots + human approval (07-19) for D-16.
- Gate script in the same style as `di-gate.sh` / `kmp-nc-version-gate.sh` (runnable locally, early in the iOS job).

</specifics>

<deferred>
## Deferred Ideas

- Replacing the remaining iOS-17-deprecated SwiftUI APIs (`navigationBarTitle`, `navigationBarItems`, `accentColor`, `foregroundColor`, one-parameter `onChange`, ...) — Phase 12 (warning-free build), logged as debt.
- Swift value types instead of Kotlin models in ports — Phase 11/12 with the bridge package.
- Continuous observation of sessions/speakers/partners (instead of first emission) — Phase 9 for the agenda (IOSDATA-03); others stay as v1.0.
- "Load once per VM" (no reload on tab reappear) — not adopted, parity kept.
- Error UI on store failure — pinned v1.0 debt.

</deferred>

---

*Phase: 08-modern-swiftui-ios-17-and-observation*
*Context gathered: 2026-11-03*
