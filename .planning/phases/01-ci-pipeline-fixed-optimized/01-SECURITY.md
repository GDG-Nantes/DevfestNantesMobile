---
phase: "1"
slug: "ci-pipeline-fixed-optimized"
status: verified
# threats_open = count of OPEN threats at or above workflow.security_block_on severity (the blocking gate)
threats_open: 0
asvs_level: 1
created: "2026-09-17"
---

# Phase 1 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| GitHub-hosted runner image → workflow shell steps | Runner tool inventory (`simctl` JSON, `/Applications` listing) parsed into shell variables interpolated into an `xcodebuild` command line | Runner-local, non-secret facts |
| PR contributor → GitHub Actions runner | Branch/fork code executes on the runner; `github.event.*` is attacker-influenceable if interpolated into a `run:` block | Event context |
| PR contributor → GitHub Actions cache service | A `pull_request` job can write cache entries a later `main` run restores and builds from | Gradle/Konan build cache contents |
| Workflow → `android-setup` composite action | Cache-write policy crosses from the calling workflow into the shared composite action as an input value | `cache-read-only` boolean expression |
| Third-party Action tag → runner execution | Every `uses:` reference resolves a mutable tag at run time rather than an immutable commit | Third-party Action code |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-01-01 | Tampering | Simulator/Xcode resolution steps in `ios.yml` | medium | mitigate | Only workflow-computed `steps.*.outputs.*` values are interpolated into `run:` blocks; every shell expansion double-quoted | closed |
| T-01-02 | Elevation of Privilege | `sudo xcode-select -s` in `ios.yml` | low | accept | Pre-existing, unchanged privilege; argument derives solely from a runner-local glob, no PR-supplied content reaches it | closed |
| T-01-03 | Information Disclosure | iOS job logs | low | accept | Added steps echo only runner-public facts (simulator name, Xcode version); no secrets referenced | closed |
| T-01-SC | Tampering | npm/pip/cargo installs | high | mitigate | Zero new packages or Marketplace Actions added; only pre-existing runner tools (`jq`, `sed`, `sort`, `xcrun`) used | closed |
| T-01-04 | Tampering | Gradle cache writes from `pull_request` runs via `build-debug` in `android.yml` | high | mitigate | All four `android-setup` call sites set `cache-read-only` to `${{ github.event_name == 'pull_request' }}`; enforced by Task 1's four-occurrence gate | closed |
| T-01-05 | Denial of Service | `concurrency` group in `android.yml` | low | accept | `cancel-in-progress: true` is the intended cost-saving behaviour (D-07), self-healing on next push; confirmed via UAT test 2 as an accepted judgment call | closed |
| T-01-09 | Tampering | `.github/actions/android-setup/action.yml` (shared by all Android jobs and, after Plan 03, the iOS job) | medium | mitigate | Content-based gate fails if the action references `github.event_name` or stops forwarding `inputs.cache-read-only`, preventing silent relocation of the branch-aware expression | closed |
| T-01-06 | Tampering | Gradle cache writes from the iOS job in `ios.yml` | high | mitigate | Single `android-setup` call site sets `cache-read-only` to `${{ github.event_name == 'pull_request' }}`, replacing the removed inline permanent read-only pin | closed |
| T-01-08 | Repudiation | `continue-on-error: true` on `setup-gradle` inside `android-setup/action.yml` | medium | mitigate | Live-run gate requires a `BUILD SUCCEEDED` banner in the log in addition to `completed:success`, so job conclusion alone is never trusted as provisioning evidence | closed |
| T-01-07 | Spoofing | Tag-pinned Actions (`actions/checkout@v4`, `actions/setup-java@v4`, `gradle/actions/setup-gradle@v4`, `actions/cache@v4`, `reactivecircus/android-emulator-runner@v2`) | medium | accept | Pre-existing, repository-wide condition unchanged by this phase; SHA-pinning is outside this phase's locked decisions | closed |

*Status: open · closed · open — below `high` threshold (non-blocking)*
*Severity: critical > high > medium > low — only open threats at or above `workflow.security_block_on` (`high`) count toward `threats_open`*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

All `high`-severity threats (T-01-SC, T-01-04, T-01-06) carry disposition `mitigate` and are closed,
satisfying the configured `security_block_on: high` threshold. `threats_open: 0`.

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-01-01 | T-01-02 | `sudo xcode-select -s` runs on an ephemeral runner with an argument derived solely from a runner-local glob; no PR-supplied content reaches `/Applications` before this step | Phase 1 plan (01-01-PLAN.md) | 2026-09-17 |
| AR-01-02 | T-01-03 | Added steps echo only runner-public facts; no repository secret is referenced | Phase 1 plan (01-01-PLAN.md) | 2026-09-17 |
| AR-01-03 | T-01-05 | `cancel-in-progress: true` is the intended cost-saving behaviour (D-07); self-healing, CI-throughput-only impact; confirmed as accepted via UAT test 2 | Robin Caroff (UAT phase 01, test 2: "pass") | 2026-09-17 |
| AR-01-04 | T-01-07 | Tag-pinning (not SHA-pinning) is a pre-existing, repository-wide condition outside this phase's locked decisions | Phase 1 plan (01-03-PLAN.md) | 2026-09-17 |

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-09-17 | 10 | 10 | 0 | /gsd-secure-phase (register authored at plan time, ASVS L1 short-circuit — no deep auditor pass required) |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-09-17
