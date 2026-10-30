---
phase: 07-kmp-domain-layer-and-android-adoption
reviewed: 2026-10-30T00:00:00Z
depth: standard
files_reviewed: 10
files_reviewed_list:
  - core/data/src/iosMain/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsBookmarksStore.kt
  - core/data/src/iosMain/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsSessionFiltersStore.kt
  - core/data/src/iosTest/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsBookmarksStoreTest.kt
  - core/data/src/iosTest/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsSessionFiltersStoreTest.kt
  - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/Speakers.kt
  - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/SpeakersViewModel.kt
  - feature/speakers/src/test/java/com/gdgnantes/devfest/feature/speakers/list/SpeakerIndexCaseFoldingTest.kt
  - feature/speakers/src/test/java/com/gdgnantes/devfest/feature/speakers/list/SpeakerIndexEmptyNameTest.kt
  - scripts/README.md
  - scripts/record-agenda-snapshot.sh
findings:
  critical: 0
  warning: 0
  info: 2
  total: 2
status: issues_found
---

# Phase 7: Code Review Report

**Reviewed:** 2026-10-30
**Depth:** standard
**Files Reviewed:** 10
**Status:** issues_found

## Summary

Incremental re-review after fixes WR-01..WR-04 and the Nyquist test commit. All four earlier
warnings are fixed as described:

- The iOS stores now update and persist under one `NSLock`, so the persisted array always matches
  the latest in-memory state. There is no re-entrancy: `StateFlow.update` and `setObject` do not call
  back into the store.
- `SpeakersViewModel` computes `letterIndexes` once per data load, and the empty-name report is made
  only from that collector. The tests cover this: no repeat on re-reads, and a new load reports again.
- `speakerLetterKey` is shared by the index map and the row header check. This fixes the `ß`
  divergence; the shared use case keeps `"SS"` and `"S"` as distinct letters, so both entries survive.
- `record-agenda-snapshot.sh` runs the rewrite test alone, and the README matches the script.

I found no bugs, security problems or test-reliability problems that block shipping. Two minor
quality items remain.

## Info

### IN-01: Unused destructured variable `letter`

**File:** `feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/SpeakersViewModel.kt:68`
**Issue:** `for ((letter, index) in indexes.filter { ... })` never uses `letter`. This triggers a
Kotlin "unused variable" warning, and the key now comes from `speakerLetterKey`.
**Fix:**
```kotlin
for ((_, index) in indexes.filter { it.letter.isNotEmpty() }) {
```

### IN-02: "Cannot diverge" comment overstates the guarantee for letters that share an upper-case form

**File:** `feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/SpeakersViewModel.kt:64-66`
**Issue:** The shared use case dedups on `name.take(1).uppercase()`, while the key is
`name.lowercase().first()`. Two different lowercase keys can share one upper-case letter, for example
dotless `ı` and `i` (both give `"I"`). Given `["Ian", "ısı"]`, `firstIndexes` returns only index 0.
The `ı` key is then absent from the map, so the `ısı` row gets no header and the scroller cannot reach
it. The `ß` case is fixed, but this class of divergence is not removed by the shared key function. The
use case documents that such edge cases are absent from the data, so this is low risk.
**Fix:** Either soften the comment (the key is consistent between map and header, but grouping still
follows the shared upper-case letter), or build the map directly in the ViewModel from `speakerLetterKey`
and keep the shared use case only for the empty-name check:
```kotlin
val keyed = LinkedHashMap<Char, Int>()
speakers.forEachIndexed { i, s -> speakerLetterKey(s.name)?.let { keyed.putIfAbsent(it, i) } }
```

---

_Reviewed: 2026-10-30_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
