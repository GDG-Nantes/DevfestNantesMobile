---
phase: "6"
slug: "socle-de-tests-et-gates-ci-ios"
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
- **Before `/gsd-verify-work`:** iOS CI job green on a PR, plus a deliberate-failure demonstration of the three guards (zero tests, Swift name change, KMP-NC version skew)
- **Max feedback latency:** 600 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 6-TBD | TBD | 1 | IOSTEST-03 | — | N/A | integration (simulator) | full suite command | ❌ W0 | ⬜ pending |
| 6-TBD | TBD | 1 | IOSTEST-03 | — | N/A | script check | `bash scripts/xcresult-assert-tests.sh <zero-test xcresult>` expects rc 1 | ❌ W0 | ⬜ pending |
| 6-TBD | TBD | 1 | IOSTEST-03 | — | N/A | static | `grep -n "steps.xcode.outputs.version" .github/workflows/ios.yml` | ❌ W0 | ⬜ pending |
| 6-TBD | TBD | 2 | IOSTEST-04 | — | N/A | gate selftest + check | `bash scripts/swift-names-gate.sh selftest` → `SWIFT-GATE-SELFTEST-OK` | ❌ W0 | ⬜ pending |
| 6-TBD | TBD | 3 | CONC-03 | — | N/A | script | `bash scripts/kmp-nc-version-gate.sh` → `KMPNC-VERSION-OK 1.0.6` | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky* — Task IDs are filled in once plans exist.

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
