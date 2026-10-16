---
phase: "03"
slug: "multi-module-architecture-extraction"
status: verified
# threats_open = count of OPEN threats at or above workflow.security_block_on severity (the blocking gate)
threats_open: 0
asvs_level: 1
block_on: high
created: "2026-09-27"
---

# Phase 03 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Kotlin/Native umbrella → Swift (`shared.framework`) | Only `:core:model`, `:core:data`, `:core:analytics` are `export()`-ed (D-11); `:core:network` (Apollo) stays internal | Public Swift API surface; Apollo/GraphQL types must not leak |
| `:androidApp` shell → `:feature:*` modules | Features receive callbacks and plain values only (D-07); app BuildConfig and OpenFeedback secrets stay in the app (D-02) | `OpenFeedbackConfig(enabled, projectId)`, `versionName`/`versionCode`; URLs opened via app-owned `ExternalContentService` |
| On-device persistence | SharedPreferences for consent, bookmarks, agenda filters — moved into consumer modules (D-03) | User consent state (high), bookmarks, filters — keys must stay byte-identical |
| Build supply chain | `build-logic` included build replaces `buildSrc`; version catalog is the single source of versions | Gradle plugin artifacts, dependency resolution rules, merged manifest |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-03-01 | Tampering | build-logic/convention | low | accept | In-repo, replaces buildSrc; compileOnly plugin deps on existing catalog refs; same repositories as root settings | closed |
| T-03-02 | Information Disclosure | shared export list | medium | mitigate | `shared/build.gradle.kts:10-12` exports only core:model/data/analytics; no transitiveExport; swift-names-gate empty diff | closed |
| T-03-03 | Denial of Service | ios.yml cache keys | low | mitigate | `ios.yml:40,76` keys include core/** and build-logic/** | closed |
| T-03-SC | Tampering | libs.versions.toml compileOnly entries | low | mitigate | `:71,84,94` reuse agp/kotlin/detekt refs of existing plugin aliases | closed |
| T-03-04 | Information Disclosure | :core:network in :shared | medium | mitigate | 0 core:network refs in shared build file; 0 Apollo/GraphQL/Network* names in shared.h | closed |
| T-03-05 | Tampering | analytics event/trace names | low | mitigate | Analytics sources identical to baseline modulo package/imports; main-source string-literal multiset identical (except 2 @Preview values) | closed |
| T-03-06 | Elevation of Privilege | Hilt graph | low | mitigate | AppModule change limited to imports + planned `openFeedbackConfig()`; `:androidApp:assembleRelease` green | closed |
| T-03-07 | Tampering | BookmarksStoreImpl prefs key | medium | mitigate | `"selected_sessions"` unchanged (BookmarksStoreImpl.kt:61); file identical modulo package/imports | closed |
| T-03-08 | Information Disclosure | :core:network via :core:data | medium | mitigate | `implementation` dep; Apollo-typed extensions and GraphQLStore `internal` | closed |
| T-03-09 | Denial of Service | KMP-NativeCoroutines wrappers | medium | mitigate | Plugin + `@NativeCoroutines` on :core:data; member set unchanged; iOS CI green; D-12 #2 approved | closed |
| T-03-10 | Tampering | DependencyResolutionRules.kt | medium | mitigate | Rules verbatim; release external module set identical to baseline incl. firebase-auth substitution | closed |
| T-03-11 | Elevation of Privilege | merged AndroidManifest | medium | mitigate | Merged release manifest canonically identical (9 permissions, 6 exported) | closed |
| T-03-12 | Information Disclosure | :core:testing in release | low | mitigate | Only `testImplementation`; absent from releaseRuntimeClasspath | closed |
| T-03-13 | Tampering | string resources incl. consent/legal | medium | mitigate | `resources-gate.sh` RESOURCES-OK at HEAD; drawables byte-identical | closed |
| T-03-14 | Repudiation | resource merge precedence | low | mitigate | No resource name defined in two modules | closed |
| T-03-15 | Spoofing | external links (About/Venue) | low | mitigate | URL callbacks route to app `ExternalContentService.openUrl`; URL literals unchanged (see Note 3) | closed |
| T-03-16 | Information Disclosure | app BuildConfig in features | medium | mitigate | No `BuildConfig` in feature/core; version passed as plain values | closed |
| T-03-17 | Tampering | DataCollectionSettingsServiceImpl prefs keys | high | mitigate | `"SHARED_PREFERENCES_KEY_ENABLED_DATA_COLLECTION_TOOLS"` unchanged; file identical modulo package/imports | closed |
| T-03-18 | Information Disclosure | consent gating | high | mitigate | DataSharingInitializer/DataCollectionViewModel identical modulo package/imports; dialog trigger unchanged (MainActivity.kt:174-176); manifest keeps `firebase_*_collection_enabled=false`; Android smoke #3 approved | closed |
| T-03-19 | Spoofing | speaker social links | low | accept | Routed to `ExternalContentService.openUrl` (MainActivity.kt:131) | closed |
| T-03-20 | Information Disclosure | OpenFeedback secrets | medium | mitigate | Only `enabled`/`projectId` cross; no OPEN_FEEDBACK under feature/core | closed |
| T-03-21 | Tampering | SessionFiltersServiceImpl prefs key | medium | mitigate | `"SHARED_PREFERENCES_KEY_AGENDA_FILTERS"` unchanged; non-polymorphic JSON | closed |
| T-03-22 | Denial of Service | assisted ViewModel factories | low | mitigate | EntryPoint stays in MainActivity; no EntryPoint/NavController in feature/core; release build green | closed |
| T-03-23 | Information Disclosure | shared export list (final) | medium | mitigate | Exactly three `export(` lines + matching `api`; no core:network | closed |
| T-03-24 | Tampering | :androidApp release classpath | medium | mitigate | External deps identical to baseline; only project lines differ | closed |
| T-03-25 | Repudiation | module graph erosion | low | mitigate | No core→feature or feature→feature edges | closed |
| T-03-10-01 | Tampering | extra.graphqls / Apollo codegen | medium | mitigate | operations/schema byte-identical; regenerated Apollo literal multiset identical (328/88) | closed |
| T-03-10-02 | Repudiation | swift-names re-baseline | medium | mitigate | 03-10 re-baseline confined to five-family regex; D-11 amendment recorded (see Note 1) | closed |
| T-03-10-03 | Spoofing | Swift type identity | high | mitigate | SWIFT-TYPE-COLLISION / SWIFT-MEMBERS-CHANGED checks; `check` + `selftest` pass at HEAD | closed |
| T-03-10-04 | Information Disclosure | swift-names exclusion list | low | mitigate | Each exclusion justified (Link → SwiftUI.Link; SpeakerDetails → SwiftUI struct) | closed |
| T-03-10-SC | Tampering | package installs (03-10) | low | accept | No dependency or build file touched | closed |
| T-03-11-01 | Elevation of Privilege | 03-01-SUMMARY status flip | medium | mitigate | Blocking gate + precondition; approval 2026-09-24 cited | closed |
| T-03-11-02 | Repudiation | 03-01-SUMMARY history | low | mitigate | Original failing evidence retained (03-01-SUMMARY.md:199-246) | closed |
| T-03-11-SC | Tampering | package installs (03-11) | low | accept | Docs-only plan | closed |

*Status: open · closed · open — below high threshold (non-blocking)*
*Severity: critical > high > medium > low — only open threats at or above workflow.security_block_on count toward threats_open*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

Verification baseline: `f42a027` (origin/main, phase fork point). Evidence re-run at HEAD `b90347e`; raw outputs kept in the session scratchpad (gate-check, gate-selftest, deps, manifest, Apollo literal diffs).

### Audit Notes (non-blocking)

1. **Second Swift-names re-baseline:** 03-02 (2b93cf0) re-baselined again (835 removals, 0 additions) despite the D-11 amendment saying "single re-baseline in 03-10". Removal-only; the one Swift-referenced name dropped (SpeakerDetails) is excluded with evidence. Wording inaccuracy, nothing hidden.
2. **Gates are manual:** `swift-names-gate.sh` and `resources-gate.sh` live in `.planning` and do not run in CI; after this phase, identity-swap detection relies on iOS compile failures unless they are wired into CI (candidate for CICD-V2-01).
3. **Maps intent in a feature:** `feature/venue/.../utils/NavigationUtils.kt:11` launches an `ACTION_VIEW` maps intent directly (pre-existing, unchanged modulo package/imports) — T-03-15 wording slightly inaccurate, no regression.
4. **core-ktx catalog entry:** `androidx-core-ktx` (1.16.0) added in 03-03 as `implementation`; outside T-03-SC's compileOnly scope, but the resolved release classpath is unchanged.
5. **iOS cache key omits `*.graphqls`:** pre-existing in baseline, not a regression.

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-03-01 | T-03-01 | In-repo convention plugins, same trust level as the buildSrc they replace; no external plugin | Plan threat model (03-01), verified by gsd-security-auditor | 2026-09-27 |
| AR-03-02 | T-03-19 | Speaker social links open via unchanged app-owned ExternalContentService | Plan threat model (03-07), verified by gsd-security-auditor | 2026-09-27 |
| AR-03-03 | T-03-10-SC | No new dependency; @targetName ships in already-pinned Apollo 5.2.0 | Plan threat model (03-10), verified by gsd-security-auditor | 2026-09-27 |
| AR-03-04 | T-03-11-SC | Documentation-only plan, no dependency change | Plan threat model (03-11), verified by gsd-security-auditor | 2026-09-27 |

*Accepted risks do not resurface in future audit runs.*

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-09-27 | 34 | 34 | 0 | gsd-security-auditor (ASVS L1, block_on high) via /gsd-secure-phase 03 |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-09-27
