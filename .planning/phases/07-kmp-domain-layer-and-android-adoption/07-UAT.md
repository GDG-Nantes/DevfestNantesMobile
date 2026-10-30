---
status: testing
phase: 07-kmp-domain-layer-and-android-adoption
source: [07-VERIFICATION.md]
started: 2026-10-30T00:00:00Z
updated: 2026-10-30T00:00:00Z
---

## Current Test

number: 1
name: Speakers screen parity after WR-01/WR-04 (re-capture 14-speakers-scroller)
expected: |
  On the current branch head (>= 575970ae), open Speakers and compare with
  07-parity/main/14-speakers-scroller.png. Headers appear on the first speaker of each letter
  exactly as on main, dragging the A-Z scroller (including a letter with no speaker, such as "i")
  jumps to the same rows, and there is no flicker or missing header after load or one device rotation.
awaiting: user response

## Tests

### 1. Speakers screen parity after WR-01/WR-04 (re-capture 14-speakers-scroller)
expected: Headers appear on the first speaker of each letter exactly as on main, the scroller jumps to the same rows, no flicker or missing header after load or rotation.
result: [pending]

## Summary

total: 1
passed: 0
issues: 0
pending: 1
skipped: 0
blocked: 0

## Gaps
