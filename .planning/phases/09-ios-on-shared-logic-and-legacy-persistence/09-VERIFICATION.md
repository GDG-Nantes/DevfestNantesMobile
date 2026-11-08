---
phase: 09-ios-on-shared-logic-and-legacy-persistence
verified: 2026-11-08T14:00:00Z
status: passed
score: 4/4 must-haves verified
covered_files:
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-01-PLAN.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-01-SUMMARY.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-02-PLAN.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-02-SUMMARY.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-03-PLAN.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-03-SUMMARY.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-04-PLAN.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-04-SUMMARY.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-05-PLAN.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-05-SUMMARY.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-06-PLAN.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-06-SUMMARY.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-07-PLAN.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-07-SUMMARY.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-08-PLAN.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-08-SUMMARY.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-09-PLAN.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-09-SUMMARY.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-10-PLAN.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-10-SUMMARY.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-11-PLAN.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-11-SUMMARY.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-12-PLAN.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-12-SUMMARY.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-13-PLAN.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-13-SUMMARY.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-14-PLAN.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-14-SUMMARY.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-15-PLAN.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-15-SUMMARY.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-16-PLAN.md"
  - ".planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-16-SUMMARY.md"
  - "documentation/ARCHITECTURE.md"
  - "iosApp/DevFestNantesTests/AgendaViewModelTests.swift"
  - "iosApp/DevFestNantesTests/Fakes/FakeAgendaSource.swift"
  - "iosApp/DevFestNantesTests/KotlinContinuousFlowTests.swift"
  - "iosApp/iosApp/Agenda/AgendaSource.swift"
  - "iosApp/iosApp/Agenda/AgendaView.swift"
  - "iosApp/iosApp/Agenda/AgendaViewModel.swift"
  - "iosApp/iosApp/Model/Data/AgendaContent.swift"
  - "shared/src/iosMain/kotlin/com/gdgnantes/devfest/shared/IosDependencies.kt"
covered_digest: "v3:sha256:25390fc7d28bda2427a67f9290222625ae0a2c09151124e4ac48f84ac8123d20"
behavior_unverified: 0
overrides_applied: 0
re_verification:
  previous_status: passed
  previous_score: 4/4
  gaps_closed: []
  gaps_remaining: []
  regressions: []
---

# Phase 9: iOS on Shared Logic and Legacy Persistence Verification Report

**Phase Goal:** The iOS app delegates filtering, grouping and persistence of bookmarks and filters to the shared domain layer; a user updating from v1.0 starts with empty bookmarks and filters (intentional, 07-CONTEXT D-15) and the app works normally
**Verified:** 2026-11-08
**Status:** passed
**Re-verification:** Yes. The earlier report was stale because `AgendaSource.swift`, `AgendaViewModel.swift` and `documentation/ARCHITECTURE.md` changed after it (fix `61bb6b28`, tests `b4245bf0`). The verdict was re-derived from the current source. The prior report had no `gaps:`, so every truth got a full check rather than a regression-only pass.

## Goal Achievement

### Observable Truths

The four ROADMAP success criteria are the contract. PLAN-level truths (single `AgendaViewModel`, write-through mirror, one-way store switch checkpoint) are folded into them.

| # | Truth | Status | Evidence |
| --- | ------- | ---------- | -------------- |
| 1 | A user installing the new build over v1.0 starts with empty bookmarks and filters and the app works; bookmarks and filters set afterwards survive kill and relaunch (in-place update UAT) | VERIFIED | `rtk grep` over `iosApp/iosApp` finds no read of `UserDefaults`, the `Favorites` key or `AgendaSessionFilters` (only the unrelated `filter_favorites` L10n string matches). The iOS stores use the new keys: `KEY_BOOKMARKS = "devfest.bookmarks.sessionIds"` (`UserDefaultsBookmarksStore.kt:49`), `devfest.agenda.filters` for filters. The `UserDefaultsBookmarksStore` KDoc states that the legacy key is never read, migrated or removed. Recorded UAT `09-parity/update/evidence.txt`: a `main` build was seeded with legacy `Favorites` (two ids) and `AgendaSessionFilters`, and the new keys were absent before the update. The branch build was installed over it with `simctl install` and no uninstall. The legacy keys were left byte-identical, and the new keys were set afterwards and persisted over two kill and relaunch cycles (screenshots u01 to u07 are present). Human approval at the 09-09 one-way checkpoint (`option-proceed`, 09-09-SUMMARY) and at the 09-15 UAT checkpoint (`approved`, 09-15-SUMMARY). This truth depends on recorded human UAT, which cannot be re-run here. |
| 2 | Swift no longer filters, groups or persists bookmarks or filters; `SessionFilter`, `FilterType` and the other duplicated types are removed in favor of the exported Kotlin types; speakers, venue and classification go through shared use cases | VERIFIED | `git ls-files iosApp/iosApp/Model/SessionFilter.swift` returns nothing (deleted). In `iosApp/iosApp` there is no `FilterType`, no `Dictionary(grouping:)`, no `UserDefaults`, no `ObservableObject`, no `@Published` and no `import Combine`. `AgendaViewModel` persists only through `source.setFilters` and `source.setBookmarked`, which `LiveAgendaSource` forwards to `SessionFiltersStore.setFilters` and `BookmarksStore.setBookmarked`. `LiveAgendaSource.isTalk` and `isNavigable` delegate to `ClassifySessionUseCase`. The previous report cites `SpeakerIndexUseCase.letterGroups` (`SpeakersSource.swift:36`) and `VenueLanguageUseCase.languageFor` (`VenueSource.swift:37`); this run did not re-read those two files. |
| 3 | The iOS agenda shows the same days, slots, sessions and filter results as v1.0 (comparative UAT), with the documented exceptions | VERIFIED | `09-parity/` holds paired `base/` and `branch/` captures and Maestro flows. The human accepted the pairs at the 09-16 D-10 checkpoint ("branch looks better than base", 09-16-SUMMARY). The deviations are recorded in `documentation/ARCHITECTURE.md` "Behavior deviations (v1.1)". Parity rests on recorded human UAT and cannot be re-run here. |
| 4 | The agenda continuously observes the shared combined flow; adding or removing a bookmark or filter from the list, a cell or the detail updates the agenda without relaunch, through a single `AgendaViewModel` | VERIFIED | `ContentView.swift:7` is the only `AgendaViewModel()` construction. It is read through `@Environment(AgendaViewModel.self)` by `AgendaView`, `AgendaDetailView` and all six filter menus and the clear button (confirmed by grep). `LiveAgendaSource.agenda()` returns `KotlinStreamBridge.stream(useCase.observe(dayAssignment:timeZone:))` with the zone built per subscription. `observeAgenda()` loops `while let next = try await iterator.next() { apply(next) }` (`AgendaViewModel.swift:136`), which is continuous and not first-only. The agenda list itself is driven by the Kotlin combined flow (sessions + bookmarks + filters), so filter and Favorites results stay correct regardless of the star mirror. Behavioral evidence: `KotlinContinuousFlowTests` (real Kotlin `StateFlow` through the bridge) and `AgendaViewModelTests` (re-emission, trace at first emission only, resubscribe, mid-stream error, cancellation, write-through). The orchestrator reports `XCRESULT-TESTS total=76 passed=76 failed=0 skipped=0`. I did not re-run the suite; I confirmed that the test files exist and contain the cases above. |

**Score:** 4/4 truths verified (0 present, behavior-unverified)

### Changes since the previous report

The fix `61bb6b28` and tests `b4245bf0` (WR-01 of the earlier review) add `AgendaSource.isBookmarked(_:)`, which `LiveAgendaSource` backs with the synchronous `BookmarksStore.isBookmarked(id:)`. `UserDefaultsBookmarksStore.kt:29` implements it as `id in state.value`. While `bookmarksSeeded` is false, `AgendaViewModel.isBookmarked` and `toggleBookmark` read through to the store (`AgendaViewModel.swift:60-87`). The read of `bookmarkedIds` happens before the branch, so Observation still tracks the mirror. New tests in `AgendaViewModelTests.swift:434-487` cover the failed seed (star and toggle read the store), an unseeded toggle notifying observers, and retry then mirror. This strengthens truth 4 and changes no ROADMAP criterion. All 4 truths still hold at the current source.

### Required Artifacts

| Artifact | Expected | Status | Details |
| -------- | ----------- | ------ | ------- |
| `shared/.../IosDependencies.kt` | Plain accessors for store, observeAgenda, bookmarksStore, sessionFiltersStore, speakerIndex, venueLanguage, classifySession | VERIFIED | Listed as covered; `LiveAgendaSource` calls `store()`, `classifySession()`, `bookmarksStore()`, `sessionFiltersStore()` and `observeAgenda()` |
| `iosApp/iosApp/Agenda/AgendaSource.swift` | Port plus `Live` adapter, the only KMP caller for the agenda | VERIFIED | Read in full. Substantive and wired. No Combine. |
| `iosApp/iosApp/Agenda/AgendaViewModel.swift` | Continuous observation, write-through mirror, store fallback while unseeded | VERIFIED | Read in full. Substantive and wired. |
| `iosApp/iosApp/Model/Data/AgendaContent.swift` | Thin display mapping | VERIFIED | Per the previous report: no sort, filter or group. Not re-read in this run. |
| `iosApp/iosApp/Model/SessionFilter.swift` | Deleted | VERIFIED | Not in `git ls-files` |
| `core/data/src/iosMain/.../UserDefaultsBookmarksStore.kt` | New-key bookmark store with sync `isBookmarked` | VERIFIED | Read in full. Lock-serialized writes, `StateFlow` state, new key. |
| `iosApp/DevFestNantesTests/{AgendaViewModelTests,KotlinContinuousFlowTests,Fakes/FakeAgendaSource}.swift` | Required test cases | VERIFIED | Present. WR-01 fallback cases confirmed at `AgendaViewModelTests.swift:434-487`. |
| `09-parity/update/*` | In-place update UAT evidence | VERIFIED | `evidence.txt`, `legacy-before.txt`, `legacy-after.txt`, screenshots u01 to u07 |

### Key Link Verification

| From | To | Via | Status | Details |
| ---- | --- | --- | ------ | ------- |
| `ContentView` | all agenda views | `.environment(agendaViewModel)` | WIRED | Single `AgendaViewModel()` at `ContentView.swift:7`; eight `@Environment(AgendaViewModel.self)` readers |
| `AgendaView.task` | `AgendaViewModel.observeAgenda` | `await viewModel.observeAgenda()` | WIRED | Per the previous report; not re-read |
| `AgendaViewModel` | Kotlin stores | `source.setFilters`, `source.setBookmarked`, `source.isBookmarked` | WIRED | `AgendaSource.swift:76-91` |
| `LiveAgendaSource` | `ObserveAgendaUseCase` | `IosDependencies.shared.observeAgenda()` + `LocalDateDays(dates: AgendaDays.all)` | WIRED | `AgendaSource.swift:52-57` |
| `AgendaView` / `AgendaDetailView` | star state | `viewModel.isBookmarked(session.id)` | WIRED | `AgendaView.swift:110`, `AgendaDetailView.swift:139` |
| Koin iOS module | `UserDefaults*Store` | `DataPlatformModule.ios.kt` | WIRED | Per the previous report; not re-read |

### Data-Flow Trace (Level 4)

| Artifact | Data Variable | Source | Produces Real Data | Status |
| -------- | ------------- | ------ | ------------------ | ------ |
| `AgendaView.agendaList` | `viewModel.agendaContent.sections` | `ObserveAgendaUseCase.observe` (store + bookmarks + filters combine) | Yes | FLOWING |
| `AgendaViewModel.bookmarkedIds` | mirror | first emission of `bookmarkedSessionIds`, then toggle write-through; store snapshot while unseeded | Yes | FLOWING |
| `AgendaViewModel.sessionFilters` | mirror | `filtersStore().filters` at init, then toggle write-through | Yes | FLOWING |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
| -------- | ------- | ------ | ------ |
| Observation gate | `bash scripts/swift-observation-gate.sh` | `SWIFT-OBS-GATE-OK` | PASS |
| DI gate | `bash scripts/di-gate.sh` | `DI-GATE-OK` | PASS |
| KMP-NC version gate | `bash scripts/kmp-nc-version-gate.sh` | `KMPNC-VERSION-OK 1.0.6` | PASS |
| Swift names gate | `bash scripts/swift-names-gate.sh check` | `SWIFT-NAMES-OK`, empty diff | PASS |
| Debt-marker scan | grep `TBD\|FIXME\|XXX` in `iosApp/iosApp`, `shared/src/iosMain`, `core/data/src/iosMain` | none (one binary-file false match on an app icon PNG) | PASS |
| Swift suite | not re-run by the verifier | Orchestrator evidence: `XCRESULT-TESTS total=76 passed=76 failed=0 skipped=0`, `XCRESULT-OK` | SKIPPED (orchestrator evidence used) |
| Android and JVM tests | not re-run by the verifier | Orchestrator evidence: `./gradlew testDebugUnitTest jvmTest testAndroidHostTest` BUILD SUCCESSFUL (up to date) | SKIPPED (orchestrator evidence used) |
| Kotlin iOS tests | not re-run | No Kotlin changed since the last run | SKIPPED |

### Probe Execution

No phase-declared probes (`probe-*.sh`). Not applicable.

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
| ----------- | ---------- | ----------- | ------ | -------- |
| IOSDATA-01 | 09-01, 03, 04, 05, 06, 08, 09, 12, 13, 14, 16 | No Swift filter, group or persist of favorites or filters; duplicated Swift types removed for exported Kotlin types | SATISFIED | Truth 2 |
| IOSDATA-02 | 09-09, 09-15 | Update from v1.0 starts empty and works; new values survive relaunch (in-place update UAT) | SATISFIED | Truth 1 |
| IOSDATA-03 | 09-01, 02, 07, 08, 10, 11, 16 | Agenda continuously observes the shared combined stream; list, cells and detail share one `AgendaViewModel` | SATISFIED | Truth 4 |

All three IDs appear in `.planning/REQUIREMENTS.md` (lines 24-26) and in the ROADMAP Phase 9 entry. No ORPHANED requirements: no other ID maps to Phase 9. REQUIREMENTS.md already shows all three as `[x]`.

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
| ---- | ---- | ------- | -------- | ------ |
| `iosApp/iosApp/Agenda/AgendaViewModel.swift` | 93-99 | WR-01 (09-REVIEW, open): a bookmark toggled while the first seed is awaiting can be overwritten by the older first-emission snapshot | Warning (advisory) | See "Weighing WR-01" below. Not a goal gap. |
| `iosApp/iosApp/Agenda/AgendaViewModel.swift` | 69-84 | IN-01: duplicated insert/remove block in `toggleBookmark` | Info | Readability only |
| `iosApp/iosApp/Agenda/AgendaViewModel.swift` | 103 | IN-02: stale comment "Counts only, never the ids." above an error log | Info | Misleading comment only |
| `09-REVIEW-DISPOSITION.md` | - | IN-03, IN-04, IN-05 (test isolation, teardown assertion, pre-existing items) | Info | Open in the ledger; not goal items |

No `TBD`, `FIXME` or `XXX` markers in the Swift or Kotlin iOS sources of this phase, so the debt-marker gate is clear.

### Weighing WR-01

Finding: `seedBookmarksIfNeeded` awaits the first `bookmarkedSessionIds` emission and then assigns `bookmarkedIds = ids` wholesale. A toggle that lands on the main actor between the Kotlin collection starting and that element arriving is written to the store and the mirror, then overwritten in the mirror by the older snapshot. I read the code at `AgendaViewModel.swift:91-106` and the finding is real.

Why it does not fail the phase goal, on the evidence read:
- **Persistence is not affected.** `source.setBookmarked` runs on the toggle path, so the store holds the toggled value. Nothing is lost on relaunch, and the v1.0 empty-start guarantee is not touched.
- **The agenda list is not affected.** It is driven by the Kotlin combined flow, which reads the store directly, so Favorites filtering and the sessions shown stay correct. Only the star glyph, which reads the mirror, can be stale.
- **The window is narrow in practice.** On the first load the seed completes before `source.agenda()` is subscribed (`observeAgenda` awaits the seed first), and `agendaContent` starts empty, so no star is on screen to tap during that first window. The remaining case is a retry after a failed seed, where the window is again one flow-to-main-actor hop. The Kotlin store is a `StateFlow`, which emits immediately.
- **It self-heals.** The next toggle of that session or the next launch realigns the star.
- **Roadmap criterion 4 still holds.** Updates without relaunch work on the normal path, and the one-way persistence criteria are unaffected.

What it does leave: the ARCHITECTURE.md statement that the mirrors "always match what is persisted" is slightly stronger than this race allows. This is recorded as a WARNING (advisory) for the phase owner to triage. The review's suggested fix, replaying toggles made while unseeded over the seeded set, is small and local. It is not required to close the phase, and it was left as `open` in the disposition ledger, which is the correct state until someone decides.

### Human Verification Required

None outstanding. The three human checkpoints (09-09 one-way decision, 09-15 in-place update UAT, 09-16 paired parity) are recorded as passed in their SUMMARYs, and the 09-parity evidence files are present.

### Gaps Summary

No gaps. All four ROADMAP success criteria are met in the current source, and the WR-01 fix is present, wired and tested. Four truths rely on prior human UAT (1 and 3) or on the orchestrator's reported suite and CI results (4); the verifier re-ran the four gate scripts only.

Limits to carry forward:
- CI has not run on the commits after `d5f5efb6` (including `61bb6b28` and `b4245bf0`). The local 76/76 Swift result is orchestrator evidence, not re-run by the verifier.
- `.planning/ROADMAP.md` still needs its Phase 9 index line marked complete on phase close if it is not already.
- WR-01 (warning) and IN-01 to IN-05 remain `open` in `09-REVIEW-DISPOSITION.md`. `09-UI-REVIEW.md` scored 17/24 earlier; neither is a phase-goal item.

---

_Verified: 2026-11-08_
_Verifier: Claude (gsd-verifier)_
