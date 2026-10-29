---
phase: 07-kmp-domain-layer-and-android-adoption
plan: 05
subsystem: testing
tags: [characterization, android, robolectric, golden]
status: complete

requires: ["07-04"]
provides:
  - "GoldenFiles (read / assertMatches) over core/domain/testdata on JVM, Android and iOS; an absent golden fails a default run without writing, record mode writes only absent files"
  - "Golden models and codec (AgendaGolden, FilteringGolden, AndroidSpeakerIndexGolden, IosSpeakerIndexGolden, Goldens) and FilterCases decoder in :core:testing"
  - "AgendaGroupingCharacterizationTest pinning the v1.0 Android grouping under two zones and two locales"
  - "golden/android-agenda.json: v1.0 Android days/slots oracle"
affects: [07-06, 07-08, 07-12, 07-13, 07-15]

tech-stack:
  added: []
  patterns:
    - "Golden support as expect/actual (internal) in :core:testing: system property devfest.domainTestdata on JVM/Android, env DOMAIN_TESTDATA on iOS"
    - "Record mode only via -PrecordGoldens (feature/agenda test tasks); default runs never write"

key-files:
  created:
    - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/snapshot/GoldenFiles.kt
    - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/snapshot/Goldens.kt
    - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/snapshot/FilterCases.kt
    - core/testing/src/androidMain/kotlin/com/gdgnantes/devfest/core/testing/snapshot/TestDataFiles.android.kt
    - core/testing/src/jvmMain/kotlin/com/gdgnantes/devfest/core/testing/snapshot/TestDataFiles.jvm.kt
    - core/testing/src/iosMain/kotlin/com/gdgnantes/devfest/core/testing/snapshot/TestDataFiles.ios.kt
    - core/testing/src/commonTest/kotlin/com/gdgnantes/devfest/core/testing/snapshot/GoldensTest.kt
    - core/testing/src/jvmTest/kotlin/com/gdgnantes/devfest/core/testing/snapshot/GoldenFilesTest.kt
    - feature/agenda/src/test/java/com/gdgnantes/devfest/feature/agenda/characterization/AgendaGroupingCharacterizationTest.kt
    - core/domain/testdata/agenda/golden/android-agenda.json
  modified:
    - feature/agenda/build.gradle.kts
    - feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaColumn.kt

key-decisions:
  - "The expect declarations of TestDataFiles live in GoldenFiles.kt (commonMain); the plan listed only the three actual files"
  - "Slots are keyed by start Instant in the golden; ids inside a slot are canonicalized by (sortIndex, room name, id), null room first"

requirements-completed: [SHARED-07, SHARED-03]

duration: 2 sessions (interrupted by limits)
completed: 2026-10-29

actuals:
  tokens: 17000
  tasks: 1
  commits: 3
plan_head_before: 0196959d0844eb28ea2b65970652ee50eb786be4
plan_head_after: a6aa9bc9beb525b756a3ecb21db2b129b7c33417

coverage:
  deliverables:
    - name: "Golden-file support (JVM, iOS simulator)"
      human_judgment: false
      verification:
        - kind: command
          ref: "./gradlew :core:testing:jvmTest :core:testing:iosSimulatorArm64Test (GoldensTest, GoldenFilesTest)"
          status: pass
    - name: "Android grouping characterization and golden"
      human_judgment: false
      verification:
        - kind: command
          ref: "./gradlew :feature:agenda:testDebugUnitTest --tests '*AgendaGroupingCharacterizationTest*' (default run, twice, --rerun-tasks)"
          status: pass
---

# Phase 7 Plan 05: Android grouping characterization and golden-file support Summary

**Golden-file support in `:core:testing` for JVM, Android and iOS, plus a Robolectric characterization of the unchanged v1.0 agenda grouping (two time zones, two locales) that records `golden/android-agenda.json`.**

## Accomplishments

- `GoldenFiles.assertMatches` fails with `golden missing` in a default run without writing, writes an absent golden only in record mode (then fails with `golden recorded, re-run`), and never overwrites an existing one (`GoldenFilesTest`, 4 tests).
- `Goldens` / `FilterCases` encode and decode every golden format of the 07-04 README (2-space indent, trailing newline), round-trip tested in `GoldensTest` on JVM and iOS simulator.
- `AgendaGroupingCharacterizationTest` drives the real v1.0 `AgendaViewModel`, then `groupSessionsByStartTime` and the LazyColumn `sortedBy { room?.sortIndex }`, under Europe/Paris + fr-FR and Pacific/Auckland + en-US. D-06 holds on the snapshot: each label group has one start Instant, groups are chronological, no session is dropped, and the slots are identical for both combinations. fr-FR and en-US labels of the same slot differ, so the locale is applied.
- Displayed in-slot order is non-decreasing by (sortIndex, name) with a roomless session first (also on a synthetic slot); equal starts merge and distinct starts never share a label.
- `golden/android-agenda.json`: 2 days, 17 and 16 slots, 94 session ids, equal to all 94 snapshot sessions (every snapshot session falls in a day window).
- Production diff: only `private` to `internal` on `groupSessionsByStartTime` (commit `354cce6`).

## Task Commits

1. **Task 1: golden support and Android grouping characterization** - `354cce6` (test)
2. SUMMARY - `0026138` (docs)
3. Detekt `BlankLineBeforeDeclaration` fix in the new test (two blank lines, no behavior change) - `a6aa9bc` (style)

## Deviations from Plan

None - plan executed as written. The `expect` functions of `TestDataFiles` are declared in `GoldenFiles.kt` since no commonMain file of that name was planned.

## Verification

- `./gradlew :core:testing:jvmTest :core:testing:iosSimulatorArm64Test`: green.
- Record run (`-PrecordGoldens`): only the expected failure `golden recorded, re-run`; default runs afterwards (including `--rerun-tasks`) green, nothing written, `git status` clean for the golden.
- `git diff HEAD -- feature/agenda/src/main` before commit: exactly the `groupSessionsByStartTime` visibility line.
- CI list: `./gradlew detekt lint` green (after the style fix), `di-gate.sh` DI-GATE-OK, `kmp-nc-version-gate.sh` KMPNC-VERSION-OK 1.0.6, `./gradlew testDebugUnitTest jvmTest testAndroidHostTest` exit 0 (a Robolectric temp-directory cleanup stack trace is printed but does not fail the build).

## Self-Check: PASSED

- All created files exist; commits `354cce6`, `0026138`, `a6aa9bc` are ancestors of HEAD.

## Known Stubs

None.

## Threat Flags

None.

## Notes for downstream plans

- 07-12 / 07-15 read `agenda/golden/android-agenda.json` through `GoldenFiles.read`; iOS passes `DOMAIN_TESTDATA` (with `SIMCTL_CHILD_` prefix).
- A fresh worktree needs `local.properties` and `androidApp/google-services.json` copied from the main checkout (not committed).
