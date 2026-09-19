---
gsd_state_version: "1.0"
current_phase: 02
current_phase_name: Dependency & Build Tooling Upgrade
status: executing
stopped_at: Completed 02-04-PLAN.md
last_updated: "2026-09-19T08:44:05.836Z"
last_activity: 2026-09-19
last_activity_desc: Phase 02 execution started
state_head: 736c8434ae1303c0cd4cb1b1d05ed5fc71cdd929
progress:
  total_phases: 5
  completed_phases: 1
  total_plans: 8
  completed_plans: 7
  percent: 20
---

# Project State

## Project Reference

See: .planning/PROJECT.md (updated 2026-09-17)

**Core value:** La CI/CD doit refonctionner et le projet doit redevenir maintenable (build moderne, architecture modulaire, DI décentralisée, couverture de tests solide) sans jamais régresser le comportement existant de l'application pour les utilisateurs.
**Current focus:** Phase 02 — Dependency & Build Tooling Upgrade

## Current Position

Phase: 02 (Dependency & Build Tooling Upgrade) — EXECUTING
Plan: 5 of 5
Status: Ready to execute
Last activity: 2026-09-19 — Completed 02-04-PLAN.md (Firebase/coroutines/serialization/datetime bump)

Progress: [██░░░░░░░░] 20%

## Performance Metrics

**Velocity:**

- Total plans completed: 3
- Average duration: N/A
- Total execution time: 0 hours

**By Phase:**

| Phase | Plans | Total | Avg/Plan |
|-------|-------|-------|----------|
| 01 | 3 | - | - |

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

### Pending Todos

None yet.

### Blockers/Concerns

- Phase 3: la home des écrans About/Partners (feature-settings vs nouveau feature-about) reste une décision ouverte à trancher avant l'extraction des feature modules (voir research/SUMMARY.md)
- Phase 3: l'export() du framework umbrella iOS doit être validé par un spike avant la découpe complète des modules — risque architectural le plus élevé du chantier (three-framework problem)
- Phase 2: versions AGP 9.x / KGP / KSP2 à revérifier via `android docs search` juste avant l'implémentation (recherche datée, tooling en mouvement rapide)
- Follow-up (not blocking): bump targetSdk 36->37 in a dedicated future stage after reviewing Android 17's behavior-change surface (kb://android/about/versions/17/behavior-changes-all / -17) — deliberately deferred from 02-03 per user direction to avoid folding a runtime-behavior-change decision into the Compose-BOM commit
- Follow-up (not blocking): the firebase-auth-ktx -> firebase-auth:24.2.0 dependency substitution in androidApp/build.gradle.kts is version-coupled to the pinned firebaseBom (34.19.0); re-verify the literal version if firebaseBom is bumped again in a future stage

## Deferred Items

Items acknowledged and deferred at milestone close, most recent first:

| Category | Item | Status | Deferred At | Milestone |
|----------|------|--------|-------------|-----------|
| *(none)* | | | | |

## Session Continuity

Last session: 2026-09-19T08:44:05.821Z
Stopped at: Completed 02-04-PLAN.md
Resume file: None
