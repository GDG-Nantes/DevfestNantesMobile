---
phase: 07-kmp-domain-layer-and-android-adoption
plan: 09
subsystem: shared-model-data
tags: [kmp, serialization, swift-names, store]
requires:
  - phase: 07-06
    provides: Android filter characterizations that pin the v1.0 filter behavior
  - phase: 07-08
    provides: iOS Swift test bundle (goldens) used as the unchanged-app proof
provides:
  - "core:model SessionFilter (@Serializable data class) and top-level SessionFilterType"
  - "core:data SessionFiltersStore interface (@NativeCoroutinesState filters, setFilters)"
  - "core:data internal SessionFilterCodec (encode, decode, decodeOrNull)"
  - "core:testing FakeSessionFiltersStore bound in testDoublesModule"
  - "swift-names exclusion for SessionFilter and regenerated shared-h baseline"
affects: [07-10, 07-11, 07-12, 07-14, 07-16]
tech-stack:
  added: []
  patterns: [shared codec in commonMain with default Json, StateFlow-backed store fake]
key-files:
  created:
    - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/SessionFilter.kt
    - core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/SessionFiltersStore.kt
    - core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/SessionFilterCodec.kt
    - core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/SessionFilterCodecTest.kt
    - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/FakeSessionFiltersStore.kt
    - core/testing/src/commonTest/kotlin/com/gdgnantes/devfest/core/testing/FakeSessionFiltersStoreTest.kt
  modified:
    - scripts/swift-names/names-exclude.txt
    - scripts/swift-names/shared-h-baseline.txt
    - documentation/ARCHITECTURE.md
    - core/data/build.gradle.kts
    - core/testing/src/commonMain/kotlin/com/gdgnantes/devfest/core/testing/di/TestDoublesModule.kt
    - core/testing/src/commonTest/kotlin/com/gdgnantes/devfest/core/testing/di/TestDoublesModuleTest.kt
key-decisions:
  - "No @SerialName: JSON stays {type,value} with enum entries by name, pinned by literal assertions in SessionFilterCodecTest"
  - "SessionFilterCodec is internal to :core:data and keeps v1.0 semantics (default Json, throwing decode, decodeOrNull catching SerializationException and IllegalArgumentException)"
  - "The feature/agenda SessionFilter copy is left untouched; 07-10 switches its consumers and deletes it"
requirements-completed: [SHARED-02, SHARED-05]
status: complete
commits: 2
plan_head_before: 1991519a35ef6cf3ee811ab4fd1e2d8cfb588a73
plan_head_after: d4e5cdc
actuals:
  tokens: 14000
  tasks: 2
  commits: 2
---

# Phase 7 Plan 09: Shared SessionFilter, SessionFiltersStore and codec Summary

**Shared `SessionFilter` data class with top-level `SessionFilterType` exported to Swift with a names-exclude entry, plus the common `SessionFiltersStore` interface, the Android-format `SessionFilterCodec` and `FakeSessionFiltersStore`, with no production binding.**

## Performance

- Tasks: 2 of 2; files: 14 (6 + 8 per commit)

## Task Commits

1. **Task 1: SessionFilter in :core:model with Swift exclusion and baselines** - `994a3ac` (feat)
2. **Task 2: SessionFiltersStore, SessionFilterCodec, FakeSessionFiltersStore** - `d4e5cdc` (feat)

## D-13 exception to the AGENTS.md baseline rule

AGENTS.md says to regenerate the swift-names baselines only with a matching Swift call-site change. D-13 (locked) requires regenerating them in the export commit with no Swift change. Task 1 follows D-13: `shared-h-baseline.txt` was regenerated (7 new names: `SessionFilter`, `SessionFilter.Companion`, `SessionFilterType`, `bookmark`, `doCopy(type:value:)`, `init(type:value:)`, `value`), `referenced-names.txt` and `members-baseline.txt` came out unchanged (SessionFilter is excluded from the referenced set), and no file under `iosApp/iosApp` was touched. The exception is recorded in the commit body of `994a3ac`. The iOS app and test bundle built unchanged and the Swift tests passed (19 of 19, XCRESULT-OK), so RESEARCH A1 holds: the app-local `struct SessionFilter` wins name lookup.

## D-14 deviation

`SessionFilter` is now a data class: equal-content filter sets no longer re-emit through `StateFlow`. No visible effect. Documented in `documentation/ARCHITECTURE.md` under "Behavior deviations (v1.1)" (created here, 07-13 extends it).

## Verification

- `:core:model` jvmTest and iosSimulatorArm64Test; `:feature:agenda:compileDebugUnitTestKotlin` (both SessionFilter classes coexist without ambiguity)
- Swift gate `check` (SWIFT-NAMES-OK) and `selftest` (SWIFT-GATE-SELFTEST-OK)
- `SessionFilterCodecTest` ran on JVM, Android host and iOS simulator of `:core:data`; `FakeSessionFiltersStoreTest` and `TestDoublesModuleTest` on `:core:testing` JVM and iOS simulator
- AGENTS.md CI list: `./gradlew detekt lint`, `di-gate.sh`, `kmp-nc-version-gate.sh`, `testDebugUnitTest jvmTest testAndroidHostTest` all green after each task
- No production reference to `SessionFiltersStore` in androidMain, iosMain, jvmMain, feature or androidApp main sources

## Deviations from Plan

None - plan executed as written. Task 2 was committed as a single feat commit (tests and implementation together) as the plan specifies, rather than separate RED/GREEN commits.

## Known Stubs

None.

## Threat Flags

None. T-07-12 mitigated (exclusion, baselines, gate, Xcode build and tests unchanged); T-07-13 mitigated (`decodeOrNull` tests for malformed, unknown-enum and extra-key input).

## Self-Check: PASSED

Created files exist, commits `994a3ac` and `d4e5cdc` are in HEAD, and `git show --stat 994a3ac -- iosApp` lists no file.
