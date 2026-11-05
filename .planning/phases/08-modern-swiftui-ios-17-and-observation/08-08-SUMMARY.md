---
phase: 08-modern-swiftui-ios-17-and-observation
plan: 08
subsystem: ios-ci
tags: [ios, combine-removal, ci-gate, docs, observation, spm]

requires:
  - phase: 08-modern-swiftui-ios-17-and-observation
    provides: "Every ViewModel on Observation behind ports (08-03 to 08-07); shared AgendaViewModel in the environment (08-07)"
provides:
  - "No Combine, legacy observation API or BaseViewModel left in the iOS sources or the Xcode project"
  - "KMPNativeCoroutinesCombine SPM product removed; package pin 1.0.6 and the Async product unchanged"
  - "scripts/swift-observation-gate.sh (check and selftest), wired into ios.yml before Gradle"
  - "Documentation of the finished Observation architecture, including the Agenda hand-off from 08-07"
  - "deferred-items.md with the D-12 Phase 12 debt list"
affects: [08-09 parity pass, Phase 9, Phase 12]

plan_head_before: 968c2b935b16184614a15541589b03ea65d66a2c
plan_head_after: eff1a1aaa0c55f8a730c27413a257797e01ce28f

actuals:
  tokens: 9700
  tasks: 3
  commits: 3

tech-stack:
  added: []
  patterns:
    - "Permanent text gate per banned API family (scan set from git ls-files, selftest fixtures, never-silent empty scan)"

key-files:
  created:
    - scripts/swift-observation-gate.sh
    - .planning/phases/08-modern-swiftui-ios-17-and-observation/deferred-items.md
  modified:
    - iosApp/iosApp/iOSApp.swift
    - iosApp/iosApp/UI/ContentView.swift
    - iosApp/iosApp/Performance/PerformanceMonitoring.swift
    - iosApp/iosApp.xcodeproj/project.pbxproj
    - .github/workflows/ios.yml
    - scripts/README.md
    - README.md
    - AGENTS.md
    - documentation/DEVELOPMENT.md
    - documentation/ARCHITECTURE.md
    - CONTRIBUTING.md
  deleted:
    - iosApp/iosApp/Model/BaseViewModel.swift

key-decisions:
  - "The gate has no allowlist; the only exclusion is generated SwiftGen output, and comments, tests and the asyncAfter variant are scanned"
  - "A missing pbxproj is a failure token (SWIFT-OBS-PBXPROJ-MISSING), like an empty scan set, so the gate cannot pass by scanning nothing"
  - "An unknown mode exits 2 (the plan's contract), unlike the older gates that exit 1"

patterns-established:
  - "Docs and gate land in the same change as the removal they describe (AGENTS.md documentation rule)"

requirements-completed: [IOSUI-03]

coverage:
  - id: D1
    description: "No Combine import, KMP-NativeCoroutines Combine module, ObservableObject, @Published, objectWillChange or DispatchQueue.main.async in iOS Swift sources; BaseViewModel deleted; Combine product removed from the project"
    requirement: IOSUI-03
    verification:
      - kind: other
        ref: "bash scripts/swift-observation-gate.sh (SWIFT-OBS-GATE-OK) and the Task 1 static grep checks"
        status: pass
      - kind: integration
        ref: "xcodebuild -resolvePackageDependencies, then AgendaCharacterizationTests and SpeakersVenueCharacterizationTests (XCRESULT-OK total=8)"
        status: pass
    human_judgment: false
  - id: D2
    description: "swift-observation-gate.sh detects each pattern, spares a clean Observation file and SwiftGen output, detects the Combine product, and fails on an empty scan set; wired in ios.yml"
    requirement: IOSUI-03
    verification:
      - kind: other
        ref: "bash scripts/swift-observation-gate.sh selftest (SWIFT-OBS-GATE-SELFTEST-OK); mutation check: removing the will-change pattern makes selftest fail on swift-pattern-6"
        status: pass
    human_judgment: false
  - id: D3
    description: "Documentation matches the code (ARCHITECTURE, DEVELOPMENT, CONTRIBUTING, AGENTS, README, scripts/README) including the Agenda hand-off from 08-07; D-12 debt recorded"
    requirement: IOSUI-03
    verification:
      - kind: other
        ref: "Task 3 grep verify (no BaseViewModel in docs, gate/environment/legacy key/bridge/onTermination present, seven APIs in deferred-items.md)"
        status: pass
    human_judgment: true
    rationale: "Whether the prose is accurate and readable is a reviewer judgment; the grep proves only presence of the required statements"
  - id: D4
    description: "Full Swift suite stays green after the removals"
    requirement: IOSUI-03
    verification:
      - kind: integration
        ref: "xcodebuild build-for-testing + test-without-building, iosApp test plan (XCRESULT-OK total=57)"
        status: pass
    human_judgment: false

duration: 55min
completed: 2026-11-05
status: complete
---

# Phase 08 Plan 08: Combine Removal, Observation Gate and Docs Summary

**Combine, `BaseViewModel` and the `KMPNativeCoroutinesCombine` SPM product are gone, `scripts/swift-observation-gate.sh` (check plus selftest, run early in `ios.yml`) keeps them out, and the docs describe the finished Observation architecture; full Swift suite 57/57.**

## Performance

- **Duration:** about 55 min (dominated by two Xcode builds and test runs)
- **Tasks:** 3
- **Files:** 13 touched in the plan's list plus `README.md` (see deviations); 1 deleted, 2 created

## Accomplishments
- Task 1 (tracer): removed the Combine imports from `iOSApp.swift` and `ContentView.swift`, the unused `BaseViewModel` state object, the legacy observable conformance of `PerformanceMonitoring`, `BaseViewModel.swift` with its four project entries, and the Combine product at its three project sites. `KMPNativeCoroutinesCore`, the Async product and the exact 1.0.6 pin are untouched; package resolution re-ran cleanly; `KMPNC-VERSION-OK 1.0.6`.
- Task 2: `scripts/swift-observation-gate.sh`, modeled on `di-gate.sh`: six Swift patterns on any line (comments included; `asyncAfter` caught by the spaced-dot regex), the Combine product in `project.pbxproj`, `SWIFT-OBS-SCAN-EMPTY` and `SWIFT-OBS-PBXPROJ-MISSING` instead of silent passes, SwiftGen output the only exclusion, exit 2 on an unknown mode. Selftest covers 10 pattern fixtures (including a spaced variant, a comment, and `@preconcurrency import`), a clean Observation file with near-miss words, the SwiftGen exclusion (and the same line outside it), both project cases, a missing project and an empty list. `ios.yml` step "Swift observation gate" sits between the KMP-NativeCoroutines gate and the Android setup.
- Task 3: `ARCHITECTURE.md` iOS section rewritten for the final state (Agenda hand-off from 08-07 included: single instance in `ContentView`, `.environment`, `@Environment(AgendaViewModel.self)`, explicit bar-item re-injection, legacy `Favorites` / `AgendaSessionFilters` keys until Phase 9; the 08-02 bridge sentence kept); `DEVELOPMENT.md` Swift-tests section (no Koin start for ViewModel tests, per-test `UserDefaults(suiteName:)`, characterization suites on migrated ViewModels); `CONTRIBUTING.md` iOS example on a port, `@Observable` and `.task`; `deferred-items.md` with the D-12 list and file locations.

## Verification
- Task 1 static greps and `plutil -lint`, `xcodebuild -list`, `-resolvePackageDependencies`: pass. Targeted run: `XCRESULT-TESTS total=8 passed=8 failed=0 skipped=0`, `XCRESULT-OK`. Tracer verified end to end before expanding.
- Task 2: check ends `SWIFT-OBS-GATE-OK`, selftest ends `SWIFT-OBS-GATE-SELFTEST-OK`, `bogus` mode exits non-zero (2), workflow YAML parses, gate step precedes `android-setup`. Mutation check: deleting the will-change alternative from a copy of the script makes selftest fail with `SWIFT-OBS-GATE-SELFTEST-FAILED swift-pattern-6`.
- Task 3: doc greps pass, gate OK; full suite (build-for-testing then test-without-building, parallel testing disabled): `XCRESULT-TESTS total=57 passed=57 failed=0 skipped=0`, `XCRESULT-OK` (plan floor 56).

## Task Commits
1. **Task 1 (tracer): remove Combine, BaseViewModel and the Combine SPM product** - `b2fc5b16` (refactor)
2. **Task 2: Swift observation gate, ios.yml step, docs** - `b93789d6` (ci)
3. **Task 3: iOS architecture docs and D-12 debt list** - `eff1a1aa` (docs)

## Scope justification
13 planned files (above the 10-file tier) as recorded in the plan: Task 1 is one removal across 5 files (the product and its imports must go together), Task 2 adds the gate and its `ios.yml` step plus the docs AGENTS.md requires in the same change, Task 3 is documentation and the D-12 list. Each task stays at or under 5 planned files and ended in its own commit.

## Decisions Made
- No allowlist, no comment skipping; only generated SwiftGen output is excluded (prohibition in the plan).
- Extra failure tokens (`SWIFT-OBS-PBXPROJ-MISSING`) beyond the plan's list, to honor "never a silent pass".

## Deviations from Plan

### Auto-fixed Issues

**1. [Rule 2 - Missing critical documentation] Root README.md gate list updated**
- **Found during:** Task 2
- **Issue:** `README.md` (quick start, "iOS gates") lists the CI gates; AGENTS.md requires docs to change in the same change as what they describe, but the plan's file list omitted it.
- **Fix:** added one line for `swift-observation-gate.sh`.
- **Files modified:** `README.md`
- **Committed in:** `b93789d6`

**2. [Rule 2 - Documentation accuracy] `navigationViewStyle(StackNavigationViewStyle())` added to the D-12 list**
- **Found during:** Task 3
- **Issue:** the grep for `NavigationView` also found the deprecated style modifier in `AboutView.swift`, which belongs to the same replacement.
- **Fix:** listed under the `NavigationView` row of `deferred-items.md`.
- **Committed in:** `eff1a1aa`

### TDD note (Task 2, `tdd="true"`)
The selftest lives inside the gate script, so test and implementation are one file and one commit; no separate RED commit exists. A mutation check stands in for the RED evidence (selftest fails when a pattern is removed). Same shape as the 08-06 and 08-07 notes.

**Total deviations:** 2 auto-fixed (documentation only), 1 TDD process note. **Impact:** none on code or behavior.

## Issues Encountered
- The worktree command guard refuses compound shell lines and `rtk`-rewritten git: the plan's `<automated>` blocks were run as separate plain commands or as scripts in the scratchpad (same checks), and git was invoked as `/usr/bin/git`. The `rtk` grep filter collapses match output, so `rtk proxy grep` was used to read matches.
- Xcode prints "command failed with exit code 0" style noise; exit codes and result bundles were clean.

## Known Stubs
None.

## Threat Flags
None. T-08-18 (regression to Combine) and T-08-19 (gate false negative) are mitigated by the gate, its never-silent empty scan and the selftest; T-08-20 (SPM resolution) by the targeted removal, `-resolvePackageDependencies`, the KMP-NC gate and the full suite.

## Deferred Items
- D-12 deprecated SwiftUI APIs: logged in `deferred-items.md` for Phase 12.
- `scripts/swift-names/shared-h-baseline.txt` stale since Phase 7 (223 `core:domain` names): recorded in `deferred-items.md`, not regenerated.

## Next Phase Readiness
IOSUI-03 and Roadmap Phase 8 criterion 3 are met. 08-09 (parity pass) can proceed: it should visit Speakers > speaker > session, the filter menus, and the shifted-clock initial scroll noted by 08-07.

## Self-Check: PASSED

- Files exist: `scripts/swift-observation-gate.sh`, `deferred-items.md`; `BaseViewModel.swift` is absent.
- Commits `b2fc5b16`, `b93789d6`, `eff1a1aa` are on the branch; measured count since `plan_head_before` is 3.
- Gate check and selftest OK; full suite 57/57.
