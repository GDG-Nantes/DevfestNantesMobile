# Feature Landscape

**Domain:** Modern KMP + native SwiftUI architecture (milestone v1.1 iOS Architecture, strict behavior parity)
**Researched:** 2026-10-23
**Overall confidence:** HIGH on the duplication inventory and dependencies (read from source); MEDIUM on Swift 6 / KMP interop specifics (no authoritative Swift 6 statement found for KMP-NativeCoroutines; must be proven by a spike)

"Features" here are architectural capabilities. Every item below must ship with zero user-visible change.

Legend: Complexity = Low (under 1 day), Med (1-3 days), High (3+ days or real risk). "Depends on" lists existing code or earlier items.

---

## 1. Inventory of duplicated logic (iOS Swift vs Android/KMP)

This is the concrete scope of the "shared domain layer" capability. Paths are relative to the repo root.

### 1.1 Logic that is duplicated and SHOULD move to KMP

| # | Concern | iOS location | Android / KMP location | Parity status | Target shared API (suggested) |
|---|---------|--------------|------------------------|---------------|-------------------------------|
| D1 | **Favorites persistence + toggle** | `Agenda/AgendaViewModel.swift` L21, L30-37, L40-49, L63-83: `UserDefaults` key `"Favorites"` (ordered `[String]`), manual `objectWillChange.send()`. `AgendaDetailView.swift` L14 creates a **second** `AgendaViewModel()` to toggle favorites. | `core/data/.../BookmarksStore.kt` (interface, commonMain) + `BookmarksStoreImpl.kt` (**androidMain only**, SharedPreferences key `selected_sessions`, string set). `core/ui/.../BookmarksViewModel.kt` couples toggle + `analyticsService.eventBookmark`. | Same concept, different storage. **iOS has no `BookmarksStore` actual**: `core/data/src/iosMain/.../DataPlatformModule.ios.kt` is an empty `module { }`. | `BookmarksStore` iOS actual + `ToggleBookmarkUseCase` (store + analytics event) |
| D2 | **Session filter model + persistence** | `Model/SessionFilter.swift` (Codable struct, lowercase raw values `bookmark/complexity/language/room/type`); `AgendaViewModel.swift` L24-26, L52-61: JSON `Data` under `"AgendaSessionFilters"`, `clearFilters()` removes the key. | `feature/agenda/.../utils/SessionFilter.kt` (**in the Android feature module, not shared**, `@Serializable`, uppercase enum names) + `services/SessionFiltersService.kt` (SharedPreferences string-set of JSON). | Same semantics, **incompatible storage formats** (JSON `Data` vs JSON string set, lowercase vs uppercase `type`, bookmark value `"bookmark"` vs `""`). | `SessionFilter` + `SessionFiltersRepository` (`StateFlow<Set<SessionFilter>>`) moved to `core:model` / `core:data`, iOS actual reads legacy format |
| D3 | **Session filtering algorithm** | `Agenda/AgendaView.swift` L136-144 (`filteredSections`, `filteredSessionsForSection`, favorites applied separately) and L197-225 (`getFilteredSessions`) | `feature/agenda/.../AgendaViewModel.kt` L112-167 (`filterSessions`, suppresses `CyclomaticComplexMethod`) | **Same semantics**: OR within a filter type, AND across types, bookmark = membership in favorites. Subtle iOS latent issue: room filter value is `room.id` (`RoomFilterMenu.swift`) but `getFilteredSessions` compares it to `AgendaContent.Session.room` which is `room.name`. Works only because ids equal names ("Jules Verne"...). | `FilterSessionsUseCase(sessions, filters, bookmarkedIds)` as a pure function. Pin with the existing Android filter-matrix test as golden spec. |
| D4 | **Time-slot grouping and in-slot sort** | `AgendaViewModel.swift` L121-136: group by `startDate` (parsed to `Date`), keys sorted, sessions sorted by **room name** string. Day label via `DateFormatter("yyyy-MM-d")` in **device timezone**. | `feature/agenda/.../AgendaColumn.kt` L102-126 `groupSessionsByStartTime` (groups by locale-formatted start-time string, sorts by `session.room?.name`). Day split in `core/model/.../Agenda.kt` `Agenda.Builder` (UTC day buckets). | Same grouping and sort key (room name, not `RoomSortIndex`). Differs in how keys are built (locale string vs `Date`) and day bucketing (UTC vs device tz). | `GroupAgendaUseCase`: `Agenda`/sessions -> `days -> timeSlots -> sessions`, with an explicit tz rule |
| D5 | **Event dates / day selection** | `AgendaView.swift` L23, L72-73, L187-192: literal `"2025-10-16"` / `"2025-10-17"` x5; `prepareInitialDayAndScroll` picks today else day 1. | `core/model/.../Agenda.kt` `DAY_ONE_ISO`/`DAY_TWO_ISO` constants; `feature/agenda/.../Agenda.kt` L91-101 `todayPageIndex()` (last day whose local date == today else 0). | Same rule (today if it is a day of the event, else first day). Hardcoded dates triplicated. | `InitialDayUseCase(days, today, tz)` returning a day index |
| D6 | **"Current slot" scroll target** | `AgendaView.swift` L148-177: last section whose time-of-day <= now (time-of-day only, ignores date), scrolls to it. | `AgendaColumn.kt` L131-150 `currentStartTimeIndex`: first group whose **end** is in the future. | **Diverges**: iOS = last started slot; Android = first not-yet-ended slot (differ while a slot is in progress and at gaps). | Do NOT unify under strict parity. Either leave per-platform (UI concern) or share with a documented, parameterized rule. See Anti-Features. |
| D7 | **Session classification** | `Model/Data/AgendaContent.swift` L30-38 `isATalk` (false for lunch/opening/break); `AgendaView.swift` L103 `isNavigable` (conference/codelab/quickie) | `feature/agenda/.../utils/SessionTypeUtils.kt` `SessionType.isService()` (OPENING/BREAK/LUNCH), used by `AgendaRow.kt` L149 | `isATalk == !isService()` | Extension in `core:model` (`SessionType.isService()`, `isNavigable`) |
| D8 | **Speakers alphabetical index** | `UI/Speakers/SpeakersViewModel.swift` L56-70 `getAlphabets()`, `speakers(for:)` (uppercase first letter, `Set` then sorted) | `feature/speakers/.../list/Speakers.kt` L169, L259 (lowercase `first()` index map) | Same intent; different casing/unicode handling (no accent folding on either) | `GroupSpeakersByInitialUseCase` -> `List<Section(letter, speakers)>` |
| D9 | **Partners grouping + order** | `UI/About/AboutViewModel.swift` L28-45 (sort category keys; `PartnerCategory: Comparable` by `ordinal` in `Model/Data/PartnersContent.swift`) | `feature/about/.../partners/PartnersViewModel.kt` (five hardcoded buckets: PXL, PLATINIUM, GOLD, VIRTUAL, VELOTYPE) | **Diverges**: iOS shows every category in enum order, Android only the five known buckets in a fixed UI order. | `GetPartnersByCategoryUseCase` -> ordered `List<PartnerGroup>`; characterize both before choosing |
| D10 | **Venue mapping + language + fallbacks** | `Model/Data/VenueContent.swift` (hardcoded fallback URLs for image and floor plan when null; force-unwraps lat/lon); `UI/Venue/VenueViewModel.swift` L20-27 `currentLanguage` (fr -> French else English) | `feature/venue/.../VenueViewModel.kt` + `utils/toContentLanguage()` (Android locale); fallback handled in `GraphQLStore` via `buildVenueStub` | Same `fr`/else rule; fallback location differs (UI vs data layer). | `GetVenueUseCase(languageCode: String)` taking a plain language code; fallbacks in the mapper |
| D11 | **"First emission" loading + perf-trace wrapper** | `AgendaViewModel`, `SpeakersViewModel`, `SpeakerDetailsViewModel`, `AboutViewModel`: `asyncSequence(for:)` + `break` on first element, wrapped in Swift `PerformanceMonitoring.trackDataLoad` | Android VMs `collect` forever inside `performanceMonitoring.traceDataLoading(...)` | Behavioral difference: iOS takes first emission only (no live updates from Apollo `CacheAndNetwork`), Android keeps collecting. Trace names duplicated as string constants (`Performance/PerformanceMonitoring.swift` L24-34 "matching Android"). | Keep first-emission policy in the iOS VM (parity); share trace-name constants only |

### 1.2 Duplication that is NOT domain logic (leave native)

| Item | Why it stays native |
|------|---------------------|
| `PerformanceMonitoring.swift` (349 lines, native Firebase Performance), `FirebaseAnalyticsService.swift` (implements the shared `AnalyticsService` interface already), `RCValues` / `remoteConfig` | Platform SDK adapters. Fix is DI/seam (see section 3), not relocation. |
| Thin VMs: `SpeakerDetailsViewModel` (`getSpeaker` then `getSpeakerSessions`), `VenueViewModel`, session detail | Pure store passthrough; no logic to share. Only mapping (D10) moves. |
| Formatting (`DateFormatter`, `L10n`, `Complexity.text`, SwiftGen) | Locale/UI concerns; SwiftGen stays app-level. |
| Scroll position, glass UI, tab bar appearance | UI. |

---

## 2. Capability categories: table stakes / differentiators / anti-features

### 2.1 Shared domain layer (KMP)

**Table stakes**

| Capability | Why expected | Complexity | Depends on |
|------------|--------------|------------|------------|
| Use cases in a KMP module for D3, D4, D7, D8, D9, D10 (pure functions over `core:model` types) | The milestone's stated goal; these are pure and trivially testable in `commonTest` | Low-Med each | `core:model` (`Session`, `Agenda`, `SessionType`), `core:testing` fixtures (`SessionFixtures`) |
| Shared `SessionFilter` model moved out of `feature/agenda` (D2) | Both platforms need one type; today it lives only in an Android feature module | Low | Android `feature:agenda` must switch import; `@Serializable` already present (kotlinx-serialization on classpath) |
| Shared `BookmarksStore` iOS actual (D1) | Without it iOS cannot share favorites; today `dataPlatformModule` is empty on iOS | Med | `core:data` iosMain, Koin `dataPlatformModule` `expect/actual`, `IosKoinTest` (graph must still verify) |
| Shared `SessionFiltersRepository` with iOS actual (D2) | Removes `UserDefaults` filter logic from the iOS VM | Med | D1 pattern, `SessionFiltersServiceImpl` (Android) refactored behind a commonMain interface |
| Legacy-data-compatible persistence on iOS | "No favorite lost" is a hard requirement | Med | See migration below |
| Store contract unchanged (`DevFestNantesStore` + `GraphQLStore` + `DevFestNantesStoreMocked` in the same change) | AGENTS.md rule; any new store method must land in both | Low | Existing `DevFestNantesStoreContractTest` |
| iOS-friendly API surface: `@NativeCoroutines` on every Flow/suspend exposed; no Koin types in `shared.h` | Existing convention (KMP-NativeCoroutines already in SPM, swift-names-gate.sh) | Low | `swift-names-gate.sh`, `@HiddenFromObjC` on Koin modules |
| Existing Android behavior preserved: Android VMs consume the same use cases | The shared layer is only valid if Android is refactored onto it (otherwise it is a third copy) | Med | `AgendaViewModel`, `SpeakersViewModel`, `PartnersViewModel` and their v1.0 unit tests as safety net |

Favorites/filters migration (table stakes, Med-High risk):
- Lowest-risk option: the iOS `BookmarksStore` actual reads and writes the **existing** `UserDefaults` key `"Favorites"` as `[String]` through `NSUserDefaults`. No data migration needed, and rollback is free.
- Filters: iOS actual reads the legacy `Codable` JSON in `"AgendaSessionFilters"` (lowercase `type`) and writes the same format. If the format is changed to the Kotlin `@Serializable` shape, a one-time idempotent migrator (read legacy key -> write new -> keep legacy for one release) is required, with a test using a captured legacy payload.
- Never delete the legacy key in the same release that introduces the new one.

**Differentiators** (value, not required)

| Capability | Value | Complexity | Notes |
|------------|-------|------------|-------|
| Reactive combined use case `ObserveAgendaUseCase` (sessions + filters + bookmarks -> grouped, filtered sections as a `Flow`) | Single source of truth; kills the dual iOS `AgendaViewModel` instances; Android and iOS VMs become thin | Med-High | Must preserve iOS "first emission" load policy at the VM edge. Highest-leverage, highest-risk item. |
| Injectable `Clock`/`TimeZone` in the use cases | Deterministic tests for day selection (D5) around midnight and DST | Low | `kotlin.time.Clock`; keep `kotlinx-datetime` on the `0.8.0-0.6.x-compat` pin (AGENTS.md) |
| `InitialDayUseCase` (D5) | Removes three hardcoded date copies on iOS | Low | Dates still live in `Agenda.DAY_ONE_ISO`; edition change stays a one-file edit |
| Shared perf trace-name constants | One definition of "agenda_load" etc. | Low | Optional |
| `FeedbackFlags`-style seam for OpenFeedback enablement on iOS | Removes the `RCValues`/global `remoteConfig` singleton use in views | Low-Med | Android already has `FeedbackFlags.kt` (feature/session-detail); iOS implements it in Swift and injects |

**Anti-features** (do not build)

| Anti-feature | Why avoid | Instead |
|--------------|-----------|---------|
| Shared ViewModels (androidx.lifecycle KMP, SKIE/ViewModel bridging) | Explicitly out of scope in PROJECT.md; extra interop risk with Swift 6 | Native VMs on each platform over shared use cases |
| Moving scroll-to-current-slot (D6) into KMP "to unify" | The two platforms differ today; unifying changes behavior | Keep per platform, document divergence in tech debt |
| Fixing latent bugs while porting (room id vs name, timezone sensitivity, iOS first-emission-only, Android partner buckets) | Strict parity; PROJECT.md says log, do not fix | Characterization tests that pin current behavior; note in deferred items |
| Re-architecting `DevFestNantesStore` into per-entity repositories | Large API churn across Android + iOS for no parity benefit; an interface split would break `GraphQLStore`/`Mocked`/`FakeDevFestNantesStore` triplication | Keep the Store as data facade; add use cases on top |
| Using a heavyweight persistence lib (SQLDelight/Room KMP) for favorites | Overkill for a string set | NSUserDefaults actual (or multiplatform-settings only if it demonstrably reads the legacy key) |
| Domain models re-mapped to Swift structs by hand (`AgendaContent.Session` style) beyond what the UI needs | The current `AgendaContent` mirror is itself a duplication source | Let use cases return display-ready shared types; iOS adds only formatting |

### 2.2 iOS modularization (local SPM packages)

**Table stakes**

| Capability | Why expected | Complexity | Depends on |
|------------|--------------|------------|------------|
| Local SPM packages per feature (Agenda, Speakers, Venue, About) + core packages (DesignSystem, Core/DI) | Stated milestone goal; mirrors Android feature modules | High (first one), Med (each next) | Xcode project restructure; `SwiftGen` outputs (`Strings.swift`, `Assets.swift`, `L10n`, `Asset`) are used by every view and must live in a package both can import (DesignSystem or Resources) |
| Feature packages do not depend on each other (same rule as Android) | Architecture rule already in AGENTS.md | Low | Cross-feature navigation passed as closures to root (AgendaView -> Speaker detail currently via `PushLink`) |
| Kotlin `shared` framework consumable from every package | Features import `shared` types (`Session`, `Speaker`) | **High risk** | The Kotlin docs path for local SPM uses `embedAndSignAppleFrameworkForXcode` as a scheme pre-action (Kotlin 2.0+); a package that `import shared` must find the framework in search paths. A prebuilt local XCFramework (`binaryTarget`) is the alternative (needs `assemble...XCFramework`, slower CI). **Spike this first.** |
| App target reduced to a shell: `@main`, Firebase configure, composition root, `ContentView` tabs | Milestone goal | Med | iOSApp.swift (39 lines), `ContentView.swift` |
| Package-level test targets | Needed for Swift Testing per VM | Low-Med | Local packages support `testTarget`; run with `xcodebuild test` / `swift test` per package |
| Firebase and KMP-NativeCoroutines (SPM) declared once, per-package dependencies explicit | Avoid duplicate-symbol / version drift | Med | Existing SPM refs (firebase-ios-sdk 11.14+, KMP-NativeCoroutines) currently on the app target only |

**Differentiators**

| Capability | Value | Complexity | Notes |
|------------|-------|------------|-------|
| Previews/test-only fake store package (Swift fakes implementing `DevFestNantesStore`) | Fast VM tests without Koin; replaces stale `MockDevFestNantesStore.swift` | Low-Med | Existing `MockDevFestNantesStore.swift` is a starting point |
| SwiftGen run as a build tool plugin or committed output | Packages build without a shell phase | Med | Today generated files are cached in CI by key; `Strings.swift`/`Assets.swift` are in the tree |
| Scheme per feature package for isolated iteration | Dev speed | Low | Optional |

**Anti-features**

| Anti-feature | Why avoid | Instead |
|--------------|-----------|---------|
| One package per tiny view or per `Common` widget | Package overhead exceeds benefit at ~3.8k Swift lines | Feature packages + one DesignSystem + one Core |
| Moving the app to Tuist/XcodeGen/Bazel | New toolchain, CI risk; no parity benefit | Plain Xcode project + local packages |
| Splitting the project into a Swift Package workspace with remote packages | Not needed; extra release management | Local only |
| CocoaPods for KMP framework | AGENTS.md prefers SPM; project already direct-integrated | Direct integration |
| Making `shared` a binary Swift package published remotely | Release pipeline with no consumer | Local build |

### 2.3 iOS DI (explicit composition root)

**Table stakes**

| Capability | Why expected | Complexity | Depends on |
|------------|--------------|------------|------------|
| Composition root in the app target (e.g. `AppDependencies` built in `iOSApp.init`) that owns `DevFestNantesStore`, `AnalyticsService`, performance monitor, remote-config reader | Milestone goal; replaces `BaseViewModel` default arg `IosDependencies.shared.store()` | Med | `IosDependencies.kt` (`start()`, `store()`), `initKoin()` stays the **single Kotlin composition root** |
| Constructor-injected ViewModels (protocol/closure params, no default arguments that hit globals) | Testability; removes service locator | Med | Every VM today derives from `BaseViewModel` (`ContentView` even creates an unused `@StateObject BaseViewModel`) |
| Remove singletons `RCValues.sharedInstance`, `PerformanceMonitoring.shared`, `FirebaseAnalyticsService.shared`, global `var remoteConfig` from VM/view call sites | Milestone goal | Med | `AgendaView` calls `RCValues.sharedInstance.fetchCloudValues()` and `FirebaseAnalyticsService.shared.pageEvent(...)` directly; `PerformanceMonitoring` uses static state for startup trace |
| Dependencies passed through SwiftUI `Environment` or init params from the shell into feature package entry views | The only pattern that works across packages | Med | Feature packages expose `AgendaScreen(dependencies:)`-style entry points (analogue of Android `*Route`) |
| Protocols for Firebase-backed services (perf, remote config, analytics) | Allows fakes in Swift Testing | Low-Med | `FirebaseAnalyticsService` already conforms to shared `AnalyticsService`; perf/RC need Swift protocols |
| One Koin start, still in `iOSApp.init` before first view | Existing guarantee (`IosDependencies.start()` is idempotent) | Low | Keep `IosDependencies.store()` self-start for previews until previews migrate |

**Differentiators**

| Capability | Value | Complexity | Notes |
|------------|-------|------------|-------|
| Expose use cases from Kotlin via a small `IosDependencies`-like facade (`IosUseCases`) consumed **only** by the composition root | Keeps Koin invisible to Swift | Low-Med | `swift-names-gate.sh` (member sets + collisions) must keep passing |
| Preview-friendly `AppDependencies.preview` using shared `FakeDevFestNantesStore`-equivalent | Reliable SwiftUI previews | Med | `core/testing` is KMP; either export it to iOS or write Swift fakes |
| Boot-order assertion (Firebase configure -> consent -> perf trace), mirroring Android `KoinStartupTest` | Preserves consent-before-performance contract (T-04-19) | Low | Android contract documented in PROJECT.md decisions |

**Anti-features**

| Anti-feature | Why avoid | Instead |
|--------------|-----------|---------|
| Third-party Swift DI container (Factory, Swinject, swift-dependencies) | One more dependency/learning surface for ~6 VMs; Koin already is the real graph | Hand-written composition root |
| Exposing Koin (`KoinComponent`, `get()`) to Swift | Would reintroduce service locator and break `shared.h` naming rule | Facade returning plain Kotlin interfaces |
| `@Environment` as a service locator inside VMs | Same anti-pattern, just hidden | Resolve at the view/entry layer, pass to VM init |
| Global `@MainActor` singletons "to satisfy Swift 6" | Hides the problem | Inject and isolate |

### 2.4 Observation / state management

**Table stakes**

| Capability | Why expected | Complexity | Depends on |
|------------|--------------|------------|------------|
| iOS 17 deployment target on **all** targets | Required for `@Observable`; currently mixed 15.0 (L701) / 17.0 (L779) in `project.pbxproj` | Low | `AgendaView` uses `#available(iOS 26.0, *)` glass APIs and `PushLink`; those must keep compiling. Drop-iOS-15/16 support is a user-facing support-matrix decision; confirm with maintainer. |
| `@Observable` + `@MainActor` view models replacing `ObservableObject`/`@Published` | Milestone goal | Med | 23 files import Combine/ObservableObject/@Published/@StateObject (list in section 4) |
| Property wrapper migration in views: `@ObservedObject` -> plain property or `@Bindable`; `@StateObject` -> `@State` | Observation semantic differences (no `objectWillChange`; `@State` owns lifecycle) | Med | `LoadingView(isShowing: $viewModel.isLoading)` needs `@Bindable` |
| Remove `objectWillChange.send()` hacks and the dual `AgendaViewModel()` instances (`AgendaView`, `AgendaDetailView`, each filter menu takes `viewModel`) | Observation makes the hack unnecessary; shared favorites via `BookmarksStore` Flow solves cross-view sync | Med | D1 |
| Remove `import Combine` everywhere (including `iOSApp.swift` and `KMPNativeCoroutinesCombine` product) | Milestone goal | Low | Drop SPM product `KMPNativeCoroutinesCombine`; keep `KMPNativeCoroutinesAsync` |
| Consume Kotlin Flows via `asyncSequence(for:)` in a `Task` tied to view lifetime (`.task {}`) with cancellation | async/await replaces Combine; cancellation matters (`observeRooms` currently spawns an unstructured `Task` inside `async` and never cancels) | Med | KMP-NativeCoroutines Async |
| Explicit view state enum or flags (`isLoading`) preserved exactly | Parity; v1.0 left Android error state unfixed on purpose | Low | Do not add error UI |

**Differentiators**

| Capability | Value | Complexity | Notes |
|------------|-------|------------|-------|
| Single `ViewState`-style struct per VM instead of several independent properties | Fewer invalidations, simpler tests | Low-Med | Optional |
| `@ObservationIgnored` for non-UI dependencies | Prevents spurious invalidation and Sendable noise | Low | Needed with constructor-injected services |
| Observation-backed shared bookmarks view (`@Observable BookmarksModel` driven by the Flow) injected via environment | One favorites truth for Agenda + Detail | Med | Depends on D1 |

**Anti-features**

| Anti-feature | Why avoid | Instead |
|--------------|-----------|---------|
| Keeping Combine "for the Kotlin Flow bridge" | Contradicts milestone; NativeCoroutines Async is enough | `asyncSequence(for:)` |
| TCA / Redux frameworks | Heavy dependency, architectural rewrite, contradicts "native idiomatic VMs" | Plain `@Observable` VMs |
| Back-deploying Observation (Perception library) to iOS 16 | The milestone sets iOS 17 min | Raise target |
| `@Published` kept next to `@Observable` "temporarily" | Double notifications and compile errors under strict concurrency | Migrate VM by VM, finish each one |

### 2.5 Concurrency (Swift 6 strict)

**Table stakes**

| Capability | Why expected | Complexity | Depends on |
|------------|--------------|------------|------------|
| `SWIFT_VERSION = 6` (currently `5.0`, `project.pbxproj` L792) with complete checking, zero warnings | Milestone goal | High | Sequenced last, after Observation; can be enabled **per package** (Swift tools 6 language mode) which allows incremental adoption |
| `@MainActor` on all ViewModels (today only some: `SpeakersViewModel`, `VenueViewModel`, `AboutViewModel`, `BaseViewModel` are; `AgendaViewModel`, `SpeakerDetailsViewModel` rely on `DispatchQueue.main.async`) | Required for `@Observable` UI state | Med | Replace `DispatchQueue.main.async` with direct mutation inside `@MainActor` |
| Sendable story for Kotlin types crossing isolation (`Session`, `Speaker`, `Room`, `Partner`...) | Kotlin/Native classes are not `Sendable`; strict mode flags them | **High risk** | Options: `@preconcurrency import shared`, `@unchecked Sendable` extensions for immutable data classes, or hand-mapped Swift value types at the boundary. Needs a spike. Community reports (Kotlin Slack, SKIE discussion #48) confirm friction; MEDIUM confidence, no authoritative KMP-NativeCoroutines Swift 6 statement found. |
| Replace `DispatchQueue.main.async` closures, unstructured `Task {}` inside `async` functions, and `Date()` fallbacks in time code with structured tasks | Strict concurrency correctness; the unstructured `Task` in `observeRooms`/`observeVenue` can outlive the view | Med | VMs |
| Remove mutable globals: `var remoteConfig` (iOSApp.swift L14), `PerformanceMonitoring.appStartupTrace` static var, `RCValues.sharedInstance` | Strict mode errors on global mutable state | Med | DI capability |
| `DevFestLogger`, `FirebaseAnalyticsService` made `Sendable` (they are final-like stateless) | Used from tasks and closures | Low | `Analytics/DevFestLogger.swift` (36 lines) |

**Differentiators**

| Capability | Value | Complexity | Notes |
|------------|-------|------------|-------|
| Default main-actor isolation (Swift 6.2 `-default-isolation MainActor`) for UI packages | Less annotation noise | Low | Requires Xcode 26 toolchain; CI "latest installed Xcode" step already selects the newest |
| Keep Kotlin-side thread confinement explicit (all `StateFlow`s on `Dispatchers.Default`/Main as today) and document it | Avoid races on shared repositories | Low | Kotlin/Native new memory model already in force |

**Anti-features**

| Anti-feature | Why avoid | Instead |
|--------------|-----------|---------|
| Blanket `@unchecked Sendable` or `nonisolated(unsafe)` to silence errors | Defeats purpose; hides real races | Isolate on `@MainActor`, convert at boundary, narrow `@preconcurrency` |
| Disabling strict checking per file (`-swift-version 5` islands) beyond a documented, ticketed exception list | Milestone requires "without warning" | Time-boxed exception list only if Kotlin-generated code forces it |
| Switching Flow interop library (SKIE) mid-milestone | Kotlin 2.4.20 support lagged (SKIE 0.10.15 first with 2.4.20 per a third-party PR); mixing interop libs is discouraged by SKIE itself | Stay on KMP-NativeCoroutines (README targets Kotlin 2.4.20) |
| Rewriting analytics/perf to a Kotlin implementation to avoid Swift concurrency issues | Firebase iOS SDKs are native; no benefit | Swift adapters behind protocols |

### 2.6 Testing and CI

**Table stakes**

| Capability | Why expected | Complexity | Depends on |
|------------|--------------|------------|------------|
| `commonTest` tests for each shared use case, run on JVM and `iosSimulatorArm64Test` | Milestone goal; CI already runs `:shared/:core:*:iosSimulatorArm64Test` | Low-Med each | `core:testing` fakes (`FakeDevFestNantesStore`, `FakeBookmarksStore`), `SessionFixtures`. New use-case module must be added to the CI task list in `.github/workflows/ios.yml`. |
| Golden-master / characterization tests copied from the Android filter matrix and grouping behavior, executed against the shared use cases | Proves parity before deleting Swift and Android copies | Med | v1.0 Phase 5 tests in `feature/agenda` |
| Legacy-data migration tests with captured UserDefaults payloads (favorites array, filters JSON) | "No favorite lost" | Low-Med | D1/D2 |
| Swift Testing (`import Testing`, `@Test`, `#expect`) for each iOS VM, with Swift fakes of the store | Milestone goal; replaces XCTest | Med | Constructor DI; Xcode 16+ |
| One SwiftUI smoke test per feature (render the entry view with fakes, assert it builds and shows loaded content) | Milestone goal; mirrors Android Compose smoke tests | Med | Feature packages; may use ViewInspector-free approach (host in `UIHostingController` and assert no crash / accessibility tree) |
| iOS tests executed in GitHub Actions | Today CI only runs `xcodebuild build`; the single test class is not run | Med | `ios.yml` has simulator-resolution step already (`steps.sim.outputs.device_name`); add `xcodebuild test` (or `swift test` per package) with `CODE_SIGNING_ALLOWED=NO` and the mock `GoogleService-Info.plist` already created in CI |
| Repair or delete the stale `DevFest NantesTests` target | It references a non-existent `DevFestViewModel` (grep finds no such class), so it cannot compile; the scheme still lists it as a testable | Low | `iosApp.xcscheme` TestAction; do this first or the CI test step fails immediately |
| Test target wired into the scheme, shared scheme committed | `xcodebuild test -scheme` needs it | Low | `iosApp.xcodeproj/xcshareddata/xcschemes/iosApp.xcscheme` exists |

**Differentiators**

| Capability | Value | Complexity | Notes |
|------------|-------|------------|-------|
| Swift test results published as CI artifact (xcresult) and Kover merged view for shared code | Visibility | Low-Med | Kover is report-only at 30.57% (v1.0 decision: no strict %) |
| Cache SPM + DerivedData keyed on `Package.resolved` and package manifests | Faster CI | Low | Existing DerivedData cache keyed on `project.pbxproj`; add `Package.resolved` |
| Parity test in Swift that decodes legacy `UserDefaults` payload through the shared repository | End-to-end proof on simulator | Med | Needs simulator run in CI |
| Snapshot tests per screen | Visual regression | High | Depends on third-party lib; skip unless asked |

**Anti-features**

| Anti-feature | Why avoid | Instead |
|--------------|-----------|---------|
| UI test suite (XCUITest) as a gate | Slow and flaky on CI; Firebase mock plist, network | Smoke tests via hosting controller |
| Mocking libraries in Swift/Kotlin | AGENTS.md: hand-written fakes only, no mocking library | Hand-written fakes |
| A coverage % gate | v1.0 decision: qualitative coverage | Report only |
| Live-network tests in iOS CI | Flaky | Apollo MockServer-style / fakes, as v1.0 did |

---

## 3. Feature dependencies

```
Fix/remove stale DevFest NantesTests target ──► CI iOS test step
SessionFilter model in core:model (D2) ──► FilterSessionsUseCase (D3) ──► Android VM refactor
                                      └──► SessionFiltersRepository ──► iOS actual + legacy reader
BookmarksStore iOS actual (D1) ──► ToggleBookmarkUseCase ──► iOS VMs drop UserDefaults
                               └──► single favorites truth ──► remove dual AgendaViewModel instances
Shared use cases (D3,D4,D5,D7,D8,D9,D10) ──► commonTest (JVM + iOS sim) ──► Swift VMs consume them
Spike: Kotlin `shared` framework visible from local SPM packages ──► ALL SPM modularization
Spike: Sendable/@preconcurrency strategy for Kotlin types ──► Swift 6 strict
iOS 17 target on all targets ──► @Observable ──► Combine removal
Constructor DI + protocols for perf/RC/analytics ──► Swift Testing VM tests ──► smoke tests
Composition root ──► feature package entry points (AgendaScreen(dependencies:) ...)
@Observable + @MainActor VMs ──► Swift 6 strict language mode (last)
All of the above ──► iOS tests in GitHub Actions (xcodebuild test)
```

Concrete existing-code coupling to respect:
- `shared.h` must expose no Koin names (`swift-names-gate.sh`); new Kotlin types exported to iOS must pass the gate (member sets + collisions).
- `shared` is an umbrella framework exporting `core:model`, `core:data`, `core:analytics`; a new `core:domain` module (if created) must be added to that export list and to `SharedFrameworkPlaceholder.kt`'s framework config. Do not delete the placeholder.
- Koin: `allowOverride(false)`; new bindings in one module only; `verify()` (JVM) and `IosKoinTest` must keep passing.
- Dependency direction: new shared use-case code sits between `core:model/core:data` and features; Android `feature:*` and iOS packages consume it; features never depend on each other.

---

## 4. Scope facts for planning (iOS surface to migrate)

- ~3,761 lines Swift across 45 files; one app target, two deployment targets (15.0 and 17.0), `SWIFT_VERSION = 5.0`.
- SPM today: `firebase-ios-sdk` (min 11.14.0; Analytics-without-AdId, Crashlytics, Performance, RemoteConfig), `KMP-NativeCoroutines` (Async + Combine products).
- Files using Combine/ObservableObject/@Published/@StateObject/@ObservedObject (23): all `*ViewModel.swift` (Agenda, Speakers, SpeakerDetails, Venue, About, Base), `AgendaView`, `AgendaDetailView`, `AgendaCellView`, `AgendaNavigationBarItems`, `Favorites/Language/Complexity/Room/SessionType FilterMenu`, `ClearFiltersButton`, `SpeakersView`, `SpeakerDetailsView`, `VenueView`, `AboutView`, `ContentView`, `iOSApp`, `PerformanceMonitoring` (it is an `ObservableObject` for no reason).
- Only DI-global call sites to remove: `BaseViewModel.init(store: = IosDependencies.shared.store())`, `RCValues.sharedInstance`, `PerformanceMonitoring.shared`, `FirebaseAnalyticsService.shared`, global `remoteConfig`.
- Tests: one stale XCTest class (does not compile against current code); CI runs `xcodebuild build` plus Gradle `iosSimulatorArm64Test` for `shared`, `core:model`, `core:data`, `core:testing`.

---

## 5. MVP recommendation (ordering inside the milestone)

Prioritize (each step keeps the app shippable):

1. **Safety net and unblockers**: delete/repair the stale test target and add `xcodebuild test` to `ios.yml`; characterization tests for D3/D4/D2-format against current behavior. (Low)
2. **Shared domain layer, pure parts first**: SessionFilter model move, `FilterSessionsUseCase`, grouping, `isService`, speakers/partners/venue use cases, with `commonTest` on JVM + iOS sim; refactor Android VMs to call them (v1.0 tests prove no regression). (Med)
3. **Shared persistence**: `BookmarksStore` iOS actual reading the legacy `"Favorites"` key; `SessionFiltersRepository` with legacy-format reader; iOS VMs switch; migration tests. (Med-High)
4. **iOS 17 + `@Observable` + Combine removal + constructor DI/composition root** (one VM at a time). (Med-High)
5. **SPM local packages**: core first (DesignSystem/Resources, DI), then Agenda, then the rest; resolve the `shared` framework visibility spike before starting. (High)
6. **Swift 6 strict** per package, last, once types and isolation are settled. (High)
7. **Swift Testing per VM + smoke tests per feature** can land incrementally after step 4 and gate each package.

Defer / out of scope: Reactive `ObserveAgendaUseCase` mega-use-case (differentiator, do only if step 3 leaves obvious duplication), unified scroll-to-now behavior (D6), snapshot tests, shared ViewModels, any bug fix found on the way (log to deferred items).

---

## 6. Open questions for later phases

- Whether to drop iOS 15/16 support is a product decision (user-visible support matrix), although the milestone states iOS 17 minimum.
- Timezone rule for D4/D5: iOS uses device tz, Android day buckets use UTC, listing uses device tz. A shared function must pick the device tz for display grouping (`TimeZone.currentSystemDefault()`) and the characterization tests must run under at least two timezones.
- Whether `PartnerCategory` ordering (D9) should be preserved per platform (iOS: all categories; Android: five fixed) or normalized; any normalization is a visible change.
- Exact Swift 6 strategy for Kotlin-generated types (preconcurrency vs Sendable mapping) and KMP-NativeCoroutines release that supports it: needs a spike with the real Kotlin 2.4.20 framework.
- New `core:domain` module vs placing use cases in `core:data`: module layout decision for the architecture file (affects `shared` export list and the one-way dependency rule).

## Sources

- Repository source read for the duplication inventory (HIGH): `iosApp/iosApp/Agenda/AgendaViewModel.swift`, `AgendaView.swift`, `AgendaDetailView.swift`, `Model/SessionFilter.swift`, `Model/Data/*.swift`, `UI/**/**ViewModel.swift`, `iOSApp.swift`, `Resources/RCValues.swift`, `Performance/PerformanceMonitoring.swift`, `Analytics/FirebaseAnalyticsService.swift`; `feature/agenda/.../AgendaViewModel.kt`, `AgendaColumn.kt`, `Agenda.kt`, `services/SessionFiltersService.kt`, `utils/*`; `feature/speakers`, `feature/about`, `feature/venue` ViewModels; `core/data/.../BookmarksStore*.kt`, `DataPlatformModule.ios.kt`, `GraphQLStore.kt`; `core/model/.../Agenda.kt`; `shared/.../IosDependencies.kt`; `.github/workflows/ios.yml`; `iosApp/iosApp.xcodeproj/project.pbxproj`.
- [Using Kotlin from local Swift packages (kotlinlang.org)](https://kotlinlang.org/docs/multiplatform/multiplatform-spm-local-integration.html) - official, HIGH: `embedAndSignAppleFrameworkForXcode` pre-action approach for local SPM (Kotlin 2.0+).
- [Kotlin Slack: local shared KMP with multiple SPM modules](https://slack-chats.kotlinlang.org/t/16295952/is-there-a-way-to-use-local-shared-kmp-in-xcode-that-is-conf) - community, MEDIUM: build-phase timing problem for SPM modules; XCFramework `binaryTarget` workaround.
- [rickclephas/KMP-NativeCoroutines (GitHub)](https://github.com/rickclephas/KMP-NativeCoroutines) - README lists Kotlin 2.4.20 support; no Swift 6 statement found, MEDIUM.
- [SKIE discussion #48: Sendable conformance](https://github.com/touchlab/SKIE/discussions/48) and [Kotlin Slack touchlab-tools thread on Swift 6 build failures](https://slack-chats.kotlinlang.org/t/33169631/hi-we-re-running-into-build-failures-when-enabling-swift-6-s) - community, MEDIUM-LOW: Sendable friction and `@preconcurrency import` workaround.
- [Swift 6 strict concurrency meets Kotlin coroutines in KMP (DEV Community)](https://dev.to/software_mvp-factory/swift-6-strict-concurrency-meets-kotlin-coroutines-in-kmp-148c) - third-party article, LOW: general friction patterns, not authoritative.
- [Replace KMP-NativeCoroutines with SKIE, PR #97](https://github.com/igorcferreira/MusicStreamSync/pull/97) - third-party, LOW: SKIE 0.10.15 first release for Kotlin 2.4.20.
