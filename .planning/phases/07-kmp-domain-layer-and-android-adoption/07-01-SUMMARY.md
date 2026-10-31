---
phase: 07-kmp-domain-layer-and-android-adoption
plan: 01
subsystem: shared-kmp
tags: [kmp, koin, swift-names, tracer, gradle]
status: complete

requires:
  - phase: 06
    provides: Koin composition root in :shared, swift-names gate, xcresult assertion
provides:
  - KMP module :core:domain (devfest.kmp.library + ksp + kmp-native-coroutines)
  - ClassifySessionUseCase (isService / isTalk / isNavigable) shared by Android and iOS
  - domainModule (Koin, @HiddenFromObjC) appended to sharedModules
  - :shared exports :core:domain to the Swift framework
affects: [07-02, 07-08, 07-09, 07-17]

actuals:
  tokens: 3000
  tasks: 1
  commits: 1
plan_head_before: 0bec387742c1a82c6c6284393b94ff64f443472f
plan_head_after: e22571e

tech-stack:
  added: []
  patterns:
    - "Use cases are Koin factory definitions in a @HiddenFromObjC domainModule"
    - "No public extensions on exported model types in :core:domain; behavior lives in use-case classes"

key-files:
  created:
    - core/domain/build.gradle.kts
    - core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/agenda/ClassifySessionUseCase.kt
    - core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/di/DomainModule.kt
    - core/domain/src/commonTest/kotlin/com/gdgnantes/devfest/core/domain/agenda/ClassifySessionUseCaseTest.kt
    - core/domain/src/jvmTest/kotlin/com/gdgnantes/devfest/core/domain/di/DomainModuleTest.kt
  modified:
    - settings.gradle.dcl
    - shared/build.gradle.kts
    - shared/src/commonMain/kotlin/com/gdgnantes/devfest/shared/di/InitKoin.kt
    - shared/src/jvmTest/kotlin/com/gdgnantes/devfest/shared/di/KoinGraphTest.kt
    - shared/src/iosTest/kotlin/com/gdgnantes/devfest/shared/di/IosKoinTest.kt

key-decisions:
  - "ClassifySessionUseCase takes a Session and exposes three named methods; isTalk is type != null && !isService"
  - "Swift-names baselines not regenerated; new header names appear only in the informational diff of check"
  - "No Android host test and no framework in :core:domain; only :shared packages the framework"

requirements-completed: [SHARED-01, SHARED-04]
---

# Phase 7 Plan 01: :core:domain tracer Summary

New KMP module `:core:domain` holding `ClassifySessionUseCase` and `domainModule`, exported from `:shared` and proven end to end on JVM, iOS simulator, the Swift header gate and Xcode.

## Accomplishments

- `:core:domain` module (android, jvm, iOS targets via `devfest.kmp.library`), `api` on `:core:model` and `:core:data`.
- `ClassifySessionUseCase`: service = OPENING/BREAK/LUNCH, talk = typed and not service, navigable = QUICKIE/CONFERENCE/CODELAB; null type is none of the three. Table test covers every `SessionType` plus null, and fails if a new enum entry is added.
- `sharedModules` is now `listOf(networkModule, dataModule, analyticsModule, domainModule)`; `:shared` has `api` + `export` of `:core:domain`.
- Tests added: `ClassifySessionUseCaseTest` (JVM + iOS sim), `DomainModuleTest` (verify + factory semantics), `KoinGraphTest` (2 new), `IosKoinTest` (1 new).

## Verification (all green)

- `:core:domain:jvmTest :core:domain:iosSimulatorArm64Test :shared:jvmTest :shared:iosSimulatorArm64Test :shared:linkDebugFrameworkIosSimulatorArm64`
- `shared.h` contains `ClassifySessionUseCase`; `swift-names-gate.sh check` -> SWIFT-NAMES-OK (new names only in informational diff), `selftest` -> SWIFT-GATE-SELFTEST-OK
- Xcode build-for-testing + test-without-building: XCRESULT-OK (11 passed, 0 failed)
- `./gradlew detekt lint`, `di-gate.sh` (DI-GATE-OK), `kmp-nc-version-gate.sh` (KMPNC-VERSION-OK 1.0.6), `testDebugUnitTest jvmTest testAndroidHostTest`

## Deviations from Plan

None - plan executed as written. Environment note: the fresh worktree lacked the gitignored `local.properties`, `androidApp/google-services.json` and `iosApp/iosApp/GoogleService-Info.plist`; they were copied from the main checkout (ignored by git, not committed) so Gradle and Xcode could run.

## Known Stubs

None.

## Threat Flags

None. T-07-01 and T-07-02 mitigations hold: `allowOverride(false)` kept, `verify()` tests pass, module hidden from ObjC, no public extensions on exported model types.

## Commits

- e22571e: feat(07-01): add :core:domain with the shared session classification

## Self-Check: PASSED

All created files present; commit e22571e is an ancestor of HEAD; no change under `iosApp/` or `scripts/swift-names/`.
