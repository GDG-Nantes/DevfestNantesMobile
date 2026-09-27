---
phase: 03-multi-module-architecture-extraction
fixed_at: 2026-09-27T16:11:42Z
review_path: /Users/robin/Development/Android/DevFestNantes/.planning/phases/03-multi-module-architecture-extraction/03-REVIEW.md
iteration: 1
findings_in_scope: 2
fixed: 2
skipped: 0
status: all_fixed
---

# Phase 03: Code Review Fix Report

**Fixed at:** 2026-09-27T16:11:42Z
**Source review:** /Users/robin/Development/Android/DevFestNantes/.planning/phases/03-multi-module-architecture-extraction/03-REVIEW.md
**Iteration:** 1

**Summary:**
- Findings in scope: 2 (WR-01, WR-02 — Critical/Warning scope; IN-01 excluded, Info out of scope)
- Fixed: 2
- Skipped: 0

**Verification environment:** isolated git worktree at
`.claude/worktrees/rf-03-10907-1790524823` on temp branch `gsd-reviewfix/03-10907`
(created off `feature/reno_phase_3`), per the mandatory review-fix isolation step. All
Gradle builds, tests, detekt, lint, and the Swift-names gate ran there, then the branch
was fast-forwarded onto `feature/reno_phase_3` and pushed to `origin/feature/reno_phase_3`
after each commit (checkpointed individually due to rate-limit risk). Not reproducible
from the main checkout after worktree teardown — re-run `./gradlew ...` from
`feature/reno_phase_3` post-merge if re-verification is needed.

## Fixed Issues

### WR-01: `failOnNoDiscoveredTests` disabled build-wide, not scoped to the stated "leaf modules"

**Files modified:** `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidCommon.kt`, `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidLibraryConventionPlugin.kt`
**Commit:** `0c92ad7`
**Applied fix:** Extracted the `tasks.withType<Test>().configureEach { failOnNoDiscoveredTests.set(false) }` block out of `configureAndroidCommon()` (which is invoked by both the application and library convention plugins) into a new standalone function `configureAndroidLibraryTestDefaults()`, documented with why it exists and why it must stay library-only. Wired this new function into `AndroidLibraryConventionPlugin` only — `AndroidApplicationConventionPlugin` (applied solely by `:androidApp`) no longer disables the safety net, so `:androidApp`'s own unit-test suite will keep Gradle's default fail-on-no-tests discoverability behavior once it exists. Replaced the stale comment (which already described the intended library-only scope but wasn't implemented that way) with the new function's KDoc.
**Verification:** Confirmed via `./gradlew --no-daemon testDebugUnitTest` (BUILD SUCCESSFUL — `:androidApp:testDebugUnitTest NO-SOURCE`, since it has no unit-test source set today, so no regression; all library/feature modules still pass without `failOnNoDiscoveredTests` tripping). Also re-verified as part of the full combined suite below.

### WR-02: Deprecated/no-op Kotlin opt-in compiler flag duplicated across every Android module

**Files modified:** `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidCommon.kt`
**Commit:** `7dbd61d`
**Applied fix:** Removed the dead `-Xopt-in=kotlin.Experimental` compiler flag from `configureAndroidCommon()`'s `freeCompilerArgs`, leaving `-Xopt-in=kotlin.RequiresOptIn` untouched (kept the existing spelling rather than switching to the modern `-opt-in=` form, to minimize risk — the fix suggestion only asked to "re-verify whether it's still needed", not to change it).
**Verification:** `./gradlew :androidApp:kspDebugKotlin --offline --rerun-tasks` (the same command the reviewer used to originally surface the warning) confirmed the `Opt-in requirement marker 'kotlin.Experimental' is unresolved` warning no longer appears, and the build still succeeds. Also confirmed via `./gradlew --no-daemon assembleDebug detekt` (BUILD SUCCESSFUL).

## Combined full-suite verification (after both fixes)

Run once more with both commits applied, per orchestrator instructions (every build-logic
change affects all modules):

- `./gradlew --no-daemon assembleDebug assembleRelease testDebugUnitTest allTests detekt lint` → **BUILD SUCCESSFUL** (1033 actionable tasks, 33 executed + 1000 up-to-date on the second pass; all modules including KMP `core:*` targets, `androidApp` debug+release, and every `feature:*`/`core:*` detekt+lint task passed).
- `bash .planning/phases/03-multi-module-architecture-extraction/swift-names-gate.sh check` → **SWIFT-NAMES-OK**.
- Confirmed `shared/src/commonMain/kotlin/com/gdgnantes/devfest/shared/SharedFrameworkPlaceholder.kt` still exists (untouched).

## Skipped Issues

None — both in-scope findings were fixed.

---

_Fixed: 2026-09-27T16:11:42Z_
_Fixer: Claude (gsd-code-fixer)_
_Iteration: 1_
