---
phase: "4"
slug: "hilt-to-koin-di-migration"
status: verified
# threats_open = count of OPEN threats at or above workflow.security_block_on severity (the blocking gate)
threats_open: 0
asvs_level: 1
created: "2026-10-13"
---

# Phase 4 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Maven Central → build | Koin (first new runtime dependency) and Robolectric (test-only) enter the build | Third-party code |
| Kotlin/Native umbrella → Swift | Public Kotlin API of exported modules becomes Swift-visible | API surface |
| Process start → DI graph | Koin start runs before the first frame (Android) / in App init (iOS) | Start-up order, consent |
| Device storage → DI graph | Bookmarks, agenda filters and data-collection consent read via Koin-owned SharedPreferences | User preferences, consent (sensitive) |
| Test code → production graph | Fakes must never load in production `initKoin` | Test doubles |
| Coexistence (Hilt ↔ Koin) | Unmigrated Hilt consumers saw the same singletons as Koin consumers (transitional, now removed) | Stateful singletons |
| Contributor code → main | DI gate keeps service-locator calls and Hilt from re-entering | Source code |
| Navigation argument → ViewModel | Speaker/session id from the back stack becomes a Koin parameter | Ids |
| App BuildConfig → feature | OpenFeedback flag and project id cross into `:feature:session-detail` | Config (non-secret subset) |
| Firebase Remote Config → UI | Remote flags choose the feedback form | Remote flags |
| Activity lifecycle → DI | `ExternalContentService` holds an Activity reference | Activity reference |
| Build output → users | R8-minified release APK | Release binary |
| App → legal notice | `licenses.html` third-party attribution | Attribution |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-04-SC | Tampering | io.insert-koin artifacts | high | mitigate | Single `koin = "4.2.2"` ref, official `io.insert-koin` group (`gradle/libs.versions.toml`); repositories limited to google/mavenCentral/gradlePluginPortal (`settings.gradle.dcl`) | closed |
| T-04-01 | Information Disclosure | shared.h Swift API surface | medium | mitigate | `@HiddenFromObjC` on Koin entry points (5 sites); built `shared.h` has 0 Koin names | closed |
| T-04-02 | Denial of Service | Koin start-up | medium | mitigate | `initKoin` (line 30) before `super.onCreate()` (line 32) in `DevFestNantesApplication.kt`; `KoinStartupTest`, `ProductionGraphStartupTest`, `IosKoinTest`; UAT tests 1–3 passed | closed |
| T-04-03 | Tampering | Duplicate bindings | medium | mitigate | `allowOverride(false)` in `shared/.../InitKoin.kt:28`; `KoinGraphTest` asserts `DefinitionOverrideException` | closed |
| T-04-04 | Information Disclosure | Koin definition logging | low | mitigate | No Koin logger (`androidLogger`/`printLogger`) configured anywhere | closed |
| T-04-05 | Information Disclosure | SharedPreferences binding | high | mitigate | `single<SharedPreferences> { PreferenceManager.getDefaultSharedPreferences(androidApplication()) }` in `DataPlatformModule.android.kt:15` (same file as before) | closed |
| T-04-06 | Elevation of Privilege | `:core:testing` fakes | medium | mitigate | `:core:testing` only via `testImplementation` (`core/ui`, `androidApp`); overrides require `allowOverride(true)`, rejected in production (`TestDoublesModuleTest`) | closed |
| T-04-07 | Tampering | Duplicate singletons during coexistence | medium | mitigate | Coexistence bridges removed with Hilt in 04-07; no Dagger/Hilt references remain in sources or catalog | closed |
| T-04-08 | Information Disclosure | shared.h after core changes | low | mitigate | Same as T-04-01: header Koin count 0 | closed |
| T-04-09 | Elevation of Privilege | Service-locator escape hatches | medium | mitigate | `scripts/di-gate.sh` locator rule run in CI (`android.yml:29`); `di-gate.sh selftest` exit 0 | closed |
| T-04-10 | Information Disclosure | Consent service duplication / key change | high | mitigate | Single `SharedPreferences`/consent instance; consent dialog and toggle persistence confirmed in UAT test 1 | closed |
| T-04-11 | Tampering | Gate allowlist growth | low | mitigate | `scripts/di-gate-hilt-allowlist.txt` deleted; gate runs strict | closed |
| T-04-12 | Tampering | SpeakerViewModel parameter binding | medium | mitigate | Positional lambda `viewModel { (speakerId: String) -> … }` (`SpeakersModule.kt:16`); `parametersOf` test in `SpeakersModuleTest` | closed |
| T-04-13 | Information Disclosure | SessionFiltersService duplication | medium | mitigate | Single Koin instance; filter persistence observed in UAT test 1 | closed |
| T-04-14 | Denial of Service | Agenda start destination crash | medium | mitigate | Android CI green on `62825aa`; UAT test 1 (no crash at launch) | closed |
| T-04-15 | Information Disclosure | OpenFeedback secrets | medium | mitigate | 0 `OPEN_FEEDBACK` references under `feature/` | closed |
| T-04-16 | Tampering | Remote Config fetch interval | low | mitigate | `setConfigSettingsAsync` with `REMOTE_CONFIG_MINIMUM_FETCH_INTERVAL = 300L` (`SessionDetailModule.kt:13,27-29`) | closed |
| T-04-17 | Tampering | SessionViewModel parameter binding | medium | mitigate | Positional lambda `viewModel { (sessionId: String) -> … }` (`SessionDetailModule.kt:19`); `parametersOf` test in `SessionDetailModuleTest` | closed |
| T-04-18 | Tampering | Bookmark state divergence | medium | mitigate | One `single { BookmarksStoreImpl(get()) } bind BookmarksStore::class` (`DataPlatformModule.android.kt:16`) | closed |
| T-04-19 | Information Disclosure | ApplicationInitializer order (consent bypass) | high | mitigate | Ordered `single(named("applicationInitializers"))` list (`AppModule.kt:45`); `KoinStartupTest` asserts exact order | closed |
| T-04-20 | Tampering | New test dependency (Robolectric) | medium | mitigate | `robolectric = "4.17"` used only as `testImplementation` (`androidApp/build.gradle.kts:99`) | closed |
| T-04-21 | Denial of Service | Koin-only Android start-up | medium | mitigate | Same as T-04-02; `KoinStartupTest`; CI green | closed |
| T-04-22 | Information Disclosure | Activity reference retained by DI | low | accept | `factory { (activity: Activity) -> ExternalContentService(activity) }` (`AppModule.kt:54`), injected per Activity — see Accepted Risks Log | closed |
| T-04-23 | Denial of Service | R8-minified release with Koin | medium | mitigate | Release APK launch confirmed by a human (UAT test 2) | closed |
| T-04-24 | Tampering | Stale Hilt/KSP generated sources | low | mitigate | Clean full suite in 04-07; CI uses fresh checkouts | closed |
| T-04-25 | Elevation of Privilege | Re-introduction of Hilt / locator calls | medium | mitigate | Allowlist deleted; strict gate + selftest in CI | closed |
| T-04-26 | Repudiation | Third-party attribution | low | mitigate | `androidApp/src/main/assets/licenses.html` lists Koin, no Dagger/Hilt | closed |

*Status: open · closed · open — below high threshold (non-blocking)*
*Severity: critical > high > medium > low — only open threats at or above workflow.security_block_on count toward threats_open*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-04-01 | T-04-22 | `factory` binding: Koin keeps no reference to factory instances; the service lives as long as the injecting Activity, same as the previous Hilt activity scope | Phase 4 plan (04-06) | 2026-10-13 |

*Accepted risks do not resurface in future audit runs.*

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-10-13 | 27 | 27 | 0 | /gsd-secure-phase (L1 grep verification, orchestrator) |

## Security Audit 2026-10-13
| Metric | Count |
|--------|-------|
| Threats found | 27 |
| Closed | 27 |
| Open | 0 |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-10-13
