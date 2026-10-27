---
phase: "6"
slug: "ios-test-foundation-and-ci-gates"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: draft
nyquist_compliant: false
wave_0_complete: false
created: "2026-10-26"
---

# Phase 6 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Swift Testing (Xcode toolchain) in a hosted unit-test bundle; bash gates with built-in `selftest` |
| **Config file** | `iosApp/iosApp.xctestplan` (new, Wave 0), scheme `iosApp.xcscheme` |
| **Quick run command** | `bash scripts/kmp-nc-version-gate.sh && SWIFT_GATE_HEADER=shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework/Headers/shared.h bash scripts/swift-names-gate.sh check` |
| **Full suite command** | `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -testPlan iosApp -destination 'platform=iOS Simulator,id=<UDID>' -derivedDataPath build build-for-testing CODE_SIGNING_ALLOWED=NO`, then the same with `-resultBundlePath <path> -parallel-testing-enabled NO test-without-building`, then `bash scripts/xcresult-assert-tests.sh <path>` |
| **Estimated runtime** | ~600 seconds (full suite, includes KMP framework link); quick gates ~10 seconds with `SWIFT_GATE_HEADER` |

---

## Sampling Rate

- **After every task commit:** Run the relevant gate(s), or `plutil -lint iosApp/iosApp.xcodeproj/project.pbxproj && xcodebuild -list -project iosApp/iosApp.xcodeproj` after pbxproj edits
- **After every plan wave:** Run the full suite command (build-for-testing + test-without-building + xcresult assertion)
- **Before `/gsd-verify-work`:** iOS CI job green on a PR, plus a deliberate-failure demonstration of the three guards (zero tests, Swift name change, KMP-NC version skew), plus the AGENTS.md pre-merge commands green on the final tree (`./gradlew detekt lint`, `bash scripts/di-gate.sh`, `bash scripts/kmp-nc-version-gate.sh`, `./gradlew testDebugUnitTest jvmTest testAndroidHostTest`; 06-05 Task 2). This once-per-phase gate is Gradle-bound and sits outside the per-task 600 s loop
- **Max feedback latency:** 600 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 06-01-T1 | 06-01 | 1 | IOSTEST-03 | T-06-01, T-06-02, T-06-03 | Under-test guard inert outside XCTest; app version lines unchanged | integration (simulator) | `plutil -lint` + `xcodebuild -list` + LOCAL LANE (totalTestCount >= 1, failedTests 0, tracer test among passed tests) | ❌ W0 | ⬜ pending |
| 06-01-T2 | 06-01 | 1 | IOSTEST-03 | T-06-03 | Version-line multiset back to pre-phase | static + integration (simulator) | `plutil -lint` + `xcodebuild -list` (exactly two targets) + LOCAL LANE | ❌ W0 | ⬜ pending |
| 06-02-T1 | 06-02 | 2 | IOSTEST-03 | T-06-18 | Legacy filter format pinned | integration (simulator) | LOCAL LANE + named check (six SessionFilterTests passed, failedTests 0) | ❌ W0 | ⬜ pending |
| 06-02-T2 | 06-02 | 2 | IOSTEST-03 | T-06-17 | Production date parsing unchanged; fractional-seconds gap recorded as debt, not fixed | integration (simulator) + static | LOCAL LANE + named check (every named test of both suites passed, failedTests 0); extraction commit numstat; `plutil -lint`; grep of the fractional-seconds entry in `deferred-items.md` and of the "Pins v1.0 behavior" test comment | ❌ W0 | ⬜ pending |
| 06-03-T1 | 06-03 | 3 | IOSTEST-03 | T-06-08 | Zero / skipped / failed runs fail closed | script selftest + real bundles (simulator) | `bash scripts/xcresult-assert-tests.sh selftest` → `XCRESULT-SELFTEST-OK`; LOCAL LANE bundle → `XCRESULT-OK`; zero-test bundle produced in the same verify block (`-only-testing:DevFestNantesTests/DoesNotExist`, xcresulttool `totalTestCount` 0) → rc 1 `XCRESULT-NO-TESTS-EXECUTED` | ❌ W0 | ⬜ pending |
| 06-03-T2 | 06-03 | 3 | IOSTEST-03 | T-06-04, T-06-05, T-06-06, T-06-07 | Read-only token, no PR data in `run:`, Xcode-aware cache | static + CI | Ruby YAML parse + step-order awk + `grep -c 'steps.xcode.outputs.version'` >= 4; dispatched ios.yml green | ❌ W0 | ⬜ pending |
| 06-04-T1 | 06-04 | 4 | IOSTEST-04 | T-06-10, T-06-11 | Empty / missing header fail | gate check + selftest + demos | `SWIFT_GATE_HEADER=… bash scripts/swift-names-gate.sh check` → `SWIFT-NAMES-OK`; selftest → `SWIFT-GATE-SELFTEST-OK`; renamed type → `SWIFT-NAME-MISSING` | ❌ W0 | ⬜ pending |
| 06-04-T2 | 06-04 | 4 | IOSTEST-04 | T-06-09 | CI never regenerates the baseline | static + CI | Ruby YAML parse + step-order awk; dispatched ios.yml green with both gate tokens | ❌ W0 | ⬜ pending |
| 06-05-T1 | 06-05 | 5 | CONC-03 | T-06-12, T-06-13, T-06-SC | exactVersion pin enforced | script + selftest + LOCAL LANE | `bash scripts/kmp-nc-version-gate.sh` → `KMPNC-VERSION-OK 1.0.6`; selftest → `KMPNC-SELFTEST-OK`; pre-phase pbxproj (6b211dd) → `KMPNC-VERSION-SKEW` | ❌ W0 | ⬜ pending |
| 06-05-T2 | 06-05 | 5 | CONC-03 | T-06-14 | N/A | docs grep + CI + Android pre-merge suite | grep docs; dispatched ios.yml green with all gate tokens; `./gradlew detekt lint && bash scripts/di-gate.sh && bash scripts/kmp-nc-version-gate.sh && ./gradlew testDebugUnitTest jvmTest testAndroidHostTest` | ❌ W0 | ⬜ pending |
| 06-06-T1 | 06-06 | 6 | CONC-03 | T-06-15 | Real plist required, never the CI mock (stop-and-report otherwise) | build + install + screenshot | `API_KEY` of the real plist is not `mock-api-key-for-ci`; `06-smoke/` created with `mkdir -p` before the capture; `file 06-smoke/00-launch.png` is PNG; app listed by `simctl listapps booted` | ❌ W0 | ⬜ pending |
| 06-06-T2 | 06-06 | 6 | CONC-03 | T-06-15, T-06-16 | Parity confirmed by a human | manual (blocking-human) | D-11 smoke checklist, 8 screenshots | n/a | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky* — LOCAL LANE is the xcodebuild build-for-testing + test-without-building + xcresulttool sequence written out in 06-01-PLAN.md Task 1 `<verify>`; the named check reads `xcresulttool get test-results tests` (no fixed test count, D-05).

---

## Wave 0 Requirements

- [ ] `iosApp/DevFestNantesTests/SessionFilterTests.swift` (+ new target in pbxproj) — covers IOSTEST-03
- [ ] `iosApp/iosApp.xctestplan` + scheme `TestPlans` — covers IOSTEST-03
- [ ] `scripts/xcresult-assert-tests.sh` — covers IOSTEST-03 (D-05)
- [ ] `scripts/swift-names-gate.sh` + `scripts/swift-names/*` — covers IOSTEST-04
- [ ] `scripts/kmp-nc-version-gate.sh` — covers CONC-03
- Framework install: none (toolchain provided)

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| iOS app behaves as in v1.0 after KMP-NC 1.0.6 bump | CONC-03 | UI + network flows, no automated UI tests until Phase 12 | Simulator smoke (D-11): Agenda (sessions, filters, favorite add/remove, relaunch), session detail, Speakers, Venue, About; screenshots attached |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 600s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
