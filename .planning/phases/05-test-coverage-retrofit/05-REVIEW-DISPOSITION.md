---
phase: 05
review: 05-REVIEW.md
titles: json
findings:
  - id: WR-01
    severity: warning
    disposition: open
    title: "`Agenda.Builder` puts sessions that start up to 24h before a conference day into that day"
  - id: WR-02
    severity: warning
    disposition: open
    title: "`ScheduleSlot` throws on construction for default or malformed dates, and `compareTo` can overflow"
  - id: WR-03
    severity: warning
    disposition: open
    title: "Agenda sorts by raw date string, while bucketing parses instants"
  - id: IN-01
    severity: info
    disposition: open
    title: "Linkage test name claims sorting, and two statements assert nothing"
  - id: IN-02
    severity: info
    disposition: open
    title: "Reflection helpers fail opaquely on an OpenFeedback bump"
  - id: IN-03
    severity: info
    disposition: open
    title: "Display-format assertion is very weak and locale-fixed"
  - id: IN-04
    severity: info
    disposition: open
    title: "Version pin depends on Gradle's ordering of an unconventional qualifier"
open: 7
total: 7
recorded: 2026-10-16T14:55:40.000Z
---

# Phase 05: Code Review Disposition

| Finding | Severity | Disposition | Source |
|---------|----------|-------------|--------|
| WR-01 | warning | open | pre-existing code (05-11 changed only the import line) |
| WR-02 | warning | open | pre-existing code (05-11 changed only the import line) |
| WR-03 | warning | open | pre-existing code (05-11 changed only the import line) |
| IN-01 | info | open | - |
| IN-02 | info | open | - |
| IN-03 | info | open | - |
| IN-04 | info | open | - |

Dispositions: `open` (recorded, not yet triaged), `fixed`, `skipped`, `deferred`.
Set `deferred` by hand and put the reason in the Source cell; both are preserved. A `|` in the reason is kept as prose and escaped on the next run.
Re-running the gate keeps every row it can. A row the current review no longer reports is kept and its Source cell flagged, so a finding does not leave this record silently. ONE exception: when a finding id is REUSED by a different finding, the earlier decision cannot keep a row — the id is taken — and it is dropped. A RECORDED decision (anything but `open`) is named on the console when that happens; a row still at `open` is replaced silently, because `open` records no decision to lose.

**Id reuse on this run (2026-10-16):** the incremental 05-11 review reused WR-01..03 and IN-01..04. The previous review's recorded decisions — WR-01, WR-02, WR-03 `fixed` (05-REVIEW-FIX.md, commits 399059c, c17825e, f5ed59f) and IN-01..06 `open` — were superseded by id and are preserved in git history (05-REVIEW-DISPOSITION.md at 9fe8760).
