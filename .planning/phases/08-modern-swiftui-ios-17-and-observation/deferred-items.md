# Phase 8: Deferred Items

Items found or consciously left in place during Phase 8. None of them blocks the phase.

## D-12: iOS-17-deprecated SwiftUI APIs left in place (Phase 12 debt)

Phase 8 changes the observation model only. The SwiftUI APIs below are deprecated at the iOS 17
deployment target but still work, so they are kept as they are to avoid mixing a visual or navigation
change into the Observation migration. Phase 12 (SwiftUI modernization) replaces them. Locations come
from a grep of `iosApp/iosApp` after plan 08-08 Task 1 (generated `SwiftGen/` output excluded).

| Deprecated API | Replacement (Phase 12) | Locations |
|----------------|------------------------|-----------|
| `navigationBarTitle(_:)` | `navigationTitle(_:)` plus `navigationBarTitleDisplayMode(_:)` | `Agenda/AgendaView.swift:55`, `Agenda/AgendaDetailView.swift:128` |
| `navigationBarItems(trailing:)` | `toolbar { ToolbarItem(placement: .topBarTrailing) { ... } }` | `Agenda/AgendaView.swift:56` (this is why `AgendaView` re-injects the shared `AgendaViewModel` into the bar items explicitly) |
| `accentColor(_:)` | `tint(_:)` | `UI/ContentView.swift:42` |
| `foregroundColor(_:)` | `foregroundStyle(_:)` | `Agenda/AgendaView.swift` (315, 345), `Agenda/AgendaCellView.swift:23`, `Agenda/AgendaDetailView.swift` (49, 90, 100, 112, 140), `UI/About/AboutView.swift` (30, 36, 48, 53, 62, 71, 80, 94, 151, 166, 177, 179), `UI/Venue/VenueView.swift` (53, 68), `UI/Speakers/SpeakerDetailsView.swift` (66, 73), `UI/Common/LoadingView.swift:31`, `UI/Common/SocialIcon.swift:34` |
| One-parameter `onChange(of:perform:)` | the two-parameter or zero-parameter `onChange(of:)` closure | `Agenda/AgendaView.swift` (45, 52) |
| `NavigationView` (and `navigationViewStyle(StackNavigationViewStyle())`) | `NavigationStack` | `UI/Speakers/SpeakersView.swift:20`, `UI/About/AboutView.swift` (16, and the style at 197), `UI/Venue/VenueView.swift:16` |
| `Locale.current.languageCode` | `Locale.current.language.languageCode?.identifier` | `UI/Venue/VenueViewModel.swift:27` (the characterization test `SpeakersVenueCharacterizationTests.swift:42` mirrors it on purpose) |

Notes:
- `onChange` in `AgendaViewModelTests.swift` is the `withObservationTracking(_:onChange:)` parameter, not the
  deprecated modifier.
- `accentColor` in `SwiftGen/Assets.swift` is a generated asset name, not the modifier.
- The `SwiftUI` deprecations do not produce build errors; the swift-observation gate does not cover them.

## Stale Swift-names header baseline

`scripts/swift-names/shared-h-baseline.txt` has been stale since Phase 7: regenerating it adds 223
`core:domain` names (for example `DaySchedule`, `FilterSessionsUseCase`) that were exported to Swift after
the baseline was recorded. `swift-names-gate.sh check` still ends with `SWIFT-NAMES-OK` (it prints a diff
section for that file only). Found in plan 08-02, confirmed in 08-06 and 08-07. Not regenerated in
Phase 8; AGENTS.md allows a baseline regeneration only in the same commit as the matching Swift call-site
update, so it belongs to the first plan that touches a Swift call site of a `core:domain` export (Phase 9).

## From 08-09

Found during the iOS parity pass (plan 08-09). Neither item is phase-caused: both behave identically on the
pre-phase baseline build and on the branch build.

### Shifted-clock initial scroll check (RESEARCH A4) could not be done by hand

D-12 removed the scroll hop from the initial agenda scroll, and the plan asked the human to verify the
result with the Mac clock set to 2025-10-16 12:30 (event day). On iOS this cannot be done by hand: the
simulators use the host clock, and a shifted host clock makes every TLS request fail ("certificate is not
yet valid"), so the app loaded nothing on either build (see the next item). Recommendation: cover the
initial scroll with a unit test that injects a clock into `AgendaViewModel` (the initial scroll index for
an event-day "now") in a later phase, instead of a manual shifted-clock pass.

### Pre-existing v1.0 behavior: no cached agenda shown when the network call fails

With the network failing (here TLS validation, but the same applies to any failed request), the iOS agenda
shows no previously cached data, even though the app had loaded the agenda before. This is the same on the
baseline build (v1.0 behavior), so per strict parity it was not fixed in Phase 8. Candidate for a later
phase: fall back to the Apollo normalized cache when the network fetch errors.
