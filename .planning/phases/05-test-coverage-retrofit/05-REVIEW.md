---
phase: 05-test-coverage-retrofit
reviewed: 2026-10-16T00:00:00Z
depth: standard
files_reviewed: 7
files_reviewed_list:
  - androidApp/proguard-rules.pro
  - androidApp/src/test/java/com/gdgnantes/devfest/androidapp/feedback/OpenFeedbackDatetimeLinkageTest.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/Agenda.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/ScheduleSlot.kt
  - core/model/src/commonTest/kotlin/com/gdgnantes/devfest/core/model/ScheduleSlotDateParsingTest.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/Agenda.kt
  - gradle/libs.versions.toml
findings:
  critical: 0
  warning: 3
  info: 4
  total: 7
status: issues_found
---

# Phase 5: Code Review Report (gap-closure plan 05-11, G-05-3)

**Reviewed:** 2026-10-16
**Depth:** standard
**Files Reviewed:** 7
**Status:** issues_found

## Summary

Incremental review of plan 05-11 (kotlinx-datetime `0.8.0-0.6.x-compat` pin for OpenFeedback, reflection linkage guard, `kotlin.time.Instant` import swaps, removal of the R8 `-dontwarn` rules). The previous review (plans 05-01..05-10) is superseded by this report. Its WR-01..WR-03 findings were fixed (see `05-REVIEW-FIX.md` and `05-REVIEW-DISPOSITION.md`).

The 05-11 changes themselves are sound. The import swaps are mechanical, and every `Instant`/`Clock` use in the touched files is consistent with `kotlin.time`. No `kotlinx.datetime.Instant` or `kotlinx.datetime.Clock` reference remains in first-party code. Removing the `-dontwarn` rules is correct: with the classes on the classpath they only hid dangling references. I checked the guard test's fixed expectation by hand: `Timestamp(1_760_605_200L, 0)` is 2025-10-16T09:00:00Z, which matches the `ScheduleSlot` test constant.

No change in the 05-11 diff introduces a BLOCKER. The remaining findings are mostly in code in the reviewed files that predates this plan and was not touched by it. The one real behavioural defect is the day-bucketing truncation in `Agenda.Builder` (WR-01), which the Phase 5 characterization test does not pin.

## Warnings

### WR-01: `Agenda.Builder` puts sessions that start up to 24h before a conference day into that day

**File:** `core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/Agenda.kt:15,17`
**Issue:** `startInstant.minus(DAY_ONE).inWholeDays == 0L` uses `Duration.inWholeDays`, which truncates toward zero. A session starting 2025-10-15T12:00Z gives -12h, so `inWholeDays` is `0` and the session is bucketed into day one. The same applies to day two for starts on 2025-10-16 after 00:00Z and before 2025-10-17T00:00Z. Because the first branch wins, those are caught by day one first, which hides the problem for day two. The check admits the half-open window `(DAY_ONE - 24h, DAY_ONE + 24h)` instead of `[DAY_ONE, DAY_ONE + 24h)`. `ScheduleSlotDateParsingTest.agendaBuilder_drops_sessions_outside_the_two_conference_days` only uses 2025-10-18, so it passes by luck and does not pin the lower edge.
**Fix:**
```kotlin
val dayOneEnd = DAY_ONE + 1.days
val dayTwoEnd = DAY_TWO + 1.days
when (startInstant) {
    in DAY_ONE until dayOneEnd -> dayOneSessions.add(session)
    in DAY_TWO until dayTwoEnd -> dayTwoSessions.add(session)
}
```
or `startInstant >= DAY_ONE && startInstant < DAY_ONE + 1.days`. Add a test with a session on 2025-10-15T12:00Z that expects it to be dropped.

### WR-02: `ScheduleSlot` throws on construction for default or malformed dates, and `compareTo` can overflow

**File:** `core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/ScheduleSlot.kt:6-14`
**Issue:** The constructor defaults are `""`, but the property initializers call `Instant.parse(startDate)` and `Instant.parse(endDate)`. `ScheduleSlot()` therefore throws `IllegalArgumentException` on construction, and so does any backend row with an empty or unparseable date. The defaults are misleading because they can never be used. `Agenda.Builder.build()` (`Agenda.kt:14`) re-parses with `Instant::parse` and has the same all-or-nothing failure mode, so one bad session aborts the whole agenda. `compareTo` returns `(a - b).toInt()`, which overflows for start dates more than about 24.8 days apart (`Int.MAX_VALUE` ms). This is harmless for a two-day event but breaks the `Comparable` contract for any other data. The new 05-02 test does not cover malformed input.
**Fix:**
```kotlin
data class ScheduleSlot(val endDate: String, val startDate: String) : Comparable<ScheduleSlot> {
    val startDateAsEpochMilliseconds = Instant.parse(startDate).toEpochMilliseconds()
    val endDateAsEpochMilliseconds = Instant.parse(endDate).toEpochMilliseconds()
    override fun compareTo(other: ScheduleSlot): Int =
        startDateAsEpochMilliseconds.compareTo(other.startDateAsEpochMilliseconds)
}
```
Remove the unusable defaults (check callers; `Mappers.kt:66` and `SessionFixtures.kt:33` pass explicit values). Decide on a policy for malformed backend data, either skip the session or fall back, and test it. If the behaviour is deliberately deferred, record it in `deferred-items.md`; nothing about it is recorded there today.

### WR-03: Agenda sorts by raw date string, while bucketing parses instants

**File:** `core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/Agenda.kt:12`
**Issue:** `sortedBy { it.scheduleSlot.startDate }` sorts lexically. The characterization test itself notes that the backend emits the `+02:00` offset form as well as `Z`. Lexical order is only correct when every string has the same offset. A mix, for example `2025-10-16T11:00:00+02:00` (09:00Z) against `2025-10-16T10:00:00Z`, sorts wrongly. The instants are already computed for the bucketing, and `ScheduleSlot` already exposes `startDateAsEpochMilliseconds`.
**Fix:** `sessions.sortedBy { it.scheduleSlot.startDateAsEpochMilliseconds }`, and add a mixed-offset ordering test.

## Info

### IN-01: Linkage test name claims sorting, and two statements assert nothing

**File:** `androidApp/src/test/java/com/gdgnantes/devfest/androidapp/feedback/OpenFeedbackDatetimeLinkageTest.kt:56-76`
**Issue:** `posting_comments_stamps_and_sorts_them_with_the_system_clock` explicitly does not assert order, so "sorts" is misleading. `first.hashCode()` at line 67 is a bare call with no assertion. It relies on throwing `NoClassDefFoundError` to fail, which is fine for a linkage guard but easy to delete as dead code.
**Fix:** Rename to `..._stamps_them_with_the_system_clock`, and make the intent explicit, for example `assertEquals(first.hashCode(), first.hashCode())` or `@Suppress("UnusedExpression")` plus a comment.

### IN-02: Reflection helpers fail opaquely on an OpenFeedback bump

**File:** `androidApp/src/test/java/com/gdgnantes/devfest/androidapp/feedback/OpenFeedbackDatetimeLinkageTest.kt:133-150`
**Issue:** `callStatic` and `newInstance` use `.single { ... }`. If OpenFeedback renames a function or adds an overload of the same arity, the failure is a generic `NoSuchElementException` or `IllegalArgumentException` with no class or method name. The header comment tells the maintainer to re-point names after a bump, but the failure message does not say which one broke.
**Fix:** Replace with `.singleOrNull { ... } ?: error("No $className.$name with ${args.size} params")`, and the same for constructors.

### IN-03: Display-format assertion is very weak and locale-fixed

**File:** `androidApp/src/test/java/com/gdgnantes/devfest/androidapp/feedback/OpenFeedbackDatetimeLinkageTest.kt:84-91`
**Issue:** `createdAt` is only checked as non-blank and containing a digit. That proves the `toLocalDateTime` path links, which is the purpose of the guard. It would not catch a wrong date. The `"en"` locale and the null arguments are positional magic values with no names.
**Fix:** Optional. Either label the arguments in a comment or add a relaxed check that the year `2025`/current year appears. Not required for the guard's purpose.

### IN-04: Version pin depends on Gradle's ordering of an unconventional qualifier

**File:** `gradle/libs.versions.toml:35`
**Issue:** `0.8.0-0.6.x-compat` is selected over a transitive `0.8.0` only because of Gradle's version-ordering rules for a non-standard qualifier. It is not a `strictly` constraint. The summary records that this resolved correctly on the three app runtime classpaths, and `OpenFeedbackDatetimeLinkageTest` covers `:androidApp` regression. Two gaps remain. A transitive request for a higher plain version, such as `0.8.1`, would silently win. The comment says a Dependabot bump "is rejected", but that only holds if CI's unit-test job runs on that PR. Also, `proguard-rules.pro` ends with a stray trailing blank line (line 29-30) left after the deletion.
**Fix:** Optional hardening: declare the catalog entry with `version { strictly("0.8.0-0.6.x-compat") }`, or keep the existing test as the sole guard. Trim the extra trailing blank line in `androidApp/proguard-rules.pro`.

---

_Reviewed: 2026-10-16_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
