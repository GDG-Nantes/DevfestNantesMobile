---
phase: 06-ios-test-foundation-and-ci-gates
plan: 03
subsystem: ci
tags: [ci, github-actions, xcodebuild, xcresult, ios]

requires:
  - phase: 06-01
    provides: DevFestNantesTests target and iosApp.xctestplan
  - phase: 06-02
    provides: eleven real Swift tests to run
provides:
  - scripts/xcresult-assert-tests.sh, zero-test / all-skipped / failure assertion over an .xcresult, with selftest
  - ios.yml test lane (build-for-testing, test-without-building via the test plan, UDID simulator, summary, failure artifact)
  - Xcode-version-aware DerivedData cache
  - Docs for the iOS test lane (DEVELOPMENT.md, README.md, AGENTS.md, scripts/README.md)
affects: [phase-08, phase-09, phase-10, phase-11, phase-12]

actuals:
  tokens: 9000
  tasks: 2
  commits: 2
plan_head_before: 10ec155046dabc3a6dbcb31b45b0bfce59550cda
plan_head_after: 028271280d190293268d55071883592343412fe4

tech-stack:
  added: [actions/upload-artifact@v4]
  patterns: [result-bundle assertion instead of trusting xcodebuild exit code, cache key carrying the toolchain version, step outputs passed through env not interpolated in run]

key-files:
  created:
    - scripts/xcresult-assert-tests.sh
  modified:
    - scripts/README.md
    - .github/workflows/ios.yml
    - documentation/DEVELOPMENT.md
    - README.md
    - AGENTS.md

key-decisions:
  - "Decision order in the assertion: total < 1, then failed > 0, then passed < 1, so a run where every test failed reports XCRESULT-TESTS-FAILED rather than the misleading NO-TESTS-EXECUTED"
  - "An unparseable or unreadable summary fails closed with an extra XCRESULT-SUMMARY-UNREADABLE token"
  - "No separate SPM cache and no -derivedDataPath on the resolve step (as decided in the plan)"

requirements-completed: [IOSTEST-03]

coverage:
  - id: D1
    description: "xcresult-assert-tests.sh fails on zero executed tests, all skipped, any failure, missing bundle, schema drift, unreadable summary; passes on a real passing run"
    requirement: IOSTEST-03
    verification:
      - kind: command
        ref: "bash scripts/xcresult-assert-tests.sh selftest -> XCRESULT-SELFTEST-OK (10 cases)"
        status: pass
      - kind: command
        ref: "real passing bundle (LOCAL LANE) -> XCRESULT-TESTS total=11 passed=11 failed=0 skipped=0 / XCRESULT-OK, exit 0"
        status: pass
      - kind: command
        ref: "real zero-test bundle (-only-testing:DevFestNantesTests/DoesNotExist, xcresulttool totalTestCount 0) -> XCRESULT-NO-TESTS-EXECUTED, exit 1"
        status: pass
    human_judgment: false
  - id: D2
    description: "ios.yml runs build-for-testing then test-without-building through -testPlan iosApp on a UDID-selected booted simulator, asserts the result, summarises, uploads the bundle on failure, keys DerivedData on the Xcode version"
    requirement: IOSTEST-03
    verification:
      - kind: command
        ref: "YAML parse (ruby Psych) + step-order awk check + all grep acceptance criteria for ios.yml"
        status: pass
      - kind: command
        ref: "lane commands (UDID resolution, boot, build-for-testing, test-without-building, assert) run locally on Xcode 27.1"
        status: pass
    human_judgment: true
    rationale: "The dispatched GitHub Actions run (Xcode 26.6 runner, cold cache, 30-minute budget, job summary rendering) was not performed: pushing was not authorised for this run. It is pending."
  - id: D3
    description: "Docs describe the lane and the Swift test command"
    requirement: IOSTEST-03
    verification: []
    human_judgment: true
    rationale: "Documentation adequacy is a reading judgment; only presence of the command and script references was grep-checked"

duration: 25 min
completed: 2026-10-27
status: complete
---

# Phase 6 Plan 03: iOS CI test lane and zero-test assertion Summary

**`ios.yml` now builds the app and its tests once, runs them through `iosApp.xctestplan` on a UDID-selected simulator, and fails the job when the `.xcresult` shows zero executed, all-skipped or failed tests (`scripts/xcresult-assert-tests.sh` with selftest); the DerivedData cache is keyed on the selected Xcode version. The real GitHub Actions run is still pending.**

## Performance

- **Duration:** about 25 min (the cold local `build-for-testing` dominates)
- **Tasks:** 2
- **Files:** 1 created, 5 modified, no deletions

## Accomplishments

- `scripts/xcresult-assert-tests.sh`: reads `xcrun xcresulttool get test-results summary`, counts read via `jq` with `// 0` and a numeric type guard (fail closed), tokens `XCRESULT-TESTS`, `XCRESULT-OK`, `XCRESULT-NO-TESTS-EXECUTED`, `XCRESULT-TESTS-FAILED`, `XCRESULT-MISSING`, `XCRESULT-SUMMARY-UNREADABLE`. `XCRESULT_SUMMARY_JSON` env override feeds `selftest` (10 fixture cases run through the script itself). Mode 100755, `set -euo pipefail`, no `eval`, no fixed test count.
- Evidence on real bundles produced by the verify block: passing run `XCRESULT-TESTS total=11 passed=11 failed=0 skipped=0` then `XCRESULT-OK`; zero-test run (`-only-testing:DevFestNantesTests/DoesNotExist`) `XCRESULT-TESTS total=0 ...` then `XCRESULT-NO-TESTS-EXECUTED`, exit 1; `/nonexistent.xcresult` gives `XCRESULT-MISSING`, exit 1. `ZERO-TEST-XCODEBUILD-RC 0`: xcodebuild itself exits 0 on the zero-test run, which is exactly why the script exists.
- A mutation of the script (`passed -lt 1` weakened to `-lt 0`) makes the selftest fail on `all-skipped`, so the selftest does detect a broken guard.
- `ios.yml`: top-level `permissions: contents: read`; Xcode step has `id: xcode` and outputs `version` (`<version>-<build>`); the DerivedData cache moved right after "Show Xcode version" with the version in the key and both restore-keys (no version-less prefix left); "Resolve and boot latest iPhone simulator" (`id: sim`, outputs `udid` and `name`, fails on empty UDID, `simctl boot ... || true`, `bootstatus -b`); "Build app and tests for testing" (`build-for-testing`, `-testPlan iosApp`, the four signing flags); "Run Swift tests" (`test-without-building`, result bundle in `$RUNNER_TEMP`, `-parallel-testing-enabled NO`, no retry or tolerance); "Assert tests executed" (`shell: bash`, selftest then the real assertion piped through `tee -a "$GITHUB_STEP_SUMMARY"`); "Job summary" (`if: always()`, Xcode, simulator name and UDID via `env:`); "Upload test results" (`if: failure()`, `actions/upload-artifact@v4`, `ios-xcresult`, 7 days, `if-no-files-found: ignore`). Job id, display name, triggers, concurrency and every other step are unchanged; step outputs only reach shells through `env:`.
- Docs: `documentation/DEVELOPMENT.md` (lane description), `README.md` (Swift test command, Swift Testing mention), `AGENTS.md` (iOS bullet and tests bullet), `scripts/README.md` (script entry).

## Task Commits

1. **Task 1: xcresult assertion script, selftest, scripts/README.md entry** - `62e72b3` (ci)
2. **Task 2: ios.yml test lane and lane docs** - `0282712` (ci)

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 1 - Bug] Misleading verdict when every test failed**
- **Found during:** Task 1 design
- **Issue:** The plan's literal order (`passed < 1` before `failed > 0`) reports a run with failures and no passes as `XCRESULT-NO-TESTS-EXECUTED`.
- **Fix:** Order is total < 1, then failed > 0, then passed < 1. All behaviors listed in the plan still hold; one extra selftest case (`everything-failed`) pins it.
- **Files modified:** scripts/xcresult-assert-tests.sh
- **Commit:** 62e72b3

**2. [Rule 2 - Missing critical] Unreadable summary failed with a bare jq/xcresulttool error**
- **Found during:** Task 1 design
- **Issue:** A failing `xcresulttool` or non-JSON output would abort under `set -e` with no token, so the workflow log would not say why.
- **Fix:** Added fail-closed token `XCRESULT-SUMMARY-UNREADABLE` (exit 1) and a selftest case.
- **Files modified:** scripts/xcresult-assert-tests.sh
- **Commit:** 62e72b3

**3. [Rule 3 - Blocking] Gitignored local files copied into the worktree**
- **Found during:** local lane setup
- **Fix:** copied `local.properties` and `iosApp/iosApp/GoogleService-Info.plist` from the main checkout; not committed.

**4. [Process] The plan's third verify command was run with the result bundles in the scratchpad**
- The verify block writes bundles to `${TMPDIR:-/tmp}`; they were written to the session scratchpad directory instead (same commands otherwise, split into plain commands because the worktree command guard rejects long compound commands). Same pass/fail evidence.

### Authorised-scope narrowing (not a plan deviation)

- Task 2 carries a precondition (`gh auth status` and `git push` allowed) and an action tail "push the feature branch, dispatch the workflow and watch it". The orchestrator instructed that pushing and remote CI runs are not authorised for this run, so that tail was not executed. See Pending.

**Total deviations:** 2 auto-fixed (1 bug, 1 missing-critical), 1 blocking environment setup. **Impact:** none on deliverables; both script changes are strictly more conservative.

### Deletions

None (the 33 deleted lines in `ios.yml` are in-file edits; no file removed).

## Pending (needs the orchestrator or user)

- **Dispatched ios.yml run on a pushed branch was not done** (`git push -u origin HEAD`, `gh workflow run ios.yml --ref <branch>`, `gh run watch <id> --exit-status`). Still to confirm there: exit 0; log shows `XCRESULT-SELFTEST-OK` and `XCRESULT-OK`; job summary shows Xcode version, simulator and an `XCRESULT-TESTS` line with `failed=0` and a total of at least 1; wall-clock duration under the 30-minute timeout (D-04, record it); assumption A1 (`xcresulttool` behaviour on Xcode 26.6), A2 (06-01: `XCTestConfigurationFilePath` under Xcode 26.6; the `NSClassFromString` fallback covers it), A3 (`upload-artifact@v4` still accepted; if the runner reports it deprecated, bump to the current major) and A4 (cold run within 30 minutes).
- No `actionlint` or `yamllint` was available locally; the workflow was validated with a Ruby `Psych` parse, the plan's step-order awk check and the grep acceptance criteria. The simulator-resolution step body was executed locally as a standalone script against the real `simctl` output.

## Verification Results

- `bash scripts/xcresult-assert-tests.sh selftest` -> `XCRESULT-SELFTEST-OK`; `bash scripts/di-gate.sh` -> `DI-GATE-OK`.
- Task 1 acceptance: `test -x`, mode 100755, `set -euo pipefail` present, no `eval`, `xcresulttool get test-results summary` referenced, real passing and zero-test bundles behave as specified, README entry present.
- Task 2 acceptance (static part): every grep criterion passes (`-testPlan iosApp` x2, `build-for-testing`, `test-without-building`, `-parallel-testing-enabled NO`, `platform=iOS Simulator,id=` x2, `bootstatus`, no `device_name`, no version-less `-xcode-deriveddata-`, no `continue-on-error|retry-tests-on-failure|test-iterations`, assert script referenced twice, `GITHUB_STEP_SUMMARY` x2, `upload-artifact@v4`, `if: failure()`, `contents: read`, job name unchanged); step order Xcode (73) < cache (90) < build (221) < test (242) < assert (258); `steps.xcode.outputs.version` x4; every `steps.(sim|xcode).outputs` use is an `env:` mapping, a cache key or a restore-key; README, AGENTS and DEVELOPMENT.md greps pass. The CI-run acceptance bullet is Pending above.

## Known Stubs

None.

## Threat Flags

None. T-06-04 to T-06-08 mitigations are in place in `ios.yml` (env-only outputs, read-only token, failure-only 7-day artifact, Xcode-versioned cache, assertion with selftest, no retries).

## Self-Check: PASSED

- `scripts/xcresult-assert-tests.sh` present, executable, mode 100755 in the index; `.github/workflows/ios.yml` parses.
- Commits 62e72b3 and 0282712 are ancestors of HEAD; `git rev-list --count` from the dispatch base is 2 (before this SUMMARY commit).
