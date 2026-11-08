---
phase: 09-ios-on-shared-logic-and-legacy-persistence
reviewed: 2026-11-08T00:00:00Z
depth: standard
files_reviewed: 6
files_reviewed_list:
  - documentation/ARCHITECTURE.md
  - iosApp/DevFestNantesTests/AgendaViewModelTests.swift
  - iosApp/DevFestNantesTests/Fakes/FakeAgendaSource.swift
  - iosApp/DevFestNantesTests/KotlinContinuousFlowTests.swift
  - iosApp/iosApp/Agenda/AgendaSource.swift
  - iosApp/iosApp/Agenda/AgendaViewModel.swift
findings:
  critical: 0
  warning: 1
  info: 2
  total: 3
status: issues_found
---

# Phase 9: Code Review Report (incremental, WR-01 fix)

**Reviewed:** 2026-11-08
**Depth:** standard
**Files Reviewed:** 6
**Status:** issues_found

## Summary

Incremental review of the diff `c087f202..HEAD`, which fixes prior WR-01 (the bookmark mirror was never reconciled with the store after a failed or skipped seed). The fix adds `AgendaSource.isBookmarked(_:)`, backed by the synchronous `BookmarksStore.isBookmarked(id:)`. While `bookmarksSeeded` is false, `isBookmarked` and `toggleBookmark` read through to the store.

The fix is sound for the case it targets (failed or skipped seed). Checks that passed:

- `isBookmarked` reads `bookmarkedIds` before branching, so Observation tracks the mirror even in the fallback path.
- The unseeded toggle aligns the mirror entry with the store before flipping, so the star and the toggle agree.
- The `LiveAgendaSource` adapter stays the only KMP caller, with no Combine and no new logging.
- The new tests cover the failed seed, the skipped seed with an observation notification, retry then mirror, and the live store.
- The ARCHITECTURE.md text matches the code.

One narrow race remains, plus two minor quality items.

## Warnings

### WR-01: A bookmark toggled while the seed is awaiting can be overwritten by the stale first emission

**File:** `iosApp/iosApp/Agenda/AgendaViewModel.swift:93-99`
**Issue:** `seedBookmarksIfNeeded` awaits the first emission, which is a snapshot taken when the Kotlin collection starts. It then assigns `bookmarkedIds = ids` wholesale. The suspension spans a hop from the Kotlin flow back to the main actor, and the user can toggle a star in that window. The unseeded toggle writes the store and the mirror correctly, but the seed then replaces the mirror with the older snapshot and sets `bookmarksSeeded = true`. The store holds the toggled value while the mirror, and therefore every star, shows the old one. It stays wrong until the next toggle of that session or the next launch. Before the fix this window existed too, but the fallback now makes the "mirror always matches the store" claim in ARCHITECTURE.md ("so they always match what is persisted") stronger than the code guarantees. The window is narrow.
**Fix:** Record the ids toggled while unseeded and re-apply them over the seeded set:
```swift
@ObservationIgnored private var unseededOverrides: [String: Bool] = [:]
// in toggleBookmark, after computing `bookmarked`:
if !bookmarksSeeded { unseededOverrides[sessionId] = bookmarked }
// in seedBookmarksIfNeeded, instead of `bookmarkedIds = ids`:
var seeded = ids
for (id, value) in unseededOverrides { if value { seeded.insert(id) } else { seeded.remove(id) } }
bookmarkedIds = seeded
unseededOverrides.removeAll()
bookmarksSeeded = true
```
Alternatively, after the seed, rebuild the set from `source.isBookmarked` for the ids touched while unseeded.

## Info

### IN-01: The insert/remove pair is duplicated inside `toggleBookmark`

**File:** `iosApp/iosApp/Agenda/AgendaViewModel.swift:69-84`
**Issue:** The same `if x { insert } else { remove }` block appears twice, once for the alignment and once for the toggle. This makes the method harder to read.
**Fix:** Extract a private helper such as `setMirror(_ id: String, _ bookmarked: Bool)`. The alignment becomes `setMirror(sessionId, source.isBookmarked(sessionId))`. Setting an equal value is harmless, because the Set mutation is idempotent. If skipping a redundant mutation notification matters, keep the `!=` guard inside the helper.

### IN-02: Stale comment in the seed error path

**File:** `iosApp/iosApp/Agenda/AgendaViewModel.swift:103`
**Issue:** The comment `// Counts only, never the ids.` sits above a log of `error.localizedDescription` and the error object. It is a leftover from earlier wording and describes nothing here. It could also lead a reader to think the log carries counts.
**Fix:** Remove the comment, or reword it to "The ids are never logged."

---

_Reviewed: 2026-11-08_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
