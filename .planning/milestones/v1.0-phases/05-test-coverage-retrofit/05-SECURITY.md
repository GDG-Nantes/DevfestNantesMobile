---
phase: "5"
slug: "test-coverage-retrofit"
status: verified
# threats_open = count of OPEN threats at or above workflow.security_block_on severity (the blocking gate)
threats_open: 0
asvs_level: 1
created: "2026-10-16"
---

# Phase 5 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Live backend → CI | The canary reads confetti-app.dev; its availability must not decide CI status | see the owning plan's `<threat_model>` |
| Build plugin registry → build | A new Gradle plugin enters the build classpath | see the owning plan's `<threat_model>` |
| Backend → date parsing | Server-provided date strings are parsed on device | see the owning plan's `<threat_model>` |
| Device locale → outbound URL | The OpenFeedback fallback URL is built from a locale-sensitive date | see the owning plan's `<threat_model>` |
| Production fallback → user | `buildVenueStub` is shown offline in production | see the owning plan's `<threat_model>` |
| Kotlin → Swift header | New public `:core:model` objects enter `shared.h` | see the owning plan's `<threat_model>` |
| Public backend → repository | Recorded live responses are committed | see the owning plan's `<threat_model>` |
| Test loopback server → client | MockServer serves HTTP on localhost | see the owning plan's `<threat_model>` |
| Generated test helpers → production module | Data builders compile into :core:network commonMain | see the owning plan's `<threat_model>` |
| Device storage → services | Persisted bookmarks, filters and consent are read at startup | see the owning plan's `<threat_model>` |
| Consent state → Firebase collection | Stored consent drives Analytics, Crashlytics and Performance collection | see the owning plan's `<threat_model>` |
| Test support module → production graph | `:core:testing` and `testDoublesModule` must stay test-only | see the owning plan's `<threat_model>` |
| Koin composition root → Agenda start screen | A new named binding is resolved at app start | see the owning plan's `<threat_model>` |
| Tests → production code | Tests may expose bugs; fixing them would change user-visible behavior | see the owning plan's `<threat_model>` |
| Remote config → feature flags | Remote values gate the OpenFeedback UI | see the owning plan's `<threat_model>` |
| Koin composition root → session detail | A new `FeedbackFlags` binding is resolved each time the feedback form is composed | see the owning plan's `<threat_model>` |
| Test APK → production artifacts | androidTest dependencies must not leak into debug/release APKs | see the owning plan's `<threat_model>` |
| CI build resources | More androidTest APKs share the Gradle daemon heap | see the owning plan's `<threat_model>` |
| Measurement artifacts → phase verdict | The before/after report decides "measurably improved" | see the owning plan's `<threat_model>` |
| Test APK → network (Firebase Remote Config) | The session detail smoke test must not create a remote-config instance or fetch over the network | see the owning plan's `<threat_model>` |
| Prebuilt third-party bytecode → app runtime classpath | OpenFeedback AARs were compiled against kotlinx-datetime 0.6.x. Gradle conflict resolution, not OpenFeedback, decides which classes exist at runtime | see the owning plan's `<threat_model>` |
| Maven Central → build | A new coordinate is resolved: `org.jetbrains.kotlinx:kotlinx-datetime:0.8.0-0.6.x-compat` (and its `-jvm` and iOS klib artifacts) | see the owning plan's `<threat_model>` |
| Dependency bots → catalog | Weekly Dependabot gradle PRs (`.github/dependabot.yml`) can propose a plain kotlinx-datetime version that drops the compat classes | see the owning plan's `<threat_model>` |
| Developer device → production OpenFeedback Firestore | The UAT re-check posts a real comment | see the owning plan's `<threat_model>` |

---

## Threat Register

Built from the `<threat_model>` blocks of plans 05-01 to 05-11 (register authored at plan time). Each plan carries its own supply-chain row, shown as `T-5-SC (plan)`. Mitigations are abridged; the plan is authoritative.

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-5-01 | Denial of Service | Default `:core:data:jvmTest` depending on the live backend | medium | mitigate | `*LiveNetwork*` excluded unless `-PliveNetworkTests`; CANARY-EXCLUDED and CANARY-RUNNABLE gates | closed |
| T-5-02 | Tampering | Coverage instrumentation reaching shipped artifacts | low | accept | Kover instruments test tasks only and adds no runtime dependency; `:androidApp:assembleDebug` and CI build-debug stay green | closed |
| T-5-SC (05-01) | Tampering | Gradle plugin install (Kover) | high | mitigate | Exact version pinned in the catalog after a read-only Maven metadata re-check; coordinate vetted in RESEARCH Package Legitimacy Audit (official JetBrains `or... | closed |
| T-5-03 | Denial of Service | `toInstantOrNull` / display helpers on malformed dates | medium | mitigate | Parser returns null instead of throwing; IsoDateTest covers empty, garbage, date-only and zone-less input | closed |
| T-5-04 | Tampering | OpenFeedback fallback URL in non-Latin-digit locales | medium | mitigate | `LocalDate.toString()` (ASCII); ar-EG test asserts an all-ASCII URL | closed |
| T-5-SC (05-02) | Tampering | Dependency changes | low | accept | No new coordinate: `robolectric` and `androidx-test-core` already pinned in the catalog, added to the test-only bundle | closed |
| T-5-05 | Tampering | `buildVenueStub` content | medium | mitigate | Byte diff against origin/main (VENUE-STUB-UNCHANGED) | closed |
| T-5-06 | Information Disclosure | Swift name identity in `shared.h` (Phase 3 halt class) | medium | mitigate | `swift-names-gate.sh check` after linking the framework | closed |
| T-5-SC (05-03) | Tampering | Dependency changes | low | accept | No dependency added in this plan | closed |
| T-5-07 | Information Disclosure | `RecordedResponses.kt` | medium | mitigate | Only public conference data is recorded; RECORDED-CLEAN scan for key, token and password shapes | closed |
| T-5-08 | Elevation of Privilege | Data builders in :core:network commonMain | low | mitigate | :core:network is never `export()`-ed (swift-names gate); Kover excludes the generated package; unused code is stripped by R8/K/N DCE; JSON-variant fallback i... | closed |
| T-5-09 | Information Disclosure | Cleartext MockServer | low | accept | Loopback only, test-only; production network security config untouched | closed |
| T-5-SC (05-04) | Tampering | `apollo-mockserver` install | high | mitigate | Exact pin after Maven metadata re-check; coordinate vetted in RESEARCH audit (Apollo's own `com.apollographql.mockserver` group); old 4.x coordinate forbidden | closed |
| T-5-10 | Tampering | Persisted pref keys and formats | high | mitigate | Literal-key tests on real SharedPreferences; PREFS-KEYS-OK diff of production string literals against origin/main | closed |
| T-5-11 | Repudiation | Consent default and propagation | high | mitigate | Test pins "nothing enabled, no agreement" before any choice, and that each choice is persisted and applied to Crashlytics/Performance flags | closed |
| T-5-12 | Information Disclosure | Dummy FirebaseApp in unit tests | low | accept | Fake project id and API key; Robolectric unit tests only; no real project is contacted successfully | closed |
| T-5-22 | Tampering | Test-only Crashlytics mapping-file id in `feature/settings/src/test/res` | medium | mitigate | Lives in the unit-test source set only; TEST-RES-CONFINED proves it is absent from the library's debug and release packaged resources, so the app keeps the p... | closed |
| T-5-SC (05-05) | Tampering | Dependency changes | low | accept | No dependency added in this plan | closed |
| T-5-13 | Elevation of Privilege | `:core:testing` / `testDoublesModule` leaking into production | high | mitigate | TESTING-ISOLATED-OK (configurations and production source references) | closed |
| T-5-14 | Denial of Service | Named dispatcher mis-binding crashing the start screen | medium | mitigate | Module verify + resolution tests, AndroidGraphVerifyTest, KoinStartupTest/ProductionGraphStartupTest (allowOverride(false)), ANDROID-LAUNCH-OK | closed |
| T-5-SC (05-06) | Tampering | Turbine install | high | mitigate | Exact pin after Maven metadata re-check; vetted in RESEARCH audit (`app.cash.turbine:turbine`); test configurations only | closed |
| T-5-15 | Tampering | Production ViewModels changed while "making tests pass" | medium | mitigate | No production file in `files_modified`; `git status --porcelain` checks on the main source sets; D-10 deferred list | closed |
| T-5-SC (05-07) | Tampering | Dependency changes | low | accept | No dependency added in this plan | closed |
| T-5-16 | Tampering | Feedback flag logic (local kill switch ANDed with remote flags) | medium | mitigate | `FeedbackFormViewModelTest` on the fake pins that `OpenFeedbackConfig.enabled = false` turns both flags off regardless of remote values, both at init and aft... | closed |
| T-5-17 | Information Disclosure | Dummy FirebaseApp in `KoinStartupTest` (existing; this plan edits one comment and adds one binding assertion) | low | accept | Fake ids/key; fetch to a non-existent project fails and is ignored. The new FeedbackForm unit test does not initialize Firebase at all | closed |
| T-5-23 | Denial of Service | `FeedbackFlags` seam in `FeedbackFormViewModel` / `sessionDetailModule` (production remote-config wiring, D-23) | medium | mitigate | Behavior-neutral adapter. Keys, AND logic and log lines stay in the ViewModel (SEAM-WIRED-OK), the listener overload is unchanged (main-thread callback), and... | closed |
| T-5-SC (05-08) | Tampering | Dependency changes | low | accept | No dependency added; mocking frameworks explicitly absent (grep gate) | closed |
| T-5-18 | Elevation of Privilege | `:core:testing` in feature androidTest | high | mitigate | Added only as `androidTestImplementation`; TESTING-ISOLATED-OK gate | closed |
| T-5-19 | Denial of Service | CI instrumentation job (D8 dex-merging OOM) | medium | mitigate | Log scan for `OutOfMemoryError` (D8-OOM-RETURNED) and explicit stop-and-report rule; no silent sharding (D-19) | closed |
| T-5-SC (05-09) | Tampering | Dependency changes | low | accept | No new coordinate: `koin-test-junit4`, `ui-test-junit4`, `androidx-test-runner`/`ext-junit` already pinned in the catalog | closed |
| T-5-20 | Tampering | Baseline/final comparison integrity | medium | mitigate | Baseline committed in 05-01 before any change; measurement config diffed against the baseline commit; comparison regenerated in the verify and byte-compared ... | closed |
| T-5-21 | Denial of Service | Live remote-config fetch making the session detail smoke test nondeterministic in the CI instrumentation job | medium | mitigate | `FeedbackFlags` overridden by a private androidTest fake (fixed flags, synchronous fetch completion) under `allowOverride(true)`, so the Firebase-backed fact... | closed |
| T-5-SC (05-10) | Tampering | Dependency changes | low | accept | No dependency added in this plan | closed |
| T-5-24 | Denial of Service | Session detail feedback area: OpenFeedback linkage on the app runtime classpath (crash on main thread) | high | mitigate | The catalog resolves `0.8.0-0.6.x-compat`, checked on the debug, release and unit-test runtime classpaths (DATETIME-COMPAT-GUARD-GREEN). `OpenFeedbackDatetim... | closed |
| T-5-25 | Tampering | R8 configuration hiding missing classes (`-dontwarn` on a false premise) | medium | mitigate | The three suppressions and their comment are removed. `assembleRelease` passes and `missing_rules.txt` has no kotlinx-datetime entry (R8-CLEAN-WITHOUT-SUPPRE... | closed |
| T-5-26 | Denial of Service | Future plain kotlinx-datetime ≥0.8.1/0.9 bump (Dependabot or manual) silently outranking compat | medium | mitigate | The guard test turns such a PR red in CI. The catalog comment and the STATE.md follow-up state that compat ends with 0.8.x and that an OpenFeedback release o... | closed |
| T-5-27 | Tampering | iOS exported API (`shared.h`) drifting because the deprecated class becomes a distinct type under compat | medium | mitigate | `core:model` commonMain imports move to `kotlin.time.Instant` in the same commit as the pin. Task 1's pre-push gate requires `shared.h` to be byte-identical ... | closed |
| T-5-28 | Tampering | Real comment written to the production OpenFeedback project during the UAT re-check | low | accept | A human performs it deliberately, picks the session and text, and can edit the comment afterwards. Automated tests never touch Firestore (the guard is pure J... | closed |
| T-5-SC (05-11) | Tampering | New Maven coordinate `org.jetbrains.kotlinx:kotlinx-datetime:0.8.0-0.6.x-compat` | low | accept | Official JetBrains group, the same publisher as the 0.8.0 artifact already in use. It is documented in the kotlinx-datetime README ("Deprecation of Instant")... | closed |

*Status: open · closed · open — below high threshold (non-blocking)*
*Severity: critical > high > medium > low — only open threats at or above workflow.security_block_on count toward threats_open*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

**Verification evidence (L1, 2026-10-16):** every mitigate row maps to a gate token reported green in its plan's SUMMARY (no SUMMARY raised a new threat flag), spot-checked in the tree: the live canary is excluded unless `-PliveNetworkTests` (`core/data/build.gradle.kts`); `toInstantOrNull` returns null on unparseable input (`IsoDate.kt`); Kover 0.9.11, Apollo MockServer 0.3.1 and Turbine 1.2.1 are exact catalog pins; `:core:testing` appears only in test configurations; `kotlinxDatetime = "0.8.0-0.6.x-compat"`, `proguard-rules.pro` has no kotlinx-datetime rule and keeps `-keep class io.openfeedback.** { *; }`; no first-party source outside the guard test names the deprecated kotlinx-datetime Instant/Clock types; `SessionDetailSmokeTest` fakes `FeedbackFlags`.

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-5-01 | T-5-02 | Kover instruments test tasks only and adds no runtime dependency; `:androidApp:assembleDebug` and CI build-debug stay green | plan-time threat model (planner, approved with the plan) | 2026-10-15 |
| AR-5-02 | T-5-SC (05-02) | No new coordinate: `robolectric` and `androidx-test-core` already pinned in the catalog, added to the test-only bundle | plan-time threat model (planner, approved with the plan) | 2026-10-15 |
| AR-5-03 | T-5-SC (05-03) | No dependency added in this plan | plan-time threat model (planner, approved with the plan) | 2026-10-15 |
| AR-5-04 | T-5-09 | Loopback only, test-only; production network security config untouched | plan-time threat model (planner, approved with the plan) | 2026-10-15 |
| AR-5-05 | T-5-12 | Fake project id and API key; Robolectric unit tests only; no real project is contacted successfully | plan-time threat model (planner, approved with the plan) | 2026-10-15 |
| AR-5-06 | T-5-SC (05-05) | No dependency added in this plan | plan-time threat model (planner, approved with the plan) | 2026-10-15 |
| AR-5-07 | T-5-SC (05-07) | No dependency added in this plan | plan-time threat model (planner, approved with the plan) | 2026-10-15 |
| AR-5-08 | T-5-17 | Fake ids/key; fetch to a non-existent project fails and is ignored. The new FeedbackForm unit test does not initialize Firebase at all | plan-time threat model (planner, approved with the plan) | 2026-10-15 |
| AR-5-09 | T-5-SC (05-08) | No dependency added; mocking frameworks explicitly absent (grep gate) | plan-time threat model (planner, approved with the plan) | 2026-10-15 |
| AR-5-10 | T-5-SC (05-09) | No new coordinate: `koin-test-junit4`, `ui-test-junit4`, `androidx-test-runner`/`ext-junit` already pinned in the catalog | plan-time threat model (planner, approved with the plan) | 2026-10-15 |
| AR-5-11 | T-5-SC (05-10) | No dependency added in this plan | plan-time threat model (planner, approved with the plan) | 2026-10-15 |
| AR-5-12 | T-5-28 | A human performs it deliberately, picks the session and text, and can edit the comment afterwards. Automated tests never touch Firestore (the guard is pure J... | plan-time threat model (planner, approved with the plan) | 2026-10-15 |
| AR-5-13 | T-5-SC (05-11) | Official JetBrains group, the same publisher as the 0.8.0 artifact already in use. It is documented in the kotlinx-datetime README ("Deprecation of Instant")... | plan-time threat model (planner, approved with the plan) | 2026-10-15 |

*Accepted risks do not resurface in future audit runs.*

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-10-16 | 39 | 39 | 0 | secure-phase (L1 short-circuit, orchestrator) |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-10-16
