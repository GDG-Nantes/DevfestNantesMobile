# DevFest Nantes App Modernization

## What This Is

The DevFest Nantes mobile app (Kotlin Multiplatform: Android/Jetpack Compose + iOS/SwiftUI) lets attendees browse the agenda, the speakers and the venue, and manage their favorites for the DevFest Nantes event. The project has not evolved for a year, and this effort aims at a purely technical modernization of its codebase, with no change to user behavior, to get back onto a sound foundation before resuming feature development.

## Core Value

CI/CD must work again and the project must become maintainable again (modern build, modular architecture, decentralized DI, solid test coverage) without ever regressing the app's existing behavior for users.

## Current State

**v1.0 Modernization shipped on 2026-10-16** (phases 1-5, 37 plans, PR #417 to #421). Android/iOS CI green and cached, toolchain up to date (Kotlin 2.4.20, AGP 9.4.0, Gradle 9.7.1), `build-logic` + 6 `core-*` + 6 `feature-*` architecture with `:shared` as an umbrella iOS framework, Koin 4.2.2 DI verified in CI, Kover coverage 30.57%. No user behavior change. Audit: `milestones/v1.0-MILESTONE-AUDIT.md` (tech_debt, 25/25 requirements).

## Current Milestone: v1.1 iOS Architecture

**Goal:** Modernize the iOS app to the same level as the Android app — business logic shared in KMP, modular SwiftUI app on Observation + strict Swift 6, real test coverage — with no user behavior change whatsoever.

**Target features:**
- Shared KMP domain layer: agenda grouping/sorting, session filtering, favorites, speakers/venue/partners mapping exposed as use cases/repositories consumed by Android and iOS (ViewModels remaining native on each platform)
- Removal of duplicated logic on iOS: agenda favorites and filters move out of `UserDefaults` in `AgendaViewModel` into the shared logic, with migration of existing data (no favorite lost)
- Modular iOS app: local SPM packages per feature (Agenda, Speakers, Venue, About…) + core packages (DesignSystem, Core/DI), app target reduced to a shell
- Explicit iOS DI: composition root injecting dependencies into ViewModels instead of `IosDependencies.shared` lookups and global singletons (`RCValues`, `PerformanceMonitoring`, global `remoteConfig`)
- Modern SwiftUI: iOS 17 minimum on all targets, `@Observable` ViewModels instead of `ObservableObject`/`@Published`, Combine removed
- Strict Swift 6: Swift 6 language mode with complete concurrency checking, `@MainActor` ViewModels, async/await over Kotlin Flows
- Tests & CI: commonTest tests of the shared use cases (JVM + iOS simulator), Swift Testing tests of each iOS ViewModel, SwiftUI smoke test per feature, iOS tests run in GitHub Actions

## Requirements

### Validated

- ✓ Browsing the session agenda (Agenda tab) — existing
- ✓ Session detail (SessionLayout/SessionViewModel) — existing
- ✓ Browsing the speakers (Speaker screens) — existing
- ✓ Browsing the venue (Venue screen) — existing
- ✓ Favorites/bookmarks management (BookmarksStore, persisted in SharedPreferences) — existing
- ✓ Session filtering (SessionFiltersService) — existing
- ✓ Data fetching via GraphQL (Apollo Client, confetti-app.dev endpoint) — existing
- ✓ Analytics (Firebase Analytics/Crashlytics/Performance) — existing
- ✓ Conditional OpenFeedback integration — existing
- ✓ Android target (API 23-36, Jetpack Compose) and iOS target (SwiftUI, via Cocoapods) — existing
- ✓ iOS CI: GitHub Actions pipeline repaired (dynamic resolution of the "latest plain iPhone" simulator + the most recent installed Xcode, instead of the hardcoded "iPhone 16" name) — v1.0 (Phase 1)
- ✓ DevOps: optimized GitHub Actions CI/CD (read-only Gradle cache on `pull_request` runs for both workflows, `android-setup` composite action shared by Android and iOS, proof of a real `pull_request` run on all four Android jobs) — v1.0 (Phase 1)
- ✓ Build & dependencies: full dependency update (Kotlin 2.4.20, AGP 9.4.0, Gradle 9.7.1, Compose BOM 2026.09.00, Apollo 5.2.0, Firebase BOM 34.19.0…) — v1.0 (Phase 2)
- ✓ Build & dependencies: Gradle Declarative DSL as a documented partial migration (`settings.gradle.dcl`; modules left in `.kts` for lack of AGP/KMP support) — v1.0 (Phase 2)
- ✓ Architecture: Now in Android-style multi-module split adapted to KMP — `build-logic` (6 convention plugins, `buildSrc` removed), `:core:{model,network,analytics,data,testing,ui}`, `:feature:{venue,about,settings,speakers,agenda,session-detail}` (`*Route` entry points via callbacks), `:shared` reduced to a single umbrella iOS framework (bookmarks with no screen, in `core:data`/`core:ui`) — v1.0 (Phase 3)
- ✓ DI: full migration from Hilt to Koin 4.2.2 — one Koin module per Gradle module, composed by `initKoin()` in `:shared` (`allowOverride(false)`), `IosDependencies` on iOS with no Koin name in `shared.h`, graph verified in CI (`verify()` + JVM/iOS/Robolectric startup tests), strict `scripts/di-gate.sh` against Hilt and service-locator calls — v1.0 (Phase 4)
- ✓ Quality: Kover line coverage raised from 15.96% to 30.57% — GraphQLStore/mapper tests on Apollo MockServer (JVM/Android/iOS), shared Store contract, pinned SharedPreferences persistence, unit tests of the ten ViewModels on a controllable fake, agenda filter matrix, Compose smoke tests of the six feature modules; OpenFeedback/kotlinx-datetime crash (G-05-3) fixed and verified on device — v1.0 (Phase 5)

- ✓ iOS tests: hosted Swift Testing target `DevFestNantesTests` (replaces the broken `DevFest NantesTests`) run through the committed `iosApp.xctestplan`; iOS CI runs build-for-testing + test-without-building on every PR, fails on zero executed tests (`scripts/xcresult-assert-tests.sh`), Xcode-keyed DerivedData cache; `swift-names-gate.sh` restored in `scripts/` and run in CI; KMP-NativeCoroutines aligned on 1.0.6 (SPM + Gradle) guarded by `scripts/kmp-nc-version-gate.sh`; v1.0 parity smoke approved — v1.1 (Phase 6, IOSTEST-03/04, CONC-03)

### Active

Milestone v1.1 iOS Architecture (details and REQ-IDs in `REQUIREMENTS.md`):
- [ ] Domain logic (agenda, filters, favorites, mapping) shared in KMP and consumed by Android and iOS
- [ ] No more duplicated business logic on iOS; existing `UserDefaults` data migrated without loss
- [ ] iOS app split into local feature/core SPM packages, app target as a shell
- [ ] Explicit iOS DI through a composition root, with no service-locator or global singletons in ViewModels
- [ ] iOS 17 minimum, `@Observable` ViewModels, Combine removed
- [ ] Swift 6 strict concurrency with no warning
- [ ] Shared tests + Swift Testing + SwiftUI smoke tests, run in iOS CI

### Out of Scope

- New user features — v1.1 remains a purely technical effort (like v1.0)
- v1.0 debt (`speakersPage`, `RoomSortIndex`, `SocialItem`, Speaker page-view, ViewModels ERROR state, CI hardening WR-02/WR-03) — deferred; bugs found during v1.1 are logged as debt, not fixed (strict parity)
- ViewModels shared in KMP (androidx.lifecycle KMP / SKIE) — choice to share only the domain layer; each platform keeps idiomatic ViewModels
- Shared UI (Compose Multiplatform) — native UI kept on Android (Compose) and iOS (SwiftUI)
- Visible UX/UI changes — except unavoidable side effects of the technical migration
- Deadline tied to the next DevFest Nantes edition — no strict date constraint, progress phase by phase

## Context

- State after v1.0: KMP monorepo `build-logic` (convention plugins `devfest.*`) + `:core:{model,network,analytics,data,testing,ui}` + `:feature:{agenda,speakers,venue,session-detail,about,settings}` + `:androidApp` + `:shared` (umbrella iOS framework exporting core:model/data/analytics) + `iosApp` (SwiftUI).
- Stack: Kotlin 2.4.20, AGP 9.4.0, Gradle 9.7.1 (`settings.gradle.dcl`, modules in `.kts`), Compose BOM 2026.09.00, Apollo 5.2.0, Koin 4.2.2, Firebase BOM 34.19.0, Coroutines 1.11.0, kotlinx-datetime `0.8.0-0.6.x-compat`, minSdk 26 / compileSdk 37.
- Size: ~15.8k lines of Kotlin, ~3.8k lines of Swift; v1.0 touched 328 files (+9,795 / −2,005).
- Tests: Kover 30.57% (report only), JVM/Android host/iOS simulator tests + Compose smoke tests per feature module, Koin `verify()` and `scripts/di-gate.sh` in CI.
- Known debt: see Active above and STATE.md § Deferred Items. OpenFeedback still depends on `kotlinx.datetime.Instant` (compat pin limited to 0.8.x: durable fix = an OpenFeedback release on kotlin.time).
- The `.planning/codebase/` documents were regenerated after v1.0 (commit `99edc0a`).
- iOS state at the start of v1.1: ~45 Swift files in a single app target (no local package), `ObservableObject` + Combine ViewModels deriving from `BaseViewModel` which resolves `IosDependencies.shared.store()`, favorites (`"Favorites"`) and filters (`"AgendaSessionFilters"`) persisted in `UserDefaults` by `AgendaViewModel`, global singletons (`RCValues`, `PerformanceMonitoring`, `remoteConfig`), mixed 15.0/17.0 deployment targets, `SWIFT_VERSION = 5.0`, SPM (Firebase, KMP-NativeCoroutines), a single test class (`DevFest NantesTests`) not run in CI.

## Constraints

- **Tooling**: The agent MUST use the `android` CLI for any Android task — in particular `android docs search` to check the exact syntax of the Gradle Declarative DSL and other recent APIs, and the lint/validation commands before considering a change done.
- **Functional compatibility**: No regression in user behavior is acceptable — every step of the modernization must preserve the app's existing behavior (Android and iOS).
- **KMP**: Every technical solution (DI, modularization) must work on both Android and iOS via the `shared` module.
- **Gradle Declarative DSL**: Preview/incubating technology — AGP/KMP support may be partial depending on versions. Accept a documented partial migration rather than blocking on a 100% conversion.
- **CI/CD**: GitHub Actions remains the target CI/CD platform (no change of CI tool).

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| Order of work: iOS CI first, then deps/Gradle DSL, then multi-module, then DI, then tests | The broken CI is blocking and a quick win; the build foundations must be stable before refactoring the architecture | ✓ Phase 1 shipped — iOS CI repaired + read-only Gradle cache on PRs for both workflows |
| Gradle cache: `cache-read-only` expression duplicated at each call site rather than moved into the default input of the `android-setup` composite action | A composite action's metadata is parsed statically; an expression in the input default would be passed to `setup-gradle` as raw uninterpolated text and coerced into an always-true string | Phase 1 |
| `cancel-in-progress: true` kept as is on `ios.yml`/`android.yml` (no per-event conditional `cancel-in-progress`, no `queue`) | Intended cost behavior (D-07), self-healing on the next push; `queue` + `cancel-in-progress` is a blocking GitHub validation error | Phase 1 — accepted as a documented risk (T-01-05) |
| DI: full Hilt → Koin migration | Hilt does not work in the KMP `shared` module; Koin is KMP-native and enables real decentralized DI per module | ✓ Phase 4 shipped — no Hilt artifact on the classpath, DI-01..06 complete, Android (debug + release R8) and iOS UAT approved, 27/27 security threats closed |
| Multi-module split: feature + core (full Now in Android style) | Full NIA vision explicitly requested by the user, adapted to the KMP context (Android + iOS) | ✓ Phase 3 shipped — 12 modules + `build-logic`, strictly core ← feature ← app graph, green CI, 5 iOS/Android smoke tests approved, 34/34 security threats closed |
| Phase 3: `:shared` keeps a single internal file `SharedFrameworkPlaceholder.kt` (amendment D-10) | Kotlin/Native skips compile+link (NO-SOURCE) on an empty source set → no `shared.framework` produced | Phase 3 — approved by the user at checkpoint 03-03; never delete |
| Phase 3: Apollo types renamed via `@targetName` (GraphQLVenue/Session/Speaker/Room/Partner), Swift names of the 5 domain types changed once (amendment D-11) | Permanently removes the Kotlin/Native Swift name collision that halted 03-01; `swift-names-gate.sh` strengthened (member sets + collisions) | Phase 3 (03-10/03-11) |
| Phase 3: Gradle daemon heap raised to 4 GB | D8 OOM (dex merging) in the Instrumentation tests CI job with 6 feature modules | Phase 3 — approved by the user |
| Gradle Declarative DSL: full migration if possible, otherwise documented partial | The technology is incubating; avoid blocking the whole project on incomplete AGP/KMP support | ✓ v1.0 — documented partial (`settings.gradle.dcl` only) |
| Test coverage: no strict % target, priority on business logic | The user prefers qualitative coverage (ViewModels, Store, mappers) over an arbitrary number | ✓ Phase 5 shipped — Kover 0.9.11 report only, 15.96% → 30.57%, UAT 3/3, 0 open threats |
| No new user feature during this effort | Purely technical effort — reduce regression risk by isolating modernization from functional evolution | ✓ v1.0 — held, UAT with no regression across the 5 phases |
| No strict deadline tied to the next edition | In-depth effort carried out phase by phase without date pressure | ✓ v1.0 — shipped in ~30 days |
| Phase 4: explicit `ApplicationInitializer` order (`single(named("applicationInitializers"))`), asserted by `KoinStartupTest` | Collection consent must be applied before Performance Monitoring is forced (T-04-19) | Phase 4 |
| Phase 4: Robolectric 4.17 as `testImplementation` to test the real startup of the Koin graph with Firebase | Exercises the production Firebase definitions without modifying them; reusable foundation for Phase 5 | Phase 4 — chosen by the user |
| Phase 5: kotlinx-datetime resolved to `0.8.0-0.6.x-compat` (no return to 0.6.2), first-party code on `kotlin.time.Instant`, R8 suppressions removed | OpenFeedback 1.0.0-alpha.3 (and alpha.6) still references `kotlinx.datetime.Instant`; the compat variant restores these classes while keeping 0.8. Compat releases stop at 0.8.x: the durable fix is an OpenFeedback release on kotlin.time | Phase 5 (05-11) — guarded in CI by `OpenFeedbackDatetimeLinkageTest` |
| Phase 5: `FeedbackFlags` seam on top of Firebase Remote Config, `AgendaViewModel` IO dispatcher injected through a named Koin binding | Make FeedbackForm and Agenda testable on pure JVM without changing behavior | Phase 5 (05-06, 05-08) |
| Phase 5: uncaught store failures in ViewModels are pinned by characterization tests, not fixed | Effort with no behavior change; the fix (ERROR state) is deferred — see `deferred-items.md` and `05-UI-REVIEW.md` | Phase 5 — known debt |
| Phase 2: version upgrade in sequential groups (one commit per group), with a green CI between each step | Cumulative major upgrades would be impossible to diagnose on failure; each group gets its own revertible commit and its own green signal before tackling the next one | ✓ Phase 2 shipped — Kotlin 2.4.20, AGP 9.4.0 + `com.android.kotlin.multiplatform.library` plugin, Gradle 9.7.1, Compose BOM 2026.09.00, Apollo 5.2.0 (cache migrated to `com.apollographql.cache`), Firebase BOM 34.19.0/Coroutines 1.11.0/kotlinx-serialization 1.11.0/kotlinx-datetime 0.8.0; Gradle Declarative DSL pilot succeeded on `settings.gradle.dcl` (androidApp/shared stay in Kotlin DSL, AGP/KMP DCL support not mature for these modules) — full details in `.planning/STATE.md` § Phase 02 version deviations / DCL pilot outcome |
| Phase 6: plain `*-gate.sh` scripts with `check`/`selftest` modes and greppable OK/FAIL tokens for every iOS CI gate (xcresult, swift-names, kmp-nc) | A gate that cannot be shown to fail is not a gate; tokens make CI logs auditable and the selftests catch a weakened guard | ✓ Phase 6 — all three gates green on real CI runs (40477853950, 40488566873, 40501276582) |
| Phase 6: KMP-NativeCoroutines bump (alpha → 1.0.6) kept only after a human parity smoke, revertible as one commit (D-12) | The AsyncSequence cancel-on-dealloc change touches the user-visible flows and no UI test exists before Phase 12 | ✓ Phase 6 — smoke approved on 8 flows, bump kept |

## Evolution

This document evolves at phase transitions and milestone boundaries.

**After each phase transition** (via `/gsd-transition`):
1. Requirements invalidated? → Move to Out of Scope with reason
2. Requirements validated? → Move to Validated with phase reference
3. New requirements emerged? → Add to Active
4. Decisions to log? → Add to Key Decisions
5. "What This Is" still accurate? → Update if drifted

**After each milestone** (via `/gsd-complete-milestone`):
1. Full review of all sections
2. Core Value check — still the right priority?
3. Audit Out of Scope — reasons still valid?
4. Update Context with current state

---
*Last updated: 2026-10-27 after Phase 6*
