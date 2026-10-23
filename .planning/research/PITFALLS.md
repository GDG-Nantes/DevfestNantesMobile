# Domain Pitfalls

**Domain:** Modernizing the iOS side of an existing Kotlin Multiplatform app (DevFest Nantes): shared KMP domain layer, local SPM packages, explicit Swift DI, `@Observable`, Swift 6 strict concurrency, iOS tests in CI. Strict behavior parity (no user-visible change, no lost favorites).
**Milestone:** v1.1 iOS Architecture
**Researched:** 2026-10-23
**Overall confidence:** MEDIUM-HIGH. Repo-specific findings (marked "verified in repo") come from reading `iosApp/`, `shared/`, `core/`, `ios.yml` and `gradle/libs.versions.toml`. Swift/SPM/Kotlin-Native behavior comes from training knowledge plus a few web checks, so items tagged MEDIUM or LOW need a spike before the phase commits to them.

Confidence tags: HIGH = verified in this repo or official docs, MEDIUM = well-known behavior not re-verified against current toolchains, LOW = inference or unverified.

---

## Repo facts that drive the pitfalls (verified in repo)

| Fact | Why it matters |
|------|----------------|
| `shared` is a **static** framework (`isStatic = true`), exports `core:model/data/analytics`, and is built by an Xcode **Run Script** (`./gradlew :shared:embedAndSignAppleFrameworkForXcode`) on the app target. Linker flag `-ObjC` is set on the app. | A Run Script on the app target runs after SPM package targets compile. Packages cannot `import shared` unless the framework already exists. A static framework linked from two places duplicates the Kotlin runtime. |
| Test target `DevFest NantesTests` is **hosted** (`TEST_HOST`/`BUNDLE_LOADER` = the app). The only test class references `DevFestViewModel`, which no longer exists. CI never runs it. | Hosted tests launch `iOSApp.init` (Firebase configure, Koin start, Remote Config fetch). The current test file does not compile. |
| `@ObservedObject var viewModel = AgendaViewModel()` as a **default property value** in `AgendaView`, `AgendaCellView`, `AgendaDetailView`, `SpeakersView`, `AboutView`, `VenueView`. `AgendaCellView` and `AgendaDetailView` each build their **own** `AgendaViewModel()`. | Today, favorites are consistent across these instances only because they all read `UserDefaults`. This is the exact "ViewModel re-creation" trap, and it is already live. |
| `AgendaViewModel` takes the **first** emission of `store.sessions`/`store.speakers` and `break`s. `observeRooms()` starts an unstructured `Task {}` that is never cancelled and is re-created on each `.task` run. Section grouping uses `ISO8601DateFormatter` (no fractional seconds) with `?? Date()` fallback, labels use device-timezone `DateFormatter("yyyy-MM-d")`, rooms sort with string `<`. | These are the behaviors a shared domain layer must reproduce, or consciously change. |
| Favorites = `UserDefaults["Favorites"]` as `[String]`. Filters = `UserDefaults["AgendaSessionFilters"]` as JSON `Data` of `Set<SessionFilter>` with lowercase `type` (`bookmark`, `complexity`, `language`, `room`, `type`). | Android persists the same concepts differently: `SharedPreferences` `selected_sessions` (string set) and `SHARED_PREFERENCES_KEY_AGENDA_FILTERS` (string set of kotlinx-serialization JSON, **uppercase** enum names `BOOKMARK`...). `BookmarksStoreImpl` and `SessionFiltersServiceImpl` are `androidMain`/Android-only today. `dataPlatformModule` on iOS is empty. |
| Swift pins `KMP-NativeCoroutines` to exactVersion `1.0.0-ALPHA-45` while Gradle uses plugin `1.0.6`. The package is `swift-tools-version:5.3`. | Version skew between the Swift package and the Gradle plugin. The Swift runtime library is not Swift 6 annotated (MEDIUM). |
| Globals/singletons: `var remoteConfig = RemoteConfig.remoteConfig()` (global var), `RCValues.sharedInstance`, `PerformanceMonitoring.shared` plus `private static var appStartupTrace`, `FirebaseAnalyticsService.shared`, `IosDependencies.shared`. `SWIFT_VERSION = 5.0`, deployment targets mixed 15.0/17.0. | Every one of these is a Swift 6 error or a DI-violation candidate. |
| SwiftGen generates `Strings.swift`/`Assets.swift` (git-tracked) from an **app build phase**, using `structured-swift4`/`swift4` templates, whose lookups are `Bundle(for: BundleToken.self)`. | Breaks as soon as strings/assets move into a package (resources live in a different bundle). Old templates may not be Swift 6 clean. |
| `scripts/` has no swift-names gate. `swift-names-gate.sh` lives in `.planning/milestones/v1.0-phases/03-.../` and is **not wired into CI** (STATE.md lists this as follow-up CICD-V2-01). | The v1.0 halt (Swift name identity swap) can recur silently when new shared types are added. |
| `ios.yml` caches `~/Library/Developer/Xcode/DerivedData` with a key based on `project.pbxproj` and Kotlin sources, **not the Xcode version**, while Xcode is selected dynamically ("latest installed"). It builds only; it never runs `xcodebuild test`. | Stale-module-cache flakiness once tests are added; no safety net during the refactor. |

---

## Critical Pitfalls

Mistakes that cause rewrites, data loss, or a milestone that stalls mid-way.

### Pitfall 1: Favorites/filters lost or silently reset by the migration (UserDefaults to shared)
**What goes wrong:** Users upgrade and their starred sessions or filters are gone, or reappear after being removed, or are overwritten with an empty set.
**Why it happens (specific to this repo):**
- Storage choice. If shared persistence on iOS uses a file in `cachesDirectory` (or a temp path), the OS can purge it and it is excluded from backups. `UserDefaults` is backed up and restored. DataStore-style KMP storage needs an explicit Documents/Application Support path.
- Type-mismatch silent defaults. Current code reads `defaults.object(forKey: "Favorites") as? [String] ?? []`. A migration written the same way cannot tell "no favorites" from "unreadable favorites".
- "Migrated" flag set before the write is verified, or set when the source was unreadable. The user is then locked into an empty store.
- Overwrite instead of merge. If the shared store already has entries (second launch, TestFlight downgrade/upgrade), a copy-over deletes newer data.
- Format mismatch between platforms. iOS filters use lowercase `type`; Android's kotlinx-serialization uses uppercase enum names. If the shared `SessionFilter` model is unified, one platform's persisted shape changes. On Android that is a **regression risk** for existing installs.
- Race with first render. `AgendaViewModel.init` currently rewrites favorites; a new async migration that finishes after the first screen renders can flash an empty list, and a user tap in that window gets overwritten.
- Deleting the legacy keys immediately removes any rollback path.
**Consequences:** Direct violation of the milestone's one hard rule ("no favorites lost"). Hard to detect: nobody sees it until a returning attendee opens the app next year.
**Prevention:**
1. Decide the persistence backend first, in a spike, with this ranking: (a) shared code reads/writes the **same** `NSUserDefaults` keys in the same shape (zero migration, lowest risk); (b) a new store plus a one-shot copy migration. Choose (b) only with a stated reason.
2. If (b): migration is synchronous, runs at the composition root **before** any ViewModel or store read, is idempotent, merges (set union) instead of overwriting, only sets the "migrated" flag after read-back verification, and **keeps the legacy keys** for at least one release.
3. Treat three states separately: key absent (nothing to migrate), key present and valid, key present but wrong type or undecodable (do not mark migrated, log it, keep the source).
4. Keep the Android on-disk shape byte-compatible: same `SharedPreferences` file, same keys, same JSON field names/case. Put a persistence-pinning test in place **before** moving the code (v1.0 already added "persistance SharedPreferences épinglée"; extend it to filters).
5. Make the filter mapping explicit and case-aware (`bookmark` to `BOOKMARK` and so on). Do not let `rawValue` strings become an accidental contract.
6. Test matrix (Swift Testing + a Kotlin commonTest): absent, empty array, valid, duplicates, wrong type, filters with an unknown type (current behavior drops the whole set because of `try?`; decide whether to preserve or improve, record it as deliberate), large lists, migration run twice, migration after the shared store already holds data.
7. Manual UAT step: install the **v1.0 release build** on a simulator, star sessions and set filters, install the new build **over** it (`xcrun simctl install` preserves data), confirm both are intact; then repeat after force-quit.
**Detection:** Favorites count drops after upgrade; the "bookmark" filter shows an empty agenda; `Favorites` key still present but the app shows none.
**Phase:** Shared favorites/filters phase (domain extraction). Spike the backend choice at phase start. Needs a dedicated verification gate.
**Confidence:** HIGH (code read), prevention MEDIUM.

### Pitfall 2: Kotlin/Native `shared` framework linked more than once (duplicate symbols, two Kotlin runtimes, two Koin graphs)
**What goes wrong:** Build succeeds or fails with duplicate-symbol errors, or builds and then crashes/misbehaves: "Class X is implemented in both", failed casts between "the same" Kotlin type, `KoinPlatform` showing as not started or started twice, two memory managers.
**Why it happens:** `shared` is a **static** framework. A static library or framework linked into several binaries is copied into each. With local SPM packages this happens when (a) several feature packages each link it and they end up in different dynamic libraries; (b) a hosted test bundle links it while the host app also contains it; (c) a package test target links it and also loads the app host. The Kotlin runtime and all `object` singletons (including Koin's global) are duplicated in that case.
**Consequences:** Intermittent test failures, startup crashes, or a Koin singleton that is "not initialized" in one copy. Hard to diagnose.
**Prevention:**
- Exactly **one** module in the dependency graph owns the Kotlin framework (a `SharedKit`/`KMPBridge` package that vends it as a `.binaryTarget`). All other packages depend on that product and let SPM link it once into the app.
- Keep package products **static libraries** (SPM default `automatic` is fine) so the final app is the single link point. Avoid `.dynamic` library products unless there is a reason.
- Do not make the test bundle both hosted and independently linking `shared`. Prefer **unhosted** package test targets (no `TEST_HOST`).
- Do not introduce a second Kotlin framework (for example a `sharedTesting` framework). Kotlin/Native frameworks do not share a runtime or type identity. This is why v1.0 built the single umbrella.
- Add a CI sanity check: `nm`/`otool -L` on the built app to confirm one copy of `Kotlin_initRuntimeIfNeeded` (or equivalent), and a startup test that asserts `IosDependencies.start()` is idempotent.
**Detection:** Linker `duplicate symbol` for `_Kotlin_*`/`kfun:*`; runtime log line "Class ... is implemented in both"; `NSInvalidArgumentException` unrecognized selector on a Kotlin type.
**Phase:** SPM modularization phase.
**Confidence:** MEDIUM (mechanism well known; specific symbol names not re-verified).

### Pitfall 3: Gradle-built framework cannot be seen by SPM package targets (build order, search paths, previews)
**What goes wrong:** Packages fail with `No such module 'shared'`, the first clean build fails but the second passes, CI passes but a fresh clone does not, SwiftUI previews of package views never build.
**Why it happens:** Today the framework is produced by a Run Script phase of the **app** target, and Xcode finds it in `BUILT_PRODUCTS_DIR`. SPM package targets compile as dependencies of the app target, i.e. **before** its Run Script phases. A package target also cannot set `FRAMEWORK_SEARCH_PATHS` through `Package.swift` (only `unsafeFlags`, which are allowed for local packages but fragile). Previews build the package scheme and may skip or sandbox the Gradle script. A `.binaryTarget(path:)` pointing at a Gradle output that does not exist yet makes **package resolution itself** fail (`xcodebuild -resolvePackageDependencies` in CI runs after the framework build today, but a clean checkout in Xcode does not).
**Prevention (pick one and prove it in a spike before anything else):**
- **Recommended:** produce an **XCFramework** with Gradle (`assembleSharedDebugXCFramework`, needs simulator and device slices; the convention plugin already declares `iosX64`/`iosArm64`/`iosSimulatorArm64`) and expose it through a local `SharedKit` package with `.binaryTarget(path: "../../shared/build/XCFrameworks/debug/shared.xcframework")`. Add a **build-tool step that runs before package resolution**: an Xcode scheme "Build > Pre-actions" entry and/or an aggregate target that every package-consuming target depends on, plus a documented `./gradlew` bootstrap command and a clear failure message when the XCFramework is missing.
- Debug vs release: keep a separate configuration-specific path; do not hardcode `debug/` in `Package.swift`. Release archive scripts (`scripts/build-ios-release.sh`) must be updated in the same change.
- Keep the Gradle task incremental and cached, and keep `ENABLE_USER_SCRIPT_SANDBOXING = NO` for the Gradle script (the project already works without it set; confirm after the migration since Xcode 26 templates default to YES).
- Limit `import shared` to **as few packages as possible** (ideal: the bridge/DI package plus mappers). Feature packages depend on small Swift-native protocols and `Sendable` value models (see Pitfall 5). Previews and Swift tests then never need the Gradle build, and the swift-names collision surface shrinks.
- Make iOS 17 minimum explicit in each `Package.swift` (`platforms: [.iOS(.v17)]`) and in the project targets in the same commit.
**Detection:** `xcodebuild -resolvePackageDependencies` fails on a clean clone; previews show "Cannot find module"; works only after a manual Gradle run.
**Phase:** SPM modularization phase (first plan: a build-order spike; do not split features until it is green on CI from a clean checkout).
**Confidence:** MEDIUM (consistent with community reports that `embedAndSign...` runs too late for SPM modules; a Kotlin Slack thread and the Kotlin docs recommend XCFramework + binary target for SPM). Needs a spike.

### Pitfall 4: ViewModel re-creation and wrong ownership when moving to `@Observable`
**What goes wrong:** State resets mid-session (selected day, scroll position, filters flash, in-flight loads restart), duplicate Flow collectors, or the data fetch fires many times.
**Why it happens (and it is already latent here):**
- `@ObservedObject var viewModel = AgendaViewModel()` as a default value creates a **new instance every time the parent view struct is re-initialized**. `@Observable` has no `@ObservedObject` equivalent: a plain `var vm = AgendaViewModel()` in a view is *also* recreated on every re-init. Ownership requires `@State`.
- `@State var vm = VM(...)`: SwiftUI keeps the first instance, but the initializer expression **still runs on every view re-init** and the result is discarded. If `init` has side effects (starts a Flow collection, hits Firebase, reads `UserDefaults`), you pay it repeatedly and can leak collectors.
- `AgendaCellView` and `AgendaDetailView` each create **their own** `AgendaViewModel()`. After the move there is one shared favorites source of truth; a naive port gives each instance its own collector and its own state.
- `@StateObject` with a custom init (`SpeakerDetailsView`) becomes `@State` initialized via `State(initialValue:)` in `init`; parameter changes (a new speaker id) do **not** recreate it. Use `.task(id:)` or `.id(...)`.
- `@Environment(Type.self)` for injection **traps at runtime** if the object is missing (previews, tests, a forgotten `.environment(...)`), unlike a compile-time init parameter.
- Bindings: `$viewModel.x` on an `@Observable` needs `@Bindable`; passing the VM down as a plain `let` works for reads only.
- `LoadingView(isShowing: $viewModel.isLoading)` and `onChange(of:)` single-parameter closures are used today; the single-parameter `onChange` is deprecated at iOS 17 (warning noise that conflicts with a "no warning" Swift 6 goal).
- Property observers (`didSet` on `sessionFilters`) and `lazy` are poor fits for `@Observable` macro-tracked properties; the filters leave the VM anyway.
**Prevention:**
1. Rule: **a view that owns a VM holds it in `@State`; children receive it as `let`/`@Bindable`/environment, never construct it.** Construct VMs only in the composition root or a factory closure passed down.
2. VM `init` is pure (store references, no I/O, no collection). All work starts in `.task {}` / `.task(id:)`.
3. Mark non-UI properties `@ObservationIgnored` (store, tasks, formatters, loggers).
4. Replace the three-way `AgendaViewModel()` with one instance owned at the Agenda screen root and passed to row/detail views.
5. Add a Swift Testing test counting `init` calls via an injected factory, and a smoke test that toggles a favorite from the detail view and asserts the list view updates.
6. Preserve the semantics that users can see: keep `.task` triggering rules (current code fetches again each time the tab appears; decide deliberately whether to keep that).
**Detection:** Selected agenda day jumps back to the default; filters reset on tab switch; log shows N "Agenda sessions loaded" lines per launch.
**Phase:** `@Observable` migration phase. Do it screen by screen, after DI is explicit.
**Confidence:** HIGH for the repo state; the SwiftUI rules are MEDIUM-HIGH.

### Pitfall 5: Swift 6 Sendable/actor-isolation wall from Kotlin/Native types and from Kotlin Flow bridging
**What goes wrong:** Flipping `SWIFT_VERSION = 6` produces hundreds of errors ("non-Sendable type ... cannot cross actor boundary", "sending risks causing data races", "var is not concurrency-safe"), and the temptation is to sprinkle `@unchecked Sendable`, which hides real races.
**Why it happens:**
- Kotlin/Native exports classes as ObjC `NSObject` subclasses with **no** `Sendable` annotations. Every Kotlin type is non-Sendable from Swift's point of view. Passing a `Session` across `Task`, actor or `@MainActor` boundaries is flagged.
- KMP-NativeCoroutines is built as `swift-tools-version:5.3`; its `NativeSuspend`/`NativeFlow` closure types and `asyncSequence(for:)` are not annotated for `Sendable`/`sending` (MEDIUM). Calls from a `@MainActor` VM may warn or error depending on region isolation. Trace-style closures like `performanceMonitoring.trackDataLoad { ... asyncSequence(for: self.store.sessions) ... }` capture `self` and a Kotlin store inside a `@Sendable`-looking closure.
- Existing globals and singletons fail outright: `var remoteConfig` (global mutable), `static let shared` of non-Sendable classes (`PerformanceMonitoring`, `RCValues`, `FirebaseAnalyticsService`), `private static var appStartupTrace`, `PerformanceMonitoring: ObservableObject`.
- `DispatchQueue.main.async { self.x = ... }` inside an already `@MainActor` method is an error source and an anti-pattern (the work should simply be on the actor).
- Firebase's Swift types are not all Sendable (`Trace`, `RemoteConfig`); Swift 6 on SDK 11.15 needs `@preconcurrency import` (MEDIUM).
- Upcoming-feature drift: Swift 6.2's default-actor-isolation and `nonisolated(nonsending)` change what "async nonisolated" means. Package targets and the app target can disagree if only one opts in.
- SwiftGen output (`structured-swift4`) may contain non-Sendable statics (LOW, check when upgrading).
**Prevention:**
1. **Stage it.** Enable `SWIFT_STRICT_CONCURRENCY = complete` in Swift 5 mode first, fix warnings per package, and only then flip the language mode (`swift-tools-version: 6.0` + `swiftLanguageModes: [.v6]` per package; `SWIFT_VERSION = 6.0` for the app). Do not do the flip in one commit.
2. **Quarantine Kotlin types.** One bridge layer converts Kotlin objects to Swift `struct`s that are `Sendable` (ids, strings, dates, enums) *on the way in*, in one isolated hop. ViewModels and views consume Swift values only. This is also the cleanest answer to name collisions (Pitfall 9), to previews, and to fakes (Pitfall 15).
3. Isolate with intent: `@MainActor @Observable final class` ViewModels; use cases wrapped in a `Sendable` Swift protocol (`protocol AgendaRepository: Sendable`); where a Kotlin object must cross, wrap it in a tiny, documented `struct KotlinBox<T>: @unchecked Sendable` in the bridge only, never in features.
4. `@preconcurrency import shared` / `FirebaseXxx` is acceptable as a *temporary, listed* escape hatch with an issue per use; ban `@unchecked Sendable` outside the bridge via a lint check (grep gate in CI, like `di-gate.sh`).
5. Replace singletons by instances created in the composition root (`PerformanceMonitoring`, `RCValues`, analytics) and hand them to VMs as protocol-typed dependencies. Global `remoteConfig` disappears.
6. Remove `DispatchQueue.main.async` from VMs.
7. Run one spike on the real bridge first: one VM with `asyncSequence(for:)` and `asyncFunction(for:)` under complete checking, to learn what KMP-NativeCoroutines `1.0.6` actually emits, then size the phase from that result. If the library is a blocker, the fallback is hand-written `AsyncStream` wrappers around Kotlin collectors in the bridge package.
**Detection:** Warning/error counts when `SWIFT_STRICT_CONCURRENCY=complete`; any `@unchecked Sendable` outside the bridge.
**Phase:** Swift 6 phase (after DI and `@Observable`). Spike early; the outcome affects the shared API design (Pitfall 7).
**Confidence:** MEDIUM (general Swift 6 behavior and the library's tools-version are verified; exact diagnostics need a spike). A web check found no authoritative Swift 6 note in KMP-NativeCoroutines releases.

### Pitfall 6: Flow collection lifetime, cancellation and leaks in `@Observable` ViewModels
**What goes wrong:** Collectors that keep running after the screen is gone, duplicate collectors on each appearance, UI updates after dismissal, or a collector that never starts because the task was cancelled too early. Kotlin keeps a coroutine alive (and the store, and the list) as long as the Swift `Task` is.
**Why it happens (specific to this repo):**
- `observeRooms()` starts an unstructured `Task { for try await ... }` inside an `async` method, so `.task` cancellation does **not** cancel it, and each re-appear adds another collector (verified in repo).
- Sessions/speakers take only the first emission (`break`), while a shared use case will likely expose a continuous `Flow` combining sessions, filters and bookmarks. Switching to live emissions changes behavior: list updates mid-scroll, re-sorting, loading flag transitions, a repeat `agenda_load` Firebase Performance trace.
- Storing a `Task` in a `@MainActor` class and cancelling it in `deinit` runs into Swift 6 isolation: `deinit` is nonisolated, and **isolated deinit needs a newer OS than the iOS 17 floor** (MEDIUM), so `task?.cancel()` on a MainActor-isolated property will not compile cleanly or is unsafe.
- Cancellation must propagate through the bridge. `asyncSequence(for:)` cancels the Kotlin collection when the Swift task is cancelled, but a wrapper that catches `CancellationError` and logs it as an error will pollute Crashlytics (the VMs log errors with `DevFestLogger` which forwards to Crashlytics).
- `Dispatchers.Main` in the Kotlin collector plus a Swift `@MainActor` consumer: blocking the main thread (`wait(for:)`, semaphore) while Kotlin wants to dispatch on Main deadlocks (the existing test uses `wait(for: [expectation], timeout: 0.1)`).
**Prevention:**
- Drive all collection from the view with `.task`/`.task(id:)` so lifetime equals view lifetime and cancellation is structural. No unstructured `Task {}` in VMs; if one is unavoidable keep a handle and cancel it from `onDisappear`/a `stop()` method (not `deinit`).
- Treat `CancellationError` (and Kotlin `CancellationException`) as non-errors; never log them to Crashlytics.
- Preserve "first emission only" for the screens that have it today unless the roadmap explicitly accepts the change (record it in `REQUIREMENTS` as an intentional exception). Prefer a shared use case that exposes both a one-shot and a stream form.
- Add a Swift Testing test: start the collector, cancel the task, assert the fake Flow subscriber count reaches 0 (fakes in Kotlin or the Swift port expose `activeCollectors`).
- Keep heavy transformation in Kotlin and emit once per change; bridging large lists converts to `NSArray` on every emission.
**Detection:** A fake store whose active-subscription count grows when switching tabs; repeated "Observe Rooms" log lines; Crashlytics non-fatals with cancellation text.
**Phase:** Domain-layer API design (what the Flow shape is) plus the `@Observable` phase.
**Confidence:** HIGH (repo), MEDIUM (isolated-deinit availability).

### Pitfall 7: Exposing the shared domain API in a way Swift handles badly (crashes, boxing, sealed types, missing exports)
**What goes wrong:** Uncatchable crashes on iOS, `KotlinInt`/`KotlinLong` boxing noise, sealed hierarchies that Swift cannot switch exhaustively, new types missing from the framework header, or Android breaking because the iOS shape leaked into common code.
**Why it happens:**
- A non-`suspend`, non-`Flow` Kotlin function that throws is an **uncatchable** crash from Swift unless annotated `@Throws` (the existing `IosDependencies.store()` comment already warns about uncatchable Kotlin exceptions). KMP-NativeCoroutines wraps suspend/Flow, not plain functions.
- A new `:core:domain`-style module must be added to the framework `export(...)` list **and** apply the nativecoroutines KSP plugin, otherwise its types appear only as opaque references or its `@NativeCoroutines` functions have no Swift counterpart. Adding it also changes `shared.h` (see Pitfall 9).
- `sealed class`/`sealed interface` become class hierarchies in Swift without exhaustive `switch`; use `enum class` or data classes with a discriminator for anything Swift must branch on (filter type, section kind).
- Generics over primitives box (`List<Int>` → `[KotlinInt]`), `kotlin.time.Instant`/`Duration` are not mapped to `Date`, value classes are mangled, default arguments do not exist in Swift, extension functions turn into static helpers on a `...Kt` class.
- `@NativeCoroutines` does not work on top-level functions; put use cases in classes/`object`s with `operator fun invoke` or named functions.
- Shaping the common API for iOS convenience (flattening, string dates) can degrade the Android ViewModels; the reverse (Android-shaped result types with `Pair`/`Triple`/`Map` generics) renders badly on iOS.
- Koin: every new use case must be registered once (`allowOverride(false)`); the Swift entry point (`IosDependencies`) must stay a **plain object** with typed accessors, no Koin types in `shared.h` (a v1.0 constraint). Do not expose a generic `get<T>()` to Swift.
**Prevention:**
- Define the domain API in terms of **simple immutable data classes and enums** with `String`/`Int`/`Boolean`/`List`/`Set` members. Dates as ISO `String` or epoch `Long` plus explicit helpers, matching what the Swift side parses today, unless the roadmap decides to move date formatting into Kotlin too.
- Annotate every plain function that can throw with `@Throws(Exception::class)` or return a result type.
- Add a "Swift surface" check to the phase's plan: build the framework and diff `shared.h` (Pitfall 9), and write one Swift Testing test per new use case that calls it through the bridge.
- Keep `core:testing` fakes **out of** the production framework export.
- Mirror the repo rule: a new store/use-case method is added in both `GraphQLStore` and `DevFestNantesStoreMocked` in the same change.
**Phase:** Domain extraction phase (API design review before implementation).
**Confidence:** MEDIUM-HIGH.

### Pitfall 8: Behavior drift when the Swift grouping/sorting logic becomes shared Kotlin
**What goes wrong:** The iOS agenda looks subtly different (day sections, session order, room order, time labels, empty/fallback sections), or the Android agenda changes because the "shared" implementation adopted the iOS semantics (or the reverse), violating strict parity on at least one platform.
**Why it happens (concrete differences in this repo):**
- iOS `getDate` uses `ISO8601DateFormatter()` with default options (no fractional seconds, requires a timezone designator). On a parse failure it silently falls back to `Date()` (now). Kotlin `Instant.parse` accepts fractional seconds and differs on malformed input. The fallback creates a "now" section that Kotlin will not.
- iOS groups sessions by exact start `Date`, then labels the section with a **device-timezone** `DateFormatter("yyyy-MM-d")`. Moving to kotlinx-datetime requires picking a time zone; `TimeZone.currentSystemDefault()` mirrors it, a fixed `Europe/Paris` does not. A phone set to another timezone (a speaker or attendee abroad, or a test device) shows different day labels today.
- iOS sorts sessions in a section with `$0.room < $1.room` (string, locale-insensitive per Swift `String <`), while Android has a `RoomSortIndex`. `RoomSortIndex` is listed as v1.0 deferred debt; unifying sort order changes one platform. Kotlin `String.compareTo` and Swift `String <` both compare by Unicode scalar/UTF-16 order, which differ for some accented characters (MEDIUM).
- `@State private var day = "2025-10-16"` is a hardcoded string in `AgendaView`. If the shared layer now provides "current day" logic, the initial scroll position changes.
- Dictionary iteration: Swift `Dictionary(grouping:)` order is random but keys are sorted afterwards; Kotlin `groupBy` keeps insertion order. Equal-key stability is not guaranteed in the Swift version (`sorted` is not documented stable, though it is in practice since Swift 5).
**Prevention:**
- **Characterization first** (the pattern that worked in v1.0): before moving any logic, write golden-master tests on the *current* platform implementations over the recorded fixtures in `core/model/.../stubs/` and over a **live API snapshot** (retrospective lesson 4: fixtures hide mapping gaps such as 110 speakers and room ids). Then make the shared implementation pass the same goldens on JVM and iOS simulator.
- Where the two platforms already disagree, **choose per platform, not per ideal**: expose a parameter (`TimeZone`, `SortStrategy`) so each platform keeps its behavior, and record any intentional unification as a deliberate, user-approved exception rather than a side effect.
- Take the timezone, locale and clock as injected parameters (testable, no hidden `Date()`).
- Keep ordering stable-sorted explicitly with a documented comparator; add tests with equal keys.
**Detection:** Golden diff in CI; a UAT script that compares agenda day tabs, first/last session per section and room order with screenshots from the previous build.
**Phase:** Domain extraction phase (characterization plan is plan 1).
**Confidence:** HIGH (read from code); the string-ordering note is MEDIUM.

---

## Moderate Pitfalls

### Pitfall 9: Swift name collisions in the shared framework recur (and the existing gate is not in CI)
**What goes wrong:** Adding domain types to `shared` flips which same-named class gets the `_` suffix, or collides with Swift/SwiftUI/Foundation names, breaking Swift compilation at a distance. This stopped v1.0 Phase 3 for two inserted plans.
**Why it happens:** Kotlin/Native resolves same-simple-name collisions across the whole framework by an alphabetical FQN tie-break. New candidate names collide with: Swift types already in `iosApp` (`SessionFilter`, `FilterType`, `AgendaContent.Session`), SwiftUI/Foundation (`Section`, `Group`, `Task`, `Result`, `Locale`, `Duration`, `Date`), and existing Kotlin names (`Category`, `Complexity`, `Session`, `Room` already need `shared.Category`). Enum entries that are Swift keywords become `break_`. Also, public extension functions on non-exported types leak types into `shared.h` (found in 03-02).
**Prevention:**
- Wire `swift-names-gate.sh` into `scripts/` and `ios.yml` **now**, before adding types (it currently lives in `.planning/milestones/v1.0-phases/03-.../` and is manual-only). Re-baseline against the current header first. Update its baseline files in the same commit as each intentional new type.
- Naming rule for new shared types: distinctive, domain-specific, and never SwiftUI/Foundation words (`AgendaDay` not `Section`, `SessionFilterCriteria` not `SessionFilter`, `BookmarkChange`). Rename the Swift-side types (`SessionFilter`, `FilterType`) in the same change that introduces Kotlin equivalents, or delete them.
- Mark non-API declarations `internal`; keep Apollo types `internal`/`@targetName`-renamed.
- Isolate `import shared` to the bridge package (Pitfall 3/5) so a collision breaks one package, not the whole app, and so the Swift names of Kotlin types are wrapped in Swift-owned names.
**Detection:** Gate output `SWIFT-NAME-MISSING`, `SWIFT-TYPE-COLLISION`, `SWIFT-MEMBERS-CHANGED`; ambiguous type lookup errors in Swift.
**Phase:** Before the domain extraction phase (gate wiring), then every phase touching `shared`.
**Confidence:** HIGH.

### Pitfall 10: SwiftGen resources and strings break when views move into packages
**What goes wrong:** Strings render as keys, images and colors are missing at runtime although the build is green, or the generated files stop compiling.
**Why it happens:** Generated `Strings.swift`/`Assets.swift` use `Bundle(for: BundleToken.self)`. In an SPM package, resources are copied into a separate `<Pkg>_<Target>.bundle` accessed via `Bundle.module`; `Bundle(for:)` on a statically linked class returns the **app** bundle. `Localizable.strings` (en/fr) in a package needs `defaultLocalization` in `Package.swift`. SwiftUI `Text("literal")` looks in the main bundle unless `bundle: .module`. The generation runs as an app build phase that writes into the app target. A SwiftGen SPM build-tool plugin runs sandboxed (cannot read outside the package), and requires `-skipPackagePluginValidation` in CI. The repo's templates (`structured-swift4`, `swift4`) are old.
**Prevention:**
- Decide one resource home: a `DesignSystem` (assets, colors, fonts) package plus feature-local strings, or one `Resources` package. Do not split a single `.xcassets` across packages.
- Verify at spike time whether SwiftGen 6.6.x templates accept a `bundle` parameter for `Bundle.module`. If not, use a custom stencil or move to Xcode-generated asset symbols / String Catalogs (scope creep; log it as an option, not a default).
- Keep generated files git-tracked (as now) so packages compile without SwiftGen on CI; keep CI's "generate if missing" caching logic consistent with the new paths (it caches `iosApp/iosApp/SwiftGen/*.swift`, which will move).
- Add a smoke test per package that resolves one string and one asset by key and fails on a missing result; add a French-locale test of one string.
- Add a `fr` snapshot check: `Localizable.strings` completeness between `en.lproj` and `fr.lproj`.
**Detection:** Raw keys on screen; `UIImage` nil; asset catalog warnings.
**Phase:** SPM modularization (core/DesignSystem split first).
**Confidence:** MEDIUM-HIGH.

### Pitfall 11: Firebase and the app/package boundary
**What goes wrong:** Duplicate symbol or duplicate framework errors, Crashlytics symbol upload not running, analytics disabled, or a mock-plist CI crash.
**Why it happens:** `FirebaseApp.configure()` and the `GoogleService-Info.plist` (not committed; mock in CI) belong to the **app target**. Packages that depend on `firebase-ios-sdk` must declare the same repository and compatible version range as the app (the app pins `minimumVersion 11.14.0`, `Package.resolved` is on 11.15.0), otherwise resolution conflicts. The "Firebase Crashlytics" run script phase, `-ObjC`, and dSYM upload (`scripts/extract-firebase-dsyms.sh`) must stay on the app target; Kotlin crashes also need the `shared` dSYM. The Consent ordering rule (v1.0: apply consent before Performance is enabled) must hold when `PerformanceMonitoring`/`RCValues` become injected instances: an eager initializer in the composition root reorders it. A moved `Package.resolved` (project-level vs workspace-level) can silently float Firebase to 12.x.
**Prevention:**
- Put Firebase behind small **protocols** in `Core/Analytics` (`AnalyticsReporting`, `PerformanceTracing`, `RemoteFlags`), implemented in one adapter package that depends on Firebase. Feature packages never import Firebase. Previews and tests use no-op implementations.
- Keep `FirebaseApp.configure()` first in `App.init`, before building the container; document the initialization order next to the container and test it (mirror `KoinStartupTest`, which only pins initializer order on Android).
- Commit `Package.resolved` at the new location and fail CI if it changes unexpectedly (`xcodebuild -resolvePackageDependencies` then `git diff --exit-code`).
- Do not touch the consent behavior while refactoring (the Android `PerformanceInitializer` consent bug is a known deferred item; do not "fix" it here and do not copy the pattern to iOS).
- Verify the release archive (`build-ios-release.sh`) uploads dSYMs and that Crashlytics receives a test crash in a TestFlight build before closing the milestone.
**Phase:** SPM modularization + DI phase.
**Confidence:** MEDIUM.

### Pitfall 12: Koin on iOS: start-up, idempotence, previews and tests
**What goes wrong:** Crash "KoinApplication has already been started" or `KoinApplication not started`; ViewModel created in a preview or test touches Koin before start; tests share one process-wide graph.
**Why it happens:** The Koin context is process-global. Today `IosDependencies.start()` is idempotent via `getKoinOrNull()` under a lock and `store()` self-starts, which hides ordering bugs. When Swift receives dependencies through an explicit container, the temptation is to remove the self-start and require the app entry point to call `start()`; previews and package tests then crash. Swift tests that want fakes cannot easily replace bindings (`allowOverride(false)` and no `stopKoin` exposed). A `Dispatchers.Main`-bound singleton created off the main thread, or a store resolved lazily from a background task, can race at first use.
**Prevention:**
- The composition root (`AppContainer`/`CompositionRoot`) is the **only** Swift file that calls `IosDependencies`; it builds all VMs/factories once and hands them down. The service-locator rule becomes enforceable by a grep gate (extend `di-gate.sh`: no `IosDependencies` outside the root and the bridge package).
- Keep `start()` idempotent. Add a Swift Testing test that calls it twice and from two threads.
- For tests and previews, do not start Koin: create the container with **Swift fakes of ports** (Pitfall 15). If real shared logic must run in tests (migration, use cases), expose a Kotlin-side factory that builds a graph from explicit arguments (in-memory settings, fake clock) without touching the global Koin, or add a test-only `reset` entry in an iosTest-only source set (not in the shipped framework).
- Keep Koin types out of `shared.h` (existing constraint); add typed accessors per use case instead of reflection.
- Run `scripts/di-gate.sh` and the existing Koin `verify()` tests whenever a binding is added.
**Detection:** Preview canvas crash on first render; duplicate-start exception in test logs.
**Phase:** DI phase (composition root) and test phase.
**Confidence:** MEDIUM-HIGH.

### Pitfall 13: Swift Testing specifics (parallelism, shared global state, hosted bundle)
**What goes wrong:** Tests pass alone and fail together, or pass locally and fail in CI. Tests run zero cases and report green.
**Why it happens:**
- Swift Testing runs tests in parallel by default, in one process. `UserDefaults.standard`, the process-wide Koin graph, and any singleton are shared across tests.
- Swift Testing does not support UI tests; mixing XCTest-style `setUp/tearDown` conventions with `@Suite` structs leads to state assumptions (a new `struct` instance is created per test, `init`/`deinit` replace setUp/tearDown).
- The existing hosted bundle starts the real app (`FirebaseApp.configure()` with a mock plist, Remote Config fetch, real Koin start) on every test run.
- The existing test file does not compile (`DevFestViewModel` missing) and must be removed or ported, not "fixed".
- `@MainActor` VMs require `@MainActor` tests or `await MainActor.run`.
**Prevention:**
- Inject `UserDefaults(suiteName: UUID().uuidString)` (or the shared store abstraction) per test; no `.standard`.
- Mark suites that touch global state `@Suite(.serialized)`, but better remove the global state.
- Use unhosted package test targets for ViewModels and migration. Keep a very small hosted/UI target only for the SwiftUI smoke tests.
- Guard `iOSApp.init` side effects behind an injected "environment" (`ProcessInfo` flag such as `XCTestConfigurationFilePath`) only as a last resort; the better fix is that nothing in packages needs the app host.
- Prefer deterministic async tests: `confirmation`, fake clocks, awaited state changes, never `sleep`/`wait(for:timeout:)` on the main thread (also see Pitfall 6).
**Phase:** Test and CI phase, but follow the rule from the first ViewModel conversion.
**Confidence:** MEDIUM-HIGH.

### Pitfall 14: CI: iOS tests flaky, slow, silently empty, or caches poisoned by Xcode drift
**What goes wrong:** Intermittent red from simulator boot, hours spent on stale module-cache errors after GitHub updates the runner image, 30-minute timeout hit once Gradle plus tests plus SwiftPM are in the same job, or a green run that executed 0 tests.
**Why it happens (verified in `ios.yml`):**
- The DerivedData cache key does not include the Xcode version, but the job selects the "latest installed" Xcode dynamically. A restored cache from a different compiler gives module-cache/PCH errors ("compiled module was created by a different version of the compiler").
- A single job: Gradle build, KMP iOS tests, SwiftGen, xcodebuild build. Adding `xcodebuild test` compiles the app a second time unless `build-for-testing`/`test-without-building` is used. `timeout-minutes: 30`.
- Package test targets are not run unless they are part of a shared scheme's test action or a test plan; `xcodebuild test -scheme iosApp` with only the old target runs 0 or 1 tests and passes.
- The simulator is resolved by **name**; a fresh runner's simulator may not be booted, causing launch timeouts. Cloning/erasing and parallel testing add flakiness on 3-4 core runners.
- Firebase and package resolution are slow and network-bound; the checkout has no package cache.
- Plugin/macro validation (`-skipPackagePluginValidation`, `-skipMacroValidation`) is needed if SwiftGen or other plugins run as SPM plugins.
**Prevention:**
- Add the iOS test lane **early** (see phase warnings), initially running the Swift tests that exist, so every refactor lands under a safety net.
- Cache key = `xcodebuild -version` + `Package.resolved` + `Package.swift` hashes + Kotlin source hash. Cache the SwiftPM `SourcePackages` directory separately (`-clonedSourcePackagesDirPath`).
- Use `xcodebuild build-for-testing` then `test-without-building`; select the simulator by UDID, run `xcrun simctl boot <udid>` and `bootstatus -b`; pass `-parallel-testing-enabled NO` initially, add `-retry-tests-on-failure -test-iterations 2` only for the UI smoke step, and keep retries off for the unit tests so flakes are visible.
- Use a shared scheme with a test plan listing every package test target, and add an assertion step that parses the `.xcresult` (`xcresulttool`) and fails if the executed test count is below a threshold.
- Upload the `.xcresult` as an artifact on failure.
- Split jobs if needed: (1) Gradle + XCFramework (upload as artifact), (2) Swift build/test downloading it. Do not raise the timeout as the first fix.
- Keep the dynamic Xcode/simulator resolution (v1.0 decision), and log selected versions into the job summary.
**Phase:** CI phase, with the lane stood up first.
**Confidence:** HIGH (workflow read), tool flags MEDIUM.

### Pitfall 15: Testability trap: Kotlin fakes unavailable in Swift, so tests drive the real store
**What goes wrong:** Swift Testing tests of ViewModels either hit the real GraphQL store (network, flaky) or can't be written; previews can't render without the Gradle framework.
**Why it happens:** The Kotlin fakes live in `core:testing` (Android/JVM focus) and are not exported to iOS (and shouldn't be, see Pitfall 2). The existing `MockDevFestNantesStore.swift` shows that hand-implementing a Kotlin interface from Swift is awkward (suspend functions become completion handlers, `@NativeCoroutines` Flows have different generated members).
**Prevention:** Put a Swift protocol layer (ports) between ViewModels and the bridge: `AgendaRepository`, `SpeakersRepository`, `FavoritesRepository`, `AnalyticsReporting`. The bridge package adapts the Kotlin use cases to them; tests and previews use trivial Swift fakes with `Sendable` structs and `AsyncStream`s. The cost is a small mapping layer; the benefit is tests, previews, Swift 6 containment and collision isolation in one move. Cover the adapters themselves with a few real-framework tests (iOS simulator, `commonTest`/Swift) to prove the mapping.
**Phase:** DI phase (shape the ports), tests phase.
**Confidence:** MEDIUM-HIGH.

### Pitfall 16: Android regressions when logic moves to commonMain
**What goes wrong:** Android agenda, bookmarks or filters change behavior, startup crashes, or release builds break while iOS "works".
**Why it happens:**
- Platform semantics differ between JVM and Kotlin/Native for string formatting, case conversion, collation and time zones; common code replacing JVM calls (`String.format`, `java.time`) is not byte-identical.
- Koin: moving `SessionFiltersService`/bookmarks bindings into a shared module while the old binding remains in `AgendaModule`/`CoreUiModule` causes duplicate-definition crashes (overriding is disabled). The JVM `verify()` tests catch it, but only if the new module is added to the verification list.
- `BookmarksStoreImpl` is not thread-safe (mutable `HashSet` plus `StateFlow` copy). Moving to common and calling from iOS threads exposes races; use `MutableStateFlow.update {}` and immutable sets.
- Changing the persistence API (e.g. DataStore) silently resets Android favorites on update (see Pitfall 1). Keep `SharedPreferences` on Android via `expect/actual`.
- kotlinx-datetime pin: new common code must use `kotlin.time.Instant`/`Clock`; importing `kotlinx.datetime.Instant` ties to the `0.8.0-0.6.x-compat` workaround (AGENTS.md rule, guarded by `OpenFeedbackDatetimeLinkageTest`). A Kotlin or datetime bump during this milestone reopens the linkage hazard (v1.0 lesson 1).
- Module boundary rules: `androidApp → feature → core:ui → core:data → core:network/model`; a new domain module must sit below `core:ui` and above `core:data` without feature-to-feature edges; `di-gate.sh` and the convention plugins (`devfest.kmp.library`) must be applied.
- R8 release: serialization metadata of moved `@Serializable` classes; v1.0 validated a release build on a device. Re-validate.
- `core:ui` `BookmarksViewModel` and `feature/agenda` `AgendaViewModel` tests rely on `FakeBookmarksStore`/`FakeSessionFiltersService`; moving the interfaces breaks them unless the fakes move at the same time.
**Prevention:**
- **Pin, move, re-verify** (the v1.0 pattern): characterization tests on the Android side first; move; run the same tests unchanged against the new shared implementation.
- Run the full CI list from AGENTS.md (`detekt lint`, `di-gate.sh`, `testDebugUnitTest jvmTest testAndroidHostTest`) for every plan that touches shared code, plus an Android release (R8) smoke on a device before milestone closure.
- Move one concept at a time (bookmarks, then filters, then agenda grouping), each as a revertible commit with green CI between (v1.0 retrospective).
- Do not rename Android-visible persisted keys, JSON field names or `FilterType` constants.
**Phase:** Domain extraction phase; also release UAT.
**Confidence:** HIGH (repo specifics), MEDIUM (platform semantics).

---

## Minor Pitfalls

### Pitfall 17: KMP-NativeCoroutines version skew
**What goes wrong:** Swift package `1.0.0-ALPHA-45` (exact pin in the Xcode project) next to Gradle plugin `1.0.6`. The library's docs require matching versions; skew can compile and then fail at the generated-code boundary after a Kotlin bump.
**Prevention:** Align both to the same release in the first iOS plan, and add the Swift package to the new bridge package's `Package.swift` with the same exact version. Add a check in CI that the two values match (grep `libs.versions.toml` vs `Package.swift`). The library is at the "1.0.x tracks Kotlin" cadence, so every Kotlin bump (and its AGENTS.md rule: bump KSP too) must include the Swift pin.
**Confidence:** MEDIUM.

### Pitfall 18: Deployment target and Swift version alignment across targets
**What goes wrong:** App at 17.0 for one configuration and 15.0 for others (verified: mixed), tests at 15.0, packages at 17. A package requiring iOS 17 cannot be linked by a 15.0 target; `@available(iOS 26.0, *)` blocks in `ContentView` and `DevFestSiteBackground` must remain.
**Prevention:** Raise all targets (app, tests, packages) to 17.0 in one commit; keep the `#available(iOS 26, *)` visual branches untouched (parity). Replace deprecated iOS 17 API calls (`onChange` single parameter, `navigationBarItems`) only where needed to hit "no warning", with identical visuals.
**Confidence:** HIGH.

### Pitfall 19: Previews depend on the real shared store
**What goes wrong:** Previews in feature packages need the Gradle XCFramework and a running Koin graph; they crash or never render. Today's `IosDependencies.store()` self-start comment names previews as a driver.
**Prevention:** Previews use the Swift fakes from the ports layer (Pitfall 15) and `@Previewable`/`#Preview` with in-memory data. Add a "previews compile" step by building each package scheme in CI.
**Confidence:** MEDIUM.

### Pitfall 20: Singleton removal changes initialization order
**What goes wrong:** `RCValues.sharedInstance` is touched in `App.init` to set Remote Config defaults, `fetchCloudValues()` runs in `AgendaView.task`, and the OpenFeedback flag is read later. Replacing singletons with lazily created instances moves the fetch or default-setting later and the feature flag flips for one session.
**Prevention:** Keep the same order explicitly in the composition root and add a test that asserts defaults are set before the first flag read. No behavior change to the fetch interval (300 s) or the keys.
**Confidence:** HIGH (read in code).

### Pitfall 21: Documentation and gates drift
**What goes wrong:** `AGENTS.md`, `README.md`, `documentation/ARCHITECTURE.md` and `DEVELOPMENT.md` describe `iosApp` as one target and `IosDependencies` as the lookup; scripts (`build-ios-development.sh`, `build-ios-release.sh`) assume the old project layout.
**Prevention:** Update docs and scripts in the same change that moves things (AGENTS.md rule), and make the `di-gate.sh`/`swift-names-gate.sh` expectations part of the phase's verification.
**Confidence:** HIGH.

---

## Phase-Specific Warnings

Suggested ordering rationale: stand up the iOS test lane first (so everything after is protected), then extract shared logic on both platforms (characterization first), then make iOS DI explicit, then modularize, then `@Observable`, then flip Swift 6. Flipping Swift 6 last avoids fighting concurrency errors in code that is about to move.

| Phase topic | Likely pitfall | Mitigation |
|-------------|---------------|------------|
| CI/test lane first | Pitfall 14 (Xcode-drift cache, 0-test green, flaky simulator); Pitfall 13 (hosted bundle) | Make the old test target compile or retire it; add `build-for-testing` lane with xcresult count assertion and Xcode-version cache key before any refactor |
| Pre-flight (gates) | Pitfall 9 (gate not in CI), Pitfall 17 (NativeCoroutines skew) | Wire `swift-names-gate.sh` into CI, re-baseline, align library versions |
| Shared domain: bookmarks and filters | Pitfalls 1, 16, 7 | Choose backend in a spike; keep Android prefs shape; persistence-pinning tests; migration idempotent + non-destructive; upgrade-in-place UAT |
| Shared domain: agenda grouping/sort | Pitfall 8, 7, 16 | Golden-master on fixtures plus live-API snapshot before extraction; inject timezone/locale/clock; per-platform parity parameters |
| Swift-side API consumption | Pitfalls 5, 6 | Spike the bridge under `SWIFT_STRICT_CONCURRENCY=complete` before finalizing Flow shapes |
| Explicit DI / composition root | Pitfalls 12, 15, 11, 20 | Ports layer in Swift; only the root touches `IosDependencies`; keep Firebase init order; extend `di-gate.sh` |
| SPM modularization | Pitfalls 2, 3, 10, 11, 19 | Build-order spike first (XCFramework + binary target + pre-resolution Gradle step); one owner of `shared`; SwiftGen/resource strategy decided before moving views; previews use fakes |
| `@Observable` + iOS 17 | Pitfalls 4, 6, 18 | `@State` ownership rule, side-effect-free init, `.task` driven collection, one shared Agenda VM; migrate one screen per commit |
| Swift 6 strict | Pitfalls 5, 13 | Complete checking in Swift 5 mode per package first; flip mode last; no `@unchecked Sendable` outside the bridge; lint gate |
| Swift Testing + smoke tests | Pitfalls 13, 15, 14 | Unhosted package tests, per-test `UserDefaults` suite, serialized suites for global state, one XCUITest or hosted smoke per feature |
| Release UAT | Pitfalls 1, 3, 11, 16 | Upgrade-in-place from the v1.0 build, release archive with dSYMs, Android R8 build on device, Crashlytics test crash |

### Research flags (where `/gsd-research-phase` or a spike is warranted)

- **High:** SPM + Gradle XCFramework build order (Pitfall 3) and static framework duplication (Pitfall 2). Nothing else can be planned reliably until this is proven on CI from a clean checkout.
- **High:** KMP-NativeCoroutines + Swift 6 diagnostics (Pitfall 5, 17) and the choice between continuing with the library versus a hand-written `AsyncStream` bridge.
- **High:** Persistence backend and migration design (Pitfall 1).
- **Medium:** SwiftGen/resources in packages (Pitfall 10) and Firebase boundary (Pitfall 11).
- **Low (standard patterns):** `@Observable` conversion (Pitfall 4/6 rules above), Swift Testing basics, deployment target alignment.

---

## Sources

- Repository (HIGH): `iosApp/iosApp/**/*.swift` (BaseViewModel, AgendaViewModel, SpeakersViewModel, AgendaView, ContentView, iOSApp, RCValues, PerformanceMonitoring, SessionFilter), `iosApp/iosApp.xcodeproj/project.pbxproj` (build phases, deployment targets, SPM pins, TEST_HOST), `iosApp/DevFest NantesTests/`, `shared/build.gradle.kts`, `shared/src/iosMain/.../IosDependencies.kt`, `core/data/.../BookmarksStore*.kt`, `core/data/src/iosMain/.../DataPlatformModule.ios.kt`, `feature/agenda/.../SessionFiltersService.kt`, `SessionFilter.kt`, `build-logic/.../KmpLibraryConventionPlugin.kt`, `gradle/libs.versions.toml`, `.github/workflows/ios.yml`.
- Project docs (HIGH): `.planning/PROJECT.md`, `.planning/RETROSPECTIVE.md`, `.planning/codebase/CONCERNS.md`, `.planning/STATE.md` (Phase 3 name-collision history, CICD-V2-01 follow-up), `.planning/milestones/v1.0-phases/03-multi-module-architecture-extraction/swift-names-gate.sh`.
- [rickclephas/KMP-NativeCoroutines releases and Package.swift](https://github.com/rickclephas/KMP-NativeCoroutines) (HIGH for versions: v1.0.6 current, `swift-tools-version:5.3`, iOS 13 minimum; no Swift 6 notes found in release notes).
- [Swift 6 strict concurrency meets Kotlin coroutines in KMP (DEV Community)](https://dev.to/software_mvp-factory/swift-6-strict-concurrency-meets-kotlin-coroutines-in-kmp-148c) and [MVP Factory write-up](https://mvpfactory.io/blog/swift-6-strict-concurrency-meets-kotlin-coroutines-in-kmp-sendable-conformance) (LOW-MEDIUM: third-party claims that Kotlin types fail Sendable checks).
- [Fixing iOS duplicate symbols in KMM: umbrella framework (Medium)](https://medium.com/mobile-innovation-network/fixing-ios-duplicate-symbols-in-kmm-exporting-multiple-modules-as-an-xcframework-ea408d948c00) and Kotlin Slack threads on [local KMP in an SPM setup](https://slack-chats.kotlinlang.org/t/16295952/is-there-a-way-to-use-local-shared-kmp-in-xcode-that-is-conf) and [SPM-first iOS projects](https://slack-chats.kotlinlang.org/t/23182408/i-am-trying-to-set-up-an-ios-swift-project-in-a-way-that-for) (MEDIUM: `embedAndSign...` timing versus SPM modules, XCFramework as the SPM-compatible form).
- Swift evolution and Apple documentation knowledge from training (MEDIUM, not re-fetched): SE-0414 region-based isolation, SE-0371 isolated deinit availability, Observation framework ownership rules, Swift Testing parallelism, `xcodebuild` `build-for-testing`/`-retry-tests-on-failure`.
