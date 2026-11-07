# Phase 9 — UI Review

**Audited:** 2026-11-07
**Baseline:** 09-UI-SPEC.md (approved 2026-11-05, strict v1.0 parity, parity exceptions accepted) plus the accepted deviations in 09-CONTEXT.md (room order, 07 D-15 silent reset, four edge-case deviations, the 2026-11-07 layout offsets)
**Screenshots:** not captured (native SwiftUI app, no dev server). The existing parity pairs under `09-parity/base/` and `09-parity/branch/` were NOT re-inspected in this run: direct reads of the PNG paths failed and the shell was unavailable, so the visual pillars rest on code analysis plus the screenshot findings recorded in the phase artifacts.
**Interaction captures:** off (`interaction_capture: false`; also not applicable to a native iOS app)

**Audit limits:** The Bash tool was blocked by an auto-mode hook for this run. Repo-wide greps (hardcoded colors, accessibility labels, `font(` and `padding(` census across `iosApp/iosApp/`) could not be run. Findings below cover the 16 Swift files actually read, so pillar scores are an evidence-limited estimate, not a full-tree census.

---

## Pillar Scores

| Pillar | Score | Key Finding |
|--------|-------|-------------|
| 1. Copywriting | 3/4 | Spec's string inventory is honored (L10n keys reused), but one hard-coded English accessibility label ("What is the day?") ships in the French build |
| 2. Visuals | 3/4 | Hierarchy follows the frozen spec (red title, secondary meta, yellow star and chip); no focal point declared; icon-only controls carry no visual text |
| 3. Color | 3/4 | Colors come from `Asset` tokens in every read file; no hex literals found in read files; accent use matches the 60/30/10 reservation list |
| 4. Typography | 3/4 | Dynamic Type text styles used for all text; three fixed-size glyphs (`size: 12`, `16`, `15`) do not scale at accessibility sizes |
| 5. Spacing | 3/4 | Values sit on the frozen scale plus the declared exceptions; `CategoryView` uses 5pt (fallback) and the detail uses 10pt stacks, neither in the exception list |
| 6. Experience Design | 2/4 | Loading, error and cancellation paths are covered; icon-only bookmark stars have no accessibility label; the bookmark-toggle scroll offset is an accepted but real UX regression |

**Overall: 17/24**

---

## Top 3 Priority Fixes

1. **Bookmark stars have no accessibility label (row, detail toolbar, and the filter toolbar icon)** — VoiceOver reads the SF Symbol name ("star fill") with no action context, so a screen-reader user cannot reliably bookmark or understand the filter menu — **Fix:** add `.accessibilityLabel` and `.accessibilityAddTraits` to the star `Button`s in `AgendaView.swift` (`SessionRow`) and `AgendaDetailView.swift` (toolbar), and to the filter `Menu` in `AgendaNavigationBarItems.swift`, using localized strings. Note: this adds strings, which the spec's Copywriting Contract forbids (zero new strings), so it needs an explicit user approval of a copy-contract change before it lands.

2. **Hard-coded English VoiceOver label on the day picker** — `AgendaView.swift:74` `Picker("What is the day?", selection: $day)` is a literal in Swift, not an `L10n.*` key, so a French-locale VoiceOver user hears English — **Fix:** replace the literal with an existing localized key if one fits, or add a key to `Localizable.strings` (en and fr) in the same change. Spec says "the executor reuses `L10n.*` and must not hand-write literals in Swift"; this line violates that rule, and it is a parity-neutral fix since the segmented picker hides its title visually.

3. **Scroll position jumps about 35 pt on return after a bookmark change** — accepted by the user on 2026-11-07 (pairs 06, 07, 22), but the cause is unconfirmed and it is a visible regression in a list the spec wants to keep unchanged — **Fix (hypothesis to verify, not confirmed):** in `AgendaViewModel.apply(_:)` skip the `agendaContent` assignment when the new value equals the current one, so a re-emission that changes nothing does not rebuild the `List`. Confirm with a before/after simulator pair on pairs 06/22 before accepting it as closed.

---

## Detailed Findings

### Pillar 1: Copywriting (3/4)

**Met:**
- Filter menu labels and order follow the spec: `FavoritesFilterMenu.swift:11` uses `L10n.filterFavorites`; `RoomFilterMenu.swift:20` uses `L10n.sessionFiltersDrawerRoomsLabel`; `ClearFiltersButton.swift:11` uses `L10n.filterClear`.
- Loading copy uses `L10n.loading` (`LoadingView.swift:25`).
- Day labels use `L10n.day1` / `L10n.day2` (`AgendaView.swift:75-76`).
- No new empty-state or error copy was added, as the spec requires.

**Gaps:**
- **WARNING** `AgendaView.swift:74`: `Picker("What is the day?", ...)` is a Swift literal. Violates the spec's "must not hand-write literals in Swift" rule and surfaces English to French VoiceOver users.
- **WARNING (accepted debt, logged)** `AgendaContent.swift:79-80` `AgendaDays.day1 = "2025-10-16"` are hardcoded dates. Known single-source debt; not a copy issue but worth tracking.

### Pillar 2: Visuals (3/4)

**Met:**
- Clear hierarchy in `SessionRow` (`AgendaView.swift:268-288`): headline red title, footnote secondary metadata, category chip, room and speaker lines. Matches the spec's Typography and Color reservations.
- Detail title is the largest element (`AgendaDetailView.swift:50`, `.title2.weight(.bold)`), and the speaker heading is styled consistently.
- Icon-only star is paired with a semantic filled/unfilled state (`star` / `star.fill`), which is a visual cue for state.

**Gaps:**
- **WARNING** No focal point declared for the agenda screen. Spec lists this as an open recommendation; the day picker and list compete at the top of the screen.
- **WARNING** Icon-only controls (star in row, star in detail toolbar, filter icon) have no text or label. Visual affordance depends entirely on the glyph (see Pillar 6).

### Pillar 3: Color (3/4)

**Met:**
- All colors in the read files come from `Asset.devFestRed`, `Asset.devFestYellow`, and system styles (`.secondary`, `.primary`). No hardcoded hex or `Color(red:...)` appears in `AgendaView.swift`, `AgendaDetailView.swift`, `AgendaContent.swift`, `CategoryView.swift`, `LoadingView.swift`, the filter menus, or `AgendaSource.swift`.
- Accent reservations hold: red on the session title and detail "Speaker" heading; yellow on the star and the `CategoryView` chip only.
- Filter selection uses `checkmark.square` / `star.fill` glyphs, not a new color (spec compliant).

**Gaps:**
- **WARNING** `AgendaDetailView.swift:100` uses `Color(UIColor.placeholderText)` for the "powered by OpenFeedback" caption. Not listed in the spec's color table, so the palette is wider than declared. Low impact.
- **WARNING** Yellow chip uses `.opacity(0.5)` fill over glass (`CategoryView.swift:25`). Contrast of the chip label on that fill was not measured.

### Pillar 4: Typography (3/4)

**Met:**
- Text uses Dynamic Type text styles throughout the read files: `.footnote`, `.footnote.weight(.semibold)`, `.headline`, `.title2.weight(.bold)`, `.subheadline.weight(.semibold)`, `.body`, `.callout`.
- Weight set matches the spec's inherited exceptions (regular, semibold, bold).

**Gaps:**
- **WARNING** Fixed-size glyphs that do not scale with Dynamic Type: `CategoryView.swift:19,36` `.font(.system(size: 12))` (declared in the spec as the one fixed label); `AgendaView.swift:300` star `.font(.system(size: 16, weight: .semibold))` (declared, "keeps its 16pt symbol"); `AgendaView.swift:308` chevron `.font(.system(size: 15, weight: .semibold))` (not declared in the spec's Typography table). At accessibility sizes (screenshots 26 to 30 in the parity set) these glyphs and chips will not grow with the surrounding text.

### Pillar 5: Spacing (3/4)

**Met:**
- Row, list and header spacing use the frozen values: `.padding(.vertical, 4)` on rows and headers, `.padding(.horizontal, 12)` on the picker, `.padding(.horizontal, 8)` on the time header, `spacing: 12` on the list stack, `spacing: 6` in `SessionRow`, `.padding(.trailing, 4)` on the star, `listRowInsets` 4/16.
- No arbitrary `[...]` spacing values in the read files.

**Gaps:**
- **WARNING** `CategoryView.swift:21,38-39` uses 5pt vertical padding on the iOS <26 fallback and 6pt/10pt/8pt on the glass path. 5pt is not in the spec's exception list (2, 6, 10, 12, 14). The 6pt and 10pt values are declared exceptions.
- **WARNING** `AgendaDetailView.swift:47,58,77` uses `spacing: 10` stacks inside cards. 10pt is declared only for `GlassRowContainer` vertical padding, so these are undeclared instances. They are likely inherited v1.0 values; the phase had no git diff available in this run to prove it.

### Pillar 6: Experience Design (2/4)

**Met:**
- Loading: `LoadingView` covers first load; `isLoading` only goes true to false (`AgendaViewModel.swift:97-143`), so resubscribe shows no spinner flash (D-03).
- Error paths: failures are logged through `DevFestLogger` and `isLoading` is cleared; cancellation is silent (`AgendaViewModel.swift:126-137`).
- Destructive action: "Clear filters" is immediate, as the spec accepts; the button only renders when a filter is active (`ClearFiltersButton.swift:7`).
- Disabled state: content is `.disabled` and blurred while loading (`LoadingView.swift:22-23`).

**Gaps:**
- **WARNING (highest-impact gap)** No accessibility label on the bookmark stars (`AgendaView.swift:295-302`, `AgendaDetailView.swift:131-141`) or the filter toolbar `Menu` (`AgendaNavigationBarItems.swift:6`). Grep of the read files: zero `accessibilityLabel` or `accessibilityHint` occurrences. Spec lists this as an open recommendation; it is a VoiceOver task-completion gap, not just polish.
- **WARNING** Bookmark toggle causes a ~35 pt scroll offset on return from detail (accepted by the user on 2026-11-07; cause unconfirmed). This is a real UX regression against the parity intent, recorded as accepted and not re-litigated.
- **WARNING** Empty state: a day or filter with no sessions renders a blank list with no message (accepted parity debt in the spec).
- **WARNING** Error after first emission keeps the last content and stops with no retry (accepted, D-04). Acceptable under the contract but leaves stale data silently.
- **Note** Copy for the star is absent, so the "bookmarked" state is conveyed only by the glyph; combined with the missing label this is the main discoverability risk.

---

## Registry Safety

Not applicable: `shadcn_initialized: false`, no `components.json`, and no third-party registries in the UI-SPEC Registry Safety table. Registry audit skipped.

---

## Files Audited

- `.planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-UI-SPEC.md`
- `.planning/phases/09-ios-on-shared-logic-and-legacy-persistence/09-CONTEXT.md`
- `iosApp/iosApp/Agenda/AgendaViewModel.swift`
- `iosApp/iosApp/Agenda/AgendaView.swift`
- `iosApp/iosApp/Agenda/AgendaDetailView.swift`
- `iosApp/iosApp/Agenda/AgendaSource.swift`
- `iosApp/iosApp/Agenda/AgendaNavigationBarItems.swift`
- `iosApp/iosApp/Agenda/RoomFilterMenu.swift`
- `iosApp/iosApp/Agenda/FavoritesFilterMenu.swift`
- `iosApp/iosApp/Agenda/ClearFiltersButton.swift`
- `iosApp/iosApp/Model/Data/AgendaContent.swift`
- `iosApp/iosApp/UI/Common/CategoryView.swift`
- `iosApp/iosApp/UI/Common/LoadingView.swift`

Not read in this run: Language, Complexity and SessionType filter menus, `GlassRowContainer`, `CardView`, Speakers and Venue views, `ContentView`, the `Localizable.strings` files, and the parity screenshot PNGs.
