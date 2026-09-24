---
gsd_state_version: "1.0"
current_phase: 03
current_phase_name: Multi-Module Architecture Extraction
status: executing
stopped_at: Completed 03-02-PLAN.md
last_updated: "2026-09-24T14:05:22.974Z"
last_activity: 2026-09-24
last_activity_desc: Phase 03 execution started
state_head: de0d83dbe817d01adf31a1dd0badfde15961cad5
progress:
  total_phases: 5
  completed_phases: 2
  total_plans: 19
  completed_plans: 12
  percent: 40
---

# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-09-17)

**Core value:** La CI/CD doit refonctionner et le projet doit redevenir maintenable (build moderne, architecture modulaire, DI décentralisée, couverture de tests solide) sans jamais régresser le comportement existant de l'application pour les utilisateurs.
**Current focus:** Phase 03 — Multi-Module Architecture Extraction

## Current Position

Phase: 03 (Multi-Module Architecture Extraction) — EXECUTING
Plan: 4 of 11
Status: Ready to execute
Last activity: 2026-09-24 — Phase 03 execution started

Progress: [████░░░░░░] 40%

## Performance Metrics

**Velocity:**

- Total plans completed: 8
- Average duration: N/A
- Total execution time: 0 hours

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 01 | 3 | - | - |
| 02 | 5 | - | - |

**Recent Trend:**

- Last 5 plans: N/A
- Trend: N/A

*Updated after each plan completion*
**Per-Plan Metrics:**

| Plan | Duration | Tasks | Files |
|------|----------|-------|-------|
| Phase 01 P01 | 18min | 2 tasks | 1 files |
| Phase 01 P02 | 2min | 2 tasks | 1 files |
| Phase 01 P03 | 21min | 1 tasks | 1 files |
| Phase 02 P01 | 75min | 2 tasks | 4 files |
| Phase 02 P02 | 43min | 3 tasks | 16 files |
| Phase 02 P03 | 180min | 2 tasks | 6 files |
| Phase 02 P04 | 75min | 2 tasks | 6 files |
| Phase 02 P05 | 50min | 2 tasks | 3 files |
| Phase 03 P01 | 95min | 2 tasks | 77 files |
| Phase 03 P10 | 70min | 2 tasks | 16 files |
| Phase 03 P11 | 15min | 2 tasks | 4 files |
| Phase 03 P02 | 65min | 2 tasks | 31 files |

## Accumulated Context

### Decisions

Decisions are logged in PROJECT.md Key Decisions table.
Recent decisions affecting current work:

- Roadmap: Ordre strictement séquentiel CI iOS -> deps/build -> multi-module -> DI -> tests (aucune parallélisation entre phases, chaque phase dépend de la précédente)
- Roadmap: CICD-01/02/03 (cache Gradle/Konan, matrice CI) regroupés dans la Phase 1 avec CI-01 plutôt qu'une phase CICD séparée — même surface CI, évite une phase à faible densité
- Roadmap: Gradle Declarative DSL traité comme migration partielle documentée (BUILD-07), pas de blocage sur un support AGP/KMP incomplet
- [Phase 01]: iOS Xcode/simulator resolution kept shell+jq (no marketplace action); no fallback step for empty simulator resolution per D-01/D-02 — Avoids new unaudited third-party dependency; xcodebuild's own destination error is the accepted failure mode per locked CONTEXT.md decisions
- [Phase 01]: Phase 01 (CI Pipeline Fixed & Optimized) complete: ios.yml routed through the shared android-setup composite action with branch-aware Gradle cache policy; Konan cache and macos-latest/ubuntu-latest job separation confirmed intact via live workflow_dispatch run 35228437188
- [Phase 02]: Version-target policy: newest-verified (Kotlin 2.4.20, AGP 9.4.0, Compose BOM 2026.09.00, Apollo 5.2.0) for BUILD-01/02/04/05, not the REQUIREMENTS.md-pinned 2026-09-12 snapshot — Checkpoint resolved by developer in 02-01; keeps Phase 3 from immediately needing another toolchain bump
- [Phase 02]: minSdk raised 23 -> 26 (user-authorized) to resolve AGP 8.13's lint tool being unable to read Kotlin 2.4's @Metadata format, mis-flagging 6 forEach call sites as NewApi — User explicitly authorized mid-execution; real fix (API level genuinely available) rather than a lint suppression, which the plan's threat model prohibits
- [Phase 02]: D-03 commit fold: Gradle 9.7.1 + AGP 9.4.0 landed in one commit instead of Task 1's planned standalone wrapper commit — Gradle 9.7.1 does not build under AGP 8.13.0 (removed internal API org.gradle.api.problems.internal.InternalProblems)
- [Phase 02]: Detekt pinned to 2.0.0-alpha.6 under the dev.detekt group/plugin-id, approved via the blocking-human package-legitimacy checkpoint — official detekt org's own pre-1.0 next-major line, closest compat baseline to this stage's targets
- [Phase 02]: Compose BOM 2026.09.00 forced compileSdk 36->37 (AAR metadata floor); targetSdk deliberately decoupled and kept at 36 per user direction, tracked as a follow-up decision
- [Phase 02]: Apollo 5.2.0 + appolloCache 1.0.8: normalized cache migrated to com.apollographql.cache artifact group; CacheResolver adapted to v5's ResolverContext signature; resolveArgument replaced with argumentValue (hard DEPRECATION_ERROR under Kotlin 2.4)
- [Phase 02]: User-authorized deviation: GraphQLStore.kt's CacheAndNetwork Flow accessors changed map->mapNotNull to stop a network-failure emission from clobbering correctly-cached offline data (pre-existing bug surfaced by D-07 verification, not a migration regression)
- [Phase 02]: kotlinx-datetime 0.8.0's Instant typealias survived (assumption A2 resolved) but Clock.System does not resolve through the typealias; ScheduleSlot.kt/Agenda.kt needed zero changes, but androidApp's UI-layer Agenda.kt needed a one-line kotlin.time.Clock import swap
- [Phase 02]: Firebase BOM 34.x removed the -ktx artifact constraints (Firebase stopped publishing -ktx modules July 2025); repointed the four firebase-*-ktx catalog aliases at their merged plain artifacts, updated two production files' imports, and added a dependency substitution for the transitive firebase-auth-ktx pulled in via openfeedback
- [Phase 02]: Added three R8 -dontwarn rules (R8-generated, verbatim) for openfeedback's stale kotlinx-datetime 0.6.x Clock/Instant class references, safe because OPEN_FEEDBACK_ENABLED=false makes those code paths unreachable at runtime
- [Phase 02]: settings.gradle.kts converted to settings.gradle.dcl (Gradle Declarative DSL pilot, D-08/D-09) — succeeded on the primary attempt; androidApp/shared deliberately stay on Kotlin DSL, corroborated by a live 2026-09-19 re-check confirming Declarative Gradle's module-level Software Types support remains experimental/unready
- [Phase 02]: BUILD-01..BUILD-07 version outcomes consolidated into a single STATE.md record, discharging D-03's documentation obligation for the whole phase
- [Phase 03]: 03-01 halt resolution (user, 2026-09-23): options 1+3, planned OUTSIDE 03-01's scope — (1) rename Apollo-generated response types away from domain-model names (Venue/Session/Speaker/Room/Partner) via Apollo Gradle config; (3) strengthen swift-names-gate.sh to diff each colliding type's member set, not just name+count. Requires replanning before 03-01 Task 2 CI gate / Task 3 can pass. — Durable fix for the K/N Swift-name collision flip; option 2 alone would leave 4 latent flips for 03-02/03-03
- [Phase 03]: 03-01 halt resolved — Apollo schema-type holders renamed via @targetName (GraphQL<Type>), swift-names-gate.sh strengthened (member sets + type-level collisions), D-11 one-time Swift rename amendment; 03-02..03-09 runnable on next /gsd-execute-phase 3
- [Phase 03]: [Phase 03] 03-02: extracted :core:network (Apollo, implementation-only, never export()-ed, D-11) and :core:analytics (exported, D-11) as two-commit-per-module moves (D-16); discovered and closed a Kotlin/Native ObjC-header leak where public extension functions/properties on non-exported :core:network receiver types forced module-prefixed shadow declarations into shared.h — fixed by marking 8 declarations (Mappers.kt's 7 toXxx() functions, RoomSortIndex.kt's sortIndex property) internal — No explicit export() line existed for :core:network, but public extension-function receivers alone were enough to leak its non-exported Apollo types into the iOS umbrella header; the acceptance check "no export line" alone could not have caught this — swift-names-gate.sh's member/collision diff did.

### Pending Todos

None yet.

### Blockers/Concerns

- Phase 3: la home des écrans About/Partners (feature-settings vs nouveau feature-about) reste une décision ouverte à trancher avant l'extraction des feature modules (voir research/SUMMARY.md)
- **Phase 3 — RESOLVED 2026-09-24 by 03-10 + 03-11:** (history, kept for context) moving `Venue`/`Session`/`Speaker`/`Room`/`Partner` from package `com.gdgnantes.devfest.model` into `com.gdgnantes.devfest.core.model` flipped which of two same-simple-named Kotlin classes (the domain model vs. the Apollo-generated `GetXQuery.X`/`fragment.XDetails.X` GraphQL response class already colliding on that name) keeps the unprefixed Swift name vs. gets suffixed `_`. Kotlin/Native's ObjC header generator resolves same-simple-name collisions across the whole compiled framework by an alphabetical-FQN tie-break (shortest proof: `com.gdgnantes.devfest.model.Venue` sorted AFTER `com.gdgnantes.devfest.graphql.GetVenueQuery.Venue` at baseline — domain model got `Venue_`; `com.gdgnantes.devfest.core.model.Venue` sorts BEFORE `...graphql...` post-rename — domain model now gets clean `Venue`, GraphQL type flips to `Venue_`). This broke ~10 pre-existing Swift files (`VenueContent.swift`, `AgendaContent.swift`, `AgendaViewModel.swift`, `AgendaView.swift`, `AgendaCellView.swift`, `AgendaDetailView.swift`, `SpeakerDetailsViewModel.swift`, `SpeakersViewModel.swift`, `SpeakerDetailsView.swift`, `SpeakerView.swift`, `AboutViewModel.swift`) that hardcode the `_`-suffixed name expecting it to be the domain model. iOS CI failed on Swift compilation (run 35847334261, PR #419) — the swift-names-gate.sh gate (missing-name + collision-COUNT checks) did not catch this because the collision COUNT stayed the same, only the per-name IDENTITY swapped. **Resolution:** see `.planning/phases/03-multi-module-architecture-extraction/03-01-SUMMARY.md` "Halt Resolution (03-10 + 03-11)" for the full diagnosis, the @targetName mechanism, the corrected collision attribution, and the D-11 amendment.
- Phase 3: l'export() du framework umbrella iOS doit être validé par un spike avant la découpe complète des modules — risque architectural le plus élevé du chantier (three-framework problem) — **partially validated by 03-01: the export()/api mechanics work as designed; the newly-discovered risk is the Swift-name collision-flip above, a different (related) failure mode**
- Phase 2: AGP 9.x/KGP/KSP2 version re-verification was performed live during Phase 2 (each plan re-checked Maven Central/Google Maven metadata immediately before its own commit); outcomes are recorded per-requirement in `## Phase 02 version deviations` below — discharged, no longer open.
- Follow-up (not blocking): bump targetSdk 36->37 in a dedicated future stage after reviewing Android 17's behavior-change surface (kb://android/about/versions/17/behavior-changes-all / -17) — deliberately deferred from 02-03 per user direction to avoid folding a runtime-behavior-change decision into the Compose-BOM commit
- Follow-up (not blocking): the firebase-auth-ktx -> firebase-auth:24.2.0 dependency substitution in androidApp/build.gradle.kts is version-coupled to the pinned firebaseBom (34.19.0); re-verify the literal version if firebaseBom is bumped again in a future stage

## Phase 02 DCL pilot outcome

**Requirement:** BUILD-07 — "Les fichiers de build sont migrés vers le Gradle Declarative DSL (`.gradle.dcl`) là où le support AGP/KMP le permet ; les modules non supportés restent documentés en Kotlin DSL (`.kts`) avec la raison du blocage." Satisfied via the partial-migration branch (D-08): one low-risk leaf target converted, the rest documented on `.kts` with the reason support is missing.

**Live re-check performed before converting anything (per the plan's action):** fetched `docs.gradle.org/9.7.1/userguide/userguide.html` (no dedicated Declarative Gradle chapter), `blog.gradle.org` (latest Declarative Gradle post is still "Declarative Gradle EAP3 - April 2025 Update", no 2026 update found), and `github.com/gradle/declarative-gradle`'s `docs/getting-started/README.md` on its `main` branch, which states as of today (2026-09-19): *"Declarative Gradle is **not ready** for adoption by plugin authors, build engineers or software engineers."* Its `setup.md` confirms the full software-type samples (`javaApplication`, `androidApplication`, etc.) require *nightly* Gradle builds and experimental IDE flags, not stable Gradle. Even Gradle's own "bleeding edge" `unified-prototype` multi-module sample keeps its root **settings file in Kotlin DSL** (`settings.gradle.kts`, applying `org.gradle.experimental.*-ecosystem` plugins) and pins a Gradle *milestone* build (`9.6.0-milestone-1`), not a `.dcl` settings file on stable Gradle — corroborating 02-RESEARCH.md's MEDIUM-confidence April-2025 snapshot as still current; nothing has changed materially since.

**Target 1 (primary): `settings.gradle.kts` → `settings.gradle.dcl` — CONVERTED, project builds and CI is green through it.**
Despite the software-type DSL (used for full project/module definitions like `javaApplication`) genuinely being unready per the above, this repo's `settings.gradle.kts` never used software types — its entire content is `pluginManagement { repositories { ... } }`, `dependencyResolutionManagement { repositories { ... } }`, `rootProject.name = ...`, and two `include(...)` calls. These are core Gradle settings-file APIs, not part of the experimental "ecosystem" plugin surface, and Gradle 9.7.1 (stable, no experimental flags, no nightly build) parses a `.dcl`-suffixed settings file containing them without error. Converted 1:1 (repository lists unchanged: Google, Maven Central, Gradle Plugin Portal in both `pluginManagement` and `dependencyResolutionManagement`; `rootProject.name = "DevFest_Nantes"`; both `include(":androidApp")`/`include(":shared")` preserved).

- `@file:Suppress("UnstableApiUsage")`: dropped, no DCL equivalent needed — this is a Kotlin-compiler annotation suppressing a Kotlin-DSL-specific warning, not a Gradle construct; DCL has no such warning to suppress.
- `enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")`: dropped — no DCL equivalent found (DCL settings files have no `enableFeaturePreview(...)` call form). Verified safe by search: `grep -rn 'projects\.\(shared\|androidApp\)' **/*.kts` returns zero hits project-wide; both build scripts use the string form `project(":shared")` (see `androidApp/build.gradle.kts:137`), never the typesafe accessor form the flag enables. Dropping it is behavior-preserving, confirmed by a full local build (not assumed).
- The commented-out `//repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)` line was not carried across (dead code, and DCL is declarative-only — no commented-out imperative statements to preserve).
- **Verification:** `./gradlew --no-daemon projects` → root `DevFest_Nantes` with exactly `:androidApp` and `:shared` (topology intact). `./gradlew --no-daemon clean :shared:assemble :androidApp:assembleDebug :androidApp:assembleRelease :shared:jvmTest :androidApp:testDebugUnitTest detekt lint` → `BUILD SUCCESSFUL` (194 tasks, 176 executed). `./gradlew --no-daemon :shared:compileKotlinIosSimulatorArm64` → `BUILD SUCCESSFUL`.

**Target 2 (fallback): `buildSrc/build.gradle.kts` — NOT ATTEMPTED, primary succeeded.** Per the plan's explicit ordering ("only if the primary attempt fails"), the fallback conversion was not needed and `buildSrc/build.gradle.kts` (its 4-line `plugins { \`embedded-kotlin\` }` content) is untouched.

**Repository ends on:** `settings.gradle.dcl` (the `.kts` file was deleted in the same commit — the two settings-file forms never coexisted at any point in this repo's history).

**`androidApp`/`shared` remain on Kotlin DSL (`.kts`), unchanged by this pilot — reason (discharges BUILD-07's "documented with the reason support is missing" clause):** D-08 scoped the pilot to one low-risk leaf target only; neither module's build file was attempted, per the plan's explicit scope boundary. Independent of that scoping decision, the live re-check above confirms AGP/KMP DCL support for a module of this shape would not be viable today even if attempted: `shared/build.gradle.kts` applies 7 plugins including `com.android.kotlin.multiplatform.library` (AGP 9's new KMP plugin), Detekt, KSP, Apollo, and `kmpNativeCoroutines`; `androidApp/build.gradle.kts` applies 8 plugins including Dagger Hilt, Firebase Crashlytics/Performance, and Compose. Declarative Gradle's only path to expressing a module like this is its "Software Types" model (`javaApplication`/`androidApplication`/`kotlinJvmLibrary` etc.) via the experimental `org.gradle.experimental.android-ecosystem`/`kmp-ecosystem` settings plugins — exactly the surface the Declarative Gradle project's own docs call "not ready for adoption," gated behind nightly Gradle builds and IDE internal-mode flags, with no stable-Gradle path found. This is a genuinely different case from the settings file's plain repository/include declarations, which map onto core (non-experimental) Gradle settings APIs DCL already parses correctly on stable Gradle.

**Phase 3 interaction (per assumption A5/D-09):** the pilot target chosen here is the settings file, which is orthogonal to Phase 3's module split (Phase 3 changes `shared`'s internal module boundaries, not the root settings file's repository/include shape) — no re-pilot expected to be forced by Phase 3, but Phase 3 should re-run the same live status check before assuming DCL's software-type surface has matured enough to attempt `shared`'s or any new `core-*`/`feature-*` module's build file.

**Phase 03 re-check (03-01 tracer, 2026-09-23):** `includeBuild("build-logic")` inside `settings.gradle.dcl`'s `pluginManagement` parsed and resolved correctly on Gradle 9.7.1 (confirmed by `./gradlew projects` listing `Included build ':build-logic'` and `:core:model`) — D-20's fallback branch was not needed.
`build-logic/settings.gradle.kts` and every new module's `build.gradle.kts` (`core/model/build.gradle.kts`, `build-logic/convention/build.gradle.kts`) stay plain Kotlin DSL per D-20/RESEARCH Pitfall 2 — no `.dcl` file exists anywhere under `build-logic/` or `core/`, matching the same software-types-not-ready assessment as Phase 2's pilot.
No re-pilot of DCL's software-type surface was attempted; nothing found this session changes the Phase 02 assessment above.

## Phase 02 version deviations

**Version-target policy (selected at 02-01's checkpoint, 2026-09-18):** `newest-verified` — this phase targets the newest verified stable release found during each plan's own live re-verification immediately before that plan's commit, not the literal REQUIREMENTS.md/CONTEXT.md snapshot numbers taken on 2026-09-12. Governs BUILD-01/02/04/05 (all four pin exact numbers in REQUIREMENTS.md); BUILD-03/06/07 were not pinned to exact numbers in REQUIREMENTS.md text so this policy does not create a deviation for them. See `.planning/REQUIREMENTS.md`'s `D-03 version-target deviation` note and each plan's own SUMMARY for the live-verification evidence.

| Req | Target in REQUIREMENTS.md | Version pinned | Matched? | Reason if not |
|-----|---------------------------|-----------------|----------|----------------|
| BUILD-01 | Kotlin 2.4.0 (hard pin) | Kotlin 2.4.20, KSP 2.3.12 | Superset (newest-verified policy) | N/A — deliberate per-policy superset, not a forced deviation. KSP 2.3.12 not Kotlin-paired: KSP 2.3.0+ decoupled its versioning from the Kotlin compiler version entirely (google/ksp release notes) |
| BUILD-02 | AGP 9.2.0 + `com.android.kotlin.multiplatform.library` (hard pin) | AGP 9.4.0 | Superset (newest-verified policy) | N/A — deliberate per-policy superset. Forced sibling bumps in the same commit: Detekt 1.23.8 → 2.0.0-alpha.6 (`dev.detekt` group/plugin-id, approved via blocking-human package-legitimacy checkpoint — official detekt org's own pre-1.0 next-major line), Dagger Hilt 2.57.2 → 2.60.1, Firebase Performance plugin 2.0.1 → 2.0.2 — all three are AGP-9-documented compatibility floors, not discretionary |
| BUILD-03 | Gradle 9.7.1 (hard pin, not part of the newest-verified policy — already exact) | Gradle 9.7.1 | Matched exactly | N/A |
| BUILD-04 | Compose BOM 2026.08.00 (hard pin) | Compose BOM 2026.09.00 | Superset (newest-verified policy) | N/A — deliberate per-policy superset. Forced compileSdk 36 → 37 (Compose BOM 2026.09.00's AAR metadata floor: `androidx.compose.material:material-android:1.12.1` and 11 sibling artifacts declare `compileSdk >= 37`); targetSdk deliberately kept at 36, decoupled, tracked as a separate follow-up (see Blockers/Concerns above) |
| BUILD-05 | Apollo GraphQL 5.0.1 (hard pin) | Apollo 5.2.0, `appolloCache` 1.0.8 | Superset (newest-verified policy) | N/A — deliberate per-policy superset. Normalized cache migrated from the deprecated `com.apollographql.apollo` group to the new `com.apollographql.cache` group (`normalized-cache`, `normalized-cache-sqlite`, `normalized-cache-apollo-compiler-plugin`), all pinned to `appolloCache` 1.0.8 |
| BUILD-06 | Firebase BOM / Coroutines / kotlinx-serialization / kotlinx-datetime "latest stable" (not pinned) | Firebase BOM 34.19.0, kotlinx-coroutines 1.11.0, kotlinx-serialization-json 1.11.0, kotlinx-datetime 0.8.0 | Matched (requirement text says "latest stable", not an exact number) | N/A |
| BUILD-07 | Build files migrated to `.gradle.dcl` where AGP/KMP support allows; unsupported modules documented on `.kts` with the reason | `settings.gradle.dcl` converted; `androidApp`/`shared` stay on `.kts` | Matched — satisfied via the explicit partial-migration branch (D-08), not the full-migration branch | N/A — see `## Phase 02 DCL pilot outcome` above for the full outcome record and the specific reason `androidApp`/`shared` are undocumented-for-DCL-support |

**kotlinx-datetime `Instant`/`Clock` migration surface (assumption A2, closes an open research question so Phase 3 does not re-investigate):** `kotlinx.datetime.Instant`'s typealias to `kotlin.time.Instant` survived (deprecated, not removed) into kotlinx-datetime 0.8.0 — `ScheduleSlot.kt`/`Agenda.kt` (shared/model) needed zero code changes. However `kotlinx.datetime.Clock`'s typealias does **not** carry `.System` through to `kotlin.time.Clock` the same way — any call site using `Clock.System` directly (not just `Instant`) needs its import swapped to `kotlin.time.Clock`. One such call site existed outside the shared/model scope: `androidApp/.../ui/screens/agenda/Agenda.kt`, fixed with a one-line import swap. Grep for `kotlinx.datetime.Clock` before assuming a future kotlinx-datetime bump is a no-op.

**Apollo cache import paths actually used (authoritative, for Phase 3 reuse):**

- Cache-normalized wildcard: `com.apollographql.cache.normalized.*` (was `com.apollographql.apollo.cache.normalized.*`)
- `MemoryCacheFactory`: `com.apollographql.cache.normalized.memory.MemoryCacheFactory` (own `memory` subpackage — was `...api.MemoryCacheFactory`)
- `SqlNormalizedCacheFactory`: `com.apollographql.cache.normalized.sql.SqlNormalizedCacheFactory` (same subpackage name, new root group)
- `NormalizedCacheFactory`, `CacheKey`, `CacheKeyGenerator`, `CacheKeyGeneratorContext`, `CacheResolver`, `DefaultCacheResolver`, `ResolverContext`: all under `com.apollographql.cache.normalized.api`
- `FetchPolicy`, `fetchPolicy` extension, `normalizedCache` extension: `com.apollographql.cache.normalized`
- Runtime APIs (`ApolloClient`, `api.http.HttpHeader`) stay on `com.apollographql.apollo.*` — unmoved

**Detekt alpha pinned:** `dev.detekt` 2.0.0-alpha.6 (group and plugin-id both renamed from `io.gitlab.arturbosch.detekt`). **Advisory-findings note:** treat findings from this pre-1.0 alpha as advisory, not authoritative, until Detekt reaches a stable 2.x release — approved via a blocking-human package-legitimacy checkpoint in 02-02, not a supply-chain risk (official detekt org's own next-major line) but pre-1.0 maturity means CI-breaking regressions are more likely than with a stable release. **Before/after issue counts:** 11 real findings surfaced immediately after the `dev.detekt:detekt-rules-ktlint-wrapper` coordinate fix (proof the ktlint-wrapper ruleset was genuinely active, not a silent no-op), 0 after the 11 source fixes landed in the same commit (`c56d6ab`).

## Deferred Items

Items acknowledged and deferred at milestone close, most recent first:

| Category | Item | Status | Deferred At | Milestone |
|----------|------|--------|-------------|-----------|
| *(none)* | | | | |

## Session Continuity

Last session: 2026-09-24T14:05:22.952Z
Stopped at: Completed 03-02-PLAN.md
Resume file: None
