---
phase: 08
review: 08-REVIEW.md
titles: json
findings:
  - id: WR-01
    severity: warning
    disposition: open
    title: "Upstream (Kotlin-side) cancellation is swallowed as a view cancellation and leaves the blocking spinner up forever"
  - id: WR-02
    severity: warning
    disposition: open
    title: "Returning to the Agenda tab resets the selected day and refetches everything"
  - id: WR-03
    severity: warning
    disposition: open
    title: "Force-unwrapped URLs built from remote data (crash on malformed value)"
  - id: WR-04
    severity: warning
    disposition: open
    title: "Sessions opened from Speaker details get a broken OpenFeedback link (`day: \"day\"` literal)"
  - id: WR-05
    severity: warning
    disposition: open
    title: "Hard-coded event dates and a lossy `yyyy-MM-d` section key"
  - id: IN-01
    severity: info
    disposition: open
    title: "Deprecated APIs remain after moving to the iOS 17 target"
  - id: IN-02
    severity: info
    disposition: open
    title: "Duplicated filter-toggle and push-link code"
  - id: IN-03
    severity: info
    disposition: open
    title: "Dead or unused code"
  - id: IN-04
    severity: info
    disposition: open
    title: "`speakerId` parameter shadows the stored property"
  - id: IN-05
    severity: info
    disposition: open
    title: "Cancelled loads are still stopped and sent as Firebase traces; `trackNavigation` was not updated"
  - id: IN-06
    severity: info
    disposition: open
    title: "`swift-observation-gate.sh` has detection gaps"
  - id: IN-07
    severity: info
    disposition: open
    title: "Test fakes mutate a stored `AsyncStream.Iterator` from an `@unchecked Sendable` class"
  - id: IN-08
    severity: info
    disposition: open
    title: "Immediate scroll after the loading flip (unverified timing change)"
---

# Phase 08 code review disposition

All findings from `08-REVIEW.md` (0 critical, 5 warning, 8 info) are recorded as `open`: none were triaged during phase execution. The review is advisory and did not block the phase.

Notes for triage:
- WR-04 (`day: "day"` literal) predates Phase 8 (reviewer's note).
- WR-02 (Agenda day reset/refetch on reappearance) and IN-08 (initial-scroll timing) were not reproduced: the 08-09 parity pass found tab switching identical to v1.0, and the shifted-clock scroll check is logged as debt in `deferred-items.md`.
