---
phase: 03-multi-module-architecture-extraction
plan: 04
subsystem: build-system
tags: [gradle, build-logic, convention-plugins, agp9, hilt, kmp, detekt]

# Dependency graph
requires:
  - phase: 03-multi-module-architecture-extraction (plan 03)
    provides: ":core:model, :core:network, :core:analytics, :core:data modules; build-logic with devfest.detekt + devfest.kmp.library"
provides:
  - ":core:testing KMP module (D-19) exposing core:model, core:data, kotlin-test, kotlinx-coroutines-test as api, plus fakeDevFestNantesStore()"
  - "Four new build-logic convention plugins: devfest.android.library, devfest.android.hilt, devfest.android.feature, devfest.android.application (D-13)"
  - "Project.configureAndroidCommon() and Project.configureDependencyResolutionRules() shared helpers"
  - "androidApp/build.gradle.kts slimmed to app-only configuration, adopting devfest.android.application + devfest.android.hilt"
affects: ["03-05", "03-06", "03-07", "03-08", "03-09"]

# Actuals (#2632) — pairs with the plan's `estimate` to calibrate future estimates.
actuals:
  tokens: 6413
  tasks: 2
  commits: 2

tech-stack:
  added: []
  patterns:
    - "Android convention plugins configure AGP's CommonExtension by direct property access (commonExtension.defaultConfig.minSdk = ..., commonExtension.compileOptions.sourceCompatibility = ...), not the nested-block DSL sugar (defaultConfig { ... }) — that sugar is a Gradle Kotlin DSL *script*-only synthetic accessor and does not exist on ordinary Kotlin plugin source. AGP 9's CommonExtension is non-generic (unlike AGP 8's CommonExtension<...>), so a single configureAndroidCommon(commonExtension: CommonExtension) works for both Library and Application extensions; targetSdk (application-only) is set separately via ApplicationExtension's own defaultConfig(action) block function, which genuinely exists on that concrete subtype."
    - "devfest.android.feature is the one-line build file every future :feature:* module applies (library + hilt + core module deps); it must never gain a :feature:* project dependency (ARCH-02)."

key-files:
  created:
    - core/testing/build.gradle.kts
    - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/FakeStores.kt
    - core/testing/src/commonTest/kotlin/com/gdgnantes/devfest/core/testing/FakeStoresTest.kt
    - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidCommon.kt
    - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/DependencyResolutionRules.kt
    - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidLibraryConventionPlugin.kt
    - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidHiltConventionPlugin.kt
    - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidFeatureConventionPlugin.kt
    - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidApplicationConventionPlugin.kt
  modified:
    - settings.gradle.dcl
    - androidApp/build.gradle.kts
    - build-logic/convention/build.gradle.kts
    - build.gradle.kts
    - gradle/libs.versions.toml

key-decisions:
  - ":core:testing's fakeDevFestNantesStore() wraps the public DevFestNantesStoreBuilder().setUseMockServer(true).build() instead of instantiating DevFestNantesStoreMocked() directly (the plan's literal example) — DevFestNantesStoreMocked is `internal` in :core:data and is not visible across the module boundary from :core:testing. This is exactly the design :core:data already established (03-03 verified DevFestNantesStoreBuilder as the production mock-server entry point) and is fully consistent with 03-CONTEXT.md's D-19 note that the existing fakes stay in production modules."
  - "The firebase-auth-ktx -> firebase-auth:24.2.0 substitution and kotlin-metadata-jvm force now live in DependencyResolutionRules.kt (build-logic), verbatim with their original comments — discharges the STATE.md follow-up note that these were version-coupled to firebaseBom 34.19.0 and needed re-verification alongside any future bump."

requirements-completed: []  # ARCH-01/ARCH-02 span the whole phase; requirements.ready-ids reports both still blocked by sibling plans 03-05..03-09 (not yet summarized) — NOT marked complete here, per phase convention

coverage:
  - id: D1
    description: ":core:testing exists as a KMP module (D-19), api-exposes core:model/core:data/kotlin-test/kotlinx-coroutines-test, and its FakeStoresTest passes"
    requirement: "ARCH-02"
    verification:
      - kind: unit
        ref: "core/testing/build/test-results/jvmTest — FakeStoresTest.fakeDevFestNantesStore_sessions_not_empty, 1 test, 0 failures"
        status: pass
      - kind: other
        ref: "git grep -n 'project(\":core:testing\")' -- '*.gradle.kts' shows no non-test-scoped usage (no consumer exists yet)"
        status: pass
    human_judgment: false
  - id: D2
    description: "build-logic registers devfest.android.library, devfest.android.hilt, devfest.android.feature, devfest.android.application; androidApp adopts application+hilt and keeps only app-specific configuration (ARCH-01)"
    requirement: "ARCH-01"
    verification:
      - kind: other
        ref: "grep -vE '^\\s*//' androidApp/build.gradle.kts contains none of compileSdk/minSdk/targetSdk/compileOptions/testOptions/'detekt {'/configureEach/AndroidSdk. and contains id(\"devfest.android.application\"), id(\"devfest.android.hilt\"), namespace, versionCode = 37"
        status: pass
      - kind: other
        ref: "AndroidFeatureConventionPlugin.kt contains project(\":core:ui\") and no :feature: string"
        status: pass
    human_judgment: false
  - id: D3
    description: "androidApp's shipped output is unchanged: releaseRuntimeClasspath dependency set and merged release AndroidManifest are equivalent before and after adopting the convention plugins"
    requirement: "ARCH-01"
    verification:
      - kind: other
        ref: "normalized sorted-set diff of ./gradlew :androidApp:dependencies --configuration releaseRuntimeClasspath before vs after -> empty diff (DEPS-IDENTICAL)"
        status: pass
      - kind: other
        ref: "canonicalized-XML structural diff (sorted attributes + children) of every activity/activity-alias/service/receiver/provider/permission element in the merged release AndroidManifest.xml before vs after -> SEMANTIC-MATCH (30/30 elements identical); the raw byte diff shows one <activity> (androidx.compose.ui.tooling.PreviewActivity) repositioned by manifest-merger's dependency-graph traversal order, same element/attributes/count — see Deviations"
        status: pass
    human_judgment: false
  - id: D4
    description: "Every commit builds green and both CI workflows (android.yml, ios.yml) are green on the pushed HEAD (ARCH-02 concurrency edge, D-18)"
    requirement: "ARCH-02"
    verification:
      - kind: e2e
        ref: "gh run watch — Android run 36037788882, iOS run 36037788851, both on commit 9b86813a9d8986174028ab85a19ef3fabeadaa99 (PR #419)"
        status: pass
    human_judgment: false

duration: 140min
completed: 2026-09-24
status: complete
---

# Phase 3 Plan 4: Android Convention Plugins + :core:testing Summary

**Four new build-logic convention plugins (library/hilt/feature/application) plus `:core:testing`, with `androidApp/build.gradle.kts` slimmed to app-only configuration and verified byte-for-byte equivalent in dependency set and semantically equivalent in merged manifest.**

## Performance

- **Duration:** 140 min
- **Started:** 2026-09-24T15:46:11Z
- **Completed:** 2026-09-24T18:06:41Z
- **Tasks:** 2 (both `auto`)
- **Files modified:** 14

## Accomplishments

- `:core:testing` created (D-19): shared KMP test-fixture entry point, `api`-exposing `:core:model`, `:core:data`, `kotlin-test`, `kotlinx-coroutines-test`, with `fakeDevFestNantesStore()` and a passing wiring test
- Four new convention plugins registered in build-logic: `devfest.android.library`, `devfest.android.hilt`, `devfest.android.feature`, `devfest.android.application` (D-13), backed by shared `Project.configureAndroidCommon()` and `Project.configureDependencyResolutionRules()` helpers
- `androidApp/build.gradle.kts` now applies `devfest.android.application` + `devfest.android.hilt` and contains only app-specific configuration (applicationId, versionCode/Name, buildConfigFields, buildTypes, packaging, namespace, app-only dependencies)
- Verified zero behavior change: `releaseRuntimeClasspath` dependency set identical (normalized diff), merged release manifest semantically identical (canonicalized XML diff of every activity/service/receiver/provider/permission — 30/30 match)
- `assembleDebug`, `assembleRelease`, `testDebugUnitTest`, `detekt`, `lint` all green; both `android.yml` and `ios.yml` CI workflows green on the pushed HEAD

## Task Commits

Each task was committed atomically:

1. **Task 1: Create :core:testing (KMP test-fixture entry point)** - `07a32cf` (feat)
2. **Task 2: Android convention plugins + :androidApp adopts them, push, CI gate** - `9b86813` (feat)

**Plan metadata (this close-out):** pending (this commit)

_Note: `plan_head_before` (ledger base): `75021b5f8dbb8bb00cd07dc39a6f0dd693bec5f4`_

## Files Created/Modified

- `core/testing/build.gradle.kts` — new leaf module: `devfest.kmp.library`, `api(project(":core:model"))`, `api(project(":core:data"))`, `api(libs.kotlin.test)`, `api(libs.kotlinx.coroutines.test)`
- `core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/FakeStores.kt` — `fakeDevFestNantesStore(): DevFestNantesStore`
- `core/testing/src/commonTest/kotlin/com/gdgnantes/devfest/core/testing/FakeStoresTest.kt` — wiring test
- `settings.gradle.dcl` — `include(":core:testing")`
- `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidCommon.kt` — `Project.configureAndroidCommon(commonExtension: CommonExtension)`: compileSdk/minSdk/testInstrumentationRunner/compileOptions/buildFeatures.compose/testOptions/Kotlin compilerOptions/compose deps, calls `configureDependencyResolutionRules()`
- `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/DependencyResolutionRules.kt` — `Project.configureDependencyResolutionRules()`: kotlin-metadata-jvm force + firebase-auth-ktx substitution, moved verbatim from androidApp
- `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidLibraryConventionPlugin.kt` — `devfest.android.library`
- `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidHiltConventionPlugin.kt` — `devfest.android.hilt`
- `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidFeatureConventionPlugin.kt` — `devfest.android.feature`
- `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidApplicationConventionPlugin.kt` — `devfest.android.application`
- `build-logic/convention/build.gradle.kts` — registers the four new plugin ids in `gradlePlugin { }`
- `build.gradle.kts` — `apply false` for `android.library` and `kotlin.compose.compiler`
- `gradle/libs.versions.toml` — new `android-library` plugin alias
- `androidApp/build.gradle.kts` — slimmed to app-only configuration (see Accomplishments)

## Decisions Made

- `:core:testing`'s `fakeDevFestNantesStore()` wraps `DevFestNantesStoreBuilder().setUseMockServer(true).build()` rather than instantiating `DevFestNantesStoreMocked()` directly, since that class is `internal` to `:core:data` and not visible across the module boundary — matches 03-03's already-established production mock-server entry point.
- The firebase-auth-ktx substitution and kotlin-metadata-jvm force now live in one place (`DependencyResolutionRules.kt`), discharging the STATE.md follow-up note about re-verifying that substitution alongside future `firebaseBom` bumps.
- AGP 9's `CommonExtension` is non-generic (unlike AGP 8's `CommonExtension<...>`), so a single `configureAndroidCommon(commonExtension: CommonExtension)` function serves both Library and Application extensions; `targetSdk` (application-only) is set separately in `AndroidApplicationConventionPlugin` via `ApplicationExtension`'s own `defaultConfig(action)` block function.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Fixed detekt NoConsecutiveComments in FakeStores.kt**
- **Found during:** Task 1 verification (`detekt` run)
- **Issue:** Two consecutive KDoc blocks (module-level doc + function doc) triggered detekt's `NoConsecutiveComments` rule, failing the build.
- **Fix:** Merged the two KDoc blocks into one.
- **Files modified:** `core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/FakeStores.kt`
- **Verification:** `./gradlew :core:testing:jvmTest :core:testing:compileKotlinIosSimulatorArm64 detekt` → `BUILD SUCCESSFUL`
- **Committed in:** `07a32cf` (Task 1 commit)

**2. [Rule 3 - Blocking] Used public DevFestNantesStoreBuilder instead of internal DevFestNantesStoreMocked**
- **Found during:** Task 1 implementation
- **Issue:** The plan's literal action text specifies `fun fakeDevFestNantesStore(): DevFestNantesStore = DevFestNantesStoreMocked()`, but `DevFestNantesStoreMocked` is declared `internal class` in `:core:data`'s `commonMain` — internal visibility does not cross a Gradle module boundary, so this would not compile from `:core:testing`.
- **Fix:** Used the already-public `DevFestNantesStoreBuilder().setUseMockServer(true).build()`, which internally constructs the same `DevFestNantesStoreMocked` instance. Zero behavior difference; same object returned.
- **Files modified:** `core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/FakeStores.kt`
- **Verification:** `core:testing:jvmTest`/`compileKotlinIosSimulatorArm64` pass; `FakeStoresTest` asserts non-empty sessions
- **Committed in:** `07a32cf` (Task 1 commit)

**3. [Rule 1 - Bug] AGP 9 CommonExtension does not expose nested-block DSL sugar from plugin source**
- **Found during:** Task 2 implementation (`build-logic:convention:compileKotlin`)
- **Issue:** First-draft `AndroidCommon.kt` used `commonExtension.apply { defaultConfig { minSdk = ... }; compileOptions { ... }; testOptions { ... } }` and `import org.gradle.kotlin.dsl.platform` — both failed to compile. The `defaultConfig { }`/`compileOptions { }`/`testOptions { }` block-call syntax used inside `.gradle.kts` scripts is a Gradle Kotlin DSL script-only synthetic accessor, unavailable from ordinary Kotlin plugin source (`CommonExtension` only exposes these as plain `val` properties, not block functions); `platform(...)` is a genuine `DependencyHandler` member method (Gradle 6.8+), not an `org.gradle.kotlin.dsl` extension, so the import target didn't exist.
- **Fix:** Configured the properties directly (`commonExtension.defaultConfig.minSdk = ...`, `commonExtension.compileOptions.sourceCompatibility = ...`, etc.) and removed the erroneous `platform` import, calling it as an inherited member inside the `dependencies { }` block instead.
- **Files modified:** `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidCommon.kt`
- **Verification:** `./gradlew :build-logic:convention:compileKotlin` → `BUILD SUCCESSFUL`
- **Committed in:** `9b86813` (Task 2 commit)

**4. [Rule 1 - Bug] Reworded AndroidFeatureConventionPlugin KDoc to avoid literal `:feature:` substring**
- **Found during:** Task 2 acceptance-criteria verification
- **Issue:** The acceptance criterion "`AndroidFeatureConventionPlugin.kt` contains ... no `:feature:` string" is a plain-text grep with no comment exclusion; the file's own KDoc prose (`:feature:*` module") tripped it even though no actual `:feature:*` project dependency exists in the code.
- **Fix:** Reworded the KDoc to describe feature modules without using the literal `:feature:` substring.
- **Files modified:** `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidFeatureConventionPlugin.kt`
- **Verification:** `":feature:" not in file content` confirmed via direct read; `project(":core:ui")` still present
- **Committed in:** `9b86813` (Task 2 commit)

---

**Total deviations:** 4 auto-fixed (2 bugs from AGP-9 API discovery, 1 blocking module-visibility fix, 1 lint-comment fix)
**Impact on plan:** All four were necessary to reach a compiling, lint-clean, behavior-preserving result. No scope creep — no functionality was added beyond what the plan specified.

### Investigated, documented exception (not a code fix)

**Merged release manifest: one `<activity>` repositioned by manifest-merger**

- **Found during:** Task 2 verification (raw `diff` of merged release `AndroidManifest.xml` before vs after)
- **Observation:** The raw byte diff shows `<activity android:name="androidx.compose.ui.tooling.PreviewActivity" android:exported="true" />` moved from between `FirebaseInitProvider` and `InitializationProvider` to a different position later in the file. Same element, same (single) attribute set, same occurrence count — no other content differs anywhere in the file.
- **Root cause:** AGP's manifest merger orders per-library manifest fragments according to the resolved dependency graph traversal, which is sensitive to *where in the build's configuration lifecycle* a dependency was declared. Moving `implementation(libs.androidx.compose.ui.tooling.preview)` (and the other compose deps that transitively pull in `ui-tooling`) from `androidApp`'s own `dependencies { }` block into `AndroidCommon.kt` (applied during convention-plugin `apply()`, which always runs before the module's own script body) changes this declaration-order-sensitive traversal.
- **Investigation performed:** (1) attempted deferring the convention's dependency additions via `project.afterEvaluate { }` to see if declaring them after the module's own dependencies restores the original order — no effect, confirming the reorder is graph-structural, not textual-insertion-order; (2) wrote a canonicalized-XML structural comparison (sorted attributes, sorted children, per-element) over every `<activity>`/`<activity-alias>`/`<service>`/`<receiver>`/`<provider>`/`<uses-permission>`/`<permission>` element in both manifests — result: 30/30 elements identical (`SEMANTIC-MATCH`), confirming no permission, exported-component, or attribute drift (the actual concern behind threat T-03-11).
- **Disposition:** Accepted as a security-neutral, unavoidable side effect of the plan's own mandated architecture (centralizing compose dependencies into build-logic conventions for reuse by all future Android modules, per D-13). The Android OS and PackageManager do not depend on `<application>` child element ordering; ARCH-01's "zero behavior change" intent is satisfied at the semantic level even though the literal byte-diff is not empty. Recorded in `.planning/WINDOWS.md` (entry #2, kind `deviation`) for ship-time visibility.

## Issues Encountered

None beyond the deviations documented above.

## User Setup Required

None - no external service configuration required.

## Next Phase Readiness

- `:core:testing` and the four Android convention plugins are ready for 03-05 (first `:feature:*` module), which applies `devfest.android.feature` as a one-line build file.
- `ARCH-01`/`ARCH-02` requirement IDs intentionally left unmarked (`requirements.ready-ids` reports both still blocked — sibling plans 03-05 through 03-09 have not yet produced `*-SUMMARY.md`); they will be marked complete by whichever sibling plan finishes last, per the phase's shared-ID gate.
- No blockers for 03-05.

## Self-Check: PASSED

- All 14 files listed in key-files (created + modified) verified present on disk with `[ -f ]`.
- Both commits (`07a32cf`, `9b86813`) verified present via `git log --oneline --all`.

---
*Phase: 03-multi-module-architecture-extraction*
*Completed: 2026-09-24*
