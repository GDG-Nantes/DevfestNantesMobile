# Phase 2: Dependency & Build Tooling Upgrade - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-09-17
**Phase:** 2-dependency-build-tooling-upgrade
**Areas discussed:** Staging order & granularity, AGP 9 / KMP plugin verification depth, Apollo 5.0.1 cache-regression guard, Gradle Declarative DSL pilot scope

---

## Staging order & granularity

| Option | Description | Selected |
|--------|-------------|----------|
| Sequential, one commit per group | Kotlin → AGP+plugin → Compose → Apollo → Firebase/kotlinx → DCL pilot, each its own commit, green build before next | ✓ |
| Sequential, one PR per group | Same order, but each group merged as its own PR | |
| Different order | User-described alternative order | |

**User's choice:** Sequential, one commit per group
**Notes:** None additional.

| Option | Description | Selected |
|--------|-------------|----------|
| CI green + one local build per group | Full CI (Android + iOS) green, plus one local sanity build per group | ✓ |
| CI green only | Rely entirely on CI, no local step | |
| Local build only, batch CI at the end | Verify locally, push/run CI only at natural checkpoints | |

**User's choice:** CI green + one local build per group

| Option | Description | Selected |
|--------|-------------|----------|
| Pin to newest version that builds, document why | Don't block the phase; land newest compatible version, document the gap | ✓ |
| Stop and escalate to the user | Halt the phase, ask before accepting any version below target | |

**User's choice:** Pin to newest version that builds, document why

---

## AGP 9 / KMP plugin verification depth

| Option | Description | Selected |
|--------|-------------|----------|
| CI green + one manual smoke pass | Run `android run`/`layout`/`screen capture` once after the plugin swap | ✓ |
| CI green only | Trust existing test suite and CI | |
| Full manual regression per screen | Walk every screen manually | |

**User's choice:** CI green + one manual smoke pass

| Option | Description | Selected |
|--------|-------------|----------|
| Android-only for this stage | iOS just needs CI green, no separate manual pass | ✓ |
| Verify both Android and iOS manually | Manual sanity build+run on iOS too | |

**User's choice:** Android-only for this stage
**Notes:** Deeper iOS/framework risk explicitly deferred to Phase 3's "three-framework problem" spike.

---

## Apollo 5.0.1 cache-regression guard

| Option | Description | Selected |
|--------|-------------|----------|
| Write a targeted cache-behavior test first | Add a test asserting current cache read/write behavior, rerun after bump | |
| Manual verification against existing tests only | Rely on existing suite + manual check | |
| Skip cache verification, trust Apollo's migration guide | Treat CacheManager rename as safe by design | |
| (free text) | User: losing cache is acceptable — next year's content fully replaces current data, bookmarks don't need to persist across this update | ✓ |

**User's choice:** Free text — cache data loss across this migration is acceptable
**Notes:** Explicitly scoped: this relaxes "no regression" only for Apollo's cached data during this specific migration, not for any other behavior or the separate SharedPreferences-backed BookmarksStore.

| Option | Description | Selected |
|--------|-------------|----------|
| Yes — functional check only, no data-preservation test | Verify cache mechanism works post-migration (repeated query hits cache, offline shows last-loaded data) | ✓ |
| No verification needed at all | Skip any dedicated cache check | |

**User's choice:** Yes — functional check only, no data-preservation test

---

## Gradle Declarative DSL pilot scope

| Option | Description | Selected |
|--------|-------------|----------|
| Pilot on one low-risk leaf module | Convert simplest/lowest-risk module to `.gradle.dcl`, document what worked/blocked | ✓ |
| No pilot this phase — defer DCL entirely | Explicitly decide not to attempt DCL this phase | |
| Attempt full migration, fall back per-module as blocked | Try converting everything, fall back where blocked | |

**User's choice:** Pilot on one low-risk leaf module

| Option | Description | Selected |
|--------|-------------|----------|
| Claude picks at implementation time | Researcher/planner picks the simplest/lowest-risk current build file | ✓ |
| Root `settings.gradle.kts` | Pilot specifically on the root settings file | |

**User's choice:** Claude picks at implementation time
**Notes:** Exact module list isn't finalized until Phase 3's multi-module split anyway.

---

## Claude's Discretion

- Exact pilot module for the Gradle Declarative DSL trial.
- Exact commands/scripts used for the local sanity build per staging group and the manual smoke pass for AGP 9 (beyond using the `android` CLI per AGENTS.md).

## Deferred Ideas

None — discussion stayed within Phase 2 scope. iOS framework/umbrella verification depth (raised while discussing AGP 9's iOS scope) is already tracked as Phase 3's "three-framework problem" spike, not a new deferred item.
