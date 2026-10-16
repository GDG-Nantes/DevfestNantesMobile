---
phase: 05-test-coverage-retrofit
plan: 04
subsystem: testing
tags: [apollo, mockserver, graphql, kmp, android-host-test, mappers, normalized-cache]

requires:
  - phase: 05-test-coverage-retrofit
    provides: "05-03 fixed fixtures and the deterministic buildVenueStub fallback"
provides:
  - "RecordedResponses: trimmed confetti-app.dev recordings for all six operations as Kotlin string constants"
  - "GraphQLStoreMockServerTest: GraphQLStore over Apollo MockServer on JVM, Android host and iOS simulator (17 tests)"
  - "MapperTest: recorded-JSON and data-builder tests for every mapper branch (23 tests)"
  - "Android host tests enabled for :core:data (testAndroidHostTest, androidHostTest source set with Robolectric ready) and run by CI"
  - "ApolloCacheKeys.kt: cacheKeyGenerator and cacheResolver split from the production apolloClient file"
  - "deferred-items.md with the five production behaviors the tests expose"
affects: [05-05, 05-06, 05-10]

actuals:
  tokens: 21000
  tasks: 3
  commits: 3

tech-stack:
  added:
    - "com.apollographql.mockserver:apollo-mockserver 0.3.1 (test only)"
  patterns:
    - "Recorded responses as Kotlin string constants because iOS commonTest has no portable resource loading"
    - "Per-test MockServer + ApolloClient (+ in-memory cache with the production cache keys), server closed in finally"
    - "Apollo data builders generated into :core:network commonMain for edge-case responses"

key-files:
  created:
    - core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/graphql/RecordedResponses.kt
    - core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/graphql/GraphQLStoreMockServerTest.kt
    - core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/graphql/MapperTest.kt
    - core/network/src/commonMain/kotlin/com/gdgnantes/devfest/core/network/ApolloCacheKeys.kt
    - .planning/phases/05-test-coverage-retrofit/deferred-items.md
  modified:
    - gradle/libs.versions.toml
    - core/data/build.gradle.kts
    - core/network/build.gradle.kts
    - core/network/src/commonMain/kotlin/com/gdgnantes/devfest/core/network/Apollo.kt
    - .github/workflows/android.yml

key-decisions:
  - "apollo-mockserver pinned to 0.3.1 after re-checking Maven metadata (latest and release both 0.3.1); coordinate is com.apollographql.mockserver, not the old 4.x one"
  - "Data builders worked (D-13 data-builder half kept): generateDataBuilders + connectToKotlinSourceSet(\"commonMain\") compile, :core:data commonTest sees them, the iOS framework links and swift-names-gate passes"
  - "Android host tests are opted in per module (:core:data) and not in the convention plugin"

patterns-established:
  - "Mutation check for pins: temporarily restoring the pre-fix mapNotNull-to-empty behavior makes four store tests fail"

requirements-completed: [TEST-02]

coverage:
  - id: D1
    description: "GraphQLStore runs against Apollo MockServer with a real ApolloClient and recorded data on JVM, Android host and iOS simulator"
    requirement: TEST-02
    verification:
      - kind: unit
        ref: "core/data GraphQLStoreMockServerTest (17 tests) present in jvmTest, testAndroidHostTest and iosSimulatorArm64Test results"
        status: pass
    human_judgment: false
  - id: D2
    description: "Mapper branches pinned by concrete-value tests from recorded JSON (parseResponse) and Apollo data builders"
    requirement: TEST-02
    verification:
      - kind: unit
        ref: "core/data MapperTest (23 tests) on jvmTest, testAndroidHostTest and iosSimulatorArm64Test; swift-names-gate.sh check prints SWIFT-NAMES-OK"
        status: pass
    human_judgment: false
  - id: D3
    description: "CacheAndNetwork behavior (cache miss, cached-then-error, cached-then-success) and offline fallbacks pinned with the production cache keys"
    requirement: TEST-02
    verification:
      - kind: unit
        ref: "GraphQLStoreMockServerTest#network_error_after_cached_success_emits_only_cached_sessions (and speakers, partners, rooms), cached_then_network_success_emits_twice, getVenue_falls_back_to_the_offline_stub_on_error, getters_return_null_or_empty_on_error"
        status: pass
    human_judgment: false
  - id: D4
    description: "CI unit job runs testAndroidHostTest and is green on the final code HEAD"
    requirement: TEST-02
    verification:
      - kind: other
        ref: "android.yml run 38865791837 (log contains Task :core:data:testAndroidHostTest) and ios.yml run 38865791835 on d23375b, both success"
        status: pass
    human_judgment: false

duration: 50min
completed: 2026-10-16
status: complete
plan_head_before: 5175b6079581b10b2951793b5738038c743cbcb8
plan_head_after: d23375ba776dde2a927917ff1c924a8bc04897e5
commits: 3
---

# Phase 5 Plan 04: GraphQLStore and Mapper Tests Summary

**GraphQLStore and every GraphQL-to-model mapper branch now run offline against Apollo MockServer with trimmed recorded confetti-app.dev responses on JVM, Android host and the iOS simulator, with the Phase 2 offline-cache fix pinned using the production cache keys.**

## Performance

- **Duration:** about 50 min (including about 10 min waiting for CI)
- **Tasks:** 3 (1 tracer, 1 auto, 1 auto/tdd)
- **Files:** 10 changed (5 new)

## Accomplishments

- Tracer: `apollo-mockserver` 0.3.1 in the catalog, `:core:data` opted into `withHostTest`, CI step changed to `./gradlew testDebugUnitTest jvmTest testAndroidHostTest`. Seven recordings (rooms, 5 of 94 sessions, one session, 7 speakers, one partner per group, venue en and fr) were captured from the live API with the generated `OPERATION_DOCUMENT`s and stored as string constants (largest 19 KB).
- Mapper tests: 23 tests. Recorded JSON goes through `Operation.parseResponse(jsonReader)`; expectations are literals from the recording (ids, titles, `+02:00` dates, category label, room sort index, feedbackId differing from id, four-speaker codelab, room-less lunch, venue en/fr). Edge cases come from Apollo data builders: null/unknown language, complexity and type, no rooms, no speakers, no tags, null feedbackId, the three `startsAt` forms, unknown social name, unknown room id, partner title case and unknown title, null address.
- Store tests: 17 tests in total. All flows and getters on recorded data, plus cache behavior on a client with `MemoryCacheFactory` and the production `cacheKeyGenerator`/`cacheResolver`: a miss emits only the network result; a 500 after a cached success emits only the cached value for sessions, speakers, partners and rooms; cached then success emits twice; `getVenue` falls back to `buildVenueStub`; getters return null or empty on error.
- `cacheKeyGenerator` and `cacheResolver` moved verbatim to `ApolloCacheKeys.kt`, so tests no longer build the production SQLite-backed `apolloClient`.
- Mutation check: temporarily making the sessions flow emit an empty list on error (the pre-Phase-2 behavior) failed four store tests; the production file was restored.

## Task Commits

1. Task 1 (tracer): `c627aef` test(05-04): GraphQLStore over Apollo MockServer on JVM, Android host and iOS
2. Task 2: `bcc258f` test(05-04): GraphQL mapper tests from recorded responses and data builders
3. Task 3: `d23375b` test(05-04): pin GraphQLStore CacheAndNetwork and offline fallbacks (D-14)

## Verification

- MOCKSERVER-3-RUNTIMES-OK (GraphQLStoreMockServerTest present in jvmTest, testAndroidHostTest, iosSimulatorArm64Test results), RECORDED-CLEAN (no key/token/password shapes), MAPPERS-GREEN, SWIFT-NAMES-OK, CACHE-PINNED, STORE-TESTS-IDEMPOTENT (jvmTest `--rerun` twice), DI-GATE-OK.
- Full local run: `:core:network:jvmTest :core:data:jvmTest :core:data:testAndroidHostTest :core:data:iosSimulatorArm64Test :androidApp:testDebugUnitTest :shared:jvmTest detekt` BUILD SUCCESSFUL; `git status --porcelain -- core/data/src/commonMain` empty.
- CI-GREEN-BOTH on `d23375b`: android.yml run 38865791837 (all four jobs, Instrumentation tests (34) included) and ios.yml run 38865791835. CI-RUNS-HOST-TESTS: the android log contains `Task :core:data:testAndroidHostTest`.
- The SUMMARY/STATE/ROADMAP commit that follows changes only `.planning` files.

## TDD Notes

Task 3 is a characterization of behavior that already exists (the Phase 2 `mapNotNull` fix), so the new tests passed on first run and there is no separate RED commit. The mutation check above stands in for RED: restoring the old behavior fails four tests, so the pins are real.

## Deviations from Plan

None in scope. Two small notes:

- The speakers recording has 7 of the 8 speakers of the recorded sessions because the live deprecated `speakers` field omits `xavier_charef`. That is production behavior and is logged in `deferred-items.md` (item 2), not worked around.
- The data-builder half of D-13 worked on the first attempt, so no `D-13 data-builder fallback` entry is needed. The Apollo build-script API prints one `w:` experimental-API warning at configuration time for `dataBuildersOutputDirConnection`.

## Issues Encountered

- The `GetVenue` and `GetSession` operation documents contain `${'$'}` as Kotlin escapes when copied from the generated source, so the first recording attempts returned GraphQL syntax errors. Fixed by replacing the escapes with `$` before posting; no impact on the committed files.

## Authentication Gates

None.

## Known Stubs

None.

## Threat Flags

None new. T-5-07 (recorded data): public conference data only, RECORDED-CLEAN scan passes. T-5-08 (builders in :core:network commonMain): the swift-names gate passes and `:core:network` is still not exported. T-5-SC: exact pin 0.3.1 after the Maven metadata check.

## Next Phase Readiness

Ready for 05-05. The `androidHostTest` source set already has Robolectric, JUnit and androidx test-core on its classpath; `RecordedResponses` and the `withStore` pattern are reusable. Deferred list in `.planning/phases/05-test-coverage-retrofit/deferred-items.md`.

## Self-Check: PASSED

Created files exist on disk; commits c627aef, bcc258f and d23375b are on the branch (3 measured from the plan ledger); MOCKSERVER-3-RUNTIMES-OK, RECORDED-CLEAN, MAPPERS-GREEN, SWIFT-NAMES-OK, CACHE-PINNED, STORE-TESTS-IDEMPOTENT, DI-GATE-OK, CI-GREEN-BOTH and CI-RUNS-HOST-TESTS observed.
