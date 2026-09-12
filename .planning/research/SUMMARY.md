# Project Research Summary

**Project:** DevFest Nantes app — KMP technical modernization
**Domain:** Brownfield KMP mobile app modernization (Android + iOS, no new user-facing features)
**Researched:** 2026-09-12
**Confidence:** MEDIUM-HIGH

## Executive Summary

This is a purely technical modernization of an existing Kotlin Multiplatform conference app (Android/Compose + iOS/SwiftUI, sharing business logic via a `shared` KMP module). Experts build this kind of upgrade the way Now in Android does: a one-directional `app to feature-* to core-*` module graph, decentralized DI via Koin's per-module `module { }` DSL, `commonTest`-first business-logic testing with fakes over mocks, and CI split across cheap Android runners and expensive macOS/iOS runners with aggressive Gradle/Konan caching. This project's specific twist -- iOS UI is native SwiftUI, not Compose Multiplatform -- means only data/domain modules (`core-model`, `core-network`, `core-data`, `core-analytics`) can be true KMP `commonMain`; every Compose UI module (`core-ui`, `feature-*`) is Android-only, and `shared` must collapse into a thin Cocoapods umbrella framework rather than being modularized 1:1 into iOS frameworks.

The recommended approach is staged and strictly sequenced to avoid stacking independent sources of risk: (1) fix the iOS CI simulator-resolution root cause first (pin the runner image, not just Xcode/device name) so every subsequent phase has trustworthy CI signal; (2) upgrade dependencies (Kotlin 2.4.0, AGP 9.2.0 -- deciding the AGP-9 KMP-plugin path explicitly, Compose BOM 2026.08.00, Apollo Kotlin 5.0.1, Koin BOM 4.1.1, kotlinx libs) one logical group at a time with a green build after each, and treat the Gradle Declarative DSL as out of scope beyond one low-risk pilot module (it remains experimental); (3) extract the module skeleton bottom-up (`core-model` to `core-network`/`core-analytics` to `core-data` to `core-testing` to `core-ui` to features, least-coupled feature first, `feature-session-detail` last) while keeping Hilt working across modules and validating the iOS umbrella-framework export early via a spike; (4) migrate Hilt to Koin as its own separate pass once the module skeleton is stable, module by module, with `checkModules()`/`verify()` wired into CI to replace Hilt's lost compile-time safety net; (5) retrofit test coverage last, prioritizing ViewModels/Store/mappers over trivial code, after first hardening test fixtures (seed `StoreStubs.kt`'s randomness, replace `SimpleDateFormat`).

The dominant risk across this whole milestone is **conflating independent variables**: bumping five major dependencies in one commit, mixing module extraction with DI-framework swaps, or letting "no regression" erode via incidental cleanup during refactors (Apollo cache-key behavior, Firebase Remote Config timing, `!!` navigation-argument fixes). The single highest-leverage architectural risk specific to this project is the iOS "three-framework problem" -- exporting each new Kotlin module as its own iOS framework instead of maintaining one umbrella framework -- which must be designed and spike-validated before the module split, not discovered afterward. Mitigation throughout is the same principle: one change per commit, a green build/CI checkpoint after each stage, and explicit before/after verification (cache-behavior tests, `checkModules()`, `android run`/`layout`/`screen capture`) rather than trusting "it compiles."

## Key Findings

### Recommended Stack

Kotlin 2.4.0, Gradle 9.7.1, Compose BOM 2026.08.00, Apollo Kotlin 5.0.1, Koin BOM 4.1.1, Firebase BOM 34.18.0 (Android-only), Kotlin Coroutines 1.11.0, kotlinx-serialization 1.11.0, kotlinx-datetime 0.8.0 (note: 0.7.0 introduced breaking `Instant`/`Clock` changes -- budget real migration effort, not a drive-by bump). The single highest-leverage/highest-risk stack decision is **AGP 9 vs staying on AGP 8.13.0** -- AGP >=9.0 forbids applying `com.android.library` + `org.jetbrains.kotlin.multiplatform` in the same module, requiring the new `com.android.kotlin.multiplatform.library` plugin for every KMP module. Recommendation: adopt AGP 9.2.0 (Path A) as the first concrete build-system change, before the multi-module split, since the module graph is being rebuilt from scratch anyway. Gradle Declarative DSL (`.gradle.dcl`) remains explicitly experimental -- do not attempt a full migration; at most pilot it on one leaf, non-Android module. Use plain Koin `module { }` DSL (not Koin Annotations/KSP) for this migration to avoid stacking a third build-graph-churn axis on top of AGP 9 + Kotlin 2.4 + module restructuring.

**Core technologies:**
- Kotlin 2.4.0 -- required baseline for AGP 9's built-in Kotlin support and current kotlinx library versions
- AGP 9.2.0 (Path A, recommended) -- new `com.android.kotlin.multiplatform.library` plugin is the long-term-supported way to declare KMP+Android modules; do it once, timed before the module split
- Koin BOM 4.1.1 -- native KMP DI with no annotation-processor requirement on Kotlin/Native, replacing Hilt which cannot run in `shared`
- Apollo Kotlin 5.0.1 -- current GraphQL client major line; breaking changes (binary cache format, `ApolloStore`->`CacheManager` rename) are scoped and non-blocking since this project has no subscriptions
- Compose BOM 2026.08.00 -- bumps `compileSdk` to 37; Android-only concern since iOS UI is native SwiftUI

### Expected Features

This is an engineering-capability modernization, not a user feature set -- "features" here means the credibility bar for a modernized KMP codebase.

**Must have (table stakes):**
- One-directional module dependency graph (`app -> feature-* -> core-*`, enforced via convention plugins)
- `core-*` modules for cross-cutting concerns (model, network, data, analytics, ui, testing) and `feature-*` per user journey (agenda, speakers, venue, bookmarks, session-detail, settings)
- Single iOS umbrella Kotlin/Native framework aggregating all KMP modules
- One Koin module per Gradle module, composed at a single `startKoin()` entry point; constructor injection everywhere (no scattered `get()`)
- `commonTest`-first business logic tests extending the existing `DevFestNantesStoreContractTest`/`DevFestNantesStoreMocked` pattern
- Root-cause fix for the broken iOS CI simulator resolution (pin runner image + explicit OS, not just device name)
- Gradle build cache + Konan cache in CI; Android/iOS jobs split across `ubuntu-latest`/`macos-latest`

**Should have (differentiators):**
- Koin `checkModules()`/`verify()` graph verification test
- `core-testing` module centralizing shared fakes
- Dependency-graph lint enforcement in CI

**Defer (v2+/future):**
- `api`/`impl` submodule split per feature (only for highest-churn features, if ever)
- Koin Annotations/compiler plugin adoption
- Compose screenshot/visual regression testing (Paparazzi/Roborazzi)
- Full NIA taxonomy (sync/benchmark/lint/test-app modules) -- explicit anti-feature, scope creep for this app's size
- Compose Multiplatform for iOS UI -- explicit anti-feature, out of scope, would be a UI rewrite

### Architecture Approach

Adapt Now in Android's module graph to the fact that this app shares only business logic with iOS, not UI: `core-model`, `core-network`, `core-data`, `core-analytics`, `core-testing` are true KMP `commonMain` modules; `core-ui` and all `feature-*` modules are Android-only Compose libraries; `shared` collapses into a thin Cocoapods umbrella that explicitly `export()`s every KMP module iOS touches. Feature modules never depend on each other -- cross-feature navigation goes through callbacks owned by `app`'s `NavHost`, and cross-feature shared state (bookmarks) is pushed down into `core-data`'s `BookmarksStore`. Migration proceeds bottom-up: convention plugins -> `core-model` -> `core-network`/`core-analytics` -> `core-data` (checkpoint: full behavioral parity) -> `core-testing` -> `core-ui` -> features (venue -> settings -> speakers -> bookmarks -> agenda -> session-detail last), with the Hilt->Koin migration deliberately kept as a separate, later pass.

**Major components:**
1. `core-model` -- pure domain data classes, zero dependencies, root of the graph
2. `core-network` -- Apollo GraphQL client/codegen, depends only on `core-model`
3. `core-data` -- Store abstraction (`DevFestNantesStore`, `GraphQLStore`, `BookmarksStore`, mappers), depends on `core-model` + `core-network`
4. `core-analytics` -- `AnalyticsService` interface in `commonMain`, Firebase impl Android-only
5. `core-ui` / `feature-*` -- Android-only Compose UI + ViewModels + per-feature Koin modules
6. `shared` -- thin iOS umbrella framework re-exporting the KMP core modules

### Critical Pitfalls

1. **Runner-image drift undoes the CI fix** -- pinning Xcode/device name without pinning the `macos-latest` runner image tag means the fix silently regresses on the next GitHub image bump. Pin the runner image explicitly and resolve simulator destination dynamically via `xcrun simctl list`.
2. **Simultaneous major dependency bumps compound breaking changes** -- upgrading Kotlin/AGP/Compose/Apollo/Firebase together makes root-causing any failure impossible. Stage each dependency group as its own commit with a green build checkpoint.
3. **iOS "three-framework problem"** -- exporting each new Kotlin module as its own iOS framework duplicates shared types and causes link/runtime conflicts. Always maintain a single umbrella framework; validate with a spike before the full module cut.
4. **Koin trades compile-time safety for runtime resolution** -- a missing `module { }` registration for a newly extracted module compiles fine and only crashes at runtime. Wire `checkModules()`/`verify()` into CI as part of the DI migration itself, not as a follow-up.
5. **"No regression" collides with incidental cleanup** -- Apollo bumps can silently change cache-key behavior; moving `FirebaseAnalyticsService` can shift event timing; tempting drive-by fixes (`println`->Timber, `!!` navigation asserts) expand scope invisibly. Snapshot/test behavior before changes and keep pure-move commits separate from behavior-changing ones.

## Implications for Roadmap

Based on research, suggested phase structure (matches PROJECT.md's own stated sequencing, validated by research):

### Phase 1: Fix iOS CI (root cause)
**Rationale:** Currently broken; every subsequent phase needs trustworthy CI signal to validate "no regression."
**Delivers:** Deterministic iOS simulator resolution (pinned runner image + explicit OS/device via `xcrun simctl list`), plus Gradle/Konan CI caching and Android/iOS job matrix split.
**Addresses:** Table-stakes CI features from FEATURES.md.
**Avoids:** Pitfall 1 (runner-image drift), reduces macOS runner cost.

### Phase 2: Dependency & build-tooling upgrade (staged)
**Rationale:** Must happen before modularization so the new module graph isn't built on a build system also being rewritten; must be staged internally to isolate breaking changes.
**Delivers:** Kotlin 2.4.0, AGP 9.2.0 (with the `com.android.kotlin.multiplatform.library` plugin decision made explicitly), Compose BOM 2026.08.00, Apollo 5.0.1, kotlinx-datetime 0.8.0, one documented pilot (or explicit non-adoption) of `.gradle.dcl`.
**Uses:** STACK.md's full version matrix; each dependency group bumped and green-built separately.
**Avoids:** Pitfall 2 (compounded bumps), Pitfall 3 (DCL all-or-nothing), Pitfall 7's Apollo cache-behavior regression risk (add a cache-behavior test before bumping).

### Phase 3: Multi-module architecture (core-* + feature-*)
**Rationale:** Requires the Phase 2 build tooling to be settled first; must precede full Koin migration since Hilt keeps working fine across multiple Android modules.
**Delivers:** Convention plugins, bottom-up module extraction (`core-model` -> `core-network`/`core-analytics` -> `core-data` -> `core-testing` -> `core-ui` -> features, least-coupled first), iOS umbrella framework with explicit `export()` of every touched KMP module, resolved "where do About/Partners screens go" decision.
**Implements:** ARCHITECTURE.md's full module graph and dependency-direction pattern.
**Avoids:** Pitfall 4 (three-framework problem) -- validate with a spike before the full cut; Anti-Pattern 4 (don't bundle this with the Koin migration).

### Phase 4: Hilt -> Koin migration
**Rationale:** Orthogonal to modularization; doing it after the module skeleton is stable means each step changes exactly one thing, so regressions are attributable.
**Delivers:** One Koin module per Gradle module, single `startKoin()` entry point, `expect`/`actual` platform modules, constructor injection everywhere, `checkModules()`/`verify()` wired into CI.
**Uses:** STACK.md's Koin setup pattern; ARCHITECTURE.md Pattern 2.
**Avoids:** Pitfall 5 (stale Hilt code / scope semantics gaps -- clean build after annotation removal, explicit scope-mapping table) and Pitfall 6 (runtime-only wiring failures -- `checkModules()` gate before Phase 5).

### Phase 5: Test coverage retrofit
**Rationale:** Writing tests against not-yet-testable, Hilt-injected code is wasted effort; must follow DI decentralization so fakes/constructor injection make tests trivial.
**Delivers:** Seeded `StoreStubs.kt`, `SimpleDateFormat` replaced in tested paths, expanded `commonTest` coverage for ViewModels/Store/mappers extending `DevFestNantesStoreContractTest`, `core-testing` module of shared fakes.
**Addresses:** FEATURES.md's explicit PROJECT.md priority (business-logic coverage, not a coverage-percentage target).
**Avoids:** Pitfall 8 (flaky/low-value tests from unhardened fixtures) -- pull the seeding/date fixes forward as prerequisites, not mid-phase discoveries.

### Phase Ordering Rationale

- CI must come first because every later phase depends on trustworthy build/test signal to prove "no regression."
- Dependency upgrade precedes modularization so the module graph isn't built on a build system that's also being rewritten; AGP 9's new KMP plugin shape should be adopted before, not during, the module split (doing it twice is wasteful).
- Modularization precedes the DI migration because Hilt already works fine across multiple Android modules -- decoupling "where code lives" from "how it's injected" makes each step's regressions attributable to one cause.
- Test coverage comes last because constructor-injected, Koin-wired code is what makes fakes/tests cheap to write; testing Hilt-injected legacy code first would be discarded work.
- Within Phase 3, the iOS umbrella-framework design must be validated via a spike before the full module cut -- it's a hard architectural blocker discovered too late otherwise.

### Research Flags

Phases likely needing deeper research during planning:
- **Phase 2 (dependency upgrade):** AGP 9's Declarative-DSL-adjacent new DSL types and exact KGP/KSP2 patch-version pairings are fast-moving; use `android docs search` at implementation time rather than trusting this research's exact version numbers.
- **Phase 3 (multi-module + iOS umbrella framework):** The `export()` mechanics for the Cocoapods umbrella framework are a known easy-to-miss gotcha with thin community documentation for this exact shape (KMP business logic + native SwiftUI, not CMP) -- validate directly against this project's Xcode/Podfile setup.

Phases with standard patterns (skip research-phase):
- **Phase 1 (CI fix):** Well-documented GitHub Actions/`xcrun simctl` pattern, multiple corroborating sources.
- **Phase 4 (Hilt->Koin):** Koin's official migration docs and Now in Android's own precedent give a clear, standard pattern.
- **Phase 5 (test coverage):** Existing project patterns (`DevFestNantesStoreContractTest`/`Mocked`) already establish the template to extend.

## Confidence Assessment

| Area | Confidence | Notes |
|------|------------|-------|
| Stack | MEDIUM-HIGH | Version numbers cross-checked against official release notes/blogs (HIGH); AGP 9/Declarative DSL guidance is MEDIUM due to fast-moving, recently-shipped tooling with thin real-world migration reports |
| Features | MEDIUM | Cross-checked across official docs (Now in Android, kotlinlang.org, insert-koin.io) and multiple independent practitioner write-ups; no project-specific benchmarking possible since this is an internal-only technical modernization |
| Architecture | MEDIUM | Module-boundary theory is HIGH-confidence (official NIA doc, official Kotlin migration docs); exact incremental step ordering and Koin wiring recommendations are a synthesis adapted to this codebase -- treat as a strong recommendation, validate each step with `android run`/`android layout` |
| Pitfalls | MEDIUM | Web-sourced, cross-checked against official docs/repos; project-specific facts (CacheKeyGenerator, StoreStubs.kt, CONCERNS.md items) verified directly against this repo's code |

**Overall confidence:** MEDIUM-HIGH

### Gaps to Address

- Exact AGP 9.x / KGP / KSP2 patch-version compatibility should be re-verified via `android docs search` / Maven Central immediately before Phase 2 implementation -- these are fast-moving and this research's exact patch tags may drift.
- The "About/Partners screens" home (fold into `feature-settings` vs. a new `feature-about`) is flagged as an explicit roadmap decision point, not resolved by research -- decide before Phase 3's feature-extraction step.
- Gradle Declarative DSL's exact support surface (Version Catalogs, Secrets plugin, BuildConfig fields) should be re-checked at Phase 2 implementation time given its experimental, fast-moving status -- do not assume this research's snapshot is still current.
- No project-specific validation exists yet for the iOS umbrella-framework `export()` approach against this project's actual Podfile/Xcode setup -- treat the Phase 3 spike as mandatory, not optional.

## Sources

### Primary (HIGH confidence)
- https://developer.android.com/build/releases/agp-9-0-0-release-notes and agp-9-2-0-release-notes -- official AGP release notes
- https://kotlinlang.org/docs/multiplatform/multiplatform-project-agp-9-migration.html -- official Kotlin AGP-9 KMP migration guide
- https://kotlinlang.org/docs/whatsnew24.html -- Kotlin 2.4.0 release
- https://insert-koin.io/docs/reference/koin-core/kmp-setup/ and /docs/reference/koin-android/hilt-migration/ -- official Koin KMP setup and Hilt migration docs
- https://github.com/android/nowinandroid/blob/main/docs/ModularizationLearningJourney.md -- official NIA modularization reference
- https://www.apollographql.com/docs/kotlin/v5/migration/5.0 -- official Apollo Kotlin 5 migration guide
- https://github.com/actions/runner-images (issues #13435, #12771, #12758) -- GitHub-official simulator/runner-image drift reports

### Secondary (MEDIUM confidence)
- https://blog.gradle.org/gradle-at-kotlinconf-2026 / declarative-gradle -- Gradle's own vendor blog on Declarative DSL's experimental status
- https://blog.kotzilla.io/koin-4.1-is-here and migrate-from-hilt-to-koin -- Koin maintainer blog
- https://medium.com/xorum-io/three-framework-problem-with-kotlin-multiplatform-mobile-16267c5afa53 -- community deep-dive on the iOS three-framework problem
- https://ajailani.medium.com/modularization-in-a-kotlin-multiplatform-project-81e06d2170b6 -- community KMP modularization write-up

### Tertiary (LOW confidence)
- Various single-source Medium/blog posts cited in STACK.md/ARCHITECTURE.md/PITFALLS.md for Koin Annotations multi-module caveats and KMP CI patterns -- corroborated by official docs but treated as secondary confirmation only

---
*Research completed: 2026-09-12*
*Ready for roadmap: yes*
