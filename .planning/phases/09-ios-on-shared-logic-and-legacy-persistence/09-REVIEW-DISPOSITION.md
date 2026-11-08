---
phase: 09
review: 09-REVIEW.md
titles: json
findings:
  - id: WR-01
    severity: warning
    disposition: open
    title: "A bookmark toggled while the seed is awaiting can be overwritten by the stale first emission"
  - id: IN-01
    severity: info
    disposition: open
    title: "The insert/remove pair is duplicated inside `toggleBookmark`"
  - id: IN-02
    severity: info
    disposition: open
    title: "Stale comment in the seed error path"
  - id: IN-03
    severity: info
    disposition: open
    title: "`KotlinContinuousFlowTests` write to the real stores and are not isolated from other suites"
  - id: IN-04
    severity: info
    disposition: open
    title: "Bookmark subscription teardown after the seed is not asserted"
  - id: IN-05
    severity: info
    disposition: open
    title: "Pre-existing items on touched lines (not phase defects)"
open: 6
total: 6
recorded: 2026-11-08T09:39:40.397Z
---

# Phase 09: Code Review Disposition

| Finding | Severity | Disposition | Source |
|---------|----------|-------------|--------|
| WR-01 | warning | open | - |
| IN-01 | info | open | - |
| IN-02 | info | open | - |
| IN-03 | info | open | - (not in the current review) |
| IN-04 | info | open | - (not in the current review) |
| IN-05 | info | open | - (not in the current review) |

Dispositions: `open` (recorded, not yet triaged), `fixed`, `skipped`, `deferred`.
Set `deferred` by hand and put the reason in the Source cell; both are preserved. A `|` in the reason is kept as prose and escaped on the next run.
Re-running the gate keeps every row it can. A row the current review no longer reports is kept and its Source cell flagged, so a finding does not leave this record silently. ONE exception: when a finding id is REUSED by a different finding, the earlier decision cannot keep a row — the id is taken — and it is dropped. A RECORDED decision (anything but `open`) is named on the console when that happens; a row still at `open` is replaced silently, because `open` records no decision to lose.
