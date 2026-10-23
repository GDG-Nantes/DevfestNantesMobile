# Project Research Summary

**Project:** DevFest Nantes (KMP conference app), milestone v1.1 iOS Architecture
**Researched:** 2026-10-23
**Confidence:** MEDIUM-HIGH (HIGH on what to build and the Swift/KMP interop choices; MEDIUM on the SPM/XCFramework build order and CI release path until spiked)

## Executive Summary

The iOS app re-implements agenda logic in Swift and keeps favorites/filter persistence in view models over `UserDefaults`. v1.1 moves the duplicated logic into a new `:core:domain` KMP module, rebuilds iOS on an iOS 17 floor with `@Observable` view models, replaces singletons with an explicit Swift composition root, splits the app into local SPM packages, flips to Swift 6 strict concurrency, and runs iOS tests in GitHub Actions. Behavior must stay identical and no favorite or filter may be lost on upgrade.

Sequence the work so each step ships green: test lane and gates first; Kotlin domain layer next (Android consumes it first, since its tests are the parity oracle); iOS persistence on the same legacy keys; view models and DI one screen at a time; package split and Swift 6 flip last. Interop stays on KMP-NativeCoroutines 1.0.6 (not SKIE, not Swift Export). The Kotlin framework reaches SPM as a static XCFramework `binaryTarget`, verified in a local experiment.

Main risks: favorites/filters lost on upgrade; Gradle framework not visible to SPM from a clean checkout; Swift 6 Sendable friction at the Kotlin boundary; silent drift in agenda grouping, room order and timezone. Mitigations: reuse legacy persistence keys and shapes; prove the XCFramework build order on CI before splitting features; confine Kotlin escape hatches to one bridge target with a lint gate; characterize current behavior before moving logic.

## Key Findings

### Recommended Stack

- Kotlin: no new libraries. Add `:core:domain` (`devfest.kmp.library`, KSP, nativecoroutines), export via `:shared`. Keep kotlinx-datetime `0.6.x-compat` pin; use `kotlin.time` Clock/Instant.
- Swift: Swift 6 mode on all local packages (`swift-tools-version: 6.0`, `.iOS(.v17)`, `swiftLanguageModes: [.v6]`). KMP-NativeCoroutines exact 1.0.6 in the bridge only (pbxproj currently pins 1.0.0-ALPHA-45). Remove Combine product.
- Static XCFramework (`isStatic = true`) via `binaryTarget(path:)`; slices iosArm64 + iosSimulatorArm64; link `sqlite3` and `c++` (undefined `_sqlite3_*` without it, verified).
- Tests: Swift Testing from toolchain (no package), `UIHostingController` smoke tests, `.xctestplan`. No ViewInspector/snapshot libs.
- DI: plain Swift composition root; Koin stays Kotlin-side behind `IosDependencies`. No Factory/swift-dependencies/Swinject.
- Persistence: hand-written `NSUserDefaults` actual in `core:data` iosMain. No multiplatform-settings, no DataStore.
- Firebase: keep resolved 11.15.0 (min 11.14) behind protocols; no 12.x/13.x bump in this milestone.
- CI: `macos-26`, Xcode 26.6 / Swift 6.3.3. Replace `xcodebuild build` with build-for-testing + test-without-building; `-resultBundlePath`; XCFramework task before package resolution.
- Do not add: SKIE, Swift Export, Combine, RxSwift, Swift DI libs, settings libs, ViewInspector, snapshot libs, swift-testing package, CocoaPods leftovers.

### Expected Features (architectural capabilities; zero user-visible change unless listed under Disagreements)

**Table stakes:**
- `:core:domain` policies for session filter matching (OR within type, AND across types, bookmark = membership), agenda grouping and in-slot sort, `isService`/`isNavigable`, speaker A-Z grouping, partner grouping, venue language selection. Golden oracle: v1.0 Android filter matrix.
- Move `SessionFilter` to `core:model` as top-level `data class` (`SessionFilterType`, not nested).
- iOS `BookmarksStore` and `SessionFiltersStore` actuals over legacy `"Favorites"` and `"AgendaSessionFilters"`. Android keeps `selected_sessions` and its JSON string-set.
- iOS 17 everywhere (today 15.0/17.0 mixed). `@Observable @MainActor` VMs; `@State` ownership; remove `objectWillChange.send()` hacks and duplicate `AgendaViewModel()` instances (AgendaCellView, AgendaDetailView).
- Constructor-injected VMs; remove `BaseViewModel` default `IosDependencies.shared.store()`, `RCValues.sharedInstance`, global `remoteConfig`, `PerformanceMonitoring.shared`, `FirebaseAnalyticsService.shared`.
- Local SPM packages per feature plus core packages; features never depend on each other.
- Swift 6 strict, zero warnings.
- Test lane: working Swift Testing VM tests with fakes; one smoke test per feature; `xcodebuild test` in `ios.yml`; remove stale `DevFest NantesTests` (references non-existent `DevFestViewModel`); restore `swift-names-gate.sh` into CI.
- Legacy-data tests: captured payloads for favorites and filters; upgrade-in-place UAT.

**Differentiators (optional):** `ObserveAgendaUseCase` combined flow (highest risk); injectable Clock/TimeZone; `InitialDayUseCase` (removes 3 hardcoded 2025 date copies); `ViewState` struct; `@ObservationIgnored`; FeedbackFlags seam.

**Defer:** shared VMs/UI state; unifying scroll-to-current-slot (D6); per-entity store refactor; snapshot/XCUITest gates; coverage % gate; Tuist/Bazel; fixing latent bugs (room id vs name, device tz, first-emission-only, partner buckets, RoomSortIndex, Android PerformanceInitializer consent ordering); iOS 15/16 drop decision (open).

### Architecture Approach

- Kotlin graph: `androidApp -> feature:* -> core:ui -> core:domain -> core:data -> core:network / core:model`; `:shared` exports model, data, domain, analytics. `DevFestNantesStore` unchanged. Use cases are named-method classes; inject `CoroutineDispatcher` (default `Dispatchers.Default`). `domainModule` appended to `sharedModules`; remove old bindings in the same commit the new ones appear (`allowOverride(false)`). `IosDependencies.graph()` returns Koin-free `IosGraph`.
- iOS: static XCFramework behind one bridge package. Feature packages depend on ports (protocols defined in feature targets). Only the bridge touches `asyncSequence(for:)`. VMs are `@MainActor @Observable`, consume via `AsyncThrowingStream` in `.task {}`. App target becomes shell; `AppDependencies.live()` built once in `init` after `FirebaseApp.configure()`.

**Major components:**
1. `:core:domain`
2. `SessionFilter`/`SessionFiltersStore`/`BookmarksStore` contracts
3. Swift bridge package (sole importer of `shared`; binaryTarget + sqlite3 flag; nativecoroutines; the only `@unchecked Sendable` for immutable Kotlin models)
4. CoreServices/FirebaseServices/DesignSystem (SwiftGen output, resources)
5. CoreDI (composition root: `AppDependencies` Sendable struct of ports)
6. Feature packages (AgendaFeature, SpeakersFeature, VenueFeature, AboutFeature), each with factory entry view building VM in `init`

Dependency rules: features do not import Firebase or CoreDI; CoreDI does not hold UI; add `scripts/swift-di-gate.sh`; extend `di-gate.sh` to the domain module. Extraction order: About, Venue, Speakers, Agenda.

### Critical Pitfalls

1. **Favorites/filters lost on upgrade.** Prevention: reuse legacy keys and shapes; if a migrator is used, read-before-write, union merge, three-state read (absent / valid / wrong-type), flag only after read-back, never delete legacy keys in the same release. UAT: install v1.0 release build, star sessions, set filters, install new build over it, force-quit, verify.
2. **Framework invisible to SPM / clean-checkout resolution failure.** `embedAndSign` runs after packages compile. Prevention: XCFramework + `binaryTarget` at a fixed gitignored path; Gradle sync before resolution (scheme pre-action + CI step); one owner; static packages; host-less test targets; delete `embedAndSign` in the same change.
3. **Swift 6 Sendable wall.** `@preconcurrency import` does not silence the `sending` error (verified). Confine bridging to one package; feature code never calls `asyncFunction`/`asyncSequence`; grep gate on `@unchecked Sendable`; remove `DispatchQueue.main.async`.
4. **Behavior drift in grouping/sort/day/timezone.** Concrete: iOS `ISO8601DateFormatter` without fractional seconds and `Date()` fallback; device-tz day labels; iOS sorts rooms by name vs Android `sortIndex`; current-slot target differs; hardcoded 2025 dates. Prevention: golden-master tests per platform before extraction; inject tz/locale/clock; per-platform parity parameters; UAT comparing day tabs, section first/last sessions, room order.
5. **Observation lifetime bugs.** `@ObservedObject var vm = VM()` and `@State var vm = VM()` re-run initializer on re-init; `observeRooms()` unstructured Task never cancelled. Prevention: `@State` via `State(initialValue:)` in init or factory view; side-effect-free init; collection in `.task(id:)`; treat CancellationError as non-error.

Also: Koin `allowOverride(false)`; name collisions (`SessionFilter`, `FilterType`, `Section`, `Task`, `Duration`); SwiftGen `Bundle(for:)` breaking in packages; CI caches without Xcode version; zero-executed-test green runs.

## Where the Four Files Disagree (resolve or accept before the affected phase)

1. **Persistence: legacy keys vs new keys plus Swift migrator.** STACK, FEATURES, PITFALLS: reuse legacy keys/shapes. ARCHITECTURE: new iOS keys, filters in Android uppercase JSON, Swift `LegacyDefaultsMigrator`. Recommendation: reuse legacy keys; iOS actual keeps its own lowercase codec for the filter value. Accept that Android and iOS filter JSON differ (contradicts ARCHITECTURE's "one serializer").
2. **Package granularity.** ARCHITECTURE/FEATURES: one `DevFestKit` manifest with per-feature targets. STACK: separate `KotlinShared` and `CoreKotlinBridge` packages. Recommendation: one manifest; keep sqlite3/c++ linker settings on the binary target.
3. **Phase order: @Observable vs SPM.** ARCHITECTURE/FEATURES: Observation and DI before SPM split. PITFALLS: @Observable after modularization. Recommendation: follow ARCHITECTURE, interleaving ports per VM with the Observation phase; SPM split after both.
4. **Swift 6 mode timing.** STACK/ARCHITECTURE: new packages in v6 from creation. FEATURES: sequence last. PITFALLS: complete checking in Swift 5 first, flip per package, never in one commit. Recommendation: new packages start in v6; moved existing code flips via PITFALLS staging; ticketed exception if the bridge backlog is large.
5. **Feature packages importing the bridge.** ARCHITECTURE lists SharedKit as a feature dependency. STACK, PITFALLS, FEATURES: features depend on ports and Swift values only. Recommendation: features never import the Kotlin framework.
6. **Room order in a slot (visible change).** ARCHITECTURE: adopt Android `sortIndex`, record deviation. FEATURES/PITFALLS: strict parity. Recommendation: `RoomOrder` parameter preserving iOS name order until the user signs off. Surface to user.
7. **Agenda collection on iOS.** ARCHITECTURE: continuous collection is "required". FEATURES/PITFALLS: keep first-emission unless accepted. Recommendation: one-shot for speakers/partners/venue; for agenda, user decides between continuous (documented change) and on-demand re-read. Surface to user.
8. **Kotlin Sendable technique.** STACK: `extension Model: @retroactive @unchecked Sendable` (verified compiling). FEATURES/PITFALLS: blanket `@unchecked Sendable` is an anti-feature outside the bridge. Recommendation: STACK's technique, confined to the bridge, immutable types only, grep gate, review checkpoint for mutable exports.
9. **Status of framework-visibility spike.** STACK: verified locally (Xcode 27.1 / Swift 6.4). ARCHITECTURE/PITFALLS: still MEDIUM, needs spike. Recommendation: mostly de-risked locally; still a gating spike on CI (Xcode 26.6 / Swift 6.3.3) from a clean checkout.
10. **Where `AppDependencies` lives.** ARCHITECTURE: `CoreDI` package target. FEATURES: app target. Recommendation: `CoreDI`; app target only calls `live()`.
11. **Domain module placement.** STACK/ARCHITECTURE: `:core:domain`. FEATURES: open question. Recommendation: `:core:domain`.

## Implications for Roadmap (local numbering; roadmapper to renumber from v1.0's 5)

### Phase 1: iOS CI, test harness, gates
Remove stale test target; working Swift Testing target; `xcodebuild` test lane with xcresult minimum-count assertion; Xcode-aware cache key; KMP-NC pinned to 1.0.6 with version-gate script; restore and wire `swift-names-gate.sh`; simulator by UDID. Avoids pitfalls 9, 13, 14, 17. Standard pattern.

### Phase 2: Shared domain (Kotlin only)
`:core:domain`; `SessionFilter` to `core:model`; `SessionFiltersStore` with Android actual unchanged in key/format; `FakeSessionFiltersStore`; pure policies; `commonTest` on JVM and iOS sim; characterization tests (filter matrix, persistence format, grouping at two timezones); `domainModule` + `DomainModuleTest`/`verify()`; export. Android unaffected. Avoids 1, 7, 8, 16. Standard, with timezone/room-order characterization.

### Phase 3: Android adopts domain
AgendaViewModel, BookmarksViewModel, HomeViewModel use shared policies; remove `SessionFiltersService` and its binding in the same commit as the store binding. Gate: full AGENTS.md CI list plus agenda UAT. Standard.

### Phase 4: iOS 17, `@Observable`, Combine removal, Swift Testing with fakes
iOS 17 on all targets; `@Observable @MainActor` for Agenda, Speakers, SpeakerDetails, Venue, About; `@State` ownership; `.task`-driven collection; remove `DispatchQueue.main.async`, Combine and the Combine product; `@ObservationIgnored`; narrow ports per VM; one shared Agenda VM. Avoids 4, 6, 18. Standard patterns.

### Phase 5: iOS adopts domain + legacy persistence
`IosGraph`/`graph()`; iosMain stores over legacy keys with lowercase codec; delete Swift `SessionFilter`/`FilterType` in the same change as Kotlin export; move grouping/filter/initial-day to shared policies with parity parameters; captured-payload tests; upgrade-in-place UAT from v1.0 build. Highest data-loss risk; needs spike and UAT. Avoids 1, 7, 8.

### Phase 6: Explicit DI, still in app target
`AppDependencies` with `live()`/`preview`; CoreServices/FirebaseServices protocols; remove singletons and `BaseViewModel`; initialization-order test (Firebase configure, consent, performance, RC defaults before first read); `swift-di-gate.sh` in CI. Note: interleave ports with phase 4. Avoids 11, 12, 15, 19, 20.

### Phase 7: SPM modularization (riskiest structural step)
(a) XCFramework Gradle task + sync to fixed path + scheme pre-action + CI step + release script; remove `embedAndSign`; (b) package with bridge target and linker flags; (c) DesignSystem with SwiftGen output and resources (`Bundle.module`, `defaultLocalization`); (d) CoreServices, FirebaseServices, CoreDI; (e) extract About, Venue, Speakers, Agenda, one commit each. Gate: clean-checkout CI resolution, manual smoke per extraction, release archive with dSYMs and Crashlytics test crash. Avoids 2, 3, 10, 11, 19. Needs spike.

### Phase 8: Swift 6 flip, smoke tests, docs
App target to v6 with zero warnings; per-feature `UIHostingController` smoke test; lint gate for `@unchecked Sendable`; update README, documentation/ARCHITECTURE.md, DEVELOPMENT.md, AGENTS.md, iOS_BUILD_DSYM.md; CI cache keys include Package.swift, Package.resolved, XCFramework inputs; resolved-file change check. Avoids 5, 13, 21.

### Phase Ordering Rationale

Tests/gates first; Kotlin domain before iOS adoption; Observation before package split; persistence separate from Observation (own UAT); DI before SPM; XCFramework switch late; Swift 6 last.

### Research Flags

Needs spike/deeper research:
- Phase 7: clean-checkout XCFramework on CI, Xcode refresh behavior, Release/dSYM path, `binaryTarget` outside package root on CI; SwiftGen bundle support (verify 6.6.3 templates); Firebase concurrency at the package boundary.
- Phase 5: real legacy JSON shape, `room.id == room.name`, agenda flow decision.
- Phase 4/5 bridge and Phase 8: re-run the Swift 6 KMP-NC bridge on CI's 6.3.3, not just the 6.4 toy experiment.

Standard: Phase 1, 2 (apart from characterization), 3, 4, 6 (apart from the init-order test).

## Confidence Assessment

| Area | Confidence | Notes |
|---|---|---|
| Stack | HIGH | Versions from release pages; XCFramework, Swift 6 bridge and sqlite3 verified locally on Xcode 27.1 only |
| Features | HIGH | Duplication and persistence formats read from source; Swift 6 interop MEDIUM |
| Architecture | MEDIUM-HIGH | Kotlin graph and Koin HIGH; SPM/XCFramework MEDIUM (debug arm64 only, locally) |
| Pitfalls | MEDIUM-HIGH | Repo-specific items verified; several Swift/SPM items from training, flagged for spikes |

**Overall confidence:** MEDIUM-HIGH. Most findings agree; the disagreements above are decisions, not unknowns.

### Gaps to Address

- Clean-checkout XCFramework build order on CI: phase 7 gate spike.
- CI toolchain (Swift 6.3.3) vs local 6.4 diagnostics: re-run bridge on CI.
- Release XCFramework, dSYM, Crashlytics: check `documentation/iOS_BUILD_DSYM.md` and `scripts/build-ios-release.sh`.
- iOS 17 runtime not testable on CI (only 26.x simulators): compile-time checks, or 17.x runtime download (decide in phase 1).
- Real legacy data shape and `room.id == room.name`: capture from v1.0 install or Swift `Codable` fixture before phase 5.
- Open product decisions: iOS 15/16 drop (assumed yes); room order; agenda continuous vs one-shot; partner category ordering; whether any visible change is accepted.
- Hardcoded 2025 event dates: keep single-source constant; log as debt.
- SwiftGen 6.6.3 `Bundle.module` support: verify or commit generated output.
- Firebase 11.15 strict concurrency: decide per symbol; no bump.
- Characterization needs a live API snapshot (110 speakers, room ids), not only `core/model` stubs.

## Sources

### Primary (HIGH confidence)
- Repo source and project docs: iosApp sources, project.pbxproj, DevFest NantesTests, shared/build.gradle.kts, IosDependencies.kt, core/data, core/model, feature/agenda, speakers, about, venue, build-logic, libs.versions.toml, scripts/di-gate.sh, .github/workflows/ios.yml, .planning/PROJECT.md, RETROSPECTIVE.md, STATE.md, codebase/CONCERNS.md, v1.0 swift-names-gate.sh.
- Kotlin docs: SwiftPM export (XCFramework/binaryTarget), direct integration, Swift export status, Kotlin 2.4.20 notes.
- KMP-NativeCoroutines releases and Package.swift: https://github.com/rickclephas/KMP-NativeCoroutines (versions; no Swift 6 guidance).
- GitHub runner images: https://github.com/actions/runner-images
- Local experiments 2026-10-23 on Xcode 27.1 / Swift 6.4 (STACK.md "Verified Experiments").

### Secondary (MEDIUM confidence)
- SKIE releases and issues #131, #179: https://github.com/touchlab/SKIE
- Firebase iOS SDK versions: https://github.com/firebase/firebase-ios-sdk
- Kotlin Slack threads on KMP with SPM (links cited in ARCHITECTURE.md and FEATURES.md).

### Tertiary (LOW confidence)
- DEV Community/MVP Factory Swift 6 article; MusicStreamSync PR #97; Medium KMM duplicate-symbol article.

---
*Research completed: 2026-10-23*
*Ready for roadmap: yes*
