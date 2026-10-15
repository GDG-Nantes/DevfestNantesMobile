---
phase: "5"
slug: "test-coverage-retrofit"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: draft
nyquist_compliant: false
wave_0_complete: false
created: "2026-10-15"
---

# Phase 5 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | `kotlin.test` + JUnit 4.13.2, kotlinx-coroutines-test 1.11.0, Turbine 1.2.1, Robolectric 4.17, Compose ui-test-junit4 (BOM 2026.09.00), Apollo MockServer 0.3.1, Kover 0.9.11 |
| **Config file** | Convention plugins in `build-logic/convention` (no separate test config files) |
| **Quick run command** | `./gradlew :core:model:jvmTest :core:data:jvmTest` (plus the touched module's `testDebugUnitTest` / `testAndroidHostTest`) |
| **Full suite command** | `./gradlew detekt lint testDebugUnitTest jvmTest testAndroidHostTest && bash scripts/di-gate.sh` |
| **Estimated runtime** | ~240 seconds (full, warm daemon); instrumented `connectedDebugAndroidTest` separate |

---

## Sampling Rate

- **After every task commit:** Run the module-scoped quick command + `./gradlew detekt` + `bash scripts/di-gate.sh`
- **After every plan wave:** Run the full suite command, push, require `android.yml` and `ios.yml` green
- **Before `/gsd-verify-work`:** Full suite green, instrumented job green, baseline + final Kover reports attached
- **Max feedback latency:** 300 seconds

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 5-xx | TBD | — | TEST-01 | — | N/A | unit (common) | `./gradlew :core:model:jvmTest :core:data:jvmTest` | ❌ W0 | ⬜ pending |
| 5-xx | TBD | — | TEST-02 | — | N/A | unit/integration (common, MockServer, Robolectric) | `./gradlew :core:data:jvmTest :core:data:iosSimulatorArm64Test :core:data:testAndroidHostTest` | ❌ W0 | ⬜ pending |
| 5-xx | TBD | — | TEST-03 | — | N/A | unit (JVM) | `./gradlew testDebugUnitTest` | ❌ W0 | ⬜ pending |
| 5-xx | TBD | — | TEST-04 | — | N/A | instrumented | `./gradlew connectedDebugAndroidTest` | ❌ W0 | ⬜ pending |
| 5-xx | TBD | — | D-18 | — | N/A | report | `./gradlew koverXmlReport` | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*
*Task IDs are filled in once PLAN.md files exist.*

---

## Wave 0 Requirements

- [ ] Catalog entries (kover, turbine, apollo-mockserver) and convention-plugin wiring (Kover on all modules, `withHostTest` opt-in, feature-module test deps incl. `androidTestImplementation`)
- [ ] `MainDispatcherRule` (`:core:testing` androidMain), `FakeDevFestNantesStore`, extended `FakeAnalyticsService` if recording is needed
- [ ] Recorded JSON constants `RecordedResponses.kt`
- [ ] CI: add `testAndroidHostTest` to `android.yml`; widen `ios.yml:131` to `:core:model`, `:core:data`, `:core:testing` iOS tests

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Compose smoke tests on emulator | TEST-04 | Needs a running emulator (API 34) unless CI instrumented job exists | `./gradlew connectedDebugAndroidTest` with an emulator started via `android emulator start` |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 300s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
