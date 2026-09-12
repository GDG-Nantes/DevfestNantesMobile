# Pitfalls Research

**Domain:** KMP (Kotlin Multiplatform) app modernization — broken CI, mass dependency upgrade, Gradle Declarative DSL migration, Now-in-Android-style multi-module split (with iOS target), Hilt→Koin DI migration, and test-coverage retrofit, all in one milestone
**Researched:** 2026-09-12
**Confidence:** MEDIUM (web-sourced, cross-checked against official docs/repos; project-specific facts verified directly against this repo's code)

This file assumes the project's own stated phase order (see `PROJECT.md` Key Decisions): **1) CI iOS fix → 2) dependency upgrade + Gradle Declarative DSL → 3) multi-module split → 4) Hilt→Koin → 5) test coverage.** Pitfalls are tagged with which of the six sub-projects they apply to and which phase should own the fix.

## Critical Pitfalls

### Pitfall 1: Pinning Xcode but not the runner image (the CI fix regresses again)

**What goes wrong:**
The current `ios.yml` pins `runs-on: macos-latest` but hard-codes `xcode-select -s /Applications/Xcode_26.0.1.app` and a destination of `platform=iOS Simulator,name=iPhone 16` with no OS version. `macos-latest` is a moving target — GitHub periodically repoints it to a new image (e.g. `macos-26-arm64`), and each new image ships a different, evolving set of pre-installed Xcode versions and simulator runtimes. When the image moves out from under the pinned Xcode path (or the image keeps the Xcode version but drops/changes the simulator runtime bundled with it), `xcodebuild` can no longer resolve `name=iPhone 16` + implicit `OS=latest` and fails with exactly the error this project already hit. This is an actively recurring class of failure, not a one-off: GitHub's own `runner-images` repo tracks fresh instances of "simulator devices missing on new runner image" after every image bump.

**Why it happens:**
Teams fix the symptom (bump Xcode path, add a specific device name) without decoupling from the two things that actually drift: the runner image tag and the "OS:latest" resolution. A fix that passes today silently rots the next time GitHub rolls the `macos-latest` alias forward.

**How to avoid:**
- Pin the **runner image** explicitly (e.g. `macos-15` or a specific labeled image), not `macos-latest`.
- Add a `Show available simulators` step (already present in this workflow — keep it, but *use its output*: fail fast with a clear message if the exact device/OS pair is absent) before the build step, and prefer resolving destination by explicit `OS=<version>` rather than bare `name=` + implicit latest.
- Consider driving the simulator by UDID (`xcrun simctl create` a known device/runtime pair) instead of relying on whatever ships pre-provisioned, so the destination is deterministic regardless of image churn.
- Treat this as recurring maintenance, not a one-time fix: add a scheduled (e.g. monthly) CI run or Dependabot-style reminder to catch drift before a PR does.

**Warning signs:**
- Workflow was green for months then fails only on `macos-latest` jobs with no code change in the diff.
- `xcrun simctl list devices available` output in CI logs shows a device/OS combination that doesn't match the hardcoded destination string.

**Phase to address:** Phase 1 (CI iOS fix) — and re-verify at the end of Phase 2 (dependency/toolchain upgrade), since bumping Xcode-adjacent tooling (Cocoapods, SwiftGen, Kotlin/Native) can also shift what's resolvable.

---

### Pitfall 2: Simultaneous major-version bumps compound breaking changes instead of isolating them

**What goes wrong:**
Kotlin, AGP, Compose, Apollo Kotlin, and the Firebase BoM are bumped together "while we're in there." When something breaks, it's unclear whether it's a Kotlin/KGP↔AGP/Gradle/Xcode compatibility mismatch, an Apollo Kotlin behavior change, a Firebase BoM transitive conflict, or a third-party plugin (KSP, Compose compiler, DI codegen) that hasn't caught up yet to the newest Kotlin. Real-world reports show this exact failure mode: upgrades that work fine one minor version at a time start failing once several majors stack, because ecosystem plugins are version-locked to specific Kotlin releases and lag behind.

**Why it happens:**
"Upgrade everything to latest" feels efficient and is tempting to batch with the modularization/DI work already planned. But Kotlin↔AGP↔Gradle↔Xcode versions are tightly coupled (per Kotlin's own compatibility guide), Apollo Kotlin major bumps promote previously-warning-level deprecations to hard compile errors, and AGP major bumps (AGP 9) change the KMP Android plugin model itself (a new `androidMultiplatformLibrary`/`com.android.kotlin.multiplatform.library` plugin replaces applying `kotlin("android")` directly) — each of these is independently a non-trivial migration.

**How to avoid:**
- Upgrade in stages with a green build/CI checkpoint after each: Kotlin/KGP → Gradle → AGP → Compose → Apollo → Firebase → remaining third-party libs (Koin, Timber, Accompanist, etc.), not all-at-once.
- Before touching Apollo, read its major-version migration guide specifically for the "deprecated → error" promotion pattern — grep the codebase for any Apollo API usage flagged deprecated on the current version first.
- Before touching AGP, confirm target AGP version's KMP plugin model against what this project's `shared` module currently uses; treat "swap to `androidMultiplatformLibrary`" as its own step, not a side effect of a version bump.
- Pin exact versions in the version catalog and bump one row (or one logically-grouped set of rows) per commit so `git bisect` is possible if CI breaks later.

**Warning signs:**
- A single "upgrade dependencies" commit touches Kotlin, AGP, Compose, Apollo, and Firebase versions at once.
- Build failures reference symbols in generated Apollo code, Compose compiler, or Hilt/KSP with no clear owner.
- New compiler warnings turn into hard errors only after the bump (Apollo's known deprecation-escalation pattern).

**Phase to address:** Phase 2 (dependency upgrade) — sequence sub-steps explicitly in the phase plan; do not fold into Phase 3/4 refactors.

---

### Pitfall 3: Treating the Gradle Declarative DSL migration as all-or-nothing

**What goes wrong:**
The team attempts to convert every `build.gradle.kts` to `.gradle.dcl` in one pass, then gets stuck because: DCL doesn't yet support Version Catalogs (no `libs.foo` shorthand — GAV coordinates only), combining the Android ecosystem plugin with the KMP/JetBrains ecosystem plugin isn't fully supported for all project shapes, and several AGP options used by this project (BuildConfig fields via Secrets Gradle Plugin, custom `buildConfigField` entries for OpenFeedback/Firebase config, Detekt/lint wiring) have no DCL equivalent yet. This stalls the whole modernization on an experimental/EAP technology, which directly contradicts the project's own stated constraint ("accept a partial migration rather than block on 100% conversion").

**Why it happens:**
Declarative Gradle is explicitly experimental (EAP) and under active, breaking development; its own migration guide says migration currently requires more manual work than expected and composability/extensibility support is still incomplete. Teams new to it underestimate this and plan a full-project cutover.

**How to avoid:**
- Migrate module-by-module, starting with the modules with the least imperative build logic and fewest third-party plugins (per Gradle's own guidance) — likely a leaf `core-*` module, not `androidApp` (which has Secrets plugin, BuildConfig fields, Firebase/Crashlytics/Perf plugins, and Detekt config).
- Explicitly budget for `.kts` fallback on modules that use Version Catalog references, custom BuildConfig fields, or plugins without DCL software-type support — document this partial state in the phase's own output rather than treating it as failure.
- Verify Version Catalog usage compatibility early: this project's `gradle/libs.versions.toml` is central to every module; confirm exactly which module conversions would force a GAV-coordinate rewrite before committing to converting that module.
- Use `android docs search` (per this repo's tooling constraint) to check current-day AGP+DCL support status before starting, since this is a fast-moving target and stale blog posts will mislead.

**Warning signs:**
- A module conversion attempt requires rewriting `libs.x.y` references to hardcoded GAV strings just to compile as `.dcl`.
- Secrets/BuildConfig or Detekt/lint configuration can't be expressed and the team starts hand-rolling workarounds instead of falling back to `.kts`.
- The phase drags on far longer than the dependency-upgrade phase because of DCL rather than because of actual project complexity.

**Phase to address:** Phase 2 (Gradle DSL migration, alongside dependency upgrade) — define the "partial migration, `.kts` fallback" boundary *before* starting, module by module, per the project's own constraint.

---

### Pitfall 4: The iOS "three-framework problem" when splitting `shared` into Now-in-Android-style modules

**What goes wrong:**
Today `shared` compiles to a single Kotlin/Native framework consumed by `iosApp`. Once it's split into `core-data`, `core-network`, `core-analytics`, `feature-agenda`, `feature-speakers`, etc. (per the NIA-inspired plan in `PROJECT.md`), a naive approach exports **each** KMP module as its own iOS framework. Any code they share (a common `ApiResult`, a `DevFestNantesStore` contract type, model classes) gets duplicated per framework, and at link time or runtime this causes type-identity conflicts, duplicate symbol issues, and bloated iOS build output. This is a well-documented KMP+iOS pitfall, not a hypothetical: it directly threatens the "no iOS regression" constraint the milestone has committed to.

**Why it happens:**
The NIA reference architecture assumes an Android-only consumer, where Gradle module boundaries map cleanly to compile units. iOS doesn't have that flexibility — Kotlin/Native frameworks are the interop boundary, and multiplying frameworks multiplies duplicated shared code and cross-framework type mismatches.

**How to avoid:**
- Keep a single **umbrella framework** (e.g. `shared.framework`) that depends on all the new `core-*`/`feature-*` Kotlin modules internally, and export *only* that umbrella framework to `iosApp`/Cocoapods — never export feature modules as separate frameworks.
- Design the module graph so Gradle-level modularization (for build parallelism, Koin DI-per-module, ownership boundaries) is decoupled from the iOS export boundary (always the umbrella).
- Validate this decision with a spike before committing to the full module cut: create two `core-*` modules with a shared dependency, wire them into the umbrella framework, and confirm `iosApp` still builds and the shared type resolves as identical on the Swift side.

**Warning signs:**
- `iosApp`'s Podfile/Xcode project references more than one Kotlin-produced `.framework`.
- Swift code needs to cast or bridge between what should be "the same" Kotlin type imported from two different frameworks.
- iOS build times or binary size jump sharply right after modularization, with no corresponding Android regression.

**Phase to address:** Phase 3 (multi-module architecture) — explicitly design and validate the umbrella-framework export strategy as a sub-step before splitting `shared`, not as an afterthought once modules already exist.

---

### Pitfall 5: Stale Hilt-generated code and scope semantics gaps break the Koin migration silently

**What goes wrong:**
Two distinct failure modes recur in real Hilt→Koin migrations: (1) removing Hilt annotations/modules without clearing `build/` directories leaves stale Hilt-generated (Dagger) code around that causes confusing compile errors unrelated to the actual Koin code just written; (2) Hilt's `@ViewModelScoped` and component-scoping semantics have no direct Koin equivalent, so scope lifetimes (e.g. a ViewModel shared between a parent screen and a bottom-sheet child screen) can silently change behavior — a scope gets disposed too early (or too late), causing crashes or state leaks that don't show up until a specific navigation path is exercised. Given this project's explicit "no behavior regression" constraint, a subtly wrong scope lifetime is exactly the kind of bug that slips through manual QA.

**Why it happens:**
Koin's DSL-based, runtime-resolved DI has a different mental model from Hilt's compile-time-checked, annotation-scoped components; a 1:1 mechanical translation of annotations doesn't exist for every Hilt scoping pattern, and the migration is easy to treat as "swap annotations" rather than "redesign scope ownership."

**How to avoid:**
- After removing each Hilt module/annotation set, do a clean build (`./gradlew clean`) before trusting compiler errors — don't debug against a stale build cache.
- Before migrating, enumerate every custom Hilt scope (`@ViewModelScoped`, any custom `@Scope` annotations) in the current `AppModule` and explicitly design its Koin equivalent (a named/qualified scope, or a `viewModel { }` definition with explicit lifecycle) rather than defaulting to Koin's global singleton scope for convenience.
- Migrate and test progressively, module by module, keeping both frameworks registered side-by-side temporarily (proven pattern, used at scale in Now in Android's own Hilt→Koin Annotations migration across ~30 modules) rather than a big-bang cutover.
- Add a regression test (or manual UAT script) specifically for the shared-ViewModel-across-bottom-sheet navigation pattern if this app has one, since it's the exact case known to break.

**Warning signs:**
- Compile errors reference `Hilt_*` or `Dagger*` generated classes after annotations were already removed.
- A ViewModel behaves correctly in isolation but loses/duplicates state when navigated to via a bottom sheet or nested back-stack entry.
- Koin's `get()`/`inject()` throws `ClosedScopeException` or `NoBeanDefFoundException` only on specific navigation paths, not on cold start.

**Phase to address:** Phase 4 (DI migration) — scope inventory and clean-build discipline should be an explicit checklist item in the plan, not left implicit.

---

### Pitfall 6: Koin trades Hilt's compile-time safety for runtime resolution — errors move from build-time to production/QA

**What goes wrong:**
Dagger/Hilt catches missing or ambiguous bindings at compile time. Koin (in its plain DSL form) resolves dependencies at runtime by default, so a missing or misconfigured binding compiles fine and only surfaces as a crash when that code path executes — a serious risk when migrating a full app's DI *at the same time* as splitting it into many new modules (each needing its own Koin module declaration, per the project's "one Koin module per Gradle module" decision). A missing `module { }` registration for a newly-extracted module is easy to miss and easy for it to slip past a spot-check.

**Why it happens:**
Combining "decentralize DI across N new Gradle modules" with "move from compile-time to runtime DI" doubles the number of new wiring points (one Koin module per Gradle module) at exactly the same time the safety net that used to catch wiring mistakes (Hilt's KSP-time checks) is removed.

**How to avoid:**
- If using Koin, turn on Koin's `verify()` / compile-time check tooling (or Koin Annotations with `KOIN_CONFIG_CHECK`) so missing bindings are caught in a test/build step rather than at runtime — set this up as part of the DI migration, not as a later nice-to-have.
- Add a fast smoke test that starts Koin and calls `checkModules()`/`verify()` against every declared module as part of CI, so a forgotten module registration fails the build instead of shipping.
- When extracting each new Gradle module in Phase 3/4, require its Koin module registration and a corresponding entry in the aggregate `startKoin { modules(...) }` list as part of that same PR/commit — never as a follow-up.

**Warning signs:**
- App crashes with `NoBeanDefFoundException`/`NoDefinitionFoundException` in a feature area that "should have been tested."
- New Gradle modules exist with business logic but no corresponding `val xModule = module { ... }`.
- No `checkModules()`/verify step exists anywhere in the test suite or CI.

**Phase to address:** Phase 4 (DI migration) — but the CI verification hook should be added before Phase 5 (test coverage) so it's in place while the biggest wave of new module registrations happens.

---

### Pitfall 7: "No regression" collides with modularization/DI/dependency-bump side effects that are easy to miss

**What goes wrong:**
The milestone's hard constraint is zero behavior change for end users, but several of the planned changes have subtle behavior-affecting side effects that don't show up as compile errors: (a) Apollo major-version bumps can change normalized-cache key generation or resolver defaults — this project has a **custom** `CacheKeyGenerator`/`CacheResolver` (`shared/.../store/graphql/Apollo.kt`) that must be re-verified after any Apollo bump, not just recompiled; (b) moving `FirebaseAnalyticsService` (currently one large class in `androidApp`) into a `core-analytics` module during the modularization can change event parameter ordering/timing subtly if constructors or lifecycle hooks change; (c) known anti-patterns already flagged in `CONCERNS.md` (`println()` for GraphQL error logging instead of Timber, unsafe `!!` navigation-argument assertions, unimplemented `eventFilter()`) are tempting to "clean up while we're in the file" during the module extraction, which quietly expands scope beyond "purely technical, no behavior change."

**Why it happens:**
Refactoring-adjacent bug fixes feel free during a large migration ("I'm already touching this file"), but the milestone explicitly scoped these out unless directly required by the migration itself, and mixing them in makes it impossible to attribute a later regression to "the migration" vs. "the incidental fix."

**How to avoid:**
- Before the Apollo bump lands, snapshot current cache behavior (e.g., a jvmTest that fetches, mutates, and re-fetches through the normalized cache and asserts on cache-hit vs. network-hit) so the custom `CacheKeyGenerator`/`CacheResolver` behavior is verifiable, not just compilable, after the bump.
- When extracting `FirebaseAnalyticsService` into `core-analytics`, keep the extraction as a pure move (same method signatures, same call sites) in one commit, and treat implementing the missing `eventFilter()` or fixing `println()`→Timber as a **separate, explicitly-labeled** commit/decision — not silently bundled.
- Explicitly re-list the known anti-patterns from `CONCERNS.md` at the start of each phase's plan and decide per-item whether it's in scope for *this* phase (e.g., the `!!` navigation asserts are natural to fix during the multi-module navigation-graph rework, since NIA-style modularization typically touches navigation anyway) — make that decision visible, don't let it happen implicitly.

**Warning signs:**
- A PR touching module extraction also changes logging calls, error handling, or adds new analytics parameters not previously present.
- No test exists that would catch a GraphQL cache-key behavior change after the Apollo bump.
- Manual QA notes "seems the same" without a specific before/after checklist for cache hit-rate, analytics event payloads, or navigation argument handling.

**Phase to address:** All phases — but call it out explicitly as a plan-review checklist item for Phase 2 (Apollo bump) and Phase 3 (module extraction touching Analytics/Navigation).

---

### Pitfall 8: Retrofitting test coverage into legacy code produces flaky or low-value tests if setup isn't fixed first

**What goes wrong:**
The project currently has near-zero unit test coverage (per `CONCERNS.md`: empty `androidApp/src/test/`, no ViewModel tests, no service tests) and one shallow instrumented test. A push for "significant coverage" without first correcting known test-fragility sources produces tests that are flaky, non-deterministic, or test trivial things: (a) `StoreStubs.kt` uses `Random.nextInt`/`Random.nextLong` for stub data generation with no seed, making any test built on top of it non-deterministic; (b) `SimpleDateFormat` (not thread-safe, used with the Java-specific `"XXX"` timezone pattern) is used both in production code and existing tests — new concurrent tests exercising date parsing can intermittently fail for reasons unrelated to the code under test; (c) once Compose UI tests are added, they must stay on JUnit 4 (the project already is JUnit 4, but if Compose Multiplatform-style shared UI testing is ever considered for the `core-ui`/`feature-*` split, note it's JUnit 4-only and its assertion API is still experimental/incomplete — this app's UI is Compose-on-Android + SwiftUI-on-iOS, not shared Compose UI, so this specifically only bites if the team later decides to share more UI code).

**Why it happens:**
"Increase coverage" is treated as a volume goal, so tests get bolted onto existing fragile fixtures (stubs, date parsing) instead of first hardening the fixtures. Chasing coverage percentage over quality also pulls effort toward easy-to-test trivial code instead of the ViewModels/Stores/mappers this milestone's own `PROJECT.md` says should be the priority.

**How to avoid:**
- Before writing significant new tests, seed `StoreStubs.kt`'s random generation (accept a `Random(seed)` instance) so stub-based tests are deterministic — this is a small, low-risk fix that unblocks everything downstream.
- Replace `SimpleDateFormat` usage in the code paths being tested with `kotlinx-datetime` (already a dependency per `CONCERNS.md`) or `java.time`, at minimum in the modules getting new test coverage, to avoid thread-safety-induced flakiness in concurrent test runs.
- Prioritize per the project's own stated intent: ViewModels, Store/repository, and GraphQL→model mappers first; defer trivial getter/DTO tests.
- For the GraphQL store specifically, add tests for error paths (network failure, malformed response) and cache behavior — not just the existing happy-path-only tests — since `CONCERNS.md` flags this as a real gap and it directly overlaps with Pitfall 7's Apollo-bump verification need.
- Set up mock/fake Apollo clients (error injection) rather than only hitting the live `confetti-app.dev` endpoint in tests, both for determinism and CI speed.

**Warning signs:**
- New tests pass locally but flake in CI (timing/random-seed-related).
- Coverage percentage rises while critical files (ViewModels, Stores) remain untested and trivial data classes get 100% coverage instead.
- Test run time balloons because tests hit the live GraphQL endpoint instead of a mock.

**Phase to address:** Phase 5 (test coverage) — but the `StoreStubs.kt` seeding fix and date-utility hardening should be pulled forward and done as prerequisites, ideally noted as a small early step within Phase 5 rather than discovered mid-phase.

---

## Technical Debt Patterns

| Shortcut | Immediate Benefit | Long-term Cost | When Acceptable |
|----------|-------------------|-----------------|------------------|
| Bumping Kotlin/AGP/Compose/Apollo/Firebase in one commit | Fewer PRs, feels faster | Unbisectable breakage, compounded migration guides to reconcile at once | Never — always stage per Pitfall 2 |
| Converting a module to `.gradle.dcl` by hand-copying GAV versions instead of resolving the Version Catalog gap | Unblocks that one module quickly | Version drift between `.dcl` GAV strings and `libs.versions.toml`, silent divergence | Only as a documented, tracked exception, never silently |
| Leaving both Hilt and Koin registered "for now" past the migration phase | Safety net during transition | Two DI graphs to reason about, doubled boilerplate, confusion about source of truth | Acceptable *during* Phase 4 only, must be fully removed before phase closes |
| Exporting each new Kotlin module as its own iOS framework "since Gradle modules exist now" | Matches Gradle module boundaries 1:1, conceptually simple | Three-framework problem: duplicated types, link errors, growing iOS binary size | Never — always use the umbrella framework |
| Writing Koin modules without `verify()`/`checkModules()` | Faster to wire up initially | Wiring bugs surface as runtime crashes instead of build failures | Only in a throwaway spike, never in the shipped migration |

## Integration Gotchas

| Integration | Common Mistake | Correct Approach |
|-------------|-----------------|-------------------|
| Apollo GraphQL (confetti-app.dev) | Assume a major-version bump is "just a recompile" and skip re-verifying the custom `CacheKeyGenerator`/`CacheResolver` behavior | Add a cache-behavior test before bumping; read the Apollo migration guide for the deprecated→error promotion pattern first |
| Firebase (Analytics/Crashlytics/Performance/Remote Config) | Bump the Firebase BoM alongside Kotlin/AGP without checking Firebase's own Kotlin/AGP compatibility notes | Bump Firebase BoM as its own staged step; watch for Crashlytics/Perf Gradle plugin version coupling to AGP |
| GitHub Actions macOS runner (iOS CI) | Pin the Xcode app path but leave `runs-on: macos-latest` floating | Pin both the runner image and an explicit simulator OS/device, verify against `xcrun simctl list devices available` output at build time |
| Koin (module registration) | Register a new Gradle module's dependencies without adding it to the aggregate `startKoin { modules(...) }` list | Treat Koin module registration as a required part of every module-extraction PR; verify with `checkModules()` in CI |
| Secrets Gradle Plugin / OpenFeedback config | Migrating `build.gradle.kts` to `.gradle.dcl` without checking DCL support for `buildConfigField`/Secrets plugin, risking secrets handling regressing to hardcoded values | Keep any module using the Secrets Gradle Plugin or custom `buildConfigField` on `.kts` until DCL explicitly supports it; don't route around by hardcoding secrets in `.dcl` |

## Performance Traps

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|-----------------|
| CI cache keys (Gradle/Konan/DerivedData) not updated for new module boundaries after modularization | CI cache-hit rate drops sharply post-split, build times regress instead of improving | Re-derive `actions/cache` `key`/`hashFiles` patterns to include new module paths (`core-*/**`, `feature-*/**`) as part of Phase 3, not left pointing only at old `shared/**` | Immediately after the module split lands, if cache keys weren't updated |
| Koin runtime graph resolution overhead scales with module count if modules aren't lazily loaded | App cold-start time creeps up after DI decentralization across many small modules | Prefer `single { }`/lazy definitions over eager instantiation; measure cold-start before/after Phase 4 | Once module count is high enough that eager Koin graph construction becomes measurable (worth a startup trace, not a guess) |
| Random/non-seeded stub data (`StoreStubs.kt`) used inside performance-sensitive test loops | Non-deterministic test timing, flaky perf assertions if any are added later | Seed random generation (Pitfall 8) before building any perf-sensitive tests on top of stubs | As soon as tests assert on stub-derived data volume or timing |

## Security Mistakes

| Mistake | Risk | Prevention |
|---------|------|------------|
| Migrating `androidApp/build.gradle.kts` to Declarative DSL and hardcoding the currently-placeholder "SECRET" OpenFeedback values because DCL doesn't support the Secrets plugin cleanly | Real secrets could end up committed in a `.dcl` file if this migration is rushed | Keep this module on `.kts` (already flagged as a DCL limitation in Pitfall 3); resolve the existing "SECRET" placeholder issue from `CONCERNS.md` independently, not as a side effect of the DSL migration |
| Bumping the Secrets Gradle Plugin version as part of the mass dependency upgrade without re-testing `local.properties`/`secrets.properties` resolution | CI or local builds could silently fall back to placeholder secrets without failing loudly | Add an explicit build-time check/test that fails if OpenFeedback secrets are the literal string "SECRET" in a release build variant |
| Koin's runtime resolution silently succeeding with a wrong/mock binding in a release build (e.g. leftover test module accidentally included) | Wrong dependency (e.g. a test double for Firebase/Analytics) ships to production, silently disabling real analytics/crash reporting | Use separate, explicitly named Koin module sets for `debug`/`test`/`release` build types and verify via `checkModules()` per variant, not a single shared list |

## UX Pitfalls

This is a purely technical modernization with an explicit "no UX change" constraint, so the relevant UX risk is *regression*, not new UX design.

| Pitfall | User Impact | Better Approach |
|---------|-------------|-------------------|
| Navigation argument handling (`!!` on `backStackEntry.arguments`, flagged in `CONCERNS.md`) gets touched during the Navigation-graph rework needed for modularization, and a fix changes crash-vs-fallback behavior | Users could see a new empty/fallback screen instead of a crash (or vice versa) for edge cases like deep links with missing args | Decide explicitly (per Pitfall 7) whether fixing this is in scope for the modularization phase; if fixed, add a test for the missing-argument case so the new behavior is intentional and verified, not incidental |
| Firebase Remote Config flags (`openfeedback_enabled`, `openfeedback_fallback_requested_android`) behave differently if `FeedbackFormViewModel`'s Remote Config fetch/activate timing changes during the analytics module extraction or DI migration | OpenFeedback form could appear/disappear inconsistently for users depending on fetch timing changes | Preserve the exact fetch/activate call sequence and its lifecycle scope when the ViewModel or its Firebase Remote Config dependency moves modules or DI containers; add a targeted test for this ViewModel |

## "Looks Done But Isn't" Checklist

- [ ] **iOS CI fix:** Looks fixed after pinning the destination string once — verify it survives a `macos-latest` image bump by also pinning the runner image tag and confirming via `xcrun simctl list devices available`, not just re-running the workflow once.
- [ ] **Dependency upgrade:** Looks done once the app compiles — verify Apollo's normalized-cache behavior (custom `CacheKeyGenerator`/`CacheResolver`) still matches pre-upgrade behavior with an explicit test, not just "no compile errors."
- [ ] **Gradle Declarative DSL migration:** Looks complete when `.gradle.dcl` files exist for some modules — verify Secrets/BuildConfig/Detekt/lint config, which may not have a DCL equivalent, is intentionally left on `.kts` and documented, not silently dropped.
- [ ] **Multi-module split (iOS):** Looks done once Android builds and modules compile — verify `iosApp` still links against a single umbrella framework, not multiple per-module frameworks, before calling the split complete.
- [ ] **Hilt→Koin migration:** Looks complete once `@Inject`/`@HiltViewModel` annotations are gone and the app runs — verify with `checkModules()`/`verify()` that every extracted module's dependencies are actually registered, and specifically test any parent/child (bottom-sheet) ViewModel-sharing navigation flow.
- [ ] **Test coverage increase:** Looks done once a coverage number goes up — verify the increase covers ViewModels/Stores/mappers (the project's own stated priority) and not just trivial data classes made easy to test by `StoreStubs.kt`'s existing structure.

## Recovery Strategies

| Pitfall | Recovery Cost | Recovery Steps |
|---------|-----------------|------------------|
| CI breaks again after a runner image bump (Pitfall 1) | LOW | Re-run `xcrun simctl list devices available` in a fresh CI run, adjust destination or pin runner image tag; no app code involved |
| Compounded dependency-upgrade breakage with unclear root cause (Pitfall 2) | MEDIUM–HIGH | Revert to last-known-good version catalog state; re-apply bumps one at a time with a green build after each, using `git bisect`-style isolation |
| DCL migration stalls a module indefinitely (Pitfall 3) | LOW | Revert that module's `.dcl` file back to `.kts`; document the specific blocking DCL limitation for a future retry once Declarative Gradle matures |
| iOS three-framework linking issues discovered late (Pitfall 4) | HIGH | Consolidate exported frameworks into a single umbrella framework retroactively; requires reworking the Xcode project's framework references and Podfile, non-trivial but well-documented |
| Koin scope/lifecycle bug reaches QA or production (Pitfall 5/6) | MEDIUM | Patch the specific scope definition; add the missing `checkModules()` gate to CI so the class of bug can't recur silently |
| Flaky tests from non-seeded stubs or `SimpleDateFormat` (Pitfall 8) | LOW | Seed the random generator and swap the date API in the specific fragile test/production path; quarantine flaky tests until fixed rather than ignoring failures |

## Pitfall-to-Phase Mapping

| Pitfall | Prevention Phase | Verification |
|---------|-------------------|----------------|
| Runner image drift undoes the CI fix | Phase 1 (CI iOS fix) | Workflow pins an explicit runner image + explicit OS in destination; re-check after Phase 2 toolchain bumps |
| Compounded dependency upgrade breakage | Phase 2 (dependency upgrade) | Each dependency group bumped and green-built as a separate commit/step in the plan |
| DCL migration stalls on unsupported features | Phase 2 (Gradle DSL migration) | Explicit per-module `.dcl` vs `.kts` decision documented; no module blocked indefinitely |
| iOS three-framework problem | Phase 3 (multi-module architecture) | `iosApp` links a single umbrella framework; spike validated before full module cut |
| Stale Hilt code / scope semantics gaps | Phase 4 (DI migration) | Clean build after annotation removal; explicit scope-mapping table from Hilt scopes to Koin scopes |
| Koin runtime-only wiring failures | Phase 4 (DI migration), gate added before Phase 5 | `checkModules()`/`verify()` step running in CI for every Koin module set |
| "No regression" broken by incidental cleanup during refactors | All phases, checked explicitly in Phase 2 & 3 | Cache-behavior test for Apollo; pure-move commits separated from behavior-changing commits |
| Flaky/low-value tests from fragile fixtures | Phase 5 (test coverage), prerequisite pulled forward | `StoreStubs.kt` seeded; `SimpleDateFormat` replaced in tested paths; coverage concentrated on ViewModels/Stores/mappers |

## Sources

- [actions/runner-images Issue #13435 — iOS 26 Simulator Devices Missing on macos-26-arm64 Runner](https://github.com/actions/runner-images/issues/13435)
- [actions/runner-images Issue #12771 — Unable to find a destination matching the provided destination specifier](https://github.com/actions/runner-images/issues/12771)
- [actions/runner-images Issue #12758 — iOS build fails on CI due to incorrect SDK targeting](https://github.com/actions/runner-images/issues/12758)
- [fastlane Issue #29659 — xcodebuild fails with "Unable to find a destination"](https://github.com/fastlane/fastlane/issues/29659)
- [Apple Developer Forums — Unable to find a device matching the provided destination specifier](https://developer.apple.com/forums/thread/810605)
- [Kotlin Multiplatform Compatibility Guide (kotlinlang.org)](https://kotlinlang.org/docs/multiplatform/multiplatform-compatibility-guide.html)
- [Updating multiplatform projects with Android apps to use AGP 9 (kotlinlang.org)](https://kotlinlang.org/docs/multiplatform/multiplatform-project-agp-9-migration.html)
- [Apollo Kotlin Migration Guides](https://www.apollographql.com/docs/kotlin/v2/essentials/migration)
- [Apollo Kotlin Releases/CHANGELOG](https://github.com/apollographql/apollo-kotlin/blob/main/CHANGELOG.md)
- [Declarative Gradle EAP3 — April 2025 Update](https://blog.gradle.org/declarative-gradle-april-2025-update)
- [Declarative Gradle Migration Guide](https://declarative.gradle.org/docs/reference/migration-guide/)
- [Declarative Gradle Example Project Migration Case Study](https://declarative.gradle.org/docs/reference/migration-case-study/)
- [Kotlin/declarative-gradle-jetbrains-ecosystem-plugin (GitHub)](https://github.com/Kotlin/declarative-gradle-jetbrains-ecosystem-plugin)
- [Three-framework problem with Kotlin Multiplatform Mobile (Medium/xorum-io)](https://medium.com/xorum-io/three-framework-problem-with-kotlin-multiplatform-mobile-16267c5afa53)
- [Modularization in a Kotlin Multiplatform Project (Medium)](https://ajailani.medium.com/modularization-in-a-kotlin-multiplatform-project-81e06d2170b6)
- [Why Migrate from Dagger2/Hilt to Koin? (Kotzilla blog)](https://blog.kotzilla.io/why-migrate-from-dagger2/hilt-to-koin)
- [How To Migrate from Hilt to Koin — A Detailed Guide (Kotzilla blog)](https://blog.kotzilla.io/migrate-from-hilt-to-koin)
- [Migrating from Hilt to Koin (insert-koin.io official docs)](https://insert-koin.io/docs/reference/koin-android/hilt-migration/)
- [Koin vs Hilt/Dagger (insert-koin.io)](https://insert-koin.io/docs/intro/koin-vs-hilt/)
- [Kotlin Slack — Dagger vs Hilt vs Koin vs Metro discussion](https://slack-chats.kotlinlang.org/t/33094191/dagger-vs-hilt-vs-koin-vs-metro-vs-lt-what-comes-next-gt-ser)
- [Kotlin Slack — Hilt to Koin migration thread](https://slack-chats.kotlinlang.org/t/15698377/android-wave-i-am-migrating-a-project-from-dagger-hilt-to-ko)
- [Testing Compose Multiplatform UI (kotlinlang.org official docs)](https://kotlinlang.org/docs/multiplatform/compose-test.html)
- [Unit Testing Compose Multiplatform (Medium)](https://medium.com/@callmeryan/unit-testing-compose-multiplatform-cf902ee42c5c)
- [Kotlin Multiplatform Testing in 2025: Complete Guide (kmpship.app)](https://www.kmpship.app/blog/kotlin-multiplatform-testing-guide-2025)
- Project-internal: `.planning/codebase/CONCERNS.md`, `.planning/codebase/INTEGRATIONS.md`, `.planning/codebase/TESTING.md`, `.github/workflows/ios.yml` (analyzed directly, 2026-09-12)

---
*Pitfalls research for: KMP app modernization (CI, dependency upgrade, Gradle Declarative DSL, multi-module split, Hilt→Koin, test coverage)*
*Researched: 2026-09-12*
