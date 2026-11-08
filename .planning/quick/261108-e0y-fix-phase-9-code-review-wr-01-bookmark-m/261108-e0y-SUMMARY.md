---
phase: quick-261108-e0y
plan: 01
subsystem: ios-agenda
tags: [ios, bookmarks, wr-01, code-review-fix]
status: complete
requirements: [IOSDATA-03]
key-files:
  modified:
    - iosApp/iosApp/Agenda/AgendaSource.swift
    - iosApp/iosApp/Agenda/AgendaViewModel.swift
    - iosApp/DevFestNantesTests/Fakes/FakeAgendaSource.swift
    - iosApp/DevFestNantesTests/KotlinContinuousFlowTests.swift
    - iosApp/DevFestNantesTests/AgendaViewModelTests.swift
    - documentation/ARCHITECTURE.md
    - .planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-REVIEW-DISPOSITION.md
decisions:
  - "While the bookmark mirror is unseeded, isBookmarked and toggleBookmark read the store through the new AgendaSource.isBookmarked(_:) snapshot (D-05/D-06 kept)"
actuals:
  tokens: 14000
  tasks: 3
  commits: 3
plan_head_before: 45164ee6d72879a533f9a2d454cbbdf7a1a5fe7d
plan_head_after: 40c2179fb68829d7ac6ed291c30ba5a311ce6e01
---

# Quick 261108-e0y: Fix Phase 9 review WR-01 (bookmark mirror fallback) Summary

While the iOS bookmark mirror is unseeded (failed first-emission read, or a session opened through Speakers before the first agenda load), the star and the toggle now read the shared store's per-session snapshot, so a stored favorite shows filled and un-bookmarks correctly with the right analytics value.

## What changed

- `AgendaSource.isBookmarked(_:)` added; `LiveAgendaSource` implements it over `bookmarksStore().isBookmarked(id:)` (lazy closure, still the only Kotlin caller).
- `AgendaViewModel.isBookmarked` always reads the mirror first (keeps Observation tracking), then answers from the store when `bookmarksSeeded` is false. `toggleBookmark` aligns the mirror entry with the store before the existing flip when unseeded, so observers re-render and the returned value is the true post-toggle value.
- `FakeAgendaSource` gained `storedBookmarks` (updated by `setBookmarked`) and `bookmarkReads`.
- `ARCHITECTURE.md`: store fallback described; the obsolete "iOS bookmark mirror seed window" deviation row removed.
- `09-REVIEW-DISPOSITION.md`: WR-01 `fixed`, `open: 5`.

## Evidence

RED (Task 1, before the ViewModel change): `KotlinContinuousFlowTests.agenda_view_model_reads_a_stored_bookmark_from_the_live_store_before_the_seed` failed on its first expectation (`Expectation failed: viewModel.isBookmarked(id)`, KotlinContinuousFlowTests.swift:63), plus the three dependent expectations (4 issues); the other 27 selected tests passed.

XCRESULT lines:
- Task 1 GREEN subset: `XCRESULT-TESTS total=28 passed=28 failed=0 skipped=0` / `XCRESULT-OK`
- Task 2 full plan: `XCRESULT-TESTS total=76 passed=76 failed=0 skipped=0` / `XCRESULT-OK`

Gates: `SWIFT-OBS-GATE-OK`, `SWIFT-NAMES-OK` (check only, no baseline change), `DI-GATE-OK`, `KMPNC-VERSION-OK 1.0.6`. `./gradlew detekt lint testDebugUnitTest jvmTest testAndroidHostTest`: BUILD SUCCESSFUL. `scripts/swift-names/` and the `.pbxproj` untouched.

Commits (local, on `worktree-agent-a20fe16878a6d4b5e`):
- 61bb6b28 fix(ios): read the bookmarks store until the agenda mirror is seeded
- b4245bf0 test(ios): cover the bookmark store fallback after a failed seed
- 40c2179f docs(09): mark WR-01 fixed in the review disposition

Nothing was pushed.

## Deviations from Plan

None in scope. Execution notes:
- The `<automated>` blocks were run as their component steps (separate plain commands), per the dispatch instructions; the same checks were applied.
- The per-plan commit ledger file under the git dir could not be written (command guard); the measured base is the known dispatch HEAD 45164ee6, and `commits: 3` comes from `git rev-list --count 45164ee6..HEAD`.
- The three new AgendaViewModelTests passed on first run as expected; no expectation was changed.

## Known Stubs

None.

## Threat Flags

None.

## Self-Check: PASSED

All three commits exist on HEAD; all seven listed files are modified; working tree clean; no xcodebuild left running.
