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

Filled from the 19 plans (07-01 ... 07-19). "Swift" = the Phase 06 build-for-testing + test-without-building command with `scripts/xcresult-assert-tests.sh` (XCRESULT-OK). "Gate" = `swift-names-gate.sh check` (+ `selftest`) on a freshly linked header. "CI list" = the AGENTS.md pre-merge list.

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 07-01-T1 | 01 | 1 | SHARED-01, SHARED-04 | T-07-01, T-07-02 | No duplicate binding; Swift names stable | tracer: unit JVM + iOS sim + gate + Swift | `./gradlew :core:domain:jvmTest :core:domain:iosSimulatorArm64Test :shared:jvmTest :shared:iosSimulatorArm64Test` + Gate + Swift | ❌ W0 (created by task) | ⬜ pending |
| 07-02-T1 | 02 | 2 | SHARED-01 | T-07-31 | DI gate clean; CI runs the domain iOS tests | CI list | CI list + `:feature:agenda` classpath / ios.yml / Kover greps | ✅ | ⬜ pending |
| 07-03-T1 | 03 | 1 | SHARED-07 | T-07-03, T-07-05 | Public data only; no network in default runs | data check | `jq` facts on `core/data/testdata/graphql/*.json` + recorder excluded ("No tests found") | ❌ W0 | ⬜ pending |
| 07-04-T1 | 04 | 2 | SHARED-07 | T-07-04 | Drift check | unit JVM + iOS sim | `./gradlew :core:data:jvmTest --tests '*AgendaSnapshotTest*' :core:testing:jvmTest :core:testing:iosSimulatorArm64Test` | ❌ W0 | ⬜ pending |
| 07-05-T1 | 05 | 3 | SHARED-07, SHARED-03 | T-07-06, T-07-07 | Goldens on unmodified v1.0 code | Robolectric + golden | `./gradlew :core:testing:jvmTest :core:testing:iosSimulatorArm64Test :feature:agenda:testDebugUnitTest --tests '*AgendaGroupingCharacterizationTest*'` | ❌ W0 | ⬜ pending |
| 07-06-T1 | 06 | 4 | SHARED-07, SHARED-02 | T-07-32 | D-14 equality-agnostic pins | Robolectric Compose + unit | `./gradlew :feature:agenda:testDebugUnitTest --tests '*characterization*'` | ❌ W0 | ⬜ pending |
| 07-06-T2 | 06 | 4 | SHARED-07, SHARED-04 | T-07-32 | — | unit + golden | `./gradlew :feature:speakers:testDebugUnitTest :feature:venue:testDebugUnitTest` | ❌ W0 | ⬜ pending |
| 07-07-T1 | 07 | 2 | SHARED-07 | T-07-08 | Behavior-preserving extraction | Swift | Swift | ✅ | ⬜ pending |
| 07-08-T1 | 08 | 3 | SHARED-07, SHARED-03 | T-07-09, T-07-10 | Baselines only with Swift call sites | Swift Testing + golden + gate | Swift + Gate | ❌ W0 | ⬜ pending |
| 07-08-T2 | 08 | 3 | SHARED-07, SHARED-04 | T-07-10 | Goldens written by tests only | Swift Testing + golden | Swift | ❌ W0 | ⬜ pending |
| 07-09-T1 | 09 | 5 | SHARED-02 | T-07-12 | App compiles unchanged; D-13 baseline exception recorded | unit + gate + Swift | `./gradlew :core:model:jvmTest :core:model:iosSimulatorArm64Test :feature:agenda:compileDebugUnitTestKotlin :shared:linkDebugFrameworkIosSimulatorArm64` + Gate + Swift | ✅ | ⬜ pending |
| 07-09-T2 | 09 | 5 | SHARED-05 | T-07-13 | Malformed input -> null | unit JVM + Android host + iOS sim | `./gradlew :core:data:jvmTest :core:data:testAndroidHostTest :core:data:iosSimulatorArm64Test :core:testing:jvmTest :core:testing:iosSimulatorArm64Test` + Gate | ❌ W0 | ⬜ pending |
| 07-10-T1 | 10 | 6 | SHARED-02 | T-07-11 | Persisted bytes unchanged | unit + Robolectric | `./gradlew :feature:agenda:testDebugUnitTest :androidApp:testDebugUnitTest :feature:agenda:compileDebugAndroidTestKotlin` | ✅ | ⬜ pending |
| 07-10-T2 | 10 | 6 | SHARED-02 | T-07-11 | — | CI list | CI list | ✅ | ⬜ pending |
| 07-11-T1 | 11 | 6 | SHARED-05 | T-07-14, T-07-16 | Legacy keys never read | Kotlin iOS sim | `./gradlew :core:data:iosSimulatorArm64Test --tests '*UserDefaultsBookmarksStoreTest*'` | ❌ W0 | ⬜ pending |
| 07-11-T2 | 11 | 6 | SHARED-05 | T-07-14, T-07-15, T-07-16 | Safe casts, no payload logs | Kotlin iOS sim + gate + Swift | `./gradlew :core:data:iosSimulatorArm64Test :shared:iosSimulatorArm64Test :core:data:jvmTest :shared:jvmTest` + Gate + Swift | ❌ W0 | ⬜ pending |
| 07-12-T1 | 12 | 6 | SHARED-02, SHARED-07 | T-07-18 | — | commonTest JVM + iOS sim vs goldens | `./gradlew :core:domain:jvmTest :core:domain:iosSimulatorArm64Test --tests '*FilterSessionsUseCaseTest*'` | ❌ W0 | ⬜ pending |
| 07-12-T2 | 12 | 6 | SHARED-03, SHARED-07 | T-07-17, T-07-18 | Non-throwing shared API | commonTest JVM + iOS sim vs goldens + gate + Swift | `./gradlew :core:domain:jvmTest :core:domain:iosSimulatorArm64Test :shared:jvmTest` + Gate + Swift | ❌ W0 | ⬜ pending |
| 07-13-T1 | 13 | 7 | SHARED-04 | T-07-21 | — | commonTest JVM + iOS sim vs goldens | `./gradlew :core:domain:jvmTest :core:domain:iosSimulatorArm64Test` | ❌ W0 | ⬜ pending |
| 07-13-T2 | 13 | 7 | SHARED-01, SHARED-03 | T-07-19, T-07-20 | Failure propagation pinned; one dispatcher binding | commonTest + Koin verify + CI list + gate + Swift | `./gradlew :core:domain:jvmTest :core:domain:iosSimulatorArm64Test :shared:jvmTest :shared:iosSimulatorArm64Test :androidApp:testDebugUnitTest` + CI list + Gate + Swift | ❌ W0 | ⬜ pending |
| 07-14-T1 | 14 | 8 | SHARED-06, SHARED-05 | T-07-30 | One filters holder under two types | unit + Robolectric + DI gate + CI list | `./gradlew :feature:agenda:testDebugUnitTest :androidApp:testDebugUnitTest :feature:agenda:compileDebugAndroidTestKotlin` + `bash scripts/di-gate.sh` + CI list | ✅ | ⬜ pending |
| 07-15-T1 | 15 | 9 | SHARED-06, SHARED-02, SHARED-03 | T-07-24, T-07-34 | No dangling dispatcher binding | unit + Robolectric + DI gate + CI list | `./gradlew :feature:agenda:testDebugUnitTest :androidApp:testDebugUnitTest :core:ui:testDebugUnitTest :feature:agenda:compileDebugAndroidTestKotlin` + `bash scripts/di-gate.sh` + CI list | ✅ | ⬜ pending |
| 07-16-T1 | 16 | 10 | SHARED-06, SHARED-05 | T-07-22, T-07-23 | One filters holder; malformed entries skipped | unit + Robolectric + DI gate | `./gradlew :core:data:testAndroidHostTest :core:data:jvmTest :feature:agenda:testDebugUnitTest :androidApp:testDebugUnitTest :feature:agenda:compileDebugAndroidTestKotlin` + `bash scripts/di-gate.sh` | ✅ (moved) | ⬜ pending |
| 07-16-T2 | 16 | 10 | SHARED-06 | T-07-22 | — | CI list + instrumentation | CI list + `./gradlew :feature:agenda:connectedDebugAndroidTest` | ✅ | ⬜ pending |
| 07-17-T1 | 17 | 11 | SHARED-04, SHARED-06 | T-07-25, T-07-26 | Locator only in MainActivity | unit + DI gate | `./gradlew :feature:agenda:testDebugUnitTest :androidApp:testDebugUnitTest :feature:agenda:compileDebugAndroidTestKotlin` + `bash scripts/di-gate.sh` | ✅ | ⬜ pending |
| 07-18-T1 | 18 | 11 | SHARED-04, SHARED-06 | T-07-33 | — | unit + CI list | `./gradlew :feature:speakers:testDebugUnitTest :feature:venue:testDebugUnitTest` + CI list | ✅ | ⬜ pending |
| 07-19-T1 | 19 | 12 | SHARED-06, SHARED-02, SHARED-05 | T-07-27, T-07-29 | R8 keeps the serializer | full gate + screenshots + release smoke | CI list + all `iosSimulatorArm64Test` + three `connectedDebugAndroidTest` + Gate + Swift | ✅ | ⬜ pending |
| 07-19-T2 | 19 | 12 | SHARED-06 | T-07-28 | Human-approved parity | manual (blocking-human checkpoint) | n/a (D-12 screenshot comparison) | n/a | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

---

## Wave 0 Requirements

- [ ] `core/domain/` module (build file, `settings.gradle.dcl`) — 07-01; Kover, `ios.yml` test task, feature-plugin dependency — 07-02
- [ ] Test-data reading: `GoldenFiles`/`TestDataFiles` expect/actual in `:core:testing` (android, jvm, ios) + Gradle `devfest.domainTestdata` / `SIMCTL_CHILD_DOMAIN_TESTDATA` / `inputs.dir` wiring — 07-05 (core:testing, feature/agenda), 07-06 (feature/speakers), 07-12 (core:domain)
- [ ] `scripts/record-agenda-snapshot.sh` + gated recorder + committed raw responses — 07-03; neutral model snapshot + drift-check test in `:core:data` + `AgendaSnapshot` decoder + `filter-cases.json` + format README — 07-04
- [ ] Use-case tests: `ClassifySessionUseCaseTest` (07-01), `FilterSessionsUseCaseTest`, `GroupSessionsUseCaseTest`, `DayAssignmentTest` (07-12), `SpeakerIndexUseCaseTest`, `VenueLanguageUseCaseTest`, `ObserveAgendaUseCaseTest` (07-13)
- [ ] `DomainModuleTest`, `KoinGraphTest` extra types, `IosKoinTest` resolution — 07-01, 07-11, 07-13
- [ ] `FakeSessionFiltersStore` + `testDoublesModule` binding — 07-09
- [ ] Android characterization: grouping (two zones, two locales) — 07-05; filtering, drawer + equality (D-14), classification, speaker index, venue language — 07-06
- [ ] iOS characterization: extracted static functions — 07-07; `AgendaCharacterizationTests`, `SpeakersVenueCharacterizationTests`, golden helper, pbxproj entries — 07-08; `UserDefaults*` store tests (Kotlin iosTest) — 07-11
- [ ] `names-exclude.txt` entry for `SessionFilter` + regenerated baselines — 07-09 (D-13 exception recorded; baselines also regenerated with the Swift call sites in 07-08)

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
