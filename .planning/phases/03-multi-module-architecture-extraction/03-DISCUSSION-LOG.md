# Phase 3: Multi-Module Architecture Extraction - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-09-22
**Phase:** 03-multi-module-architecture-extraction
**Areas discussed:** DI during the split, Feature boundaries, iOS umbrella framework, Rollout & conventions

---

## DI during the split

| Option | Description | Selected |
|--------|-------------|----------|
| Keep Hilt | Hilt keeps working across new modules; Koin entirely Phase 4 | ✓ |
| Koin modules now | Per-feature Koin `module { }` at creation, SC3 literally | |
| Hilt + empty Koin stubs | Hilt wiring, empty Koin placeholders | |

| Option | Description | Selected |
|--------|-------------|----------|
| Keep one AppModule | AppModule stays in :androidApp; features carry only @HiltViewModel | ✓ |
| Split per Android module | Each Android module gets its own @Module/@InstallIn | |
| You decide | Planner picks | |

| Option | Description | Selected |
|--------|-------------|----------|
| Into core androidMain | Service impls move next to their interface in core androidMain | ✓ |
| Stay in :androidApp | Only interfaces/models move | |
| Case by case | Planner decides per service | |

| Option | Description | Selected |
|--------|-------------|----------|
| Amend roadmap | Update SC3 / ARCH-03 so the Koin clause reads as Phase 4 | ✓ |
| Note in CONTEXT only | Leave text, verifier treats as accepted deviation | |

**Notes:** ROADMAP SC3 and REQUIREMENTS ARCH-03 amended in the same commit as CONTEXT.md.

---

## Feature boundaries

| Option | Description | Selected |
|--------|-------------|----------|
| feature-about + feature-settings | About tab + Partners / Settings + DataCollection + Legal | ✓ |
| All in feature-settings | Fold About/Partners into settings | |
| About in feature-venue | Group informational screens | |

| Option | Description | Selected |
|--------|-------------|----------|
| No feature-bookmarks | Store in core-data, toggle + VM in core-ui; amend ARCH-03 | ✓ |
| UI-only feature-bookmarks | Breaks no-feature→feature rule | |
| Fold into feature-agenda | Session-detail gets toggle via callback/slot | |

| Option | Description | Selected |
|--------|-------------|----------|
| App owns graph, features expose entries | NavGraphBuilder extensions, string routes kept | |
| Same + type-safe routes | Migrate to @Serializable routes during move | |
| Feature-home module | Home shell as its own feature | |

**User's choice (free text):** "We should later migrate to Navigation 3. What is the best practice then?" — Claude fetched `kb://android/guide/navigation/navigation-3/modularize` (api/impl modules, NavKeys in api, `EntryProviderScope` entry builders in impl, app-owned `NavDisplay`, no NavController in features) and re-asked:

| Option | Description | Selected |
|--------|-------------|----------|
| Nav3-ready on Nav2 | Stateless feature entry points with callbacks, no NavController in features, app owns NavHosts; Nav3 deferred | ✓ |
| Migrate to Nav3 in Phase 3 | One pass, but bundles nav-behavior change | |
| Nav3-ready + NavKeys now | Also introduce @Serializable routes now | |

| Option | Description | Selected |
|--------|-------------|----------|
| Used by ≥2 features → core-ui | Plus theme + UiState; single-use stays in feature | ✓ |
| Everything generic → core-ui | Move all components/utils up front | |
| You decide | Planner per file | |

---

## iOS umbrella framework

| Option | Description | Selected |
|--------|-------------|----------|
| Tracer first plan | build-logic + core-model re-exported by :shared, iOS CI + sim run gate | ✓ |
| Throwaway spike branch | Prototype and discard | |
| No spike | Cut everything then fix iOS | |

| Option | Description | Selected |
|--------|-------------|----------|
| Thin umbrella, name 'shared' kept | Path, baseName, Xcode build phase, `import shared` unchanged | ✓ |
| Rename to :ios-umbrella | Requires Xcode + Swift import edits | |
| Keep code in shared | Fails ARCH-02 | |

| Option | Description | Selected |
|--------|-------------|----------|
| Only what Swift touches | export core-model/data/analytics; core-network internal | ✓ |
| Export every KMP module | Leaks Apollo types into Swift header | |

| Option | Description | Selected |
|--------|-------------|----------|
| CI + local sim at checkpoints | iOS CI every step + sim smoke at tracer and after core-data | ✓ |
| Local sim after every move | Safest, slow | |
| CI only | No manual sim run | |

**Notes:** Scouting found iOS uses `embedAndSignAppleFrameworkForXcode` direct integration, not CocoaPods as research assumed.

---

## Rollout & conventions

**Build-logic:** user first asked for an explanation of `build-logic` vs `buildSrc` (Claude explained: buildSrc = auto-built, on every script classpath, any change invalidates the whole build; included build = independent build publishing `devfest.*` plugin IDs, per-plugin invalidation, direct catalog access, NIA/Gradle-recommended). Then:

| Option | Description | Selected |
|--------|-------------|----------|
| build-logic included build | build-logic/convention, AndroidSdk moved, buildSrc deleted | ✓ |
| Keep buildSrc | Less setup, whole-build invalidation | |

| Option | Description | Selected |
|--------|-------------|----------|
| Nested dirs, packages unchanged | :core:model etc., pure moves | |
| Flat dirs, packages unchanged | :core-model etc. | |
| Nested dirs + repackage | Packages renamed to match modules | ✓ |

| Option | Description | Selected |
|--------|-------------|----------|
| Move commit, then repackage commit | Pure git mv, then package rename, per module | ✓ |
| Move + repackage together | One commit per module | |
| Repackage everything at the end | Single final pass | |

| Option | Description | Selected |
|--------|-------------|----------|
| Move with owner | Feature/core-ui/app ownership, fr/night in lockstep, non-transitive R | ✓ |
| All in core-ui | Resource dumping ground | |
| Leave in app for now | Not viable | |

| Option | Description | Selected |
|--------|-------------|----------|
| One PR, commit per step | Bottom-up order, CI green per step, Android smoke at 3 checkpoints | ✓ |
| PR per wave | 3 PRs | |
| PR per module | ~12 PRs | |

| Option | Description | Selected |
|--------|-------------|----------|
| Move existing fakes now | StoreMocked + stubs to core-testing if test/preview-only | ✓ |
| Empty skeleton | Leave moves to Phase 4 | |

| Option | Description | Selected |
|--------|-------------|----------|
| .kts + quick live re-check | New modules on .kts, re-run DCL status check | ✓ |
| Try DCL on a new module | Second pilot | |

---

## Claude's Discretion

- Placement of services without a core interface and of androidApp `core/` utilities
- Convention plugin names/granularity, exact package scheme
- File-by-file core-ui and resource ownership application
- How tests/jvm target move with their code; per-module detekt wiring

## Deferred Ideas

- Navigation 3 migration (own phase, not yet in ROADMAP)
- Type-safe Nav2 routes (superseded by Nav3)
- Feature api/impl split (ARCH-V2-01), graph lint in CI (CICD-V2-01) — already tracked
