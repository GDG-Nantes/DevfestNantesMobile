---
phase: 03-multi-module-architecture-extraction
reviewed: 2026-09-27T15:38:01Z
depth: standard
files_reviewed: 210
files_reviewed_list:
  - .github/workflows/ios.yml
  - androidApp/build.gradle.kts
  - androidApp/proguard-rules.pro
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/DataSharingInitializer.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/injection/AppModule.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/components/appbars/BottomAppBar.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/Home.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/home/HomeViewModel.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/Screen.kt
  - androidApp/src/main/res/values-fr/strings.xml
  - androidApp/src/main/res/values/strings.xml
  - build-logic/convention/build.gradle.kts
  - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidApplicationConventionPlugin.kt
  - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidCommon.kt
  - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidFeatureConventionPlugin.kt
  - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidHiltConventionPlugin.kt
  - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidLibraryConventionPlugin.kt
  - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidSdk.kt
  - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/DependencyResolutionRules.kt
  - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/DetektConventionPlugin.kt
  - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/KmpLibraryConventionPlugin.kt
  - build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/ProjectExtensions.kt
  - build-logic/settings.gradle.kts
  - build.gradle.kts
  - core/analytics/build.gradle.kts
  - core/analytics/src/androidMain/kotlin/com/gdgnantes/devfest/core/analytics/FirebaseAnalyticsService.kt
  - core/analytics/src/androidMain/kotlin/com/gdgnantes/devfest/core/analytics/performance/PerformanceExtensions.kt
  - core/analytics/src/androidMain/kotlin/com/gdgnantes/devfest/core/analytics/performance/PerformanceMonitoring.kt
  - core/analytics/src/commonMain/kotlin/com/gdgnantes/devfest/core/analytics/AnalyticsEvent.kt
  - core/analytics/src/commonMain/kotlin/com/gdgnantes/devfest/core/analytics/AnalyticsPage.kt
  - core/analytics/src/commonMain/kotlin/com/gdgnantes/devfest/core/analytics/AnalyticsParam.kt
  - core/analytics/src/commonMain/kotlin/com/gdgnantes/devfest/core/analytics/AnalyticsService.kt
  - core/data/build.gradle.kts
  - core/data/src/androidMain/kotlin/com/gdgnantes/devfest/core/data/BookmarksStoreImpl.kt
  - core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/BookmarksStore.kt
  - core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/DevFestNantesStore.kt
  - core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/DevFestNantesStoreBuilder.kt
  - core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/DevFestNantesStoreMocked.kt
  - core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/domain/RoomSortIndex.kt
  - core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/graphql/GraphQLStore.kt
  - core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/graphql/Mappers.kt
  - core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/DevFestNantesStoreContractTest.kt
  - core/data/src/jvmTest/kotlin/com/gdgnantes/devfest/core/data/graphql/GraphQLStoreJvmTest.kt
  - core/model/build.gradle.kts
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/Agenda.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/AgendaDay.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/Category.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/Complexity.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/ContentLanguage.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/Partner.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/PartnerCategory.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/Room.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/ScheduleSlot.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/Session.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/SessionLanguage.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/SessionType.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/SocialItem.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/Speaker.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/stubs/CategoryStubs.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/stubs/RoomStubs.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/stubs/SocialItemStubs.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/stubs/StoreStubs.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/utils/SessionExtensions.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/Venue.kt
  - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/WebLinks.kt
  - core/model/src/commonTest/kotlin/com/gdgnantes/devfest/core/model/ScheduleSlotDateParsingTest.kt
  - core/network/build.gradle.kts
  - core/network/src/commonMain/graphql/extra.graphqls
  - core/network/src/commonMain/graphql/operations.graphql
  - core/network/src/commonMain/graphql/schema.graphqls
  - core/network/src/commonMain/kotlin/com/gdgnantes/devfest/core/network/Apollo.kt
  - core/network/src/commonMain/kotlin/com/gdgnantes/devfest/core/network/ApolloCache.kt
  - core/testing/build.gradle.kts
  - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/FakeStores.kt
  - core/testing/src/commonTest/kotlin/com/gdgnantes/devfest/core/testing/FakeStoresTest.kt
  - core/ui/build.gradle.kts
  - core/ui/src/androidTest/java/com/gdgnantes/devfest/core/ui/utils/ScheduleSlotDateFormatAndroidTest.kt
  - core/ui/src/main/AndroidManifest.xml
  - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/BookmarksViewModel.kt
  - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/components/appbars/AppBarIcons.kt
  - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/components/appbars/TopAppBar.kt
  - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/components/LoadingLayout.kt
  - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/components/SessionCategory.kt
  - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/components/SocialIcon.kt
  - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/components/SpeakerPicture.kt
  - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/theme/Color.kt
  - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/theme/Theme.kt
  - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/theme/Type.kt
  - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/UiState.kt
  - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/utils/DateUtils.kt
  - core/ui/src/main/java/com/gdgnantes/devfest/core/ui/utils/StringExtensions.kt
  - core/ui/src/main/res/drawable/github.xml
  - core/ui/src/main/res/drawable/ic_network_facebook.xml
  - core/ui/src/main/res/drawable/ic_network_linkedin.xml
  - core/ui/src/main/res/drawable/ic_network_twitter.xml
  - core/ui/src/main/res/drawable/ic_network_web.xml
  - core/ui/src/main/res/drawable/ic_person_black_24dp.xml
  - core/ui/src/main/res/values-fr/strings.xml
  - core/ui/src/main/res/values/strings.xml
  - feature/about/build.gradle.kts
  - feature/about/src/main/AndroidManifest.xml
  - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/About.kt
  - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/AboutHeader.kt
  - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/AboutLinks.kt
  - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/AboutLocalCommunities.kt
  - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/AboutRoute.kt
  - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/AboutSocial.kt
  - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/AboutVersion.kt
  - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/components/GithubCard.kt
  - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/partners/PartnerCard.kt
  - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/partners/Partners.kt
  - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/partners/PartnersViewModel.kt
  - feature/about/src/main/res/drawable-night-nodpi/about_header.png
  - feature/about/src/main/res/drawable-nodpi/about_header.png
  - feature/about/src/main/res/drawable-nodpi/local_communities_logo.png
  - feature/about/src/main/res/drawable/ic_network_youtube.xml
  - feature/about/src/main/res/values-fr/strings.xml
  - feature/about/src/main/res/values/strings.xml
  - feature/agenda/build.gradle.kts
  - feature/agenda/src/main/AndroidManifest.xml
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/Agenda.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaColumn.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaPager.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaRoute.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaRow.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaViewModel.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/EmptyLayout.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/services/SessionFiltersService.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/SessionFiltersDrawer.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/utils/PagerTab.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/utils/SessionFilter.kt
  - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/utils/SessionTypeUtils.kt
  - feature/agenda/src/main/res/drawable/ic_bookmark.xml
  - feature/agenda/src/main/res/drawable/ic_bookmarked.xml
  - feature/agenda/src/main/res/values-fr/strings.xml
  - feature/agenda/src/main/res/values/strings.xml
  - feature/session-detail/build.gradle.kts
  - feature/session-detail/src/main/AndroidManifest.xml
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/components/SessionComplexity.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/components/SessionType.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/FallbackFeedbackForm.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/FeedbackForm.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/FeedbackFormViewModel.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/OpenFeedbackConfig.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/SessionDetailRoute.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/SessionDetails.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/SessionLayout.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/SessionSpeaker.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/SessionViewModel.kt
  - feature/session-detail/src/main/java/com/gdgnantes/devfest/feature/sessiondetail/utils/ScheduleSlotUtils.kt
  - feature/session-detail/src/main/res/drawable/ic_bookmark_fab.xml
  - feature/session-detail/src/main/res/drawable/ic_bookmarked_fab.xml
  - feature/session-detail/src/main/res/values-fr/strings.xml
  - feature/session-detail/src/main/res/values/strings.xml
  - feature/settings/build.gradle.kts
  - feature/settings/src/main/AndroidManifest.xml
  - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/datacollection/DataCollectionAgreementDialog.kt
  - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/datacollection/DataCollectionSettingsScreen.kt
  - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/datacollection/DataCollectionViewModel.kt
  - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/legal/LegalScreen.kt
  - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/services/DataCollectionSettingsService.kt
  - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/Settings.kt
  - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/SettingsItem.kt
  - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/SettingsRoute.kt
  - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/SettingsTileIcon.kt
  - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/SettingsTileSubtitle.kt
  - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/SettingsTileTexts.kt
  - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/SettingsTileTitle.kt
  - feature/settings/src/main/res/values-fr/strings.xml
  - feature/settings/src/main/res/values/strings.xml
  - feature/speakers/build.gradle.kts
  - feature/speakers/src/main/AndroidManifest.xml
  - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/details/SpeakerDetails.kt
  - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/details/SpeakerLayout.kt
  - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/details/SpeakerSession.kt
  - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/Speakers.kt
  - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/SpeakersViewModel.kt
  - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/SpeakersRoute.kt
  - feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/SpeakerViewModel.kt
  - feature/venue/build.gradle.kts
  - feature/venue/src/main/AndroidManifest.xml
  - feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/plan/VenueFloorPlan.kt
  - feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/utils/LocalUtils.kt
  - feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/utils/NavigationUtils.kt
  - feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/Venue.kt
  - feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/VenueDetails.kt
  - feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/VenueRoute.kt
  - feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/VenueViewModel.kt
  - feature/venue/src/main/res/values-fr/strings.xml
  - feature/venue/src/main/res/values/strings.xml
  - gradle.properties
  - gradle/libs.versions.toml
  - gradle/wrapper/gradle-wrapper.jar
  - gradle/wrapper/gradle-wrapper.properties
  - gradlew
  - gradlew.bat
  - iosApp/iosApp/Agenda/AgendaViewModel.swift
  - iosApp/iosApp/Model/Data/AgendaContent.swift
  - iosApp/iosApp/Model/Data/PartnersContent.swift
  - iosApp/iosApp/Model/Data/VenueContent.swift
  - iosApp/iosApp/UI/Common/SpeakerPicture.swift
  - iosApp/iosApp/UI/Common/SpeakerView.swift
  - iosApp/iosApp/UI/Speakers/SpeakerDetailsViewModel.swift
  - iosApp/iosApp/UI/Speakers/SpeakersView.swift
  - iosApp/iosApp/UI/Speakers/SpeakersViewModel.swift
  - linters/detekt-config.yml
  - settings.gradle.dcl
  - shared/build.gradle.kts
  - shared/src/commonMain/kotlin/com/gdgnantes/devfest/shared/SharedFrameworkPlaceholder.kt
findings:
  critical: 0
  warning: 2
  info: 1
  total: 3
status: issues_found
---

# Phase 03: Code Review Report

**Reviewed:** 2026-09-27T15:38:01Z
**Depth:** standard
**Files Reviewed:** 210
**Status:** issues_found

## Summary

Phase 3 splits the KMP + Android app into `build-logic` convention plugins, `:core:*`, `:feature:*`
and a thinned `:shared` iOS umbrella. This review read every substantive file (152 flagged by the
orchestrator, plus 6 more it also listed as changed — `androidApp/proguard-rules.pro`,
`gradle/wrapper/gradle-wrapper.{jar,properties}`, `gradlew`, `gradlew.bat`,
`linters/detekt-config.yml` — which turned out to be Gradle/detekt/AGP version-bump content, not
Phase-3 module-extraction content) and spot-checked the remaining ~50 near-pure-move files
(R090–R100) with `git diff -M` to confirm only package/import lines changed, per the reviewer's
own instructions.

Given the DI-wiring risk area called out for this phase, I traced whether classes with plain
`@Inject constructor` living in KMP `core:*` modules (`FirebaseAnalyticsService` in
`core:analytics`, `BookmarksStoreImpl` in `core:data`) — which do **not** apply the Hilt Gradle
plugin or the `dagger-hilt-compiler` KSP processor locally, unlike `core:ui`/`androidApp`/every
`feature:*` module — could still be resolved by the `@Binds` declarations in `:androidApp`'s
`AppModule`. Rather than rely on recollection of Dagger's cross-module annotation-processing
model, I verified this empirically: `./gradlew :androidApp:kspDebugKotlin --offline --rerun-tasks`
ran the full dependency chain from scratch and reported `BUILD SUCCESSFUL`, confirming Dagger
resolves these `@Inject` constructors correctly from the compiled classpath without requiring the
Hilt compiler in the declaring module. This is not a defect.

I also verified: the D-11 export list (`core:model`, `core:data`, `core:analytics` as `api` +
`export()` in `shared/build.gradle.kts`, `core:network` kept `implementation`-only/internal), the
Apollo `@targetName` schema extension (`extra.graphqls`) matching the D-11 amendment exactly, the
Swift call-site diffs (all 9 changed `.swift` files show *only* the `Venue_/Session_/Speaker_/
Room_/Partner_` → `Venue/Session/Speaker/Room/Partner` renames, nothing else), every feature
`*Route.kt` entry point (no `NavController` reference anywhere — D-07 respected), module namespace
uniqueness (D-14, no collisions), and resource-ownership moves (D-08/D-17 — the only
`values`/`values-fr` string-count mismatches found are all `translatable="false"` entries,
correctly excluded from French resources, not missing translations).

No Critical/BLOCKER findings. Two Warnings and one Info below, all in the newly-authored
`build-logic` convention-plugin code (not in the moved application/feature code, which is sound).

## Warnings

### WR-01: `failOnNoDiscoveredTests` disabled build-wide, not scoped to the stated "leaf modules"

**File:** `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidCommon.kt:38-45`
**Issue:** The comment justifies disabling `failOnNoDiscoveredTests` specifically for "leaf modules
(e.g. `:core:ui` today, every future `:feature:*` module)" so `testDebugUnitTest` doesn't fail CI
just because a module hasn't grown tests yet. But `configureAndroidCommon()` is invoked by **both**
`AndroidLibraryConventionPlugin` and `AndroidApplicationConventionPlugin`, so the setting is
applied unconditionally to every Android module including `:androidApp` itself — not just the
leaf modules the comment describes. Today this is harmless because `androidApp/src/test` is also
empty, but the code doesn't actually implement the scoping its own comment promises: once Phase 5
adds real unit tests to `:androidApp` (or any core/feature module), a future regression that
silently drops all discovered tests from a module (e.g. a misconfigured test source set, an
accidentally-`@Ignore`d suite) will no longer fail `testDebugUnitTest` for CI to catch, anywhere in
the graph — the safety net this project explicitly relies on for its stated CI-driven, "green CI
per step" discipline (D-16/D-18) is switched off everywhere, indefinitely.
**Fix:** Move the `failOnNoDiscoveredTests.set(false)` into a mechanism actually scoped to modules
without tests, e.g. gate it behind a small allow-list property per module (`extra` property /
convention plugin parameter), or only apply it from `AndroidLibraryConventionPlugin` (never from
`AndroidApplicationConventionPlugin`) so `:androidApp`'s own unit-test suite keeps the
discoverability safety net once it exists.

### WR-02: Deprecated/no-op Kotlin opt-in compiler flags now duplicated across every Android module (external: none — self-found, confirmed against a live build)

**File:** `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidCommon.kt:50-53`
**Issue:** `freeCompilerArgs.addAll("-Xopt-in=kotlin.RequiresOptIn", "-Xopt-in=kotlin.Experimental")`
was copied verbatim from the pre-split `androidApp/build.gradle.kts` (confirmed via
`git show f42a027^:androidApp/build.gradle.kts`), which is the correct D-16 pure-move behavior.
However, running the build now (`./gradlew :androidApp:kspDebugKotlin --offline --rerun-tasks`)
reproduces a compiler warning for every module that applies this convention:
`Opt-in requirement marker 'kotlin.Experimental' is unresolved. Make sure it's present in the
module dependencies.` Under Kotlin 2.4.20 (this phase's toolchain, per `gradle/libs.versions.toml`)
`kotlin.Experimental` no longer exists, so the flag is a dead no-op. Centralizing this file means
the warning that used to print once (for `:androidApp` only) now prints once per module
(`core:ui`, every `feature:*`, `androidApp`) — pure noise amplification of a pre-existing issue,
now baked into the one place from which it's easiest to fix.
**Fix:** Drop `-Xopt-in=kotlin.Experimental` (and re-verify whether `-Xopt-in=kotlin.RequiresOptIn`
is still needed vs. the modern `-opt-in=kotlin.RequiresOptIn` spelling) while touching this file,
since this convention plugin is a new artifact and not itself bound by the D-16 "preserve exact
line" pure-move rule that applied to the original `androidApp/build.gradle.kts` move.

## Info

### IN-01: `core:testing` currently has zero real consumers

**File:** `build-logic/convention/src/main/kotlin/com/gdgnantes/devfest/buildlogic/AndroidFeatureConventionPlugin.kt:25`, `core/testing/build.gradle.kts`
**Issue:** Every feature module gets `testImplementation(project(":core:testing"))` via the
convention plugin, but no feature module currently has any unit tests that import
`fakeDevFestNantesStore()` (or anything else from `core:testing`) — confirmed via
`find . -type d -name test -path '*/src/*'`, which shows only `androidApp/src/test` (empty) exists
in the whole repo. This is intentional per D-19 ("Phase 4 (DI-05) / Phase 5 extend it") and the
module correctly keeps `DevFestNantesStoreMocked` / `model/stubs/*` in their production modules
(`core:data` / `core:model`) rather than moving them here, so there's no release-code-in-test-jar
risk. Flagging only so a later phase doesn't mistake the module for dead code and delete it.
**Fix:** None needed now; revisit once Phase 5 actually wires tests through it.

---

_Reviewed: 2026-09-27T15:38:01Z_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
