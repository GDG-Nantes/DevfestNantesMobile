# Phase 2: Dependency & Build Tooling Upgrade - Pattern Map

**Mapped:** 2026-09-17
**Files analyzed:** 10 (all existing files to be modified in place — this phase creates zero new files except one new-format DCL pilot file)
**Analogs found:** 10 / 10 (this phase modifies existing files rather than creating new ones; each file's own current content plus one cross-cutting analog is its own best "pattern source" — see below)

**Framing note:** unlike a typical feature phase, Phase 2 has almost no *new* files — its unit of work is staged edits to existing build/config/source files. Consequently "closest analog" below means: (a) for build files being migrated, the official-migration-guide shape already captured in RESEARCH.md's Pattern 1/2, cross-checked against this repo's own current file (the truest same-repo analog is the file's own pre-migration content); (b) for the one net-new artifact (the DCL pilot file, `settings.gradle.dcl`), the closest analog is the file it replaces (`settings.gradle.kts`) since DCL has no prior instance in this repo to copy from.

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|---|---|---|---|---|
| `gradle/libs.versions.toml` | config | batch (version catalog) | itself (current content, edited per stage) | exact |
| `shared/build.gradle.kts` | config | transform (build-graph config) | itself (current content) + `androidApp/build.gradle.kts` for detekt-block sibling pattern | exact |
| `androidApp/build.gradle.kts` | config | transform (build-graph config) | itself (current content) + `shared/build.gradle.kts` for detekt-block sibling pattern | exact |
| `build.gradle.kts` (root) | config | transform (plugin-alias registry) | itself (current content) | exact |
| `settings.gradle.kts` → `settings.gradle.dcl` (DCL pilot, D-08/D-09) | config | transform (project-topology declaration) | `settings.gradle.kts` (its own pre-migration content — no existing DCL file in repo) | role-match (no true DCL analog exists in-repo; RESEARCH.md Open Question #2 already picks this as the primary pilot candidate) |
| `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/Apollo.kt` | service | request-response (Apollo client config) | itself (current content) | exact |
| `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/ApolloCache.kt` | service | file-I/O (normalized cache factory, SQLite-backed) | itself (current content) | exact |
| `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/GraphQLStore.kt` | service | request-response + streaming (Flow-based CacheAndNetwork queries) | itself (current content) | exact |
| `shared/src/commonMain/kotlin/com/gdgnantes/devfest/model/ScheduleSlot.kt` | model | transform (date parsing) | `Agenda.kt` (same `kotlinx.datetime.Instant` usage pattern, same package) | exact |
| `shared/src/commonMain/kotlin/com/gdgnantes/devfest/model/Agenda.kt` | model | transform (date parsing) | `ScheduleSlot.kt` (same `kotlinx.datetime.Instant` usage pattern, same package) | exact |
| `shared/src/commonTest/kotlin/com/gdgnantes/devfest/store/DevFestNantesStoreContractTest.kt` (Wave 0 gap: possibly extended with a date-parsing test before Stage 5) | test | request-response (contract test over `DevFestNantesStore` interface) | itself (current content) — this is the only test file in scope | exact |

No genuinely new source files are created by this phase's requirements (BUILD-01..07); every "file" in scope is an edit to an existing tracked file. The one exception — `settings.gradle.dcl` — is a new file but its content is a format-conversion of `settings.gradle.kts`, not a new pattern with an unrelated analog.

## Pattern Assignments

### `shared/build.gradle.kts` (config, transform) — Stage 2 target (BUILD-02)

**Analog:** itself, pre-migration (this is the primary migration target — RESEARCH.md's Pattern 1 already contains the full before/after diff verified against this exact file)

**Current plugins block** (lines 3-11):
```kotlin
plugins {
    alias(libs.plugins.detekt)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)   // forbidden coexistence in AGP 9+
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kmp.native.coroutines)
    alias(libs.plugins.appollo)
}
```

**Current `android {}` block to migrate into `kotlin { android {} }`** (lines 77-89):
```kotlin
android {
    compileSdk = AndroidSdk.compile
    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    defaultConfig {
        minSdk = AndroidSdk.min
    }
    namespace = "com.gdgnantes.devfest"

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}
```

**Detekt config block, unify with `androidApp`'s equivalent** (lines 13-25):
```kotlin
detekt {
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = false
    config.setFrom("$rootDir/linters/detekt-config.yml")

    source.setFrom(
        "src/commonMain/kotlin",
        "src/androidMain/kotlin",
        "src/iosMain/kotlin",
    )
}
```

**Apollo compiler-plugin registration to extend for the cache-package migration (Stage 4)** (lines 71-75):
```kotlin
apollo {
    service("service") {
        packageName.set("com.gdgnantes.devfest.graphql")
        // Stage 4 adds: plugin("com.apollographql.cache:normalized-cache-apollo-compiler-plugin:1.0.8")
        //               pluginArgument("com.apollographql.cache.packageName", packageName.get())
    }
}
```

**Migration target shape:** see RESEARCH.md "Pattern 1: AGP 9 KMP-library plugin swap" for the full verified before/after — that section already reflects this file's exact current content, so it is the authoritative diff, not a generic guide excerpt.

---

### `androidApp/build.gradle.kts` (config, transform) — Stages 2, 3, 5 target (BUILD-02, BUILD-04, BUILD-06)

**Analog:** itself, pre-migration; sibling detekt block in `shared/build.gradle.kts` for consistency

**Detekt config + jvmTarget wiring — unify plugin coordinate here when bumping to `dev.detekt`** (lines 18-29):
```kotlin
detekt {
    buildUponDefaultConfig = true
    allRules = false
    autoCorrect = false
    config.setFrom("$rootDir/linters/detekt-config.yml")
}
tasks.withType<Detekt>().configureEach {
    jvmTarget = "1.8"
}
tasks.withType<DetektCreateBaselineTask>().configureEach {
    jvmTarget = "1.8"
}
```
Note the existing `jvmTarget = "1.8"` inconsistency with `shared/build.gradle.kts`'s `JvmTarget.JVM_11` — RESEARCH.md flags unifying JVM targets during the AGP 9 migration; do not silently "fix" this beyond what's needed for the plugin swap unless a build failure forces it (scope discipline per D-01).

**Packaging DSL — exact syntax fix required regardless of KMP plugin swap (Pitfall 5)** (lines 82-86):
```kotlin
// BEFORE
packagingOptions {
    resources {
        excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}
// AFTER (RESEARCH.md Code Examples section — exact fix)
packaging {
    resources {
        excludes.add("/META-INF/AL2.0")
        excludes.add("/META-INF/LGPL2.1")
    }
}
```

**Hilt/Firebase-perf plugin aliases needing version-floor bumps in the same Stage-2 commit** (lines 4-16, plugin block):
```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.detekt)             // -> dev.detekt 2.0.0-alpha.x
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)                // bumped in Stage 1, not here
    alias(libs.plugins.google.services)
    alias(libs.plugins.dagger.hilt)        // dagger = "2.57.2" -> "2.60.1"
    alias(libs.plugins.crashlytics)
    alias(libs.plugins.firebase.perf)      // firebasePerfPlugin = "2.0.1" -> "2.0.2"
    alias(libs.plugins.secrets)
}
```

**Compose BOM platform pin — Stage 3 target** (line 106):
```kotlin
implementation(platform(libs.androidx.compose.bom))
```
No structural change needed — only `gradle/libs.versions.toml`'s `composeBom` version bumps.

**Firebase BOM platform pin — Stage 5 target** (line 127):
```kotlin
implementation(platform(libs.firebase.bom))
```
No structural change needed — only `gradle/libs.versions.toml`'s `firebaseBom` version bumps.

---

### `gradle/libs.versions.toml` (config, batch) — every stage touches this file

**Analog:** itself — single source of truth, edited incrementally per D-01's staged commits

**Version catalog entries to bump per stage** (exact current lines, for diffing):
```toml
kotlin = "2.2.0"                              # Stage 1 -> 2.4.0 (verify 2.4.20 per Open Q1)
ksp = "2.2.20-2.0.3"                          # Stage 1 -> paired with final Kotlin version, >= 2.3.1
agp = "8.13.0"                                 # Stage 2 -> 9.2.0 (verify 9.4.0 per Open Q1)
detekt = "1.23.8"                              # Stage 2 -> 2.0.0-alpha.x (plugin id also changes)
dagger = "2.57.2"                              # Stage 2 -> 2.60.1 (floor 2.59)
firebasePerfPlugin = "2.0.1"                   # Stage 2 -> 2.0.2
composeBom = "2025.09.01"                      # Stage 3 -> 2026.08.00 (verify 2026.09.00 per Open Q1)
appollo = "4.3.3"                              # Stage 4 -> 5.0.1 (verify 5.2.0 per Open Q1)
firebaseBom = "33.16.0"                        # Stage 5 -> 34.19.0
kotlinxCoroutines = "1.10.2"                   # Stage 5 -> 1.11.0
kotlinxSerialization = "1.9.0"                 # Stage 5 -> 1.11.0
kotlinxDatetime = "0.6.2"                      # Stage 5 -> 0.8.0
googleServicesPlugin = "4.4.3"                 # Stage 5 (optional) -> 4.5.0
firebaseCrashlyticsPlugin = "3.0.6"            # Stage 5 (optional) -> 3.0.8
```

**Apollo cache library group change — Stage 4, not a version-only bump** (current lines):
```toml
[libraries]
appollo-normalized-cache = { group = "com.apollographql.apollo", name = "apollo-normalized-cache", version.ref = "appollo" }
appollo-normalized-cache-sqlite = { group = "com.apollographql.apollo", name = "apollo-normalized-cache-sqlite", version.ref = "appollo" }
```
Must become (new version key + new group, per RESEARCH.md Pattern 2 / Pitfall 3):
```toml
[versions]
appolloCache = "1.0.8"
[libraries]
appollo-normalized-cache = { group = "com.apollographql.cache", name = "normalized-cache", version.ref = "appolloCache" }
appollo-normalized-cache-sqlite = { group = "com.apollographql.cache", name = "normalized-cache-sqlite", version.ref = "appolloCache" }
```

**Detekt plugin id rename — Stage 2** (current line):
```toml
[plugins]
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
```
becomes `id = "dev.detekt"` per RESEARCH.md Pitfall 1.

---

### `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/Apollo.kt` + `ApolloCache.kt` + `GraphQLStore.kt` (service, request-response/streaming) — Stage 4 target (BUILD-05)

**Analog:** each other (all three files are the complete Apollo integration surface in this repo — no other GraphQL/network service file exists to compare against, so they are mutually the closest analogs and must migrate together as one functional unit)

**Import pattern to update (all three files use the deprecated group)**:
```kotlin
// Apollo.kt (lines 1-12) — imports the cache-normalized API from the OLD group
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.CompiledField
import com.apollographql.apollo.api.Executable
import com.apollographql.apollo.api.http.HttpHeader
import com.apollographql.apollo.cache.normalized.api.CacheKey
import com.apollographql.apollo.cache.normalized.api.CacheKeyGenerator
import com.apollographql.apollo.cache.normalized.api.CacheKeyGeneratorContext
import com.apollographql.apollo.cache.normalized.api.CacheResolver
import com.apollographql.apollo.cache.normalized.api.DefaultCacheResolver
import com.apollographql.apollo.cache.normalized.normalizedCache
```
```kotlin
// ApolloCache.kt (lines 1-5) — the NormalizedCacheFactory imports move wholesale to com.apollographql.cache
import com.apollographql.apollo.cache.normalized.api.MemoryCacheFactory
import com.apollographql.apollo.cache.normalized.api.NormalizedCacheFactory
import com.apollographql.apollo.cache.normalized.sql.SqlNormalizedCacheFactory
```
Per RESEARCH.md Pattern 2/Pitfall 3, these become `com.apollographql.cache.*` equivalents (exact new import paths to confirm against `apollographql.com/docs/kotlin/v5/caching/migration-guide` at implementation time — RESEARCH.md flags this as the bulk of Stage 4's work).

**Core cache-config pattern to preserve unchanged in shape (`ApolloCache.kt`, lines 7-11)**:
```kotlin
val normalizedCache: NormalizedCacheFactory =
    MemoryCacheFactory(10 * 1024 * 1024)
        .chain(
            SqlNormalizedCacheFactory("apollo.db")
        )
```
This memory-then-SQLite chain pattern is the project's one cache-construction idiom — keep the `.chain()` composition shape when swapping factory import origins.

**Error-handling pattern already established in `GraphQLStore.kt` — do NOT introduce new error handling here, this phase is build-tooling only** (repeated 6x across the file, e.g. lines 41-45, 61-65, 76-80):
```kotlin
.map { response ->
    if (response.exception != null) {
        println("Apollo error: ${response.exception}")
        return@map emptyMap()
    }
    ...
}
```
Note: `ARCHITECTURE.md` (per CONTEXT.md canonical refs) already flags `println` for GraphQL errors as a known anti-pattern — but fixing it is explicitly out of scope for Phase 2 (no behavior change, build-tooling only). Do not "clean this up" as a drive-by while touching these files for the Apollo 5 migration; only touch what the type/API rename forces.

**Fetch-policy / Flow-streaming pattern (repeated 4x, e.g. lines 36-58)**:
```kotlin
override val partners: Flow<Map<PartnerCategory, List<Partner>>>
    get() =
        apolloClient.query(GetPartnerGroupsQuery())
            .fetchPolicy(FetchPolicy.CacheAndNetwork)
            .toFlow()
            .map { response -> /* ... */ }
            .catch { e -> println(e.message) }
```
Confirm this compiles unchanged post-migration since `FetchPolicy`/`.toFlow()` are part of the Apollo runtime API (not the cache package) — the cache-package rename should only affect `ApolloCache.kt`'s factory imports and `Apollo.kt`'s cache-key/resolver imports, not this query-building shape.

**D-07 functional verification target:** the `CacheAndNetwork` fetch policy above is exactly the mechanism D-07 wants manually confirmed post-migration (repeated query hits cache; airplane-mode shows last-loaded data) — no new test file, per RESEARCH.md's Validation Architecture table (BUILD-05 row: manual functional check, not automated).

---

### `shared/src/commonMain/kotlin/com/gdgnantes/devfest/model/ScheduleSlot.kt` + `Agenda.kt` (model, transform) — Stage 5 target (BUILD-06, kotlinx-datetime pitfall)

**Analog:** each other — identical `kotlinx.datetime.Instant` import/usage idiom, same package, same risk surface

**Current import + usage (`ScheduleSlot.kt`, lines 1-10)**:
```kotlin
package com.gdgnantes.devfest.model

import kotlinx.datetime.Instant

data class ScheduleSlot(
    val endDate: String = "",
    val startDate: String = ""
) : Comparable<ScheduleSlot> {
    val startDateAsEpochMilliseconds = Instant.parse(startDate).toEpochMilliseconds()
    val endDateAsEpochMilliseconds = Instant.parse(endDate).toEpochMilliseconds()
    ...
```

**Current import + usage (`Agenda.kt`, lines 1-3, 14, 33-34)**:
```kotlin
import kotlinx.datetime.Instant
...
val startInstant = session.scheduleSlot.startDate.let(Instant::parse)
if (startInstant.minus(DAY_ONE).inWholeDays == 0L) { ... }
...
val DAY_ONE = Instant.parse(DAY_ONE_ISO)
val DAY_TWO = Instant.parse(DAY_TWO_ISO)
```

**Migration action per RESEARCH.md Pitfall 4:** after bumping `kotlinxDatetime` to `0.8.0`, do a full `shared` compile and specifically watch these two files. If `kotlinx.datetime.Instant` no longer typealiases to `kotlin.time.Instant` in 0.8.0 (unconfirmed — see Assumption A2), the fix is a one-line import swap in each file: `import kotlinx.datetime.Instant` → `import kotlin.time.Instant`. `.parse()`, `.toEpochMilliseconds()`, and `.minus(Instant): Duration` signatures are unchanged on `kotlin.time.Instant` per RESEARCH.md, so no call-site changes beyond the import are expected.

---

### `shared/src/commonTest/kotlin/com/gdgnantes/devfest/store/DevFestNantesStoreContractTest.kt` (test, request-response) — Wave 0 gap candidate before Stage 5

**Analog:** itself — this is the only commonTest file exercising `DevFestNantesStore`, and the only place a new date-parsing regression test (per RESEARCH.md's Wave 0 Gaps checklist item) would naturally live

**Existing test structure to extend, not replace** (lines 9-16):
```kotlin
class DevFestNantesStoreContractTest {
    private val store: DevFestNantesStore = DevFestNantesStoreMocked()

    @Test
    fun agenda_flow_emits_agenda() = runTest {
        val agenda = store.agenda.first()
        assertNotNull(agenda)
    }
    ...
```
If the planner decides to close RESEARCH.md's Wave 0 gap ("confirm `ScheduleSlot`/`Agenda` date-parsing has real test coverage before the kotlinx-datetime bump"), the closest-pattern extension is a new `@Test fun` in this same class (or a small sibling test class in the same package) following this file's `runTest { ... assertNotNull(...) }` idiom — not a new test framework or assertion style. `store.agenda.first()` already exercises `Agenda.Builder().build()`'s `Instant.parse`/`.minus()` code path indirectly (via `DevFestNantesStoreMocked`'s `sessions` flow), so this may already partially satisfy the gap — planner should check `DevFestNantesStoreMocked.kt`'s `agenda`/`sessions` implementation before deciding a new test is needed.

---

### `settings.gradle.kts` → `settings.gradle.dcl` (config, transform) — Stage 6 pilot target (BUILD-07, D-08/D-09)

**Analog:** its own current `.kts` content — no DCL file exists anywhere in this repo to copy from; RESEARCH.md's Open Question #2 already picked this file as the primary pilot candidate over `buildSrc`

**Current full content** (all 24 lines):
```kotlin
@file:Suppress("UnstableApiUsage")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    //repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "DevFest_Nantes"
include(":androidApp")
include(":shared")
```
This is a minimal, purely declarative file (repository declarations + two `include()` calls + one feature-preview flag, no imperative logic) — exactly the shape DCL's settings-file support targets best per RESEARCH.md. Convert to `settings.gradle.dcl` using Gradle's documented settings-file DCL schema; if the `enableFeaturePreview`/`@file:Suppress` constructs have no DCL equivalent, document that specific blocker in STATE.md per D-08's "document what's blocked" fallback and keep `settings.gradle.kts` as-is. Fall back to `buildSrc/build.gradle.kts` (below) only if this attempt fails outright.

**Fallback candidate — `buildSrc/build.gradle.kts` (all 4 lines, for reference if Stage 6's primary attempt fails)**:
```kotlin
plugins {
    `embedded-kotlin`
}
```

---

## Shared Patterns

### Version-catalog-first configuration
**Source:** `gradle/libs.versions.toml`
**Apply to:** every stage's version bump (Stages 1-5) and the two forced-plugin-id changes (Detekt, Apollo cache group)
Every dependency change in this phase is expressed as a `[versions]`/`[libraries]`/`[plugins]` catalog edit, never a hardcoded version string in a module's `build.gradle.kts`. Both `shared/build.gradle.kts` and `androidApp/build.gradle.kts` already consume everything via `libs.*` accessors (`alias(libs.plugins.X)`, `implementation(libs.Y)`) — preserve this discipline for every new/changed dependency, including the Apollo cache package's new `com.apollographql.cache` group and the Detekt `dev.detekt` plugin-id rename.

### One-change-per-commit, green-CI-checkpoint discipline
**Source:** Phase 1 pattern, referenced in `02-CONTEXT.md` D-01/D-02 (established in `01-CONTEXT.md`, not a code file — process pattern)
**Apply to:** all six staging commits
Each of the six stages (Kotlin → AGP+KMP plugin → Compose BOM → Apollo → Firebase/Coroutines/serialization/datetime → DCL pilot) is its own commit; "green" = full CI (`.github/workflows/android.yml` + `ios.yml`) passing on the pushed commit AND one local sanity build/run. Do not combine two stages' version bumps into one commit even if convenient — this was an explicit, deliberate decision (D-01) that a plan violating it would need to flag back to the user.

### Detekt dual-module consistency
**Source:** `shared/build.gradle.kts` lines 13-25 and `androidApp/build.gradle.kts` lines 18-29
**Apply to:** the Stage 2 Detekt version/plugin-id bump
Both modules configure Detekt independently (`buildUponDefaultConfig`, `allRules = false`, `autoCorrect = false`, same `linters/detekt-config.yml` config file) but `shared` additionally sets `source.setFrom(...)` for its multiplatform source sets while `androidApp` additionally pins `jvmTarget` on the `Detekt`/`DetektCreateBaselineTask` task types. When bumping the plugin id to `dev.detekt`, check both configuration shapes still apply — the 2.0.0-alpha DSL may consolidate or rename these knobs (RESEARCH.md flags this as needing live-build confirmation, not assumed-safe).

### CI checkpoint plumbing (unaffected by version bumps, verify only)
**Source:** `.github/actions/android-setup/action.yml`, `.github/workflows/android.yml`
**Apply to:** every stage's "CI green" checkpoint (D-02)
The composite `android-setup` action already pins JDK 17 (`zulu`) and uses `gradle/actions/setup-gradle@v4` with `gradle-version: wrapper`, so the Gradle 9.7.1 bump (BUILD-03) needs zero CI-file edits — only `gradle/wrapper/gradle-wrapper.properties` changes. `android.yml`'s four jobs (`checks` → detekt+lint, `unit-tests`, `instrumentation-tests`, `build-debug`) are the exact "full CI" surface D-02's checkpoint refers to; no new CI job is added by this phase.

## No Analog Found

None — every file in this phase's scope is either an edit to an existing tracked file (with itself as the truest before/after analog) or, in the one net-new-file case (`settings.gradle.dcl`), has an explicit same-repo predecessor file (`settings.gradle.kts`) to convert from, per D-08/D-09's own framing ("pilot on one existing low-risk module," not "invent a new file from scratch").

## Metadata

**Analog search scope:** repo root, `shared/`, `androidApp/`, `buildSrc/`, `gradle/`, `.github/actions/`, `.github/workflows/` — full-file reads (all files ≤175 lines; no Grep-then-offset needed)
**Files scanned:** `gradle/libs.versions.toml`, `shared/build.gradle.kts`, `androidApp/build.gradle.kts`, `build.gradle.kts` (root), `settings.gradle.kts`, `buildSrc/build.gradle.kts`, `shared/src/commonMain/kotlin/com/gdgnantes/devfest/model/{ScheduleSlot,Agenda}.kt`, `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/{Apollo,ApolloCache,GraphQLStore}.kt`, `shared/src/commonTest/kotlin/com/gdgnantes/devfest/store/DevFestNantesStoreContractTest.kt`, `.github/workflows/android.yml`, `.github/actions/android-setup/action.yml` — all confirmed git-tracked via `git ls-files`
**Pattern extraction date:** 2026-09-17
