---
phase: "9"
slug: "ios-on-shared-logic-and-legacy-persistence"
status: verified
# threats_open = count of OPEN threats at or above workflow.security_block_on severity (the blocking gate)
threats_open: 0
asvs_level: 1
created: "2026-11-07"
---

# Phase 9 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Kotlin shared layer -> Swift | `IosDependencies` accessors and `KotlinStreamBridge` hand use cases and flows to the app | Public conference data, bookmark ids, filter values (non-sensitive) |
| Persisted values -> Kotlin stores | NSUserDefaults values are untrusted input, parsed only by the Kotlin stores | Session ids and filter names (non-sensitive) |
| v1.0 installed data -> updated app | Legacy `Favorites` and `AgendaSessionFilters` keys on an updating device | Session ids and filter names; intentionally not read (07 D-15) |
| Developer machine -> repository / CI | Screenshots, flows and evidence committed under `.planning`; CI uses a mock `GoogleService-Info.plist` | Public conference data; secrets stay gitignored |

---

## Threat Register

Built from the `<threat_model>` blocks of plans 09-01 to 09-16 (authored at plan time). Duplicate `T-09-SC` rows are merged.

| Threat ID | Category | Component | Severity | Disposition | Mitigation / Evidence | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-09-01 | Elevation of Privilege | `IosDependencies` accessors | low | mitigate | Accessors return plain interfaces; `DI-GATE-OK` | closed |
| T-09-02 | Tampering | Spike writes on the test host's defaults | low | mitigate | Spike tests restore values in `defer` and use `spike-` values (KotlinContinuousFlowTests) | closed |
| T-09-SC | Tampering | Package and tool installs (all plans) | low | accept | No package added or changed in any plan; Maestro 1.35.0 was already installed | closed (accepted) |
| T-09-03 | Repudiation | CI replay claim | medium | mitigate | 09-02: run 42289484240, head SHA matched, `success`, `XCRESULT-OK` total=59 | closed |
| T-09-04 | Information disclosure | Pushed branch | low | accept | Only committed sources are pushed; the real `GoogleService-Info.plist` stays gitignored and CI creates its own mock | closed (accepted) |
| T-09-05 | Tampering | Characterization goldens | medium | mitigate | `core/domain/testdata` unchanged against the merge-base | closed |
| T-09-06 | Denial of service | Speaker name with an unexpected first character | low | accept | `SpeakerIndexUseCase` handles empty names with an empty letter (documented in `documentation/ARCHITECTURE.md`); no such value exists in the data | closed (accepted) |
| T-09-28 | Tampering | Device locale code passed to `VenueLanguageUseCase` | low | accept | Local input, read as today; the use case maps any value, nil and empty included, to French or English | closed (accepted) |
| T-09-08 | Denial of service | A session with an unknown type | low | accept | The use case treats it as neither talk nor navigable, the v1.0 behavior of both Swift copies | closed (accepted) |
| T-09-07 | Tampering | Classification parity | medium | mitigate | Shared classification table asserted in SpeakerDetailsViewModelTests and AgendaCharacterizationTests | closed |
| T-09-29 | Tampering | Device clock driving the initial scroll | low | accept | The clock only picks a scroll position, read once in the view; nothing is written, a wrong clock scrolls to the wrong slot as in v1.0, and the selection is a pure function tested with an injected now | closed (accepted) |
| T-09-09 | Tampering | Legacy persistence during the refactor | medium | mitigate | Legacy-format tests stayed green through 09-08 (post-merge gate 71/71) | closed |
| T-09-10 | Repudiation | Bookmark analytics value | low | mitigate | `toggle_bookmark_returns_the_post_toggle_value` test | closed |
| T-09-11 | Denial of service (user data) | Silent loss of v1.0 favorites and filters on update | high | mitigate | Human chose option-proceed at the 09-09 checkpoint; standalone revertible switch commit 586eb4b0; in-place update UAT approved (09-15) | closed |
| T-09-12 | Tampering | Corrupt or hostile persisted values | medium | mitigate | Kotlin stores decode with `decodeOrNull` (SessionFilterCodec, UserDefaultsSessionFiltersStore); Swift never parses persisted values | closed |
| T-09-13 | Repudiation | Mirror drift between UI and storage | medium | mitigate | Write-through, one-shot seed and failed-seed store fallback tests in AgendaViewModelTests (WR-01 fix 61bb6b28); UAT kill-and-relaunch kept writes | closed |
| T-09-14 | Information disclosure | Persisted payloads in logs | low | mitigate | Seed failure log carries the error description only, no ids or filters (AgendaViewModel.swift:104) | closed |
| T-09-15 | Information disclosure | Legacy keys left on disk | low | accept | They hold session ids and filter names, no secret; removal is a deferred cleanup (CONTEXT Deferred Ideas) | closed (accepted) |
| T-09-17 | Tampering | Data race between native callbacks and the ViewModel | medium | mitigate | `SWIFT-OBS-GATE-OK`; no unstructured `Task` in app sources; spike proved the bridge | closed |
| T-09-18 | Information disclosure | Agenda logging | low | mitigate | Agenda logs carry counts and error descriptions only (AgendaViewModel.swift:134,149,168) | closed |
| T-09-19 | Denial of service | Repeated emissions on rapid taps | low | accept | The `.task` cancels on disappear, the flow runs on the domain dispatcher and each emission is a small mapping; the conference data is under 100 sessions | closed (accepted) |
| T-09-16 | Denial of service | A failing or stalled agenda stream | medium | mitigate | Lifetime and failure tests in AgendaViewModelTests (09-11) | closed |
| T-09-20 | Tampering | Golden files | high | mitigate | `core/domain/testdata` unchanged against the merge-base; goldens asserted, never overwritten | closed |
| T-09-21 | Tampering | Baseline regenerated to silence a check | medium | mitigate | Baselines regenerated only with their Swift call sites; `SWIFT-NAMES-OK`; test floor held (72) | closed |
| T-09-22 | Repudiation | Undocumented behavior change | low | mitigate | Deviation rows in ARCHITECTURE.md `Behavior deviations (v1.1)`; stated at the D-10 checkpoint | closed |
| T-09-23 | Repudiation | IOSDATA-02 claim | high | mitigate | Human approved committed evidence (09-15); legacy values byte-identical before and after install | closed |
| T-09-24 | Tampering | A flow that resets state | high | mitigate | `update-verify-flow.yaml` has no state reset (0 `clearState`) | closed |
| T-09-25 | Information disclosure | Committed evidence and temporary worktree | low | mitigate | No secret tracked; baseline worktree outside the repository, removed in 09-16 | closed |
| T-09-26 | Repudiation | iOS parity claim | high | mitigate | Human compared 31 pairs and classified every difference (09-16); new deviation recorded in 09-CONTEXT and ARCHITECTURE.md | closed |
| T-09-27 | Information disclosure | Committed screenshots, temporary worktree | low | mitigate | No secret tracked; parity simulators deleted; baseline worktree removed | closed |

*Status: open · closed · open — below high threshold (non-blocking)*
*Severity: critical > high > medium > low — only open threats at or above workflow.security_block_on count toward threats_open*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-01 | T-09-SC | No package added or changed in any plan; Maestro 1.35.0 was already installed | plan threat model (user-approved plans) | 2026-11-05 |
| AR-02 | T-09-04 | Only committed sources are pushed; the real `GoogleService-Info.plist` stays gitignored and CI creates its own mock | plan threat model (user-approved plans) | 2026-11-05 |
| AR-03 | T-09-06 | `SpeakerIndexUseCase` handles empty names with an empty letter (documented in `documentation/ARCHITECTURE.md`); no such value exists in the data | plan threat model (user-approved plans) | 2026-11-05 |
| AR-04 | T-09-28 | Local input, read as today; the use case maps any value, nil and empty included, to French or English | plan threat model (user-approved plans) | 2026-11-05 |
| AR-05 | T-09-08 | The use case treats it as neither talk nor navigable, the v1.0 behavior of both Swift copies | plan threat model (user-approved plans) | 2026-11-05 |
| AR-06 | T-09-29 | The clock only picks a scroll position, read once in the view; nothing is written, a wrong clock scrolls to the wrong slot as in v1.0, and the selection is a pure function tested with an injected now | plan threat model (user-approved plans) | 2026-11-05 |
| AR-07 | T-09-15 | They hold session ids and filter names, no secret; removal is a deferred cleanup (CONTEXT Deferred Ideas) | plan threat model (user-approved plans) | 2026-11-05 |
| AR-08 | T-09-19 | The `.task` cancels on disappear, the flow runs on the domain dispatcher and each emission is a small mapping; the conference data is under 100 sessions | plan threat model (user-approved plans) | 2026-11-05 |

*Accepted risks do not resurface in future audit runs.*

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-11-07 | 30 | 30 | 0 | secure-phase (orchestrator, ASVS L1 grep-depth; no auditor needed: register authored at plan time, no open threats) |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-11-07

## Security Audit 2026-11-08

| Metric | Count |
|---|---|
| Threats found | 30 |
| Closed | 30 |
| Open | 0 |
