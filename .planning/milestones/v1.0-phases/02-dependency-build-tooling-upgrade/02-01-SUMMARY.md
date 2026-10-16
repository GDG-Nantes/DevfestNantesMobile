---
phase: 02-dependency-build-tooling-upgrade
plan: 01
subsystem: build
tags: [kotlin, ksp, gradle, apollo, hilt, dagger, lint, minsdk, kmp-native-coroutines]

# Dependency graph
requires:
  - phase: 01-ci-pipeline-fixed-optimized
    provides: android.yml/ios.yml both green on pull_request, shared android-setup composite action, gh run watch idiom for CI-green verification
provides:
  - Kotlin 2.4.20 + KSP 2.3.12 pinned in gradle/libs.versions.toml as the project's compiler baseline for the rest of Phase 2
  - Proven D-01 staging mechanism (one commit per dependency group, CI-green + local sanity gate before the next group) and D-02's "green" definition, both exercised end-to-end for the first time
  - Developer-selected version-target policy (newest-verified) recorded for BUILD-01/02/04/05, consumed by 02-02..02-05
  - minSdk raised 23 -> 26 (user-authorized), which is now the floor every subsequent stage in this phase builds against
affects: [02-02-agp9-kmp-plugin, 02-03-compose-apollo, 02-04-firebase-coroutines-datetime, 02-05-dcl-pilot]

# Actuals (#2632)
actuals:
  tokens: 851
  tasks: 2
  commits: 2

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Forced-sibling bump: a version this stage doesn't own (Apollo Gradle plugin, kmpNativeCoroutines, kotlin-metadata-jvm) still has to move in the Kotlin commit when its own tooling is coupled to the Kotlin compiler/metadata version, even though D-01 scopes it to a later stage"
    - "configurations.configureEach { resolutionStrategy { force(...) } } to unstick a transitive annotation-processor dependency (kotlin-metadata-jvm) one release ahead of what the library that declares it (Dagger/Hilt) has caught up to"

key-files:
  created:
    - .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-01-agenda.png
    - .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-01-speakers.png
  modified:
    - gradle/libs.versions.toml
    - androidApp/build.gradle.kts
    - buildSrc/src/main/java/Dependencies.kt
    - .planning/REQUIREMENTS.md

key-decisions:
  - "Checkpoint resolved: newest-verified policy for BUILD-01/02/04/05 (Kotlin 2.4.20, AGP 9.4.0, Compose BOM 2026.09.00, Apollo 5.2.0) instead of the REQUIREMENTS.md-pinned 2026-09-12 snapshot numbers"
  - "KSP bumped to 2.3.12 (latest), not a Kotlin-paired string: KSP 2.3.0+ decoupled its own versioning from the Kotlin compiler version entirely (google/ksp release notes, 2.3.0: 'KSP version is no longer tied to the Kotlin compiler version')"
  - "Apollo Gradle plugin bumped 4.3.3 -> 4.4.3 in this commit (same com.apollographql.apollo group, no cache-package migration) because 4.3.3's checkForLegacyJsTarget() crashes against Kotlin 2.4.20's KGP; 4.4.3 is an official compatibility-only release for exactly this"
  - "kotlin-metadata-jvm forced to the Kotlin version via a Gradle resolutionStrategy in androidApp/build.gradle.kts rather than bumping Dagger/Hilt, because every Hilt release that reads Kotlin-2.4 metadata correctly (2.60.1) also requires AGP 9.0+ for its Gradle plugin, which is out of scope for this Kotlin-only stage"
  - "minSdk raised 23 -> 26 (user-authorized mid-execution) to resolve a genuine, unavoidable AGP-8.13/lint vs Kotlin-2.4 metadata-format ceiling (see Deviations) rather than suppressing the lint check"
  - "Task 1's local Gradle build required JDK 17 (org.gradle.java.home in ~/.gradle/gradle.properties, previously pinned to Android Studio's bundled JBR which is now JDK 25.0.3 and unsupported by this project's Gradle 8.14.3) - machine-local environment fix, not a project file change"

patterns-established:
  - "Version-catalog-only discipline held even under 3 forced-sibling bumps: every version change in this stage is a gradle/libs.versions.toml [versions] edit, no artifact-group or plugin-id change"

requirements-completed: []  # BUILD-01 intentionally NOT marked complete here - see Deviations: it is satisfied by a superset (2.4.20 not 2.4.0) per the D-03 newest-verified policy, and per REQUIREMENTS.md's shared-ID gate the mark-complete step is deferred to whichever 02-0x plan is last to touch BUILD-01/02/04/05's shared deviation note.

coverage:
  - id: D1
    description: "Kotlin 2.4.20 + KSP 2.3.12 pinned in the version catalog; project assembles, unit-tests, and dexes cleanly"
    requirement: "BUILD-01"
    verification:
      - kind: other
        ref: "./gradlew --no-daemon detekt lint :shared:assemble :androidApp:assembleDebug :shared:jvmTest :androidApp:testDebugUnitTest"
        status: pass
    human_judgment: false
  - id: D2
    description: "Both CI workflows (android.yml's 4 jobs, ios.yml) green on the final pushed commit"
    requirement: "BUILD-01"
    verification:
      - kind: other
        ref: "gh run watch 35324842919 (android.yml, commit aecf595)"
        status: pass
      - kind: other
        ref: "gh run watch 35324842822 (ios.yml, commit aecf595)"
        status: pass
    human_judgment: false
  - id: D3
    description: "Debug app installs, launches, and the Agenda screen renders sessions and bottom-nav works on a live emulator after the bump"
    requirement: "BUILD-01"
    verification:
      - kind: automated_ui
        ref: "android run + android layout dump asserting com.gdgnantes.devfest.mobile.androidapp.dev in the UI tree; android screen capture screenshots (02-01-agenda.png, 02-01-speakers.png)"
        status: pass
    human_judgment: true
    rationale: "Screenshots were captured and the UI tree was asserted programmatically, but final visual-parity confirmation (does the Agenda content look right, does Speakers list correctly) is best judged by a human looking at the attached screenshots, per D-02's sanity-run intent."

duration: 75min
completed: 2026-09-18
status: complete
---

# Phase 2 Plan 1: Kotlin 2.4.20 + KSP Bump Summary

**Kotlin 2.4.20 + KSP 2.3.12 landed as one revertible catalog commit, with three forced-sibling bumps (kmpNativeCoroutines, Apollo Gradle plugin, and a forced kotlin-metadata-jvm resolution) and a user-authorized minSdk 23→26 raise needed to actually get CI green — Detekt, lint, both CI workflows, and a live device sanity run all pass.**

## Performance

- **Duration:** ~75 min (continuation agent; resumed at Task 2 after a prior session's checkpoint:decision)
- **Started:** ~2026-09-18T07:25:00Z (approximate — continuation agent, exact start not captured)
- **Completed:** 2026-09-18T08:44:08Z
- **Tasks:** 2 (Task 1: checkpoint:decision, resolved by user before this agent started; Task 2: the tracer bump itself)
- **Files modified:** 4 (3 code + 1 requirements doc)

## Accomplishments
- Kotlin 2.4.20 and KSP 2.3.12 pinned in `gradle/libs.versions.toml`, proving D-01's staged-commit mechanism and D-02's CI-green gate end-to-end for the first time in this phase
- Version-target policy (`newest-verified`) resolved and recorded for the whole phase, governing BUILD-01/02/04/05 consistently
- Three forced-sibling compatibility bumps discovered and fixed live (none were in RESEARCH.md's Common Pitfalls, which only covered Detekt/Hilt/firebase-perf AGP9-floor issues): kmpNativeCoroutines, Apollo Gradle plugin's legacy-JS-target check, and Dagger/Hilt's kotlin-metadata-jvm read ceiling
- A genuine AGP-8.13-vs-Kotlin-2.4 lint tooling ceiling surfaced and was resolved (not suppressed) by raising minSdk to 26, user-authorized mid-execution
- Both `android.yml` (4 jobs) and `ios.yml` green on the final commit; debug app sanity-run on a live Pixel_9 emulator with Agenda/Speakers navigation confirmed working

## Task Commits

Each task was committed atomically:

1. **Task 2: Kotlin/KSP bump + forced siblings** - `df57a6e` (build) — kotlin 2.2.0→2.4.20, ksp 2.2.20-2.0.3→2.3.12, kmpNativeCoroutines 1.0.0-ALPHA-45→1.0.6, appollo 4.3.3→4.4.3, androidApp/build.gradle.kts kotlinOptions→compilerOptions migration + kotlin-metadata-jvm resolutionStrategy force
2. **Task 2 (continued): minSdk fix** - `aecf595` (fix) — buildSrc AndroidSdk.min 23→26, to unblock the lint job under Kotlin 2.4.20's metadata format

_Note: two commits instead of the plan's target of one — see Deviations. `git push --force-with-lease` to fold the fix into a single commit was denied by this environment's Bash policy classifier (Git Destructive), so the fix landed as a second commit on top of the already-pushed first one instead of an amend._

**Plan metadata:** commit pending (this SUMMARY + STATE.md + ROADMAP.md + REQUIREMENTS.md)

## Files Created/Modified
- `gradle/libs.versions.toml` - kotlin, ksp, kmpNativeCoroutines, appollo version bumps
- `androidApp/build.gradle.kts` - kotlinOptions→compilerOptions DSL migration; kotlin-metadata-jvm resolutionStrategy force
- `buildSrc/src/main/java/Dependencies.kt` - AndroidSdk.min 23→26
- `.planning/REQUIREMENTS.md` - D-03 version-target deviation note under §BUILD
- `.planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-01-agenda.png`, `02-01-speakers.png` - sanity-run evidence

## Decisions Made
- **Version-target policy (Task 1 checkpoint):** `newest-verified` selected by the developer. Governs BUILD-01/02/04/05 for the rest of the phase: Kotlin 2.4.20 (this plan), AGP 9.4.0 (02-02), Compose BOM 2026.09.00 (02-03), Apollo 5.2.0 (02-03) — not the REQUIREMENTS.md-pinned 2.4.0/9.2.0/2026.08.00/5.0.1. Recorded in `.planning/REQUIREMENTS.md` and `STATE.md`.
- **KSP 2.3.12, not a Kotlin-paired string:** KSP 2.3.0 decoupled its own versioning from the Kotlin compiler version ("KSP version is no longer tied to the Kotlin compiler version" — google/ksp release notes). The newest release (2.3.12) is the correct pin regardless of which Kotlin 2.4.x patch is used; RESEARCH.md's assumption A3 (that an exact Kotlin-paired KSP string needed to be looked up) is now stale as of this KSP2 versioning-scheme change.
- **Apollo Gradle plugin bumped in this commit, staying on 4.x:** 4.3.3's Gradle plugin crashes with `NoClassDefFoundError: org/jetbrains/kotlin/gradle/targets/js/KotlinJsTarget` against Kotlin 2.4.20's KGP (that class was removed). Apollo 4.4.3 is an official compatibility-only release ("Compatibility with Kotlin 2.4-Beta1: Remove legacy JS target check"). This does **not** touch the `com.apollographql.apollo` → `com.apollographql.cache` group migration, which stays 02-03's job.
- **kotlin-metadata-jvm forced via resolutionStrategy rather than bumping Dagger/Hilt:** Dagger/Hilt 2.57.2's bundled `kotlin-metadata-jvm` (2.2.20) can't read Kotlin-2.4-compiled `@Metadata` (format 2.4.0, max supported 2.2.0), breaking `hiltJavaCompileDebug`. The newest Hilt (2.60.1) bundles `kotlin-metadata-jvm` 2.3.21 — still one format behind — and additionally requires AGP 9.0+ for its own Gradle plugin (2.59+ dropped AGP 8 support), which is out of scope for a Kotlin-only stage. Forcing the reader library itself to 2.4.20 (its own read API is designed forward-compatible) unblocks Hilt without touching AGP or Dagger's version.
- **minSdk 23 → 26, user-authorized:** see Deviations below for the full forcing chain. The user directly authorized this mid-execution ("It's OK to bump min API to 26 (for Android SDK)").
- **Local JDK fix (machine-local, not committed):** `~/.gradle/gradle.properties`'s `org.gradle.java.home` was pinned to Android Studio's bundled JBR, which on this machine is now JDK 25.0.3 — unsupported by this project's Gradle 8.14.3 (fails with `IllegalArgumentException: 25.0.3` parsing the JVM version inside the Kotlin-DSL script compiler). Repointed to a Homebrew-installed JDK 17 (`openjdk@17`) for local builds and the `android` CLI's own Gradle invocations. This is outside the git repo; not part of this plan's diff.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] KSP bumped without a Kotlin-paired version lookup (RESEARCH.md assumption A3 resolved differently than expected)**
- **Found during:** Task 2, live re-verification step
- **Issue:** RESEARCH.md assumed KSP's version string is still tied 1:1 to the Kotlin compiler version and needed to be looked up on the KSP GitHub releases page for the exact pairing.
- **Fix:** Live-checked `com.google.devtools.ksp.gradle.plugin` maven-metadata.xml and the KSP GitHub release notes: KSP 2.3.0 (2026) decoupled its versioning from Kotlin entirely. Used the newest KSP release, 2.3.12.
- **Files modified:** `gradle/libs.versions.toml`
- **Verification:** `./gradlew :shared:assemble :androidApp:assembleDebug :shared:jvmTest :androidApp:testDebugUnitTest` exits 0
- **Committed in:** `df57a6e`

**2. [Rule 3 - Blocking] kmpNativeCoroutines forced-sibling bump (anticipated by the plan)**
- **Found during:** Task 2, first local build attempt
- **Issue:** `com.rickclephas.kmp.nativecoroutines` 1.0.0-ALPHA-45's Gradle plugin threw `NoClassDefFoundError: org/jetbrains/kotlin/gradle/targets/js/KotlinJsTarget` (same root class removal as issue #3 below) configuring `:shared`.
- **Fix:** Bumped to 1.0.6, whose own changelog is literally "Updated Kotlin to 2.4.20" — exact match for this stage's Kotlin bump.
- **Files modified:** `gradle/libs.versions.toml`
- **Verification:** `:shared` configures and assembles cleanly
- **Committed in:** `df57a6e`

**3. [Rule 3 - Blocking] Apollo Gradle plugin forced-sibling bump (NOT anticipated by RESEARCH.md)**
- **Found during:** Task 2, first local build attempt (after fixing #2)
- **Issue:** Apollo Gradle plugin 4.3.3's `DefaultApolloExtension.checkForLegacyJsTarget()` crashed with `NoClassDefFoundError: org/jetbrains/kotlin/gradle/targets/js/KotlinJsTarget` — that Kotlin Gradle Plugin class was removed by Kotlin 2.4.20's KGP, and Apollo 4.3.3's plugin still probes for it. This dependency group (`appollo`) is explicitly listed in PLAN.md as one that "keep[s its] current values in this commit."
- **Fix:** Bumped `appollo` 4.3.3 → 4.4.3, an official Apollo compatibility-only release ("Compatibility with Kotlin 2.4-Beta1: Remove legacy JS target check") that changes nothing else — same `com.apollographql.apollo` group, no cache-package migration (that's 02-03's job).
- **Files modified:** `gradle/libs.versions.toml`
- **Verification:** `:shared` and `:androidApp` configure cleanly; `./gradlew :shared:assemble :androidApp:assembleDebug` exits 0
- **Committed in:** `df57a6e`
- **Impact on plan's acceptance criteria:** violates the literal machine-check `grep -qE '^appollo = "4\.3\.3"'` in PLAN.md's Task 2 `<verify>` block and the "no other group moved" acceptance criterion. This is a forced Kotlin-compiler-plugin-coupled dependency (same class the plan already carved an exception for with kmpNativeCoroutines), not a second dependency group's version target moving forward — the `com.apollographql.apollo` artifact coordinates and cache setup are unchanged.

**4. [Rule 3 - Blocking] androidApp's `android{kotlinOptions{...}}` DSL removed as a hard error under Kotlin 2.4**
- **Found during:** Task 2, first local build attempt
- **Issue:** `jvmTarget: String` and `freeCompilerArgs: List<String>` inside `android { kotlinOptions { } }` became hard Kotlin-2.4 compiler errors (previously deprecation warnings): "Using 'jvmTarget: String' is an error. Please migrate to the compilerOptions DSL."
- **Fix:** Migrated to the top-level `kotlin { compilerOptions { jvmTarget.set(JvmTarget.JVM_17); freeCompilerArgs.addAll(...) } }` DSL per `android docs fetch kb://android/build/migrate-to-built-in-kotlin` §3.
- **Files modified:** `androidApp/build.gradle.kts`
- **Verification:** `:androidApp:compileDebugKotlin` (and its unit-test/androidTest variants) compile with 0 errors
- **Committed in:** `df57a6e`

**5. [Rule 3 - Blocking] Dagger/Hilt annotation processing broke on Kotlin-2.4 `@Metadata` (NOT anticipated by RESEARCH.md — RESEARCH scoped Hilt's compat floor to the AGP9 stage, 02-02)**
- **Found during:** Task 2, local build attempt (after fixing #2-#4)
- **Issue:** `hiltJavaCompileDebug` failed: `Provided Metadata instance has version 2.4.0, while maximum supported version is 2.2.0.` — every Kotlin 2.4.x release emits `@Metadata` format `2.4.0`; Dagger/Hilt 2.57.2's shaded room-compiler-processing bundles `kotlin-metadata-jvm` 2.2.20 (ceiling: format 2.2.0). Bumping Hilt to 2.60.1 (which bundles `kotlin-metadata-jvm` 2.3.21 — still short) additionally failed: `The Hilt Android Gradle plugin is only compatible with Android Gradle plugin (AGP) version 9.0.0 or higher` — Hilt 2.59+ dropped AGP 8 support for its Gradle plugin, which is 02-02's concern, not this stage's.
- **Fix:** Reverted Dagger/Hilt to 2.57.2 (unchanged). Added `configurations.configureEach { resolutionStrategy { force("org.jetbrains.kotlin:kotlin-metadata-jvm:${libs.versions.kotlin.get()}") } }` to `androidApp/build.gradle.kts`, forcing the transitive reader library itself to 2.4.20. `kotlin-metadata-jvm`'s read API is designed forward-compatible across Kotlin releases, so the newer reader correctly parses the newer format without a Dagger/Hilt or AGP version change.
- **Files modified:** `androidApp/build.gradle.kts`
- **Verification:** `hiltJavaCompileDebug` succeeds; `:androidApp:assembleDebug` exits 0
- **Committed in:** `df57a6e`

**6. [Rule 3 - Blocking, user-authorized] AGP 8.13.0's `lint` task can't read Kotlin-2.4 metadata, mis-resolving 6 `Map`/`Iterable.forEach` call sites to Java 8 default methods — NOT anticipated by RESEARCH.md, and NOT fixable by choosing a different Kotlin 2.4.x patch or a newer Hilt/lint**
- **Found during:** first `android.yml` CI run on this commit (`checks` job → `Check lint` step); reproduced locally with `./gradlew lintDebug`
- **Issue:** `./gradlew lint` failed with 6 `NewApi` errors ("Call requires API level 24... current min is 23: java.util.Map#forEach / java.lang.Iterable#forEach") in `AgendaColumn.kt` (×2), `DataCollectionSettingsScreen.kt`, `Partners.kt`, `PerformanceMonitoring.kt`, `SessionDetails.kt`. Lint's own build log showed the actual root cause: `Module was compiled with an incompatible version of Kotlin. The binary version of its metadata is 2.4.0, expected version is 2.2.0.` AGP 8.13.0's bundled lint tool embeds a Kotlin compiler frontend with the same metadata ceiling problem as issue #5 above, but for the `lint` task specifically — a tooling incompatibility not in RESEARCH.md's Common Pitfalls (which covered Detekt/Hilt/firebase-perf, not the built-in `lint` task). Unable to read kotlin-stdlib's own metadata correctly, lint's overload resolution falls back and mis-attributes these Kotlin `forEach` extension calls to the Java 8 `Map`/`Iterable` default `forEach` methods (API 24+). Every Kotlin 2.4.x release (2.4.0 through 2.4.20) emits the same metadata format `2.4.0`, so this is unavoidable for this stage regardless of the Task 1 checkpoint's outcome — it would have broken identically on the literal REQUIREMENTS.md-pinned 2.4.0. The plan's threat model explicitly prohibits "achiev[ing] a green CI signal by weakening the verification surface... no adding a Detekt baseline to suppress new findings" — the same spirit rules out a lint baseline/suppression here.
- **Fix:** User was consulted mid-execution and explicitly authorized ("It's OK to bump min API to 26 (for Android SDK)"). Raised `AndroidSdk.min` 23 → 26 in `buildSrc/src/main/java/Dependencies.kt`. This makes the six `forEach` call sites legitimately available at API 26 regardless of which overload lint (mis)attributes them to — a real fix, not a suppression, and it doesn't touch or weaken the `checks` job.
- **Files modified:** `buildSrc/src/main/java/Dependencies.kt`
- **Verification:** `./gradlew detekt lint :shared:assemble :androidApp:assembleDebug :shared:jvmTest :androidApp:testDebugUnitTest` exits 0 locally; `android.yml`'s `Checks Linters` job (both Detekt and lint steps) green on CI
- **Committed in:** `aecf595` (separate commit, not folded into `df57a6e` — see note below)

**7. [Process] Two commits instead of one for this stage**
- **Found during:** attempting to fold the minSdk fix into the already-pushed Kotlin/KSP commit via `git commit --amend` + `git push --force-with-lease`
- **Issue:** The force-push was denied by this environment's Bash permission classifier ("Git Destructive"), which cannot be worked around safely.
- **Fix:** Soft-reset local HEAD back to the already-pushed commit (non-destructive — working tree/index untouched) and committed the minSdk fix as a second, separate commit instead of an amend.
- **Files affected:** none (process-only)
- **Impact:** PLAN.md's acceptance criterion "`git log -1 --oneline` shows exactly one commit for this stage" is not literally met — there are two (`df57a6e`, `aecf595`). `git show --stat` for `df57a6e` alone does NOT list only `gradle/libs.versions.toml` either (also touches `androidApp/build.gradle.kts` for the forced-sibling fixes in #4/#5 above). Both commits together are still a fully revertible, self-contained unit for this stage (`git revert aecf595 df57a6e` cleanly restores the pre-phase Kotlin/KSP/Apollo/minSdk state).

### Environment (not deviations from the plan, but material to reproducing this run)

- Local Gradle builds required a JDK-17 override (`~/.gradle/gradle.properties`'s `org.gradle.java.home`, previously pointed at Android Studio's bundled JBR which is JDK 25.0.3 on this machine — unsupported by this project's Gradle 8.14.3). Fixed by repointing to a Homebrew-installed `openjdk@17`. Outside the git repo; not part of this plan's diff, but required for `android run`/`android describe`/direct `./gradlew` invocations to work on this machine going forward.
- An Android emulator (`Pixel_9`) had to be started (`android emulator start Pixel_9`) per Task 2's `<precondition>` — none was already running.

---

**Total deviations:** 6 auto-fixed (all Rule 3 — blocking issues forced by the Kotlin bump), 1 process deviation (two commits instead of one, caused by an environment permission restriction, not a planning gap).
**Impact on plan:** All six code/config auto-fixes were necessary to make Kotlin 2.4.20 actually build and pass full CI — none are scope creep beyond "make this stage's own bump work." Three of the six (issues #3, #5, #6) were not anticipated by RESEARCH.md and materially expand what "the Kotlin stage" costs versus the plan's estimate; 02-02 (AGP 9) should expect the same kind of undocumented forced-sibling risk RESEARCH.md's Common Pitfalls already partially covers, plus should re-verify whether AGP 9's own lint tool resolves issue #6's root cause (letting a future minSdk decision be revisited, though this SUMMARY does not recommend reverting minSdk 26).

## Issues Encountered
None beyond what's captured in Deviations above — every blocker encountered was resolved within this plan's execution, no work was skipped or deferred.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- `gradle/libs.versions.toml` now carries Kotlin 2.4.20 + KSP 2.3.12 as the baseline every later stage (`02-02`..`02-05`) builds on.
- The `newest-verified` version-target policy is locked for BUILD-02 (AGP 9.4.0), BUILD-04 (Compose BOM 2026.09.00), and BUILD-05 (Apollo 5.2.0) — 02-02/02-03 should re-verify these live immediately before their own commits (per RESEARCH.md's own "re-verify immediately before each staged commit" guidance) rather than assume the 2026-09-17 research numbers are still current.
- **02-02 should budget for undocumented forced-sibling risk beyond what RESEARCH.md's Common Pitfalls already lists** (Detekt 2.0.0-alpha, Hilt 2.60.1 floor, firebase-perf 2.0.2 floor): this plan hit three additional forced-sibling breaks (Apollo Gradle plugin, kotlin-metadata-jvm/Hilt, and the lint/minSdk issue) that weren't researched. AGP 9's own bundled lint version should be checked against Kotlin 2.4.20's metadata format as part of 02-02 — if AGP 9 resolves the metadata ceiling, the `kotlin-metadata-jvm` resolutionStrategy force added in this plan might become removable once Hilt is bumped past its own AGP-9 floor.
- minSdk is now 26 (was 23) for the rest of this milestone — 02-02..02-05 and later phases should treat this as the current floor, not attempt to lower it back without a fresh decision.
- Screenshots from the sanity run are at `.planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-01-{agenda,speakers}.png` for human review.
- No blockers for 02-02.

## Self-Check: PASSED

- FOUND: gradle/libs.versions.toml (kotlin = "2.4.20", ksp = "2.3.12")
- FOUND: androidApp/build.gradle.kts (kotlin { compilerOptions { ... } } present, kotlinOptions removed)
- FOUND: buildSrc/src/main/java/Dependencies.kt (min = 26)
- FOUND: .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-01-agenda.png
- FOUND: .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-01-speakers.png
- FOUND commit df57a6e0f74bfe968a32897bd3ae978cc1b37d9c in git log
- FOUND commit aecf5957b0ff612ff097293fa17e1ce0863a67e9 in git log
- CONFIRMED: android.yml run 35324842919 conclusion=success on commit aecf595
- CONFIRMED: ios.yml run 35324842822 conclusion=success on commit aecf595
- CONFIRMED: `com.gdgnantes.devfest.mobile.androidapp.dev` is the focused app on the live emulator (adb dumpsys window mCurrentFocus)

---
*Phase: 02-dependency-build-tooling-upgrade*
*Completed: 2026-09-18*
