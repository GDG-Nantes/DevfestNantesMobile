# Phase 8: Modern SwiftUI, iOS 17 and Observation - Pattern Map

**Mapped:** 2026-11-03
**Files analyzed:** 30 (new + modified)
**Analogs found:** 26 / 30 (all analog paths verified git-tracked)

## File Classification

| New/Modified File | Role | Data Flow | Closest Analog | Match |
|---|---|---|---|---|
| `iosApp/iosApp.xcodeproj/project.pbxproj` (deploy target 15.0 -> 17.0, lines ~688/744) | config | n/a | the four target-level configs already at 17.0 | exact |
| `iosApp/iosApp/UI/About/AboutViewModel.swift` (modify) | viewmodel | request-response (first emission) | itself (v1.0) + Pattern 2 in RESEARCH | self |
| `iosApp/iosApp/UI/About/AboutSource.swift` (new) | port + live adapter | streaming -> AsyncThrowingStream | `Performance/PerformanceMonitoring.swift` (seam), RESEARCH Pattern 1 | partial |
| `iosApp/iosApp/UI/Venue/VenueViewModel.swift`, `VenueSource.swift` | viewmodel + port | request-response (`asyncFunction`) | `UI/Speakers/SpeakersViewModel.swift` | role-match |
| `iosApp/iosApp/UI/Speakers/SpeakersViewModel.swift`, `SpeakersSource.swift` | viewmodel + port | streaming first emission | itself (v1.0) | self |
| `iosApp/iosApp/UI/Speakers/SpeakerDetailsViewModel.swift`, `SpeakerDetailsSource.swift` | viewmodel + port | request-response | `SpeakersViewModel.swift` | role-match |
| `iosApp/iosApp/Agenda/AgendaViewModel.swift`, `AgendaSource.swift` | viewmodel + port | streaming + UserDefaults persistence | itself (v1.0) | self |
| `iosApp/iosApp/Agenda/AgendaView.swift`, `AgendaDetailView.swift`, `AgendaCellView.swift`, filter menus, `AgendaNavigationBarItems.swift`, `ClearFiltersButton.swift` | view | event-driven | existing views | self |
| `iosApp/iosApp/UI/ContentView.swift` | view/root | n/a | itself | self |
| `iosApp/iosApp/Model/BaseViewModel.swift` (delete or de-Observable) | model | n/a | itself | self |
| `iosApp/iosApp/Performance/PerformanceMonitoring.swift` (+ `DataLoadTracing`) | service | request-response | itself, lines 71-75 | self |
| `iosApp/iosApp/iOSApp.swift` (drop Combine imports) | app entry | n/a | itself | self |
| `iosApp/DevFestNantesTests/Fakes/*.swift` (Fake*Source, RecordingDataLoadTracing) | test fakes | streaming | `CharacterizationSupport.swift` (TestData) | role-match |
| `iosApp/DevFestNantesTests/<Feature>ViewModelTests.swift` x5 | test | request-response | `SpeakersVenueCharacterizationTests.swift` | exact |
| `scripts/swift-observation-gate.sh` (new) | script/gate | batch | `scripts/di-gate.sh` (+ `kmp-nc-version-gate.sh`) | exact |
| `.github/workflows/ios.yml` (add step) | config | n/a | "KMP-NativeCoroutines version gate" step, lines 32-35 | exact |
| `scripts/README.md`, `AGENTS.md`, `documentation/ARCHITECTURE.md` (~l.324), `DEVELOPMENT.md` | docs | n/a | existing gate entries | exact |

No analog: `Bridge.stream` generic AsyncSequence-to-AsyncThrowingStream helper, `DataLoadTracing` protocol, AsyncStream-handshake fakes (use RESEARCH Patterns 1, 2, 4 and Test Code Examples; no existing Swift equivalent). Android seam named in CONTEXT (Phase 4 `PerformanceMonitoring` interface) is conceptual only.

## Pattern Assignments

### ViewModels (About, Venue, Speakers, SpeakerDetails, Agenda)

**Analog:** `iosApp/iosApp/UI/Speakers/SpeakersViewModel.swift` (v1.0, lines 9-55), the file to convert.

v1.0 shape to replace (imports lines 9-14, class lines 16-20):
```swift
import Combine
import KMPNativeCoroutinesAsync
@MainActor
class SpeakersViewModel: BaseViewModel {
    @Published var speakersContent: [Speaker]?
    @Published var isLoading = true
    private let performanceMonitoring = PerformanceMonitoring.shared
    private let logger = DevFestLogger(category: "SpeakersViewModel")
```
Load body (lines 23-51) keeps: `PerformanceMonitoring.TRACE_SPEAKERS_LOAD`, `dataSource: "graphql"`, first-emission `break`, logger message `"Observe Speakers error: ..."`. Replace `DispatchQueue.main.async { ... }` with direct assignment, add `catch is CancellationError { return }` and `try Task.checkCancellation()` after the loop (RESEARCH Pitfall 1). Target shape is RESEARCH Pattern 2 (`@Observable @MainActor final class`, `@ObservationIgnored` ports/logger, init defaults `source: = LiveSpeakersSource()`, `tracing: = PerformanceMonitoring.shared`).

**AboutViewModel** (`UI/About/AboutViewModel.swift` lines 16-45): keep `isLoading = true; partnersContent = []` reset at start (D-10), `PartnerContent` grouping with sorted keys, error log `DevFestLogger(category: "About").log(.error, "Observe Partners error: ...", error: error)`. Port returns `[PartnerCategory: [Partner]]` stream.

**BaseViewModel** (`Model/BaseViewModel.swift` lines 11-18): `ObservableObject` holder of `IosDependencies.shared.store()` in its init. Live adapters must read the store lazily inside methods (RESEARCH Pitfall 3/4); delete the class once the last VM is migrated.

Constraints from existing characterization tests (Pitfall 5): keep `speakersContent` settable, `getAlphabets()`, `speakers(for:)`, `sectionDateFormatter` mutable internal var (`@ObservationIgnored`), `sessionsChanged(sessions:)`, `agendaContent.sections`, `VenueViewModel().currentLanguage`, static `AgendaViewModel.parseDate`.

### Port + live adapter files (`<Feature>Source.swift`)

No in-repo analog; copy RESEARCH Pattern 1:
```swift
enum Bridge {
    static func stream<S: AsyncSequence>(_ make: @escaping () -> S) -> AsyncThrowingStream<S.Element, Error> {
        AsyncThrowingStream { continuation in
            let task = Task {
                do { for try await v in make() { continuation.yield(v) }; continuation.finish() }
                catch { continuation.finish(throwing: error) }
            }
            continuation.onTermination = { _ in task.cancel() }
        }
    }
}
// Bridge.stream { asyncSequence(for: IosDependencies.shared.store().speakers) }
```
Only these files import `KMPNativeCoroutinesAsync`. One-shot calls use `asyncFunction(for:)` (throws `CancellationError` on cancel).

### `DataLoadTracing` (in `Performance/PerformanceMonitoring.swift` or own file)

**Analog:** `PerformanceMonitoring.swift` lines 71-75 (verified signature):
```swift
func trackDataLoad<T>(traceName: String, dataSource: String = "unknown",
                      operation: @escaping () async throws -> T) async throws -> T {
```
Protocol uses this exact signature without the default value; conform with `extension PerformanceMonitoring: DataLoadTracing {}` (D-06 text says `block:` but the real label is `operation:`).

### Views (ownership and lifecycle)

Use RESEARCH Pattern 3: `@State private var viewModel = X()`, `.task { await viewModel.load() }`, `State(initialValue:)` in `init` for `SpeakerDetails`/Venue, `@Environment(AgendaViewModel.self)` + `@Bindable var viewModel = viewModel` in body. `ContentView` owns `@State private var agendaViewModel = AgendaViewModel()` and `.environment(agendaViewModel)` on the `TabView`; remove its `import Combine` and unused `@StateObject var viewModel = BaseViewModel()`. `AgendaView` splits rooms and sessions into two `.task` modifiers (Pitfall 2) and replaces the `DispatchQueue.main.async` at ~lines 211-213 with direct assignment (Pitfall 6).

### Test suites (`<Feature>ViewModelTests.swift`)

**Analog:** `iosApp/DevFestNantesTests/SpeakersVenueCharacterizationTests.swift`

```swift
import Testing
import Foundation
import shared
@testable import DevFest_Nantes

@MainActor @Suite struct SpeakersVenueCharacterizationTests {
    @Test func speaker_alphabet_matches_the_golden() throws {
        TestHost.start()
        let viewModel = SpeakersViewModel()
        viewModel.speakersContent = try TestData.speakers()
```
Conventions: `@MainActor @Suite struct`, snake_case sentence test names, `@testable import DevFest_Nantes`, Kotlin values from `TestData.speakers()` / `TestData.sessions()` (`CharacterizationSupport.swift`), write `DevFest_Nantes.SessionFilter` when `shared` is imported. New fake-based tests need no `TestHost.start()`. Test bodies: RESEARCH "Code Examples" (first-emission, silent cancellation via AsyncStream handshake, Agenda defaults with `UserDefaults(suiteName:)`). Existing tests keep calling `SpeakersViewModel()` / `VenueViewModel()` so init defaults must remain.

### Fakes (`iosApp/DevFestNantesTests/Fakes/`)

Hand-written, `final class ... : @unchecked Sendable`, no mocking library; `RecordingDataLoadTracing` records `traceName`/`dataSource` and runs `operation()`. Hand-built Kotlin values: `Venue(address:description:floorPlanUrl:latitude:longitude:imageUrl:name:)`, `Partner(name:logoUrl:url:)`, `Room(id:name:sortIndex:)`.

### `scripts/swift-observation-gate.sh`

**Analog:** `scripts/di-gate.sh`. Copy: header comment block documenting rules/modes/usage (lines 1-36); `set -euo pipefail`, `export LC_ALL=C`; `SCRIPT_DIR`/`REPO_ROOT` resolution (lines 38-45); mode validation (47-51); temp dir with trap (53-54):
```bash
WORK="$(mktemp -d "${TMPDIR:-/tmp}/di-gate.XXXXXX")"
trap 'rm -rf "$WORK"' EXIT
```
`scan_set` over tracked files (line 67-69):
```bash
git -C "$REPO_ROOT" ls-files -- '*.kt' ... | { grep -v '^\.planning/' || true; } | sort -u
```
-> use `'iosApp/**/*.swift'` excluding `iosApp/iosApp/SwiftGen/`. Matcher structure: `*_lines <file>` with `{ grep -n -E "$RE" "$1" || true; } | cut -d: -f1 | sort -n -u` (lines 80-82); `run_checks <root> <list>` printing `SWIFT-OBS-FOUND <path>:<line> <pattern>` and nothing when clean (96-136); `mode_check` prints output and `exit 1`, else `SWIFT-OBS-GATE-OK` (138-147); helpers `expect_contains`, `expect_clean`, `plant` (160-182) and `mode_selftest` planting one violation per pattern in `$WORK/fx` and ending `SWIFT-OBS-GATE-SELFTEST-OK` (184-253); dispatch `case "$MODE"` (255-260). Extra pbxproj rule for `KMPNativeCoroutinesCombine` (pattern from `kmp-nc-version-gate.sh`, which reads `iosApp/iosApp.xcodeproj/project.pbxproj`; header lines 1-50 show its token/selftest documentation style). Patterns: `import Combine`, `KMPNativeCoroutinesCombine`, `ObservableObject`, `@Published`, `objectWillChange`, `DispatchQueue\.main\.async`. Keep Swift comments free of these words.

### `.github/workflows/ios.yml`

**Analog:** lines 30-35, add a sibling step right after (text-only, before Gradle):
```yaml
      - name: KMP-NativeCoroutines version gate
        run: |
          bash scripts/kmp-nc-version-gate.sh
          bash scripts/kmp-nc-version-gate.sh selftest
```
New step "Swift observation gate" runs `check` and `selftest` the same way. The Swift names gate step (line 154-159) shows the multi-line `run:` style.

### `project.pbxproj` edits

**Analog:** `SessionFilterTests.swift` entries; each new file needs four entries:
- `PBXBuildFile` (line 67): `F58B5AB8B24860681567F470 /* SessionFilterTests.swift in Sources */ = {isa = PBXBuildFile; fileRef = 07FA17E61A9DDADF5C507A31 /* SessionFilterTests.swift */; };`
- `PBXFileReference` (line 141): `... = {isa = PBXFileReference; lastKnownFileType = sourcecode.swift; path = SessionFilterTests.swift; sourceTree = "<group>"; };`
- tests group `70708D92D3C406142E250997` children (line 370-377 region)
- tests `Sources` phase `521F86961B5FB3C2DD590C59` (line 601-609 region); app Sources phase is `7555FF77242A565900829871`.
New `Fakes` folder needs a new `PBXGroup` inside the tests group. Combine product removal sites: line 19 (`PBXBuildFile` 003EC8DB...), line 154 (app Frameworks phase), lines 937-941 (`XCSwiftPackageProductDependency` 003EC8DA...). Do not touch `MARKETING_VERSION`.

## Shared Patterns

### Logging
**Source:** `DevFestLogger` (`iosApp/iosApp/Analytics/DevFestLogger.swift`); usage `DevFestLogger(category: "SpeakersViewModel").log(.error, "...: \(error.localizedDescription)")`. Never log `CancellationError`.

### First emission only + silent cancellation
Apply to all streaming VMs: `for try await x in source.stream() { first = x; break }`, then `try Task.checkCancellation()`, `catch is CancellationError { return }`, other errors log and set `isLoading = false`.

### Lazy Koin access
Apply to every `Live...Source`: call `IosDependencies.shared.store()` inside methods, never in init (Pitfall 3).

### Gate script wiring
Apply to `swift-observation-gate.sh`: also document in `scripts/README.md`, the `AGENTS.md` "run what CI runs" list, and run `bash scripts/kmp-nc-version-gate.sh` after pbxproj edits.

## Metadata

**Analog search scope:** `iosApp/`, `scripts/`, `.github/workflows/`
**Files read:** CONTEXT, RESEARCH, `di-gate.sh`, `kmp-nc-version-gate.sh` (header), `SpeakersViewModel.swift`, `AboutViewModel.swift`, `BaseViewModel.swift`, `CharacterizationSupport.swift` (head), `SpeakersVenueCharacterizationTests.swift`, `PerformanceMonitoring.swift` (signature), pbxproj (targeted greps)
**Pattern extraction date:** 2026-11-03
