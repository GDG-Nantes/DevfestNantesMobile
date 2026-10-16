---
phase: 05-test-coverage-retrofit
plan: 03
subsystem: testing
tags: [fixtures, deterministic-stubs, kmp, core-model, mocked-store]

requires:
  - phase: 05-test-coverage-retrofit
    provides: "05-02 shared ISO parser and characterization suites"
provides:
  - "SessionFixtures (10 named sessions + all), SpeakerFixtures (6 named speakers + all), PartnerFixtures (byCategory over all 6 PartnerCategory values) in :core:model stubs"
  - "buildSessionStub/buildSpeakerStub/buildPartnerStub/buildScheduleSlotStub as deterministic wrappers; the seven @Preview callers compile unchanged"
  - "DevFestNantesStoreMocked serving the fixtures; getSpeakerSessions implemented (was emptyList())"
  - "FixturesTest (JVM + iOS) and a real DevFestNantesStoreContractTest with concrete assertions"
affects: [05-04, 05-05, 05-06, 05-07, 05-08, 05-09, 05-10]

actuals:
  tokens: 14000
  tasks: 2
  commits: 2

tech-stack:
  added: []
  patterns:
    - "Fixture objects with intention-revealing vals; private lookup helpers fail loudly with first { } so a typo throws at load time"
    - "Wrapper-over-fixture: legacy buildXStub() entry points return a fixed fixture instead of generating data"

key-files:
  created:
    - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/stubs/SessionFixtures.kt
    - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/stubs/SpeakerFixtures.kt
    - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/stubs/PartnerFixtures.kt
    - core/model/src/commonTest/kotlin/com/gdgnantes/devfest/core/model/stubs/FixturesTest.kt
  modified:
    - core/model/src/commonMain/kotlin/com/gdgnantes/devfest/core/model/stubs/StoreStubs.kt
    - core/data/src/commonMain/kotlin/com/gdgnantes/devfest/core/data/DevFestNantesStoreMocked.kt
    - core/data/src/commonTest/kotlin/com/gdgnantes/devfest/core/data/DevFestNantesStoreContractTest.kt
    - core/testing/src/commonTest/kotlin/com/gdgnantes/devfest/core/testing/FakeStoresTest.kt

key-decisions:
  - "Fixture constant names and values follow the plan's contract table verbatim, because 05-05..05-10 assert against them"
  - "buildPartnerStub returns the first PLATINIUM partner (Loire Cloud) via PartnerFixtures.byCategory, as the plan specified"
  - "PartnerFixtures also exposes the seven partners as named vals (pixelStudio, loireCloud, ...) next to byCategory; additive"

patterns-established:
  - "Pattern: tests express expectations as filters over named fixtures (FR + ADVANCED yields only webRendering)"

requirements-completed: [TEST-01]

coverage:
  - id: D1
    description: "No kotlin.random in :core:model stubs or DevFestNantesStoreMocked; one fixed varied dataset is the single source"
    requirement: TEST-01
    verification:
      - kind: unit
        ref: "core/model FixturesTest (10 tests on jvmTest and iosSimulatorArm64Test); git grep for kotlin.random / Random. / MAX_SESSIONS / MAX_PARTNERS prints nothing"
        status: pass
    human_judgment: false
  - id: D2
    description: "Mocked store serves the fixtures; getSpeakerSessions returns the speaker's sessions; two instances return equal data"
    requirement: TEST-01
    verification:
      - kind: unit
        ref: "core/data DevFestNantesStoreContractTest (11 tests on jvmTest and iosSimulatorArm64Test), core/testing FakeStoresTest"
        status: pass
    human_judgment: false
  - id: D3
    description: "buildVenueStub unchanged (production offline fallback)"
    requirement: TEST-01
    verification:
      - kind: other
        ref: "diff of the buildVenueStub body against origin/main prints VENUE-STUB-UNCHANGED"
        status: pass
    human_judgment: false
  - id: D4
    description: "New public objects do not shift Swift names in shared.h"
    requirement: TEST-01
    verification:
      - kind: other
        ref: "swift-names-gate.sh check prints SWIFT-NAMES-OK (only the new fixture member names are listed as additions)"
        status: pass
    human_judgment: false

duration: 40min
completed: 2026-10-16
status: complete
plan_head_before: 6a8bdabd55cee315815337867d6104744e4f3637
plan_head_after: faf10a37360ec57cad85a26b6d5f6110180adf3f
commits: 2
---

# Phase 5 Plan 03: Fixed Fixture Dataset Summary

**The random stubs are replaced by three hand-written fixture objects in `:core:model` (10 sessions, 6 speakers, partners in all 6 categories) that previews, `DevFestNantesStoreMocked` and tests all read, with `getSpeakerSessions` now implemented on the mock.**

## Performance

- **Duration:** about 40 min
- **Tasks:** 2
- **Files modified:** 8 (4 new, 4 changed)

## Accomplishments

- `SpeakerFixtures`, `SessionFixtures` and `PartnerFixtures` follow the plan's contract table exactly: ids `speaker-*` / `session-*`, `+02:00` slots on 2025-10-16 and 2025-10-17, all 7 rooms plus the room-less lunch, FR/EN/null, all complexities and null, all 7 `SessionType` values plus null (`closing`), a speaker shared by two sessions (alice), multi-speaker sessions, alice with one social of every `SocialType`, farid with every optional field null, only lunch without `openFeedbackFormId`.
- `StoreStubs.kt` lost `kotlin.random`, `MAX_ROOMS`, `MAX_SPEAKERS`, `MAX_SPEAKER_PER_SESSION`, `STUB_STARTING_HOUR`, `STUB_ENDING_HOUR` (Task 1) and `MAX_SESSIONS`, `MAX_PARTNERS` (Task 2). The four `buildXStub()` functions keep their signatures. `buildVenueStub` is byte-identical to `origin/main`.
- `DevFestNantesStoreMocked` reads the fixtures; `speakersCache` is now the distinct speaker list; `getSpeakerSessions` uses the same rule as `GraphQLStore`.
- `DevFestNantesStoreContractTest` asserts concrete fixture values and unknown-id results (11 tests); `FakeStoresTest` asserts the fixture list. Everything passes on JVM and `iosSimulatorArm64`.

## Task Commits

1. Task 1: `34ce024` test(05-03): fixed fixture dataset replaces random stubs (D-01..D-04)
2. Task 2: `faf10a3` test(05-03): mocked store serves the fixed fixtures

## Verification

- FIXTURES-GREEN (FixturesTest present in the jvmTest and iosSimulatorArm64Test results), VENUE-STUB-UNCHANGED, DI-GATE-OK, SWIFT-NAMES-OK.
- `./gradlew testDebugUnitTest jvmTest` over the whole project: BUILD SUCCESSFUL, so no other module depended on the old random shapes.
- Preview modules build: `:feature:agenda`, `:feature:speakers`, `:feature:session-detail`, `:feature:about`, `:feature:venue`, `:androidApp` `assembleDebug`, and `:shared:linkDebugFrameworkIosSimulatorArm64`; detekt clean.
- CI on `faf10a3`: android.yml run 38863543487 and ios.yml run 38863543475, both green (CI-GREEN-BOTH).

## TDD Notes

The fixtures and `FixturesTest` were written together and committed in one commit, as the plan prescribes; there is no separate RED commit. The new test asserts properties of data that did not exist before, so a RED run against the old random stubs would only have shown unresolved references. For Task 2 the contract test was updated alongside the mock change in one commit.

## Deviations from Plan

None. Plan executed as written.

One note on behavior, not a deviation: `SocialItem.Builder` prefixes `baseUrl` (which already ends with `/`) with another `/`, so the fixture socials resolve to links like `https://github.com//alice-moreau`. The fixtures pass the raw handles the plan specifies, and the double slash is pre-existing builder behavior (D-10: characterized, not fixed). Candidate for the deferred list.

## Issues Encountered

None.

## Authentication Gates

None.

## Known Stubs

None. The buildXStub() wrappers are intentional preview entry points backed by real fixtures.

## Threat Flags

None. T-5-05 (venue fallback content) mitigated by the byte diff; T-5-06 (Swift name identity) mitigated by `swift-names-gate.sh check`, which reports only the new fixture names; T-5-SC: no dependency added.

## Next Phase Readiness

Ready for 05-04. Later plans can reference `SessionFixtures.*`, `SpeakerFixtures.*` and `PartnerFixtures.byCategory`; the derived facts (FR, EN, ADVANCED, titan/belem sets, alice's sessions, 6/4 day split) are listed in the `SessionFixtures` KDoc and pinned by `FixturesTest`.

## Self-Check: PASSED

Files verified present; commits 34ce024 and faf10a3 on the branch (2 measured from the plan ledger); FIXTURES-GREEN, VENUE-STUB-UNCHANGED, SWIFT-NAMES-OK, DI-GATE-OK and CI-GREEN-BOTH observed.
