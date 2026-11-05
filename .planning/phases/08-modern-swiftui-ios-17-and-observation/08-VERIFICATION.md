---
phase: 08-modern-swiftui-ios-17-and-observation
verified: 2026-11-05T00:00:00Z
status: passed
score: 5/5 must-haves verified
covered_files:
  - ".github/workflows/ios.yml"
  - ".planning/phases/08-modern-swiftui-ios-17-and-observation/08-01-PLAN.md"
  - ".planning/phases/08-modern-swiftui-ios-17-and-observation/08-01-SUMMARY.md"
  - ".planning/phases/08-modern-swiftui-ios-17-and-observation/08-02-PLAN.md"
  - ".planning/phases/08-modern-swiftui-ios-17-and-observation/08-02-SUMMARY.md"
  - ".planning/phases/08-modern-swiftui-ios-17-and-observation/08-03-PLAN.md"
  - ".planning/phases/08-modern-swiftui-ios-17-and-observation/08-03-SUMMARY.md"
  - ".planning/phases/08-modern-swiftui-ios-17-and-observation/08-04-PLAN.md"
  - ".planning/phases/08-modern-swiftui-ios-17-and-observation/08-04-SUMMARY.md"
  - ".planning/phases/08-modern-swiftui-ios-17-and-observation/08-05-PLAN.md"
  - ".planning/phases/08-modern-swiftui-ios-17-and-observation/08-05-SUMMARY.md"
  - ".planning/phases/08-modern-swiftui-ios-17-and-observation/08-06-PLAN.md"
  - ".planning/phases/08-modern-swiftui-ios-17-and-observation/08-06-SUMMARY.md"
  - ".planning/phases/08-modern-swiftui-ios-17-and-observation/08-07-PLAN.md"
  - ".planning/phases/08-modern-swiftui-ios-17-and-observation/08-07-SUMMARY.md"
  - ".planning/phases/08-modern-swiftui-ios-17-and-observation/08-08-PLAN.md"
  - ".planning/phases/08-modern-swiftui-ios-17-and-observation/08-08-SUMMARY.md"
  - ".planning/phases/08-modern-swiftui-ios-17-and-observation/08-09-PLAN.md"
  - ".planning/phases/08-modern-swiftui-ios-17-and-observation/08-09-SUMMARY.md"
  - "iosApp/iosApp.xcodeproj/project.pbxproj"
  - "iosApp/iosApp/Agenda/AgendaView.swift"
  - "iosApp/iosApp/Agenda/AgendaViewModel.swift"
  - "iosApp/iosApp/Model/KotlinStreamBridge.swift"
  - "iosApp/iosApp/UI/About/AboutViewModel.swift"
  - "iosApp/iosApp/UI/ContentView.swift"
  - "iosApp/iosApp/UI/Speakers/SpeakerDetailsViewModel.swift"
  - "iosApp/iosApp/UI/Speakers/SpeakersViewModel.swift"
  - "iosApp/iosApp/UI/Venue/VenueViewModel.swift"
  - "scripts/swift-observation-gate.sh"
covered_digest: "v3:sha256:66bba03682709cd6b63b500ae49e592a816808100981cff801973f588184310a"
behavior_unverified: 0
overrides_applied: 1
overrides:
  - must_have: "UAT: agenda, filters, bookmarks, session detail, speakers, venue and about behave as in v1.0 (shifted-clock initial-scroll sub-check)"
    reason: "The shifted-clock initial-scroll check could not be performed on iOS (a shifted host clock breaks TLS, so neither baseline nor branch loaded any agenda). Identical on both builds, logged as debt in deferred-items.md 'From 08-09'. The 21 paired baseline/branch screenshots showed no difference and tab switching was OK on both builds."
    accepted_by: "Robin Caroff (08-09 blocking-human checkpoint, relayed by the orchestrator)"
    accepted_at: "2026-11-05T00:00:00Z"
re_verification: false
---

# Phase 8: Modern SwiftUI, iOS 17 and Observation - Verification Report

**Phase Goal:** The iOS ViewModels move to the Observation model on an iOS 17 foundation, without Combine, with collection tied to the view lifecycle and unit tests on fakes, with no visible change
**Verified:** 2026-11-05
**Status:** passed
**Re-verification:** No, initial verification

Source under `iosApp/`, `scripts/` and `.github/` is unchanged between 3fb8ba7b (full phase gate commit) and HEAD 813a5102: `git diff --name-only 3fb8ba7b HEAD` outside `.planning/` is empty.

## Goal Achievement

### Observable Truths (ROADMAP success criteria)

| # | Truth | Status | Evidence |
|---|-------|--------|----------|
| 1 | All iOS targets declare iOS 17.0 (no 15.0/17.0 mix); app builds and runs on simulator | VERIFIED | `project.pbxproj` has exactly 6 `IPHONEOS_DEPLOYMENT_TARGET` entries (lines 770, 826, 848, 881, 909, 933), all `17.0`. Those are 3 configuration lists (project + 2 native targets) x Debug/Release. No `Package.swift` of the app exists yet (packages arrive in Phase 11). iOS CI run 41986925570 (`gh run view --json`: conclusion success, workflow "iOS CI", sha c8aa2d3f) built and tested on Xcode 26.6; 08-09 ran the app on simulator for the parity pass. |
| 2 | Agenda, Speakers, SpeakerDetails, Venue, About ViewModels are `@Observable @MainActor`, owned via `@State`; re-render does not recreate the VM; collection starts in `.task`, stops on disappear; no unstructured `Task {}` | VERIFIED | All five VM files carry `@Observable` + `@MainActor` (`AgendaViewModel.swift:13-14`, `SpeakersViewModel.swift:13-14`, `SpeakerDetailsViewModel.swift:13-14`, `VenueViewModel.swift:13-14`, `AboutViewModel.swift:13-14`). Ownership: `SpeakersView.swift:13`, `VenueView.swift:13`, `AboutView.swift:13` use `@State private var viewModel = ...`; `SpeakerDetailsView.swift:13,19` uses `State(initialValue:)`; the single Agenda VM is `@State` in `ContentView.swift:7` and injected with `.environment(agendaViewModel)` (`ContentView.swift:43`), read via `@Environment(AgendaViewModel.self)` in `AgendaView.swift:11`. Collection is in `.task` (`SpeakersView:94`, `VenueView:107`, `AboutView:193`, `SpeakerDetailsView:89`, `AgendaView:57,60`). Production sources contain no `Task {`/`Task.detached`/`Task.init` (grep of `iosApp/iosApp` minus SwiftGen: only test files use `Task { @MainActor in ... }` to drive cancellation). `KotlinStreamBridge` has no task of its own and cancels the native subscription from `onTermination`. Cancellation behavior is exercised by tests (`cancelled_first_load_is_silent` etc. in each `*ViewModelTests.swift`) that passed in CI (32/32) and in the local full gate (57/57). |
| 3 | No `import Combine`, `KMPNativeCoroutinesCombine`, `ObservableObject`, `@Published`, `objectWillChange.send()`, `DispatchQueue.main.async` in iOS sources; Combine SPM product removed | VERIFIED | Re-ran `bash scripts/swift-observation-gate.sh` -> `SWIFT-OBS-GATE-OK`. Independent grep across `iosApp/iosApp`, `iosApp/DevFestNantesTests` and the whole `iosApp.xcodeproj` (SwiftGen excluded) returned zero hits; `BaseViewModel` and `DispatchQueue` are absent from all tracked Swift files. Gate wired into CI at `.github/workflows/ios.yml:41-42` (check + selftest). |
| 4 | Each iOS ViewModel has Swift Testing tests on fakes (narrow ports per VM), run in the Phase 6 CI lane | VERIFIED | Per-VM test files: `AboutViewModelTests` (7 `@Test`), `AgendaViewModelTests` (11), `SpeakersViewModelTests` (7), `SpeakerDetailsViewModelTests` (7), `VenueViewModelTests` (6); total 57 `@Test` across the target, matching the 57/57 gate. Fakes in `DevFestNantesTests/Fakes/` (`FakeAboutSource`, `FakeAgendaSource`, `FakeSpeakersSource`, `FakeSpeakerDetailsSource`, `FakeVenueSource`, `FakeStream`, `FakeCall`, `RecordingDataLoadTracing`); each VM takes a narrow `<Feature>Source` port (`AboutSource.swift`, `AgendaSource.swift`, ...). Lane: `iosApp.xctestplan` targets `DevFestNantesTests`; `ios.yml` runs `-testPlan iosApp` and `xcresult-assert-tests.sh`. |
| 5 | UAT: agenda, filters, bookmarks, session detail, speakers, venue, about behave as in v1.0 | PASSED (override) | 08-09 blocking human checkpoint approved 2026-11-05: 21 paired baseline/branch screenshots (`08-parity/base`, `08-parity/branch`, 21 files each, tracked in git) with no difference; tab switching OK on both builds. The shifted-clock initial-scroll sub-check (the D-12 scroll-hop removal) was not performable (TLS breaks under a shifted host clock; identical on both builds) and was accepted as debt by the user. See overrides and Warnings. |

**Score:** 5/5 truths verified (0 present, behavior-unverified; 1 carried by an accepted override)

### Required Artifacts

| Artifact | Expected | Status | Details |
|----------|----------|--------|---------|
| `iosApp/iosApp/Model/KotlinStreamBridge.swift` | Task-free flow-to-AsyncThrowingStream bridge | VERIFIED | Substantive (native flow callbacks, `onTermination` cancels); used by `Live*Source` adapters; exercised over the mocked Kotlin store in `AgendaViewModelTests.live_source_loads_the_mocked_store_sessions_and_rooms`. |
| `iosApp/iosApp/{Agenda,UI/*}/*Source.swift` | Narrow ports per VM | VERIFIED | Five port files present, injected through VM initializers with live defaults. |
| `iosApp/DevFestNantesTests/Fakes/*` | Hand-written fakes | VERIFIED | Eight fake/support files. |
| `scripts/swift-observation-gate.sh` | Regression gate | VERIFIED | Runs OK; wired in `ios.yml`. |
| `iosApp/iosApp.xcodeproj/project.pbxproj` | 17.0 everywhere, no Combine product | VERIFIED | See truths 1 and 3. |

### Key Link Verification

| From | To | Via | Status | Details |
|------|----|-----|--------|---------|
| `ContentView` | `AgendaView` | `@State` VM + `.environment(...)` | WIRED | `ContentView.swift:7,43` -> `AgendaView.swift:11`. |
| Views | VM collection | `.task { await viewModel.observe...() }` | WIRED | Lines listed in truth 2. |
| VM | Kotlin store | `Live*Source` -> `KotlinStreamBridge.stream(store.flow)` | WIRED | Tracer test drives the mocked store end to end. |
| `ios.yml` | gate script | `bash scripts/swift-observation-gate.sh` (+ selftest) | WIRED | `ios.yml:41-42`. |

### Data-Flow Trace (Level 4)

| Artifact | Data Variable | Source | Produces Real Data | Status |
|----------|---------------|--------|--------------------|--------|
| `AgendaViewModel` | `agendaContent`, `roomsContent` | `LiveAgendaSource` -> GraphQL store flows | Yes (tracer test over the mocked store asserts non-empty sections and rooms) | FLOWING |
| `SpeakersViewModel`, `AboutViewModel` | `speakersContent`, `partnersContent` | `Live*Source` -> store | Yes | FLOWING |
| `VenueViewModel`, `SpeakerDetailsViewModel` | `venueContent`, `speaker`/`speakerSession` | one-shot store calls | Yes | FLOWING |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
|----------|---------|--------|--------|
| Observation gate | `bash scripts/swift-observation-gate.sh` | `SWIFT-OBS-GATE-OK` | PASS |
| Deployment targets | awk over `project.pbxproj` | 6 entries, all `17.0` | PASS |
| iOS CI | `gh run view 41986925570 --json conclusion` | `success` (sha c8aa2d3f; XCRESULT total=32 passed=32 per orchestrator) | PASS |
| Swift suite at final source | orchestrator-reported full gate on 3fb8ba7b (57/57); source identical to HEAD | not re-run here (slow) | ACCEPTED |

Step 7c (probes): no probe scripts declared by this phase; skipped.

### Requirements Coverage

| Requirement | Source Plan(s) | Description | Status | Evidence |
|-------------|----------------|-------------|--------|----------|
| IOSUI-01 | 08-01, 08-04 | iOS 17.0 deployment target on all iOS targets | SATISFIED | Truth 1. Phase 11 packages are covered by Phase 11 SC3 (REQUIREMENTS note). |
| IOSUI-02 | 08-03, 08-04, 08-05, 08-06, 08-09 | VMs `@Observable @MainActor`, `@State`-owned, `.task`, no unstructured `Task {}` | SATISFIED | Truth 2. |
| IOSUI-03 | 08-09 (gate shipped in 08-08) | No Combine / `ObservableObject` / `@Published` / `objectWillChange` / `DispatchQueue.main.async` | SATISFIED | Truth 3. |
| IOSTEST-01 | 08-03, 08-04, 08-05, 08-06 | Each iOS VM has Swift Testing tests on fakes | SATISFIED | Truth 4. |

Plans 08-02, 08-07 and 08-08 carry no `requirements:` frontmatter field, but all four IDs are claimed by other plans, and REQUIREMENTS.md maps no other ID to Phase 8. No orphaned requirements. Note: the traceability table in REQUIREMENTS.md still shows IOSUI-01/02/03 and IOSTEST-01 as `Pending` with unchecked boxes; bookkeeping to flip on phase completion.

### Anti-Patterns Found

| File | Line | Pattern | Severity | Impact |
|------|------|---------|----------|--------|
| (all phase-modified sources) | - | `TBD`/`FIXME`/`XXX` markers | none found among reviewed VM/bridge/view files | No debt-marker blocker. |
| `iosApp/iosApp/Agenda/AgendaView.swift`, others | various | Deprecated SwiftUI APIs (`navigationBarTitle`, `foregroundColor`, `NavigationView`, ...) | Info | Deliberately kept for strict parity; catalogued in `deferred-items.md` D-12 for Phase 12. |

### Human Verification Required

None outstanding. The blocking human UAT was completed and approved on 2026-11-05 (08-09).

### Warnings (advisory, not gaps)

1. **Shifted-clock initial-scroll check not performed** (accepted by user as debt). D-12 removed the scroll hop from the initial agenda scroll; review IN-08 flags the timing change as unverified. Mitigation recommended in `deferred-items.md`: a unit test injecting a clock into `AgendaViewModel`.
2. **Code review findings all open** (0 critical, 5 warning, 8 info; `08-REVIEW-DISPOSITION.md`). Most relevant to the goal:
   - WR-01: a Kotlin-side cancellation is swallowed as a `CancellationError` by every VM, leaving `isLoading == true` (blurred, non-interactive screen) when the Swift task is still alive. Edge path, untested; does not affect the five success criteria but is a robustness gap in the new bridge.
   - WR-02: `.task` blocks in `AgendaView` restart on each reappearance and `.onAppear` unconditionally resets the selected day. Not reproduced in the 08-09 parity pass (tab switching identical to baseline); pop-from-detail day reset is pre-existing.
3. Pre-existing v1.0 behavior (no cached agenda when the network fails) and a stale Swift-names header baseline are logged as non-phase-caused debt.

### Gaps Summary

No gaps. All four requirement IDs are accounted for and backed by code, gate output, CI result and the approved human UAT. The only unexercised behavior (initial-scroll after the scroll-hop removal) is an explicit, user-accepted debt recorded as an override; reviewers who disagree with treating it as accepted should downgrade the status to `human_needed`.

---

_Verified: 2026-11-05_
_Verifier: Claude (gsd-verifier)_
