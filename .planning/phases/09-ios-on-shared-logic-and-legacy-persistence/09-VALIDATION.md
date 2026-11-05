---
phase: "9"
slug: "ios-on-shared-logic-and-legacy-persistence"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: draft
nyquist_compliant: false
wave_0_complete: false
created: "2026-11-05"
---

# Phase 9 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Swift Testing (Xcode toolchain), app-hosted target `DevFestNantesTests`, test plan `iosApp`; Kotlin: `kotlin.test` + Turbine (`jvmTest`, `iosSimulatorArm64Test`) |
| **Config file** | `iosApp/iosApp.xctestplan`; Gradle per module |
| **Quick run command** | `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -testPlan iosApp -destination 'platform=iOS Simulator,id=<UDID>' -derivedDataPath build -only-testing:DevFestNantesTests/<Suite> test CODE_SIGNING_ALLOWED=NO` |
| **Full suite command** | `build-for-testing`, then `test-without-building -parallel-testing-enabled NO -resultBundlePath <R>`, then `bash scripts/xcresult-assert-tests.sh <R>` (expects `XCRESULT-OK`); plus `./gradlew :shared:iosSimulatorArm64Test :core:domain:iosSimulatorArm64Test :core:data:iosSimulatorArm64Test` and the gates |
| **Estimated runtime** | ~300 seconds for the Swift lane (build + test, Phase 8 figure); Kotlin native lane not yet measured |

Unlike Phase 8, the Swift test total does not only grow: deleted suites reduce it. Each plan that deletes a suite records the intended new `XCRESULT-TESTS total=` so a test file silently dropped from `project.pbxproj` stays detectable.

---

## Sampling Rate

- **After every task commit:** quick `-only-testing` run for the touched suite, plus `bash scripts/swift-observation-gate.sh`; for Kotlin changes `./gradlew :shared:iosSimulatorArm64Test` and `bash scripts/di-gate.sh`
- **After every plan wave:** full suite command, plus `bash scripts/swift-names-gate.sh check`, `bash scripts/kmp-nc-version-gate.sh` and `./gradlew detekt lint testDebugUnitTest jvmTest testAndroidHostTest`
- **Before `/gsd-verify-work`:** full suite green on CI (`ios.yml` and `android.yml`), and the D-09 and D-10 human checkpoints approved
- **Max feedback latency:** ~300 seconds

---

## Per-Task Verification Map

Task IDs are assigned once plans exist. Requirement-level map from 09-RESEARCH.md § Validation Architecture:

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| TBD | TBD | TBD | IOSDATA-01 | — | N/A | Kotlin iosTest | `./gradlew :shared:iosSimulatorArm64Test` (six `IosDependencies` accessors resolve) | ❌ W0 | ⬜ pending |
| TBD | TBD | TBD | IOSDATA-01 | — | N/A | script | `test ! -e iosApp/iosApp/Model/SessionFilter.swift && ! grep -rnE 'AgendaSessionFilters|forKey: "Favorites"' iosApp/iosApp --include='*.swift'` | ❌ W0 | ⬜ pending |
| TBD | TBD | TBD | IOSDATA-01 | — | N/A | gate | `bash scripts/swift-names-gate.sh check && bash scripts/swift-names-gate.sh selftest` | ✅ | ⬜ pending |
| TBD | TBD | TBD | IOSDATA-01 | — | N/A | Swift Testing | `-only-testing:DevFestNantesTests/AgendaCharacterizationTests` (rewritten, same goldens; classification table) | ❌ rewrite | ⬜ pending |
| TBD | TBD | TBD | IOSDATA-01 | — | N/A | Swift Testing | `-only-testing:DevFestNantesTests/SpeakersVenueCharacterizationTests`, `SpeakersViewModelTests`, `VenueViewModelTests` | ✅ adapt | ⬜ pending |
| TBD | TBD | TBD | IOSDATA-02 | Silent data loss (D-15) | Legacy keys never read; VM seeds only from the port | Swift Testing | `-only-testing:DevFestNantesTests/AgendaViewModelTests` | ❌ W0 | ⬜ pending |
| TBD | TBD | TBD | IOSDATA-03 | — | N/A | Swift Testing spike | `-only-testing:DevFestNantesTests/KotlinContinuousFlowTests` | ❌ W0 | ⬜ pending |
| TBD | TBD | TBD | IOSDATA-03 | Stale mirror drift | Mirror write-through | Swift Testing | `-only-testing:DevFestNantesTests/AgendaViewModelTests` | ❌ rewrite | ⬜ pending |
| TBD | TBD | TBD | IOSDATA-03 | — | N/A | Swift Testing | `-only-testing:DevFestNantesTests/AgendaScrollTargetTests` | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `iosApp/DevFestNantesTests/KotlinContinuousFlowTests.swift` — research-flag spike (IOSDATA-03), written first
- [ ] `iosApp/DevFestNantesTests/Fakes/FakeAgendaSource.swift` — new port shape (scripted stream, filters and bookmarks snapshots, write recorders, classify stub)
- [ ] `iosApp/DevFestNantesTests/AgendaScrollTargetTests.swift` — D-11
- [ ] In-place update Maestro flow without `clearState` under the phase `09-parity/` directory
- [ ] `IosKoinTest` accessor cases
- [ ] Rebuilt `shared.framework` before any Swift compile check

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| In-place update starts empty and new values persist | IOSDATA-02 | Needs a real install-over on a simulator and a human decision (D-09) | Build and run `main`, add bookmarks and filters, kill; `xcrun simctl install` the branch build over it; relaunch: empty, app works; set new ones, kill, relaunch: they persist; compare the data container path before and after; human checkpoint |
| Visual parity with the room order as sole difference | IOSDATA-01, IOSDATA-03 | Visual comparison needs a human (D-10) | Paired `main` vs branch simulator screenshots: both days, several slots, each filter type, favorites toggled from the list, the agenda detail and Speakers → session, favorites after relaunch, speakers A-Z, venue, plus one accessibility-size pair; any other difference is a documented deviation; human checkpoint |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 300s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
