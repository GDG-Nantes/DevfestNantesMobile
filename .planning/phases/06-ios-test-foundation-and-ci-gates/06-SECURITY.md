---
phase: "06"
slug: "ios-test-foundation-and-ci-gates"
status: verified
# threats_open = count of OPEN threats at or above workflow.security_block_on severity (the blocking gate)
threats_open: 0
asvs_level: 1
created: "2026-10-27"
---

# Phase 06 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| XCTest runner -> host app process | The test runner launches the production app binary with injected environment; the app decides at launch whether it is under test | Process environment (`XCTestConfigurationFilePath`) |
| Developer machine -> Firebase project | A locally hosted test run would use the real, gitignored `GoogleService-Info.plist` | Analytics, Crashlytics, Remote Config traffic |
| v1.0 user data -> later app versions | `AgendaSessionFilters` JSON persisted in `UserDefaults` by v1.0 must stay readable | User filter preferences (low sensitivity) |
| Test bundle -> host app code | Tests call internal app code through `@testable import` | None at runtime |
| PR author -> CI runner | PR-controlled code (scripts, Swift, workflow, baselines) executes on the macOS runner | Source code, job token |
| Workflow -> actions | `actions/cache`, `actions/upload-artifact`, setup actions run with the job token | `GITHUB_TOKEN` (read-only) |
| CI -> artifact storage | Result bundles leave the runner as downloadable artifacts | Test logs (no secrets) |
| SwiftPM -> GitHub (rickclephas/KMP-NativeCoroutines) | Swift half of the coroutine bridge fetched from a third-party repository at a tag | Third-party code shipped in the app |
| Dependabot / PR author -> CI | Version bumps arrive as PRs that touch only one side | Dependency versions |
| Local workstation -> repository | Smoke screenshots committed under `.planning` | Public conference data |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-06-01 | Tampering / Elevation of privilege | `iOSApp.isRunningTests` guard | medium | mitigate | `iosApp/iosApp/iOSApp.swift:20-24`: keyed only on the runner-injected `XCTestConfigurationFilePath` env var, and since WR-01 (`43c0456`) compiled to a constant `false` outside `#if DEBUG`, so a Release build cannot take the test path. No launch argument, user default or URL scheme reaches it | closed |
| T-06-02 | Information disclosure | Hosted test runs on a developer machine | low | mitigate | `iOSApp.swift:29`: `if Self.isRunningTests { return }` precedes `FirebaseApp.configure()` (line 32) | closed |
| T-06-03 | Repudiation | `project.pbxproj` version fields | medium | mitigate | Version-line multiset unchanged by the phase: `MARKETING_VERSION = 2025.10.00` x2, `1.0` x2 (test target), `CURRENT_PROJECT_VERSION = 37` x4 | closed |
| T-06-04 | Tampering | `ios.yml` `run:` steps (shell injection) | medium | mitigate | No `github.event.*` in any `run:`; the only `${{ }}` inside `run:` is `github.workspace` (runner-controlled). Step outputs reach shells through `env:` (`SIM_UDID`, `XCODE_VERSION`, `SWIFT_GATE_HEADER`) | closed |
| T-06-05 | Elevation of privilege | `GITHUB_TOKEN` | medium | mitigate | `ios.yml:14-15` top-level `permissions: contents: read`; no `secrets.*` referenced | closed |
| T-06-06 | Information disclosure | Uploaded `.xcresult` artifact | low | mitigate | `ios.yml:297` `if: failure()`, `ios.yml:303` `retention-days: 7`; built with the CI mock plist | closed |
| T-06-07 | Tampering | DerivedData cache across Xcode versions | medium | mitigate | `ios.yml:103-106`: Xcode version in the key and in every restore-key prefix; cache step (line 97) after Xcode selection (line 80) | closed |
| T-06-08 | Repudiation (false green) | `xcodebuild` exits 0 with zero executed tests | high | mitigate | `scripts/xcresult-assert-tests.sh` fails on total < 1, passed < 1, failed > 0 and (WR-02, `fcef4d3`/`4195159`) a non-accepted overall result; no env override. CI runs its selftest first (`ios.yml:278-279`), `-parallel-testing-enabled NO`, no retries | closed |
| T-06-09 | Tampering / Repudiation | `scripts/swift-names/*.txt` baselines | medium | mitigate | CI runs only `check` and `selftest` (`ios.yml:158-159`), never `baseline`; re-baseline rule in `AGENTS.md` and `scripts/README.md` | closed |
| T-06-10 | Tampering | `swift-names-gate.sh` selftest writes | low | mitigate | `set -euo pipefail` (line 47), `mktemp -d` with EXIT trap (lines 71-72), no `eval` | closed |
| T-06-11 | Repudiation (false green) | Gate reading a stale or empty header | medium | mitigate | `ios.yml:156` points `SWIFT_GATE_HEADER` at the header linked by "Build shared framework"; missing/empty headers fail (06-04 SUMMARY) | closed |
| T-06-12 | Tampering | KMP-NativeCoroutines SPM requirement drift | high | mitigate | `project.pbxproj`: `kind = exactVersion; version = "1.0.6"`, matches `libs.versions.toml`; `kmp-nc-version-gate.sh` (`KMPNC-VERSION-OK 1.0.6`) is the first CI step (`ios.yml:32-35`) | closed |
| T-06-13 | Tampering | Gate bypass through `KMP_NC_TOML` / `KMP_NC_PBXPROJ` | low | mitigate | `ios.yml` sets neither variable | closed |
| T-06-14 | Tampering | Dependabot Gradle-only bump with skew | medium | mitigate | Gate fails the PR; manual pin step documented in `documentation/DEVELOPMENT.md` ("Bumping KMP-NativeCoroutines") and `AGENTS.md` | closed |
| T-06-15 | Tampering / Repudiation | User-visible behavior after the 1.0.6 bump | high | mitigate | Blocking human smoke recorded in `06-06-SUMMARY.md` (eight steps, screenshots in `06-smoke/`, human reply "approved") | closed |
| T-06-16 | Information disclosure | Committed smoke screenshots | low | accept | See AR-06-01 | closed |
| T-06-17 | Tampering | `AgendaViewModel.parseDate` extraction | medium | mitigate | `AgendaViewModel.swift:160-162` keeps the default-option `ISO8601DateFormatter` and `?? Date()` fallback; `AgendaDateParsingTests.swift` (5 tests) pins offset, UTC and fallback behavior | closed |
| T-06-18 | Tampering / Repudiation | Legacy `AgendaSessionFilters` persistence format | medium | mitigate | `SessionFilterTests.swift` (6 tests) decodes literal legacy payloads; `SessionFilter.swift` unchanged against `main` | closed |
| T-06-SC-01 | Tampering | Supply chain (06-01) | low | accept | See AR-06-02 | closed |
| T-06-SC-02 | Tampering | Supply chain (06-02) | low | accept | See AR-06-02 | closed |
| T-06-SC-03 | Tampering | New `actions/upload-artifact@v4` reference | medium | accept | See AR-06-03 | closed |
| T-06-SC-04 | Tampering | Supply chain (06-04) | low | accept | See AR-06-02 | closed |
| T-06-SC-05 | Tampering | KMP-NativeCoroutines SPM bump to 1.0.6 | high | mitigate | Same repository already in use, tag resolved to revision `54a45fbb15bc0bebc54081f87683830d3bc9e84e` (06-RESEARCH), `exactVersion` pin enforced by T-06-12, behavior parity by T-06-15 | closed |
| T-06-SC-06 | Tampering | Supply chain (06-06) | low | accept | See AR-06-02 | closed |

*Status: open · closed · open — below high threshold (non-blocking)*
*Severity: critical > high > medium > low — only open threats at or above workflow.security_block_on count toward threats_open*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

*Each plan's threat model used the id `T-06-SC` for its supply-chain row; they are numbered `T-06-SC-0N` here by plan so every row keeps a unique id.*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-06-01 | T-06-16 | Screenshots show public conference data only (sessions, speakers, venue, partners); the real `GoogleService-Info.plist` is gitignored (`.gitignore:109,118`) and not tracked | Plan 06-06 threat model | 2026-10-27 |
| AR-06-02 | T-06-SC-01, -02, -04, -06 | These plans add or bump no package, SPM dependency or GitHub Action; Swift Testing ships with the Xcode toolchain | Plans 06-01, 06-02, 06-04, 06-06 threat models | 2026-10-27 |
| AR-06-03 | T-06-SC-03 | GitHub-owned first-party action, same major-tag pinning as `actions/checkout@v4` and `actions/cache@v4` in the same workflow; blast radius limited by T-06-05 (read-only token, no secrets) | Plan 06-03 threat model | 2026-10-27 |

*Accepted risks do not resurface in future audit runs.*

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-10-27 | 24 | 24 | 0 | /gsd-secure-phase (orchestrator, ASVS L1 grep verification; plan-time register, auditor short-circuited) |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-10-27
