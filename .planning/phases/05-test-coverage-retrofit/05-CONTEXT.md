# Phase 5: Test Coverage Retrofit - Context

**Gathered:** 2026-10-13
**Status:** Ready for planning

<domain>
## Phase Boundary

Business-logic test coverage is measurably improved on a hardened, fully deterministic fixture foundation. Order: (1) fix the blocking fragilities first (non-deterministic stubs, `SimpleDateFormat` date handling), (2) extend `commonTest` coverage of the Store/repository layer and GraphQL→model mappers in `:core:data`, (3) extend ViewModel tests across all feature modules using constructor injection, (4) add per-feature Compose smoke tests (`androidTest`) built on `:core:testing` fakes. Improvement is shown by a before/after Kover report, with no percentage target.

Technical work only: the user-visible behavior must not change. The one deliberate exception is the OpenFeedback fallback-URL locale fix (D-05).

**Not in this phase:** fixing bugs that the tests uncover (D-10: tests characterize current behavior, the bugs are logged as deferred), Swift/XCTest coverage on iOS (D-19), screenshot/visual regression tests (REQUIREMENTS Out of Scope), MockK/Mockative (REQUIREMENTS Out of Scope: fakes only), coverage % gates (D-18).

</domain>

<decisions>
## Implementation Decisions

### Fixture determinism (TEST-01)
- **D-01:** **Fully fixed, hand-written fixture data** replaces all randomness. Not a seeded `Random`: the user challenged that option because seeded data doesn't reveal intent ("why 3?") and shifts silently when someone adds or reorders a `random.next*()` call. `StoreStubs.kt` loses `kotlin.random.Random`, and so does `DevFestNantesStoreMocked` (its `Random.nextInt` session/partner counts).
- **D-02:** The fixed dataset **replaces `StoreStubs` in `:core:model`** (same location, no new module edges). It is the **single source** for `DevFestNantesStoreMocked`, Compose previews, `:core:testing` fakes and test assertions. Tests reference fixture constants (ids, titles, counts) instead of hard-coding magic numbers.
- **D-03:** The dataset is **deliberately varied** to make up for the lost randomness and cover the edge cases tests need: sessions on both days, every room, mixed languages/complexities/types/categories, a session with no room, null language/complexity, a speaker shared by two sessions, a session with several speakers, and partners across all categories. The planner/researcher derives the exact edge-case list from the filter logic (`AgendaViewModel.filterSessions`) and from `Mappers.kt` nullability.
- **D-04:** **Stable, unique ids** for sessions and speakers, plus a non-empty `openFeedbackFormId` where relevant. This resolves the `// TODO Replace with UUID implementation` stubs, so `getSession`/`getSpeaker`/`getSpeakerSessions` become testable on mock data. Mock mode never ships to users (production uses `setUseMockServer(false)`), so changing mock data is not a user-visible change.

### Date handling (TEST-01)
- **D-05:** Replace `SimpleDateFormat` in production code with **Kotlin types (kotlinx-datetime / `kotlin.time.Instant`)**, not `java.time`. The user chose this for KMP consistency: the model already uses `kotlin.time.Instant`. **The OpenFeedback fallback URL date is built with `LocalDate.toString()`**, which is always ASCII `yyyy-MM-dd`. This fixes today's device-default-locale bug in `FallbackFeedbackForm.kt` (non-Latin-digit locales produce a broken URL). The output is identical for fr/en users. This is a **deliberate, user-approved behavior fix**, recorded the same way as Phase 4's D-02.
- **D-06:** **ISO-8601 parsing moves to `commonMain` in `:core:model`** (e.g. a `ScheduleSlot`/String extension returning `kotlin.time.Instant?`) and is tested in `commonTest`, so it runs on JVM and iOS. It must accept both formats the current parser handles: offset (`2024-10-17T08:00:00+02:00`) and legacy UTC (`…'Z'`). It returns null on unparseable input, as today. **Localized display stays Android-only** in `:core:ui` (`android.text.format.DateUtils.formatDateTime(context, instant.toEpochMilliseconds(), …)`), because kotlinx-datetime has no localized formatting. Timezone parity is kept by using `TimeZone.currentSystemDefault()` wherever the old code relied on the default-timezone `SimpleDateFormat`.
- **D-07:** **Characterization tests are written before the swap**: they pin today's parse results and formatted outputs for real confetti date strings, and they must pass both before and after the migration. Test-only `SimpleDateFormat` usages (`GraphQLStoreJvmTest`, `ScheduleSlotDateFormatAndroidTest`) are reviewed. Remove or adapt them where they only existed to guard the old parser. Reversibility: reversible.

### ViewModel test harness (TEST-03)
- **D-08:** **Turbine** is added as a test-only dependency (version catalog + convention plugin) for asserting flow emission sequences (e.g. `UiState.STARTING → SUCCESS`, filter updates).
- **D-09:** **Inject the dispatcher** where a ViewModel hard-codes one: `AgendaViewModel.updateFilteredDays` uses `withContext(Dispatchers.IO)`, so it gets a constructor `CoroutineDispatcher` param bound in Koin. Tests pass the test dispatcher. Also check `AgendaColumn.kt:131` `withContext(Dispatchers.IO)` and handle it if it sits in a tested path. Add a shared **`MainDispatcherRule`** (or equivalent) to `:core:testing` for `viewModelScope`. These are behavior-neutral refactors. Update the Koin module `verify()` tests to match.
- **D-10:** **When a test exposes a real bug, characterize it, don't fix it.** Examples: no error handling in ViewModel `init {}`, a flow stuck in `STARTING` on store error, `println` logging. The test pins the current behavior (or is `@Ignore`d with a TODO pointing to the deferred item), and the bug goes into the phase's deferred list. This keeps the no-regression rule; fixes come later with feature work.
- **D-11:** Add a **controllable `FakeDevFestNantesStore`** to `:core:testing`, backed by `MutableStateFlow`s and seeded with the D-02 fixed dataset by default. Tests can push new values or make calls throw or emit errors. The existing `fakeDevFestNantesStore()` helper stays. Add other fakes as ViewModels need them (e.g. `FakeSessionFiltersService`, `FakeDataCollectionSettingsService`), also in `:core:testing` when they are shared, or in the feature's test source set when only one feature uses them. **Fakes only, no mocking framework.**
- **D-12:** **All 10 ViewModels are in scope**: `AgendaViewModel`, `SessionViewModel`, `FeedbackFormViewModel`, `SpeakersViewModel`, `SpeakerViewModel`, `VenueViewModel`, `PartnersViewModel`, `DataCollectionViewModel`, `BookmarksViewModel` (`:core:ui`), `HomeViewModel` (`:androidApp`). Test depth is proportional to logic: Agenda filtering (each `FilterType`, combined filters as intersection across types, bookmark filter, sort by start date) gets the most. Thin pass-through ViewModels get load/success plus their key action. ViewModels are constructed directly with fakes in JVM unit tests (`src/test`).

### Store & mapper tests (TEST-02)
- **D-13:** GraphQL→model **mapper tests use both input styles**. **Recorded JSON responses** cover the happy path: trimmed real confetti-app.dev responses committed as test resources, parsed with Apollo's `parseJsonResponse`, then mapped. **Apollo data builders** (`generateDataBuilders`) cover edge cases (nulls, missing room/speakers, unknown enums). Tests live in `:core:data` `commonTest` where Apollo test APIs allow; otherwise in `jvmTest`.
- **D-14:** **`GraphQLStore` is tested with Apollo MockServer**: a real `ApolloClient` with a memory cache, served the same recorded JSON. The tests cover CacheAndNetwork, cache hits, and network failure after a cached success. That last case pins Phase 2's user-authorized `map → mapNotNull` offline-cache fix, so a network error must not overwrite cached data.
- **D-15:** The existing **live-network `GraphQLStoreJvmTest` stays as a manual-only canary**, excluded from the default `testDebugUnitTest jvmTest` run and runnable through a dedicated Gradle task/property (the planner picks the mechanism). It serves as a schema-drift check. The default CI run becomes offline and deterministic. No new CI workflow.
- **D-16:** **`DevFestNantesStoreContractTest` becomes a real contract**: an abstract contract class asserting concrete values against the fixed dataset, with explicit `getX(unknownId) → null` checks instead of "no crash" comments. It runs against **both** `DevFestNantesStoreMocked` and `GraphQLStore`-on-MockServer, wherever both can serve equivalent data. Where they can't, the shared contract asserts structural invariants and the implementation-specific value assertions go in per-implementation subclasses.
- **D-17:** **The SharedPreferences-backed implementations are tested with Robolectric**, reusing Phase 4's Robolectric 4.17 base: `BookmarksStoreImpl` (`:core:data` androidMain), `SessionFiltersServiceImpl` (`:feature:agenda`) and `DataCollectionSettingsServiceImpl` (`:feature:settings`). The tests run against real `SharedPreferences` and pin the persisted keys and format, so users' bookmarks, filters and consent survive any later refactor.

### Compose smoke tests & measurement (TEST-04)
- **D-18:** **Measure with a Kover report and no threshold.** Add Kover through `build-logic` (convention plugin) and capture a **baseline report before the coverage work** and a final report after. Record both in VERIFICATION. No CI coverage gate, consistent with "no strict % target".
- **D-19:** **Compose smoke tests run as `androidTest` on the emulator**, in the existing CI instrumentation job (API 34, `connectedDebugAndroidTest`), one test class per feature module. Keep `MainActivityTest`. Watch for the Phase 3 D8 dex-merging OOM with more androidTest APKs (daemon heap is 4 GB). If it comes back, say so; don't silently shard or drop tests.
- **D-20:** **Smoke depth: render plus one key interaction per screen.** The feature `*Route` renders with fake fixed data (assert a known title/name from the D-02 dataset). One critical interaction per screen: bookmark toggle, applying a filter, a speaker/session click firing its callback, a consent toggle, and similar.
- **D-21:** **UI tests are wired through Koin test modules.** Each feature androidTest loads Koin with the feature's module plus `:core:testing`'s `testDoublesModule` (`FakeDevFestNantesStore` with the fixed dataset, fake analytics, no-op performance). The real `*Route` resolves its ViewModel via `koinViewModel()`, so the production wiring is exercised too. The planner handles Koin start/stop isolation between tests and the app's own `initKoin()` (e.g. a test `Application`/runner or `loadKoinModules` with overrides). Note that Phase 4 set `allowOverride(false)` in production `initKoin()`.

### iOS
- **D-22:** **No Swift/XCTest work.** iOS coverage comes from `commonTest` running on `iosSimulatorArm64`. **Required CI adjustment:** iOS CI currently runs only `:shared:iosSimulatorArm64Test` (`.github/workflows/ios.yml:131`), so the new `commonTest` suites in `:core:model`/`:core:data` (and `:core:testing`) must be added to the iOS CI test step, or the step widened, so they actually run on iOS.

### Claude's Discretion
- Exact fixture content (names, counts) within D-03's coverage goals; the naming of fixture constants.
- Turbine, Kover, Apollo MockServer and kotlinx-datetime versions (re-verify the newest stable at plan time, per the Phase 2 newest-verified policy) and their placement in the catalog and convention plugins.
- Name and placement of the common ISO parser (D-06), as long as it is in `:core:model` commonMain.
- Mechanism for excluding the live canary (D-15): Gradle task, system property, or source set.
- Plan sequencing. Hard constraint: TEST-01 (fixtures + dates) lands first, and the Kover baseline is captured before the coverage additions. Otherwise follow the established pattern of one commit per logical step with CI green per pushed step (Phase 3 D-18 style).
- Test naming style (current tests use `snake_case` `fun agenda_flow_emits_agenda()`; stay consistent).

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Scope & requirements
- `.planning/ROADMAP.md` §"Phase 5: Test Coverage Retrofit": goal and 4 success criteria
- `.planning/REQUIREMENTS.md` §"TEST (Couverture de tests)": TEST-01..04; Out of Scope table (no % target, no mocking framework, no Paparazzi/Roborazzi)
- `.planning/PROJECT.md` §Constraints (`android` CLI usage, no-regression rule, KMP rule) and §Key Decisions (Robolectric base from Phase 4, coverage without %)

### Prior phase decisions that constrain this phase
- `.planning/phases/04-hilt-to-koin-di-migration/04-CONTEXT.md`: D-09/D-10 (parameterized ViewModels via `parametersOf`, `koinViewModel()` inside `*Route`), D-12 (graph verification tests that must stay green after the dispatcher injection), D-13 (`scripts/di-gate.sh`: test code must not trip the service-locator gate; check its allowlist for test source sets), D-14 (`:core:testing` fakes this phase extends)
- `.planning/phases/03-multi-module-architecture-extraction/03-CONTEXT.md`: D-07 (callback-only feature routes), D-18 (one commit per step, CI green per step)
- `.planning/STATE.md` §Decisions [Phase 02]: the `GraphQLStore` CacheAndNetwork `map → mapNotNull` fix (D-14 pins it); kotlinx-datetime 0.8.0 `Instant`/`Clock` notes (use `kotlin.time.Clock`, not `kotlinx.datetime.Clock`)

### Codebase maps
- `.planning/codebase/CONCERNS.md`: StoreStubs randomness/TODO ids, `SimpleDateFormat` concerns, test coverage gaps (pre-Phase-3 paths, patterns still apply)
- `.planning/codebase/TESTING.md`: test conventions (pre-Phase-3 paths and pre-Koin DI notes are outdated)

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `core/testing/src/commonMain/.../FakeStores.kt` (`fakeDevFestNantesStore()`), `FakeBookmarksStore.kt`, `FakeAnalyticsService.kt`, `di/TestDoublesModule.kt`; `androidMain/.../NoOpPerformanceMonitoring.kt`, `di/AndroidTestDoublesModule.kt`: base for D-11/D-21.
- Robolectric 4.17 already in the catalog (`robolectric`), used by `androidApp/src/test/.../KoinStartupTest.kt` / `ProductionGraphStartupTest.kt`: base for D-17.
- Per-module Koin `verify()` tests (`*ModuleTest.kt` in every feature/core module): must be updated when constructors change (D-09).
- `CoroutinesDispatcherProvider` (`androidApp/.../core/CoroutinesDispatcherProvider.kt`) exists, but only in `:androidApp`. Feature modules can't see it, so dispatcher injection needs a feature-visible binding (e.g. a named `CoroutineDispatcher` in a core module).

### Current test inventory
- `core/data/src/commonTest/.../DevFestNantesStoreContractTest.kt` (weak assertions, D-16), `core/data/src/jvmTest/.../graphql/GraphQLStoreJvmTest.kt` (live network, D-15), `core/model/src/commonTest/.../ScheduleSlotDateParsingTest.kt`, `core/ui/src/androidTest/.../ScheduleSlotDateFormatAndroidTest.kt`, `androidApp/src/androidTest/.../MainActivityTest.kt`, plus DI graph tests. **Zero ViewModel tests today.**

### Fragility locations (TEST-01)
- `core/model/src/commonMain/.../stubs/StoreStubs.kt`: `Random.nextLong/nextInt` for ids, category, room, speaker count, slot hour, partner name; empty speaker id and `openFeedbackFormId` TODOs.
- `core/data/src/commonMain/.../DevFestNantesStoreMocked.kt:23,38,42,46`: `Random.nextInt` counts.
- `core/ui/src/main/java/.../utils/DateUtils.kt`: `getDateFromIso8601` (per-call `SimpleDateFormat`, two patterns, system locale), `getDayFromIso8601` (Android `DateUtils.formatDateTime`).
- `feature/session-detail/src/main/java/.../FallbackFeedbackForm.kt:46-52`: `SimpleDateFormat("yyyy-MM-dd")` with the default locale for the OpenFeedback URL (D-05).

### Established Patterns
- ViewModels: constructor-injected, `viewModelScope`, `StateFlow<UiState>`, Timber logging (no tree planted in JVM tests, so harmless).
- `AgendaViewModel.filterSessions`: OR within a filter type, intersection across types; the main logic target for D-12.
- Mappers: `core/data/src/commonMain/.../graphql/Mappers.kt` (internal `toXxx()` functions, internal since Phase 3 to avoid an iOS header leak, so keep them internal and test from the same module).

### Integration Points
- CI Android: `.github/workflows/android.yml`: `testDebugUnitTest jvmTest` (unit job), `connectedDebugAndroidTest` (API 34 emulator job), `scripts/di-gate.sh`.
- CI iOS: `.github/workflows/ios.yml:131` `./gradlew :shared:iosSimulatorArm64Test` (to widen, D-22).
- Version catalog `gradle/libs.versions.toml` + `build-logic` convention plugins for Turbine/Kover/MockServer deps.

</code_context>

<specifics>
## Specific Ideas

- The user explicitly preferred fully fixed fixtures over seeded `Random` (intention-revealing data, no RNG call-order drift).
- The user explicitly preferred Kotlin date types over `java.time` for KMP consistency. Split: shared parsing in common code, platform-specific localized display.
- Agenda filter tests should read like specifications against named fixture sessions (e.g. "FR + ADVANCED returns only <fixture session>").

</specifics>

<deferred>
## Deferred Ideas

- Bugs that the new tests uncover (D-10): logged here during execution, fixed in a later feature-work phase. Known candidates: missing error handling in ViewModel `init {}` blocks, `println` instead of Timber for GraphQL errors.
- Running the live-network canary on a schedule in GitHub Actions (D-15 kept it manual-only).
- Swift/XCTest coverage of iOS ViewModels (D-22).
- iOS reusing the shared ISO parser from D-06 (Swift side unchanged this phase).
- Coverage non-regression gate in CI (D-18 chose report-only).

</deferred>

---

*Phase: 05-test-coverage-retrofit*
*Context gathered: 2026-10-13*
