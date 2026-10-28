---
phase: "7"
slug: "kmp-domain-layer-and-android-adoption"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: draft
nyquist_compliant: false
wave_0_complete: false
created: "2026-10-28"
---

# Phase 7 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | JUnit 4 + `kotlin.test` + Turbine + kotlinx-coroutines-test (KMP/Android); Robolectric 4.17 (Android host); Swift Testing (`DevFestNantesTests`); Compose UI tests (instrumentation) |
| **Config file** | `build-logic/.../KmpLibraryConventionPlugin.kt`; `iosApp/iosApp.xctestplan` |
| **Quick run command** | `./gradlew :core:domain:jvmTest` |
| **Full suite command** | `./gradlew detekt lint && bash scripts/di-gate.sh && bash scripts/kmp-nc-version-gate.sh && ./gradlew testDebugUnitTest jvmTest testAndroidHostTest && ./gradlew :shared:iosSimulatorArm64Test :core:model:iosSimulatorArm64Test :core:data:iosSimulatorArm64Test :core:domain:iosSimulatorArm64Test :core:testing:iosSimulatorArm64Test && bash scripts/swift-names-gate.sh check` |
| **Estimated runtime** | ~600 seconds (full), ~60 seconds (quick) |

---

## Sampling Rate

- **After every task commit:** Run `./gradlew :core:domain:jvmTest` plus the touched module's `jvmTest`/`testDebugUnitTest`
- **After every plan wave:** Run the full suite command
- **Before `/gsd-verify-work`:** Full suite green, plus Swift Testing run with `scripts/xcresult-assert-tests.sh`, `:feature:agenda:connectedDebugAndroidTest`, the D-12 screenshot checkpoint and an Android release (R8) build smoke
- **Max feedback latency:** 600 seconds

---

## Per-Task Verification Map

Derived from 07-RESEARCH.md § Validation Architecture; task IDs are filled in once plans exist.

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| TBD | TBD | TBD | SHARED-01 | — | N/A | unit (JVM) + iOS sim | `./gradlew :core:domain:jvmTest :shared:jvmTest :shared:iosSimulatorArm64Test` | ❌ W0 | ⬜ pending |
| TBD | TBD | TBD | SHARED-01 | — | N/A | gate | `bash scripts/swift-names-gate.sh check && bash scripts/swift-names-gate.sh selftest` | ✅ | ⬜ pending |
| TBD | TBD | TBD | SHARED-02 | — | N/A | unit commonTest | `./gradlew :core:domain:jvmTest :core:domain:iosSimulatorArm64Test` | ❌ W0 | ⬜ pending |
| TBD | TBD | TBD | SHARED-02 | — | N/A | Robolectric host | `./gradlew :core:data:testAndroidHostTest` | ✅ | ⬜ pending |
| TBD | TBD | TBD | SHARED-03 | — | N/A | unit commonTest vs goldens | `./gradlew :core:domain:jvmTest :core:domain:iosSimulatorArm64Test` | ❌ W0 | ⬜ pending |
| TBD | TBD | TBD | SHARED-04 | — | N/A | unit commonTest + Swift | `./gradlew :core:domain:jvmTest` + Swift Testing | ❌ W0 | ⬜ pending |
| TBD | TBD | TBD | SHARED-05 | — | N/A | Robolectric + iOS sim | `./gradlew :core:data:testAndroidHostTest :core:data:iosSimulatorArm64Test` | ❌ W0 | ⬜ pending |
| TBD | TBD | TBD | SHARED-06 | — | N/A | unit + Robolectric | `./gradlew :feature:agenda:testDebugUnitTest :androidApp:testDebugUnitTest :core:ui:testDebugUnitTest :feature:venue:testDebugUnitTest :feature:speakers:testDebugUnitTest` | ✅ | ⬜ pending |
| TBD | TBD | TBD | SHARED-07 | — | N/A | unit + Swift Testing | `./gradlew :core:data:jvmTest :core:data:iosSimulatorArm64Test` + Swift Testing | ❌ W0 | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `core/domain/` module (build file, `settings.gradle.dcl`, Kover, `ios.yml` test task, feature-plugin dependency)
- [ ] `core/domain/src/commonTest/.../TestData.kt` (`expect fun readTestData`) + `jvmTest`/`iosTest` actuals + Gradle env (`SIMCTL_CHILD_`) / `inputs.dir` wiring
- [ ] `scripts/record-agenda-snapshot.sh` + committed raw responses + neutral model snapshot + drift-check test in `:core:data`
- [ ] Use-case tests: `FilterSessionsUseCaseTest`, `GroupSessionsUseCaseTest`, `DayAssignmentTest`, `ClassifySessionUseCaseTest`, `VenueLanguageUseCaseTest`, `SpeakerIndexUseCaseTest`, `ObserveAgendaUseCaseTest`
- [ ] `DomainModuleTest`, `KoinGraphTest` extra types, `IosKoinTest` resolution
- [ ] `FakeSessionFiltersStore` + `testDoublesModule` binding
- [ ] Android characterization: `groupSessionsByStartTime` (two zones), `SessionFilter` identity equality (D-14)
- [ ] iOS characterization: extracted filter/grouping functions, `AgendaFilteringTests`, `AgendaGroupingTests`, golden helper, `UserDefaults*` store tests, pbxproj entries
- [ ] `names-exclude.txt` entry for `SessionFilter` + regenerated baselines

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Android agenda parity (both days, slots, room order, each filter type, favorites, favorites after relaunch) | SHARED-06 | Visual parity needs human judgement (D-12) | `android screen capture` on `main` and on the branch; human approves the diff |
| Release (R8) build with moved `@Serializable` `SessionFilter` | SHARED-02 | Needs a release build on device | Build release, set filters, relaunch, check filters persist |

---

## Validation Sign-Off

- [ ] All tasks have `<automated>` verify or Wave 0 dependencies
- [ ] Sampling continuity: no 3 consecutive tasks without automated verify
- [ ] Wave 0 covers all MISSING references
- [ ] No watch-mode flags
- [ ] Feedback latency < 600s
- [ ] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
