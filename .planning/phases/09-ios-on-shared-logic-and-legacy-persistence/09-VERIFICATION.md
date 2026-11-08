---
phase: 09-ios-on-shared-logic-and-legacy-persistence
verified: 2026-11-08T12:00:00Z
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
  - "iosApp/iosApp/Agenda/AgendaSource.swift"
  - "iosApp/iosApp/Agenda/AgendaView.swift"
  - "iosApp/iosApp/Agenda/AgendaViewModel.swift"
  - "iosApp/iosApp/Model/Data/AgendaContent.swift"
  - "shared/src/iosMain/kotlin/com/gdgnantes/devfest/shared/IosDependencies.kt"
covered_digest: "v3:sha256:1b40a9da030e78eb4720e7ec3bd009a49766fd92a9d9b99d936e54675382d0fe"
behavior_unverified: 0
overrides_applied: 0
---

# Phase 9: iOS on Shared Logic and Legacy Persistence Verification Report

**Phase Goal:** The iOS app delegates filtering, grouping and persistence of bookmarks and filters to the shared domain layer; a user updating from v1.0 starts with empty bookmarks and filters (intentional, 07-CONTEXT D-15) and the app works normally
**Verified:** 2026-11-08
**Status:** passed
**Re-verification:** No, initial verification

## Goal Achievement

### Observable Truths

The four ROADMAP success criteria are the contract. The plan-level truths (single `AgendaViewModel`, write-through mirror, one-way store switch checkpoint) are folded into them.

| # | Truth | Status | Evidence |
| --- | ------- | ---------- | -------------- |
| 1 | A user installing the new build over v1.0 starts with empty bookmarks and filters and the app works; bookmarks and filters set afterwards survive kill and relaunch (in-place update UAT) | VERIFIED | `iosApp/iosApp/` has no read of the legacy `Favorites` or `AgendaSessionFilters` keys (grep: only the unrelated `L10n.filterFavorites` string matches). The iOS stores use the new keys `devfest.bookmarks.sessionIds` and `devfest.agenda.filters` (`UserDefaultsBookmarksStore.kt:49`, `UserDefaultsSessionFiltersStore.kt:45`) and are bound in `DataPlatformModule.ios.kt:16-17`. Recorded UAT `09-parity/update/evidence.txt`: a `main` build seeded legacy keys, `simctl install` of the branch build left them byte-identical (`cmp` identical) and unread, the new keys were absent at start and then set, and they persisted over two kill and relaunch cycles. Human approval at the 09-09 one-way checkpoint (`option-proceed`) and at the 09-15 UAT checkpoint (`approved`). |
| 2 | Swift no longer filters, groups or persists bookmarks or filters; `SessionFilter`, `FilterType` and the other duplicated types are removed in favor of the exported Kotlin types; speakers, venue and classification go through shared use cases | VERIFIED | `iosApp/iosApp/Model/SessionFilter.swift` is not tracked. No `FilterType`, no `UserDefaults`, no `Dictionary(grouping:)`, no `ObservableObject` or Combine in `iosApp/iosApp`. `SessionFilter` and `SessionFilterType` in Swift are the Kotlin types (`core/model/.../SessionFilter.kt`). `AgendaContent.init(days:isTalk:isNavigable:)` is a display mapping only; classification delegates to `ClassifySessionUseCase` (`AgendaSource.swift:62-68`), speakers to `SpeakerIndexUseCase.letterGroups` (`SpeakersSource.swift:36`), venue to `VenueLanguageUseCase.languageFor` (`VenueSource.swift:37`). The only remaining `.filter`/`.sorted` in the agenda are `AgendaView.filteredSections` (selects the shared section for the picked day by equality) and `scrollTarget` (D-11 pure scroll-to-now function, test-covered); neither is the filtering, grouping or persistence the criterion targets. |
| 3 | The iOS agenda shows the same days, slots, sessions and filter results as v1.0 (comparative UAT), with the documented exceptions | VERIFIED | `09-parity/` holds paired base and branch captures (69 PNGs tracked) and Maestro flows. Human accepted the pairs at the 09-16 D-10 checkpoint ("branch looks better than base"). Every deviation is recorded in `documentation/ARCHITECTURE.md` "Behavior deviations (v1.1)": room order (`RoomSortIndex`, line 306), scroll target with filters (317), non-Gregorian calendar day labels (318), D-05 seed window (319), fractional-second dates (310), and the 09-16 layout offsets (320). Parity cannot be re-run here; this rests on recorded human UAT. |
| 4 | The agenda continuously observes the shared combined flow; adding or removing a bookmark or filter from the list, a cell or the detail updates the agenda without relaunch, through a single `AgendaViewModel` | VERIFIED | `ContentView.swift:7` is the only `AgendaViewModel()` construction; it is injected with `.environment(agendaViewModel)` (line 43) and read via `@Environment` by `AgendaView`, `AgendaDetailView` and all five filter menus. `LiveAgendaSource.agenda()` returns `KotlinStreamBridge.stream(observeAgenda().observe(dayAssignment: LocalDateDays, timeZone: currentSystemDefault))`. `observeAgenda()` loops `while let next = try await iterator.next() { apply(next) }` (continuous, not first-only). Behavioral evidence: `KotlinContinuousFlowTests` proves a real Kotlin `StateFlow` delivers a second element through the bridge after a write; `AgendaViewModelTests` covers re-emission replacing content, trace at first emission only, resubscribe keeping content, mid-stream error keeping content, cancellation silence, mirror correct while collection is cancelled, write-through for bookmark and filters. Orchestrator reports 72 of 72 Swift tests passed and CI green at head `d5f5efb6`; I confirmed `gh pr checks 433` shows 5 passed, 0 failed, head `d5f5efb6`. |

**Score:** 4/4 truths verified (0 present, behavior-unverified)

### Required Artifacts

| Artifact | Expected | Status | Details |
| -------- | ----------- | ------ | ------- |
| `shared/.../IosDependencies.kt` | Plain accessors for store, observeAgenda, bookmarksStore, sessionFiltersStore, speakerIndex, venueLanguage, classifySession | VERIFIED | All seven present (lines 40-58), resolved from Koin, no Koin type in the Swift API |
| `iosApp/iosApp/Agenda/AgendaSource.swift` | Port plus `Live` adapter | VERIFIED | Wired to shared use cases and stores; the only Agenda caller of `IosDependencies` |
| `iosApp/iosApp/Agenda/AgendaViewModel.swift` | Continuous observation, write-through mirror | VERIFIED | Substantive and wired (see truth 4) |
| `iosApp/iosApp/Model/Data/AgendaContent.swift` | Thin mapping | VERIFIED | No sort, filter or group |
| `iosApp/iosApp/Model/SessionFilter.swift` | Deleted | VERIFIED | Absent from `git ls-files` |
| `iosApp/DevFestNantesTests/{AgendaViewModelTests,AgendaScrollTargetTests,KotlinContinuousFlowTests,...}` | Test list from CONTEXT | VERIFIED | Present with the required cases |
| `09-parity/update/*` | In-place update UAT evidence | VERIFIED | `evidence.txt`, before and after legacy dumps, screenshots u01 to u07 |

### Key Link Verification

| From | To | Via | Status | Details |
| ---- | --- | --- | ------ | ------- |
| `ContentView` | all agenda views | `.environment(agendaViewModel)` | WIRED | Single instance |
| `AgendaView.task` | `AgendaViewModel.observeAgenda` | `await viewModel.observeAgenda()` | WIRED | D-01, cancelled on disappear |
| `AgendaViewModel` | Kotlin stores | `source.setFilters`, `source.setBookmarked` | WIRED | Persistence only in Kotlin |
| `LiveAgendaSource` | `ObserveAgendaUseCase` | `IosDependencies.shared.observeAgenda()` + `LocalDateDays(AgendaDays.all)` | WIRED | Zone built per subscription |
| Koin iOS module | `UserDefaults*Store` | `DataPlatformModule.ios.kt:16-17` | WIRED | Singletons on `standardUserDefaults` |
| Speakers/Venue/Details sources | shared use cases | `IosDependencies.shared.*` | WIRED | |

### Data-Flow Trace (Level 4)

| Artifact | Data Variable | Source | Produces Real Data | Status |
| -------- | ------------- | ------ | ------------------ | ------ |
| `AgendaView.agendaList` | `viewModel.agendaContent.sections` | `ObserveAgendaUseCase.observe` (store + bookmarks + filters combine) | Yes | FLOWING |
| `AgendaViewModel.bookmarkedIds` | mirror | first emission of `bookmarkedSessionIds`, then tap write-through | Yes | FLOWING |
| `AgendaViewModel.sessionFilters` | mirror | `filtersStore().filters` at init, then tap write-through | Yes | FLOWING |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
| -------- | ------- | ------ | ------ |
| Observation gate | `bash scripts/swift-observation-gate.sh` | `SWIFT-OBS-GATE-OK` | PASS |
| DI gate | `bash scripts/di-gate.sh` | `DI-GATE-OK` | PASS |
| KMP-NC version gate | `bash scripts/kmp-nc-version-gate.sh` | `KMPNC-VERSION-OK 1.0.6` | PASS |
| Swift names gate | `bash scripts/swift-names-gate.sh check` | `SWIFT-NAMES-OK`, empty diff | PASS |
| Swift suite, CI | `gh pr checks 433` | 5 passed, 0 failed, head `d5f5efb6` | PASS |
| Swift suite, local | not run | Relied on CI and orchestrator `XCRESULT-TESTS total=72 passed=72` | SKIPPED (CI evidence used) |

### Probe Execution

No phase-declared probes (`probe-*.sh`). Not applicable.

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
| ----------- | ---------- | ----------- | ------ | -------- |
| IOSDATA-01 | 09-01, 03, 04, 05, 06, 08, 09, 12, 13, 14, 16 | No Swift filter, group or persist of favorites or filters; duplicated Swift types removed for exported Kotlin types | SATISFIED | Truth 2 |
| IOSDATA-02 | 09-09, 09-15 | Update from v1.0 starts empty and works; new values survive relaunch (in-place update UAT) | SATISFIED | Truth 1 |
| IOSDATA-03 | 09-01, 02, 07, 08, 10, 11, 16 | Agenda continuously observes the shared combined stream; list, cells and detail share one `AgendaViewModel` | SATISFIED | Truth 4 |

All three IDs appear in `.planning/REQUIREMENTS.md`, mapped to Phase 9. No ORPHANED requirements: no other ID in REQUIREMENTS.md maps to Phase 9.

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
| ---- | ---- | ------- | -------- | ------ |
| none | - | No `TBD`, `FIXME` or `XXX` in iOS Swift, `shared` iosMain or `core/data` iosMain | - | - |
| `iosApp/iosApp/Agenda/AgendaViewModel.swift` | 77-92 | WR-01 (from 09-REVIEW): a failed first bookmark seed is logged and leaves the mirror empty with no fallback read | Warning | Narrow edge: the store flow is a `StateFlow` that emits immediately, and the window is already an accepted deviation (D-05 seed window). Not a goal failure; left open in the review disposition |

### Human Verification Required

None outstanding. The three human checkpoints (09-09, 09-15, 09-16) were already passed and are recorded in the SUMMARYs.

### Gaps Summary

No gaps. All four ROADMAP success criteria are met in the code. The accepted deviations are documented, and the Swift suite, the gates and CI agree.

Bookkeeping for the orchestrator, none of it blocking:
- `.planning/REQUIREMENTS.md` still shows IOSDATA-01, IOSDATA-02 and IOSDATA-03 as `[ ]` and `Pending` (lines 24-26, 98-100). Mark them complete on phase close.
- The ROADMAP index line for Phase 9 (line 40) is still `[ ]`.
- Advisory: WR-01 plus 5 info items from 09-REVIEW remain open in the disposition ledger, and 09-UI-REVIEW scored 17/24. Neither is a phase-goal item.
- Limits: the local Swift suite was not re-run here, and visual parity rests on the recorded human UAT.

---

_Verified: 2026-11-08_
_Verifier: Claude (gsd-verifier)_
