# Architecture Patterns

**Domain:** Kotlin Multiplatform conference app, v1.1 iOS Architecture (shared domain layer + modular SwiftUI app), strict behavior parity
**Researched:** 2026-10-23
**Overall confidence:** HIGH on the Kotlin side (read from the code), MEDIUM on the SPM/XCFramework wiring (well documented in the community, not yet proven in this repo)
**Supersedes:** the v1.0 file (module extraction / Koin migration). Its content is shipped and described in `documentation/ARCHITECTURE.md`.

## Recommendation in one screen

| Question | Decision | Confidence |
|---|---|---|
| Where do use cases live? | New KMP module `:core:domain`, depending on `:core:model` + `:core:data` (interfaces only). NIA-style `feature -> domain -> data`. | HIGH |
| Where do filter/bookmark persistence contracts live? | `SessionFilter` model in `:core:model`; `SessionFiltersStore` interface next to `BookmarksStore` in `:core:data`, with `androidMain` (SharedPreferences, **same key and format as today**) and `iosMain` (`NSUserDefaults`) actuals. | HIGH |
| How does iOS get them? | `:shared` adds `api` + `export(project(":core:domain"))`; `domainModule` is appended to `sharedModules` in `initKoin()`. `IosDependencies` grows a Koin-free `graph()` accessor. | HIGH |
| How do SPM packages see the Kotlin framework? | Replace the Xcode `embedAndSignAppleFrameworkForXcode` build phase by a **static XCFramework** consumed through a local `binaryTarget(path:)` in one `SharedKit` target. `embedAndSign` cannot feed SPM modules (they build before the script phase). | MEDIUM |
| Package layout | One local package `iosApp/Packages/DevFestKit` with one library target (+ test target) per feature and per core concern. App target = shell + composition root. | HIGH |
| Swift composition root | `CoreDI` target holds `AppDependencies` (Sendable struct of protocols). Only `CoreDI` and the app target may touch `IosDependencies`. | HIGH |
| Flows to `@Observable` | Keep KMP-NativeCoroutines (`asyncSequence(for:)`) behind ports returning `AsyncThrowingStream`; ViewModels are `@MainActor @Observable` and consume in `.task {}`. Do not adopt Swift Export (Alpha in Kotlin 2.4). | HIGH |

## Recommended Architecture

### Target module graph (Kotlin)

```
:androidApp ──> :feature:* ──> :core:ui ──┐
                    │                      ▼
                    └────────────> :core:domain  (NEW)  ──> :core:data ──> :core:network
                                         │                      │
                                         └──────────────────────┴──> :core:model
:shared (iOS umbrella)  ──api/export──> :core:model, :core:data, :core:domain (NEW), :core:analytics
:core:testing ──> :core:model, :core:data  (+ fakes used by :core:domain tests)
```

Existing rules stay: one-way flow, features never depend on each other, `initKoin()` is the only composition root, `allowOverride(false)`.
The `devfest.android.feature` convention plugin gets `implementation(project(":core:domain"))` so every feature module has it without editing six build files.

### Why `:core:domain` and not more code in `:core:data`

- `:core:data` owns I/O: Apollo mappers, `GraphQLStore`, storage actuals. It pulls `:core:network` (Apollo, SQLite cache). Agenda grouping/sorting/filtering is pure logic with no Apollo dependency; living in `:core:data` would let it reach `internal` GraphQL types and would tie its tests to the network module.
- A separate module gives a clean public surface to export to Swift: only use cases and pure policies, not the store plumbing.
- Cost is one more Gradle module (`devfest.kmp.library`, same 5 targets, KSP + `kmp.native.coroutines` plugins like `:core:data`). Precedent: `:core:data`, `:core:analytics` were extracted the same way in v1.0.
- Do NOT move `DevFestNantesStore` / `BookmarksStore` interfaces into domain. The cross-module churn buys nothing; domain depends on data for the interfaces (this is exactly the Now in Android shape).

### Component boundaries (new vs modified)

| Component | Status | Responsibility | Communicates with |
|---|---|---|---|
| `:core:domain` | **NEW** KMP module | Use cases + pure policies (filter, group, sort) + `domainModule` | `:core:data` (store, bookmarks, filters store), `:core:model` |
| `SessionFilter` (+ `SessionFilterType`) | **MOVED** from `feature/agenda/utils` to `:core:model` | Serializable value type, `data class` so `Set<SessionFilter>` has value equality | everything |
| `SessionFiltersStore` (interface) | **NEW** in `:core:data` (derived from `SessionFiltersService`) | `filters: StateFlow<Set<SessionFilter>>`, `setFilters` | domain |
| Android `SessionFiltersStoreImpl` | **MOVED** from `feature/agenda/services` to `:core:data` androidMain | SharedPreferences; same key `SHARED_PREFERENCES_KEY_AGENDA_FILTERS`, same JSON string-set format | `dataPlatformModule` |
| iOS bookmarks/filters stores | **NEW** `:core:data` iosMain (`NSUserDefaults`) | Back `BookmarksStore` and `SessionFiltersStore` on iOS (today iOS binds neither) | `dataPlatformModule` iosMain actual |
| `domainModule` | **NEW** | Koin bindings of use cases (`factoryOf`) | added to `sharedModules` in `InitKoin.kt` |
| `:shared` | MODIFIED | `api`/`export` of `:core:domain`; XCFramework outputs | iOS |
| `IosDependencies` | MODIFIED | Adds `graph()` returning a plain Kotlin holder of use cases. Still no Koin type in `shared.h` | Swift `CoreDI` only |
| `feature:agenda` `AgendaViewModel`, `core:ui` `BookmarksViewModel`, `androidApp` `HomeViewModel` | MODIFIED | Consume use cases instead of re-implementing filtering/bookmark logic; drop `SessionFiltersService` | `:core:domain` |
| `agendaModule` | MODIFIED | Remove the `SessionFiltersService` binding **in the same commit** the store binding appears in `dataPlatformModule` (no overriding allowed) | Koin |
| `core:testing` | MODIFIED | Add `FakeSessionFiltersStore` next to `FakeBookmarksStore` | domain/VM tests |
| `iosApp/Packages/DevFestKit` | **NEW** local SPM package | See "iOS layout" | app target |
| iOS app target | MODIFIED -> shell | `@main`, tab shell, `Info.plist`, `GoogleService-Info.plist`, AppIcon, composition (`AppComposition.swift`) | `CoreDI`, feature products |
| `.github/workflows/ios.yml`, `scripts/build-ios-*.sh`, `iosApp.xcodeproj`, `settings.gradle.dcl` | MODIFIED | XCFramework build, iOS 17, tests, `include(":core:domain")` | |
| `DevFestNantesStore` | **UNCHANGED** (no new store methods expected) | If one becomes necessary: add to `GraphQLStore` and `DevFestNantesStoreMocked` in the same change, contract test covers both | |

### What the use cases are (keep the set small, logic-bearing only)

Both platforms duplicate or diverge today; extract only where logic exists. Pure pass-throughs (session by id, venue) stay direct store calls.

| Use case / policy | Source of truth today | Notes |
|---|---|---|
| `SessionFilterPolicy.apply(sessions, filters, bookmarkedIds)` (pure function) | Android `AgendaViewModel.filterSessions`; iOS `AgendaView.getFilteredSessions` | Semantics are already identical: OR within a filter type, AND across types, bookmark = membership. One implementation + a matrix test. |
| `ObserveAgendaUseCase` | Android `AgendaViewModel` combine of `store.agenda` + filters + bookmarks; iOS groups `store.sessions` itself | Returns `Flow<Map<dayIndex, DayTimeline>>`, each timeline = `List<TimeSlot(startDate: Instant, sessions)>`, sessions filtered and sorted. Formatting stays in each UI. |
| Observe / set / clear session filters | `SessionFiltersService` (Android), `UserDefaults` (iOS) | Thin over `SessionFiltersStore`. |
| Observe bookmarks / `ToggleBookmarkUseCase` | `BookmarksStore` (Android), `UserDefaults` "Favorites" (iOS) | Toggle = read-then-write inside the use case. |
| `ObserveSpeakersUseCase` | iOS `SpeakersViewModel.getAlphabets/speakers(for:)` | Only if Android does the same A-Z grouping; otherwise leave Swift-side presentation. Confirm in phase research. |
| `ObservePartnersUseCase` | iOS `AboutViewModel` (sorted category keys) | Same confirmation. |

Style: classes with **one named method** (`observe(...)`, `execute(...)`), not `operator fun invoke`. Swift sees `invoke()`, which reads poorly and is hard to rename; use `@ObjCName` only where a collision with a Swift type exists. Flows get `@NativeCoroutines`, `StateFlow` properties `@NativeCoroutinesState`.

Computation dispatcher: inject a `CoroutineDispatcher` into the use case constructor defaulting to `Dispatchers.Default`. Do not reuse the `IO_DISPATCHER` named binding (it lives in `:core:ui`, which domain cannot see, and Koin forbids a second binding of the same name).

### Parity conflicts the roadmap must decide (not silently pick)

1. **Room ordering inside a time slot.** Android sorts by `Room.sortIndex` (Jules Verne, Titan, Belem, ...). iOS sorts by room *name* string (`$0.room < $1.room`). A single shared policy changes one platform. Recommendation: adopt Android's `sortIndex` (it is the designed order) and record the iOS ordering change as an accepted, visible deviation in the phase's UAT, or keep a `RoomOrder` parameter that reproduces iOS order until the user signs off.
2. **Day splitting.** Android uses `Agenda.days` (hard-coded 2025 `DAY_ONE/DAY_TWO`); iOS splits by a formatter on `startDate`. Use `store.agenda` days in the shared use case; both hard-code 2025 dates, which is existing debt (out of scope).
3. **First-emission-only on iOS.** iOS currently reads one emission (`break`) from flows; Android collects continuously. Combining bookmarks/filters in the use case requires continuous collection on iOS. This is a required (not cosmetic) change; keep one-shot behavior for speakers/partners/venue where nothing changes at runtime.
4. **`SessionFilter` is a plain `class` on Android.** Moving it to a `data class` is required for set semantics in Swift; add a characterization test on the Android drawer toggle logic first.
5. **Filter value identity across platforms.** iOS persists `room.id`, `language.name`, `complexity.name`, `type.name` with lowercase `FilterType` raw values (`bookmark`, `room`, ...). Android persists enum names (`BOOKMARK`, ...). The migrator must map these; characterize that `room.id == room name` in the real data before relying on it.

## Data Flow

### Agenda after the change (both platforms)

```
Apollo cache/network
  -> GraphQLStore.agenda (Flow<Agenda>)          [:core:data, unchanged]
  -> ObserveAgendaUseCase                         [:core:domain]
       combine( store.agenda,
                SessionFiltersStore.filters,      [persisted: SharedPreferences | NSUserDefaults]
                BookmarksStore.bookmarkedIds )    [persisted: SharedPreferences | NSUserDefaults]
       -> SessionFilterPolicy.apply -> group by start -> sort by room
       -> Flow<Map<Int, DayTimeline>>
  -> Android: AgendaViewModel (StateFlow) -> Compose
  -> iOS:     port (AsyncThrowingStream) -> AgendaViewModel (@MainActor @Observable) -> SwiftUI
Writes: UI -> ToggleBookmarkUseCase / SetSessionFilters -> store -> persisted -> flows re-emit
```

### Koin wiring

```kotlin
// core/domain/.../di/DomainModule.kt
@HiddenFromObjC
val domainModule: Module = module {
    factoryOf(::ObserveAgendaUseCase)
    factoryOf(::ToggleBookmarkUseCase)
    // ...
}

// shared/.../InitKoin.kt  (the ONLY change to the composition root)
val sharedModules: List<Module> = listOf(networkModule, dataModule, domainModule, analyticsModule)
```

- `dataPlatformModule` actuals gain the filter store (and, on iOS, the bookmarks store). Android's `BookmarksStoreImpl` binding is unchanged.
- Add `DomainModuleTest` using `verify()` (as the other `*ModuleTest`s do) and keep `KoinGraphTest` green; the iOS `IosKoinTest` additionally asserts the new graph accessor resolves.
- `scripts/di-gate.sh` allowlist already covers `IosDependencies.kt`; any new iosMain file that touches `KoinPlatform` must be under `di/` or named `*Module.kt`, or the gate fails.

### Exposure to Swift (Koin stays invisible)

```kotlin
// shared/src/iosMain/.../IosDependencies.kt (extended)
object IosDependencies {
    fun start() { /* unchanged, idempotent */ }
    fun graph(): IosGraph { start(); return KoinPlatform.getKoin().let { IosGraph(it.get(), it.get(), it.get(), ...) } }
}
class IosGraph(val store: DevFestNantesStore, val bookmarks: BookmarksStore, val observeAgenda: ObserveAgendaUseCase, ...)
```

`IosGraph` is a plain holder, so no DI type reaches `shared.h` (v1.0 invariant). Keep `IosDependencies.store()` temporarily for not-yet-migrated screens, delete it when the last `BaseViewModel` goes.

## iOS: Packages, Framework, Composition Root

### Consuming the Kotlin framework from SPM (the pivotal decision)

Today the app target builds the framework in a shell phase (`./gradlew :shared:embedAndSignAppleFrameworkForXcode`) and does `import shared`. Local SPM packages resolve and compile **before** that phase runs and cannot see its output. Community evidence (Kotlin Slack, kotlin-spm-plugin) converges on a static XCFramework as a `binaryTarget`.

Recommended pipeline:

1. `shared/build.gradle.kts`: declare `val xcf = XCFramework("shared")` and `xcf.add(framework)` inside the existing `binaries.framework { baseName = "shared"; isStatic = true; export(...) }`. Drop `iosX64` from the XCFramework slices if CI and devs are Apple Silicon only (faster); keep it for Gradle tests if desired. Produces `assembleSharedDebugXCFramework` / `assembleSharedReleaseXCFramework` -> `shared/build/XCFrameworks/{debug,release}/shared.xcframework`.
2. A small Gradle `Copy` task (`syncSharedXCFramework -PxcfConfig=debug|release`) places it at the fixed, gitignored path `iosApp/Frameworks/shared.xcframework`, because `binaryTarget(path:)` needs a static path while debug/release differ.
3. `Packages/DevFestKit/Package.swift`: `.binaryTarget(name: "shared", path: "../../Frameworks/shared.xcframework")` (target name must equal the module name `shared`; keep `baseName = "shared"` so Swift `import shared` and the names gate keep working).
4. Delete the `embedAndSign` Run Script phase from the app target (static framework is linked through the package; keeping both would link two copies of the Kotlin runtime).
5. Xcode scheme pre-action + CI step + `scripts/build-ios-*.sh` run the sync task first (release scripts use `release`).

Consequences to document in `DEVELOPMENT.md`: on a fresh checkout the Gradle sync task must run once before Xcode can resolve packages; Kotlin changes are no longer rebuilt automatically by Xcode (re-run the task, or add a scheme pre-action).

Fallback if this friction proves unacceptable (flag as a phase research item): keep `embedAndSign`, and make feature packages **not** import `shared` at all. They would depend on Swift protocols + Sendable value types in a `CoreModel` target, and only the app target would hold the Kotlin adapters. This is cleaner for Swift 6 (Kotlin types never cross actors) but needs a Swift mirror of ~10 domain types and a view-layer rewrite, so it is more work and more parity risk. Not recommended as the default.

Rejected: Swift Export (Alpha in Kotlin 2.4, gaps such as functions taking suspend types were reported on 2.4.20, requires recent Xcode); KMMBridge/remote binary (no distribution need); CocoaPods (project already moved to SPM).

### Package layout

One manifest, tools-version 6.0, `swiftLanguageModes: [.v6]` on every target from day one (new code starts strict; only the app target flips at the end), `platforms: [.iOS(.v17)]`.

```
iosApp/
  iosApp.xcodeproj           # app target + shared scheme / test plan only
  iosApp/                    # @main, ContentView(tab shell), AppComposition.swift, Info.plist, AppIcon
  Frameworks/                # gitignored: shared.xcframework (synced from Gradle)
  Packages/DevFestKit/
    Package.swift
    Sources/
      SharedKit/             # binaryTarget "shared" + @_exported @preconcurrency import shared,
                             #   KMPNativeCoroutinesAsync, retroactive @unchecked Sendable on Kotlin models
      CoreServices/          # protocols only: AnalyticsTracking, PerformanceTracing, FeatureFlags, Logger (DevFestLogger)
      FirebaseServices/      # Firebase impls (the only target depending on firebase-ios-sdk)
      DesignSystem/          # theme, common components, SwiftGen Strings/Assets, Localizable.strings en/fr, asset catalog
      CoreDI/                # AppDependencies, live adapters over IosGraph, legacy-defaults migrator, preview/test doubles
      AgendaFeature/         # AgendaView, AgendaViewModel, filter menus, session detail
      SpeakersFeature/
      VenueFeature/
      AboutFeature/
    Tests/                   # one Swift Testing target per feature + CoreDI
```

- Dependency rules (mirror the Android rules): `Feature -> DesignSystem, CoreServices, SharedKit`; `CoreDI -> SharedKit, CoreServices, FirebaseServices, feature ports`; features never import each other, never import `CoreDI`, never import Firebase.
- This reads "per feature" as one target + product per feature in one package. If the roadmap insists on one `Package.swift` per feature, the structure is identical and the split is mechanical; the cost is N manifests and N `Package.resolved` interactions. Recommend the single manifest.
- Session detail stays inside `AgendaFeature` (as today). Android's separate `session-detail` module is not mirrored.
- Extract in order of increasing coupling: About, Venue, Speakers, Agenda last.
- Firebase moves out of the project into `FirebaseServices` (keep the existing 11.14+ range; the Crashlytics Run Script path to `SourcePackages/checkouts/firebase-ios-sdk` stays valid).

### Swift composition root

```swift
// CoreDI
public struct AppDependencies: Sendable {
    public let agenda: any AgendaPort
    public let speakers: any SpeakersPort
    public let venue: any VenuePort
    public let partners: any PartnersPort
    public let analytics: any AnalyticsTracking
    public let performance: any PerformanceTracing
    public let flags: any FeatureFlags

    @MainActor public static func live() -> AppDependencies {
        IosDependencies.shared.start()
        let graph = IosDependencies.shared.graph()
        LegacyDefaultsMigrator().runOnce(bookmarks: graph.bookmarks, filters: graph.sessionFilters)
        return AppDependencies(agenda: LiveAgendaPort(graph), /* ... */
                               analytics: FirebaseAnalyticsService(), performance: FirebasePerformance(),
                               flags: RemoteConfigFlags())
    }
    public static let preview = AppDependencies(/* fakes */)
}

// App target
@main struct iOSApp: App {
    private let dependencies: AppDependencies
    init() { FirebaseApp.configure(); dependencies = .live() }
    var body: some Scene { WindowGroup { RootView(dependencies: dependencies) } }
}
```

- Ports (`AgendaPort`, ...) are defined **in the feature targets** they serve (dependency inversion), implemented in `CoreDI`. A feature only sees a narrow protocol, so its ViewModel tests use hand-written Swift fakes (same philosophy as `core:testing`; no mocking library).
- Features expose a factory view (`AgendaScreen(port:analytics:performance:flags:)`) that builds its ViewModel in `init`: `_viewModel = State(initialValue: AgendaViewModel(...))`. Constructor injection only; no `@Environment` service locator, no `.shared`.
- Removed by this: `BaseViewModel` (and its default argument `IosDependencies.shared.store()`), `RCValues.sharedInstance`, the global `var remoteConfig`, `PerformanceMonitoring.shared`, `FirebaseAnalyticsService.shared`.
- Add a Swift analogue of `di-gate.sh` (`scripts/swift-di-gate.sh`): `IosDependencies` and `.shared`/`sharedInstance` allowed only in `CoreDI`, `FirebaseServices` and the app target. Wire it into the iOS workflow.
- Note: `scripts/swift-names-gate.sh` is **not** in `scripts/` or any workflow; the only copy is archived under `.planning/milestones/v1.0-phases/03-multi-module-architecture-extraction/`. Restore it (and run it in CI) before adding exported Kotlin types, since new public domain types are exactly what can re-trigger the v1.0 Swift/Kotlin name collisions. The Swift structs `SessionFilter`/`FilterType` must be deleted in the same change that exports Kotlin `SessionFilter`.

### Kotlin Flow -> `@Observable` ViewModel

```swift
// CoreDI (adapter; the only code that sees asyncSequence(for:))
struct LiveAgendaPort: AgendaPort, @unchecked Sendable {
    let useCase: ObserveAgendaUseCase
    func observe() -> AsyncThrowingStream<AgendaSnapshot, Error> {
        AsyncThrowingStream { continuation in
            let task = Task {
                do { for try await value in asyncSequence(for: useCase.observeNative()) { continuation.yield(map(value)) }
                     continuation.finish() }
                catch { continuation.finish(throwing: error) }
            }
            continuation.onTermination = { _ in task.cancel() }   // cancels the Kotlin collection
        }
    }
}

// Feature
@MainActor @Observable
final class AgendaViewModel {
    private(set) var state: AgendaState = .loading
    private let port: any AgendaPort
    init(port: any AgendaPort, ...) { self.port = port }
    func observe() async {
        do { for try await snapshot in port.observe() { state = .loaded(snapshot) } }
        catch { logger.log(.error, "...", error: error) }
    }
}
// View: .task { await viewModel.observe() }   // SwiftUI cancels on disappear -> Task -> Flow
```

Rules:
- One Flow-to-Swift bridge layer (`CoreDI` adapters). ViewModels never call `asyncSequence(for:)` or import `KMPNativeCoroutinesAsync`.
- Map Kotlin results to Sendable Swift values (existing `AgendaContent`/`PartnerContent`/`VenueContent` are the precedent) or rely on the documented `@unchecked Sendable` extensions in `SharedKit` for immutable Kotlin data classes. Pick the mapping where a view needs derived data (sections, formatted dates), the extension where the view uses the Kotlin type as-is (parity-safe).
- Dispatchers: do not mix `Dispatchers.Main` assumptions into use cases; Swift `await` inside a `@MainActor` context already hops back to the main actor. Default to `Dispatchers.Default` in Kotlin.
- Replace `DispatchQueue.main.async` with `@MainActor` isolation, `ObservableObject/@Published/@ObservedObject/@StateObject` with `@Observable/@State/@Bindable`, and remove `KMPNativeCoroutinesCombine` and every `import Combine`.

### Persistence migration (no favorite lost)

- Kotlin iosMain stores use **new** keys (filters stored in Android's JSON format, so both platforms share one serializer and one test).
- Swift `LegacyDefaultsMigrator` (in `CoreDI`) runs once at composition time, before any ViewModel exists: reads legacy `"Favorites"` (`[String]`) and `"AgendaSessionFilters"` (Swift `Codable` `Set` with lowercase types, decoded by a private legacy struct), writes through the **public store API** (`setBookmarked`, `setFilters`), then sets a `migration.v1.done` flag. It never deletes the legacy keys in this release (rollback safety); delete in a later release.
- Why Swift and not Kotlin: the legacy format is Swift-`Codable`, the Swift types are being deleted anyway (keep a private legacy struct in the migrator), and Swift Testing can drive it with `UserDefaults(suiteName:)`.
- Idempotence and partial failure: write first, flag last; a crash between re-runs the migration harmlessly (set semantics).
- The Android side needs no migration: same prefs file, same keys, same JSON; the Phase 5 persistence-pinning tests stay the guard.
- iOS stores must be thread-safe (update a `MutableStateFlow` atomically with `update {}`); the Android `BookmarksStoreImpl` uses an unsynchronized `HashSet`, do not copy that into the iOS actual.

## Patterns to Follow

### Pattern 1: Pure policy + thin use case
**What:** Business rules are top-level pure functions/objects (`SessionFilterPolicy.apply`), use cases only combine flows and call them.
**When:** Any rule both platforms need.
**Why:** Matrix-testable in `commonTest` (JVM + iOS simulator, per existing CI), no fakes needed for the core logic; reuse the v1.0 filter matrix test as the characterization oracle.

### Pattern 2: Ports defined by the consumer
**What:** Feature targets declare the protocol they need; `CoreDI` implements it.
**When:** Every ViewModel dependency (domain access, analytics, performance, flags).
**Why:** ViewModels testable with Swift fakes, features independent of Firebase and of Kotlin plumbing.

### Pattern 3: Single bridge layer for Kotlin concurrency
**What:** `@preconcurrency import shared` and all `@unchecked Sendable` escape hatches live in `SharedKit`/`CoreDI` only.
**Why:** Swift 6 strict concurrency stays enforceable everywhere else; one place to audit.

### Pattern 4: Add-then-switch-then-remove for every migration
**What:** Add the shared capability (CI green) -> switch one platform/screen (CI green) -> delete the old code in a separate commit.
**Why:** Each step is revertible and shippable; avoids Koin duplicate-binding failures (`allowOverride(false)`) by sequencing the binding move.

## Anti-Patterns to Avoid

### Anti-Pattern 1: Keeping `embedAndSign` and adding a second framework path
**Why bad:** Two statically linked copies of the Kotlin runtime -> duplicate class warnings, state divergence (two Koin graphs), crashes.
**Instead:** One framework source (the XCFramework via `SharedKit`). Same for tests: package test targets must be host-less (no app host), otherwise the framework is linked into both app and test bundle.

### Anti-Pattern 2: Exposing Koin or a generic `get<T>()` to Swift
**Why bad:** Reintroduces service locator in Swift and a DI type in `shared.h` (v1.0 D-invariant).
**Instead:** `IosGraph` holder; Swift resolves nothing at runtime.

### Anti-Pattern 3: A "domain" module that just proxies the store
**Why bad:** Dead layers; every new passthrough must be added to both stores and a use case.
**Instead:** Use cases only where there is logic to share.

### Anti-Pattern 4: Sharing ViewModels or `StateFlow` UI state across platforms
**Why bad:** Explicitly out of scope; SKIE/androidx.lifecycle KMP not adopted.
**Instead:** Platform-native ViewModels over shared use cases.

### Anti-Pattern 5: `@MainActor` on the whole bridge
**Why bad:** Hidden main-thread dependencies in code that only forwards Kotlin calls.
**Instead:** `@MainActor` on ViewModels only; adapters are nonisolated and `Sendable`.

### Anti-Pattern 6: Fixing discovered bugs while migrating
**Why bad:** Breaks the strict-parity contract (e.g. the unhandled store failures pinned in Phase 5, `speakersPage`, `RoomSortIndex`).
**Instead:** Log to deferred items; the only deliberate changes are the ones listed under "Parity conflicts" and signed off.

## Suggested Build Order (each step ships, CI stays green)

Numbering continues from v1.0 (phases 1-5), so these would be phases 6+. Dependencies drive the order: Kotlin domain before iOS adoption; iOS language/toolchain floor before modularization; DI before extraction; strict Swift 6 enforced per package as they are created and on the app last.

| # | Phase | Contents | Shippable/CI gate | Depends on |
|---|---|---|---|---|
| 1 | **iOS CI + test harness** | Replace dead `DevFest NantesTests` (its mock is fully commented out) with a working Swift Testing target; `xcodebuild test` on the resolved simulator in `ios.yml`; bump the Swift `KMP-NativeCoroutines` pin from `1.0.0-ALPHA-45` to `1.0.6` to match the Gradle plugin (`kmpNativeCoroutines = 1.0.6`; the two must match); restore `swift-names-gate.sh` into CI | iOS job runs real tests | none |
| 2 | **Shared domain (Kotlin only)** | `:core:domain` module (+ `settings.gradle.dcl`), `SessionFilter` to `:core:model`, `SessionFiltersStore` interface + Android impl moved with identical key/format, `FakeSessionFiltersStore`, pure policies + use cases, `domainModule` in `sharedModules`, `DomainModuleTest`/`verify()`, `commonTest` matrix on JVM + iOS sim, export from `:shared` | Android unaffected, existing VM tests pass; iOS framework larger but app unchanged | none (parallel with 1) |
| 3 | **Android adopts domain** | `AgendaViewModel`, `BookmarksViewModel`, `HomeViewModel` use use cases; remove `SessionFiltersService`/Impl + its Koin binding (same commit as the data-module binding); keep v1.0 VM tests as the parity oracle | Android CI + instrumentation green; UAT agenda filters/bookmarks | 2 |
| 4 | **iOS toolchain floor + Observation** (in place, still one target) | iOS 17 on all targets (today 15.0 and 17.0 are mixed), `@Observable` ViewModels, remove Combine and `KMPNativeCoroutinesCombine`, `SWIFT_STRICT_CONCURRENCY=complete` as warnings (Swift 5 mode) to surface the backlog early, add Swift Testing tests for each ViewModel using fakes | iOS build + tests green | 1 |
| 5 | **iOS adopts domain + persistence migration** | iosMain stores (`NSUserDefaults`), `IosGraph`/`graph()`, `AgendaViewModel` loses `UserDefaults`, grouping and filtering, other VMs on shared mapping, `LegacyDefaultsMigrator` with tests, delete Swift `SessionFilter`; decide the parity conflicts above; manual UAT on device with a pre-seeded old install | iOS tests + UAT "upgrade keeps favorites and filters" | 2, 4 |
| 6 | **iOS explicit DI** (still in app target) | `AppDependencies` + ports + protocol services for analytics/performance/flags, delete `BaseViewModel`, `RCValues` singleton, global `remoteConfig`, `.shared` singletons; `swift-di-gate.sh` in CI | iOS CI green; gate green | 5 |
| 7 | **SPM modularization** | XCFramework Gradle tasks + sync + Xcode/CI/release-script changes (riskiest step, isolate it), `SharedKit`, `DesignSystem` (SwiftGen output moved, resources via `Bundle.module`), `CoreServices`, `FirebaseServices`, `CoreDI`; then extract About -> Venue -> Speakers -> Agenda, one commit each, test target per feature; app target ends as a shell; new targets declared Swift 6 mode | After each extraction: iOS CI (build + tests) green and a manual smoke | 6 |
| 8 | **Swift 6 flip + smoke tests + docs** | App target to Swift 6 mode, fix remaining warnings, per-feature SwiftUI smoke test (host a view in `UIHostingController` and load it; no extra dependency), update `README.md`, `documentation/ARCHITECTURE.md`/`DEVELOPMENT.md`, `AGENTS.md`, `iOS_BUILD_DSYM.md`, CI cache keys (add `Package.swift` and `Frameworks`) | Full CI green; `warnings == 0` | 7 |

Why this order:
- Phase 2/3 are Kotlin-only and independently revertible; Android is the lower-risk first consumer of the new module and validates the shared logic against existing tests before iOS depends on it.
- Observation + iOS 17 (4) before domain adoption (5) keeps the diff in `AgendaViewModel` reviewable (mechanical first, semantic second).
- DI (6) before SPM (7) because extracting features that still call `IosDependencies.shared.store()` would force `CoreDI` access into every package.
- XCFramework switch (7) last among structural changes: until then the proven `embedAndSign` flow keeps everything working, and every earlier phase is independent of it.
- Tests start at phase 1, not at the end; each phase adds its own tests.

## Pitfalls specific to integration (details in PITFALLS.md)

| Concern | Risk | Mitigation |
|---|---|---|
| Koin `allowOverride(false)` | Moving `SessionFiltersService` between modules duplicates a binding and crashes at start | Remove old and add new binding in one commit; `KoinGraphTest` + `verify()` catch it |
| Kotlin/Native naming | New exported types (`SessionFilter`, `TimeSlot`, `DayTimeline`) collide with Swift types or each other | Gate in CI; rename with `@ObjCName` before first export; delete the Swift duplicates in the same change |
| `SessionFilter` nested enum | `SessionFilter.FilterType` exports as `SessionFilterFilterType` | Make it a top-level `SessionFilterType` when moving to `:core:model` |
| Static framework in two bundles | Duplicate Kotlin runtime in app + app-hosted test | Host-less package test targets; one `SharedKit` |
| SwiftGen with packages | Generated `L10n` uses `Bundle(for: BundleToken.self)`, wrong for SPM resource bundles; SPM cannot run the script | Generate with a bundle-aware template (verify the `bundle` parameter in SwiftGen 6.6) or commit generated output into `DesignSystem`; `defaultLocalization: "en"` + `fr.lproj` as processed resources |
| `@State` + `@Observable` | `@State var vm = VM()` re-creates the object on each parent re-evaluation | Create in `init` via `State(initialValue:)`, or hold in a factory view |
| KMP-NativeCoroutines version skew | Swift pin ALPHA-45 vs plugin 1.0.6 | Align first (phase 1) |
| Fresh-clone Xcode | Package resolve fails until the XCFramework exists | Documented bootstrap command; CI step order |

## Scalability Considerations (build and team, not traffic)

| Concern | Now (1 maintainer, ~4k Swift lines) | If features grow |
|---|---|---|
| iOS build time | Single package, XCFramework prebuilt, incremental per target | Split packages per feature (mechanical); cache `Frameworks/` in CI keyed on Kotlin sources |
| Cross-platform logic | One `:core:domain`, tested on JVM + iOS sim | Split by area only if the module exceeds ~20 use cases |
| Swift concurrency audit | One bridge layer | Same; reviewable surface stays small |

## Sources

- Code read for this research (HIGH): `shared/build.gradle.kts`, `InitKoin.kt`, `IosDependencies.kt`, `core/data` (`DevFestNantesStore`, `BookmarksStore`, `DataModule`, platform modules, `BookmarksStoreImpl`), `core/model` (`Agenda`, `Session`), `feature/agenda` (`AgendaViewModel`, `SessionFiltersService`, `AgendaColumn`), `iosApp/iosApp` (`iOSApp`, `BaseViewModel`, `AgendaViewModel`, `AgendaView`, filter menus, VMs, `RCValues`), `iosApp.xcodeproj/project.pbxproj`, `.github/workflows/ios.yml`, `gradle/libs.versions.toml`, `scripts/di-gate.sh`, `build-logic` KMP convention.
- KMP-NativeCoroutines README and releases (MEDIUM, official repo): https://github.com/rickclephas/KMP-NativeCoroutines (1.0.6 latest, Kotlin 2.4.20; Swift package version must match the Gradle plugin; `asyncSequence(for:)`/`asyncFunction(for:)`; no Swift 6/Sendable guidance in README).
- Local Swift package + KMP XCFramework discussion (LOW-MEDIUM, community): https://slack-chats.kotlinlang.org/t/18874574/hello-i-am-testing-out-some-local-swift-packages-in-my-proje ; https://github.com/PaGr0m/kotlin-spm-plugin ; https://medium.com/@kaseken/creating-a-kotlin-library-for-shared-use-on-ios-and-android-with-kotlin-multiplatform-68cf92d946ed (embedAndSign cannot serve SPM modules; static XCFramework `binaryTarget(path:)` works).
- Kotlin 2.4 Swift export status (MEDIUM): https://kotlinlang.org/docs/whatsnew24.html ; https://medium.com/@santimattius/kotlin-2-4-swift-export-flow-asyncsequence-and-swift-packages-6a97c3afe7b7 (Alpha; suspend -> async, Flow -> AsyncSequence; not recommended yet).
- Swift 6 + KMP concurrency patterns (LOW, blog): https://dev.to/software_mvp-factory/swift-6-strict-concurrency-meets-kotlin-coroutines-in-kmp-148c (Kotlin data classes are not Sendable; AsyncStream adapter with `onTermination` cancellation; keep bridging at one boundary).
