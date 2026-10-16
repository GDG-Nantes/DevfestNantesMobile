---
phase: 05-test-coverage-retrofit
verified: 2026-10-16T14:00:00Z
status: human_needed
score: 4/4 roadmap success criteria verified (all plan must-have truths verified; 3 on-device human checks open)
covered_files:
  - .planning/phases/05-test-coverage-retrofit/05-01-PLAN.md
  - .planning/phases/05-test-coverage-retrofit/05-01-SUMMARY.md
  - .planning/phases/05-test-coverage-retrofit/05-02-PLAN.md
  - .planning/phases/05-test-coverage-retrofit/05-02-SUMMARY.md
  - .planning/phases/05-test-coverage-retrofit/05-03-PLAN.md
  - .planning/phases/05-test-coverage-retrofit/05-03-SUMMARY.md
  - .planning/phases/05-test-coverage-retrofit/05-04-PLAN.md
  - .planning/phases/05-test-coverage-retrofit/05-04-SUMMARY.md
  - .planning/phases/05-test-coverage-retrofit/05-05-PLAN.md
  - .planning/phases/05-test-coverage-retrofit/05-05-SUMMARY.md
  - .planning/phases/05-test-coverage-retrofit/05-06-PLAN.md
  - .planning/phases/05-test-coverage-retrofit/05-06-SUMMARY.md
  - .planning/phases/05-test-coverage-retrofit/05-07-PLAN.md
  - .planning/phases/05-test-coverage-retrofit/05-07-SUMMARY.md
  - .planning/phases/05-test-coverage-retrofit/05-08-PLAN.md
  - .planning/phases/05-test-coverage-retrofit/05-08-SUMMARY.md
  - .planning/phases/05-test-coverage-retrofit/05-09-PLAN.md
  - .planning/phases/05-test-coverage-retrofit/05-09-SUMMARY.md
  - .planning/phases/05-test-coverage-retrofit/05-10-PLAN.md
  - .planning/phases/05-test-coverage-retrofit/05-10-SUMMARY.md
  - build.gradle.kts
  - core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/DevFestNantesStoreMocked.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/utils/IsoDate.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaViewModel.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/FeedbackFlags.kt
covered_digest: "v3:sha256:6d9cd18e6688c58fd7873fc6a9667120bb95421b95c72b2f8a020190f4cc7cb8"
behavior_unverified: 0
overrides_applied: 0
human_verification:
  - test: "Switch the device (or emulator) language to English; open the Agenda (both day tabs) and one session detail"
    expected: "Day tab labels, time headers and the session date range read as before the phase (same words, digits and order); the agenda opens scrolled to the current time slot"
    why_human: "Unit tests pin Robolectric's ICU output, not the device's. French was checked on a Pixel_9 emulator in 05-02; English on device was not (05-02 SUMMARY, 'Human check (open)'). The agenda auto-scroll was not compared against a pre-change build."
  - test: "On the debug app, open the Agenda, switch days, apply then clear a filter from the drawer, toggle a bookmark, pull to refresh"
    expected: "Agenda loads, filters, bookmarks and refreshes as before; no jank or freeze while filtering (AgendaViewModel now receives its IO dispatcher through Koin)"
    why_human: "Runtime feel of filtering off the main thread is only observable on a device (05-06 plan human-check, still open)"
  - test: "On the debug app, open several session details in a row and scroll to the feedback area (after the consent dialog)"
    expected: "The feedback area shows the OpenFeedback form, the fallback link, or nothing, according to the remote flags, as before the FeedbackFlags seam; no crash"
    why_human: "Real Remote Config values only arrive on a device; the feedback area sits behind the consent dialog and was not exercised in 05-08"
---

# Phase 5: Test Coverage Retrofit Verification Report

**Phase Goal:** Business-logic test coverage is measurably improved on a hardened test-fixture foundation, prioritizing ViewModels, Store/repository, and GraphQL mappers over a strict percentage target
**Verified:** 2026-10-16
**Status:** human_needed
**Re-verification:** No (initial verification)

The goal is achieved in the code. Every roadmap success criterion and every plan truth I checked holds against the repository, and I found no gaps. Status is `human_needed` only because three on-device checks that the plans deliberately deferred are still open. They are display and feel checks that no test in this repo can prove.

## Goal Achievement

### Observable Truths (ROADMAP success criteria)

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | Blocking fragilities fixed first: `StoreStubs.kt` RNG seeded, `SimpleDateFormat` thread-safety resolved in tested paths | VERIFIED | `grep -rn "kotlin.random\|Random" core/model core/data/src/commonMain` returns nothing. `StoreStubs.kt` is now deterministic wrappers over `SessionFixtures`/`SpeakerFixtures`/`PartnerFixtures`, and the RNG is removed rather than seeded, which is stronger. `SimpleDateFormat` appears in no production source: the only 2 hits in the repo are KDoc comments in tests. `core/model/.../utils/IsoDate.kt` provides `String.toInstantOrNull()` (via `Instant.parseOrNull`) and `Instant.toLocalDate()`. Commit order is right: characterization `f337dc1` precedes the swap `7dc3620`, and the Kover baseline `8983b66` precedes both. |
| 2 | `commonTest` coverage of Store/repository and GraphQL mappers extended, following the `DevFestNantesStoreContractTest` pattern | VERIFIED | `DevFestNantesStoreContractTest` is abstract and run by `MockedStoreContractTest` and `GraphQLStoreContractTest` (Apollo MockServer). `MapperTest`, `GraphQLStoreMockServerTest`, `RecordedResponses*` are in `core/data/src/commonTest` (86 `@Test` in 8 files). I ran `:core:data:jvmTest`: 80 tests, 0 failures, 0 skipped (the live-network canary is excluded by default, `core/data/build.gradle.kts:45`). `ios.yml:131` runs `:shared`, `:core:model`, `:core:data`, `:core:testing` `iosSimulatorArm64Test`, and `android.yml:49` runs `testAndroidHostTest`, so the commonTest suites run on all three targets. |
| 3 | ViewModel test coverage extended across feature modules via constructor injection / Koin | VERIFIED | All 10 ViewModels in the repo have a matching `*ViewModelTest` (Agenda, Session, FeedbackForm, Speakers, Speaker, Venue, Partners, DataCollection, Bookmarks, Home). `AgendaViewModel` takes `ioDispatcher: CoroutineDispatcher`, bound by `single<CoroutineDispatcher>(named(IO_DISPATCHER)) { Dispatchers.IO }` in `CoreUiModule.kt:19`. `FeedbackFormViewModel` depends on the `FeedbackFlags` seam. I ran `:feature:agenda:testDebugUnitTest` (34 tests) and `:feature:speakers:testDebugUnitTest` (8 tests): 0 failures, 0 skipped. There is no `@Ignore` anywhere in the repo. |
| 4 | Compose smoke tests on critical Android screens, per feature module (`androidTest`), on `core-testing` fakes | VERIFIED | `*SmokeTest.kt` exists in all six feature modules (speakers, venue, about, settings, agenda, session-detail), each with 4 to 10 assertions. `AgendaSmokeTest` uses `KoinTestRule` with the production `agendaModule` + `coreUiModule` + `androidTestDoubles`, and avoids wall-clock dependence. CI runs `connectedDebugAndroidTest` (`android.yml:85`). Android CI run on `034cfe9` (last code commit) and `495b9bb` is `completed success`; iOS CI also success. The instrumented tests cannot run in this verification (no emulator), so the proof for them is CI. |
| 5 | Improvement is measurable (phase goal wording) | VERIFIED | Same-filter Kover baseline (`kover/baseline-summary.txt`, captured before any TEST-01 change) vs final (`kover/final-summary.txt`): total LINE 15.96% -> 30.57%, `:core:data` 26.25% -> 95.74%, `:core:model` 81.70% -> 95.31%, `:core:network` 68.97% -> 96.55%, `:feature:agenda` 9.74% -> 18.54%. `comparison.txt` ends `KOVER-IMPROVED`. No module regressed (`:feature:about` 8.42% -> 8.42%, `:shared` 100% -> 100% unchanged). |

**Score:** 4/4 roadmap success criteria verified, plus the measurable-improvement criterion. No behavior-dependent truth lacks a behavioral test. The state-transition and invariant claims (Turbine-asserted `STARTING -> SUCCESS`, filter emission order, `CacheAndNetwork` ordering) are each exercised by a test that passed in my run or in CI.

### Plan must-have spot checks (SUMMARY claims vs code)

| Plan truth | Status | Evidence |
|------------|--------|----------|
| 05-01 Kover on all 13 production modules, merged at root | VERIFIED | `build.gradle.kts:23-35` lists 13 `kover(project(...))`; `final-summary.txt` has 13 MODULE lines |
| 05-01 Live canary excluded by default | VERIFIED | `core/data/build.gradle.kts:42-47` excludes `*LiveNetwork*` unless `-PliveNetworkTests` |
| 05-01 iOS CI runs core commonTest | VERIFIED | `.github/workflows/ios.yml:131` |
| 05-02 Pure `currentStartTimeIndex`, no `Dispatchers.IO` / `java.util.Date` in `AgendaColumn` | VERIFIED | `CurrentStartTimeIndexTest` exists; review confirmed no regression |
| 05-03 No random in stubs/mock; mock serves fixtures | VERIFIED | grep above; `DevFestNantesStoreMocked.kt` shrank (-40 lines) and `MockedStoreContractTest` pins fixture ids |
| 05-04 MockServer + recorded responses + CacheAndNetwork pins | VERIFIED | `GraphQLStoreMockServerTest`, `MapperTest`, `RecordedResponses.kt` present and green |
| 05-05 Robolectric persistence tests (bookmarks, filters, consent) | VERIFIED | `BookmarksStoreImplTest`, `SessionFiltersServiceImplTest`, `DataCollectionSettingsServiceImplTest` present. See WR-02 on isolation. |
| 05-06 Turbine + `MainDispatcherRule` + `FakeDevFestNantesStore`; test doubles never in production | VERIFIED | files present in `core/testing`; ran `:core:testing:jvmTest` green |
| 05-08 `FeedbackFlags` seam, behavior-neutral in production | VERIFIED | `FeedbackFlags.kt` + `FirebaseFeedbackFlags` use the same `addOnCompleteListener` path; review diffed production against `68419e8` and found no regression |
| 05-10 Final Kover report committed, `KOVER-IMPROVED` | VERIFIED | `kover/final-report.xml`, `comparison.txt` |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| core:model, core:data, core:testing JVM suites | `./gradlew --offline :core:model:jvmTest :core:data:jvmTest :core:testing:jvmTest` | exit 0; 27 + 80 tests, 0 failed, 0 skipped (core:model/core:data counted) | PASS |
| Agenda and Speakers ViewModel suites | `./gradlew --offline :feature:agenda:testDebugUnitTest :feature:speakers:testDebugUnitTest` | exit 0; 34 + 8 tests, 0 failed, 0 skipped | PASS |
| CI on last code commit | `gh run list` | `034cfe9` Android + iOS `success`; `495b9bb` both `success`; `8ed0e82` (docs only) still `in_progress` | PASS |

I did not run the instrumented tests (no device) or the iOS tests locally. Both rely on CI evidence above.

### Requirements Coverage

| Requirement | Source Plans | Description | Status | Evidence |
|-------------|--------------|-------------|--------|----------|
| TEST-01 | 05-02, 05-03 | Blocking fragilities fixed first (RNG, `SimpleDateFormat`) | SATISFIED | SC 1 above |
| TEST-02 | 05-01, 05-04, 05-05 | commonTest Store/repository + GraphQL mapper coverage, contract pattern | SATISFIED | SC 2 above; `:core:data` 26% -> 96% |
| TEST-03 | 05-06, 05-07, 05-08 | ViewModel coverage via constructor injection (Koin) | SATISFIED | SC 3 above; 10/10 ViewModels |
| TEST-04 | 05-01, 05-09, 05-10 | Compose smoke tests per feature module on `core-testing` | SATISFIED | SC 4 above; 6/6 feature modules |

All four IDs appear in plan frontmatter and in `REQUIREMENTS.md` (lines 47-50, marked `[x]`; traceability lines 117-120 `Complete`). No orphaned requirements: `REQUIREMENTS.md` maps no other ID to Phase 5.

### Anti-Patterns Found

| File | Pattern | Severity | Impact |
|------|---------|----------|--------|
| TBD/FIXME/XXX markers in phase-modified files | none found by `05-REVIEW.md`; spot grep of production diffs found none | none | clean |
| `core/data/.../GraphQLStoreLiveNetworkTest.kt` | several tests with no real assertion (REVIEW IN-03) | Info | Canary is excluded from default runs; low cost |

### Non-blocking warnings (from `05-REVIEW.md`, 0 blockers; each independently checked and not goal-blocking)

- **WR-01** `KoinStartupTest` resolves `FeedbackFormViewModel` against the real Remote Config, so `fetchAndActivate()` may attempt a real network call from a host-JVM test (hermeticity, not correctness).
- **WR-02** Firebase process-wide singletons are not reset between Robolectric tests (`DataCollectionSettingsServiceImplTest`, `KoinStartupTest`); order-dependence risk. Tests are green in CI today.
- **WR-03** Two defects pinned by tests are missing from `deferred-items.md`: the `//` in the OpenFeedback fallback URL (I confirmed: `WebLinks.OPENFEEDBACK_BASE_URL` ends in `/` and `FallbackFeedbackForm.kt:48` appends another `/`), and the fixed `+02:00` offset appended in the mapper. Neither is a Phase 5 regression. The ledger should gain two entries.
- **Process note:** `deferred-items.md` item 4 is documented by code reading only, not pinned by a test, as its own note says; its closing count (16) is consistent with the list.

These are recommended follow-ups, not gaps against the phase goal. They can be fixed in this branch before merge or tracked as backlog items.

### Human Verification Required

#### 1. English-locale display and agenda auto-scroll on a device

**Test:** Set the device language to English. Open the Agenda (both day tabs) and one session detail.
**Expected:** Labels, time headers and session date range read as before this phase; the agenda opens scrolled to the current time slot.
**Why human:** Robolectric's ICU output is not the device's; French was verified, English was not; auto-scroll was not compared with a pre-change build.

#### 2. Agenda interaction walkthrough (dispatcher injection)

**Test:** Debug app: open Agenda, switch days, apply then clear a filter, toggle a bookmark, pull to refresh.
**Expected:** Behaves as before, no jank while filtering.
**Why human:** Responsiveness off the main thread is only observable on a device.

#### 3. Feedback area on session detail

**Test:** Debug app: open several session details in a row, accept the consent dialog, scroll to the feedback area.
**Expected:** Form, fallback link, or nothing per remote flags, as before; no crash.
**Why human:** Real Remote Config values arrive only on a device; the area is behind the consent dialog.

### Gaps Summary

No gaps. The phase goal holds: fragilities are fixed before extension, store and mapper coverage runs on JVM, Android host and iOS, all 10 ViewModels are tested, all six features have smoke tests that pass in CI, and coverage improved measurably against a same-filter baseline (total 15.96% -> 30.57%, `:core:data` 26% -> 96%). Remaining work is the three open device checks and, optionally, the three review warnings (WR-01 to WR-03).

---

_Verified: 2026-10-16_
_Verifier: Claude (gsd-verifier)_
