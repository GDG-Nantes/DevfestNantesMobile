---
phase: "5"
slug: "test-coverage-retrofit"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: validated
nyquist_compliant: true
wave_0_complete: true
created: "2026-10-15"
validated: "2026-10-16"
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

Every task in plans 05-01 to 05-11 carries an `<automated>` verify block. The table maps each task to its requirement, its main test evidence, and the plan's automated command (abridged; the plan's `<verify>` block is authoritative).

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 5-01-1 | 05-01 | 1 | TEST-02, TEST-04 (D-18) | — | N/A | report | `./gradlew koverXmlReport` + `kover-summary.sh` | ✅ | ✅ green |
| 5-01-2 | 05-01 | 1 | TEST-02 | — | N/A | report (baseline) | `kover/baseline-report.xml`, live canary behind `-PliveNetworkTests` | ✅ | ✅ green |
| 5-01-3 | 05-01 | 1 | TEST-02 | — | N/A | CI (iOS core commonTest) | `ios.yml` core iOS tests, CI-GREEN-BOTH | ✅ | ✅ green |
| 5-02-1 | 05-02 | 2 | TEST-01 | — | N/A | unit (Robolectric characterization) | `DateUtilsCharacterizationTest`, `ScheduleSlotFormattedRangeTest` | ✅ | ✅ green |
| 5-02-2 | 05-02 | 2 | TEST-01 | — | N/A | unit (common) | `IsoDateTest`, `FallbackFeedbackUrlTest` | ✅ | ✅ green |
| 5-02-3 | 05-02 | 2 | TEST-01 | — | N/A | unit (JVM) | `CurrentStartTimeIndexTest`; CI green | ✅ | ✅ green |
| 5-03-1 | 05-03 | 3 | TEST-01 | — | N/A | unit (common) | `FixturesTest` (`:core:model:jvmTest`) | ✅ | ✅ green |
| 5-03-2 | 05-03 | 3 | TEST-01 | — | N/A | unit + header gate | `MockedStoreContractTest`; swift-names-gate; CI green | ✅ | ✅ green |
| 5-04-1 | 05-04 | 4 | TEST-02 | — | N/A | integration (MockServer, 3 runtimes) | `GraphQLStoreMockServerTest` (`:core:data:jvmTest :core:data:testAndroidHostTest :core:data:iosSimulatorArm64Test`) | ✅ | ✅ green |
| 5-04-2 | 05-04 | 4 | TEST-02 | — | N/A | unit (mappers) | `MapperTest` | ✅ | ✅ green |
| 5-04-3 | 05-04 | 4 | TEST-02 | — | N/A | integration (cache fallbacks) | `GraphQLStoreMockServerTest` cache cases; CI green | ✅ | ✅ green |
| 5-05-1 | 05-05 | 5 | TEST-02 | — | N/A | contract (Mocked + GraphQLStore) | `MockedStoreContractTest`, `GraphQLStoreContractTest` | ✅ | ✅ green |
| 5-05-2 | 05-05 | 5 | TEST-02 | — | N/A | unit (Robolectric persistence) | `BookmarksStoreImplTest`, `SessionFiltersServiceImplTest`, `DataCollectionSettingsServiceImplTest` | ✅ | ✅ green |
| 5-06-1 | 05-06 | 6 | TEST-03 | — | N/A | unit (ViewModel, Turbine) | `SpeakersViewModelTest` | ✅ | ✅ green |
| 5-06-2 | 05-06 | 6 | TEST-03 | — | N/A | unit (common, JVM + iOS) | `FakeDevFestNantesStoreTest` | ✅ | ✅ green |
| 5-06-3 | 05-06 | 6 | TEST-03 | — | N/A | unit (Koin graph/startup) | `KoinStartupTest`, `ProductionGraphStartupTest`; CI green | ✅ | ✅ green |
| 5-07-1 | 05-07 | 7 | TEST-03 | — | N/A | unit (ViewModel) | `AgendaViewModelTest` (filter matrix) | ✅ | ✅ green |
| 5-07-2 | 05-07 | 7 | TEST-03 | — | N/A | unit (ViewModel) | `HomeViewModelTest`, `BookmarksViewModelTest` | ✅ | ✅ green |
| 5-08-1 | 05-08 | 8 | TEST-03 | — | N/A | unit (ViewModel) | `SessionViewModelTest`, `SpeakerViewModelTest`, `PartnersViewModelTest`, `DataCollectionViewModelTest` | ✅ | ✅ green |
| 5-08-2 | 05-08 | 8 | TEST-03 | — | N/A | unit (ViewModel, FeedbackFlags seam) | `FeedbackFormViewModelTest` | ✅ | ✅ green |
| 5-08-3 | 05-08 | 8 | TEST-03 | — | N/A | unit (Robolectric) | `VenueViewModelTest`; CI green | ✅ | ✅ green |
| 5-09-1 | 05-09 | 9 | TEST-04 | — | N/A | instrumented smoke | `SpeakersSmokeTest` (`connectedDebugAndroidTest`, CI instrumented job) | ✅ | ✅ green |
| 5-09-2 | 05-09 | 9 | TEST-04 | — | N/A | instrumented smoke | `VenueSmokeTest`, `AboutSmokeTest`, `SettingsSmokeTest` | ✅ | ✅ green |
| 5-10-1 | 05-10 | 10 | TEST-04 | — | N/A | instrumented smoke | `AgendaSmokeTest` | ✅ | ✅ green |
| 5-10-2 | 05-10 | 10 | TEST-04 | — | N/A | instrumented smoke | `SessionDetailSmokeTest` | ✅ | ✅ green |
| 5-10-3 | 05-10 | 10 | TEST-02 (D-18) | — | N/A | report | `kover/final-report.xml`, `comparison.txt` (KOVER-IMPROVED) | ✅ | ✅ green |
| 5-11-1 | 05-11 | 11 | TEST-03 (G-05-3) | T-5-24 | OpenFeedback linkage on app runtime classpath | unit (JVM linkage guard, RED→GREEN) | `./gradlew :androidApp:testDebugUnitTest --tests '*OpenFeedbackDatetimeLinkageTest'` + classpath resolution + `shared.h` diff (DATETIME-COMPAT-GUARD-GREEN) | ✅ | ✅ green |
| 5-11-2 | 05-11 | 11 | TEST-03 (G-05-3) | T-5-27 | iOS header unchanged | unit + header gate | `./gradlew testDebugUnitTest jvmTest :core:model:iosSimulatorArm64Test :shared:linkDebugFrameworkIosSimulatorArm64` + swift-names-gate (INSTANT-MIGRATED-HEADER-UNCHANGED) | ✅ | ✅ green |
| 5-11-3 | 05-11 | 11 | TEST-03 (G-05-3) | T-5-25 | R8 fails loudly on missing classes | build (R8) + full suite + CI | `./gradlew :androidApp:assembleRelease` (R8-CLEAN-WITHOUT-SUPPRESSION), full suite (DI-GATE-OK), CI-GREEN-BOTH 62a8857 | ✅ | ✅ green |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [x] Catalog entries (kover, turbine, apollo-mockserver) and convention-plugin wiring (Kover on all modules, `withHostTest` opt-in, feature-module test deps incl. `androidTestImplementation`)
- [x] `MainDispatcherRule` (`:core:testing` androidMain), `FakeDevFestNantesStore`, extended `FakeAnalyticsService` if recording is needed
- [x] Recorded JSON constants `RecordedResponses.kt`
- [x] CI: add `testAndroidHostTest` to `android.yml`; widen `ios.yml:131` to `:core:model`, `:core:data`, `:core:testing` iOS tests

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Compose smoke tests on emulator (local run) | TEST-04 | CI's instrumented job runs them; a local run needs an emulator (API 34) | `./gradlew connectedDebugAndroidTest` with an emulator started via `android emulator start` |
| UAT test 3: OpenFeedback area with real Remote Config, posting a comment (G-05-3) | TEST-03 | Real Remote Config, populated secrets and the production OpenFeedback Firestore only exist on a developer device; posting writes a real comment. The JVM guard covers the same code paths in CI | Steps in `05-11-SUMMARY.md` "Open Device Check" / 05-11-PLAN Task 3 `<human-check>` |

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references
- [x] No watch-mode flags
- [x] Feedback latency < 300s
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** validated 2026-10-16 (validate-phase audit, no gaps)

## Validation Audit 2026-10-16

| Metric | Count |
|---|---|
| Gaps found | 0 |
| Resolved | 0 |
| Escalated | 0 |
