# Phase 5: Test Coverage Retrofit - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-10-13
**Phase:** 05-test-coverage-retrofit
**Areas discussed:** Fixture determinism & dates, ViewModel test harness, Store & mapper tests, Compose smoke & measurement

---

## Fixture determinism & dates

| Option | Description | Selected |
|--------|-------------|----------|
| Injectable seeded Random | Builders take `random: Random = Random(SEED)`; minimal diff | |
| Fully fixed fixture data | Hand-written constant lists, stable ids | ✓ |
| Both | Seeded for previews + fixed for assertions | |

**User's choice:** Fully fixed fixtures (after a clarification round).
**Notes:** The user asked whether fully deterministic fixtures aren't better, apart from the rewrite cost. Claude agreed: seeded Random is reproducible but doesn't reveal intent, drifts when someone adds or reorders RNG calls, and only covers edge cases by luck. The rewrite is small (~115 lines, mock mode never ships). The recommendation was revised.

| Option | Description | Selected |
|--------|-------------|----------|
| Replace StoreStubs in core:model | Single fixed dataset for mock store, previews, tests | ✓ |
| Move to core:testing | Main-code previews/mock store couldn't use it | |
| Fixed + seeded Random hybrid | Keep random builders for previews | |

| Option | Description | Selected |
|--------|-------------|----------|
| java.time | minSdk 26, thread-safe (Claude's first recommendation) | |
| kotlinx-datetime in commonMain | Shared parsing, iOS-ready | ✓ |
| Keep SimpleDateFormat, just test | Per-call instances already safe | |

**User's choice:** Common parser (kotlinx-datetime / `kotlin.time.Instant`) + Android-only localized display.
**Notes:** The user interrupted to ask whether Kotlin types aren't better than Java types for a KMP project. Claude agreed: kotlinx-datetime is already a dependency, the model already uses `kotlin.time.Instant`, the parser becomes testable in commonTest on JVM+iOS, and `LocalDate.toString()` fixes the locale bug for free. The limit is localized display, which stays on Android via `DateUtils.formatDateTime` with epoch millis.

| Option | Description | Selected |
|--------|-------------|----------|
| Accept locale fix as deliberate | ASCII yyyy-MM-dd always; fixes broken URLs in non-Latin-digit locales | ✓ |
| Preserve exactly | Keep locale-dependent output | |

---

## ViewModel test harness

| Question | Options | Selected |
|----------|---------|----------|
| Flow assertions | Turbine / plain coroutines-test | Turbine |
| Dispatchers | Inject dispatcher + MainDispatcherRule / only setMain | Inject dispatcher |
| Store fake | Controllable FakeDevFestNantesStore / keep wrapping mock store | Controllable fake |
| VM scope | All 10 / logic-heavy only | All 10 |
| Bugs found by tests | Characterize, don't fix / fix with approval | Characterize, don't fix |

---

## Store & mapper tests

| Question | Options | Selected |
|----------|---------|----------|
| Mapper input | Recorded JSON / Apollo data builders / Both | Both |
| GraphQLStore | Apollo MockServer / mappers only | Apollo MockServer |
| Live GraphQLStoreJvmTest | Keep out of default CI / delete / keep in CI | Keep out of default CI |
| Contract test | Real assertions on both impls / mocked only | Both impls |
| Canary location | Manual task only / scheduled GH Actions job | Manual task only |
| SharedPreferences impls | Robolectric tests / fakes only | Robolectric tests |

---

## Compose smoke & measurement

| Question | Options | Selected |
|----------|---------|----------|
| Where to run | androidTest on emulator / Robolectric JVM / hybrid | androidTest on emulator |
| Depth | Render + key interaction / render only / full flows | Render + key interaction |
| Measurement | Kover report, no threshold / Kover + gate / inventory only | Kover report, no threshold |
| Test wiring | Koin test modules / pass ViewModel directly | Koin test modules |
| iOS Swift tests | No (commonTest on iOS) / extend XCTest | No |

**Notes:** Claude found that iOS CI only runs `:shared:iosSimulatorArm64Test`. It is recorded as a required CI widening (D-22).

## Claude's Discretion

- Exact fixture content, library versions (newest-verified), parser naming/placement in core:model, canary exclusion mechanism, plan sequencing (TEST-01 first, Kover baseline before coverage work), test naming style.

## Deferred Ideas

- Fixing bugs uncovered by tests; scheduled canary job; Swift XCTest coverage; iOS reusing the shared parser; coverage non-regression gate.
