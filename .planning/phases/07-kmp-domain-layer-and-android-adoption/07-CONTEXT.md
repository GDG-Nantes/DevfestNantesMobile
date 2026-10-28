# Phase 7: KMP Domain Layer and Android Adoption - Context

**Gathered:** 2026-10-28
**Status:** Ready for planning

<domain>
## Phase Boundary

A new KMP module `:core:domain` (`devfest.*` convention plugin, exported by `:shared`, `domainModule`
appended to `sharedModules`) holds the agenda business logic: session filtering (OR within a type,
AND across types, bookmarks by membership), day and time-slot grouping with in-slot `RoomSortIndex`
sorting, service/navigable classification, the A-Z speaker index and the venue language choice.
`SessionFilter`/`SessionFilterType` move to `:core:model`. `BookmarksStore` and `SessionFiltersStore`
become common interfaces with an Android implementation (unchanged keys and format) and an iOS
implementation on `NSUserDefaults` (new keys, shared codec, see D-15). Android consumes all of it in
this phase, and `SessionFiltersService` is removed. iOS only has to compile and behave as before:
wiring the shared logic into the iOS app is Phase 9.

Out of this phase: any iOS ViewModel/UI change (Phases 8-9), fixing latent bugs (logged as debt),
unifying the divergent behaviors listed in D-08.

</domain>

<decisions>
## Implementation Decisions

### Use-case API shape
- **D-01:** `:core:domain` exposes **both** pure policies (filter, group by day/slot, sort,
  classify, speaker index, venue language) **and** a combined `ObserveAgendaUseCase` that combines
  `DevFestNantesStore.agenda`/sessions, `SessionFiltersStore` and `BookmarksStore` into one Flow.
  Android `AgendaViewModel` consumes the combined flow in this phase; iOS will consume it in Phase 9
  (IOSDATA-03, continuous observation). The combined flow must reproduce today's Android combine
  semantics exactly (re-emission on agenda, filter and bookmark changes; refresh cancels and restarts).
- **D-02:** The combined flow emits a **fully shaped model: days → time slots → sessions**, already
  filtered, grouped and sorted (in-slot order by `RoomSortIndex`). Shape along the lines of
  `AgendaDay(date, slots: List<TimeSlot(start: Instant, sessions)>)`; exact names are Claude's
  discretion. Each UI only formats labels (slot time in its locale, day label).
  `groupSessionsByStartTime` leaves `feature/agenda/.../AgendaColumn.kt`.
- **D-03:** Use cases are **classes with named methods** (e.g. `ObserveAgendaUseCase.observe()`,
  `FilterSessionsUseCase.filter(...)`), not `operator invoke` and not top-level functions, so they
  read well from Swift. Flow-returning methods carry `@NativeCoroutines`. Registered with `factory {}`
  in `domainModule`. A `CoroutineDispatcher` is injected where work is offloaded (default
  `Dispatchers.Default`), matching the named-dispatcher pattern from Phase 5.
- **D-04:** The **ViewModel keeps** `UiState`, `traceDataLoading`, refresh/cancel and error
  handling. The use case emits data only (no Loading/Error result type). Uncaught store failures stay
  as pinned v1.0 debt.

### Time zone and slot grouping
- **D-05:** Use cases take the **`TimeZone` (and a `Clock` wherever "now" matters) as explicit
  parameters**. Each platform passes `TimeZone.currentSystemDefault()` (strict parity). Tests pin at
  least two time zones (SHARED-07).
- **D-06:** The **slot key is the start `Instant`** (parsed `scheduleSlot.startDate`). Android's
  current key (locale short-time string, device tz) and iOS's (exact `Date`) are equivalent on real
  data; characterization on the recorded API snapshot (D-09) must prove it.
- **D-07:** **Day assignment is an explicit parameter** (e.g. a `DayAssignment` with an
  "Android: 24 h UTC windows from `DAY_ONE`/`DAY_TWO`" strategy, as in `Agenda.Builder`, and an
  "iOS: local date of the start in a given time zone" strategy, as in `AgendaViewModel.sessionsChanged`).
  Each platform keeps its current behavior. The hardcoded 2025 dates stay a single-source constant
  (known debt).
- **D-08:** **Scroll to the current time slot stays per platform.** Android keeps
  `currentStartTimeIndex`, adapted to the new slot model with an identical result; iOS keeps its own.
  Partner categories also stay per platform unless their semantics are already identical (SHARED-04);
  the iOS room-order deviation (adopting `RoomSortIndex`) is documented.

### Characterization assets
- **D-09:** **Real API snapshot = recorded GraphQL JSON.** Run the app's real query against
  confetti-app.dev once, commit the raw response as a test resource, and replay it through the existing
  Apollo MockServer harness (`GraphQLStoreMockServerTest` pattern) to build real
  `Session`/`Speaker`/`Room` models (110 speakers, room ids, check `room.id == room.name`). Tests stay
  offline and deterministic. A small documented script or task re-records the snapshot.
- **D-10:** **Current iOS behavior** is characterized by **Swift Testing tests in
  `DevFestNantesTests`**: they pin today's iOS output (filtering in `AgendaView`, `sessionsChanged`
  grouping) on the recorded snapshot and write **golden expected-output files**. Kotlin `commonTest`
  runs the shared use case with the iOS parameters against the same goldens. A minimal,
  behavior-preserving extraction of the `AgendaView` filter code is allowed to make it testable.
- **D-11:** Characterization commits land **before** each logic move and are green on the old code.
  The move commit keeps them green unchanged (SHARED-07).
- **D-12:** **Android parity proof = before/after screenshots.** The agent captures agenda screens with
  the `android` CLI on `main` and on the branch: both days, a few slots, room order, each filter type,
  favorites, and favorites after relaunch. A **human checkpoint** approves the diff (same pattern as the
  Phase 6 smoke). This is in addition to the v1.0 tests staying green.

### SessionFilter move and Swift names
- **D-13:** The Kotlin types keep their final names, **`SessionFilter` + top-level
  `SessionFilterType`** (not nested). The iOS app's own `SessionFilter`/`FilterType` keep shadowing
  the import until Phase 9 deletes them. The Swift app must compile unchanged. `swift-names-gate.sh`
  baselines are regenerated with the new exports in the same commit and the gate passes. If a Swift
  file ever needs both types, use module qualification (`shared.SessionFilter`); no `@ObjCName`.
- **D-14:** `SessionFilter` becomes a **`data class`** (value equality, required for shared use and
  iOS). Before the move, characterize what the Android drawer/UI observably does with today's identity
  equality. Any observable difference is recorded as a documented deviation or debt. The Android
  persisted filter JSON stays **byte-identical** (`@SerialName` as needed for the enum moved to top
  level).
- **D-15:** **Clean approach for iOS persistence: legacy iOS data is intentionally NOT migrated.**
  The 2025 edition is over and session ids change every edition, so old favorites and filters are
  worthless. The user explicitly accepts losing them.
  - Android: unchanged. Keeps `selected_sessions` (string set) and
    `SHARED_PREFERENCES_KEY_AGENDA_FILTERS` (string set of per-filter JSON), because the v1.0
    Robolectric tests pin both and must pass unchanged.
  - The **shared Kotlin filter codec is the Android one** (the current `kotlinx.serialization` JSON per
    filter). No separate lowercase iOS codec.
  - iOS (`core:data` iosMain, hand-written `NSUserDefaults`): **new keys** (names are Claude's
    discretion, must differ from `Favorites`/`AgendaSessionFilters`) using the shared codec. It
    **never reads the legacy keys**. Removing the legacy keys is deferred.
  - No legacy payload capture is needed (this replaces the earlier "Codable + device capture"
    choice).
  — **Reversibility:** one-way — once Phase 9 ships the iOS app on the new keys, v1.0 iOS favorites
  and filters are unreachable from the app. Going back would need a migrator reading `Favorites` (`[String]`)
  and `AgendaSessionFilters` (Swift `JSONEncoder` `Data`, lowercase enum) before the legacy keys are
  ever removed.
- **D-16:** Planning docs follow D-15 in the same commit as this CONTEXT.md: SHARED-05, SHARED-07,
  IOSDATA-02 (REQUIREMENTS.md), Phase 7 criteria 2/4 + research flag and Phase 9 criterion 1
  (ROADMAP.md), the target-features line and a Key Decision in PROJECT.md.

### Decisions confirmed during planning (2026-10-28)
- **D-17:** **Speaker characterization pins what the app loads today: 100 speakers** from the app's own
  speakers query. The server's 110 total is recorded only as snapshot metadata; the 100/110 gap stays
  debt (FUT-02). This refines D-09's "110 speakers" wording.
- **D-18:** **"v1.0 tests pass unchanged" (D-11, criterion 4) means wiring-only edits**: imports, type
  renames, constructor/Koin wiring and selector helpers may change; no expected value, assertion or
  literal may change. Each plan checks this on the diff.
- **D-19:** **D-05 stays literal.** The shared observe/group API takes a `TimeZone` on every platform
  (Android passes `TimeZone.currentSystemDefault()`); the Android UTC-window day strategy ignores it by
  design. Uniform API for Phase 9.
- **D-20:** **Empty speaker name: no crash, but report it.** v1.0 Android throws
  `NoSuchElementException`; the shared A-Z index skips the empty name instead, and Android records a
  non-fatal exception to Crashlytics (through the app's existing Crashlytics/logging path) so the bad
  data is visible. Accepted, documented behavior change (exception to the "no latent bug fixes" rule).

### Claude's Discretion
- Exact type and class names in `:core:domain` (`AgendaDay`/`TimeSlot`/`DayAssignment`…), package
  layout and how many use-case classes. Avoid Swift-colliding names (`Section`, `Task`, `Duration`).
- Speaker A-Z index and venue language: share them when Android and iOS semantics are already
  identical, otherwise use an explicit parameter or keep them per platform (SHARED-04). Decide after
  characterization.
- iOS `NSUserDefaults` key names and the exact stored types (e.g. a string array of per-filter JSON).
- Plan slicing (e.g. module skeleton, then characterization, then one move per logic area, then
  Android adoption, then stores), as long as each step leaves CI green and D-11 holds.
- How `HomeViewModel`/`BookmarksViewModel` adopt the shared stores and use cases.
- Format and location of the golden files shared between Swift tests and Kotlin `commonTest`.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Scope and requirements
- `.planning/ROADMAP.md` § Phase 7 — goal, success criteria, research flag (updated per D-16)
- `.planning/REQUIREMENTS.md` — SHARED-01..07 (SHARED-05/07 updated per D-16), IOSDATA-01..03 (Phase 9 consumers)
- `.planning/PROJECT.md` — parity constraint, Key Decisions table

### Research
- `.planning/research/SUMMARY.md` — domain layer recommendations, disagreements (now resolved by D-15 for persistence), pitfalls 4/7/8 (behavior drift, name collisions, Koin override)
- `.planning/research/ARCHITECTURE.md` — `:core:domain` placement, use-case conventions, `domainModule`
- `.planning/research/PITFALLS.md` — grouping/timezone drift, Swift name collisions
- `.planning/research/FEATURES.md` — table-stakes domain policies, defer list

### Prior phase context
- `.planning/phases/06-ios-test-foundation-and-ci-gates/06-CONTEXT.md` — `DevFestNantesTests` target, `swift-names-gate.sh` baselines, xcresult gate

### Codebase maps
- `.planning/codebase/ARCHITECTURE.md`, `.planning/codebase/CONVENTIONS.md`, `.planning/codebase/TESTING.md`

### Project rules
- `AGENTS.md` — module dependency direction, store interface rule, Koin rules, CI command list, swift-names gate rule
- `documentation/ARCHITECTURE.md`, `documentation/DEVELOPMENT.md` — to update with the new module

</canonical_refs>

<code_context>
## Existing Code Insights

### Logic to move (current locations)
- `feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaViewModel.kt` — `filterSessions` (OR within a type / AND across types, bookmarks via `bookmarksStore.isBookmarked`), combine of agenda + filters + bookmarks, per-day sort by `startDate`
- `feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaColumn.kt` — `groupSessionsByStartTime` (locale short time, `TimeZone.getDefault()`, sorted by `room.name`, then `sortedBy { room.sortIndex }` in the `LazyColumn`) and `currentStartTimeIndex` (stays per platform, D-08)
- `feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/utils/SessionFilter.kt` — plain `class` with nested `FilterType` (moves to `core:model`, D-13/D-14)
- `feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/services/SessionFiltersService.kt` — SharedPreferences key `SHARED_PREFERENCES_KEY_AGENDA_FILTERS`, string set of per-filter JSON (format kept, D-15). Removed with its Koin binding in the same commit (SHARED-06)
- `feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/utils/SessionTypeUtils.kt` — `isService()` (OPENING/BREAK/LUNCH)
- `core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/Agenda.kt` — `Agenda.Builder` day assignment (24 h windows from `DAY_ONE_ISO`/`DAY_TWO_ISO` 2025, D-07)
- `core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/domain/RoomSortIndex.kt` — hardcoded room order by `id`
- iOS: `iosApp/iosApp/Agenda/AgendaViewModel.swift` (`sessionsChanged`: groups by exact `Date`, sorts by `room` name, day label `yyyy-MM-d` device tz; `Favorites`/`AgendaSessionFilters` in `UserDefaults`), `iosApp/iosApp/Agenda/AgendaView.swift` (filtering, lines ~136-224), `iosApp/iosApp/Model/SessionFilter.swift`

### Reusable Assets
- `core/data/.../BookmarksStore.kt` — already a common interface with `@NativeCoroutines`. Android impl `BookmarksStoreImpl` (androidMain). **No iOS binding exists yet** (`dataPlatformModule` iosMain is empty)
- `core/testing` — `FakeBookmarksStore`, `TestDoublesModule`. Add `FakeSessionFiltersStore` there
- `core/model/.../stubs/` — Session/Room/Speaker/Partner fixtures
- `GraphQLStoreMockServerTest` — Apollo MockServer harness (JVM/Android/iOS) to replay the recorded snapshot (D-09)
- v1.0 tests that are the parity oracle: `feature/agenda/.../AgendaViewModelTest.kt` (filter matrix), `SessionFiltersServiceImplTest.kt`, `core/data/.../BookmarksStoreImplTest.kt`, `HomeViewModelTest.kt`, `BookmarksViewModelTest.kt`, `AgendaSmokeTest.kt`
- `iosApp/DevFestNantesTests/SessionFilterTests.swift` — existing Swift Testing tests (Phase 6)

### Established Patterns
- One Koin module per Gradle module, `allowOverride(false)`, `verify()` + `AndroidGraphVerifyTest`/`KoinStartupTest`. Remove the old binding in the same commit the new one appears
- Named IO dispatcher injected into `AgendaViewModel` (Phase 5)
- `scripts/di-gate.sh` must cover the new module. `scripts/swift-names-gate.sh check` after any exported-type change, with baselines regenerated in the same commit

### Integration Points
- `:shared` exports (add `:core:domain`), `sharedModules` in `initKoin()`, `IosDependencies`
- Dependency direction becomes `feature:* → core:ui → core:domain → core:data → core:network/core:model`
- `feature/agenda/di/AgendaModule.kt` (binding of `SessionFiltersService` to replace), `core/data/.../di/DataPlatformModule.*.kt` (store bindings per platform)

</code_context>

<specifics>
## Specific Ideas

- The user retracted a first answer ("combined flow only") and restarted the API-shape discussion. The final choice is pure policies **and** a combined flow (D-01).
- The user's own rationale for D-15: "I don't mind losing the filters data in the migration process. The conference is over and next time the app will be used, we'll use new data. We should use a clean approach." Later confirmed to cover favorites too.

</specifics>

<deferred>
## Deferred Ideas

- Removing the obsolete iOS `Favorites`/`AgendaSessionFilters` `UserDefaults` keys (cleanup), in a later phase or as debt.
- Grouping in the event time zone (Europe/Paris) instead of device time zone: a visible change, deferred.
- Unifying "scroll to the current time slot" across platforms (research D6).
- Replacing the hardcoded 2025 event dates with data-driven days (`InitialDayUseCase`): debt.
- Loading/Error result type from use cases (the ViewModel ERROR state is v1.0 debt).

</deferred>

---

*Phase: 07-kmp-domain-layer-and-android-adoption*
*Context gathered: 2026-10-28*
