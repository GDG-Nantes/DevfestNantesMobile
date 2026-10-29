---
phase: 07-kmp-domain-layer-and-android-adoption
verified: 2026-10-29T23:00:00Z
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
  - "core/data/src/iosMain/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsSessionFiltersStore.kt"
  - "core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/di/DomainModule.kt"
  - "feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaViewModel.kt"
  - "shared/src/commonMain/kotlin/com/gdgnantes/devfest/shared/di/InitKoin.kt"
covered_digest: "v3:sha256:b31007554adcb1cc33902b7fd75030a186731e0998c9bec13a6f0fe7660d264f"
behavior_unverified: 0
overrides_applied: 0
advisory:
  - finding: "WR-01 (07-REVIEW): speaker A-Z index key (`letter.lowercase().first()` on an uppercased letter) and the row header check (`name.lowercase().firstOrNull()`) can diverge for characters whose uppercase form changes length or base letter (`ß`, `ﬁ`, `ı`). v1.0 used one lowercase pipeline on both sides. No such name exists in the recorded 100-speaker snapshot."
    category: other
    reason: "Edge-case parity deviation from v1.0, not reachable with the current data; recorded as open in 07-REVIEW-DISPOSITION.md. Triage before Phase 9 or the next edition."
    evidence_status: "code review reading, no failing test"
  - finding: "WR-02..WR-04 and IN-01..IN-04 (07-REVIEW): recorder script tolerance, iOS store last-writer-wins, composition-time non-fatal report, KEYNOTE star without detail screen (faithful to v1.0), stale comments, duplicate TestDataFiles, repeated day-label parse."
    category: other
    reason: "None blocks a Phase 7 success criterion; all recorded as open/advisory in 07-REVIEW-DISPOSITION.md."
    evidence_status: "code review reading"
---

# Phase 7: KMP Domain Layer and Android Adoption Verification Report

**Phase Goal:** The agenda business logic (filtering, grouping, classification, bookmarks and filters) lives in a KMP `:core:domain` module tested on JVM and iOS simulator, and Android already uses it without any visible difference
**Verified:** 2026-10-29
**Status:** passed
**Re-verification:** No, initial verification

## Goal Achievement

### Observable Truths (ROADMAP success criteria)

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | `:core:domain` exported by `:shared`, `domainModule` in `sharedModules`, `verify()` passes with no duplicate binding, `swift-names-gate.sh` passes, iOS compiles and behaves as before | VERIFIED | `core/domain/build.gradle.kts` applies `devfest.kmp.library`. `shared/build.gradle.kts` has `api(project(":core:domain"))` and `export(project(":core:domain"))`. `InitKoin.kt`: `sharedModules = listOf(networkModule, dataModule, analyticsModule, domainModule)` with `allowOverride(false)`. `KoinGraphTest` covers `verify()`, domain-module membership, use-case resolution and `DefinitionOverrideException` on a duplicate. Re-ran `bash scripts/swift-names-gate.sh check`: SWIFT-NAMES-OK; `bash scripts/di-gate.sh`: DI-GATE-OK. Orchestrator evidence: Swift tests 19/19 on simulator, jvmTest/iosSimulatorArm64Test green. |
| 2 | Characterization tests (filter matrix, grouping in two zones, recorded API snapshot, iOS and Android behaviors) committed before each logic move; shared use cases pass them in `commonTest` on JVM and iOS simulator | VERIFIED | Commit order from `git log`: snapshot 07-03 / 07-04, Android grouping 07-05 (`354cce68`), iOS 07-08 (`3c86433f`, `1b73bab9`), Android filtering/speaker/venue 07-06 (`47ecff97`, `21bacd2c`) all precede the shared filter (`dbc4d7e8`, 07-12), grouping (`6a5f5d74`), speaker/venue (`5b4bec44`) and the Android moves (07-15 to 07-18). Goldens exist in `core/domain/testdata/agenda/golden/` (android and ios agenda in `Europe_Paris` and `Pacific_Auckland`, filtering, speaker-index). `core/domain/src/commonTest` holds tests for Classify, Filter, Group, DayAssignment, ObserveAgenda, SpeakerIndex and VenueLanguage; `build.gradle.kts` wires testdata into jvmTest and the iOS simulator runner. Orchestrator evidence: 678 tests, 0 failures, iosSimulatorArm64Test included. |
| 3 | Divergent behaviors stay per platform or go through an explicit parameter; the iOS room-order deviation is documented | VERIFIED | `AgendaViewModel` passes `dayAssignment` (`UtcDayWindows`) and `TimeZone.currentSystemDefault()` explicitly to `observeAgenda.observe(...)`. The domain module has no scroll/partner-category logic. `documentation/ARCHITECTURE.md:306` documents the in-slot `RoomSortIndex` order row and the single deviation; `GroupSessionsUseCase` KDoc cites it. |
| 4 | `BookmarksStore` and `SessionFiltersStore` are common interfaces; v1.0 Robolectric tests pinning `selected_sessions` and the Android filter format pass; iOS stores use new `NSUserDefaults` keys with the shared codec and never read the legacy keys (simulator tests) | VERIFIED | Interfaces and `SessionFilterCodec` are in `core/data/commonMain`. `BookmarksStoreImplTest` is unchanged versus `main` (diff empty). The v1.0 `SessionFiltersServiceImplTest` (95 lines) was carried to `SharedPreferencesSessionFiltersStoreTest` (96 lines) with the same JSON-string and malformed-entry assertions (`{"type":"LANGUAGE","value":"FRENCH"}`). iOS: `UserDefaultsSessionFiltersStore` uses key `devfest.agenda.filters` and `SessionFilterCodec`; `iosTest` files write the legacy `Favorites` / `AgendaSessionFilters` keys and assert they are untouched. Orchestrator evidence: `testAndroidHostTest` and iOS simulator tests green. |
| 5 | Android agenda, filters, bookmarks, speakers and venue behave as in v1.0; `SessionFiltersService` gone and Koin binding replaced in the same commit | VERIFIED | Zero occurrences of `SessionFiltersService` in tracked source (grep over `kt/swift/md/kts` outside `.planning`; `git ls-tree` count 0). Commit `a28bf160` removes the service and adds the `SharedPreferencesSessionFiltersStore` binding in `DataPlatformModule.android.kt` together with the `AgendaModule` change. Main code uses the shared use cases: `AgendaViewModel` (`ObserveAgendaUseCase`, `ClassifySessionUseCase`, `SessionFiltersStore`), `SpeakersViewModel` (`SpeakerIndexUseCase`), `VenueViewModel`, `MainActivity`. Parity: 15 paired main/branch screenshots plus an R8 release persistence smoke, human approval recorded 2026-10-29 in `07-19-SUMMARY.md`. Orchestrator evidence: `connectedDebugAndroidTest` agenda 2/2, speakers 2/2, venue 1/1. |

**Score:** 5/5 truths verified (0 present, behavior-unverified)

Behavior-dependent invariants (filtering rule, grouping order, persistence format, store replacement) are each covered by a passing named test in the green full-gate run rather than by symbol presence alone.

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `core/domain/` module (7 use-case/model files, `DomainModule.kt`) | Shared agenda, speaker, venue logic | VERIFIED | Substantive, not stubs; wired from `InitKoin.kt`, `shared`, and Android features |
| `core/domain/src/commonTest` + `testdata/agenda/golden` | Characterization and use-case tests | VERIFIED | 7 commonTest classes, `DomainModuleTest` on jvmTest, 7 golden files |
| `core/model/.../SessionFilter.kt` | Shared filter type | VERIFIED | Present; baselines in `scripts/swift-names/` |
| `core/data` `BookmarksStore`, `SessionFiltersStore`, `SessionFilterCodec` | Common interfaces and codec | VERIFIED | Android and iOS actuals bound in the platform modules |
| iOS `UserDefaults*Store.kt` and `iosTest` | New-key stores | VERIFIED | Legacy keys asserted untouched |
| `iosApp/DevFestNantesTests/*Characterization*` | iOS characterization | VERIFIED | 4 Swift test files present |
| `07-parity/` screenshots, R8 evidence | Android parity (D-12) | VERIFIED | main, branch and release folders; human approval recorded |

### Key Link Verification

| From | To | Via | Status |
|------|----|-----|--------|
| `shared` | `:core:domain` | `api` + framework `export` | WIRED |
| `InitKoin.sharedModules` | `domainModule` | list membership, `KoinGraphTest` | WIRED |
| `AgendaViewModel` | `ObserveAgendaUseCase` | `AgendaModule` DI, constructor | WIRED |
| `SessionFiltersStore` | Android `SharedPreferencesSessionFiltersStore` | `DataPlatformModule.android.kt` single bind | WIRED |
| `SessionFiltersStore` / `BookmarksStore` | iOS `UserDefaults*Store` | `DataPlatformModule.ios.kt` | WIRED (app-side wiring is Phase 9) |

### Data-Flow Trace (Level 4)

| Artifact | Data | Source | Real data | Status |
|----------|------|--------|-----------|--------|
| `AgendaViewModel.days` | `DaySchedule` list | `ObserveAgendaUseCase` combining store sessions, filters and bookmarks | Yes | FLOWING |
| `SpeakersViewModel` index | speaker letters | `SpeakerIndexUseCase` over `store` speakers | Yes | FLOWING |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| Swift names gate | `bash scripts/swift-names-gate.sh check` | SWIFT-NAMES-OK | PASS |
| DI gate | `bash scripts/di-gate.sh` | DI-GATE-OK | PASS |
| Full suites | Orchestrator run on HEAD (not re-run): `testDebugUnitTest jvmTest testAndroidHostTest` + iosSimulatorArm64Test, 678 tests, 0 failures; Swift 19/19; connectedDebugAndroidTest 5/5; detekt, lint, kmp-nc gate exit 0 | as reported | PASS (orchestrator evidence) |

### Probe Execution

SKIPPED: no probes declared by the plans (`scripts/*/tests/probe-*.sh` not present).

### Requirements Coverage

All seven IDs are claimed by PLAN frontmatter and none is orphaned (ROADMAP and REQUIREMENTS map exactly SHARED-01..07 to Phase 7).

| Requirement | Source Plans | Status | Evidence |
|-------------|-------------|--------|----------|
| SHARED-01 | 07-01, 07-02, 07-13 | SATISFIED | Truth 1 |
| SHARED-02 | 07-06, 07-09, 07-10, 07-12, 07-15, 07-19 | SATISFIED | Truths 2, 4; `FilterSessionsUseCase` + matrix |
| SHARED-03 | 07-05, 07-08, 07-12, 07-13, 07-15 | SATISFIED | Truths 2, 3; `GroupSessionsUseCase`, `ARCHITECTURE.md:306` |
| SHARED-04 | 07-01, 07-06, 07-08, 07-13, 07-17, 07-18 | SATISFIED | Classify, speaker index, venue language shared; divergences parameterized (Truth 3) |
| SHARED-05 | 07-09, 07-11, 07-14, 07-16, 07-19 | SATISFIED | Truth 4 |
| SHARED-06 | 07-14 to 07-19 | SATISFIED | Truth 5 |
| SHARED-07 | 07-03 to 07-08, 07-12, 07-13 | SATISFIED | Truth 2, commit ordering |

**Bookkeeping gap (not a code gap):** `.planning/REQUIREMENTS.md` still shows SHARED-01, -03, -04 and -07 as unchecked `[ ]` and "Pending" in the traceability table (only -02, -05, -06 are marked Complete). The implementation satisfies them; the orchestrator should tick them during phase completion.

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| (phase-modified source) | - | `TBD` / `FIXME` / `XXX` without issue reference | none found | - |
| `SpeakersViewModel.kt` | ~61 | WR-01 case-folding mismatch for `ß`, `ﬁ`, `ı` | Warning (advisory) | Edge-case deviation, no such speaker in current data |
| `ClassifySessionUseCase.kt` | 22-29 | IN-01 KEYNOTE star without detail | Info | Faithful to v1.0 on both platforms |

Remaining review items (WR-02 to WR-04, IN-02 to IN-04) are tooling, concurrency and hygiene notes recorded in `07-REVIEW-DISPOSITION.md`; none touches a success criterion.

### Human Verification Required

None outstanding. The D-12 Android parity checkpoint was completed by the human on 2026-10-29 (07-19-SUMMARY.md).

### Gaps Summary

No gaps. All five roadmap success criteria are backed by code in the repository, passing gates, characterization-before-move commit ordering, and recorded human parity approval. Advisory only: triage WR-01 (speaker index case folding) because it is the one open review finding that could differ from v1.0 behavior on unusual names, and tick SHARED-01/03/04/07 in REQUIREMENTS.md.

---

_Verified: 2026-10-29_
_Verifier: Claude (gsd-verifier)_
