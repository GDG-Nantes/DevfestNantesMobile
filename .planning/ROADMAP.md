# Roadmap: DevFest Nantes App Modernization

## Milestones

- ✅ **v1.0 Modernization**: Phases 1-5 (shipped 2026-10-16). See [archive](milestones/v1.0-ROADMAP.md).
- 🚧 **v1.1 iOS Architecture**: Phases 6-12 (in progress)

## Overview

v1.1 brings the iOS app up to the level of the Android app without changing anything users see. The milestone starts with a real iOS test lane in CI and the Kotlin/Swift interop gates, so that every subsequent rework has a reliable signal. The duplicated business logic (filtering, agenda grouping, classification, bookmarks and filters) then moves into a KMP `:core:domain` module, adopted first by Android, whose v1.0 tests serve as the parity oracle. On iOS, the ViewModels move to Observation on an iOS 17 foundation, then the app switches to the shared logic while reusing the legacy `UserDefaults` keys (no bookmark or filter lost), before DI becomes an explicit composition root. The split into local SPM packages, the riskiest structural step, comes next. The milestone ends with the move to strict Swift 6, one smoke test per feature, and documentation. Every phase leaves CI green and the app shippable. A single visible deviation is accepted: on iOS, the order of rooms within a time slot follows `RoomSortIndex`, as on Android.

## Phases

**Phase Numbering:**
- Integer phases (6, 7, 8…): planned milestone work, numbering continues from v1.0
- Decimal phases (7.1, 7.2): urgent insertions (marked INSERTED)

Decimal phases slot in between their neighboring integer phases, in numeric order.

<details>
<summary>✅ v1.0 Modernization (Phases 1-5): SHIPPED 2026-10-16</summary>

- [x] Phase 1: CI Pipeline Fixed & Optimized (3/3 plans), completed 2026-09-17
- [x] Phase 2: Dependency & Build Tooling Upgrade (5/5 plans), completed 2026-09-19
- [x] Phase 3: Multi-Module Architecture Extraction (11/11 plans), completed 2026-09-27
- [x] Phase 4: Hilt to Koin DI Migration (7/7 plans), completed 2026-10-13
- [x] Phase 5: Test Coverage Retrofit (11/11 plans), completed 2026-10-16

Full details: [milestones/v1.0-ROADMAP.md](milestones/v1.0-ROADMAP.md). Requirements: [milestones/v1.0-REQUIREMENTS.md](milestones/v1.0-REQUIREMENTS.md). Audit: [milestones/v1.0-MILESTONE-AUDIT.md](milestones/v1.0-MILESTONE-AUDIT.md).

</details>

### 🚧 v1.1 iOS Architecture (In Progress)

**Milestone Goal:** Modernize the iOS app to the level of the Android app (business logic shared in KMP, modular SwiftUI app on Observation + strict Swift 6, real test coverage) without any change in user-visible behavior.

- [ ] **Phase 6: iOS Test Foundation and CI Gates** - iOS CI runs real Swift tests on every PR and the Kotlin/Swift interop gates are active
- [ ] **Phase 7: KMP Domain Layer and Android Adoption** - The agenda/filters/bookmarks logic lives in `:core:domain`, tested on JVM and iOS simulator, and Android uses it with no visible difference
- [ ] **Phase 8: Modern SwiftUI, iOS 17 and Observation** - iOS 17 everywhere, `@Observable @MainActor` ViewModels tested on fakes, Combine removed
- [ ] **Phase 9: iOS on Shared Logic and Legacy Persistence** - iOS delegates filtering, grouping and bookmarks/filters to the shared domain without losing v1.0 data
- [ ] **Phase 10: Explicit iOS DI** - `AppDependencies` composition root and constructor injection; singletons and service-locator removed
- [ ] **Phase 11: SPM Modularization** - `shared` as a static XCFramework, local core and feature packages, app target reduced to a shell
- [ ] **Phase 12: Strict Swift 6, Smoke Tests and Documentation** - Swift 6 with no warnings, Kotlin interop confined and checked, one smoke test per feature, documentation up to date

**v1.1 transverse rules:**
- Every phase leaves Android and iOS CI green and the app shippable on both platforms.
- Strict parity: bugs found along the way are logged as debt, not fixed. Only accepted visible deviation: on iOS, the order of rooms within a time slot follows `RoomSortIndex` (SHARED-03).
- Any documentation invalidated by a phase is updated in the same change (`AGENTS.md` rule); Phase 12 checks all of it (DOC-01).
- Research trade-offs adopted (`research/SUMMARY.md`): legacy `UserDefaults` keys reused without a migrator, a single SPM manifest, features never import the Kotlin framework, `AppDependencies` lives in CoreDI, new packages are born in Swift 6 and moved code switches over package by package.

## Phase Details

### Phase 6: iOS Test Foundation and CI Gates

**Goal**: Every PR actually runs Swift tests on iOS CI and the Kotlin/Swift interop gates are active, so that every subsequent iOS rework has a reliable signal
**Depends on**: Phase 5 (v1.0 shipped)
**Requirements**: IOSTEST-03, IOSTEST-04, CONC-03
**Success Criteria** (what must be TRUE):
  1. On every PR, iOS CI runs `xcodebuild` in build-for-testing then test-without-building through a `.xctestplan` test plan, and a working Swift Testing target (replacing `DevFest NantesTests`, which references a nonexistent `DevFestViewModel`) runs at least one real test green
  2. The iOS job fails when the `.xcresult` counts zero executed tests, and the iOS cache key includes the Xcode version
  3. `scripts/swift-names-gate.sh` is restored and runs in CI: a change that modifies the Swift name of an exported Kotlin type fails the job
  4. KMP-NativeCoroutines is at 1.0.6 on the SPM side (instead of 1.0.0-ALPHA-45) as on the Gradle side, a CI gate fails if the two versions diverge, and the iOS app behaves as in v1.0

**Plans**: 5/6 plans executed
Plans:
**Wave 1**
- [x] 06-01-PLAN.md — App-hosted Swift Testing target `DevFestNantesTests`, `.xctestplan`, XCTest guard in `iOSApp`, first `SessionFilter` test (tracer), then removal of the old target (separate commit)

**Wave 2** *(blocked on Wave 1 completion)*
- [x] 06-02-PLAN.md — Full `SessionFilter` suite (legacy format), `AgendaViewModel.parseDate` extraction (dedicated commit) and its tests, iOS test docs

**Wave 3** *(blocked on Wave 2 completion)*
- [x] 06-03-PLAN.md — iOS CI lane: build-for-testing / test-without-building, failure on 0 tests (`xcresult-assert-tests.sh`), Xcode-aware cache, simulator by UDID

**Wave 4** *(blocked on Wave 3 completion)*
- [x] 06-04-PLAN.md — `swift-names-gate.sh` restored in `scripts/` (baselines in `scripts/swift-names/`), check + selftest in CI

**Wave 5** *(blocked on Wave 4 completion)*
- [x] 06-05-PLAN.md — KMP-NativeCoroutines SPM at 1.0.6 and `kmp-nc-version-gate.sh` in the same commit, gate docs, AGENTS.md Android CI commands green

**Wave 6** *(blocked on Wave 5 completion)*
- [ ] 06-06-PLAN.md — Manual v1.0 parity smoke on simulator after the bump (blocking human checkpoint)

**Research flag**: standard pattern, no spike

### Phase 7: KMP Domain Layer and Android Adoption

**Goal**: The agenda business logic (filtering, grouping, classification, bookmarks and filters) lives in a KMP `:core:domain` module tested on JVM and iOS simulator, and Android already uses it without any visible difference
**Depends on**: Phase 6 (the `swift-names-gate.sh` gate in CI covers the new exports)
**Requirements**: SHARED-01, SHARED-02, SHARED-03, SHARED-04, SHARED-05, SHARED-06, SHARED-07
**Success Criteria** (what must be TRUE):
  1. `:core:domain` (`devfest.*` convention plugin) is exported by `:shared`; its `domainModule` is part of `sharedModules`, `verify()` passes with no duplicate binding, `swift-names-gate.sh` passes on the new exports, and the iOS app compiles and behaves as before
  2. Characterization tests (v1.0 filter matrix, grouping under at least two time zones, captured legacy payloads, current iOS and Android behaviors) are committed before each logic move; the shared use cases (OR filtering within a type / AND across types / bookmarks by membership, day/time-slot grouping with `RoomSortIndex` sorting, service/navigable classification, A-Z speaker index, venue language) pass them in `commonTest` on JVM and iOS simulator
  3. Divergent behaviors (current time slot on scroll, partner categories, day time zone) stay per platform or go through an explicit parameter, and the iOS room-order deviation is documented
  4. `BookmarksStore` and `SessionFiltersStore` are common interfaces: the v1.0 Robolectric tests that pin `selected_sessions` and the Android filter format pass unchanged, and the iOS implementation reads and writes `Favorites` and `AgendaSessionFilters` in the legacy format (tests on captured payloads, on simulator)
  5. On Android, the agenda, filters, bookmarks, speakers and venue behave exactly as in v1.0 (v1.0 tests green + agenda UAT); `SessionFiltersService` is gone and its Koin binding was replaced in the same commit

**Plans**: TBD
**Research flag**: characterization to prepare: a real API snapshot (110 speakers, room ids, check `room.id == room.name`) in addition to the `core/model` stubs, and the real shape of the `Favorites`/`AgendaSessionFilters` payloads captured from a v1.0 install or a Swift `Codable` fixture

### Phase 8: Modern SwiftUI, iOS 17 and Observation

**Goal**: The iOS ViewModels move to the Observation model on an iOS 17 foundation, without Combine, with collection tied to the view lifecycle and unit tests on fakes, with no visible change
**Depends on**: Phase 6 (no technical dependency on Phase 7, executed after it by numeric order)
**Requirements**: IOSUI-01, IOSUI-02, IOSUI-03, IOSTEST-01
**Success Criteria** (what must be TRUE):
  1. All existing iOS targets declare an iOS 17.0 deployment target (no more 15.0/17.0 mix) and the app builds and runs on simulator
  2. The Agenda, Speakers, SpeakerDetails, Venue and About ViewModels are `@Observable @MainActor` and owned via `@State`: a re-render does not recreate the ViewModel, collection starts in `.task` and stops when the view disappears (no unstructured `Task {}`)
  3. The iOS sources no longer contain `import Combine`, `KMPNativeCoroutinesCombine`, `ObservableObject`, `@Published`, `objectWillChange.send()` or `DispatchQueue.main.async`, and the Combine SPM product is removed from the project
  4. Each iOS ViewModel has Swift Testing tests on fakes (narrow ports per ViewModel), run in the Phase 6 CI lane
  5. UAT: agenda, filters, bookmarks, session detail, speakers, venue and about behave as in v1.0

**Plans**: TBD
**Research flag**: replay the KMP-NativeCoroutines async bridge (`asyncSequence`/`asyncFunction`) on the CI toolchain (Xcode 26.6 / Swift 6.3.3), not only on the local Swift 6.4 experiment
**UI hint**: yes

### Phase 9: iOS on Shared Logic and Legacy Persistence

**Goal**: The iOS app delegates filtering, grouping and persistence of bookmarks and filters to the shared domain layer, without any user losing their bookmarks or filters on update
**Depends on**: Phase 7, Phase 8
**Requirements**: IOSDATA-01, IOSDATA-02, IOSDATA-03
**Success Criteria** (what must be TRUE):
  1. A user who installs the new version over a v1.0 build (bookmarked sessions, active filters, app killed then relaunched) gets all their bookmarks and filters back; the in-place update UAT and the tests on captured legacy payloads confirm it
  2. The Swift sources no longer filter, group or persist bookmarks or filters; `SessionFilter`, `FilterType` and the other duplicated types are removed in favor of the exported Kotlin types, and speakers, venue and session classification go through the shared use cases
  3. The iOS agenda shows the same days (labels and time zone), time slots, sessions and filter results as in v1.0 (comparative UAT), with the sole documented exception of the room order within a time slot, which now follows `RoomSortIndex`
  4. The agenda continuously observes the shared combined flow (sessions + bookmarks + filters): adding or removing a bookmark or a filter from the list, a cell or the detail updates the agenda without relaunch, through a single `AgendaViewModel` instance

**Plans**: TBD
**Research flag**: spike + UAT, highest data-loss risk of the milestone: the in-place update UAT requires an installed v1.0 release build (store, TestFlight or v1.0 tag); confirm `room.id == room.name` for the room filter; validate the continuous combined flow across the bridge on the CI toolchain
**UI hint**: yes

### Phase 10: Explicit iOS DI

**Goal**: iOS dependencies are composed once at launch by an explicit composition root and injected through constructors, with no service-locator or global singleton in the ViewModels
**Depends on**: Phase 9
**Requirements**: IOSDI-01, IOSDI-02, IOSDI-03
**Success Criteria** (what must be TRUE):
  1. At launch, `AppDependencies.live()` is built only once (still in the app target at this stage) and a test checks that the initialization order stays Firebase, consent, performance, then Remote Config defaults before the first read
  2. ViewModels receive their dependencies through constructors via protocols; `BaseViewModel`, `RCValues.sharedInstance`, the global `remoteConfig`, `PerformanceMonitoring.shared` and `FirebaseAnalyticsService.shared` no longer exist
  3. `scripts/swift-di-gate.sh` runs in CI and fails the job as soon as a file outside the composition root accesses `IosDependencies` or a singleton
  4. Analytics events, performance traces, Remote Config values and consent choices behave as in v1.0 (UAT + tests on fakes green)

**Plans**: TBD
**Research flag**: standard pattern, except the initialization-order test

### Phase 11: SPM Modularization

**Goal**: The iOS app is split into local core and feature SPM packages that consume `shared` as a static XCFramework, with an app target reduced to a shell, and builds from a clean checkout locally as in CI
**Depends on**: Phase 10
**Requirements**: IOSMOD-01, IOSMOD-02, IOSMOD-03
**Success Criteria** (what must be TRUE):
  1. From a clean checkout, the app builds locally and in CI with `shared` produced by Gradle as a static XCFramework and consumed as an SPM `binaryTarget`; the `embedAndSignAppleFrameworkForXcode` phase no longer exists
  2. A Release archive keeps its dSYMs, and a test crash shows up symbolicated in Crashlytics
  3. The single manifest contains the core targets, all at `.iOS(.v17)`: Kotlin bridge (sole importer of `shared`), CoreServices, FirebaseServices, DesignSystem (SwiftGen output and resources via `Bundle.module`) and CoreDI, which hosts `AppDependencies`
  4. Agenda (session detail included), Speakers, Venue and About are feature targets with no dependency on each other and no import of the Kotlin framework; the app target only calls `AppDependencies.live()` and assembles the features, and the package tests run in the CI test plan
  5. After each extraction (About, Venue, Speakers then Agenda, one commit each), a manual smoke confirms that the app behaves as in v1.0

**Plans**: TBD
**Research flag**: blocking spike before the extractions: XCFramework build order from a clean checkout on CI (Xcode 26.6), refresh in Xcode, `binaryTarget` outside the package root, Release/dSYM path (`scripts/build-ios-release.sh`, `documentation/iOS_BUILD_DSYM.md`), `Bundle.module` support in the SwiftGen 6.6.3 templates (otherwise commit the generated output), Firebase 11.15 concurrency at the package boundary
**UI hint**: yes

### Phase 12: Strict Swift 6, Smoke Tests and Documentation

**Goal**: All iOS code compiles in strict Swift 6 with no warnings, Kotlin interop is confined and checked, every feature has a smoke test and the documentation describes the new architecture
**Depends on**: Phase 11
**Requirements**: CONC-01, CONC-02, IOSTEST-02, DOC-01
**Success Criteria** (what must be TRUE):
  1. All packages and the app target compile in Swift 6 language mode with complete concurrency checking and zero warnings in CI, and the app behaves as in v1.0
  2. `asyncFunction`/`asyncSequence` and the `@unchecked Sendable` conformances (limited to immutable Kotlin types) appear only in the Kotlin bridge; a CI gate fails if any are added elsewhere
  3. Every feature (Agenda, Speakers, Venue, About) has a SwiftUI smoke test rendered via `UIHostingController`, run in CI
  4. `README.md`, `documentation/ARCHITECTURE.md`, `documentation/DEVELOPMENT.md`, `documentation/iOS_BUILD_DSYM.md` and `AGENTS.md` describe the packages, the composition root, the XCFramework and the iOS commands, and a clean checkout builds by following only these documents

**Plans**: TBD
**Research flag**: replay the KMP-NativeCoroutines bridge in Swift 6 on the CI toolchain (Swift 6.3.3); if the bridge diagnostics are numerous, switch over package by package, never in a single commit
**UI hint**: yes

## Progress

**Execution Order:**
Phases execute in numeric order: 6 → 7 → 8 → 9 → 10 → 11 → 12

| Phase | Milestone | Plans Complete | Status | Completed |
|-------|-----------|----------------|--------|-----------|
| 1. CI Pipeline Fixed & Optimized | v1.0 | 3/3 | Complete | 2026-09-17 |
| 2. Dependency & Build Tooling Upgrade | v1.0 | 5/5 | Complete | 2026-09-19 |
| 3. Multi-Module Architecture Extraction | v1.0 | 11/11 | Complete | 2026-09-27 |
| 4. Hilt to Koin DI Migration | v1.0 | 7/7 | Complete | 2026-10-13 |
| 5. Test Coverage Retrofit | v1.0 | 11/11 | Complete | 2026-10-16 |
| 6. iOS Test Foundation and CI Gates | v1.1 | 5/6 | In Progress | - |
| 7. KMP Domain Layer and Android Adoption | v1.1 | 0/? | Not started | - |
| 8. Modern SwiftUI, iOS 17 and Observation | v1.1 | 0/? | Not started | - |
| 9. iOS on Shared Logic and Legacy Persistence | v1.1 | 0/? | Not started | - |
| 10. Explicit iOS DI | v1.1 | 0/? | Not started | - |
| 11. SPM Modularization | v1.1 | 0/? | Not started | - |
| 12. Strict Swift 6, Smoke Tests and Documentation | v1.1 | 0/? | Not started | - |
