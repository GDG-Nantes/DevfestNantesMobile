# Phase 9: iOS on Shared Logic and Legacy Persistence - Research

**Researched:** 2026-11-05
**Domain:** SwiftUI app (iOS 17, `@Observable`) consuming a Kotlin Multiplatform `shared` framework (KMP-NativeCoroutines 1.0.6): replacing Swift filtering/grouping/persistence with shared use cases and the iOS `UserDefaults` stores.
**Confidence:** HIGH on the Kotlin/Swift API surface (read from the built header and sources this session), MEDIUM on runtime behavior that needs the spike or the simulator UAT (continuous flow over the bridge, `.task` cancellation on push).

<user_constraints>
## User Constraints (from CONTEXT.md)

### Locked Decisions

#### Carried forward (locked in earlier phases, not re-discussed)
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

#### Agenda observation lifetime
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

#### Bookmark and filter state
- **D-05:** The VM holds an **observable mirror** of bookmarked ids and active filters, **seeded
  from the Kotlin stores** when the VM is created and **updated synchronously on every tap** while
  writing through `setBookmarked` / `setFilters` (persistence happens only in Kotlin). The VM is the
  single iOS writer and the Kotlin writes are synchronous, so the mirror cannot drift. This keeps the
  star correct in a pushed detail while the agenda `.task` is cancelled (including Speakers → speaker
  detail → session) and keeps the bookmark analytics event reading the post-toggle value right after
  the toggle, as in v1.0. The **filtered agenda list itself only comes from the combined flow**.
- **D-06:** No new shared output type for this; the mirror reads the stores through the agenda port
  (exact port methods are Claude's discretion). Clearing filters writes an empty set through the store.

#### Swift display model and use-case access
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

#### Proof and UAT
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

### Deferred Ideas (OUT OF SCOPE)
- Removing the legacy `Favorites` / `AgendaSessionFilters` keys (cleanup), carried from 07.
- Continuous observation for Speakers / About / SpeakerDetails (they stay first-emission-only).
- Error UI or retry for a failed agenda stream (pinned v1.0 debt).
- Data-driven conference days instead of the hardcoded 2025 dates (debt).

(Also out of this phase per CONTEXT domain: explicit DI / `AppDependencies` (Phase 10), SPM packages (Phase 11), strict Swift 6 and deprecated SwiftUI APIs (Phase 12).)
</user_constraints>

<phase_requirements>
## Phase Requirements

| ID | Description | Research Support |
|----|-------------|------------------|
| IOSDATA-01 | The iOS app no longer filters, groups or persists favorites/filters in Swift; the duplicated Swift types (`SessionFilter`, `FilterType`…) are removed in favor of the exported Kotlin types | Exported API verified in `shared.h` (see Standard Stack); deletion list and gate/baseline side effects in Architecture Patterns and Pitfalls 2, 3, 9 |
| IOSDATA-02 | A user who updates from the v1.0 version starts with empty favorites and filters (07 D-15) and the app works normally; favorites and filters set afterwards survive a relaunch (in-place update UAT) | Runtime State Inventory; store keys and Koin bindings verified; UAT procedure and Maestro `clearState` trap in Pitfall 8 and Validation Architecture |
| IOSDATA-03 | The iOS agenda continuously observes the shared combined stream (sessions + favorites + filters); the list, cells and detail share a single `AgendaViewModel` instance | `ObserveAgendaUseCase.observe` native-flow signature, bridge `next()` semantics, first-emission trace pattern (Pattern 2), mirror design (Pattern 3) |
</phase_requirements>

## Project Constraints (from CLAUDE.md / AGENTS.md)

- Dependencies one way: `androidApp → feature:* → core:ui → core:domain → core:data → core:network / core:model`. Data access only through `DevFestNantesStore`; a new store method goes in both `GraphQLStore` and `DevFestNantesStoreMocked`. Not expected here.
- DI is Koin; service-locator access only in `di/` packages, `*Module.kt`, `DevFestNantesApplication`, `MainActivity` and `IosDependencies`. `bash scripts/di-gate.sh` enforces it. New accessors belong in `IosDependencies` only. Koin overriding is disabled (no duplicate bindings).
- Never change `versionName`, `versionCode`, `MARKETING_VERSION`.
- Tests: JUnit 4 + `kotlin.test` + Turbine on Kotlin; Swift Testing on iOS; hand-written fakes, no mocking library; snake_case test names.
- After changing any Kotlin type exported to Swift, run `bash scripts/swift-names-gate.sh check`; regenerate `scripts/swift-names/` baselines only in the same commit as the matching Swift call-site update, never to silence a failing check.
- ViewModels are `@Observable @MainActor`, owned with `@State`, loaded in `.task`, read data through a per-VM `<Feature>Source` port with `Live…` adapter as the only KMP-NativeCoroutines caller. Combine and legacy observation APIs are banned in iOS Swift sources, **comments included** (`bash scripts/swift-observation-gate.sh`).
- Logging: `DevFestLogger(category:)`; no `os_log`/`print`.
- All `.planning/**` artifacts in English. Docs (`README.md`, `documentation/`) updated in the same change when a module/command/version they describe changes.
- Pre-merge: `./gradlew detekt lint`, `scripts/di-gate.sh`, `scripts/kmp-nc-version-gate.sh`, `scripts/swift-observation-gate.sh`, `./gradlew testDebugUnitTest jvmTest testAndroidHostTest`, plus (iOS CI) `swift-names-gate.sh check|selftest`, `:shared:iosSimulatorArm64Test` etc. and the Swift suite through `xcodebuild … test-without-building` + `scripts/xcresult-assert-tests.sh`.
- Conventional commits; attribution lines per the session system-reminder.

## Summary

Everything Phase 9 needs on the Kotlin side already exists and is exported to Swift in the current branch header (`ObserveAgendaUseCase`, `DaySchedule`, `TimeSlot`, `LocalDateDays`, `ClassifySessionUseCase`, `SpeakerIndexUseCase`, `VenueLanguageUseCase`, `SessionFilter`, `SessionFilterType`, `BookmarksStore`, `SessionFiltersStore`, `Kotlinx_datetimeTimeZone`). The iOS stores are already Koin singles on `NSUserDefaults.standardUserDefaults` with new keys. The only Kotlin change is adding plain accessors to `IosDependencies` (D-08) plus a test for each. The work is therefore mostly Swift: a new `AgendaSource` port shape, a continuous `observeAgenda()` in `AgendaViewModel` with a first-emission trace, a mirror of bookmarks/filters, a thinned `AgendaContent`, view/menu rewiring to the exported Kotlin `SessionFilter`/`SessionFilterType`, adoption of the speaker/venue/classify use cases, and deletion of `SessionFilter.swift` plus the Swift grouping/filtering tests.

Three findings shape the plan. (1) `SessionFiltersStore.filters` is a `@NativeCoroutinesState` property, so Swift gets a synchronous `NSSet` snapshot (`filters`) next to the flow (`filtersFlow`), which makes seeding the filter mirror synchronous; `BookmarksStore.bookmarkedSessionIds` is a plain `Flow` with no snapshot, so the bookmark mirror cannot be seeded synchronously without a Kotlin interface change (Open Question 1). (2) `KotlinStreamBridge` already calls `next()` after each `yield`, so it supports continuous flows; what is not yet proven is a real Kotlin continuous flow through it on the CI toolchain, which the ROADMAP flags as the spike. The spike can be hermetic using the real filters/bookmarks store flows (no network). (3) The shipped state of the repo makes parity cheaply provable: the `ios-agenda-<Zone>.json` goldens use a canonical in-slot order, so a Swift test mapping the shared `DaySchedule`s through the new `AgendaContent` can be asserted against the **same v1.0 goldens**, proving days, slots and sessions are unchanged while the room order (the single accepted difference) stays invisible to the golden.

The highest-risk item is the one-way store switch (07 D-15). Keep it as its own commit behind a `checkpoint:decision`, preceded by all behavior-preserving steps (accessors, port, mapping, use-case adoption) that can run on the legacy keys if desired.

**Primary recommendation:** Add the six `IosDependencies` accessors first (Kotlin + `IosKoinTest` + baseline), then migrate Swift in revertible slices — Speakers/Venue/classification (no data-risk), then agenda display + continuous flow on the new port, and last the one-way commit that stops reading/writing `Favorites`/`AgendaSessionFilters` — then run the D-09 in-place UAT and the D-10 Maestro parity pass at human checkpoints.

## Architectural Responsibility Map

| Capability | Primary Tier | Secondary Tier | Rationale |
|------------|-------------|----------------|-----------|
| Filtering sessions (OR within type, AND across) | Shared domain (`FilterSessionsUseCase`) | — | One rule for both platforms; Swift copy is deleted (IOSDATA-01) |
| Day / time-slot grouping and in-slot room order | Shared domain (`GroupSessionsUseCase` with `LocalDateDays`) | — | `RoomSortIndex` order is the documented deviation in normal use (three edge cases accepted at planning, see 09-CONTEXT "Accepted deviations") |
| Combining sessions + filters + bookmarks | Shared domain (`ObserveAgendaUseCase`, `flowOn(Default)`) | iOS VM (collects, owns loading/error/trace) | Use case is data-only (its KDoc D-04); loading, tracing, cancellation stay in the ViewModel |
| Persisting bookmarks and filters | Shared data, iOS `UserDefaults*Store` (Koin singles) | — | Writes synchronous and locked in Kotlin; Swift never touches `UserDefaults` for these |
| Observable bookmark/filter mirror | iOS ViewModel (Swift, `@Observable`) | Shared stores (seed + write-through) | SwiftUI needs observable properties; mirror is a cache of store truth, not a second source of persistence |
| Session classification (service/talk/navigable) | Shared domain (`ClassifySessionUseCase`) | Swift mapping stores the result | Computed once at mapping time, so views and `Hashable` models carry plain Bools |
| Speaker A–Z index, venue language | Shared domain (`SpeakerIndexUseCase`, `VenueLanguageUseCase`) | iOS VM (holds result) | Same code as Android |
| Display formatting (slot `Date`, `durationAndLanguage`, header text) | iOS (Swift, `AgendaContent`) | — | Presentation only (D-07) |
| Initial day + scroll-to-now | iOS (SwiftUI + pure function) | — | Stays platform-specific (07 D-07); extracted for testing (D-11) |
| Koin resolution | `IosDependencies` (Kotlin, `shared`) | `Live…` adapters (Swift) | Single access path until Phase 10 |

## Standard Stack

No new libraries or SPM packages. Everything below is already in the repo.

### Core
| Component | Version | Purpose | Why Standard |
|-----------|---------|---------|--------------|
| `shared` framework (static, `export` of core:model/data/analytics/domain) | in-repo | Kotlin API consumed by Swift | `shared/build.gradle.kts` exports `core:domain` already [VERIFIED: shared/build.gradle.kts `export(project(":core:domain"))`] |
| KMP-NativeCoroutines | 1.0.6 (Gradle `kmpNativeCoroutines = "1.0.6"`, SPM `exactVersion` `1.0.6`) | `Flow` → native-flow closure | Already used by every `Live…` adapter [VERIFIED: gradle/libs.versions.toml:23, project.pbxproj kind=exactVersion version="1.0.6"] |
| `KotlinStreamBridge` (in-repo) | — | native-flow closure → `AsyncThrowingStream`, Task-free, calls `next()` after each `yield` | The only bridge allowed in Live adapters (08 D-07) |
| Swift Testing | Xcode 27.1 toolchain locally | VM and mapping tests, app-hosted target `DevFestNantesTests` | Existing suite and CI path |
| Kotlin / Koin | Kotlin 2.4.21 / Koin 4.2.2 | `IosDependencies` accessors | [VERIFIED: libs.versions.toml:24,37] |

### Kotlin API Swift will call (read from `shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework/Headers/shared.h`, built from this branch)
| Kotlin | Swift signature (verbatim `swift_name`) |
|--------|------------------------------------------|
| `ObserveAgendaUseCase.observe` | `observe(dayAssignment:timeZone:)` returns the native-flow closure typed over `NSArray<SharedDaySchedule *> *` (same shape as `store.sessions`) |
| `LocalDateDays` | `init(dates:)` (`NSArray<NSString *> *`), conforms to `SharedDayAssignment` |
| `DaySchedule` | `init(dayIndex:date:slots:)`; properties `dayIndex`, `date` (String), `slots` (`[TimeSlot]`) |
| `TimeSlot` | `init(start:sessions:)`; `start` is `KotlinInstant` (`epochSeconds`, `nanosecondsOfSecond`, `toEpochMilliseconds()`); `sessions` is `[Session]` |
| `Kotlinx_datetimeTimeZone` | class property `companion`; `Kotlinx_datetimeTimeZone.Companion` has `currentSystemDefault()` and `of(zoneId:)` |
| `ClassifySessionUseCase` | `init()`, `isNavigable(session:)`, `isService(session:)`, `isTalk(session:)` |
| `SpeakerIndexUseCase` | `init()`, `letterGroups(speakers:)` → `[SpeakerLetterGroup]` (`letter`, `speakers`), `firstIndexes(speakers:)` |
| `VenueLanguageUseCase` | `init()`, `languageFor(languageCode:)` (`String?`) → `ContentLanguage` |
| `SessionFilter` / `SessionFilterType` | `SessionFilter.init(type:value:)`; enum class properties `bookmark`, `complexity`, `language`, `room`, `type`; value equality and `hash()` (D-14) |
| `BookmarksStore` | `isBookmarked(id:)`, `setBookmarked(sessionId:bookmarked:)`, `bookmarkedSessionIds` (native flow of `NSSet<NSString *>`, **no snapshot**), `subscribe(id:)` |
| `SessionFiltersStore` | `setFilters(filters:)` (`NSSet<SharedSessionFilter *> *`), `filters` (**synchronous `NSSet` snapshot**), `filtersFlow` (native flow) |

Quoted from the header (verbatim):
`- (SharedKotlinUnit *(^(^)(…NSArray<SharedDaySchedule *> *…))(void))observeDayAssignment:(id<SharedDayAssignment>)dayAssignment timeZone:(SharedKotlinx_datetimeTimeZone *)timeZone __attribute__((swift_name("observe(dayAssignment:timeZone:)")))`,
`@property (readonly) NSSet<SharedSessionFilter *> *filters __attribute__((swift_name("filters")));`,
`@property (readonly) SharedKotlinUnit *(^(^bookmarkedSessionIds)(… NSSet<NSString *> * …` [VERIFIED: shared.h lines 583, 306, 261]. Note the header on disk is a local debug build; the planner's first task must rebuild it (`./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`) before relying on it.

`IosDependencies` today exposes only `start()` and `store()` [VERIFIED: shared/src/iosMain/kotlin/com/gdgnantes/devfest/shared/IosDependencies.kt, `fun start()` / `fun store(): DevFestNantesStore`]. Koin bindings of the iOS stores: `single { UserDefaultsBookmarksStore(NSUserDefaults.standardUserDefaults) } bind BookmarksStore::class` and `single { UserDefaultsSessionFiltersStore(NSUserDefaults.standardUserDefaults) } bind SessionFiltersStore::class` [VERIFIED: core/data/src/iosMain/kotlin/com/gdgnantes/devfest/core/data/di/DataPlatformModule.ios.kt:16-17]; use cases are `factory` definitions [VERIFIED: core/domain/.../di/DomainModule.kt].

### Alternatives Considered
| Instead of | Could Use | Tradeoff |
|------------|-----------|----------|
| Swift literal option lists in filter menus (current) | `SessionLanguage.entries` / `Complexity.entries` / `SessionType.entries` | Entries are not the menu: session-type menu is a 3-item subset (conference, quickie, codelab) of an enum that also has keynote/opening/break/lunch, and label mapping is Swift anyway. **Use the existing Swift literals, replacing only `SessionFilter`/`.type` construction**: smallest parity diff. |
| Swift `enum AgendaDays` constant for the two dates | Exported Kotlin constant | Kotlin has none for these (the only `2025-10-16` in Kotlin is a private test-fixture const). A Swift single-source constant is the smaller change (5 literal sites in `AgendaView.swift` today). |
| Seed bookmark mirror by first-emission read (recommended) | Add a snapshot to `BookmarksStore` | See Open Question 1. |

**Installation:** none (no new dependency). Package Legitimacy Gate: not applicable — no external package is added, so no `package-legitimacy check` run was needed.

## Package Legitimacy Audit

| Package | Registry | Age | Downloads | Source Repo | Verdict | Disposition |
|---------|----------|-----|-----------|-------------|---------|-------------|
| (none added) | — | — | — | — | — | — |

**Packages removed due to [SLOP] verdict:** none
**Packages flagged as suspicious [SUS]:** none

## Architecture Patterns

### System Architecture Diagram

```
 iOSApp.init ──► IosDependencies.start() (Koin)
                         │
 ContentView  @State AgendaViewModel ── init: seed filter mirror (sync) ───────────┐
      │ (.environment)                                                              │
      ▼                                                                             │
 AgendaView .task ─► RCValues.fetchCloudValues() ─► vm.seedBookmarks() (1st emission, once)
      │                                                                             │
      └─► vm.observeAgenda()                                                        │
            │  tracing.trackDataLoad(AGENDA_LOAD) { iterator.next() }   ◄── ends at FIRST emission only (D-02)
            │  then: for try await next in same iterator { apply }                  │
            ▼                                                                       │
   AgendaSource (port)  ◄── Fake in tests                                           │
     Live adapter ── IosDependencies.shared.observeAgenda().observe(LocalDateDays, tz now)
            │                         │ KotlinStreamBridge.stream (next() after each yield)
            ▼                         ▼
   ObserveAgendaUseCase = combine(store.sessions, filtersStore.filters, bookmarksStore.bookmarkedSessionIds)
                                      │ filter → group (LocalDateDays) → flowOn(Default)
                                      ▼
                          [DaySchedule] ─► AgendaContent mapping (format only, classify flags) ─► agendaContent
 UI taps:
   star ─► vm.toggleBookmark ─► mirror.insert/remove (sync) ─► source.setBookmarked ─► Kotlin store (sync, NSLock)
   menu ─► vm.toggleFilter/clearFilters ─► mirror = new set ─► source.setFilters ─► Kotlin store
                                                                      │ StateFlow emits
                                                                      └──► combined flow re-emits ─► list refreshes (no relaunch)
 Detail / Speakers → session: star reads the MIRROR (works while the agenda .task is cancelled)
```

### Recommended change set
```
shared/src/iosMain/.../IosDependencies.kt      # + observeAgenda(), bookmarksStore(), sessionFiltersStore(),
                                               #   speakerIndex(), venueLanguage(), classifySession()
shared/src/iosTest/.../IosKoinTest.kt          # + one resolve test per accessor
iosApp/iosApp/Agenda/AgendaSource.swift        # new port shape + LiveAgendaSource
iosApp/iosApp/Agenda/AgendaViewModel.swift     # continuous observeAgenda, mirror, intents; no UserDefaults
iosApp/iosApp/Agenda/AgendaView.swift          # data source swap only; scroll-target pure function
iosApp/iosApp/Agenda/*FilterMenu.swift, ClearFiltersButton.swift  # exported SessionFilter, VM intents
iosApp/iosApp/Agenda/AgendaDetailView.swift    # star reads the mirror
iosApp/iosApp/Model/Data/AgendaContent.swift   # thin mapping from DaySchedule/TimeSlot/Session
iosApp/iosApp/Model/SessionFilter.swift        # DELETE
iosApp/iosApp/UI/Speakers/SpeakersViewModel.swift + SpeakersSource.swift   # SpeakerIndexUseCase
iosApp/iosApp/UI/Venue/VenueViewModel.swift + VenueSource.swift            # VenueLanguageUseCase
iosApp/iosApp/UI/Speakers/SpeakerDetailsViewModel.swift (+ view)           # classify when mapping sessions
iosApp/DevFestNantesTests/                     # delete SessionFilterTests, AgendaDateParsingTests, swap characterization
scripts/swift-names/*.txt, names-exclude.txt   # regenerate; remove SessionFilter exclusion
documentation/ARCHITECTURE.md (+ AGENTS.md if accessors/gates change)
```

### Pattern 1: `IosDependencies` accessors (D-08)
**What:** One private reified resolver, public concrete-typed accessors, each self-starting like `store()`.
**Example:**
```kotlin
// Source: extends the existing store() in IosDependencies.kt (self-starting pattern)
private inline fun <reified T : Any> resolve(): T {
    start()
    return KoinPlatform.getKoin().get()
}

fun observeAgenda(): ObserveAgendaUseCase = resolve()
fun bookmarksStore(): BookmarksStore = resolve()
fun sessionFiltersStore(): SessionFiltersStore = resolve()
fun speakerIndex(): SpeakerIndexUseCase = resolve()
fun venueLanguage(): VenueLanguageUseCase = resolve()
fun classifySession(): ClassifySessionUseCase = resolve()
```
`IosKoinTest` already resolves `ClassifySessionUseCase`, `ObserveAgendaUseCase`, and both stores as singles [VERIFIED: shared/src/iosTest/.../IosKoinTest.kt], so the graph is known good; add one assertion per accessor. Adding public members to `IosDependencies` changes its member set, so `swift-names-gate.sh baseline` must be re-run in the same commit as the Swift call sites (gate message `SWIFT-MEMBERS-CHANGED IosDependencies` otherwise).

### Pattern 2: Continuous collection with a first-emission trace (D-01..D-04)
**What:** `trackDataLoad` wraps one operation. Make that operation "await the first element of an iterator that the surrounding method keeps using", so the trace ends at the first emission and the same subscription continues afterward.
**Example (shape, not final code):**
```swift
func observeAgenda() async {
    var iterator = source.agenda().makeAsyncIterator()   // port returns AsyncThrowingStream<[DaySchedule], Error>
    var hasEmitted = false
    do {
        let first = try await tracing.trackDataLoad(
            traceName: PerformanceMonitoring.TRACE_AGENDA_LOAD, dataSource: "graphql"
        ) {
            let value = try await iterator.next()
            // The bridge ends silently on cancellation, so check explicitly (same as v1.0 code).
            try Task.checkCancellation()
            return value
        }
        guard let first else { isLoading = false; return }   // stream ended without emitting (v1.0: empty agenda)
        apply(first); hasEmitted = true
        while let next = try await iterator.next() { apply(next) }   // later emissions: not traced
        try Task.checkCancellation()
    } catch is CancellationError {
        return                                               // silent (D-04)
    } catch {
        logger.log(.error, "Observe agenda error: \(error.localizedDescription)", error: error)
        if !hasEmitted { isLoading = false }                 // before first emission: v1.0 behavior; after: keep content, stop
    }
}
```
`apply` sets `agendaContent` and `isLoading = false`. `isLoading` is initialized `true` once and never set back to `true` (D-03). A captured `var iterator` mutated in the non-`Sendable` escaping `operation` closure is legal under `SWIFT_VERSION = 5.0` [VERIFIED: project.pbxproj `SWIFT_VERSION = 5.0;`]; Phase 12 owns Swift 6 strictness, so do not restructure for it now. The `RecordingDataLoadTracing` fake can assert exactly one recorded trace after several emissions.

### Pattern 3: Observable mirror, write-through (D-05, D-06)
**What:** `@Observable` VM keeps `private(set) var bookmarkedIds: Set<String>` and `private(set) var sessionFilters: Set<SessionFilter>`; intents mutate the mirror synchronously and call the port's synchronous setters. The list never reads the mirror (it comes from the flow); stars, menu checkmarks, "Clear" visibility and the analytics `bookmarked:` argument do.
**Seeding:**
- Filters: synchronous. `store.filters` is an `NSSet` snapshot, so the port can offer `func currentFilters() -> Set<SessionFilter>` and the VM seeds in `init` (consistent with D-05). The Live adapter resolves lazily inside the call, never in its initializer.
- Bookmarks: see Open Question 1. Recommended: port `func bookmarkedIds() -> AsyncThrowingStream<Set<String>, Error>` read once (first element) by `seedBookmarks()` called from the `.task` before collection starts; it is idempotent and also re-syncs on every appear.
**Intents:** `toggleBookmark(sessionId:) -> Bool` returning the post-toggle value (feeds `FirebaseAnalyticsService.shared.eventBookmark(page:sessionId:bookmarked:)` exactly as v1.0 reads it right after the toggle), `toggleFilter(_:)`, `clearFilters()` (writes `[]` through the store; no `removeObject`).

### Pattern 4: Thin `AgendaContent` mapping (D-07)
Keep `AgendaContent.Section { date: Date, day: String, sessions: [Session] }` and `AgendaContent.Session`, so views change minimally. Build `Section.date` from `TimeSlot.start` (`Date(timeIntervalSince1970: TimeInterval(start.epochSeconds))`; session start strings are whole seconds, `nanosecondsOfSecond` is 0 on the recorded data), `Section.day` from `DaySchedule.date`. Replace the computed `isATalk` by stored `isATalk` and add stored `isNavigable`, both filled from `ClassifySessionUseCase` at mapping time (port method), so the model stays a pure `Hashable` struct and no view calls Kotlin. Call sites that build `AgendaContent.Session` outside the agenda VM: `SpeakerDetailsView` (lines 71-72) maps `Session` directly in the view; route that mapping through `SpeakerDetailsViewModel` (it gets the classify port) or give `AgendaContent.Session.init(from:isATalk:isNavigable:)` explicit parameters. `AgendaView` then filters sections by `section.day == day` (the shared `DaySchedule` list always contains both configured days, empty ones with no slots) and no longer filters sessions or hides empty slots itself (the shared output has no empty slot).

### Pattern 5: Scroll-to-now as a pure function (D-11)
Extract the body of `handleInitialScrollIfNeeded` (compare minute-of-day of each slot with the minute-of-day of `now`; pick the latest slot with `t <= nowTotal` after sorting the day's slots by minute-of-day) into `static func scrollTarget(slotDates: [Date], now: Date, calendar: Calendar) -> Date?`. The rest of the method (`currentDay == day && firstAppear`, `firstAppear = false`) stays in the view. Tests must pass an explicit `Calendar` with `timeZone = Europe/Paris`: CI runs on UTC (existing characterization tests pin the zone for the same reason, e.g. `sectionDateFormatter.timeZone`), and the code uses `Calendar.current` today.

### Anti-Patterns to Avoid
- **Collecting the three sources separately in Swift** (re-implementing `combine`): defeats IOSDATA-03; use the one `observe` flow.
- **Reading the mirror to build the list, or the flow to render the star**: the star must stay correct while the agenda `.task` is cancelled (pushed detail), the list must come only from the flow (D-05).
- **Caching the `TimeZone` or the `LocalDateDays` across subscriptions**: evaluate `currentSystemDefault()` in the port call so every resubscribe uses the zone current at that time.
- **Calling `IosDependencies` from `AgendaContent`, views or VMs**: only `Live…` adapters (D-08).
- **Writing Swift comments that name banned APIs** (see Pitfall 6).

## Don't Hand-Roll

| Problem | Don't Build | Use Instead | Why |
|---------|-------------|-------------|-----|
| Filtering / grouping / room order | Swift loops over sessions | `ObserveAgendaUseCase` (+ `FilterSessionsUseCase`, `GroupSessionsUseCase` inside) | Kotlin goldens already prove equivalence with v1.0 behaviors |
| Bookmark / filter persistence and encoding | `UserDefaults` code, `JSONEncoder` of filters | Koin `UserDefaultsBookmarksStore` / `UserDefaultsSessionFiltersStore` | Locked writes, shared codec, tolerant loading of untrusted values |
| Session classification | `switch` over `SessionType` | `ClassifySessionUseCase` | One table for both platforms (note `keynote` is a talk but not navigable) |
| Speaker letter grouping | `prefix(1).uppercased()` + `Set` + sort in Swift | `SpeakerIndexUseCase.letterGroups` | Same definition as Android |
| Venue language choice | `Locale.current.languageCode == "fr"` | `VenueLanguageUseCase.languageFor(languageCode:)` | Shared table |
| Flow → Swift | `Task { for await … }` or KMP-NativeCoroutines async helpers in VMs | `KotlinStreamBridge.stream(...)` inside the Live adapter | Task-free, cancellation-correct, already tested |

**Key insight:** the phase is deletion-heavy. If a step adds Swift logic that looks like filtering, grouping, sorting, or persistence, it is wrong.

## Runtime State Inventory

The switch of persistence keys makes this a migration-class phase.

| Category | Items Found | Action Required |
|----------|-------------|------------------|
| Stored data | `UserDefaults.standard` keys `Favorites` (`[String]`) and `AgendaSessionFilters` (JSON `Data`, legacy `{type,value}` with lowercase type) written by Phase 8 `main` [VERIFIED: iosApp/iosApp/Agenda/AgendaViewModel.swift `defaults.set(favorites, forKey: "Favorites")`, `"AgendaSessionFilters"`]. New keys `devfest.bookmarks.sessionIds`, `devfest.agenda.filters` [VERIFIED: UserDefaultsBookmarksStore.kt `KEY_BOOKMARKS = "devfest.bookmarks.sessionIds"`, UserDefaultsSessionFiltersStore.kt `KEY_FILTERS = "devfest.agenda.filters"`] | Code edit only (VM stops using the legacy keys). **No data migration** (07 D-15). Legacy keys stay on disk, unread (cleanup deferred) |
| Live service config | None — verified: bookmarks/filters are local only; no backend or Firebase config references the keys (analytics events carry session ids, not keys) | None |
| OS-registered state | None — no launch agents, background tasks or widgets read these keys (iOS app has a single target plus the test target) | None |
| Secrets / env vars | None — keys are non-sensitive preferences | None |
| Build artifacts / installed packages | `shared.framework` and generated `shared.h` are stale until rebuilt; `scripts/swift-names/*.txt` baselines (stale: `shared-h-baseline.txt` lacks `DaySchedule`, `ObserveAgendaUseCase`, etc.); `names-exclude.txt` contains a `SessionFilter` exclusion that will hide references once the Swift struct is deleted | Rebuild framework; re-run `swift-names-gate.sh baseline` in the Swift call-site commit; remove the `SessionFilter` line from `names-exclude.txt` (and the "app-local type shadows" note); no installed-package rename |

**Canonical question — after every file is updated, what still holds the old data?** Only the two legacy `UserDefaults` keys on devices that ran v1.0/Phase 8, intentionally orphaned.

## Common Pitfalls

### Pitfall 1: `SessionFiltersStore.filters` vs bookmarks have no symmetric snapshot
**What goes wrong:** Planning assumes both mirrors can be seeded synchronously in `init`. Only filters can (`filters` property). `BookmarksStore` exposes just a `Flow`, `isBookmarked(id:)` and `subscribe(id:)`.
**How to avoid:** Decide Open Question 1 up front. With the recommended first-emission seed, put it in the `.task` before the collection and test it with a `FakeStream`.
**Warning signs:** star shows empty on first render after relaunch, then fills in; or a Kotlin interface change surprises the Android fakes in `core/testing`.

### Pitfall 2: Swift `SessionFilter` shadows the Kotlin export today
**What goes wrong:** Deleting `SessionFilter.swift` silently re-binds every unqualified `SessionFilter`/`.type` use to the Kotlin class; code like `FilterType(rawValue:)`, `Codable`/`JSONDecoder` use, and tests written `DevFest_Nantes.SessionFilter` stop compiling. Filter type spelling changes from Swift raw values (`"type"`) to Kotlin enum properties (`SessionFilterType.type`, compared with `==`, not exhaustive-`switch`able).
**How to avoid:** Delete `SessionFilter.swift`, `SessionFilterTests.swift`, the `DevFest_Nantes.SessionFilter` qualifications in `AgendaCharacterizationTests`/`CharacterizationSupport`, and the exclusion line in `names-exclude.txt` in one coherent change; then re-baseline. Keep the favorites filter value `"bookmark"` as in v1.0 (the shared rule ignores the value for `BOOKMARK`; Android uses `""` [VERIFIED: feature/agenda/.../SessionFiltersDrawer.kt:61 `SessionFilter(SessionFilterType.BOOKMARK, "")`]; the platforms never share a store).

### Pitfall 3: `swift-names-gate.sh` baselines are stale and the SessionFilter exclusion hides references
**What goes wrong:** `check` only fails on missing referenced names, changed member sets of referenced types, and collisions; new Swift references to `DaySchedule`, `TimeSlot`, `LocalDateDays`, `Kotlinx_datetimeTimeZone`, `KotlinInstant`, `SessionFilterType`, `ObserveAgendaUseCase`, … are absent from the current baselines. `IosDependencies` members change.
**How to avoid:** Run `bash scripts/swift-names-gate.sh baseline` then `check` and `selftest` in the commit that lands the Swift references; review the diff of `referenced-names.txt`/`members-baseline.txt` (it must add the new names and `SessionFilter` as a real Kotlin reference). Never regenerate to silence a failing check.

### Pitfall 4: Continuous flow over the bridge has never been exercised with a real Kotlin flow
**What goes wrong:** All current Live adapters consume single-emission flows. `KotlinStreamBridge` does call `next()` after each `yield` (code comment: "without it a continuous flow stalls after its first value") [VERIFIED: iosApp/iosApp/Model/KotlinStreamBridge.swift], and `FakeStream` plays multi-element scripts, but neither proves a real `StateFlow`-backed flow on the CI toolchain. This is the ROADMAP research flag.
**How to avoid:** Spike as the first Swift task: an app-hosted Swift Testing test that opens `KotlinStreamBridge.stream(IosDependencies.shared.sessionFiltersStore().filtersFlow)`, takes the first element, calls `setFilters`, and expects a second element; same for `bookmarkedSessionIds` with `setBookmarked`. Hermetic (no network), and it uses the Koin stores on `standardUserDefaults` of the test host, so clean up (`setFilters([])`, `setBookmarked(false)`) in `defer`. The full `observe` flow needs the real GraphQL store (Koin binds `GraphQLStore`), so it is covered by Kotlin tests (`ObserveAgendaUseCaseTest`, JVM + `:core:domain:iosSimulatorArm64Test`) plus the UAT, not by a hosted Swift test. Remember the Debug app skips Koin start when run under XCTest [CITED: documentation/DEVELOPMENT.md "Swift tests"]; `IosDependencies.shared.start()` (via `TestHost.start()`) or the self-starting accessors are required.

### Pitfall 5: Time-zone and calendar dependence of tests and mapping
**What goes wrong:** `LocalDateDays` assigns days using the zone passed to `observe`; CI simulators run in UTC. Any Swift test that builds `DaySchedule`s must pass an explicit zone (`Kotlinx_datetimeTimeZone.companion.of(zoneId: "Europe/Paris")`) and compare ISO UTC strings, as the existing golden tests do. v1.0 day labels came from a `DateFormatter` using the user's calendar; a non-Gregorian device calendar would have mislabeled days in v1.0 and no longer does (Kotlin is proleptic Gregorian) — treat as a recorded neutral deviation, not something to reproduce.

### Pitfall 6: Observation gate scans comments
**What goes wrong:** Any new Swift comment or identifier containing the banned names fails CI (`SWIFT-OBS-FOUND`). Banned: the Combine import, the KMP-NativeCoroutines Combine module, the legacy observable-object protocol name, the published property wrapper, the will-change publisher name, and a main-queue async hop (`DispatchQueue`, `.main`, `.async`) [VERIFIED: scripts/swift-observation-gate.sh header]. Write "the legacy observation APIs" in prose and run the gate before committing.

### Pitfall 7: Fractional-second start dates, parse fallback
**What goes wrong:** v1.0 `AgendaViewModel.parseDate` used `ISO8601DateFormatter`, which fails on fractional seconds and falls back to "now" (pinned by `AgendaDateParsingTests`; STATE.md notes Phase 9 "must reproduce it or change it as a recorded deviation"). The shared grouping parses with Kotlin `Instant.parse`, which accepts fractions. The recorded data uses `yyyy-MM-dd'T'HH:mm:ss+02:00` [CITED: core/domain/testdata/agenda/README.md], so no visible effect.
**How to avoid:** Choose "change, recorded as neutral deviation": delete `parseDate`, `getDate`, `sectionDateFormatter` and `AgendaDateParsingTests` with the grouping code, and note the deviation in the D-10 deviation list. `AgendaContent.Session.init(from:)` and `AgendaDetailView.iso(_:)` keep using `ISO8601DateFormatter` for formatting only.

### Pitfall 8: Maestro `clearState` wipes the very data the update UAT needs
**What goes wrong:** The Phase 8 parity flow starts with `launchApp: clearState: true` [VERIFIED: .planning/phases/08-modern-swiftui-ios-17-and-observation/08-parity/parity-flow.yaml]. For D-09 the branch build must be installed over the `main` install and launched **without** clearing state; a flow with `clearState: true` would pass trivially. Also `xcrun simctl uninstall` or "Delete app" deletes the container.
**How to avoid:** Use a separate in-place flow (`launchApp` without `clearState`), install with `xcrun simctl install <udid> <branch .app>` over the existing app (bundle id `com.gdgnantes.devfest.iosApp` [VERIFIED: project.pbxproj PRODUCT_BUNDLE_IDENTIFIER]), assert empty bookmarks/filters, then set new ones, `xcrun simctl terminate`, relaunch, assert they persist. Remember the memory note: do not shift the device clock (breaks TLS).

### Pitfall 9: Rooms menu order is not deterministic in v1.0
**What goes wrong:** `roomsContent = Array(rooms)` where `rooms` is a Swift `Set<Room>`: iteration order varies per process, so "same order as the rooms list today" (UI-SPEC) cannot be pinned by screenshots. A parity screenshot of the Rooms submenu may differ run to run for reasons unrelated to the phase.
**How to avoid:** Keep `observeRooms()` and `Array(rooms)` unchanged (strict parity) and record the nondeterminism as pre-existing debt in the checkpoint notes; do not sort silently. (Alternative if the user wants it fixed: sort by `sortIndex` and record it as a deliberate deviation.)

### Pitfall 10: `.task` restarts and the day reset (inherited)
**What goes wrong:** Both agenda `.task` blocks restart on each reappearance and `.onAppear` resets the selected day (08 review WR-02, pre-existing, accepted) [CITED: 08-VERIFICATION.md Warnings]. 08 WR-01: a Kotlin-side cancellation surfaces as `CancellationError` and is swallowed, which can leave `isLoading == true` when the Swift task is still alive.
**How to avoid:** Keep the `.task` structure and call order (`observeRooms` task; `fetchCloudValues()` then the agenda task). For WR-01, when the catch for `CancellationError` runs while `Task.isCancelled == false` and nothing has emitted, set `isLoading = false` (log at info). This is a robustness addition that does not change normal behavior; add a test with a Kotlin-side cancellation script. Mark it optional if the planner wants strict minimalism.

### Pitfall 11: `@State private var agendaViewModel = AgendaViewModel()` re-evaluates the initializer
**What goes wrong:** The default expression of a `@State` property can run each time `ContentView`'s initializer runs (only the first instance is kept). With init-time seeding, every throwaway instance calls into Kotlin.
**How to avoid:** Seeding is cheap and idempotent (Koin singles, a snapshot read), so this is acceptable; keep the seed free of side effects and keep the Live adapter lazy. Do not start work (`Task`, subscriptions) in `init`.

## Code Examples

### Live adapter (only place that touches the bridge and `IosDependencies`)
```swift
// Source: pattern of iosApp/iosApp/Agenda/AgendaSource.swift (LiveAgendaSource) extended for the new API
struct LiveAgendaSource: AgendaSource {
    func agenda() -> AsyncThrowingStream<[DaySchedule], Error> {
        let useCase = IosDependencies.shared.observeAgenda()
        let days = LocalDateDays(dates: AgendaDays.all)
        let zone = Kotlinx_datetimeTimeZone.companion.currentSystemDefault()   // per subscription
        return KotlinStreamBridge.stream(useCase.observe(dayAssignment: days, timeZone: zone))
    }
    func currentFilters() -> Set<SessionFilter> { IosDependencies.shared.sessionFiltersStore().filters }
    func setFilters(_ filters: Set<SessionFilter>) { IosDependencies.shared.sessionFiltersStore().setFilters(filters: filters) }
    func bookmarkedIds() -> AsyncThrowingStream<Set<String>, Error> {
        KotlinStreamBridge.stream(IosDependencies.shared.bookmarksStore().bookmarkedSessionIds)
    }
    func setBookmarked(_ id: String, _ bookmarked: Bool) {
        IosDependencies.shared.bookmarksStore().setBookmarked(sessionId: id, bookmarked: bookmarked)
    }
    func isTalk(_ session: Session) -> Bool { IosDependencies.shared.classifySession().isTalk(session: session) }
    func isNavigable(_ session: Session) -> Bool { IosDependencies.shared.classifySession().isNavigable(session: session) }
    func rooms() -> AsyncThrowingStream<Set<Room>, Error> { /* unchanged: store().rooms */ }
}

enum AgendaDays {   // single source (07 D-07 debt): day picker tags, initial day, LocalDateDays, detail `day:`
    static let day1 = "2025-10-16"
    static let day2 = "2025-10-17"
    static var all: [String] { [day1, day2] }
}
```
Exact call spellings (`companion`, `setFilters(filters:)`, `filters`) come from the header lines cited above; the final compile is the check. `Set<SessionFilter>` bridges `NSSet<SharedSessionFilter *>`; `Set<String>` bridges `NSSet<NSString *>` [VERIFIED: header shapes].

### Speakers / Venue adoption (view code unchanged)
`SpeakersViewModel` stores `letterGroups: [SpeakerLetterGroup]` from `speakerIndex.letterGroups(speakers:)` after the first emission, and keeps `getAlphabets()` / `speakers(for:)` signatures answering from it, so `SpeakersView` is untouched. `VenueViewModel.currentLanguage` becomes `venueLanguage.languageFor(languageCode: Locale.current.languageCode)` through the Venue port (keep the deprecated `languageCode` read; Phase 12 owns deprecations).

### Display mapping sketch
```swift
// AgendaContent.Section from a TimeSlot; sessions are already filtered, grouped and room-ordered by Kotlin
let sections = days.flatMap { day in
    day.slots.map { slot in
        AgendaContent.Section(
            date: Date(timeIntervalSince1970: TimeInterval(slot.start.epochSeconds)),
            day: day.date,
            sessions: slot.sessions.map { AgendaContent.Session(from: $0, isATalk: source.isTalk($0), isNavigable: source.isNavigable($0)) })
    }
}
```

### Parity test reusing the v1.0 goldens
Build `[DaySchedule]` with `GroupSessionsUseCase().group(sessions:dayAssignment:timeZone:)` over `TestData.sessions()` (Europe/Paris and Pacific/Auckland, as `assertGrouping` does today), map through the new `AgendaContent` code, produce `AgendaGolden(source: "ios-v1.0", …)` with the same canonical sort `(room, id)` and `Golden.assertMatches("agenda/golden/ios-agenda-<Zone>.json", …)`. The existing goldens are canonicalized, so they are valid despite the room-order change [CITED: core/domain/testdata/agenda/README.md "Session ids inside a slot are canonicalized"]. Never regenerate or edit a golden.

## State of the Art

| Old Approach | Current Approach | When Changed | Impact |
|--------------|------------------|--------------|--------|
| Swift `sessionsChanged` grouping, `AgendaView.filteredSessions`, `Favorites`/`AgendaSessionFilters` `UserDefaults` | `ObserveAgendaUseCase` + Koin `UserDefaults*Store`, new keys | Phase 7 delivered Kotlin side; Phase 9 wires iOS | Deletes ~150 lines of Swift logic |
| Single-shot first-emission load for the agenda | Continuous combined flow, trace on first emission only | Phase 9 | Bookmark/filter changes refresh the list without relaunch |
| Room order in slot: room name only (iOS), unspecified (Android) | `RoomSortIndex` (`sortIndex`, then name, no-room first, ties in store order) | Phase 7 shared grouping | The one accepted visible deviation |

**Deprecated/outdated:** `AgendaViewModel.favorites`/`sessionFilters` `didSet` persistence, `defaults:` init parameter, `sessionsChanged`, `getDate`/`parseDate`, `sectionDateFormatter`, `AgendaView.sections/filteredSessions`, `SessionFilter.swift`, `SessionFilterTests`, `AgendaDateParsingTests`, and the Swift grouping/filtering characterization tests (superseded by the golden-based mapping test and the Kotlin goldens).

## Assumptions Log

| # | Claim | Section | Risk if Wrong |
|---|-------|---------|---------------|
| A1 | `xcrun simctl install` over an existing install with the same bundle id preserves the data container | Pitfall 8, UAT | If it resets data, the UAT proves nothing; the first UAT step ("bookmarks exist before the install, app killed") must be asserted by screenshot/`maestro hierarchy` on the `main` build, and the `simctl get_app_container <udid> <bundle> data` path compared before/after |
| A2 | A Kotlin `private inline fun <reified T : Any>` helper inside `object IosDependencies` compiles on K/N and is not exported to ObjC | Pattern 1 | Fall back to six explicit `KoinPlatform.getKoin().get()` bodies |
| A3 | Swift spells the companion access as `Kotlinx_datetimeTimeZone.companion.currentSystemDefault()` | Code Examples | Compile error only; header also offers `SharedKotlinx_datetimeTimeZoneCompanion.shared` (`@property (class, readonly, getter=shared)`) |
| A4 | A real `StateFlow`-backed native flow delivers a second element through `KotlinStreamBridge` on the CI Xcode (the spike) | Pitfall 4 | If it stalls, the bridge needs a fix before any VM work; this is why the spike is first |
| A5 | `.task` on the NavigationStack root is cancelled when a destination is pushed (D-01 premise) | Architecture | Mirror design already covers both outcomes; only "refreshes on next appear" wording would change. Evidence: 08 WR-02 says it restarts on each reappearance |
| A6 | `UserDefaults` writes performed by the Kotlin stores survive `simctl terminate` | UAT | If a write is lost, the persistence requirement fails and is a real bug; the UAT exists to catch it |
| A7 | KMP-NativeCoroutines 1.0.6 native flows from a `StateFlow`/`combine` invoke `onItem` possibly off the main thread; yielding into `AsyncThrowingStream` from any thread is safe and the `@MainActor` consumer resumes on main | Pattern 2 | A data race would show in the spike or the strict-concurrency Phase 12 |

## Open Questions (RESOLVED)

Resolved at planning, 2026-11-05. Q1 was confirmed by the user; Q2 to Q4 take the recommendation.

1. **RESOLVED: first-emission read, once per ViewModel, at the start of the first agenda load (09-CONTEXT D-05 amendment). How is the bookmark mirror seeded?**
   - What we know: `BookmarksStore` has no snapshot (`bookmarkedSessionIds` is a plain `Flow`); `SessionFiltersStore.filters` has one. D-06 forbids a new shared output type but leaves seeding to discretion ("a current bookmarks snapshot, or a first-emission read").
   - What's unclear: whether the user accepts a one-hop async seed.
   - Recommendation: first-emission read in the agenda `.task` (no Kotlin change, no Android impact). The alternative (a `@NativeCoroutinesState`/snapshot member on `BookmarksStore`) touches `BookmarksStoreImpl`, the `core:testing` fakes and `swift-names` baselines for a window of a few milliseconds at launch; reject unless the plan-checker objects.

2. **RESOLVED: keep the existing order. Rooms menu order (non-deterministic `Set` order).** Recommendation: keep as is, document as pre-existing (Pitfall 9).

3. **RESOLVED: included, agenda only (09-11). Optional: should the mid-flow Kotlin cancellation guard (08 WR-01) be included?** Recommendation: include the one-line guard with a test; it cannot change normal-path behavior.

4. **RESOLVED: `main`. Baseline for D-10/D-09: `main` (Phase 8) or tag `iOS_1_0_3`?** CONTEXT allows either; `main` has the same legacy keys and the existing Maestro harness, so use `main`; run the v1.0 tag pass only if the user asks (tags `iOS_1_0_0` and `iOS_1_0_3` exist locally).

## Environment Availability

| Dependency | Required By | Available | Version | Fallback |
|------------|------------|-----------|---------|----------|
| Xcode / xcodebuild | Swift build and tests | ✓ | 27.1 (27A9275) | — |
| iOS simulators | Tests, UAT, parity | ✓ | iOS 27.0, 26.1, 18.x runtimes; iPhone 17 present | — |
| Maestro | D-09 / D-10 flows | ✓ | `/opt/homebrew/bin/maestro` (08 used 1.35.0) | `xcrun simctl io screenshot` + manual taps |
| SwiftGen | Build phase | ✓ | 6.6.3 | — |
| `GoogleService-Info.plist` | App build | ✓ (local, not committed) | — | CI creates a mock |
| jq | `xcresult-assert-tests.sh` | ✓ | /usr/bin/jq | — |
| gh | PR / CI replay | ✓ | /opt/homebrew/bin/gh | — |
| JDK / Gradle | Framework link, Kotlin tests | ✓ (`/usr/bin/java`; wrapper in repo) | — | — |
| Built `shared.framework` / `shared.h` | Swift compile, names gate | ✓ stale | built from this branch | Rebuild: `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` |

**Missing dependencies with no fallback:** none.
**Note:** the v1.0 App Store/TestFlight build is not needed; `main` plus the `iOS_1_0_*` tags cover the "installed v1.0" UAT requirement. CI Xcode may differ from the local 27.1 (ios.yml selects the latest installed Xcode); the spike must also pass in CI.

## Validation Architecture

### Test Framework
| Property | Value |
|----------|-------|
| Framework | Swift Testing (Xcode toolchain), app-hosted target `DevFestNantesTests`; Kotlin: `kotlin.test` + Turbine (`jvmTest`, `iosSimulatorArm64Test`) |
| Config file | `iosApp/iosApp.xctestplan` (plan `iosApp`); Gradle per module |
| Quick run command | `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -testPlan iosApp -destination 'platform=iOS Simulator,id=<UDID>' -derivedDataPath build -only-testing:DevFestNantesTests/<Suite> test CODE_SIGNING_ALLOWED=NO` [CITED: 08-VALIDATION.md] |
| Full suite command | `build-for-testing`, then `test-without-building -parallel-testing-enabled NO -resultBundlePath <R>`, then `bash scripts/xcresult-assert-tests.sh <R>` (expects `XCRESULT-OK`); plus `./gradlew :shared:iosSimulatorArm64Test :core:domain:iosSimulatorArm64Test :core:data:iosSimulatorArm64Test` and the gates |

### Phase Requirements → Test Map
| Req ID | Behavior | Test Type | Automated Command | File Exists? |
|--------|----------|-----------|-------------------|-------------|
| IOSDATA-01 | `IosDependencies` accessors resolve (6) | Kotlin iosTest | `./gradlew :shared:iosSimulatorArm64Test` | ❌ extend `IosKoinTest` |
| IOSDATA-01 | Swift sources hold no filter/group/persist logic, `SessionFilter.swift` absent | script | `test ! -e iosApp/iosApp/Model/SessionFilter.swift && ! grep -rn 'AgendaSessionFilters\|"Favorites"' iosApp/iosApp --include='*.swift'` | ❌ Wave 0 (add as a verification step, optionally a gate) |
| IOSDATA-01 | New references to Kotlin names covered | gate | `bash scripts/swift-names-gate.sh check && bash scripts/swift-names-gate.sh selftest` | ✅ |
| IOSDATA-01 | Mapping preserves v1.0 days/slots/sessions (golden) | Swift Testing | `-only-testing:DevFestNantesTests/AgendaCharacterizationTests` (rewritten) | ❌ rewrite, same goldens |
| IOSDATA-01 | Classification via shared use case (table) | Swift Testing | `-only-testing:…/AgendaCharacterizationTests` (`is_a_talk` table + `isNavigable`) | ✅ extend |
| IOSDATA-01 | Speakers A–Z golden unchanged via `SpeakerIndexUseCase`; venue language | Swift Testing | `-only-testing:…/SpeakersVenueCharacterizationTests`, `SpeakersViewModelTests`, `VenueViewModelTests` | ✅ adapt |
| IOSDATA-02 | Legacy keys never read: VM seeds only from the port | Swift Testing | `-only-testing:…/AgendaViewModelTests` (fake port; assert no `UserDefaults` dependency in VM init) | ❌ Wave 0 |
| IOSDATA-02 | Store switch: set → kill → relaunch persists; update over `main` starts empty | manual UAT (agent-driven + human checkpoint) | `xcrun simctl install` + Maestro in-place flow | ❌ Wave 0 (flow without `clearState`) |
| IOSDATA-03 | Real Kotlin continuous flow reaches Swift (filters + bookmarks) | Swift Testing spike | `-only-testing:…/KotlinContinuousFlowTests` | ❌ Wave 0 |
| IOSDATA-03 | Re-emission on bookmark/filter change; trace once; keep content on resubscribe; mid-stream error keeps content; pre-emission error ends loading; mirror write-through; cancellation silent; single VM instance | Swift Testing | `-only-testing:…/AgendaViewModelTests` | ❌ rewrite on new port + `FakeStream<[DaySchedule]>` |
| IOSDATA-03 | Scroll target for both days with injected now and Paris calendar | Swift Testing | `-only-testing:…/AgendaScrollTargetTests` | ❌ Wave 0 |
| Parity (D-10) | Visual parity main vs branch; room order only | manual (Maestro screenshots + human) | `maestro --device <udid> test` on both sims | harness exists in 08-parity (adapt) |

### Sampling Rate
- **Per task commit:** the touched Swift suite via `-only-testing`, `bash scripts/swift-observation-gate.sh`; for Kotlin changes `./gradlew :shared:iosSimulatorArm64Test` and `bash scripts/di-gate.sh`.
- **Per wave merge:** full Swift suite + `swift-names-gate.sh check` + `kmp-nc-version-gate.sh` + `./gradlew detekt lint testDebugUnitTest jvmTest testAndroidHostTest`.
- **Phase gate:** CI green on `ios.yml` and `android.yml`, `XCRESULT-TESTS total=` strictly consistent with the intended count (deleted suites reduce it; record the new total), D-09 and D-10 checkpoints approved.

### Wave 0 Gaps
- [ ] `iosApp/DevFestNantesTests/KotlinContinuousFlowTests.swift` — the research-flag spike (IOSDATA-03), written first.
- [ ] `iosApp/DevFestNantesTests/Fakes/FakeAgendaSource.swift` — new port shape (scripted `FakeStream<[DaySchedule]>`, filters/bookmarks snapshots and write recorders, classify stub).
- [ ] `AgendaScrollTargetTests.swift` — D-11.
- [ ] In-place update Maestro flow (no `clearState`) under `.planning/phases/09-…/09-parity/`.
- [ ] `IosKoinTest` accessor cases.
- [ ] Rebuild `shared.framework` before any Swift compile check.

## Security Domain

### Applicable ASVS Categories

| ASVS Category | Applies | Standard Control |
|---------------|---------|-----------------|
| V2 Authentication | no | — |
| V3 Session Management | no | — |
| V4 Access Control | no | — |
| V5 Input Validation | yes | Persisted values are untrusted: Kotlin stores accept only arrays of strings and skip entries that do not decode (`decodeOrNull`); Swift must not parse them itself [VERIFIED: UserDefaults*Store.kt `load()`] |
| V6 Cryptography | no | Preferences are non-sensitive (session ids, filter enum names) |
| V7 Logging | yes (light) | Never log persisted payloads; `DevFestLogger` for errors (stores already avoid logging payloads) |
| V8 Data Protection | yes (light) | Legacy keys remain in the app container, unread; contain no secrets |

### Known Threat Patterns for this stack

| Pattern | STRIDE | Standard Mitigation |
|---------|--------|---------------------|
| Corrupt or hostile `UserDefaults` content (wrong types, huge arrays) | Tampering / DoS | Existing tolerant Kotlin `load()`; Swift only calls store APIs |
| Silent data loss on update (D-15) | Denial of availability of user data | Intentional, documented; revertible commit behind `checkpoint:decision`; legacy keys untouched so a migrator can be added later |
| Stale mirror drift (UI shows state different from storage) | Repudiation of UI state | Single writer; re-seed on appear; tests for write-through |
| Payload leakage through logs | Information disclosure | Log counts and ids only, never raw store content |

Config has `security_enforcement: true`, ASVS level 1; no new network surface, entitlements or dependencies are introduced.

## Sources

### Primary (HIGH confidence, read this session)
- `.planning/phases/09-…/09-CONTEXT.md`, `09-UI-SPEC.md`, `09-DISCUSSION-LOG.md`; `.planning/REQUIREMENTS.md`, `.planning/STATE.md`
- `shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework/Headers/shared.h` (lines 256-306, 323-358, 380-410, 532-615, 682-790, 984-1092, 1293-1320, 1395-1397, 1479-1489, 1686-1693, 1813)
- `shared/src/iosMain/…/IosDependencies.kt`, `shared/src/commonMain/…/InitKoin.kt`, `shared/src/iosTest/…/IosKoinTest.kt`, `shared/build.gradle.kts`
- `core/domain/src/commonMain/**` (ObserveAgenda, Group, Filter, Classify, DayAssignment, DaySchedule, SpeakerIndex, VenueLanguage, DomainModule); `core/data/src/{commonMain,iosMain}/**` (BookmarksStore, SessionFiltersStore, SessionFilterCodec, UserDefaults stores, DataPlatformModule.ios.kt); `core/model` SessionFilter, ScheduleSlot
- `iosApp/iosApp/**` (Agenda/*, Model/*, UI/*, Performance/*, iOSApp.swift) and `iosApp/DevFestNantesTests/**`
- `core/domain/testdata/agenda/README.md`, `rooms.snapshot.json`; `scripts/swift-names-gate.sh`, `scripts/swift-names/*`, `scripts/swift-observation-gate.sh`, `scripts/xcresult-assert-tests.sh`; `.github/workflows/ios.yml`; `documentation/DEVELOPMENT.md`, `documentation/ARCHITECTURE.md`
- `.planning/phases/08-…/08-parity/parity-flow.yaml`, `08-VERIFICATION.md`, `08-VALIDATION.md`, `08-09-SUMMARY.md`
- Local environment probes: `xcodebuild -version`, `xcrun simctl list devices available`, `which maestro swiftgen jq gh java`, `git log`/`git tag`

### Secondary / Tertiary
- None. No web or Context7 research was needed: the phase consumes only in-repo APIs, and the library behaviors that matter (KMP-NativeCoroutines native-flow shape) are demonstrated by the generated header and the existing bridge. No cache entries were written (`research-store` not used).

## Metadata

**Confidence breakdown:**
- Standard stack / API surface: HIGH — read from the built header and sources this session (the header must be rebuilt after the Kotlin accessor change).
- Architecture: HIGH for structure and deletions, MEDIUM for the continuous-flow runtime behavior (spike).
- Pitfalls: HIGH for gate/baseline/shadowing/TZ/Maestro items (verified in files), MEDIUM for A1, A4-A7.

**Research date:** 2026-11-05
**Valid until:** 2026-12-05 (stable; invalidated by a KMP-NativeCoroutines or Kotlin bump, or by changes to `core/domain` exports)
