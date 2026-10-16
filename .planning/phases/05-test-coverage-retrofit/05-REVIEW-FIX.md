---
phase: 05-test-coverage-retrofit
fixed_at: 2026-10-16T00:00:00Z
review_path: .planning/phases/05-test-coverage-retrofit/05-REVIEW.md
iteration: 1
findings_in_scope: 3
fixed: 3
skipped: 0
status: all_fixed
---

# Phase 5: Code Review Fix Report

**Fixed at:** 2026-10-16
**Source review:** .planning/phases/05-test-coverage-retrofit/05-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 3 (critical and warning; Info findings IN-01 to IN-06 are out of scope)
- Fixed: 3
- Skipped: 0

**Verification location:** the fixes were edited, committed and verified in an isolated git worktree (`.claude/worktrees/rf-05-*`, since removed), with `local.properties` copied in. The Gradle test runs below were made in that worktree, not in the main checkout, and were fast-forwarded onto `feature/reno_phase_5` afterwards.

## Fixed Issues

### WR-01: `KoinStartupTest` performs a real Firebase Remote Config fetch

**Files modified:** `androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/KoinStartupTest.kt`
**Commit:** 399059c
**Applied fix:** Split the test in two. `feedback_flags_resolve_to_the_real_remote_config_binding` only resolves `FeedbackFlags` and asserts it is `FirebaseFeedbackFlags`, which builds the Remote Config handle but starts no fetch. `feedback_form_view_model_resolves_with_offline_flags` loads an override module binding `FeedbackFlags` to a private no-op `OfflineFeedbackFlags`, then resolves `FeedbackFormViewModel`, so its init-time `fetchAndActivate` does nothing. Class KDoc updated. Verified: `./gradlew :androidApp:testDebugUnitTest --tests '*KoinStartupTest' --offline` passed.

### WR-02: Firebase singletons are shared across Robolectric tests and are never reset

**Files modified:** `feature/settings/src/test/java/com/gdgnantes/devfest/feature/settings/services/DataCollectionSettingsServiceImplTest.kt`, `androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/KoinStartupTest.kt`, `androidApp/src/test/java/com/gdgnantes/devfest/androidapp/di/ProductionGraphStartupTest.kt`
**Commit:** c17825e
**Applied fix:** In each class, `setUp` now calls `FirebaseApp.clearInstancesForTest()` and then initializes `FirebaseApp` unconditionally (the `getApps().isEmpty()` guard is removed), and `tearDown` calls `clearInstancesForTest()` again (a new `@After` in `DataCollectionSettingsServiceImplTest`). `ProductionGraphStartupTest` has the identical pattern, so it was fixed too even though the review named only the first two. `clearInstancesForTest` was visible from both modules, so the `getApps().forEach { it.delete() }` fallback was not needed. Verified: `:androidApp:testDebugUnitTest --tests '*KoinStartupTest' --tests '*ProductionGraphStartupTest'` and `:feature:settings:testDebugUnitTest` passed.

### WR-03: Two pinned production defects are not recorded in `deferred-items.md`

**Files modified:** `.planning/phases/05-test-coverage-retrofit/deferred-items.md`, `feature/session-detail/src/test/java/com/gdgnantes/devfest/feature/sessiondetail/FallbackFeedbackUrlTest.kt`, `core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/graphql/MapperTest.kt`
**Commit:** f5ed59f
**Applied fix:** Added items 17 (double slash in the fallback OpenFeedback URL) and 18 (fixed `+02:00` offset on zone-less backend timestamps) to `deferred-items.md` under a new "From the Phase 5 code review (WR-03)" section, and updated the closing count. Added a pointer comment to the `FallbackFeedbackUrlTest` KDoc and above `MapperTest.start_dates_get_the_paris_offset_appended`. Comment and documentation changes only, so no build was run.

---

_Fixed: 2026-10-16_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
