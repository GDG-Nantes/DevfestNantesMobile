# Phase 6: Socle de tests et gates CI iOS - Pattern Map

**Mapped:** 2026-10-26
**Files analyzed:** 17 (new/modified/deleted)
**Analogs found:** 12 / 17 (the rest take their shape from verified RESEARCH.md prototypes)

All analog paths below were checked as git-tracked (`git ls-files`). The archived gate lives under
`.planning/milestones/v1.0-phases/03-multi-module-architecture-extraction/` (tracked).

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match Quality |
|---|---|---|---|---|
| `scripts/kmp-nc-version-gate.sh` (new) | gate script | batch / text compare | `scripts/di-gate.sh` | role-match (style) + RESEARCH Ex. E |
| `scripts/xcresult-assert-tests.sh` (new) | gate script | batch | `scripts/di-gate.sh` | role-match (style) + RESEARCH Ex. D |
| `scripts/swift-names-gate.sh` (restored) | gate script | batch | `.planning/milestones/v1.0-phases/03-multi-module-architecture-extraction/swift-names-gate.sh` | exact (move + 5 path edits) |
| `scripts/swift-names/{names-exclude,referenced-names,members-baseline,shared-h-baseline}.txt` | config (baselines) | file-I/O | archived `03-swift-*.txt`, `03-shared-h-baseline.txt` (same dir) | exact (git mv, drop `03-`; regenerate shared-h) |
| `.github/workflows/ios.yml` (modified) | CI config | request-response | itself (current steps) | exact |
| `iosApp/iosApp/iOSApp.swift` (modified) | app entry / guard | event-driven | itself | exact |
| `iosApp/DevFestNantesTests/SessionFilterTests.swift` (new) | test | transform | `iosApp/iosApp/Model/SessionFilter.swift` (subject); RESEARCH Ex. B | no in-repo test analog |
| `iosApp/DevFestNantesTests/AgendaDateParsingTests.swift` (new, 06-02) | test | transform | `iosApp/DevFestNantesTests/SessionFilterTests.swift` (created in 06-01, same suite style); subject `AgendaViewModel.parseDate(_:)` | role-match (in-phase analog) |
| `iosApp/DevFest NantesTests/*` (deleted, 2 files) | test | - | - | n/a (delete) |
| `iosApp/iosApp.xctestplan` (new) | config | - | none in repo | no analog (RESEARCH Pattern 2) |
| `iosApp/iosApp.xcodeproj/xcshareddata/xcschemes/iosApp.xcscheme` (modified) | config | - | itself | exact |
| `iosApp/iosApp.xcodeproj/project.pbxproj` (modified) | config | - | itself (old target objects) | exact |
| `gradle/libs.versions.toml` | config | - | read-only source of truth (`kmpNativeCoroutines = "1.0.6"` line 23) | n/a |
| `iosApp/iosApp/Agenda/AgendaViewModel.swift` (`getDate` extraction to static `parseDate`, mandatory in 06-02, own commit) | viewmodel | transform | itself | exact |
| `scripts/README.md`, `documentation/DEVELOPMENT.md`, `README.md`, `AGENTS.md` | docs | - | `scripts/README.md` entries | role-match |

## Pattern Assignments

### `scripts/kmp-nc-version-gate.sh` and `scripts/xcresult-assert-tests.sh` (gate, batch)

**Analog:** `scripts/di-gate.sh`

**Header / strict mode / paths** (lines 1-2, 38-45):
```bash
#!/usr/bin/env bash
# Dependency-injection regression gate ... (header comment describes rules, modes, tokens, Usage)
set -euo pipefail
# Byte-wise collation so sort/comm agree between separately-sorted lists.
export LC_ALL=C
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
```
**Mode dispatch + usage** (lines 47-51, 255-260):
```bash
MODE="${1:-check}"
if [ "$MODE" != "check" ] && ...; then echo "Usage: $0 [check|selftest|hits]" >&2; exit 1; fi
...
cd "$REPO_ROOT"
case "$MODE" in check) mode_check ;; selftest) mode_selftest ;; esac
```
**Output convention** (lines 112, 138-147): one `PREFIX-TOKEN <detail>` line per violation, exit 1; success prints `DI-GATE-OK`. New scripts follow it: `KMPNC-VERSION-SKEW`, `KMPNC-SPM-NOT-EXACT`, `KMPNC-VERSION-OK`, `XCRESULT-NO-TESTS-EXECUTED`, `XCRESULT-OK`.
**Temp dir cleanup** (lines 53-54): `WORK="$(mktemp -d ...)"; trap 'rm -rf "$WORK"' EXIT`.

Bodies: copy RESEARCH.md Example E (kmp-nc gate, env overrides `KMP_NC_TOML`/`KMP_NC_PBXPROJ`) and Example D (xcresult assert) verbatim; both were verified. Optional: give the kmp-nc gate a small `selftest` in the di-gate style (`expect_contains`, `plant`, lines 160-182), using the env overrides on temp fixtures.

---

### `scripts/swift-names-gate.sh` + `scripts/swift-names/*.txt` (restore)

**Analog:** `.planning/milestones/v1.0-phases/03-multi-module-architecture-extraction/swift-names-gate.sh`
Use `git mv` so history follows. Edits (RESEARCH section G):
1. `REPO_ROOT="$(cd "${SCRIPT_DIR}/../../.." && pwd)"` -> `"${SCRIPT_DIR}/.."`
2. `03-shared-h-baseline.txt` -> `swift-names/shared-h-baseline.txt`
3. `03-swift-referenced-names.txt` -> `swift-names/referenced-names.txt`
4. `03-swift-members-baseline.txt` -> `swift-names/members-baseline.txt`
5. `03-swift-names-exclude.txt` -> `swift-names/names-exclude.txt`
6. Update header comment naming the `03-*.txt` files.
Then run `baseline` (after `git add` of new Swift test files, Pitfall 8); only `shared-h-baseline.txt` should change. Env `SWIFT_GATE_HEADER` replays an existing header; modes `baseline|check|selftest`. Uses BSD `sed -i ''` so macOS only.
Note: Grep of the script for the exact variable lines was not displayed by the tool; planner/executor must locate them with `grep -n "03-" scripts/swift-names-gate.sh` after the move.

---

### `.github/workflows/ios.yml` (CI)

**Analog:** itself. Keep step style (`name:` + `run: |`, `actions/*@v4`, `${{ runner.os }}-` cache keys).

**Cache key to change** (lines 70-79, currently BEFORE Xcode selection at lines 81-88, no Xcode in key):
```yaml
      - name: Cache Xcode DerivedData
        uses: actions/cache@v4
        with:
          path: |
            ~/Library/Developer/Xcode/DerivedData
            build
          key: ${{ runner.os }}-xcode-deriveddata-${{ hashFiles('iosApp/iosApp.xcodeproj/project.pbxproj') }}-${{ hashFiles('shared/**/*.kt', 'core/**/*.kt') }}
          restore-keys: |
            ${{ runner.os }}-xcode-deriveddata-${{ hashFiles('iosApp/iosApp.xcodeproj/project.pbxproj') }}-
            ${{ runner.os }}-xcode-deriveddata-
```
Move it after "Select latest installed Xcode" (give that step `id: xcode`, emit `version`), put `xcode-${{ steps.xcode.outputs.version }}` in key and in every restore-key (RESEARCH Ex. F).

**Simulator step to replace** (lines 96-112, resolves by name only, `device_name` output): swap for UDID resolution (RESEARCH Ex. C) emitting `udid` and `name`, then `simctl boot` + `bootstatus -b`.

**Build step to replace** (lines 206-219): `xcodebuild ... -destination "platform=iOS Simulator,name=${{ steps.sim.outputs.device_name }}" -derivedDataPath build build CODE_SIGNING_ALLOWED=NO CODE_SIGNING_REQUIRED=NO CODE_SIGN_IDENTITY="" PROVISIONING_PROFILE=""`. Keep those four signing flags, change to `-testPlan iosApp ... build-for-testing`, add a `test-without-building` step with `-resultBundlePath "$RUNNER_TEMP/ios-tests.xcresult" -parallel-testing-enabled NO`, `rm -rf` the bundle first, then assert step, step-summary step, `if: failure()` `upload-artifact@v4`.

**Gate insertion points:** `bash scripts/kmp-nc-version-gate.sh` right after checkout (line 25) before `android-setup` (line 27); swift-names `check` + `selftest` after "Build shared framework" (lines 114-128) with `SWIFT_GATE_HEADER` exported. Other existing steps (mock plist lines 153-190, `-resolvePackageDependencies` lines 198-204, KMP iOS tests line 131) stay unchanged. Pitfall 4: do not add `-derivedDataPath` to the resolve step.

---

### `iosApp/iosApp/iOSApp.swift` (app entry, event-driven)

**Analog:** itself (lines 14-39). Current `init` and `body`:
```swift
@main
struct iOSApp: App {
    init() {
        FirebaseApp.configure()
        IosDependencies.shared.start()
        PerformanceMonitoring.startAppStartupTrace()
        _ = RCValues.sharedInstance
        PerformanceMonitoring.stopAppStartupTrace()
    }
    var body: some Scene {
        WindowGroup { ContentView().preferredColorScheme(.dark) }
    }
}
```
Add `static let isRunningTests = ProcessInfo.processInfo.environment["XCTestConfigurationFilePath"] != nil`, `if Self.isRunningTests { return }` as first line of `init`, and in `body` render `Color.clear` under test (RESEARCH Ex. A). Guarding `init` alone is insufficient (ContentView -> BaseViewModel self-starts Koin). Production calls and order unchanged. Optionally OR `NSClassFromString("XCTestCase") != nil` (assumption A2). Leave the file-scope `var remoteConfig` (line 12) untouched (lazy global).

---

### `iosApp/DevFestNantesTests/SessionFilterTests.swift` (test, transform)

**Subject:** `iosApp/iosApp/Model/SessionFilter.swift` lines 3-20:
```swift
enum FilterType: String, Codable { case bookmark, complexity, language, room, type }
struct SessionFilter: Codable, Hashable, Equatable { let type: FilterType; let value: String ... }
```
No existing passing Swift test to copy (old `DevFest_NantesTests.swift` is broken and deleted). Copy RESEARCH Example B (Swift Testing, `@testable import DevFest_Nantes`, snake_case test names, decode fixed legacy payload plus Set round-trip, never byte-compare encoded Set).

---

### `iosApp/DevFestNantesTests/AgendaDateParsingTests.swift` + `iosApp/iosApp/Agenda/AgendaViewModel.swift` (test + extraction, transform)

**Analog:** `SessionFilterTests.swift` (06-01): `import Testing`, `@testable import DevFest_Nantes`, `@Suite struct`, snake_case test names, no ViewModel instance.
**Extraction (mandatory, plan 06-02, own commit):** `static func parseDate(_ date: String) -> Date` on `AgendaViewModel` with the exact current `getDate` body (fresh default-option `ISO8601DateFormatter()`, `?? Date()` fallback); `getDate(date:)` keeps name, signature and call site and returns `AgendaViewModel.parseDate(date)`. Tests call the static member only, so Koin (`BaseViewModel`) never starts. The fractional-seconds fallback is a v1.0 gap: pinned with a "Pins v1.0 behavior" comment and logged in the phase `deferred-items.md`, not fixed.

---

### `iosApp/iosApp.xctestplan` (new) and `iosApp.xcscheme` (modified)

**No in-repo analog for the plan**; use RESEARCH Pattern 2 JSON (`containerPath: container:iosApp.xcodeproj`, identifier = new target blueprint ID, name `DevFestNantesTests`).
**Scheme analog:** `iosApp/iosApp.xcodeproj/xcshareddata/xcschemes/iosApp.xcscheme` lines 25-45: replace the `<Testables>...</Testables>` block (lines 32-44, `BlueprintIdentifier = "CB3979CC2919B78E00BDAE6F"`, `BlueprintName = "DevFest NantesTests"`) with
```xml
<TestPlans>
   <TestPlanReference reference = "container:iosApp.xctestplan" default = "YES"></TestPlanReference>
</TestPlans>
```
(keep `TestAction` attributes; `codeCoverageEnabled = "YES"` is ignored once a plan is default; open question 2).

---

### `iosApp/iosApp.xcodeproj/project.pbxproj`

**Analog:** the old target's objects (RESEARCH Pattern 1 lists every ID: `CB3979CC...` target, `CB3979C9/CA/CB` phases, `CB3979D2` dependency, `CB3979D3/D4/D5` configs, build files `CB3979D0`, `CB4041DB`, file refs `CB3979CD/CF`, `CB4041DA`, group `CB3979CE`, TargetAttributes entry, and the CocoaPods "Check Pods Manifest.lock" phase `454FFC6B2083FF400649C68F` at ~lines 499-516). New target settings: keep `BUNDLE_LOADER = "$(TEST_HOST)"` and `TEST_HOST = "$(BUILT_PRODUCTS_DIR)/DevFest Nantes.app/$(BUNDLE_EXECUTABLE_FOLDER_PATH)/DevFest Nantes"` (lines ~830-851), drop stale `HEADER_SEARCH_PATHS`, set `IPHONEOS_DEPLOYMENT_TARGET = 17.0`, `SWIFT_VERSION = 5.0`. New sources must be explicitly added to the target's Sources phase (objectVersion 54).
**KMP-NC pin** (lines 921-928): change only `version = "1.0.0-ALPHA-45";` to `version = "1.0.6";`, keep `kind = exactVersion;`.
Validate with `plutil -lint` and `xcodebuild -project iosApp/iosApp.xcodeproj -list`. No xcodeproj gem; use a Python script.

---

## Shared Patterns

### Gate output tokens and exit codes
**Source:** `scripts/di-gate.sh` lines 112, 146
**Apply to:** all new/restored scripts: `PREFIX-TOKEN detail` per failure, exit 1; final `*-OK` line; `selftest` mode proving detection (`*-SELFTEST-OK`).

### Strict bash + overridable inputs
**Source:** `scripts/di-gate.sh` lines 38-45. `set -euo pipefail`, `LC_ALL=C`, `SCRIPT_DIR`/`REPO_ROOT` derivation, quoted expansions, no `eval`.

### CI step conventions
**Source:** `.github/workflows/ios.yml`. `actions/*@v4`, `${{ runner.os }}-` keys, no untrusted `${{ }}` in `run:`, no Xcode pin (dynamic latest selection), `shell: bash` where piping to `tee`.

### Docs-in-same-change rule
**Source:** `AGENTS.md` General Guidelines. Add gate entries to `scripts/README.md` (format: `### \`script.sh\`` + short description, usage block, "What it does"), plus iOS test commands in `documentation/DEVELOPMENT.md`, `README.md` and `AGENTS.md` (`## iOS`, and the pre-merge command list); note manual pbxproj edit needed when Dependabot bumps `kmpNativeCoroutines`.

## No Analog Found

| File | Role | Reason |
|---|---|---|
| `iosApp/iosApp.xctestplan` | config | No test plan exists; use RESEARCH Pattern 2 |
| `iosApp/DevFestNantesTests/SessionFilterTests.swift` | test | No working Swift test or Swift Testing usage; use RESEARCH Example B |
| xcresult/UDID shell snippets | CI | Not present in repo; use RESEARCH Examples C, D |

## Metadata

**Analog search scope:** `scripts/`, `.github/workflows/ios.yml`, `iosApp/` (app, test dir, scheme), archived v1.0 phase 3 dir
**Files read:** di-gate.sh, ios.yml, iOSApp.swift, SessionFilter.swift, scheme (partial), scripts/README.md (partial), CONTEXT, RESEARCH
**Pattern extraction date:** 2026-10-26
