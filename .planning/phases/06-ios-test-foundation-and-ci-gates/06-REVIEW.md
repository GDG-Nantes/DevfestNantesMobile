---
phase: 06-ios-test-foundation-and-ci-gates
reviewed: 2026-10-27T00:00:00Z
depth: standard
files_reviewed: 15
files_reviewed_list:
  - .github/workflows/ios.yml
  - AGENTS.md
  - documentation/DEVELOPMENT.md
  - iosApp/DevFestNantesTests/AgendaDateParsingTests.swift
  - iosApp/DevFestNantesTests/SessionFilterTests.swift
  - iosApp/iosApp.xcodeproj/project.pbxproj
  - iosApp/iosApp.xcodeproj/xcshareddata/xcschemes/iosApp.xcscheme
  - iosApp/iosApp.xctestplan
  - iosApp/iosApp/Agenda/AgendaViewModel.swift
  - iosApp/iosApp/iOSApp.swift
  - README.md
  - scripts/kmp-nc-version-gate.sh
  - scripts/README.md
  - scripts/swift-names-gate.sh
  - scripts/xcresult-assert-tests.sh
findings:
  critical: 0
  warning: 3
  info: 5
  total: 8
status: issues_found
---

# Phase 6: Code Review Report

**Reviewed:** 2026-10-27
**Depth:** standard
**Files Reviewed:** 15
**Status:** issues_found

## Summary

Reviewed the diff against `5c5ceb8`: the new Swift Testing target and test plan, the pbxproj
rewiring (old `DevFest NantesTests` target and CocoaPods phase removed, new `DevFestNantesTests`
target, KMP-NativeCoroutines pin moved to `1.0.6`), the `iOSApp` test-host short-circuit, the three
gate scripts and the `ios.yml` changes.

I executed `kmp-nc-version-gate.sh` (check and selftest) and `xcresult-assert-tests.sh selftest`
locally: both pass (`KMPNC-VERSION-OK 1.0.6`, `KMPNC-SELFTEST-OK`, `XCRESULT-SELFTEST-OK`). I checked
the epoch constant in `AgendaDateParsingTests` (1760598000 = 2025-10-16T07:00:00Z, correct). The
pbxproj object IDs referenced by the test plan (`48271312CBCC986AA4EA1DCC`) and scheme are
consistent. I found no crash, data-loss or security defect. The remaining issues are robustness and
determinism concerns in the test-host detection and the CI gates.

## Warnings

### WR-01: Test-host detection ships in release builds and can blank the real app

**File:** `iosApp/iosApp/iOSApp.swift:19-25,43-50`
**Issue:** `isRunningTests` is evaluated in every configuration, including Release/App Store. When it
is true, `init()` skips `FirebaseApp.configure()`, `IosDependencies.shared.start()` and performance
traces, and `body` renders `Color.clear`. The heuristic is broad: `NSClassFromString("XCTestCase") != nil`
is true whenever XCTest is loaded into the process, not only for a unit-test host. Any future UI-test
target, or a scheme/diagnostic setting that injects XCTest into the app, would silently produce an
empty app with no Firebase and no Koin, and the failure would look like a UI-test problem. A hidden
"render nothing" path in production code is also needless attack and maintenance surface.
**Fix:** Compile the branch out of release builds:
```swift
#if DEBUG
static let isRunningTests = ProcessInfo.processInfo.environment["XCTestConfigurationFilePath"] != nil
#else
static let isRunningTests = false
#endif
```
Keep the `NSClassFromString` fallback out of the check (or document why Swift Testing needs it), and
make it explicit in `DEVELOPMENT.md` that future UI-test targets must not rely on this switch.

### WR-02: `xcresult-assert-tests.sh` can be bypassed by an ambient env var and ignores the bundle result

**File:** `scripts/xcresult-assert-tests.sh:85-95`
**Issue:** The assertion is the only guard against a zero-test run, yet `XCRESULT_SUMMARY_JSON`,
if present in the environment, replaces the real bundle read and also skips the "bundle exists" check.
The header says "never set it in CI", but nothing enforces it (a repository/organization variable or
a stray `env:` entry would make the gate pass against a fixture). The script also never reads the
summary's own `result` field, so a bundle where the runner died partway (some passed, none counted as
failed) is accepted as `XCRESULT-OK` when the process exit code is the only other defence, which the
header explicitly says it does not trust.
**Fix:** Only honour the override in selftest, e.g. use a private variable set solely by
`selftest_case` (`_XCRESULT_SELFTEST_SUMMARY`) and ignore `XCRESULT_SUMMARY_JSON` otherwise. In
`assert_summary`, additionally fail when `.result` is present and not `"Passed"`:
```bash
result="$(printf '%s' "$summary" | jq -r '.result // empty')"
if [ -n "$result" ] && [ "$result" != "Passed" ]; then echo "XCRESULT-TESTS-FAILED"; exit 1; fi
```
Add a selftest case for it (the selftest zero-test fixture uses `"result":"unknown"`, so only apply
this check after the zero-test check).

### WR-03: CI runs on a floating Xcode and simulator runtime while one test pins OS formatter behaviour

**File:** `.github/workflows/ios.yml:80-133`, `iosApp/DevFestNantesTests/AgendaDateParsingTests.swift:33-41`
**Issue:** The job always selects the newest installed Xcode and the newest iPhone runtime. The
comment "a flaky test must fail the job" is taken seriously, but the environment is not pinned, so a
runner-image update can change the OS and break the build without any repository change.
`falls_back_to_now_for_fractional_seconds` is the concrete exposure: it asserts that Foundation's
default `ISO8601DateFormatter` rejects fractional seconds, i.e. it pins Apple framework behaviour, not
app logic, and will start failing the day a runtime relaxes that. The "fixed in a later phase" intent
is already recorded in `deferred-items.md`.
**Fix:** Either pin the Xcode version and runtime (a `matrix`/env value with a fallback), or at least
make the characterization test robust, for example assert only that the result is not the expected
fractional instant when the formatter rejects it, or mark the expectation with a comment and a
`.bug`/`withKnownIssue` wrapper so a behaviour change reports as an unexpected pass rather than a CI
break.

## Info

### IN-01: Code coverage silently turned off when moving to the test plan

**File:** `iosApp/iosApp.xctestplan:9-11`, `iosApp/iosApp.xcodeproj/xcshareddata/xcschemes/iosApp.xcscheme:27-36`
**Issue:** The scheme previously had `codeCoverageEnabled = "YES"`; the test plan sets
`"codeCoverage": false`, so local runs lose coverage with no mention in the docs.
**Fix:** Set `"codeCoverage": true` in the plan (CI can pass `-enableCodeCoverage NO`), or note the
change as intentional in `DEVELOPMENT.md`.

### IN-02: `FilterType` round-trip test enumerates cases by hand

**File:** `iosApp/DevFestNantesTests/SessionFilterTests.swift:49`
**Issue:** `arguments: [FilterType.bookmark, .complexity, .language, .room, .type]` will not cover a
new case added to `FilterType` (`iosApp/iosApp/Model/SessionFilter.swift:3-5`), which is what a
round-trip test is meant to guard.
**Fix:** Make `FilterType` conform to `CaseIterable` and use `arguments: FilterType.allCases`.

### IN-03: `AGENTS.md` refers to a module that does not exist

**File:** `AGENTS.md` (iOS section, "After changing any Kotlin type exported to Swift ...")
**Issue:** The text lists `core:domain from Phase 7`, but there is no `core/domain` directory and it
is not in `settings.gradle.dcl`. `AGENTS.md` is the single source of truth for agents and should not
carry forward-looking planning references; the module list in the Architecture section does not
include it either.
**Fix:** Drop the `core:domain` mention until the module lands, and add it in the same change as the
module.

### IN-04: `swift-names-gate.sh` fails silently and is macOS-only without a guard

**File:** `scripts/swift-names-gate.sh:107-109,332-334,372`
**Issue:** (a) Under `set -euo pipefail`, `grep -oE 'swift_name...' "$header"` exits 1 when the header
has no matches, so an empty or truncated header ends the script with exit 1 and no message, which
contradicts the README claim that a missing or empty header "fails" with a token. (b) `sed -i ''` is
BSD syntax and breaks on GNU sed; the README says macOS, but the script has no `uname` check, and
`kmp-nc-version-gate.sh` in the same directory advertises "runs anywhere", which invites running both
the same way.
**Fix:** Append `|| true` to the first grep in `derive_sets` and add an explicit "no swift_name found"
`SWIFT-NAME-MISSING` message when `ALL_NAMES` is empty; guard with
`[ "$(uname)" = Darwin ] || { echo "macOS only" >&2; exit 1; }` or use a portable `sed -i.bak`.

### IN-05: `simctl boot ... || true` hides the real boot failure; step summary lines are unformatted

**File:** `.github/workflows/ios.yml:132-133,282-286`
**Issue:** `xcrun simctl boot "$UDID" || true` swallows every error, not just "already booted", so a
genuine boot failure surfaces later as an unrelated `bootstatus` or test-destination error. Separately,
`xcresult-assert-tests.sh ... | tee -a "$GITHUB_STEP_SUMMARY"` appends the raw token lines to the job
summary without markdown formatting, so they collapse into one paragraph.
**Fix:** Tolerate only the benign case (`xcrun simctl boot "$UDID" 2>&1 | grep -q "current state: Booted" || ...`,
or check `simctl list` state first). For the summary, wrap the output in a fenced block or prefix with
`- `.

---

_Reviewed: 2026-10-27_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
