---
phase: "8"
slug: "modern-swiftui-ios-17-and-observation"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: draft
nyquist_compliant: false
wave_0_complete: false
created: "2026-11-03"
---

# Phase 8 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Swift Testing (Xcode toolchain), app-hosted target `DevFestNantesTests`, test plan `iosApp` |
| **Config file** | `iosApp/iosApp.xctestplan` |
| **Quick run command** | `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -testPlan iosApp -destination 'platform=iOS Simulator,id=<UDID>' -derivedDataPath build -only-testing:DevFestNantesTests/<Suite> test CODE_SIGNING_ALLOWED=NO` |
| **Full suite command** | CI sequence: `build-for-testing` then `test-without-building -parallel-testing-enabled NO -resultBundlePath <R>`, then `bash scripts/xcresult-assert-tests.sh <R>` (expects `XCRESULT-OK`) |
| **Estimated runtime** | ~300 seconds (build + test) |

Baseline: CI on main reports `XCRESULT-TESTS total=19 passed=19 failed=0 skipped=0`. The total must strictly increase after each VM plan (guards against a test file silently dropped from `project.pbxproj`).

---

## Sampling Rate

- **After every task commit:** quick `-only-testing` run for the touched suite, plus `bash scripts/swift-observation-gate.sh` once it exists
- **After every plan wave:** full suite command, plus `bash scripts/kmp-nc-version-gate.sh` (the pbxproj is edited)
- **Before `/gsd-verify-work`:** full suite green on CI (`ios.yml`) and the D-16 paired-screenshot checkpoint approved
- **Max feedback latency:** ~300 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 8-01-* | 01 | 1 | IOSUI-01 | — | N/A | script/grep | `grep -c "IPHONEOS_DEPLOYMENT_TARGET = 17.0;" iosApp/iosApp.xcodeproj/project.pbxproj` = 6 and no `15.0` | ✅ | ⬜ pending |
| 8-02..05-* | 02–05 | 2+ | IOSUI-02, IOSTEST-01 | — | N/A | unit (Swift Testing) | `-only-testing:DevFestNantesTests/<Feature>ViewModelTests` | ❌ W0 | ⬜ pending |
| 8-06-* | 06 | last | IOSUI-03 | — | N/A | gate | `bash scripts/swift-observation-gate.sh && bash scripts/swift-observation-gate.sh selftest` | ❌ W0 | ⬜ pending |
| (regression) | all | all | — | — | N/A | unit | existing 19 characterization tests | ✅ | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

(Task IDs are finalized by the planner; this map is refreshed by `/gsd-validate-phase`.)

---

## Wave 0 Requirements

- [ ] `iosApp/DevFestNantesTests/Fakes/` group and fakes (per-VM source fakes + `RecordingDataLoadTracing`), with pbxproj entries
- [ ] Five `<Feature>ViewModelTests.swift` suites (About, Venue, Speakers, SpeakerDetails, Agenda)
- [ ] `scripts/swift-observation-gate.sh` (with `selftest`) and its `ios.yml` step
- [ ] Hand-built Kotlin values for `Venue`, `Partner`, `Room` where the snapshot does not provide them

Each test suite is created inside the VM plan it covers (no separate Wave 0 plan).

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| ViewModel survives parent re-render; collection stops on disappear | IOSUI-02 | Needs the SwiftUI runtime | D-16 parity pass on simulator |
| No visible change from v1.0 (agenda, filters, bookmarks, detail, speakers, venue, about) | UAT (criterion 5) | Visual parity | Paired `main` vs branch simulator screenshots, human approval (07-19 pattern), incl. shifted-clock initial scroll with data prefetched |
| Build passes on CI toolchain (Xcode 26.6 / Swift 6.3.3) | IOSUI-01..03 | Not available locally | Push branch, `gh workflow run ios.yml`, check run green |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 300s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
