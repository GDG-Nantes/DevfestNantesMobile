---
phase: 07-kmp-domain-layer-and-android-adoption
plan: 03
subsystem: testing
tags: [characterization, graphql, snapshot, test-data]
status: complete

requires: []
provides:
  - "Raw recorded GetSessions/GetSpeakers/GetRooms responses of confetti-app.dev (conference devfestnantes2025) under core/data/testdata/graphql/"
  - "snapshot-meta.json with recording date and speakersPageTotal (metadata only, D-17)"
  - "Gated recorder AgendaSnapshotRecorder and the documented re-record script scripts/record-agenda-snapshot.sh"
affects: [07-04, 07-05, 07-06, 07-08]

tech-stack:
  added: []
  patterns:
    - "Opt-in network test gated by a Gradle property (-PrecordAgendaSnapshot), same style as -PliveNetworkTests"
    - "Test data as files read through absolute paths passed as system properties (devfest.dataTestdata, devfest.domainTestdata, devfest.recordAgendaSnapshot)"

key-files:
  created:
    - core/data/src/jvmTest/kotlin/com/gdgnantes/devfest/core/data/snapshot/AgendaSnapshotRecorder.kt
    - scripts/record-agenda-snapshot.sh
    - core/data/testdata/graphql/GetSessions.json
    - core/data/testdata/graphql/GetSpeakers.json
    - core/data/testdata/graphql/GetRooms.json
    - core/data/testdata/graphql/snapshot-meta.json
  modified:
    - core/data/build.gradle.kts
    - scripts/README.md

key-decisions:
  - "Recorded values are used as recorded: 94 sessions, 100 speakers, 8 rooms; the speakersPage total (110) is kept in snapshot-meta.json as metadata only (D-17, FUT-02 debt)"
  - "kotlinx-serialization-json added to :core:data jvmTest (it was only an implementation dependency of :core:model)"
  - "The re-record script tolerates only Gradle's 'No tests found' for the not-yet-existing AgendaSnapshotTest; any other failure stops it"

requirements-completed: [SHARED-07]

duration: 35 min
completed: 2026-10-28

actuals:
  tokens: 2900
  tasks: 1
  commits: 1
plan_head_before: 0bec387742c1a82c6c6284393b94ff64f443472f
plan_head_after: b0bd8d5dd71b57166021ca5c392be6a8395c421f

coverage:
  deliverables:
    - name: "Raw GraphQL responses recorded and committed"
      human_judgment: false
      verification:
        - kind: command
          ref: "jq -e '.data.sessions.nodes | length > 0' core/data/testdata/graphql/GetSessions.json (and the speakers, rooms and meta equivalents)"
          status: pass
    - name: "Recorder gated behind -PrecordAgendaSnapshot"
      human_judgment: false
      verification:
        - kind: command
          ref: "./gradlew :core:data:jvmTest --tests '*AgendaSnapshotRecorder*' reports 'No tests found for given includes'"
          status: pass
    - name: "Documented re-record script"
      human_judgment: false
      verification:
        - kind: command
          ref: "test -x scripts/record-agenda-snapshot.sh; git grep record-agenda-snapshot scripts/README.md"
          status: pass
---

# Phase 7 Plan 03: Agenda API snapshot recorder Summary

**Gated JVM recorder plus `scripts/record-agenda-snapshot.sh` that capture the app's own GetSessions, GetSpeakers and GetRooms responses (and the server speakersPage total) from confetti-app.dev into committed raw JSON files.**

## Accomplishments

- `AgendaSnapshotRecorder` (jvmTest) posts each query's generated `OPERATION_DOCUMENT` with only the `Content-Type` and `conference: devfestnantes2025` headers, requires HTTP 200 and no GraphQL `errors`, and writes pretty-printed files; one extra `speakersPage(first: 200) { nodes { id } }` request yields the server total for `snapshot-meta.json`.
- `core/data/build.gradle.kts` excludes the recorder from `jvmTest` unless `-PrecordAgendaSnapshot` is set, passes the three system properties, declares the recorded data and the neutral `*.snapshot.json` files as optional test inputs (tolerant of the not-yet-created `core/domain/testdata`), and forces the task to re-run when recording.
- `scripts/record-agenda-snapshot.sh` (executable, last line `AGENDA-SNAPSHOT-RECORDED`) and a `scripts/README.md` section document the re-record workflow.
- Recorded on 2026-10-28: 94 sessions, 100 speakers, 8 rooms (`Belem, Hangar, Jules Verne, L'Atelier, Les Machines, Titan, Tour Bretagne, Unconf`), `speakersPageTotal` 110. All match the RESEARCH expectations; nothing was edited. The JSON key set across the three files is public conference data only (no email, phone or credential fields).

## Task Commits

1. **Task 1: gated recorder, re-record script and raw GraphQL responses** - `b0bd8d5` (test)

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing critical functionality] Re-record script no longer swallows real failures of the second step**
- **Found during:** Task 1
- **Issue:** The plan said the second invocation "finds no test, which is expected"; a plain `|| true` would also hide real `AgendaSnapshotTest` failures once plan 07-04 adds it.
- **Fix:** The script tolerates a non-zero exit only when Gradle printed "No tests found"; anything else exits 1.
- **Files modified:** scripts/record-agenda-snapshot.sh
- **Commit:** b0bd8d5

**2. [Rule 3 - Blocking] kotlinx-serialization-json on the :core:data jvmTest classpath**
- **Found during:** Task 1
- **Issue:** `:core:model` only has it as `implementation`, so the recorder's `buildJsonObject` would not compile.
- **Fix:** `jvmTest.dependencies { implementation(libs.kotlinx.serialization.json) }` (already in the version catalog, no new library, consistent with T-07-SC-03).
- **Files modified:** core/data/build.gradle.kts
- **Commit:** b0bd8d5

**Total deviations:** 2 auto-fixed (1 Rule 2, 1 Rule 3). **Impact:** none on scope.

## Issues Encountered

- A fresh worktree has no gitignored `local.properties`, so Gradle failed at configuration; copied from the main checkout (untracked, not committed).
- The precondition check (live POST returning 200) passed.

## Verification

- `./gradlew :core:data:jvmTest --tests '*AgendaSnapshotRecorder*'` in a default run: "No tests found for given includes" (gate works); with `-PrecordAgendaSnapshot` it passed and wrote the files.
- `./gradlew detekt lint`, `bash scripts/di-gate.sh` (DI-GATE-OK), `bash scripts/kmp-nc-version-gate.sh` (KMPNC-VERSION-OK 1.0.6), `./gradlew testDebugUnitTest jvmTest testAndroidHostTest`: all pass.
- Acceptance: `grep -c 'header(' AgendaSnapshotRecorder.kt` prints 2; `recordAgendaSnapshot` present in build.gradle.kts and the script; README references the script.

## Known Stubs

None.

## Threat Flags

None. The only network surface is the opt-in recorder, already covered by T-07-03 and T-07-05.

## Next Phase Readiness

Plan 07-04 can replay `core/data/testdata/graphql/*.json` offline into the neutral snapshot; the system properties `devfest.dataTestdata` and `devfest.domainTestdata` are already wired on `:core:data:jvmTest`.

## Self-Check: PASSED

- Created files exist (recorder, script, four JSON files) and commit `b0bd8d5` is an ancestor of HEAD.
