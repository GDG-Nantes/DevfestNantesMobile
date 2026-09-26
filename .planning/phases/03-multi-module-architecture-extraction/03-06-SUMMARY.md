---
phase: 03-multi-module-architecture-extraction
plan: 06
subsystem: android-architecture
tags: [gradle, multi-module, jetpack-compose, hilt, feature-module, resources]

# Dependency graph
requires:
  - phase: 03-multi-module-architecture-extraction (03-05)
    provides: ":core:ui module, resources-gate.sh, fixed Phase 3 module map"
provides:
  - ":feature:venue module (com.gdgnantes.devfest.feature.venue.*): Venue/VenueDetails/VenueViewModel/VenueFloorPlan, LocalUtils/NavigationUtils, callback-only VenueRoute entry point"
  - ":feature:about module (com.gdgnantes.devfest.feature.about.*): About + AboutHeader/AboutLinks/AboutLocalCommunities/AboutSocial/AboutVersion, partners (Partners/PartnerCard/PartnersViewModel), GithubCard, callback-only AboutRoute entry point (versionName/versionCode params)"
  - "app_version string relocated to :core:ui (2+ consumer resource per D-17: :feature:about + androidApp's Settings.kt)"
  - "Proven feature-module extraction pattern (convention plugin, Hilt ViewModel in a leaf, own R, callbacks-only boundary) for 03-07/03-08"
affects: [03-07-feature-settings-speakers, 03-08-feature-agenda-session-detail, 03-09-umbrella-thinning]

# Actuals (#2632)
actuals:
  tokens: 10431
  tasks: 2
  commits: 4

# Plan commit ledger (#3968) — measured, not narrated
commits: 4
plan_head_before: 975c65d77faa4d3c7732bef5544dbdb2c928262c

# Tech tracking
tech-stack:
  added: []
  patterns:
    - "Two-commit module extraction (D-16) reused for feature modules: pure git-mv with packages unchanged + module wiring/compile-enabling edits, then package-rename-only commit + new callback-only Route entry point"
    - "BuildConfig lift pattern (D-07/D-15): a feature composable that needs an app-only value (AboutVersion's version display) takes it as a plain parameter instead of importing the app's BuildConfig; the app supplies the value at the call site"
    - "2+ consumer resource promotion mid-extraction (D-17): app_version discovered to be shared between the extracting feature (about) and a not-yet-extracted app screen (Settings.kt) moved to :core:ui instead of the feature, per the ownership rule, with the app screen's R reference repointed to the already-imported core:ui R alias"

key-files:
  created:
    - feature/venue/build.gradle.kts
    - feature/venue/src/main/AndroidManifest.xml
    - feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/Venue.kt
    - feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/VenueDetails.kt
    - feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/VenueViewModel.kt
    - feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/VenueRoute.kt
    - feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/plan/VenueFloorPlan.kt
    - feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/utils/LocalUtils.kt
    - feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/utils/NavigationUtils.kt
    - feature/venue/src/main/res/values/strings.xml
    - feature/venue/src/main/res/values-fr/strings.xml
    - feature/about/build.gradle.kts
    - feature/about/src/main/AndroidManifest.xml
    - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/About.kt
    - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/AboutHeader.kt
    - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/AboutLinks.kt
    - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/AboutLocalCommunities.kt
    - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/AboutSocial.kt
    - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/AboutVersion.kt
    - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/AboutRoute.kt
    - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/partners/Partners.kt
    - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/partners/PartnerCard.kt
    - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/partners/PartnersViewModel.kt
    - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/components/GithubCard.kt
    - feature/about/src/main/res/values/strings.xml
    - feature/about/src/main/res/values-fr/strings.xml
    - feature/about/src/main/res/drawable-nodpi/about_header.png
    - feature/about/src/main/res/drawable-night-nodpi/about_header.png
    - feature/about/src/main/res/drawable-nodpi/local_communities_logo.png
    - feature/about/src/main/res/drawable/ic_network_youtube.xml
  modified:
    - settings.gradle.dcl
    - androidApp/build.gradle.kts
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/Home.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/settings/Settings.kt
    - androidApp/src/main/res/values/strings.xml
    - androidApp/src/main/res/values-fr/strings.xml
    - core/ui/src/main/res/values/strings.xml

key-decisions:
  - "app_version moved to :core:ui rather than :feature:about because it is referenced by 2 modules at this point in the phase (about + Settings.kt, still in androidApp pending 03-07) — D-17's 2+ consumer rule applied mid-extraction, discovered via git grep before moving resources"
  - "libs.androidx.compose.material (Compose Material2, for MaterialTheme.typography.h5/subtitle2) and libs.timber added as explicit feature/venue and feature/about dependencies — not part of the devfest.android.feature convention plugin's default set, required by files unchanged since before the split"
  - "libs.androidx.compose.material.icons.extended added to feature/venue (Icons.Filled.ZoomIn is not in the compose-material-icons-core curated subset)"

patterns-established:
  - "Feature Route entry point signature = exactly the callbacks/values the app passes at the call site today, with app-only values (BuildConfig) lifted to plain parameters supplied by androidApp"
  - "D-17 2+ consumer resources discovered via git grep at extraction time move to :core:ui even when found late (mid-feature-extraction), not just during the core:ui plan"

requirements-completed: []  # ARCH-02/ARCH-03 are phase-spanning; requirements.ready-ids reports 0/2 ready (sibling plans 03-07/03-08 not yet finished) — not marked complete here.

coverage:
  - id: D1
    description: ":feature:venue extracted as a devfest.android.feature leaf (Venue/VenueDetails/VenueViewModel/VenueFloorPlan/LocalUtils/NavigationUtils), repackaged to com.gdgnantes.devfest.feature.venue.*, with callback-only VenueRoute entry point wired into Home.kt; no androidapp/NavController reference in feature code"
    requirement: "ARCH-02"
    verification:
      - kind: automated_ui
        ref: "gradlew :feature:venue:assembleDebug :androidApp:assembleDebug :androidApp:assembleRelease :androidApp:testDebugUnitTest detekt lint (both tasks' <verify>)"
        status: pass
      - kind: other
        ref: "git grep -n -E 'com\\.gdgnantes\\.devfest\\.androidapp|NavController' -- 'feature/venue/*.kt' -> empty"
        status: pass
      - kind: other
        ref: "./gradlew :feature:venue:dependencies --configuration releaseRuntimeClasspath -> no project :feature: entry"
        status: pass
    human_judgment: false
  - id: D2
    description: ":feature:about extracted as a devfest.android.feature leaf (About + Partners, D-05), repackaged to com.gdgnantes.devfest.feature.about.*, with callback-only AboutRoute entry point (versionName/versionCode params, no BuildConfig import) wired into Home.kt"
    requirement: "ARCH-02"
    verification:
      - kind: automated_ui
        ref: "gradlew :feature:about:assembleDebug :feature:venue:assembleDebug :androidApp:assembleDebug :androidApp:assembleRelease :androidApp:testDebugUnitTest detekt lint (Task 2 <verify>)"
        status: pass
      - kind: other
        ref: "git grep -n -E 'com\\.gdgnantes\\.devfest\\.androidapp|NavController|BuildConfig' -- 'feature/about/*.kt' -> empty"
        status: pass
      - kind: other
        ref: "./gradlew :feature:about:dependencies --configuration releaseRuntimeClasspath -> no project :feature: entry"
        status: pass
    human_judgment: false
  - id: D3
    description: "Resources (venue/about strings + about_header/local_communities_logo/ic_network_youtube drawables) partitioned by ownership rule (D-17), byte-identical to origin/main, no duplicate names across modules; app_version correctly promoted to :core:ui as a 2+ consumer resource"
    requirement: "ARCH-03"
    verification:
      - kind: other
        ref: "resources-gate.sh -> RESOURCES-OK (run after each of the 4 commits)"
        status: pass
    human_judgment: false
  - id: D4
    description: "Both feature extractions pushed and CI green on both workflows (android.yml, ios.yml)"
    requirement: "ARCH-02"
    verification:
      - kind: other
        ref: "CI-GREEN-BOTH — android.yml run 36144514930, ios.yml run 36144514964, HEAD 5a71437"
        status: pass
    human_judgment: false

# Metrics
duration: 60min
completed: 2026-09-25
status: complete
---

# Phase 3 Plan 6: Venue + About Feature Extraction Summary

**Extracted `:feature:venue` and `:feature:about` (About + Partners) as devfest.android.feature leaves with callback-only `VenueRoute`/`AboutRoute` entry points, proving the feature-module pattern; both CI workflows green.**

## Performance

- **Duration:** 60 min
- **Started:** 2026-09-25T13:15:06Z
- **Completed:** 2026-09-25T14:08:37Z
- **Tasks:** 2
- **Files modified:** 37 (across 4 commits since 975c65d)

## Accomplishments
- `:feature:venue` created (Venue/VenueDetails/VenueViewModel/VenueFloorPlan, LocalUtils/NavigationUtils), repackaged to `com.gdgnantes.devfest.feature.venue.*`, with a new callback-only `VenueRoute` entry point; Home.kt's Venue tab now calls `VenueRoute(...)`
- `:feature:about` created (About + AboutHeader/AboutLinks/AboutLocalCommunities/AboutSocial/AboutVersion, Partners/PartnerCard/PartnersViewModel, GithubCard), repackaged to `com.gdgnantes.devfest.feature.about.*`, with a new callback-only `AboutRoute` entry point (`versionName`/`versionCode` params replacing a direct BuildConfig read); Home.kt's About tab now calls `AboutRoute(...)` passing `BuildConfig.VERSION_NAME`/`VERSION_CODE`
- Resources partitioned per D-17: venue's 3 strings and about's 15 strings + 3 drawables moved to their feature module (single consumer each); `app_version` promoted to `:core:ui` after discovering it is a 2-consumer resource (about + Settings.kt, still in androidApp) — `resources-gate.sh` confirmed `RESOURCES-OK` after each commit
- Neither feature module depends on the other or on `:androidApp` (`ARCH-02`); both `./gradlew :feature:<name>:dependencies --configuration releaseRuntimeClasspath` checks show no `project :feature:` entry
- Both CI workflows (android.yml, ios.yml) green on the pushed HEAD (`5a71437`)

## Task Commits

Each task was committed atomically (2 tasks, 4 commits — D-16 two-commit pattern per feature):

1. **Task 1: :feature:venue pure move (packages unchanged)** - `5254385` (feat)
2. **Task 1: repackage :feature:venue to com.gdgnantes.devfest.feature.venue.***  - `00184fd` (feat)
3. **Task 2: :feature:about pure move (packages unchanged) + BuildConfig lift** - `8044668` (feat)
4. **Task 2: repackage :feature:about to com.gdgnantes.devfest.feature.about.***  - `5a71437` (feat)

**Plan metadata:** committed as part of this SUMMARY commit (docs(03-06))

## Files Created/Modified
- `feature/venue/build.gradle.kts` - devfest.android.feature leaf; coil-compose, compose-material (M2), material-icons-extended, timber
- `feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/**` - Venue, VenueDetails, VenueViewModel, VenueRoute (new), plan/VenueFloorPlan, utils/LocalUtils, utils/NavigationUtils
- `feature/venue/src/main/res/**` - venue_go_to_button/venue_image_content_description/venue_plan_content_description strings (values + values-fr)
- `feature/about/build.gradle.kts` - devfest.android.feature leaf; coil-compose, timber
- `feature/about/src/main/java/com/gdgnantes/devfest/feature/about/**` - About, AboutHeader, AboutLinks, AboutLocalCommunities, AboutSocial, AboutVersion, AboutRoute (new), partners/{Partners,PartnerCard,PartnersViewModel}, components/GithubCard
- `feature/about/src/main/res/**` - about/partners strings (values + values-fr) and about_header/local_communities_logo/ic_network_youtube drawables
- `core/ui/src/main/res/values/strings.xml` - `app_version` added (2+ consumer promotion, D-17)
- `androidApp/build.gradle.kts` - `implementation(project(":feature:venue"))`, `implementation(project(":feature:about"))`
- `androidApp/src/main/java/.../ui/screens/Home.kt` - imports + call sites switched to `VenueRoute`/`AboutRoute`; `BuildConfig` import added for the version values
- `androidApp/src/main/java/.../ui/screens/settings/Settings.kt` - `R.string.app_version` repointed to the already-imported `CoreUiR.string.app_version` alias
- `androidApp/src/main/res/values/strings.xml`, `values-fr/strings.xml` - venue/about strings removed (moved out)
- `settings.gradle.dcl` - `include(":feature:venue")`, `include(":feature:about")`

## Decisions Made
- `app_version` promoted to `:core:ui` instead of `:feature:about` — discovered via `git grep` that Settings.kt (still in androidApp, pending 03-07) also reads it, matching D-17's 2+ consumer rule exactly as it did in 03-05
- Compose Material2 (`libs.androidx.compose.material`) and Timber added explicitly to both feature modules — these files' pre-existing imports (`MaterialTheme.typography.h5`, `Timber.w`) were never part of the `devfest.android.feature` convention's default dependency set, only pulled in transitively inside the monolithic `androidApp` module before the split
- `libs.androidx.compose.material.icons.extended` added to `:feature:venue` for `Icons.Filled.ZoomIn`, which is outside the small curated `material-icons-core` set bundled by default

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Added missing compose-material/timber/material-icons-extended dependencies to feature/venue and feature/about**
- **Found during:** Task 1 and Task 2 (initial `:feature:venue:compileDebugKotlin` / `:feature:about:compileDebugKotlin`)
- **Issue:** `androidx.compose.material.MaterialTheme` (M2), `timber.log.Timber`, and `Icons.Filled.ZoomIn` (material-icons-extended) failed to resolve — the `devfest.android.feature` convention plugin only wires core-module + material3 dependencies, not these
- **Fix:** Added `libs.androidx.compose.material`, `libs.timber` to `feature/venue/build.gradle.kts` and `feature/about/build.gradle.kts`; added `libs.androidx.compose.material.icons.extended` to `feature/venue/build.gradle.kts`
- **Files modified:** `feature/venue/build.gradle.kts`, `feature/about/build.gradle.kts`
- **Verification:** `./gradlew :feature:venue:assembleDebug`/`:feature:about:assembleDebug` compile cleanly; full verify (assembleDebug/Release/testDebugUnitTest/detekt/lint) green
- **Committed in:** `5254385` (venue), `8044668` (about)

**2. [Rule 1 - Bug] Fixed import-ordering (detekt ImportOrdering) after R-import edits**
- **Found during:** Task 1 (`:feature:venue:detekt`)
- **Issue:** Manually inserting/reordering R-class imports in `VenueDetails.kt` and `VenueViewModel.kt` broke detekt's required lexicographic import order
- **Fix:** Re-sorted the affected import blocks alphabetically
- **Files modified:** `feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/VenueDetails.kt`, `VenueViewModel.kt`
- **Verification:** `:feature:venue:detekt` passes
- **Committed in:** `00184fd`

---

**Total deviations:** 2 auto-fixed (1 blocking dependency, 1 bug/lint)
**Impact on plan:** Both fixes are mechanical compile/lint corrections required by the module split itself; no scope creep, no behavior change.

## Issues Encountered
None beyond the deviations documented above. Execution was interrupted once by a transient network error (ENOTFOUND) between finishing the `:feature:about` repackage edits and running the verify build; resumed cleanly from the staged working tree with no rework needed.

## Authentication Gates
None.

## User Setup Required
None - no external service configuration required.

## Next Phase Readiness
- Feature-module extraction pattern (convention plugin, Hilt ViewModel in a leaf, own R, callbacks-only Route entry point, resources-gate.sh reuse) proven twice; ready for 03-07 (`:feature:settings`, `:feature:speakers`) and 03-08 (`:feature:agenda`, `:feature:session-detail`)
- `app_version` now lives in `:core:ui`; 03-07's Settings extraction should NOT attempt to move it again — it is already the correct D-17 owner
- ARCH-02/ARCH-03 remain open (phase-spanning); `requirements.ready-ids` confirms 0/2 ready at this point (03-07/03-08 not yet finished), correctly deferred
- No blockers for 03-07

---
*Phase: 03-multi-module-architecture-extraction*
*Completed: 2026-09-25*

## Self-Check: PASSED

- FOUND: feature/venue/build.gradle.kts
- FOUND: feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/VenueRoute.kt
- FOUND: feature/about/build.gradle.kts
- FOUND: feature/about/src/main/java/com/gdgnantes/devfest/feature/about/AboutRoute.kt
- FOUND commit 5254385 in git log
- FOUND commit 00184fd in git log
- FOUND commit 8044668 in git log
- FOUND commit 5a71437 in git log
- Plan commit ledger: plan_head_before 975c65d, commits measured = 4 (git rev-list --count 975c65d..HEAD)
- CI-GREEN-BOTH confirmed: android.yml run 36144514930 (success), ios.yml run 36144514964 (success), HEAD 5a71437
