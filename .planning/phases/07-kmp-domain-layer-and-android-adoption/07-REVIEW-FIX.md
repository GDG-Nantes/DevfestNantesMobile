---
phase: 07-kmp-domain-layer-and-android-adoption
fixed_at: 2026-10-30T14:00:00Z
review_path: .planning/phases/07-kmp-domain-layer-and-android-adoption/07-REVIEW.md
iteration: 1
findings_in_scope: 4
fixed: 4
skipped: 0
status: all_fixed
---

# Phase 7: Code Review Fix Report

**Fixed at:** 2026-10-30
**Source review:** .planning/phases/07-kmp-domain-layer-and-android-adoption/07-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 4 (Critical 0, Warning 4; Info excluded by `fix_scope: critical_warning`)
- Fixed: 4
- Skipped: 0

No golden under `core/domain/testdata/agenda/golden/` and no characterization expectation was edited. No public API of `core:model`, `core:data` or `core:domain` changed, so the Swift names gate was not needed (the iOS store edits are in `internal` classes).

## Fixed Issues

### WR-01: Speaker A-Z scroller index and row header use different case folding, so some letters break

**Files modified:** `feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/SpeakersViewModel.kt`, `feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/Speakers.kt`, `feature/speakers/src/test/java/com/gdgnantes/devfest/feature/speakers/list/SpeakerIndexCaseFoldingTest.kt` (new)
**Commit:** b6ff4909
**Applied fix:** Added `internal fun speakerLetterKey(name)` (`name.lowercase().firstOrNull()`). The ViewModel now derives each map key from `speakers[index].name` through it (first index per key wins, as in v1.0), and the `SpeakersList` header check uses the same function. New regression tests cover `ß` before and after an `S` speaker (map entries and header rows).
**Status:** fixed: requires human verification (logic change). Remaining known limit: the shared use case de-duplicates by the uppercase letter, so when an `ı` speaker comes before an `i` speaker the `i` entry is never produced (the header for the later `i` speaker is not shown). v1.0 would have shown both. Fixing that means changing the shared `SpeakerIndexUseCase` letter definition, which is also the iOS contract, so it was left alone. None of these letters appears in the recorded data.

### WR-02: `record-agenda-snapshot.sh` swallows real failures and prints a success marker

**Files modified:** `scripts/record-agenda-snapshot.sh`, `scripts/README.md`
**Commit:** 89ccddd0
**Applied fix:** Removed the "No tests found" tolerance and the reference to a later plan; any non-zero Gradle exit now stops the script (`set -e`). Step 2 runs only `AgendaSnapshotTest.neutral_snapshot_matches_the_raw_responses`, so the pinned-count test cannot block re-recording. The script prints a reminder to update the pinned counts by hand, and the README says the same. Verified by running step 2 in record mode against the committed raw responses: exactly one test ran, and the committed neutral snapshots stayed byte-identical. No regression test (shell script).

### WR-03: iOS stores persist `state.value` after the update, so concurrent writers can lose a write

**Files modified:** `core/data/src/iosMain/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsBookmarksStore.kt`, `core/data/src/iosMain/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsSessionFiltersStore.kt`, `core/data/src/iosTest/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsBookmarksStoreTest.kt`
**Commit:** 87c9fe66
**Applied fix:** Update and persist now run under one `NSLock` in both stores (the reviewer's lock option, using the platform lock so no new dependency is needed). Added `concurrent_writers_leave_every_bookmark_persisted` (200 concurrent `setBookmarked` calls on `Dispatchers.Default`). Checked that it fails with the old code (stash of the `iosMain` change) and passes with the fix.
**Status:** fixed: requires human verification (concurrency change).

### WR-04: Non-fatal report fires from a composition-time call and repeats on every recomputation

**Files modified:** `feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/SpeakersViewModel.kt`, `feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/Speakers.kt`, `feature/speakers/src/test/java/com/gdgnantes/devfest/feature/speakers/list/SpeakerIndexEmptyNameTest.kt`
**Commit:** 575970ae
**Applied fix:** `firstLetterIndexes(speakers)` is now pure. The store collector in the ViewModel calls a private `reportEmptyNames` and publishes the map as `letterIndexes: StateFlow<Map<Char, Int>>`, so the report happens once per data load. `Speakers` collects `letterIndexes` instead of calling the function inside `remember`. The empty-name tests now load through the store; new tests check that repeated reads and recomputation do not repeat the report and that a new load reports again. The characterization tests (`SpeakerIndexCharacterizationTest`, golden `android-speaker-index.json`) are unchanged.
**Status:** fixed: requires human verification (state-flow change).

## Skipped Issues

None. IN-01 to IN-04 are outside `fix_scope: critical_warning` and were not attempted.

## Verification

All gates ran in the isolated worktree (`.claude/worktrees/rf-07-9593-*`, with the main checkout's `local.properties` copied in), not in the main checkout. The fixes were then fast-forwarded onto `feature/reno_phase_7` and the worktree was removed, so the numbers cannot be re-read from that worktree. The final code is the same as in the main checkout.

| Gate | Result |
|------|--------|
| `./gradlew detekt lint` | pass (after fixing two detekt findings in the WR-04 code, folded into its commit) |
| `bash scripts/di-gate.sh` | `DI-GATE-OK` |
| `bash scripts/kmp-nc-version-gate.sh` | `KMPNC-VERSION-OK 1.0.6` |
| `./gradlew testDebugUnitTest jvmTest testAndroidHostTest` | pass (no failures or errors in the result XML) |
| `./gradlew :core:data:iosSimulatorArm64Test` | pass (iosMain touched; 8 bookmark store tests including the new one) |
| `bash scripts/swift-names-gate.sh check` | not run: no exported Kotlin API changed |

---

_Fixed: 2026-10-30_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
