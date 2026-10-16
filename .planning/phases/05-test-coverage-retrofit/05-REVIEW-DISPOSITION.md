---
phase: 05
review: 05-REVIEW.md
titles: json
findings:
  - id: WR-01
    severity: warning
    disposition: fixed
    title: "`KoinStartupTest` performs a real Firebase Remote Config fetch"
  - id: WR-02
    severity: warning
    disposition: fixed
    title: "Firebase singletons are shared across Robolectric tests and are never reset"
  - id: WR-03
    severity: warning
    disposition: fixed
    title: "Two pinned production defects are not recorded in `deferred-items.md`"
  - id: IN-01
    severity: info
    disposition: open
    title: "Stale and redundant text in the characterization tests"
  - id: IN-02
    severity: info
    disposition: open
    title: "Characterization tests are blind to a non-Paris device time zone"
  - id: IN-03
    severity: info
    disposition: open
    title: "Live-network canary tests assert nothing"
  - id: IN-04
    severity: info
    disposition: open
    title: "Mock server tests depend on the exact number of requests the store makes"
  - id: IN-05
    severity: info
    disposition: open
    title: "`VenueViewModelTest` builds the ViewModel before the dispatcher rule starts"
  - id: IN-06
    severity: info
    disposition: open
    title: "Test-only code ships in production source sets"
open: 6
total: 9
recorded: 2026-10-16T12:04:52.878Z
---

# Phase 05: Code Review Disposition

| Finding | Severity | Disposition | Source |
|---------|----------|-------------|--------|
| WR-01 | warning | fixed | 05-REVIEW-FIX.md |
| WR-02 | warning | fixed | 05-REVIEW-FIX.md |
| WR-03 | warning | fixed | 05-REVIEW-FIX.md |
| IN-01 | info | open | - |
| IN-02 | info | open | - |
| IN-03 | info | open | - |
| IN-04 | info | open | - |
| IN-05 | info | open | - |
| IN-06 | info | open | - |

Dispositions: `open` (recorded, not yet triaged), `fixed`, `skipped`, `deferred`.
Set `deferred` by hand and put the reason in the Source cell; both are preserved. A `|` in the reason is kept as prose and escaped on the next run.
Re-running the gate keeps every row it can. A row the current review no longer reports is kept and its Source cell flagged, so a finding does not leave this record silently. ONE exception: when a finding id is REUSED by a different finding, the earlier decision cannot keep a row — the id is taken — and it is dropped. A RECORDED decision (anything but `open`) is named on the console when that happens; a row still at `open` is replaced silently, because `open` records no decision to lose.
