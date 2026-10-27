---
phase: 06
review: 06-REVIEW.md
titles: json
findings:
  - id: WR-01
    severity: warning
    disposition: open
    title: "Test-host detection ships in release builds and can blank the real app"
  - id: WR-02
    severity: warning
    disposition: open
    title: "`xcresult-assert-tests.sh` can be bypassed by an ambient env var and ignores the bundle result"
  - id: WR-03
    severity: warning
    disposition: open
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
open: 8
total: 8
recorded: 2026-10-27T13:45:59.573Z
---

# Phase 06: Code Review Disposition

| Finding | Severity | Disposition | Source |
|---------|----------|-------------|--------|
| WR-01 | warning | open | - |
| WR-02 | warning | open | - |
| WR-03 | warning | open | - |
| IN-01 | info | open | - |
| IN-02 | info | open | - |
| IN-03 | info | open | - |
| IN-04 | info | open | - |
| IN-05 | info | open | - |

Dispositions: `open` (recorded, not yet triaged), `fixed`, `skipped`, `deferred`.
Set `deferred` by hand and put the reason in the Source cell; both are preserved.
