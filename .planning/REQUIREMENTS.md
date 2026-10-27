# Requirements: DevFest Nantes App Modernization — v1.1 iOS Architecture

**Defined:** 2026-10-23
**Core Value:** CI/CD must work again and the project must become maintainable again (modern build, modular architecture, decentralized DI, solid test coverage) without ever regressing the app's existing behavior for users.

## v1.1 Requirements

Modernize the iOS app to the level of the Android app: domain logic shared in KMP (native UIs kept), modular SwiftUI app on Observation + strict Swift 6, iOS tests and CI. Strict behavior parity, with a single accepted deviation: the order of rooms within a time slot on iOS aligns with Android (`RoomSortIndex`).

Research context: `.planning/research/SUMMARY.md` (and STACK/FEATURES/ARCHITECTURE/PITFALLS).

### SHARED (Shared KMP domain)

- [ ] **SHARED-01**: A KMP module `:core:domain` (`devfest.*` convention plugin, depending on `:core:model`/`:core:data`) is exported by `:shared`; its Koin `domainModule` is added to `sharedModules` and verified by `verify()` with no duplicate binding
- [ ] **SHARED-02**: `SessionFilter`/`SessionFilterType` live in `:core:model`; the session filtering rule (OR within a type, AND across types, favorites = membership) is a shared use case covered by the filter matrix in `commonTest` (JVM + iOS simulator)
- [ ] **SHARED-03**: Grouping the agenda by day and by time slot, with intra-slot sorting by `RoomSortIndex`, is a shared use case; the room-order deviation on iOS is documented
- [ ] **SHARED-04**: Session classification (service/navigable), the alphabetical speaker index and the venue language choice are shared where the semantics are already identical on both platforms; divergent behaviors (current time slot on scroll, partner categories, day time zone) stay per platform or go through an explicit parameter
- [ ] **SHARED-05**: `BookmarksStore` and `SessionFiltersStore` are common interfaces; the Android implementation keeps its SharedPreferences keys and format (`selected_sessions`, filters), the iOS implementation relies on `NSUserDefaults` with the existing `Favorites` and `AgendaSessionFilters` keys read and written in the legacy format
- [ ] **SHARED-06**: The affected Android ViewModels consume the shared use cases and `SessionFiltersService` is removed (Koin binding replaced in the same commit), with no regression (v1.0 tests green + agenda UAT)
- [ ] **SHARED-07**: Characterization tests (captured legacy payloads, grouping under at least two time zones, current iOS and Android behavior) exist before each move of logic

### IOSDATA (iOS on the shared logic)

- [ ] **IOSDATA-01**: The iOS app no longer filters, groups or persists favorites/filters in Swift; the duplicated Swift types (`SessionFilter`, `FilterType`…) are removed in favor of the exported Kotlin types
- [ ] **IOSDATA-02**: A user who updates from the v1.0 version gets back all their favorites and filters (tests on captured legacy payloads + in-place update UAT from a v1.0 build)
- [ ] **IOSDATA-03**: The iOS agenda continuously observes the shared combined stream (sessions + favorites + filters); the list, cells and detail share a single `AgendaViewModel` instance

### IOSUI (Modern SwiftUI)

- [ ] **IOSUI-01**: All iOS targets and packages have an iOS 17.0 deployment target
- [ ] **IOSUI-02**: All iOS ViewModels are `@Observable @MainActor`, owned via `@State` without being recreated on re-render, with collection started in `.task` and cancelled when the view disappears (no more unstructured `Task {}`)
- [ ] **IOSUI-03**: Combine, `KMPNativeCoroutinesCombine`, `ObservableObject`/`@Published`, `objectWillChange.send()` and `DispatchQueue.main.async` are no longer used

### IOSDI (Explicit iOS DI)

- [ ] **IOSDI-01**: A Swift composition root (`AppDependencies.live()`) is built once at launch; the current initialization order (Firebase, consent, performance, Remote Config defaults) is preserved and covered by a test
- [ ] **IOSDI-02**: ViewModels receive their dependencies through the constructor via protocols; `BaseViewModel`, `RCValues.sharedInstance`, the global `remoteConfig`, `PerformanceMonitoring.shared` and `FirebaseAnalyticsService.shared` are removed
- [ ] **IOSDI-03**: `scripts/swift-di-gate.sh`, run in CI, forbids access to `IosDependencies` and to singletons outside the composition root

### IOSMOD (SPM modularization)

- [ ] **IOSMOD-01**: `shared` is produced by Gradle as a static XCFramework and consumed as an SPM `binaryTarget`; the app builds from a clean checkout locally and in CI, the `embedAndSignAppleFrameworkForXcode` phase is removed and the Release archive keeps dSYMs and Crashlytics
- [ ] **IOSMOD-02**: Local core packages/targets exist: Kotlin bridge (sole importer of `shared`), CoreServices (protocols), FirebaseServices, DesignSystem (SwiftGen output and resources via `Bundle.module`), CoreDI
- [ ] **IOSMOD-03**: Feature packages/targets Agenda (session detail included), Speakers, Venue and About exist, with no dependency between features and no direct import of the Kotlin framework; the app target is reduced to a shell

### CONC (Swift 6 concurrency)

- [ ] **CONC-01**: All packages and the app target compile in Swift 6 language mode with complete concurrency checking and zero warnings
- [ ] **CONC-02**: The Kotlin bridges (`asyncFunction`/`asyncSequence`, `@unchecked Sendable` conformances limited to immutable Kotlin types) are confined to the bridge package, enforced by a CI gate
- [x] **CONC-03**: KMP-NativeCoroutines is at the same version (1.0.6) on the Swift (SPM) and Gradle sides, verified by a CI gate

### IOSTEST (iOS tests & CI)

- [ ] **IOSTEST-01**: The obsolete test target (`DevFest NantesTests`) is replaced; each iOS ViewModel has Swift Testing tests on fakes
- [ ] **IOSTEST-02**: Each iOS feature has a SwiftUI smoke test (rendered via `UIHostingController`)
- [x] **IOSTEST-03**: iOS CI runs `xcodebuild test` via a test plan including the package tests, fails if no test is run, and its cache key includes the Xcode version
- [x] **IOSTEST-04**: `swift-names-gate.sh` is restored in `scripts/` and run in CI

### DOC (Documentation)

- [ ] **DOC-01**: `README.md`, `documentation/` (ARCHITECTURE, DEVELOPMENT, iOS dSYM build) and `AGENTS.md` describe the new iOS architecture and the associated commands

## Future Requirements

Deferred, tracked but outside the v1.1 roadmap.

- **FUT-01**: Unify across platforms the current time slot on scroll, partner categories and the day time zone rule (visible change)
- **FUT-02**: v1.0 debt: `speakersPage` (110 speakers), `RoomSortIndex` for `Tour Bretagne`/`Unconf`, `SocialItem`, Speaker page-view, ViewModels ERROR state, CI hardening WR-02/WR-03
- **FUT-03**: iOS Firebase upgrade to 12.x/13.x
- **FUT-04**: Running the tests on an iOS 17 runtime in CI
- **FUT-05**: Snapshot / XCUITest tests
- **FUT-06**: Hardcoded 2025 event dates replaced by a data source

## Out of Scope

| Feature | Reason |
|---------|--------|
| ViewModels shared in KMP | Explicit choice: only the domain layer is shared, idiomatic ViewModels per platform |
| Shared UI (Compose Multiplatform) | Native UI kept on Android (Compose) and iOS (SwiftUI) |
| SKIE / Swift Export | KMP-NativeCoroutines already in place and compatible; Swift Export in Alpha and incompatible with SPM; SKIE conflicts with `shared`/`.shared` |
| Swift DI library (Factory, swift-dependencies, Swinject) | Manual composition root is sufficient, with no global locator |
| New user features | Purely technical effort, strict parity |
| Fixing bugs found along the way | Strict parity: logged as debt, not fixed |

## Traceability

Which phases cover which requirements. Updated during roadmap creation.

| Requirement | Phase | Status |
|-------------|-------|--------|
| SHARED-01 | Phase 7 | Pending |
| SHARED-02 | Phase 7 | Pending |
| SHARED-03 | Phase 7 | Pending |
| SHARED-04 | Phase 7 | Pending |
| SHARED-05 | Phase 7 | Pending |
| SHARED-06 | Phase 7 | Pending |
| SHARED-07 | Phase 7 | Pending |
| IOSDATA-01 | Phase 9 | Pending |
| IOSDATA-02 | Phase 9 | Pending |
| IOSDATA-03 | Phase 9 | Pending |
| IOSUI-01 | Phase 8 | Pending |
| IOSUI-02 | Phase 8 | Pending |
| IOSUI-03 | Phase 8 | Pending |
| IOSDI-01 | Phase 10 | Pending |
| IOSDI-02 | Phase 10 | Pending |
| IOSDI-03 | Phase 10 | Pending |
| IOSMOD-01 | Phase 11 | Pending |
| IOSMOD-02 | Phase 11 | Pending |
| IOSMOD-03 | Phase 11 | Pending |
| CONC-01 | Phase 12 | Pending |
| CONC-02 | Phase 12 | Pending |
| CONC-03 | Phase 6 | Complete |
| IOSTEST-01 | Phase 8 | Pending |
| IOSTEST-02 | Phase 12 | Pending |
| IOSTEST-03 | Phase 6 | Complete |
| IOSTEST-04 | Phase 6 | Complete |
| DOC-01 | Phase 12 | Pending |

**Coverage:**
- v1.1 requirements: 27 total
- Mapped to phases: 27
- Unmapped: 0 ✓

**Traceability notes:**
- SHARED-05 ships in Phase 7 (common interfaces, Android implementation unchanged, iOS implementation on the legacy keys tested on simulator); its wiring into the iOS app and the update UAT belong to IOSDATA-01/02 (Phase 9).
- IOSTEST-01 is closed in Phase 8 (tests of each ViewModel); the obsolete `DevFest NantesTests` target is replaced as early as Phase 6, as a prerequisite of the IOSTEST-03 test lane.
- IOSTEST-03 ships in Phase 6; Phase 11 adds the package tests to the test plan (Phase 11 success criterion 4).
- IOSUI-01 covers the existing targets in Phase 8; the packages created in Phase 11 are born with `.iOS(.v17)` (Phase 11 success criterion 3).

---
*Requirements defined: 2026-10-23*
*Last updated: 2026-10-23 after roadmap creation (phases 6-12)*
