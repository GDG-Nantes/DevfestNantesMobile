---
phase: 02-dependency-build-tooling-upgrade
plan: 02
subsystem: build
tags: [agp, gradle, kmp, detekt, hilt, dagger, firebase-perf, android-kotlin-multiplatform-library]

# Dependency graph
requires:
  - phase: 02-dependency-build-tooling-upgrade
    plan: 01
    provides: Kotlin 2.4.20 + KSP 2.3.12 baseline, newest-verified version-target policy, minSdk 26 floor
provides:
  - shared migrated to com.android.kotlin.multiplatform.library on AGP 9.4.0, replacing the kotlin.multiplatform + com.android.library coexistence AGP 9 forbids
  - Gradle 9.7.1 (checksum-verified distribution) as the project's build-tool baseline
  - Detekt 2.0.0-alpha.6 under the dev.detekt group/plugin-id, Dagger Hilt 2.60.1, Firebase Performance plugin 2.0.2 (AGP-9 compatibility floors)
  - androidApp packaging DSL migrated off the brace-expansion packagingOptions pattern
affects: [02-03-compose-apollo, 02-04-firebase-coroutines-datetime, 02-05-dcl-pilot]

# Actuals (#2632)
actuals:
  tokens: 2042
  tasks: 3
  commits: 1

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "AGP 9 built-in Kotlin: applying org.jetbrains.kotlin.android alongside AGP 9's built-in Kotlin support is a hard conflict, not a deprecation warning — the plugin alias must be removed entirely wherever AGP 9's new DSL is active, not just deprioritized"
    - "Forced-sibling AGP-9-compatibility bump: same D-01/02-01 pattern repeats for Detekt/Hilt/firebase-perf — one commit, all four version moves together, because scoping to BUILD-02's named packages alone breaks detekt/lint/ksp for reasons that look unrelated to the plugin swap"

key-files:
  created:
    - .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-02-agenda.png
    - .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-02-speakers.png
    - .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-02-speaker-detail.png
    - .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-02-venue.png
    - .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-02-agenda-bookmarked.png
    - .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-02-bookmarks-filtered.png
  modified:
    - gradle/wrapper/gradle-wrapper.properties
    - gradle/libs.versions.toml
    - build.gradle.kts
    - shared/build.gradle.kts
    - androidApp/build.gradle.kts
    - linters/detekt-config.yml
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/services/DataCollectionSettingsService.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/services/SessionFiltersService.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/agenda/AgendaViewModel.kt
    - shared/src/commonMain/kotlin/com/gdgnantes/devfest/model/SocialItem.kt
    - shared/src/commonMain/kotlin/com/gdgnantes/devfest/model/stubs/StoreStubs.kt
    - shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/BookmarksStore.kt

key-decisions:
  - "D-03 commit fold: Gradle 9.7.1 + AGP 9.4.0 landed in one commit instead of Task 1's planned standalone wrapper commit, because Gradle 9.7.1 does not build under AGP 8.13.0 (removed internal API org.gradle.api.problems.internal.InternalProblems) — the two-commit separation was forced closed rather than silently pinning a lower Gradle"
  - "Detekt pinned to 2.0.0-alpha.6 under the dev.detekt group/plugin-id, approved via the blocking-human package-legitimacy checkpoint after live verification against plugins.gradle.org and the detekt/detekt GitHub releases page confirmed it is the official org's own pre-1.0 next-major line with the closest compat baseline (Kotlin 2.4.10/Gradle 9.6.1/AGP 9.3.1) to this stage's targets"

patterns-established:
  - "Version-catalog-only discipline held under four forced-sibling AGP-9-floor bumps (detekt, dagger, firebasePerfPlugin, plus the new KMP-library plugin alias) — every version change is a gradle/libs.versions.toml [versions]/[plugins] edit"

requirements-completed: [BUILD-02, BUILD-03]

coverage:
  - id: D1
    description: "shared migrates to com.android.kotlin.multiplatform.library on AGP 9.4.0; Gradle 9.7.1 folded into the same commit per D-03; project assembles, unit-tests, lints and Detekts cleanly (11 real findings pre-fix, 0 post-fix)"
    requirement: "BUILD-02"
    verification:
      - kind: other
        ref: "./gradlew detekt lint :shared:assemble :androidApp:assembleDebug :androidApp:assembleRelease :shared:jvmTest :androidApp:testDebugUnitTest"
        status: pass
    human_judgment: false
  - id: D2
    description: "Gradle wrapper reports 9.7.1"
    requirement: "BUILD-03"
    verification:
      - kind: other
        ref: "./gradlew --version reports Gradle 9.7.1 (verified pre-close-out, not re-run in this close-out session)"
        status: pass
    human_judgment: false
  - id: D3
    description: "Both CI workflows (android.yml's 4 jobs, ios.yml) green on the pushed AGP-9 commit c56d6ab"
    requirement: "BUILD-02"
    verification:
      - kind: other
        ref: "gh run watch 35328488816 (android.yml, commit c56d6ab, 4/4 jobs)"
        status: pass
      - kind: other
        ref: "gh run watch 35328488808 (ios.yml, commit c56d6ab)"
        status: pass
    human_judgment: false
  - id: D4
    description: "D-04 device smoke pass on Pixel_9 emulator: Agenda, Speakers, Venue and Bookmarks (via the Favori filter chip) all render and navigate correctly after the plugin swap"
    verification:
      - kind: automated_ui
        ref: "android screen capture screenshots: 02-02-agenda.png, 02-02-speakers.png, 02-02-speaker-detail.png, 02-02-venue.png, 02-02-agenda-bookmarked.png, 02-02-bookmarks-filtered.png"
        status: pass
    human_judgment: true
    rationale: "Screenshots were captured for all four D-04 screens plus the bookmark-and-filter flow, but final visual-parity confirmation (does each screen genuinely look and behave right, not just render without crashing) is best judged by a human looking at the attached screenshots, per D-02's sanity-run intent — same rationale as 02-01's D3."

duration: 43min
completed: 2026-09-18
status: complete
---

# Phase 2 Plan 2: AGP 9 KMP-Library Plugin Migration Summary

**shared migrated to com.android.kotlin.multiplatform.library on AGP 9.4.0 with Gradle 9.7.1 folded into the same commit (forced by a Gradle/AGP-8.13 incompatibility), plus the four AGP-9-forced sibling bumps (Detekt 2.0.0-alpha.6, Dagger Hilt 2.60.1, Firebase Performance plugin 2.0.2, and removing the now-conflicting kotlin-android plugin) — all landed in one revertible commit, green on all four Android CI jobs plus iOS, and smoke-passed on a live device across Agenda, Speakers, Venue and Bookmarks.**

## Performance

- **Duration:** ~43 min (implementation work completed by a prior session; this close-out session covers documentation and metadata only)
- **Started:** ~2026-09-18T10:43:00Z (approximate, from screenshot capture window)
- **Completed:** 2026-09-18T11:26:44Z
- **Tasks:** 3 (Task 1: Gradle wrapper bump — folded into Task 2's commit per D-03; the package-legitimacy checkpoint — resolved before this close-out; Task 2: the AGP 9 KMP-library plugin swap plus forced siblings)
- **Files modified:** 16

## Accomplishments
- `shared` builds as a KMP Android library through `com.android.kotlin.multiplatform.library` on AGP 9.4.0, with the `kotlin.multiplatform` + `com.android.library` coexistence AGP 9 forbids fully removed (BUILD-02)
- Gradle wrapper bumped to 9.7.1 with a checksum-verified distribution (BUILD-03), folded into the same commit as the AGP move per a D-03 forced closure
- Detekt bumped to the checkpoint-approved `dev.detekt` 2.0.0-alpha.6, Dagger Hilt to 2.60.1, and the Firebase Performance Gradle plugin to 2.0.2 — all three AGP-9-documented compatibility floors, forced siblings not optional cleanup
- `androidApp`'s resource packaging migrated off the brace-expansion `packagingOptions` pattern onto individual `packaging { resources { excludes.add(...) } }` entries
- Both `android.yml` (4/4 jobs) and `ios.yml` green on the pushed commit; D-04 device smoke pass confirmed Agenda, Speakers, Venue and Bookmarks all render and navigate correctly on a live `Pixel_9` emulator

## Task Commits

Each task was committed atomically:

1. **Task 1 + Task 2 (folded per D-03): Gradle 9.7.1 + AGP 9 KMP-library plugin swap + forced siblings** - `c56d6ab` (build) — Gradle wrapper 8.14.3→9.7.1, AGP 8.13.0→9.4.0, `shared/build.gradle.kts` migrated to `com.android.kotlin.multiplatform.library`, `androidApp` packaging DSL migration, Detekt 1.23.8→2.0.0-alpha.6 (`dev.detekt` group), Dagger Hilt 2.57.2→2.60.1, Firebase Performance plugin 2.0.1→2.0.2, `org.jetbrains.kotlin.android` plugin removed from `androidApp`

**Plan metadata:** commit pending (this SUMMARY + STATE.md + ROADMAP.md + REQUIREMENTS.md)

_Note: this is the only production commit for this plan. Task 1's planned standalone Gradle wrapper commit was folded into Task 2's commit per D-03 — see Deviations._

## Files Created/Modified
- `gradle/wrapper/gradle-wrapper.properties` (+ `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`) - Gradle wrapper bumped to 9.7.1 with `distributionSha256Sum`
- `gradle/libs.versions.toml` - `agp=9.4.0`, `detekt=2.0.0-alpha.6`, `dagger=2.60.1`, `firebasePerfPlugin=2.0.2`, new `android-kotlin-multiplatform-library` plugin alias, `detekt-fomatting` library repointed to `dev.detekt:detekt-rules-ktlint-wrapper`
- `build.gradle.kts` (root) - AGP library plugin alias replaced by the new KMP-library plugin alias
- `shared/build.gradle.kts` - migrated to `com.android.kotlin.multiplatform.library`, `kotlin { android { ... } }` DSL replacing the top-level `android { }` block
- `androidApp/build.gradle.kts` - `org.jetbrains.kotlin.android` plugin removed, Detekt imports → `dev.detekt.gradle.*`, `packagingOptions{}` → `packaging{ resources{...} }`
- `linters/detekt-config.yml` - property renames for Detekt 2.0's ktlint-wrapper ruleset (`allowedFunctionParameters`/`allowedConstructorParameters`/`allowedLines`, `formatting`→`ktlint`)
- 6 source files (`MainActivity.kt`, `DataCollectionSettingsService.kt`, `SessionFiltersService.kt`, `AgendaViewModel.kt`, `SocialItem.kt`, `StoreStubs.kt`, `BookmarksStore.kt`) - mechanical style fixes for 11 genuine `BlankLineBeforeDeclaration`/`ArgumentListWrapping` findings the now-working ktlint-wrapper ruleset newly flagged
- `.planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-02-{agenda,speakers,speaker-detail,venue,agenda-bookmarked,bookmarks-filtered}.png` - D-04 smoke-pass evidence

## Decisions Made
- **D-03 commit fold (Gradle + AGP in one commit):** Gradle 9.7.1 does not build under AGP 8.13.0 (fails on a removed internal API, `org.gradle.api.problems.internal.InternalProblems`), so the planned standalone wrapper commit could not stand alone without breaking the build in between. Folded into the single AGP-9 commit per D-03's escape valve; recorded here as the deviation obligation D-03 pairs with the escape valve.
- **Detekt 2.0.0-alpha.6 approved (package-legitimacy checkpoint, resolved before this close-out):** confirmed via `plugins.gradle.org/plugin/dev.detekt` and the `detekt/detekt` GitHub releases page that it is the official detekt org's own pre-1.0 next-major line, not a supply-chain risk, and that alpha.6 has the closest compatibility baseline (Kotlin 2.4.10/Gradle 9.6.1/AGP 9.3.1) to this stage's targets among the available alphas.

## Deviations from Plan

### Auto-fixed Issues

**1. [D-03 escape valve] Gradle 9.7.1 + AGP 9.4.0 folded into one commit instead of two**
- **Found during:** Task 1, first local build attempt on Gradle 9.7.1 under the still-unmoved AGP 8.13.0
- **Issue:** PLAN.md's Task 1 required Gradle 9.7.1 to land as its own standalone, revertible commit with a green CI gate before AGP moved. Gradle 9.7.1 does not build the project on AGP 8.13.0 — the build fails on a removed Gradle internal API (`org.gradle.api.problems.internal.InternalProblems`) that AGP 8.13.0's tooling still references.
- **Fix:** Applied the D-03 escape valve named explicitly in PLAN.md's Task 1 action: folded the Gradle wrapper bump into Task 2's AGP commit so Gradle and AGP move together, recording the deviation and forcing error here rather than silently pinning a lower Gradle.
- **Files modified:** `gradle/wrapper/gradle-wrapper.properties`, `gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar` (committed alongside the AGP-9 files in the same commit)
- **Verification:** `./gradlew detekt lint :shared:assemble :androidApp:assembleDebug :androidApp:assembleRelease :shared:jvmTest :androidApp:testDebugUnitTest` exits 0 on Gradle 9.7.1 + AGP 9.4.0 together
- **Committed in:** `c56d6ab`

**2. [Rule 3 - Blocking, not anticipated by PLAN.md or RESEARCH.md] AGP 9's built-in Kotlin conflicts with the still-applied `org.jetbrains.kotlin.android` plugin**
- **Found during:** Task 2, first local build attempt after the KMP-library plugin swap
- **Issue:** AGP 9's built-in Kotlin support conflicts with `org.jetbrains.kotlin.android` still being applied in `androidApp/build.gradle.kts` (the only consumer of that plugin in the repo). The build failed with AGP's own error instructing removal, per official migrate-to-built-in-Kotlin guidance.
- **Fix:** Removed `alias(libs.plugins.kotlin.android)` from `androidApp/build.gradle.kts`'s `plugins{}` block, the corresponding `apply false` alias from the root `build.gradle.kts`, and the now-unused `kotlin-android` entry from `gradle/libs.versions.toml [plugins]`.
- **Files modified:** `androidApp/build.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`
- **Verification:** `:androidApp:assembleDebug` and `:androidApp:assembleRelease` compile and configure with no plugin-conflict error
- **Committed in:** `c56d6ab`

**3. [Rule 1/3 auto-fix, corrects PLAN.md's assumption] `detekt-formatting` does not exist under the `dev.detekt` Maven group**
- **Found during:** Task 2, catalog update for the Detekt group/plugin-id rename
- **Issue:** PLAN.md's action instructed repointing `detekt-fomatting`'s module group to `dev.detekt` while keeping the artifact name `detekt-formatting`. That coordinate returns a live 404 on Maven Central — confirmed by diffing Detekt's own `settings.gradle.kts` between the `v1.23.8` tag and `main`, which shows the artifact was renamed, not just re-grouped.
- **Fix:** Repointed the `detekt-fomatting` catalog entry (key name's existing misspelling preserved, per PLAN.md's explicit out-of-scope note) to `dev.detekt:detekt-rules-ktlint-wrapper`.
- **Files modified:** `gradle/libs.versions.toml`
- **Verification:** `./gradlew detekt` resolves the dependency and runs; 11 real findings surfaced pre-fix (proof the ktlint-wrapper ruleset is genuinely active, not a silent no-op), 0 after the 11 source fixes
- **Committed in:** `c56d6ab`

**4. [Package-legitimacy checkpoint, already resolved before this close-out] Detekt 2.0.0-alpha.6 approved for pinning**
- **Found during:** the `blocking-human` package-legitimacy gate task, before Task 2's catalog changes were made
- **Issue:** 02-RESEARCH.md's `[SUS]` verdict flagged pinning CI's static-analysis step to a pre-1.0 `dev.detekt` release as a build-stability concern requiring explicit human sign-off before the pin landed.
- **Fix:** User approved pinning to `2.0.0-alpha.6` after live verification confirmed alpha.3+ removes the need for AGP-9-new-DSL opt-out flags, and alpha.6 has the closest compatibility baseline (Kotlin 2.4.10/Gradle 9.6.1/AGP 9.3.1) to this stage's targets among the available alpha releases.
- **Files modified:** `gradle/libs.versions.toml` (the pin itself, applied in Task 2)
- **Verification:** N/A — human decision, not a code check
- **Committed in:** `c56d6ab` (the approved version is what's pinned)

---

**Total deviations:** 4 (1 D-03 commit-fold escape valve, 2 Rule-3 blocking auto-fixes not anticipated by PLAN.md/RESEARCH.md, 1 already-resolved package-legitimacy checkpoint recorded here for the first time)
**Impact on plan:** All auto-fixes were necessary for the AGP-9 migration to actually build and pass full CI and Detekt verification. No scope creep beyond making this stage's own plugin swap work — consistent with 02-01's pattern of undocumented forced-sibling risk that 02-01's own SUMMARY predicted 02-02 would hit.

## Issues Encountered
None beyond what's captured in Deviations above — every blocker encountered was resolved within this plan's execution, no work was skipped or deferred.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- `shared` now builds as a KMP Android library through `com.android.kotlin.multiplatform.library` on AGP 9.4.0 / Gradle 9.7.1 — the baseline 02-03 (Compose/Apollo), 02-04 (Firebase/coroutines/datetime) and 02-05 (DCL pilot) build on.
- AGP 9's bundled `lint` tool resolves the Kotlin-2.4-metadata ceiling that forced `minSdk` 23→26 in 02-01 (lint ran clean on this commit with no metadata-format errors) — worth confirming explicitly in a future stage before considering any minSdk-related revisit, though this SUMMARY does not recommend lowering it back.
- Apollo in the catalog is `4.4.3`, not PLAN.md's literal `4.3.3` — a pre-existing divergence from 02-01 (forced by a Kotlin-2.4.20/KGP incompatibility in Apollo 4.3.3's Gradle plugin), already documented in `02-01-SUMMARY.md`, not introduced or touched by this plan.
- Bookmarks has no dedicated bottom-nav tab — it is implemented via the "Favori" filter chip in the Agenda filter drawer. The D-04 smoke pass exercised this path directly (bookmarked a session, applied the filter, confirmed the list narrowed with the green checkmark badge) rather than assuming a separate screen exists.
- No blockers for 02-03.

## Self-Check: PASSED

- FOUND commit c56d6ab34632fa238770473e7ea9fa4e5190fa47 in git log
- FOUND: .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-02-agenda.png
- FOUND: .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-02-speakers.png
- FOUND: .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-02-speaker-detail.png
- FOUND: .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-02-venue.png
- FOUND: .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-02-agenda-bookmarked.png
- FOUND: .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-02-bookmarks-filtered.png

---
*Phase: 02-dependency-build-tooling-upgrade*
*Completed: 2026-09-18*
