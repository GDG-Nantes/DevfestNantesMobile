---
phase: 02-dependency-build-tooling-upgrade
reviewed: 2026-09-19T09:58:30Z
depth: standard
files_reviewed: 25
files_reviewed_list:
  - androidApp/build.gradle.kts
  - androidApp/proguard-rules.pro
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/injection/AppModule.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/services/DataCollectionSettingsService.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/services/FirebaseAnalyticsService.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/services/SessionFiltersService.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/agenda/Agenda.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/agenda/AgendaViewModel.kt
  - build.gradle.kts
  - buildSrc/src/main/java/Dependencies.kt
  - gradle/libs.versions.toml
  - gradle/wrapper/gradle-wrapper.jar
  - gradle/wrapper/gradle-wrapper.properties
  - gradlew
  - gradlew.bat
  - linters/detekt-config.yml
  - settings.gradle.dcl
  - shared/build.gradle.kts
  - shared/src/commonMain/kotlin/com/gdgnantes/devfest/model/SocialItem.kt
  - shared/src/commonMain/kotlin/com/gdgnantes/devfest/model/stubs/StoreStubs.kt
  - shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/BookmarksStore.kt
  - shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/Apollo.kt
  - shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/ApolloCache.kt
  - shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/GraphQLStore.kt
  - shared/src/commonTest/kotlin/com/gdgnantes/devfest/model/ScheduleSlotDateParsingTest.kt
findings:
  critical: 0
  warning: 1
  info: 1
  total: 2
status: issues_found
---

# Phase 02: Code Review Report

**Reviewed:** 2026-09-19T09:58:30Z
**Depth:** standard
**Files Reviewed:** 25
**Status:** issues_found

## Summary

This phase is a staged dependency/build-tooling upgrade (Kotlin/KSP → AGP 9 → Compose BOM/Apollo 5.x → Firebase/kotlinx → Gradle Declarative DSL pilot). Each of the 25 in-scope files was read against the actual `git diff` for the phase (`a9810825f7201a9c3ec627e2c3e1afc71e44064d^..HEAD`) to separate genuine phase-introduced changes from pre-existing code that merely appears in a required-reading file.

The vast majority of the diff is mechanical: version-catalog bumps, `-ktx` → merged-artifact import renames (Firebase BOM 34.x dropped separately-published KTX artifacts), the Apollo 4→5 cache API migration (`CompiledField`/`Executable.Variables` signature → `ResolverContext`), the KMP `com.android.library` → `com.android.kotlin.multiplatform.library` plugin migration in `shared/build.gradle.kts`, the `kotlinx.datetime.Clock` → `kotlin.time.Clock` import swap, an auto-generated Gradle-9 wrapper script regen, and a new regression test file (`ScheduleSlotDateParsingTest.kt`, whose asserted epoch-millisecond values were independently recomputed and confirmed correct). These were traced line-by-line against the diff and found behavior-preserving; the decisions already flagged as pre-approved in the review brief (targetSdk/compileSdk split, the `GraphQLStore` `.map`→`.mapNotNull` change, the `firebase-auth-ktx` substitution, the three `-dontwarn` R8 rules, the Detekt 2.0.0-alpha pin, and the user-authorized minSdk 23→26 raise) were re-verified against their documented rationale and not re-flagged.

Two findings resulted. Neither is a defect introduced by this phase's diff:

- **WR-01** is a genuine, provable functional bug (Speaker screen analytics never fire) discovered while reading `MainActivity.kt` in full per the standard-depth review process; the diff for this file is a single blank-line addition, so the bug predates this phase. Flagged for visibility since it was surfaced during required reading, not attributed to this phase's work.
- **IN-01** is a naming/maintainability nit introduced by this phase's Firebase KTX-artifact merge in `gradle/libs.versions.toml`.

No hardcoded secrets, injection vectors, unsafe deserialization, or other Critical-tier issues were found in the reviewed files. `gradle/wrapper/gradle-wrapper.jar` changed as expected (Gradle 9.7.1 wrapper bump, binary, not source-reviewed).

## Warnings

### WR-01: Speaker screen analytics page-view event never fires (pre-existing, not introduced by this phase)

**File:** `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt:213`
**Issue:** `onDestinationChanged` compares the resolved `NavDestination.route` against `Screen.Speaker.route` with exact equality:

```kotlin
} else if (route == Screen.Speaker.route) {
    analyticsService.pageEvent(AnalyticsPage.SPEAKER, route)
}
```

`Screen.Speaker.route` is the literal string `"speaker"`, but the composable is registered as `route = "${Screen.Speaker.route}/{speakerId}"` (`"speaker/{speakerId}"`), and Jetpack Navigation's `NavDestination.route` always returns the declared route *template*, not the argument-substituted path. So `route` for the speaker screen is always `"speaker/{speakerId}"`, which never equals `"speaker"` — this branch is dead and the `SPEAKER` `pageEvent` is never emitted. Note the adjacent `Session` branch correctly anticipated this by using `route.contains(Screen.Session.route)` instead of `==`; the same pattern was not applied to `Speaker`.

This bug predates the phase (the file's only change in this phase's diff is an unrelated blank line inserted at line 40), so it is not attributable to this dependency/build-tooling upgrade. Flagged because it was directly observed while reading a file in the required review scope.

**Fix:**
```kotlin
} else if (route.contains(Screen.Speaker.route)) {
    analyticsService.pageEvent(AnalyticsPage.SPEAKER, route)
}
```

## Info

### IN-01: Firebase KTX-suffixed catalog aliases now resolve to non-KTX artifacts

**File:** `gradle/libs.versions.toml:81,84-86`
**Issue:** Firebase BOM 34.19.0 no longer publishes separate `-ktx` artifacts (their APIs were merged into the base artifacts). This phase's diff correctly repoints the coordinates:

```toml
firebase-analytics-ktx = { group = "com.google.firebase", name = "firebase-analytics" }
firebase-config-ktx = { group = "com.google.firebase", name = "firebase-config" }
firebase-crashlytics-ktx = { group = "com.google.firebase", name = "firebase-crashlytics" }
firebase-perf-ktx = { group = "com.google.firebase", name = "firebase-perf" }
```

but keeps the `-ktx` suffix in the catalog *alias* names, even though they now resolve to the plain (non-KTX) `com.google.firebase` artifacts. Every consumer of these aliases (`androidApp/build.gradle.kts`: `implementation(libs.firebase.analytics.ktx)`, etc.) reads as if it's pulling in a KTX extension artifact when it is not. This is cosmetic today but will mislead the next person auditing the catalog for stale KTX references (the exact category of confusion that caused the `firebase-auth-ktx` transitive-dependency issue this same phase had to work around via `dependencySubstitution`).

**Fix:** Rename the aliases to drop `-ktx` (e.g. `firebase-analytics`, `firebase-config`, `firebase-crashlytics`, `firebase-perf`) and update the four call sites in `androidApp/build.gradle.kts` accordingly, in a follow-up cleanup commit.

---

_Reviewed: 2026-09-19T09:58:30Z_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
