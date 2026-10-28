# Phase 7: KMP Domain Layer and Android Adoption - Research

**Researched:** 2026-10-28
**Domain:** Kotlin Multiplatform domain extraction (pure policies + a combined Flow use case), common store interfaces with Android/iOS persistence, Android adoption with strict parity, Kotlin/Swift characterization tests
**Confidence:** HIGH on repo facts, real-data facts and the iOS-test spike; MEDIUM on Swift-side tooling details (see Assumptions Log)

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

**Use-case API shape**
- **D-01:** `:core:domain` exposes **both** pure policies (filter, group by day/slot, sort, classify, speaker index, venue language) **and** a combined `ObserveAgendaUseCase` that combines `DevFestNantesStore.agenda`/sessions, `SessionFiltersStore` and `BookmarksStore` into one Flow. Android `AgendaViewModel` consumes the combined flow in this phase; iOS will consume it in Phase 9 (IOSDATA-03, continuous observation). The combined flow must reproduce today's Android combine semantics exactly (re-emission on agenda, filter and bookmark changes; refresh cancels and restarts).
- **D-02:** The combined flow emits a **fully shaped model: days → time slots → sessions**, already filtered, grouped and sorted (in-slot order by `RoomSortIndex`). Shape along the lines of `AgendaDay(date, slots: List<TimeSlot(start: Instant, sessions)>)`; exact names are Claude's discretion. Each UI only formats labels (slot time in its locale, day label). `groupSessionsByStartTime` leaves `feature/agenda/.../AgendaColumn.kt`.
- **D-03:** Use cases are **classes with named methods** (e.g. `ObserveAgendaUseCase.observe()`, `FilterSessionsUseCase.filter(...)`), not `operator invoke` and not top-level functions, so they read well from Swift. Flow-returning methods carry `@NativeCoroutines`. Registered with `factory {}` in `domainModule`. A `CoroutineDispatcher` is injected where work is offloaded (default `Dispatchers.Default`), matching the named-dispatcher pattern from Phase 5.
- **D-04:** The **ViewModel keeps** `UiState`, `traceDataLoading`, refresh/cancel and error handling. The use case emits data only (no Loading/Error result type). Uncaught store failures stay as pinned v1.0 debt.

**Time zone and slot grouping**
- **D-05:** Use cases take the **`TimeZone` (and a `Clock` wherever "now" matters) as explicit parameters**. Each platform passes `TimeZone.currentSystemDefault()` (strict parity). Tests pin at least two time zones (SHARED-07).
- **D-06:** The **slot key is the start `Instant`** (parsed `scheduleSlot.startDate`). Android's current key (locale short-time string, device tz) and iOS's (exact `Date`) are equivalent on real data; characterization on the recorded API snapshot (D-09) must prove it.
- **D-07:** **Day assignment is an explicit parameter** (e.g. a `DayAssignment` with an "Android: 24 h UTC windows from `DAY_ONE`/`DAY_TWO`" strategy, as in `Agenda.Builder`, and an "iOS: local date of the start in a given time zone" strategy, as in `AgendaViewModel.sessionsChanged`). Each platform keeps its current behavior. The hardcoded 2025 dates stay a single-source constant (known debt).
- **D-08:** **Scroll to the current time slot stays per platform.** Android keeps `currentStartTimeIndex`, adapted to the new slot model with an identical result; iOS keeps its own. Partner categories also stay per platform unless their semantics are already identical (SHARED-04); the iOS room-order deviation (adopting `RoomSortIndex`) is documented.

**Characterization assets**
- **D-09:** **Real API snapshot = recorded GraphQL JSON.** Run the app's real query against confetti-app.dev once, commit the raw response as a test resource, and replay it through the existing Apollo MockServer harness (`GraphQLStoreMockServerTest` pattern) to build real `Session`/`Speaker`/`Room` models (110 speakers, room ids, check `room.id == room.name`). Tests stay offline and deterministic. A small documented script or task re-records the snapshot.
- **D-10:** **Current iOS behavior** is characterized by **Swift Testing tests in `DevFestNantesTests`**: they pin today's iOS output (filtering in `AgendaView`, `sessionsChanged` grouping) on the recorded snapshot and write **golden expected-output files**. Kotlin `commonTest` runs the shared use case with the iOS parameters against the same goldens. A minimal, behavior-preserving extraction of the `AgendaView` filter code is allowed to make it testable.
- **D-11:** Characterization commits land **before** each logic move and are green on the old code. The move commit keeps them green unchanged (SHARED-07).
- **D-12:** **Android parity proof = before/after screenshots.** The agent captures agenda screens with the `android` CLI on `main` and on the branch: both days, a few slots, room order, each filter type, favorites, and favorites after relaunch. A **human checkpoint** approves the diff (same pattern as the Phase 6 smoke). This is in addition to the v1.0 tests staying green.

**SessionFilter move and Swift names**
- **D-13:** The Kotlin types keep their final names, **`SessionFilter` + top-level `SessionFilterType`** (not nested). The iOS app's own `SessionFilter`/`FilterType` keep shadowing the import until Phase 9 deletes them. The Swift app must compile unchanged. `swift-names-gate.sh` baselines are regenerated with the new exports in the same commit and the gate passes. If a Swift file ever needs both types, use module qualification (`shared.SessionFilter`); no `@ObjCName`.
- **D-14:** `SessionFilter` becomes a **`data class`** (value equality, required for shared use and iOS). Before the move, characterize what the Android drawer/UI observably does with today's identity equality. Any observable difference is recorded as a documented deviation or debt. The Android persisted filter JSON stays **byte-identical** (`@SerialName` as needed for the enum moved to top level).
- **D-15:** **Clean approach for iOS persistence: legacy iOS data is intentionally NOT migrated.** The 2025 edition is over and session ids change every edition, so old favorites and filters are worthless. The user explicitly accepts losing them.
  - Android: unchanged. Keeps `selected_sessions` (string set) and `SHARED_PREFERENCES_KEY_AGENDA_FILTERS` (string set of per-filter JSON), because the v1.0 Robolectric tests pin both and must pass unchanged.
  - The **shared Kotlin filter codec is the Android one** (the current `kotlinx.serialization` JSON per filter). No separate lowercase iOS codec.
  - iOS (`core:data` iosMain, hand-written `NSUserDefaults`): **new keys** (names are Claude's discretion, must differ from `Favorites`/`AgendaSessionFilters`) using the shared codec. It **never reads the legacy keys**. Removing the legacy keys is deferred.
  - No legacy payload capture is needed (this replaces the earlier "Codable + device capture" choice).
  - **Reversibility:** one-way. Once Phase 9 ships the iOS app on the new keys, v1.0 iOS favorites and filters are unreachable from the app. Going back would need a migrator reading `Favorites` (`[String]`) and `AgendaSessionFilters` (Swift `JSONEncoder` `Data`, lowercase enum) before the legacy keys are ever removed.
- **D-16:** Planning docs follow D-15 in the same commit as this CONTEXT.md: SHARED-05, SHARED-07, IOSDATA-02 (REQUIREMENTS.md), Phase 7 criteria 2/4 + research flag and Phase 9 criterion 1 (ROADMAP.md), the target-features line and a Key Decision in PROJECT.md.

### Claude's Discretion
- Exact type and class names in `:core:domain` (`AgendaDay`/`TimeSlot`/`DayAssignment`…), package layout and how many use-case classes. Avoid Swift-colliding names (`Section`, `Task`, `Duration`).
- Speaker A-Z index and venue language: share them when Android and iOS semantics are already identical, otherwise use an explicit parameter or keep them per platform (SHARED-04). Decide after characterization.
- iOS `NSUserDefaults` key names and the exact stored types (e.g. a string array of per-filter JSON).
- Plan slicing (e.g. module skeleton, then characterization, then one move per logic area, then Android adoption, then stores), as long as each step leaves CI green and D-11 holds.
- How `HomeViewModel`/`BookmarksViewModel` adopt the shared stores and use cases.
- Format and location of the golden files shared between Swift tests and Kotlin `commonTest`.

### Deferred Ideas (OUT OF SCOPE)
- Removing the obsolete iOS `Favorites`/`AgendaSessionFilters` `UserDefaults` keys (cleanup), in a later phase or as debt.
- Grouping in the event time zone (Europe/Paris) instead of device time zone: a visible change, deferred.
- Unifying "scroll to the current time slot" across platforms (research D6).
- Replacing the hardcoded 2025 event dates with data-driven days (`InitialDayUseCase`): debt.
- Loading/Error result type from use cases (the ViewModel ERROR state is v1.0 debt).
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| SHARED-01 | `:core:domain` KMP module (`devfest.*` plugin) exported by `:shared`, `domainModule` in `sharedModules`, `verify()` clean | Module skeleton recipe, Koin verify consequences (JVM graph has no `BookmarksStore`), CI/Kover/ios.yml wiring, swift-names baseline |
| SHARED-02 | `SessionFilter`/`SessionFilterType` in `:core:model`; filter rule as a shared use case with the matrix in `commonTest` (JVM + iOS sim) | Verified current Android algorithm, iOS differences (room by name), codec byte-identity, data-class consequences |
| SHARED-03 | Day/time-slot grouping with `RoomSortIndex` in-slot sort as a shared use case; iOS room-order deviation documented | Verified Android group/sort semantics, iOS grouping semantics, `DayAssignment` design, real-snapshot facts |
| SHARED-04 | Classification, speaker A-Z index, venue language shared where identical; divergent behaviors per platform | Per-item verdicts (classification, venue language: share; speaker index: share after snapshot proof; scroll, partners, day tz: per platform) |
| SHARED-05 | `BookmarksStore`/`SessionFiltersStore` common interfaces; Android keeps keys and format; iOS `NSUserDefaults` new keys + shared codec; no legacy migration | Store placement in `:core:data`, Android impl move, iOS impl sketch, simulator test approach |
| SHARED-06 | Android ViewModels consume shared use cases; `SessionFiltersService` removed, Koin binding replaced in the same commit; no regression | Consumer inventory, atomic-commit rule, test-edit boundary, parity proof plan |
| SHARED-07 | Characterization tests (recorded snapshot, 2 time zones, iOS and Android behavior) before each logic move | Snapshot recording recipe, verified iOS-test file access spike, golden pipeline, Swift extraction plan |
</phase_requirements>

## Summary

The logic to move is small and fully readable: Android filtering is `AgendaViewModel.filterSessions` (OR within a type, AND across types, bookmarks by membership), grouping is `AgendaColumn.groupSessionsByStartTime` (key = locale short time in the device zone, in-slot sort by `room.name` then a stable `room.sortIndex` sort), day assignment is `Agenda.Builder` (24 h UTC windows). iOS re-implements the same ideas differently in `AgendaView.swift`/`AgendaViewModel.swift` (exact `Date` key, room sorted by name only, local-date day label, room filter compared by room *name*). On the real 2025 data the two are equivalent except the documented room order: I verified against the live API that all 94 sessions have whole-minute starts, `room.id == room.name` for every room, and 17 sessions have no room.

Three facts shape the plan. (1) The shared snapshot must be **read from files**, not embedded as constants: the full `GetSessions` response is about 213 KB and `GetSpeakers` about 92 KB, and I proved in a throwaway spike that Kotlin/Native `iosSimulatorArm64Test` can read repo files if Gradle passes an absolute directory through a `SIMCTL_CHILD_`-prefixed environment variable (the simulator process cwd is the CoreSimulator data directory, so relative paths fail). (2) `GraphQLStore` and the mappers are `internal` to `:core:data`, so the raw GraphQL snapshot can only be turned into `Session` models inside `:core:data` tests; `:core:domain` tests need a neutral, committed model snapshot (JSON) produced from it. (3) The legacy iOS storage is out of scope (D-15), so iOS persistence is greenfield: new `NSUserDefaults` keys written with the Android filter codec.

The riskiest parts are not the algorithms but the seams: Koin `verify()` on the JVM graph (no preference-backed stores exist there), two `StateFlow` holders over one SharedPreferences key if the old service and the new store are ever bound together, LazyColumn duplicate-key crashes if the slot header key stays the formatted label, Gradle test tasks that do not re-run when only a golden file changes, and the swift-names baselines (`SessionFilter` will newly appear as a referenced name that binds to a Swift struct, not to Kotlin).

**Primary recommendation:** Build `:core:domain` as a plain `devfest.kmp.library` module (KSP + KMP-NativeCoroutines like `:core:data`), keep the store interfaces and platform stores in `:core:data`, drive every move with characterization tests that read one shared `testdata/` directory (Kotlin via `SIMCTL_CHILD_`/env path, Swift via `#filePath`), switch all Android consumers and delete `SessionFiltersService` in a single commit, and gate with the full AGENTS.md CI list plus `:core:domain:iosSimulatorArm64Test` (add it to `ios.yml`).

## Project Constraints (from CLAUDE.md)

`CLAUDE.md` imports `AGENTS.md`; the actionable directives:

- Modules are declared in `settings.gradle.dcl`; new Gradle modules apply a `devfest.*` convention plugin, never hand-configured Android/Kotlin.
- Every dependency used in a `build.gradle.kts` must be declared in `gradle/libs.versions.toml`.
- Dependencies flow one way (`androidApp → feature:* → core:ui → core:domain → core:data → core:network / core:model`); feature modules never depend on each other; cross-feature effects go through lambdas handled in `androidApp`.
- A new `DevFestNantesStore` method must be added to both `GraphQLStore` and `DevFestNantesStoreMocked` in the same change (this phase should not need one).
- Koin only: constructor injection, `koinViewModel()`, service-locator access only in `di/` packages, `*Module.kt`, `DevFestNantesApplication`, `MainActivity`, `IosDependencies`. Never reintroduce Hilt/Dagger. **Koin overriding is disabled: no binding declared in two modules.** `bash scripts/di-gate.sh` enforces it.
- ViewModels expose `StateFlow`s; mutable state stays private.
- Do not bump kotlinx-datetime past the `0.8.0-0.6.x-compat` pin; new common code uses `kotlin.time.Instant`/`kotlin.time.Clock` (guarded by `OpenFeedbackDatetimeLinkageTest`).
- Never change `versionName`, `versionCode`, `MARKETING_VERSION`.
- Tests: JUnit 4 + `kotlin.test` + Turbine, hand-written fakes in `core/testing`, fixtures in `core/model/.../stubs/`, **no mocking library**; test names are snake_case sentences. iOS: Swift Testing, no mocking library.
- After changing any Kotlin type exported to Swift (`core:model`, `core:data`, `core:analytics`, and `core:domain` from Phase 7) run `bash scripts/swift-names-gate.sh check`; regenerate `scripts/swift-names/` baselines only in the same commit as the matching Swift call-site update, never to silence a failing check.
- Write all `.planning/**` artifacts in English.
- Update `README.md` / `documentation/` in the same change when a module, command or version the docs describe changes.
- Conventional commits; before done, run the CI list: `./gradlew detekt lint`, `bash scripts/di-gate.sh`, `bash scripts/kmp-nc-version-gate.sh`, `./gradlew testDebugUnitTest jvmTest testAndroidHostTest`.
- iOS logging uses `DevFestLogger`, never `print`/`os_log`, in new Swift code.
- User workflow memory: commit/push finished artifacts immediately (usage limits can cut an agent off mid-run) - plans should be slices that each end in a committed, green state.
- Tooling gotcha: the user's `rtk` hook rewrites `grep` output into summaries ("N matches in 0 files"). Executors should use the Grep tool, `git grep`, or `rtk proxy grep` when they need raw lines.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Session filtering (OR/AND/bookmarks), grouping, in-slot sort, classification, speaker index, venue-language mapping | Shared domain (`:core:domain`, KMP commonMain) | — | Pure functions of models; identical on both platforms once parameters (day assignment, time zone) are explicit |
| Combined agenda stream (sessions + filters + bookmarks) | Shared domain | Platform ViewModel | The flow is shared; `UiState`, tracing, refresh/cancel stay in the ViewModel (D-04) |
| `SessionFilter`/`SessionFilterType`, filter JSON codec | Shared model (`:core:model`) / data (`:core:data` commonMain) | — | The type is a plain model; the codec belongs with the stores that persist it |
| `BookmarksStore`/`SessionFiltersStore` interfaces | Shared data (`:core:data` commonMain) | — | `BookmarksStore` already lives there; no module churn (research ARCHITECTURE.md) |
| Persistence implementations | Platform data (`:core:data` androidMain SharedPreferences, iosMain NSUserDefaults) | — | Android keeps keys/format byte-identical; iOS gets new keys with the same codec |
| Slot header label, day tab label, "scroll to current slot" | Platform UI (Compose / SwiftUI) | — | Locale formatting and wall-clock scroll stay per platform (D-02, D-08) |
| Partner category layout | Platform UI | — | Semantics differ (Android omits `PARTNERS`, iOS shows every category sorted by ordinal) |
| Koin wiring (`domainModule`, store bindings) | Per-module DI (`:core:domain`, `:core:data` platform actuals) | `:shared` composition root | One module per Gradle module, `allowOverride(false)` |
| Swift-visible naming contract | Build gate (`scripts/swift-names-gate.sh`) | — | New exports must be baselined in the same commit |

## Standard Stack

No new external libraries are needed. Everything below is already in `gradle/libs.versions.toml`.

### Core
| Library | Version | Purpose | Why Standard |
|---------|---------|---------|--------------|
| Kotlin / KGP | 2.4.21 | KMP module, `kotlin.time.Instant` | `kotlin = "2.4.21"` [VERIFIED: gradle/libs.versions.toml `kotlin = "2.4.21"`] |
| kotlinx-coroutines | 1.11.0 | `Flow`, `combine`, `flowOn`, test dispatchers | `kotlinxCoroutines = "1.11.0"` [VERIFIED: gradle/libs.versions.toml] |
| kotlinx-datetime | 0.8.0-0.6.x-compat | `TimeZone`, `LocalDate` only (use `kotlin.time.Instant`/`Clock`) | Pinned for OpenFeedback; guarded by a linkage test [VERIFIED: gradle/libs.versions.toml `kotlinxDatetime = "0.8.0-0.6.x-compat"`] |
| kotlinx-serialization-json | 1.12.0 | Filter JSON codec (already used by `SessionFiltersServiceImpl`) | `kotlinxSerialization = "1.12.0"` [VERIFIED: gradle/libs.versions.toml] |
| Koin | 4.2.2 | `domainModule`, `verify()` | Project DI [VERIFIED: gradle/libs.versions.toml `koin = "4.2.2"`] |
| KMP-NativeCoroutines + KSP | 1.0.6 / 2.3.12 | `@NativeCoroutines`/`@NativeCoroutinesState` on Flow members | Same plugins as `:core:data` [VERIFIED: gradle/libs.versions.toml `kmpNativeCoroutines = "1.0.6"`, `ksp = "2.3.12"`] |

### Supporting (tests)
| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| kotlin-test, kotlinx-coroutines-test, Turbine | per catalog | Added to every `devfest.kmp.library` `commonTest` by the convention plugin | All `:core:domain` tests |
| Robolectric | 4.17 | Android grouping characterization (locale + default time zone) and SharedPreferences stores | `feature:agenda` tests; `:core:data` `androidHostTest` |
| Apollo MockServer | 0.3.1 | Replays the recorded raw GraphQL responses through the real `GraphQLStore` | `:core:data` commonTest only (the store is `internal`) |
| Swift Testing | Xcode toolchain | iOS characterization and iOS store/golden tests | `DevFestNantesTests` |

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Raw snapshot read from files via env var | Large Kotlin string constants (the Phase 5 `RecordedResponses` pattern) | Constants work but the full sessions response (~213 KB) plus speakers (~92 KB) is too big to review and risks JVM per-literal limits; files also serve Swift |
| `SessionFiltersStore` in `:core:data` | Interface in `:core:domain` | `BookmarksStore` is already in `:core:data`; the Android impl and iOS impl live there too; domain would force `data → domain` (wrong direction) |
| Named dispatcher binding inside `domainModule` | Reuse `IO_DISPATCHER` from `:core:ui` | `:core:ui` is above domain and cannot be seen; Koin forbids a second binding of the same name (research ARCHITECTURE.md) |

**Installation:** none. New module `:core:domain`:
```kotlin
// core/domain/build.gradle.kts (mirror core/data/build.gradle.kts)
plugins {
    id("devfest.kmp.library")
    alias(libs.plugins.ksp)
    alias(libs.plugins.kmp.native.coroutines)
}
kotlin {
    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))
            api(project(":core:data"))
            implementation(libs.koin.core)
        }
    }
}
```
`core:data` uses `api(project(":core:model"))` and `implementation(project(":core:network"))`; `:core:domain` needs `api` on `:core:data` because use-case constructors expose `DevFestNantesStore`/`BookmarksStore`/`SessionFiltersStore` [ASSUMED: derived from core/data/build.gradle.kts structure].

**Version verification:** no new packages, so no registry check is required. Versions above were read from `gradle/libs.versions.toml` in this session.

## Package Legitimacy Audit

This phase installs **no new external packages**. Every dependency is already in `gradle/libs.versions.toml` and resolved by earlier phases.

| Package | Registry | Age | Downloads | Source Repo | Verdict | Disposition |
|---------|----------|-----|-----------|-------------|---------|-------------|
| (none) | — | — | — | — | — | No installation |

**Packages removed due to [SLOP] verdict:** none
**Packages flagged as suspicious [SUS]:** none
If the planner decides to add one (for example a golden-file helper), it must go through `gsd-tools query package-legitimacy check` first.

## Architecture Patterns

### System Architecture Diagram

```
                         ┌────────────── recorded once (script) ──────────────┐
 confetti-app.dev  ──►   core/data/testdata/…/GetRooms|GetSessions|GetSpeakers.json   (raw, committed)
                                                  │ replayed by Apollo MockServer (GraphQLStoreMockServerTest pattern)
                                                  ▼
                              GraphQLStore (internal) → Session/Room/Speaker models
                                                  │ dumped by a :core:data JVM test (drift-checked)
                                                  ▼
                       core/domain/testdata/agenda/{sessions,rooms,speakers}.snapshot.json   (neutral, committed)
                                  │                                   │
        read via SIMCTL_CHILD_/env dir                         read via #filePath
                                  ▼                                   ▼
   :core:domain commonTest (JVM + iOS sim)               DevFestNantesTests (Swift Testing)
   shared policies + ObserveAgendaUseCase                 extracted pure iOS filter/group functions
        │  assert == golden/*.expected.json ◄── golden files written by the Swift pins, asserted both sides

 RUNTIME
 DevFestNantesStore.sessions ─┐
 SessionFiltersStore.filters ─┼─► ObserveAgendaUseCase.observe(dayAssignment) ─► Flow<List<DaySchedule>>
 BookmarksStore.bookmarkedIds ┘        (combine → filter → assign day → group by start Instant → sort in slot)
                                                       │
            Android AgendaViewModel (UiState, trace, refresh/cancel) ─► Compose (labels, scroll index)
            iOS (Phase 9) ─► SwiftUI

 PERSISTENCE (same SessionFiltersStore / BookmarksStore interfaces)
   Android: SharedPreferences  "selected_sessions", "SHARED_PREFERENCES_KEY_AGENDA_FILTERS"  (unchanged)
   iOS:     NSUserDefaults     new keys (never the legacy Favorites / AgendaSessionFilters), same JSON codec
```

### Recommended Project Structure
```
core/domain/
├── build.gradle.kts
├── testdata/agenda/                      # shared Kotlin+Swift inputs and goldens (NOT a resources dir)
└── src/
    ├── commonMain/kotlin/com/gdgnantes/devfest/core/domain/
    │   ├── agenda/                       # DaySchedule, TimeSlot, DayAssignment, Filter/Group/Classify/ObserveAgenda use cases
    │   ├── speakers/                     # SpeakerIndexUseCase
    │   ├── venue/                        # VenueLanguageUseCase
    │   └── di/DomainModule.kt            # @HiddenFromObjC val domainModule
    ├── commonTest/…                      # policies vs goldens, flow semantics, expect fun readTestData
    ├── jvmTest/…  iosTest/…              # actual readTestData + DomainModuleTest (verify)
core/data/src/commonMain/…/SessionFiltersStore.kt, SessionFilterCodec.kt
core/data/src/androidMain/…/SharedPreferencesSessionFiltersStore (keeps ctor (SharedPreferences))
core/data/src/iosMain/…/UserDefaultsBookmarksStore, UserDefaultsSessionFiltersStore
core/model/src/commonMain/…/SessionFilter.kt  (data class + top-level enum SessionFilterType)
core/testing/…/FakeSessionFiltersStore.kt     (+ binding in testDoublesModule)
```
Package of the module is `com.gdgnantes.devfest.core.domain` (derived by `moduleNamespace()`: path `:core:domain` → `com.gdgnantes.devfest.core.domain`) [VERIFIED: build-logic/convention/.../ProjectExtensions.kt `"com.gdgnantes.devfest." + path.removePrefix(":").replace(':', '.')…`].

### Verified current behavior to reproduce (the parity contract)

**Filtering (Android = the reference).** [VERIFIED: feature/agenda/.../AgendaViewModel.kt:112-167]
- No filters: every session passes.
- Otherwise a session must match, for **every filter type present**, **at least one** filter of that type (the code builds a per-type set, unions all, then `retainAll` each type set = intersection).
- Match rules: `BOOKMARK` → `bookmarksStore.isBookmarked(session.id)` (the filter `value` is ignored); `LANGUAGE` → `filter.value == session.language?.name`; `COMPLEXITY` → `filter.value == session.complexity?.name`; `ROOM` → `filter.value == session.room?.id`; `TYPE` → `filter.value == session.type?.name`. A session with a null language/complexity/room/type never matches a filter of that type.
- Quote (lines 164-166): `val origin = sessionsByFilterType.values.flatten().toMutableSet()` / `sessionsByFilterType.values.forEach { origin.retainAll(it) }` / `return origin.toList()`.
- The no-filter branch uses `hashSetOf<Session>()` (line 116) and the result is then `sortedBy { session -> session.scheduleSlot.startDate }` (line 106): sessions sharing a start time come out in an **unspecified** order (the existing test says so: "Sessions sharing a start time come out in an unspecified order (HashSet iteration)"). The shared version should keep input (store) order, which is a deterministic subset of the old behavior.

**iOS filtering differences** [VERIFIED: iosApp/iosApp/Agenda/AgendaView.swift:216 `selectedRooms.contains(s.room)`; iosApp/iosApp/Model/Data/AgendaContent.swift:55 `room: session.room?.name ?? ""`]: iOS compares the filter value (which the room menu fills with `room.id`) against the room **name**. They agree only because `room.id == room.name` in the real data (verified below). The shared rule uses the id (Android). All other filter types and the favorites filter agree with Android; sections that end up empty are not shown on either platform.

**Grouping and in-slot order (Android).** [VERIFIED: feature/agenda/.../AgendaColumn.kt:104-128 and 62-63]
- Key = `DateFormat.getTimeInstance(DateFormat.SHORT, locale)` in `TimeZone.getDefault()`; sessions whose start cannot be parsed are dropped (`.filter { pair -> pair.first.isNotEmpty() }`); groups keep first-appearance order of the `startDate`-string-sorted list; in-group `sortedBy { session -> session.room?.name }`, then in the LazyColumn `sessions.sortedBy { session -> session.room?.sortIndex }`. Two stable sorts = ordering by (`sortIndex`, `name`), **nulls first** for sessions without a room; sessions with equal `sortIndex` and name keep their incoming (arbitrary) order.
- `Room.sortIndex` already exists on the model, computed from `RoomSortIndex` at mapping time [VERIFIED: core/data/.../domain/RoomSortIndex.kt:5-17, quote `"Tour de Bretagne" -> 3` … `else -> Int.MAX_VALUE`]. Real ids `Tour Bretagne` and `Unconf` therefore sort last (known debt FUT-02, do not fix).

**Grouping (iOS).** [VERIFIED: iosApp/iosApp/Agenda/AgendaViewModel.swift:120-134, 154-163] `Dictionary(grouping:)` by `getDate(date:)` (default-option `ISO8601DateFormatter`, falls back to `Date()` on failure), keys sorted, per-section `.sorted { $0.room < $1.room }` (name only), day label `DateFormatter("yyyy-MM-d")` in the device zone. Known: fractional-second starts fall back to "now" on iOS (pinned by `AgendaDateParsingTests`); the shared code parses them (recorded deviation, never occurs in real data).

**Day assignment.** Android: `Agenda.Builder` [VERIFIED: core/model/.../Agenda.kt:14-19 `startInstant.minus(DAY_ONE).inWholeDays == 0L` / `… DAY_TWO …`], constants `DAY_ONE_ISO = "2025-10-16T00:00:00Z"`, `DAY_TWO_ISO = "2025-10-17T00:00:00Z"` (lines 31-32). Note `inWholeDays == 0L` also accepts starts up to 24 h *before* the day start (truncation toward zero); sessions outside both windows are dropped; both days always exist (possibly empty). iOS: local date in the device zone. They diverge for devices far from UTC+2 (an 18:30+02:00 session is 05:30 the next day in Pacific/Auckland); that is exactly why D-07 makes it a parameter.

**Classification.** [VERIFIED: SessionTypeUtils.kt:5-6 `fun SessionType.isService() =` / `this == SessionType.OPENING || this == SessionType.BREAK || this == SessionType.LUNCH`; iosApp/iosApp/Model/Data/AgendaContent.swift:30-37 `isATalk`: nil type or `.lunch, .opening, .break_` → false; MainActivity.kt:151-157 and AgendaView.swift:103 navigable = `QUICKIE`/`CONFERENCE`/`CODELAB`]. "Service" (no bookmark star) and "navigable" are identical on both platforms, including the null-type case (`session.type?.isService() == false` is false for null on Android). Verdict: **share** both as `ClassifySessionUseCase`. `KEYNOTE`, `PARTY` (null) are neither service nor navigable.

**Venue language.** [VERIFIED: feature/venue/.../utils (`Locale.toContentLanguage`): `"fr" -> FRENCH`, `"en" -> ENGLISH`, `else -> ENGLISH`; VenueViewModel.swift:22-28: nil code → `.english`, `"fr"` → `.french`, else english]. Identical mapping from a language-code string. Verdict: **share** as `VenueLanguageUseCase.languageFor(code: String?)`. Keep the Android quirk that no value is emitted when the system locale is null (decide in the ViewModel, not in the use case).

**Speaker A-Z index.** Android: `List<Speaker>.getMapOfFirstUniqueCharIndex()` maps `name.lowercase().first()` to the first index in the **store-ordered** list [VERIFIED: Speakers.kt:166-175]; iOS: unique `name.prefix(1).uppercased()` sorted, plus a filter per letter [VERIFIED: SpeakersViewModel.swift:58-69]. The live API returns speakers alphabetical by id, so first letters are non-decreasing and both give the same index on the real data; they differ for an empty name (`first()` throws on Android) or a leading emoji/surrogate. Verdict: **share after the snapshot proves equality** (default recommendation: letters = sorted unique uppercased first grapheme, `speakers(letter)` keeps store order; Android derives its first-index map from the shared grouping).

**Partner categories.** Android shows five fixed groups and ignores `PartnerCategory.PARTNERS`; iOS lists every category sorted by `ordinal` [VERIFIED: feature/about/.../partners/PartnersViewModel.kt (reads PXL, PLATINIUM, GOLD, VIRTUAL, VELOTYPE only); iosApp/.../AboutViewModel.swift `partners.keys.sorted()`]. Verdict: **stay per platform** (D-08).

**Scroll to current slot.** Stays per platform. Android `currentStartTimeIndex` [VERIFIED: AgendaColumn.kt:135-151] counts rows+headers of groups whose first session's `endDate` is before `now`; adapt its input from `Map<String, List<Session>>` to `List<TimeSlot>` (mechanical edit of `CurrentStartTimeIndexTest` construction only).

### Pattern 1: Pure policy classes + one combined flow
**What:** stateless classes with named methods (`FilterSessionsUseCase.filter`, `GroupSessionsUseCase.group`, `ClassifySessionUseCase`, `SpeakerIndexUseCase`, `VenueLanguageUseCase`) and `ObserveAgendaUseCase.observe(dayAssignment)` returning `Flow<List<DaySchedule>>` annotated `@NativeCoroutines`.
**When to use:** always for logic that both platforms need; the combined flow is what the ViewModel collects.
**Key rules:**
- Read `store.sessions` **inside** `observe()` (never cache the flow in a property): `onRefresh()` cancels the job and re-collects, and the cold Apollo flow with `CacheAndNetwork` is what triggers the network refresh [VERIFIED: AgendaViewModel.kt:64-84 `autoRefreshJob?.cancel()`; GraphQLStore `fetchPolicy(FetchPolicy.CacheAndNetwork)`].
- `combine(store.sessions, filtersStore.filters, bookmarksStore.bookmarkedSessionIds)` re-emits on any of the three and mirrors the old chain `agenda → combine(filters) → combine(bookmarks)`; compute with `flowOn(dispatcher)` (named, default `Dispatchers.Default`). Use the emitted bookmark **set** for membership, not `isBookmarked`.
- Emit **data classes** (`DaySchedule`, `TimeSlot`) so the ViewModel's `MutableStateFlow` conflates equal recomputes like it does today.
- Always emit every configured day (Android shows two tabs even when a day is empty).
- Do not reuse the name `AgendaDay` (it exists in `:core:model` and in the Swift header baseline). Recommended names: `DaySchedule(dayIndex: Int, date: String, slots: List<TimeSlot>)`, `TimeSlot(start: Instant, sessions: List<Session>)`. No Swift collision exists for `TimeSlot`/`DayAssignment`/`DaySchedule` (no Swift identifier in `iosApp/**/*.swift` matches) [VERIFIED: `git grep -w` over iosApp Swift sources returned no hits for TimeSlot, DayAssignment, AgendaDay, DaySchedule, SessionFilterStore, SpeakerIndex].
- `DayAssignment`: expose as small factory functions or a sealed type with `utcWindows(days)` (Android, same arithmetic as `Agenda.Builder`) and `localDate(timeZone, days)` (iOS). Avoid default arguments (not visible from Swift) and avoid generic `Map`/`Pair` in results (render badly on iOS, research PITFALLS #7).
- `Clock` is only needed if a use case needs "now"; with scroll staying per platform (D-08), none does. Do not add an unused `Clock` parameter.

### Pattern 2: Store interfaces in `:core:data`, platform impls in platform source sets
- `SessionFiltersStore { @NativeCoroutinesState val filters: StateFlow<Set<SessionFilter>>; fun setFilters(filters: Set<SessionFilter>) }`. `@NativeCoroutinesState` requires a `StateFlow` and generates `…Value` and `…Flow` Swift members [CITED: github.com/rickclephas/KMP-NativeCoroutines README].
- Android impl: the existing `SessionFiltersServiceImpl` body, renamed and moved to `core/data/src/androidMain`, constructor `(SharedPreferences)` kept so the pinned Robolectric test only changes its class name/imports. Key `"SHARED_PREFERENCES_KEY_AGENDA_FILTERS"` and one JSON string per filter [VERIFIED: SessionFiltersService.kt:22 and `Json.encodeToString(SessionFilter.serializer(), it)`].
- The codec (`SessionFilterCodec` in `commonMain`) encodes/decodes with the same `Json.encodeToString(SessionFilter.serializer(), …)` call and skips malformed entries (`SerializationException`, `IllegalArgumentException`). `Timber` is Android-only: either keep logging in the Android impl via a callback or drop the log line (non-behavioral).
- iOS impls (`iosMain`): hand-written over `NSUserDefaults`, constructor takes the `NSUserDefaults` instance (tests pass a unique suite), state held in a `MutableStateFlow`, updates with `update {}` (the Android `BookmarksStoreImpl` mutates a `HashSet` unsafely; do not copy that). Store ids as a string array and filters as a string array of codec JSON. New key names must differ from `Favorites` and `AgendaSessionFilters` (for example `devfest.bookmarks.sessionIds` and `devfest.agenda.filters`). Never read the legacy keys.
- Bindings: Android in `dataPlatformModule.android.kt` (`single { SharedPreferencesSessionFiltersStore(get()) } bind SessionFiltersStore::class`), iOS in `dataPlatformModule.ios.kt` (currently `module { }`), JVM stays empty.

### Pattern 3: Characterize, then move (D-11)
Order per logic area: (1) commit characterization green on the **old** code, (2) move/adapt code with those tests unchanged apart from mechanical edits, (3) keep them as the permanent suite.

### Anti-Patterns to Avoid
- **Two holders over one preference key.** Binding the new `SessionFiltersStore` while `SessionFiltersServiceImpl` is still bound gives two `StateFlow`s over `SHARED_PREFERENCES_KEY_AGENDA_FILTERS`; the agenda and `HomeViewModel` would diverge. Introduce the class and interface unbound, then switch every consumer, delete the service and bind the store in one commit (SHARED-06).
- **Header key = formatted label.** `stickyHeader(timeKey)` uses the label as the LazyColumn key. With Instant-keyed slots, two starts that differ only by seconds would share a label and crash with a duplicate key. Key by `slot.start` (or index).
- **Returning Android-shaped generics** (`Map<Int, …>`, `Pair`) from shared APIs; use `List<DaySchedule>`.
- **Hand-rolling the day windows twice.** `DayAssignment.utcWindows` and `Agenda.Builder` must be proven equal by a test on the snapshot; do not let them drift (one source for `DAY_ONE`/`DAY_TWO`).
- **Putting `kotlinx.datetime.Instant` in new code.** Use `kotlin.time.Instant`; `kotlinx.datetime.TimeZone`/`LocalDate` are fine.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Filter persistence format | A new lowercase/iOS codec | The existing `kotlinx.serialization` JSON per filter (`SessionFilter.serializer()`) | D-15; byte-identical on Android; one codec both sides |
| Swift-callable Flows | Manual `Flow` wrappers/callbacks | `@NativeCoroutines` / `@NativeCoroutinesState` (KMP-NativeCoroutines 1.0.6, already wired) | Same plugin as `:core:data`; version gate already in CI |
| Time-zone and ISO parsing | `java.text`/`SimpleDateFormat`/`NSDateFormatter` in common code | `kotlin.time.Instant.parse`/`toInstantOrNull` (in `:core:model` utils) and `kotlinx.datetime.TimeZone` | Common, already used by `Agenda.Builder` and `DateUtils` |
| Koin graph checks | Custom reflection tests | `org.koin.test.verify.verify` (`DataModuleTest` pattern) | Existing convention |
| Recording the real API | A custom downloader app | A `curl` script using each query's generated `OPERATION_DOCUMENT` (already documented in `RecordedResponses.kt`) | Reproducible, no code |
| Test doubles | A mocking library | Hand-written `FakeSessionFiltersStore` next to `FakeBookmarksStore` | Project rule (no mocking library) |
| Swift name-collision detection | Eyeballing the header | `scripts/swift-names-gate.sh` (check, selftest, baseline) | Catches identity swaps the v1.0 gate missed |

**Key insight:** the logic is small; the cost is in the seams (DI, naming, persistence, test data). Spend the effort on characterization and gates, not on cleverness in the algorithms.

## Runtime State Inventory

This phase moves a persisted type (`SessionFilter`), deletes a service and adds new persistence. Answers per category:

| Category | Items Found | Action Required |
|----------|-------------|-----------------|
| Stored data | Android SharedPreferences (default prefs file): `selected_sessions` (string set), `SHARED_PREFERENCES_KEY_AGENDA_FILTERS` (string set of JSON). The JSON holds only `type` and `value`; **the class name is not serialized**, so moving/renaming the class and enum does not change stored bytes [VERIFIED: SessionFilter.kt:5-13 and the literal asserts in SessionFiltersServiceImplTest `{"type":"LANGUAGE","value":"FRENCH"}`]. iOS legacy `UserDefaults` keys `Favorites`, `AgendaSessionFilters` stay untouched and unread (D-15). | Code edit only on Android (no data migration). iOS: new keys, no migration. |
| Live service config | None - there is no external service holding these names. | None - verified by inspection of the module graph. |
| OS-registered state | None. | None. |
| Secrets/env vars | None. The recording script uses only the public header `conference: devfestnantes2025` (already in `core/network` source). | None. |
| Build artifacts / installed packages | Swift-names baselines (`scripts/swift-names/shared-h-baseline.txt`, `referenced-names.txt`, `members-baseline.txt`) are generated from the framework header and must be regenerated in the same commit as each export change; `core/network/build/generated/**` is derived (not committed). Gradle `kover(project(…))` list in root `build.gradle.kts` enumerates modules explicitly. | Regenerate baselines; add `:core:domain` to the Kover list and to `settings.gradle.dcl`; add the iOS test task to `ios.yml`. |

## Common Pitfalls

### Pitfall 1: iOS Kotlin tests cannot find repo files (relative paths)
**What goes wrong:** a test that reads `testdata/...` relatively fails on `iosSimulatorArm64Test`.
**Why:** the simulator test process runs with cwd = the CoreSimulator device data directory. Spike output: `SPIKE cwd=/Users/robin/Library/Developer/CoreSimulator/Devices/6D9E4FF3-1D57-4277-BA27-F91A0776E1BB/data text=null` [VERIFIED: throwaway worktree, `:core:model:iosSimulatorArm64Test`, Kotlin 2.4.21].
**How to avoid:** pass an absolute directory. A plain `environment("X", …)` on `KotlinNativeSimulatorTest` is **not** visible (`SPIKE env=null`), but the `SIMCTL_CHILD_` prefix is: `SPIKE env=/…/core/model` and `SPIKE len=399` (file read through `NSString.stringWithContentsOfFile`) [VERIFIED: same spike]. Use an `expect fun readTestData(name: String): String` in `commonTest` with `jvmTest` (env/system property) and `iosTest` (`NSProcessInfo.processInfo.environment`) actuals. **Declare the data directory as a task input** (`inputs.dir(…)`) on the JVM `Test` and `KotlinNativeSimulatorTest` tasks, otherwise editing a golden does not re-run the tests (up-to-date check).
**Warning signs:** tests pass locally from cache after a golden edit; iOS lane reports `cannot read`.

### Pitfall 2: Koin `verify()` on the JVM graph
**What goes wrong:** `KoinGraphTest.shared_modules_graph_is_complete` (`module { includes(sharedModules) }.verify()`) starts failing once `domainModule` needs `BookmarksStore`/`SessionFiltersStore`, which have no JVM binding (the JVM `dataPlatformModule` is `module { }`).
**How to avoid:** list the two interfaces as `extraTypes` in that test (they are bound by the Android and iOS actuals), or add JVM in-memory actuals. Prefer `extraTypes` (no dead production code). Add a `DomainModuleTest` in `jvmTest` mirroring `DataModuleTest` (verify with the upstream types, and an `assertFailsWith<MissingKoinDefinitionException>` case proving verify bites). Update `AndroidGraphVerifyTest.app_module_is_complete_given_its_upstream_types` (`SessionFiltersService::class` → `SessionFiltersStore::class`) and `AgendaModuleTest`.
**Named dispatcher:** declare `single<CoroutineDispatcher>(named("domainDispatcher")) { Dispatchers.Default }` inside `domainModule`; the Android graph already shows a named `CoroutineDispatcher` binding satisfies `verify()` of a consumer in the full graph [ASSUMED: inferred from AndroidGraphVerifyTest, which lists no `CoroutineDispatcher` extra type for the full graph but does for the isolated module]. Remove `IO_DISPATCHER` and its pinning test if nothing else consumes it after the ViewModel stops taking it (planner decision; `IO_DISPATCHER` is bound in `CoreUiModule.kt:19` and used only by `AgendaModule`).

### Pitfall 3: swift-names baselines
**What goes wrong:** the gate regenerates `referenced-names.txt` as (header names) ∩ (capitalized identifiers in tracked Swift files) minus `names-exclude.txt`. Exporting Kotlin `SessionFilter` makes `SessionFilter` a "referenced" name (the Swift struct has that name), and its member set is then pinned as if Swift used the Kotlin type.
**How to avoid:** add `SessionFilter` to `scripts/swift-names/names-exclude.txt` with the per-name evidence the file header demands (`iosApp/iosApp/Model/SessionFilter.swift` defines `struct SessionFilter`), then run `baseline` and commit baselines together with the export change. Remove the exclusion when Phase 9 deletes the Swift struct. New header names such as `Kotlinx_datetimeTimeZone` and `Kotlinx_coroutines_coreCoroutineDispatcher` are informational in `check` (only missing names, `_` collisions, member-set changes and the `_`-suffix count fail). `@HiddenFromObjC` targets are PROPERTY, FUNCTION and CLASS only, **not constructors** [CITED: kotlinlang.org/api/core/kotlin-stdlib/kotlin.native/-hidden-from-obj-c/], so a public use-case constructor taking a dispatcher or `TimeZone` exposes those platform-prefixed types; mark `domainModule` itself `@HiddenFromObjC` like `dataModule`.
**Verification step the plan must include:** build the iOS app (`xcodebuild … build`) in the commit that exports `SessionFilter`, to prove the Swift struct still wins name lookup (D-13) [ASSUMED: Swift module-local declarations shadow imported ones].

### Pitfall 4: Strict-parity trap in `SessionFilter` becoming a `data class`
**What goes wrong:** behavior that depended on identity equality changes silently (for example `StateFlow` conflation of an equal-content set, `Set` de-duplication in the drawer).
**How to avoid (D-14):** before the move, add Android characterization tests that pin: (a) the drawer produces `remove then add` so duplicates never occur; (b) `setFilters` with a new set of equal-content filters re-emits today (identity equality) and will conflate after; assert the observable consequences on `days`/`sessionFilters` and record any difference as a documented deviation. The existing test file says "SessionFilter is a plain class with identity equality (characterized, not changed)" [VERIFIED: SessionFiltersServiceImplTest.kt, comment above `pairs()`] and compares `type to value` pairs, so it stays valid under value equality.
**JSON:** `@Serializable data class SessionFilter(val type: SessionFilterType, val value: String)` with declaration order `type`, `value`; enum entries serialize by entry name, so `@SerialName` is not needed [ASSUMED: kotlinx.serialization default enum encoding; the literal-JSON Robolectric assertions are the proof and must run unchanged on the moved code].

### Pitfall 5: "unchanged" tests are not literally unchanged
`AgendaViewModelTest` imports `…feature.agenda.utils.SessionFilter` and nested `FilterType`, builds `AgendaViewModel(store, bookmarks, NoOpPerformanceMonitoring(), filters, dispatcher)`, and reads `days.value.getValue(day).sessions` [VERIFIED: AgendaViewModelTest.kt:16-17, 38-45, 47-48]. After the move those lines change (imports, constructor wiring, slot-shaped selectors). Define "unchanged" for the plan as: **no expected value, key literal, JSON literal or ordering assertion is edited; only mechanical imports, type renames, constructor wiring and selector helpers change**, and say so in each plan's acceptance criteria. Same for `SessionFiltersServiceImplTest` (class under test and imports only, moved to `:core:data` `androidHostTest`), `HomeViewModelTest` (fake implements `SessionFiltersStore`), `CurrentStartTimeIndexTest` (builds `TimeSlot`s), `AgendaModuleTest`, `AgendaSmokeTest` (loads `domainModule`, asserts through `SessionFiltersStore`).

### Pitfall 6: Android grouping cannot be characterized as written
`groupSessionsByStartTime` is `private` in `AgendaColumn.kt:104`. The characterization commit must make it `internal` (pure visibility change) and test it with Robolectric under at least two default time zones and locales (precedent: `DateUtilsCharacterizationTest` sets `TimeZone.setDefault` and restores it). Assert: group order, labels, in-slot order including no-room sessions first, and that sessions of one slot with different `sortIndex` are ordered by it.

### Pitfall 7: Swift tests must not create a ViewModel
Debug builds skip Firebase and Koin when XCTest hosts the app, and `AgendaViewModel.init` starts Koin; documented rule: test static or pure helpers [CITED: documentation/DEVELOPMENT.md "Swift tests"]. D-10's allowed extraction means moving `sessionsChanged` and `getFilteredSessions` into pure static/free functions (parameterized by `TimeZone`), then calling them from the view model/view unchanged. Every new Swift file needs explicit `project.pbxproj` entries (PBXBuildFile, PBXFileReference, group, Sources phase): the project does not use file-system-synchronized groups [VERIFIED: pbxproj lines list each test file explicitly, e.g. `3C5A91E7B2D04F6810A7C3E9 /* AgendaDateParsingTests.swift */`]. Whether the test target can `import shared` (needed to build Kotlin `Session` values) is not yet proven (Assumption A2): make it the first task of the iOS characterization plan.

### Pitfall 8: Recording the real snapshot
The app's `speakers` query is the deprecated field and returns **100** speakers while the server holds **110** (`speakersPage(first: 200)` returns 110) - the known debt FUT-02. The "110 speakers" in the research flag is the server total; the characterization snapshot of what the app actually receives has 100, and the A-Z test must be written against that (document the gap, do not fix). Record `GetRooms`, `GetSessions`, `GetSpeakers` (the three the agenda/speakers use). `GetSessions` is `sessions(first: 100)` and the event has 94, so nothing is truncated.

### Pitfall 9: Gradle config details for the new module
- `settings.gradle.dcl` needs `include(":core:domain")`; root `build.gradle.kts` Kover list needs `kover(project(":core:domain"))` (its comment says "13 production modules").
- `devfest.android.feature` convention (AndroidFeatureConventionPlugin.kt) must add `implementation(project(":core:domain"))`; `androidApp` needs it too if it references domain types.
- `ios.yml` line 162 runs `:shared:iosSimulatorArm64Test :core:model:… :core:data:… :core:testing:…`; add `:core:domain:iosSimulatorArm64Test` [VERIFIED: .github/workflows/ios.yml:162]. Without it SC#2 "on iOS simulator" is unenforced.
- `kmp-native-coroutines` warns when an exposed `Flow`/suspend function lacks an annotation: annotate every Flow member of public use cases.
- Detekt thresholds: constructor parameters ≤ 7, `LongMethod` 160 lines, config in `linters/detekt-config.yml`; KMP modules scan only `commonMain`, `androidMain`, `iosMain`.

### Pitfall 10: Parity screenshots (D-12)
Real data comes from the live API, the agenda picks its start tab and scroll position from the wall clock, and moving the device clock breaks SSL (user memory: shifted-clock testing needs prefetch). Capture `main` and the branch in one sitting on the same AVD, locale and zone, never shift the clock, and use explicit scrolling/taps instead of relying on the initial scroll. After all event slots have ended (the 2025 event is over), `currentStartTimeIndex` scrolls to the end on both builds, so both start at the same place.

## Code Examples

Sketches derived from the verified semantics above (not copied from a library).

### Filter semantics (shared policy)
```kotlin
// Equivalent to AgendaViewModel.kt:112-167: every present type must match at least one of its filters.
class FilterSessionsUseCase {
    fun filter(
        sessions: List<Session>,
        filters: Set<SessionFilter>,
        bookmarkedIds: Set<String>
    ): List<Session> {
        if (filters.isEmpty()) return sessions
        val byType = filters.groupBy { it.type }
        return sessions.filter { session ->
            byType.all { (type, group) -> group.any { matches(session, type, it.value, bookmarkedIds) } }
        }
    }

    private fun matches(s: Session, type: SessionFilterType, value: String, bookmarked: Set<String>) =
        when (type) {
            SessionFilterType.BOOKMARK -> s.id in bookmarked
            SessionFilterType.LANGUAGE -> value == s.language?.name
            SessionFilterType.COMPLEXITY -> value == s.complexity?.name
            SessionFilterType.ROOM -> value == s.room?.id
            SessionFilterType.TYPE -> value == s.type?.name
        }
}
```

### Slot grouping and in-slot order (shared policy)
```kotlin
// Key = start Instant (D-06). Order = (room.sortIndex, room.name), null room first, stable otherwise.
private val roomOrder =
    compareBy<Session>({ it.room?.sortIndex }, { it.room?.name })   // null sorts first

fun group(sessions: List<Session>): List<TimeSlot> =
    sessions
        .sortedBy { it.scheduleSlot.startDateAsEpochMilliseconds }   // chronological, not string order
        .groupBy { Instant.fromEpochMilliseconds(it.scheduleSlot.startDateAsEpochMilliseconds) }
        .map { (start, inSlot) -> TimeSlot(start, inSlot.sortedWith(roomOrder)) }
```
`ScheduleSlot` parses in its constructor (`Instant.parse(startDate)`), so an unparseable start cannot reach the use case [VERIFIED: ScheduleSlot.kt:5-10].

### Combined flow skeleton
```kotlin
class ObserveAgendaUseCase(
    private val store: DevFestNantesStore,
    private val filtersStore: SessionFiltersStore,
    private val bookmarksStore: BookmarksStore,
    private val dispatcher: CoroutineDispatcher,
) {
    @NativeCoroutines
    fun observe(dayAssignment: DayAssignment): Flow<List<DaySchedule>> =
        combine(store.sessions, filtersStore.filters, bookmarksStore.bookmarkedSessionIds) { sessions, filters, bookmarked ->
            // filter → assign days → group → sort (pure policies)
        }.flowOn(dispatcher)
}
```
`store.sessions` is evaluated on each `observe()` call (the getter builds a new cold flow).

### Passing the test-data directory to JVM and iOS-simulator tests (verified mechanism)
```kotlin
// core/domain/build.gradle.kts
val testData = layout.projectDirectory.dir("testdata").asFile
tasks.withType<org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest>().configureEach {
    environment("SIMCTL_CHILD_DOMAIN_TESTDATA", testData.absolutePath)   // prefix is stripped by simctl
    inputs.dir(testData)
}
tasks.named<Test>("jvmTest") {
    environment("DOMAIN_TESTDATA", testData.absolutePath)
    inputs.dir(testData)
}
// iosTest actual: NSProcessInfo.processInfo.environment["DOMAIN_TESTDATA"] + NSString.stringWithContentsOfFile
```
The `SIMCTL_CHILD_` mechanism and `NSString.stringWithContentsOfFile` read were verified; `inputs.dir` and the jvm `environment` wiring are standard Gradle but were not run here.

### iOS store shape (new keys, shared codec)
```kotlin
class UserDefaultsSessionFiltersStore(private val defaults: NSUserDefaults) : SessionFiltersStore {
    private val state = MutableStateFlow(load())
    override val filters: StateFlow<Set<SessionFilter>> = state.asStateFlow()
    override fun setFilters(filters: Set<SessionFilter>) {
        state.value = filters
        defaults.setObject(filters.map(SessionFilterCodec::encode), forKey = KEY)   // KEY ≠ "AgendaSessionFilters"
    }
    private fun load(): Set<SessionFilter> =
        (defaults.stringArrayForKey(KEY) ?: emptyList<Any?>()).mapNotNull { SessionFilterCodec.decodeOrNull(it as? String) }.toSet()
}
```

## Real-data facts (live API, queried 2026-10-28)

[VERIFIED: `curl https://confetti-app.dev/graphql` with header `conference: devfestnantes2025`, the app's own query shapes]

| Fact | Value |
|------|-------|
| Sessions returned by `sessions(first: 100)` | 94 (no truncation) |
| Session types (raw) | break 12, codelab 19, conference 41, keynote 1, lunch 2, opening 2, party 1, quickie 16; `party` maps to `null` in `toSessionType()` (Mappers.kt:118-129) |
| Sessions without a room / with one room | 17 / 77 (none with two) |
| Rooms | 8: `Belem`, `Hangar`, `Jules Verne`, `L'Atelier`, `Les Machines`, `Titan`, `Tour Bretagne`, `Unconf` |
| `room.id == room.name` for every session room | yes (0 mismatches) |
| Start value format | `2025-10-16T08:00` (no seconds, no zone); the mapper appends `:00+02:00` (Mappers.kt:164-166) so every `startDate` is `…:00+02:00` with whole-minute starts (33 distinct starts over the two days) |
| Session languages (raw) | `fr-FR` 81, `en-US` 13 |
| `speakers` (deprecated field the app queries) | 100 |
| `speakersPage(first: 200)` | 110 |
| Unique speaker ids across sessions | 108 |
| Speaker order | alphabetical by id; first letters non-decreasing; no digit/non-Latin initials |
| Raw sizes | `GetSessions` (full fields) ≈ 213 KB, `GetSpeakers` ≈ 92 KB |

Implication for D-06: with whole-minute starts in one uniform offset, "formatted short time" and "exact Instant" keys produce the same groups, and chronological vs string ordering of `startDate` coincide. Both claims must still be proven by a committed test on the snapshot (SHARED-07), because they stop holding if a start carries seconds or a `Z` suffix.

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| `kotlinx.datetime.Instant`/`Clock` | `kotlin.time.Instant`/`Clock` (`kotlinx.datetime.Clock.System` no longer resolves through the typealias) | Phase 2/5 (kotlinx-datetime 0.8.0) | New common code must import `kotlin.time.*`; `OpenFeedbackDatetimeLinkageTest` guards the bridge |
| Android-only `SessionFiltersService` (feature-owned) | `SessionFiltersStore` common interface in `:core:data` | This phase | HomeViewModel and the agenda read the same store; iOS gets an `NSUserDefaults` impl |
| `Dispatchers.IO` named binding in `:core:ui` for the agenda | Domain-owned named `Dispatchers.Default` binding | This phase | `IO_DISPATCHER` likely becomes dead; remove with its pinning test if unused |

**Deprecated/outdated:** the GraphQL `speakers` field (100 of 110) stays as debt; `Tour de Bretagne` in `RoomSortIndex` never matches the real id `Tour Bretagne` (debt).

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | In Swift, a module-local `struct SessionFilter` wins name lookup over the imported Kotlin `SessionFilter` in files that `import shared` (D-13), so the app compiles unchanged | Pitfall 3 | iOS CI build breaks in the export commit; mitigation is an `xcodebuild build` in that commit and, if needed, `shared.`-qualification of Swift call sites |
| A2 | `DevFestNantesTests` can `import shared` (framework found through the implicit built-products framework search path; no pbxproj search-path edit) | Pitfall 7 | Swift tests cannot build Kotlin `Session` values; fallback is test-target `FRAMEWORK_SEARCH_PATHS` in `project.pbxproj` |
| A3 | Moving the enum to top level does not change serialized enum values (entries serialize by name), so `@SerialName` is unnecessary | Pitfall 4 | Persisted Android filters stop loading; caught by the literal-JSON Robolectric tests, which must run on the moved code before merge |
| A4 | A named `CoroutineDispatcher` single in `domainModule` satisfies Koin `verify()` for the consumer in the shared graph | Pitfall 2 | `KoinGraphTest` fails; fix with `extraTypes` |
| A5 | `:core:domain` needs `api(project(":core:data"))` (constructor types are public) | Standard Stack | Compile errors in consumers; trivial to adjust |
| A6 | Time zone ids `Europe/Paris`, `America/Los_Angeles`, `Pacific/Auckland` resolve on JVM and Kotlin/Native in kotlinx-datetime | Validation | Tests fail on one runtime; pick ids that resolve on both |
| A7 | Equal-content `SessionFilter` sets no longer re-emit through `StateFlow` after the data-class change, with no visible UI effect | Pitfall 4 | A visible difference must be recorded as a deviation (D-14); characterization decides |
| A8 | iOS instrumentation of the speakers A-Z default (share iOS grouping, Android derives first-index map) is acceptable to the user | Verified current behavior | SHARED-04 allows per-platform if not; decide after the snapshot test |

## Open Questions (RESOLVED)

1. **What does "v1.0 tests pass unchanged" allow?**
   - What we know: the ViewModel, store and test constructors/imports must change (Pitfall 5).
   - What's unclear: whether the user reads "unchanged" as "no assertion edits" or "no file edits".
   - Recommendation: adopt "no assertion/expected-value/literal edits, mechanical wiring only" and state it in the plans.
   - **RESOLVED (D-18, confirmed by the user 2026-10-28):** wiring-only edits. Imports, type renames, constructor/Koin wiring and selector helpers may change; no expected value, assertion or literal may change. Each plan that touches a v1.0 test or a characterization checks it on the diff.
2. **Where do the shared golden/snapshot files live and who writes them?**
   - What we know: format and location are discretionary (CONTEXT); Kotlin reads through `SIMCTL_CHILD_`/env, Swift can read through `#filePath` (no pbxproj resource plumbing) [ASSUMED for `#filePath` on CI].
   - Recommendation: `core/domain/testdata/agenda/` for the neutral inputs and goldens; raw GraphQL under `core/data/testdata/`; a script `scripts/record-agenda-snapshot.sh` (curl + `OPERATION_DOCUMENT`) plus a JVM test that fails when the neutral snapshot drifts from the raw one.
   - **RESOLVED (planning, CONTEXT discretion):** raw responses in `core/data/testdata/graphql/` recorded by a gated JVM recorder wrapped by `scripts/record-agenda-snapshot.sh` (07-03); neutral snapshot, filter cases and format README in `core/domain/testdata/agenda/` with a drift-check test (07-04); goldens in `core/domain/testdata/agenda/golden/`, written only by the tests when absent, never by hand (07-05, 07-06, 07-08). Speaker data is pinned per D-17 (the app's 100 speakers; the server's 110 is metadata).
3. **Does the user accept the speaker A-Z default?** (A8) Needs the snapshot test result first.
   - **RESOLVED (by rule, with a decision gate):** the index is shared only if it reproduces both platforms' goldens on the recorded data; plan 07-13 Task 1 stops with a `checkpoint:decision` (keep per platform vs explicit parameter, SHARED-04) if either golden cannot be matched.
4. **Who removes `IO_DISPATCHER`?** Only if nothing else consumes it after Android adoption; otherwise leave it.
   - **RESOLVED:** plan 07-15 removes it in the commit that moves `AgendaViewModel` off it (its last consumer), with its pin test; the agenda dispatcher is then pinned by `DomainModuleTest` (07-13).
5. **R8 release smoke:** `@Serializable` class moved modules; v1.0 validated a release build on a device. Recommend one human release-build check (filters survive a relaunch) at the phase checkpoint.
   - **RESOLVED:** plan 07-19 Task 1 runs a release (R8) build smoke (filter and bookmark survive a force-stop and relaunch, no `SerializationException`/`ClassNotFoundException` in logcat), reviewed at its blocking human checkpoint.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| JDK | Gradle | ✓ | OpenJDK 25.0.3 (CI uses 17) | — |
| Gradle wrapper | all builds | ✓ | 9.8.1 (seen in build output) | — |
| Xcode | iOS build, Swift tests | ✓ | 27.1 (27A9275) (CI selects the newest installed Xcode) | — |
| iOS simulators | `iosSimulatorArm64Test`, Swift tests | ✓ | iPhone 18 Pro, 17, 17e, Air, … | pick with `xcrun simctl list devices available` |
| SwiftGen | iOS build phase | ✓ | /opt/homebrew/bin/swiftgen | `brew install swiftgen` |
| `GoogleService-Info.plist` | iOS app build | ✓ locally (gitignored); CI creates a mock | — | CI mock step |
| `android` CLI | D-12 screenshots | ✓ | 1.0.16261425 | raw `adb` |
| Android emulator AVD | D-12, `connectedDebugAndroidTest` | ✓ definition `Pixel_9` (not running) | — | physical device |
| `adb` | device interaction | ✓ | platform-tools (daemon started this session) | — |
| `jq`, `gh`, `curl` | recording script, CI inspection | ✓ | — | — |
| Network to `confetti-app.dev` | recording the snapshot | ✓ (queried this session) | — | none needed after recording |
| Android SDK | AGP builds | ✓ (`local.properties` present) | — | — |

**Missing dependencies with no fallback:** none.
**Missing dependencies with fallback:** none. Note local JDK 25 vs CI JDK 17 and local Xcode 27.1 vs CI-selected Xcode: run the CI command list locally but expect the first real signal from CI.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | JUnit 4 + `kotlin.test` + Turbine + kotlinx-coroutines-test (KMP and Android); Robolectric 4.17 for Android host/unit tests that need Android classes; Swift Testing (Xcode toolchain) for `DevFestNantesTests`; Compose UI tests for instrumentation |
| Config file | convention plugin `build-logic/.../KmpLibraryConventionPlugin.kt` (adds `kotlin-test`, `kotlinx-coroutines-test`, `turbine` to `commonTest`); `iosApp/iosApp.xctestplan` |
| Quick run command | `./gradlew :core:domain:jvmTest` |
| Full suite command | `./gradlew detekt lint && bash scripts/di-gate.sh && bash scripts/kmp-nc-version-gate.sh && ./gradlew testDebugUnitTest jvmTest testAndroidHostTest` plus the iOS lane below |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| SHARED-01 | `domainModule` in `sharedModules`, `verify()` clean, no duplicate binding, iOS start resolves use cases | unit (JVM) + iOS sim | `./gradlew :core:domain:jvmTest :shared:jvmTest` ; `./gradlew :shared:iosSimulatorArm64Test` | ❌ Wave 0 (`DomainModuleTest`; extend `KoinGraphTest`, `IosKoinTest`) |
| SHARED-01 | Swift names gate passes with the new exports | gate | `bash scripts/swift-names-gate.sh check && bash scripts/swift-names-gate.sh selftest` | ✅ script exists; baselines regenerate |
| SHARED-02 | OR within type / AND across types / bookmarks by membership, v1.0 matrix + extras (null fields, empty result) | unit commonTest | `./gradlew :core:domain:jvmTest` ; `./gradlew :core:domain:iosSimulatorArm64Test` | ❌ Wave 0 (`FilterSessionsUseCaseTest`) |
| SHARED-02 | `SessionFilter` JSON byte-identical after the move | Robolectric host | `./gradlew :core:data:testAndroidHostTest` | ✅ pins exist in `SessionFiltersServiceImplTest` (to be moved) |
| SHARED-03 | Day assignment (both strategies equal `Agenda.Builder` on the snapshot), slot grouping, `RoomSortIndex` order, ≥ 2 time zones | unit commonTest vs goldens | `./gradlew :core:domain:jvmTest :core:domain:iosSimulatorArm64Test` | ❌ Wave 0 (`GroupSessionsUseCaseTest`, `DayAssignmentTest`) |
| SHARED-03 | iOS room-order deviation documented | doc check | `git grep -n "RoomSortIndex" documentation/ARCHITECTURE.md` | ❌ doc task |
| SHARED-04 | classification, venue language, speaker index equal on both platforms | unit commonTest + Swift pin | `./gradlew :core:domain:jvmTest` ; Swift: command below | ❌ Wave 0 |
| SHARED-05 | Android keys/format unchanged; iOS new keys, shared codec, never reads legacy keys | Robolectric host + iOS sim (Kotlin) | `./gradlew :core:data:testAndroidHostTest` ; `./gradlew :core:data:iosSimulatorArm64Test` | ❌ Wave 0 (`UserDefaults*StoreTest`; `BookmarksStoreImplTest` ✅) |
| SHARED-06 | ViewModels on use cases; `SessionFiltersService` gone; Koin graph valid; behavior intact | unit + Robolectric + instrumentation | `./gradlew :feature:agenda:testDebugUnitTest :androidApp:testDebugUnitTest :core:ui:testDebugUnitTest :feature:venue:testDebugUnitTest :feature:speakers:testDebugUnitTest` ; `./gradlew :feature:agenda:connectedDebugAndroidTest` | ✅ existing suites (edited mechanically) |
| SHARED-06 | Agenda parity on device | manual + `android` CLI screenshots (D-12) | `android screen capture` on `main` vs branch; human checkpoint | n/a (checkpoint) |
| SHARED-07 | Snapshot facts (94 sessions, `room.id == room.name`, 100 speakers) and goldens | unit (`:core:data` commonTest/JVM drift check) | `./gradlew :core:data:jvmTest :core:data:iosSimulatorArm64Test` | ❌ Wave 0 |
| SHARED-07 | iOS current behavior pinned and goldens written/asserted | Swift Testing | see below | ❌ Wave 0 |

Swift command (simulator id from `xcrun simctl list devices available`):
```bash
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -testPlan iosApp \
  -destination 'platform=iOS Simulator,id=<udid>' -derivedDataPath build \
  -parallel-testing-enabled NO test
bash scripts/xcresult-assert-tests.sh <path-to>.xcresult   # fails when zero tests ran
```

### Sampling Rate
- **Per task commit:** `./gradlew :core:domain:jvmTest` (plus the touched module's `jvmTest`/`testDebugUnitTest`).
- **Per wave merge:** `./gradlew detekt lint && bash scripts/di-gate.sh && bash scripts/kmp-nc-version-gate.sh && ./gradlew testDebugUnitTest jvmTest testAndroidHostTest && ./gradlew :shared:iosSimulatorArm64Test :core:model:iosSimulatorArm64Test :core:data:iosSimulatorArm64Test :core:domain:iosSimulatorArm64Test :core:testing:iosSimulatorArm64Test && bash scripts/swift-names-gate.sh check`.
- **Phase gate:** the full list above, the Swift Testing run with the xcresult assertion, `connectedDebugAndroidTest` on an emulator, the D-12 screenshot checkpoint, and an Android release (R8) build smoke, all green before `/gsd-verify-work`.

### Wave 0 Gaps
- [ ] `core/domain/` module (build file, `settings.gradle.dcl` include, Kover entry, `ios.yml` test task, feature-plugin dependency) - nothing runs without it
- [ ] `core/domain/src/commonTest/…/TestData.kt` (`expect fun readTestData`) + `jvmTest`/`iosTest` actuals + Gradle env/`inputs.dir` wiring (verified mechanism above)
- [ ] `scripts/record-agenda-snapshot.sh` + committed raw responses + neutral `sessions/rooms/speakers.snapshot.json` + drift-check test in `:core:data`
- [ ] `FilterSessionsUseCaseTest`, `GroupSessionsUseCaseTest`, `DayAssignmentTest`, `ClassifySessionUseCaseTest`, `VenueLanguageUseCaseTest`, `SpeakerIndexUseCaseTest`, `ObserveAgendaUseCaseTest` (Turbine: re-emission on agenda/filter/bookmark change, restart on re-collect)
- [ ] `DomainModuleTest` (jvmTest), `KoinGraphTest` extra types, `IosKoinTest` resolving the use cases
- [ ] `FakeSessionFiltersStore` + `testDoublesModule` binding + `TestDoublesModuleTest` update
- [ ] Android characterization: internal `groupSessionsByStartTime` test (Robolectric, two zones/locales); `SessionFilter` identity-equality characterization (D-14)
- [ ] iOS characterization: extract pure filter/grouping functions, `AgendaFilteringTests`, `AgendaGroupingTests` (two zones), golden read/write helper, `UserDefaults*` store tests in Kotlin `iosTest`; pbxproj entries for each new file; prove `import shared` in the test target
- [ ] `names-exclude.txt` entry for `SessionFilter` and regenerated baselines in the export commit

## Security Domain

`security_enforcement` is enabled (ASVS level 1, block on high).

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | no | no accounts in this phase |
| V3 Session Management | no | — |
| V4 Access Control | no | local, unauthenticated conference data |
| V5 Input Validation | yes | Persisted filter strings are untrusted input on load: decode with `kotlinx.serialization`, catch `SerializationException`/`IllegalArgumentException`, skip bad entries (current behavior, pinned by `a_service_restores_the_saved_filters_and_skips_malformed_entries`); on iOS read `NSUserDefaults` values with safe casts (`as? String`), never force-cast |
| V6 Cryptography | no | no secrets stored |
| V8 Data Protection | yes (low) | Favorites/filters are non-sensitive preferences in `SharedPreferences`/`NSUserDefaults`; do not log filter payloads at error level beyond what exists |
| V14 Configuration / supply chain | yes | no new dependencies; recorded fixtures are public conference data only |

### Known Threat Patterns for this stack

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Malformed/tampered persisted JSON crashes startup | Denial of service | `decodeOrNull` per entry, empty set on failure; test with `"not-json"` on both platforms |
| Recording script leaks credentials or private data | Information disclosure | The API is public; the only header is the public conference id; commit raw JSON only after confirming it holds public speaker/session data (precedent T-5-07) |
| Uncatchable Kotlin exception crossing into Swift | Denial of service | Shared plain functions must not throw on bad input (return null/empty); annotate any throwing plain function with `@Throws` |
| Duplicate DI binding silently overriding a store | Tampering | `allowOverride(false)` + `verify()` + `di-gate.sh`; one binding per store type per platform |
| R8 strips serializer of the moved class | Denial of service | kotlinx.serialization consumer rules + a release-build smoke that reloads filters after relaunch |

## Sources

### Primary (HIGH confidence)
- Repository files read this session: `07-CONTEXT.md`, `REQUIREMENTS.md`, `STATE.md`, `AGENTS.md`, `AgendaViewModel.kt`, `AgendaColumn.kt`, `Agenda.kt` (feature and model), `AgendaPager.kt`, `SessionFiltersService.kt`, `SessionFilter.kt`, `SessionTypeUtils.kt`, `SessionFiltersDrawer.kt`, `AgendaRow.kt`, `AgendaModule.kt`, `HomeViewModel.kt`, `AppModule.kt`, `CoreUiModule.kt`, `BookmarksStore*.kt`, `DevFestNantesStore*.kt`, `GraphQLStore.kt`, `Mappers.kt`, `RoomSortIndex.kt`, `InitKoin.kt`, `IosDependencies.kt`, `KoinGraphTest.kt`, `DataModuleTest.kt`, `KmpLibraryConventionPlugin.kt`, `AndroidFeatureConventionPlugin.kt`, `swift-names-gate.sh`, `di-gate.sh`, `ios.yml`, `android.yml`, `project.pbxproj` (test target), iOS Swift files (`AgendaView`, `AgendaViewModel`, `AgendaContent`, `SpeakersViewModel`, `VenueViewModel`, `SessionFilter`, filter menus, `AboutViewModel`), existing tests (`AgendaViewModelTest`, `SessionFiltersServiceImplTest`, `CurrentStartTimeIndexTest`, `AgendaModuleTest`, `AndroidGraphVerifyTest`, `BookmarksStoreImplTest`, `AgendaSmokeTest`, `SessionFilterTests`, `AgendaDateParsingTests`), `.planning/research/{ARCHITECTURE,PITFALLS}.md`.
- Live API queries against `https://confetti-app.dev/graphql` (2026-10-28) for the "Real-data facts" table.
- Kotlin/Native spike in a throwaway git worktree (removed afterwards): iOS simulator test cwd, absolute-path read, `SIMCTL_CHILD_` environment pass-through.

### Secondary (MEDIUM confidence)
- KMP-NativeCoroutines README (`@NativeCoroutines`, `@NativeCoroutinesState`, StateFlow `…Value`, exposed-declaration warnings): https://github.com/rickclephas/KMP-NativeCoroutines/blob/master/README.md
- Kotlin API reference for `@HiddenFromObjC` (targets PROPERTY, FUNCTION, CLASS): https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.native/-hidden-from-obj-c/

### Tertiary (LOW confidence)
- Swift name-shadowing behavior (A1) and test-target `import shared` (A2): reasoned from Xcode defaults, not exercised.

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH - no new libraries; versions read from the catalog.
- Architecture: HIGH - module boundaries and wiring follow existing modules and the research ARCHITECTURE document; Koin verify details MEDIUM (A4).
- Pitfalls: HIGH for repo-derived items and the verified simulator-file-access spike; MEDIUM for Swift-side items (A1, A2).
- Parity facts: HIGH - every behavior was read from the current Android and iOS source and checked against live data.

**Research date:** 2026-10-28
**Valid until:** 2026-11-27 (stable; re-check if Kotlin, KMP-NativeCoroutines or Xcode are bumped, and re-record the snapshot only if the API shape changes)
