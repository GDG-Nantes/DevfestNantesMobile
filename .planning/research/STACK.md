# Stack Research

**Domain:** KMP technical modernization (build tooling, DI, core libraries) for an existing Android/Compose + iOS/SwiftUI conference app
**Researched:** 2026-09-12
**Confidence:** MEDIUM-HIGH (version numbers HIGH — cross-checked against official release notes/blogs; the AGP 9 / Declarative DSL guidance is MEDIUM because it depends on fast-moving, recently-shipped tooling with limited real-world migration reports)

## Recommended Stack

### Core Technologies

| Technology | Version | Purpose | Why Recommended |
|------------|---------|---------|-----------------|
| Kotlin | **2.4.0** (stable, June 2026) | Language/compiler for `shared`, Android, iOS (Kotlin/Native) | Current stable line; required baseline for AGP 9's built-in Kotlin support (needs KGP ≥2.2.10, ideally ≥2.3.1) and for `kotlinx-datetime` 0.8.0/`kotlinx-serialization` 1.11.0. Project is already on 2.2.0 — a two-minor-version jump, well within Kotlin's compatibility guarantees. |
| Gradle | **9.7.1** (stable, Aug 2026) | Build system | Mandatory floor for AGP 9.x (`Gradle ≥9.1.0`). If you stay on AGP 8.13.0 instead (see below), Gradle 8.13–8.14 remains valid and is the lower-risk pairing. |
| Android Gradle Plugin (AGP) | **9.2.0** (stable, April 2026) — *conditional, see decision below* | Android build plugin | Latest stable; brings built-in Kotlin compilation and the new `com.android.kotlin.multiplatform.library` plugin, which is the long-term-supported way to declare a KMP module's Android target. **However** AGP ≥9.0 makes `org.jetbrains.kotlin.multiplatform` incompatible with `com.android.library`/`com.android.application` in the *same* Gradle module — see "AGP 9 decision" below before committing. |
| Jetpack Compose BOM | **2026.08.00** (stable, Aug 2026) | Android UI framework version alignment | Latest BOM (Compose 1.12 core modules); bumps `compileSdk` to API 37, so pair with AGP ≥9.0/recent 8.x and SDK 37 platform tools. Project's iOs UI is native SwiftUI, so no Compose Multiplatform UI toolkit concerns apply here — only the Android-side Compose BOM matters. |
| Apollo Kotlin (GraphQL client) | **5.0.1** (stable) | GraphQL client/codegen, shared module | Current major line; "incremental evolution" of v4 — most APIs source-compatible. Real breaking changes are scoped: (1) SQLite normalized-cache storage format changed from JSON-in-text to binary blob (cache is disposable — treat as a cold-cache reset, not a data-loss risk), (2) `ApolloStore` renamed `CacheManager`, (3) old `network.ws` WebSocket API deprecated in favor of a new stable `network.websocket` package, (4) requires KGP ≥2.2. Not used today (project has no subscriptions), so the WebSocket rewrite is a non-issue. |
| Koin (KMP DI) | **koin-bom 4.1.1** (stable) | Dependency injection, replacing Hilt | Native Kotlin Multiplatform support (Android + iOS + JVM) with zero annotation-processor requirement on Kotlin/Native targets — the exact gap Hilt cannot fill in `shared`. 4.1 brought first-class Compose 1.8+/MPP support (automatic context handling in `koinViewModel()`/`koinInject()`, fewer recomposition lookups) and a unified module/lazy-module DSL with graph verification (`verify()`). **Do not adopt 4.2.0-RC1** yet — it is still a release candidate built around an experimental Koin Compiler Plugin; wait for GA. |
| Firebase Android SDK | **BOM 34.18.0** (stable) | Analytics/Crashlytics/Performance, Android only | Firebase has no first-party Kotlin/Native (iOS) SDK — this BOM only applies to `androidApp`. Keep it there; do not attempt to pull Firebase into `commonMain` (see "What NOT to Use"). |
| Kotlin Coroutines | **1.11.0** (stable) | Async/structured concurrency, shared module | Straightforward upgrade from 1.10.2; officially recommended pairing is Kotlin ≥2.2.20 (2.4.0 satisfies this with margin). |
| kotlinx-serialization | **1.11.0** (stable) | JSON (de)serialization for Apollo custom scalars / local persistence | One minor bump from the project's 1.9.0. 1.12.0-RC exists (adds `@KeepGeneratedSerializer`/`ContextualSerializer` stabilization, ASCII-fast-path JSON parsing) but is not GA — stay on 1.11.0 until it ships stable. |
| kotlinx-datetime | **0.8.0** (stable) | Multiplatform date/time (session schedule, agenda times) | Two major bumps ahead of the project's current 0.6.2 (0.6.2 → 0.7.0 → 0.8.0). **0.7.0 introduced breaking `Instant`/`Clock` API changes** — budget explicit migration effort here, don't treat it as a drive-by version bump. A `0.7.1`-with-`0.6.x`-compat artifact exists if you need a softer landing. |

### Supporting Libraries

| Library | Version | Purpose | When to Use |
|---------|---------|---------|-------------|
| `koin-android` | via koin-bom 4.1.1 | Android-specific Koin integrations (`WorkManager`, `startKoin` in `Application`) | `androidMain` source set only |
| `koin-compose` + `koin-compose-viewmodel` | via koin-bom 4.1.1 | `koinInject()` / `koinViewModel()` in Compose screens | `commonMain`, wherever Compose UI consumes injected dependencies |
| `koin-test` | via koin-bom 4.1.1 | `verify()`-based compile-adjacent DI graph checks in unit tests | Add one `verify()` test per module's Koin module as a cheap safety net (see DI pattern below) |
| `koin-annotations` (KSP) | 2.3.1 (needs KSP 2.3.2+) | Compile-time DI graph generation via `@Single`/`@Factory`/`@ComponentScan` | **Defer to a later phase** — see DI section. Don't add KSP-based annotation processing in the same migration wave as AGP 9 + Kotlin 2.4 + module restructuring; that's three sources of build-graph churn at once. |
| `kotlin-symbol-processing` (KSP2) | pinned to Kotlin version, e.g. `2.4.0-1.0.x` | Required by `koin-annotations` and any other KSP processors | KSP1 (K1-based) is deprecated from Kotlin 2.2.0 and does **not** support Kotlin 2.3+/AGP 9+ — you must already be on KSP2 (default since KSP 2.0.0) for this modernization to work at all. |
| Detekt | keep pinned to a release compatible with Kotlin 2.4.0's K2-only compiler | Static analysis / lint | Verify Detekt's Kotlin-2.4-compatible release before bumping Kotlin; Detekt's K2 support has historically lagged the Kotlin release by a few weeks. |

### Development Tools

| Tool | Purpose | Notes |
|------|---------|-------|
| Android Studio Otter 3 Feature Drop (2025.2.3) or newer | IDE support for AGP 9.0+ | Mandatory if you take the AGP 9 path — earlier Android Studio releases don't understand the new DSL types. |
| IntelliJ IDEA | KMP-only editing (iOS/JVM targets) | As of IntelliJ 2026.1, **AGP 9.0 projects are not supported** in IntelliJ — you can still open/edit non-Android KMP source sets, but Android-module Gradle sync requires Android Studio. Relevant if any contributor uses plain IntelliJ. |
| `android docs search` (project's mandated CLI) | Verify exact current Gradle/Declarative-DSL/AGP-9 syntax before writing build files | Required by this project's constraints — the AGP 9 DSL and `.gradle.dcl` syntax are moving targets; don't trust memorized syntax. |

## AGP 9 Decision (read before touching build files)

This is the single highest-leverage/highest-risk decision in the whole modernization and it interacts directly with the planned multi-module split. Two paths, pick one explicitly and record it as a Key Decision:

**Path A — Adopt AGP 9.2.0 now (recommended given project context).**
Since the roadmap already plans a full multi-module rewrite (`core-*`/`feature-*`) *after* the dependency-upgrade phase, doing that rewrite directly against AGP 9's new `com.android.kotlin.multiplatform.library` plugin avoids building the new module graph on a plugin combination (`com.android.library` + `org.jetbrains.kotlin.multiplatform` in one module) that AGP 9 already forbids and that AGP 10 will forbid unconditionally. Concretely:
- `androidApp` keeps `com.android.application` (already an isolated subproject — unaffected by the AGP 9 restriction, which only bites when KMP and an Android app/library plugin share *one* module).
- Every new `core-*`/`feature-*` KMP module (and the migrated `shared`) applies `com.android.kotlin.multiplatform.library` instead of `com.android.library` + `org.jetbrains.kotlin.multiplatform`.
- Requires: Gradle ≥9.1.0, KGP ≥2.3.1 (2.3.3+ recommended — KSP moved off deprecated `compilerOptions` API), JDK 17+ for the whole build (bump `shared`'s current JVM 11 target to 17), Android Studio Otter 3 Feature Drop+.
- Risk: this is a young combination (AGP 9.0 shipped January 2026); tooling/IDE support is still catching up (see IntelliJ note above), and community migration reports are thin. Mitigate by doing this as its own isolated, well-tested step before the multi-module split, not bundled with it.

**Path B — Stay on AGP 8.13.0 (current) for this milestone, revisit AGP 9 later.**
Lower risk, defers the breaking KMP/Android-plugin-combination change. Valid if the team wants to isolate risk to one axis at a time (this milestone already stacks Kotlin+Compose+Apollo+Koin+multi-module changes). Downside: the multi-module split gets built on a plugin combination AGP will remove support for in AGP 10 (2026), meaning a second, very similar migration later.

**Recommendation:** Path A, timed as the *first* concrete build-system change (before the multi-module split), specifically because the project is already committed to rebuilding its module graph from scratch — doing it once against the AGP-9 shape is cheaper than doing it twice.

## Gradle Declarative DSL (`.gradle.dcl`) — status verdict

**Do not attempt a full migration this milestone.** As of the most recent Gradle communications (KotlinConf 2026, Munich), Declarative Gradle/Software Types remain explicitly **experimental**: Gradle's own team framed it as "early but points at where things are headed," and the current EAP-level Kotlin/Android ecosystem plugins support compiling and testing Java/Kotlin-JVM/Android but not publishing or third-party-plugin scenarios. There is no confirmed GA date, and no confirmation of full combined AGP+KMP production support.

Two distinct things are easy to conflate — keep them separate when implementing:
1. **AGP 9's "new DSL"** (`android.newDsl=true`, new hidden DSL interface types) — this is still **Kotlin DSL (`.kts`)**, just against new interfaces. This ships with AGP 9 and is what the project will actually use.
2. **Gradle Declarative DSL / `.gradle.dcl`** (Software Types, the separate Gradle+JetBrains+Google initiative) — this is the genuinely experimental, different-file-extension technology referenced in the project's constraint about "migration partielle documentée." Do not confuse AGP 9's DSL changes with adopting `.gradle.dcl` files.

Recommended scope for this milestone, matching the project's own constraint ("accepter une migration partielle documentée plutôt que de bloquer"):
- Keep all module build files in Kotlin DSL (`.kts`).
- Optionally, as a documented, low-risk experiment (not a requirement), pilot `.gradle.dcl` on exactly one leaf, non-Android, non-KMP module (e.g., a future `convention`/build-logic module or a pure-JVM `core-testing` utility module) to build institutional familiarity, using Gradle's documented "mixed builds" support (`.dcl` files can sit alongside `.kts` in the same project).
- Re-evaluate at the next milestone once Gradle ships confirmed Software-Types support incubating for Android+KMP combined — track via `blog.gradle.org` and the Declarative Gradle site, not assumptions.

## Koin for KMP — recommended DI setup pattern

**Use manual Koin DSL modules (`module { }`) — one per Gradle module — plus `koin-test`'s `verify()`, not Koin Annotations, for this migration.**

Rationale: the project's stated goal is literally "un module Koin déclaré par module Gradle (DI décentralisée)" — plain `module { }` blocks map 1:1 onto that requirement with the least new moving parts. Koin Annotations (KSP-based `@Single`/`@Factory`/`@ComponentScan`) does offer real compile-time graph validation and less boilerplate at scale, and is the community's stated direction for large projects — but it adds a second KSP-based code-generation axis (on top of KSP2's own Kotlin-2.3+/AGP-9 requirement) and has documented multi-module rough edges relevant here: `@ComponentScan` auto-discovery only works *within* the same Gradle module as `@KoinApplication`, overlapping scan packages across modules silently produce empty modules, and K2 incremental compilation has known edge cases around `@ComponentScan`-newly-added classes going stale. Introducing that in the same milestone as AGP 9 + Kotlin 2.4 + the module split is too much simultaneous build-graph risk.

Concrete pattern:
- **Per Gradle module:** each `core-*`/`feature-*` module exposes one `val xModule = module { ... }` in its `commonMain` (or a dedicated `di` package), scoped to what that module owns (repository/store implementations, mappers, use-cases).
- **Platform entry points:** `androidApp` calls `startKoin { modules(...) }` inside `Application.onCreate()`; `iosApp` calls the equivalent Koin bootstrap from Swift/the shared `KoinKt` initializer exposed via the Kotlin/Native framework — both simply aggregate the list of per-module `module { }` objects, they don't know about individual feature internals.
- **Platform-specific bindings:** use an `expect fun platformModule(): Module` (or `androidMain`/`iosMain` actuals) for things like Android `Context`-dependent bindings (e.g. `SharedPreferences`) vs iOS equivalents — this replaces Hilt's `@InstallIn(SingletonComponent::class)` + Android-only bindings.
- **Compose injection:** use `koinViewModel()` / `koinInject()` directly in composables (Koin 4.1's Compose 1.8+ support), not manual `get()` calls threaded through constructors.
- **Safety net:** add one `verify()`-based unit test per module's Koin module (via `koin-test`) as a cheap CI-time graph-validity check, given you're intentionally not using Koin Annotations' stronger compile-time guarantees this round.
- **Revisit later:** once the module split has stabilized on plain DSL modules, evaluate Koin Annotations as a *follow-up, additive* modernization — not bundled with this milestone — module by module, starting with leaf modules that have no cross-module `@ComponentScan` ambiguity.

## Installation

```kotlin
// gradle/libs.versions.toml (illustrative — verify exact patch versions via `android docs search` / Maven Central at implementation time)
[versions]
kotlin = "2.4.0"
agp = "9.2.0"              # or "8.13.0" if Path B is chosen
composeBom = "2026.08.00"
apollo = "5.0.1"
koinBom = "4.1.1"
firebaseBom = "34.18.0"
coroutines = "1.11.0"
serialization = "1.11.0"
datetime = "0.8.0"
ksp = "2.4.0-1.0.x"        # confirm exact tag matching kotlin 2.4.0 on google/ksp releases

[libraries]
koin-bom = { module = "io.insert-koin:koin-bom", version.ref = "koinBom" }
koin-core = { module = "io.insert-koin:koin-core" }
koin-android = { module = "io.insert-koin:koin-android" }
koin-compose = { module = "io.insert-koin:koin-compose" }
koin-compose-viewmodel = { module = "io.insert-koin:koin-compose-viewmodel" }
koin-test = { module = "io.insert-koin:koin-test" }
```

```kotlin
// per KMP module build.gradle.kts (Path A — AGP 9 shape)
plugins {
    kotlin("multiplatform")
    id("com.android.kotlin.multiplatform.library") // replaces com.android.library for KMP modules under AGP 9+
}

kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(platform(libs.koin.bom))
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }
        androidMain.dependencies {
            implementation(libs.koin.android)
        }
        commonTest.dependencies {
            implementation(libs.koin.test)
        }
    }
}
```

## Alternatives Considered

| Recommended | Alternative | When to Use Alternative |
|-------------|-------------|--------------------------|
| AGP 9.2.0 + new `com.android.kotlin.multiplatform.library` plugin | AGP 8.13.0 (stay put) | If the team wants to isolate the AGP-9 KMP-plugin breaking change to its own dedicated future milestone rather than absorbing it alongside Kotlin/Compose/Apollo/Koin bumps in this one. |
| Manual Koin `module { }` DSL, one per Gradle module | Koin Annotations (`koin-annotations` 2.3.1 + KSP) | Once the module graph has stabilized post-migration and the team wants stronger compile-time DI-graph guarantees; adopt module-by-module, not project-wide, given the documented `@ComponentScan` cross-module caveats. |
| Apollo Kotlin 5.0.1 | Apollo Kotlin 4.x latest patch | If you want to decouple "upgrade GraphQL client" from "adopt the new normalized-cache storage format" — v4's newest patch still gets the version bump without the cache-schema change, at the cost of not being on the current major line. |
| kotlinx-datetime 0.8.0 | kotlinx-datetime 0.7.1 (`0.6.x`-compat build) | If the `Instant`/`Clock` API breaking changes introduced in 0.7.0 need to be absorbed in a separate, smaller step before jumping all the way to 0.8.0. |
| Keep `.gradle.dcl` out of scope (Kotlin DSL everywhere) | Pilot `.gradle.dcl` on one leaf module | Only as a low-risk, explicitly-scoped experiment for team familiarity — not as a milestone deliverable, given DCL's confirmed-experimental status. |

## What NOT to Use

| Avoid | Why | Use Instead |
|-------|-----|--------------|
| Full/mandatory `.gradle.dcl` migration this milestone | Gradle's own 2026 messaging (KotlinConf Munich) is explicit that Declarative Gradle/Software Types are still experimental with no confirmed combined Android+KMP GA; forcing it risks build breakage with no upstream support path | Kotlin DSL (`.kts`) project-wide; document the decision and defer, per the project's own stated constraint |
| `com.android.library` + `org.jetbrains.kotlin.multiplatform` applied together in the same module, if adopting AGP ≥9.0 | AGP 9 explicitly stops supporting this combination; it will hard-fail or require the deprecated-API opt-out flags that AGP 10 removes entirely | `com.android.kotlin.multiplatform.library` for KMP modules with an Android target |
| Koin 4.2.0-RC1 / the experimental Koin Compiler Plugin | Not GA — building the whole DI migration on a release candidate compounds risk in a migration that already touches build tooling, module graph, and DI simultaneously | Koin BOM 4.1.1 (stable) |
| KSP1 (legacy K1-based Kotlin Symbol Processing) | Deprecated from Kotlin 2.2.0; unsupported on Kotlin 2.3+/AGP 9+ — will simply not work with the recommended stack | KSP2 (default since KSP 2.0.0), pinned to a Kotlin-2.4.0-matching release |
| Pulling Firebase into `commonMain`/`shared` via a KMP wrapper (e.g. GitLive `firebase-kotlin-sdk`) as part of *this* modernization | Out of scope for a "no behavior change" technical migration — it's a legitimate pattern in general, but adopting a third-party Firebase KMP wrapper here adds a new dependency surface, known iOS CocoaPods linking friction, and platform feature-parity gaps, none of which this milestone needs to solve | Keep Firebase Android-native (BOM 34.18.0) in `androidApp` only; if the existing anti-pattern of direct Firebase references inside Android ViewModels needs fixing, do it via a plain Kotlin interface + Android-only implementation injected through Koin's `androidMain` module — not a KMP Firebase SDK |
| Bundling the AGP-9 KMP-plugin migration *and* the multi-module split *and* Koin Annotations adoption in one wave | Three independent sources of build-graph churn (new AGP plugin type, new module topology, new KSP annotation processor) landing together makes root-causing any single build failure much harder | Sequence: (1) dependency/AGP upgrade in isolation → (2) multi-module split built directly on the new AGP-9 KMP plugin shape → (3) Koin Annotations as an optional later increment |

## Stack Patterns by Variant

**If the team chooses Path A (AGP 9.2.0):**
- Bump `shared`'s (and every future `core-*`/`feature-*` module's) JVM target to 17 to match the app-level requirement — AGP 9 needs JDK 17+ for the whole build.
- Require Android Studio Otter 3 Feature Drop (2025.2.3)+ for all contributors touching Android/KMP build files.
- Build every new `core-*`/`feature-*` KMP module on `com.android.kotlin.multiplatform.library` from day one of the multi-module split — don't create them with the old `com.android.library` combination and migrate later.

**If the team chooses Path B (stay on AGP 8.13.0):**
- Use Kotlin 2.4.0 / Compose BOM 2026.08.00 / Apollo 5.0.1 / Koin 4.1.1 as planned — none of those require AGP 9.
- Confirm Compose BOM 2026.08.00's `compileSdk 37` requirement doesn't force an AGP bump anyway — if it does, re-evaluate whether Path B is actually available, or use the previous BOM (2026.04.01, `compileSdk` aligned with AGP 8.13) instead.
- Explicitly flag the deferred AGP-9 KMP-plugin migration as a follow-up milestone in `PROJECT.md`'s Key Decisions.

## Version Compatibility

| Package A | Compatible With | Notes |
|-----------|------------------|-------|
| AGP 9.2.0 | Gradle ≥9.1.0, KGP ≥2.3.1 (2.3.3+ recommended), JDK ≥17 | Below these floors, AGP 9 will refuse to configure or silently misbehave with KSP-based plugins |
| Compose BOM 2026.08.00 | `compileSdk` 37 | Forces an SDK bump; verify AGP's supported max API level covers 37 (AGP 9.2 does; AGP 8.13 tops out lower) |
| Apollo Kotlin 5.0.1 | KGP ≥2.2 | Transparent for Android/JVM consumers; native/JS/Wasm consumers must be on KGP ≥2.2 to compile against it |
| koin-annotations 2.3.1 (if/when adopted) | KSP 2.3.2 | Exact pairing called out in Koin's own docs — mismatches fail at KSP-processing time, not obviously |
| kotlinx-datetime 0.8.0 | Kotlin ≥2.4.0-line toolchains bundling 2026c IANA tz database | Confirm CI/build agents have an up-to-date system tz database if relying on platform-provided data on any target |
| KSP2 | Kotlin 2.3.0+ / AGP 9.0+ | KSP1 is explicitly unsupported here — this is a hard floor, not a recommendation |

## Sources

- https://developer.android.com/build/releases/agp-9-0-0-release-notes — AGP 9.0 release notes (confidence: HIGH, official)
- https://developer.android.com/build/releases/agp-9-2-0-release-notes — AGP 9.2.0 release notes (confidence: HIGH, official)
- https://blog.jetbrains.com/kotlin/2026/01/update-your-projects-for-agp9/ — JetBrains AGP 9 migration guidance for Kotlin/KMP projects (confidence: HIGH, official)
- https://kotlinlang.org/docs/multiplatform/multiplatform-project-agp-9-migration.html — Official Kotlin Multiplatform AGP-9 migration doc (confidence: HIGH, official)
- https://github.com/kotlin/kotlin-agent-skills/blob/main/skills/kotlin-tooling-agp9-migration/references/VERSION-MATRIX.md — Kotlin/JetBrains AGP-9 KMP version compatibility matrix (confidence: HIGH, official repo)
- https://blog.gradle.org/gradle-at-kotlinconf-2026 and https://blog.gradle.org/declarative-gradle — Declarative Gradle 2026 status (confidence: MEDIUM — vendor blog, but primary source and explicit about experimental status)
- https://gradle.org/whats-new/gradle-9/ and https://docs.gradle.org/current/release-notes.html — Gradle 9.x release notes (confidence: HIGH, official)
- https://kotlinlang.org/docs/whatsnew24.html / https://blog.jetbrains.com/kotlin/2026/06/kotlin-2-4-0-released/ — Kotlin 2.4.0 release (confidence: HIGH, official)
- https://android-developers.googleblog.com/2026/08/jetpack-compose-august-2026-release.html and https://developer.android.com/develop/ui/compose/bom — Compose BOM 2026.08.00 (confidence: HIGH, official)
- https://www.apollographql.com/blog/apollo-kotlin-5-is-now-available and https://www.apollographql.com/docs/kotlin/v5/migration/5.0 — Apollo Kotlin 5 release + migration guide (confidence: HIGH, official)
- https://insert-koin.io/docs/setup/koin/ , https://insert-koin.io/docs/setup/annotations/ , https://blog.kotzilla.io/koin-4.1-is-here — Koin BOM/annotations versions and 4.1 feature set (confidence: HIGH, official docs + maintainer blog)
- https://insert-koin.io/docs/reference/koin-compiler/compile-safety/ and community deep-dives (carrion.dev, Medium) on Koin Annotations multi-module caveats (confidence: MEDIUM — official docs HIGH, community posts corroborate but are secondary)
- https://firebase.google.com/support/releases — Firebase BOM 34.18.0 (confidence: HIGH, official)
- https://github.com/Kotlin/kotlinx.coroutines/releases , https://github.com/Kotlin/kotlinx.serialization/releases , https://github.com/Kotlin/kotlinx-datetime/releases — kotlinx library versions (confidence: HIGH, official repos)
- https://github.com/google/ksp/releases and https://kotlinlang.org/docs/ksp-faq.html — KSP2/Kotlin 2.4.0 pairing and KSP1 deprecation (confidence: MEDIUM-HIGH — official, but exact `2.4.0-1.0.x` patch tag should be re-verified against Maven Central at implementation time)
- https://github.com/GitLiveApp/firebase-kotlin-sdk — GitLive Firebase KMP wrapper, evaluated and explicitly not recommended for this milestone's scope (confidence: MEDIUM — project README + community reports)

---
*Stack research for: KMP technical modernization (build tooling, DI, core dependency versions)*
*Researched: 2026-09-12*
