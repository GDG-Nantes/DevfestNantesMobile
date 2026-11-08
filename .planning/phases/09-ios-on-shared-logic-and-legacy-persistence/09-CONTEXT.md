# Phase 9: iOS on Shared Logic and Legacy Persistence - Context

**Gathered:** 2026-11-05
**Status:** Ready for planning

<domain>
## Phase Boundary

The iOS app stops filtering, grouping and persisting bookmarks/filters in Swift. The agenda
continuously observes the shared `ObserveAgendaUseCase` (iOS passes `LocalDateDays` and
`TimeZone.currentSystemDefault()`), bookmarks and filters persist through the iOS
`UserDefaultsBookmarksStore` / `UserDefaultsSessionFiltersStore` (new keys, shared codec), speakers,
venue and session classification go through `SpeakerIndexUseCase`, `VenueLanguageUseCase` and
`ClassifySessionUseCase`, and the Swift `SessionFilter` / `FilterType` types are deleted
(IOSDATA-01/02/03). No user-visible change except the documented in-slot room order
(`RoomSortIndex`), the intentional loss of v1.0 bookmarks/filters (07 D-15) and the accepted edge-case
deviations listed under "Accepted deviations" below.

Out of this phase: explicit DI / `AppDependencies` (Phase 10), SPM packages (Phase 11), strict
Swift 6 and deprecated SwiftUI APIs (Phase 12), removing the legacy `UserDefaults` keys (deferred).

</domain>

<decisions>
## Implementation Decisions

### Carried forward (locked in earlier phases, not re-discussed)
- 07 D-15: legacy iOS `Favorites` / `AgendaSessionFilters` are never read, migrated or removed; the
  iOS stores use `devfest.bookmarks.sessionIds` / `devfest.agenda.filters` with the Android codec.
  — **Reversibility:** one-way — once shipped, v1.0 iOS favorites/filters are unreachable; going back
  needs a migrator reading the legacy keys before they are ever deleted. The planner must put a
  `checkpoint:decision` before the commit that switches the app to the new stores.
- 07 D-07/D-08/D-19: iOS keeps local-date day assignment (`LocalDateDays`) with the hardcoded 2025
  dates as a single-source constant (debt), its own "scroll to current slot", and passes the device
  time zone.
- 08 D-01/D-02: one `AgendaViewModel`, owned by `ContentView` via `@State`, read through
  `@Environment`.
- 08 D-04/D-05/D-07: narrow per-VM ports; only `Live…` adapters touch the Kotlin bridge, streams go
  through the Task-free `KotlinStreamBridge`; live wiring through init default arguments until
  Phase 10; fakes in `iosApp/DevFestNantesTests/Fakes/`, no mocking library.
- 08 D-08 is superseded here: the VM stops persisting on the legacy keys.
- 08 D-09/D-10/D-11: other VMs keep first-emission-only and reload on every appear; cancellation is
  silent. Only the agenda becomes continuous (IOSDATA-03).
- Room filter: Phase 7 research verified `room.id == room.name` for every room on the real API
  (07-RESEARCH), so switching iOS from name to id comparison is parity-safe.

### Agenda observation lifetime
- **D-01:** The combined flow is collected in **`AgendaView`'s `.task`**, as today. It is cancelled
  when the view disappears (push, tab switch) and resubscribes when it reappears, keeping v1.0
  "reload on appear" (08 D-10). The single VM keeps the last state, so coming back from a detail
  shows the change immediately. No collection at `ContentView` / app lifetime.
- **D-02:** The `AGENDA_LOAD` trace (`trackDataLoad`, same trace name and `dataSource`) ends at the
  **first emission of each subscription**. Later emissions (bookmark/filter changes) are not traced.
- **D-03:** On resubscribe, **keep the previous content**: `isLoading` stays false after the first
  load and the content is replaced when the new emission arrives (no spinner flash).
- **D-04:** If the flow fails after having emitted: **log through `DevFestLogger`, keep the last
  content, stop**; it resubscribes on the next appear. No error UI, no retry (pinned v1.0 debt).
  Failure before the first emission keeps today's behavior (log, `isLoading = false`).
  `CancellationError` stays silent.

### Bookmark and filter state
- **D-05:** The VM holds an **observable mirror** of bookmarked ids and active filters, **seeded
  from the Kotlin stores** when the VM is created and **updated synchronously on every tap** while
  writing through `setBookmarked` / `setFilters` (persistence happens only in Kotlin). The VM is the
  single iOS writer and the Kotlin writes are synchronous, so the mirror cannot drift. This keeps the
  star correct in a pushed detail while the agenda `.task` is cancelled (including Speakers → speaker
  detail → session) and keeps the bookmark analytics event reading the post-toggle value right after
  the toggle, as in v1.0. The **filtered agenda list itself only comes from the combined flow**.
  **Amended 2026-11-05 at planning (user decision):** `BookmarksStore` has no synchronous snapshot, so
  filters seed in the VM `init` and bookmarks seed **once per ViewModel** through a first-emission read at
  the start of the first agenda load, behind `LoadingView`. No Kotlin or Android change. Accepted risk: a
  star reached through Speakers inside that first window can read stale until next launch; the store
  stays correct.
- **D-06:** No new shared output type for this; the mirror reads the stores through the agenda port
  (exact port methods are Claude's discretion). Clearing filters writes an empty set through the store.

### Swift display model and use-case access
- **D-07:** **`AgendaContent` stays as a thin display mapping** from `DaySchedule` / time slot /
  `Session`: formatting only (slot `Date` for headers and scroll ids, `durationAndLanguage`, room
  name, speaker names). `isATalk` / `isNavigable` delegate to `ClassifySessionUseCase`. No grouping,
  sorting or filtering remains in Swift (`sessionsChanged`, `AgendaView.sections/filteredSessions`
  logic go away). Views change as little as possible to keep the parity diff readable.
- **D-08:** **All shared use cases and iOS stores reach Swift through plain `IosDependencies`
  accessors** (e.g. `observeAgenda()`, `bookmarksStore()`, `sessionFiltersStore()`,
  `speakerIndex()`, `classifySession()`, `venueLanguage()`), resolved from Koin, with no Koin type in
  the Swift API. Only `Live…` adapters call them. One access path that Phase 10 moves into
  `AppDependencies`. Requires a `swift-names-gate.sh` baseline update in the same commit as the Swift
  call sites.

### Proof and UAT
- **D-09:** **In-place update UAT on a simulator from `main`** (Phase 8, still on the legacy keys;
  the v1.0 tag is an acceptable alternative): build and run `main`, add bookmarks and filters, kill;
  `xcrun simctl install` the branch build over it (same bundle id keeps the data container); relaunch
  → bookmarks and filters empty, app works normally; set new ones, kill, relaunch → they persist.
  Agent-driven, approved at a **human checkpoint**.
- **D-10:** **Comparative agenda proof = paired simulator screenshots `main` vs branch + human
  checkpoint** (08 D-16 pattern): both days, several slots, each filter type, favorites toggled from
  the list, from the agenda detail and from Speakers → session, favorites after relaunch, speakers A–Z
  and venue. The room order inside a slot is listed as the only expected difference. Any other
  difference is recorded as a documented deviation, not silently accepted.
- **D-11:** **Scroll to the current slot** is proven by a **unit test with an injected "now"**: the
  scroll-target selection is extracted into a pure function (behavior-preserving) and tested on the
  recorded snapshot for both days. No shifted-clock UAT.

### Accepted deviations (user decision at planning, 2026-11-05)
The shared flow only outputs filtered, shared-grouped data, so these three edge cases are accepted rather
than rebuilt in Swift. With the D-05 seed window below, they are the only additions to the room-order
exception of ROADMAP criterion 3 and go in the `Behavior deviations (v1.1)` table of
`documentation/ARCHITECTURE.md`:
- With filters active on a conference day, the initial scroll goes to the latest visible slot; v1.0 could
  pick a hidden slot and not scroll.
- Day labels no longer follow a non-Gregorian device calendar.
- Fractional-second start dates are parsed in Kotlin instead of falling back to the current date; this
  never occurs in the data.
- D-05 seed window: a star reached through Speakers before the first agenda load seeds the bookmark mirror
  can read stale until next launch; the toggle still writes what the user sees, so the store stays correct.

### Accepted deviation (user decision at the 09-16 D-10 checkpoint, 2026-11-07)
- Small layout offsets on the agenda: after a bookmark change in a session detail, the list on return sits
  about 35 points higher than on `main` (parity pairs 06, 07, 22), and the filter menu panel opens a few
  points further left (pairs 07, 27). Content, order and filter results are unchanged. The user compared the
  pairs and accepted them ("some positions differ a bit but branch looks better than base"). Cause not
  confirmed; likely the shared flow re-emitting the agenda on a bookmark change, which redraws the list.

### Claude's Discretion
- Where the filter menu option lists (languages, complexities, session types) come from — Kotlin
  enum entries or Swift literals — as long as items, labels and order are identical to v1.0 and no
  filtering/persistence logic stays in Swift.
- Exact port methods, accessor names, and how the mirror is seeded (e.g. `filters.value`, a current
  bookmarks snapshot, or a first-emission read).
- Whether filter menus mutate through VM intent methods (`toggleFilter`, `clearFilters`,
  `toggleBookmark`) or a settable property, as long as writes go through the stores.
- Where the iOS day constants live (Swift constant shared by the day picker and `LocalDateDays`, or
  an exported Kotlin constant), kept single-source.
- Plan slicing and deletion order of `SessionFilter.swift` / `FilterType` / `AgendaContent` logic,
  as long as every step leaves Android and iOS CI green and shippable, and the store switch (D-15
  one-way) is its own revertible commit behind the checkpoint.
- Test list per VM (at minimum: continuous re-emission on bookmark/filter change, trace on first
  emission only, keep content on resubscribe, mid-stream error keeps content, mirror write-through,
  cancellation silent).
- Documentation updates required by the AGENTS.md rule (`documentation/ARCHITECTURE.md` iOS data
  flow and persistence, `AGENTS.md` if accessors or gates change).

</decisions>

<canonical_refs>
## Canonical References

**Downstream agents MUST read these before planning or implementing.**

### Planning
- `.planning/ROADMAP.md` § Phase 9 — goal, success criteria 1-4, research flag (in-place update UAT, room id, continuous flow across the bridge)
- `.planning/REQUIREMENTS.md` — IOSDATA-01, IOSDATA-02, IOSDATA-03
- `.planning/PROJECT.md` — v1.1 milestone goal, Key Decisions (Phase 7 D-15 row)
- `.planning/phases/07-kmp-domain-layer-and-android-adoption/07-CONTEXT.md` — D-01..D-20 (shared API shape, day assignment, D-15 no-migration)
- `.planning/phases/07-kmp-domain-layer-and-android-adoption/07-RESEARCH.md` — iOS vs Android filtering/grouping differences, `room.id == room.name` verification
- `.planning/phases/08-modern-swiftui-ios-17-and-observation/08-CONTEXT.md` — D-01..D-16 (single VM, ports, bridge, lifecycle, parity checkpoint)

### Shared Kotlin (consumed by iOS)
- `core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/agenda/ObserveAgendaUseCase.kt` — combined flow, `@NativeCoroutines observe(dayAssignment, timeZone)`
- `core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/agenda/DayAssignment.kt` — `LocalDateDays` (iOS strategy)
- `core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/agenda/DaySchedule.kt`
- `core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/agenda/FilterSessionsUseCase.kt`
- `core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/agenda/ClassifySessionUseCase.kt`
- `core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/speakers/SpeakerIndexUseCase.kt`
- `core/domain/src/commonMain/kotlin/com/gdgnantes/devfest/core/domain/venue/VenueLanguageUseCase.kt`
- `core/data/src/iosMain/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsBookmarksStore.kt` — new key `devfest.bookmarks.sessionIds`
- `core/data/src/iosMain/kotlin/com/gdgnantes/devfest/core/data/UserDefaultsSessionFiltersStore.kt` — new key `devfest.agenda.filters`
- `shared/src/iosMain/kotlin/com/gdgnantes/devfest/shared/IosDependencies.kt` — Swift entry point to extend (D-08)

### iOS code to change
- `iosApp/iosApp/Agenda/AgendaViewModel.swift`, `AgendaSource.swift`, `AgendaView.swift`, `AgendaDetailView.swift`, filter menus (`*FilterMenu.swift`, `ClearFiltersButton.swift`, `AgendaNavigationBarItems.swift`)
- `iosApp/iosApp/Model/SessionFilter.swift` — to delete
- `iosApp/iosApp/Model/Data/AgendaContent.swift` — becomes a thin mapping (D-07)
- `iosApp/iosApp/UI/Speakers/SpeakersViewModel.swift`, `iosApp/iosApp/UI/Venue/VenueViewModel.swift` — adopt shared use cases

### Gates and docs
- `scripts/swift-names-gate.sh` + `scripts/swift-names/` — baselines regenerated with the Swift call-site change
- `scripts/swift-observation-gate.sh`, `scripts/kmp-nc-version-gate.sh`, `scripts/xcresult-assert-tests.sh`
- `documentation/ARCHITECTURE.md`, `AGENTS.md` (iOS section)

</canonical_refs>

<code_context>
## Existing Code Insights

### Reusable Assets
- `KotlinStreamBridge`: Task-free `NativeFlow` → `AsyncThrowingStream`; use it for the `observe()` flow.
- `DataLoadTracing` seam + recording fake: assert the trace ends at first emission only.
- `CharacterizationSupport.swift` `TestData` (recorded snapshot) and Phase 7 goldens: fixtures for the new VM tests and the scroll-target test.
- `AgendaView.sections(_:forDay:)` / `filteredSessions(...)` static extractions and their Swift tests: they pin v1.0 behavior and are replaced by the shared use case (the Kotlin goldens already prove equivalence).

### Established Patterns
- Ports per VM with `Live…` adapters resolving the store lazily (`{ IosDependencies.shared.store() }`), never in an initializer.
- `@Observable @MainActor` VMs, `@ObservationIgnored` for ports/tracing, loads in `.task`, no `Task {}`, no Combine (gate-enforced).
- iOS stores are Koin `single`s on `NSUserDefaults.standardUserDefaults`, serialized with `NSLock`, synchronous writes.

### Integration Points
- `ContentView` owns the single `AgendaViewModel`; `AgendaDetailView` is reached from the Agenda tab and from Speakers → speaker detail.
- `FirebaseAnalyticsService.shared.eventBookmark(... bookmarked:)` reads the VM state right after the toggle (D-05 keeps it correct).
- `RCValues.sharedInstance.fetchCloudValues()` runs in the same `.task` as the sessions load; keep its call order.

</code_context>

<specifics>
## Specific Ideas

- In-place update via `xcrun simctl install` over a `main` build on the same simulator (data container preserved).
- Memory note: shifting the device clock breaks SSL; this is why scroll-to-now is unit-tested with an injected "now" instead.

</specifics>

<deferred>
## Deferred Ideas

- Removing the legacy `Favorites` / `AgendaSessionFilters` keys (cleanup), carried from 07.
- Continuous observation for Speakers / About / SpeakerDetails (they stay first-emission-only).
- Error UI or retry for a failed agenda stream (pinned v1.0 debt).
- Data-driven conference days instead of the hardcoded 2025 dates (debt).

</deferred>

---

*Phase: 09-ios-on-shared-logic-and-legacy-persistence*
*Context gathered: 2026-11-05*
