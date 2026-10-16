---
phase: 05-test-coverage-retrofit
audited: 2026-10-16
baseline: abstract 6-pillar standards (no UI-SPEC.md)
screenshots: not captured (code-only audit)
overall_score: 17/24
---

# Phase 05 — UI Review (code-only)

Scope: the phase is a test retrofit. Its only production UI-affecting changes are the FeedbackFlags seam (05-08), the kotlinx-datetime/OpenFeedback pin (05-11), the AgendaViewModel IO dispatcher injection (05-06) and the agenda date rendering path (05-02). Scores cover the UI code as it stands in the touched screens, including pre-existing code.

## Pillar Scores

| Pillar | Score | Key Finding |
|---|---|---|
| 1. Copywriting | 3/4 | Hardcoded English `contentDescription = "favorite"` in AgendaRow.kt:171, not localized |
| 2. Visuals | 3/4 | Bookmark control has one static description for both states |
| 3. Color | 3/4 | No hex/`Color(` literals in touched feature modules; no color changes in the phase |
| 4. Typography | 3/4 | Consistent M3 type roles; no ad-hoc sizes |
| 5. Spacing | 3/4 | Consistent 4/8/16 dp scale in touched files |
| 6. Experience Design | 2/4 | Store failures never surface an error state; fallback feedback URL has a double slash |

**Overall: 17/24.** No pillar scored 1. G-05-3 (feedback crash) is resolved and re-verified on device (UAT test 3 pass).

## Top 3 Priority Fixes

1. **Store errors never surfaced (Pillar 6, WARNING).** `SpeakersViewModel.init`, `AgendaViewModel.onRefresh` and `SessionViewModel.init` collect store flows without `catch`; `UiState.ERROR` is never set, so `Speakers.kt:93` would spin forever on failure (deferred items 9, 10, 13). Reachability unconfirmed: the real GraphQL store appears to log and return null/empty. Fix: `.catch { _uiState.value = UiState.ERROR }`, an error layout with retry copy (en/fr), and invert the `store_failure_is_characterized` tests.
2. **Fallback feedback URL double slash (Pillar 6, WARNING).** `WebLinks.OPENFEEDBACK_BASE_URL` ends with `/` and `FallbackFeedbackForm.kt:48` appends `/` (deferred item 17). Fix the join and update `FallbackFeedbackUrlTest`.
3. **Bookmark control unlocalized and state-blind (Pillars 1/2, WARNING).** Move `"favorite"` (AgendaRow.kt:171) to strings.xml (en/fr) with distinct add/remove labels keyed on bookmark state.

## Detailed Findings

### Pillar 1: Copywriting (3/4)
- AgendaRow.kt:171 literal `"favorite"` content description.
- Empty-state and feedback copy present in both locales (`empty_day`, `session_filters_clear`, `session_feedback_label`).
- No error copy exists, since `UiState.ERROR` is unused.

### Pillar 2: Visuals (3/4)
- Clear hierarchy in AgendaRow (`titleMedium` primary title, `labelMedium` metadata).
- FeedbackForm uses a `headlineSmall` label; FallbackFeedbackForm a centered OutlinedButton.
- Bookmark icon description does not reflect toggled state.

### Pillar 3: Color (3/4)
- No hardcoded colors in agenda, session-detail or speakers `src/main`. 60/30/10 balance not verified without screenshots.

### Pillar 4: Typography (3/4)
- Consistent M3 roles; no font-size/weight overrides in touched code.

### Pillar 5: Spacing (3/4)
- Consistent `8.dp`/`16.dp`/`spacedBy(8.dp)`/`4.dp`; no arbitrary values.

### Pillar 6: Experience Design (2/4)
- Missing error state (Priority 1), pinned by `store_failure_is_characterized` tests.
- Fallback link defect (Priority 2).
- Feedback area hidden silently when the flag is off (intended); no placeholder during `fetchAndActivate`, so it pops in. Minor.
- UAT tests 1–3 passed on device.

## Recommendations (non-priority)
- AgendaColumn.kt time formatter uses `Resources.getSystem()` locale rather than the per-app language; confirm intended.
- REVIEW IN-04: kotlinx-datetime `0.8.0-0.6.x-compat` relies on Gradle ordering; consider `strictly`.
- REVIEW WR-01: agenda day bucketing admits sessions up to 24 h before day one.

## Files Audited
- feature/session-detail: FeedbackFlags.kt, FeedbackFormViewModel.kt, FeedbackForm.kt, FallbackFeedbackForm.kt, OpenFeedbackConfig.kt, di/SessionDetailModule.kt
- feature/agenda: AgendaRow.kt, AgendaColumn.kt, AgendaPager.kt, Agenda.kt, AgendaViewModel.kt
- feature/speakers: list/SpeakersViewModel.kt, list/Speakers.kt
- core/ui DateUtils.kt, core/model WebLinks.kt, strings.xml (en/fr) in agenda and session-detail
