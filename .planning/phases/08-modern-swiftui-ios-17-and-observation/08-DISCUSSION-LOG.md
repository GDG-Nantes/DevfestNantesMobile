# Phase 8: Modern SwiftUI, iOS 17 and Observation - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-11-03
**Phase:** 08-modern-swiftui-ios-17-and-observation
**Areas discussed:** Agenda VM instances, Ports and fakes shape, Collection lifecycle, Modernization scope & proof

---

## Agenda VM instances

| Option | Description | Selected |
|--------|-------------|----------|
| One shared instance now | Single VM passed down; possible immediate-refresh side effect recorded as deviation | ✓ |
| Keep per-view instances | Strict parity; Phase 9 merges | |
| Cells get a toggle closure | Cells/detail take `isBookmarked` + closure | |

**User's choice:** One shared instance now.

| Option | Description | Selected |
|--------|-------------|----------|
| ContentView + environment | `@State` in ContentView, `.environment`, `@Environment(AgendaViewModel.self)` | ✓ |
| ContentView + explicit init params | Same owner, passed through initializers | |
| Agenda tab only | AgendaView owns it; Speakers-path detail keeps its own | |

**User's choice:** ContentView + environment.
**Notes:** Scouting showed `AgendaCellView` is only used in `SpeakerDetailsView` with `isBookmarked: false`, and `AgendaDetailView` is reachable from both tabs, so the owner must sit above the tabs.

---

## Ports and fakes shape

| Option | Description | Selected |
|--------|-------------|----------|
| Kotlin models via AsyncThrowingStream | Live adapter wraps asyncSequence/asyncFunction; fakes use snapshot values | ✓ |
| Swift value types now | Kotlin→Swift mapping in adapters | |
| Reuse the Kotlin store protocol | VM takes DevFestNantesStore, Kotlin fake exported | |

| Option | Description | Selected |
|--------|-------------|----------|
| Init default argument | `init(source: X = LiveX())` until Phase 10 | ✓ |
| Factory in the app shell | ViewModelFactory in ContentView | |

| Option | Description | Selected |
|--------|-------------|----------|
| Small protocol, shared default | `DataLoadTracing`, `PerformanceMonitoring` conforms, recording fake | ✓ |
| Tracing inside the live port | VMs never see tracing | |
| Leave the singleton | Rely on unconfigured Firebase | |

| Option | Description | Selected |
|--------|-------------|----------|
| Port next to its VM, fakes in tests | `<Feature>Source.swift`; `DevFestNantesTests/Fakes/` | ✓ |
| One Ports/ folder | All ports in `iosApp/Ports/` | |

| Option | Description | Selected |
|--------|-------------|----------|
| Inject UserDefaults | `defaults: UserDefaults = .standard`, suite in tests | ✓ |
| Swift port now | `AgendaPreferences` protocol | |

---

## Collection lifecycle

| Option | Description | Selected |
|--------|-------------|----------|
| Keep first-emission | Rooms continuous in `.task`; continuous agenda is Phase 9 | ✓ |
| Go continuous now | Collect all streams while visible | |

| Option | Description | Selected |
|--------|-------------|----------|
| Reload on every appear | Same as v1.0 | ✓ |
| Load once per VM | Skip if content exists | |

| Option | Description | Selected |
|--------|-------------|----------|
| Silent, state untouched | CancellationError not logged, no state change | ✓ |
| Treat like any error | Logged, isLoading = false | |

---

## Modernization scope & proof

| Option | Description | Selected |
|--------|-------------|----------|
| Only what Observation touches | Other deprecations logged for Phase 12 | ✓ |
| All iOS 17 deprecations | navigationTitle, tint, foregroundStyle, ... | |
| Only deprecation warnings | Compiler-flagged only | |

| Option | Description | Selected |
|--------|-------------|----------|
| Paired screenshots + human checkpoint | 07-19 pattern | ✓ |
| Guided manual smoke only | 06-06 pattern | |

| Option | Description | Selected |
|--------|-------------|----------|
| Floor, then one VM per plan | iOS 17 → About → Venue → Speakers → Agenda → Combine removal + gate → parity | ✓ |
| Big bang | All VMs in one plan | |

| Option | Description | Selected |
|--------|-------------|----------|
| CI grep gate script | `scripts/swift-observation-gate.sh` in ios.yml | ✓ |
| One-time verification | No script | |

---

## Claude's Discretion

- Port/adapter/fake names and signatures; AsyncThrowingStream construction
- `@State` creation of parameterized VMs (SpeakerDetails, Venue)
- Fate of `BaseViewModel` in Phase 8 (no longer ObservableObject; deleted in Phase 10)
- `@ObservationIgnored` usage; test case list per VM; gate output format and scan scope
- Documentation updates per AGENTS.md rule

## Deferred Ideas

- Remaining iOS-17-deprecated SwiftUI APIs → Phase 12
- Swift value types in ports → Phase 11/12
- Continuous observation → Phase 9 (agenda)
- Load-once-per-VM → not adopted
- Error UI on store failure → pinned v1.0 debt
