---
phase: 05-test-coverage-retrofit
plan: 02
subsystem: testing
tags: [kotlinx-datetime, kotlin-time, robolectric, characterization-tests, kmp, dates]

requires:
  - phase: 05-test-coverage-retrofit
    provides: "05-01 Kover baseline, live-canary split and widened iOS CI"
provides:
  - "String.toInstantOrNull() and Instant.toLocalDate(timeZone) in :core:model commonMain (kotlin.time.Instant, HiddenFromObjC), tested on JVM and iOS"
  - "No production SimpleDateFormat anywhere: DateUtils, FallbackFeedbackForm and ScheduleSlotUtils run on the shared parser"
  - "Robolectric characterization suites for the day label, session date range and OpenFeedback fallback URL"
  - "currentStartTimeIndex(groups, now: Instant): pure, unit-tested agenda auto-scroll index"
affects: [05-03, 05-10]

actuals:
  tokens: 5200
  tasks: 3
  commits: 3

tech-stack:
  added: []
  patterns:
    - "Characterize-then-swap: pin outputs on the old code in one commit, swap in the next, diff the characterization files against the first commit"
    - "Robolectric tests pin sdk 34, set Europe/Paris as default zone and restore zone and locale in @After"

key-files:
  created:
    - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/utils/IsoDate.kt
    - core/model/src/commonTest/kotlin/com/gdgnantes/devfest/core/model/utils/IsoDateTest.kt
    - core/ui/src/test/java/com/gdgnantes/devfest/core/ui/utils/DateUtilsCharacterizationTest.kt
    - feature/session-detail/src/test/java/com/gdgnantes/devfest/feature/sessiondetail/FallbackFeedbackUrlTest.kt
    - feature/session-detail/src/test/java/com/gdgnantes/devfest/feature/sessiondetail/utils/ScheduleSlotFormattedRangeTest.kt
    - feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/CurrentStartTimeIndexTest.kt
  modified:
    - gradle/libs.versions.toml
    - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/utils/DateUtils.kt
    - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/FallbackFeedbackForm.kt
    - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/utils/ScheduleSlotUtils.kt
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaColumn.kt
    - core/ui/src/androidTest/java/com/gdgnantes/devfest/core/ui/utils/ScheduleSlotDateFormatAndroidTest.kt
    - core/data/src/jvmTest/kotlin/com/gdgnantes/devfest/core/data/graphql/GraphQLStoreLiveNetworkTest.kt

key-decisions:
  - "Localized display stays Android-only (android.text.format.DateUtils on epoch millis); only parsing and the fallback date moved to common code"
  - "IsoDate helpers take kotlin.time.Instant (the non-deprecated name); the existing kotlinx.datetime.Instant imports in ScheduleSlot/Agenda are the same type via the typealias and were left alone"
  - "getDateFromIso8601 keeps returning java.util.Date? so the time-header caller (groupSessionsByStartTime) is untouched"

patterns-established:
  - "Pattern: expected values for characterization tests are written as literals, with ICU thin spaces as unicode escapes"

requirements-completed: [TEST-01]

coverage:
  - id: D1
    description: "Day label, session date range and fallback URL outputs are pinned on the pre-swap code and unchanged after the swap"
    requirement: TEST-01
    verification:
      - kind: unit
        ref: "DateUtilsCharacterizationTest, ScheduleSlotFormattedRangeTest, FallbackFeedbackUrlTest (green before and after; characterization files byte-identical to f337dc1)"
        status: pass
    human_judgment: false
  - id: D2
    description: "Shared ISO parser in :core:model, null on unparseable input, tested on JVM and iOS"
    requirement: TEST-01
    verification:
      - kind: unit
        ref: "core/model IsoDateTest (9 tests on jvmTest and iosSimulatorArm64Test)"
        status: pass
    human_judgment: false
  - id: D3
    description: "OpenFeedback fallback URL date is ASCII yyyy-MM-dd in a non-Latin-digit locale (D-05)"
    requirement: TEST-01
    verification:
      - kind: unit
        ref: "FallbackFeedbackUrlTest#fallback_url_is_ascii_in_a_non_latin_digit_locale (RED on old code, GREEN after)"
        status: pass
    human_judgment: false
  - id: D4
    description: "Agenda auto-scroll index is a pure function of an explicit Instant, no Dispatchers.IO and no java.util.Date"
    requirement: TEST-01
    verification:
      - kind: unit
        ref: "CurrentStartTimeIndexTest (5 tests)"
        status: pass
    human_judgment: false
  - id: D5
    description: "Localized rendering on a device reads as before (agenda day tabs, time headers, session range, scroll position), English and French"
    requirement: TEST-01
    verification:
      - kind: other
        ref: "French verified on the Pixel_9 emulator; English on device not verified (see Human check)"
        status: unknown
    human_judgment: true
    rationale: "Tests pin Robolectric's ICU output, not the device's; the plan marks this a human check"

duration: 35min
completed: 2026-10-15
status: complete
plan_head_before: 6164c15cd56d98f2368e4ed344b32c7b4b0bb284
plan_head_after: 707efbb14c513c84d278c81b53e3a2143ac6eae9
commits: 3
---

# Phase 5 Plan 02: Shared ISO Date Parser and Locale-Proof Fallback URL Summary

**ISO-8601 parsing moved into `:core:model` commonMain as `String.toInstantOrNull()` / `Instant.toLocalDate()` (kotlin.time.Instant), every production `SimpleDateFormat` removed behind unchanged signatures, the OpenFeedback fallback URL made ASCII-only, and the agenda auto-scroll index turned into a pure, tested function.**

## Performance

- **Duration:** about 35 min
- **Tasks:** 3
- **Files modified:** 13 (6 new, 7 changed)

## Accomplishments

- Task 1 pinned today's behavior with 16 Robolectric tests (9 `DateUtilsCharacterizationTest`, 2 `ScheduleSlotFormattedRangeTest`, 5 `FallbackFeedbackUrlTest`) on the unmodified production code, and committed them before any date code changed. Expected values: absolute epoch millis worked out with `date`, never by calling the code under test.
- Task 2 added `IsoDate.kt`, swapped `DateUtils.kt`, `FallbackFeedbackForm.kt` and `ScheduleSlotUtils.kt`, and left the Task 1 characterization files untouched (`git diff f337dc1 -- <those files>` is empty, CHARACTERIZATION-UNCHANGED).
- Task 3 replaced `getCurrentStartTimeIndex` (suspend, `Dispatchers.IO`, `Date()`) with `internal fun currentStartTimeIndex(groups, now: Instant)`, called with `Clock.System.now()`.
- `git grep SimpleDateFormat` over every production source set, `core/ui/src/androidTest` and `core/data/src/jvmTest` prints nothing.

## Observed localized literals (pinned, Europe/Paris default zone, Robolectric sdk 34)

| Case | en-US | fr-FR |
|------|-------|-------|
| `getDayFromIso8601(DAY_ONE_ISO)` | `October 16, 2025` | `16 octobre 2025` |
| `getDayFromIso8601(DAY_TWO_ISO)` | `October 17, 2025` | `17 octobre 2025` |
| `getFormattedRange` 11:00 to 11:45 on 2025-10-16 | `Thursday, October 16, 2025, 11:00 – 11:45 AM` | `Jeudi 16 octobre 2025, 11:00 – 11:45` |
| Fallback URL | `https://openfeedback.io/devfestnantes2025//2025-10-16/form-1` | identical |

The `//` comes from `OPENFEEDBACK_BASE_URL` already ending with `/`; it is current behavior and was kept. A 23:30Z session yields `2025-10-17` with a Paris default zone and `2025-10-16` with UTC.

## D-05 evidence: ar-EG before and after

`fallback_url_is_ascii_in_a_non_latin_digit_locale` was added and run against the Task 1 (pre-swap) production code first. It failed:

```
expected:<.../devfestnantes2025//[2025-10-16]/form-1> but was:<.../devfestnantes2025//[٢٠٢٥-١٠-١٦]/form-1>
```

(`٢٠٢٥-١٠-١٦`, Arabic-Indic digits from `SimpleDateFormat(pattern)` with the default locale.) After the swap to `LocalDate.toString()` it passes and every character is below 128.

## Divergent parser inputs (not asserted, unreachable)

Every string reaching the UI helpers has already passed `ScheduleSlot`'s `Instant.parse` initializer (or is an `Agenda.DAY_*_ISO` constant). Inputs where the old lenient `SimpleDateFormat` and `Instant.parseOrNull` can differ therefore never arrive:

- fractional seconds (`...:00.123Z`): `Instant.parse` accepts them; the old pattern's trailing text handling differed
- lenient overflow such as month 13 or hour 25: the old formatter rolled the date forward; the new parser returns null (and `ScheduleSlot` would already have thrown)
- trailing garbage after a valid prefix: the old `parse` ignored it; the new parser returns null
- offset without a colon (`+0200`)

These were reasoned from the APIs, not measured; the suite pins only the reachable forms (offset form, `Z`, the two day constants, late-evening and previous-year offsets, and four null cases).

## Task Commits

1. Task 1: `f337dc1` test(05-02): characterize date parsing and formatting before the swap
2. Task 2: `7dc3620` fix(05-02): shared ISO parser; OpenFeedback fallback date is locale-proof (D-05)
3. Task 3: `707efbb` refactor(05-02): agenda scroll index as a pure Instant function; adapt formatter-only tests

## TDD Notes (Tasks 2 and 3)

- Task 2: `IsoDateTest` was written first and failed to compile (unresolved `toInstantOrNull`/`toLocalDate`), then went green on JVM and iOS after `IsoDate.kt`. A compile failure is not a classifier-grade RED, which is acceptable for a brand-new API. The behavior RED that matters, the ar-EG URL, was a true assertion failure (above). The plan prescribes one commit per task, so RED/GREEN were not split into separate commits.
- Task 3: `currentStartTimeIndex` was extracted before its test was written, so there is no RED for it. It is a behavior-preserving extraction of the existing algorithm; the test pins that behavior. Treat D4 as characterization, not test-first.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Task 3 launch verify cannot run as written**
- **Found during:** Task 3, ANDROID-LAUNCH-OK step
- **Issue:** `android run` with no arguments fails ("No apks specified"), and the check greps the layout for `...mobile.androidapp.dev`, but the debug APK installs as `com.gdgnantes.devfest.mobile.androidapp` (AGENTS.md agrees); the `.dev` package on the emulator is an older install.
- **Fix:** Ran `android run --apks=androidApp/build/outputs/apk/debug/androidApp-debug.apk --activity=.MainActivity`; the Pixel_9 emulator then failed to bring the app to the foreground, so I launched it with `adb shell am start` and confirmed the Agenda through `android layout` and `android screen capture`.
- **Files modified:** none
- **Commit:** n/a

**2. [Rule 1 - Style] detekt ArgumentListWrapping on a long assertEquals**
- **Found during:** Task 1
- **Fix:** wrapped the arguments in `ScheduleSlotFormattedRangeTest` before committing.

**Total deviations:** 2 auto-fixed. **Impact:** none on scope or outputs.

## Device check (partial)

On the Pixel_9 emulator with a French system locale, the debug build showed the Agenda day tabs `16 octobre 2025` and `17 octobre 2025`, and a session detail range `Jeudi 16 octobre 2025, 17:50 - 18:10`, matching the pinned format. Screenshots: `/private/tmp/claude-501/-Users-robin-Development-Android-DevFestNantes/22355a4f-5116-4bc9-88a8-c70715b8921f/scratchpad/agenda_fr.png` and `.../detail_fr.png`. The data-sharing dialog was left unconsented (back out of "Personnaliser" with all toggles off). The English system locale could not be set without root, so English was not checked on a device; its output is covered only by the Robolectric characterization. The auto-scroll position was not compared against a pre-change build.

## Human check (open)

Switch the device language to English, open the Agenda (both day tabs) and one session detail, and confirm day tab labels, time headers and the session date range read as before; confirm the agenda opens scrolled to the current time slot.

## Issues Encountered

- Running the unit-test and canary suites repeatedly with `--no-daemon` took 15 to 30 seconds each; no other problems. The live canary (`-PliveNetworkTests`) was run once against the backend: 12 of 12 passed, including `scheduleSlot_dates_are_parseable_by_the_shared_parser`.

## Authentication Gates

None.

## Known Stubs

None.

## Threat Flags

None. No new network endpoint, auth path or trust boundary. T-5-03 (malformed dates return null, covered by `IsoDateTest`) and T-5-04 (ASCII URL, covered by the ar-EG test) are mitigated; T-5-SC: no new coordinate, `robolectric` and `androidx-test-core` were already in the catalog.

## Next Phase Readiness

Ready for 05-03. CI is green on 707efbb: Android CI run 38798121326 and iOS CI run 38798121311.

## Self-Check: PASSED

Files verified present; commits f337dc1, 7dc3620 and 707efbb on the branch; tokens CHARACTERIZATION-GREEN, DATE-SWAP-GREEN, CHARACTERIZATION-UNCHANGED, SWIFT-NAMES-OK, AGENDA-INDEX-GREEN and CI-GREEN-BOTH observed. ANDROID-LAUNCH-OK as scripted was not run (see deviation 1).
