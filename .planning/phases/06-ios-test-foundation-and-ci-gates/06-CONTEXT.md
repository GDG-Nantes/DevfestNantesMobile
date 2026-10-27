# Phase 6: iOS Test Foundation and CI Gates - Context

**Gathered:** 2026-10-26
**Status:** Ready for planning

<domain>
## Phase Boundary

Every PR actually runs Swift tests on the iOS CI (`ios.yml`) and the Kotlin/Swift interop gates
are active: a new Swift Testing target that replaces `DevFest NantesTests`, a
`build-for-testing` / `test-without-building` lane driven by a `.xctestplan`, failure on 0 tests,
Xcode-aware cache, `swift-names-gate.sh` restored and wired into CI, KMP-NativeCoroutines aligned
to 1.0.6 on the SPM side with a version gate. Requirements: IOSTEST-03, IOSTEST-04, CONC-03.
Out of scope: ViewModel tests on fakes (Phase 8, IOSTEST-01), SwiftUI smoke tests
(Phase 12), iOS 17 everywhere (Phase 8), SPM package tests (Phase 11).

</domain>

<decisions>
## Implementation Decisions

### First real test
- **D-01:** The new target contains a few Swift Testing tests of **pure Swift logic**,
  with no network and no Koin: at minimum the `Codable` encoding/decoding of `SessionFilter`
  (`iosApp/iosApp/Model/SessionFilter.swift`, format of the legacy `UserDefaults` keys that
  Phase 9 must read back) and `AgendaViewModel.getDate(date:)` if testable without triggering
  `BaseViewModel`/`IosDependencies`. No trivial canary test, no characterization of the
  agenda (building Kotlin `Session` objects from Swift is too heavy here).
- **D-02:** **App-hosted** target (`TEST_HOST`/`BUNDLE_LOADER` kept, for
  `@testable import` of app code as long as it is not in packages). `iOSApp.init` is
  **neutralized under XCTest**: `FirebaseApp.configure()`, `IosDependencies.shared.start()`,
  `PerformanceMonitoring` traces and `RCValues.sharedInstance` do not run when the
  process is running under test. Production behavior stays strictly identical (parity).
  — **Reversibility:** reversible — guard local to `iOSApp.swift`, removed when Phase 10/11
  moves the logic out of the app host.
- **D-03:** **Delete** the `DevFest NantesTests` target and its two files
  (`DevFest_NantesTests.swift` broken, `MockDevFestNantesStore.swift` entirely commented out);
  create a new target with a name without spaces (e.g. `DevFestNantesTests`), referenced by the
  shared `iosApp` scheme through a committed `.xctestplan`.

### CI job structure
- **D-04:** **A single job** in `ios.yml`: the `xcodebuild build` step is replaced by
  `build-for-testing` then `test-without-building` (a single compilation of the app). The job is
  split into several jobs only if exceeding `timeout-minutes: 30` is measured.
- **D-05:** `.xcresult` assertion: the job **fails if 0 tests** ran (counted via
  `xcresulttool`). No minimum threshold to maintain.
- **D-06:** Robustness measures chosen (all of them): `.xcresult` artifact uploaded on failure;
  simulator resolved **by UDID** (always the latest available iPhone, v1.0 dynamic selection
  kept) then `xcrun simctl boot` + `bootstatus -b`; `-parallel-testing-enabled NO` and
  no retry; Xcode version, simulator and test count written to `$GITHUB_STEP_SUMMARY`.
- **D-07:** The DerivedData cache key includes the Xcode version (`xcodebuild -version`),
  required by success criterion 2.

### Gate restoration
- **D-08:** `swift-names-gate.sh` restored in `scripts/`; its baseline files in
  `scripts/swift-names/`, **without the `03-` prefix** (exclusions, referenced names, members,
  header). Baseline **regenerated against the current `shared.h` header** before wiring. The
  script's internal paths are updated accordingly.
- **D-09:** CI runs `check` **and** `selftest` (the selftest reuses the already linked header;
  it proves the gate still detects an identity swap and a collision).
- **D-10:** KMP-NC gate: **dedicated script** `scripts/kmp-nc-version-gate.sh`, which compares
  `kmpNativeCoroutines` in `gradle/libs.versions.toml` with the pin in `project.pbxproj`
  (must be `kind = exactVersion`), fails if they diverge or if the pin is not exact.
  Designed to later accept a `Package.swift` (Phase 11). Runs early in the job (before
  Gradle) and locally.

### KMP-NC bump validation
- **D-11:** Parity proof after `1.0.0-ALPHA-45` → `1.0.6`: **guided manual smoke**
  (human checkpoint) on simulator: Agenda (sessions, filters, add/remove favorite,
  app relaunch), session detail, Speakers, Venue, About; with screenshots as evidence.
- **D-12:** Order: new target + CI lane first, then gates, then KMP-NC bump under
  the safety net. Separate, reversible commits; the KMP-NC gate is introduced **in the same
  commit** as the bump (otherwise it would fail on `main`).

### Claude's Discretion
- Exact mechanism for detecting "under XCTest" in `iOSApp.init` (env
  `XCTestConfigurationFilePath`, `NSClassFromString("XCTestCase")`…), as long as prod is
  unchanged.
- Deployment target and `SWIFT_VERSION` of the new target (align with the app; iOS 17
  everywhere is in Phase 8, Swift 6 in Phase 12).
- Separate SPM cache (`-clonedSourcePackagesDirPath`), exact composition of the cache keys
  beyond the Xcode version.
- Exact name of the `.xctestplan` and of the target, location of the `xcresulttool` parsing
  (inline or script).
- Updating `README.md`, `documentation/DEVELOPMENT.md`, `scripts/README.md` and
  `AGENTS.md` (iOS test commands, gates) in the same commits (AGENTS.md rule).

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Scope and requirements
- `.planning/ROADMAP.md` §Phase 6 — goal, 4 success criteria, v1.1 cross-cutting rules
- `.planning/REQUIREMENTS.md` — IOSTEST-03, IOSTEST-04, CONC-03 (+ note: obsolete target
  replaced as early as Phase 6)
- `.planning/PROJECT.md` — core value, strict parity

### v1.1 research
- `.planning/research/SUMMARY.md` — sequencing, "Test lane" line and "tests + gates" phase
- `.planning/research/PITFALLS.md` — Pitfall 9 (Swift names / gate), 13 (Swift Testing, hosted
  bundle), 14 (iOS CI: cache, 0 tests, simulator), 17 (KMP-NC skew), 18 (deployment targets)
- `.planning/research/STACK.md` — Swift Testing toolchain, KMP-NC 1.0.6

### Archived v1.0 gates
- `.planning/milestones/v1.0-phases/03-multi-module-architecture-extraction/swift-names-gate.sh`
  — script to restore (baseline/check/selftest modes, `SWIFT_GATE_HEADER` env)
- `.planning/milestones/v1.0-phases/03-multi-module-architecture-extraction/03-swift-names-exclude.txt`,
  `03-swift-referenced-names.txt`, `03-swift-members-baseline.txt`, `03-shared-h-baseline.txt`
  — baselines to move/regenerate
- `scripts/di-gate.sh` — existing gate model (style, output, CI usage)

### Codebase
- `.planning/codebase/TESTING.md`, `.planning/codebase/CONCERNS.md`
- `AGENTS.md` §iOS — iOS build commands, SwiftGen, uncommitted plist

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `.github/workflows/ios.yml`: existing steps to keep (dynamic Xcode selection,
  simulator resolution via `jq`, `embedAndSignAppleFrameworkForXcode` framework build,
  KMP `iosSimulatorArm64Test` tests, SwiftGen, dummy plist, `-resolvePackageDependencies`).
- Archived `swift-names-gate.sh`: links the `iosSimulatorArm64` debug framework of `:shared` and
  reads `Headers/shared.h`; `SWIFT_GATE_HEADER` allows replaying a header without a rebuild.
- `iosApp/iosApp.xcodeproj/xcshareddata/xcschemes/iosApp.xcscheme`: shared scheme with a
  `TestAction` to wire to the `.xctestplan`.

### Established Patterns
- Bash gates in `scripts/` run by CI (`di-gate.sh`), output with prefixed lines
  and `*-OK` on success.
- Dynamic selection of Xcode and the simulator (v1.0 decision): do not pin a version.
- Tests: sentence names, no mocking library; toolchain Swift Testing only.

### Integration Points
- `iosApp/iosApp/iOSApp.swift`: `init()` to neutralize under test (Firebase, Koin, perf, RC).
- `iosApp/iosApp.xcodeproj/project.pbxproj`: removal of the old target, addition of the
  new one, pin `XCRemoteSwiftPackageReference "KMP-NativeCoroutines"`
  (`exactVersion 1.0.0-ALPHA-45` → `1.0.6`); `Package.resolved` to update.
- `gradle/libs.versions.toml`: `kmpNativeCoroutines = "1.0.6"` (the gate's source of truth).
- Mixed deployment targets (15.0 ×4, 17.0 ×2) and `SWIFT_VERSION = 5.0` in the pbxproj.

</code_context>

<specifics>
## Specific Ideas

- The `SessionFilter` tests serve as a safeguard for the persisted format that Phase 9 must
  read back without a migrator.
- KMP-NC smoke modeled on the v1.0 simulator UATs (screenshots).

</specifics>

<deferred>
## Deferred Ideas

- `resources-gate.sh` (v1.0 Phase 3): one-shot proof of **Android** resource migration,
  unusable as is. To reconsider as an iOS gate (`en.lproj`/`fr.lproj`
  `Localizable.strings` with identical keys) in Phase 11 with the SwiftGen resources in
  packages, or as a permanent Android invariant in a dedicated phase.
- Configurable minimum test threshold (beyond "0 tests = failure"): to revisit once
  Phases 8/12 have added tests.
- Splitting the iOS job into several jobs: only if the 30 min timeout is exceeded
  (naturally reconsidered in Phase 11 with the XCFramework).

</deferred>

---

*Phase: 06-ios-test-foundation-and-ci-gates*
*Context gathered: 2026-10-26*
