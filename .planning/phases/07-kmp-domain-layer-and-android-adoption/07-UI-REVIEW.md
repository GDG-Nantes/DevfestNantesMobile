# Phase 07 — UI Review

**Audited:** 2026-10-29
**Baseline:** Abstract 6-pillar standards (no UI-SPEC.md for this phase), plus the phase's own contract: NO visible difference from v1.0 (07-CONTEXT.md, 07-parity/CAPTURE-NOTES.md)
**Screenshots:** not captured by this audit (Android app, no localhost dev server; web probe on :3000 returned 000). Visual evidence = the 15 paired main/branch emulator captures in `07-parity/` (Pixel 9, en-US, Europe/Paris), human-approved 2026-10-29, plus `07-parity/release/` smoke captures.
**Interaction captures:** off (workflow.ui_interaction_capture is false). Interaction findings below are code-derived.

**Scope:** Android agenda (Agenda, AgendaPager, AgendaColumn, AgendaRow), speakers A-Z scroller (feature/speakers list), venue screen (feature/venue). Pre-existing v1.0 issues are recorded but are NOT phase regressions, because the branch screenshots match main pixel-for-pixel (01, 03 to 15 identical; 02 differs by a 2 px scroll offset from the scripted drag).

---

## Pillar Scores

| Pillar | Score | Key Finding |
|--------|-------|-------------|
| 1. Copywriting | 3/4 | Visible copy identical to v1.0 in parity captures; one hardcoded English accessibility string (`"favorite"`) in AgendaRow.kt:175 |
| 2. Visuals | 3/4 | Clear time-slot hierarchy and focal session cards; venue screen leaves ~40% of the viewport as an unexplained blank band above the title |
| 3. Color | 3/4 | Phase-touched files contain no hardcoded hex/Color literals; accent is restrained; bookmarked state uses an olive-green tint that sits outside the blue accent family |
| 4. Typography | 3/4 | Type comes from theme roles (`typography.*`); 9 distinct roles in the phase-touched UI, with only 2 literal `fontSize` overrides (14.sp, 16.sp) |
| 5. Spacing | 3/4 | Spacing is dominated by 8/12/16 dp tokens; no arbitrary spacing added by the phase; the speakers A-Z index hugs the right edge |
| 6. Experience Design | 3/4 | Loading, empty and error handling exist in the agenda, speakers and venue ViewModels/layouts; parity verified; cold start 12.4 to 13.7 s under host memory pressure is a real UX risk (pre-existing, not measured as a regression) |

**Overall: 18/24**

No pillar scored 1. No BLOCKER was found. The phase introduced no user-visible regression. All WARNINGs below are either pre-existing v1.0 issues or polish items.

---

## Top 3 Priority Fixes

1. **Venue screen has a large empty band above the title** (`07-parity/branch/15-venue.png`, identical on main) — users see a blank ~1/3 of the screen before "Cité des Congrès de Nantes", which reads as a loading or broken hero image — Replace the hero area with a fixed-height image placeholder (or reduce its height) in `feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/VenueDetails.kt` (image content description at line 70 suggests a hero image slot that is not rendering in the capture). Verify the image loads in the capture; if it is a network image, add an explicit placeholder color.
2. **Hardcoded English accessibility label on the bookmark affordance** (`feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/AgendaRow.kt:175`, `contentDescription = "favorite"`) — TalkBack users in French locale hear English, and the label does not state the action or state (bookmarked vs not) — Move to `stringResource` with a localized label such as "Add to favorites" / "Remove from favorites" bound to the current bookmark state. Note: AgendaRow.kt was not modified by Phase 07, so this is a v1.0 issue; fixing it is out of parity scope and should be a separate change.
3. **Cold start is 12.4 to 13.7 s in parity measurements** (CAPTURE-NOTES.md, emulator under host memory pressure) — the agenda is the first screen users see, and a multi-second blank start is the biggest experience risk in the phase's evidence — Re-measure on a clean emulator with the data partition freed, and profile the first agenda emission (the new ObserveAgendaUseCase path adds a shared-module hop). Treat as a performance follow-up, not a visual regression, since main measured 13.7 s and branch 12.4 s on the same run.

---

## Detailed Findings

### Pillar 1: Copywriting (3/4)

- Parity: visible labels are identical between main and branch across all 15 pairs: tab titles "October 16, 2025" / "October 17, 2025", slot headers "8:00 AM", "9:00 AM", "9:40 AM", "10:00 AM", row metadata "50 minutes / flag", "60 minutes", "20 minutes", bottom navigation "Agenda / Speakers / Venue / About", venue CTA "Go to".
- Hardcoded accessibility string: `AgendaRow.kt:175` `contentDescription = "favorite"` (English, not localized, no state). Pre-existing, not touched by Phase 07.
- Hardcoded text literals: a grep for `Text("...")` in agenda/speakers/venue returned no matches; visible strings go through resources. Good.
- Empty/error copy: resource-backed through the shared layouts (EmptyLayout.kt, LoadingLayout). Exact strings not re-verified against a contract because no UI-SPEC exists.
- Venue body copy is truncated with an ellipsis ("...hand's on and networking space…") in the capture. Acceptable, but no "Read more" affordance is visible.

### Pillar 2: Visuals (3/4)

- Focal point on the agenda is clear: the selected day tab carries a blue underline, and large slot headers (8:00 AM, 9:00 AM) anchor the list. Session cards with blue titles stand out against the dark surface.
- Bookmark state is visually distinct in captures: unbookmarked shows an outlined "+" bookmark (01), bookmarked shows a filled check bookmark (03; 11 not individually inspected). Good state differentiation, though the icon-only control relies on the non-localized "favorite" label (Pillar 1).
- Venue (15-venue.png): roughly the top 40% of the content area is empty before the venue name. Hierarchy below that is fine (title, address, "Go to" pill button, description). Unexplained whitespace is a visual defect, identical on main, so pre-existing.
- Speakers (14-speakers-scroller.png): the A-Z index is drawn at the right screen edge with letters "m" and "z" almost touching the edge; the letter gutter on the left (K, L, M) is clear. Tight but legible.
- Icon-only controls in the top bar (filter, settings) are visually consistent with the Material icon set. Their accessibility labels were not verified in the phase-touched code.

### Pillar 3: Color (3/4)

- No hardcoded color literals (`Color(0x...)` or `#RRGGBB`) in the phase-touched agenda, speakers or venue source. Colors come from the theme in `core/ui/theme`.
- Accent use is restrained: the blue accent appears on session titles, the selected tab indicator, the "Go to" button, the selected bottom-nav pill and the slot date header. It is used on a small number of elements per screen, which is correct for a 60/30/10 split on a dark surface.
- Bookmarked-state tint (03-day2-top, 11-home-filtered-indicator) is olive green rather than the blue accent. This is a second accent hue on the same screen. It reads as "success/saved" and is defensible, but it is a second accent and should be recorded in the theme if intentional.
- Dark surface split (visual estimate from captures, no hex values extracted): background, cards and bottom bar read as three close dark surface levels. Three surface levels, no contrast failure observed in captures.

### Pillar 4: Typography (3/4)

- Phase-touched UI uses theme roles: titleLarge, titleMedium, titleSmall, headlineLarge, headlineMedium, bodyLarge, bodyMedium, labelMedium (6 uses), subtitle2 (2 uses), h5 (1 use). Distribution is roughly 9 roles across the three screens.
- Literal overrides: `fontSize = 14.sp` and `fontSize = 16.sp` (one each), plus one `FontWeight.Bold`. Minor, but they bypass the type scale.
- Hierarchy is clear in captures: slot time headers are the largest text on the agenda and compete with session titles. The slot-header size is a deliberate v1.0 choice and is not a phase regression.
- Recommendation: consolidate the 10 roles toward at most 6 in a future pass, since the agenda, speakers and venue screens currently use more roles than the content requires.

### Pillar 5: Spacing (3/4)

- Phase-touched spacing values are dominated by the 8/12/16 dp scale (8.dp x13, 16.dp x8, 12.dp x8), with 4.dp x3 and 24.dp x3. This is a consistent rhythm.
- Larger literals (48.dp x4, 96.dp x2, 128.dp, 200.dp, 250.dp) are mostly component sizes (icons, touch targets, hero/image heights). The 250.dp / 200.dp values are the likely sources of the venue blank band (Pillar 2) and should be checked against the hero image sizing.
- No arbitrary bracket values (`[..px]`, `[..rem]`) were introduced by the phase.
- Speakers A-Z index sits flush to the right edge (Pillar 2). The index needs a small end inset to clear the edge and match the 16 dp horizontal rhythm used elsewhere.

### Pillar 6: Experience Design (3/4)

- State coverage exists in the phase-touched code: loading (LoadingLayout), empty (EmptyLayout.kt, agenda), and error paths in SpeakersViewModel and the agenda ViewModel. Agenda ViewModel collects `ObserveAgendaUseCase` with an explicit `TimeZone.currentSystemDefault()`, so the day grouping is deterministic.
- Parity verified behavior, not only pixels: filters (room, language, complexity, type), favorites filter, drawer, session detail, non-navigable rows (13-non-navigable-row.png matches main), and bookmark persistence across force-stop and relaunch (release/01 to 03). The release build logged zero SerializationException or ClassNotFoundException lines.
- Non-navigable rows (break, 13-non-navigable-row.png) match main; tap-affordance behavior was not separately verified.
- Destructive actions: none added in this phase.
- Gaps: no skeleton for the agenda first paint, so the 12 to 14 s cold start shows a blank or spinner state with no progress cue. Interaction states (pressed, focus ring on the Tab order, hover) were not captured; findings for those are code-derived only and not scored beyond the parity evidence.
- Registry audit: not applicable (no components.json / shadcn in the Android project).

---

## Pre-existing v1.0 issues (not phase regressions)

- Bookmark `contentDescription = "favorite"` hardcoded English (AgendaRow.kt:175).
- Venue hero blank band (15-venue.png), identical on main.
- Speakers A-Z index flush to the right edge.
- Cold start 12 to 14 s under host memory pressure, measured on both main and branch.

## Phase 07 changes checked for regressions

- Agenda rendering moved to `DaySchedule` / `TimeSlot` from the shared domain: slot order, label text, room order and scroll index match main in 01, 03 to 13.
- Slots without a label are dropped in AgendaColumn (documented in 07-15-SUMMARY.md); v1.0 also dropped them, so this preserves behavior.
- Speakers A-Z scroller (14) identical to main.
- Venue (15) identical to main.

---

## Files Audited

- `.planning/phases/07-kmp-domain-layer-and-android-adoption/07-CONTEXT.md`
- `.planning/phases/07-kmp-domain-layer-and-android-adoption/07-parity/CAPTURE-NOTES.md`
- `.planning/phases/07-kmp-domain-layer-and-android-adoption/07-15-SUMMARY.md`, `07-17-SUMMARY.md`, `07-18-SUMMARY.md`, `07-19-SUMMARY.md`
- `.planning/phases/07-kmp-domain-layer-and-android-adoption/07-parity/branch/01-day1-top.png`, `03-day2-top.png`, `13-non-navigable-row.png`, `14-speakers-scroller.png`, `15-venue.png` (viewed); the remaining pairs were checked via CAPTURE-NOTES objective comparison
- `feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/` (AgendaViewModel.kt, Agenda.kt, AgendaPager.kt, AgendaColumn.kt, AgendaRow.kt, EmptyLayout.kt, SessionFiltersDrawer.kt)
- `feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/` (list/Speakers.kt, list/SpeakersViewModel.kt, details/SpeakerLayout.kt)
- `feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/` (Venue.kt, VenueDetails.kt, plan/VenueFloorPlan.kt)
- Grep sweeps: hardcoded colors, fontSize/FontWeight/typography roles, dp literals, `Text("...")` literals, `contentDescription`, state-layout usage
