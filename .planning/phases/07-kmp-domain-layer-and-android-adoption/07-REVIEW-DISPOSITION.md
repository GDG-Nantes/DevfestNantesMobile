---
phase: 07
review: 07-REVIEW.md
titles: json
findings:
  - id: WR-01
    severity: warning
    disposition: open
    title: "Speaker A-Z scroller index and row header use different case folding, so some letters break"
  - id: WR-02
    severity: warning
    disposition: fixed
    title: "`record-agenda-snapshot.sh` swallows real failures and prints a success marker"
  - id: WR-03
    severity: warning
    disposition: fixed
    title: "iOS stores persist `state.value` after the update, so concurrent writers can lose a write"
  - id: WR-04
    severity: warning
    disposition: fixed
    title: "Non-fatal report fires from a composition-time call and repeats on every recomputation"
  - id: IN-01
    severity: info
    disposition: open
    title: "Shared \"navigable\" table leaves KEYNOTE sessions with a bookmark star and no detail screen"
  - id: IN-02
    severity: info
    disposition: open
    title: "Stale phase-planning comments left in code and docs"
  - id: IN-03
    severity: info
    disposition: open
    title: "Duplicate `TestDataFiles` implementations for android and jvm"
  - id: IN-04
    severity: info
    disposition: open
    title: "Every day-label parse repeats inside the per-session loop"
open: 5
total: 8
recorded: 2026-10-30T17:45:13.024Z
---

# Phase 07: Code Review Disposition

| Finding | Severity | Disposition | Source |
|---------|----------|-------------|--------|
| WR-01 | warning | open | - |
| WR-02 | warning | fixed | 07-REVIEW-FIX.md |
| WR-03 | warning | fixed | 07-REVIEW-FIX.md |
| WR-04 | warning | fixed | 07-REVIEW-FIX.md |
| IN-01 | info | open | - |
| IN-02 | info | open | - |
| IN-03 | info | open | - |
| IN-04 | info | open | - |

Dispositions: `open` (recorded, not yet triaged), `fixed`, `skipped`, `deferred`.
Set `deferred` by hand and put the reason in the Source cell; both are preserved. A `|` in the reason is kept as prose and escaped on the next run.
Re-running the gate keeps every row it can. A row the current review no longer reports is kept and its Source cell flagged, so a finding does not leave this record silently. ONE exception: when a finding id is REUSED by a different finding, the earlier decision cannot keep a row — the id is taken — and it is dropped. A RECORDED decision (anything but `open`) is named on the console when that happens; a row still at `open` is replaced silently, because `open` records no decision to lose.
