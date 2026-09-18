---
phase: 02-dependency-build-tooling-upgrade
plan: 03
subsystem: build
tags: [compose, apollo, graphql, normalized-cache, sqlite, coroutines-flow]

# Dependency graph
requires:
  - phase: 02-dependency-build-tooling-upgrade
    plan: 02
    provides: AGP 9.4.0 + com.android.kotlin.multiplatform.library plugin swap on shared, Gradle 9.7.1, Detekt/Hilt/firebase-perf AGP-9-compatible floors
provides:
  - Compose BOM bumped to 2026.09.00 on androidApp's Compose UI dependency graph
  - Apollo Kotlin migrated to 5.2.0 with the normalized cache moved from the deprecated com.apollographql.apollo group to the new com.apollographql.cache artifact family (normalized-cache, normalized-cache-sqlite, normalized-cache-apollo-compiler-plugin), all pinned to appolloCache 1.0.8
  - compileSdk raised to 37 (forced by Compose BOM 2026.09.00's AAR metadata floor), targetSdk deliberately kept at 36 (decoupled from compileSdk) pending a dedicated future decision
  - GraphQLStore.kt's four CacheAndNetwork Flow accessors no longer let a network-failure emission clobber a prior successful cache emission (map -> mapNotNull fix)
affects: [02-04-firebase-coroutines-datetime, 02-05-dcl-pilot]

# Actuals (#2632)
actuals:
  tokens: 2534
  tasks: 2
  commits: 3

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Apollo v5 cache-resolver signature: CacheResolver.resolveField(field, variables, parent, parentId) -> resolveField(context: ResolverContext) — context exposes field/variables/parent plus a CacheKey-typed parentKey; adapt the override signature, keep the same decision logic"
    - "Apollo v5 CompiledField.resolveArgument(...) is a hard DEPRECATION_ERROR under Kotlin 2.4 (not a warning) — use context.field.argumentValue(name, variables).getOrNull() instead, same null-check semantics"
    - "CacheAndNetwork's dual-emission behavior (cache response, then network response) means any .map { if (exception) emptyList() ... } pattern will overwrite good cache data with an empty result on a subsequent network failure — use .mapNotNull { if (exception) null ... } to skip the failed emission instead of emitting a value that replaces good state downstream"
    - "AndroidSdk.compile and AndroidSdk.target (buildSrc/Dependencies.kt) should be independently settable, not `target = compile` — a compileSdk floor forced by a UI-library AAR metadata check must not silently drag targetSdk (and its runtime behavior-change surface) along with it"

key-files:
  created:
    - .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-03-agenda-compose-bom.png
    - .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-03-01-agenda-online.png
    - .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-03-02-speakers-online.png
    - .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-03-03-venue-online.png
    - .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-03-05-agenda-offline.png
    - .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-03-08-agenda-offline-fixed.png
    - .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-03-09-speakers-offline.png
    - .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-03-10-venue-offline.png
    - .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-03-20-bookmark-persisted-final.png
  modified:
    - gradle/libs.versions.toml
    - buildSrc/src/main/java/Dependencies.kt
    - shared/build.gradle.kts
    - shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/Apollo.kt
    - shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/ApolloCache.kt
    - shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/GraphQLStore.kt

key-decisions:
  - "Compose BOM 2026.09.00 (newest-verified, per 02-01's checkpoint policy) rather than the plan's pinned 2026.08.00 fallback"
  - "compileSdk raised 36 -> 37 (forced by Compose BOM 2026.09.00 AAR metadata: androidx.compose.* artifacts declare a compileSdk >= 37 floor) — a compile-time-only, no-user-impact fix, applied without a checkpoint"
  - "targetSdk deliberately decoupled from compileSdk and kept at 36 — user-directed mid-execution: 'keep target to 36 but add 37 as a tracked follow-up decision' — targeting SDK 37 opts the app into new Android 17 runtime behavior for every user, out of this build-tooling-only phase's no-regression scope; tracked as a follow-up in STATE.md Blockers/Concerns"
  - "Apollo 5.2.0 (newest-verified) with appolloCache 1.0.8 — both re-verified live against Maven Central immediately before the commit, matching the versions carried forward from 02-01's checkpoint"
  - "CacheResolver.resolveField adapted to v5's ResolverContext-based signature using CompiledField.argumentValue(...).getOrNull() instead of the deprecated resolveArgument(...) (which is a hard compiler error under Kotlin 2.4, not just deprecated) — same id-based decision logic preserved, per the plan's explicit prohibition on changing cache-key strategy"
  - "User-authorized deviation from the plan's error-handling prohibition: GraphQLStore.kt's four CacheAndNetwork Flow accessors (partners, rooms, sessions, speakers) changed from .map{...emptyList()...} to .mapNotNull{...null...} on exception, because the plan's own D-07 acceptance criterion (offline relaunch shows cached data) was failing — not from a broken cache, but from a pre-existing anti-pattern where a network-failure emission overwrote a prior successful cache emission. Confirmed via logcat this bug predates the migration (CacheAndNetwork's dual-emission semantics are unchanged by Apollo 5); user explicitly chose 'apply a minimal scoped fix now' over leaving it as a tracked gap"

patterns-established:
  - "Version-catalog-only discipline held for both stages: composeBom and appollo/appolloCache are the only [versions] edits; the Apollo cache artifact-group change is expressed purely as group/name swaps on the existing appollo-normalized-cache* catalog keys, keeping libs.bundles.appollo stable"

requirements-completed: [BUILD-04, BUILD-05]

coverage:
  - id: D1
    description: "Compose BOM bumped to 2026.09.00; androidApp assembles in debug/release, unit tests pass, both CI workflows green, app renders unchanged on device (day pager, Material 3 theming, bookmark badge intact)"
    requirement: "BUILD-04"
    verification:
      - kind: other
        ref: "./gradlew --no-daemon detekt lint :androidApp:assembleDebug :androidApp:assembleRelease :androidApp:testDebugUnitTest"
        status: pass
      - kind: other
        ref: "gh run watch 35349037276 (android.yml, commit c61f957, 4/4 jobs) + gh run watch 35349037329 (ios.yml)"
        status: pass
      - kind: automated_ui
        ref: "android run + android layout dump asserting the app window is focused; screenshot 02-03-agenda-compose-bom.png"
        status: pass
    human_judgment: true
    rationale: "Visual parity (day pager swipe/indicator intact, Material 3 theming unchanged, no clipped/overlapped content) is best judged by a human looking at the attached screenshot, per D-02's sanity-run intent."
  - id: D2
    description: "Apollo migrated to 5.2.0 with the normalized cache resolving from com.apollographql.cache (not the deprecated apollo group); cache compiler plugin registered; project builds/tests/lints clean on a `clean` build; both CI workflows green including the iOS klib build"
    requirement: "BUILD-05"
    verification:
      - kind: other
        ref: "./gradlew --no-daemon clean :shared:generateApolloSources :shared:assemble :androidApp:assembleDebug :androidApp:assembleRelease :shared:jvmTest :androidApp:testDebugUnitTest detekt lint"
        status: pass
      - kind: other
        ref: "gh run watch 35351574468 (android.yml, commit 00ab89d, 4/4 jobs) + gh run watch 35351574449 (ios.yml) — repeated on 4ab7f01 (fix commit): gh run watch 35355127442 + 35355127310"
        status: pass
    human_judgment: false
  - id: D3
    description: "D-07 functional cache verification: a repeated query is served from cache (immediate re-render, no loading flash); with network off, a cold relaunch still shows the last-loaded Agenda/Speakers/Venue data; bookmarks set before this stage (and a newly-set one) survive the migration and a relaunch"
    requirement: "BUILD-05"
    verification:
      - kind: other
        ref: "adb logcat: 'Agenda loaded with 94 sessions' single emission (no clobbering '0 sessions' follow-up) after the mapNotNull fix; apollo.db (316KB) confirmed on-disk under databases/ with fresh write timestamp"
        status: pass
      - kind: automated_ui
        ref: "screenshots 02-03-01/02/03 (online populate), 02-03-05 (offline BEFORE fix — empty state, the bug), 02-03-08/09/10 (offline AFTER fix — real data), 02-03-20 (bookmark CHECKED state surviving unbookmark/rebookmark/relaunch, via `android layout` state assertion)"
        status: pass
    human_judgment: true
    rationale: "Functional-only, per D-07/D-06 scoping — no before/after cache-content comparison. Final confirmation that the offline screens genuinely show correct data (not just non-empty) and that the D-06 boundary held (BookmarksStore unaffected) is a human visual/behavioral judgment call over the attached screenshot sequence."

duration: 180min
completed: 2026-09-18
status: complete
---

# Phase 2 Plan 3: Compose BOM Bump + Apollo 5.x Normalized-Cache Migration Summary

**Compose BOM bumped to 2026.09.00 (forcing a compileSdk 37 floor) and Apollo migrated to 5.2.0 with its normalized cache moved to the `com.apollographql.cache` artifact group and compiler plugin — plus a user-authorized fix for a pre-existing CacheAndNetwork error-handling bug that was silently discarding correctly cached offline data.**

## Performance

- **Duration:** ~180 min
- **Started:** 2026-09-18T13:20:00Z (approximate)
- **Completed:** 2026-09-18T16:20:00Z
- **Tasks:** 2 (Task 1: Compose BOM; Task 2: Apollo 5.x + cache migration, plus a user-directed follow-up fix)
- **Files modified:** 6

## Accomplishments
- Compose BOM bumped to 2026.09.00 (newest-verified), the Android UI dependency graph re-resolved cleanly, and the app renders unchanged on device (day pager, Material 3 theming, bookmark badge)
- Apollo Kotlin bumped to 5.2.0 with the normalized cache's artifact-group migration completed in full: `appollo-normalized-cache`/`-sqlite` now resolve from `com.apollographql.cache` (not the deprecated `com.apollographql.apollo` group), the `normalized-cache-apollo-compiler-plugin` is registered in `shared/build.gradle.kts`, and all three cache imports in `Apollo.kt`/`ApolloCache.kt` are repointed
- `Apollo.kt`'s custom `CacheResolver` adapted to v5's single-`ResolverContext`-parameter signature, preserving the exact id-based cache-key decision logic; `CompiledField.resolveArgument(...)` (now a hard `DEPRECATION_ERROR` under Kotlin 2.4) replaced with `argumentValue(...).getOrNull()`
- D-07's functional offline verification uncovered — via `logcat` and on-disk `apollo.db` inspection, not guesswork — that the SQLite cache genuinely persisted and read back correctly (94 real sessions served offline), but a pre-existing bug in `GraphQLStore.kt`'s `CacheAndNetwork` error handling was overwriting that correct data with an empty result on the network-failure half of the dual emission; fixed with a minimal, user-authorized `.map` → `.mapNotNull` change
- Both CI workflows (`android.yml` 4/4 jobs, `ios.yml`) green on all three commits; iOS klib resolution specifically confirmed unaffected by the cache-package move
- Full D-07 human-check cycle completed on a live Pixel_9 emulator: online populate (Agenda/Speakers/Venue) → immediate cache-served re-render → real airplane-mode-equivalent offline (`svc wifi/data disable`, network agents confirmed absent) → cold relaunch showing correct cached data on all three screens → bookmarks (pre-existing + a fresh unbookmark/rebookmark cycle) surviving relaunch, confirming the D-06 boundary held

## Task Commits

Each task was committed atomically:

1. **Task 1: Compose BOM bump** - `c61f957` (build) — composeBom 2025.09.01 → 2026.09.00; forced compileSdk 36 → 37 fix in `buildSrc/Dependencies.kt` with `targetSdk` deliberately decoupled and kept at 36
2. **Task 2: Apollo 5.x + cache-package migration** - `00ab89d` (build) — appollo 4.4.3 → 5.2.0, new `appolloCache = "1.0.8"` catalog key, cache artifact-group migration, compiler plugin registration, `Apollo.kt`/`ApolloCache.kt`/`GraphQLStore.kt` import and signature migration
3. **D-07 fix (user-authorized deviation): stop CacheAndNetwork emission clobbering** - `4ab7f01` (fix) — `GraphQLStore.kt`'s four Flow accessors changed `.map` → `.mapNotNull`, skipping network-failure emissions instead of overwriting prior cache data

**Plan metadata:** commit pending (this SUMMARY + STATE.md + ROADMAP.md + REQUIREMENTS.md)

_Note: three commits instead of the plan's target of two — see Deviations. The third commit is a user-authorized, explicitly scoped fix required to satisfy the plan's own D-07 acceptance criterion._

## Files Created/Modified
- `gradle/libs.versions.toml` - `composeBom`, `appollo`, new `appolloCache` version; Apollo cache library `group`/`name` migration
- `buildSrc/src/main/java/Dependencies.kt` - `compile = 37` (forced), `target = 36` (deliberately decoupled)
- `shared/build.gradle.kts` - `plugin(...)`/`pluginArgument(...)` cache compiler plugin registration
- `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/Apollo.kt` - cache-normalized imports repointed; `CacheResolver.resolveField` adapted to `ResolverContext`; `resolveArgument` → `argumentValue(...).getOrNull()`
- `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/ApolloCache.kt` - cache-normalized imports repointed; `.chain()`/`apollo.db` composition unchanged
- `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/GraphQLStore.kt` - `FetchPolicy`/`fetchPolicy` imports repointed; four `CacheAndNetwork` accessors changed `.map` → `.mapNotNull` (D-07 fix)
- `.planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-03-*.png` - D-02/D-07 evidence (9 screenshots retained; see key-files)

## Decisions Made

**Authoritative import paths for Phase 3 reuse (per this plan's `<output>` requirement):**
- Cache-normalized wildcard: `com.apollographql.cache.normalized.*` (was `com.apollographql.apollo.cache.normalized.*`)
- `MemoryCacheFactory`: `com.apollographql.cache.normalized.memory.MemoryCacheFactory` (moved to its own `memory` subpackage — was `...api.MemoryCacheFactory`)
- `SqlNormalizedCacheFactory`: `com.apollographql.cache.normalized.sql.SqlNormalizedCacheFactory` (same subpackage name as before, new root group)
- `NormalizedCacheFactory`, `CacheKey`, `CacheKeyGenerator`, `CacheKeyGeneratorContext`, `CacheResolver`, `DefaultCacheResolver`, `ResolverContext`: all under `com.apollographql.cache.normalized.api`
- `FetchPolicy`, `fetchPolicy` extension, `normalizedCache` extension: `com.apollographql.cache.normalized`
- Runtime APIs (`ApolloClient`, `api.http.HttpHeader`) stay on `com.apollographql.apollo.*` — unmoved
- `CacheResolver.resolveField` is now `fun resolveField(context: ResolverContext): Any?` — `ResolverContext` exposes `field: CompiledField`, `variables: Executable.Variables`, `parent: Map<String, Any?>`, `parentKey: CacheKey` (was `parentId: String`), `parentType`, `cacheHeaders`, `fieldKeyGenerator`, `path`
- `CompiledField.resolveArgument(name, variables): Any?` is `@ApolloDeprecatedSince(v4_0_0)` and now a **hard compiler `DEPRECATION_ERROR`** under Kotlin 2.4.20 (not a warning) — use `argumentValue(name, variables): Optional<ApolloJsonElement?>` with `.getOrNull()` instead
- `apolloClient.apolloStore` (property name) is **unchanged** — only the `ApolloStore` *interface* (for custom implementations) is renamed to `CacheManager`; this project has no custom store implementation, so no call site was affected (confirms 02-RESEARCH.md's assumption)
- No cache-spec schema-directive extension was needed — this project declares no `@typePolicy`/`@fieldPolicy` directives in `schema.graphqls`/`operations.graphql` (confirmed by grep before editing)

**Accompanist D-03 branch:** not needed. `accompanist-pager`/`accompanist-pager-indicators` (0.36.0) resolved and compiled cleanly against Compose BOM 2026.09.00 with no changes.

**Compiler-plugin call shape actually registered** (`shared/build.gradle.kts`):
```kotlin
apollo {
    service("service") {
        packageName.set("com.gdgnantes.devfest.graphql")
        plugin("com.apollographql.cache:normalized-cache-apollo-compiler-plugin:${libs.versions.appolloCache.get()}")
        pluginArgument("com.apollographql.cache.packageName", packageName.get())
    }
}
```

**CI run IDs:**
| Commit | android.yml | ios.yml |
|---|---|---|
| `c61f957` (Compose BOM) | 35349037276 (4/4 jobs, success) | 35349037329 (success) |
| `00ab89d` (Apollo 5.x) | 35351574468 (4/4 jobs, success) | 35351574449 (success) |
| `4ab7f01` (D-07 fix) | 35355127442 (4/4 jobs, success) | 35355127310 (success) |

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] compileSdk forced 36 → 37 by Compose BOM 2026.09.00's AAR metadata**
- **Found during:** Task 1, first local build attempt
- **Issue:** `:androidApp:checkDebugAarMetadata` failed — `androidx.compose.material:material-android:1.12.1` (and 11 sibling artifacts) declare `compileSdk >= 37` as a hard floor. The project's `compileSdk` was still 36.
- **Fix:** Installed `platforms/android-37.2` via `android sdk install`, raised `AndroidSdk.compile` to 37 in `buildSrc/Dependencies.kt`. Verified against the official `kb://android/about/versions/17/setup-sdk` doc that `compileSdk = 37` (plain integer) is the correct Gradle-side value for "Android 17."
- **Files modified:** `buildSrc/src/main/java/Dependencies.kt`
- **Verification:** `./gradlew detekt lint :androidApp:assembleDebug :androidApp:assembleRelease :androidApp:testDebugUnitTest` exits 0
- **Committed in:** `c61f957`

**2. [User-directed scoping, not a bug] targetSdk deliberately decoupled from compileSdk, kept at 36**
- **Found during:** Task 1, resolving deviation #1 above
- **Issue:** `buildSrc/Dependencies.kt` previously had `target = compile`, so raising `compile` to 37 would have silently raised `targetSdk` to 37 too — opting the app into Android 17's new runtime behavior changes for every user, which is out of this build-tooling-only phase's declared scope and risks PROJECT.md's "zero regression" constraint.
- **Resolution:** User was asked directly; explicitly chose to keep `targetSdk` at 36 and track SDK 37 targeting as a separate, dedicated follow-up decision rather than folding it into this Compose-BOM commit.
- **Files modified:** `buildSrc/src/main/java/Dependencies.kt` (`target = 36` explicit, no longer derived from `compile`)
- **Tracked as:** new entry in `.planning/STATE.md` Blockers/Concerns (see Next Phase Readiness below)
- **Committed in:** `c61f957`

**3. [Rule 3 - Blocking] `CompiledField.resolveArgument(...)` is a hard `DEPRECATION_ERROR`, not a warning, under Kotlin 2.4.20**
- **Found during:** Task 2, first full clean build attempt
- **Issue:** The Kotlin compiler failed `:shared:compileAndroidMain`/`:shared:compileKotlinIosArm64` with `Kotlin compiler: DEPRECATION_ERROR — 'fun resolveArgument(...)' is deprecated. Use argumentValue instead.` — RESEARCH.md's Pattern 2 assumed a same-shape signature adaptation would suffice; it did not anticipate this API being elevated to a build-breaking error level in this Kotlin version.
- **Fix:** Replaced `context.field.resolveArgument("id", context.variables)?.toString()` with `context.field.argumentValue("id", context.variables).getOrNull()?.toString()`, matching Apollo's own v5 migration-guide/programmatic-ids example exactly. Same null-check/`CacheKey` decision logic preserved.
- **Files modified:** `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/Apollo.kt`
- **Verification:** `./gradlew clean :shared:generateApolloSources :shared:assemble :androidApp:assembleDebug :androidApp:assembleRelease :shared:jvmTest :androidApp:testDebugUnitTest detekt lint` exits 0
- **Committed in:** `00ab89d`

**4. [Rule 1 - Bug, user-authorized] CacheAndNetwork network-failure emission overwrote correctly-cached offline data**
- **Found during:** Task 2's D-07 functional cache verification (offline cold-relaunch step)
- **Issue:** Offline relaunch of Agenda/Speakers showed an empty state despite the SQLite cache genuinely containing (and correctly serving) real data. Root-caused via `logcat` (two sequential emissions: "Agenda loaded with 94 sessions" then "Agenda loaded with 0 sessions" ~900ms later) and on-disk inspection of `apollo.db` (316 KB, correctly written). `GraphQLStore.kt`'s four `CacheAndNetwork` Flow accessors unconditionally mapped any `response.exception != null` to `emptyList()`/`emptyMap()`, so the network-failure half of `CacheAndNetwork`'s dual emission clobbered the prior successful cache-hit emission in `AgendaViewModel`'s `MutableStateFlow`.
- **Conflict:** the plan's `<prohibitions>` explicitly forbids touching error handling in these files ("out of scope for this phase"), while the plan's own `<must_haves>` requires the offline screen to show cached data (D-07). Confirmed this is a pre-existing anti-pattern (unrelated to the Apollo 5 migration — `CacheAndNetwork`'s dual-emission semantics are unchanged per Apollo's own migration guide), not a regression this plan introduced.
- **Resolution:** Surfaced the conflict and two options directly to the user; user chose "apply a minimal scoped fix now."
- **Fix:** Changed `.map { response -> if (exception) emptyList()/emptyMap() ... }` to `.mapNotNull { response -> if (exception) null ... }` across all four accessors (`partners`, `rooms`, `sessions`, `speakers`) — a network-failure emission is now skipped rather than replacing good state. Error logging (`println`) and `FetchPolicy.CacheAndNetwork` are unchanged.
- **Files modified:** `shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/GraphQLStore.kt`
- **Verification:** Full clean build green; `logcat` shows only the single correct "94 sessions" emission after the fix; offline relaunch screenshots (Agenda/Speakers/Venue) confirm real cached data displays; both CI workflows green on the fix commit
- **Committed in:** `4ab7f01`

**5. [Pre-existing, not introduced here] `appollo` catalog value is `4.4.3` prior to this plan's edit, not the plan's assumed literal `4.3.3`**
- **Found during:** Task 1's `STAGE3-CATALOG-OK` verification gate
- **Issue:** The plan's automated gate asserts `appollo = "4.3.3"` was unchanged going into Task 1. It was actually `4.4.3` — a forced-sibling bump from 02-01 (Kotlin 2.4.20/KGP compatibility, already documented in `02-01-SUMMARY.md` and `02-02-SUMMARY.md`), not something this plan touched or introduced.
- **Resolution:** No fix needed — documented here for the audit trail. The gate's real intent (composeBom bumped, firebaseBom/kotlinxDatetime untouched by a later-stage leak) was satisfied.
- **Files modified:** none (informational only)

---

**Total deviations:** 5 (2 Rule-3 blocking auto-fixes, 1 user-directed scoping decision, 1 Rule-1 bug fix with explicit user authorization overriding a plan prohibition, 1 pre-existing catalog divergence carried forward from 02-01/02-02)
**Impact on plan:** All fixes were necessary either to make the version bumps build at all, or to satisfy the plan's own D-07 acceptance criterion. The CacheAndNetwork fix is the one genuine scope deviation from an explicit written prohibition — it was not applied unilaterally; the conflict was surfaced to the user first and the user made the call.

## Issues Encountered
None beyond what's captured in Deviations above — every blocker encountered was resolved within this plan's execution.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- `gradle/libs.versions.toml` now carries `composeBom = "2026.09.00"`, `appollo = "5.2.0"`, `appolloCache = "1.0.8"` as the baseline `02-04` (Firebase/Coroutines/kotlinx-serialization/kotlinx-datetime) builds on.
- `compileSdk` is now 37 (was 36); `targetSdk` remains 36, deliberately decoupled in `buildSrc/Dependencies.kt` — **tracked follow-up decision**: bump `targetSdk` to 37 in a future dedicated stage after reviewing Android 17's behavior-change surface (`kb://android/about/versions/17/behavior-changes-all` / `-17`), not as a drive-by in an unrelated commit.
- The authoritative Apollo v5 cache import paths (see Decisions Made above) are recorded for Phase 3 reuse — no need to re-derive them from the migration guide.
- `GraphQLStore.kt`'s `.mapNotNull`-based error handling is a narrow, targeted fix for the CacheAndNetwork-clobbering bug only — it does **not** address the broader `println`-based logging anti-pattern (still tracked in `ARCHITECTURE.md` as a known issue, still out of scope until Phase 3/4/5).
- Screenshots from the D-02/D-07 verification are at `.planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-03-*.png` for human review.
- No blockers for 02-04.

## Self-Check: PASSED

- FOUND: gradle/libs.versions.toml (composeBom = "2026.09.00", appollo = "5.2.0", appolloCache = "1.0.8")
- FOUND: buildSrc/src/main/java/Dependencies.kt (compile = 37, target = 36)
- FOUND: shared/build.gradle.kts (normalized-cache-apollo-compiler-plugin registered)
- FOUND: shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/Apollo.kt (com.apollographql.cache imports, ResolverContext-based resolveField)
- FOUND: shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/ApolloCache.kt (com.apollographql.cache.memory/sql imports)
- FOUND: shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/GraphQLStore.kt (mapNotNull, com.apollographql.cache.normalized FetchPolicy)
- FOUND: .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-03-agenda-compose-bom.png
- FOUND: .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-03-08-agenda-offline-fixed.png
- FOUND: .planning/phases/02-dependency-build-tooling-upgrade/screenshots/02-03-20-bookmark-persisted-final.png
- FOUND commit c61f9578bed032ca815c87325267583eddb61c95 in git log
- FOUND commit 00ab89de99da23b7056ab93e41560d6767b652ee in git log
- FOUND commit 4ab7f0189ccc2f6e003bc8ce5b018e3f1126aa25 in git log
- CONFIRMED: android.yml run 35355127442 conclusion=success on commit 4ab7f01 (4/4 jobs)
- CONFIRMED: ios.yml run 35355127310 conclusion=success on commit 4ab7f01

---
*Phase: 02-dependency-build-tooling-upgrade*
*Completed: 2026-09-18*
