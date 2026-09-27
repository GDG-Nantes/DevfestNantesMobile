---
phase: 03-multi-module-architecture-extraction
plan: 09
subsystem: build-system
tags: [gradle, kotlin-multiplatform, convention-plugins, kotlin-native, ci]

# Dependency graph
requires:
  - phase: 03-multi-module-architecture-extraction
    provides: "03-01..03-08: build-logic convention plugins, all six core:* and six feature:* modules extracted, DCL settings"
provides:
  - "Source-less :shared umbrella framework (D-10/D-11) exporting exactly core:model/core:data/core:analytics"
  - ":androidApp with no :shared dependency, wired directly to the core/feature modules it uses"
  - "buildSrc fully removed (D-13); AndroidSdk lives only in build-logic"
  - "Proven 14-project unidirectional module graph (ARCH-02), zero duplicated leaf build config (ARCH-01)"
  - "Green CI on both android.yml and ios.yml for the phase's final HEAD"
affects: [phase-04-dependency-injection, phase-05-testing]

# Actuals (#2632)
actuals:
  tokens: 1327
  tasks: 2
  commits: 4

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Thin umbrella KMP module: :shared carries zero real sources (one internal marker file to avoid Kotlin/Native NO-SOURCE skip), only api()+export() re-exports of the core modules that should reach Swift"
    - "SDK levels, detekt, and target configuration live exclusively in build-logic convention plugins — no leaf build.gradle.kts repeats them"

key-files:
  created: []
  modified:
    - shared/build.gradle.kts
    - androidApp/build.gradle.kts
    - buildSrc/.gitignore (deleted)
    - buildSrc/build.gradle.kts (deleted)
    - buildSrc/settings.gradle.kts (deleted)
    - buildSrc/src/main/java/Dependencies.kt (deleted)
    - gradle.properties

key-decisions:
  - "Gradle daemon heap raised 2048M -> 4096M to fix a real Android CI OutOfMemoryError in D8 dex merging (Rule 3 - Blocking, user-approved 2026-09-27), Kotlin daemon heap left unchanged"
  - "Kept SharedFrameworkPlaceholder.kt (D-10 amendment from 03-03) as :shared's sole source file rather than achieving a literally zero-file source set, because Kotlin/Native's compile+link tasks treat an empty source set as NO-SOURCE and skip the framework link entirely"

patterns-established:
  - "Verify-script quoting for Gradle dependency-report diffs must be quote-aware (project ':shared' vs an unquoted exclusion silently under-matches) — documented as a script-bug deviation, not a plan defect"

requirements-completed: [ARCH-01, ARCH-02, ARCH-03, ARCH-04]

coverage:
  - id: D1
    description: ":shared thinned to a pure umbrella — zero own sources except the D-10 placeholder marker, framework baseName \"shared\", isStatic = true, exports exactly core:model/core:data/core:analytics for every iOS target, no core:network export, no transitive export, iosApp/Xcode project and Swift sources untouched"
    requirement: "ARCH-04"
    verification:
      - kind: other
        ref: "bash .planning/phases/03-multi-module-architecture-extraction/swift-names-gate.sh check -> SWIFT-NAMES-OK"
        status: pass
      - kind: other
        ref: "git diff origin/main --stat -- iosApp/ (empty output, confirmed against 930f287 baseline)"
        status: pass
    human_judgment: false
  - id: D2
    description: ":androidApp no longer depends on :shared; release runtime classpath diff vs pre-change baseline shows only the removed project ':shared' entry"
    requirement: "ARCH-01"
    verification:
      - kind: other
        ref: "./gradlew :androidApp:dependencies --configuration releaseRuntimeClasspath, quote-aware normalized diff vs p3-09-deps-before.txt"
        status: pass
    human_judgment: false
  - id: D3
    description: "buildSrc deleted entirely; no build file references the old AndroidSdk object, SDK levels come only from build-logic"
    requirement: "ARCH-01"
    verification:
      - kind: other
        ref: "git ls-files buildSrc (empty); git grep -n 'AndroidSdk\\.' -- '*.gradle.kts' (empty)"
        status: pass
    human_judgment: false
  - id: D4
    description: "Module graph proven unidirectional: exactly 14 Gradle projects, no core:* -> feature:* edge, no feature:* -> feature:* edge"
    requirement: "ARCH-02"
    verification:
      - kind: other
        ref: "GRAPH-OK gate (per-module ./gradlew :<m>:dependencies scan across all 5 core + 7 non-shared/androidApp feature/ui modules, project count == 14)"
        status: pass
    human_judgment: false
  - id: D5
    description: "No leaf build file duplicates shared config (compileSdk/minSdk/targetSdk/compileOptions/jvmTarget/detekt); no Koin references; no feature references NavController; DCL settings shape unchanged"
    requirement: "ARCH-01"
    verification:
      - kind: other
        ref: "HYGIENE-OK gate"
        status: pass
    human_judgment: false
  - id: D6
    description: "Full suite green: clean build, detekt, lint, assembleDebug/Release, all module jvmTest suites (incl. GraphQLStoreJvmTest, DevFestNantesStoreContractTest, ScheduleSlotDateParsingTest, FakeStoresTest, 0 failures), iOS framework link, Swift names gate, resources gate"
    verification:
      - kind: unit
        ref: "core/data/build/test-results/jvmTest#GraphQLStoreJvmTest, DevFestNantesStoreContractTest"
        status: pass
      - kind: unit
        ref: "core/model/build/test-results/jvmTest#ScheduleSlotDateParsingTest"
        status: pass
      - kind: unit
        ref: "core/testing/build/test-results/jvmTest#FakeStoresTest"
        status: pass
      - kind: other
        ref: "bash .../resources-gate.sh -> RESOURCES-OK"
        status: pass
    human_judgment: false
  - id: D7
    description: "Final pushed HEAD green on both CI workflows"
    requirement: "ARCH-02"
    verification:
      - kind: e2e
        ref: "android.yml run https://github.com/GDG-Nantes/DevfestNantesMobile/actions/runs/36328538946 (all 4 jobs pass incl. Instrumentation tests (34))"
        status: pass
      - kind: e2e
        ref: "ios.yml run https://github.com/GDG-Nantes/DevfestNantesMobile/actions/runs/36328538965"
        status: pass
    human_judgment: false

# Metrics
duration: ~18h wall-clock across two sessions (paused mid-flight for CI OOM investigation and user approval); active execution ~90 min
completed: 2026-09-27
status: complete
---

# Phase 03 Plan 09: Thin :shared umbrella + delete buildSrc + phase gates Summary

**`:shared` reduced to a source-less umbrella exporting exactly core:model/core:data/core:analytics; `:androidApp` wired directly to its core/feature deps; `buildSrc` deleted; all phase-level graph/hygiene/suite/CI gates green on the final HEAD.**

## Performance

- **Duration:** ~18h wall-clock (includes a multi-day pause for Android CI OOM investigation and a user-approved fix), ~90 min active execution
- **Started:** 2026-09-26T20:34:06Z (930f287, plan base)
- **Completed:** 2026-09-27T15:09:47Z (5cb0389)
- **Tasks:** 2
- **Files modified:** 8 (7 code/config + STATE.md)

## Accomplishments
- Rewrote `shared/build.gradle.kts` to a pure umbrella: `id("devfest.kmp.library")` only, `binaries.framework { baseName = "shared"; isStatic = true; export(:core:model, :core:data, :core:analytics) }` per Kotlin/Native target, `api()` deps mirroring the exports, D-10 `SharedFrameworkPlaceholder.kt` kept as the sole source file
- Removed `androidApp`'s dependency on `:shared`; it now depends directly on `:core:model` (plus the already-present `:core:data`/`:core:analytics`/`:core:ui`/six feature modules)
- Deleted `buildSrc/` entirely (D-13) — all SDK-level config now comes from build-logic's `com.gdgnantes.devfest.buildlogic.AndroidSdk`
- Proved the full module graph invariant: exactly 14 Gradle projects, no `core:* -> feature:*` edge, no `feature:* -> feature:*` edge (GRAPH-OK), no leaf build file duplicates shared config (HYGIENE-OK)
- Ran the full local suite (clean build, detekt, lint, all unit tests, iOS framework link, Swift-names + resources gates) and, after fixing a real CI OOM, confirmed both `android.yml` and `ios.yml` green on the final pushed HEAD

## Task Commits

Each task was committed atomically:

1. **Task 1: Thin :shared to a pure umbrella, drop :androidApp -> :shared, delete buildSrc** - `1bf81ae` (refactor), `2b03d4f` (build)
2. **Task 2: Phase gates — graph direction, no duplicated config, full suite, final CI** - verification-only task; the CI-OOM blocker discovered during this task's Gate C was resolved by `5cb0389` (fix), with `371e122` (docs) recording the mid-execution halt for the resume session

**Plan metadata:** commit to follow this SUMMARY

_Note: this plan is `type="auto"` (not TDD); no RED/GREEN/REFACTOR cycle applies._

## Files Created/Modified
- `shared/build.gradle.kts` - Thinned to `devfest.kmp.library` + framework export block, no android/detekt/ksp/native-coroutines blocks
- `androidApp/build.gradle.kts` - Dropped `implementation(project(":shared"))`, added explicit `:core:model` dependency
- `buildSrc/.gitignore`, `buildSrc/build.gradle.kts`, `buildSrc/settings.gradle.kts`, `buildSrc/src/main/java/Dependencies.kt` - Deleted (D-13); `AndroidSdk` object now lives only in build-logic
- `gradle.properties` - `org.gradle.jvmargs` heap raised `-Xmx2048M` -> `-Xmx4096M` to fix D8 dex-merging OOM in CI
- `.planning/STATE.md` - OOM blocker marked RESOLVED with root cause and fix commit

## Decisions Made
- Gradle daemon heap doubled to 4 GB (Rule 3 - Blocking, user-approved) to fix a genuine Android CI OutOfMemoryError in D8's `DexMergingWorkAction` while assembling three feature modules' androidTest APKs concurrently — not a regression from this plan's diff (confirmed: the touched files have nothing to do with dexing/test-APK assembly), but CI runner resource contention exposed by the daemon's fixed 2 GB ceiling.
- Kept the D-10 `SharedFrameworkPlaceholder.kt` marker file rather than pursuing a literally empty `shared/src` — Kotlin/Native's compile+link tasks treat a zero-file source set as NO-SOURCE and skip the framework link step entirely, which would silently break the iOS build.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Verify-script quoting bug in the release-classpath diff**
- **Found during:** Task 1 (release runtime classpath diff)
- **Issue:** The plan's literal diff script excluded `project :shared` lines using an unquoted pattern; Gradle's actual dependency-report output quotes the project path (`project ':shared'`), so the unquoted exclusion under-matched and would have falsely flagged the expected removal as an unexplained diff.
- **Fix:** Used a quote-aware version of the exclusion pattern when running the diff.
- **Files modified:** none (verification-only fix, no production file touched)
- **Verification:** Diff output showed only the expected `project ':shared'` removal, no other changes
- **Committed in:** 1bf81ae (part of Task 1 commit; the diff itself is not persisted as a script file)

**2. [Rule 3 - Blocking] Gate A grep does not exclude Gradle's auto-synthesized aggregator parent projects**
- **Found during:** Task 2 (GRAPH-OK gate)
- **Issue:** Gradle auto-creates parent aggregator projects `:core` and `:feature` for any project declared as `:core:xxx`/`:feature:xxx`; the plan's literal grep for exactly 14 named leaf projects does not distinguish these auto-synthesized parents from real modules, so a naive project count could over- or under-count depending on how the grep pattern is scoped.
- **Fix:** Scoped the project-count check to the 14 real leaf modules explicitly (matching the plan's own enumerated list), excluding the two Gradle-synthesized aggregator parents.
- **Files modified:** none (verification-only)
- **Verification:** `./gradlew projects` confirmed exactly 14 real leaf projects plus the two expected aggregator parents; GRAPH-OK printed
- **Committed in:** N/A (verification-only; no code change required)

**3. [Rule 3 - Blocking] Gradle daemon heap raised to fix CI OutOfMemoryError**
- **Found during:** Task 2 (Gate C, CI-GREEN-BOTH)
- **Issue:** Android CI's "Instrumentation tests (34)" job failed 4 times (3 before the halt, 1 more after resume) with `java.lang.OutOfMemoryError: Java heap space` in D8's `DexMergingWorkAction` while assembling `feature:settings`/`feature:session-detail`/`feature:speakers` androidTest APKs. Unit tests, debug build, and lint all passed on every attempt — isolating the failure to D8's dex-merging step sharing the Gradle daemon's 2 GB heap.
- **Fix:** Raised `org.gradle.jvmargs` from `-Xmx2048M` to `-Xmx4096M` in `gradle.properties`. Kotlin daemon heap left unchanged. Change approved explicitly by the user mid-execution (2026-09-27), given the architectural-adjacent nature of a CI resource-limit change.
- **Files modified:** `gradle.properties`
- **Verification:** Both `android.yml` (run 36328538946, all 4 jobs green incl. Instrumentation tests) and `ios.yml` (run 36328538965) green on HEAD `5cb0389`
- **Committed in:** 5cb0389

---

**Total deviations:** 3 auto-fixed (2 Rule 3 verification-script/gate scoping fixes, 1 Rule 3 blocking CI-resource fix)
**Impact on plan:** All three were necessary to reach a truthful, passing verification of the plan's own acceptance criteria — no scope creep, no change to the umbrella framework's exported API surface or the module graph shape.

## Issues Encountered
- Android CI's Instrumentation-tests job OOM'd 3 times in a row on the pre-fix HEAD (`2b03d4f`), causing an intentional mid-execution halt (`371e122`) so the user could authorize the daemon-heap change before it was applied. Resolved by `5cb0389`; both workflows confirmed green afterward. See "Deviations" #3 above and the STATE.md blocker (now marked RESOLVED) for full detail.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Phase 03 (Multi-Module Architecture Extraction) is now fully complete: all 14 Gradle projects exist with a proven unidirectional dependency graph, `:shared` is a thin umbrella, `buildSrc` is gone, and both CI workflows are green on the final HEAD.
- ARCH-01, ARCH-02, ARCH-03, ARCH-04 are ready to mark complete via the shared-ID gate (`requirements.ready-ids`) now that this plan's SUMMARY exists — this was the last sibling plan in the phase declaring these IDs.
- Phase 04 (Dependency Injection) and Phase 05 (Testing) can build on the now-stable 14-module graph without further structural changes expected.
- No blockers carried forward from this plan.

---
*Phase: 03-multi-module-architecture-extraction*
*Completed: 2026-09-27*

## Self-Check: PASSED

- FOUND: shared/build.gradle.kts
- buildSrc removed (git ls-files buildSrc empty; residual untracked local Gradle/IDE artifacts only)
- FOUND commits: 1bf81ae, 2b03d4f, 371e122, 5cb0389, 6b80a4e (all present in `git log --oneline --all`)
- Working tree clean after SUMMARY commit
