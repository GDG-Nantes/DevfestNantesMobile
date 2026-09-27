---
phase: "3"
slug: "multi-module-architecture-extraction"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: draft
nyquist_compliant: false
wave_0_complete: false
created: "2026-09-22"
---

# Phase 3 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 4 + `kotlin.test` (commonTest/jvmTest), Espresso + Compose UI test (androidTest) |
| **Config file** | none dedicated — `testOptions { unitTests { isReturnDefaultValues = true; isIncludeAndroidResources = true } }` in `androidApp/build.gradle.kts` (must be preserved by the app convention plugin) |
| **Quick run command** | `./gradlew --no-daemon detekt` + the moved module's own test task |
| **Full suite command** | `./gradlew --no-daemon detekt lint assembleDebug assembleRelease testDebugUnitTest` + module `jvmTest`/`commonTest`-equivalent tasks + Android & iOS CI green |
| **Estimated runtime** | ~300 seconds locally (full suite) |

---

## Sampling Rate

- **After every task commit:** Run the quick run command (detekt + moved module's test task)
- **After every plan wave:** Run the full suite command, push, and require Android + iOS CI green
- **Before `/gsd-verify-work`:** Full suite must be green, plus D-12 iOS simulator smoke and D-18 Android smoke checkpoints
- **Max feedback latency:** 300 seconds

---

## Per-Task Verification Map

*Filled by the planner/executor per task once PLAN.md files exist.*

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 3-01-01 | 01 | 1 | ARCH-01, ARCH-04 | — | N/A | build/config | `./gradlew --no-daemon :core:model:tasks detekt :shared:assemble` | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `build-logic` + `core/model` tracer module exist — every module-scoped test command is MISSING until then (D-09)
- [ ] Confirm generated Android unit-test task name for `com.android.kotlin.multiplatform.library` modules (RESEARCH Open Question 2) — decides whether CI's unqualified `testDebugUnitTest` still covers moved tests
- [ ] Decide `jvm()`/`jvmTest` and `commonTest` ownership before the `core/data` extraction so `GraphQLStoreJvmTest`, `DevFestNantesStoreContractTest`, `ScheduleSlotDateParsingTest` never go dark

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| iOS app renders agenda/speakers/venue/about with data | ARCH-04 | No iOS UI test harness | Build & run iosApp on simulator after core/model tracer and after core/data extraction (D-12) |
| Android app zero behavior change | ARCH-02, ARCH-03 | Limited UI test coverage | `android run` + `android layout`/`android screen capture` over all tabs, session detail, speaker detail, settings after core/data, core/ui, last feature (D-18) |
| Swift-visible names unchanged | ARCH-04 | Header diff needs judgement | Diff generated `shared.h` before/after each exported-module move (D-16) |
| Dependency direction app → feature → core | ARCH-02 | Graph lint deferred (CICD-V2-01) | Review `./gradlew :<module>:dependencies` / build files for feature→feature or core→feature edges |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 300s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
