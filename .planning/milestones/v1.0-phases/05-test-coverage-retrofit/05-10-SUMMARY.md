---
phase: 05-test-coverage-retrofit
plan: 10
subsystem: testing
tags: [compose-ui-test, koin-test, androidTest, smoke-tests, kover, feedback-flags]

requires:
  - phase: 05-test-coverage-retrofit
    provides: "05-09 smoke-test pattern and feature androidTest wiring, 05-08 FeedbackFlags seam (D-23), 05-01 Kover baseline and kover-summary.sh, 05-02/05-03 fakes and fixtures"
provides:
  - "AgendaSmokeTest: render + session click + english filter from the drawer, independent of the wall clock"
  - "SessionDetailSmokeTest: render + speakers + bookmark FAB on the real sessionDetailModule with FeedbackFlags faked (no Firebase surface)"
  - "kover/final-report.xml, final-summary.txt and comparison.txt: the committed before/after evidence (D-18), KOVER-IMPROVED"
  - "deferred-items.md reviewed and closed with a count (16 items)"
affects: [phase-05-verification]

actuals:
  tokens: 6000
  tasks: 3
  commits: 3

plan_head_before: b15a64695aab2cc9503a62474adb3377971a4ece
plan_head_after: 034cfe938169b8712a8d3e74ab5b1325d38bb311

tech-stack:
  added: []
  patterns:
    - "Wall-clock-independent list assertions: select the first day tab, then performScrollToNode on the vertical list (hasScrollToNodeAction + VerticalScrollAxisRange), never on the first row"
    - "Private androidTest fake of a production seam (SmokeFeedbackFlags) bound last under allowOverride(true), so lazy Firebase-backed factories are never resolved"

key-files:
  created:
    - feature/agenda/src/androidTest/java/com/gdgnantes/devfest/feature/agenda/AgendaSmokeTest.kt
    - feature/session-detail/src/androidTest/java/com/gdgnantes/devfest/feature/sessiondetail/SessionDetailSmokeTest.kt
    - .planning/phases/05-test-coverage-retrofit/kover/final-report.xml
    - .planning/phases/05-test-coverage-retrofit/kover/final-summary.txt
    - .planning/phases/05-test-coverage-retrofit/kover/comparison.txt
  modified:
    - .planning/phases/05-test-coverage-retrofit/deferred-items.md

key-decisions:
  - "AgendaSmokeTest selects the first day tab (first selectable node) before asserting, so neither the pager's start page nor the auto-scroll position matters; the filter test first scrolls to the French session to prove it is present, then asserts it is gone after the english filter"
  - "SessionDetailSmokeTest matches speakers with hasText(name, substring = true) because the speaker line is the full name followed by the company"
  - "Item 4 of deferred-items.md (double-slash social links) stays unpinned: adding a core:model test would have changed the already-recorded final report, so it is documented by code reading only and says so"

patterns-established:
  - "Smoke tests that need SharedPreferences supply a named test preferences file cleared in @Before"

requirements-completed: [TEST-04]

coverage:
  - id: D1
    description: "Agenda smoke test: fixture sessions render regardless of wall clock, a click reports the session, and the english filter from the drawer is stored in the real SessionFiltersServiceImpl and removes French sessions"
    requirement: TEST-04
    verification:
      - kind: automated_ui
        ref: "AgendaSmokeTest (2 tests); AGENDA-SMOKE-GREEN; CI android.yml run 38888333772"
        status: pass
    human_judgment: false
  - id: D2
    description: "Session detail smoke test: title and speakers render through the real module, FeedbackFlags override took effect with exactly one fetch on the fake, bookmark FAB toggles the store; no Firebase type in the test"
    requirement: TEST-04
    verification:
      - kind: automated_ui
        ref: "SessionDetailSmokeTest (2 tests); ALL-SMOKES-GREEN; SMOKE-FLAGS-FAKED-OK; CI android.yml run 38888333772"
        status: pass
    human_judgment: false
  - id: D3
    description: "Final Kover report measured with the unchanged root koverXmlReport config is committed; kover-summary.sh compare prints KOVER-IMPROVED and :core:data, :core:model and :feature:agenda line coverage are strictly above baseline"
    requirement: TEST-04
    verification:
      - kind: other
        ref: "COVERAGE-MEASURABLY-IMPROVED (comparison regenerated and byte-compared with the committed file)"
        status: pass
    human_judgment: false
  - id: D4
    description: "Full CI-equivalent suite and both workflows are green on the final code HEAD; the instrumentation job ran every feature module and androidApp"
    requirement: TEST-04
    verification:
      - kind: other
        ref: "detekt lint testDebugUnitTest jvmTest testAndroidHostTest + DI-GATE-OK; android.yml 38888333772 and iOS CI 38888333762 green on 034cfe9"
        status: pass
    human_judgment: false

duration: 1h 30m
completed: 2026-10-16
status: complete
---

# Phase 5 Plan 10: Agenda and Session Detail Smoke Tests, Final Kover Comparison Summary

**All six feature modules now have a Compose smoke test (agenda on a wall-clock-independent strategy, session detail on a private `FeedbackFlags` fake with no Firebase surface), and the committed Kover comparison shows total line coverage going from 15.96% to 30.57% (KOVER-IMPROVED).**

## Performance

- **Duration:** about 1h 30m
- **Completed:** 2026-10-16
- **Tasks:** 3 (all auto)
- **Files:** 6 (5 created, 1 modified)

## Accomplishments

- `AgendaSmokeTest`: `agenda_renders_fixture_sessions_and_click_reports_the_session` scrolls to "Git en 15 minutes" and checks the click reports `SessionFixtures.gitQuickie`. `english_filter_from_the_drawer_is_stored_and_hides_french_sessions` opens the drawer, confirms "Les secrets du rendu web" is reachable, clicks `english`, and asserts the real `SessionFiltersService` holds exactly `(LANGUAGE, ENGLISH)`, "Compose deep dive" is reachable and the French session is gone.
- `SessionDetailSmokeTest`: the fixture session title and both speakers render through the real `sessionDetailModule` (ViewModels via `koinViewModel()`); `assertSame(flags, koin.get<FeedbackFlags>())` proves the override took effect and `fetchCount == 1` proves the feedback form ViewModel ran its single init fetch on the fake; the bookmark FAB toggles the store true then false. `OpenFeedbackConfig(enabled = false, ...)` keeps the OpenFeedback form out of the tree.
- The final Kover report, comparison and summary are committed next to the baseline. The measurement config was verified unchanged since the baseline commit (no diff in `build.gradle.kts` or the Android library/application convention plugins; later build changes only add test dependencies and `withHostTest`).
- `deferred-items.md` reviewed: 16 items, no ignored test exists in the repo, closing count added.

## Kover comparison (D-18, evidence of improvement only, not a target or a gate)

```
TOTAL LINE 15.96% -> 30.57%
MODULE :androidApp LINE 17.25% -> 17.57%
MODULE :core:analytics LINE 12.99% -> 18.83%
MODULE :core:data LINE 26.25% -> 95.74%
MODULE :core:model LINE 81.70% -> 95.31%
MODULE :core:network LINE 68.97% -> 96.55%
MODULE :core:ui LINE 3.08% -> 8.01%
MODULE :feature:about LINE 8.42% -> 8.42%
MODULE :feature:agenda LINE 9.74% -> 18.54%
MODULE :feature:session-detail LINE 11.98% -> 19.67%
MODULE :feature:settings LINE 6.86% -> 16.89%
MODULE :feature:speakers LINE 11.14% -> 12.13%
MODULE :feature:venue LINE 3.82% -> 11.46%
MODULE :shared LINE 100.00% -> 100.00%
KOVER-IMPROVED
```

Note: the Kover report covers JVM and Android host tests (as the baseline did); the instrumented smoke tests are not part of it, which is why `:feature:about` is unchanged.

## Task Commits

1. **Task 1: agenda smoke test (filter + click)** - `00b620d`
2. **Task 2: session detail smoke test (render + bookmark)** - `29f7730`
3. **Task 3: final Kover report and before/after comparison, deferred list review** - `034cfe9`

Plan metadata (SUMMARY, STATE, ROADMAP, REQUIREMENTS) is committed separately after these.

## CI evidence

- Final code HEAD `034cfe9`: android.yml run 38888333772 green (Build debug, Unit tests, Checks Linters, Instrumentation tests (34) all success), iOS CI run 38888333762 green.
- The Android log contains `Task :<module>:connectedDebugAndroidTest` for `:androidApp`, `:feature:venue`, `:feature:about`, `:feature:settings`, `:feature:speakers`, `:feature:agenda` and `:feature:session-detail`, plus `Task :core:data:testAndroidHostTest`.
- **D8 OOM status: absent.** The CI log has zero `OutOfMemoryError` lines, and the full local `connectedDebugAndroidTest detekt` run (all modules, `-Xmx4096M`) also completed without it. No build configuration was touched.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug in my test] Speaker lookup by exact name failed**
- **Found during:** Task 2, first emulator run
- **Issue:** `onAllNodesWithText("Alice Moreau")` found nothing: `SessionSpeaker` renders `getFullNameAndCompany()`, not the bare name.
- **Fix:** `hasText(speaker.name, substring = true)` with a comment.
- **Files modified:** `SessionDetailSmokeTest.kt`
- **Commit:** `29f7730` (fixed before the commit)

**2. [Rule 3 - Blocking] Missing `BookmarksStore` import in the session detail test**
- **Found during:** Task 2, first compile. A `sed` insert using `\n` is not portable on macOS and did nothing; fixed with an Edit.
- **Commit:** `29f7730`

**Total deviations:** 2 (both inside test code, no production file touched). **Impact:** none on scope.

## Issues Encountered

- The plan's shell snippets use `"$T:connectedDebugAndroidTest"`. In zsh `$T:c` is a parameter modifier, so those lines only work under bash (as in CI). The same checks were run with `${T}` and passed.
- `deferred-items.md` item 4 (double-slash social links) has no characterization test; the entry now says so explicitly rather than claiming a pin.

## Authentication Gates

None.

## Known Stubs

None.

## Threat Flags

None new. T-5-20 mitigated: measurement config diffed against the baseline commit, the comparison was regenerated and byte-compared with the committed file. T-5-21 mitigated: `FeedbackFlags` is overridden by a private fake under `allowOverride(true)`, the test file has no Firebase reference (SMOKE-FLAGS-FAKED-OK) and `KoinStartupTest` still asserts `FirebaseFeedbackFlags` for the production wiring. T-5-SC: no dependency added.

## Next Phase Readiness

Phase 5 plans are all executed. Ready for phase verification (`/gsd-verify-work`), which can carry the comparison above into VERIFICATION.

## Self-Check: PASSED

All created files exist on disk; commits 00b620d, 29f7730 and 034cfe9 are on the branch (3 measured from the plan ledger, base b15a646); AGENDA-SMOKE-GREEN, ALL-SMOKES-GREEN, SMOKE-FLAGS-FAKED-OK, COVERAGE-MEASURABLY-IMPROVED, DI-GATE-OK, CI-GREEN-BOTH and CI-RUNS-ALL-SMOKES observed.
