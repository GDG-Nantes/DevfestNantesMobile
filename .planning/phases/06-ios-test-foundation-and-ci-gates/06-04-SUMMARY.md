---
phase: 06-ios-test-foundation-and-ci-gates
plan: 04
subsystem: ci
tags: [ci, kotlin-native, swift-interop, gate, github-actions]

requires:
  - phase: 06-03
    provides: ios.yml test lane (shared framework build step, step order) that the gate extends
provides:
  - scripts/swift-names-gate.sh restored from the v1.0 archive (modes baseline, check, selftest; env SWIFT_GATE_HEADER)
  - baselines under scripts/swift-names/ regenerated on the current shared.h
  - ios.yml "Swift names gate" step (check then selftest) right after the shared framework build
  - docs for the gate and the re-baseline rule
affects: [phase-07, phase-08, phase-09, phase-10, phase-11, phase-12]

actuals:
  tokens: 7000
  tasks: 2
  commits: 2
plan_head_before: d6b7962fb7484875db3d8db4188a8760c085298c
plan_head_after: e37cc1e8076b70528b4b2173cc0939b1670e60ba

tech-stack:
  added: []
  patterns: [gate reuses the header of the framework linked by the previous CI step, baseline regenerated only alongside the Swift call-site change]

key-files:
  created:
    - scripts/swift-names-gate.sh
    - scripts/swift-names/names-exclude.txt
    - scripts/swift-names/referenced-names.txt
    - scripts/swift-names/members-baseline.txt
    - scripts/swift-names/shared-h-baseline.txt
  modified:
    - .github/workflows/ios.yml
    - scripts/README.md
    - documentation/DEVELOPMENT.md
    - AGENTS.md

key-decisions:
  - "Files were moved with git mv (history kept); only the five path lines, the header comment and the usage line of the gate were edited, no logic change"
  - "CI runs only check and selftest; baseline mode never runs in CI (documented re-baseline rule)"
  - "SWIFT_GATE_HEADER points at the debug iosSimulatorArm64 header produced by Build shared framework; this was confirmed by the green dispatched run, so no fallback to a second Gradle link was needed"

requirements-completed: [IOSTEST-04]

coverage:
  - id: D1
    description: "swift-names-gate.sh and its four baselines live under scripts/, baseline regenerated on the current shared.h, check prints SWIFT-NAMES-OK and selftest SWIFT-GATE-SELFTEST-OK"
    requirement: IOSTEST-04
    verification:
      - kind: command
        ref: "SWIFT_GATE_HEADER=<linked shared.h> bash scripts/swift-names-gate.sh check -> SWIFT-NAMES-OK; selftest -> SWIFT-GATE-SELFTEST-OK"
        status: pass
    human_judgment: false
  - id: D2
    description: "Renaming the Swift name of a referenced exported type fails check (SWIFT-NAME-MISSING); an empty or missing header fails without SWIFT-NAMES-OK"
    requirement: IOSTEST-04
    verification:
      - kind: command
        ref: "renamed header (Agenda -> AgendaRenamed): exit 1, SWIFT-NAME-MISSING Agenda and SWIFT-MEMBERS-CHANGED Agenda; empty header: exit 1, no SWIFT-NAMES-OK; missing header: exit 1, SWIFT-NAME-MISSING <shared.h not found ...>"
        status: pass
    human_judgment: false
  - id: D3
    description: "ios.yml runs the gate in check and selftest modes after the shared framework build; a dispatched run is green with both tokens in the log"
    requirement: IOSTEST-04
    verification:
      - kind: integration
        ref: "gh workflow run ios.yml, run 40488566873 on feature/reno_phase_6 -> conclusion success; Swift names gate step logs SWIFT-NAMES-OK and SWIFT-GATE-SELFTEST-OK"
        status: pass
    human_judgment: false
  - id: D4
    description: "Docs describe the gate, SWIFT_GATE_HEADER and the re-baseline rule"
    requirement: IOSTEST-04
    verification: []
    human_judgment: true
    rationale: "Documentation adequacy is a reading judgment; only the presence of the script references was grep-checked"

duration: 55 min
completed: 2026-10-27
status: complete
---

# Phase 6 Plan 04: Swift-names gate restored and run in iOS CI Summary

**The v1.0 Swift-visible-name gate now lives in `scripts/` with its baselines under `scripts/swift-names/`, the baseline is regenerated on today's `shared.h`, and `ios.yml` runs `check` then `selftest` on the freshly linked header; dispatched run 40488566873 is green with `SWIFT-NAMES-OK` and `SWIFT-GATE-SELFTEST-OK`.**

## Performance

- **Duration:** about 55 min (dominated by the ~16 min CI run and the local framework link)
- **Started:** 2026-10-27T09:55Z (approx.)
- **Completed:** 2026-10-27T10:48Z
- **Tasks:** 2
- **Files:** 5 renamed (1 script, 4 baselines), 4 modified

## Accomplishments

- Moved `swift-names-gate.sh` and the four `03-*` baselines out of `.planning/milestones/v1.0-phases/03-multi-module-architecture-extraction/` with `git mv` (rename detected at 93% for the script and 100% / 96% for the baselines). Gate edits: `REPO_ROOT` is now the parent of `SCRIPT_DIR`, the four file variables point to `${SCRIPT_DIR}/swift-names/<new name>`, the header comment names the new files and states the re-baseline rule, usage reads `bash scripts/swift-names-gate.sh <baseline|check|selftest>`. No `03-` string remains in the script; `export LC_ALL=C` kept; mode 755 kept.
- Regenerated the baseline on the current header (`./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`, then `baseline` with `SWIFT_GATE_HEADER`): 400 names (was 380), 21 referenced names, 216 member pairs. Only `shared-h-baseline.txt` changed (27 insertions, 7 deletions); `referenced-names.txt`, `members-baseline.txt` and `names-exclude.txt` have no content change, so the "every added name referenced from iosApp/DevFestNantesTests" evidence is not needed.
- Local behavior: `check` -> `SWIFT-NAMES-OK`; `selftest` -> `SWIFT-GATE-SELFTEST-OK`.
- Rename demonstration (header with `swift_name("Agenda")` changed to `AgendaRenamed`): exit 1, `SWIFT-NAME-MISSING Agenda` and `SWIFT-MEMBERS-CHANGED Agenda`. Empty header (0 bytes): exit 1 with no output and no `SWIFT-NAMES-OK`. Missing header: exit 1, `SWIFT-NAME-MISSING <shared.h not found at ...>`.
- `ios.yml`: new step "Swift names gate" between "Build shared framework" and "Run shared and core iOS tests" (lines 147 and 154), `env: SWIFT_GATE_HEADER: ${{ github.workspace }}/shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework/Headers/shared.h`, `run:` executes `check` then `selftest`; no `baseline` call, no `continue-on-error`.
- Docs: `scripts/README.md` (new entry with modes, baseline files, macOS only, `SWIFT_GATE_HEADER`, CI usage, re-baseline rule, exit codes), `documentation/DEVELOPMENT.md` (Code Quality command line plus the ios.yml sentence), `AGENTS.md` (iOS bullet naming core:model, core:data, core:analytics and core:domain from Phase 7).

## CI Confirmation

`ios.yml` workflow_dispatch run **40488566873** on `feature/reno_phase_6` at `e37cc1e` (pushed fast-forward from `d6b7962`): **conclusion success**, wall-clock about 15.8 min (10:27:07Z to 10:42:56Z), within the 30-minute budget. Every step succeeded; "Upload test results" was skipped (no failure).
Log tokens from the "Swift names gate" step: `SWIFT-NAMES-OK` (check), then the selftest output with the expected doctored-copy findings (`SWIFT-MEMBERS-CHANGED Agenda`, `SWIFT-MEMBERS-CHANGED AnalyticsEvent`, `SWIFT-TYPE-COLLISION Agenda.Builder_`, `SWIFT-NAME-COLLISION baseline=3 current=4`) and `SWIFT-GATE-SELFTEST-OK`. The test lane from 06-03 stayed green: `XCRESULT-TESTS total=11 passed=11 failed=0 skipped=0`, `XCRESULT-OK`.
Assumption from the plan ("the header written by embedAndSignAppleFrameworkForXcode is the debug iosSimulatorArm64 header"): confirmed, no `shared.h not found` and no fallback needed.

## Task Commits

1. **Task 1: restore the gate and baselines under scripts/, regenerate the baseline** - `a2acff9` (ci)
2. **Task 2: ios.yml gate step and docs** - `e37cc1e` (ci)

## Archive references (left unedited)

Files under `.planning/milestones/` that still cite the moved gate or the `03-*` baselines (history; they now resolve to `scripts/swift-names-gate.sh` and `scripts/swift-names/`):

- `.planning/milestones/v1.0-ROADMAP.md`
- `.planning/milestones/v1.0-phases/03-multi-module-architecture-extraction/`: `03-01-PLAN.md`, `03-01-SUMMARY.md`, `03-02-PLAN.md`, `03-02-SUMMARY.md`, `03-03-PLAN.md`, `03-03-SUMMARY.md`, `03-09-PLAN.md`, `03-09-SUMMARY.md`, `03-10-PLAN.md`, `03-10-SUMMARY.md`, `03-11-PLAN.md`, `03-11-SUMMARY.md`, `03-REVIEW-FIX.md`, `03-SECURITY.md`, `03-VERIFICATION.md`
- `.planning/milestones/v1.0-phases/04-hilt-to-koin-di-migration/`: `04-01-PLAN.md`, `04-01-SUMMARY.md`, `04-02-PLAN.md`, `04-03-PLAN.md`, `04-07-PLAN.md`, `04-07-SUMMARY.md`, `04-CONTEXT.md`, `04-PATTERNS.md`, `04-RESEARCH.md`, `04-VALIDATION.md`
- `.planning/milestones/v1.0-phases/05-test-coverage-retrofit/`: `05-02-PLAN.md`, `05-03-PLAN.md`, `05-03-SUMMARY.md`, `05-04-PLAN.md`, `05-04-SUMMARY.md`, `05-11-PLAN.md`, `05-11-SUMMARY.md`, `05-RESEARCH.md`, `05-SECURITY.md`

`git show --name-status` of the Task 1 commit under `.planning/milestones/` shows only the five `R` (rename) entries and no `M`.

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 3 - Blocking] Gitignored local files copied into the worktree**
- **Found during:** Task 1 (framework link)
- **Fix:** copied `local.properties` and `iosApp/iosApp/GoogleService-Info.plist` from the main checkout, as instructed by the orchestrator; both are gitignored and not committed.

**2. [Process] Plan verify commands split into plain commands**
- The worktree command guard rejects long compound commands and runtime-built `sed` programs, so the rename demonstration used a literal name (`Agenda`, the first line of `referenced-names.txt`) and the demo headers were written to the session scratchpad instead of `${TMPDIR:-/tmp}`. Same pass/fail evidence as the plan's verify block.

**Total deviations:** 1 blocking environment setup, 1 process note. **Impact:** none on deliverables.

### Deletions

The five v1.0 files under `.planning/milestones/v1.0-phases/03-multi-module-architecture-extraction/` (`swift-names-gate.sh`, `03-swift-names-exclude.txt`, `03-swift-referenced-names.txt`, `03-swift-members-baseline.txt`, `03-shared-h-baseline.txt`) are removed from that directory as renames to `scripts/`. No other deletion.

## Issues Encountered

None. Two transient auto-mode classifier failures on the Task 2 `git add`/`git commit` were retried once and passed.

## Known Stubs

None.

## Threat Flags

None. T-06-09 (baseline tampering): CI runs only `check` and `selftest` (no `baseline` call in `ios.yml`), re-baseline rule documented in `AGENTS.md` and `scripts/README.md`. T-06-10: script logic unchanged, writes only to `mktemp -d` copies. T-06-11: missing and empty headers demonstrated to fail; CI points `SWIFT_GATE_HEADER` at the header just linked.

## Next Phase Readiness

- Phase 7 can add `:core:domain` exports knowing any renamed or missing referenced Swift name fails iOS CI; newly referenced exports require a baseline regeneration in the same commit as the Swift call-site update.

## Self-Check: PASSED

- `scripts/swift-names-gate.sh` (755) and the four files in `scripts/swift-names/` exist; old paths are gone from the index.
- Commits `a2acff9` and `e37cc1e` are ancestors of HEAD; `git rev-list --count d6b7962..HEAD` was 2 before this SUMMARY commit; pushed to `origin/feature/reno_phase_6`.
- ios.yml parses (Ruby Psych); step order 128 < 147 < 154; acceptance greps pass (`SWIFT_GATE_HEADER` x1, no `swift-names-gate.sh baseline`, no `continue-on-error`, script referenced in the three docs).
