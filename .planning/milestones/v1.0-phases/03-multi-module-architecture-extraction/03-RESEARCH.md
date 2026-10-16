# Phase 3: Multi-Module Architecture Extraction - Research

**Researched:** 2026-09-22
**Domain:** KMP/Android Gradle multi-module extraction (Now-in-Android-style convention plugins + core/feature module graph) with a single iOS umbrella Kotlin/Native framework
**Confidence:** MEDIUM-HIGH (codebase facts VERIFIED by direct file reads this session; Gradle/KMP mechanics CITED from official docs re-fetched this session; incremental step ordering is a synthesis, already de-risked by CONTEXT.md's locked decisions)

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

- **D-01:** Dagger Hilt stays the only DI framework throughout Phase 3. No Koin code of any kind (not even empty `module { }` stubs) — Koin is entirely Phase 4. Rationale: one variable per phase (research Anti-Pattern 4 / Pitfall 5: don't bundle module extraction with a DI-framework swap).
- **D-02:** The single Hilt `AppModule` stays in `:androidApp` and keeps providing the store, services, etc. Feature modules only carry their `@HiltViewModel` classes (feature modules therefore apply the Hilt plugin + KSP via their convention plugin). No per-module Hilt `@Module` split — Phase 4 dismantles `AppModule` anyway.
- **D-03:** Android-only service implementations currently in `androidApp/.../services/` move into the `androidMain` source set of the core module owning their interface (e.g. `BookmarksStoreImpl` → `core/data` androidMain, `FirebaseAnalyticsService` → `core/analytics` androidMain). `:androidApp` becomes a thin shell (Application, MainActivity, Home shell, navigation, `AppModule`, initializers). Services with no core interface (`SessionFiltersService`, `DataCollectionSettingsService`, `ExternalContentService`) go to the module that owns their consumer, per the same "lowest module that needs it" logic — planner decides exact placement.
- **D-04:** ROADMAP Phase 3 success criterion 3 and REQUIREMENTS ARCH-03 are **amended**: the "own Koin module" clause moves to Phase 4 (DI-02), so the verifier must not flag the absence of Koin modules in Phase 3 as a gap.
- **D-05:** Feature module set = `feature/agenda`, `feature/speakers`, `feature/venue`, `feature/session-detail`, `feature/about`, `feature/settings`. `feature/about` = About tab (About*, header/links/communities/social/version) + Partners (`PartnersViewModel`, `PartnerCard`). `feature/settings` = Settings screen + DataCollection (screen, agreement dialog, `DataCollectionViewModel`) + Legal. ARCH-03's list is amended accordingly (about added, bookmarks removed — see D-06).
- **D-06:** **No `feature/bookmarks` module** — there is no bookmarks screen. `BookmarksStore` (interface + Android impl) lives in `core/data`; `BookmarksViewModel` and the shared bookmark-toggle UI live in `core/ui`, consumed by `feature/agenda` (row toggle + favorites filter) and `feature/session-detail`.
- **D-07:** Navigation stays on Navigation Compose 2 with string routes, but is shaped **Nav3-ready**: features expose stateless screen entry points taking callbacks (e.g. `AgendaRoute(onSessionClick, onSpeakerClick)`); **no `NavController` is ever passed into or referenced from a feature module**. `:androidApp` owns `MainActivity`, the Home shell (bottom bar + nested tab `NavHost`, `HomeViewModel`), both `NavHost`s, the `Screen` route definitions, and wires all cross-feature navigation through callbacks. No route/argument format change in this phase.
- **D-08:** `core/ui` content rule: anything consumed by ≥2 features (or by the app + a feature) moves to `core/ui`, together with the theme (`Color`/`Theme`/`Type`) and `UiState`. Single-consumer composables/utils stay in their feature. Planner applies the rule file by file.
- **D-09:** De-risk the "three-framework problem" with a **tracer-first plan**: first plan = `build-logic` convention plugins + extract `core/model` only, re-exported by `:shared`, proven by iOS CI green + a local simulator run. If the umbrella/export approach fails, stop and rethink before any other module moves. — **Reversibility:** reversible.
- **D-10:** `:shared` becomes a **thin umbrella** and keeps its Gradle path `:shared`, framework `baseName = "shared"` (static), and the Xcode build phase `./gradlew :shared:embedAndSignAppleFrameworkForXcode`. Its source code moves out to `core/*`; it only declares the iOS framework binaries and `export()`s modules. Swift `import shared` and the Xcode project stay unchanged. **Note:** iOS uses direct integration (`embedAndSignAppleFrameworkForXcode`), **not CocoaPods**. — **Reversibility:** costly.
- **D-11:** The umbrella `export()`s **only modules whose types Swift touches**: `core/model`, `core/data` (`DevFestNantesStore`, `DevFestNantesStoreBuilder`, `BookmarksStore`), `core/analytics` (`AnalyticsService`). `core/network` (Apollo, generated GraphQL types) is an internal implementation dependency, not exported. Swift-visible type/member names must not change (KMP-NativeCoroutines `asyncSequence`/`asyncFunction` usage in Swift must keep compiling). Export dependencies must be `api` in the umbrella.
- **D-12:** iOS verification depth: iOS CI green on every pushed step; **plus** a manual iOS simulator smoke run (agenda, speakers, venue, about render and load data) at two checkpoints — the `core/model` tracer and after the `core/data` extraction. Android-only moves (core/ui, features) only need iOS CI green.
- **D-13:** Convention plugins live in a **`build-logic` included build** (`build-logic/convention`, wired via `includeBuild("build-logic")` in `settings.gradle.dcl`'s `pluginManagement`), NIA-style, reading the root `gradle/libs.versions.toml`. Expected plugin set (planner may refine): KMP library, Android library, Android feature (library + Compose + Hilt + common feature deps), Android application, plus shared detekt config. `buildSrc`'s `AndroidSdk` constants (min 26 / compile 37 / target 36) move into `build-logic`; `buildSrc` is deleted.
- **D-14:** Module paths are **nested**: `:core:model`, `:core:network`, `:core:data`, `:core:analytics`, `:core:ui`, `:core:testing`, `:feature:agenda`, `:feature:speakers`, `:feature:venue`, `:feature:session-detail`, `:feature:about`, `:feature:settings` (dirs `core/model`, `feature/agenda`, …). Each module gets its own unique Android namespace.
- **D-15:** Kotlin packages are **renamed to match modules** (e.g. `com.gdgnantes.devfest.core.model`, `com.gdgnantes.devfest.core.data`, `com.gdgnantes.devfest.feature.agenda`) — exact package scheme at planner's discretion as long as it mirrors the module path consistently. — **Reversibility:** costly.
- **D-16:** Per module, **two commits: (1) pure `git mv` into the new module with packages unchanged, build green; (2) package rename + import updates**. Kotlin/Native exports class names to Swift without the package prefix, so Swift names should stay stable — the tracer must confirm no new ObjC name collisions/renames (e.g. inspect the generated `shared.h` before/after).
- **D-17:** Android resources move **with their owner**: feature-specific strings/drawables → the feature; shared ones → `core/ui`; app name, launcher icons, splash/theme-manifest resources stay in `:androidApp`. `values-fr` and `values-night` variants move in lockstep; translations stay byte-identical. Non-transitive R classes; each module references its own `R`.
- **D-18:** **One phase branch/PR, one commit per step** (move + repackage per module), bottom-up order: `build-logic` → `core/model` (tracer) → `core/network` / `core/analytics` → `core/data` → `core/testing` → `core/ui` → features least-coupled first (`venue` → `about` → `settings` → `speakers` → `agenda` → `session-detail` last) → `:shared` thinned to pure umbrella + `buildSrc` removal. Full CI (Android + iOS) green per pushed step. Android manual smoke (`android run` + `android layout`/`screen capture`, all tabs + session detail + speaker detail + settings) at three checkpoints: after `core/data`, after `core/ui`, after the last feature.
- **D-19:** `core/testing` (KMP) is created now and receives the existing fakes — `DevFestNantesStoreMocked` and the `model/stubs/*` (`StoreStubs`, `CategoryStubs`, `RoomStubs`, `SocialItemStubs`) — **if** they're only consumed by tests/previews. Anything production code or Compose previews rely on must stay reachable without shipping test code in release. Do NOT fix `StoreStubs`' unseeded RNG here (that's TEST-01, Phase 5).
- **D-20:** New module build files are `build.gradle.kts` (tiny, applying convention plugins). No DCL pilot on new modules. The researcher re-runs Phase 2's live Declarative Gradle status check and records the outcome; `settings.gradle.dcl` stays DCL and must accommodate the new `include(...)` lines + `includeBuild("build-logic")` — if DCL settings can't express `includeBuild` in `pluginManagement`, that's a documented fallback case.

### Claude's Discretion

- Exact placement of services with no core interface (`SessionFiltersService`, `DataCollectionSettingsService`, `ExternalContentService`) and of `core/` utilities (`CoroutinesDispatcherProvider`, `SuspendUseCase`, performance helpers) per D-03's "lowest module that needs it" logic.
- Exact convention plugin names/granularity and exact package naming scheme (D-13, D-15), within the stated principles.
- File-by-file application of the `core/ui` rule (D-08) and resource-ownership rule (D-17).
- How `jvm()` target / `jvmTest` (`GraphQLStoreJvmTest`) and `commonTest` (`DevFestNantesStoreContractTest`, `ScheduleSlotDateParsingTest`) move with their code — tests follow the code they test.
- Detekt configuration per module (via convention plugin, keeping `linters/detekt-config.yml` as the single config).

### Deferred Ideas (OUT OF SCOPE)

- **Navigation 3 migration** — NavKeys (`@Serializable`), per-feature `EntryProviderScope` entry builders, app-owned `NavDisplay`/back stack, possibly feature `api`/`impl` split per the official Nav3 modularization guide. Own phase after this milestone's structural work.
- Type-safe (serializable) Navigation Compose routes — superseded by the Nav3 item above.
- Feature `api`/`impl` split — already tracked as ARCH-V2-01.
- Module dependency-graph lint in CI — already tracked as CICD-V2-01.
- Hilt→Koin migration (Phase 4), `!!` nav-arg fixes, `println`→Timber cleanups, test-coverage expansion (Phase 5) — not this phase.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| ARCH-01 | Build-logic uses convention plugins + the existing version catalog (`libs.versions.toml`), no duplicated build config across modules | See `## Architecture Patterns` (build-logic included build), `## Code Examples` (convention plugin skeletons, catalog-from-included-build wiring), `## Common Pitfalls` (DCL `includeBuild` gap, catalog access from `build-logic`) |
| ARCH-02 | `core/*` modules (model, network, data, analytics, ui, testing) exist; unidirectional graph, core never depends on feature | See `## Architectural Responsibility Map`, `## Architecture Patterns` (Pattern 3: dependency direction), `## Recommended Project Structure` |
| ARCH-03 (amended by D-04..D-06) | `feature/*` modules (agenda, speakers, venue, session-detail, about, settings) exist, each with its own ViewModel(s) + Compose screens; no per-feature Koin module this phase; no `feature/bookmarks` | See `## Runtime State Inventory` (service/file placement table), `## Common Pitfalls` (Hilt-in-library-module gotcha), `## Code Examples` (feature entry-point pattern) |
| ARCH-04 | `iosApp` continues consuming one umbrella Kotlin/Native framework | See `## Architecture Patterns` (umbrella `export()` mechanics, CITED against official Kotlin docs fetched this session), `## Common Pitfalls` (three-framework problem, unprefixed-naming-without-export gotcha) |
</phase_requirements>

## Summary

This phase takes a codebase that is today exactly two Gradle modules (`:androidApp`, `:shared`) and splits it into a `build-logic` included build plus 12 leaf modules (6 `core/*`, 6 `feature/*`), while keeping `:shared` alive as a thin umbrella that iOS still links against as a single framework. Nearly every open question a researcher would normally have to resolve from scratch (feature boundaries, About/Partners placement, bookmarks handling, Koin timing, package renaming, commit granularity, verification depth, DCL scope) is **already locked in `03-CONTEXT.md`** from the `/gsd-discuss-phase` session — this research therefore focuses on (a) verifying the Gradle/KMP mechanics the plan will need to get right on the first try, (b) mapping the actual current codebase files to their target module per D-03/D-05/D-06/D-08/D-17, and (c) re-running the two live status checks CONTEXT.md explicitly asks for (Declarative Gradle `includeBuild` support, D-20; and confirming the CI workflow-file impact claim in D-18's canonical refs).

Three findings materially affect planning. First, the CI workflow files (`android.yml`, `ios.yml`) use **unqualified** Gradle task invocations (`./gradlew testDebugUnitTest`, `assembleDebug`, `detekt`, `lint`, `connectedDebugAndroidTest`) — not module-qualified paths — so Android CI requires **no task-path edits** as modules are added; only the iOS workflow's Gradle/Konan/DerivedData `actions/cache` key `hashFiles(...)` patterns (currently scoped to `shared/**`) need widening to the new `core/*` paths for cache-hit efficiency (this is a correctness-neutral, cache-efficiency-only gap — [VERIFIED: .github/workflows/android.yml, .github/workflows/ios.yml, read this session]). Second, official Kotlin docs (fetched live this session) confirm the `export()` mechanics precisely: only `api`-declared dependencies can be exported, `export()` must be repeated **per target** (`iosX64`, `iosArm64`, `iosSimulatorArm64` each need their own `export(project(":core:model"))` call), and — critically — a module reachable only via `api` **without** `export()` is *still visible to Swift* but under a name prefixed by the source module (e.g. `CoreDataFoo` instead of `Foo`), which is the mechanism behind D-16's "no new ObjC name collisions/renames" verification requirement, not a build failure. Third, the live Declarative-Gradle re-check (repeating Phase 2's method) found `includeBuild` inside `pluginManagement` **is** expressible in `.dcl` per Gradle's own migration case-study and cross-version test suite, but `build-logic`'s own settings file must stay **plain `settings.gradle.kts`** (NIA's own pattern, and Phase 2's precedent of scoping DCL adoption narrowly) since it needs `dependencyResolutionManagement { versionCatalogs { create("libs") { from(files("../gradle/libs.versions.toml")) } } }` to expose the catalog to convention-plugin code — a construct not established as portable to `.dcl` this session.

**Primary recommendation:** Follow D-18's bottom-up, tracer-first, one-commit-per-module-move order exactly; treat `build-logic`'s own settings file as `.kts` (not `.dcl`) even though the root `settings.gradle.dcl` gains the `includeBuild("build-logic")` line; verify the umbrella framework's Swift-visible names via `shared.h` diffing at both D-12 checkpoints, not just "does it build."

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Domain models (Session, Speaker, Room, Agenda, Partner, Venue, enums) | Database/Storage-adjacent KMP `core:model` | — | Root of the graph, zero dependencies, consumed by every other tier including iOS |
| GraphQL client + codegen (Apollo, normalized cache) | API/Backend-client KMP `core:network` | `core:model` (mapping target) | Owns the Apollo Gradle plugin config; only `core:data` talks to it directly |
| Store abstraction + mappers + local bookmarks persistence | API/Backend-client KMP `core:data` | `core:network`, `core:model` | `DevFestNantesStore`/`GraphQLStore`/`BookmarksStore` — the single source of truth both Android and iOS read through |
| Analytics interface + Firebase impl | API/Backend-client KMP `core:analytics` (interface `commonMain`, impl `androidMain`) | `core:model` | Firebase Android SDK is Android-only; iOS analytics (if any) stays in `iosApp` outside this graph |
| Test fixtures/fakes (`DevFestNantesStoreMocked`, `*Stubs`) | KMP `core:testing` | `core:model`, `core:data` | Exposed as `api` so both `commonTest`/`jvmTest` and Android `test`/`androidTest` in feature modules can consume it |
| Design system (theme, app bars, shared chips/icons, `BookmarksViewModel` + toggle UI) | Browser/Client (Android Compose) `core:ui` | `core:model` | Android-only — no `iosMain` source set; anything consumed by ≥2 features per D-08 |
| Per-feature screens + ViewModels (agenda, speakers, venue, session-detail, about, settings) | Browser/Client (Android Compose) `feature:*` | `core:ui`, `core:data`, `core:model`, `core:analytics` | Each is a leaf Android library; never depends on a sibling `feature:*` |
| Navigation graph, route strings, cross-feature wiring, Home shell, Hilt `AppModule` | Frontend Server-equivalent (app shell) `:androidApp` | all `feature:*` + `core:*` | Only module that knows the full nav graph; owns the one place `NavController` is referenced (D-07) |
| iOS umbrella export surface | CDN/Static-equivalent (framework packaging) `:shared` | `core:model`, `core:data`, `core:analytics` (via `api` + `export()`); `core:network` as `api`-only, not exported | Thin re-export shim; the only module Swift links against |
| Build configuration (plugin versions, SDK constants, detekt) | Build tooling `build-logic` (included build) | root `gradle/libs.versions.toml` | Single source read by every convention plugin; replaces `buildSrc` |

## Project Constraints (from CLAUDE.md/AGENTS.md)

- **Prefer the `android` CLI over raw `adb`/`gradlew` invocations** for Android-specific tasks — `android run`, `android layout`, `android screen capture`/`resolve`, `android docs search`, `android describe`, `android emulator list|start|stop`. D-18's Android smoke-verification steps must use this tool, not ad hoc `adb`. [VERIFIED: `android --version` → `1.0.16261425`, confirmed installed and on `PATH` this session]
- **Prefer `gh` CLI over raw GitHub API/web requests** for anything involving this repo's issues/PRs/checks/releases — relevant if the plan opens a PR per phase or checks CI run status via `gh run watch`/`gh pr checks`. [VERIFIED: `gh --version` → `2.100.0`, installed this session]
- Community Android skills (`android-architecture`, `android-gradle-logic`, `gradle-build-performance`, etc.) are **not part of this repo** — installed once per machine outside any git-tracked project. Use them if available in the agent's environment; do not assume they exist.
- No project-specific coding-convention overrides beyond what `.planning/codebase/CONVENTIONS.md` documents (Detekt, 120-char lines, no wildcard imports, Timber over `println`, camelCase/PascalCase per Kotlin norms) — these conventions must be preserved across the module split, not relaxed.

## Standard Stack

This phase installs **no new external packages or dependencies**. It restructures how the *existing* catalog (`gradle/libs.versions.toml`, [VERIFIED: gradle/libs.versions.toml, read this session]) is applied across modules via convention plugins. The plugins each convention plugin will alias already exist in the catalog:

| Plugin alias (from `libs.versions.toml`) | Applies to | Purpose |
|---|---|---|
| `android-kotlin-multiplatform-library` (AGP 9.4.0's `com.android.kotlin.multiplatform.library`) | `core:model`, `core:network`, `core:data`, `core:analytics`, `core:testing` | KMP+Android hybrid plugin — [VERIFIED: gradle/libs.versions.toml:115, `android-kotlin-multiplatform-library = { id = "com.android.kotlin.multiplatform.library", version.ref = "agp" }`; already in production use by `:shared`, shared/build.gradle.kts:6, read this session] |
| `kotlin-multiplatform`, `kotlin-serialization`, `ksp`, `kmp-native-coroutines`, `appollo` | Same KMP `core:*` modules that need them (network/data need Apollo; all need serialization) | Unchanged from `:shared`'s current plugin block |
| `android-application` | `:androidApp` only | Unchanged |
| `detekt` (`dev.detekt` 2.0.0-alpha.6) | Every module, via a shared convention plugin reading `linters/detekt-config.yml` | Consolidates the currently-duplicated `detekt { buildUponDefaultConfig = true; ... config.setFrom("$rootDir/linters/detekt-config.yml") }` block that appears near-identically in both `shared/build.gradle.kts:14-24` and `androidApp/build.gradle.kts:47-52` [VERIFIED: both files read this session] — exactly the duplication ARCH-01 requires eliminating |
| `dagger-hilt`, `ksp` (hilt-compiler) | Every `feature:*` module (per D-02) | Per official Android docs: `@HiltViewModel` processing needs the compiler in any module declaring one, and Google's own multi-module guidance is that in practice teams apply the Hilt Gradle plugin in every module using Hilt annotations, not just the app module — [CITED: developer.android.com/training/dependency-injection/hilt-multi-module, fetched via WebSearch this session] |
| `kotlin-compose-compiler` | `core:ui`, every `feature:*` | Compose UI modules only |

**Installation:** No new `implementation(...)`/`plugins { ... }` entries need adding to the catalog itself. The convention plugins add a `build-logic/convention/build.gradle.kts` with `compileOnly` dependencies on the AGP and Kotlin Gradle plugin artifacts so the convention plugin code can call their DSLs — this is new *build-logic-internal* tooling, not an app dependency:

```kotlin
// build-logic/convention/build.gradle.kts (new file)
plugins {
    `kotlin-dsl`
}
dependencies {
    compileOnly(libs.android.gradlePlugin) // needs adding: android.gradlePlugin = androidPluginForGradle
    compileOnly(libs.kotlin.gradlePlugin)  // needs adding: kotlin.gradlePlugin
    compileOnly(libs.ksp.gradlePlugin)
}
```
[CITED: pattern synthesized from community NIA-replication guides — dev.to/coltonidle, medium.com/@beranger.guillaume — corroborating the official NIA `build-logic/convention/build.gradle.kts` shape; WebSearch this session, MEDIUM confidence]

**Version verification:** All plugin versions come from the catalog already verified live during Phase 2 (AGP 9.4.0, Kotlin 2.4.20, KSP 2.3.12, Detekt 2.0.0-alpha.6, Dagger 2.60.1 — [VERIFIED: gradle/libs.versions.toml, read this session; also STATE.md's "Phase 02 version deviations" table, read this session]). No re-verification needed since Phase 3 does not bump any version.

## Package Legitimacy Audit

**Not applicable — no new external packages are installed in this phase.** Phase 3 is a pure structural refactor: it moves existing code between Gradle modules and adds build-logic tooling that references plugins/artifacts already present in `gradle/libs.versions.toml`. D-01 explicitly forbids adding Koin (even as stubs) during this phase. The only "new" Gradle-catalog entries this phase may need are `compileOnly` accessors for the AGP/Kotlin/KSP Gradle-plugin artifacts themselves (`com.android.tools.build:gradle`, `org.jetbrains.kotlin:kotlin-gradle-plugin`, `com.google.devtools.ksp:symbol-processing-gradle-plugin`) inside `build-logic/convention`'s own dependencies — these are the same JetBrains/Google-published artifacts already applied via `alias(libs.plugins.*)` in the root `build.gradle.kts`, just referenced as GAV coordinates for `compileOnly` plugin-authoring use instead of via `apply false`. No legitimacy check is warranted for artifacts already trusted and in production use in this repo.

**Packages removed due to [SLOP] verdict:** none
**Packages flagged as suspicious [SUS]:** none

## Architecture Patterns

### System Architecture Diagram

```text
┌──────────────────────────────────────────────────────────────────────────┐
│  :androidApp  (app shell)                                                │
│  MainActivity → root NavHost → Home shell (bottom bar + nested NavHost)  │
│  owns: Screen route defs, HomeViewModel, Hilt AppModule, initializers    │
└───┬──────┬──────┬──────┬──────┬──────┬───────────────────────────────────┘
    │      │      │      │      │      │  (callback-driven; no cross-feature deps)
    ▼      ▼      ▼      ▼      ▼      ▼
 feature: agenda speakers venue session- about  settings
                                 detail
    │      │      │      │      │      │
    └──────┴──────┴──┬───┴──────┴──────┘
                      ▼
              ┌──────────────┐
              │   core:ui    │  (theme, app bars, shared chips,
              │ (Android-only)│   BookmarksViewModel + toggle UI)
              └──────┬───────┘
                      │
    ╔═════════════════▼══════════════════════════════════════════════╗
    ║  KMP commonMain modules (built once; consumed by Android AND   ║
    ║  re-exported through the :shared umbrella framework for iOS)   ║
    ║                                                                  ║
    ║  core:data ──depends on──▶ core:network ──depends on──▶ core:model
    ║      │                                                    ▲     ║
    ║      └────────────────────────────────────────────────────┘     ║
    ║  core:analytics (interface, commonMain) ──depends on──▶ core:model
    ║  core:testing (fakes/stubs) ──depends on──▶ core:model, core:data║
    ╚═══════════════════════════════════════════════════════════════╝
                      │
                      ▼  (api + export(), per iOS target)
              ┌──────────────┐        ┌───────────────────────┐
              │   :shared    │───────▶│  iosApp (Xcode)        │
              │ thin umbrella │ embed  │  `import shared`       │
              │ Kotlin/Native│ &Sign  │  KMPNativeCoroutines    │
              │ framework    │        │  asyncSequence/Function │
              └──────────────┘        └───────────────────────┘

  build-logic (included build, wired via settings.gradle.dcl's
  pluginManagement { includeBuild("build-logic") })
  ├── reads root gradle/libs.versions.toml (own settings.gradle.kts,
  │   NOT .dcl — see Common Pitfalls)
  └── convention/  → KmpLibraryConventionPlugin, AndroidLibraryConventionPlugin,
                      AndroidFeatureConventionPlugin (+Compose+Hilt+KSP),
                      AndroidApplicationConventionPlugin, DetektConventionPlugin
```

A reader tracing "user taps a session" follows: `feature:agenda`'s `AgendaRoute(onSessionClick)` composable fires a callback → `:androidApp`'s root `NavHost` navigates → `feature:session-detail`'s screen resolves via its injected `DevFestNantesStore` (from `core:data`) → `core:data` calls `core:network`'s Apollo client → response mapped into `core:model` types → `SessionViewModel` (in `feature:session-detail`, still `@HiltViewModel` per D-01/D-02) emits `StateFlow` → Compose recomposes → `core:analytics`'s `AnalyticsService` (impl in `androidMain`, injected via the still-central Hilt `AppModule`) logs the page event.

### Recommended Project Structure

```
DevFest_Nantes/
├── build-logic/
│   ├── settings.gradle.kts          # plain .kts — pulls in ../gradle/libs.versions.toml
│   └── convention/
│       ├── build.gradle.kts
│       └── src/main/kotlin/
│           ├── KmpLibraryConventionPlugin.kt        # core:model/network/data/analytics/testing
│           ├── AndroidLibraryConventionPlugin.kt    # core:ui
│           ├── AndroidFeatureConventionPlugin.kt    # feature:* (Compose + Hilt + KSP + common deps)
│           ├── AndroidApplicationConventionPlugin.kt
│           └── DetektConventionPlugin.kt            # shared linters/detekt-config.yml wiring
├── core/
│   ├── model/        # KMP, commonMain only, zero deps
│   ├── network/       # KMP, Apollo client + generated GraphQL code, depends on core:model
│   ├── data/          # KMP, Store layer (commonMain/androidMain), depends on core:model + core:network
│   ├── analytics/     # KMP, interface commonMain + Firebase impl androidMain, depends on core:model
│   ├── testing/       # KMP, fakes/stubs, depends on core:model + core:data
│   └── ui/            # Android-only, Compose design system + BookmarksViewModel, depends on core:model
├── feature/
│   ├── agenda/
│   ├── speakers/
│   ├── venue/
│   ├── session-detail/
│   ├── about/          # About + Partners
│   └── settings/       # Settings + DataCollection + Legal
├── androidApp/         # MainActivity, Home shell, NavHost×2, Screen, AppModule, initializers
├── shared/             # thin umbrella: binaries.framework { export(...) } × 3 iOS targets
├── iosApp/              # unchanged
└── settings.gradle.dcl # + includeBuild("build-logic") + 12 new include(...) lines
```

### Pattern 1: Callback-driven, route-agnostic feature modules (already the codebase's shape)

**What:** Feature composables take `onX: (...) -> Unit` lambdas; `:androidApp` owns all `NavController` references. This is not a new pattern to introduce — it is **already how the code is written today**.
**Verified against the actual codebase:** `Home.kt`'s nested `NavHost` (androidApp/src/main/java/.../ui/screens/Home.kt:141-211, read this session) already calls `Agenda(onSessionClick = ...)`, `Speakers(onSpeakerClick = ...)`, `Venue(onNavigationClick = ..., onVenuePlanClick = ...)`, `About(onCodeOfConductClick = ..., onPartnerClick = ..., ...)` — none of these composables receive a `NavController`. `MainActivity.kt`'s root `NavHost` (read this session) is the only place `mainNavController.navigate(...)` is called. D-07 formalizes this existing shape as the phase's Nav3-prep boundary; **no navigation-call-site rewrite is required**, only a module-boundary move of already-callback-shaped composables.
**Anti-pattern already avoided:** `ExternalContentService` (`@ActivityScoped`, needs an `Activity`) is consumed **only** inside `MainActivity.kt` and `Home.kt` [VERIFIED: grep this session — 3 files total reference `ExternalContentService`: the service itself, `MainActivity.kt`, `Home.kt`] — every feature composable receives an already-resolved `onXClick: () -> Unit` lambda that closes over `externalContentService.openUrl(...)`, never the service itself. This means `ExternalContentService` can stay entirely inside `:androidApp` with **zero** cross-module wiring required — it is not a "lowest module that needs it" placement decision at all, contrary to what a surface reading of D-03 might suggest.

### Pattern 2: KMP dependency direction enforced by Gradle module graph, not lint (this phase)

**What:** `core:model` → zero deps. `core:network`, `core:analytics` → depend only on `core:model`. `core:data` → depends on `core:model` + `core:network`. `core:testing` → depends on `core:model` + `core:data`. No `core:*` module ever adds a dependency on a `feature:*` module.
**Enforcement in this phase:** Gradle itself rejects circular graphs, but nothing stops a `core:*` module from *accidentally* adding a feature dependency the wrong way — CICD-V2-01 (dependency-graph lint) is explicitly deferred to v2, so this phase relies on code review + the convention-plugin types themselves (a `KmpLibraryConventionPlugin`-typed module has no mechanism to `implementation(project(":feature:agenda"))` unless someone manually adds it — nothing prevents it syntactically, so plan verification steps should include an `./gradlew :core:model:dependencies` (or similar) spot-check per D-18 commit).

### Pattern 3: Umbrella framework `export()` — verified mechanics (highest-risk pattern this phase)

**What (CITED, fetched live this session from kotlinlang.org/docs/multiplatform/multiplatform-build-native-binaries.html):**
- Only dependencies declared with `api(...)` in a source set **can** be exported; `implementation(...)` dependencies cannot.
- `export()` is declared inside `binaries.framework { }`, and **must be repeated per target** if there are multiple iOS targets (this repo has three: `iosX64`, `iosArm64`, `iosSimulatorArm64`).
- Export is **non-transitive by default**: exporting `core:data` does not automatically export `core:network` or `core:model` even if `core:data` depends on them via `api` — each module Swift needs to see by its own (unprefixed) name must be explicitly `export()`-ed.
- `transitiveExport = true` exists but official docs explicitly warn against it (increases compile time and binary size) — do not use it as a shortcut for D-11's "only export what Swift touches" rule.
- A dependency reachable only via `api` **without** `export()` is *not* invisible to Swift — Kotlin/Native still compiles it into the framework's API — but its declarations surface under a **name prefixed by the originating module** (community source, MEDIUM confidence — WebSearch cross-checked against the general "avoid name collisions across separately-exported KMM modules" principle in JetBrains' own umbrella-module guidance). This is the precise mechanism D-16 is worried about ("Kotlin/Native exports class names to Swift without the package prefix, so Swift names should stay stable") — getting `export()` right, not just `api`, is what keeps `Session`/`Speaker`/`DevFestNantesStore` unprefixed in Swift.

**Applying this to the current shared/build.gradle.kts shape [VERIFIED: shared/build.gradle.kts:33-42, read this session]:**

```kotlin
// shared/build.gradle.kts — target end-state shape (Direct integration, NOT cocoapods{})
kotlin {
    android { /* unchanged: namespace, compileSdk, minSdk, jvmTarget */ }
    jvm() // unchanged — jvmTest (GraphQLStoreJvmTest) still needs a jvm() target somewhere;
          // confirm during planning whether jvm() stays on :shared or moves with core:network

    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "shared"          // unchanged — D-10
            isStatic = true              // unchanged — D-10
            export(project(":core:model"))
            export(project(":core:data"))
            export(project(":core:analytics"))
            // core:network deliberately NOT exported — D-11
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":core:model"))       // must be api, not implementation — required for export()
            api(project(":core:data"))
            api(project(":core:analytics"))
            implementation(project(":core:network"))  // internal only, implementation is fine here
        }
    }
}
```
[CITED: kotlinlang.org/docs/multiplatform/multiplatform-build-native-binaries.html, fetched live this session — export()/api requirement, per-target repetition, transitiveExport warning all directly from this fetch]

**Verification beyond "does it build" (D-16's actual ask):** after each `export()`-affecting step, diff the generated Objective-C header (`build/xcode-frameworks/.../shared.h` or wherever the Xcode build phase emits it) before/after, or open it in Xcode, and confirm `Session`, `Speaker`, `DevFestNantesStore`, `BookmarksStore`, `AnalyticsService` etc. remain **unprefixed** class/protocol names — a silent rename would compile Swift call sites referencing e.g. `KMPNativeCoroutinesAsync`'s generated `asyncSequence`/`asyncFunction` wrappers around a differently-named type and could produce confusing Swift compile errors far from the actual Gradle change.

### Anti-Patterns to Avoid

- **Compose code in a module with an `iosMain` source set:** this app's iOS UI is native SwiftUI, not Compose Multiplatform. `core:ui` and every `feature:*` module must be plain Android library modules (no `iosMain`), never `com.android.kotlin.multiplatform.library`-typed.
- **Feature modules depending on each other:** never add `feature:agenda` → `feature:session-detail` (or any sibling) as a Gradle dependency, even though both need bookmark toggling — that shared state already lives in `core:data`'s `BookmarksStore` + `core:ui`'s `BookmarksViewModel` per D-06.
- **Exporting each new Kotlin module as its own iOS framework:** always a single umbrella (`:shared`) — this is the exact "three-framework problem" D-09's tracer step exists to de-risk before the full cut.
- **Treating `api` as a substitute for `export()`, or vice versa:** both are required together for a module's types to reach Swift under their real names (see Pattern 3 above) — a module declared `api` but never `export()`-ed is a silent Swift-naming regression, not a build failure, so it will not surface in CI without the `shared.h` diff step.
- **Doing the module split and the Hilt→Koin migration in the same commit:** explicitly forbidden by D-01/D-02; Hilt's `AppModule` stays put, only `@HiltViewModel` classes travel with their feature.
- **Forgetting `build-logic`'s own settings file needs the version catalog wired explicitly:** unlike a regular included subproject, `build-logic` is a **separate Gradle build** with its own settings file — it does not automatically see the root project's `libs.versions.toml` accessor. See Common Pitfalls.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Per-module Gradle boilerplate (compileSdk/minSdk/jvmTarget/detekt config repeated 12+ times) | Copy-pasted `build.gradle.kts` blocks per module | `build-logic` convention plugins (D-13) | This is the entire point of ARCH-01; NIA's own `ModularizationLearningJourney.md` [CITED: github.com/android/nowinandroid, referenced in prior research SUMMARY.md/ARCHITECTURE.md] documents this as the standard fix for exactly this duplication |
| Cross-module dependency graph enforcement | A custom Gradle task that walks `project.configurations` looking for forbidden edges | Nothing this phase (CICD-V2-01 defers a real lint to v2) — rely on convention-plugin typing + code review + the D-18 `./gradlew :module:dependencies` spot-check | Building a bespoke lint now duplicates work v2 will do properly with a maintained plugin (e.g. Square's `dependency-analysis-gradle-plugin`) |
| iOS umbrella framework packaging | Hand-rolled `.framework` merging or a custom Xcode build phase | Kotlin/Native's own `binaries.framework { export(...) }` (Pattern 3 above) | This is exactly the built-in mechanism JetBrains ships for this use case; hand-rolling framework merging is the literal "three-framework problem" antipattern |
| Shared test fixtures across modules | Duplicating `StoreStubs`/`DevFestNantesStoreMocked` per module that needs them | `core:testing`, exposed as `api` not `testImplementation` (D-19) | Duplication would immediately diverge and defeats the point of `core-testing` (also tracked as DI-05 in Phase 4) |

**Key insight:** every "don't hand-roll" item in this phase already has a name in D-01..D-20 (build-logic, no custom lint yet, `export()`, `core:testing`) — the risk is not inventing a wrong custom solution, it's under-using or misconfiguring the correct built-in one (e.g., `api` without `export()`, or `implementation` where `api` was needed).

## Runtime State Inventory

This phase is a rename/refactor (module extraction + package rename per D-15/D-16), so this section is mandatory.

| Category | Items Found | Action Required |
|----------|-------------|------------------|
| **Stored data** | `BookmarksStoreImpl` and `SessionFiltersServiceImpl`/`DataCollectionSettingsServiceImpl` all read/write **Android `SharedPreferences`** using **hardcoded string keys** (`PREF_SELECTED_SESSIONS`, `SHARED_PREFERENCES_KEY_AGENDA_FILTERS`, `SHARED_PREFERENCES_KEY_ENABLED_DATA_COLLECTION_TOOLS`) [VERIFIED: androidApp/.../services/BookmarksStoreImpl.kt:62, SessionFiltersService.kt:23, DataCollectionSettingsService.kt:129-130, read this session]. These keys are **stored on-device in existing installs** and must NOT change when the code moves modules/packages — only the Kotlin class's *package* changes (D-15), the SharedPreferences key *string* must stay byte-identical, otherwise existing users lose their bookmarks/filters/consent-status silently on app update. | Code edit only (class moves + package rename) — **the literal SharedPreferences key string constants must be verified unchanged** in the plan's diff, not just "code compiles." No data migration needed since the underlying Android `SharedPreferences` file/keys are untouched by a Kotlin package rename. |
| **Live service config** | None found. This app has no n8n/Datadog/Tailscale/Cloudflare-style external service configured outside git — Firebase config (`GoogleService-Info.plist`/`google-services.json`) and Remote Config flags (`openfeedback_enabled`, etc.) are read by *key name* from Firebase's console, unaffected by Gradle module boundaries. | None. |
| **OS-registered state** | None found. No Windows Task Scheduler/pm2/launchd/systemd equivalents — this is a mobile app with no background OS-level task registration outside the Android app's own manifest-declared components (which move with `AndroidManifest.xml` merging, standard AGP behavior). | None. |
| **Secrets/env vars** | OpenFeedback `BuildConfig` fields (`OPEN_FEEDBACK_PROJECT_ID`, etc., currently literal `"SECRET"` placeholders per `androidApp/build.gradle.kts:66-70`, read this session) are generated by the **Secrets Gradle Plugin** applied to `:androidApp` only. If any of these fields are read from a moved module (none currently are — `OpenFeedbackInitializer` stays in `:androidApp`'s `core/`), the `BuildConfig` class reference would break. Firebase BOM / `google-services.json` stay app-scoped. | None — verified by reading `androidApp/build.gradle.kts` and confirming `OpenFeedbackInitializer.kt` is not in the D-05 feature list; it stays in `:androidApp`. |
| **Build artifacts** | `buildSrc/` (to be deleted per D-13) currently produces the `AndroidSdk` object consumed by `shared/build.gradle.kts:26-27` and `androidApp/build.gradle.kts` (`compileSdk = AndroidSdk.compile`, etc.) [VERIFIED: both files read this session]. Deleting `buildSrc` without first moving `AndroidSdk` into `build-logic` and repointing both consumers would break the build immediately (not silently) — this is a hard compile-time dependency, easy to sequence correctly, but must land in the **same commit** as `buildSrc`'s deletion (D-13's own step, last in D-18's bottom-up order). An untracked `shared-ui/` directory exists at repo root but is **not in `settings.gradle.dcl`'s `include(...)` list** [VERIFIED: settings.gradle.dcl, read this session — only `:androidApp` and `:shared` are included] — it is a stray leftover directory, not a live Gradle module; do not confuse it with the new `core:ui` module and do not attempt to reuse or delete it as part of this phase's Gradle graph changes (research/ARCHITECTURE.md flags this as a known confusion point). | Code edit (move `AndroidSdk` into `build-logic`, repoint 2 known consumers) in the same commit as `buildSrc` removal. `shared-ui/` — leave untouched, out of scope. |

## Common Pitfalls

### Pitfall 1: `build-logic`'s own settings file cannot silently inherit the root catalog

**What goes wrong:** A convention plugin inside `build-logic/convention` tries to reference `libs.versions.kotlin.get()` or `alias(libs.plugins.android.application)`, but `build-logic` is a **separate Gradle build** (an included build via `includeBuild("build-logic")` in the root's `pluginManagement`), so it has its own classpath and its own settings resolution — it does not automatically see the root's `gradle/libs.versions.toml` accessor just because the file lives in the same repo.
**Why it happens:** Teams new to the `build-logic` pattern assume "it's all one Gradle build" the way `buildSrc` was (which genuinely is auto-included and does share the root catalog).
**How to avoid:** `build-logic/settings.gradle.kts` (plain `.kts`, per D-20's scoping — do not attempt this as `.dcl`) must explicitly declare:
```kotlin
dependencyResolutionManagement {
    repositories { google(); mavenCentral(); gradlePluginPortal() }
    versionCatalogs {
        create("libs") {
            from(files("../gradle/libs.versions.toml"))
        }
    }
}
rootProject.name = "build-logic"
include(":convention")
```
[CITED: pattern corroborated across multiple community NIA-replication guides — dev.to/coltonidle, medium.com/@beranger.guillaume, michiganlabs.com — WebSearch this session, MEDIUM confidence; this is the standard, widely-documented NIA pattern, not a novel claim]
**Warning signs:** `build-logic/convention/build.gradle.kts` fails to resolve `libs.*` at all, or IDE shows "Unresolved reference: libs" only inside `build-logic` files.

### Pitfall 2: Declarative Gradle's `includeBuild` support is real but narrow — don't over-extend it

**What goes wrong (live re-check performed this session, repeating Phase 2's method):** WebSearch of Gradle's own `declarative.gradle.org` migration case-study and cross-version test suite this session confirms `pluginManagement { includeBuild("build-logic") }` **is** expressible and tested inside a `.dcl` settings file. However, this is the *root* settings file's concern only. Do not conclude from this that `build-logic`'s own settings file, or the convention-plugin `build.gradle.kts` files themselves, are now safe to author in `.dcl` — Phase 2's own live re-check (STATE.md, "Phase 02 DCL pilot outcome", read this session) already established that Declarative Gradle's "Software Types" surface (needed for anything beyond bare `pluginManagement`/`include()`/`dependencyResolutionManagement` — i.e. anything a convention plugin or a KMP/Hilt/Compose module build file needs) remains gated behind experimental ecosystem plugins and non-stable Gradle builds, and nothing found this session (2026-09-22) changes that assessment.
**How to avoid:** Add the `includeBuild("build-logic")` line and the 12 new `include(":core:model")`-style lines directly to the existing `settings.gradle.dcl` (this is within the already-proven-safe "plain repository/include declarations" surface from Phase 2's pilot). Keep `build-logic/settings.gradle.kts` and every new module's `build.gradle.kts` on plain Kotlin DSL, per D-20's explicit instruction — do not attempt a DCL pilot on any of them.
**Warning signs:** Any attempt to write `core/model/build.gradle.kts` as `.dcl` and hitting "no DCL equivalent" for `android.kotlin.multiplatform.library`, KSP, or Apollo plugin configuration — expected and should not be treated as a phase blocker, just confirmation that D-20's "no DCL pilot on new modules" scoping was correct.

### Pitfall 3: Hilt Gradle plugin needed per-feature-module, not just per-annotation

**What goes wrong:** Assuming `@HiltViewModel` only needs the `androidx.hilt:hilt-compiler` KSP dependency and skipping the Hilt Gradle plugin (`com.google.dagger.hilt.android`) in feature modules to "keep them lighter," since only `:androidApp` has `@HiltAndroidApp`.
**Why it happens:** Official Android docs on Hilt in multi-module apps note the *component generation* only strictly requires all Hilt-using code be a transitive dependency of the app module — which reads like the plugin itself might be app-only.
**How to avoid:** D-02 already made this call explicitly ("feature modules therefore apply the Hilt plugin + KSP via their convention plugin") — this matches the practical pattern most real multi-module Hilt codebases use (applying the plugin in every module with Hilt annotations for consistent annotation-processor argument configuration), per [CITED: developer.android.com/training/dependency-injection/hilt-multi-module, WebSearch this session, MEDIUM confidence]. Bake `alias(libs.plugins.dagger.hilt)` + `ksp(libs.dagger.hilt.compiler)` into the `AndroidFeatureConventionPlugin` (D-13's expected plugin set already lists "Android feature (library + Compose + Hilt + common feature deps)") so every feature module gets it uniformly — do not special-case which features "need" it.
**Warning signs:** A feature module's `@HiltViewModel` class compiles fine locally but the DI graph silently fails to resolve it at runtime only when reached via a specific navigation path (matches PITFALLS.md's Pitfall 6 — "Koin trades compile-time for runtime" — the Hilt equivalent here is "forgetting the compiler dependency in one feature module while KSP is present in others").

### Pitfall 4: SharedPreferences key strings changing incidentally during the package rename

**What goes wrong:** D-16's two-commit pattern (pure move, then package rename) is specifically designed to make this attributable, but a careless find-and-replace during the package-rename commit could touch a string literal like `"selected_sessions"` or `"SHARED_PREFERENCES_KEY_AGENDA_FILTERS"` if the rename tooling isn't scoped to import statements and package declarations only.
**Why it happens:** IDE "rename package" refactors are generally safe for code symbols but a manual `sed`-based approach across many files risks touching string literals that happen to contain package-like substrings.
**How to avoid:** Diff the SharedPreferences key constants specifically (`BookmarksStoreImpl.PREF_SELECTED_SESSIONS`, `SessionFiltersServiceImpl.SHARED_PREFERENCES_KEY_FILTERS`, `DataCollectionSettingsServiceImpl.SHARED_PREFERENCES_KEY_ENABLED_DATA_SERVICES`) before/after each of those files' repackage commit — see Runtime State Inventory above.
**Warning signs:** Existing bookmarks/agenda filters/data-collection consent silently reset on the first launch after this phase's changes ship — this would only appear in production with real user data, not in CI or a fresh emulator, making it easy to miss if the literal-string diff isn't done explicitly.

### Pitfall 5: CI cache-key staleness after the module split (cache-efficiency only, not correctness)

**What goes wrong (VERIFIED this session, not previously documented accurately in CONTEXT.md's Integration Points):** `.github/workflows/ios.yml`'s three `actions/cache` steps key on `hashFiles('shared/**/*.kt', 'shared/build.gradle.kts', 'gradle/libs.versions.toml')` (Konan cache) and `hashFiles('iosApp/iosApp.xcodeproj/project.pbxproj', 'shared/**/*.kt')` (Xcode DerivedData) [VERIFIED: .github/workflows/ios.yml:39, :75, read this session]. Once code moves out of `shared/` into `core/*`, these hash patterns stop capturing changes to the moved code — the cache key will not change when `core:model`/`core:data`/etc. source changes, so CI may **serve a stale cache** (Gradle/Xcode still correctly rebuild based on their own up-to-date checks once the cache is restored, so this is not a correctness bug, only a cache-hit-rate/build-time regression). This directly matches the general "CI cache keys not updated for new module boundaries" trap already catalogued in `.planning/research/PITFALLS.md`'s Performance Traps table.
**Correction to CONTEXT.md's claim:** CONTEXT.md's Integration Points section says "`.github/workflows/android.yml` / `ios.yml` — Gradle task paths ... must be updated as tests/code move to new modules so CI still runs every test." This session's direct read of both workflow files found **no module-qualified task paths** in `android.yml` (`./gradlew testDebugUnitTest`, `assembleDebug`, `detekt`, `lint`, `connectedDebugAndroidTest` are all unqualified and will automatically pick up new modules with no edits needed) and exactly **one** module-qualified reference in `ios.yml` (`./gradlew :shared:embedAndSignAppleFrameworkForXcode`, which D-10 explicitly keeps pointed at `:shared` unchanged). The actual required CI edit is narrower than CONTEXT.md implies: only the three `hashFiles(...)` cache-key patterns in `ios.yml` need widening (e.g. add `'core/**/*.kt'`), not task paths in either workflow.
**How to avoid:** Widen the `hashFiles()` glob patterns in `ios.yml`'s three cache steps to include `core/**/*.kt` and `core/**/build.gradle.kts` alongside the existing `shared/**` patterns, ideally in the same commit that first moves code out of `shared/` (the `core:model` tracer, D-09).
**Warning signs:** iOS CI build times creep up after the module split with no corresponding code-complexity increase — check the cache-hit/miss log line in the `actions/cache` step output.

### Pitfall 6: `jvm()` target and `jvmTest` ownership after `core:network`/`core:data` split

**What goes wrong:** `shared/build.gradle.kts:35` currently declares a bare `jvm()` target used solely to run `GraphQLStoreJvmTest` (`shared/src/jvmTest/kotlin/.../store/graphql/GraphQLStoreJvmTest.kt`) [VERIFIED: shared/build.gradle.kts:35, shared/src/jvmTest path, read this session]. Once `GraphQLStore`/`Mappers` move to `core:data` and `Apollo.kt`/`ApolloCache.kt` move to `core:network`, the `jvm()` target and the `jvmTest` source set must move to whichever module ends up owning `GraphQLStoreJvmTest`'s subject-under-test — most likely `core:data` (since `GraphQLStore` is the class under test) — but the test may also need `core:network`'s Apollo test utilities on its test classpath.
**Why it happens:** A `jvm()` target existing only to host one test file is easy to either duplicate (declaring `jvm()` on both `core:data` and `core:network` "just in case") or drop by accident during the split.
**How to avoid:** This is explicitly flagged as Claude's Discretion in CONTEXT.md ("How `jvm()` target / `jvmTest` ... move with their code — tests follow the code they test") — the plan should decide definitively (likely: `jvm()` + `jvmTest` on `core:data`, since that's `GraphQLStoreJvmTest`'s subject) and verify the test still runs post-move with an explicit `./gradlew :core:data:jvmTest` (or equivalent) check, not just relying on the unqualified `./gradlew testDebugUnitTest` (Android-target test task) which won't invoke a `jvm()` target's tests.
**Warning signs:** `GraphQLStoreJvmTest` silently stops running in CI because no module declares a `jvm()` target anymore, and the unqualified Android CI task list doesn't surface its absence (a jvm-target test task has a different name pattern than `testDebugUnitTest`).

## Code Examples

### Convention plugin skeleton (KMP library type — `core:model`, `core:network`, `core:data`, `core:analytics`, `core:testing`)

```kotlin
// build-logic/convention/src/main/kotlin/KmpLibraryConventionPlugin.kt
import com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryTarget
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.getByType

class KmpLibraryConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            pluginManager.apply("com.android.kotlin.multiplatform.library")
            pluginManager.apply("org.jetbrains.kotlin.multiplatform")
            pluginManager.apply("org.jetbrains.kotlin.plugin.serialization")
            pluginManager.apply("dev.detekt")
            // AndroidSdk constants moved from buildSrc (D-13)
            extensions.configure<com.android.build.api.dsl.KotlinMultiplatformAndroidLibraryExtension> {
                namespace = "com.gdgnantes.devfest.${target.name}" // planner refines exact scheme, D-15
                compileSdk = AndroidSdk.compile
                minSdk = AndroidSdk.min
            }
        }
    }
}
```
[CITED: shape synthesized from `shared/build.gradle.kts`'s existing working plugin block (shared/build.gradle.kts:1-42, VERIFIED read this session) converted to Gradle's convention-plugin idiom per NIA's documented approach — the exact `com.android.build.api.dsl.*` extension type names for `com.android.kotlin.multiplatform.library` are AGP-9-specific and should be confirmed against the AGP 9.4.0 Javadoc/KDoc at implementation time, not assumed from this snippet]

### Registering a convention plugin (`build-logic/convention/build.gradle.kts`)

```kotlin
gradlePlugin {
    plugins {
        register("kmpLibrary") {
            id = "devfest.kmp.library"
            implementationClass = "KmpLibraryConventionPlugin"
        }
        register("androidFeature") {
            id = "devfest.android.feature"
            implementationClass = "AndroidFeatureConventionPlugin"
        }
        // ... one register block per convention plugin type
    }
}
```
[CITED: standard `gradlePlugin { plugins { register(...) } }` DSL — WebSearch cross-checked across multiple NIA-replication guides this session, MEDIUM confidence]

### Consuming a convention plugin in a leaf module (`core/model/build.gradle.kts`, new tiny file)

```kotlin
plugins {
    id("devfest.kmp.library")
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
        }
    }
}
```

### Feature module entry point (D-07's Nav3-ready shape, applied to `feature:agenda`)

```kotlin
// feature/agenda/src/main/.../AgendaRoute.kt
@Composable
fun AgendaRoute(
    onSessionClick: (Session) -> Unit,
    viewModel: AgendaViewModel = hiltViewModel(),
) {
    // existing Agenda() composable body, unchanged — only the wrapper name/entry point is new
}
```
[VERIFIED: pattern matches the existing `Agenda(onSessionClick = onSessionClick)` call already present at Home.kt:147-150, read this session — this is a rename/wrap of an already-callback-shaped composable, not a new pattern]

## State of the Art

| Old Approach (this repo, today) | Current/Target Approach (this phase) | When Changed | Impact |
|--------------------|------------------|---------------|--------|
| `buildSrc` for shared build constants (`AndroidSdk`) | `build-logic` included build with convention plugins | This phase (D-13) | Per-plugin cache invalidation instead of whole-build invalidation on any `buildSrc` change — user was briefed on this tradeoff in the CONTEXT.md discuss session |
| Two Gradle modules (`:androidApp`, `:shared`) each with a hand-written, duplicated `detekt {}`/plugin block | 14 modules (`build-logic` + 12 leaf + 2 existing) sharing convention-plugin-defined config | This phase | Eliminates the exact duplication ARCH-01 targets |
| `shared` is the single fat KMP module (all domain code) | `shared` is a thin umbrella re-exporting `core:model`/`core:data`/`core:analytics` via `api` + `export()` | This phase (D-10) | Umbrella pattern is the standard fix for KMP's "three-framework problem" — [CITED: kotlinlang.org's own umbrella-module guidance, cross-referenced via WebSearch this session] |
| Feature code lives inside `:androidApp`'s `ui/screens/*` packages | Feature code lives in dedicated `feature:*` Gradle modules | This phase | Enables independent module builds/tests; sets up (but does not implement) the later Nav3 migration's `api`/`impl` split |

**Deprecated/outdated:** Nothing about the existing patterns is deprecated by upstream tooling — this is a structural reorganization of already-current code (Kotlin 2.4.20, AGP 9.4.0, Compose BOM 2026.09.00, all confirmed current as of Phase 2, no re-verification needed since Phase 3 bumps no versions).

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | A dependency reachable via `api` but not `export()`-ed is visible to Swift under a module-prefixed name rather than causing a build failure | Architecture Patterns, Pattern 3 | If wrong (e.g. it actually fails to compile, or is fully invisible), the D-16 verification step ("inspect the generated `shared.h`") would need to become a hard gate rather than a naming-diff check — low risk since D-11 already scopes exports narrowly and D-16 already mandates the `shared.h` inspection regardless |
| A2 | Applying the Hilt Gradle plugin in every `feature:*` module (not just `:androidApp`) is the correct, working pattern for `@HiltViewModel` in a multi-module Hilt setup at this repo's scale | Standard Stack, Common Pitfalls #3 | Already a locked decision (D-02); if this assumption is wrong, the risk surfaces immediately as a compile error in the first feature-module extraction (`feature:venue`, per D-18's order), not silently — low risk, self-correcting early |
| A3 | `build-logic`'s own settings file must stay plain `.kts` because `versionCatalogs { create("libs") { from(files(...)) } }` is not established as portable to `.dcl` | Common Pitfalls #1, #2 | If DCL actually does support this construct, the only cost of this assumption being conservative is a missed (optional) DCL-adoption opportunity — D-20 already scopes DCL adoption away from new modules, so this assumption is consistent with, not contradicting, the locked decision |
| A4 | `GraphQLStoreJvmTest`'s `jvm()` target should move to `core:data` rather than `core:network` | Common Pitfalls #6 | Low risk — explicitly flagged as Claude's Discretion in CONTEXT.md; wrong initial placement is caught immediately by a failing/missing test-run verification step, not a silent regression |
| A5 | The AGP 9.4.0 Kotlin-DSL extension type for configuring `com.android.kotlin.multiplatform.library` inside a convention plugin is named `KotlinMultiplatformAndroidLibraryExtension` (used in the Code Examples skeleton) | Code Examples | If the exact type/package name differs in AGP 9.4.0's actual API, the convention-plugin skeleton code won't compile as literally written — flagged inline in that section as needing confirmation against AGP 9.4.0's KDoc at implementation time; this is a code-example detail only, not an architectural claim |

## Open Questions

1. **Exact placement of `SessionFiltersService` and `DataCollectionSettingsService`**
   - What we know: `SessionFiltersService` is consumed by `AgendaViewModel` (→ `feature:agenda`) and `HomeViewModel` (→ stays in `:androidApp` per D-07) [VERIFIED: grep this session]. `DataCollectionSettingsService` is consumed by `DataSharingInitializer` (→ stays in `:androidApp`'s `core/`) and `DataCollectionViewModel` (→ `feature:settings`) [VERIFIED: grep this session].
   - What's unclear: Since `:androidApp` already depends on every `feature:*` module (app→feature is an allowed edge), both services *could* live in their respective feature module with `:androidApp` importing them — or they could live in a lower-level module if a natural `core:*` home exists. Neither has a domain-model home in `core:data`/`core:analytics` (they're `SharedPreferences`-backed, Android-only, UI-adjacent settings, not shared business data).
   - Recommendation: Place `SessionFiltersService` in `feature:agenda` and `DataCollectionSettingsService` in `feature:settings` (matching their primary UI-facing consumer), with `:androidApp`'s `HomeViewModel`/`DataSharingInitializer` importing the feature module's public interface — this is explicitly CONTEXT.md's "Claude's Discretion" item and the planner should confirm this reading against D-03's "lowest module that needs it" wording before finalizing.

2. **Whether `androidApp`'s unqualified `testDebugUnitTest` CI task actually exercises new KMP `core:*` modules' Android-target unit tests**
   - What we know: `android.yml`'s `unit-tests` job runs unqualified `./gradlew testDebugUnitTest` [VERIFIED: .github/workflows/android.yml:46, read this session]. Modules built with `com.android.kotlin.multiplatform.library` (AGP 9's new plugin) may not register build-type-suffixed task names (`testDebugUnitTest`) the same way a plain `com.android.library` module does, since KMP-Android targets historically use a different task-naming convention (e.g. `testReleaseUnitTest` only, or `androidUnitTest` without a build-type axis).
   - What's unclear: This session did not build a `core:*` module against AGP 9.4.0 to observe the actual generated task graph (no such module exists yet — this phase creates the first ones).
   - Recommendation: After the `core:model` tracer step lands (D-09), run `./gradlew :core:model:tasks --group verification` (or equivalent) and confirm which unit-test task name(s) are actually generated, then verify the unqualified `testDebugUnitTest` invocation in `android.yml` picks them up — if it doesn't, either add an explicit task reference to the workflow or reconcile the task name expectation with `core:model`'s actual test task before extracting `core:data` (whose tests matter more, per `DevFestNantesStoreContractTest`).

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| `android` CLI | D-18's Android smoke-verification steps (`android run`, `android layout`, `android screen capture`) | ✓ | 1.0.16261425 | — |
| `gh` CLI | Opening/checking the phase PR, watching CI runs | ✓ | 2.100.0 | Fall back to `git`/GitHub web UI if needed |
| Gradle (wrapper) | Every build/verification step | ✓ | 9.7.1 | — |
| Kotlin (via Gradle wrapper report) | Compilation | ✓ (wrapper reports 2.4.0; project catalog pins 2.4.20 — [VERIFIED: gradle/libs.versions.toml:24] is the authoritative project version, the wrapper's own bundled Kotlin DSL runtime version is a separate, non-blocking number) | 2.4.20 (project) | — |
| Xcode | iOS CI/local simulator smoke checks (D-12) | ✓ | 27.0 (Build 27A266a) | — |
| JVM | Gradle daemon | ✓ | OpenJDK 25.0.3 (launcher); Gradle daemon JVM configured to OpenJDK 17 via `org.gradle.java.home` | — |

**Missing dependencies with no fallback:** none identified.
**Missing dependencies with fallback:** none identified — all tooling this phase needs was confirmed present this session.

## Validation Architecture

### Test Framework

| Property | Value |
|----------|-------|
| Framework | JUnit 4 (`junit:junit:4.13.2`) + `kotlin.test` for `commonTest`/`jvmTest`, Espresso + Compose UI test for `androidTest` [VERIFIED: gradle/libs.versions.toml — `junit`, `kotlin-test` entries; androidApp/build.gradle.kts `androidx.test.espresso`/`androidx-compose-ui-test-junit4` deps, read this session] |
| Config file | No dedicated test-runner config beyond `testOptions { unitTests { isReturnDefaultValues = true; isIncludeAndroidResources = true } }` in `androidApp/build.gradle.kts` — this block must be preserved by the `AndroidApplicationConventionPlugin` |
| Quick run command | `./gradlew detekt` (fast lint check, catches most structural mistakes in a move-heavy phase) |
| Full suite command | `./gradlew testDebugUnitTest connectedDebugAndroidTest` (unchanged, unqualified — see Common Pitfalls #5's correction) plus, once `core:*` modules exist, an explicit `./gradlew :core:data:jvmTest` / `:core:data:commonTest`-equivalent per Open Question 2 |

### Phase Requirements → Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| ARCH-01 | Convention plugins apply cleanly, no duplicated config | build/config check | `./gradlew help --task :core:model:tasks` (confirms plugin applies) + `./gradlew detekt` (confirms shared config wired) | ✅ existing config, ❌ new modules (created this phase) |
| ARCH-02 | `core:*` modules exist, never depend on `feature:*` | dependency-graph spot-check | `./gradlew :core:model:dependencies --configuration commonMainImplementation` (manual review, no automated lint this phase per CICD-V2-01 deferral) | ❌ Wave 0 — no modules exist yet |
| ARCH-03 | `feature:*` modules exist, each with ViewModel(s) + Compose screens | existing test classes move with code | `./gradlew testDebugUnitTest` (existing `MainActivityTest`, `ScheduleSlotDateFormatAndroidTest`) — [VERIFIED: androidApp/src/androidTest/java/..., read this session, only 2 android test files exist] | ✅ existing files, must be repointed as they move |
| ARCH-04 | `iosApp` builds against a single umbrella framework | manual + CI | iOS CI (`./gradlew :shared:embedAndSignAppleFrameworkForXcode` unchanged) + manual simulator smoke run at 2 checkpoints per D-12 | ✅ existing CI workflow, unchanged task |
| DevFestNantesStoreContractTest / ScheduleSlotDateParsingTest (commonTest) | Business-logic parity after `core:data`/`core:model` extraction | unit (commonTest) | `./gradlew :core:data:commonTest` or wherever these land post-move (Claude's Discretion item) | ✅ exists in `shared/src/commonTest`, moves per discretion |
| GraphQLStoreJvmTest (jvmTest) | GraphQL store parity | unit (jvmTest) | `./gradlew :core:data:jvmTest` (assuming A4's placement) | ✅ exists in `shared/src/jvmTest`, moves per discretion |

### Sampling Rate

- **Per task commit (D-18's "one commit per module move"):** `./gradlew detekt` + the specific module's test task
- **Per D-18 checkpoint (after core/data, after core/ui, after last feature):** full Android smoke via `android run` + `android layout`/`screen capture`, plus `./gradlew testDebugUnitTest connectedDebugAndroidTest`
- **Phase gate:** Full Android + iOS CI green, plus the D-12 iOS simulator manual smoke run, before `/gsd-verify-work`

### Wave 0 Gaps

- [ ] No `core:*` module exists yet — every module-scoped test command in the table above is a Wave 0 gap until `build-logic` + the `core:model` tracer land (D-09's first plan)
- [ ] Confirm actual generated Android-target unit-test task name for `com.android.kotlin.multiplatform.library`-typed modules once `core:model` exists (Open Question 2) — this determines whether `android.yml`'s unqualified `testDebugUnitTest` needs an explicit task addition
- [ ] Decide final `jvm()`/`jvmTest` and `commonTest` module ownership (Claude's Discretion / A4) before the `core:data` extraction step, so `GraphQLStoreJvmTest`/`DevFestNantesStoreContractTest`/`ScheduleSlotDateParsingTest` don't go dark mid-phase

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V1 Architecture, Design and Threat Modeling | Yes — this phase *is* an architecture change | Unidirectional module graph (Pattern 2) as the architectural control; no automated lint yet (CICD-V2-01 deferred to v2) — code review is the interim control |
| V2 Authentication | No | Not touched — no auth surfaces exist in this app |
| V3 Session Management | No | Not touched |
| V4 Access Control | No | Not touched |
| V5 Input Validation | No new surfaces | Existing GraphQL response parsing (Apollo-generated, kotlinx.serialization) moves modules unchanged — no new input parsing introduced |
| V6 Cryptography | No | Not touched — no crypto in this app |
| V7 Error Handling and Logging | Indirect | Timber logging call sites move with their owning classes unchanged; `FirebaseAnalyticsService`'s `Timber.e(exception)` in `ExternalContentService` stays as-is per the "pure move, no drive-by fixes" rule (Pitfall 7 in prior research) |

### Known Threat Patterns for this stack

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Accidental `core:*` → `feature:*` dependency reintroducing coupling that could leak feature-specific state/secrets into a lower, more widely-reused module | Information Disclosure (broadened blast radius, not a direct leak) | Convention-plugin typing + code review per commit (Pattern 2); no automated gate this phase |
| SharedPreferences key-string drift during package rename silently resetting user consent/preference state (Runtime State Inventory, Pitfall 4) | Tampering (unintended state change, not attacker-driven) | Explicit before/after diff of the literal key-string constants at the relevant repackage commit |
| Umbrella framework accidentally exporting `core:network` (Apollo internals, API endpoint config) to Swift when only `core:model`/`core:data`/`core:analytics` should be exported (D-11) | Information Disclosure (implementation details needlessly surfaced to the iOS binary's public Swift API) | D-11's explicit export list + the `shared.h` diff verification step (D-16) |

## Sources

### Primary (HIGH confidence — direct codebase reads this session)
- `shared/build.gradle.kts`, `androidApp/build.gradle.kts`, `settings.gradle.dcl`, `build.gradle.kts`, `buildSrc/src/main/java/Dependencies.kt`, `gradle/libs.versions.toml` — read in full this session
- `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/{MainActivity.kt, core/injection/AppModule.kt, ui/screens/Home.kt, ui/screens/Screen.kt, ui/BookmarksViewModel.kt, ui/screens/about/About.kt, services/*.kt}` — read in full this session
- `.github/workflows/android.yml`, `.github/workflows/ios.yml` — read in full this session
- `.planning/STATE.md` (Phase 02 DCL pilot outcome, version deviations) — read this session
- `iosApp/iosApp/Agenda/AgendaViewModel.swift` (confirms `import shared`, `import KMPNativeCoroutinesAsync` usage) — read this session

### Secondary (MEDIUM confidence — official docs fetched/cross-checked this session)
- [kotlinlang.org — Build final native binaries](https://kotlinlang.org/docs/multiplatform/multiplatform-build-native-binaries.html) — `export()`/`api` mechanics, per-target repetition, `transitiveExport` warning; fetched live this session via WebFetch
- [developer.android.com — Hilt in multi-module apps](https://developer.android.com/training/dependency-injection/hilt-multi-module) — multi-module Hilt plugin application pattern; WebSearch this session
- [declarative.gradle.org — Example Project Migration](https://declarative.gradle.org/docs/reference/migration-case-study/) — `includeBuild` support in DCL `pluginManagement`; WebSearch this session
- [github.com/android/nowinandroid — ModularizationLearningJourney.md](https://github.com/android/nowinandroid/blob/main/docs/ModularizationLearningJourney.md) — referenced via prior research/ARCHITECTURE.md and SUMMARY.md, official Google sample
- Community NIA-replication guides (dev.to/coltonidle, medium.com/@beranger.guillaume, michiganlabs.com) — convention-plugin/`build-logic` settings-file wiring pattern; WebSearch this session

### Tertiary (LOW confidence, flagged for validation)
- Exact AGP 9.4.0 Kotlin-DSL extension type names for `com.android.kotlin.multiplatform.library` used in the Code Examples skeleton (`KotlinMultiplatformAndroidLibraryExtension`) — not verified against AGP 9.4.0's actual KDoc this session, flagged in Assumptions Log (A5)
- The "module-prefixed name without `export()`" claim (Pattern 3) — MEDIUM-sourced via community umbrella-module writeups, not a direct official-docs quote; flagged in Assumptions Log (A1)

## Metadata

**Confidence breakdown:**
- Standard stack: HIGH — no new packages, all versions already verified live in Phase 2, direct file reads this session confirm current plugin/catalog state
- Architecture: MEDIUM-HIGH — module-boundary theory and `export()` mechanics confirmed against official docs fetched this session; exact AGP 9.4.0 convention-plugin API surface not hands-on verified (flagged as an assumption)
- Pitfalls: MEDIUM-HIGH — two pitfalls (CI cache keys, Hilt-per-module) are corrections/confirmations grounded in direct file reads this session; DCL scoping pitfall repeats Phase 2's own live-verified precedent

**Research date:** 2026-09-22
**Valid until:** 30 days (stable domain — no fast-moving dependency versions in scope this phase; re-check only if Phase 2's pinned versions change before Phase 3 execution starts)
