---
phase: 04-hilt-to-koin-di-migration
plan: 03
subsystem: infra
tags: [koin, di, di-gate, ci, hilt-coexistence, feature-about, feature-settings, consent]

requires:
  - phase: 04-hilt-to-koin-di-migration
    provides: "initKoin composition root, sharedModules/androidModules, Hilt-to-Koin bridge pattern, Koin-owned Firebase handles and SharedPreferences, venue tracer template"
provides:
  - "scripts/di-gate.sh: D-13 regression gate (Hilt rule + service-locator rule), modes check/selftest/hits, run in android.yml checks job"
  - "scripts/di-gate-hilt-allowlist.txt: transitional, shrink-only Hilt allowlist (35 lines at end of plan; deleted in 04-07)"
  - "aboutModule (PartnersViewModel via viewModelOf) and settingsModule (DataCollectionViewModel + single consent service)"
  - "Hilt AppModule bridge for DataCollectionSettingsService so the still-Hilt DataSharingInitializer shares the Koin instance"
affects: [04-04, 04-05, 04-06, 04-07]

actuals:
  tokens: 6000
  tasks: 3
  commits: 4

tech-stack:
  added: []
  patterns:
    - "CI shell gate over tracked files with a stale-entry check, so a transitional allowlist can only shrink"
    - "Feature-migration recipe: plain ViewModel, <x>Module with viewModelOf, koinViewModel() default parameter, append to androidModules, Hilt bridge if a Hilt consumer remains, <x>ModuleTest with verify(extraTypes)"
    - "Stateful app-wide service = Koin single + one-line KoinPlatform bridge in the Hilt AppModule (one instance)"

key-files:
  created:
    - scripts/di-gate.sh
    - scripts/di-gate-hilt-allowlist.txt
    - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/di/AboutModule.kt
    - feature/about/src/test/java/com/gdgnantes/devfest/feature/about/di/AboutModuleTest.kt
    - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/di/SettingsModule.kt
    - feature/settings/src/test/java/com/gdgnantes/devfest/feature/settings/di/SettingsModuleTest.kt
  modified:
    - .github/workflows/android.yml
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/di/AndroidModules.kt
    - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/injection/AppModule.kt
    - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/partners/PartnersViewModel.kt
    - feature/about/src/main/java/com/gdgnantes/devfest/feature/about/partners/Partners.kt
    - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/datacollection/DataCollectionViewModel.kt
    - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/datacollection/DataCollectionAgreementDialog.kt
    - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/datacollection/DataCollectionSettingsScreen.kt
    - feature/settings/src/main/java/com/gdgnantes/devfest/feature/settings/services/DataCollectionSettingsService.kt

key-decisions:
  - "Portable regexes in di-gate.sh: the annotation word boundary is spelled ([^A-Za-z0-9_]|$) instead of \\b, which not every grep -E implements"
  - "The settings single is written as a wrapped multi-line single { ... } bind ...::class because the one-line form exceeds detekt's 120-column limit"
  - "The gate scans git-tracked files only, so it must be re-run after staging (git add) new files; a local run before staging does not see them"

patterns-established:
  - "Comments in tracked .kt/.kts/.toml/.xml files must not mention the removed DI framework by name; the gate matches substrings case-insensitively"
  - "Allowlist shrink is part of each migration commit; allowlist line count recorded per commit"

requirements-completed: []  # DI-01, DI-02, DI-04 are also declared by 04-04..04-07; marked complete by the last declaring plan (shared-ID gate)

coverage:
  - id: D1
    description: "scripts/di-gate.sh in CI: Hilt rule with shrink-only allowlist and service-locator rule with the fixed permanent allowlist, proven by selftest"
    requirement: DI-04
    verification:
      - kind: other
        ref: "bash scripts/di-gate.sh selftest = DI-GATE-SELFTEST-OK; bash scripts/di-gate.sh = DI-GATE-OK; bash scripts/di-gate.sh bogus exits 1 with Usage"
        status: pass
      - kind: e2e
        ref: "gh run 38145985879 (Android CI, Checks Linters incl. DI gate step) on 809db11"
        status: pass
    human_judgment: false
  - id: D2
    description: ":feature:about resolves PartnersViewModel from Koin (aboutModule)"
    requirement: DI-02
    verification:
      - kind: unit
        ref: "feature/about/src/test/.../di/AboutModuleTest.kt#about_module_graph_is_complete_given_the_store"
        status: pass
      - kind: unit
        ref: "androidApp/src/test/.../di/AndroidGraphVerifyTest.kt#full_android_graph_is_complete"
        status: pass
    human_judgment: false
  - id: D3
    description: ":feature:settings on Koin with one shared DataCollectionSettingsService instance, persisted preference key literals unchanged"
    requirement: DI-04
    verification:
      - kind: unit
        ref: "feature/settings/src/test/.../di/SettingsModuleTest.kt#settings_module_graph_is_complete_given_upstream_bindings"
        status: pass
      - kind: other
        ref: "string-literal diff of DataCollectionSettingsService.kt vs origin/main = PREFS-KEYS-OK"
        status: pass
      - kind: other
        ref: "debug app on emulator-5554 after pm clear: consent dialog shown on fresh install, tapping Consentir then force-stop and relaunch does not show it again, zero FATAL EXCEPTION in logcat"
        status: pass
    human_judgment: true
    rationale: "Automation exercised fresh-install dialog and consent persistence once; the manual toggle-off/on flow in Settings > Data collection was not driven"
  - id: D4
    description: "Both CI workflows green on the pushed HEAD"
    verification:
      - kind: e2e
        ref: "gh runs 38145985879 (Android CI) and 38145985964 (iOS CI) on 809db11"
        status: pass
    human_judgment: false

duration: 1h 53m
completed: 2026-10-11
status: complete
plan_head_before: 054c24dfef4b351d2c53e9840487bde0277132d3
commits: 4
---

# Phase 4 Plan 3: DI gate, :feature:about and :feature:settings on Koin Summary

**A CI-enforced D-13 gate (Hilt rule with a shrink-only allowlist plus a service-locator rule, proven by selftest) now guards the migration, and `:feature:about` and `:feature:settings` run on Koin, with the stateful consent service a Koin single bridged into the still-Hilt `DataSharingInitializer`; both CI workflows green on 809db11.**

## Performance

- **Duration:** 1h 53m (dominated by CI waits on a loaded host)
- **Started:** 2026-10-11T13:46:48Z
- **Completed:** 2026-10-11T15:40:40Z
- **Tasks:** 3
- **Files modified:** 17 (per `git diff --name-only 054c24d..HEAD` before this SUMMARY)

## Accomplishments

- `scripts/di-gate.sh` (check/selftest/hits) emits `DI-HILT-FOUND`, `DI-LOCATOR-FOUND`, `DI-ALLOWLIST-STALE`, `DI-GATE-OK`, `DI-GATE-SELFTEST-OK`; selftest plants violations in fixtures and proves detection, the permanent locator allowlist (`/di/`, `*Module.kt`, the three named files), the `koinViewModel` exemption, annotation word-boundary and stale-entry detection.
- Transitional allowlist seeded from `hits` (41 files), run as the `DI gate` step of the `checks` job before Detekt.
- `aboutModule` and `settingsModule` appended to `androidModules`; `PartnersViewModel`, `DataCollectionViewModel` and `DataCollectionSettingsServiceImpl` are plain classes; the three call sites use `koinViewModel()` with unchanged ViewModelStoreOwners.
- Hilt `AppModule` `@Binds` for `DataCollectionSettingsService` replaced by a `@AppScope @Provides` `KoinPlatform` bridge, so the Koin `single` is the only instance (T-04-10).

## Allowlist line count per commit

| Commit | Allowlist lines |
|--------|-----------------|
| `8996cd2` Task 1 (seed) | 41 |
| `54bbc6b` Task 2 (about) | 39 (-Partners.kt, -PartnersViewModel.kt) |
| `db04055` Task 3 (settings) | 35 (-4 settings files) |
| `809db11` comment fix | 35 |

## Task Commits

1. **Task 1: D-13 gate, allowlist, selftest, CI step** - `8996cd2` (ci)
2. **Task 2: :feature:about on Koin** - `54bbc6b` (feat)
3. **Task 3: :feature:settings on Koin** - `db04055` (feat)
4. **Gate-triggered comment fix** - `809db11` (fix)

**Plan metadata:** committed with this SUMMARY (docs: complete plan).

## Decisions Made

See `key-decisions` above.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Two 04-02 comments mentioned Hilt**
- **Found during:** Task 1 (seeding the allowlist with `hits`)
- **Issue:** comments in `AnalyticsPlatformModule.android.kt` and `DataPlatformModule.android.kt` matched the Hilt rule, so they would have been seeded as permanent offenders and could never leave the allowlist.
- **Fix:** reworded both comments (no code change).
- **Committed in:** `8996cd2`

**2. [Rule 3 - Blocking] detekt line length on the settings single**
- **Found during:** Task 3
- **Issue:** the one-line `single { DataCollectionSettingsServiceImpl(get(), get(), get(), get()) } bind ...::class` is 121+ columns (MaxLineLength / ArgumentListWrapping).
- **Fix:** wrapped across lines; semantics identical. The plan's literal one-line acceptance string therefore does not match textually; the binding is the same.
- **Committed in:** `db04055`

**3. [Rule 1 - Bug] CI gate flagged a Hilt word in a new file**
- **Found during:** Task 3 CI (run 38144851115, Checks Linters failed with `DI-HILT-FOUND ...SettingsModule.kt:16`)
- **Issue:** my own comment in `SettingsModule.kt` said "Hilt-built". The local gate run passed because the file was still untracked and the gate scans `git ls-files` only.
- **Fix:** reworded the comment; re-ran the gate after `git add`; recorded the untracked-file caveat as a pattern.
- **Committed in:** `809db11`

---

**Total deviations:** 3 auto-fixed (2 blocking, 1 bug). **Impact on plan:** none on scope; the gate proved it bites on its first CI run.

### TDD note

Tasks carry `tdd="true"`, but as in 04-01/04-02 implementation and tests were produced together, so there is no separate failing-test commit. Gate bite is demonstrated by selftest; module graphs by `verify()` tests. No RED commit exists, flagged here rather than fabricated.

## Issues Encountered

- `android run` with no arguments fails ("No apks specified"); the debug APK was passed with `--apks` and the debug application id `com.gdgnantes.devfest.mobile.androidapp.dev` used. `android layout` output has no package field, so the plan's `grep -q <package>` check was replaced by checking the resumed activity via `dumpsys` plus layout text.
- CI waits were long on the loaded host; no environmental failures beyond that.

## Authentication Gates

None.

## User Setup Required

None - no external service configuration required.

## Known Stubs

None.

## Threat Flags

None beyond the plan's threat model. T-04-09 (locator rule in CI, selftest), T-04-10 (single + bridge, PREFS-KEYS-OK, emulator consent check) and T-04-11 (stale-entry check) are mitigated.

## Next Phase Readiness

- Ready for 04-04: recipe and gate in place; allowlist at 35 lines and shrinking per commit; remember to `git add` new files before running the gate.
- Remaining manual check (human_judgment D3): toggle a data-collection service off/on in Settings and relaunch.

---
*Phase: 04-hilt-to-koin-di-migration*
*Completed: 2026-10-11*

## Self-Check: PASSED

- Created files exist: scripts/di-gate.sh, scripts/di-gate-hilt-allowlist.txt, AboutModule.kt, AboutModuleTest.kt, SettingsModule.kt, SettingsModuleTest.kt.
- Commits 8996cd2, 54bbc6b, db04055, 809db11 present and pushed.
- DI-GATE-SELFTEST-OK, DI-GATE-OK, PREFS-KEYS-OK, emulator launch/consent check, CI-GREEN-BOTH 809db11 (runs 38145985879, 38145985964).
