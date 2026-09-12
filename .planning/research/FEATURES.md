# Feature Research

**Domain:** KMP (Kotlin Multiplatform) engineering modernization — multi-module architecture, decentralized DI, test strategy, CI/CD (not user-facing features)
**Researched:** 2026-09-12
**Confidence:** MEDIUM (cross-checked across official docs — Now in Android repo, kotlinlang.org, insert-koin.io — and multiple independent practitioner write-ups; no project-specific benchmarking was possible)

## Note on Scope

This is a **brownfield, purely-technical modernization** (per `PROJECT.md`): no new user-facing features, no UX changes. "Table stakes / differentiators / anti-features" below describe the **engineering capability set** of a credibly modernized KMP codebase in 2025/2026, evaluated against this specific project's shape:

- `shared` (KMP commonMain, no androidApp dependency — good starting point) + `androidApp` (Compose, currently Hilt) + `iosApp` (**native SwiftUI**, not Compose Multiplatform UI).
- Because iOS UI is native SwiftUI (not CMP), most "Compose Multiplatform iOS UI testing" ecosystem content (XCTest-via-Gradle-wrapper for CMP semantics trees, CMP screenshot testing across 4 targets, Parikshan-style E2E) **does not apply** to this project's UI layer — only to shared `commonMain`/`iosMain` logic. This is flagged explicitly below so the roadmap doesn't chase irrelevant tooling.

## Feature Landscape

### Table Stakes (Expected in a "Modernized KMP Project" in 2025/2026)

Missing these makes the modernization look incomplete or performative — a reviewer (or future maintainer) would immediately flag the gap.

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| **One-directional module dependency graph** (`app → feature-* → core-*`, core never depends on feature) | This is the entire point of modularization — without it you have "many Gradle projects" not "a modular architecture." NIA's documented rule: a feature's `api` must not depend on another feature's `api`/`impl`; `impl` may only depend on other features' `api`. | MEDIUM | Enforce via Gradle convention plugins + module visibility, not just convention. Verify with a dependency-graph check task (`./gradlew :app:dependencies` or a lint rule) in CI. |
| **`core-*` modules for cross-cutting concerns** (data/store, network, ui/design-system, analytics, testing) | Standard NIA taxonomy; matches what's already implicit in this codebase (Store layer, Analytics layer, Domain/Model layer per `ARCHITECTURE.md`) — this is largely a *re-packaging* of existing layers into modules, not new design. | MEDIUM | Natural core module set for this project: `core-model`, `core-network` (Apollo/GraphQL), `core-data` (Store/BookmarksStore), `core-analytics`, `core-ui` (Compose theme/components), `core-testing` (fakes, test rules). |
| **`feature-*` modules per user-facing feature** (agenda, speakers, venue, bookmarks, session-detail, settings) | Matches PROJECT.md's explicit requirement; each feature module owns its ViewModel(s) + Compose screens + its own Koin module. | MEDIUM-HIGH | The riskiest table stake: existing code isn't organized this way today (everything lives flat in `androidApp`/`shared`). Extraction work, not net-new code. |
| **Convention plugins + version catalog for build logic** | Without shared build logic, N modules mean N copies of the same `android { }` / `kotlin { }` block — a classic modularization failure mode. `libs.versions.toml` already exists in this project. | LOW-MEDIUM | `build-logic` included build with `kmp-library`, `android-feature`, `compose` convention plugins, mirroring NIA's `build-logic/convention`. |
| **iOS umbrella framework for the shared side** | iOS cannot consume multiple Kotlin/Native frameworks cleanly — once `shared` is split into several Gradle modules, iOS still needs to link against **one** framework. | MEDIUM | Configure a single umbrella Kotlin/Native framework target that aggregates all KMP modules' `commonMain`/`iosMain` output; `iosApp` (SwiftUI) continues to import exactly one framework, unaware of the internal module split. |
| **One Koin module per Gradle module, composed at a single entry point** | This is the literal definition of "decentralized DI" requested in PROJECT.md, and is Koin's documented idiomatic KMP pattern (each module ships a `val xModule = module { ... }`; app-level `initKoin(additionalModules = ...)` composes them). | MEDIUM | Requires each `feature-*`/`core-*` module to expose its own `module { }` without needing to know about sibling modules; composition happens only at the app/composition-root layer. |
| **Constructor injection everywhere; no scattered `get()`/service-locator calls in business logic** | Koin's DSL makes it *possible* to call `get()` anywhere (global service locator), but that reintroduces the same untestable coupling Hilt migration is meant to fix. | LOW | Enforce via code review convention: ViewModels/Stores take dependencies as constructor params; only the DI wiring layer calls `get()`/`viewModel { }`. |
| **`expect`/`actual` (or Koin platform modules) for platform-bound dependencies** | Context (Android), file system, secure storage, etc. differ per platform and cannot live in `commonMain`. Standard, unavoidable KMP pattern — already implicitly needed since `shared` has no Android dependency today. | LOW-MEDIUM | e.g., `androidMain/di/PlatformModule.android.kt` provides `Context`-bound deps (SharedPreferences for BookmarksStore); `iosMain/di/PlatformModule.ios.kt` provides iOS equivalents. |
| **Business-logic tests live in `commonTest`, run on every platform** | This is *the* KMP testing table stake — "write once, test everywhere" for Store/mapper/pure-logic code, explicitly called out by Kotlin's own docs and matches PROJECT.md's priority (ViewModels, Store/repository, GraphQL→model mappers). | LOW-MEDIUM | `DevFestNantesStoreContractTest` already exists as `commonTest` and is the right pattern to extend — reuse it as the template rather than reinventing. |
| **Contract tests for Store abstraction, run against both real and mocked implementations** | Already present in this codebase (`DevFestNantesStoreContractTest` against `DevFestNantesStoreMocked`) — a genuine existing strength to preserve and extend to new/split Store modules. | LOW | Keep this pattern when Store is modularized into `core-data`; add contract coverage for `BookmarksStore` too if missing. |
| **Compose UI smoke tests for critical Android screens** | `MainActivityTest` already exists (`hasTestTag("topAppBar")` pattern); modernized projects keep at least this level per screen/module, not necessarily deep UI coverage. | LOW-MEDIUM | Move per-feature Compose tests into each `feature-*` module's own `androidTest`, using shared test utilities from `core-testing`. |
| **`kotlinx-coroutines-test` (`runTest`) for all async/Flow testing** | Already the pattern in this codebase; standard and non-negotiable for testing suspend/Flow-based Store and ViewModel logic. | LOW | No change needed — extend the existing pattern into new modules. |
| **Koin `checkModules()` / graph verification test** | Prevents the classic runtime-DI failure mode (missing binding discovered only when a screen crashes in production) — Koin ships this specifically to catch config errors compile-/test-time instead of at runtime. | LOW-MEDIUM | Add one lightweight JVM test per app target that calls Koin's module-verification API against the full composed graph. |
| **Split Android/iOS build jobs on separate runners, with a build matrix** | Confirmed both by official Kotlin docs and every practitioner source: Android (JVM-only) work belongs on cheap `ubuntu-latest` runners; only iOS/Xcode work needs `macos-latest`. Already implied by the fact iOS CI is broken independently of Android CI. | LOW-MEDIUM | `strategy: matrix: include: [{os: ubuntu-latest, target: android}, {os: macos-latest, target: ios}]`. |
| **Gradle build cache via `gradle/actions/setup-gradle`** | The single highest-leverage, near-zero-effort CI win for any Gradle project in 2025/2026; explicitly requested in PROJECT.md ("cache Gradle/Kotlin"). | LOW | Use `gradle-home-cache-cleanup: true` to avoid unbounded cache growth; no manual cache-key management needed — the action derives keys from build files. |
| **Konan (`~/.konan`) cache for Kotlin/Native compilation** | Native compiler artifact caching is repeatedly cited as "the single biggest win" for iOS build time in KMP CI — without it, every iOS CI run recompiles Kotlin/Native from scratch. | LOW | Straightforward `actions/cache` on `~/.konan`, keyed on Kotlin version + dependency lockfile hash. |
| **Fix the broken iOS simulator resolution (root cause, not a pin-and-hope patch)** | This is the *currently failing* CI step (PROJECT.md: "simulateur iPhone 16 introuvable"). The failure mode — a hardcoded device name that stops existing when the macOS runner image updates its simulator set — is a well-known, well-documented CI fragility pattern. | LOW-MEDIUM | Table stakes fix: resolve the destination dynamically (`xcrun simctl list devices available` + `xcodebuild -destination "platform=iOS Simulator,name=<resolved>,OS=<resolved>"`), or pin the macOS runner image to a specific version (e.g. `macos-14`) instead of `macos-latest` so the available simulator set is stable and known. Treat as its own micro-project since PROJECT.md already sequences it first. |

### Differentiators (Valuable, Not Required for Credibility)

These improve on the table-stakes baseline but a "good enough" modernization can ship without them.

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| **`api`/`impl` submodule split per feature (NIA's full pattern)** | Reduces recompilation blast radius (changing a feature's internals doesn't force recompilation of consumers that only depend on its `api`) and enforces stricter encapsulation than a single feature module. | HIGH | Real NIA overhead: doubles the module count. For a team of this project's likely size, judge against the "over-modularization" anti-feature below — may only be worth it for the 2-3 largest/most-churned features (agenda, session-detail), not all six. |
| **Koin Annotations / compiler plugin (`@Single`, `@KoinViewModel`, `@ComponentScan`)** | Removes hand-written `module { }` DSL boilerplate; JetBrains' own KMP App Template now ships this as the default. | MEDIUM | Adds a KSP/compiler-plugin dependency back into the KMP build (the exact category of tooling the Hilt migration is escaping); worth adopting only after the manual-DSL migration is stable and boilerplate pain is actually felt. |
| **Screenshot/visual regression testing for Compose (Paparazzi/Roborazzi-style)** | Catches unintended visual regressions from the modernization itself — directly serves the "no behavior/UX regression" constraint in PROJECT.md. | MEDIUM | High alignment with project's stated risk (regression prevention) but not requested explicitly; good candidate for the testing phase if time allows. |
| **Dependency-graph lint/enforcement in CI** (e.g., a custom Gradle task or `dependency-analysis-gradle-plugin`) | Automates enforcement of the one-directional module rule instead of relying on code review discipline; prevents architecture erosion after the migration ships. | LOW-MEDIUM | Cheap once modules exist; catches "feature A accidentally depends on feature B" before merge. |
| **Remote/shared Gradle build cache (beyond per-run GH Actions cache)** | Further build-time reduction across branches/PRs, not just within a single workflow run. | MEDIUM-HIGH | Requires either GitHub's build cache node or a self-hosted cache backend; likely overkill unless CI time remains a pain point after the local-cache table stakes are in place. |
| **Path-filtered CI triggers** (skip iOS job on Android-only diffs and vice versa) | Reduces expensive macOS runner minutes for changes that can't affect iOS (e.g., Android-only Compose UI tweaks). | LOW | Directly reduces CI cost/time; low risk since shared-module changes still trigger both jobs. |
| **`core-testing` module with shared fakes/test doubles** | Centralizes `DevFestNantesStoreMocked`-style fakes so every feature module tests against the same fakes instead of each reinventing its own. | LOW-MEDIUM | Natural extension of an existing good pattern; enables consistent "prefer fakes over mocks" testing philosophy across all new feature modules. |

### Anti-Features (Commonly Suggested for KMP Modernizations, But a Trap Here)

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|------------------|-------------|
| **Full NIA module taxonomy including `sync`, `benchmark`, `lint`, `test-app` modules** | NIA is Google's reference app and "doing it exactly like NIA" feels like the gold standard. | NIA is a showcase app maintained by a platform team; its full taxonomy (benchmark modules, macrobenchmark, custom lint module) solves problems (perf regression tracking at scale, enforcing lint across a large org) this single-conference-app project doesn't have. Copying it wholesale is scope creep that delays the actual goal (maintainability + working CI). | Adopt NIA's **dependency-direction philosophy** (feature/core split, one-directional graph) without importing every module type. Add `benchmark`/custom-`lint` modules later only if a concrete need appears. |
| **Module-per-screen granularity** (e.g., separate modules for `SessionListScreen` vs `SessionDetailScreen` inside "agenda") | Seems like "more modular = more modern." | For a ~6-feature app, this multiplies build-logic surface, Gradle configuration time, and Koin composition-root wiring without a corresponding maintainability win — the classic "over-modularization" failure mode multiple sources warn about (JVM/Gradle configuration overhead can start to *dominate* build time past a certain module count on a project this size). | One module per **feature** (agenda, speakers, venue, bookmarks, session-detail, settings) as PROJECT.md already specifies; split further only if a feature module demonstrably grows too large. |
| **Compose Multiplatform (shared UI) migration for iOS** | "True" KMP modernization sometimes gets conflated with sharing UI code too, and CMP is the trendiest 2025/2026 KMP topic. | Explicitly out of scope: PROJECT.md forbids UX changes and this project's iOS app is native SwiftUI — replacing it with CMP would be a UI rewrite carrying real regression risk, not a technical-debt cleanup. It would also invalidate this research's iOS-testing findings (CMP has its own, different testing story). | Keep iOS as native SwiftUI consuming the KMP `shared` framework as today; only share business logic, not UI. |
| **Chasing a numeric code-coverage percentage (e.g., "80% line coverage")** | Coverage percentages are an easy, visible KPI to report as "modernization done." | PROJECT.md explicitly rejects this ("pas d'objectif % strict imposé") — chasing a number incentivizes low-value tests (trivial getters/data classes) over the actually-risky logic (GraphQL mappers, filtering, bookmarks persistence) and can slow the migration down for vanity metrics. | Prioritize qualitative coverage of business logic (ViewModels, Store, mappers) as PROJECT.md states; use coverage reports as a *gap-finding* tool, not a gate. |
| **Mocking-framework-heavy testing (MockK/Mockative) as the default strategy** | Mocking frameworks are the "obvious" tool reached for when writing new tests, especially for AI-generated test suites. | 2025/2026 KMP consensus (and this project's own existing pattern via `DevFestNantesStoreMocked`) favors hand-written fakes: mocks don't work uniformly across all KMP targets without native-friendly libraries, and fakes are more readable/debuggable and double as documentation of the contract. | Default to fakes (extend `DevFestNantesStoreMocked`-style doubles in `core-testing`); reserve a mocking library for genuinely complex interaction-verification cases only, if any arise. |
| **Global Koin `get()` calls scattered through ViewModels/Stores (service-locator style)** | Fastest way to "just make it compile" during a Hilt→Koin migration under time pressure. | Reintroduces the exact untestable, hard-to-reason-about coupling that motivated moving off a DI framework that couldn't run in `shared` — defeats the purpose of the DI migration and makes the "decentralized DI" claim hollow. | Constructor-inject everywhere; confine all `get()`/`viewModel { }` calls to the Koin module-definition files themselves. |
| **Running the full iOS build+test matrix on every commit to every branch** | "More CI coverage is always better" instinct. | macOS runners are the most expensive/slowest GitHub Actions minutes; running the full iOS pipeline on every trivial Android-only or docs commit burns CI budget and slows feedback loops without corresponding safety benefit. | Path-filtered triggers (differentiator above) — always run iOS CI on `shared`/`iosApp` changes and on `main`/release branches, skip it for Android-only or docs-only diffs. |
| **Pinning `xcodebuild -destination` to a hardcoded named simulator (e.g., "iPhone 16")** | This is literally the pattern currently causing the broken CI in PROJECT.md — it looks like the simplest way to specify "test on iOS" when first writing the workflow. | macOS runner images update their pre-installed simulator/Xcode set on a schedule outside the project's control; a hardcoded device name silently becomes invalid whenever GitHub rotates `macos-latest` (or even a pinned macOS version's Xcode update), producing exactly the current failure and guaranteeing it recurs. | Resolve the destination dynamically at workflow run-time (`xcrun simctl list`) or pin the Xcode version explicitly and treat the runner-image/Xcode version as a maintained, tracked constant reviewed on Kotlin/Xcode upgrades. |

## Feature Dependencies

```
Fix broken iOS CI (simulator resolution)
    └── independent, unblocks CI signal for everything else — do first (already sequenced first in PROJECT.md)

Multi-module architecture (core-* + feature-*)
    └──requires──> Convention plugins / version catalog (build logic must exist before N modules copy it)
    └──requires──> Decentralized Koin DI
                       (you cannot cleanly split shared-module code into independent feature modules
                        while DI is still Hilt-only, because Hilt cannot run in `shared`/KMP — any
                        business logic that needs DI and lives outside `androidApp` needs Koin first,
                        or the module split will re-create the current DI bottleneck inside each new module)
    └──requires──> iOS umbrella framework (once `shared` is split into >1 Gradle module, iOS needs one
                       aggregating framework target, or `iosApp` breaks)

Decentralized Koin DI (per-module Koin modules)
    └──enables──> Testable ViewModels/Stores via constructor injection
                       └──enables──> Meaningful unit test coverage of business logic (PROJECT.md's stated priority)
    └──enables──> Koin checkModules() graph verification test

Test coverage improvement (ViewModels, Store, mappers)
    └──benefits from──> Multi-module architecture (isolates what to test per module, smaller surface per test target)
    └──benefits from──> Decentralized Koin DI (constructor injection makes fakes trivial to substitute)
    └──uses──> core-testing module (shared fakes) — should exist before feature modules write their own tests,
                   to avoid each feature reinventing DevFestNantesStoreMocked-style doubles

GitHub Actions CI optimization (cache, matrix, parallelization)
    └──partially independent of the above (Gradle/Konan caching, Android/iOS job split can land any time)
    └──benefits from──> Multi-module architecture (per-module Gradle build cache hits become meaningful;
                            a monolithic `shared`+`androidApp` gets far less benefit from module-level caching)
```

### Dependency Notes

- **Multi-module architecture requires decentralized Koin DI, not the other way around, in practice**: PROJECT.md's stated phase order is CI → deps/Gradle DSL → multi-module → DI → tests. Research suggests treating "finalize DI migration" as tightly coupled to modularization rather than strictly sequential: any shared-module code that needs dependency injection during the modularization step (which is likely immediately, given the Store/Analytics abstractions already live in `shared`) will hit the "Hilt doesn't work outside `androidApp`" wall the moment it's extracted into its own module. The practical implication for the roadmap: do the module *skeleton* extraction and the Koin *foundation* (single entry point + core modules' Koin modules) close together, even if full feature-by-feature DI migration continues afterward.
- **iOS umbrella framework requirement is a hard blocker, not optional**, the moment `shared` becomes more than one Gradle module — this needs to be designed before or during the first module split, not discovered afterward.
- **CI caching/matrix work is the most decoupled item** — it can be implemented independently of modularization and DI, and in fact should land early since it compounds (every subsequent phase's CI runs get faster) and directly serves the currently-broken-CI pain point.
- **Test coverage work should follow, not precede, DI decentralization** for any code being newly modularized: writing tests against Hilt-injected, not-yet-testable code and then re-writing those tests after the Koin migration is wasted effort.

## MVP Definition

Framed as "what makes this modernization credible," not user-facing MVP.

### Launch With (Phase 1 of this milestone)

- [ ] iOS CI fixed with a **root-cause** simulator resolution (not a re-pin that will break again) — unblocks trustworthy CI signal for every subsequent phase
- [ ] Gradle build cache (`gradle/actions/setup-gradle`) + Konan cache wired into CI — immediate, low-effort build-time win
- [ ] Android/iOS jobs split across `ubuntu-latest`/`macos-latest` in a matrix — prerequisite for any further CI optimization

### Add After Validation (later phases in this milestone)

- [ ] `core-*` module skeleton (model, network, data, analytics, ui, testing) with convention plugins
- [ ] `feature-*` module skeleton for the 6 named features, wired through the one-directional dependency rule
- [ ] iOS umbrella framework configured to keep `iosApp` on a single Kotlin/Native framework dependency
- [ ] Koin foundation: single `initKoin()` entry point, one Koin module per core/feature Gradle module, `expect`/`actual` platform modules for Android-`Context`-bound deps (BookmarksStore's SharedPreferences, etc.)
- [ ] Constructor-injected ViewModels/Stores (no scattered `get()` calls) across all migrated modules
- [ ] Business-logic test coverage expansion in `commonTest` for Store/mapper logic, extending the existing `DevFestNantesStoreContractTest` pattern into new `core-data`/feature modules
- [ ] `core-testing` module centralizing fakes (extending `DevFestNantesStoreMocked`)
- [ ] Koin `checkModules()` verification test

### Future Consideration (beyond this milestone)

- [ ] `api`/`impl` submodule split for the highest-churn feature modules only (agenda, session-detail)
- [ ] Koin Annotations/compiler plugin adoption to reduce DSL boilerplate
- [ ] Screenshot/visual regression testing (Paparazzi/Roborazzi) for Compose screens
- [ ] Dependency-graph lint enforcement in CI (automating the one-directional module rule)
- [ ] Path-filtered CI triggers to reduce macOS runner usage
- [ ] Remote/shared Gradle build cache beyond per-run GitHub Actions caching

## Feature Prioritization Matrix

| Feature | User Value | Implementation Cost | Priority |
|---------|------------|---------------------|----------|
| Fix iOS CI simulator resolution (root cause) | HIGH (unblocks everything, restores trust in CI) | LOW-MEDIUM | P1 |
| Gradle + Konan CI caching, Android/iOS job matrix | HIGH (fast feedback, direct PROJECT.md ask) | LOW | P1 |
| `core-*`/`feature-*` module skeleton + convention plugins | HIGH (core deliverable of this milestone) | MEDIUM-HIGH | P1 |
| iOS umbrella framework | HIGH (hard blocker once modularized) | MEDIUM | P1 |
| Koin foundation (single entry point, per-module modules) | HIGH (core deliverable; unblocks testability) | MEDIUM | P1 |
| Constructor injection discipline | MEDIUM-HIGH (enables real testing) | LOW | P1 |
| Business-logic test coverage expansion | HIGH (explicit PROJECT.md priority) | MEDIUM | P1 |
| `core-testing` shared fakes module | MEDIUM (consistency, less duplication) | LOW | P2 |
| Koin `checkModules()` verification | MEDIUM (catches DI regressions early) | LOW | P2 |
| `api`/`impl` split for top features | MEDIUM (build-time win at scale) | HIGH | P3 |
| Koin Annotations/compiler plugin | LOW-MEDIUM (DX only, adds tooling back) | MEDIUM | P3 |
| Compose screenshot testing | MEDIUM (regression safety net) | MEDIUM | P3 |
| Dependency-graph CI lint | MEDIUM (prevents architecture erosion) | LOW-MEDIUM | P3 |
| Path-filtered CI triggers | LOW-MEDIUM (cost/speed only) | LOW | P3 |

**Priority key:**
- P1: Must have — this milestone isn't credible without it
- P2: Should have, add when the P1 foundation for it exists
- P3: Nice to have, defer to a future milestone unless time allows

## Competitor / Reference Project Analysis

Not a market-competitor analysis (internal tooling, no users to compare against) — instead, reference implementations informing the approach:

| Concern | Now in Android (Google reference app) | Generic KMP multi-module templates (community) | Our Approach |
|---------|----------------------------------------|--------------------------------------------------|--------------|
| Module taxonomy | Full taxonomy: app, feature (api+impl), core, sync, benchmark, lint, test-app | Simpler: app/composeApp, feature/*, core/*, shared | Adapt NIA's core/feature split and one-directional rule; skip sync/benchmark/lint/test-app modules (not needed at this scale) |
| DI framework | Hilt (Android-only, not KMP-portable) | Koin (most common for pure-KMP; some use Kotlin-inject-anvil for compile-time safety) | Koin, per PROJECT.md's explicit decision — only framework that is both KMP-native and supports decentralized per-module modules without annotation processing |
| iOS UI | N/A (Android-only app) | Increasingly Compose Multiplatform for shared UI | Keep native SwiftUI — no UI sharing, only business-logic sharing via `shared` |
| Testing philosophy | Heavy reliance on fakes over mocks, contract-style tests | `commonTest` for business logic, platform test source sets for platform specifics only | Directly matches this project's existing pattern (`DevFestNantesStoreMocked`, `DevFestNantesStoreContractTest`) — extend, don't replace |
| CI/CD | GitHub Actions equivalent internal tooling not public in detail | `gradle/actions/setup-gradle` + Konan cache + OS matrix is the emerging community consensus | Adopt directly; this is the most well-converged, lowest-risk area of the research |

## Sources

- [nowinandroid/docs/ModularizationLearningJourney.md — android/nowinandroid (GitHub, official)](https://github.com/android/nowinandroid/blob/main/docs/ModularizationLearningJourney.md)
- [Guide to Android app modularization — Android Developers (official)](https://developer.android.com/topic/modularization)
- [Common modularization patterns — Android Developers (official)](https://developer.android.com/topic/modularization/patterns)
- [Choosing a configuration for your Kotlin Multiplatform project — Kotlin Multiplatform Documentation (official)](https://kotlinlang.org/docs/multiplatform/multiplatform-project-configuration.html)
- [Modularization in a Kotlin Multiplatform Project — Ahmad Jailani, Medium](https://ajailani.medium.com/modularization-in-a-kotlin-multiplatform-project-81e06d2170b6)
- [Kotlin Multiplatform project structure production — kmpship.app](https://www.kmpship.app/blog/kotlin-multiplatform-project-structure-production-2026)
- [Kotlin Multiplatform Setup — Koin (official)](https://insert-koin.io/docs/reference/koin-core/kmp-setup/)
- [Koin in Kotlin Multiplatform: A Complete Guide — Arnaud Giuliani, Koin Developers (official blog)](https://blog.insert-koin.io/koin-in-kotlin-multiplatform-a-complete-guide-576a24ffeeab)
- [Why Koin? — insert-koin.io (official)](https://insert-koin.io/docs/setup/why/)
- [Test your multiplatform app − tutorial — Kotlin Multiplatform Documentation (official)](https://kotlinlang.org/docs/multiplatform/multiplatform-run-tests.html)
- [Kotlin Multiplatform Testing in 2025: Complete Guide — kmpship.app](https://www.kmpship.app/blog/kotlin-multiplatform-testing-guide-2025)
- [Kotlin Multiplatform's Three Levels Of Testing With Kotest — Xebia](https://xebia.com/blog/kotlin-multiplatform-three-levels-testing-kotest/)
- [Configure GitHub Actions for continuous integration of a Kotlin Multiplatform application — Kotlin Multiplatform Documentation (official)](https://kotlinlang.org/docs/multiplatform/github-actions-for-kmp.html)
- [Building a CI Pipeline for Kotlin Multiplatform Mobile Using GitHub Actions — Nate Ebel, Engineering at Premise](https://engineering.premise.com/kotlin-multiplatforrm-github-actions-workflow-3e5e0fcb7081)
- [How to publish a Kotlin Multiplatform iOS app on App Store with GitHub Actions — Marco Gomiero](https://www.marcogomiero.com/posts/2024/kmp-ci-ios/)
- [E2E Testing for Compose Multiplatform — Preetam Bhosle, Medium (used only to confirm CMP UI testing is a separate, inapplicable track for this project's native-SwiftUI iOS)](https://medium.com/@preetambhosle/e2e-testing-for-compose-multiplatform-5d4666bfc3a3)
- Project context: `.planning/PROJECT.md`, `.planning/codebase/ARCHITECTURE.md`, `.planning/codebase/TESTING.md` (this repository)

---
*Feature research for: KMP engineering modernization (multi-module, Koin DI, testing, CI/CD)*
*Researched: 2026-09-12*
