# Phase 8: Modern SwiftUI, iOS 17 and Observation - Research

**Researched:** 2026-11-03
**Domain:** SwiftUI Observation (`@Observable`), Swift concurrency lifecycle, KMP-NativeCoroutines async bridge, Swift Testing, Xcode project surgery (iOS app, objectVersion 54)
**Confidence:** HIGH for the repo facts and the locally verified behaviors; MEDIUM for CI-toolchain parity (Xcode 26.6 / Swift 6.3.3 cannot be run locally)

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

**Agenda ViewModel instances**
- **D-01:** **One shared `AgendaViewModel` instance app-wide.** The per-view `AgendaViewModel()` instances in `AgendaCellView` and `AgendaDetailView` (and the `@ObservedObject var viewModel = AgendaViewModel()` default in `AgendaView`) are removed.
- **D-02:** **`ContentView` owns it via `@State` and injects it with `.environment(agendaViewModel)`.** `AgendaView`, `AgendaDetailView` (reached from the Agenda tab *and* from Speakers → speaker detail → session) and the filter menus / `AgendaNavigationBarItems` / `ClearFiltersButton` read `@Environment(AgendaViewModel.self)` (with `@Bindable` where a binding is needed). `AgendaCellView` (only used in `SpeakerDetailsView` with `isBookmarked: false`) drops its unused VM.
- **D-03:** If the single instance makes a bookmark or filter change refresh the agenda list immediately where v1.0 did not (e.g. un-bookmarking from a detail while the Favorites filter is on), this is **accepted and recorded as a documented deviation**, not fixed back to v1.0 behavior. Phase 9 criterion 4 requires the single instance anyway.

**Ports and fakes**
- **D-04:** **Narrow port per VM, exposing Kotlin models.** Streams as `AsyncThrowingStream<[Model], Error>`, one-shot calls as `async throws` (e.g. `protocol SpeakersSource { func speakers() -> AsyncThrowingStream<[Speaker], Error> }`, `func venue(language:) async throws -> Venue`). A `Live…` adapter wraps `asyncSequence(for:)` / `asyncFunction(for:)` on `DevFestNantesStore`. **Only live adapters call the KMP-NativeCoroutines bridge**; ViewModels never do. Swift value types are not introduced here.
- **D-05:** **Live wiring through an init default argument** until Phase 10: `init(source: SpeakersSource = LiveSpeakersSource(), ...)`, the live adapter reading `IosDependencies.shared.store()`. Tests pass fakes. Phase 10 removes the defaults.
- **D-06:** **Performance tracing seam:** a small protocol (e.g. `DataLoadTracing` with `trackDataLoad<T>(traceName:dataSource:block:)`) that `PerformanceMonitoring` conforms to; VMs take `tracing: DataLoadTracing = PerformanceMonitoring.shared`. Tests use a recording fake and may assert trace names. Mirrors the Android `PerformanceMonitoring` seam (Phase 4, 04-02). Trace names and `dataSource` values stay identical.
- **D-07:** **Locations:** each port protocol + its live adapter in a `<Feature>Source.swift` file next to its VM (ready to move into feature packages in Phase 11). Fakes in `iosApp/DevFestNantesTests/Fakes/`. Fakes return Kotlin values built from the recorded snapshot (`TestData` in `CharacterizationSupport.swift`) or small hand-built values. No mocking library.
- **D-08:** **`AgendaViewModel` persistence stays in the VM on the legacy keys**, made testable by injecting `defaults: UserDefaults = .standard`. Tests use a throwaway `UserDefaults(suiteName:)`. Keys (`Favorites`, `AgendaSessionFilters`) and formats stay byte-identical (`[String]` and Swift `JSONEncoder` data of `Set<SessionFilter>`). `favorites` becomes stored observable state (written through to `UserDefaults`) so Observation tracks it. No Swift persistence port, because Phase 9 replaces this with the shared Kotlin stores.

**Collection lifecycle**
- **D-09:** **Keep v1.0 "first emission only" semantics** for Agenda sessions, Speakers, About (partners) and SpeakerDetails. **Rooms stays continuous**, now collected inside the view's `.task` (no unstructured `Task {}`) and cancelled when the view disappears. Continuous agenda observation is Phase 9 (IOSDATA-03).
- **D-10:** **Reload on every appear, as in v1.0.** `.task` re-runs the load each time the view appears. About still resets `isLoading = true` and its content as today; the others keep their content until the new value arrives. Only VM recreation goes away.
- **D-11:** **Cancellation is silent.** `CancellationError` (view disappears mid-load) is not logged as an error and does not change `isLoading`/content; the next `.task` run starts over. Other errors keep today's logging and `isLoading = false` (no error UI, pinned v1.0 debt).

**Modernization scope, slicing and proof**
- **D-12:** **Only what Observation touches.** Change only the lines the migration needs (`@State`/`@Environment`/`@Bindable`, `.task`, `onChange` sites observing VM state, removal of `DispatchQueue.main.async`, including the one in `AgendaView` scroll handling). Other APIs deprecated or soft-deprecated at iOS 17 (`navigationBarTitle`, `navigationBarItems`, `accentColor`, `foregroundColor`, the one-parameter `onChange` where not otherwise touched...) stay and are logged as debt for Phase 12.
- **D-13:** **iOS 17.0 on every target configuration** (today two configs at 15.0, four at 17.0) as the first plan.
- **D-14:** **Plan slicing:** (1) iOS 17 floor; (2) About; (3) Venue; (4) Speakers + SpeakerDetails; (5) Agenda (shared instance, environment, filter menus, rooms in `.task`); each VM plan carries its port, live adapter, fake and tests; (6) Combine removal: `iOSApp.swift` imports, `BaseViewModel` and `PerformanceMonitoring` off `ObservableObject`, unused `@StateObject BaseViewModel()` in `ContentView` removed, `KMPNativeCoroutinesCombine` product removed from `project.pbxproj`, gate wired; then the parity checkpoint. Every step green and shippable.
- **D-15:** **Permanent CI grep gate** `scripts/swift-observation-gate.sh` (same style as `di-gate.sh` / `kmp-nc-version-gate.sh`): fails on `import Combine`, `KMPNativeCoroutinesCombine`, `ObservableObject`, `@Published`, `objectWillChange` and `DispatchQueue.main.async` in iOS Swift sources (and the Combine product in `project.pbxproj`). Runs in `ios.yml` and locally.
- **D-16:** **Parity proof = paired simulator screenshots + human checkpoint** (07-19 pattern): the agent captures `main` vs branch for agenda (both days, each filter type, favorite toggle from list and from detail, favorites after relaunch), session detail, speakers + A–Z index, speaker detail, venue and about; a human approves the diff. Swift tests stay green. Any difference from D-03 is listed as a documented deviation.

### Claude's Discretion
- Exact port/adapter/fake names and method signatures; whether `Live…` adapters are structs or classes; how `AsyncThrowingStream` is built from `asyncSequence`.
- How `SpeakerDetailsViewModel` (takes `speakerId`) and `VenueViewModel` (reads the language) are created under `@State` (e.g. `State(initialValue:)` in the view's `init`), as long as init stays side-effect free.
- What `BaseViewModel` becomes in Phase 8 (plain class holding the store, or dropped in favor of the ports) as long as it is no longer `ObservableObject`; Phase 10 deletes it.
- Use of `@ObservationIgnored` for non-UI stored properties (formatters, defaults, ports).
- Test case list per VM (at minimum: loading → content, first-emission-only, error → `isLoading = false`, cancellation silent; Agenda: favorites toggle persisted, filters persisted/cleared, rooms stream).
- Exact gate script output format and whether it also scans `DevFestNantesTests`.
- Documentation updates required by the AGENTS.md rule (`documentation/ARCHITECTURE.md` line on "Combine / async sequences", `documentation/DEVELOPMENT.md`, `scripts/README.md`, `AGENTS.md` gate list) in the same commits.

### Deferred Ideas (OUT OF SCOPE)
- Replacing the remaining iOS-17-deprecated SwiftUI APIs (`navigationBarTitle`, `navigationBarItems`, `accentColor`, `foregroundColor`, one-parameter `onChange`, ...) — Phase 12 (warning-free build), logged as debt.
- Swift value types instead of Kotlin models in ports — Phase 11/12 with the bridge package.
- Continuous observation of sessions/speakers/partners (instead of first emission) — Phase 9 for the agenda (IOSDATA-03); others stay as v1.0.
- "Load once per VM" (no reload on tab reappear) — not adopted, parity kept.
- Error UI on store failure — pinned v1.0 debt.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| IOSUI-01 | All iOS targets and packages have an iOS 17.0 deployment target | Exactly two `IPHONEOS_DEPLOYMENT_TARGET = 15.0;` lines in `project.pbxproj` (project-level Debug/Release, lines 688 and 744); the four target-level configs are already 17.0 (see "Xcode project facts") |
| IOSUI-02 | All iOS ViewModels `@Observable @MainActor`, owned via `@State`, collection in `.task`, cancelled on disappear, no unstructured `Task {}` | Patterns 1-4, Pitfalls 1-6; locally verified Observation, `didSet`, stream bridge and cancellation behavior |
| IOSUI-03 | No Combine, `KMPNativeCoroutinesCombine`, `ObservableObject`/`@Published`, `objectWillChange.send()`, `DispatchQueue.main.async` | Full inventory of offending lines (below), pbxproj removal sites, gate script design |
| IOSTEST-01 | Each iOS ViewModel has Swift Testing tests on fakes | Test architecture, fake design, verified hosted-test facts (Logger safe, no Koin needed with lazy live adapters) |
</phase_requirements>

## Summary

The migration is mechanical once four non-obvious facts are respected. (1) The KMP-NativeCoroutines `asyncSequence` iterator **ends the sequence silently (returns nil, does not throw) when the consuming task is cancelled**, so the v1.0 "take the first element, return `[]` if none" shape would turn a view-disappears-mid-load into an empty list plus `isLoading = false`; D-11 therefore needs an explicit `Task.isCancelled` / `Task.checkCancellation()` check after the loop (Pitfall 1). `asyncFunction`, by contrast, throws `CancellationError`. (2) In v1.0 `AgendaView`'s `.task` awaits `observeRooms()` first, which only returned because it spawned an unstructured `Task`; once rooms is a real continuous collection inside `.task`, the sessions load must run in a **separate `.task`** or it never starts (Pitfall 2). (3) `@State private var vm = VM()` evaluates the initializer on every view-struct init on the CI toolchain (Xcode 26.6); only Xcode 27 makes it lazy, so VM init and live-adapter init must be cheap and side-effect free, and the live adapters must read `IosDependencies.shared.store()` lazily (Pitfall 3). (4) The project is `objectVersion = 54` with explicit file references: every new Swift file (ports, fakes, tests, gate-adjacent) needs hand-added `PBXFileReference` + `PBXBuildFile` + group + Sources-phase entries.

Locally verified this session (Xcode 27.1, Swift 6.4, hosted tests on the iPhone 17 simulator, project in Swift 5 language mode): `@Observable @MainActor` with `didSet` write-through to `UserDefaults` works and `withObservationTracking` fires; `DevFestLogger.log(.error, ...)` with an error does not crash in the hosted test process (Firebase unconfigured); non-Sendable Kotlin `Speaker` values flow through a generic `AsyncSequence -> AsyncThrowingStream` helper with no diagnostics; cancelling the consumer ends the stream normally and fires upstream `onTermination`. The CI-toolchain research flag is answered as far as is possible without Xcode 26.6: the *existing* v1.0 `asyncSequence`/`asyncFunction` call sites build and their 19 tests pass on CI under Xcode 26.6 (run 41229227528 on main `f136ef88`), the project stays in Swift 5 language mode with no strict-concurrency or warnings-as-errors setting, and the new shapes only add Observation (iOS 17 SDK, any Xcode 26.x) and a Sendable-element stream wrapper that compiled warning-free locally. The residual risk (a diagnostic that exists only in Swift 6.3.3) is retired by the first CI run of plan 08-02/03, which therefore should be the first push of the phase.

**Primary recommendation:** Build one generic `bridge(_ make: @escaping () -> some AsyncSequence)` helper inside the live-adapter file(s) that returns an `AsyncThrowingStream` and cancels upstream in `onTermination`; make every ViewModel load method treat "loop ended while `Task.isCancelled`" and `CancellationError` as a silent no-op; use two separate `.task` modifiers in `AgendaView`; and hand-edit `project.pbxproj` file entries with a verification step (`xcodebuild -list` plus a build) after every plan.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Deployment target floor (iOS 17.0) | iOS app project config (`project.pbxproj`) | — | Build setting; packages arrive in Phase 11 |
| Observable UI state (loading, content, filters, favorites) | iOS ViewModel (`@Observable @MainActor`) | SwiftUI view (`@State`/`@Environment`/`@Bindable`) | State ownership and tracking live in the VM; views only read it |
| Lifecycle of collection (start/stop) | SwiftUI view (`.task`) | ViewModel (`async` load methods) | View lifecycle is the only correct cancellation scope; VMs expose `async` functions |
| Kotlin Flow/suspend to Swift bridge | Live adapter (`Live…Source`) | KMP-NativeCoroutines `KMPNativeCoroutinesAsync` | D-04: VMs never call `asyncSequence`/`asyncFunction` |
| Data access seam for tests | Port protocol per VM | Fakes in `DevFestNantesTests/Fakes/` | Narrow ports replace the single Koin store in tests |
| Performance traces | `DataLoadTracing` protocol (conformer `PerformanceMonitoring`) | Recording fake in tests | D-06; keeps Firebase Performance out of unit tests |
| Favorites / filters persistence | `AgendaViewModel` over injected `UserDefaults` | — | D-08; replaced by shared Kotlin stores in Phase 9 |
| Shared Agenda instance | `ContentView` (`@State` owner) | `.environment(...)` consumers | D-01/D-02 |
| Regression guard for banned APIs | `scripts/swift-observation-gate.sh` in CI | — | D-15 |

## Standard Stack

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Observation framework (`@Observable`, `@ObservationIgnored`, `withObservationTracking`) | System, iOS 17+ SDK, ships with Xcode 26.6 and 27.1 | ViewModel change tracking | Required by IOSUI-02. Locally verified compile and tracking `[VERIFIED: local spike, Xcode 27.1]` |
| SwiftUI `@State` / `@Bindable` / `@Environment(Type.self)` / `.environment(_:)` / `.task` | iOS 17+ | Ownership, injection, lifecycle | Replaces `@StateObject`/`@ObservedObject`/`@EnvironmentObject` `[CITED: developer.apple.com migration guide, content paraphrased via fetch; see A1]` |
| KMP-NativeCoroutines Swift package (`KMPNativeCoroutinesAsync`) | exact `1.0.6` | `asyncSequence(for:)` / `asyncFunction(for:)` | Already pinned: `requirement = { kind = exactVersion; version = "1.0.6"; };` in `project.pbxproj` `[VERIFIED: project.pbxproj:906-913]`; Gradle `kmpNativeCoroutines = "1.0.6"` `[VERIFIED: gradle/libs.versions.toml:23]` |
| Swift Testing (toolchain copy) | Xcode 26.6 / 27.1 bundled | ViewModel tests | Already used by 4 suites; do **not** add the swift-testing package `[CITED: .planning/research/STACK.md line 43]` |

### Supporting
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `KMPNativeCoroutinesCore` | 1.0.6 (transitive of Async; `iOSApp.swift` also imports it directly) | `NativeFlow` types | Stays; only the **Combine** product leaves |
| Foundation `UserDefaults(suiteName:)` | system | Throwaway defaults in Agenda tests | Per-test unique suite name (UUID), `removePersistentDomain` in teardown |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Generic `AsyncSequence -> AsyncThrowingStream` helper | Hand-wrap each flow | Duplicated cancellation plumbing; one helper is the single place to get `onTermination` right |
| Two `.task` modifiers in `AgendaView` | `async let` / `withTaskGroup` inside one `.task` | Both are structured; two modifiers is the smallest diff and each is cancelled independently |
| Dropping `BaseViewModel` | Keep as plain class holding the store | After all five VMs take ports nothing needs it; deleting is smaller than keeping dead code (CONTEXT says Phase 10 deletes it, but discretion allows dropping earlier) |

**Installation:** none. No new package is added; one is removed (`KMPNativeCoroutinesCombine`).

**Version verification:** KMP-NativeCoroutines `v1.0.6` confirmed from the local SwiftPM checkout (`git describe --tags` printed `v1.0.6`; `Package.swift` declares `.library(name: "KMPNativeCoroutinesCombine"...)` and `KMPNativeCoroutinesAsync` products, `swift-tools-version:5.3`, iOS 13 floor) `[VERIFIED: build/SourcePackages/checkouts/KMP-NativeCoroutines]`.

## Package Legitimacy Audit

No external package is installed by this phase, so the registry gate does not apply. `Observation`, `SwiftUI`, `Testing` are toolchain/SDK frameworks. The only package change is **removal** of the already-pinned `KMPNativeCoroutinesCombine` product (same repository and version as the retained `KMPNativeCoroutinesAsync`).

| Package | Registry | Age | Downloads | Source Repo | Verdict | Disposition |
|---------|----------|-----|-----------|-------------|---------|-------------|
| KMPNativeCoroutinesAsync (retained) | SwiftPM (github.com/rickclephas/KMP-NativeCoroutines) | pinned since v1.0 | n/a | https://github.com/rickclephas/KMP-NativeCoroutines.git | OK (existing, exact pin, version-gated by `scripts/kmp-nc-version-gate.sh`) | Approved, unchanged |
| KMPNativeCoroutinesCombine | same | — | — | same | n/a | **Removed** from the app target |

**Packages removed due to [SLOP] verdict:** none
**Packages flagged as suspicious [SUS]:** none

## Architecture Patterns

### System Architecture Diagram

```
App launch
  iOSApp.init  --(not under XCTest)-->  FirebaseApp.configure, IosDependencies.shared.start (Koin)
       |
       v
  ContentView  (@State agendaViewModel = AgendaViewModel())  --.environment(agendaViewModel)-->
       |
  TabView ──┬─ AgendaView ─ NavigationStack ─ AgendaDetailView (env AgendaViewModel)
            ├─ SpeakersView (@State SpeakersViewModel) ─ SpeakerDetails (@State SpeakerDetailsViewModel)
            │                                              └─ AgendaDetailView (env AgendaViewModel)
            ├─ VenueView (@State VenueViewModel)
            └─ AboutView (@State AboutViewModel)

View appears ──> .task { await vm.load…() }            (SwiftUI cancels the task on disappear)
                      |
                      v
              ViewModel (@Observable @MainActor)
                 - tracing.trackDataLoad(traceName:dataSource:operation:) { … }
                 - for try await value in source.<stream>()  /  try await source.<call>()
                 - CancellationError or "loop ended while Task.isCancelled"  -> return silently (D-11)
                 - other error -> DevFestLogger .error, isLoading = false
                      |
                      v
              Port protocol (SpeakersSource, …)  <── fake in tests (AsyncThrowingStream / async throws)
                      |
                      v
              Live…Source  (only place that calls asyncSequence(for:) / asyncFunction(for:))
                      |  lazily: IosDependencies.shared.store()
                      v
              DevFestNantesStore (Kotlin, KMP-NativeCoroutines closures)
```

### Recommended Project Structure
```
iosApp/iosApp/
├── Agenda/AgendaViewModel.swift, AgendaSource.swift (port + Live), AgendaView.swift, …
├── UI/About/AboutViewModel.swift, AboutSource.swift
├── UI/Venue/VenueViewModel.swift, VenueSource.swift
├── UI/Speakers/SpeakersViewModel.swift, SpeakersSource.swift, SpeakerDetailsViewModel.swift, SpeakerDetailsSource.swift
├── Performance/PerformanceMonitoring.swift      # + DataLoadTracing conformance (protocol may live in its own file)
iosApp/DevFestNantesTests/
├── Fakes/ (FakeSpeakersSource.swift, …, RecordingDataLoadTracing.swift)
├── <Feature>ViewModelTests.swift
scripts/swift-observation-gate.sh
```
Every new `.swift` file needs the four pbxproj edits (Pitfall 7).

### Pattern 1: Generic bridge helper (live adapters only)
**What:** wrap any `AsyncSequence` into an `AsyncThrowingStream`, cancelling the upstream when the consumer stops (break, cancellation, deinit).
**When to use:** every streaming port method (`sessions`, `rooms`, `speakers`, `partners`).
**Example:**
```swift
// Verified shape: compiled warning-free with Kotlin [Speaker] elements in Swift 5 mode
// and cancelled upstream correctly (local spike, Xcode 27.1). Live adapters pass
// `asyncSequence(for: IosDependencies.shared.store().speakers)` from inside the closure.
enum Bridge {
    static func stream<S: AsyncSequence>(_ make: @escaping () -> S) -> AsyncThrowingStream<S.Element, Error> {
        AsyncThrowingStream { continuation in
            let task = Task {
                do {
                    for try await value in make() { continuation.yield(value) }
                    continuation.finish()
                } catch { continuation.finish(throwing: error) }
            }
            continuation.onTermination = { _ in task.cancel() }
        }
    }
}
// struct LiveSpeakersSource: SpeakersSource {
//     func speakers() -> AsyncThrowingStream<[Speaker], Error> {
//         Bridge.stream { asyncSequence(for: IosDependencies.shared.store().speakers) }
//     }
// }
```
The `Task {}` here is inside the adapter, bound to the stream lifetime by `onTermination`; it is not an unstructured fire-and-forget task. If the project prefers not to have `Task {` anywhere, that is the only place it appears; the observation gate does not forbid it.

### Pattern 2: ViewModel with first-emission load and silent cancellation
**What:** `@Observable @MainActor final class`, ports and defaults marked `@ObservationIgnored`, load methods `async` and cancellation-aware.
**Example:**
```swift
@Observable @MainActor
final class SpeakersViewModel {
    var speakersContent: [Speaker]?
    var isLoading = true
    @ObservationIgnored private let source: SpeakersSource
    @ObservationIgnored private let tracing: DataLoadTracing
    @ObservationIgnored private let logger = DevFestLogger(category: "SpeakersViewModel")

    init(source: SpeakersSource = LiveSpeakersSource(),
         tracing: DataLoadTracing = PerformanceMonitoring.shared) {
        self.source = source; self.tracing = tracing        // no I/O, no Koin access
    }

    func observeSpeakers() async {
        do {
            let speakers = try await tracing.trackDataLoad(
                traceName: PerformanceMonitoring.TRACE_SPEAKERS_LOAD, dataSource: "graphql"
            ) {
                var first: [Speaker]?
                for try await list in self.source.speakers() { first = list; break }
                try Task.checkCancellation()          // asyncSequence ends silently on cancel (Pitfall 1)
                return first ?? []
            }
            speakersContent = speakers
            isLoading = false
        } catch is CancellationError {
            return                                     // D-11: no log, no state change
        } catch {
            logger.log(.error, "Observe Speakers error: \(error.localizedDescription)")
            isLoading = false
        }
    }
}
```
`TRACE_SPEAKERS_LOAD = "speakers_load"`, `"graphql"`, the `"SpeakersViewModel"` logger category and the message text are the v1.0 values `[VERIFIED: PerformanceMonitoring.swift:25-29, SpeakersViewModel.swift:20-52]`. Note `PerformanceMonitoring.trackDataLoad` rethrows the operation's error after marking the trace failed (so a cancelled load records a failed trace; this is v1.0-equivalent behavior and acceptable).

### Pattern 3: View ownership and lifecycle
```swift
struct SpeakersView: View {
    @State private var viewModel = SpeakersViewModel()          // init is cheap and effect-free
    var body: some View {
        LoadingView(isShowing: $viewModel.isLoading) { … }       // $viewModel.prop works on @State of a class
            .task { await viewModel.observeSpeakers() }           // cancelled on disappear
    }
}
struct SpeakerDetails: View {
    @State private var viewModel: SpeakerDetailsViewModel
    init(speakerId: String) { _viewModel = State(initialValue: SpeakerDetailsViewModel(speakerId: speakerId)) }
    var body: some View { … .task { await viewModel.load() } }   // moves the Task { } out of VM init
}
struct AgendaDetailView: View {
    @Environment(AgendaViewModel.self) private var viewModel
    // need a binding?  @Bindable var viewModel = viewModel   (inside body)
}
```
`AgendaView` (two tasks, see Pitfall 2):
```swift
.task { RCValues.sharedInstance.fetchCloudValues() }   // or keep inside one of the two
.task { await viewModel.observeRooms() }               // continuous; ends when the view disappears
.task { await viewModel.observeSessions() }            // first emission
```
`ContentView` owns it: `@State private var agendaViewModel = AgendaViewModel()` and `.environment(agendaViewModel)` on the `TabView` (outside every `NavigationStack`/`NavigationView`, so destinations inherit it).

### Pattern 4: Tracing seam matching the real signature
D-06 names the method `trackDataLoad<T>(traceName:dataSource:block:)`, but the existing method is
```swift
func trackDataLoad<T>(
    traceName: String,
    dataSource: String = "unknown",
    operation: @escaping () async throws -> T
) async throws -> T {
```
`[VERIFIED: PerformanceMonitoring.swift:71-75]`. The `block:` label belongs to the convenience `traceDataLoading(operation:dataSource:block:)` wrapper (`[VERIFIED: PerformanceMonitoring.swift:317-327]`, where `operation` is the trace *name*). Declare the protocol with the **existing** `trackDataLoad(traceName:dataSource:operation:)` signature so `PerformanceMonitoring` conforms with an empty `extension PerformanceMonitoring: DataLoadTracing {}` and the VM call sites keep their v1.0 shape. Protocol requirements cannot carry a default argument, so declare `dataSource` without a default in the protocol and keep the default on the class (every VM call site passes `dataSource: "graphql"` explicitly).

### Anti-Patterns to Avoid
- **`@Published`-style mirroring**: do not keep a `var objectWillChange` or call `objectWillChange.send()`; assigning to a stored `@Observable` property is enough.
- **Eager Koin access in an init**: `LiveXSource.init` or VM init calling `IosDependencies.shared.store()` forces Koin to exist at construction time and breaks tests/previews and the not-lazy `@State` on Xcode 26.6.
- **`DispatchQueue.main.async` replacement by `Task { @MainActor in }`**: in a `@MainActor` VM, assign directly after the `await`; in a view, assign in `onChange`/`.task` directly.
- **Logging cancellation** through `DevFestLogger` (it forwards errors to Crashlytics).
- **Passing the VM as an init parameter *and* reading the environment** in the same menu views: pick `@Environment` (D-02) and remove the `viewModel:` parameters at the call sites in `AgendaNavigationBarItems`.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Change tracking | `objectWillChange` plumbing, Combine subjects | `@Observable` | Framework tracks property reads per view |
| Cancellation scope | Stored `Task` handles + manual cancel in VM | SwiftUI `.task` | Cancelled automatically on disappear |
| Kotlin Flow to Swift | Custom callback wrappers over `NativeFlow` | `asyncSequence(for:)`/`asyncFunction(for:)` in the live adapter | Already handles Kotlin-side cancellation, back-pressure |
| Test doubles | A mocking library | Hand-written fakes (AGENTS.md: no mocking library) | Project rule |
| Kotlin test values | Re-declaring model builders | `TestData` (`speakers()`, `sessions()`) in `CharacterizationSupport.swift` | Already exists, reuse |
| Pbxproj editing | Rewriting the file | Minimal targeted edits + `xcodebuild -list`/build verification (no `xcodeproj` gem is installed here) | Corruption risk |

**Key insight:** the phase's risk is not the Observation API, it is lifecycle semantics (`.task` cancellation, silent bridge termination, non-lazy `@State`) and project-file bookkeeping.

## Runtime State Inventory

Rename/refactor-adjacent (Combine/ObservableObject removal and a deployment-target change); stored state is what matters.

| Category | Items Found | Action Required |
|----------|-------------|------------------|
| Stored data | `UserDefaults.standard` keys `"Favorites"` (`[String]`) and `"AgendaSessionFilters"` (JSON of `Set<SessionFilter>`) written by `AgendaViewModel` `[VERIFIED: AgendaViewModel.swift:35,44,56-58,68]`. `SessionFilter` is `Codable` with `FilterType: String` raw values `bookmark, complexity, language, room, type` `[VERIFIED: Model/SessionFilter.swift]` | **No migration.** Keys and encodings stay byte-identical (D-08). Add a test that decodes a v1.0-format blob |
| Live service config | None — verified by reading: no Firebase/Remote Config key changes (`openfeedback_enabled` untouched) | None |
| OS-registered state | None — no launch agents, background tasks, or registered URL handlers touched | None |
| Secrets/env vars | `GoogleService-Info.plist` (uncommitted, mocked in CI) unchanged | None |
| Build artifacts / installed packages | Stale local `build/` DerivedData. `Package.resolved` pins the package (identity `kmp-nativecoroutines`), not its products, so it does not change when the Combine product is removed `[VERIFIED: Package.resolved:95-97]` | After removing the product, do a clean resolve (`xcodebuild -resolvePackageDependencies`) once; no manual cache action |

## Common Pitfalls

### Pitfall 1: `asyncSequence` ends silently on cancellation
**What goes wrong:** The view disappears while the first emission is pending; the `for try await` loop finishes **normally** with no element, v1.0-shaped code returns `[]`, sets content to empty and `isLoading = false`. Violates D-11.
**Why it happens:** In `NativeFlowAsyncSequence.Iterator`, the Kotlin-cancelled callback calls `continuation.resume(returning: nil)` (ends iteration); only the early-cancel path (`cancellationError != nil` when `next()` is called) throws `CancellationError` `[VERIFIED: KMP-NativeCoroutines v1.0.6 KMPNativeCoroutinesAsync/AsyncSequence.swift]`. `asyncFunction` instead resumes with `throw CancellationError()` `[VERIFIED: …/AsyncFunction.swift]`. A generic stream wrapper behaves the same: consumer cancelled -> stream ends normally and upstream `onTermination` fires `[VERIFIED: local spike output "endedNormally=true upstreamTerminated=true"]`.
**How to avoid:** After the loop, `try Task.checkCancellation()` (or `guard !Task.isCancelled`), and catch `CancellationError` silently. Do **not** treat "no emission" as an empty result unless not cancelled.
**Warning signs:** After tab switch mid-load the screen shows an empty list instead of the spinner.

### Pitfall 2: Sequential awaits in one `.task` after rooms becomes continuous
**What goes wrong:** `await viewModel.observeRooms()` then `await viewModel.observeSessions()` in the same `.task` never reaches sessions (rooms never completes) -> agenda stuck on the loading overlay.
**Why it happens:** v1.0 `observeRooms()` returned immediately because its work ran in an unstructured, never-cancelled `Task { }` `[VERIFIED: AgendaViewModel.swift:138-151]`; `.task` at `AgendaView.swift` awaits it first `[VERIFIED: AgendaView.swift:51-55]`.
**How to avoid:** separate `.task` modifiers (or `async let`) for rooms and sessions.
**Warning signs:** Agenda spinner never ends; room filter works, list empty.

### Pitfall 3: `@State` initializer runs on every view-struct init on CI's Xcode 26.6
**What goes wrong:** Extra `ViewModel.init()` calls (the instance is discarded, state preserved). With side effects (Koin lookups, `UserDefaults` writes, network) this causes redundant work or crashes before Koin start.
**Why it happens:** `@State(wrappedValue:)` takes the value, not an autoclosure (unlike `@StateObject`); Xcode 27 makes the initial value lazy, Xcode 26.x does not `[CITED: jessesquires.com/blog/2024/09/09/swift-observable-macro, nilcoalescing.com/blog/InitializingObservableClassesWithTheStateMacroInXcode27, forums.swift.org/t/…/70811]`. The local toolchain is Xcode 27.1, CI is 26.6, so **local runs hide this**.
**How to avoid:** init only stores references and reads cheap values (`UserDefaults` reads in `AgendaViewModel.init` are fine, writes are not); live adapters resolve the store inside their methods; `SpeakerDetailsViewModel.init(speakerId:)` must stop launching `Task {}`.
**Warning signs:** Logs or traces appearing multiple times at navigation; crash "Koin not started" in previews/tests.

### Pitfall 4: `AgendaViewModel.init()` in v1.0 touches Koin
`BaseViewModel.init(store: = IosDependencies.shared.store())` `[VERIFIED: BaseViewModel.swift:15-17]` is why characterization tests call `TestHost.start()` before `AgendaViewModel()`/`SpeakersViewModel()`/`VenueViewModel()` `[VERIFIED: AgendaCharacterizationTests.swift assertGrouping, SpeakersVenueCharacterizationTests.swift]`. With lazy live adapters those tests keep working (calling `start()` is idempotent) and new fake-based tests need no Koin.

### Pitfall 5: Existing characterization tests constrain the VM surface
They use `viewModel.speakersContent = …` (settable), `viewModel.getAlphabets()`, `viewModel.speakers(for:)`, `viewModel.sectionDateFormatter.timeZone = …` (must stay a mutable, internal `var`, mark `@ObservationIgnored`), `viewModel.sessionsChanged(sessions:)`, `viewModel.agendaContent.sections`, `VenueViewModel().currentLanguage`, and the static `AgendaViewModel.parseDate` called from `@MainActor` suites. Keep these members and signatures. `AgendaViewModel` is currently not `@MainActor` (class declaration has no attribute `[VERIFIED: AgendaViewModel.swift:15]`); adding it is safe because every caller is a `@MainActor` suite. Mark `parseDate` `nonisolated static` only if a non-main caller appears.

### Pitfall 6: Initial-scroll hop removal
`AgendaView.handleInitialScrollIfNeeded` uses `DispatchQueue.main.async { scrollToSectionID = previous.date }` `[VERIFIED: AgendaView.swift:211-213]` (the one hop D-12 explicitly names). The hop defers the assignment so the `List` has laid out before `proxy.scrollTo` runs from `onChange(of: scrollToSectionID)`. Assigning directly inside an `onChange` handler already takes effect on the next update, so direct assignment is the expected replacement, but this only matters on an event day, which UAT never hits unless the clock is shifted. Add "agenda opens scrolled to the current slot with the clock set to 2025-10-16 midday" to the parity checkpoint, and prefetch data before shifting the clock (user memory: shifted-clock testing breaks SSL). Fallback if the scroll regresses: a `.task(id: scrollTrigger) { await Task.yield(); scrollToSectionID = … }`, never `DispatchQueue`.

### Pitfall 7: Hand-edited Xcode project
`objectVersion = 54` with `PBXFileReference`/`PBXBuildFile` entries (no `PBXFileSystemSynchronizedRootGroup`) `[VERIFIED: project.pbxproj:6, grep]`. Each new file needs: a `PBXFileReference`, a `PBXBuildFile`, a child entry in its `PBXGroup` (tests group `70708D92D3C406142E250997` lists test files today), and an entry in the right `Sources` phase (app `7555FF77242A565900829871`, tests `521F86961B5FB3C2DD590C59`). A new `Fakes` folder needs a new `PBXGroup`. A missing entry compiles the app but silently drops the file (tests then "pass" with fewer tests; the xcresult assertion reports totals, compare counts per plan). Verify after each plan with `xcodebuild -list` and a build, and compare `XCRESULT-TESTS total=` against the expected count.

### Pitfall 8: Removing the Combine product
The product appears in exactly three places: the `PBXBuildFile` at line 19, the app `Frameworks` phase entry at line 154, and the `XCSwiftPackageProductDependency` block at lines 937-941 `[VERIFIED: project.pbxproj grep]`. There is no `packageProductDependencies` list to edit. `iOSApp.swift` imports `Combine` and `KMPNativeCoroutinesCombine` (and `KMPNativeCoroutinesCore`) and uses none of them `[VERIFIED: iOSApp.swift:1-12]`; delete the first two imports. `ContentView.swift` imports `Combine` and holds an unused `@StateObject var viewModel = BaseViewModel()` `[VERIFIED: ContentView.swift:1-7]`.

### Pitfall 9: Environment object missing -> runtime trap
`@Environment(AgendaViewModel.self)` (non-optional) crashes when not injected. All real paths descend from `ContentView`; Xcode Previews or any future hosting must inject one. If any `#Preview` exists for these views, add `.environment(AgendaViewModel(source: fake))`. Verify the filter `Menu` (in `navigationBarItems`) receives the environment during the simulator parity pass; fallback is to pass the VM down with `@Bindable` parameters.

### Pitfall 10: Tests hit Firebase or the network
`PerformanceMonitoring.shared.trackDataLoad` calls `Performance.startTrace` and `DevFestLogger.log(.error/.warning)` calls `Crashlytics.crashlytics()`. In the hosted test process FirebaseApp is never configured (the guard returns before `FirebaseApp.configure()` `[VERIFIED: iOSApp.swift:27-31]`). Locally, logging an error with an error object **did not crash** `[VERIFIED: local spike, Xcode 27.1]`; it is `[ASSUMED]` to behave the same on 26.6 (A2). Tests must inject the recording `DataLoadTracing` fake so Firebase Performance is never touched; logger calls stay real unless the first CI run shows a crash, in which case introduce a `Logging` seam in one plan (not per VM).

## Code Examples

### Test: first-emission-only, cancellation silent (Swift Testing)
```swift
@MainActor @Suite struct SpeakersViewModelTests {
    @Test func loads_the_first_emission_only() async throws {
        let first = try TestData.speakers()
        let source = FakeSpeakersSource(emissions: [first, []])   // second emission must be ignored
        let vm = SpeakersViewModel(source: source, tracing: RecordingDataLoadTracing())
        await vm.observeSpeakers()
        #expect(vm.speakersContent?.count == first.count)
        #expect(vm.isLoading == false)
        #expect(source.terminated)                                  // break cancelled the upstream
    }

    @Test func cancellation_does_not_change_state() async {
        let source = FakeSpeakersSource(neverEmits: true)
        let vm = SpeakersViewModel(source: source, tracing: RecordingDataLoadTracing())
        let load = Task { @MainActor in await vm.observeSpeakers() }
        await source.waitUntilSubscribed()                          // AsyncStream-based signal, no sleep
        load.cancel(); await load.value
        #expect(vm.isLoading == true)
        #expect(vm.speakersContent == nil)
    }
}
```
(Design notes: fakes are `final class … : @unchecked Sendable` only inside the test target; use an `AsyncStream` handshake instead of `Task.sleep`.)

### Test: Agenda persistence with throwaway defaults
```swift
let name = "AgendaViewModelTests-\(UUID().uuidString)"
let defaults = UserDefaults(suiteName: name)!
defer { defaults.removePersistentDomain(forName: name) }
let vm = AgendaViewModel(source: FakeAgendaSource(), tracing: RecordingDataLoadTracing(), defaults: defaults)
vm.toggleFavorite(ofSession: session)
#expect(defaults.object(forKey: "Favorites") as? [String] == [session.id])
```
`"Favorites"` and `"AgendaSessionFilters"` are the v1.0 keys `[VERIFIED: AgendaViewModel.swift]`. `didSet` write-through on an `@Observable` stored property fires and `init` does not trigger it `[VERIFIED: local spike (SpikeTests2), Xcode 27.1]`; so `favorites`/`sessionFilters` can keep `didSet { … }`.

### Gate script outline (`scripts/swift-observation-gate.sh`)
Follow `di-gate.sh`: `set -euo pipefail`, `export LC_ALL=C`, modes `check` (default), `selftest`; scan set = `git ls-files -- 'iosApp/**/*.swift'` minus `iosApp/iosApp/SwiftGen/` (generated) and, per discretion, include `DevFestNantesTests`; separate rule on `iosApp/iosApp.xcodeproj/project.pbxproj` for `KMPNativeCoroutinesCombine`; token lines `SWIFT-OBS-FOUND <path>:<line> <pattern>` and `SWIFT-OBS-GATE-OK`; `selftest` plants one violation per pattern in a temp tree and proves each is caught and a clean file is spared. Patterns: `import Combine`, `KMPNativeCoroutinesCombine`, `ObservableObject`, `@Published`, `objectWillChange`, `DispatchQueue\.main\.async`. Beware self-matches: the script and docs are not scanned (`.swift` only), but comments in Swift files that mention the banned words would trip the gate; keep comments free of them. Wire as an early step in `.github/workflows/ios.yml` next to the KMP-NC gate (text-only, before Gradle), and document it in `scripts/README.md` and the AGENTS.md "run what CI runs" list.

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `ObservableObject` + `@Published` + `@StateObject`/`@ObservedObject` | `@Observable` + `@State`/`@Bindable`/`@Environment(Type.self)` | iOS 17 / Swift 5.9 | Per-property tracking; requires the iOS 17 floor |
| Combine `sink` over flows | `AsyncSequence` + `.task` | iOS 15+ | Cancellation tied to view |
| `@State` eager initial value | Xcode 27: lazy initial value (macro) | Xcode 27 (2026-09) | CI on 26.6 still eager (Pitfall 3) |

**Deprecated/outdated (left in place by D-12, logged for Phase 12):** `navigationBarTitle`, `navigationBarItems`, `accentColor`, `foregroundColor`, one-parameter `onChange`, `NavigationView`, `Locale.current.languageCode` (a deprecation warning in `VenueViewModel`-adjacent test code was observed during the spikes: `'languageCode' was deprecated in iOS 16`).

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | `$viewModel.prop` bindings and `@Bindable var vm = vm` for environment models work as in Apple's migration guide (the Apple page is JS-rendered; WebFetch returned only general knowledge) | Pattern 3 | Compile errors caught immediately on build; low |
| A2 | `DevFestLogger.log(.error)` does not crash in hosted tests on Xcode 26.6 either (verified only on 27.1) | Pitfall 10 | First CI run crashes the test host; mitigation: `Logging` seam |
| A3 | SwiftUI delivers `onDisappear` (so cancels `.task`) for a root view covered by a `NavigationStack` push, and for an inactive `TabView` tab; consistent with v1.0 `.task` semantics but not re-verified in a UI run | Patterns 3, D-10 | Rooms stays collecting while a detail is shown (harmless) or reloads agenda on return (parity with v1.0); confirm in the parity pass |
| A4 | Direct assignment of `scrollToSectionID` inside `onChange(of: isLoading)` scrolls correctly without the dispatch hop | Pitfall 6 | Initial-scroll regression visible only on event day; fallback documented |
| A5 | `-parallel-testing-enabled NO` in CI does not guarantee serial execution of Swift Testing tests; assume in-process parallelism and keep suites isolated (unique defaults suite, no shared mutable fakes) | Validation | Flaky shared state; avoided by design |
| A6 | `Environment` propagates into `Menu` content used from `navigationBarItems(trailing:)` | Pitfall 9 | Menus crash/trap on open; fallback to passing the VM down |
| A7 | Swift 6.3.3 (CI) emits no new error for the verified bridge shapes in Swift 5 language mode; the CI run of the existing v1.0 bridge call sites passed on 26.6 but the *new* wrapper was only compiled on 6.4 | Summary | First CI run fails to compile; fix is local to the adapter helper |

## Open Questions (RESOLVED)

1. **Does the Swift 6.3.3 compiler accept the new adapter/stream shapes?**
   - Known: v1.0 call sites passed CI on Xcode 26.6; project is Swift 5 mode with no strict concurrency; shapes compile clean on 6.4.
   - Unclear: nothing runnable locally for 26.6.
   - Recommendation: make plan 08-01 (iOS 17 floor, no Swift changes) plus the first VM plan the earliest CI-visible changes; the human pushes and runs `gh workflow run ios.yml` before the Speakers/Agenda plans (Phase 6 recorded that push is not authorized for agents).
   - **RESOLVED (plan 08-04):** 08-04 is a blocking CI replay between the Venue plan (08-03) and the Speakers plan (08-05): the local CI-equivalent lane runs on the 08-03 head, the human pushes and dispatches `ios.yml`, and Claude verifies with `gh` that the run on the same SHA (Xcode 26.6 / Swift 6.3.3) is green with the About and Venue tests (`KotlinStreamBridge` over `asyncSequence`, `asyncFunction`, Observation, hosted `DevFestLogger`). 08-05 does not start before a green run; a Swift 6.3.3 diagnostic is fixed locally in the bridge or the adapter (A7).

2. **Delete `BaseViewModel` now or keep a plain class?** Recommendation: delete in the plan that migrates the last VM (it has no users then); update `documentation/ARCHITECTURE.md` line "`Model/BaseViewModel.swift` gets the store…" accordingly.
   - **RESOLVED (plans 08-02, 08-07, 08-08):** every migrated ViewModel drops the superclass (Agenda, the last one, in 08-07 Task 1); `BaseViewModel.swift` and its four project entries are deleted in 08-08 Task 1, the first plan in which it has no user (only the unused `ContentView` state object, removed in the same task). `documentation/ARCHITECTURE.md` gets a transitional bullet in 08-02 Task 3 and the final iOS section without `BaseViewModel` in 08-08 Task 3.

3. **Local iOS 17 runtime?** Installed simulator runtimes are iOS 18.0, 18.3, 18.6, 26.1, 27.0 (no 17.x). Success criterion 1 ("builds and runs on simulator") is satisfied by the 18.0 floor-adjacent runtime; running on a real 17.x runtime is optional (`xcodebuild -downloadPlatform iOS -buildVersion 17.5`).
   - **RESOLVED (plan 08-01):** 08-01 proves the floor with resolved build settings (17.0 for every target and configuration) and runs the Swift suite on the newest installed plain iPhone runtime, as CI does; running on a 17.x runtime stays optional and is recorded as a flagged assumption in 08-01 (FUT-04).

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Xcode | Build/test | ✓ | 27.1 (27A9275); CI 26.6 | — |
| Swift | Compile | ✓ | 6.4 local; CI 6.3.3 | — |
| iOS simulator (iPhone 17) | Tests/UAT | ✓ | iOS 18.0/18.3/18.6/26.1/27.0 | No iOS 17.x runtime (optional download) |
| SwiftGen | Build phase | ✓ | `/opt/homebrew/bin/swiftgen` | — |
| `gh` CLI | CI runs | ✓ | authenticated (run list works) | — |
| Built shared framework | App build | ✓ | `shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework` and `build/` DerivedData present | `./gradlew :shared:embedAndSignAppleFrameworkForXcode` |
| `xcodeproj` Ruby gem / xcodegen / tuist | pbxproj edits | ✗ | — | Hand-edit + verification (Pitfall 7) |
| Xcode 26.6 / Swift 6.3.3 | Research-flag replay | ✗ (CI only) | — | CI run is the replay |

**Missing dependencies with no fallback:** none.
**Missing dependencies with fallback:** project-file tooling (hand edit); iOS 17 runtime (optional).

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | Swift Testing (toolchain), hosted by the app in target `DevFestNantesTests`, run through test plan `iosApp` |
| Config file | `iosApp/iosApp.xctestplan` (single target `DevFestNantesTests`) |
| Quick run command | `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -testPlan iosApp -destination 'platform=iOS Simulator,name=iPhone 17' -derivedDataPath build -only-testing:DevFestNantesTests/<Suite> test CODE_SIGNING_ALLOWED=NO CODE_SIGNING_REQUIRED=NO CODE_SIGN_IDENTITY="" PROVISIONING_PROFILE=""` (used for the spikes; full build reuses the existing `build/`) |
| Full suite command | The CI sequence: `build-for-testing` then `test-without-building` with `-parallel-testing-enabled NO`, then `bash scripts/xcresult-assert-tests.sh <bundle>` |

Baseline: CI run on main reports `XCRESULT-TESTS total=19 passed=19 failed=0 skipped=0` `[VERIFIED: gh run 41229227528 log]`.

### Phase Requirements -> Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| IOSUI-01 | Every `IPHONEOS_DEPLOYMENT_TARGET` is 17.0 | script/grep | `grep -c "IPHONEOS_DEPLOYMENT_TARGET = 17.0;" iosApp/iosApp.xcodeproj/project.pbxproj` (expect 6) and no `15.0` | ❌ Wave 0 (add to plan 01 verification; optional gate rule) |
| IOSUI-02 | VM is `@Observable @MainActor`; load-on-appear; first emission only; silent cancel; rooms continuous | unit (Swift Testing) | `-only-testing:DevFestNantesTests/<Feature>ViewModelTests` | ❌ Wave 0 (5 suites + fakes) |
| IOSUI-02 | VM survives parent re-render (not recreated) | manual / parity checkpoint | simulator screenshots (D-16) | n/a manual (justified: needs SwiftUI runtime) |
| IOSUI-03 | No banned API in iOS sources and pbxproj | gate | `bash scripts/swift-observation-gate.sh && bash scripts/swift-observation-gate.sh selftest` | ❌ Wave 0 |
| IOSTEST-01 | Each VM has tests on fakes in CI lane | unit + CI assertion | full CI sequence; `xcresult-assert-tests.sh` total increases | ❌ Wave 0 |
| (regression) | Existing characterization suites still compile and pass | unit | existing 19 tests | ✅ |

### Sampling Rate
- **Per task commit:** quick `-only-testing` run for the touched suite + `bash scripts/swift-observation-gate.sh` once it exists.
- **Per wave merge:** full local build-for-testing + test-without-building; `bash scripts/swift-names-gate.sh check` is **not** needed (no Kotlin export changes), but run `bash scripts/kmp-nc-version-gate.sh` since the pbxproj is edited.
- **Phase gate:** full CI run green + D-16 paired-screenshot checkpoint approved before `/gsd-verify-work`.

### Wave 0 Gaps
- [ ] `iosApp/DevFestNantesTests/Fakes/` group + fakes (`FakeAboutSource`, `FakeVenueSource`, `FakeSpeakersSource`, `FakeSpeakerDetailsSource`, `FakeAgendaSource`, `RecordingDataLoadTracing`) with pbxproj entries
- [ ] Five `<Feature>ViewModelTests.swift` suites
- [ ] `scripts/swift-observation-gate.sh` (+ selftest) and `ios.yml` step
- [ ] Hand-built Kotlin values for `Venue` (`init(address:description:floorPlanUrl:latitude:longitude:imageUrl:name:)`), `Partner` (`init(name:logoUrl:url:)`), `Room` (`init(id:name:sortIndex:)`) `[VERIFIED: shared.h:897,926,1154]`; `partners` is `[PartnerCategory: [Partner]]` and `rooms` is `Set<Room>` `[VERIFIED: shared.h:277-278]`
- Framework install: none.

## Security Domain

`security_enforcement` is enabled (ASVS level 1). The phase changes UI state management only; no new input surface, network endpoint, secret or dependency.

### Applicable ASVS Categories
| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | no | — |
| V3 Session Management | no | — |
| V4 Access Control | no | — |
| V5 Input Validation | partially | Keep existing `URL(string:)` handling; no new parsing. `UserDefaults` decoding stays `try?` with empty fallback |
| V6 Cryptography | no | — |
| V8 Data Protection / Logging | yes | Do not log content or user data; cancellation is not logged; error logs keep v1.0 messages (they flow to Crashlytics) |

### Known Threat Patterns
| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Test-only seams (`defaults:`, ports, tracing) widening production surface | Tampering | Defaults stay `.standard`/live; seams are internal, not exported |
| Mock `GoogleService-Info.plist` creation in CI | Information disclosure | Unchanged from Phase 6; contains no secrets |
| Gate script patterns matched in generated or third-party code | — | Scan only tracked first-party `*.swift`, exclude `SwiftGen/` |

## Project Constraints (from CLAUDE.md / AGENTS.md)

- Dependencies flow one way; features never depend on each other; cross-feature effects via lambdas (Android rule; iOS equivalent: no feature-to-feature VM references except the shared Agenda instance injected from `ContentView`).
- Data access goes through `DevFestNantesStore`; new store methods must be added to both `GraphQLStore` and `DevFestNantesStoreMocked` (no Kotlin store change in this phase).
- Koin is the DI; service-locator access only in sanctioned files; `IosDependencies` is the iOS access point (live adapters call `IosDependencies.shared.store()`).
- Tests: Swift Testing, no mocking library, hand-written fakes, snake_case sentence test names. App filter type must be written `DevFest_Nantes.SessionFilter` in files that `import shared`.
- Never change `versionName`, `versionCode`, `MARKETING_VERSION` (leave `MARKETING_VERSION` lines untouched when editing the pbxproj).
- Logging uses `DevFestLogger` with a category named after the VM/feature; no `os_log`/`print` in new code.
- After changing Kotlin types exported to Swift run `swift-names-gate.sh check` (not applicable; no Kotlin changes). Do run `kmp-nc-version-gate.sh` after pbxproj edits.
- Planning artifacts in English. Update `README.md`/`documentation/` in the same change (ARCHITECTURE.md line ~324 and the `BaseViewModel` bullet, DEVELOPMENT.md test/gate commands, `scripts/README.md`, AGENTS.md CI list).
- Conventional commits; run what CI runs before done: `./gradlew detekt lint`, `bash scripts/di-gate.sh`, `bash scripts/kmp-nc-version-gate.sh`, `./gradlew testDebugUnitTest jvmTest testAndroidHostTest` (unaffected by Swift-only changes, but the iOS lane is the relevant one).
- User rules: commit/push finished artifacts immediately (rate limits); do not create, switch or rename branches; paired-screenshot clock shifting needs data prefetched first.

## Sources

### Primary (HIGH confidence)
- Repo files read this session: `08-CONTEXT.md`, all five ViewModels and their views, `BaseViewModel.swift`, `PerformanceMonitoring.swift`, `iOSApp.swift`, `ContentView.swift`, `DevFestLogger.swift`, `RCValues.swift`, the Agenda filter menus, `project.pbxproj`, `iosApp.xctestplan`, `ios.yml`, `di-gate.sh`, `kmp-nc-version-gate.sh`, the characterization tests and `CharacterizationSupport.swift`, `shared.h` (store protocol, `Venue`/`Partner`/`Room` inits)
- KMP-NativeCoroutines v1.0.6 sources (local SwiftPM checkout): `AsyncSequence.swift`, `AsyncFunction.swift`, `Package.swift`
- Local experiments (3 throwaway hosted-test spikes, reverted): Observation + `didSet`, Logger in hosted tests, Kotlin values through `AsyncThrowingStream`, silent consumer cancellation
- `gh run view 41229227528` (CI success on Xcode 26.6, `XCRESULT-TESTS total=19`)

### Secondary (MEDIUM confidence)
- jessesquires.com/blog/2024/09/09/swift-observable-macro; nilcoalescing.com/blog/InitializingObservableClassesWithinTheSwiftUIHierarchy and .../InitializingObservableClassesWithTheStateMacroInXcode27; forums.swift.org/t/observable-init-called-multiple-times-by-state-different-behavior-to-stateobject/70811 (State init evaluation)
- `.planning/research/{SUMMARY,STACK,ARCHITECTURE,PITFALLS}.md` (prior milestone research)

### Tertiary (LOW confidence)
- Apple developer documentation pages (JS-rendered; fetch returned no body): ownership wrapper semantics taken from general knowledge, see A1

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — no new packages; versions read from the repo and local checkout
- Architecture: HIGH — matches locked decisions; shapes compiled and ran locally
- Pitfalls: HIGH for 1, 2, 4, 5, 7, 8 (read from source/verified), MEDIUM for 3 (documented behavior, local toolchain differs), 6, 9, 10 (need the simulator/CI pass)
- CI toolchain parity: MEDIUM — retired by first CI run

**Research date:** 2026-11-03
**Valid until:** 2026-12-03 (toolchain-dependent; re-check if CI's default Xcode changes)
