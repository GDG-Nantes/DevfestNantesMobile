---
phase: "02"
slug: dependency-build-tooling-upgrade
status: verified
threats_open: 0
asvs_level: 1
created: "2026-09-19"
---

# Phase 02 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Dev/CI → Maven Central, Google Maven, Gradle Plugin Portal | Artifacts and Gradle plugins fetched and executed at configuration time | Build artifacts (untrusted bytes) |
| `gradle/libs.versions.toml` → build graph | Only place plugin ids and coordinates are declared | Coordinates |
| Dev/CI → `services.gradle.org` | Gradle distribution executed as the build tool | Distribution zip |
| Network → on-device Apollo SQLite cache | GraphQL responses normalized and persisted | Public conference data |
| Firebase services → app runtime | Remote Config, Crashlytics, Performance | Telemetry / config |
| Backend date strings → schedule parsing | Externally supplied ISO dates | Untrusted strings |
| `settings.gradle.dcl` → build graph | Repository list for all resolution | Repository config |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-02-01 | Elevation of Privilege | libs.versions.toml plugins/versions | high | mitigate | Only version scalars changed; no plugin id/group edits (catalog :113-128) | closed |
| T-02-02 | Tampering | KSP version string | medium | mitigate | Cross-checked against Maven Central plugin marker (02-01-SUMMARY:141) | closed |
| T-02-03 | Spoofing | Kotlin/KSP group typosquat | low | accept | No group authored; official coordinates present | closed |
| T-02-04 | Tampering | Gradle wrapper distribution | high | mitigate | distributionSha256Sum pinned, services.gradle.org host, validateDistributionUrl=true; checksum independently matched | closed |
| T-02-05 | Elevation of Privilege | AGP library plugin / Detekt rename | high | mitigate | New ids verified first-party; legacy arturbosch coordinate fully gone | closed |
| T-02-06 | Tampering | dev.detekt 2.0.0-alpha pin | medium | mitigate | blocking-human checkpoint ran before pin; 11 real findings prove analyser active | closed |
| T-02-07 | Tampering | R8 strict keep rules | medium | mitigate | No `-keep` added; only 3 `-dontwarn kotlinx.datetime.*` after assembleRelease broke | closed |
| T-02-08 | Information Disclosure | secrets/google-services config | medium | accept | androidApp defaultConfig/secrets/google-services untouched | closed |
| T-02-09 | Spoofing | com.apollographql.cache group | high | mitigate | Group appears exactly twice; verified vs Maven Central and official org | closed |
| T-02-10 | Elevation of Privilege | Apollo cache compiler plugin | high | mitigate | Same audited group and version ref (shared/build.gradle.kts:77); codegen re-run under clean | closed |
| T-02-11 | Tampering | Cache-key strategy | medium | mitigate | Apollo.kt generator/resolver unchanged; offline read passed UAT test 4 | closed |
| T-02-12 | Information Disclosure | apollo.db format change | low | accept | Public data only; db name kept; bookmarks intact | closed |
| T-02-13 | Denial of Service | Compose BOM transitive closure | low | accept | Trusted BOM, no group change; release build run; instrumented tests not run (covered by UAT 3/4) | closed |
| T-02-14 | Tampering | kotlinx-datetime parsing semantics | high | mitigate | ScheduleSlotDateParsingTest landed pre-bump with epoch-millis assertions | closed |
| T-02-15 | Denial of Service | iOS/Native klib breakage | medium | mitigate | Three iOS targets compiled explicitly; ios.yml green | closed |
| T-02-16 | Elevation of Privilege | Firebase Gradle plugins | medium | mitigate | Plugin ids untouched; version-only bumps | closed |
| T-02-17 | Information Disclosure | Firebase telemetry defaults | medium | accept | Manifest and consent flow untouched (see caveats) | closed |
| T-02-18 | Tampering | Pre-release serialization | medium | mitigate | kotlinxSerialization = 1.11.0 stable | closed |
| T-02-19 | Tampering | Settings repository list | high | mitigate | google/mavenCentral/gradlePluginPortal in both blocks; no other repositories blocks | closed |
| T-02-20 | Spoofing | Lost include() | high | mitigate | Exactly two includes (:androidApp, :shared) | closed |
| T-02-21 | Tampering | Duplicate settings files | medium | mitigate | Only settings.gradle.dcl tracked | closed |
| T-02-22 | Repudiation | Undocumented version deviation | medium | mitigate | STATE.md § Phase 02 version deviations lists BUILD-01..07 | closed |
| T-02-23 | Denial of Service | STATE/PROJECT overwrite | medium | mitigate | Landmarks survive; scoped edits | closed |
| T-02-SC | Tampering | npm/pip/cargo installs | low | mitigate | N/A by design; no such manifests; manual legitimacy audit in 02-RESEARCH | closed |

*Status: open · closed · open — below high threshold (non-blocking)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-02-01 | T-02-03, T-02-08, T-02-12, T-02-13, T-02-17 | Accepted at plan time; preconditions re-verified by audit | Plan (02-01..02-04) | 2026-09-19 |

---

## Audit Caveats (non-blocking)

- T-02-01: `appollo` and `kmpNativeCoroutines` also bumped (forced siblings, version-only, trusted groups).
- T-02-17: four Firebase artifact names changed (-ktx merge); same group, documented.
- T-02-13: instrumented tests not run; covered by device UAT.
- Unregistered surface (low residual): `kotlin-metadata-jvm` force and `firebase-auth-ktx` substitution with a hardcoded version literal in androidApp/build.gradle.kts; gradle-wrapper.jar replaced (not source-reviewed); minSdk 23→26; compileSdk 37/targetSdk 36 split. No SUMMARY contained a `## Threat Flags` section.

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-09-19 | 24 | 24 | 0 | gsd-security-auditor |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-09-19
