---
phase: 03-multi-module-architecture-extraction
plan: 01
subsystem: architecture
tags: [gradle, kmp, build-logic, convention-plugins, kotlin-native, ios-interop, apollo]

# Dependency graph
requires:
  - phase: 02-dependency-build-tooling-upgrade
    provides: "Gradle 9.7.1, AGP 9.4.0 + com.android.kotlin.multiplatform.library, Kotlin 2.4.20, Detekt 2.0.0-alpha.6 (dev.detekt), settings.gradle.dcl DCL pilot"
provides:
  - "build-logic included build with devfest.detekt + devfest.kmp.library convention plugins (D-13)"
  - ":core:model KMP leaf module, extracted and re-exported through :shared (D-10/D-11), Android-side green"
  - "swift-names-gate.sh baseline/check gate script, reused by later plans in this phase"
  - "Discovery: Kotlin/Native's ObjC/Swift collision-disambiguation is alphabetical-FQN-based and flips identity (not just count) on package rename — a gap in this plan's own gate script"
affects: [03-02, 03-03, 03-04, 03-05, 03-06, 03-07, 03-08, 03-09]

# Actuals (#2632)
actuals:
  tokens: 19027
  tasks: 2
  commits: 4

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "build-logic included build (NIA-style), convention plugins registered via gradlePlugin { plugins { register(...) } }"
    - "KmpLibraryConventionPlugin uses the official AGP-documented reactive pattern (targets.withType(KotlinMultiplatformAndroidLibraryTarget::class.java).configureEach) instead of a top-level android {} DSL block, required inside a precompiled Plugin<Project>"
    - "D-16 two-commit-per-module pattern: pure git mv (packages unchanged) then package rename + import updates"

key-files:
  created:
    - build-logic/settings.gradle.kts
    - build-logic/convention/build.gradle.kts
    - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidSdk.kt
    - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/ProjectExtensions.kt
    - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/DetektConventionPlugin.kt
    - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/KmpLibraryConventionPlugin.kt
    - core/model/build.gradle.kts
    - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/** (21 files, moved + repackaged)
    - .planning/phases/03-multi-module-architecture-extraction/swift-names-gate.sh
    - .planning/phases/03-multi-module-architecture-extraction/03-shared-h-baseline.txt
    - .planning/phases/03-multi-module-architecture-extraction/03-swift-referenced-names.txt
  modified:
    - settings.gradle.dcl
    - build.gradle.kts
    - gradle/libs.versions.toml
    - shared/build.gradle.kts
    - shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/**
    - .github/workflows/ios.yml
    - androidApp/src/main/java/** (import updates only)

key-decisions:
  - "D-20 resolved live: includeBuild(\"build-logic\") inside settings.gradle.dcl's pluginManagement parses and resolves correctly on Gradle 9.7.1 stable — no DCL fallback needed."
  - "build-logic/settings.gradle.kts and every new module build.gradle.kts stay plain Kotlin DSL (D-20/RESEARCH Pitfall 2) — confirmed still correct, nothing changed since Phase 2's assessment."
  - "KmpLibraryConventionPlugin configures the Android-KMP target via the official reactive targets.withType(KotlinMultiplatformAndroidLibraryTarget::class.java).configureEach pattern (from Google's own 'Build custom Gradle plugins for Android KMP' doc), not the kotlin { android { } } DSL block form used in leaf .gradle.kts files — that block-form DSL is only available inside a Kotlin-DSL *script*, not inside a compiled Plugin<Project> class."
  - "HALT at Task 2's CI gate (D-09 abort signal): repackaging :core:model's domain model classes (Venue, Session, Speaker, Room, Partner) into com.gdgnantes.devfest.core.model flips which of two same-simple-named Kotlin classes (the domain model vs. the pre-existing, colliding Apollo-generated GraphQL response class of the same simple name) keeps the clean Swift name vs. gets Kotlin/Native's collision-disambiguation _ suffix — see Deviations below for full diagnosis."

requirements-completed: []

coverage:
  - id: D1
    description: "build-logic included build exposes devfest.detekt and devfest.kmp.library, reading the root gradle/libs.versions.toml"
    requirement: "ARCH-01"
    verification:
      - kind: integration
        ref: "./gradlew projects (lists Included build ':build-logic' and Project ':core:model')"
        status: pass
    human_judgment: false
  - id: D2
    description: ":core:model exists, applies only devfest.kmp.library, holds every domain model class + stubs + SessionExtensions.kt, repackaged to com.gdgnantes.devfest.core.model"
    requirement: "ARCH-02"
    verification:
      - kind: unit
        ref: "core/model/src/commonTest/kotlin/com/gdgnantes/devfest/core/model/ScheduleSlotDateParsingTest.kt (:core:model:jvmTest, 8 tests pass)"
        status: pass
      - kind: integration
        ref: "./gradlew :core:model:jvmTest :core:model:compileKotlinIosSimulatorArm64 :shared:jvmTest :androidApp:assembleDebug :androidApp:assembleRelease detekt lint"
        status: pass
    human_judgment: false
  - id: D3
    description: ":shared re-exports :core:model to iOS with unchanged umbrella framework identity (baseName, isStatic, Xcode build phase)"
    requirement: "ARCH-04"
    verification:
      - kind: other
        ref: "swift-names-gate.sh check -> SWIFT-NAMES-OK (name-existence + collision-count gate)"
        status: pass
    human_judgment: true
    rationale: "The gate that passed (name-existence + collision-COUNT) does not detect a same-count IDENTITY swap between two colliding names — which is exactly what broke iOS CI. This deliverable is NOT actually achieved: iOS CI failed on Swift compilation across ~10 files. See Deviations / Known Stubs below."
  - id: D4
    description: "iOS simulator smoke run (agenda/speakers/venue/about) — D-12 checkpoint 1"
    human_judgment: true
    verification: []
    rationale: "Never reached. Task 3 (the checkpoint) requires Task 2's CI-GREEN-BOTH gate, which failed. Per the plan's own explicit instruction, iOS CI failure on Swift compilation is the D-09 abort signal: stop, do not continue."

# Metrics
duration: 95min
completed: 2026-09-23
status: halted
---

# Phase 3 Plan 1: Tracer — build-logic + core/model extraction Summary

**build-logic + devfest.kmp.library convention plugin and :core:model extraction land clean on Android/Kotlin (all local + Android CI green), but the package rename flips a pre-existing Kotlin/Native Swift-name collision for 5 domain-model types, breaking iOS CI — HALTED per D-09 for a human decision before any other module moves.**

## Performance

- **Duration:** 95 min
- **Started:** 2026-09-23T08:48:00Z (approx)
- **Completed:** 2026-09-23T10:23:25Z
- **Tasks:** 2 of 3 (Task 3's checkpoint never reached)
- **Files modified:** 77 (excluding `.planning/`)

## Accomplishments

- `build-logic/` included build wired via `includeBuild("build-logic")` in `settings.gradle.dcl`'s `pluginManagement` — confirmed parseable on Gradle 9.7.1 stable (D-20 re-check, no DCL fallback needed).
- `devfest.detekt` and `devfest.kmp.library` convention plugins registered and working; `AndroidSdk` constants and `Project.moduleNamespace()` helper live in `build-logic`.
- `:core:model` extracted as the first KMP leaf module: 21 files pure-moved (D-16 commit 1), then repackaged to `com.gdgnantes.devfest.core.model` (D-16 commit 2) — both commits pass locally (`:core:model:jvmTest`, `:core:model:compileKotlinIosSimulatorArm64`, `:shared:jvmTest`, `:androidApp:assembleDebug`, `:androidApp:assembleRelease`, `detekt`, `lint` all green).
- `:shared` umbrella wired with `api(project(":core:model"))` + `export(project(":core:model"))` per iOS target, `baseName = "shared"` / `isStatic = true` / the Xcode `embedAndSignAppleFrameworkForXcode` build phase all unchanged (D-10).
- `swift-names-gate.sh` (baseline/check modes) built and proven — correctly reports `SWIFT-NAMES-OK` for its own two checks (no missing referenced name, collision count unchanged), and its informational `comm -3` diff is what first surfaced the identity-swap finding below.
- `.github/workflows/ios.yml` cache keys widened to `core/**`.
- Draft PR #419 opened (`feature/reno_phase_3` -> `main`). **Android CI green** (run 35847334085: Build debug / Checks Linters / Unit tests all pass). **iOS CI failed** (run 35847334261) on Swift compilation.
- `:core:model:tasks --group verification` task names recorded: `jvmTest`, `iosSimulatorArm64Test`, `iosX64Test`, `allTests` (plus per-source-set detekt tasks).

## Task Commits

Each task was committed atomically:

1. **Task 1: Tracer — build-logic + core/model pure move + umbrella export** — two commits:
   - `08e5db9` — build-logic included build with devfest.detekt + devfest.kmp.library (feat)
   - `eab93df` — extract :core:model as first KMP leaf, re-export through :shared (feat, pure `git mv`, packages unchanged)
2. **Task 2: Repackage core/model to com.gdgnantes.devfest.core.model** — two commits:
   - `fb59408` — repackage :core:model to com.gdgnantes.devfest.core.model (feat) — **note:** this commit's `git add` failed atomically on a stale pathspec and landed only the rename, no content
   - `3d602ba` — fix(03-01): land the repackage content the prior commit's git add silently dropped (fix) — the actual package/import content change, same intended scope as commit `fb59408` was supposed to carry

_Note: commit `fb59408`/`3d602ba` together form the single logical "D-16 commit 2" the plan describes as one step; they are split across two commits only because of an executor tooling mistake (documented under Deviations), not a design choice._

**No plan-metadata commit yet** — this SUMMARY and the STATE.md blocker note are committed separately after this file, per the halted-plan close-out.

## Files Created/Modified

- `build-logic/settings.gradle.kts`, `build-logic/convention/build.gradle.kts` — included build + convention module wiring
- `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/{AndroidSdk,ProjectExtensions,DetektConventionPlugin,KmpLibraryConventionPlugin}.kt` — convention plugin implementations
- `core/model/build.gradle.kts`, `core/model/src/commonMain/**`, `core/model/src/commonTest/**` — the extracted, repackaged KMP leaf module
- `shared/build.gradle.kts` — `api`/`export` wiring to `:core:model`
- `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/**`, `shared/src/commonTest/**`, `shared/src/jvmTest/**` — import updates only
- `androidApp/src/main/java/**` — import updates only
- `.github/workflows/ios.yml` — widened cache `hashFiles()` patterns
- `.planning/phases/03-multi-module-architecture-extraction/swift-names-gate.sh`, `03-shared-h-baseline.txt`, `03-swift-referenced-names.txt` — the gate script and its baseline snapshots

## Decisions Made

- D-20 (Declarative Gradle re-check): `includeBuild("build-logic")` works on stable Gradle 9.7.1 inside `.dcl`; `build-logic`'s own settings and every new module's build file stay Kotlin DSL. See STATE.md "Phase 02 DCL pilot outcome" § "Phase 03 re-check" for the full record.
- KmpLibraryConventionPlugin configures the Android-KMP target via Google's documented reactive `targets.withType(KotlinMultiplatformAndroidLibraryTarget::class.java).configureEach { }` pattern rather than the `kotlin { android { } }` block-DSL form — the block form is a script-only convenience the AGP plugin adds to `KotlinMultiplatformExtension`; a precompiled `Plugin<Project>` must use the reactive/typed-target API instead (confirmed against `kb://android/kotlin/multiplatform/kmp-integration`).

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Convention plugin Kotlin DSL compile errors (Property `=` assign, Action/receiver-lambda ambiguity)**
- **Found during:** Task 1 (`:build-logic:convention:compileKotlin`)
- **Issue:** `.gradle.kts`-idiomatic syntax (`buildUponDefaultConfig = true`, `.configureEach { androidTarget -> ... }`, named-parameter lambdas for `Action<T>`-typed Gradle APIs) does not compile inside a plain `.kt` file compiled by `kotlin-dsl` — those conveniences come from Gradle Kotlin DSL's script-only implicit imports (`org.gradle.kotlin.dsl.assign`) and Gradle's Action-as-receiver-lambda special-casing, neither of which extends to regular compiled classes.
- **Fix:** Switched `Property<T>` assignments to explicit `.set(...)`, and switched all `Action<T>`-typed Gradle APIs (`configureEach`, `KotlinMultiplatformAndroidLibraryTarget` configuration) to implicit-receiver lambdas with no named parameter (`this` in scope) instead of `{ x -> ... }`.
- **Files modified:** `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/{DetektConventionPlugin,KmpLibraryConventionPlugin}.kt`
- **Verification:** `./gradlew :build-logic:convention:compileKotlin` → BUILD SUCCESSFUL
- **Committed in:** `08e5db9`

**2. [Rule 1 - Bug] `git add` with a stale (already-`rmdir`'d) multi-pathspec argument silently dropped the whole repackage commit's content**
- **Found during:** Task 2, right after committing `fb59408`
- **Issue:** `git add <path-that-no-longer-exists> <other-valid-paths>` fails atomically — git stages NOTHING when any pathspec in the same invocation doesn't match, but only prints one `fatal:` line, easy to miss in a large multi-file batch. `git status --short` after the failed `add` still showed the same big file list (mix of `RM`/`M` from the *prior* `git mv`), which I misread as "everything is staged" without cross-checking `git diff --cached --stat`.
- **Fix:** Verified with `git diff --stat -- '*.kt'` that the working tree still had 65 files of package/import content changes after the "repackage" commit; staged and committed them in a follow-up commit (`3d602ba`) rather than amending, per the project's git-safety policy of always creating new commits.
- **Files modified:** none beyond what was already intended for D-16 commit 2 — this only fixed the *staging*, not the content.
- **Verification:** `git show HEAD -- '*.kt' | grep -E '^[-+]' | grep -vE '^(\+\+\+|---)' | grep -vE '^[-+](package |import )'` → empty (content-scope still package/import-only); `git grep 'package com.gdgnantes.devfest.model'` → clean.
- **Committed in:** `3d602ba`

**3. [Rule 1 - Bug] ktlint `ImportOrdering` violation in `GraphQLStore.kt` / `Mappers.kt` after the repackage sed pass**
- **Found during:** Task 2 detekt run
- **Issue:** The mechanical `import com.gdgnantes.devfest.model.X` → `import com.gdgnantes.devfest.core.model.X` substitution changed each import's lexicographic sort key without re-sorting the import block, so `core.model` imports landed after `graphql`/`domain` imports that now sort later.
- **Fix:** Manually reordered the two files' import blocks to restore lexicographic order (verified against every other touched file with a small ordering-check script — no other file was affected).
- **Files modified:** `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/{GraphQLStore,Mappers}.kt`
- **Verification:** `./gradlew detekt` → BUILD SUCCESSFUL (both files' `ImportOrdering` errors gone)
- **Committed in:** `fb59408`/`3d602ba` (same scope as the repackage)

---

**Total deviations:** 3 auto-fixed (1 blocking Kotlin-DSL syntax, 1 blocking staging bug, 1 blocking lint violation). **Impact on plan:** All three were necessary corrections with no scope creep — none touched behavior, only build tooling correctness and git bookkeeping.

## Known Stubs / Unresolved Gate Failure

**Not a stub — a genuine, diagnosed CI failure that halted the plan (D-09 abort signal).**

**What broke:** iOS CI (`gh run 35847334261`, PR #419) failed at the "Build iOS App for Simulator" step with ~30 Swift compile errors across 10 files (`VenueContent.swift`, `AgendaContent.swift`, `AgendaViewModel.swift`, `AgendaView.swift`, `AgendaCellView.swift`, `AgendaDetailView.swift`, `SpeakerDetailsViewModel.swift`, `SpeakersViewModel.swift`, `SpeakerDetailsView.swift`, `SpeakerView.swift`, `AboutViewModel.swift`), all of the shape `value of type 'X_' has no member '<domain-model-property>'` or `cannot assign value of type 'X' to type 'X_'`.

**Root cause (fully diagnosed, reproduced locally):** this codebase already has 5 pre-existing simple-name collisions between a `core:model` domain class and an Apollo-generated GraphQL response class of the identical simple name:

| Domain model | Colliding GraphQL type |
|---|---|
| `Venue` | `com.gdgnantes.devfest.graphql.GetVenueQuery.Venue` |
| `Session` | `com.gdgnantes.devfest.graphql.GetSessionQuery.Session` |
| `Speaker` | `com.gdgnantes.devfest.graphql.GetSpeakersQuery.Speaker` (+ `fragment.SessionDetails.Speaker`) |
| `Room` | `com.gdgnantes.devfest.graphql.GetRoomsQuery.Room` (+ `fragment.SessionDetails.Room`) |
| `Partner` | `com.gdgnantes.devfest.graphql.GetPartnerGroupsQuery.Partner` |

Kotlin/Native's Objective-C header generator resolves same-simple-name collisions across the whole compiled framework with what empirically behaves as an **alphabetical fully-qualified-name tie-break**: whichever FQN sorts first keeps the clean (unprefixed) Swift name; the other gets a trailing `_`. At baseline, `com.gdgnantes.devfest.model.Venue` (`"model"`) sorts *after* `com.gdgnantes.devfest.graphql.GetVenueQuery.Venue` (`"graphql"`) — so the domain model was **already** `Venue_` in Swift, and the hand-written iOS code already correctly references `Venue_` as the domain type (confirmed: `Venue_`/`Session_`/`Speaker_`/`Room_`/`Partner_` were all present in the pre-phase Swift-referenced-names baseline captured by `swift-names-gate.sh baseline`, run on untouched HEAD before any change in this plan). After repackaging `:core:model` to `com.gdgnantes.devfest.core.model` (`"core.model"` sorts *before* `"graphql"`), the domain model now claims the **clean** `Venue` name and the GraphQL type flips to `Venue_` — silently swapping the *identity* behind both names while leaving the *set* of names and the *count* of `_`-suffixed names unchanged.

**Why the gate didn't catch it:** `swift-names-gate.sh` (built in Task 1, this plan) checks (a) every Swift-referenced name is still present, and (b) the total count of `_`-suffixed (collision-disambiguated) names is unchanged. Both held here — `Venue`/`Venue_` both still exist, and the total collision count is identical — because this is an **identity swap**, not a name appearing/disappearing. The gate has no way to know that "the meaning of `Venue_` changed" without inspecting each colliding type's *member signature*, which the current script does not do.

**Why this is a HALT, not an auto-fix (per D-09):** the direct fix (updating ~10 Swift files to reference the un-suffixed name reflecting the swap) is outside Task 2's declared `<files>` scope (no `iosApp/**` files listed — the plan's own text asserts "Swift sources need no change," an assumption this collision breaks), and more importantly this is **systemic**: the same identity-flip risk exists for every one of the 5 colliding names and will resurface for any future package move that changes their relative alphabetical order (e.g. `core:network`/`core:data` in 03-02/03-03, which own the GraphQL/Apollo code). A one-file reactive fix does not address the pattern; it needs an explicit decision on strategy (see below) before the phase continues extracting the remaining 8 modules.

**Options for the human/next planning session (not decided by this executor):**
1. **Rename the Apollo-generated response types away from the domain-model names** (e.g. via the Apollo Gradle plugin's `mapScalar`/custom naming, or Apollo's own "type name suffix" config) so the collision never occurs — most durable, but touches `core:network`'s future Apollo config (D-11/D-15 territory for a later plan) and is a larger-than-this-plan change.
2. **Reactively fix Swift call sites per collision as they break**, accepting this pattern will keep recurring through the phase — cheapest per-incident, but repeats the "discovered only by CI failure" pattern for every future collision-affecting move (Session/Speaker/Room/Partner are all still latent risks for 03-02+).
3. **Strengthen `swift-names-gate.sh`** to diff each colliding type's *member set* (not just name existence + count) between baseline and current, so a future identity swap is caught before push, not after a ~5-minute CI round-trip — worth doing regardless of which of 1/2 is chosen, since this repo has 5 known collisions that remain latent.
4. **Revisit whether `Venue`/`Session`/`Speaker`/`Room`/`Partner` should be the domain models' names at all**, given they already collide with GraphQL-generated types today (pre-existing, not introduced by this phase) — a naming-convention decision independent of the module split.

None of these were applied; iosApp/**/*.swift is unchanged from `main`.

## Issues Encountered

See "Known Stubs / Unresolved Gate Failure" above — this is the phase's single Issue Encountered and the reason for the halt.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

**NOT ready to continue to 03-02.** Per D-09 ("if this tracer fails, stop and rethink — nothing else moves"), Phase 3 is blocked on this plan until a human decides among the options above (or another approach) for the Swift-name collision-flip risk. The Kotlin/Android/Gradle side of the tracer (build-logic, :core:model extraction, umbrella `export()` wiring) is fully proven and green — the blocker is specifically the pre-existing Venue/Session/Speaker/Room/Partner name collisions interacting with Kotlin/Native's alphabetical disambiguation, not the umbrella-export mechanism itself (D-11's `api`/`export()` mechanics work exactly as RESEARCH.md's Pattern 3 documented).

**PR #419** (draft, `feature/reno_phase_3` -> `main`) is open with Android CI green, iOS CI red — left open for the next session to continue from.

**CI run URLs:**
- Android: https://github.com/GDG-Nantes/DevfestNantesMobile/actions/runs/35847334085 (success)
- iOS: https://github.com/GDG-Nantes/DevfestNantesMobile/actions/runs/35847334261 (failure — Swift compilation)
- PR: https://github.com/GDG-Nantes/DevfestNantesMobile/pull/419

---
*Phase: 03-multi-module-architecture-extraction*
*Completed: 2026-09-23 (halted)*
