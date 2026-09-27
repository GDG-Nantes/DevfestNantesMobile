---
phase: 03-multi-module-architecture-extraction
plan: 05
subsystem: android-architecture
tags: [gradle, multi-module, jetpack-compose, hilt, design-system, resources]

# Dependency graph
requires:
  - phase: 03-multi-module-architecture-extraction (03-04)
    provides: "Android convention plugins (devfest.android.library, devfest.android.hilt) + androidApp adopting them"
provides:
  - ":core:ui module (com.gdgnantes.devfest.core.ui.*): theme, UiState, BookmarksViewModel, shared Compose components, DateUtils/StringExtensions utils"
  - "resources-gate.sh: reusable resource-ownership/translation-invariance gate for 03-06..03-08"
  - "Fixed module map + resource ownership precedent for remaining feature extraction plans"
affects: [03-06-feature-venue-about, 03-07-feature-settings-speakers, 03-08-feature-agenda-session-detail, 03-09-umbrella-thinning]

# Actuals (#2632)
actuals:
  tokens: 17633
  tasks: 3
  commits: 3

# Plan commit ledger (#3968) — measured, not narrated
commits: 3
plan_head_before: 6d5d5900822de2c0d9cd23a08e215cf50d369423

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Two-commit module extraction (D-16): pure git-mv with packages unchanged + build wiring, then a package-rename-only commit"
    - "resources-gate.sh: multiset diff of string/color XML entries and drawable/mipmap basenames against origin/main, catching lost/duplicated/edited resources across module boundaries"
    - "D-08 consumer-count rule for file placement (2+ consumers -> core:ui; exactly one -> that feature; zero outside app -> stays in androidApp)"

key-files:
  created:
    - core/ui/build.gradle.kts
    - core/ui/src/main/AndroidManifest.xml
    - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/theme/Color.kt
    - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/theme/Theme.kt
    - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/theme/Type.kt
    - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/UiState.kt
    - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/BookmarksViewModel.kt
    - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/components/LoadingLayout.kt
    - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/components/SessionCategory.kt
    - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/components/SocialIcon.kt
    - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/components/SpeakerPicture.kt
    - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/components/appbars/TopAppBar.kt
    - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/components/appbars/AppBarIcons.kt
    - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/utils/DateUtils.kt
    - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/utils/StringExtensions.kt
    - core/ui/src/androidTest/java/com/gdgnantes/devfest/core/ui/utils/ScheduleSlotDateFormatAndroidTest.kt
    - .planning/phases/03-multi-module-architecture-extraction/resources-gate.sh
  modified:
    - settings.gradle.dcl
    - androidApp/build.gradle.kts
    - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidCommon.kt
    - "~35 androidApp consumer files (R-import / package-import updates only)"

key-decisions:
  - "StringExtensions.kt (titlecaseFirstCharIfItIsLowercase) moved to :core:ui per the 2-consumer rule, decided at execution time via git grep as specified in the plan"
  - "failOnNoDiscoveredTests disabled in build-logic's AndroidCommon.kt (Rule 1 deviation) so CI's bare ./gradlew testDebugUnitTest does not fail on leaf modules (e.g. :core:ui) that carry zero JVM unit tests"
  - "Resource ownership followed D-17 exactly: app-only resources (launcher icons, themes.xml, theme_splash.xml, locales_config.xml) stayed in androidApp; strings/colors referenced by 2+ modules or by a core:ui file moved to core:ui in values/values-fr/values-night lockstep; no resource name duplicated across modules"

patterns-established:
  - "resources-gate.sh is the reusable resource-ownership gate for every subsequent feature-extraction plan (03-06..03-08)"
  - "D-16 two-commit pattern (pure move, then repackage) reconfirmed as the safe sequencing for module extraction with resource migration"

requirements-completed: []  # ARCH-02/ARCH-03 are phase-spanning; requirements.ready-ids reports 0/2 ready (sibling plans 03-06..03-08 not yet finished) — not marked complete here, left for the last declaring plan.

coverage:
  - id: D1
    description: ":core:ui module extracted as a pure Android library (devfest.android.library + devfest.android.hilt), holding theme, UiState, BookmarksViewModel, and every composable/util consumed by 2+ modules, with zero :feature:* dependency"
    requirement: "ARCH-02"
    verification:
      - kind: automated_ui
        ref: "gradlew :core:ui:assembleDebug :core:ui:assembleDebugAndroidTest :androidApp:assembleDebug :androidApp:assembleRelease :androidApp:testDebugUnitTest detekt lint (Task 1 + Task 2 <verify>)"
        status: pass
      - kind: other
        ref: "./gradlew :core:ui:dependencies --configuration releaseRuntimeClasspath — no project :feature: entry"
        status: pass
    human_judgment: false
  - id: D2
    description: "Resources (strings/colors/drawables) partitioned by ownership rule (D-17), byte-identical to origin/main, no duplicate names across modules"
    requirement: "ARCH-03"
    verification:
      - kind: other
        ref: "resources-gate.sh -> RESOURCES-OK"
        status: pass
    human_judgment: false
  - id: D3
    description: ":core:ui repackaged to com.gdgnantes.devfest.core.ui.* with import-only diff; both CI workflows green on pushed HEAD"
    requirement: "ARCH-02"
    verification:
      - kind: other
        ref: "CI-GREEN-BOTH — android.yml run 36044231096, ios.yml run 36044231094"
        status: pass
    human_judgment: false
  - id: D4
    description: "Android smoke checkpoint 2 (D-18): Agenda, Speakers, Venue, About, session detail, speaker detail, Settings render identically in light/dark and French locale after the :core:ui extraction"
    verification: []
    human_judgment: true
    rationale: "Visual/UX parity across screens, themes and locale is a human judgment call — automated resource/build checks cannot confirm rendering equivalence."

# Metrics
duration: 30min
completed: 2026-09-25
status: complete
---

# Phase 3 Plan 5: Core UI Module Extraction Summary

**Extracted `:core:ui` (theme, UiState, BookmarksViewModel, shared Compose components, DateUtils/StringExtensions) as a two-commit module move with a reusable resource-ownership gate; both CI workflows green and Android smoke checkpoint 2 approved.**

## Performance

- **Duration:** 30 min (continuation session; prior session completed Tasks 1-2 and reached Task 3 checkpoint)
- **Started:** 2026-09-24 (original session) / continued 2026-09-25
- **Completed:** 2026-09-25T00:00:00Z (approx, continuation session)
- **Tasks:** 3
- **Files modified:** 67 (across 3 commits since 6d5d590)

## Accomplishments
- `:core:ui` module created (devfest.android.library + devfest.android.hilt, no iosMain) holding theme (Color/Theme/Type), UiState, `@HiltViewModel BookmarksViewModel`, LoadingLayout, SessionCategory, SocialIcon, TopAppBar, AppBarIcons, SpeakerPicture, DateUtils, StringExtensions, plus the moved androidTest (`ScheduleSlotDateFormatAndroidTest`)
- Resources (strings values/values-fr, colors, 6 drawables) partitioned by ownership rule and verified byte-identical to origin/main via `resources-gate.sh` (`RESOURCES-OK`), reused as the gate for 03-06..03-08
- `:core:ui` repackaged from the transitional `com.gdgnantes.devfest.androidapp.*` paths to its permanent `com.gdgnantes.devfest.core.ui.*` namespace, import-only diff, ~35 androidApp consumer files repointed
- Both CI workflows (android.yml, ios.yml) green on the pushed repackage commit; local build/test/detekt/lint green; `:core:ui` has no `:feature:*` dependency (ARCH-02)
- Android smoke checkpoint 2 (D-18) approved by the user (2026-09-24): Agenda bookmark toggle, Venue, session detail category chip + bookmark FAB, speaker detail, Settings, light/dark, French locale all render identically

## Task Commits

Each task was committed atomically:

1. **Task 1: :core:ui pure move (packages unchanged) + resources + resources-gate.sh** - `79a788c` (feat)
2. **Task 2: Repackage :core:ui to com.gdgnantes.devfest.core.ui.* + CI gate** - `6d8de43` (feat), `8e360e0` (fix — deviation, see below)
3. **Task 3: D-18 Android smoke checkpoint 2** - checkpoint only, no code commit; user approved 2026-09-24

**Plan metadata:** committed as part of this SUMMARY commit (docs(03-05))

## Files Created/Modified
- `core/ui/build.gradle.kts` - devfest.android.library + devfest.android.hilt leaf; api(core:model), implementation(core:data, core:analytics), compose-material-icons-extended, coil-compose
- `core/ui/src/main/AndroidManifest.xml` - minimal manifest for the new module
- `core/ui/src/main/java/com/gdgnantes/devfest/core/ui/**` - 13 Kotlin files moved and repackaged (theme, UiState, BookmarksViewModel, shared components, appbars, DateUtils, StringExtensions)
- `core/ui/src/androidTest/java/com/gdgnantes/devfest/core/ui/utils/ScheduleSlotDateFormatAndroidTest.kt` - moved androidTest, now part of :core:ui's instrumentation suite
- `core/ui/src/main/res/**` - moved values/values-fr strings, 6 drawables per ownership rule
- `.planning/phases/03-multi-module-architecture-extraction/resources-gate.sh` - resource ownership/translation invariance gate (multiset diff vs origin/main)
- `settings.gradle.dcl` - `include(":core:ui")`
- `androidApp/build.gradle.kts` - `implementation(project(":core:ui"))`
- `androidApp/src/main/java/**` (~35 files) - R-import and package-import updates only (no logic changes)
- `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidCommon.kt` - `failOnNoDiscoveredTests` disabled (deviation, see below)

## Decisions Made
- StringExtensions.kt's `titlecaseFirstCharIfItIsLowercase` moved to `:core:ui` — `git grep` at execution time confirmed 2+ consuming modules, matching the plan's decided-at-execution rule
- Resource ownership followed D-17 precisely: app-only resources (launcher icons, `themes.xml`, `theme_splash.xml`, `ic_launcher_background.xml`, `mipmap-anydpi-v26/*`, `locales_config.xml`) stayed in androidApp; verified still tracked there post-move
- Two-commit D-16 pattern (pure move with packages unchanged, then package-rename-only) kept the diff auditable at each step, per plan's `PURE-MOVE-OK` and Kotlin-diff-only-package/import verification gates

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Disabled `failOnNoDiscoveredTests` in build-logic's AndroidCommon.kt**
- **Found during:** Task 2 (CI gate verification after repackage push)
- **Issue:** CI's bare `./gradlew testDebugUnitTest` failed on `:core:ui`, a leaf module carrying zero JVM unit tests (all its verification is via the moved androidTest, i.e. instrumentation, not unit tests) — the Android Gradle Plugin's test task defaults to failing when no tests are discovered
- **Fix:** Set `failOnNoDiscoveredTests = false` (or module-appropriate equivalent) in the shared `AndroidCommon.kt` convention configuration so leaf modules with no unit tests don't fail the bare `testDebugUnitTest` CI invocation
- **Files modified:** `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidCommon.kt`
- **Verification:** CI re-run green on both workflows (android.yml run 36044231096, ios.yml run 36044231094); local `./gradlew testDebugUnitTest` passes across all modules
- **Committed in:** `8e360e0`

---

**Total deviations:** 1 auto-fixed (1 bug fix)
**Impact on plan:** Necessary correctness fix for CI to pass on a module topology the plan itself introduced (a leaf module with only instrumentation tests). No scope creep — confined to build-logic test-task configuration.

## Issues Encountered
None beyond the deviation documented above.

## Authentication Gates
None.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- `:core:ui` established and verified as the shared design-system/utility module for all remaining feature extractions (03-06..03-08)
- `resources-gate.sh` is ready for reuse by 03-06..03-08's own resource migrations
- Module map for 03-06 (`:feature:venue`, `:feature:about`), 03-07 (`:feature:settings`, `:feature:speakers`), 03-08 (`:feature:agenda`, `:feature:session-detail`) is fixed and unchanged by this plan
- ARCH-02/ARCH-03 remain open (phase-spanning requirements) until the last declaring plan (03-08) finishes — `requirements.ready-ids` confirms 0/2 ready at this point, correctly deferred rather than marked complete prematurely
- No blockers for 03-06

---
*Phase: 03-multi-module-architecture-extraction*
*Completed: 2026-09-25*

## Self-Check: PASSED

- FOUND: core/ui/build.gradle.kts
- FOUND: core/ui/src/main/AndroidManifest.xml
- FOUND: core/ui/src/main/java/com/gdgnantes/devfest/core/ui/BookmarksViewModel.kt
- FOUND: core/ui/src/main/java/com/gdgnantes/devfest/core/ui/theme/Theme.kt
- FOUND: .planning/phases/03-multi-module-architecture-extraction/resources-gate.sh
- FOUND commit 79a788c in git log
- FOUND commit 6d8de43 in git log
- FOUND commit 8e360e0 in git log
- Plan commit ledger: plan_head_before 6d5d590, commits measured = 3 (git rev-list --count 6d5d590..HEAD)
