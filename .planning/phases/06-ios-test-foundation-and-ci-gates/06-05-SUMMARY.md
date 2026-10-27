---
phase: 06-ios-test-foundation-and-ci-gates
plan: 05
subsystem: ci
tags: [spm, kmp-nativecoroutines, gate, ci, github-actions]

requires:
  - phase: 06-04
    provides: ios.yml gates (swift-names, xcresult assertion) that the version gate precedes
provides:
  - scripts/kmp-nc-version-gate.sh (modes check, selftest; env KMP_NC_TOML / KMP_NC_PBXPROJ)
  - KMP-NativeCoroutines SPM pin bumped from 1.0.0-ALPHA-45 to exactVersion 1.0.6
  - ios.yml step "KMP-NativeCoroutines version gate" right after checkout
  - maintainer docs for the manual pin step on Dependabot bumps
affects: [phase-11, 06-06]

actuals:
  tokens: 10500
  tasks: 2
  commits: 2
plan_head_before: 568280da544f29faa6eac97c229fd3cf282a59af
plan_head_after: aac1c81e1a23d19c190b64b84f87d977144259d1

tech-stack:
  added: []
  patterns: [version-skew gate with SPM readers as a list of functions so a Package.swift reader (Phase 11) keeps the token/exit contract, gate and bump land in one commit]

key-files:
  created:
    - scripts/kmp-nc-version-gate.sh
  modified:
    - iosApp/iosApp.xcodeproj/project.pbxproj
    - .github/workflows/ios.yml
    - documentation/DEVELOPMENT.md
    - AGENTS.md
    - scripts/README.md
    - README.md

key-decisions:
  - "Exact string equality between libs.versions.toml and the pbxproj pin (a v-prefix is a skew); the SPM requirement must stay exactVersion"
  - "The pbxproj block is found by brace depth, not indentation, and only the block whose repositoryURL contains KMP-NativeCoroutines is read (firebase's upToNextMajorVersion is never read)"
  - "SPM sources are a list of reader functions (SPM_READERS) so Phase 11 adds a Package.swift reader without changing tokens or exit codes"

requirements-completed: [CONC-03]

coverage:
  - id: D1
    description: "scripts/kmp-nc-version-gate.sh prints KMPNC-VERSION-OK 1.0.6 on the repository and KMPNC-SELFTEST-OK on its fixtures (skew, not-exact, v-prefix, missing gradle key, missing pin, firebase-only non-exact)"
    requirement: CONC-03
    verification:
      - kind: command
        ref: "bash scripts/kmp-nc-version-gate.sh -> KMPNC-VERSION-OK 1.0.6; bash scripts/kmp-nc-version-gate.sh selftest -> KMPNC-SELFTEST-OK"
        status: pass
    human_judgment: false
  - id: D2
    description: "The pre-phase pbxproj (commit 6b211dd, alpha pin) is rejected as a skew"
    requirement: CONC-03
    verification:
      - kind: command
        ref: "KMP_NC_PBXPROJ=<6b211dd project.pbxproj> bash scripts/kmp-nc-version-gate.sh -> exit 1, KMPNC-VERSION-SKEW gradle=1.0.6 spm=1.0.0-ALPHA-45"
        status: pass
    human_judgment: false
  - id: D3
    description: "SPM pin is exactVersion 1.0.6 and the app and its Swift tests build and pass against it, locally and on CI"
    requirement: CONC-03
    verification:
      - kind: integration
        ref: "local lane: build-for-testing + test-without-building -> XCRESULT-TESTS total=11 passed=11 failed=0 skipped=0, XCRESULT-OK; CI run 40501276582 success"
        status: pass
    human_judgment: false
  - id: D4
    description: "Gate is the first step after checkout in ios.yml and the docs (DEVELOPMENT.md, AGENTS.md, scripts/README.md, README.md) describe it and the manual Dependabot pin step"
    requirement: CONC-03
    verification:
      - kind: command
        ref: "awk step-order check on ios.yml; grep checks on the four docs"
        status: pass
    human_judgment: true
    rationale: "Documentation adequacy is a reading judgment; only the presence of the references was grep-checked"
  - id: D5
    description: "Android pre-merge commands pass on the final tree"
    requirement: CONC-03
    verification:
      - kind: command
        ref: "./gradlew detekt lint; bash scripts/di-gate.sh; bash scripts/kmp-nc-version-gate.sh; ./gradlew testDebugUnitTest jvmTest testAndroidHostTest"
        status: pass
    human_judgment: false

duration: 70 min
completed: 2026-10-27
status: complete
---

# Phase 6 Plan 05: KMP-NativeCoroutines SPM pin aligned on 1.0.6 with a version-skew gate Summary

**The Swift package pin moved from `1.0.0-ALPHA-45` to exactVersion `1.0.6` (equal to the Gradle `kmpNativeCoroutines`), guarded by `scripts/kmp-nc-version-gate.sh` that runs first in `ios.yml`; dispatched run 40501276582 is green at 1.0.6 with every gate token.**

## Performance

- **Duration:** about 70 min (local lane and the Android suite, plus a ~14 min CI run)
- **Started:** 2026-10-27T11:29Z
- **Completed:** 2026-10-27T12:38Z
- **Tasks:** 2
- **Files modified:** 7 (1 created)

## Accomplishments

- `scripts/kmp-nc-version-gate.sh` (mode 755, di-gate style): reads `kmpNativeCoroutines = "<v>"` from `gradle/libs.versions.toml`, extracts the KMP-NativeCoroutines `XCRemoteSwiftPackageReference` block from the pbxproj by brace depth, compares kind and version. Tokens: `KMPNC-VERSION-OK <v>`, `KMPNC-VERSION-SKEW gradle=.. spm=..`, `KMPNC-SPM-NOT-EXACT kind=.. (expected exactVersion)`, `KMPNC-GRADLE-VERSION-MISSING`, `KMPNC-SPM-PIN-MISSING`. `selftest` runs 10 fixture cases under `mktemp -d` with trap cleanup (equal pins, firebase-first ordering, skew 1.0.5, range requirement, `v1.0.6` vs `1.0.6`, missing key, missing file, missing pin, missing pbxproj file) and prints `KMPNC-SELFTEST-OK`.
- `project.pbxproj`: one-line change `version = "1.0.0-ALPHA-45";` to `version = "1.0.6";` (numstat 1 added / 1 deleted); kind stays `exactVersion`; no `ALPHA` left; no `Package.resolved` tracked.
- `ios.yml`: step "KMP-NativeCoroutines version gate" (check then selftest) between "Checkout code" and `android-setup`.
- Docs: `documentation/DEVELOPMENT.md` (Code Quality command, new "Bumping KMP-NativeCoroutines" subsection with the Dependabot manual pin step), `AGENTS.md` (bump rule bullet, gate in the pre-merge block), `scripts/README.md` (gate entry), `README.md` (both iOS gates under Testing and Code Quality).

## Verification

Local (Task 1):
- `bash scripts/kmp-nc-version-gate.sh` -> `KMPNC-VERSION-OK 1.0.6`; `selftest` -> `KMPNC-SELFTEST-OK`.
- Before the bump (alpha pin in the tree) the gate printed `KMPNC-VERSION-SKEW gradle=1.0.6 spm=1.0.0-ALPHA-45` and exited 1 (the RED state).
- Deliberate-failure demonstration: pbxproj from commit `6b211dd` through `KMP_NC_PBXPROJ` -> exit 1, `KMPNC-VERSION-SKEW gradle=1.0.6 spm=1.0.0-ALPHA-45`.
- `xcodebuild -resolvePackageDependencies` + `-list` -> `KMPNativeCoroutines: https://github.com/rickclephas/KMP-NativeCoroutines.git @ 1.0.6`.
- LOCAL LANE at 1.0.6 (iPhone 17 simulator, `build-for-testing` then `test-without-building`, no code signing): `XCRESULT-TESTS total=11 passed=11 failed=0 skipped=0`, `XCRESULT-OK`.

Android pre-merge commands on the final tree (run in the worktree; this phase changes no Kotlin/Android source):

| Command | Result |
|---|---|
| `./gradlew detekt lint` | exit 0 |
| `bash scripts/di-gate.sh` | `DI-GATE-OK` |
| `bash scripts/kmp-nc-version-gate.sh` | `KMPNC-VERSION-OK 1.0.6` |
| `./gradlew testDebugUnitTest jvmTest testAndroidHostTest` | BUILD SUCCESSFUL |

CI: `ios.yml` workflow_dispatch run **40501276582** on `feature/reno_phase_6` at `aac1c81` (pushed fast-forward from `568280d`): **conclusion success** (12:07:13Z to 12:21:17Z, about 14 min). Tokens found in the log: `KMPNC-VERSION-OK 1.0.6`, `KMPNC-SELFTEST-OK`, `SWIFT-NAMES-OK`, `SWIFT-GATE-SELFTEST-OK`, `XCRESULT-SELFTEST-OK`, `XCRESULT-TESTS total=11 passed=11 failed=0 skipped=0`, `XCRESULT-OK`; the "Resolve Swift Package Dependencies" and package listings show `KMPNativeCoroutines ... @ 1.0.6`.

Deliberate-failure demonstrations of the phase (all three gates):
1. Zero-test bundle -> `XCRESULT-NO-TESTS-EXECUTED` (06-03, `xcresult-assert-tests.sh`).
2. Renamed Swift name (`Agenda` -> `AgendaRenamed`) -> `SWIFT-NAME-MISSING Agenda` (06-04, `swift-names-gate.sh`).
3. Pre-phase pbxproj -> `KMPNC-VERSION-SKEW gradle=1.0.6 spm=1.0.0-ALPHA-45` (this plan).
Green CI run id with all tokens: **40501276582**.

## Task Commits

1. **Task 1: gate + SPM pin 1.0.6 + first-step CI wiring + maintainer docs, one commit** - `21b58bc` (build)
2. **Task 2: document the iOS gates in scripts/README.md and README.md** - `aac1c81` (docs)

Task 1's commit lists `scripts/kmp-nc-version-gate.sh`, `project.pbxproj` and `ios.yml` together (D-12).

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Selftest EXIT trap referenced a function-local variable**
- **Found during:** Task 1 (first selftest run)
- **Issue:** `KMPNC-SELFTEST-OK` printed but the script exited 1 with `work: unbound variable` because the `trap ... EXIT` ran after `local work` went out of scope under `set -u`.
- **Fix:** made the temp dir variable global (with a comment); selftest now exits 0.
- **Files modified:** scripts/kmp-nc-version-gate.sh (before the Task 1 commit)

**2. [Process] Local environment**
- Copied the gitignored `local.properties` and `GoogleService-Info.plist` from the main checkout (not committed). Compound verify commands were split into plain commands because the worktree guard rejects complex ones; the same pass/fail evidence was collected. The simulator was already booted by another session, so boot was skipped.
- TDD note: the plan requires the gate and the bump in ONE commit, so the RED/GREEN cycle was done in the working tree (gate failed on the alpha pin, passed after the bump) instead of separate test/feat commits.

**Total deviations:** 1 auto-fixed bug, 1 process note. **Impact:** none on deliverables.

### Deletions

None.

## Issues Encountered

None.

## Known Stubs

None.

## Threat Flags

None. T-06-12/T-06-14 mitigated by the gate (exact pin, skew and non-exact tokens, first CI step, documented manual pin step); T-06-13: `ios.yml` sets neither `KMP_NC_TOML` nor `KMP_NC_PBXPROJ`; T-06-SC: same repository, tag 1.0.6 as audited in RESEARCH, behavior parity is checked by the 06-06 human smoke.

## Next Phase Readiness

- 06-06 (parity smoke of the Swift coroutine bridge at 1.0.6) can run: the app builds and its 11 Swift tests pass on 1.0.6. Pitfall 11 (AsyncSequence now cancels Flow collection on deallocation) is not covered by automated tests; the smoke must exercise agenda observation, filters and favorite toggles.
- Phase 11 adds a Package.swift reader to `SPM_READERS` without changing the tokens.

## Self-Check: PASSED

- `scripts/kmp-nc-version-gate.sh` exists (executable); commits `21b58bc` and `aac1c81` are ancestors of HEAD (2 commits since `568280d` before this SUMMARY commit); pushed to `origin/feature/reno_phase_6`.
- Acceptance greps pass: `version = "1.0.6";` present, no `ALPHA`, `kind = exactVersion;` present, pbxproj numstat 1/1, no `Package.resolved` tracked, gate step order checkout < gate < android-setup, docs reference the gate.
