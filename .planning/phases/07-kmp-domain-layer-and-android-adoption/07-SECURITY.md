---
phase: "7"
slug: "kmp-domain-layer-and-android-adoption"
status: verified
# threats_open = count of OPEN threats at or above workflow.security_block_on severity (the blocking gate)
threats_open: 0
asvs_level: 1
created: "2026-10-29"
---

# Phase 7 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Shared Koin graph | `domainModule` and platform store bindings join the single composition root (`allowOverride(false)`) | DI bindings (no user data) |
| Kotlin framework → Swift | `:shared` exports `:core:domain` and the moved `SessionFilter` to the iOS umbrella header | Public API names (swift-names gate) |
| Device storage → app | Android SharedPreferences and iOS NSUserDefaults payloads decoded at start-up | Favorites and filters (public session ids, low sensitivity) |
| Public GraphQL API → committed test data | Opt-in recorder captures confetti-app.dev responses into the repo | Public conference agenda data |
| Test-data root / goldens → tests | Goldens recorded on unmodified v1.0 code and read by characterization tests | Test fixtures (integrity, not secrecy) |
| App → Crashlytics | D-20 empty-speaker-name non-fatal via Timber, behind the existing consent path | Speaker ids only |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-07-01 | Tampering | `domainModule` bindings in the shared graph | medium | mitigate | `allowOverride(false)` stays in `initKoin`; `DomainModuleTest` verify, `KoinGraphTest.shared_modules_graph_is_complete` and `init_koin_rejects_a_second_definition_of_an_already_bound_type` run in CI; `domainModule` only declares types no other module declares (07-01) | closed |
| T-07-02 | Tampering | Swift-visible names of the umbrella framework | medium | mitigate | `swift-names-gate.sh check` + `selftest` on the freshly linked header in this task; Xcode build-for-testing + Swift tests prove the app compiles; `domainModule` is `@HiddenFromObjC`; no public extensions on exported model types (07-01) | closed |
| T-07-SC-01 | Tampering | Gradle dependencies | low | accept | No new library: `:core:domain` only uses catalog entries already resolved by `:core:data` (koin, ksp, kmp-native-coroutines); RESEARCH Package Legitimacy Audit lists no install (07-01) | closed |
| T-07-31 | Repudiation | CI coverage of `:core:domain` | medium | mitigate | `:core:domain:iosSimulatorArm64Test` added to `.github/workflows/ios.yml` and Kover merges the module; acceptance greps both lines; `:core:domain:jvmTest` already runs in the Android CI `jvmTest` aggregate (07-02) | closed |
| T-07-SC-02 | Tampering | Gradle dependencies | low | accept | No new library: a project dependency on an existing module only (07-02) | closed |
| T-07-03 | Information disclosure | Committed raw and neutral snapshot files | low | mitigate | Only the app's own three public queries plus a speakersPage id count are recorded with the public `conference` header (no credential exists); the executor inspects the files with jq before committing (precedent T-5-07) (07-03) | closed |
| T-07-05 | Denial of service | Network access in CI test runs | low | mitigate | Recorder excluded from `jvmTest` unless `-PrecordAgendaSnapshot`; acceptance checks "No tests found" for it in a default run (07-03) | closed |
| T-07-SC-03 | Tampering | Gradle dependencies | low | accept | No new library: `java.net.http` (JDK) and `kotlinx-serialization-json` are already available (07-03) | closed |
| T-07-04 | Tampering | Neutral snapshot drifting from the raw responses | medium | mitigate | `neutral_snapshot_matches_the_raw_responses` fails the default jvmTest run on any byte difference; only the record mode rewrites it (07-04) | closed |
| T-07-SC-04 | Tampering | Gradle dependencies | low | accept | No new library: `:core:testing` only adds the existing `kotlinx-serialization-json` catalog entry (07-04) | closed |
| T-07-06 | Repudiation / Tampering | Goldens and characterization tests | high | mitigate | Goldens recorded on unmodified v1.0 code and committed in this plan, before 07-12/07-15 move anything; `GoldenFiles` never overwrites an existing golden; acceptance checks show the only production diff is one visibility keyword; later plans may only make mechanical edits (D-18) (07-05) | closed |
| T-07-07 | Tampering | Test-data root taken from a system property / environment variable | low | accept | Test-only code path, never compiled into the app; it reads and writes only under the configured testdata directory (07-05) | closed |
| T-07-SC-05 | Tampering | Gradle dependencies | low | accept | No new library: Robolectric, Compose UI test and kotlinx-serialization are already in the catalog and the test bundle (07-05) | closed |
| T-07-32 | Repudiation / Tampering | Filtering and speaker goldens | high | mitigate | Recorded on unmodified v1.0 code and committed here, before 07-12/07-13 implement the shared rules and 07-14 to 07-18 move anything; `GoldenFiles` never overwrites an existing golden; acceptance shows no production file in either commit; later edits are wiring-only (D-18) (07-06) | closed |
| T-07-SC-06 | Tampering | Gradle dependencies | low | accept | No new library: Robolectric, Compose UI test and kotlinx-serialization are already in the catalog and the test bundle (07-06) | closed |
| T-07-08 | Tampering | iOS agenda filtering and grouping behavior before characterization | high | mitigate | Only the D-10 filter extraction (verbatim bodies) and a one-keyword access change land before 07-08; the diff gate proves AgendaViewModel.swift changes by one line and SpeakersViewModel/VenueViewModel/agendaRow are untouched; existing Swift tests green; 07-08 then pins the v1.0 code on real data (07-07) | closed |
| T-07-SC-07 | Tampering | Swift packages / Gradle dependencies | low | accept | No package added (07-07) | closed |
| T-07-09 | Denial of service | Hosted test process (Koin start, duplicate Kotlin runtime) | medium | mitigate | ViewModels are created only after the idempotent `IosDependencies.shared.start()` (the host app already started Koin) and no test calls `observe*` or mutates persisted state; the duplicate-link symptom is checked and fixed with `CLANG_MODULES_AUTOLINK = NO` on the test target only (07-08) | closed |
| T-07-10 | Tampering | Swift-names baselines | medium | mitigate | Regenerated only in the commit adding the Swift call sites, after reviewing that no name is removed and no member set changes; check + selftest pass (07-08) | closed |
| T-07-SC-08 | Tampering | Swift packages / Gradle dependencies | low | accept | No package added; Swift Testing ships with Xcode (07-08) | closed |
| T-07-12 | Tampering | Swift name resolution of `SessionFilter` | high | mitigate | names-exclude entry with evidence, baselines regenerated in the export commit, gate check + selftest, Xcode build-for-testing + Swift tests green with no iosApp/iosApp change; stop-and-report if the app does not compile unchanged (07-09) | closed |
| T-07-13 | Tampering (input validation, ASVS V5) | `SessionFilterCodec.decodeOrNull` | medium | mitigate | Malformed, unknown-enum and extra-key entries return null instead of throwing (tests); `decode` keeps throwing only for the Android store, which catches like v1.0 (07-09) | closed |
| T-07-SC-09 | Tampering | Gradle dependencies | low | accept | `kotlinx-serialization-json` is an existing catalog entry already used by `:core:model` and `:feature:agenda` (07-09) | closed |
| T-07-11 | Denial of service | Android saved filters after the class move (start-up decode) | high | mitigate | Byte-identity pinned by the unchanged literal-JSON assertions of `SessionFiltersServiceImplTest` running on the moved class; codec literal tests on three runtimes (07-10) | closed |
| T-07-SC-10 | Tampering | Gradle dependencies | low | accept | No dependency change (07-10) | closed |
| T-07-14 | Denial of service (input validation, ASVS V5) | Store load from NSUserDefaults | high | mitigate | Safe casts only (`as? String`), `decodeOrNull` per entry, non-array/absent -> empty; tests with malformed JSON, non-string elements and non-array values; no Kotlin exception can reach Swift (07-11) | closed |
| T-07-15 | Information disclosure (ASVS V8) | Persisted favorites/filters | low | accept | Non-sensitive preferences in the app's own defaults domain; no payload logging (prohibition) (07-11) | closed |
| T-07-16 | Tampering | Legacy keys and duplicate bindings | medium | mitigate | Tests prove legacy values stay identical after writes and are never read; one `single` per store type in the iOS module, `allowOverride(false)` at start, IosKoinTest resolves each once (07-11) | closed |
| T-07-SC-11 | Tampering | Dependencies | low | accept | Only `platform.Foundation` APIs; no new package (07-11) | closed |
| T-07-17 | Denial of service | `GroupSessionsUseCase` / `DayAssignment` on bad input | medium | mitigate | `toInstantOrNull()` for day starts, epoch-millisecond starts already parsed by `ScheduleSlot`, empty inputs handled; tests cover invalid day start and empty lists (07-12) | closed |
| T-07-18 | Tampering | Parity of the moved logic | high | mitigate | Golden comparisons on both runtimes against v1.0 Android and iOS outputs recorded in 07-05/07-06/07-08; the iOS deviation is asserted, not hidden; prohibition against editing goldens (07-12) | closed |
| T-07-SC-12 | Tampering | Dependencies | low | accept | `:core:testing` and `kotlinx-serialization-json` are existing project modules / catalog entries, test scope only (07-12) | closed |
| T-07-19 | Denial of service | Store failure inside `ObserveAgendaUseCase` | medium | accept | v1.0 debt kept on purpose (strict parity, D-04): failure propagates to the ViewModel exactly as before; pinned by `ObserveAgendaUseCaseTest` and the existing `store_failure_is_characterized`; logged in deferred items (07-13) | closed |
| T-07-20 | Tampering | Dispatcher and use-case bindings | medium | mitigate | One named dispatcher single with a unique qualifier, factories only for stateless use cases, verify with and without upstream types, `KoinGraphTest` duplicate-definition test, DI gate (07-13) | closed |
| T-07-21 | Tampering | Speaker index parity | low | mitigate | Both goldens asserted on two runtimes; decision gate stops instead of bending semantics (07-13) | closed |
| T-07-SC-13 | Tampering | Dependencies | low | accept | No new library (coroutines, Koin, KMP-NativeCoroutines already in the catalog) (07-13) | closed |
| T-07-30 | Tampering | Two holders over `SHARED_PREFERENCES_KEY_AGENDA_FILTERS` during the transition | high | mitigate | One `SessionFiltersServiceImpl` definition bound under both types (no new instance); verify counts exactly one production `SessionFiltersStore::class` binding; `allowOverride(false)`, Koin verify, AndroidGraphVerifyTest and the DI gate run in verify (07-14) | closed |
| T-07-SC-14 | Tampering | Dependencies | low | accept | No dependency change (07-14) | closed |
| T-07-24 | Tampering | Agenda behavior (grouping, order, labels, scroll index) | high | mitigate | All v1.0 tests and the 07-05/07-06 characterizations (goldens unchanged) green with wiring-only edits (D-18); agenda smoke on an emulator in 07-16; D-12 screenshot checkpoint in 07-19 (07-15) | closed |
| T-07-34 | Denial of service | Removal of the `IO_DISPATCHER` binding | low | mitigate | Verify fails on any remaining Kotlin reference; Koin verify tests and the DI gate run in verify; the agenda dispatcher is the `domainDispatcher` single pinned by DomainModuleTest (07-15) | closed |
| T-07-SC-15 | Tampering | Dependencies | low | accept | No new library (07-15) | closed |
| T-07-22 | Tampering | Two holders over `SHARED_PREFERENCES_KEY_AGENDA_FILTERS` | high | mitigate | Service removal and store binding in one commit (consumers on the interface since 07-14, one instance under two types until then); verify checks exactly one binding and no feature-module binding; `allowOverride(false)` + Koin verify + DI gate (07-16) | closed |
| T-07-23 | Denial of service (ASVS V5) | Filter load at start-up | medium | mitigate | Same try/catch and skip-malformed behavior as v1.0, pinned by the moved `a_service_restores_the_saved_filters_and_skips_malformed_entries` test with its literal `not-json` entry (07-16) | closed |
| T-07-SC-16 | Tampering | Dependencies | low | accept | `timber` is an existing catalog entry already used by the Android modules (07-16) | closed |
| T-07-25 | Tampering | Star and navigation behavior | medium | mitigate | 07-06 classification table re-pointed with unchanged expectations; agenda instrumentation smoke sources compile; D-12 screenshots in 07-19 cover the star and navigation (07-17) | closed |
| T-07-26 | Elevation of privilege (architecture rule) | Service-locator use from feature code | low | mitigate | Constructor injection and lambdas only; `by inject()` only in MainActivity (allowlisted); DI gate in verify (07-17) | closed |
| T-07-SC-17 | Tampering | Dependencies | low | accept | No new library (07-17) | closed |
| T-07-33 | Tampering | Speakers A-Z and venue language behavior | medium | mitigate | 07-06 speaker golden and venue table re-pointed with unchanged expectations; speakers and venue instrumentation smoke sources compile; D-12 screenshots in 07-19 cover the scroller and the venue screen; the empty-name change is the accepted D-20 behavior, pinned by SpeakerIndexEmptyNameTest (07-18) | closed |
| T-07-35 | Information disclosure | D-20 non-fatal report to Crashlytics | low | mitigate | The report carries only speaker ids (public conference data), no name or payload; it goes through the existing Timber trees, so Crashlytics data-collection consent handling is unchanged (07-18) | closed |
| T-07-SC-18 | Tampering | Dependencies | low | accept | No new library (07-18) | closed |
| T-07-27 | Denial of service | R8 stripping the serializer of the moved `@Serializable` `SessionFilter` | high | mitigate | Release build smoke with filter persistence across relaunch and a logcat check; a loss stops the plan and becomes a phase-caused fix before approval (07-19) | closed |
| T-07-28 | Repudiation / Tampering | Android parity claim | high | mitigate | Blocking-human checkpoint on 15 paired screenshots from the same emulator, never auto-approved; phase-caused differences block approval (07-19) | closed |
| T-07-29 | Information disclosure | Committed screenshots and the temporary worktree | low | mitigate | Screens show public conference data only; secrets are copied only into the temp worktree outside the repository and the worktree is removed (07-19) | closed |
| T-07-SC-19 | Tampering | Dependencies | low | accept | No package added; `apksigner` comes from the installed Android SDK build-tools (07-19) | closed |

*Status: open · closed · open — below high threshold (non-blocking)*
*Severity: critical > high > medium > low — only open threats at or above workflow.security_block_on count toward threats_open*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

Per-threat verification evidence (file:line, test names, commit history) is in the gsd-security-auditor verdict summarized in the audit trail below. Informational notes from the audit: 07-08 and 07-16 SUMMARYs have no `## Threat Flags` section (no new surface described); `scripts/record-agenda-snapshot.sh` lines 8 and 17-18 carry stale comments about which tests are excluded (documentation only).

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-01 | T-07-SC-01 | No new library: `:core:domain` only uses catalog entries already resolved by `:core:data` (koin, ksp, kmp-native-coroutines); RESEARCH Package Legitimacy Audit lists no install | 07-01 plan threat model | 2026-10-29 |
| AR-02 | T-07-SC-02 | No new library: a project dependency on an existing module only | 07-02 plan threat model | 2026-10-29 |
| AR-03 | T-07-SC-03 | No new library: `java.net.http` (JDK) and `kotlinx-serialization-json` are already available | 07-03 plan threat model | 2026-10-29 |
| AR-04 | T-07-SC-04 | No new library: `:core:testing` only adds the existing `kotlinx-serialization-json` catalog entry | 07-04 plan threat model | 2026-10-29 |
| AR-05 | T-07-07 | Test-only code path, never compiled into the app; it reads and writes only under the configured testdata directory | 07-05 plan threat model | 2026-10-29 |
| AR-06 | T-07-SC-05 | No new library: Robolectric, Compose UI test and kotlinx-serialization are already in the catalog and the test bundle | 07-05 plan threat model | 2026-10-29 |
| AR-07 | T-07-SC-06 | No new library: Robolectric, Compose UI test and kotlinx-serialization are already in the catalog and the test bundle | 07-06 plan threat model | 2026-10-29 |
| AR-08 | T-07-SC-07 | No package added | 07-07 plan threat model | 2026-10-29 |
| AR-09 | T-07-SC-08 | No package added; Swift Testing ships with Xcode | 07-08 plan threat model | 2026-10-29 |
| AR-10 | T-07-SC-09 | `kotlinx-serialization-json` is an existing catalog entry already used by `:core:model` and `:feature:agenda` | 07-09 plan threat model | 2026-10-29 |
| AR-11 | T-07-SC-10 | No dependency change | 07-10 plan threat model | 2026-10-29 |
| AR-12 | T-07-15 | Non-sensitive preferences in the app's own defaults domain; no payload logging (prohibition) | 07-11 plan threat model | 2026-10-29 |
| AR-13 | T-07-SC-11 | Only `platform.Foundation` APIs; no new package | 07-11 plan threat model | 2026-10-29 |
| AR-14 | T-07-SC-12 | `:core:testing` and `kotlinx-serialization-json` are existing project modules / catalog entries, test scope only | 07-12 plan threat model | 2026-10-29 |
| AR-15 | T-07-19 | v1.0 debt kept on purpose (strict parity, D-04): failure propagates to the ViewModel exactly as before; pinned by `ObserveAgendaUseCaseTest` and the existing `store_failure_is_characterized`; logged in deferred items | 07-13 plan threat model | 2026-10-29 |
| AR-16 | T-07-SC-13 | No new library (coroutines, Koin, KMP-NativeCoroutines already in the catalog) | 07-13 plan threat model | 2026-10-29 |
| AR-17 | T-07-SC-14 | No dependency change | 07-14 plan threat model | 2026-10-29 |
| AR-18 | T-07-SC-15 | No new library | 07-15 plan threat model | 2026-10-29 |
| AR-19 | T-07-SC-16 | `timber` is an existing catalog entry already used by the Android modules | 07-16 plan threat model | 2026-10-29 |
| AR-20 | T-07-SC-17 | No new library | 07-17 plan threat model | 2026-10-29 |
| AR-21 | T-07-SC-18 | No new library | 07-18 plan threat model | 2026-10-29 |
| AR-22 | T-07-SC-19 | No package added; `apksigner` comes from the installed Android SDK build-tools | 07-19 plan threat model | 2026-10-29 |

*Accepted risks do not resurface in future audit runs.*

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-10-29 | 54 | 54 | 0 | gsd-security-auditor (ASVS L1, block_on high) at HEAD da292983 |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-10-29

## Security Audit 2026-10-29

| Metric | Count |
|---|---|
| Threats found | 54 |
| Closed | 54 |
| Open | 0 |
