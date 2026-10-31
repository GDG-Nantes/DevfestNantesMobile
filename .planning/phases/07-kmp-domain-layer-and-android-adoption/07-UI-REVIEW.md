# Phase 07 — UI Review (re-audit)

**Audited:** 2026-10-30
**Baseline:** Abstract 6-pillar standards (no UI-SPEC.md for this phase), plus the phase's own contract: zero visible change versus v1.0 (07-CONTEXT.md, 07-parity/CAPTURE-NOTES.md)
**Screenshots:** not captured by this audit (Android app; no dev server on localhost:3000, 5173 or 8080). Visual evidence = the 15 paired main/branch emulator captures in `07-parity/` (captured 2026-10-29) plus `07-parity/release/`. Two branch captures were viewed directly (14-speakers-scroller.png, 15-venue.png).
**Interaction captures:** off (workflow.ui_interaction_capture is false). Interaction findings are code-derived.

**Evidence staleness (important):** the parity captures were taken on branch head 4af25ed (2026-10-29). The Speakers fixes WR-01 (b6ff4909) and WR-04 (575970ae) landed on 2026-10-30, after the captures. The Speakers screen has therefore NOT been visually verified in its current form. The static analysis below suggests no visible change for ASCII speaker names, but a re-capture is required before the parity claim for Speakers is accepted.

**Scope:** Android agenda (Agenda, AgendaPager, AgendaColumn, AgendaRow, AgendaViewModel), speakers A-Z scroller (list/Speakers.kt, list/SpeakersViewModel.kt, including WR-01 and WR-04), venue (VenueDetails.kt, Venue.kt, VenueViewModel.kt).

**Attribution correction to the previous review:** the previous review said AgendaRow.kt was not modified by Phase 07. `git diff main` shows it was (isTalk parameter replaces `session.type?.isService()` in the bookmark gate). The "favorite" label itself is v1.0 (main AgendaRow.kt:171).

---

## Pillar Scores

| Pillar | Score | Key Finding |
|--------|-------|-------------|
| 1. Copywriting | 3/4 | Visible copy matches v1.0 in parity captures; bookmark button has hardcoded English `contentDescription = "favorite"` (AgendaRow.kt:175); Speakers has no empty-state copy |
| 2. Visuals | 2/4 | Venue hero is a 200 dp `AsyncImage` with no placeholder; in the capture it renders as an unexplained blank band (~30% of the viewport above the venue name); A-Z scroller hugs the right edge |
| 3. Color | 3/4 | No hardcoded color literals in phase-touched UI; accent restrained; bookmarked state uses an olive tint outside the blue accent family (v1.0) |
| 4. Typography | 3/4 | Theme type roles used throughout; literal `fontSize` overrides (14.sp, 16.sp) bypass the scale; roughly 9 roles across three screens |
| 5. Spacing | 3/4 | 8/12/16 dp rhythm dominates; no arbitrary bracket values added; the 16 dp A-Z scroller column sits flush against the screen edge with no end inset |
| 6. Experience Design | 2/4 | No UI state can reach ERROR in agenda, speakers or venue: a failed load leaves the screen on a spinner forever; Speakers has no empty state; the new empty-name report (D-20) and the WR-01 `ı`/`i` header loss are phase-caused behavior changes |

**Overall: 16/24**

Previous score 18/24 is not comparable: two claimed "error paths" (SpeakersViewModel, agenda ViewModel) do not exist in code, and the Speakers evidence predates WR-01/WR-04. Scores are reduced accordingly.

No pillar scored 1, so no pillar-level BLOCKER. Pillars 2 and 6 are WARNINGs (score 2). Per-finding classification is given below.

---

## Top 3 Priority Fixes

1. **Failed loads show a spinner forever (no error state anywhere)** — `UiState.ERROR` is never assigned in `feature/speakers`, `feature/agenda` or `feature/venue` (grep: only test comments reference it). `SpeakersViewModel` stays STARTING, `AgendaViewModel` stays STARTING, `VenueViewModel` stays LOADING if `store.getVenue` throws or no locale is present; `Venue.kt:53` then shows `LoadingLayout` indefinitely. User impact: a network or store failure looks like a permanent loading state with no retry. Fix: in each ViewModel catch the store failure, set `UiState.ERROR`, and render an error layout with a retry action (`onRefresh` already exists in AgendaViewModel). Note: this is v1.0 debt (D-04 pins uncaught store failures), so it is pre-existing, but it is the highest-impact experience gap in the phase's scope.

2. **Venue hero renders as a blank band** — `feature/venue/.../VenueDetails.kt:55-70`: `AsyncImage` with `.height(200.dp)`, no `placeholder`, no `error` painter, no background. In `07-parity/branch/15-venue.png` the top ~30% of the content area is empty before "Cité des Congrès de Nantes" and reads as broken. Fix: give the `AsyncImage` a `placeholder`/`error` painter or a `surfaceVariant` background, or hide the hero when the image fails. Confirm the cause (network or empty `imageUrl`) on the emulator. Pre-existing v1.0 (identical on main).

3. **WR-01 introduced a header regression for `ı`/`i` speakers (phase-caused)** — `SpeakerIndexUseCase.letterOf` uses `name.take(1).uppercase()`, so `ı` and `i` both map to `I`. `firstIndexes` keeps only the first, so when an `ı` speaker precedes an `i` speaker, the `i` entry is never produced and the header for that speaker is not shown. v1.0 showed both. WR-01's own report acknowledges this and leaves it unfixed. Fix: in `SpeakersViewModel.firstLetterIndexes`, derive keys directly from speaker names in list order (first index per `speakerLetterKey`) instead of deduplicating on the shared uppercase letter, and add a regression test for `ı` before `i`. The data set does not contain these letters, so this is latent, not visible today.

---

## Detailed Findings

### Pillar 1: Copywriting (3/4)

- Parity: visible labels are identical between main and branch across the 15 pairs (tab titles, slot headers, row metadata, bottom navigation, venue CTA "Go to").
- `AgendaRow.kt:175` `contentDescription = "favorite"`: hardcoded English, no state (bookmarked vs not), and not localized for a French-locale TalkBack user. Pre-existing (main:171); the line sits in a file Phase 07 modified, so it is in scope to fix. Suggested: `stringResource` with "Add to favorites" / "Remove from favorites" bound to `isBookmarked`.
- Speakers: no empty-state copy. A loaded empty list renders a blank surface (Speakers.kt has no EmptyLayout branch). Phase-independent, but Speakers was in the WR-04 change set.
- Empty/error copy elsewhere is resource-backed (EmptyLayout for agenda days with no slots). No error copy exists because no error state is reachable (Pillar 6).

### Pillar 2: Visuals (2/4)

- Focal point on agenda is clear: selected day tab underline, slot headers anchor the list (parity 01, 03).
- Venue (`15-venue.png`): blank band above the title (Top Fix 2). Hierarchy below it is clear (title, address, "Go to" pill, description truncated with ellipsis, no "Read more").
- Speakers (`14-speakers-scroller.png`, pre-WR capture): row header letters K/L/M sit in a clear left gutter; the A-Z letters at x≈865 of 891 displayed px touch the right edge, with "m" and "z" nearly clipped. Pre-existing (`AlphabetScroller` width 16.dp, flush, identical on main).
- Speakers A-Z letters are not exposed to accessibility (no semantics on `AlphabetScroller`), so the scroller is drag-only for assistive tech users. Pre-existing.
- Icon-only controls in the top bar (filter, settings) follow the Material icon set; labels not verified in phase-touched code.

### Pillar 3: Color (3/4)

- No `Color(0x...)`, `#RRGGBB` or `rgb(` literals in phase-touched agenda, speakers or venue source. Colors come from `MaterialTheme` roles.
- Accent (blue, primary) is used on a small number of elements per screen: session titles, selected tab indicator, "Go to" button, selected bottom-nav pill, speaker scroller bubble (`ScrollingBubble` uses `primary`).
- Bookmarked state tint (03-day2-top, 11-home-filtered-indicator) is olive green, a second accent hue. v1.0 behavior; record in theme if intentional.
- Surface levels (background, card, bottom bar) read as close dark levels. No contrast failure observed in captures, but no measured contrast ratios were taken.

### Pillar 4: Typography (3/4)

- Phase-touched UI uses theme roles (titleLarge, titleMedium, titleSmall, headlineLarge, headlineMedium, bodyLarge, bodyMedium, labelMedium, subtitle2, h5), roughly 9 roles across the three screens.
- Literal overrides: `fontSize = 14.sp` and `fontSize = 16.sp` (one each) and one `FontWeight.Bold`. These bypass the type scale. Pre-existing v1.0 unless they appear in the phase diff; treat as polish.
- `SpeakersList` row name uses `titleLarge` and the row header uses `bodyLarge`; the 48 dp header gutter gives a clear two-level hierarchy.

### Pillar 5: Spacing (3/4)

- Dominant values are 8, 12 and 16 dp; 4 dp and 24 dp appear a few times. No bracket arbitrary values added by the phase.
- Larger literals are component sizes: 96 dp speaker bubble, 48 dp row gutter, 32 dp speaker picture, 200 dp venue hero (Top Fix 2), 16 dp A-Z scroller column.
- The A-Z scroller has no end inset (flush to the right edge, see Pillar 2). A 16 dp end inset would match the rest of the 16 dp horizontal rhythm. Pre-existing.

### Pillar 6: Experience Design (2/4)

**State coverage, verified in code (corrects the previous review):**

| Screen | Loading | Error | Empty |
|--------|---------|-------|-------|
| Agenda | `LoadingLayout` while STARTING (AgendaPager.kt:84) | Not reachable: `UiState.ERROR` never assigned | `EmptyLayout` when a day has no slots (AgendaPager.kt:89) |
| Speakers | `LoadingLayout` while STARTING (Speakers.kt) | Not reachable: SpeakersViewModel only sets SUCCESS | None: blank surface for an empty list |
| Venue | `LoadingLayout` while LOADING or venue null (Venue.kt:53) | Not reachable: no ERROR assignment; spinner forever | Not applicable |

- The previous review's claim of "error paths in SpeakersViewModel and the agenda ViewModel" is incorrect. Neither ViewModel has an error path.
- Cold start: 12.4 s (branch) and 13.7 s (main) in CAPTURE-NOTES, under host memory pressure. Both are v1.0-level; there is no skeleton or progress cue during the wait.
- Parity verified behavior: filters, favorites filter, drawer, session detail, non-navigable rows, bookmark persistence after force-stop and relaunch (release/01 to 03). Release logcat showed zero SerializationException or ClassNotFoundException lines.

**Phase-caused behavior changes in scope (both are documented in the fix reports):**

- **WR-01 `ı`/`i` header loss** (Top Fix 3). Phase-caused, latent, not in the recorded data.
- **WR-04 empty-name report** (D-20): an empty speaker name is now skipped and reported (Timber to Crashlytics) instead of crashing with `NoSuchElementException`. Intended and accepted, not visible in the UI (the row is rendered without a header).
- **WR-04 state split** (Speakers.kt, SpeakersViewModel.kt): `speakers` and `letterIndexes` are now two separate StateFlows set back-to-back in the same collector. Compose reads both in the same frame in practice, so no visible glitch is expected, but a stale-pairing frame is theoretically possible. Low risk; do not treat as a defect without evidence.
- **Speakers visual parity after WR-01/WR-04: unverified.** Static analysis: for ASCII names, `speakerLetterKey` equals the v1.0 key (`name.lowercase().first()`), the header check is unchanged in outcome, and first-index-wins is preserved, so the rendered headers should match v1.0. Requires a re-capture to confirm.
- **AgendaRow.kt `isTalk`** (phase change): the bookmark gate moved from `session.type?.isService()` to a shared classification passed in as a parameter. Parity captures (01, 03, 13) show the same bookmark placement; the 13-non-navigable-row capture matches main.

**Registry audit:** not applicable (no components.json / shadcn in the Android project).

---

## Pre-existing v1.0 issues (not phase regressions)

- "favorite" hardcoded English contentDescription (AgendaRow.kt:175; main:171).
- Venue hero blank band (15-venue.png), identical on main.
- Speakers A-Z scroller flush to the right edge; no semantics on the scroller letters.
- Speakers has no empty state.
- Error/retry state missing in agenda, speakers and venue (UiState.ERROR never assigned).
- Cold start 12 to 14 s under host memory pressure, measured on both main and branch.
- Olive bookmark tint as a second accent hue.

## Phase 07 changes checked for regressions

- Agenda rendering moved to `DaySchedule` / `TimeSlot` from the shared domain: slot order, label text, room order and scroll index match main in 01, 03 to 13 (parity captures).
- Slots without a label are dropped in AgendaColumn (`formatSlotTime` returns empty when no system locale); v1.0 also dropped them.
- AgendaRow bookmark gate now uses `isTalk` (see Pillar 6): no visible change in captures.
- Speakers A-Z scroller: parity captured before WR-01/WR-04. **Re-capture required.**
- Venue: parity captured (15), identical to main; Phase 07 changed VenueViewModel only by 7 lines (shared language use case); no visual change claimed.

---

## Recommendations (minor, not in top 3)

- Localize the bookmark `contentDescription` and include the state (AgendaRow.kt:175).
- Add an end inset (about 16 dp) to the Speakers A-Z column and expose the letters with semantics.
- Add an empty state to Speakers (EmptyLayout or equivalent copy).
- Consolidate the literal `fontSize` overrides (14.sp, 16.sp) into the type scale.
- Re-capture `14-speakers-scroller` on the current branch head to close the WR-01/WR-04 evidence gap.

---

## Files Audited

- `.planning/phases/07-kmp-domain-layer-and-android-adoption/07-CONTEXT.md`
- `.planning/phases/07-kmp-domain-layer-and-android-adoption/07-REVIEW-FIX.md`
- `.planning/phases/07-kmp-domain-layer-and-android-adoption/07-UI-REVIEW.md` (previous, replaced)
- `.planning/phases/07-kmp-domain-layer-and-android-adoption/07-parity/CAPTURE-NOTES.md`
- `.planning/phases/07-kmp-domain-layer-and-android-adoption/07-parity/branch/14-speakers-scroller.png`, `15-venue.png` (viewed)
- 19 `07-NN-SUMMARY.md` files (read via `cat`, not individually re-verified)
- `feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/Speakers.kt`
- `feature/speakers/src/main/java/com/gdgnantes/devfest/feature/speakers/list/SpeakersViewModel.kt`
- `core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/speakers/SpeakerIndexUseCase.kt`
- `core/ui/.../UiState.kt`
- `feature/agenda/src/main/java/com/gdgnantes/devfest/feature/agenda/` (AgendaViewModel.kt, AgendaPager.kt, AgendaColumn.kt, AgendaRow.kt)
- `feature/venue/src/main/java/com/gdgnantes/devfest/feature/venue/` (VenueDetails.kt, Venue.kt, VenueViewModel.kt)
- `git diff main` for feature/speakers, feature/agenda (AgendaRow.kt, AgendaColumn.kt), feature/venue
