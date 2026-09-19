# Phase 2: Dependency & Build Tooling Upgrade - Research

**Researched:** 2026-09-17
**Domain:** Kotlin Multiplatform / Android Gradle Plugin 9 migration, staged major-dependency upgrade
**Confidence:** MEDIUM-HIGH

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

- **D-01:** Bump dependency groups sequentially in one PR, one commit per group: Kotlin 2.4.0 → AGP 9.2.0 + new KMP plugin → Compose BOM 2026.08.00 → Apollo 5.0.1 → Firebase BOM/Coroutines/kotlinx-serialization/kotlinx-datetime → Gradle Declarative DSL pilot. Each group gets its own commit, not its own PR. — Reversibility: reversible, commit-level granularity.
- **D-02:** "Green" between stages means: full CI (Android + iOS) passes on the pushed commit, AND one local sanity build/run per group (not a full manual regression) before starting the next group.
- **D-03:** If a dependency group hits a blocker (transitive conflict, a target version that doesn't build), pin to the newest version that actually builds and document the gap versus the REQUIREMENTS.md target (in STATE.md/PROJECT.md) rather than stopping the phase. Escalating to the user for every blocker was explicitly rejected.
- **D-04:** After swapping `shared`'s `kotlin.multiplatform` + `com.android.library` coexistence for `com.android.kotlin.multiplatform.library`, verification is CI green + one manual smoke pass using the `android` CLI (`android run`, `android layout`/`screen capture`) confirming Agenda/Speakers/Venue/Bookmarks still render and navigate — not a full per-screen manual regression.
- **D-05:** The AGP 9 plugin swap is verified Android-only for this stage. iOS only needs to stay CI-green (Cocoapods/Kotlin Native framework build) — no separate manual iOS smoke pass. The "three-framework problem" is Phase 3's concern.
- **D-06:** Losing cached Apollo GraphQL data across this migration is explicitly acceptable — the app is being prepared for next year's conference where all content will be replaced and bookmarks don't need to persist across this update. This relaxes the "no regression" constraint specifically for Apollo's cached data during this migration only — it does not extend to any other behavior, screen, or `BookmarksStore` (SharedPreferences-backed, separate from Apollo's cache).
- **D-07:** Verification of the Apollo 5.0.1 bump (including the `ApolloStore`→`CacheManager` rename and binary cache format change) is functional-only: confirm the normalized cache mechanism still works post-migration (repeated query hits cache, offline/airplane-mode shows last-loaded data) — no before/after data-preservation test.
- **D-08:** Pilot `.gradle.dcl` on one low-risk leaf module rather than deferring DCL entirely or attempting a full-project migration. Document what worked and what's blocked; `androidApp`/`shared` stay on `.kts`.
- **D-09:** The exact pilot module for D-08 is Claude's discretion at implementation time (likely `buildSrc` or a small standalone config module).

### Claude's Discretion

- Exact pilot module for the Gradle Declarative DSL trial (D-09).
- Exact commands/scripts used for the local sanity build per staging group (D-02) and the manual smoke pass for AGP 9 (D-04), beyond using the `android` CLI per AGENTS.md.

### Deferred Ideas (OUT OF SCOPE)

None — discussion stayed within Phase 2 scope. iOS framework/umbrella verification depth is tracked as Phase 3's "three-framework problem" spike, not a new deferred item.
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| BUILD-01 | Project compiles with Kotlin 2.4.0 | Version Verification table; Kotlin 2.4.20 is now current stable — flagged as Open Question, not unilaterally changed |
| BUILD-02 | Migrate to AGP 9.2.0 + `com.android.kotlin.multiplatform.library`, replacing forbidden `kotlin.multiplatform`+`com.android.library` coexistence | Full migration mechanics section, Common Pitfalls (Detekt/Hilt/Firebase-perf AGP9 minimums, packaging DSL syntax, R8 defaults), Code Examples |
| BUILD-03 | Gradle 9.7.1 | Verified current stable, exactly matches locked target — no gap |
| BUILD-04 | Compose BOM 2026.08.00 | Version Verification table; confirmed real, one minor release behind current latest |
| BUILD-05 | Apollo GraphQL 5.0.1 | Version Verification table (5.2.0 now latest) + full cache-package migration mechanics (`com.apollographql.apollo`→`com.apollographql.cache`), D-06/D-07 scoping |
| BUILD-06 | Firebase BOM/Coroutines/kotlinx-serialization/kotlinx-datetime → latest stable | Version Verification table (all four verified against Maven Central), kotlinx-datetime Instant/Clock pitfall verified against actual project code |
| BUILD-07 | Build files migrated to Gradle Declarative DSL where AGP/KMP support allows; undocumented modules stay `.kts` with documented reason | DCL support-matrix research, pilot-module recommendation |
</phase_requirements>

## Summary

This phase's real risk is not the "big five" dependency bumps CONTEXT.md already named — Kotlin, AGP/KMP-plugin, Compose BOM, Apollo, and the Firebase/Coroutines/serialization/datetime group are all well-documented, officially migration-guided upgrades. The risk is a set of **project-specific side effects that AGP 9.2.0 forces on every plugin already applied to this repo**, none of which are named in BUILD-01..07 but all of which will break the build if not bumped in the same commit as AGP itself: Detekt 1.23.8 (currently applied to both `shared` and `androidApp`) has no stable release compatible with AGP 9's new DSL/built-in-Kotlin — only pre-1.0 alphas fix it; Dagger Hilt 2.57.2 is below AGP 9's documented compatibility floor of 2.59; the Firebase Performance Gradle plugin 2.0.1 is one patch below its documented floor of 2.0.2; and `androidApp/build.gradle.kts`'s existing `packagingOptions { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }` uses a brace-expansion syntax AGP 9 does not reliably support. None of these are exotic — they were all confirmed against Google's own AGP 9 plugin-compatibility guidance and live Maven Central metadata — but if the planner only scopes work to BUILD-01..07's named packages, the AGP 9.2.0 stage (D-01's second commit) will fail CI on `detekt`/`lint`/Hilt annotation processing for reasons that look unrelated to the bump actually being made.

The second major finding is that Apollo Kotlin 5's normalized-cache story is not a same-package version bump: `apollo-normalized-cache` and `apollo-normalized-cache-sqlite` (group `com.apollographql.apollo`) are deprecated in v5 in favor of an entirely new artifact group, `com.apollographql.cache`, published from a separate repository with its own versioning line (currently 1.0.8) and its own Apollo-compiler-plugin dependency. This is a bigger mechanical change than "rename ApolloStore to CacheManager" — it is a dependency-coordinate migration plus a schema-directive version bump (cache spec v0.3 → v0.4) plus a destructive SQLite binary-format migration, which is exactly what CONTEXT.md's D-06/D-07 already scoped as acceptable to lose. The third finding is good news: this project's `shared` module already uses the standard KMP source-set layout (`androidMain`/`commonMain`/`iosMain`, no legacy `src/main`) and has no `androidTest`/`androidUnitTest` directories today, so the AGP 9 migration guide's "move `src/main` to `src/androidMain`" and "rename `androidUnitTest`/`androidInstrumentedTest`" steps are **not applicable** — this repo starts from a cleaner position than the generic migration guide assumes.

**Primary recommendation:** Treat "bump Detekt, Hilt, and the Firebase Gradle plugins to their AGP-9-compatible floors" as a required, undocumented sub-task of the AGP 9.2.0 commit (D-01's second stage) — not an optional cleanup — and treat the Apollo cache-package migration (not just the version number) as the bulk of the Apollo 5.0.1 commit's work.

## Version Verification (re-checked 2026-09-17)

STATE.md flagged the 2026-09-12 research as needing re-verification "just before implementation" because this toolchain area moves fast. Every version below was checked against an authoritative source (Maven Central / Google's Maven / official docs) on 2026-09-17, five days after the original research.

| Package | REQUIREMENTS.md target | Verified current latest stable | Gap | Disposition |
|---|---|---|---|---|
| Kotlin (KGP) | 2.4.0 (BUILD-01, hard pin) | **2.4.20** [VERIFIED: repo1.maven.org/.../kotlin-gradle-plugin/maven-metadata.xml] | 2 patch releases behind | Requirement is a hard pin — follow it literally unless user confirms bumping to 2.4.20. Flagged as Open Question below. |
| AGP | 9.2.0 (BUILD-02, hard pin) | **9.4.0** [VERIFIED: dl.google.com/.../com/android/tools/build/gradle/maven-metadata.xml] (9.5.0-alpha06 is the newest but pre-release) | 2 minor releases behind | Same as above — hard pin, flag as Open Question. Note AGP 9.2.0's own documented minimum Gradle is 9.4.1 [CITED: developer.android.com/build/releases/agp-9-2-0-release-notes], comfortably below the locked Gradle 9.7.1 target, so no conflict there. |
| Gradle | 9.7.1 (BUILD-03, hard pin) | **9.7.1** [VERIFIED: WebSearch of gradle.org/releases, corroborated by docs.gradle.org/current/release-notes.html], released 2026-08-19 | None | Exact match — no action needed. |
| Compose BOM | 2026.08.00 (BUILD-04, hard pin) | **2026.09.00** [VERIFIED: dl.google.com/.../androidx/compose/compose-bom/maven-metadata.xml] | 1 release behind | Hard pin, flag as Open Question. |
| Apollo Kotlin | 5.0.1 (BUILD-05, hard pin) | **5.2.0** [VERIFIED: repo1.maven.org/.../com/apollographql/apollo/apollo-runtime/maven-metadata.xml] | 2 minor releases behind | Hard pin, flag as Open Question. The cache-package migration mechanics below apply regardless of which 5.x patch is used. |
| Firebase BOM | "latest stable" (BUILD-06, not pinned) | **34.19.0** [VERIFIED: dl.google.com/.../com/google/firebase/firebase-bom/maven-metadata.xml] | N/A — requirement text says "latest stable" | Use 34.19.0 directly; this satisfies BUILD-06 as written (research/SUMMARY.md's cited 34.18.0 is one release stale). |
| kotlinx-coroutines-core | "latest stable" (BUILD-06) | **1.11.0** [VERIFIED: repo1.maven.org/.../kotlinx-coroutines-core/maven-metadata.xml] | None | Matches research/SUMMARY.md exactly. |
| kotlinx-serialization-json | "latest stable" (BUILD-06) | **1.11.0** [VERIFIED: repo1.maven.org/.../kotlinx-serialization-json/maven-metadata.xml] (1.12.0-RC exists but is a release candidate, not stable) | None | Matches research/SUMMARY.md exactly; do not take the RC. |
| kotlinx-datetime | "latest stable" (BUILD-06) | **0.8.0** [VERIFIED: repo1.maven.org/.../kotlinx-datetime/maven-metadata.xml] | None | Matches research/SUMMARY.md exactly. See Pitfall "kotlinx-datetime Instant/Clock" below — this project has 2 files using `kotlinx.datetime.Instant` directly. |

**Cross-cutting versions NOT named in BUILD-01..07 but forced by the AGP 9.2.0 bump** (see Common Pitfalls for why):

| Package | Current (STACK.md) | AGP-9-compatible floor | Verified current latest | Action |
|---|---|---|---|---|
| Detekt | 1.23.8 | No stable release fixes AGP 9 compat; alpha.3+ removes the need for opt-out flags [ASSUMED — sourced via WebSearch of GitHub issues/release notes, not official docs; treat as needing live-build confirmation] | 2.0.0-alpha.6 (plugin ID also changes: `io.gitlab.arturbosch.detekt` → `dev.detekt`) [ASSUMED] | Must bump to a 2.0.0 alpha as part of the AGP 9.2.0 commit, or the `detekt`/CI `checks` job breaks. See Pitfall below. |
| Dagger Hilt | 2.57.2 | 2.59 [CITED: JetBrains kotlin-agent-skills AGP9-migration PLUGIN-COMPATIBILITY reference] | 2.60.1 [VERIFIED: repo1.maven.org/.../com/google/dagger/hilt-android/maven-metadata.xml] | Bump to 2.60.1 (or at least 2.59) in the AGP 9.2.0 commit. Hilt itself is NOT migrating away until Phase 4 — this is a compatibility bump only. |
| Firebase Performance Gradle plugin | 2.0.1 | 2.0.2 [CITED: JetBrains kotlin-agent-skills AGP9-migration PLUGIN-COMPATIBILITY reference] | 2.0.2 [VERIFIED: dl.google.com/.../com/google/firebase/perf-plugin/maven-metadata.xml] | Bump to 2.0.2 — this is exactly the documented floor, no headroom. |
| google-services plugin | 4.4.3 | Not on the AGP9 compat table (implies OK) | 4.5.0 [VERIFIED: dl.google.com/.../com/google/gms/google-services/maven-metadata.xml] | Optional bump alongside Firebase BOM in the Firebase commit (BUILD-06), not strictly required by AGP 9. |
| firebase-crashlytics Gradle plugin | 3.0.6 | Not on the AGP9 compat table (implies OK) | 3.0.8 [VERIFIED: dl.google.com/.../com/google/firebase/firebase-crashlytics-gradle/maven-metadata.xml] | Optional bump alongside Firebase BOM. |
| KSP | 2.2.20-2.0.3 | 2.3.1 minimum, 2.3.3+ recommended [CITED: JetBrains kotlin-agent-skills AGP9-migration VERSION-MATRIX] | 2.3.12 (plugin marker artifact) [VERIFIED: repo1.maven.org/.../com.google.devtools.ksp.gradle.plugin/maven-metadata.xml] | Must be bumped in the **Kotlin 2.4.0 commit** (D-01 stage 1), not deferred to the AGP commit — KSP's own version is tied to the Kotlin compiler version it targets, so this has to move in lockstep with the Kotlin bump. Look up the exact KSP release string paired with the final chosen Kotlin version on the KSP GitHub releases page at implementation time [ASSUMED — exact paired version string not confirmed this session]. |

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Kotlin language/compiler version | Build / Toolchain | — | Affects every module; must be bumped before dependent plugin versions (KSP, Compose compiler) can move |
| Android Gradle Plugin + KMP integration | Build / Toolchain | API/Backend (`shared`) | AGP configures how `shared` (KMP library) and `androidApp` (application) are compiled; the plugin swap is purely build-system, no runtime behavior change |
| Compose BOM | Browser/Client-equivalent (Android UI in `androidApp`) | — | Compose is Android-only in this project (iOS UI is native SwiftUI) — bump is isolated to `androidApp`'s UI dependency graph |
| Apollo GraphQL client + normalized cache | API/Backend (`shared`'s `GraphQLStore`) | Database/Storage (SQLite-backed cache) | `shared`'s network/data layer owns the GraphQL client; the cache package move is a storage-layer concern nested inside it |
| Firebase (Analytics/Crashlytics/Perf/RemoteConfig) | API/Backend (Android-only services in `androidApp`) | — | Firebase SDKs are Android-only per STACK.md; no KMP `commonMain` surface exists yet (that's Phase 3's `core-analytics` extraction) |
| Coroutines / kotlinx-serialization / kotlinx-datetime | Build / Toolchain (shared library versions) | API/Backend (`shared` commonMain usage) | These are KMP `commonMain` libraries consumed by both `shared` and `androidApp`; the version bump is toolchain-level but the datetime API surface change (Instant/Clock) is a `shared`-module code concern |
| Gradle Declarative DSL pilot | Build / Toolchain | — | Purely a build-file authoring format change; zero runtime impact by construction |

## Standard Stack

### Core (staged bump order per D-01)

| Library | Target version | Purpose | Why Standard |
|---------|-----------------|---------|---------------|
| Kotlin / KGP | 2.4.0 (requirement text) — verify against 2.4.20 per Open Questions | Language + Gradle plugin baseline | Required by AGP 9's built-in Kotlin support and every downstream kotlinx library |
| AGP + `com.android.kotlin.multiplatform.library` | 9.2.0 (requirement text) — verify against 9.4.0 per Open Questions | Android build system; new plugin is the only AGP-9-supported way to combine KMP + Android target | `com.android.library`+`kotlin.multiplatform` coexistence is deprecated and removed in AGP 10.0 (2H 2026) [CITED: developer.android.com/kotlin/multiplatform/plugin] |
| Gradle | 9.7.1 | Build tool | Verified current stable; matches AGP 9.2.0's documented floor (9.4.1) with headroom |
| Compose BOM | 2026.08.00 (requirement text) — verify against 2026.09.00 per Open Questions | Android Compose UI dependency alignment | Standard BOM pattern already in use |
| Apollo Kotlin | 5.0.1 (requirement text) — verify against 5.2.0 per Open Questions | GraphQL client | Current major line; this project has no subscriptions, so the WebSocket rewrite in v5 is a non-issue |
| `com.apollographql.cache:normalized-cache` + `normalized-cache-sqlite` | 1.0.8 (latest as of 2026-09-17) [VERIFIED: repo1.maven.org/.../com/apollographql/cache/normalized-cache/maven-metadata.xml] | Replaces `com.apollographql.apollo:apollo-normalized-cache(-sqlite)` | Official successor package for Apollo Kotlin 5's cache layer; old package is deprecated, not just superseded |
| Firebase BOM | 34.19.0 | Firebase services umbrella | Verified latest stable |
| kotlinx-coroutines | 1.11.0 | Async/Flow | Verified latest stable |
| kotlinx-serialization-json | 1.11.0 | JSON parsing | Verified latest stable (1.12.0-RC excluded — not stable) |
| kotlinx-datetime | 0.8.0 | Multiplatform date/time | Verified latest stable; carries the Instant/Clock breaking change from 0.7.0 (see Pitfalls) |

### Supporting (forced bumps, not named in BUILD-01..07)

| Library | Target version | Purpose | When to Use |
|---------|-----------------|---------|-------------|
| Detekt | 2.0.0-alpha.6 (or newest alpha at implementation time) [ASSUMED] | Kotlin static analysis, CI `checks` job | Required the moment AGP 9.2.0 is applied to a module Detekt also runs against (`shared`, `androidApp`) — see Pitfall |
| Dagger Hilt | 2.60.1 (floor: 2.59) | DI (until Phase 4's Koin migration) | Required floor for AGP 9 compatibility; do not migrate off Hilt in this phase |
| Firebase Performance Gradle plugin | 2.0.2 | Firebase Perf Gradle integration | Exact documented AGP-9 floor |
| KSP | Latest paired with final Kotlin version (≥2.3.1, target ~2.3.12) | Apollo codegen, Hilt annotation processing | Bump in the Kotlin commit, not the AGP commit |

### Alternatives Considered

| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Bumping Detekt to a 2.0.0 alpha | Apply `android.newDsl=false` + `android.builtInKotlin=false` and stay on Detekt 1.23.8 | These flags revert the whole project to the legacy AGP DSL, which conflicts with `com.android.kotlin.multiplatform.library`'s explicit "new-DSL-only" design [CITED: developer.android.com/kotlin/multiplatform/plugin known-issues]. Not viable if BUILD-02 must be satisfied — flagged as an Open Question in case the user wants to explore it anyway. |
| New `com.apollographql.cache` package | Stay on deprecated `com.apollographql.apollo:apollo-normalized-cache` | Deprecated packages are not guaranteed compatible with Apollo runtime 5.x going forward; D-06/D-07 already accept losing cached data, so there's no reason to avoid the clean migration |

**Installation (illustrative — planner fills in exact catalog entries at implementation time):**
```bash
# No new install commands beyond editing gradle/libs.versions.toml — every dependency
# in this phase already exists in the catalog; only [versions] values, plugin ids
# (Detekt), and cache-library group coordinates (Apollo) change.
```

**Version verification:** All versions in the tables above were checked against Maven Central / Google's Maven `maven-metadata.xml` on 2026-09-17 via direct HTTP fetch, not training-data recall. Re-run these checks again immediately before executing each staged commit — this is a fast-moving toolchain area and even 5 days produced multiple stale numbers (see Version Verification table).

## Package Legitimacy Audit

This phase bumps existing, long-established packages (Kotlin, AGP, Gradle, Compose, Apollo, Firebase, kotlinx-*, Detekt, Hilt) — no `npm`/`pypi`/`crates` packages are introduced, so the automated `gsd_run query package-legitimacy check` gate (which only supports `--ecosystem npm|pypi|crates`) does not apply to this Gradle/Maven-ecosystem phase. The one genuinely new artifact coordinate is Apollo's cache package family, manually verified below:

| Package | Registry | Age | Downloads | Source Repo | Verdict | Disposition |
|---------|----------|-----|-----------|-------------|---------|-------------|
| `com.apollographql.cache:normalized-cache` | Maven Central | 29 published versions from alpha through 1.0.8, actively updated (last update 2026-09-17) [VERIFIED: repo1.maven.org maven-metadata.xml] | N/A (Maven Central doesn't expose download counts via metadata) | github.com/apollographql/apollo-kotlin-normalized-cache (official Apollo GraphQL org) [CITED: apollographql.com/docs/kotlin/v5/caching/migration-guide] | OK | Approved — official first-party successor package, documented on apollographql.com |
| `com.apollographql.cache:normalized-cache-sqlite` | Maven Central | Same release line as above | N/A | Same repo | OK | Approved |
| `com.apollographql.cache:normalized-cache-apollo-compiler-plugin` | Maven Central | Same release line as above | N/A | Same repo | OK | Approved |
| `dev.detekt` (Detekt 2.0.0-alpha plugin group) | Gradle Plugin Portal / Maven Central | Pre-1.0, alpha since ~2026-Q2 | N/A | github.com/detekt/detekt (official) | SUS (pre-release maturity, not a legitimacy concern) | Flagged — planner must add a `checkpoint:human-verify` task before pinning CI to an alpha static-analysis tool; document the alpha-pin rationale in STATE.md per D-03 |

**Packages removed due to [SLOP] verdict:** none.
**Packages flagged as suspicious [SUS]:** `dev.detekt` 2.0.0-alpha.x — not a supply-chain risk (it's the official detekt org's own next major version), but its pre-1.0 status means CI-breaking regressions are more likely than with a stable release; the human-verify checkpoint is about build stability, not package trust.

## Architecture Patterns

### System Architecture Diagram (staged dependency-bump pipeline)

```
                     ┌─────────────────────────────────────────────┐
                     │   Stage 1: Kotlin 2.4.0(.20?) + KSP bump     │
                     │   (root build.gradle.kts, libs.versions.toml)│
                     └───────────────────┬───────────────────────────┘
                                          │ CI green (D-02) + local build
                                          ▼
                     ┌─────────────────────────────────────────────┐
                     │ Stage 2: AGP 9.2.0(.4.0?) +                  │
                     │   com.android.kotlin.multiplatform.library   │
                     │   swap on shared/build.gradle.kts            │
                     │   + forced bumps: Detekt, Hilt, Firebase-perf│
                     │   + packagingOptions syntax fix (androidApp) │
                     └───────────────────┬───────────────────────────┘
                                          │ CI green + android CLI smoke
                                          │ pass (D-04): Agenda/Speakers/
                                          │ Venue/Bookmarks render+nav
                                          ▼
                     ┌─────────────────────────────────────────────┐
                     │ Stage 3: Compose BOM 2026.08.00(.09.00?)     │
                     │   (androidApp/build.gradle.kts only —        │
                     │   Android-only UI dependency)                │
                     └───────────────────┬───────────────────────────┘
                                          │ CI green + local build
                                          ▼
                     ┌─────────────────────────────────────────────┐
                     │ Stage 4: Apollo 5.0.1(.2.0?) +               │
                     │   com.apollographql.apollo → .cache package  │
                     │   migration (ApolloStore→CacheManager,       │
                     │   cache spec v0.3→v0.4, destructive SQLite   │
                     │   schema migration — acceptable per D-06)    │
                     └───────────────────┬───────────────────────────┘
                                          │ CI green + functional cache
                                          │ check (D-07): repeat query
                                          │ hits cache; airplane-mode
                                          │ shows last-loaded data
                                          ▼
                     ┌─────────────────────────────────────────────┐
                     │ Stage 5: Firebase BOM 34.19.0 +              │
                     │   Coroutines 1.11.0 + serialization 1.11.0 + │
                     │   datetime 0.8.0 (Instant/Clock check on     │
                     │   ScheduleSlot.kt / Agenda.kt)                │
                     └───────────────────┬───────────────────────────┘
                                          │ CI green + local build
                                          ▼
                     ┌─────────────────────────────────────────────┐
                     │ Stage 6: Gradle Declarative DSL pilot        │
                     │   (buildSrc or settings.gradle.kts →         │
                     │   settings.gradle.dcl; androidApp/shared     │
                     │   stay .kts, documented why)                 │
                     └─────────────────────────────────────────────┘
```

### Recommended Project Structure (unchanged file layout — this phase touches build files, not source layout)

```
DevFestNantes/
├── build.gradle.kts              # root — plugin aliases `apply false`; Detekt/Hilt/Firebase-perf version bumps land here
├── settings.gradle.kts           # candidate DCL pilot target (Stage 6) — currently minimal (repos + 2 includes)
├── gradle/libs.versions.toml     # single source of truth for every version in this phase
├── buildSrc/                     # candidate DCL pilot target (Stage 6) — single build.gradle.kts + AndroidSdk object
├── shared/build.gradle.kts       # Stage 2's primary target: kotlin.multiplatform + com.android.library → com.android.kotlin.multiplatform.library
├── shared/src/androidMain/       # already correctly named — no source-dir rename needed
├── shared/src/commonMain/        # already correctly named
├── shared/src/iosMain/           # already correctly named
├── androidApp/build.gradle.kts   # Stage 2 (packaging syntax, Hilt/Firebase-perf bumps), Stage 3 (Compose BOM), Stage 5 (Firebase BOM)
└── .github/workflows/{android,ios}.yml  # unaffected by version bumps except detekt/lint job outcomes; uses `gradle-version: wrapper` so Gradle bump needs no CI file edit
```

### Pattern 1: AGP 9 KMP-library plugin swap (`shared/build.gradle.kts`)

**What:** Replace `alias(libs.plugins.android.library)` with the new `com.android.kotlin.multiplatform.library` plugin, and move the top-level `android {}` block into `kotlin { android {} }`.
**When to use:** Any Gradle module that currently combines `org.jetbrains.kotlin.multiplatform` with `com.android.library` (this repo has exactly one: `shared`).
**This project's specific starting point** [VERIFIED: shared/build.gradle.kts, lines 1-89 read this session]:
```kotlin
// BEFORE (current shared/build.gradle.kts)
plugins {
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)   // <-- forbidden coexistence in AGP 9+
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kmp.native.coroutines)
    alias(libs.plugins.appollo)
}

kotlin {
    androidTarget {
        compilerOptions { jvmTarget.set(JvmTarget.JVM_11) }
    }
    jvm()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "shared"; isStatic = true } }
    sourceSets { /* commonMain, commonTest deps */ }
}

android {
    compileSdk = AndroidSdk.compile
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")  // <-- likely deletable, see note
    defaultConfig { minSdk = AndroidSdk.min }
    namespace = "com.gdgnantes.devfest"
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8   // <-- inconsistent with KGP's JVM_11 above; unify during migration
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
```
```kotlin
// AFTER — per official migration guide [CITED: developer.android.com/kotlin/multiplatform/plugin]
plugins {
    alias(libs.plugins.detekt)                          // bumped to 2.0.0-alpha.x, see Pitfalls
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.kotlin.multiplatform.library)  // NEW plugin id
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)                             // bumped to match Kotlin, see Pitfalls
    alias(libs.plugins.kmp.native.coroutines)
    alias(libs.plugins.appollo)
}

kotlin {
    android {
        namespace = "com.gdgnantes.devfest"
        compileSdk = AndroidSdk.compile
        minSdk = AndroidSdk.min
        // No sourceSets["main"].manifest.srcFile(...) override needed: the new plugin's
        // default manifest location IS src/androidMain/AndroidManifest.xml, which is
        // already where this file lives [VERIFIED: shared/src/androidMain/AndroidManifest.xml
        // exists this session] — the explicit override becomes redundant, not broken.
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)  // single source of truth for both Kotlin AND Java compilation
        }
        // androidResources { enable = true } — NOT needed: shared/src has no res/ directory
        // withJava() — NOT needed: no .java sources found under shared/src/androidMain
        // withHostTest{} / withDeviceTest{} — NOT needed today: no androidUnitTest/
        //   androidInstrumentedTest dirs exist in shared/src [VERIFIED: `find shared/src`
        //   this session shows only androidMain, commonMain, commonTest, iosMain, jvmTest]
    }
    jvm()
    listOf(iosX64(), iosArm64(), iosSimulatorArm64()).forEach { it.binaries.framework { baseName = "shared"; isStatic = true } }
    sourceSets { /* commonMain, commonTest deps — unchanged */ }
}
// Root-level android {} block is gone entirely — replaced by kotlin { android {} } above.
```
**Source not-needed rename:** the generic migration guide's Step 1 ("move `src/main` to `src/androidMain`, `src/test` to `src/androidHostTest`, `src/androidTest` to `src/androidDeviceTest`") does **not apply** to this repo — `shared/src` already uses `androidMain`/`commonMain`/`iosMain`/`commonTest`/`jvmTest` [VERIFIED: `find /shared/src -maxdepth 2 -type d` this session], and there are no `androidUnitTest`/`androidInstrumentedTest` directories to rename at all.

### Pattern 2: Apollo 5.x normalized-cache package migration

**What:** `com.apollographql.apollo:apollo-normalized-cache` and `-sqlite` are deprecated in v5; replace with `com.apollographql.cache:normalized-cache` and `-sqlite`, add the cache compiler plugin, and rename `ApolloStore`→`CacheManager`.
**When to use:** Required for this project's `shared/build.gradle.kts` `apollo { }` block and its `libs.bundles.appollo` catalog bundle.
```kotlin
// Source: apollographql.com/docs/kotlin/v5/caching/normalized-cache and migration-guide [CITED]
// gradle/libs.versions.toml changes:
// [versions]
// appolloCache = "1.0.8"   // NEW — separate version line from apollo-runtime's 5.x
// [libraries]
// appollo-normalized-cache = { group = "com.apollographql.cache", name = "normalized-cache", version.ref = "appolloCache" }          // group changed
// appollo-normalized-cache-sqlite = { group = "com.apollographql.cache", name = "normalized-cache-sqlite", version.ref = "appolloCache" } // group changed

// shared/build.gradle.kts — apollo compiler plugin registration:
apollo {
    service("service") {
        packageName.set("com.gdgnantes.devfest.graphql")
        plugin("com.apollographql.cache:normalized-cache-apollo-compiler-plugin:1.0.8")
        pluginArgument("com.apollographql.cache.packageName", packageName.get())
    }
}

// GraphQLStore.kt (or wherever ApolloStore is referenced) — rename:
// Before: val store: ApolloStore = apolloClient.apolloStore
// After:  val cacheManager: CacheManager = apolloClient.apolloStore   // name kept per Apollo's own doc,
//         type is now CacheManager; operations are now suspend functions
```
**Functional-only verification (D-07):** confirm a repeated query hits the cache and offline/airplane-mode shows last-loaded data — do not attempt to preserve or compare pre-migration cache contents (D-06 explicitly waives this).

### Anti-Patterns to Avoid

- **Applying `android.newDsl=false`/`android.builtInKotlin=false` to unblock Detekt instead of bumping it:** these flags revert the whole project off the new DSL that `com.android.kotlin.multiplatform.library` itself requires — treat them as a last-resort, not the default path, per BUILD-02.
- **Treating the Apollo bump as "just a version number":** the cache-package group change is silent at the version-catalog level (nothing errors until compile) if only the version string is bumped without also changing the `group` on the two cache artifacts.
- **Deferring the KSP bump to the AGP commit:** KSP's compatibility is tied to the Kotlin compiler version, so it must move with Kotlin in Stage 1, not sit on an old KSP release through Stage 1 and break in Stage 2.

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Detecting whether a plugin is AGP-9-compatible | Ad-hoc trial-and-error per plugin | The JetBrains kotlin-agent-skills AGP9-migration Plugin Compatibility reference (already indexed in `android docs search`) | Maintained, cross-referenced against official Android/plugin-author sources; covers exactly the plugins this repo uses (Detekt, Hilt, Firebase-perf, Apollo, KSP) |
| Verifying a Gradle/AGP/Kotlin version is genuinely current | Trusting training-data version numbers or a single blog post | Direct `maven-metadata.xml` fetch from Maven Central / Google's Maven, or `android docs search`/`android docs fetch` | This research found 4 of 9 pinned CONTEXT.md target versions already 1-2 releases stale after only 5 days — training-data recall is not reliable here |
| Cache invalidation / normalized cache correctness for GraphQL | A custom `NormalizedCache` implementation | Apollo's own `com.apollographql.cache` package (`MemoryCacheFactory`, `SqlNormalizedCacheFactory`, `.chain()`) | This is exactly Apollo's stated purpose for the rewrite (pagination, expiration, garbage collection, trimming) — reinventing it defeats the point of the upgrade |

**Key insight:** In a fast-moving toolchain migration, the temptation is to treat "verify versions" as a one-time step done in the prior research pass. It is not — every version in this phase's scope should be re-checked against Maven Central/Google's Maven immediately before the commit that bumps it, not just once at phase-research time.

## Common Pitfalls

### Pitfall 1: Detekt has no stable release compatible with AGP 9's new DSL
**What goes wrong:** `./gradlew detekt` (the CI `checks` job) silently stops generating Android-variant-specific tasks, or fails outright, once AGP 9.2.0 + built-in Kotlin is applied to a module Detekt also targets (`shared`, `androidApp` both apply Detekt today).
**Why it happens:** Detekt 1.23.8 was built against AGP 8.x's legacy DSL and `kotlin-android` plugin model; AGP 9's built-in Kotlin support conflicts with it. Detekt's own 2.0.0 alpha line (alpha.3+) fixes this but is not yet a stable (1.0-quality-bar) release as of 2026-09-17 [ASSUMED — sourced from WebSearch of GitHub issues, not an official detekt.dev release announcement; confirm the exact alpha number at implementation time].
**How to avoid:** Bump Detekt to the newest 2.0.0-alpha release in the same commit as the AGP 9.2.0 + KMP plugin swap (Stage 2), and update the plugin id from `io.gitlab.arturbosch.detekt` to `dev.detekt` in `gradle/libs.versions.toml` and both build files. Add a `checkpoint:human-verify` task before relying on this alpha in CI, per the Package Legitimacy Audit.
**Warning signs:** `./gradlew detekt` reports zero issues found across the whole codebase (task silently no-ops) rather than an explicit build failure — this is a classic silent-regression pattern, not a loud one.

### Pitfall 2: Dagger Hilt and Firebase Performance plugin versions block the AGP 9.2.0 bump
**What goes wrong:** `androidApp/build.gradle.kts` applies `com.google.dagger.hilt.android` (2.57.2) and `com.google.firebase.firebase-perf` (2.0.1) — both below AGP 9's documented compatibility floors (2.59 and 2.0.2 respectively) [CITED: JetBrains kotlin-agent-skills PLUGIN-COMPATIBILITY reference]. KSP-based Hilt annotation processing or the Perf plugin's bytecode instrumentation may fail with cryptic errors that look unrelated to AGP.
**Why it happens:** These plugins depend on now-removed AGP 8.x variant APIs internally; their own maintainers shipped fixed releases ahead of AGP 9's GA.
**How to avoid:** Bump Hilt to 2.60.1 (floor 2.59) and firebase-perf plugin to 2.0.2 in the same AGP 9.2.0 commit — do not treat this as a Phase 4 (DI migration) concern; it is a compatibility floor, not a framework swap.
**Warning signs:** `ksp`/`kspAndroidTest`/`kspTest` tasks fail during the AGP bump with Hilt-generated-code errors, or Crashlytics/Perf-instrumented release builds fail silently.

### Pitfall 3: Apollo cache package coordinates change, not just the version number
**What goes wrong:** Bumping `appollo = "5.0.1"` in `libs.versions.toml` alone leaves `appollo-normalized-cache`/`appollo-normalized-cache-sqlite` pointing at the deprecated `com.apollographql.apollo` group; the build may still resolve (if those artifacts still publish 5.x-compatible versions) but the app is then running a soon-unsupported cache implementation, or dependency resolution fails outright if the old group stops publishing new versions.
**Why it happens:** Apollo's normalized cache moved to a dedicated repository (`apollographql/apollo-kotlin-normalized-cache`) with its own `com.apollographql.cache` Maven group and independent version line (1.0.8, not tied to apollo-runtime's 5.x numbering).
**How to avoid:** Change the `group` on both cache catalog entries to `com.apollographql.cache`, add the compiler plugin (`normalized-cache-apollo-compiler-plugin`) to the `apollo { service {} }` block, and update the `ApolloStore`→`CacheManager` references. See Pattern 2 above for the exact code.
**Warning signs:** Build succeeds but GraphQL queries never hit cache (using the old, no-longer-wired cache class), or `readOperation()`/`readFragment()` call sites fail to compile because their return types changed (`ApolloResponse<D>` / `ReadResult<D>` instead of bare `<D>`).

### Pitfall 4: `kotlinx.datetime.Instant` usage in this codebase
**What goes wrong:** `ScheduleSlot.kt` and `Agenda.kt` both `import kotlinx.datetime.Instant` and call `.parse()`, `.toEpochMilliseconds()`, `.minus()`, and `Duration.inWholeDays` [VERIFIED: shared/src/commonMain/kotlin/com/gdgnantes/devfest/model/ScheduleSlot.kt:3,9-10 — `import kotlinx.datetime.Instant` / `Instant.parse(startDate).toEpochMilliseconds()`; shared/src/commonMain/kotlin/com/gdgnantes/devfest/model/Agenda.kt:3,14,33-34 — `import kotlinx.datetime.Instant` / `Instant::parse` / `Instant.parse(DAY_ONE_ISO)` / `startInstant.minus(DAY_ONE).inWholeDays`]. kotlinx-datetime 0.7.0 removed `kotlinx.datetime.Instant`/`Clock` in favor of `kotlin.time.Instant`/`Clock` (stable since Kotlin 2.3); 0.7.1 added typealiases back for smoother migration.
**Why it happens:** This is a genuine, intentional breaking change upstream — not a project bug — but this project's two files sit squarely in the affected surface.
**How to avoid:** After bumping to kotlinx-datetime 0.8.0, do a full `shared` module compile as part of Stage 5's local sanity build (D-02) specifically checking `ScheduleSlot.kt`/`Agenda.kt`. `kotlin.time.Instant` (target of the typealias, if 0.8.0 still provides one) supports `.parse()`, `.toEpochMilliseconds()`, and `.minus(Instant): Duration` with an unchanged signature [VERIFIED via WebSearch of kotlinlang.org/api/core/kotlin-stdlib/kotlin.time docs], so if the typealias survives in 0.8.0 this pitfall may cost zero code changes — but whether `kotlinx.datetime.Instant` remains a typealias in 0.8.0 specifically (vs. only 0.7.1) was **not confirmed** this session [ASSUMED — flagged for live verification, not silence-as-evidence: the 0.8.0 CHANGELOG entry fetched this session did not mention the typealias's continued existence one way or the other].
**Warning signs:** `shared` module fails to compile with "unresolved reference: Instant" after the datetime bump, or (worse) compiles fine but the JVM/Android build succeeds while Kotlin/Native (iOS) klib resolution fails later — this failure mode is platform-asymmetric per real-world reports from other projects hitting the same 0.7.0 change.

### Pitfall 5: AGP 9's packaging-exclusion DSL syntax change affects `androidApp` regardless of the KMP plugin swap
**What goes wrong:** `androidApp/build.gradle.kts` has `packagingOptions { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }` — this brace-expansion pattern is documented as unreliable under AGP 9's new DSL [CITED: JetBrains kotlin-agent-skills KNOWN-ISSUES reference].
**Why it happens:** This is a general `com.android.application`-plugin DSL change in AGP 9, unrelated to the KMP-library-plugin swap — it affects `androidApp` purely because AGP itself is moving to 9.x, independent of BUILD-02's `shared`-specific plugin change.
**How to avoid:** Replace with explicit `.add()` calls: `excludes.add("/META-INF/AL2.0"); excludes.add("/META-INF/LGPL2.1")`.
**Warning signs:** Release/debug APK packaging fails or silently includes/excludes the wrong META-INF files.

### Pitfall 6: R8's stricter AGP 9 defaults may affect `androidApp`'s existing ProGuard rules
**What goes wrong:** AGP 9.2.0 sets `android.r8.strictFullModeForKeepRules=true` by default — keep rules no longer implicitly keep default constructors — and disallows `-dontobfuscate`/global options inside consumer keep rules [CITED: android docs fetch, AGP 9.2.0 release notes + JetBrains KNOWN-ISSUES reference].
**Why it happens:** R8 default-behavior changes ship with the AGP version, not the KMP plugin.
**How to avoid:** Review `androidApp/proguard-rules.pro` for constructs assuming implicit default-constructor retention when moving to AGP 9.2.0/9.4.0; the project already uses `getDefaultProguardFile("proguard-android-optimize.txt")` (the only supported file per `proguardAndroidTxt.disallowed=true`), so no change needed there.
**Warning signs:** Release build (`isMinifyEnabled = true`, `isShrinkResources = true` are both set in `androidApp`) crashes at runtime with `NoSuchMethodException`/`ClassNotFoundException` for classes previously working via implicit keep behavior.

## Code Examples

### `shared/build.gradle.kts` — full before/after skeleton
See Pattern 1 above — the exact current content and the exact migrated shape, both verified against this repo's actual file.

### Apollo cache setup
See Pattern 2 above.

### `androidApp/build.gradle.kts` packaging fix
```kotlin
// Source: JetBrains kotlin-agent-skills AGP9-migration KNOWN-ISSUES reference [CITED]
// Before (current androidApp/build.gradle.kts, line 82-86):
packagingOptions {
    resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}
// After:
packaging {
    resources {
        excludes.add("/META-INF/AL2.0")
        excludes.add("/META-INF/LGPL2.1")
    }
}
```

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|---------------|--------|
| `kotlin.multiplatform` + `com.android.library` coexistence for KMP Android targets | `com.android.kotlin.multiplatform.library` plugin | AGP 9.0.0 (Jan 2026); old path removed entirely in AGP 10.0 (2H 2026) | This project's `shared` module must migrate now — staying on AGP 8.x is explicitly out of scope per REQUIREMENTS.md's Out of Scope table |
| `com.apollographql.apollo:apollo-normalized-cache(-sqlite)` | `com.apollographql.cache:normalized-cache(-sqlite)` + compiler plugin | Apollo Kotlin 5.0 (2026) | New cache package required for continued support; old one is deprecated, not removed-yet but frozen |
| `kotlinx.datetime.Instant`/`Clock` | `kotlin.time.Instant`/`Clock` (stdlib, stable since Kotlin 2.3) | kotlinx-datetime 0.7.0 (mid-2026), softened with typealiases in 0.7.1 | Any code importing `kotlinx.datetime.Instant` directly needs a compile-time check post-bump |
| `androidTarget { compilations.all { kotlinOptions { jvmTarget = "11" } } }` | `kotlin { android { compilerOptions { jvmTarget.set(JvmTarget.JVM_11) } } }` (or top-level `compilerOptions`/`jvmToolchain`) | AGP 9.0 (built-in Kotlin) | Old `kotlinOptions` DSL is removed, not just deprecated |
| `io.gitlab.arturbosch.detekt` plugin id / Maven group | `dev.detekt` (in Detekt 2.0.0-alpha+) | Detekt 2.0.0 alpha line (2026) | Coordinate rename; both the plugin id and `detektPlugins` artifact group change |

**Deprecated/outdated:**
- `com.android.library` + `org.jetbrains.kotlin.multiplatform` in the same module: deprecated in AGP 9.0, opt-in required, removed in AGP 10.0.
- `BaseExtension`/`AppExtension`/`LibraryExtension` (legacy AGP DSL types), `applicationVariants`/`libraryVariants`/`variantFilter` APIs: removed in AGP 9.0 — relevant only if a future convention-plugin refactor (Phase 3) touches build logic directly.
- `apollo-http-cache`, `downloadApolloSchema` task, `operationOutputGenerator`/`operationIdGenerator`: deprecated/removed in Apollo Kotlin 5 — not currently used by this project (`documentation/`/`shared/src/commonMain/graphql/schema.graphqls` uses the plain `apollo { service {} }` block), so no action needed, but worth a quick grep during implementation to confirm.

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | Detekt 2.0.0-alpha.3+ (specifically alpha.6 as latest) fixes AGP 9 compatibility without opt-out flags | Version Verification, Pitfall 1 | If the specific alpha chosen still has the AGP9 regression, the CI `checks` job (`./gradlew detekt`) breaks in Stage 2; mitigated by D-03's "pin to newest that builds, document gap" escape valve |
| A2 | `kotlinx.datetime.Instant` remains a typealias to `kotlin.time.Instant` in 0.8.0 (not just 0.7.1) | Pitfall 4 | If the typealias was dropped in 0.8.0, `ScheduleSlot.kt`/`Agenda.kt` fail to compile on Stage 5; fix is a one-line import change (`kotlinx.datetime.Instant` → `kotlin.time.Instant`) once discovered, low actual risk |
| A3 | The exact KSP release string compatible with whichever final Kotlin version is chosen (2.4.0 or 2.4.20) was not pinned this session | Version Verification (cross-cutting table) | Wrong KSP version could block Apollo codegen or Hilt annotation processing in Stage 1; must be looked up on KSP's GitHub releases page immediately before the Kotlin commit |
| A4 | `com.android.kotlin.multiplatform.library`'s default manifest location for the `android` KMP target is `src/androidMain/AndroidManifest.xml` (making this repo's explicit `sourceSets["main"].manifest.srcFile(...)` override redundant/removable) | Pattern 1 | Low risk — if wrong, simplest fix is to keep the explicit path in whatever the new equivalent DSL location is (`kotlin.android.sourceSets` or similar); worst case, extra 1-line diff |
| A5 | `settings.gradle.kts`/`buildSrc` are viable Gradle Declarative DSL pilot candidates today | DCL support-matrix discussion (see Open Questions) | Medium risk — DCL's own docs explicitly say buildSrc cannot host *custom ecosystem plugins*, but this project's buildSrc has no custom plugin, only a compiled utility object; if DCL still rejects converting buildSrc's own build script, D-08's fallback ("document what's blocked, stay on `.kts`") already covers this outcome |

**If this table is empty:** N/A — see rows above.

## Open Questions

1. **Should this phase target the exact versions REQUIREMENTS.md pins (Kotlin 2.4.0, AGP 9.2.0, Compose BOM 2026.08.00, Apollo 5.0.1), or the newer stable releases verified today (Kotlin 2.4.20, AGP 9.4.0, Compose BOM 2026.09.00, Apollo 5.2.0)?**
   - What we know: All four locked targets are real, valid, still-published stable versions — none are broken or withdrawn. Newer stable releases exist for all four as of 2026-09-17.
   - What's unclear: Whether the exact pins in REQUIREMENTS.md/CONTEXT.md were a deliberate "match this specific number" decision or simply the latest-at-research-time snapshot from 2026-09-12 that the phase inherited.
   - Recommendation: Given D-03 already establishes the philosophy "pin to newest that builds, document the gap" for blockers encountered *during* the bump, the same philosophy plausibly extends to "newest verified-good version at implementation time" for versions that were simply stale at requirements-authoring time — but this is a scope/requirements question, not a technical one, so the planner should surface it explicitly to the user rather than silently deviating from the written requirement text.

2. **Is `buildSrc` or `settings.gradle.kts` the better Gradle Declarative DSL pilot target (D-09)?**
   - What we know: DCL's own documentation explicitly states buildSrc cannot host *new ecosystem/settings plugins* (because buildSrc compiles after settings evaluation) — but this project's buildSrc defines no custom plugin, only a plain Kotlin object (`AndroidSdk`) compiled via the `embedded-kotlin` plugin. `settings.gradle.dcl` conversion is more concretely documented (used today for declaring per-project-type defaults), and this repo's `settings.gradle.kts` is minimal (repository declarations + two `include()` calls + one feature preview flag).
   - What's unclear: Whether DCL has a defined "software type" for a bare `embedded-kotlin`-only buildSrc build script at all, versus `settings.gradle.kts`'s repository/include-based content mapping cleanly onto DCL's documented settings-file patterns.
   - Recommendation: Attempt `settings.gradle.kts` → `settings.gradle.dcl` first (better-documented support surface), fall back to attempting `buildSrc/build.gradle.kts` if that fails, and if both fail, document the specific DCL error/limitation hit for each and keep both on `.kts` per D-08's explicit fallback allowance.

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| `android` CLI | D-04 manual smoke pass (`android run`, `android layout`, `android screen capture`), `android docs search`/`fetch` used throughout this research | ✓ [VERIFIED: `which android` + `android info` this session] | 1.0.16261425 | — |
| JDK 17 | AGP 9.2.0/9.4.0's documented minimum JDK (17) [CITED: AGP 9.2.0 release notes] | ✓ | CI already pins `JAVA_VERSION: 17` via `zulu` distribution [VERIFIED: .github/actions/android-setup/action.yml:8-19] | — |
| Gradle wrapper auto-resolution in CI | Gradle 9.7.1 bump (BUILD-03) needs no separate CI action change | ✓ | `gradle/actions/setup-gradle@v4` is invoked with `gradle-version: wrapper` [VERIFIED: .github/actions/android-setup/action.yml:20-25], so it will use whatever `gradle-wrapper.properties` specifies once bumped | — |
| Xcode / iOS simulator (macOS runner) | D-05's "iOS stays CI-green" requirement | Not probed this session (macOS-only, no local macOS runner available in this research environment) | — | ios.yml already resolves Xcode/simulator dynamically per Phase 1 (`xcrun simctl list`) — no changes expected from this phase's Android-only KMP plugin swap; iOS targets (`iosX64/iosArm64/iosSimulatorArm64`) are untouched by the `android` block migration |

**Missing dependencies with no fallback:** none identified.

**Missing dependencies with fallback:** none — all required tooling for the Android-side verification is present.

## Validation Architecture

### Test Framework

| Property | Value |
|----------|-------|
| Framework | JUnit 4 + Espresso 3.6.1 + Compose UI Test (Android); Kotlin Test (commonTest/jvmTest, KMP) [VERIFIED: gradle/libs.versions.toml `[bundles] test/android-test`, `androidx-test-*` entries] |
| Config file | No dedicated test-framework config file beyond `androidApp/build.gradle.kts`'s `testOptions {}` block and `gradle/libs.versions.toml` |
| Quick run command | `./gradlew testDebugUnitTest` (Android unit tests) / `./gradlew :shared:jvmTest` (KMP commonTest via JVM target) |
| Full suite command | `./gradlew detekt lint testDebugUnitTest connectedDebugAndroidTest assembleDebug` (mirrors `.github/workflows/android.yml`'s 4 jobs) plus `./gradlew :shared:embedAndSignAppleFrameworkForXcode` + Xcode build (mirrors `ios.yml`) |

### Phase Requirements → Test Map

| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|---------------------|--------------|
| BUILD-01 | Project compiles on Kotlin 2.4.0(.20) | smoke (compile) | `./gradlew compileKotlin compileDebugKotlin` | ✅ (existing Gradle task, no new file) |
| BUILD-02 | Project builds on AGP 9.2.0(.4.0) + new KMP plugin; app runs unchanged | smoke + manual | `./gradlew :shared:assemble :androidApp:assembleDebug` + `android run` + `android layout`/`screen capture` per D-04 | ✅ existing tasks; manual smoke pass is new (no test file, by design per D-04) |
| BUILD-03 | Gradle 9.7.1 | smoke | `./gradlew --version` (confirms wrapper resolution) + full CI run | ✅ |
| BUILD-04 | Compose BOM 2026.08.00(.09.00) — UI unchanged | smoke + existing Compose UI tests | `./gradlew testDebugUnitTest connectedDebugAndroidTest` (existing Compose UI Test suite, if any exists under `androidApp/src/androidTest`) | Existing test dir — planner should confirm actual Compose UI test coverage exists; TEST-04 (Phase 5) is where this gets expanded, not this phase |
| BUILD-05 | Apollo 5.0.1(.2.0) — cache functional (D-07) | manual functional check | Repeated query hit-cache check + airplane-mode check, per D-07 (explicitly not automated in this phase's scope — no before/after data-preservation test per D-06) | ❌ — D-07 explicitly scopes this as manual/functional-only, not a new automated test |
| BUILD-06 | Firebase/Coroutines/serialization/datetime bumped, no behavior change | smoke (compile) + existing unit tests | `./gradlew testDebugUnitTest :shared:jvmTest` — specifically re-run any existing test touching `ScheduleSlot`/`Agenda` given the Instant/Clock pitfall | Check whether `DevFestNantesStoreContractTest`/commonTest already covers `ScheduleSlot`/`Agenda` date parsing — if not, Wave 0 gap below |
| BUILD-07 | DCL pilot on one module, rest documented `.kts` | manual (build succeeds) | `./gradlew :buildSrc:build` or equivalent for whichever pilot target is chosen | ❌ — DCL pilot has no dedicated automated test; success criterion is "the build still works after conversion" |

### Sampling Rate
- **Per task commit:** relevant quick-run command from the table above for that stage.
- **Per wave merge:** full suite command (mirrors both `android.yml` and `ios.yml` job sets).
- **Phase gate:** Full CI (both workflows) green before `/gsd-verify-work`, per D-02.

### Wave 0 Gaps
- [ ] Confirm whether `shared/src/commonTest` already has a test exercising `ScheduleSlot.startDateAsEpochMilliseconds`/`Agenda.Builder.build()` date-parsing logic — if not, add one covering the exact `Instant.parse(...).toEpochMilliseconds()`/`.minus(...).inWholeDays` code paths *before* Stage 5's kotlinx-datetime bump, so the bump's local sanity build (D-02) has a real regression signal rather than relying purely on "does it compile."
- [ ] No dedicated Apollo cache-behavior test currently exists per `.planning/codebase/TESTING.md`/`ARCHITECTURE.md` (not read in full this session) — D-07 explicitly scopes this to a manual functional check, so this is an accepted gap, not an oversight, but the planner should confirm no such test is silently expected elsewhere in the codebase.

*(If no gaps found: N/A — see checkboxes above.)*

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-------------------|
| V2 Authentication | No | This phase touches no auth surface |
| V3 Session Management | No | N/A |
| V4 Access Control | No | N/A |
| V5 Input Validation | No | This phase changes build tooling and dependency versions, not input-handling code |
| V6 Cryptography | No | N/A |
| V14 Configuration (build/dependency security) | Yes | Verify every version bump comes from the official Maven Central / Google Maven / Gradle Plugin Portal coordinate, not a typosquatted group — already done for the one genuinely new coordinate (`com.apollographql.cache`) in the Package Legitimacy Audit above |

### Known Threat Patterns for this stack

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|----------------------|
| Dependency-coordinate confusion during a package-group migration (e.g., mistyping `com.apollographql.cache` as a similar-looking unofficial group) | Tampering / Spoofing | Verify group+artifact against the official docs page and Maven Central metadata before adding to `libs.versions.toml` — done in this research for the Apollo cache packages |
| Pinning CI static-analysis (Detekt) to an unstable pre-1.0 alpha release | Tampering (supply-chain quality, not malice) | `checkpoint:human-verify` before relying on `dev.detekt` 2.0.0-alpha.x in CI; treat findings from this Detekt version as advisory until it reaches a stable release, per Package Legitimacy Audit |

## Sources

### Primary (HIGH confidence)
- `android docs fetch kb://android/build/releases/agp-9-2-0-release-notes` / `agp-9-4-0-release-notes` — official AGP release notes (Gradle/JDK/SDK-Build-Tools compatibility matrix, R8 changes)
- `android docs fetch kb://android/kotlin/multiplatform/plugin` — official Android docs for `com.android.kotlin.multiplatform.library` (features, migration guide, known issues, prerequisites)
- Direct Maven Central / Google Maven `maven-metadata.xml` fetches (2026-09-17) for: `kotlin-gradle-plugin`, `com.android.tools.build:gradle`, `kotlinx-coroutines-core`, `kotlinx-serialization-json`, `kotlinx-datetime`, `firebase-bom`, `google-services`, `firebase-crashlytics-gradle`, `perf-plugin`, `hilt-android`, `com.google.devtools.ksp.gradle.plugin`, `com.apollographql.apollo:apollo-runtime`, `com.apollographql.cache:normalized-cache`, `androidx.compose:compose-bom`
- This repo's own source files, read directly this session: `shared/build.gradle.kts`, `androidApp/build.gradle.kts`, `settings.gradle.kts`, `gradle/libs.versions.toml`, `gradle/wrapper/gradle-wrapper.properties`, `buildSrc/build.gradle.kts`, `buildSrc/src/main/java/Dependencies.kt`, `build.gradle.kts`, `gradle.properties`, `.github/workflows/android.yml`, `.github/workflows/ios.yml`, `.github/actions/android-setup/action.yml`, `shared/src/commonMain/kotlin/com/gdgnantes/devfest/model/{ScheduleSlot,Agenda}.kt`, `shared/src/androidMain/AndroidManifest.xml`

### Secondary (MEDIUM confidence)
- `android docs fetch` of JetBrains' `kotlin-agent-skills/skills/kotlin-tooling-agp9-migration` reference set (VERSION-MATRIX, KNOWN-ISSUES, PLUGIN-COMPATIBILITY, MIGRATION-LIBRARY) — third-party-authored but distributed as an official Android-Studio-recommended agent skill and cross-referenced against Google/JetBrains sources
- Apollo GraphQL official docs (`apollographql.com/docs/kotlin/v5/...`) via WebFetch — official first-party documentation, but version numbers within it were sparse, supplemented by Maven Central metadata
- Gradle's own blog/newsletter content on Declarative Gradle status (`blog.gradle.org`) via WebSearch

### Tertiary (LOW confidence)
- WebSearch synthesis of GitHub issues/discussions for Detekt 2.0.0-alpha's AGP 9 fix timeline and the `dev.detekt` plugin-id rename — no single official detekt.dev announcement page was fetched directly; treat the specific alpha version number as needing re-confirmation immediately before use
- WebSearch synthesis for the exact `com.apollographql.cache` version (1.0.8) pairing with Apollo runtime 5.x — corroborated by direct Maven Central metadata fetch (a primary source), but the *pairing* claim (which cache version goes with which apollo-runtime version) rests on WebSearch synthesis, not an explicit compatibility table

## Metadata

**Confidence breakdown:**
- Standard stack (version numbers): HIGH — every number directly verified against Maven Central/Google Maven metadata on 2026-09-17, not training-data recall
- Architecture (AGP9/KMP plugin migration mechanics): HIGH — official Android docs plus direct comparison against this repo's actual current `shared/build.gradle.kts` content
- Apollo cache migration mechanics: MEDIUM — official Apollo docs for the mechanics, but the exact `1.0.8` version pairing with apollo-runtime 5.x was WebSearch-synthesized, not found stated explicitly on one authoritative page
- Detekt/Hilt/Firebase-perf AGP9-forced-bump pitfalls: MEDIUM-HIGH — the compatibility floors themselves are CITED from a maintained third-party AGP9-migration reference; the exact Detekt alpha number needing confirmation is the one LOW-confidence sub-claim, clearly flagged
- Pitfalls (kotlinx-datetime Instant/Clock): HIGH for "this project uses `kotlinx.datetime.Instant` directly" (verified by reading the actual files); MEDIUM for "0.8.0 still typealiases it" (not confirmed this session)
- Gradle Declarative DSL support matrix: MEDIUM — official Gradle blog/docs content, but current as of April 2025's EAP3 with no confirmed EAP4/GA update found for 2026; buildSrc/settings.gradle.kts pilot suitability is genuinely uncertain and flagged as an Open Question rather than a firm recommendation

**Research date:** 2026-09-17
**Valid until:** 7 days (this is an explicitly fast-moving toolchain area — STATE.md's own note said a 5-day-old prior research pass was already stale in 4 of 9 pinned versions; re-verify immediately before each staged commit, not just once at plan time)
