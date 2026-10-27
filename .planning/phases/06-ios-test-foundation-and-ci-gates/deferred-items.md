# Phase 6 deferred items

Production behavior the Phase 6 tests expose. Each entry is pinned by a test and is not fixed in this phase (parity rule).

## From 06-02 (start-date parsing)

1. **`AgendaViewModel.getDate(date:)` (body now in `AgendaViewModel.parseDate(_:)`) rejects start dates with fractional seconds.** It uses a default-option `ISO8601DateFormatter`, which returns nil for `2025-10-16T09:00:00.000+02:00`, so such a start date falls back to `Date()` and the session is placed at the current time. The core/model fixture start dates carry no fractional seconds, so the gap is latent as far as known. Pinned by `AgendaDateParsingTests.falls_back_to_now_for_fractional_seconds`; not fixed (parity). The Phase 9 shared grouping must reproduce this or change it as a recorded deviation.
