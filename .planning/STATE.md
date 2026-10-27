---
gsd_state_version: "1.0"
milestone: v1.1
milestone_name: iOS Architecture
current_phase: 6
current_phase_name: iOS Test Foundation and CI Gates
status: executing
stopped_at: Phase 6 context gathered
last_updated: "2026-10-26T20:45:05.700Z"
last_activity: 2026-10-23
last_activity_desc: Roadmap v1.1 created (phases 6-12, 27/27 requirements mapped)
state_head: 88ea71edd23843daba8109d71c9364f6423a5299
progress:
  total_phases: 7
  completed_phases: 0
  total_plans: 6
  completed_plans: 0
  percent: 0
---

# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-10-23)

**Core value:** CI/CD must work again and the project must become maintainable again (modern build, modular architecture, decentralized DI, solid test coverage) without ever regressing the app's existing behavior for users.
**Current focus:** Milestone v1.1 iOS Architecture — Phase 6: iOS Test Foundation and CI Gates

## Current Position

Phase: 6 (iOS Test Foundation and CI Gates) — READY TO EXECUTE
Plan: Not started
Status: Ready to execute
Last activity: 2026-10-23 — Roadmap v1.1 created (phases 6-12, 27/27 requirements mapped)

Progress: [░░░░░░░░░░] 0%

## Performance Metrics

**Velocity:**

- Total plans completed: 37
- Average duration: N/A
- Total execution time: 0 hours

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 01 | 3 | - | - |
| 02 | 5 | - | - |
| 03 | 11 | - | - |
| 4 | 7 | - | - |
| 05 | 11 | - | - |

**Recent Trend:**

- Last 5 plans: N/A
- Trend: N/A

*Updated after each plan completion*
**Per-Plan Metrics:**

| Plan | Duration | Tasks | Files |
|------|----------|-------|-------|
| Phase 01 P01 | 18min | 2 tasks | 1 files |
| Phase 01 P02 | 2min | 2 tasks | 1 files |
| Phase 01 P03 | 21min | 1 tasks | 1 files |
| Phase 02 P01 | 75min | 2 tasks | 4 files |
| Phase 02 P02 | 43min | 3 tasks | 16 files |
| Phase 02 P03 | 180min | 2 tasks | 6 files |
| Phase 02 P04 | 75min | 2 tasks | 6 files |
| Phase 02 P05 | 50min | 2 tasks | 3 files |
| Phase 03 P01 | 95min | 2 tasks | 77 files |
| Phase 03 P10 | 70min | 2 tasks | 16 files |
| Phase 03 P11 | 15min | 2 tasks | 4 files |
| Phase 03 P02 | 65min | 2 tasks | 31 files |
| Phase 03 P03 | 165min | 3 tasks | 24 files |
| Phase 03 P04 | 140min | 2 tasks | 14 files |
| Phase 03-multi-module-architecture-extraction P05 | 30min | 3 tasks | 67 files |
| Phase 03 P06 | 60min | 2 tasks | 37 files |
| Phase 03 P07 | 43min | 2 tasks | 36 files |
| Phase 03 P08 | N/A (continuation) | 3 tasks | 46 files |
| Phase 03 P09 | 90min | 2 tasks | 7 files |
| Phase 04 P01 | ~1h (continuation) | 3 tasks | 36 files |
| Phase 04 P02 | 17 min | 3 tasks | 26 files |
| Phase 04 P03 | 1h 53m | 3 tasks | 17 files |
| Phase 04 P04 | 1h 35m | 2 tasks | 18 files |
| Phase 04 P05 | 23 min | 2 tasks | 18 files |
| Phase 04 P06 | 60 min | 3 tasks | 19 files |
| Phase 04 P07 | 23 min | 2 tasks | 22 files |
| Phase 05 P01 | 20 min | 3 tasks | 11 files |
| Phase 05 P02 | 35 min | 3 tasks | 13 files |
| Phase 05 P03 | 40 min | 2 tasks | 8 files |
| Phase 05 P04 | 50 min | 3 tasks | 10 files |
| Phase 05 P05 | 25 min | 2 tasks | 8 files |
| Phase 05 P06 | 15 min | 3 tasks | 13 files |
| Phase 05 P07 | 45min | 2 tasks | 6 files |
| Phase 05 P08 | 15min | 3 tasks | 12 files |
| Phase 05 P09 | 30 min | 2 tasks | 5 files |
| Phase 05 P10 | 1h 30m | 3 tasks | 6 files |
| Phase 05 P11 | 30 min | 3 tasks | 7 files |

## Accumulated Context

### Decisions

Decisions are logged in PROJECT.md Key Decisions table.
Recent decisions affecting current work:

- [v1.1 Roadmap]: 7 phases (6-12) derived from the order in `research/SUMMARY.md`; Android adoption (SHARED-06, an isolated requirement) is merged with the KMP domain layer in Phase 7; Phase 8 (Observation) technically depends only on Phase 6
- [v1.1 Roadmap]: user decisions applied: strict parity except the iOS room order by `RoomSortIndex` (SHARED-03, documented deviation); iOS agenda continuously observes the combined flow (IOSDATA-03); full scope kept (SPM, Swift 6, smoke tests)
- [v1.1 Roadmap]: research trade-offs adopted: legacy `UserDefaults` keys reused without a migrator, a single SPM manifest, features without import of the Kotlin framework, `AppDependencies` in CoreDI (in the app target in Phase 10, moved in Phase 11)
- [v1.1 Roadmap]: SHARED-05 (including the iOS implementation on `Favorites`/`AgendaSessionFilters`) delivered and tested in Phase 7, wired into the app in Phase 9; IOSTEST-03 in Phase 6, test plan extended to package tests in Phase 11; IOSTEST-01 closed in Phase 8 (the obsolete target is replaced as early as Phase 6, a prerequisite of the lane)
- Roadmap: Strictly sequential order iOS CI -> deps/build -> multi-module -> DI -> tests (no parallelization between phases, each phase depends on the previous one)
- Roadmap: CICD-01/02/03 (Gradle/Konan cache, CI matrix) grouped into Phase 1 with CI-01 rather than a separate CICD phase — same CI surface, avoids a low-density phase
- Roadmap: Gradle Declarative DSL treated as a documented partial migration (BUILD-07), no blocking on incomplete AGP/KMP support
- [Phase 01]: iOS Xcode/simulator resolution kept shell+jq (no marketplace action); no fallback step for empty simulator resolution per D-01/D-02 — Avoids new unaudited third-party dependency; xcodebuild's own destination error is the accepted failure mode per locked CONTEXT.md decisions
- [Phase 01]: Phase 01 (CI Pipeline Fixed & Optimized) complete: ios.yml routed through the shared android-setup composite action with branch-aware Gradle cache policy; Konan cache and macos-latest/ubuntu-latest job separation confirmed intact via live workflow_dispatch run 35228437188
- [Phase 02]: Version-target policy: newest-verified (Kotlin 2.4.20, AGP 9.4.0, Compose BOM 2026.09.00, Apollo 5.2.0) for BUILD-01/02/04/05, not the REQUIREMENTS.md-pinned 2026-09-12 snapshot — Checkpoint resolved by developer in 02-01; keeps Phase 3 from immediately needing another toolchain bump
- [Phase 02]: minSdk raised 23 -> 26 (user-authorized) to resolve AGP 8.13's lint tool being unable to read Kotlin 2.4's @Metadata format, mis-flagging 6 forEach call sites as NewApi — User explicitly authorized mid-execution; real fix (API level genuinely available) rather than a lint suppression, which the plan's threat model prohibits
- [Phase 02]: D-03 commit fold: Gradle 9.7.1 + AGP 9.4.0 landed in one commit instead of Task 1's planned standalone wrapper commit — Gradle 9.7.1 does not build under AGP 8.13.0 (removed internal API org.gradle.api.problems.internal.InternalProblems)
- [Phase 02]: Detekt pinned to 2.0.0-alpha.6 under the dev.detekt group/plugin-id, approved via the blocking-human package-legitimacy checkpoint — official detekt org's own pre-1.0 next-major line, closest compat baseline to this stage's targets
- [Phase 02]: Compose BOM 2026.09.00 forced compileSdk 36->37 (AAR metadata floor); targetSdk deliberately decoupled and kept at 36 per user direction, tracked as a follow-up decision
- [Phase 02]: Apollo 5.2.0 + appolloCache 1.0.8: normalized cache migrated to com.apollographql.cache artifact group; CacheResolver adapted to v5's ResolverContext signature; resolveArgument replaced with argumentValue (hard DEPRECATION_ERROR under Kotlin 2.4)
- [Phase 02]: User-authorized deviation: GraphQLStore.kt's CacheAndNetwork Flow accessors changed map->mapNotNull to stop a network-failure emission from clobbering correctly-cached offline data (pre-existing bug surfaced by D-07 verification, not a migration regression)
- [Phase 02]: kotlinx-datetime 0.8.0's Instant typealias survived (assumption A2 resolved) but Clock.System does not resolve through the typealias; ScheduleSlot.kt/Agenda.kt needed zero changes, but androidApp's UI-layer Agenda.kt needed a one-line kotlin.time.Clock import swap
- [Phase 02]: Firebase BOM 34.x removed the -ktx artifact constraints (Firebase stopped publishing -ktx modules July 2025); repointed the four firebase-*-ktx catalog aliases at their merged plain artifacts, updated two production files' imports, and added a dependency substitution for the transitive firebase-auth-ktx pulled in via openfeedback
- [Phase 02]: Added three R8 -dontwarn rules (R8-generated, verbatim) for openfeedback's stale kotlinx-datetime 0.6.x Clock/Instant class references, safe because OPEN_FEEDBACK_ENABLED=false makes those code paths unreachable at runtime
- [Phase 02]: settings.gradle.kts converted to settings.gradle.dcl (Gradle Declarative DSL pilot, D-08/D-09) — succeeded on the primary attempt; androidApp/shared deliberately stay on Kotlin DSL, corroborated by a live 2026-09-19 re-check confirming Declarative Gradle's module-level Software Types support remains experimental/unready
- [Phase 02]: BUILD-01..BUILD-07 version outcomes consolidated into a single STATE.md record, discharging D-03's documentation obligation for the whole phase
- [Phase 03]: 03-01 halt resolution (user, 2026-09-23): options 1+3, planned OUTSIDE 03-01's scope — (1) rename Apollo-generated response types away from domain-model names (Venue/Session/Speaker/Room/Partner) via Apollo Gradle config; (3) strengthen swift-names-gate.sh to diff each colliding type's member set, not just name+count. Requires replanning before 03-01 Task 2 CI gate / Task 3 can pass. — Durable fix for the K/N Swift-name collision flip; option 2 alone would leave 4 latent flips for 03-02/03-03
- [Phase 03]: 03-01 halt resolved — Apollo schema-type holders renamed via @targetName (GraphQL<Type>), swift-names-gate.sh strengthened (member sets + type-level collisions), D-11 one-time Swift rename amendment; 03-02..03-09 runnable on next /gsd-execute-phase 3
- [Phase 03]: [Phase 03] 03-02: extracted :core:network (Apollo, implementation-only, never export()-ed, D-11) and :core:analytics (exported, D-11) as two-commit-per-module moves (D-16); discovered and closed a Kotlin/Native ObjC-header leak where public extension functions/properties on non-exported :core:network receiver types forced module-prefixed shadow declarations into shared.h — fixed by marking 8 declarations (Mappers.kt's 7 toXxx() functions, RoomSortIndex.kt's sortIndex property) internal — No explicit export() line existed for :core:network, but public extension-function receivers alone were enough to leak its non-exported Apollo types into the iOS umbrella header; the acceptance check "no export line" alone could not have caught this — swift-names-gate.sh's member/collision diff did.
- [Phase 03]: [Phase 03] 03-03: extracted :core:data (store layer: DevFestNantesStore, GraphQLStore, BookmarksStore/BookmarksStoreImpl) as a two-commit-per-module move (D-16), exported to Swift via :shared api+export(); discovered and fixed a Kotlin/Native NO-SOURCE regression -- with shared/src truly empty, compileKotlinIos*/linkDebugFramework*/linkReleaseFramework* report NO-SOURCE and are skipped entirely, producing no shared.framework for iOS at all. Fixed by adding one internal marker file (SharedFrameworkPlaceholder.kt); recorded as a permanent D-10 amendment so 03-09 (final umbrella-thinning plan) does not undo it. — Kotlin/Native's compile+link tasks treat a zero-file source set as NO-SOURCE and skip the link step, which would have silently broken the iOS build precisely when :shared reaches its D-10 zero-Kotlin-sources goal -- the placeholder is the minimal, non-exported fix. User approved at the 03-03 checkpoint (2026-09-24).
- [Phase 03]: core:testing's fakeDevFestNantesStore() wraps public DevFestNantesStoreBuilder (mock mode) instead of internal DevFestNantesStoreMocked, which is not visible across the module boundary
- [Phase 03]: AGP 9's CommonExtension is non-generic; a single configureAndroidCommon(commonExtension: CommonExtension) serves both Library and Application conventions, configured via direct property access (not nested-block DSL sugar, which is script-only)
- [Phase 03]: [Phase 03] 03-05: :core:ui extracted (theme, UiState, BookmarksViewModel, shared components, DateUtils/StringExtensions) via two-commit D-16 pattern; resources-gate.sh established as the reusable resource-ownership gate for 03-06..03-08 — CI green on both workflows post-repackage; failOnNoDiscoveredTests disabled in build-logic AndroidCommon.kt so leaf modules with only instrumentation tests (like :core:ui) don't fail CI's bare testDebugUnitTest; smoke checkpoint 2 approved by user 2026-09-24
- [Phase 03]: [Phase 03] 03-06: extracted :feature:venue and :feature:about as devfest.android.feature leaves (D-16 two-commit pattern), each with a callback-only Route entry point; app_version promoted to :core:ui as a 2+ consumer resource (about + Settings.kt, D-17); CI green on both workflows
- [Phase 03]: [Phase 03] 03-07: extracted :feature:settings (Settings+DataCollection+Legal, consumer-owned DataCollectionSettingsService) and :feature:speakers (list+detail, app-owned assisted factory) as devfest.android.feature leaves via the D-16 two-commit pattern; screen_settings/screen_data_collection/settings_legal/screen_speaker promoted to :core:ui as 2+ consumer resources (Screen.kt shares the same title strings); SpeakerDetailRoute takes the already-constructed SpeakerViewModel as a parameter so MainActivity's ViewModelFactoryProvider EntryPoint and assistedViewModel{} factory stay entirely in :androidApp; CI green on both workflows
- [Phase 03]: [Phase 03] 03-08: extracted :feature:agenda (consumer-owned SessionFiltersService, D-03) and :feature:session-detail (OpenFeedbackConfig crossing via single AppModule @Provides, D-02) as the final two devfest.android.feature leaves via the D-16 two-commit pattern; :androidApp reduced to its 18-file D-03 thin shell; CI green on both workflows (run 36154645700 android, 36154645615 ios); Android smoke checkpoint 3 (D-18) approved by user 2026-09-26 — Completes all six ARCH-03 feature-module extractions; ARCH-02/ARCH-03 requirements stay blocked pending 03-09 (shared-ID gate #2388 — 03-09 also declares them and has no SUMMARY yet)
- [Phase 03]: [Phase 03] 03-09: :shared thinned to a pure umbrella (D-10/D-11) exporting exactly core:model/core:data/core:analytics; :androidApp no longer depends on :shared; buildSrc deleted (D-13); Gradle daemon heap raised 2048M->4096M (user-approved) to fix a real Android CI D8 dex-merging OutOfMemoryError — CI-GREEN-BOTH confirmed on final HEAD (android.yml run 36328538946, ios.yml run 36328538965). Phase 03 fully complete: ARCH-01..04 marked complete.
- [Phase 04]: Koin 4.2.2 composition root initKoin in :shared with allowOverride(false); IosDependencies plain object keeps Koin out of shared.h; D-02 iOS store single is a deliberate invisible change — Tracer approved; Hilt coexists until 04-07
- [Phase 04]: 04-02: PerformanceMonitoring is an interface seam; Firebase SDK verify() extra types derived by reflection (obfuscated gms types) — Keeps ViewModel call sites unchanged and survives Firebase releases
- [Phase 04]: 04-02: Koin SharedPreferences stays default prefs file; Hilt providers for Koin-owned types are KoinPlatform bridges — Persisted bookmarks/filters/consent survive; one instance per singleton (D-06)
- [Phase 04]: 04-03: DI gate scans git-tracked files only; re-run after git add. Comments must not name the removed DI framework. — Gate matches substrings case-insensitively; untracked files are invisible locally but seen by CI
- [Phase 04]: 04-04: SpeakerViewModel declared with explicit positional lambda and resolved via koinViewModel { parametersOf(speakerId) } as route default parameter — id can never bind to a wrong parameter by type; owner stays the back-stack entry
- [Phase 04]: 04-04: Hilt AppModule became an object once its last @Binds turned into a KoinPlatform bridge — detekt UtilityClassWithPublicConstructor; Hilt supports @Module object
- [Phase 04]: 04-05: FirebaseRemoteConfig is a Koin factory in sessionDetailModule (300 s interval per ViewModel creation); OpenFeedbackConfig built in the app-shell Koin appModule; BookmarksViewModel flipped with both call sites in one commit — Preserves Hilt unscoped-provider semantics and keeps BuildConfig secrets app-only
- [Phase 04]: 04-06: Firebase strategy for KoinStartupTest is Robolectric 4.17 (testImplementation only) — Exercises the real production Firebase definitions unchanged under a real Application and gives Phase 5 a reusable Robolectric base; chosen over mockito, exclude and seam
- [Phase 04]: 04-06: application initializers are an explicit single(named("applicationInitializers")) list; order asserted by KoinStartupTest — Consent must be applied before performance collection is forced on (DI-02, T-04-19)
- [Phase 04]: 04-07: catalog activity pinned to 1.12.4 (resolved version on releaseRuntimeClasspath, unchanged by Hilt removal); KSP and firebase-auth-ktx substitution kept — Pin states what ships; KSP still needed by kmp-native-coroutines
- [Phase 04]: 04-07: no Hilt artifact on classpath; com.google.dagger:dagger:2.57.2 remains as firebase-perf internal transitive — Cannot be removed without dropping Firebase Performance
- [Phase 05]: 05-01: Kover 0.9.11 report-only via build-logic conventions; live GraphQL test is a manual-only canary (-PliveNetworkTests); baseline 15.96% line coverage measured with the canary excluded — Network-fed run showed 20.64%; the offline baseline is deterministic so the 05-10 compare is meaningful
- [Phase 05]: 05-02: date parsing is shared kotlin.time.Instant in :core:model; localized display stays on Android APIs; OpenFeedback fallback URL date is LocalDate.toString() (ASCII, user-approved D-05)
- [Phase 05]: 05-03: fixed hand-written fixtures (SessionFixtures/SpeakerFixtures/PartnerFixtures) in :core:model replace all random stubs; buildXStub() are deterministic wrappers; Mocked store implements getSpeakerSessions — Single deterministic source for previews, mocked store and later tests; buildVenueStub left byte-identical as the production offline fallback
- [Phase 05]: 05-04: GraphQLStore and mappers tested offline via Apollo MockServer 0.3.1 with recorded responses as Kotlin string constants; data builders generated into :core:network commonMain; cacheKeyGenerator/cacheResolver split into ApolloCacheKeys.kt; testAndroidHostTest enabled for :core:data and run in CI — iOS commonTest cannot load resources; one file's top-level vals share an initializer so the split keeps tests from building the production SQLite client; production defects found (100/110 speakers, RoomSortIndex ids, println logging) are logged in deferred-items.md and not fixed (D-10)
- [Phase 05]: 05-05: store contract is abstract and runs against Mocked and GraphQLStore-on-MockServer on three runtimes; bookmarks, agenda filter and consent keys/formats pinned by Robolectric tests with literal keys; test-only Crashlytics id lives in feature/settings/src/test/res and is absent from packaged resources — MockServer.Builder() replaces the deprecated MockServer(handler); one contract keeps mock and real store honest, prefs tests protect saved user state across refactors
- [Phase 05]: 05-06: ViewModel harness = Turbine 1.2.1 + MainDispatcherRule + StateFlow-backed FakeDevFestNantesStore; AgendaViewModel gets its IO dispatcher from a named Koin binding in coreUiModule (explicit positional lambda in agendaModule) — Makes ViewModels deterministic under test without changing production behavior (Dispatchers.IO is still what the binding supplies); SpeakersViewModel store failure is characterized and queued for the deferred log via 05-07
- [Phase 05]: 05-07: ties on start time are asserted as a set (HashSet before sortedBy); AgendaViewModel store failure pinned and deferred (items 10-12 in deferred-items.md)
- [Phase 05]: 05-08: FeedbackFormViewModel depends on a FeedbackFlags seam (FirebaseFeedbackFlags factory binding) so all ten ViewModels are tested on fakes; Robolectric only for VenueViewModel — Behavior-neutral adapter keeps keys, AND logic, log lines, main-thread listener and the per-ViewModel remote-config factory; user-confirmed D-23 (follows PerformanceMonitoring)
- [Phase 05]: 05-09: VenueSmokeTest serves images from memory via a Coil interceptor because the floor-plan control removes itself on image load failure; no production change
- [Phase 05]: 05-09: no packaging excludes were needed for feature androidTest APKs; Settings smoke test overrides DataCollectionSettingsService with a private recording fake
- [Phase 05]: 05-10: smoke tests assert reachable content (first day tab + scroll-to-node), never scroll position; session detail smoke test fakes the FeedbackFlags seam so no remote config is created
- [Phase 05]: 05-11: kotlinx-datetime resolves to 0.8.0-0.6.x-compat (OpenFeedback 1.0.0-alpha.3 links the 0.6.x Instant/Clock classes); first-party code on kotlin.time.Instant, shared.h unchanged, R8 suppressions removed, OpenFeedbackDatetimeLinkageTest guards the bridge (G-05-3) — Keeps the newest library, restores the runtime classes, and a plain bump past 0.8.x now fails CI; UAT test 3 re-checked on device and passed 2026-10-16

### Pending Todos

None yet.

### Blockers/Concerns

- **v1.1 — spikes to plan:** Phase 7 (real API snapshot, captured legacy `Favorites`/`AgendaSessionFilters` payloads); Phase 9 (installed v1.0 release build for the in-place update UAT, check `room.id == room.name`); Phase 11 (XCFramework from a clean checkout on Xcode 26.6 CI, Release/dSYM path, `Bundle.module` with SwiftGen 6.6.3); Phases 8 and 12 (KMP-NativeCoroutines bridge replayed on Swift 6.3.3 in CI)
- **v1.1 — granularity:** 7 phases for a `standard` granularity (4-6 expected); accepted given the risk (27 requirements, every phase green and shippable, order imposed by the user)
- **Phase 3 — RESOLVED:** About/Partners live in a dedicated `:feature:about` module (D-05, 03-06).
- **Phase 3 — RESOLVED 2026-09-24 by 03-10 + 03-11:** (history, kept for context) moving `Venue`/`Session`/`Speaker`/`Room`/`Partner` from package `com.gdgnantes.devfest.model` into `com.gdgnantes.devfest.core.model` flipped which of two same-simple-named Kotlin classes (the domain model vs. the Apollo-generated `GetXQuery.X`/`fragment.XDetails.X` GraphQL response class already colliding on that name) keeps the unprefixed Swift name vs. gets suffixed `_`. Kotlin/Native's ObjC header generator resolves same-simple-name collisions across the whole compiled framework by an alphabetical-FQN tie-break (shortest proof: `com.gdgnantes.devfest.model.Venue` sorted AFTER `com.gdgnantes.devfest.graphql.GetVenueQuery.Venue` at baseline — domain model got `Venue_`; `com.gdgnantes.devfest.core.model.Venue` sorts BEFORE `...graphql...` post-rename — domain model now gets clean `Venue`, GraphQL type flips to `Venue_`). This broke ~10 pre-existing Swift files (`VenueContent.swift`, `AgendaContent.swift`, `AgendaViewModel.swift`, `AgendaView.swift`, `AgendaCellView.swift`, `AgendaDetailView.swift`, `SpeakerDetailsViewModel.swift`, `SpeakersViewModel.swift`, `SpeakerDetailsView.swift`, `SpeakerView.swift`, `AboutViewModel.swift`) that hardcode the `_`-suffixed name expecting it to be the domain model. iOS CI failed on Swift compilation (run 35847334261, PR #419) — the swift-names-gate.sh gate (missing-name + collision-COUNT checks) did not catch this because the collision COUNT stayed the same, only the per-name IDENTITY swapped. **Resolution:** see `.planning/phases/03-multi-module-architecture-extraction/03-01-SUMMARY.md` "Halt Resolution (03-10 + 03-11)" for the full diagnosis, the @targetName mechanism, the corrected collision attribution, and the D-11 amendment.
- **Phase 3 — RESOLVED:** single iOS umbrella validated end to end — `:shared` exports exactly core:model/data/analytics (D-11), keeps `SharedFrameworkPlaceholder.kt` (D-10 amendment); iOS CI green and D-12 simulator checkpoints approved (03-VERIFICATION.md passed).
- Phase 2: AGP 9.x/KGP/KSP2 version re-verification was performed live during Phase 2 (each plan re-checked Maven Central/Google Maven metadata immediately before its own commit); outcomes are recorded per-requirement in `## Phase 02 version deviations` below — discharged, no longer open.
- Follow-up (not blocking): bump targetSdk 36->37 in a dedicated future stage after reviewing Android 17's behavior-change surface (kb://android/about/versions/17/behavior-changes-all / -17) — deliberately deferred from 02-03 per user direction to avoid folding a runtime-behavior-change decision into the Compose-BOM commit
- Follow-up (not blocking): the firebase-auth-ktx -> firebase-auth:24.2.0 dependency substitution in androidApp/build.gradle.kts is version-coupled to the pinned firebaseBom (34.19.0); re-verify the literal version if firebaseBom is bumped again in a future stage
- **RESOLVED 2026-09-27:** Phase 03 Plan 09 (final phase plan) Android CI Instrumentation-tests OOM blocker. Root cause: D8's `DexMergingWorkAction` shares the Gradle daemon heap (`org.gradle.jvmargs`, was `-Xmx2048M`) while assembling `feature:settings`/`feature:session-detail`/`feature:speakers` androidTest APKs concurrently — 3 consecutive CI failures (run 36270314662 and reruns) with `java.lang.OutOfMemoryError: Java heap space`. Fix: raised the Gradle daemon heap to `-Xmx4096M` in `gradle.properties` (commit 5cb0389, user-approved deviation, Kotlin daemon heap left unchanged). CI-GREEN-BOTH is now satisfied on HEAD 5cb0389: android.yml run 36328538946 (all 4 jobs green, incl. Instrumentation tests (34)), ios.yml run 36328538965 (green). Task 1 (thin :shared umbrella + drop androidApp->:shared dep + delete buildSrc, commits 1bf81ae/2b03d4f) and Task 2 (all phase gates) are both complete; 03-09-SUMMARY.md written and ARCH-01..04 marked complete per the shared-ID gate.
- Follow-up (not blocking, from 03-SECURITY.md note 2): `swift-names-gate.sh` and `resources-gate.sh` only run manually from `.planning`; wire them into CI (CICD-V2-01) so Swift identity-swap and resource-drift detection survive past Phase 3.
- **Phase 3 follow-up — RESOLVED in Phase 4:** `:core:testing` now consumed (`testImplementation`) by `:core:ui` and `:androidApp` (fakes + `testDoublesModule`); Phase 5 extends it.
- Note (not blocking, Phase 4): `com.google.dagger:dagger` remains on the release classpath only as a Firebase Performance internal transitive; the DI gate and `NO-HILT-ON-CLASSPATH` cover Hilt itself.
- Note (not blocking, Phase 4 UAT): speaker images may fail to load on networks with SSL interception/blocking of raw.githubusercontent.com — environmental, observed on Android and iOS, not a code regression.
- Follow-up (not blocking, Phase 5 UI review): ViewModels have no error state on store failure (pinned by characterization tests), the fallback OpenFeedback URL has a double slash, and the agenda bookmark contentDescription is an unlocalized literal — see `05-UI-REVIEW.md` and `deferred-items.md`.
- Follow-up (not blocking, G-05-3): kotlinx-datetime resolves to `0.8.0-0.6.x-compat` because OpenFeedback 1.0.0-alpha.3 (and every release up to alpha.6) links against the 0.6.x `Instant`/`Clock` classes, which plain 0.8.0 no longer ships. Compat releases end with 0.8.x, so any move to 0.9+ first needs an OpenFeedback release built on `kotlin.time.Instant` (upstream main was still on the deprecated type as of 2026-10-15). `OpenFeedbackDatetimeLinkageTest` fails CI on any bump that drops the compat classes. The Phase 02 R8 suppression premise was false: the secrets plugin sets OPEN_FEEDBACK_ENABLED from local.properties, so the feedback code was reachable on developer and release builds. The suppressions were removed in 05-11.

## Phase 02 DCL pilot outcome

**Requirement:** BUILD-07 — "Build files are migrated to the Gradle Declarative DSL (`.gradle.dcl`) where AGP/KMP support allows it; unsupported modules stay documented in Kotlin DSL (`.kts`) with the reason for the blocker." Satisfied via the partial-migration branch (D-08): one low-risk leaf target converted, the rest documented on `.kts` with the reason support is missing.

**Live re-check performed before converting anything (per the plan's action):** fetched `docs.gradle.org/9.7.1/userguide/userguide.html` (no dedicated Declarative Gradle chapter), `blog.gradle.org` (latest Declarative Gradle post is still "Declarative Gradle EAP3 - April 2025 Update", no 2026 update found), and `github.com/gradle/declarative-gradle`'s `docs/getting-started/README.md` on its `main` branch, which states as of today (2026-09-19): *"Declarative Gradle is **not ready** for adoption by plugin authors, build engineers or software engineers."* Its `setup.md` confirms the full software-type samples (`javaApplication`, `androidApplication`, etc.) require *nightly* Gradle builds and experimental IDE flags, not stable Gradle. Even Gradle's own "bleeding edge" `unified-prototype` multi-module sample keeps its root **settings file in Kotlin DSL** (`settings.gradle.kts`, applying `org.gradle.experimental.*-ecosystem` plugins) and pins a Gradle *milestone* build (`9.6.0-milestone-1`), not a `.dcl` settings file on stable Gradle — corroborating 02-RESEARCH.md's MEDIUM-confidence April-2025 snapshot as still current; nothing has changed materially since.

**Target 1 (primary): `settings.gradle.kts` → `settings.gradle.dcl` — CONVERTED, project builds and CI is green through it.**
Despite the software-type DSL (used for full project/module definitions like `javaApplication`) genuinely being unready per the above, this repo's `settings.gradle.kts` never used software types — its entire content is `pluginManagement { repositories { ... } }`, `dependencyResolutionManagement { repositories { ... } }`, `rootProject.name = ...`, and two `include(...)` calls. These are core Gradle settings-file APIs, not part of the experimental "ecosystem" plugin surface, and Gradle 9.7.1 (stable, no experimental flags, no nightly build) parses a `.dcl`-suffixed settings file containing them without error. Converted 1:1 (repository lists unchanged: Google, Maven Central, Gradle Plugin Portal in both `pluginManagement` and `dependencyResolutionManagement`; `rootProject.name = "DevFest_Nantes"`; both `include(":androidApp")`/`include(":shared")` preserved).

- `@file:Suppress("UnstableApiUsage")`: dropped, no DCL equivalent needed — this is a Kotlin-compiler annotation suppressing a Kotlin-DSL-specific warning, not a Gradle construct; DCL has no such warning to suppress.
- `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")`: dropped — no DCL equivalent found (DCL settings files have no `enableFeaturePreview(...)` call form). Verified safe by search: `grep -rn 'projects\.\(shared\|androidApp\)' **/*.kts` returns zero hits project-wide; both build scripts use the string form `project(":shared")` (see `androidApp/build.gradle.kts:137`), never the typesafe accessor form the flag enables. Dropping it is behavior-preserving, confirmed by a full local build (not assumed).
- The commented-out `//repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)` line was not carried across (dead code, and DCL is declarative-only — no commented-out imperative statements to preserve).
- **Verification:** `./gradlew --no-daemon projects` → root `DevFest_Nantes` with exactly `:androidApp` and `:shared` (topology intact). `./gradlew --no-daemon clean :shared:assemble :androidApp:assembleDebug :androidApp:assembleRelease :shared:jvmTest :androidApp:testDebugUnitTest detekt lint` → `BUILD SUCCESSFUL` (194 tasks, 176 executed). `./gradlew --no-daemon :shared:compileKotlinIosSimulatorArm64` → `BUILD SUCCESSFUL`.

**Target 2 (fallback): `buildSrc/build.gradle.kts` — NOT ATTEMPTED, primary succeeded.** Per the plan's explicit ordering ("only if the primary attempt fails"), the fallback conversion was not needed and `buildSrc/build.gradle.kts` (its 4-line `plugins { \`embedded-kotlin\` }` content) is untouched.

**Repository ends on:** `settings.gradle.dcl` (the `.kts` file was deleted in the same commit — the two settings-file forms never coexisted at any point in this repo's history).

**`androidApp`/`shared` remain on Kotlin DSL (`.kts`), unchanged by this pilot — reason (discharges BUILD-07's "documented with the reason support is missing" clause):** D-08 scoped the pilot to one low-risk leaf target only; neither module's build file was attempted, per the plan's explicit scope boundary. Independent of that scoping decision, the live re-check above confirms AGP/KMP DCL support for a module of this shape would not be viable today even if attempted: `shared/build.gradle.kts` applies 7 plugins including `com.android.kotlin.multiplatform.library` (AGP 9's new KMP plugin), Detekt, KSP, Apollo, and `kmpNativeCoroutines`; `androidApp/build.gradle.kts` applies 8 plugins including Dagger Hilt, Firebase Crashlytics/Performance, and Compose. Declarative Gradle's only path to expressing a module like this is its "Software Types" model (`javaApplication`/`androidApplication`/`kotlinJvmLibrary` etc.) via the experimental `org.gradle.experimental.android-ecosystem`/`kmp-ecosystem` settings plugins — exactly the surface the Declarative Gradle project's own docs call "not ready for adoption," gated behind nightly Gradle builds and IDE internal-mode flags, with no stable-Gradle path found. This is a genuinely different case from the settings file's plain repository/include declarations, which map onto core (non-experimental) Gradle settings APIs DCL already parses correctly on stable Gradle.

**Phase 3 interaction (per assumption A5/D-09):** the pilot target chosen here is the settings file, which is orthogonal to Phase 3's module split (Phase 3 changes `shared`'s internal module boundaries, not the root settings file's repository/include shape) — no re-pilot expected to be forced by Phase 3, but Phase 3 should re-run the same live status check before assuming DCL's software-type surface has matured enough to attempt `shared`'s or any new `core-*`/`feature-*` module's build file.

**Phase 03 re-check (03-01 tracer, 2026-09-23):** `includeBuild("build-logic")` inside `settings.gradle.dcl`'s `pluginManagement` parsed and resolved correctly on Gradle 9.7.1 (confirmed by `./gradlew projects` listing `Included build ':build-logic'` and `:core:model`) — D-20's fallback branch was not needed.
`build-logic/settings.gradle.kts` and every new module's `build.gradle.kts` (`core/model/build.gradle.kts`, `build-logic/convention/build.gradle.kts`) stay plain Kotlin DSL per D-20/RESEARCH Pitfall 2 — no `.dcl` file exists anywhere under `build-logic/` or `core/`, matching the same software-types-not-ready assessment as Phase 2's pilot.
No re-pilot of DCL's software-type surface was attempted; nothing found this session changes the Phase 02 assessment above.

## Phase 02 version deviations

**Version-target policy (selected at 02-01's checkpoint, 2026-09-18):** `newest-verified` — this phase targets the newest verified stable release found during each plan's own live re-verification immediately before that plan's commit, not the literal REQUIREMENTS.md/CONTEXT.md snapshot numbers taken on 2026-09-12. Governs BUILD-01/02/04/05 (all four pin exact numbers in REQUIREMENTS.md); BUILD-03/06/07 were not pinned to exact numbers in REQUIREMENTS.md text so this policy does not create a deviation for them. See `.planning/REQUIREMENTS.md`'s `D-03 version-target deviation` note and each plan's own SUMMARY for the live-verification evidence.

| Req | Target in REQUIREMENTS.md | Version pinned | Matched? | Reason if not |
|-----|---------------------------|-----------------|----------|----------------|
| BUILD-01 | Kotlin 2.4.0 (hard pin) | Kotlin 2.4.20, KSP 2.3.12 | Superset (newest-verified policy) | N/A — deliberate per-policy superset, not a forced deviation. KSP 2.3.12 not Kotlin-paired: KSP 2.3.0+ decoupled its versioning from the Kotlin compiler version entirely (google/ksp release notes) |
| BUILD-02 | AGP 9.2.0 + `com.android.kotlin.multiplatform.library` (hard pin) | AGP 9.4.0 | Superset (newest-verified policy) | N/A — deliberate per-policy superset. Forced sibling bumps in the same commit: Detekt 1.23.8 → 2.0.0-alpha.6 (`dev.detekt` group/plugin-id, approved via blocking-human package-legitimacy checkpoint — official detekt org's own pre-1.0 next-major line), Dagger Hilt 2.57.2 → 2.60.1, Firebase Performance plugin 2.0.1 → 2.0.2 — all three are AGP-9-documented compatibility floors, not discretionary |
| BUILD-03 | Gradle 9.7.1 (hard pin, not part of the newest-verified policy — already exact) | Gradle 9.7.1 | Matched exactly | N/A |
| BUILD-04 | Compose BOM 2026.08.00 (hard pin) | Compose BOM 2026.09.00 | Superset (newest-verified policy) | N/A — deliberate per-policy superset. Forced compileSdk 36 → 37 (Compose BOM 2026.09.00's AAR metadata floor: `androidx.compose.material:material-android:1.12.1` and 11 sibling artifacts declare `compileSdk >= 37`); targetSdk deliberately kept at 36, decoupled, tracked as a separate follow-up (see Blockers/Concerns above) |
| BUILD-05 | Apollo GraphQL 5.0.1 (hard pin) | Apollo 5.2.0, `appolloCache` 1.0.8 | Superset (newest-verified policy) | N/A — deliberate per-policy superset. Normalized cache migrated from the deprecated `com.apollographql.apollo` group to the new `com.apollographql.cache` group (`normalized-cache`, `normalized-cache-sqlite`, `normalized-cache-apollo-compiler-plugin`), all pinned to `appolloCache` 1.0.8 |
| BUILD-06 | Firebase BOM / Coroutines / kotlinx-serialization / kotlinx-datetime "latest stable" (not pinned) | Firebase BOM 34.19.0, kotlinx-coroutines 1.11.0, kotlinx-serialization-json 1.11.0, kotlinx-datetime 0.8.0 | Matched (requirement text says "latest stable", not an exact number) | N/A |
| BUILD-07 | Build files migrated to `.gradle.dcl` where AGP/KMP support allows; unsupported modules documented on `.kts` with the reason | `settings.gradle.dcl` converted; `androidApp`/`shared` stay on `.kts` | Matched — satisfied via the explicit partial-migration branch (D-08), not the full-migration branch | N/A — see `## Phase 02 DCL pilot outcome` above for the full outcome record and the specific reason `androidApp`/`shared` are undocumented-for-DCL-support |

**kotlinx-datetime `Instant`/`Clock` migration surface (assumption A2, closes an open research question so Phase 3 does not re-investigate):** `kotlinx.datetime.Instant`'s typealias to `kotlin.time.Instant` survived (deprecated, not removed) into kotlinx-datetime 0.8.0 — `ScheduleSlot.kt`/`Agenda.kt` (shared/model) needed zero code changes. However `kotlinx.datetime.Clock`'s typealias does **not** carry `.System` through to `kotlin.time.Clock` the same way — any call site using `Clock.System` directly (not just `Instant`) needs its import swapped to `kotlin.time.Clock`. One such call site existed outside the shared/model scope: `androidApp/.../ui/screens/agenda/Agenda.kt`, fixed with a one-line import swap. Grep for `kotlinx.datetime.Clock` before assuming a future kotlinx-datetime bump is a no-op.

**G-05-3 amendment (Phase 05):** the shipped kotlinx-datetime artifact is now `0.8.0-0.6.x-compat` (0.8.0 plus the deprecated `Instant`/`Clock` classes that OpenFeedback's prebuilt bytecode links against), and first-party imports moved to `kotlin.time.Instant` in 05-11. This amends the BUILD-06 row above from "0.8.0" to its compat twin; the compat classes are a runtime-only bridge for OpenFeedback.

**Apollo cache import paths actually used (authoritative, for Phase 3 reuse):**

- Cache-normalized wildcard: `com.apollographql.cache.normalized.*` (was `com.apollographql.apollo.cache.normalized.*`)
- `MemoryCacheFactory`: `com.apollographql.cache.normalized.memory.MemoryCacheFactory` (own `memory` subpackage — was `...api.MemoryCacheFactory`)
- `SqlNormalizedCacheFactory`: `com.apollographql.cache.normalized.sql.SqlNormalizedCacheFactory` (same subpackage name, new root group)
- `NormalizedCacheFactory`, `CacheKey`, `CacheKeyGenerator`, `CacheKeyGeneratorContext`, `CacheResolver`, `DefaultCacheResolver`, `ResolverContext`: all under `com.apollographql.cache.normalized.api`
- `FetchPolicy`, `fetchPolicy` extension, `normalizedCache` extension: `com.apollographql.cache.normalized`
- Runtime APIs (`ApolloClient`, `api.http.HttpHeader`) stay on `com.apollographql.apollo.*` — unmoved

**Detekt alpha pinned:** `dev.detekt` 2.0.0-alpha.6 (group and plugin-id both renamed from `io.gitlab.arturbosch.detekt`). **Advisory-findings note:** treat findings from this pre-1.0 alpha as advisory, not authoritative, until Detekt reaches a stable 2.x release — approved via a blocking-human package-legitimacy checkpoint in 02-02, not a supply-chain risk (official detekt org's own next-major line) but pre-1.0 maturity means CI-breaking regressions are more likely than with a stable release. **Before/after issue counts:** 11 real findings surfaced immediately after the `dev.detekt:detekt-rules-ktlint-wrapper` coordinate fix (proof the ktlint-wrapper ruleset was genuinely active, not a silent no-op), 0 after the 11 source fixes landed in the same commit (`c56d6ab`).

## Deferred Items

Items acknowledged and deferred at milestone close, most recent first:

| Category | Item | Status | Deferred At | Milestone |
|----------|------|--------|-------------|-----------|
| debug_sessions | feedback-kotlinx-datetime-instant-crash | diagnosed (fixed in code by 05-11; session status never closed) | 2026-10-16 | v1.0 |
| deferred_items | 05/deferred-items.md: GraphQLStore and mappers (println logging, deprecated `speakers` field returns 100 of 110, RoomSortIndex misses `Tour Bretagne`/`Unconf`, SocialItem double slash, empty-url social link) | acknowledged | 2026-10-16 | v1.0 |

## Session Continuity

Last session: 2026-10-26T13:52:09.441Z
Stopped at: Phase 6 context gathered
Resume file: .planning/phases/06-ios-test-foundation-and-ci-gates/06-CONTEXT.md

## Operator Next Steps

- Discuss Phase 6 with /gsd-discuss-phase 6, then plan it with /gsd-plan-phase 6
