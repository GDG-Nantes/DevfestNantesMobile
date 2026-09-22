# Phase 3: Multi-Module Architecture Extraction - Context

**Gathered:** 2026-09-22
**Status:** Ready for planning

<domain>
## Phase Boundary

The codebase (today: `:androidApp` + `:shared`) is decomposed into a Now in Android-inspired module graph adapted to KMP: convention plugins in a `build-logic` included build, `core/*` modules (model, network, data, analytics, ui, testing) and `feature/*` modules (agenda, speakers, venue, session-detail, about, settings), with a strictly unidirectional graph (`app → feature → core`, no feature→feature, core never depends on feature). `iosApp` keeps consuming one single Kotlin/Native framework named `shared`. Pure structural refactor: zero user-visible behavior change on Android and iOS.

**Not in this phase:** Hilt→Koin migration (Phase 4), Navigation 3 migration (deferred), type-safe routes, `api`/`impl` feature split (ARCH-V2-01), dependency-graph lint in CI (CICD-V2-01), test-coverage expansion (Phase 5), drive-by behavior fixes (`println`→Timber, `!!` nav args, etc.).

</domain>

<decisions>
## Implementation Decisions

### DI during the split
- **D-01:** Dagger Hilt stays the only DI framework throughout Phase 3. No Koin code of any kind (not even empty `module { }` stubs) — Koin is entirely Phase 4. Rationale: one variable per phase (research Anti-Pattern 4 / Pitfall 5: don't bundle module extraction with a DI-framework swap).
- **D-02:** The single Hilt `AppModule` stays in `:androidApp` and keeps providing the store, services, etc. Feature modules only carry their `@HiltViewModel` classes (feature modules therefore apply the Hilt plugin + KSP via their convention plugin). No per-module Hilt `@Module` split — Phase 4 dismantles `AppModule` anyway.
- **D-03:** Android-only service implementations currently in `androidApp/.../services/` move into the `androidMain` source set of the core module owning their interface (e.g. `BookmarksStoreImpl` → `core/data` androidMain, `FirebaseAnalyticsService` → `core/analytics` androidMain). `:androidApp` becomes a thin shell (Application, MainActivity, Home shell, navigation, `AppModule`, initializers). Services with no core interface (`SessionFiltersService`, `DataCollectionSettingsService`, `ExternalContentService`) go to the module that owns their consumer, per the same "lowest module that needs it" logic — planner decides exact placement.
- **D-04:** ROADMAP Phase 3 success criterion 3 and REQUIREMENTS ARCH-03 are **amended** (done alongside this CONTEXT.md): the "own Koin module" clause moves to Phase 4 (DI-02), so the verifier must not flag the absence of Koin modules in Phase 3 as a gap.

### Feature boundaries
- **D-05:** Feature module set = `feature/agenda`, `feature/speakers`, `feature/venue`, `feature/session-detail`, `feature/about`, `feature/settings`. `feature/about` = About tab (About*, header/links/communities/social/version) + Partners (`PartnersViewModel`, `PartnerCard`). `feature/settings` = Settings screen + DataCollection (screen, agreement dialog, `DataCollectionViewModel`) + Legal. ARCH-03's list is amended accordingly (about added, bookmarks removed — see D-06).
- **D-06:** **No `feature/bookmarks` module** — there is no bookmarks screen. `BookmarksStore` (interface + Android impl) lives in `core/data`; `BookmarksViewModel` and the shared bookmark-toggle UI live in `core/ui`, consumed by `feature/agenda` (row toggle + favorites filter) and `feature/session-detail`.
- **D-07:** Navigation stays on Navigation Compose 2 with string routes, but is shaped **Nav3-ready**: features expose stateless screen entry points taking callbacks (e.g. `AgendaRoute(onSessionClick, onSpeakerClick)`); **no `NavController` is ever passed into or referenced from a feature module**. `:androidApp` owns `MainActivity`, the Home shell (bottom bar + nested tab `NavHost`, `HomeViewModel`), both `NavHost`s, the `Screen` route definitions, and wires all cross-feature navigation through callbacks. Goal: a later Navigation 3 migration (NavKeys + `EntryProviderScope` entry builders + `NavDisplay`) only touches the app module plus a thin per-feature addition. No route/argument format change in this phase.
- **D-08:** `core/ui` content rule: anything consumed by ≥2 features (or by the app + a feature) moves to `core/ui`, together with the theme (`Color`/`Theme`/`Type`) and `UiState`. Single-consumer composables/utils stay in their feature. Planner applies the rule file by file (e.g. `SessionCategory`/`SessionComplexity`/`SessionType` chips, `SpeakerPicture`, `SocialIcon`, `LoadingLayout`, app bars, `utils/*`).

### iOS umbrella framework
- **D-09:** De-risk the "three-framework problem" with a **tracer-first plan** (same pattern as Phases 1/2): first plan = `build-logic` convention plugins + extract `core/model` only, re-exported by `:shared`, proven by iOS CI green + a local simulator run. If the umbrella/export approach fails, stop and rethink before any other module moves. — **Reversibility:** reversible — the tracer is the gate precisely so the pattern can be abandoned cheaply.
- **D-10:** `:shared` becomes a **thin umbrella** and keeps its Gradle path `:shared`, framework `baseName = "shared"` (static), and the Xcode build phase `./gradlew :shared:embedAndSignAppleFrameworkForXcode`. Its source code moves out to `core/*`; it only declares the iOS framework binaries and `export()`s modules. Swift `import shared` and the Xcode project stay unchanged. **Note:** iOS uses direct integration (`embedAndSignAppleFrameworkForXcode`), **not CocoaPods** — research/SUMMARY.md's CocoaPods wording is inaccurate for this repo. — **Reversibility:** costly — renaming the framework later means editing the Xcode build phase and every Swift `import shared`.
- **D-11:** The umbrella `export()`s **only modules whose types Swift touches**: `core/model`, `core/data` (`DevFestNantesStore`, `DevFestNantesStoreBuilder`, `BookmarksStore`), `core/analytics` (`AnalyticsService`). `core/network` (Apollo, generated GraphQL types) is an internal implementation dependency, not exported. Swift-visible type/member names must not change (KMP-NativeCoroutines `asyncSequence`/`asyncFunction` usage in Swift must keep compiling). Export dependencies must be `api` in the umbrella.
- **D-12:** iOS verification depth: iOS CI green on every pushed step; **plus** a manual iOS simulator smoke run (agenda, speakers, venue, about render and load data) at two checkpoints — the `core/model` tracer and after the `core/data` extraction. Android-only moves (core/ui, features) only need iOS CI green.

### Rollout & conventions
- **D-13:** Convention plugins live in a **`build-logic` included build** (`build-logic/convention`, wired via `includeBuild("build-logic")` in `settings.gradle.dcl`'s `pluginManagement`), NIA-style, reading the root `gradle/libs.versions.toml`. Expected plugin set (planner may refine): KMP library, Android library, Android feature (library + Compose + Hilt + common feature deps), Android application, plus shared detekt config. `buildSrc`'s `AndroidSdk` constants (min 26 / compile 37 / target 36 — targetSdk deliberately decoupled, keep the comment's rationale) move into `build-logic`; `buildSrc` is deleted. User chose this after an explanation of `buildSrc` (whole-build cache invalidation on any change) vs included build (per-plugin invalidation, catalog access, Gradle/Google-recommended for multi-module).
- **D-14:** Module paths are **nested**: `:core:model`, `:core:network`, `:core:data`, `:core:analytics`, `:core:ui`, `:core:testing`, `:feature:agenda`, `:feature:speakers`, `:feature:venue`, `:feature:session-detail`, `:feature:about`, `:feature:settings` (dirs `core/model`, `feature/agenda`, …). Each module gets its own unique Android namespace.
- **D-15:** Kotlin packages are **renamed to match modules** (e.g. `com.gdgnantes.devfest.core.model`, `com.gdgnantes.devfest.core.data`, `com.gdgnantes.devfest.feature.agenda`) — exact package scheme at planner's discretion as long as it mirrors the module path consistently. — **Reversibility:** costly — touches imports in every file across Android and the Apollo `packageName` config.
- **D-16:** Per module, **two commits: (1) pure `git mv` into the new module with packages unchanged, build green; (2) package rename + import updates**. Keeps git rename tracking and makes breakage attributable. Kotlin/Native exports class names to Swift without the package prefix, so Swift names should stay stable — the tracer must confirm no new ObjC name collisions/renames (e.g. inspect the generated `shared.h` before/after).
- **D-17:** Android resources move **with their owner**: feature-specific strings/drawables → the feature; shared ones → `core/ui`; app name, launcher icons, splash/theme-manifest resources stay in `:androidApp`. `values-fr` and `values-night` variants move in lockstep; translations stay byte-identical. Non-transitive R classes; each module references its own `R`.
- **D-18:** **One phase branch/PR, one commit per step** (move + repackage per module), bottom-up order: `build-logic` → `core/model` (tracer) → `core/network` / `core/analytics` → `core/data` → `core/testing` → `core/ui` → features least-coupled first (`venue` → `about` → `settings` → `speakers` → `agenda` → `session-detail` last) → `:shared` thinned to pure umbrella + `buildSrc` removal. Full CI (Android + iOS) green per pushed step. Android manual smoke (`android run` + `android layout`/`screen capture`, all tabs + session detail + speaker detail + settings) at three checkpoints: after `core/data`, after `core/ui`, after the last feature.
- **D-19:** `core/testing` (KMP) is created now and receives the existing fakes — `DevFestNantesStoreMocked` and the `model/stubs/*` (`StoreStubs`, `CategoryStubs`, `RoomStubs`, `SocialItemStubs`) — **if** they're only consumed by tests/previews. Anything production code or Compose previews rely on must stay reachable without shipping test code in release (planner verifies actual consumers first; if previews need stubs, they reach them via a debug-only/preview dependency or the stubs stay where production code can see them). Phase 4 (DI-05) / Phase 5 extend it. Do NOT fix `StoreStubs`' unseeded RNG here (that's TEST-01, Phase 5).
- **D-20:** New module build files are `build.gradle.kts` (tiny, applying convention plugins). No DCL pilot on new modules. The researcher re-runs Phase 2's live Declarative Gradle status check (software-type readiness on stable Gradle) and records the outcome; `settings.gradle.dcl` stays DCL and must accommodate the new `include(...)` lines + `includeBuild("build-logic")` — if DCL settings can't express `includeBuild` in `pluginManagement`, that's a documented fallback case.

### Claude's Discretion
- Exact placement of services with no core interface (`SessionFiltersService`, `DataCollectionSettingsService`, `ExternalContentService`) and of `core/` utilities (`CoroutinesDispatcherProvider`, `SuspendUseCase`, performance helpers) per D-03's "lowest module that needs it" logic.
- Exact convention plugin names/granularity and exact package naming scheme (D-13, D-15), within the stated principles.
- File-by-file application of the `core/ui` rule (D-08) and resource-ownership rule (D-17).
- How `jvm()` target / `jvmTest` (`GraphQLStoreJvmTest`) and `commonTest` (`DevFestNantesStoreContractTest`, `ScheduleSlotDateParsingTest`) move with their code — tests follow the code they test.
- Detekt configuration per module (via convention plugin, keeping `linters/detekt-config.yml` as the single config).

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Project-level context
- `.planning/PROJECT.md` — core value (no behavior regression), constraints (use `android` CLI for Android tasks, KMP solutions must work on Android + iOS, GitHub Actions), Key Decisions
- `.planning/REQUIREMENTS.md` §ARCH — ARCH-01..04 (ARCH-03 amended by D-04/D-05), §Out of Scope (no per-screen modules, no full NIA taxonomy, no CMP), §v2 (ARCH-V2-01 api/impl split, CICD-V2-01 graph lint — both deferred)
- `.planning/ROADMAP.md` §Phase 3 — goal, success criteria (SC3 amended by D-04)
- `.planning/STATE.md` — Phase 02 DCL pilot outcome (re-check obligation for Phase 3, D-20), Apollo cache import paths, kotlinx-datetime `Clock` note, Blockers/Concerns (About/Partners home — resolved by D-05; umbrella spike — resolved by D-09)

### Research
- `.planning/research/SUMMARY.md` — Phase 3 rationale, bottom-up extraction order, three-framework pitfall (note: its CocoaPods wording is wrong for this repo, see D-10)
- `.planning/research/ARCHITECTURE.md` — target module graph, dependency-direction pattern, KMP vs Android-only module split
- `.planning/research/PITFALLS.md` — three-framework problem, "no regression vs incidental cleanup" (keep pure-move commits separate, D-16)

### Official docs
- `kb://android/guide/navigation/navigation-3/modularize` (via `android docs fetch`) — Nav3 modularization pattern (api/impl, `EntryProviderScope` entry builders, app-owned `NavDisplay`) that D-07 prepares for
- Now in Android `build-logic` / modularization learning journey — https://github.com/android/nowinandroid/blob/main/docs/ModularizationLearningJourney.md

### Prior phase context
- `.planning/phases/02-dependency-build-tooling-upgrade/02-CONTEXT.md` — staging discipline (commit per step, green CI between steps, local sanity checkpoints) that D-18 reuses
- `.planning/phases/01-ci-pipeline-fixed-optimized/01-CONTEXT.md` — CI workflows / `android-setup` composite action that every Phase 3 step runs through

### Codebase maps
- `.planning/codebase/STRUCTURE.md`, `.planning/codebase/ARCHITECTURE.md` — current layer layout being split
- `.planning/codebase/TESTING.md` — existing test locations that move with their code

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `gradle/libs.versions.toml` — single source of truth the convention plugins read (D-13).
- `buildSrc/src/main/java/Dependencies.kt` — `AndroidSdk` constants (min 26, compile 37, target 36 with decoupling rationale) to migrate into `build-logic`.
- `linters/detekt-config.yml` — shared detekt config to apply from a convention plugin.
- `DevFestNantesStoreMocked`, `model/stubs/*` (shared commonMain) — candidates for `core/testing` (D-19).
- `DevFestNantesStoreContractTest`, `ScheduleSlotDateParsingTest` (commonTest), `GraphQLStoreJvmTest` (jvmTest) — move with the code they test.

### Established Patterns
- `shared/build.gradle.kts`: `com.android.kotlin.multiplatform.library` + KMP + serialization + KSP + `kmpNativeCoroutines` + Apollo (with `com.apollographql.cache` compiler plugin + `packageName` `com.gdgnantes.devfest.graphql`), targets android/jvm/iosX64/iosArm64/iosSimulatorArm64, static framework `baseName = "shared"`, `ExperimentalObjCName` opt-in → the KMP convention plugin template; Apollo config moves to `core/network`.
- `androidApp/build.gradle.kts`: Hilt + KSP, Compose, Firebase (analytics/config/crashlytics/perf), secrets plugin, BuildConfig OpenFeedback fields, `kotlin-metadata-jvm` force + `firebase-auth-ktx` substitution in `configurations.configureEach` → those resolution rules must keep applying wherever Hilt/openfeedback end up (likely a convention plugin or kept on the app — planner verifies).
- Navigation: root `NavHost` in `MainActivity.kt`, nested bottom-tab `NavHost` in `ui/screens/Home.kt`, string routes + `screenFromRoute` in `ui/screens/Screen.kt`.
- Swift consumes only `DevFestNantesStore`, `DevFestNantesStoreBuilder`, `AnalyticsService`, model classes, via `KMPNativeCoroutinesAsync/Combine` (`asyncSequence`, `asyncFunction`).

### Integration Points
- `settings.gradle.dcl` — add `includeBuild("build-logic")` + all new `include(...)` lines.
- Root `build.gradle.kts` — `apply false` plugin list (cocoapods alias present but unused by iOS integration).
- `iosApp/iosApp.xcodeproj/project.pbxproj` build phase `./gradlew :shared:embedAndSignAppleFrameworkForXcode` — must keep working unchanged (D-10).
- `androidApp/src/main/res` (values, values-fr, values-night, drawable*, xml) — split per D-17.
- `.github/workflows/android.yml` / `ios.yml` — Gradle task paths (`:shared:jvmTest`, `:androidApp:testDebugUnitTest`, `:shared:compileKotlinIosSimulatorArm64`, etc.) must be updated as tests/code move to new modules so CI still runs every test.
- Untracked `shared-ui/` directory at repo root (build leftovers, not in settings) — ignore / do not confuse with `core/ui`.

</code_context>

<specifics>
## Specific Ideas

- The user plans a later migration to **Navigation 3**; Phase 3 must not make it harder — hence D-07's "no NavController in features, app owns navigation".
- The user asked for an explanation of `buildSrc` vs `build-logic` before choosing; chose `build-logic` for per-plugin cache invalidation and catalog access.
- Same tracer + commit-per-step + green-CI-between-steps discipline as Phases 1 and 2.

</specifics>

<deferred>
## Deferred Ideas

- **Navigation 3 migration** — NavKeys (`@Serializable`), per-feature `EntryProviderScope` entry builders, app-owned `NavDisplay`/back stack, possibly feature `api`/`impl` split per the official Nav3 modularization guide. Own phase after this milestone's structural work (not in ROADMAP yet — add via `/gsd-phase` when ready).
- Type-safe (serializable) Navigation Compose routes — superseded by the Nav3 item above.
- Feature `api`/`impl` split — already tracked as ARCH-V2-01.
- Module dependency-graph lint in CI — already tracked as CICD-V2-01.

</deferred>

---

*Phase: 03-multi-module-architecture-extraction*
*Context gathered: 2026-09-22*
