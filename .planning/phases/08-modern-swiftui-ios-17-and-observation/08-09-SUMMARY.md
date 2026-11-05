---
phase: 08-modern-swiftui-ios-17-and-observation
plan: 09
subsystem: testing
tags: [ios, parity, uat, maestro, swiftui, observation, screenshots]

requires:
  - phase: 08-modern-swiftui-ios-17-and-observation
    provides: "08-01 to 08-08 Observation migration of the five iOS ViewModels, shared AgendaViewModel in ContentView, Combine removal, swift-observation gate"
provides:
  - "Maestro flow driving the 21-screen D-16 sequence (reusable for the Phase 9 and 11 iOS parity passes)"
  - "21 paired baseline/branch screenshots from two identical simulators"
  - "Green full phase gate on the branch head (Android, KMP, DI, KMP-NC, Swift gates, Swift suite 57/57)"
  - "Human approval of iOS parity with v1.0 (no phase-caused difference, no D-03 deviation observed)"
affects: [09-ios-domain-adoption, 11-ios-parity, 12-swiftui-modernization]

# Measured with git rev-list --count plan_head_before..plan_head_after: 201cbb0a, db01fc1a, the
# executor-worktree merge commit b627add4, and 82ae4d71 (this SUMMARY commit follows).
plan_head_before: 3fb8ba7b4962feb7ddcb1fe33176f8df690ce049
plan_head_after: 82ae4d71
actuals:
  tokens: 90000
  tasks: 3
  commits: 4

tech-stack:
  added: []
  patterns:
    - "Paired-simulator parity capture: two identical simulators (same device type, runtime, locale, status-bar time), one with the baseline build and one with the branch build, driven by the same Maestro flow"
    - "Noise-reference baseline: a third baseline run (not committed) separates run-to-run noise from real differences"

key-files:
  created:
    - .planning/phases/08-modern-swiftui-ios-17-and-observation/08-parity/parity-flow.yaml
    - .planning/phases/08-modern-swiftui-ios-17-and-observation/08-parity/base/ (21 PNGs, 01-agenda-day1 to 21-about)
    - .planning/phases/08-modern-swiftui-ios-17-and-observation/08-parity/branch/ (21 PNGs, 01-agenda-day1 to 21-about)
  modified:
    - .planning/phases/08-modern-swiftui-ios-17-and-observation/deferred-items.md

key-decisions:
  - "Approved iOS parity: the human found no difference between the 21 base/branch pairs; D-03 was not observed as a visible difference on any captured path, so no 'Known behavior deviations' subsection was added to documentation/ARCHITECTURE.md"
  - "Shifted-clock initial scroll check (RESEARCH A4) logged as debt rather than blocking: a shifted host clock breaks TLS on both builds, so it cannot be verified by hand; recommend an injected-clock unit test"

patterns-established:
  - "Parity evidence is committed before the human checkpoint (rate-limit rule); only the SUMMARY follows approval"

requirements-completed: [IOSUI-02, IOSUI-03]

coverage:
  - id: D1
    description: "Same 21-screen sequence captured by the agent on a baseline build and on the branch build, on two identical iPhone 17 / iOS 27.0 simulators, committed with the Maestro flow"
    requirement: IOSUI-03
    verification:
      - kind: automated_ui
        ref: ".planning/phases/08-modern-swiftui-ios-17-and-observation/08-parity/parity-flow.yaml (maestro, 42 PNGs; 13 pairs byte-identical, 8 differ by run-to-run noise only)"
        status: pass
    human_judgment: false
  - id: D2
    description: "Human comparison of every base/branch pair and tab-switching behavior approved: no phase-caused visible difference"
    requirement: IOSUI-03
    verification: []
    human_judgment: true
    rationale: "D-16 (locked) requires a human to approve the visual comparison; blocking-human checkpoint, never auto-approved"
  - id: D3
    description: "Full phase gate green on the branch head: detekt, lint, Android/JVM tests, DI gate, KMP-NC gate, IOSUI-02 zero unstructured/detached Task check, swift-observation gate (check and selftest), swift-names gate (check and selftest), Swift suite XCRESULT-OK with 57 tests"
    requirement: IOSUI-02
    verification:
      - kind: integration
        ref: "./gradlew detekt lint; ./gradlew testDebugUnitTest jvmTest testAndroidHostTest; scripts/di-gate.sh; scripts/kmp-nc-version-gate.sh; scripts/swift-observation-gate.sh; scripts/swift-names-gate.sh; scripts/xcresult-assert-tests.sh"
        status: pass
    human_judgment: false
  - id: D4
    description: "Shifted-clock initial scroll check (D-12, RESEARCH A4)"
    requirement: IOSUI-02
    verification: []
    human_judgment: true
    rationale: "Could not be verified: with the host clock shifted both builds fail TLS validation and load nothing; logged as debt in deferred-items.md"

duration: multi-session (host overload retry, reboot, human checkpoint)
completed: 2026-11-05
status: complete
---

# Phase 8 Plan 09: iOS parity pass and phase gate Summary

**D-16 iOS parity proven: 21 Maestro-captured screen pairs from a pre-phase baseline build and the Observation branch build on two identical iPhone 17 / iOS 27.0 simulators, human-approved with no phase-caused difference, and the full phase gate green (Swift suite 57/57).**

## Performance

- **Tasks:** 3 (tracer, auto, blocking-human checkpoint)
- **Files modified:** 44 (flow file, 42 PNGs, deferred-items.md)
- **Completed:** 2026-11-05

## Accomplishments

- Built a reusable parity harness: baseline build of `BASE = 03e5259d3375f21ecd33f1d42465c46ea1e4e4e7` (the commit that created the phase plans; its `iosApp` tree equals the Phase 7 main state `5896f573`) in a temporary worktree, branch head `3fb8ba7b4962feb7ddcb1fe33176f8df690ce049`, each installed on its own simulator ("DevFest Parity Base" 8960ED5E-E6FF-423E-8526-AD45B3061468, "DevFest Parity Branch" C1A43BEA-1A32-4BAE-BCE3-A3B06C7B347F), both iPhone 17 / iOS 27.0, English locale, status bar 9:41, Xcode 27.1, Maestro 1.35.0.
- Captured the full D-16 sequence (agenda day 1 and 2, stars from list and detail, filter menu and the five filter types, favorites after relaunch, session detail, speakers with the A-Z index, speaker detail, a session opened from the speaker detail, venue, about) with `launchApp: clearState: true` as the starting point.
- Scored the pairs with ssimulacra2 against a third baseline run (base2, not committed) as a noise reference: 13 pairs are byte-identical (01, 02, 04, 05, 08, 09, 10, 11, 12, 13, 14, 19, 20); pairs 03, 06, 07, 15, 16, 17, 18 and 21 differ only by run-to-run noise (scroll offset of a few pixels, filter-menu animation phase, image-loading spinners caught at different moments), at a level comparable to base versus base2.
- Pair 10 (un-starring in the detail with the Favorites filter on) is identical on both builds, so the D-03 single-instance immediate refresh was not observed as a visible difference on this path.
- Human checkpoint (2026-11-05): the user compared the pairs, including 03, 06, 07, 15, 16, 17, 18 and 21, and found no difference; tab switching on both simulators kept day, filters and favorites with no empty list. Verdict: approved.
- Full phase gate on the branch head was green: detekt + lint, `testDebugUnitTest jvmTest testAndroidHostTest`, `DI-GATE-OK`, `KMPNC-VERSION-OK 1.0.6`, IOSUI-02 check (no unstructured or detached `Task` under `iosApp/iosApp`), swift-observation gate check and selftest, swift-names gate check and selftest, Swift suite `XCRESULT-OK` with 57/57 tests.

## Task Commits

1. **Task 1 (tracer): parity harness and first screenshot pair** - `201cbb0a` (test)
2. **Task 2: full D-16 sequence on both builds and the full phase gate** - `db01fc1a` (test)
3. **Task 3: human comparison (blocking-human)** - no code commit; approved by the user, debt logged in `82ae4d71` (docs)

**Plan metadata:** this SUMMARY (docs: complete plan), committed after `82ae4d71`.

## Files Created/Modified

- `.planning/phases/08-modern-swiftui-ios-17-and-observation/08-parity/parity-flow.yaml` - Maestro flow (`appId: com.gdgnantes.devfest.iosApp`) for the 21-screen sequence
- `.planning/phases/08-modern-swiftui-ios-17-and-observation/08-parity/base/*.png` - baseline screenshots (21)
- `.planning/phases/08-modern-swiftui-ios-17-and-observation/08-parity/branch/*.png` - branch screenshots (21)
- `.planning/phases/08-modern-swiftui-ios-17-and-observation/deferred-items.md` - "From 08-09" section with two debt entries

## Decisions Made

- Approved parity as reported; no `### Known behavior deviations` subsection was added to `documentation/ARCHITECTURE.md` because D-03 was not observed as a visible difference on the captured paths (the plan only requires it when a deviation is recorded).
- The shifted-clock check is logged as debt, not as a blocker: it fails identically on the baseline, so it is not phase-caused.

## Deviations from Plan

### Execution notes

**1. [Environment] First capture attempt blocked by host overload, retried after a reboot**
- **Found during:** Task 2
- **Issue:** Host load of 80 to 166 hung the Maestro 1.35.0 XCUITest driver.
- **Fix:** After a Mac reboot Maestro ran cleanly (load 3 to 30), with no driver failure, no rebuild, and no change of runtime (iOS 27.0 kept; the iOS 18.6 fallback was not needed). The reboot wiped the temporary baseline worktree under `TMPDIR` (already pruned); `git worktree list` confirms none remains.
- **Verification:** all 42 PNGs present and committed in `db01fc1a`.

**2. [Unverifiable step] Checkpoint step 8 (shifted-clock initial scroll) could not be performed**
- **Found during:** Task 3
- **Issue:** With the Mac clock set to 2025-10-16 12:30, both the baseline and the branch loaded nothing: the API call fails TLS validation ("certificate is not yet valid") and no cached agenda is shown, although both apps had loaded it before.
- **Resolution:** identical on both builds, hence pre-existing and not phase-caused. The user decided to approve and log debt. Two entries added to `deferred-items.md` (shifted-clock check should become an injected-clock unit test of `AgendaViewModel` initial scroll; pre-existing no-cached-data-on-network-failure behavior).
- **Committed in:** `82ae4d71`

---

**Total deviations:** 0 auto-fixed code deviations; 1 environment retry and 1 unverifiable checkpoint step logged as debt.
**Impact on plan:** none on the product code. The D-12 scroll-hop removal remains covered by the existing unit tests only, not by a runtime check on event day.

## Issues Encountered

- Host overload hung the Maestro driver (see above); resolved by a reboot, no code change.
- Shifted host clock breaks TLS for every request (see above), which is the reason step 8 was not verifiable.

## Known Stubs

None.

## User Setup Required

None - no external service configuration required. Cleanup done: both parity simulators were shut down and deleted, and no temporary baseline worktree remains.

## Next Phase Readiness

- Phase 8 plans are all complete and iOS parity with v1.0 is approved; the phase is ready for verification.
- The parity flow is reusable for the Phase 9 and 11 iOS parity passes.
- Debt carried forward in `deferred-items.md`: D-12 deprecated SwiftUI APIs (Phase 12), stale Swift-names header baseline (Phase 9), shifted-clock initial scroll test, and no cached agenda on network failure.

## Threat Flags

None.

---
*Phase: 08-modern-swiftui-ios-17-and-observation*
*Completed: 2026-11-05*

## Self-Check: PASSED

- 08-parity/parity-flow.yaml and 42 PNGs tracked: committed in 201cbb0a and db01fc1a
- Commits 201cbb0a, db01fc1a, 82ae4d71 exist on the branch
- Parity simulators deleted; no baseline worktree registered
