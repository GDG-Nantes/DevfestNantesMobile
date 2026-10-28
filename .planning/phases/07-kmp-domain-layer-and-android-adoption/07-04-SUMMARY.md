---
phase: 07-kmp-domain-layer-and-android-adoption
plan: 04
subsystem: testing
tags: [characterization, snapshot, test-data, golden]
status: complete

requires: ["07-03"]
provides:
  - "AgendaSnapshotTest: offline replay of the recorded responses through GraphQLStore over an Apollo MockServer, pinning the snapshot facts and drift-checking the neutral snapshot"
  - "Neutral model snapshot core/domain/testdata/agenda/{sessions,speakers,rooms}.snapshot.json"
  - "com.gdgnantes.devfest.core.testing.snapshot.AgendaSnapshot decoder (sessions/speakers/rooms) in :core:testing"
  - "Shared filter-cases.json (15 cases, 3 real bookmark ids) and README.md specifying snapshot, filter-case and golden formats"
affects: [07-05, 07-06, 07-08, 07-12, 07-13]

tech-stack:
  added: []
  patterns:
    - "Writer (jvmTest in :core:data) and reader (commonMain in :core:testing) split to avoid a project cycle; committed JSON plus README are the contract"
    - "Record mode via system property: the default run compares and fails on drift, only -PrecordAgendaSnapshot rewrites"

key-files:
  created:
    - core/data/src/jvmTest/kotlin/com/gdgnantes/devfest/core/data/snapshot/AgendaSnapshotJson.kt
    - core/data/src/jvmTest/kotlin/com/gdgnantes/devfest/core/data/snapshot/AgendaSnapshotTest.kt
    - core/domain/testdata/agenda/sessions.snapshot.json
    - core/domain/testdata/agenda/speakers.snapshot.json
    - core/domain/testdata/agenda/rooms.snapshot.json
    - core/domain/testdata/agenda/filter-cases.json
    - core/domain/testdata/agenda/README.md
    - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/snapshot/AgendaSnapshot.kt
    - core/testing/src/commonTest/kotlin/com/gdgnantes/devfest/core/testing/snapshot/AgendaSnapshotDecoderTest.kt
  modified:
    - core/testing/build.gradle.kts

key-decisions:
  - "Recorded numbers match the plan: 94 sessions, 17 roomless, 8 rooms, room.id == room.name everywhere, 100 speakers (speakersPage total 110 stays metadata, D-17/FUT-02)"
  - "Bookmark ids: day-1-lunch (roomless service), airightinthebrowserwithchromesbuiltinaiapis (quickie), thefutureofroutingwiththenavigationapi (English conference)"

requirements-completed: [SHARED-07]

duration: 25 min
completed: 2026-10-28

actuals:
  tokens: 14000
  tasks: 1
  commits: 1
plan_head_before: 0a5853b44a6fc5cf7208d8bf6b6b4c02e9c89759
plan_head_after: 4c29611

coverage:
  deliverables:
    - name: "Offline replay facts and drift check"
      human_judgment: false
      verification:
        - kind: command
          ref: "./gradlew :core:data:jvmTest (AgendaSnapshotTest, 5 tests, 0 failures; recorder excluded)"
          status: pass
    - name: "AgendaSnapshot decoder on JVM and iOS simulator"
      human_judgment: false
      verification:
        - kind: command
          ref: "./gradlew :core:testing:jvmTest :core:testing:iosSimulatorArm64Test"
          status: pass
    - name: "Filter cases and format README"
      human_judgment: false
      verification:
        - kind: command
          ref: "jq -e '.cases | length == 15' filter-cases.json; grep README for 'golden missing', 'golden recorded, re-run', 'PrecordGoldens'"
          status: pass
---

# Phase 7 Plan 04: Neutral agenda snapshot, decoder and shared filter cases Summary

**Offline GraphQLStore replay over an Apollo MockServer that pins the recorded agenda facts and drift-checks a committed neutral model snapshot, with an `AgendaSnapshot` decoder in `:core:testing`, 15 shared filter cases and a README fixing the snapshot and golden formats.**

## Accomplishments

- `AgendaSnapshotTest` (5 tests) replays `GetSessions/GetSpeakers/GetRooms` through the real `GraphQLStore`: 94 sessions, 17 without room, 8 rooms, `room.id == room.name`, every start/end a whole minute, 100 speakers vs the recorded server total of 110 (D-17, FUT-02 gap documented, not fixed).
- `neutral_snapshot_matches_the_raw_responses` fails the default run with "neutral snapshot drifted: run scripts/record-agenda-snapshot.sh" and only rewrites in `-PrecordAgendaSnapshot` mode (mitigates T-07-04). A default run leaves `core/domain/testdata` untouched.
- `AgendaSnapshot` decodes the three snapshots into `Session`/`Speaker`/`Room` (strict `ignoreUnknownKeys = false`); `AgendaSnapshotDecoderTest` passes on JVM and iOS simulator.
- `filter-cases.json` holds the 15 ordered cases and three bookmark ids verified to exist in the snapshot; `README.md` documents provenance, re-record command, snapshot/filter formats and all golden formats and rules (default run never writes, record mode via `-PrecordGoldens` / `TEST_RUNNER_DEVFEST_RECORD_GOLDENS`).

## Task Commits

1. **Task 1: replay, neutral snapshot, decoder, filter cases, README** - `4c29611` (test)

## Deviations from Plan

None - plan executed as written. One detekt `MultilineExpressionWrapping` finding in the new decoder was fixed before the commit.

## Verification

- `./gradlew :core:data:jvmTest` (default): green, `AgendaSnapshotTest` 5 tests, recorder not executed; `git status` clean for `core/domain/testdata` afterwards.
- `./gradlew :core:testing:jvmTest :core:testing:iosSimulatorArm64Test`: green.
- `./gradlew detekt lint`, `bash scripts/di-gate.sh` (DI-GATE-OK), `bash scripts/kmp-nc-version-gate.sh` (KMPNC-VERSION-OK 1.0.6), `./gradlew testDebugUnitTest jvmTest testAndroidHostTest`: all pass.
- jq checks: 15 cases; `sessions.snapshot.json` has 94 entries, all with `speakerIds`, 17 with `room == null`.

## Known Stubs

None.

## Threat Flags

None.

## Notes for downstream plans

- The `Session` models decoded by `AgendaSnapshot` carry speakers by id only and no category/abstract/feedback id; goldens relying on those need the raw data.
- `local.properties` had to be copied into the worktree (gitignored, not committed).

## Self-Check: PASSED

- All created files exist and commit `4c29611` is an ancestor of HEAD.
