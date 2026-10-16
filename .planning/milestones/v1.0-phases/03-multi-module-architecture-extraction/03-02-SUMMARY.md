---
phase: 03-multi-module-architecture-extraction
plan: 02
subsystem: architecture
tags: [gradle, kmp, apollo, graphql, firebase, analytics, kotlin-native, ios-interop]

# Dependency graph
requires:
  - phase: 03-multi-module-architecture-extraction
    provides: "03-01 (build-logic + devfest.kmp.library convention plugin, :core:model extraction, swift-names-gate.sh); 03-10 (Apollo @targetName rename removing the Venue/Session/Speaker/Room/Partner collision, strengthened swift-names-gate.sh with member-set + type-collision checks); 03-11 (03-01 halt resolved to complete)"
provides:
  - ":core:network — Apollo client/cache/schema/codegen, implementation-only in :shared, never export()-ed (D-11), generated package com.gdgnantes.devfest.core.network.graphql"
  - ":core:analytics — AnalyticsService/Event/Page/Param contract in commonMain, Firebase-backed AnalyticsService + PerformanceMonitoring/trace/traceDataLoading in androidMain (D-03), api + export()-ed by :shared (D-11)"
  - "Discovery + fix: public Kotlin extension functions/properties whose receiver type comes from a non-exported KMP module force Kotlin/Native to synthesize module-prefixed shadow declarations in the iOS umbrella header, silently breaching the export() boundary — closed by marking those declarations `internal`"
affects: [03-03, 03-04, 03-05, 03-06, 03-07, 03-08, 03-09]

# Actuals (#2632)
actuals:
  tokens: 7883
  tasks: 2
  commits: 4
plan_head_before: bc378a45ec276f8b62c9227c812cb6ca9591c382

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "D-16 two-commit-per-module pattern applied to both :core:network and :core:analytics: pure git mv (packages unchanged) then package rename + import updates, each commit build-green"
    - "Non-exported KMP module leaf: implementation(project(...)) only, no export() call — verified structurally via 'no core:network export line' and empirically via swift-names-gate.sh's empty diff after closing the extension-function leak"
    - "Mark internal any top-level function/property whose receiver or parameter type comes from a non-exported dependency, even if only used internally — otherwise Kotlin/Native's ObjC header generator synthesizes a module-prefixed shadow type to represent it, defeating non-export intent"

key-files:
  created:
    - core/network/build.gradle.kts
    - core/network/src/commonMain/kotlin/com/gdgnantes/devfest/core/network/Apollo.kt
    - core/network/src/commonMain/kotlin/com/gdgnantes/devfest/core/network/ApolloCache.kt
    - core/network/src/commonMain/graphql/operations.graphql
    - core/network/src/commonMain/graphql/schema.graphqls
    - core/network/src/commonMain/graphql/extra.graphqls
    - core/analytics/build.gradle.kts
    - core/analytics/src/commonMain/kotlin/com/gdgnantes/devfest/core/analytics/AnalyticsEvent.kt
    - core/analytics/src/commonMain/kotlin/com/gdgnantes/devfest/core/analytics/AnalyticsPage.kt
    - core/analytics/src/commonMain/kotlin/com/gdgnantes/devfest/core/analytics/AnalyticsParam.kt
    - core/analytics/src/commonMain/kotlin/com/gdgnantes/devfest/core/analytics/AnalyticsService.kt
    - core/analytics/src/androidMain/kotlin/com/gdgnantes/devfest/core/analytics/FirebaseAnalyticsService.kt
    - core/analytics/src/androidMain/kotlin/com/gdgnantes/devfest/core/analytics/performance/PerformanceMonitoring.kt
    - core/analytics/src/androidMain/kotlin/com/gdgnantes/devfest/core/analytics/performance/PerformanceExtensions.kt
  modified:
    - settings.gradle.dcl
    - shared/build.gradle.kts
    - androidApp/build.gradle.kts
    - shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/GraphQLStore.kt
    - shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/Mappers.kt
    - shared/src/commonMain/kotlin/com/gdgnantes/devfest/domain/RoomSortIndex.kt
    - shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/DevFestNantesStoreBuilder.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/injection/AppModule.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/BookmarksViewModel.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/Home.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/agenda/AgendaRow.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/agenda/AgendaViewModel.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/session/SessionLayout.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/session/SessionViewModel.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/speakers/SpeakerViewModel.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/speakers/list/SpeakersViewModel.kt
    - .planning/phases/03-multi-module-architecture-extraction/swift-names-gate.sh (no code change; re-baselined)
    - .planning/phases/03-multi-module-architecture-extraction/03-shared-h-baseline.txt
    - .planning/phases/03-multi-module-architecture-extraction/03-swift-referenced-names.txt
    - .planning/phases/03-multi-module-architecture-extraction/03-swift-members-baseline.txt
    - .planning/phases/03-multi-module-architecture-extraction/03-swift-names-exclude.txt

key-decisions:
  - "Marked Mappers.kt's 7 toXxx() mapping functions and RoomSortIndex.kt's RoomDetails.sortIndex extension `internal` (deviation, see below) to close an unintended Kotlin/Native ObjC-header leak of :core:network's non-exported Apollo-generated types"
  - "Added SpeakerDetails to 03-swift-names-exclude.txt: iosApp's own SwiftUI View struct `SpeakerDetails` coincidentally shares a name with Apollo's now-internal fragment response class (same pattern as the existing Link exclusion from 03-10)"
  - "core/analytics androidMain depends on libs.dagger.hilt.android only for the javax.inject.Inject/Singleton annotations (no KSP/Hilt plugin applied in this leaf) — Dagger generates any missing factory during :androidApp's own component processing, per plan's placement decision"

patterns-established:
  - "Non-exported-module visibility hygiene: before marking a KMP leaf module implementation-only, grep every public top-level declaration in the depending module for receiver/parameter types sourced from that leaf, and mark internal (or restructure) any as needed"

requirements-completed: [ARCH-02, ARCH-04]
# NOTE: ARCH-02/ARCH-04 span the whole phase (planner-declared on multiple sibling plans).
# `gsd-tools query requirements.ready-ids` returned 0/2 ready at this plan's completion, so
# REQUIREMENTS.md checkboxes were intentionally NOT flipped — only the last plan to declare
# each ID will flip it. Listed here per the SUMMARY template's "copy the plan's requirements
# frontmatter verbatim" contract, not as a claim of full-phase completion.

coverage:
  - id: D1
    description: ":core:network exists (D-14), owns the Apollo Gradle plugin config and GraphQL schema/operations, generates types in com.gdgnantes.devfest.core.network.graphql (D-15), and is implementation-only in :shared — never export()-ed (D-11)"
    requirement: "ARCH-02"
    verification:
      - kind: integration
        ref: "./gradlew :core:network:generateApolloSources :core:network:jvmTest :shared:jvmTest :androidApp:assembleDebug detekt -> BUILD SUCCESSFUL"
        status: pass
      - kind: other
        ref: "grep -c 'core:network' shared/build.gradle.kts == 1 (no export line); core/network/build/generated/.../type/GraphQLVenue.kt exists, .../type/Venue.kt does not"
        status: pass
    human_judgment: false
  - id: D2
    description: ":core:analytics exists with AnalyticsService/Event/Page/Param in commonMain and FirebaseAnalyticsService + PerformanceMonitoring/trace/traceDataLoading in androidMain (D-03), api + export()-ed by :shared for all three iOS targets (D-11)"
    requirement: "ARCH-04"
    verification:
      - kind: integration
        ref: "./gradlew :core:analytics:jvmTest :core:analytics:compileKotlinIosSimulatorArm64 :shared:jvmTest :androidApp:assembleDebug :androidApp:assembleRelease detekt lint -> BUILD SUCCESSFUL"
        status: pass
      - kind: other
        ref: "shared/build.gradle.kts contains api(project(\":core:analytics\")) and export(project(\":core:analytics\")); androidApp/build.gradle.kts contains implementation(project(\":core:analytics\")); ./gradlew :core:analytics:dependencies has no project :feature: line"
        status: pass
    human_judgment: false
  - id: D3
    description: "Hilt AppModule stays in :androidApp, keeps binding FirebaseAnalyticsService to AnalyticsService, only imports change (D-02)"
    requirement: "ARCH-02"
    verification:
      - kind: unit
        ref: "AppModule.kt still at androidApp/.../core/injection/AppModule.kt; grep -c FirebaseAnalyticsService == 2, grep -c AnalyticsService == 3"
        status: pass
    human_judgment: false
  - id: D4
    description: "Every Swift-referenced Kotlin type/member is unchanged after both repackages; strengthened swift-names-gate.sh (03-10) reports SWIFT-NAMES-OK; the @targetName GraphQL* schema-type holders move with the schema unchanged"
    requirement: "ARCH-04"
    verification:
      - kind: other
        ref: "swift-names-gate.sh check -> SWIFT-NAMES-OK after core:network repackage (835-line removed-only diff, all Apollo/GraphQL-generated, classified below) and again after core:analytics repackage (empty diff)"
        status: pass
    human_judgment: true
    rationale: "The gate is deterministic and passed, but the classification of every removed/added swift_name as core:network-internal is a judgment call worth a human skim (see classification table below) given this is exactly the class of failure that halted 03-01."
  - id: D5
    description: "Each module step lands as two green commits (pure git mv, then package rename + imports); both CI workflows green on the pushed HEAD"
    requirement: "ARCH-02"
    verification:
      - kind: integration
        ref: "CI-GREEN-BOTH de0d83dbe817d01adf31a1dd0badfde15961cad5 — android.yml https://github.com/GDG-Nantes/DevfestNantesMobile/actions/runs/36008958500, ios.yml https://github.com/GDG-Nantes/DevfestNantesMobile/actions/runs/36008958517"
        status: pass
    human_judgment: false

# Metrics
duration: 65min
completed: 2026-09-24
status: complete
---

# Phase 3 Plan 2: Extract :core:network + :core:analytics Summary

**Split Apollo GraphQL (client, cache, schema, codegen) into non-exported `:core:network` and analytics/performance (contract + Firebase-backed Android impls) into exported `:core:analytics`, each as two green commits, discovering and closing a Kotlin/Native ObjC-header leak where public extension functions on non-exported types were forcing "Network"-prefixed shadow declarations into the iOS umbrella framework — both CI workflows green on PR #419.**

## Performance

- **Duration:** ~65 min
- **Started:** 2026-09-24T13:40:00Z (approx)
- **Completed:** 2026-09-24T14:02:00Z (approx, CI included)
- **Tasks:** 2 of 2
- **Files modified:** 31 (excluding `.planning/`)

## Accomplishments

- `:core:network` extracted: Apollo client (`apolloClient`), normalized cache, and the GraphQL schema/operations/`@targetName` extension all moved out of `:shared` in a pure-move commit, then repackaged to `com.gdgnantes.devfest.core.network` with the Apollo codegen `packageName` changed to `com.gdgnantes.devfest.core.network.graphql`. `:shared` depends on it via `implementation(project(":core:network"))` only — no `export()` line, verified structurally (`grep -c 'core:network'` == 1) and empirically (swift-names-gate diff shows only removals).
- `:core:analytics` extracted: the commonMain `AnalyticsService`/`AnalyticsEvent`/`AnalyticsPage`/`AnalyticsParam` contract plus the Firebase-backed `FirebaseAnalyticsService` and `PerformanceMonitoring`/`PerformanceExtensions` (`trace`, `traceDataLoading`) Android implementations, per the plan's D-03 "lowest module that needs it" placement. `:shared` re-exports it (`api` + `export()` per iOS target); `:androidApp`'s single `AppModule` keeps binding `FirebaseAnalyticsService` to `AnalyticsService` with only import changes (D-02).
- **Discovery beyond the plan's own anticipated scope:** after the `:core:network` repackage, `swift-names-gate.sh check` failed — not from the anticipated bulk removal of Apollo-generated names (that part behaved exactly as the plan's context predicted), but because Mappers.kt's 7 `toXxx()` extension functions and `RoomSortIndex.kt`'s `RoomDetails.sortIndex` extension property were still `public`, with receiver types (`SessionDetails`, `RoomDetails`, `SpeakerDetails`, `GetVenueQuery.Venue`, `GetPartnerGroupsQuery.PartnerGroup`/`.Partner`) sourced from the now-non-exported `:core:network`. Kotlin/Native's ObjC header generator can't omit a public declaration, and can't reference an unexported module's type either, so it synthesized 18 "Network"-prefixed shadow declarations (`NetworkSessionDetails`, `NetworkSpeakerDetails`, etc.) directly into `shared.h` — a real, if narrow, breach of the D-11 "never export()-ed" boundary that the "no `export()` line" acceptance check alone could not catch. Marking all 8 declarations `internal` (verified zero external call sites first) closed the leak completely — the post-fix diff shows **zero** added lines, only the expected 835 removed Apollo/GraphQL-generated names.
- `SpeakerDetails` (the Apollo fragment class, now internal) coincidentally shared its name with iosApp's own `struct SpeakerDetails: View` (`SpeakerDetailsView.swift`) — added to `03-swift-names-exclude.txt` with the same reasoning as 03-10's `Link` exclusion, then re-baselined.
- Both CI workflows green on the pushed HEAD (`de0d83d`), PR #419.

## Task Commits

Each task was committed atomically:

1. **Task 1: Extract :core:network — pure move** — `daa4a7e` (feat)
2. **Task 1: Extract :core:network — repackage (D-15) + close ObjC-header leak** — `2b93cf0` (feat)
3. **Task 2: Extract :core:analytics — pure move** — `6eb4b9f` (feat)
4. **Task 2: Extract :core:analytics — repackage (D-15)** — `de0d83d` (feat)

**Plan metadata commit:** pending (this SUMMARY + STATE.md + ROADMAP.md, committed immediately after this file per the atomic close-out protocol).

## Files Created/Modified

See frontmatter `key-files` for the full list. Highlights:
- `core/network/build.gradle.kts`, `core/analytics/build.gradle.kts` — new leaf module build files
- `shared/build.gradle.kts` — `implementation(project(":core:network"))` (no export), `api` + `export()` for `:core:analytics`
- `androidApp/build.gradle.kts` — `implementation(project(":core:analytics"))`
- `shared/.../store/graphql/Mappers.kt`, `domain/RoomSortIndex.kt` — mapping functions/property marked `internal` (deviation)
- 8 androidApp files — import updates only (analytics + performance FQN changes)

## Decisions Made

- Kept `implementation(project(":core:network"))` in `:shared` (no `export()`), matching D-11; the one gap this surfaced (public extension-function leakage) was fixed by tightening visibility, not by relaxing the non-export boundary.
- `core/analytics` androidMain uses `libs.dagger.hilt.android` only for `javax.inject` annotations — no KSP/Hilt Gradle plugin applied in the leaf module; Dagger generates any missing factory in `:androidApp`'s own component processing (as anticipated by the plan).
- `SpeakerDetails` added to the swift-names exclusion list (native SwiftUI View name, unrelated Kotlin binding) rather than treated as a regression.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug / Rule 2 - Missing Critical] Public extension functions on :core:network-internal receiver types were leaking non-exported Apollo declarations into the iOS umbrella framework header**
- **Found during:** Task 1, `<verify>` after the repackage commit (`swift-names-gate.sh check` failed with `SWIFT-NAME-MISSING SpeakerDetails`, `SWIFT-MEMBERS-CHANGED SpeakerDetails`, `SWIFT-NAME-COLLISION baseline=4 current=3`, plus an 18-line "added names" set all prefixed `Network*` in the informational diff)
- **Issue:** `Mappers.kt`'s 7 `toXxx()` top-level extension functions (`toPartnersGroup`, `toPartner`, `toSession` ×2, `toRoom`, `toSpeaker`, `toSocial`, `toVenue`) and `RoomSortIndex.kt`'s `RoomDetails.sortIndex` extension property were `public` (Kotlin's default), with receiver types generated by Apollo inside the now non-exported `:core:network` module. A public declaration's signature is always represented in the ObjC header; since Kotlin/Native cannot reference an unexported module's type by its real name, it synthesized module-prefixed shadow types (`NetworkSessionDetails`, `NetworkSpeakerDetails`, `NetworkRoomDetails`, `NetworkGraphQLSessionData`, etc.) instead — silently reintroducing exactly the kind of cross-module Kotlin/Native name surface this phase's gate work (03-01/03-10) exists to prevent, even though no explicit `export()` call was ever added for `:core:network`.
- **Fix:** Verified (via `git grep`) that all 8 declarations have zero call sites outside `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/{GraphQLStore.kt,Mappers.kt}` (their only consumer), then marked all 8 `internal`. Rebuilt and reran `swift-names-gate.sh check`: the 18 `Network*`-prefixed shadow declarations disappeared entirely from the diff (informational added-lines count went from 18 to 0); the only remaining failure was the pre-existing, unrelated `SpeakerDetails`/SwiftUI-View name coincidence (resolved separately via the exclusion list below).
- **Files modified:** `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/Mappers.kt`, `shared/src/commonMain/kotlin/com/gdgnantes/devfest/domain/RoomSortIndex.kt`
- **Verification:** `./gradlew :core:network:jvmTest :shared:jvmTest :androidApp:assembleDebug detekt` → BUILD SUCCESSFUL; `swift-names-gate.sh check` diff → 0 added lines (down from 18)
- **Committed in:** `2b93cf0` (Task 1 repackage commit — see note below on scope)
- **Note on plan-text scope:** the plan's action text for this commit said "Only package/import lines and the one packageName value change." This fix adds 8 `internal` modifiers beyond that literal scope. It was necessary to satisfy the SAME task's acceptance criterion requiring `swift-names-gate.sh check` to print `SWIFT-NAMES-OK` — the two acceptance-criteria bullets were in tension, and the `SWIFT-NAMES-OK` / D-11-boundary requirement was treated as load-bearing (matching the plan's own `<context>` assertion that Apollo classes should fully leave `shared.h` with "no Swift-referenced name or member belongs to them").

**2. [Rule 1 - Bug] `SpeakerDetails` swift-names-gate false positive (name-coincidence, not a regression)**
- **Found during:** Task 1, same `swift-names-gate.sh check` run as deviation 1
- **Issue:** `03-swift-referenced-names.txt` (from 03-10) included `SpeakerDetails`, correctly bound at the time to Apollo's now-internal fragment response class. iosApp's own `SpeakerDetailsView.swift` independently defines `struct SpeakerDetails: View` — a native SwiftUI type with the same simple name, coincidental and pre-existing.
- **Fix:** Added `SpeakerDetails` to `03-swift-names-exclude.txt` (same pattern/format as 03-10's `Link` exclusion), then re-ran `swift-names-gate.sh baseline` once to re-baseline `03-shared-h-baseline.txt` / `03-swift-referenced-names.txt` / `03-swift-members-baseline.txt`. Verified the resulting diff (pre-phase baseline vs. new baseline) is bounded entirely to Apollo/GraphQL-generated names (query/response/adapter classes, cache runtime internals, schema-type `GraphQL*` holders) — none of the five domain-model names (`Venue`/`Session`/`Speaker`/`Room`/`Partner`, unprefixed) appear in the removed set.
- **Files modified:** `.planning/phases/03-multi-module-architecture-extraction/03-swift-names-exclude.txt`, `03-shared-h-baseline.txt`, `03-swift-referenced-names.txt`, `03-swift-members-baseline.txt`
- **Verification:** `swift-names-gate.sh check` → `SWIFT-NAMES-OK`, empty diff
- **Committed in:** `2b93cf0`

---

**Total deviations:** 2 auto-fixed (1 Rule 1/2 — visibility-hygiene bug closing an unintended header leak; 1 Rule 1 — false-positive gate exclusion).
**Impact on plan:** Both were necessary for correctness of the D-11 non-export boundary and for the gate's own designed purpose; no scope creep beyond what was needed to make the plan's own acceptance criteria (`SWIFT-NAMES-OK`) actually hold, and no user-visible behavior changed on either platform.

## Swift-Names Diff Classification (informational, per acceptance criteria)

**After `:core:network` repackage (835 lines removed, 0 added, from `03-shared-h-baseline.txt`):** every removed name is either (a) Apollo runtime API surface (`Apollo_api*`, `ADAPTER`, `Cache`, JSON reader/writer, HTTP request/response types — all previously leaked into `shared.h` only because Apollo used to compile directly into `:shared`'s own commonMain, before this module split), (b) Apollo-generated query/response/adapter classes (`GetPartnerGroupsQuery*`, `GetRoomsQuery*`, `GetSessionQuery*`, `GetSessionsQuery*`, `GetSpeakersQuery*`, `GetVenueQuery*` and their `_ResponseAdapter`/`Selections` siblings, plus fragment classes `RoomDetails`/`SessionDetails`/`SpeakerDetails`/`SocialDetails`), or (c) the Apollo schema-type holders introduced by 03-10's `@targetName` rename (`GraphQL{Venue,Session,Speaker,Room,Partner}` + `.Companion`/`Data` siblings, plus the pre-existing `GraphQLString`/`GraphQLFloat`). None of the five unprefixed domain-model names (`Venue`, `Session`, `Speaker`, `Room`, `Partner`) are in the removed set — confirmed by direct grep — so the 03-01/03-10 collision fix remains intact. **After `:core:analytics` repackage: empty diff** (FQN-only change, no Swift-visible name affected).

## Issues Encountered

- **Stale Hilt aggregating-metadata build cache after the `:core:analytics` repackage commit:** `:androidApp:hiltJavaCompileDebug` failed once with `Could not find class file for 'com.gdgnantes.devfest.androidapp.services.FirebaseAnalyticsService'` even though no source file referenced that FQN anymore (confirmed via `git grep`). Resolved by `./gradlew :androidApp:clean` followed by a full rebuild, which succeeded — a Gradle/Hilt incremental-build limitation with aggregating annotation-processor metadata across a class's Gradle-module move, not a source defect. Not expected to recur once CI runs from a clean checkout (confirmed: CI build succeeded from a fresh clone).

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

`:core:network` and `:core:analytics` are the two modules sitting directly on `:core:model` (D-18 order), both wired correctly into the `:shared` umbrella (implementation-only vs. api+export), Android Hilt graph unaffected, both CI workflows green. `:core:data` (next plan, 03-03) can now depend on real `:core:network`/`:core:analytics` modules instead of stubs. The extension-function-visibility lesson from this plan's deviation is worth carrying forward: any future module extraction that keeps a dependency non-exported should grep the depending module's public API surface for that dependency's types before declaring the boundary closed.

**PR #419** (draft, `feature/reno_phase_3` -> `main`) updated, both CI workflows green on `de0d83d`.

**CI Run URLs:**
- Android: https://github.com/GDG-Nantes/DevfestNantesMobile/actions/runs/36008958500 (success)
- iOS: https://github.com/GDG-Nantes/DevfestNantesMobile/actions/runs/36008958517 (success)
- PR: https://github.com/GDG-Nantes/DevfestNantesMobile/pull/419

---
*Phase: 03-multi-module-architecture-extraction*
*Completed: 2026-09-24*

## Self-Check: PASSED

Verified all `key-files.created` exist on disk via `[ -f ]` (core/network + core/analytics build files and sources). Verified commits `daa4a7e`, `2b93cf0`, `6eb4b9f`, `de0d83d` present via `git log --oneline --all`. Re-ran the plan-level `<verification>` block: `./gradlew --no-daemon :core:network:jvmTest :core:analytics:jvmTest :shared:jvmTest :androidApp:assembleDebug :androidApp:assembleRelease detekt lint` → BUILD SUCCESSFUL; `swift-names-gate.sh check` → `SWIFT-NAMES-OK`; CI gate → `CI-GREEN-BOTH de0d83dbe817d01adf31a1dd0badfde15961cad5` (both workflows confirmed green via `gh run watch`).
