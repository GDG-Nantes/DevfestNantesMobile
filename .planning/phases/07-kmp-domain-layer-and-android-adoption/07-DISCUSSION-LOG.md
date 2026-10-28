# Phase 7: KMP Domain Layer and Android Adoption - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-10-28
**Phase:** 07-kmp-domain-layer-and-android-adoption
**Areas discussed:** Use-case API shape, Time zone and slot grouping, Characterization assets, SessionFilter move and Swift names

---

## Use-case API shape

| Option | Description | Selected |
|--------|-------------|----------|
| Pure + combined flow | Pure policies + `ObserveAgendaUseCase` combining store, filters and bookmarks | ✓ |
| Pure policies only | ViewModels keep orchestrating flows | |
| Combined flow only | One use case per screen, policies internal | |

**User's choice:** Pure + combined flow.
**Notes:** The user first picked "Combined flow only", then said that answer was wrong and asked to restart the API-shape discussion. The final answer above supersedes it.

| Option | Description | Selected |
|--------|-------------|----------|
| Days → slots → sessions | Fully filtered/grouped/sorted model, UI formats labels only | ✓ |
| Filtered days, flat sessions | Slot grouping as a separate function | |

| Option | Description | Selected |
|--------|-------------|----------|
| Classes, named methods | `observe()`, `filter(...)`, `@NativeCoroutines`, `factory {}` | ✓ |
| Operator invoke classes | Swift sees `.invoke(...)` | |
| Top-level functions | Mixed style | |

| Option | Description | Selected |
|--------|-------------|----------|
| ViewModel keeps them | UiState, perf trace, errors stay in the VM | ✓ |
| Use case emits a result type | Loading/Success/Error | |

---

## Time zone and slot grouping

| Option | Description | Selected |
|--------|-------------|----------|
| Explicit param, device tz | `TimeZone`/`Clock` params, platforms pass device tz | ✓ |
| Fixed Europe/Paris | Event tz everywhere (visible change) | |

| Option | Description | Selected |
|--------|-------------|----------|
| Start Instant | Slot key = parsed start instant | ✓ |
| Truncated to the minute | Mirror Android string key exactly | |

| Option | Description | Selected |
|--------|-------------|----------|
| Explicit parameter | `DayAssignment` (Android UTC windows / iOS local date) | ✓ |
| Android rule for both | Latent change for iOS in far time zones | |
| Days stay per platform | Less shared logic | |

| Option | Description | Selected |
|--------|-------------|----------|
| Stays per platform | Current-slot scroll target unchanged per platform | ✓ |
| Shared with a strategy param | Two behaviors to characterize | |

---

## Characterization assets

| Option | Description | Selected |
|--------|-------------|----------|
| Recorded GraphQL JSON | Replayed through Apollo MockServer | ✓ |
| Generated Kotlin fixture | Bypasses mappers | |
| Live query in tests | Flaky | |

| Option | Description | Selected |
|--------|-------------|----------|
| Both: Codable + device | Swift golden + v1.0 simulator capture | ✓ (later made moot by D-15) |
| Swift Codable fixture only | | |
| Device capture only | | |

| Option | Description | Selected |
|--------|-------------|----------|
| Swift tests + Kotlin goldens | Swift Testing pins iOS output, Kotlin reads the same goldens | ✓ |
| Kotlin port of Swift logic | Oracle could be wrong | |
| Manual UAT only for iOS | Doesn't satisfy SHARED-07 | |

| Option | Description | Selected |
|--------|-------------|----------|
| Before/after screenshots | `android` CLI captures on main vs branch + human checkpoint | ✓ |
| Human-guided smoke only | | |
| Automated tests only | | |

---

## SessionFilter move and Swift names

| Option | Description | Selected |
|--------|-------------|----------|
| Keep Kotlin name, gate it | `SessionFilter` + `SessionFilterType`, app type shadows until Phase 9 | ✓ |
| Rename Swift types now | `LegacySessionFilter` | |
| @ObjCName on Kotlin | Worse permanent Swift name | |

| Option | Description | Selected |
|--------|-------------|----------|
| data class + characterize | Value equality, persisted JSON byte-identical | ✓ |
| Keep identity equality | | |

| Option | Description | Selected |
|--------|-------------|----------|
| Mirror v1.0 exactly | Legacy-format iOS store | |
| Normalize on write | | |

**User's choice (free text):** "I don't mind losing the filters data in the migration process. The conference is over and next time the app will be used, we'll use new data. We should use a clean approach."

| Option | Description | Selected |
|--------|-------------|----------|
| Both: fully clean | Favorites and filters: iOS uses new keys + shared codec, no legacy read; Android unchanged | ✓ |
| Filters only | Keep reading legacy `Favorites` | |
| Both, but Android unchanged | | |

| Option | Description | Selected |
|--------|-------------|----------|
| Update now | Rewrite SHARED-05/07, IOSDATA-02, Phase 7/9 criteria, PROJECT Key Decision | ✓ |
| CONTEXT.md only | | |

---

## Claude's Discretion

- Domain type/class names, package layout, number of use cases
- Speaker A-Z index / venue language: share or parameterize after characterization
- iOS `NSUserDefaults` key names and stored types
- Plan slicing, HomeViewModel/BookmarksViewModel adoption details, golden file format/location

## Deferred Ideas

- Removing obsolete iOS legacy `UserDefaults` keys
- Event time zone grouping (Europe/Paris)
- Unified current-slot scroll
- Data-driven event days (`InitialDayUseCase`)
- Loading/Error result type from use cases
