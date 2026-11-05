---
phase: 08-modern-swiftui-ios-17-and-observation
reviewed: 2026-11-05T00:00:00Z
depth: standard
files_reviewed: 53
files_reviewed_list:
  - .github/workflows/ios.yml
  - AGENTS.md
  - CONTRIBUTING.md
  - README.md
  - documentation/ARCHITECTURE.md
  - documentation/DEVELOPMENT.md
  - iosApp/DevFestNantesTests/AboutViewModelTests.swift
  - iosApp/DevFestNantesTests/AgendaViewModelTests.swift
  - iosApp/DevFestNantesTests/Fakes/FakeAboutSource.swift
  - iosApp/DevFestNantesTests/Fakes/FakeAgendaSource.swift
  - iosApp/DevFestNantesTests/Fakes/FakeCall.swift
  - iosApp/DevFestNantesTests/Fakes/FakeSpeakerDetailsSource.swift
  - iosApp/DevFestNantesTests/Fakes/FakeSpeakersSource.swift
  - iosApp/DevFestNantesTests/Fakes/FakeStream.swift
  - iosApp/DevFestNantesTests/Fakes/FakeVenueSource.swift
  - iosApp/DevFestNantesTests/Fakes/RecordingDataLoadTracing.swift
  - iosApp/DevFestNantesTests/SpeakerDetailsViewModelTests.swift
  - iosApp/DevFestNantesTests/SpeakersViewModelTests.swift
  - iosApp/DevFestNantesTests/VenueViewModelTests.swift
  - iosApp/iosApp.xcodeproj/project.pbxproj
  - iosApp/iosApp/Agenda/AgendaCellView.swift
  - iosApp/iosApp/Agenda/AgendaDetailView.swift
  - iosApp/iosApp/Agenda/AgendaNavigationBarItems.swift
  - iosApp/iosApp/Agenda/AgendaSource.swift
  - iosApp/iosApp/Agenda/AgendaView.swift
  - iosApp/iosApp/Agenda/AgendaViewModel.swift
  - iosApp/iosApp/Agenda/ClearFiltersButton.swift
  - iosApp/iosApp/Agenda/ComplexityFilterMenu.swift
  - iosApp/iosApp/Agenda/FavoritesFilterMenu.swift
  - iosApp/iosApp/Agenda/LanguageFilterMenu.swift
  - iosApp/iosApp/Agenda/RoomFilterMenu.swift
  - iosApp/iosApp/Agenda/SessionTypeFilterMenu.swift
  - iosApp/iosApp/Model/KotlinStreamBridge.swift
  - iosApp/iosApp/Performance/DataLoadTracing.swift
  - iosApp/iosApp/Performance/PerformanceMonitoring.swift
  - iosApp/iosApp/UI/About/AboutSource.swift
  - iosApp/iosApp/UI/About/AboutView.swift
  - iosApp/iosApp/UI/About/AboutViewModel.swift
  - iosApp/iosApp/UI/ContentView.swift
  - iosApp/iosApp/UI/Speakers/SpeakerDetailsSource.swift
  - iosApp/iosApp/UI/Speakers/SpeakerDetailsView.swift
  - iosApp/iosApp/UI/Speakers/SpeakerDetailsViewModel.swift
  - iosApp/iosApp/UI/Speakers/SpeakersSource.swift
  - iosApp/iosApp/UI/Speakers/SpeakersView.swift
  - iosApp/iosApp/UI/Speakers/SpeakersViewModel.swift
  - iosApp/iosApp/UI/Venue/VenueSource.swift
  - iosApp/iosApp/UI/Venue/VenueView.swift
  - iosApp/iosApp/UI/Venue/VenueViewModel.swift
  - iosApp/iosApp/iOSApp.swift
  - scripts/README.md
  - scripts/swift-names/members-baseline.txt
  - scripts/swift-names/referenced-names.txt
  - scripts/swift-observation-gate.sh
findings:
  critical: 0
  warning: 5
  info: 8
  total: 13
status: issues_found
---

# Phase 8: Code Review Report

**Reviewed:** 2026-11-05
**Depth:** standard
**Files Reviewed:** 53
**Status:** issues_found

## Summary

The Observation migration is structurally sound. I traced the following and found no defects in them:

- The `KotlinStreamBridge` generic signature matches the real `NativeFlow` typealias in the checked-out KMP-NativeCoroutines package (`onItem` carries `(Output, () -> Unit, Unit)`, plus the `onComplete` and `onCancelled` callbacks).
- The `@ObservationIgnored` / `didSet` usage is consistent.
- The `pbxproj` edits are complete: no dangling references to `BaseViewModel` or `KMPNativeCoroutinesCombine`, and every new file has its four entries.
- The deployment target is 17.0 everywhere.
- `swift-observation-gate.sh check` and `selftest` both pass locally.
- The `swift-names` baselines are consistent with the new `DevFestNantesStoreBuilder` references.
- No unstructured `Task {}` remains in the app sources.

No blocker was found. The findings are:

- **Cancellation handling:** an upstream (Kotlin-side) cancellation is indistinguishable from view-task cancellation, so a load can leave the blocking loading overlay up forever (WR-01).
- **Re-appearance behaviour:** `.task` restarts and `.onAppear` resets, now more visible because `.task` re-runs loads (WR-02).
- **Crash risks:** force unwraps on remote-data URLs (WR-03).
- **Broken link:** a pre-existing broken feedback link in the Speaker -> Session path (WR-04).
- **Latent date bugs:** hard-coded event dates and the `yyyy-MM-d` section key (WR-05).

The remaining items are quality notes.

## Warnings

### WR-01: Upstream (Kotlin-side) cancellation is swallowed as a view cancellation and leaves the blocking spinner up forever

**File:** `iosApp/iosApp/Model/KotlinStreamBridge.swift:45-49`
Also: `iosApp/iosApp/Agenda/AgendaViewModel.swift:105-106,137-138`, `iosApp/iosApp/UI/Speakers/SpeakersViewModel.swift:51-52`, `iosApp/iosApp/UI/About/AboutViewModel.swift:49-50`, `iosApp/iosApp/UI/Venue/VenueViewModel.swift:44-45`, `iosApp/iosApp/UI/Speakers/SpeakerDetailsViewModel.swift:52-53,67-68`

**Issue:** The bridge maps the native `onCancelled` callback to `continuation.finish(throwing: CancellationError())` ("stays silent for the consumer"). `asyncFunction` from KMP-NativeCoroutines does the same for one-shot calls. Every ViewModel handles this with `catch is CancellationError { return }`, which does not touch `isLoading`.

That is correct when the SwiftUI `.task` was cancelled, because the view is going away or will re-run. It is wrong when the Kotlin side cancels while the Swift task is still alive. Examples are a store or scope torn down, or a Kotlin `CancellationException` surfacing from the flow. The `CancellationError` is then swallowed and `isLoading` stays `true`. `LoadingView` applies `.disabled(isShowing)` plus a blur to the whole content, so the screen stays blurred and non-interactive until the view reappears.

The tests only pin the task-cancel case (`cancelled_*_keeps_the_spinner`), so this path is untested.

**Fix:** Only treat the error as silent when the Swift task is really cancelled, and apply the same shape in every ViewModel:
```swift
} catch is CancellationError {
    if Task.isCancelled { return }   // view went away: stay silent
    // upstream cancelled while the view is alive: surface as a failure
    DevFestLogger(category: "Agenda").log(.error, "Sessions upstream cancelled")
    isLoading = false
}
```
Alternatively, have the bridge finish with a distinct `KotlinStreamError.upstreamCancelled` error instead of `CancellationError` when `Task.isCancelled` is false at callback time. Add a `FakeStream` script that invokes `onCancelled` and a matching test.

### WR-02: Returning to the Agenda tab resets the selected day and refetches everything

**File:** `iosApp/iosApp/Agenda/AgendaView.swift:65-68, 221-236` (and `.task` at 57-63)

**Issue:** `.onAppear { prepareInitialDayAndScroll() }` runs on every appearance of the `NavigationStack` (pop from `AgendaDetailView`, tab switch back). `prepareInitialDayAndScroll` unconditionally writes `day`, either to today's day or to the hard-coded `"2025-10-16"`, so a user on day 2 who opens a session and goes back is snapped to day 1.

The state reset is old behaviour. This phase makes it more visible: both `.task` blocks restart on each reappearance (SwiftUI cancels them on disappear). Each reappearance therefore re-subscribes rooms, calls `RCValues.fetchCloudValues()` and re-runs `observeSessions()`, replacing `agendaContent`.

Pairing the unconditional reset with a refetch that rebuilds the list is a visible UX regression risk. I did not run this on a device; the appearance behaviour of a `NavigationStack` root is the assumption to verify.

**Fix:** Guard the initial-day logic with the existing `firstAppear` flag, so it only runs once:
```swift
.onAppear {
    if firstAppear { prepareInitialDayAndScroll() }
    FirebaseAnalyticsService.shared.pageEvent(...)
}
```
Consider skipping the sessions refetch in `observeSessions()` when `agendaContent.sections` is already populated (or keep the load in a `.task(id:)` keyed to a one-shot value).

### WR-03: Force-unwrapped URLs built from remote data (crash on malformed value)

**File:** `iosApp/iosApp/UI/About/AboutView.swift:106`; `iosApp/iosApp/Agenda/AgendaDetailView.swift:87`

**Issue:**
- `UIApplication.shared.open(URL(string: partnerUrl)!)` force-unwraps a partner URL received from the backend. A malformed or space-containing value crashes the app on tap. Note that line 112 already handles `logoUrl` safely with `if let`.
- `URL(string: "\(WebLinks.openFeedback.url)/\(day)/\(session.id)")!` is evaluated during view body rendering. An unexpected character in `session.id` crashes when the detail screen is shown, not just on tap.
- The same line 106 passes `partnerUrl` as `partnerName:` to `eventLinkPartnerOpened`, so analytics receives the URL instead of the partner name. `partner.name` is available.

**Fix:**
```swift
if let partnerUrl = partner.url, let url = URL(string: partnerUrl) {
    Button(action: {
        UIApplication.shared.open(url)
        FirebaseAnalyticsService.shared.eventLinkPartnerOpened(partnerName: partner.name)
    }) { ... }
}
```
For the feedback link, build the URL with `URL(string:)` plus an `if let` (or percent-encode `session.id`) and hide the button when it is nil.

### WR-04: Sessions opened from Speaker details get a broken OpenFeedback link (`day: "day"` literal)

**File:** `iosApp/iosApp/UI/Speakers/SpeakerDetailsView.swift:71` (consumer at `iosApp/iosApp/Agenda/AgendaDetailView.swift:87`)

**Issue:** `AgendaDetailView(session: ..., day: "day")` passes the literal string `"day"`. The feedback URL then becomes `<openFeedback>/day/<sessionId>` instead of `<openFeedback>/2025-10-16/<sessionId>`, as built from `AgendaView`. Feedback for a talk reached through Speakers -> speaker -> session therefore points to a non-existent OpenFeedback page. This predates the phase, but the file is in scope and the Observation work touched this exact call site.

**Fix:** Stop passing `day` and derive it inside `AgendaDetailView` from the session's own `startDate` (for example the first 10 characters of the ISO date, or `Calendar` formatted `yyyy-MM-dd` in the event timezone). That removes the inconsistent parameter entirely.

### WR-05: Hard-coded event dates and a lossy `yyyy-MM-d` section key

**File:** `iosApp/iosApp/Agenda/AgendaView.swift:23, 75-76, 229-235`; `iosApp/iosApp/Agenda/AgendaViewModel.swift:156-160`

**Issue:**
- The literals `"2025-10-16"` and `"2025-10-17"` are repeated five times (initial `day`, the two picker tags, and the `currentDay ==` checks). When the event dates change, `sections(_:forDay:)` filters every section out and the agenda is empty.
- `sectionDateFormatter` uses `"yyyy-MM-d"`, which yields `2025-10-6` for the 6th. `sections.filter { $0.day.contains(day) }` then fails to match `"2025-10-06"`. It only works today because both days are >= 10.
- The formatter also uses the device time zone, so a user abroad gets different day buckets. The characterization tests pin this behaviour, which is why it needs a deliberate change, not a silent one.

**Fix:** Derive the available days from `viewModel.agendaContent.sections` (or a single `DevFestEvent.days` constant). Use `"yyyy-MM-dd"` with `TimeZone(identifier: "Europe/Paris")` and compare for equality instead of `contains`. Update the characterization goldens in the same commit.

## Info

### IN-01: Deprecated APIs remain after moving to the iOS 17 target

**File:** `iosApp/iosApp/Agenda/AgendaView.swift:45,52,55,56`; `iosApp/iosApp/UI/Speakers/SpeakersView.swift:20,164`; `iosApp/iosApp/UI/About/AboutView.swift:16,197`; `iosApp/iosApp/UI/Venue/VenueView.swift:16,112`; `iosApp/iosApp/UI/Venue/VenueViewModel.swift:27`; `iosApp/iosApp/UI/ContentView.swift:42`

**Issue:** The phase title is "Modern SwiftUI iOS 17" and the deployment target is now 17.0, but these deprecated calls remain:
- single-parameter `onChange(of:perform:)`;
- `NavigationView` / `navigationViewStyle`;
- `navigationBarTitle` / `navigationBarItems`;
- `NavigationLink(isActive:)`;
- `accentColor` / `foregroundColor`;
- `Locale.current.languageCode` (deprecated since iOS 16).

They compile with warnings.

**Fix:** Use `onChange(of:) { old, new in }`, `NavigationStack` with `navigationDestination`, `.navigationTitle` / `.toolbar`, `.tint` / `.foregroundStyle`, and `Locale.current.language.languageCode?.identifier`.

### IN-02: Duplicated filter-toggle and push-link code

**File:** `iosApp/iosApp/Agenda/FavoritesFilterMenu.swift:8-17`, `ComplexityFilterMenu.swift:31-39`, `LanguageFilterMenu.swift:22-30`, `RoomFilterMenu.swift:22-30`, `SessionTypeFilterMenu.swift:23-31`; `iosApp/iosApp/Agenda/AgendaView.swift:269-290` vs `iosApp/iosApp/UI/Speakers/SpeakersView.swift:148-170`

**Issue:** The same "copy set, insert or remove, assign back" block is copy-pasted in five menus. Two different private `PushLink` implementations share one name: `navigationDestination(isPresented:)` in Agenda and a hidden `NavigationLink(isActive:)` in Speakers.

**Fix:** Add `func toggleFilter(_ filter: SessionFilter)` to `AgendaViewModel` (testable, one place) and call it from the menus. Share one `PushLink`.

### IN-03: Dead or unused code

**File:** `iosApp/iosApp/UI/About/AboutViewModel.swift:17,38`; `iosApp/iosApp/iOSApp.swift:3`; `iosApp/iosApp/UI/Speakers/SpeakerDetailsView.swift:92-93`; `iosApp/iosApp/Agenda/AgendaView.swift:9-15`

**Issue:**
- `AboutViewModel.isLoading` is never read by `AboutView`, which has no `LoadingView`.
- The `!newContentArray.contains(...)` dedupe is pointless, because the keys come from a dictionary and are unique.
- `import KMPNativeCoroutinesCore` in `iOSApp.swift` is unused.
- `onSocialLinkClick` is an empty stub, so the social icons are dead taps.
- `AgendaView.swift` has a duplicated file header comment.

**Fix:** Remove the unused import, state and dedupe. Either implement or remove the social-link handler.

### IN-04: `speakerId` parameter shadows the stored property

**File:** `iosApp/iosApp/UI/Speakers/SpeakerDetailsViewModel.swift:38,60`

**Issue:** `getSpeaker(speakerId:)` and `getSpeakerSession(speakerId:)` take a parameter that shadows `self.speakerId`. This lets callers (and tests) load a different id than the one the ViewModel and view are bound to.

**Fix:** Make both methods `private` and read `self.speakerId`. `load()` is the only entry point.

### IN-05: Cancelled loads are still stopped and sent as Firebase traces; `trackNavigation` was not updated

**File:** `iosApp/iosApp/Performance/PerformanceMonitoring.swift:99-109, 126-`

**Issue:** The new cancellation branch calls `trace.stop()` with `success=false` and a truncated duration. Because loads now cancel whenever a tab or screen disappears mid-load, `agenda_load` and `speakers_load` get extra truncated data points that skew the duration metrics. `trackNavigation` (line 126 onward) keeps the old catch-all, so it still logs a cancellation as an error and forwards it to Crashlytics.

**Fix:** For cancellation, consider not recording the trace (Firebase offers no discard, so the alternative is to tag it distinctly, for example `outcome=cancelled`, and exclude it in dashboards). Mirror the `CancellationError` branch in `trackNavigation`.

### IN-06: `swift-observation-gate.sh` has detection gaps

**File:** `scripts/swift-observation-gate.sh:76`

**Issue:**
- The `DispatchQueue.main.async` rule matches within a single line only, so `DispatchQueue.main\n    .async { }` passes.
- `@StateObject`, `@ObservedObject` and `@EnvironmentObject` are not matched directly. They are caught only indirectly through `ObservableObject` conformance, which an externally defined type could satisfy.
- `scan_set` uses `git ls-files`, so a new, not-yet-`git add`ed file is not scanned in a local run.

**Fix:** Add `StateObject|ObservedObject|EnvironmentObject` to `SWIFT_RE` (plus a selftest fixture). Document or handle the untracked-file limitation (`git ls-files -co --exclude-standard`).

### IN-07: Test fakes mutate a stored `AsyncStream.Iterator` from an `@unchecked Sendable` class

**File:** `iosApp/DevFestNantesTests/Fakes/FakeCall.swift:23-31,42-44`; `iosApp/DevFestNantesTests/Fakes/FakeStream.swift:28-31,64-71`

**Issue:** `waitUntilCalled()`, `waitUntilSubscribed()` and `waitUntilTerminated()` mutate `var ...Iterator` stored properties across an `await`. Two concurrent waiters would hit an exclusivity violation or a race. The current tests only ever use a single waiter, so this is a latent fragility, not a failure. The nested `Suite` struct in `AgendaViewModelTests` also shadows Swift Testing's `Suite` name, which is confusing.

**Fix:** Document the single-waiter contract, or guard the iterator with a lock or an actor. Rename the nested helper (for example `DefaultsSuite`).

### IN-08: Immediate scroll after the loading flip (unverified timing change)

**File:** `iosApp/iosApp/Agenda/AgendaView.swift:214-216`

**Issue:** The `DispatchQueue.main.async` hop around `scrollToSectionID = previous.date` was removed to satisfy the observation gate. `handleInitialScrollIfNeeded` now runs inside `onChange(of: viewModel.isLoading)`, and `scrollToSectionID` is set in the same update that first renders the populated `List`. The deferral gave the rows a layout pass before `proxy.scrollTo`. It is plausible that the initial "scroll to the current slot" now sometimes misses.

I could not confirm this without running on device. It is worth a manual check on a conference day (or with a shifted clock, after prefetching data).

**Fix:** If it misses, trigger the scroll from `.task(id: scrollToSectionID)` or after `await Task.yield()`, instead of a main-queue hop.

---

_Reviewed: 2026-11-05_
_Reviewer: Claude (gsd-code-reviewer)_
_Depth: standard_
