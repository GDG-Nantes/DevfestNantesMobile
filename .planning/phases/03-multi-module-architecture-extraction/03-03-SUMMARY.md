---
phase: 03-multi-module-architecture-extraction
plan: 03
subsystem: build-system
tags: [kotlin-multiplatform, gradle, kmp-native-coroutines, hilt, apollo-graphql, ios-framework]

# Dependency graph
requires:
  - phase: 03-multi-module-architecture-extraction (plan 02)
    provides: ":core:network (Apollo, implementation-only) and :core:analytics (exported), build-logic convention plugins"
provides:
  - ":core:data module (com.gdgnantes.devfest.core.data / .graphql / .domain) holding DevFestNantesStore, DevFestNantesStoreBuilder, DevFestNantesStoreMocked, BookmarksStore, BookmarksStoreImpl (androidMain), GraphQLStore, Mappers, RoomSortIndex"
  - "core/data exported from :shared (api + export()) for all three iOS targets alongside :core:model and :core:analytics"
  - "androidApp/build.gradle.kts wired to implementation(project(\":core:data\"))"
  - "D-10 amendment: SharedFrameworkPlaceholder.kt as the one permanent exception to :shared's zero-Kotlin-sources rule"
affects: ["03-04", "03-05", "03-06", "03-07", "03-08", "03-09", "03-10", "03-11"]

# Actuals (#2632) — pairs with the plan's `estimate` to calibrate future estimates.
actuals:
  tokens: 18263
  tasks: 3
  commits: 4

tech-stack:
  added: []
  patterns:
    - "Two-commit-per-module pattern (D-16) continued: pure git mv (packages unchanged) then a repackage-only commit touching only package/import lines"
    - "Kotlin/Native NO-SOURCE guard: when a Gradle Kotlin/Native source set has zero source files, compileKotlinIos*/linkDebugFramework*/linkReleaseFramework* report NO-SOURCE and are skipped entirely, producing no framework output at all — an internal marker source file with no exported declarations is the minimal fix"

key-files:
  created:
    - core/data/build.gradle.kts
    - core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/DevFestNantesStore.kt
    - core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/DevFestNantesStoreBuilder.kt
    - core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/DevFestNantesStoreMocked.kt
    - core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/BookmarksStore.kt
    - core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/graphql/GraphQLStore.kt
    - core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/graphql/Mappers.kt
    - core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/domain/RoomSortIndex.kt
    - core/data/src/androidMain/kotlin/com/gdgnantes/devfest/core/data/BookmarksStoreImpl.kt
    - core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/DevFestNantesStoreContractTest.kt
    - core/data/src/jvmTest/kotlin/com/gdgnantes/devfest/core/data/graphql/GraphQLStoreJvmTest.kt
    - shared/src/commonMain/kotlin/com/gdgnantes/devfest/shared/SharedFrameworkPlaceholder.kt
  modified:
    - settings.gradle.dcl
    - shared/build.gradle.kts
    - androidApp/build.gradle.kts
    - gradle/libs.versions.toml
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/injection/AppModule.kt
    - .planning/phases/03-multi-module-architecture-extraction/03-CONTEXT.md
    - .planning/phases/03-multi-module-architecture-extraction/03-09-PLAN.md

key-decisions:
  - "SharedFrameworkPlaceholder.kt added as a permanent, user-approved exception to :shared's zero-Kotlin-sources goal (D-10 amendment) — Kotlin/Native's compile+link tasks report NO-SOURCE and are skipped on an empty source set, which would silently produce no shared.framework for iOS at all"
  - "03-09-PLAN.md's must-haves/acceptance-criteria for :shared updated in lockstep so the phase's final plan expects exactly one source file (the placeholder), not zero, and explicitly forbids deleting it"

requirements-completed: []  # ARCH-02/ARCH-04 span the whole phase; requirements.ready-ids reports both still blocked by sibling plans 03-04..03-09/03-11 (not yet summarized) — NOT marked complete here, per phase convention

coverage:
  - id: D1
    description: ":core:data module extracted (pure move + repackage to com.gdgnantes.devfest.core.data/.graphql/.domain), depends on :core:network (implementation) and :core:model (api), no :feature:* dependency"
    requirement: "ARCH-02"
    verification:
      - kind: unit
        ref: "core/data/build/test-results/jvmTest/TEST-com.gdgnantes.devfest.core.data.graphql.GraphQLStoreJvmTest.xml — 12 tests, 0 failures"
        status: pass
      - kind: unit
        ref: "core/data/build/test-results/jvmTest/TEST-com.gdgnantes.devfest.core.data.DevFestNantesStoreContractTest.xml — 10 tests, 0 failures"
        status: pass
    human_judgment: false
  - id: D2
    description: ":core:data exported from :shared (api + export()) so Swift keeps compiling against unchanged names; KMP-NativeCoroutines wrappers on DevFestNantesStore/BookmarksStore still generate"
    requirement: "ARCH-04"
    verification:
      - kind: other
        ref: "bash .planning/phases/03-multi-module-architecture-extraction/swift-names-gate.sh check -> SWIFT-NAMES-OK"
        status: pass
    human_judgment: false
  - id: D3
    description: "BookmarksStoreImpl SharedPreferences key (selected_sessions) byte-identical to origin/main after the move + repackage"
    requirement: "ARCH-02"
    verification:
      - kind: other
        ref: "diff of string literals origin/main BookmarksStoreImpl.kt vs core/data/.../BookmarksStoreImpl.kt -> PREFS-KEYS-OK (no diff lines)"
        status: pass
    human_judgment: false
  - id: D4
    description: "Both CI workflows (android.yml, ios.yml) green on the pushed HEAD carrying the :core:data extraction"
    requirement: "ARCH-02"
    verification:
      - kind: e2e
        ref: "gh run watch — Android run 36013381479, iOS run 36013381341, both on commit a76fce08ae4ef789463f7efe8214c51b21dea08c (PR #419)"
        status: pass
    human_judgment: false
  - id: D5
    description: "iOS simulator smoke (D-12 checkpoint 2) and Android smoke (D-18 checkpoint 1: all tabs, session detail, speaker detail, settings) both function correctly with the moved store layer"
    verification: []
    human_judgment: true
    rationale: "Visual/interactive UX verification across two platforms — no automated test asserts full-app tab navigation + bookmark persistence + no user-visible behavior change; requires human judgment. User approved 2026-09-24."

duration: 165min
completed: 2026-09-24
status: complete
---

# Phase 3 Plan 3: Extract :core:data Summary

**Extracted the KMP store layer (`DevFestNantesStore`, `GraphQLStore`, `BookmarksStore`/`BookmarksStoreImpl`) into `:core:data`, exported it to Swift, and discovered + fixed a Kotlin/Native NO-SOURCE regression that would have silently broken the iOS framework once `:shared` held zero Kotlin sources.**

## Performance

- **Duration:** 165 min (approx., across the original session and this continuation)
- **Started:** 2026-09-24 (original executor)
- **Completed:** 2026-09-24T[continuation close-out]
- **Tasks:** 3 (2 `auto` + 1 `checkpoint:human-verify`)
- **Files modified:** 24 (excluding `.planning/`)

## Accomplishments

- `:core:data` module created (D-14) with `DevFestNantesStore`, `DevFestNantesStoreBuilder`, `DevFestNantesStoreMocked`, `BookmarksStore` in commonMain, `BookmarksStoreImpl` in androidMain, `GraphQLStore`/`Mappers` and `RoomSortIndex` — all moved in two commits per module (pure move, then repackage to `com.gdgnantes.devfest.core.data` / `.graphql` / `.domain`, D-15/D-16)
- `:shared` exports `:core:data` (api + export()) for all three iOS targets alongside `:core:model`/`:core:analytics`, keeping KMP-NativeCoroutines Swift wrappers on `DevFestNantesStore`/`BookmarksStore` generating correctly
- `androidApp/build.gradle.kts` now depends directly on `:core:data`; `AppModule` (still in `:androidApp`, D-02) updated to import from the new package
- `GraphQLStoreJvmTest` (12 tests) and `DevFestNantesStoreContractTest` (10 tests) both pass from their new home in `:core:data`, 0 failures
- Both CI workflows (android.yml, ios.yml) verified green on the pushed HEAD
- iOS simulator smoke (D-12 checkpoint 2) and Android smoke (D-18 checkpoint 1) both approved by the user

## Task Commits

Each task was committed atomically:

1. **Task 1: Extract :core:data — pure move (packages unchanged)** - `94c760e` (feat)
2. **Task 1: Extract :core:data — repackage to com.gdgnantes.devfest.core.data (D-15)** - `a01ad23` (feat)
3. **[Deviation] Fix :shared NO-SOURCE framework-link skip** - `a76fce0` (fix)
4. **Task 2: Push, CI gate, Android launch sanity** - no commit (verify-only task)
5. **Task 3: D-12 checkpoint 2 + D-18 checkpoint 1 smoke** - no commit (checkpoint task; user approval only)

**Plan metadata (this close-out):**
- `d9ae67d` (docs) — D-10 amendment recorded in 03-CONTEXT.md, 03-09-PLAN.md must-haves aligned

_Note: `plan_head_before` (ledger base): `d312410cbfd83b82c4349d26e4e45b15f42d7c41`_

## Files Created/Modified

- `core/data/build.gradle.kts` — new leaf module: `devfest.kmp.library` + KSP + `kmp.native.coroutines`, `api(project(":core:model"))`, `implementation(project(":core:network"))`
- `core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/*` — store contract, builder, mocked store, bookmarks interface (moved + repackaged)
- `core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/graphql/*` — `GraphQLStore`, `Mappers` (moved + repackaged)
- `core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/domain/RoomSortIndex.kt` — `sortIndex` extension on the Apollo-generated `RoomDetails` fragment (moved + repackaged)
- `core/data/src/androidMain/kotlin/com/gdgnantes/devfest/core/data/BookmarksStoreImpl.kt` — SharedPreferences-backed impl (moved from `androidApp/.../services/`, D-03/D-06; `selected_sessions` key verified byte-identical)
- `core/data/src/commonTest/kotlin/.../DevFestNantesStoreContractTest.kt`, `core/data/src/jvmTest/kotlin/.../GraphQLStoreJvmTest.kt` — moved tests, both green
- `shared/build.gradle.kts` — added `api`/`export()` of `:core:data`
- `shared/src/commonMain/kotlin/com/gdgnantes/devfest/shared/SharedFrameworkPlaceholder.kt` — new internal marker (deviation, see below)
- `androidApp/build.gradle.kts` — added `implementation(project(":core:data"))`
- `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/injection/AppModule.kt` and 6 ViewModels — import-only updates to the new `com.gdgnantes.devfest.core.data` package
- `settings.gradle.dcl` — `include(":core:data")`
- `gradle/libs.versions.toml` — `androidx-core-ktx` alias added (for `androidx.core.content.edit` in the moved `BookmarksStoreImpl`)
- `.planning/phases/03-multi-module-architecture-extraction/03-CONTEXT.md`, `03-09-PLAN.md` — D-10 amendment + aligned must-haves (this close-out)

## Decisions Made

- **D-10 amendment (permanent, user-approved 2026-09-24):** `:shared` keeps exactly one internal source file, `SharedFrameworkPlaceholder.kt`, rather than truly zero Kotlin sources. Recorded durably in `03-CONTEXT.md` under D-10, and `03-09-PLAN.md`'s must-haves/acceptance-criteria updated to expect this one file (not zero) and to forbid deleting it, so the phase's final plan does not undo this fix.
- Kept `ksp` + `kmp.native.coroutines` plugins applied on both `:core:data` (new) and `:shared` (unchanged for this plan; 03-09 removes them from `:shared` once nothing in that module needs the annotation processor).

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Fixed :shared NO-SOURCE framework-link skip**
- **Found during:** Task 1 verification (post-repackage build/link check)
- **Issue:** With `shared/src` fully emptied per the plan's literal Task 1 acceptance criterion ("`git ls-files shared/src | grep -c '\.kt$'` prints 0"), Kotlin/Native's `compileKotlinIos*`/`linkDebugFramework*`/`linkReleaseFramework*` tasks report `NO-SOURCE` and are skipped entirely (confirmed via `--info`: "has no source files and no previous output files"). This means no `shared.framework` would be produced for iOS at all — a full iOS build break and a `swift-names-gate.sh` failure, not a cosmetic issue.
- **Fix:** Added a single internal marker constant (`SHARED_FRAMEWORK_PLACEHOLDER`) in a new file, `shared/src/commonMain/kotlin/com/gdgnantes/devfest/shared/SharedFrameworkPlaceholder.kt`, just enough to make Gradle treat the source set as non-empty so compile+link run and the framework re-exports `:core:model`/`:core:analytics`/`:core:data` as declared.
- **Files modified:** `shared/src/commonMain/kotlin/com/gdgnantes/devfest/shared/SharedFrameworkPlaceholder.kt` (new)
- **Verification:** `linkDebugFrameworkIosSimulatorArm64` now executes (previously `NO-SOURCE`); `swift-names-gate.sh check` → `SWIFT-NAMES-OK` with an empty diff (no new Swift-visible names introduced by the marker — it is `internal`, not exported)
- **Committed in:** `a76fce0`
- **Follow-up:** This supersedes Task 1's literal "0 `.kt` files" acceptance criterion. Recorded as a permanent D-10 amendment (see Decisions Made above) and propagated into `03-09-PLAN.md` so the final umbrella-thinning plan does not attempt to delete this file.

---

**Total deviations:** 1 auto-fixed (1 bug — Rule 1)
**Impact on plan:** Necessary for correctness — without this fix, the phase's own goal (iOS umbrella framework staying buildable) would have been silently broken by the very refactor meant to prove it. No scope creep; user explicitly reviewed and approved the deviation at the Task 3 checkpoint before this close-out.

## Issues Encountered

- One ANR dialog appeared on the Android debug app's very first cold launch on a freshly-booted emulator during Task 2's automated launch sanity check; resolved by tapping "Wait" and assessed as cold-emulator warmup (not a regression from this plan's changes — the app rendered Agenda with data normally afterward).

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- `:core:data` extracted, exported, and proven on both platforms (CI green, both smokes approved) — ready for 03-04 (next module in the D-18 bottom-up order).
- The D-10 amendment (`SharedFrameworkPlaceholder.kt`) is now durably recorded in `03-CONTEXT.md` and `03-09-PLAN.md`, so 03-09 (which thins `:shared` to its final umbrella form) will not regress this fix.
- `ARCH-02`/`ARCH-04` requirement IDs intentionally left unmarked (`requirements.ready-ids` reports both still `blocked` — sibling plans 03-04 through 03-09 and 03-11 have not yet produced `*-SUMMARY.md`); they will be marked complete by whichever sibling plan finishes last, per the phase's shared-ID gate.
- No blockers for 03-04.

## Self-Check: PASSED

- All 18 files listed in key-files (created + modified) verified present on disk with `[ -f ]`.
- All 4 commits (`94c760e`, `a01ad23`, `a76fce0`, `d9ae67d`) verified present via `git log --oneline --all`.

---
*Phase: 03-multi-module-architecture-extraction*
*Completed: 2026-09-24*
