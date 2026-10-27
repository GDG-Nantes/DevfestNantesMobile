---
phase: 06
review: 06-REVIEW.md
titles: json
findings:
  - id: WR-01
    severity: warning
    disposition: fixed
    title: "Test-host detection ships in release builds and can blank the real app"
  - id: WR-02
    severity: warning
    disposition: fixed
    title: "`xcresult-assert-tests.sh` can be bypassed by an ambient env var and ignores the bundle result"
  - id: WR-03
    severity: warning
    disposition: fixed
    title: "CI runs on a floating Xcode and simulator runtime while one test pins OS formatter behaviour"
  - id: IN-01
    severity: info
    disposition: open
    title: "Code coverage silently turned off when moving to the test plan"
  - id: IN-02
    severity: info
    disposition: open
    title: "`FilterType` round-trip test enumerates cases by hand"
  - id: IN-03
    severity: info
    disposition: open
    title: "`AGENTS.md` refers to a module that does not exist"
  - id: IN-04
    severity: info
    disposition: open
    title: "`swift-names-gate.sh` fails silently and is macOS-only without a guard"
  - id: IN-05
    severity: info
    disposition: open
    title: "`simctl boot ... || true` hides the real boot failure; step summary lines are unformatted"
open: 5
total: 8
recorded: 2026-10-27T14:00:03.227Z
---

# Phase 06: Code Review Disposition

| Finding | Severity | Disposition | Source |
|---------|----------|-------------|--------|
| WR-01 | warning | fixed | 06-REVIEW-FIX.md |
| WR-02 | warning | fixed | 06-REVIEW-FIX.md |
| WR-03 | warning | fixed | 06-REVIEW-FIX.md |
| IN-01 | info | open | - |
| IN-02 | info | open | - |
| IN-03 | info | open | - |
| IN-04 | info | open | - |
| IN-05 | info | open | - |

Dispositions: `open` (recorded, not yet triaged), `fixed`, `skipped`, `deferred`.
Set `deferred` by hand and put the reason in the Source cell; both are preserved. A `|` in the reason is kept as prose and escaped on the next run.
Re-running the gate keeps every row it can. A row the current review no longer reports is kept and its Source cell flagged, so a finding does not leave this record silently. ONE exception: when a finding id is REUSED by a different finding, the earlier decision cannot keep a row — the id is taken — and it is dropped. A RECORDED decision (anything but `open`) is named on the console when that happens; a row still at `open` is replaced silently, because `open` records no decision to lose.
