# Roadmap: DevFest Nantes App Modernization

## Overview

This is a purely technical modernization of an existing KMP conference app (Android/Jetpack Compose + iOS/SwiftUI sharing business logic via the `shared` module) — no new user-facing features, no behavior regressions. The journey starts by fixing the currently-broken iOS CI pipeline so every later phase has a trustworthy signal, then stages the dependency/build-tooling upgrade (Kotlin 2.4.0, AGP 9.2.0, Gradle 9.7.1, Compose/Apollo/Firebase) on its own before touching architecture. With the build system settled, the codebase is decomposed into a Now in Android-inspired multi-module graph (`core-*`/`feature-*`) while iOS keeps consuming a single umbrella framework. Only once the module skeleton is stable does DI get decentralized from Hilt to Koin module-by-module. Test coverage retrofit comes last, because constructor-injected, Koin-wired code is what makes fakes and tests cheap to write.

## Phases

**Phase Numbering:**

- Integer phases (1, 2, 3): Planned milestone work
- Decimal phases (2.1, 2.2): Urgent insertions (marked with INSERTED)

Decimal phases appear between their surrounding integers in numeric order.

- [x] **Phase 1: CI Pipeline Fixed & Optimized** - iOS CI is fixed at the root cause (dynamic simulator resolution) and both Android/iOS jobs run cached and split in a matrix (completed 2026-09-17)
- [ ] **Phase 2: Dependency & Build Tooling Upgrade** - The project builds and runs on Kotlin 2.4.0, AGP 9.2.0, Gradle 9.7.1 and updated libraries, staged bump by bump with zero behavior change
- [ ] **Phase 3: Multi-Module Architecture Extraction** - The codebase is split into a `core-*`/`feature-*` module graph with unidirectional dependencies, iOS still consuming one umbrella framework
- [ ] **Phase 4: Hilt to Koin DI Migration** - Dependency injection is fully decentralized via Koin, one module per Gradle module, with a CI-verified DI graph
- [ ] **Phase 5: Test Coverage Retrofit** - Business-logic test coverage (ViewModels, Store/repository, GraphQL mappers) is measurably improved on a hardened fixture foundation

## Phase Details

### Phase 1: CI Pipeline Fixed & Optimized

**Goal**: The GitHub Actions CI pipeline reliably builds and validates both Android and iOS on every push, using dynamic simulator resolution and caching for speed
**Depends on**: Nothing (first phase)
**Requirements**: CI-01, CICD-01, CICD-02, CICD-03
**Success Criteria** (what must be TRUE):

  1. The iOS CI job completes successfully end-to-end using dynamic simulator resolution (e.g. `xcrun simctl list` or an explicitly pinned macOS/Xcode runner image) rather than a hardcoded device name that breaks on the next runner-image rotation
  2. The Gradle build cache (`gradle/actions/setup-gradle`) is active in CI and measurably reduces repeated-build time
  3. The Konan cache (`~/.konan`) is active in CI and measurably reduces Kotlin/Native compile time
  4. Android and iOS jobs run as separate matrix entries (`ubuntu-latest`/`macos-latest`) rather than a single combined job

**Plans:** 3/3 plans complete

Plans:
**Wave 1**

- [x] 01-01-PLAN.md — Tracer: dynamic Xcode + simulator resolution in `ios.yml`, proven by a live green CI run (D-01, D-02, D-07)
- [x] 01-02-PLAN.md — Branch-aware Gradle cache policy at all four `android.yml` call sites, plus concurrency (D-04, D-07)

**Wave 2** *(blocked on Wave 1 completion)*

- [x] 01-03-PLAN.md — `ios.yml` routed through the shared `android-setup` composite action with branch-aware caching; Konan cache and job separation asserted intact (D-05, D-04, D-06, D-03)

### Phase 2: Dependency & Build Tooling Upgrade

**Goal**: The project builds and runs on the modernized toolchain with zero observable behavior change, each dependency group staged and verified independently
**Depends on**: Phase 1
**Requirements**: BUILD-01, BUILD-02, BUILD-03, BUILD-04, BUILD-05, BUILD-06, BUILD-07
**Success Criteria** (what must be TRUE):

  1. The project compiles and CI passes on Kotlin 2.4.0
  2. The project builds on Gradle 9.7.1 and on AGP 9.2.0 using the new `com.android.kotlin.multiplatform.library` plugin in place of the forbidden `kotlin.multiplatform` + `com.android.library` coexistence
  3. The app runs unchanged for users on Compose BOM 2026.08.00, Apollo GraphQL 5.0.1, and the latest stable Firebase BOM/Coroutines/kotlinx-serialization/kotlinx-datetime
  4. Build files are migrated to the Gradle Declarative DSL (`.gradle.dcl`) wherever AGP/KMP support allows; any module left on Kotlin DSL (`.kts`) is explicitly documented with the reason support is missing

**Plans:** 3/5 plans executed

Plans:
**Wave 1**

- [x] 02-01-PLAN.md — Version-target policy decision, then the Kotlin 2.4.x + KSP tracer: catalog → local build → both CI workflows green → app on device (BUILD-01, D-01, D-02, D-03)

**Wave 2** *(blocked on Wave 1)*

- [x] 02-02-PLAN.md — Gradle 9.7.1 wrapper with distribution checksum, then AGP 9 `com.android.kotlin.multiplatform.library` swap plus the forced Detekt/Hilt/Firebase-perf bumps and the packaging DSL fix (BUILD-02, BUILD-03, D-04, D-05)

**Wave 3** *(blocked on Wave 2)*

- [x] 02-03-PLAN.md — Compose BOM 2026.08.00, then Apollo 5.x with the `com.apollographql.cache` artifact-group migration and its compiler plugin (BUILD-04, BUILD-05, D-06, D-07)

**Wave 4** *(blocked on Wave 3)*

- [ ] 02-04-PLAN.md — Value-level date-parsing regression test green on the old version, then Firebase BOM / coroutines / serialization / kotlinx-datetime to latest stable (BUILD-06)

**Wave 5** *(blocked on Wave 4)*

- [ ] 02-05-PLAN.md — Gradle Declarative DSL pilot on one low-risk target with a documented outcome either way, then consolidation of every stage's version outcome into STATE.md/PROJECT.md (BUILD-07, D-03, D-08, D-09)

### Phase 3: Multi-Module Architecture Extraction

**Goal**: The codebase is decomposed into a Now in Android-inspired multi-module graph adapted to KMP, with clean dependency direction and a single iOS-facing framework
**Depends on**: Phase 2
**Requirements**: ARCH-01, ARCH-02, ARCH-03, ARCH-04
**Success Criteria** (what must be TRUE):

  1. Build logic is defined through convention plugins that read the existing version catalog (`libs.versions.toml`), with no duplicated build configuration across modules
  2. The `core-*` modules (model, network, data, analytics, ui, testing) exist and never depend on any `feature-*` module — the dependency graph is strictly unidirectional
  3. The `feature-*` modules (agenda, speakers, venue, bookmarks, session-detail, settings) exist, each with its own ViewModel(s), Compose screens, and Koin module
  4. `iosApp` continues to build and consume a single umbrella Kotlin/Native framework aggregating all KMP modules, despite `shared` now being split across several Gradle modules

**Plans**: TBD
**UI hint**: yes

### Phase 4: Hilt to Koin DI Migration

**Goal**: Dependency injection is fully decentralized from Dagger Hilt to Koin, module by module, with CI verifying the graph stays wired correctly
**Depends on**: Phase 3
**Requirements**: DI-01, DI-02, DI-03, DI-04, DI-05, DI-06
**Success Criteria** (what must be TRUE):

  1. No Dagger Hilt annotations or modules remain anywhere in the codebase — Koin is the sole DI framework
  2. Each `core-*`/`feature-*` Gradle module declares its own Koin `module { }`, composed through a single `initKoin()` entry point
  3. Platform-specific dependencies (Android `Context`, `SharedPreferences`, etc.) are supplied via `expect`/`actual` declarations or per-platform Koin modules
  4. ViewModels and Stores receive all dependencies through constructor injection — no `get()` calls appear outside the DI wiring layer
  5. A `core-testing` module centralizes shared test fakes/doubles (in the spirit of `DevFestNantesStoreMocked`) for use across feature modules, and a `checkModules()` Koin test runs in CI to verify the complete DI graph

**Plans**: TBD

### Phase 5: Test Coverage Retrofit

**Goal**: Business-logic test coverage is measurably improved on a hardened test-fixture foundation, prioritizing ViewModels, Store/repository, and GraphQL mappers over a strict percentage target
**Depends on**: Phase 4
**Requirements**: TEST-01, TEST-02, TEST-03, TEST-04
**Success Criteria** (what must be TRUE):

  1. The known blocking test fragilities are fixed first: `StoreStubs.kt`'s RNG is seeded and `SimpleDateFormat` thread-safety issues are resolved in the tested code paths
  2. `commonTest` coverage of Store/repository logic and GraphQL-to-model mappers is extended into the new `core-data`/feature modules, following the existing `DevFestNantesStoreContractTest` pattern
  3. ViewModel test coverage is extended across feature modules, made practical by constructor injection via Koin
  4. Compose smoke tests on critical Android screens are maintained or extended per feature module (`androidTest`), built on the shared `core-testing` fakes

**Plans**: TBD

## Progress

**Execution Order:**
Phases execute in numeric order: 1 → 2 → 3 → 4 → 5

| Phase | Plans Complete | Status | Completed |
|-------|----------------|--------|-----------|
| 1. CI Pipeline Fixed & Optimized | 3/3 | Complete    | 2026-09-17 |
| 2. Dependency & Build Tooling Upgrade | 3/5 | In Progress|  |
| 3. Multi-Module Architecture Extraction | 0/TBD | Not started | - |
| 4. Hilt to Koin DI Migration | 0/TBD | Not started | - |
| 5. Test Coverage Retrofit | 0/TBD | Not started | - |
