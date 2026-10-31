---
phase: 07-kmp-domain-layer-and-android-adoption
verified: 2026-10-30T19:00:00Z
status: passed
score: 5/5 must-haves verified
covered_files:
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-01-PLAN.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-01-SUMMARY.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-02-PLAN.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-02-SUMMARY.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-03-PLAN.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-03-SUMMARY.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-04-PLAN.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-04-SUMMARY.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-05-PLAN.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-05-SUMMARY.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-06-PLAN.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-06-SUMMARY.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-07-PLAN.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-07-SUMMARY.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-08-PLAN.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-08-SUMMARY.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-09-PLAN.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-09-SUMMARY.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-10-PLAN.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-10-SUMMARY.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-11-PLAN.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-11-SUMMARY.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-12-PLAN.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-12-SUMMARY.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-13-PLAN.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-13-SUMMARY.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-14-PLAN.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-14-SUMMARY.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-15-PLAN.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-15-SUMMARY.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-16-PLAN.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-16-SUMMARY.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-17-PLAN.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-17-SUMMARY.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-18-PLAN.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-18-SUMMARY.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-19-PLAN.md"
  - ".planning/phases/07-kmp-domain-layer-and-android-adoption/07-19-SUMMARY.md"
  - "core/data/src/androidMain/kotlin/com/gdgnantes/devfest/core/data/di/DataPlatformModule.android.kt"
  - "core/data/src/iosMain/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsBookmarksStore.kt"
  - "core/data/src/iosMain/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsSessionFiltersStore.kt"
  - "core/data/src/iosTest/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsBookmarksStoreTest.kt"
  - "core/data/src/iosTest/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsSessionFiltersStoreTest.kt"
  - "core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/di/DomainModule.kt"
  - "feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaViewModel.kt"
  - "feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/Speakers.kt"
  - "feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/SpeakersViewModel.kt"
  - "feature/speakers/src/test/java/com/gdgnantes/devfest/feature/speakers/list/SpeakerIndexCaseFoldingTest.kt"
  - "feature/speakers/src/test/java/com/gdgnantes/devfest/feature/speakers/list/SpeakerIndexEmptyNameTest.kt"
  - "scripts/record-agenda-snapshot.sh"
  - "shared/src/commonMain/kotlin/com/gdgnantes/devfest/shared/di/InitKoin.kt"
covered_digest: "v3:sha256:e323ce42567a5c0108e16f79d6261573e96c6ff2bc4c287333c92e354c8a753d"
behavior_unverified: 0
overrides_applied: 0
re_verification:
  previous_status: passed
  previous_score: 5/5
  gaps_closed: []
  gaps_remaining: []
  regressions: []
advisory:
  - finding: "IN-02 (07-REVIEW): WR-01 fix removes the ß divergence, but the shared SpeakerIndexUseCase still de-duplicates on the upper-cased letter, so dotless ı before i leaves the later i speaker without a row header and unreachable from the scroller (v1.0 showed both). Latent: all 100 recorded speaker names start with an ASCII letter."
    category: other
    reason: "Edge-case deviation from v1.0 on data that does not exist today; fixing it changes the shared (iOS-visible) letter definition. Triage before Phase 9 or the next edition."
    evidence_status: "code review reading and 07-UI-REVIEW Top Fix 3; no failing test (data set has no such letter)"
  - finding: "IN-01, IN-03, IN-04 (07-REVIEW and earlier): unused destructured `letter` in SpeakersViewModel.firstLetterIndexes, duplicate TestDataFiles, repeated day-label parse."
    category: other
    reason: "Hygiene only; recorded open in 07-REVIEW-DISPOSITION.md; no success criterion touched."
    evidence_status: "code review reading"
human_verification:
  - test: "Re-capture 14-speakers-scroller on the current branch head (>= 575970ae) and compare with 07-parity/main/14-speakers-scroller.png: open Speakers, check row header letters, drag the A-Z scroller to several letters (including a letter with no speaker) and rotate the device once."
    expected: "Headers appear on the first speaker of each letter exactly as on main, the scroller jumps to the same rows, no flicker or missing header after load or rotation."
    why_human: "The 15 paired parity captures were taken on 4af25ed (2026-10-29), before WR-01 (b6ff4909) and WR-04 (575970ae) changed how the Speakers screen gets its letter map (now a separate letterIndexes StateFlow). Compose rendering is not covered by any automated test; only the logic is (golden speaker-index characterization test, 100 ASCII speakers, unchanged and green)."
---

# Phase 7: KMP Domain Layer and Android Adoption Verification Report

**Phase Goal:** The agenda business logic (filtering, grouping, classification, bookmarks and filters) lives in a KMP `:core:domain` module tested on JVM and iOS simulator, and Android already uses it without any visible difference
**Verified:** 2026-10-30
**Status:** human_needed
**Re-verification:** Yes, after code-review fixes WR-01..WR-04 (b6ff4909, 89ccddd0, 87c9fe66, 575970ae), the Nyquist test (ec4b24c2) and a fresh incremental review (0 critical, 0 warning, 2 info)

## Summary of the re-verification

The previous report (passed 5/5, 2026-10-29) went stale because covered source changed. I re-checked the goal against the current tree instead of carrying the earlier verdict forward. No regression was found: all five roadmap success criteria still hold in code. The one thing the changes put out of reach of the earlier evidence is the visual parity claim for the Speakers screen, which is why the status moves from `passed` to `human_needed` (one quick manual check), not to `gaps_found`.

## Goal Achievement

### Observable Truths (ROADMAP success criteria)

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | `:core:domain` exported by `:shared`, `domainModule` in `sharedModules`, `verify()` passes with no duplicate binding, `swift-names-gate.sh` passes, iOS compiles and behaves as before | VERIFIED | Unchanged since the first verification (`shared/build.gradle.kts` `api`/`export`, `InitKoin.sharedModules` with `domainModule`, `allowOverride(false)`, `KoinGraphTest`). Re-ran in this session: `bash scripts/swift-names-gate.sh check` prints `SWIFT-NAMES-OK` (it linked the iOS simulator framework), `bash scripts/di-gate.sh` prints `DI-GATE-OK`. Post-fix commits touched no exported Kotlin API (only `internal` iOS store classes and Android feature code). |
| 2 | Characterization tests committed before each logic move; shared use cases pass them in `commonTest` on JVM and iOS simulator | VERIFIED | Commit ordering and goldens unchanged (characterization 07-03..07-08 and 07-05/07-06 precede the shared use cases 07-12/07-13 and the Android moves 07-15..07-18). `core/domain/testdata/agenda/golden/` untouched by the review fixes (`android-speaker-index.json` last changed in 21bacd2c; the speaker-index use case last changed in 5b4bec44). Full Gradle suite passed on ae3da148 (supplied evidence), including the five `iosSimulatorArm64Test` tasks. |
| 3 | Divergent behaviors stay per platform or go through an explicit parameter; the iOS room-order deviation is documented | VERIFIED | No change in this area. `AgendaViewModel` still passes the day assignment and time zone explicitly; `documentation/ARCHITECTURE.md:306` documents the `RoomSortIndex` deviation. |
| 4 | `BookmarksStore` and `SessionFiltersStore` are common interfaces; v1.0 Robolectric tests pinning `selected_sessions` and the filter format pass; iOS stores use new keys with the shared codec and never read legacy keys | VERIFIED | Re-read both iOS stores after WR-03: keys `devfest.bookmarks.sessionIds` and `devfest.agenda.filters`, `SessionFilterCodec` for filters, no reference to the legacy `Favorites`/`AgendaSessionFilters` keys in main code (legacy-key-untouched tests still present). WR-03 only wraps update and persist in one `NSLock` (`try/finally`); semantics and key/format are unchanged. I ran `:core:data:iosSimulatorArm64Test --tests '*UserDefaultsSessionFiltersStoreTest*'` against the current tree: 8 tests, 0 failures, including the new `concurrent_writers_persist_the_latest_state` (ec4b24c2, which post-dates the green full-gate commit ae3da148 and was the only code delta after it). Android side untouched by the fixes. |
| 5 | Android agenda, filters, bookmarks, speakers and venue behave as in v1.0; `SessionFiltersService` gone and Koin binding replaced in the same commit | VERIFIED for logic; visual parity of Speakers needs a human re-check | Agenda, filters, bookmarks and venue: no source change since the 2026-10-29 parity approval. `SessionFiltersService` has zero occurrences outside `.planning`. Speakers logic after WR-01/WR-04: for ASCII names `speakerLetterKey` (`name.lowercase().firstOrNull()`) equals the v1.0 key (`name.lowercase().first()`), the first index per key still wins, and the header check in `SpeakersList` now uses the same function. `SpeakerIndexCharacterizationTest` (golden over the recorded data, unchanged) plus `SpeakerIndexCaseFoldingTest` and `SpeakerIndexEmptyNameTest` pass: I ran `:feature:speakers:testDebugUnitTest`, 6 suites, 20 tests, 0 failures. The recorded snapshot has 100 speakers, all with an ASCII initial, so the recorded data produces the same map as v1.0. What no automated check covers is the Compose rendering after the `letterIndexes` StateFlow change; the parity screenshots predate it (see Human Verification). |

**Score:** 5/5 truths verified (0 present, behavior-unverified). The Speakers visual re-check is carried as a human verification item, not as a failed truth, because the logic is proven equal on the recorded data and the 15-capture parity approval still covers every other screen.

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `core/domain/` module (use cases, `DomainModule.kt`) | Shared agenda, speaker, venue logic | VERIFIED | Substantive, wired from `InitKoin.kt`, `shared`, Android features; unchanged by the fixes |
| `core/domain/src/commonTest` and `testdata/agenda/golden` | Characterization and use-case tests | VERIFIED | Unchanged; no golden edited by WR-01..WR-04 (confirmed from git history and 07-REVIEW-FIX.md) |
| `core/data` iOS stores (`UserDefaults*Store.kt`) | New-key stores, thread-safe writes | VERIFIED | `NSLock` guards update + persist in both stores; tests in `iosTest` |
| `feature/speakers` `SpeakersViewModel.kt`, `Speakers.kt` | Shared index consumed, one key function | VERIFIED | `speakerLetterKey` used by map and header; `letterIndexes` published once per load; `Speakers` collects it |
| `scripts/record-agenda-snapshot.sh` | Recorder fails on any Gradle error | VERIFIED | `set -euo pipefail`, no "No tests found" tolerance, step 2 runs the single named rewrite test |
| `07-parity/` screenshots, R8 evidence | Android parity (D-12) | VERIFIED but stale for Speakers | `main`, `branch` (15 captures) and `release` present; Speakers capture predates WR-01/WR-04 |

### Key Link Verification

| From | To | Via | Status |
|------|----|-----|--------|
| `shared` | `:core:domain` | `api` + framework `export` | WIRED |
| `InitKoin.sharedModules` | `domainModule` | list membership, `KoinGraphTest` | WIRED |
| `AgendaViewModel` | `ObserveAgendaUseCase` | `AgendaModule` DI | WIRED |
| `SessionFiltersStore` | Android `SharedPreferencesSessionFiltersStore` | `DataPlatformModule.android.kt` | WIRED |
| `SpeakersViewModel.letterIndexes` | `Speakers` composable -> `SpeakersListWithScroller` / `SpeakersList` | `collectAsState()`, `firstLetterIndexes` parameter | WIRED |
| `speakerLetterKey` | index map and row header check | single internal function | WIRED |

### Data-Flow Trace (Level 4)

| Artifact | Data | Source | Real data | Status |
|----------|------|--------|-----------|--------|
| `AgendaViewModel.days` | `DaySchedule` list | `ObserveAgendaUseCase` over store sessions, filters, bookmarks | Yes | FLOWING |
| `SpeakersViewModel.letterIndexes` | letter -> index map | `store.speakers` collector -> `firstLetterIndexes` -> `SpeakerIndexUseCase` | Yes (`speakers` and `letterIndexes` are set back to back in the same collector) | FLOWING |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| Swift names gate | `bash scripts/swift-names-gate.sh check` | SWIFT-NAMES-OK | PASS |
| DI gate | `bash scripts/di-gate.sh` | DI-GATE-OK | PASS |
| iOS filter store incl. new concurrency test | `./gradlew :core:data:iosSimulatorArm64Test --tests '*UserDefaultsSessionFiltersStoreTest*'` | 8 tests, 0 failures (includes `concurrent_writers_persist_the_latest_state`) | PASS |
| Speakers unit tests (WR-01/WR-04 + characterization) | `./gradlew :feature:speakers:testDebugUnitTest` | 6 suites, 20 tests, 0 failures | PASS |
| Full suite on ae3da148 | detekt, lint, di-gate, kmp-nc gate, `testDebugUnitTest jvmTest testAndroidHostTest`, five `iosSimulatorArm64Test` tasks, swift-names gate | passed (supplied by the caller, not re-run in full here) | PASS (orchestrator evidence) |

### Probe Execution

SKIPPED: no probes declared by the plans (`scripts/*/tests/probe-*.sh` not present).

### Requirements Coverage

All seven IDs are claimed by PLAN frontmatter; none orphaned. `.planning/REQUIREMENTS.md` now marks SHARED-01..07 as `[x]` and "Complete" in the traceability table, which closes the earlier bookkeeping note.

| Requirement | Source Plans | Status | Evidence |
|-------------|--------------|--------|----------|
| SHARED-01 | 07-01, 07-02, 07-13 | SATISFIED | Truth 1 |
| SHARED-02 | 07-06, 07-09, 07-10, 07-12, 07-15, 07-19 | SATISFIED | Truths 2, 4 |
| SHARED-03 | 07-05, 07-08, 07-12, 07-13, 07-15 | SATISFIED | Truths 2, 3 |
| SHARED-04 | 07-01, 07-06, 07-08, 07-13, 07-17, 07-18 | SATISFIED | Truths 2, 3 (speaker index and venue language shared; Speakers parity re-check pending, see below) |
| SHARED-05 | 07-09, 07-11, 07-14, 07-16, 07-19 | SATISFIED | Truth 4 |
| SHARED-06 | 07-14 to 07-19 | SATISFIED | Truth 5 |
| SHARED-07 | 07-03 to 07-08, 07-12, 07-13 | SATISFIED | Truth 2 |

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| (phase-modified source) | - | `TBD` / `FIXME` / `XXX` | none found | - |
| `SpeakersViewModel.kt` | 68 | unused destructured `letter` (IN-01) | Info | Compiler warning only (detekt and lint passed) |
| `SpeakersViewModel.kt` | 64-66 | comment overstates "cannot diverge"; `ı` before `i` case (IN-02) | Warning (advisory) | Latent; no such name in the 100 recorded speakers |
| `ClassifySessionUseCase.kt` | 22-29 | KEYNOTE star without detail screen | Info | Faithful to v1.0 on both platforms |

The remaining open items (IN-03, IN-04) are hygiene notes in `07-REVIEW-DISPOSITION.md`; none touches a success criterion. UI-review items that are v1.0 debt (no `UiState.ERROR` path, no Speakers empty state, scroller flush to the edge) are pre-existing and out of scope under the strict-parity rule ("bugs found along the way are logged as debt, not fixed").

### Human Verification Required

#### 1. Speakers screen visual parity on the current branch head

**Test:** Install the current head (>= 575970ae), open Speakers, compare with `07-parity/main/14-speakers-scroller.png`: row header letters, A-Z drag to several letters (including one with no speaker, for example "i"), one rotation.
**Expected:** Identical headers, identical scroll targets, no missing header or flicker after load or rotation.
**Why human:** `07-parity/branch/14-speakers-scroller.png` was captured on 4af25ed, before WR-01 and WR-04 reworked how the screen gets its letter map. Compose rendering has no automated coverage here. Logic equality on the recorded data is proven by tests; the pixel-level claim "no visible difference" for this one screen is not, and `07-UI-REVIEW.md` requires the re-capture before accepting it.

### Gaps Summary

No code gaps and no regressions. Every roadmap success criterion is backed by current code and passing gates. The status is `human_needed` solely because the "no visible difference" claim for the Speakers screen relies on screenshots that predate WR-01/WR-04. After that single re-capture is approved, the phase can be marked passed. Advisory only: triage IN-02 (`ı`/`i` header loss) before Phase 9, since fixing it means changing the shared `SpeakerIndexUseCase` letter definition that iOS also consumes.

---

_Verified: 2026-10-30_
_Verifier: Claude (gsd-verifier)_
