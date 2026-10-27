# Phase 6: iOS Test Foundation and CI Gates - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-10-26
**Phase:** 6-iOS Test Foundation and CI Gates
**Areas discussed:** First real test, CI job structure, Gate restoration, KMP-NC bump validation

---

## First real test

### Content of the first tests

| Option | Description | Selected |
|--------|-------------|----------|
| Pure Swift logic | Tests of Swift types with no network and no Koin (`SessionFilter` Codable, `getDate`) | ✓ |
| A single canary test | Trivial test proving the lane runs | |
| Agenda characterization | Freeze the Swift grouping/filtering as the oracle for Phase 9 | |

### Target hosting

| Option | Description | Selected |
|--------|-------------|----------|
| Hosted, init neutralized | `TEST_HOST` kept; `iOSApp.init` skips Firebase, Koin and Remote Config under XCTest | ✓ |
| Hosted, init unchanged | Every run starts Firebase (dummy plist), Koin, Remote Config fetch | |
| Not hosted | Standalone bundle compiling its own sources, no `@testable import` | |

### Old `DevFest NantesTests` target

| Option | Description | Selected |
|--------|-------------|----------|
| Delete, new target | Delete target + 2 files, create a target with a name without spaces | ✓ |
| Keep the target, empty it | Keep the target and replace its content | |

**User's choice:** recommended options on all three questions.

---

## CI job structure

### Splitting

| Option | Description | Selected |
|--------|-------------|----------|
| A single job | `build-for-testing` + `test-without-building` in the existing job | ✓ |
| Two jobs | Gradle/framework then Swift build/test via artifact | |
| Three jobs gates/build/test | Isolated gates on top of the split | |

### `.xcresult` threshold

| Option | Description | Selected |
|--------|-------------|----------|
| Fail if 0 tests | Roadmap criterion, nothing to maintain | ✓ |
| Configurable minimum | `MIN_TESTS` raised at each phase | |

### Robustness (multi-select)

| Option | Description | Selected |
|--------|-------------|----------|
| `.xcresult` artifact on failure | Upload for diagnosis | ✓ |
| Simulator by UDID + boot | `simctl boot` + `bootstatus` | ✓ |
| Parallelism disabled | `-parallel-testing-enabled NO`, no retry | ✓ |
| Job summary | Xcode, simulator, test count in `$GITHUB_STEP_SUMMARY` | ✓ |

---

## Gate restoration

### Location

| Option | Description | Selected |
|--------|-------------|----------|
| `scripts/` + `scripts/swift-names/` | Script next to `di-gate.sh`, baselines without the `03-` prefix | ✓ |
| Everything flat in `scripts/` | Script and 4 `.txt` files in `scripts/` | |
| Baselines under `iosApp/` | Baselines close to the Swift code | |

### Selftest in CI

| Option | Description | Selected |
|--------|-------------|----------|
| Check + selftest | Proves the gate still detects swap and collision | ✓ |
| Check only | Manual selftest | |

### KMP-NC gate

| Option | Description | Selected |
|--------|-------------|----------|
| Dedicated script | `scripts/kmp-nc-version-gate.sh`, fast, runnable locally | ✓ |
| Built into swift-names-gate | A single gate, two responsibilities | |
| Inline step in `ios.yml` | Not runnable locally | |

### `resources-gate.sh`

First choice: "Yes, in this phase". Clarification provided afterwards: the script is a one-shot proof of
**Android** resource migration (comparison with `origin/main` of `androidApp/res`),
irrelevant as is.

| Option | Description | Selected |
|--------|-------------|----------|
| Defer | Idea deferred; iOS resources gate rather in Phase 11 | ✓ |
| Rewrite as an Android invariant | Permanent check wired into `android.yml` | |
| iOS resources gate | Identical `en.lproj`/`fr.lproj` keys in `ios.yml` | |

**User's choice:** Defer.

---

## KMP-NC bump validation

### Parity proof

| Option | Description | Selected |
|--------|-------------|----------|
| Guided manual smoke | Human checkpoint on simulator, screenshots (v1.0 UAT pattern) | ✓ |
| Manual smoke + auto check | Plus a Swift test compiling against the KMP-NC API | |
| Green CI is enough | No runtime verification of the flows | |

### Order

| Option | Description | Selected |
|--------|-------------|----------|
| Test lane first | Target + lane, then gates, then bump; KMP-NC gate in the bump commit | ✓ |
| Bump first | Align the version first | |
| Planner's choice | | |

---

## Claude's Discretion

- Mechanism for detecting "under XCTest" in `iOSApp.init`
- Deployment target and `SWIFT_VERSION` of the new target
- Separate SPM cache and composition of the cache keys beyond the Xcode version
- Names of the `.xctestplan` / of the target, location of the `xcresulttool` parsing
- Associated documentation updates

## Deferred Ideas

- `resources-gate.sh` → iOS resources gate in Phase 11 or dedicated Android invariant
- Configurable minimum test threshold
- Splitting the iOS job if the 30 min timeout is exceeded
