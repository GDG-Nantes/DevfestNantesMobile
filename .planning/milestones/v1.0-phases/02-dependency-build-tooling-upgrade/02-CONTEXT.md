# Phase 2: Dependency & Build Tooling Upgrade - Context

**Gathered:** 2026-09-17
**Status:** Ready for planning

<domain>
## Phase Boundary

The project's toolchain and dependency graph are upgraded to the current stable stack — Kotlin 2.4.0, Gradle 9.7.1, AGP 9.2.0 with the new `com.android.kotlin.multiplatform.library` plugin, Compose BOM 2026.08.00, Apollo GraphQL 5.0.1, and the latest stable Firebase BOM/Coroutines/kotlinx-serialization/kotlinx-datetime — with zero observable behavior change for users, each dependency group staged and verified independently. Build files migrate to the Gradle Declarative DSL wherever AGP/KMP support allows, with any module left on `.kts` explicitly documented. No architecture/multi-module split, no DI migration, no test-coverage expansion — that's Phases 3-5.

</domain>

<decisions>
## Implementation Decisions

### Staging order & granularity
- **D-01:** Bump dependency groups sequentially in one PR, one commit per group: Kotlin 2.4.0 → AGP 9.2.0 + new KMP plugin → Compose BOM 2026.08.00 → Apollo 5.0.1 → Firebase BOM/Coroutines/kotlinx-serialization/kotlinx-datetime → Gradle Declarative DSL pilot. Each group gets its own commit, not its own PR — one PR per group was considered and rejected as too much overhead for 5-6 stages. — **Reversibility:** reversible — commit-level granularity, easy to `git revert` a single stage if needed.
- **D-02:** "Green" between stages means: full CI (Android + iOS) passes on the pushed commit, AND one local sanity build/run per group (not a full manual regression) before starting the next group. Local build catches things CI wouldn't (e.g. Compose Preview issues) without a full manual pass at every single stage.
- **D-03:** If a dependency group hits a blocker (transitive conflict, a target version that doesn't build), pin to the newest version that actually builds and document the gap versus the REQUIREMENTS.md target (in STATE.md/PROJECT.md) rather than stopping the phase — mirrors the already-agreed DCL partial-migration fallback for BUILD-07. Escalating to the user for every blocker was explicitly rejected as too disruptive to a 5-6-stage sequential plan.

### AGP 9 / KMP plugin verification depth
- **D-04:** After swapping `shared`'s `kotlin.multiplatform` + `com.android.library` coexistence for `com.android.kotlin.multiplatform.library`, verification is CI green + one manual smoke pass using the `android` CLI (`android run`, `android layout`/`screen capture`) confirming Agenda/Speakers/Venue/Bookmarks still render and navigate — not a full per-screen manual regression, since this stage is meant to be build-system-only.
- **D-05:** The AGP 9 plugin swap is verified Android-only for this stage. iOS only needs to stay CI-green (via the Cocoapods/Kotlin Native framework build) — no separate manual iOS smoke pass here. Deeper iOS/framework risk (the "three-framework problem") is explicitly Phase 3's concern, not this phase's.

### Apollo 5.0.1 cache-regression guard
- **D-06:** Losing cached Apollo GraphQL data across this migration is explicitly acceptable — user confirmed the app is being prepared for next year's conference, where all content (agenda, speakers, venue) will be replaced anyway and bookmarks don't need to persist across this specific update. **This relaxes the project's general "no regression" constraint specifically for Apollo's cached data during this migration only** — it does not extend to any other behavior, screen, or the separate SharedPreferences-backed `BookmarksStore`. Flag this scoping explicitly to the researcher/planner so it isn't misread as a blanket relaxation.
- **D-07:** Verification of the Apollo 5.0.1 bump (including the `ApolloStore`→`CacheManager` rename and binary cache format change) is functional-only: confirm the normalized cache mechanism still works post-migration (e.g., a repeated query hits cache, offline/airplane-mode still shows the last-loaded data) — no before/after data-preservation test, no comparison against pre-migration cache contents.

### Gradle Declarative DSL pilot scope
- **D-08:** Pilot `.gradle.dcl` on one low-risk leaf module rather than deferring DCL entirely or attempting a full-project migration. Document what worked and what's blocked; `androidApp`/`shared` stay on `.kts` since AGP/KMP DCL support is unlikely to cover them yet — satisfies BUILD-07's "partial migration, documented" fallback.
- **D-09:** The exact pilot module is Claude's discretion at implementation time (researcher/planner picks whichever current build file is simplest and lowest-risk — likely `buildSrc` or a small standalone config module) rather than a module fixed now. This choice may also be revisited once Phase 3's multi-module split changes what "leaf module" even means.

### Claude's Discretion
- Exact pilot module for the Gradle Declarative DSL trial (D-09) — user deferred this to implementation time.
- Exact commands/scripts used for the local sanity build per staging group (D-02) and the manual smoke pass for AGP 9 (D-04) — user cares about the checkpoint existing, not the specific tooling invocation, beyond using the `android` CLI per AGENTS.md.

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project-level context
- `.planning/PROJECT.md` — core value, constraints (must use `android` CLI for Android tasks, GitHub Actions stays CI platform), out-of-scope list, Key Decisions table
- `.planning/REQUIREMENTS.md` §BUILD — BUILD-01 through BUILD-07 (this phase's locked requirement targets: Kotlin 2.4.0, AGP 9.2.0 + new KMP plugin, Gradle 9.7.1, Compose BOM 2026.08.00, Apollo 5.0.1, Firebase/Coroutines/serialization/datetime latest stable, DCL partial migration)
- `.planning/ROADMAP.md` §Phase 2 — goal, success criteria, dependency on Phase 1
- `.planning/STATE.md` — notes that AGP 9.x/KGP/KSP2 versions need re-verification via `android docs search` before implementation (tooling moves fast; research is dated 2026-09-12)
- `.planning/research/SUMMARY.md` — full staged-upgrade rationale, recommended exact versions (Kotlin 2.4.0, AGP 9.2.0 Path A, Compose BOM 2026.08.00, Apollo Kotlin 5.0.1, Firebase BOM 34.18.0, Coroutines 1.11.0, kotlinx-serialization 1.11.0, kotlinx-datetime 0.8.0 — note the breaking `Instant`/`Clock` changes since 0.7.0, budget real migration effort not a drive-by bump), Critical Pitfalls 1-2 (runner-image drift, compounded bumps)

### Codebase maps
- `.planning/codebase/STACK.md` — current versions this phase upgrades from (Kotlin 2.2.0, AGP 8.13.0, Gradle 8.13.0, Compose BOM 2025.09.01, Apollo 4.3.3, Firebase BOM 33.16.0, Coroutines 1.10.2, kotlinx-serialization 1.9.0, kotlinx-datetime 0.6.2)
- `.planning/codebase/ARCHITECTURE.md` — known anti-patterns to avoid reintroducing during dependency bumps (println for GraphQL errors, direct Firebase refs in ViewModels)

### Prior phase context
- `.planning/phases/01-ci-pipeline-fixed-optimized/01-CONTEXT.md` — CI pipeline decisions this phase's CI checkpoints depend on (branch-aware Gradle cache, shared `android-setup` composite action, dynamic iOS simulator/Xcode resolution)

No external specs beyond the above — requirements fully captured in REQUIREMENTS.md §BUILD and the decisions above.

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `gradle/libs.versions.toml` — version catalog is the single source of truth for every dependency bumped in this phase; each staged commit (D-01) updates the relevant catalog entries.
- `.github/actions/android-setup/action.yml` — composite action wrapping Gradle setup with branch-aware caching (Phase 1); each staged CI push (D-02) runs through this unchanged.
- `DevFestNantesStoreContractTest` / `DevFestNantesStoreMocked` — existing Store test pattern; relevant if the researcher decides a lightweight functional cache check (D-07) is easiest expressed as an extension of this contract test.

### Established Patterns
- `shared/build.gradle.kts` currently applies `kotlin.multiplatform` + `com.android.library` together — this is the exact coexistence AGP 9 forbids; BUILD-02/D-04/D-05 target this file specifically.
- One-change-per-commit, green-CI-checkpoint pattern was already used successfully in Phase 1 (see `01-CONTEXT.md`) — D-01/D-02 extend the same discipline to dependency staging.

### Integration Points
- `androidApp/build.gradle.kts` and `shared/build.gradle.kts` are the two build files most affected by the AGP 9 plugin swap (D-04/D-05).
- Apollo cache configuration (`apollo-normalized-cache-sqlite` per STACK.md) is the integration point for D-06/D-07's functional-only verification.

</code_context>

<specifics>
## Specific Ideas

- The app is being prepared for next year's DevFest Nantes conference — agenda/speakers/venue content will be fully replaced and bookmarks don't need to survive this specific update. This is why D-06 relaxes the "no regression" bar specifically for Apollo's cached data, and only for this migration.

</specifics>

<deferred>
## Deferred Ideas

None — discussion stayed within Phase 2 scope. iOS framework/umbrella verification depth (raised while discussing D-05) is already tracked as Phase 3's "three-framework problem" spike in STATE.md and research/SUMMARY.md, not a new deferred item.

</deferred>

---

*Phase: 2-dependency-build-tooling-upgrade*
*Context gathered: 2026-09-17*
