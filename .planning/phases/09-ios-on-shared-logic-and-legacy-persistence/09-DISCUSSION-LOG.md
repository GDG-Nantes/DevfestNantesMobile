# Phase 9: iOS on Shared Logic and Legacy Persistence - Discussion Log

> **Audit trail only.** Do not use as input to planning, research, or execution agents.
> Decisions are captured in CONTEXT.md — this log preserves the alternatives considered.

**Date:** 2026-11-05
**Phase:** 09-ios-on-shared-logic-and-legacy-persistence
**Areas discussed:** Agenda observation lifetime, Bookmark & filter state, Swift display model & use-case access, Update & parity UAT

---

## Agenda observation lifetime

| Option | Description | Selected |
|--------|-------------|----------|
| AgendaView .task | Cancel on disappear, resubscribe on appear (v1.0 reload on appear) | ✓ |
| ContentView, app lifetime | Always live, no re-read per appear | |
| You decide | | |

| Option (trace) | Description | Selected |
|--------|-------------|----------|
| First emission of each subscription | One trace per load, as v1.0 | ✓ |
| Once per app launch | Fewer traces than v1.0 | |

| Option (reappear) | Description | Selected |
|--------|-------------|----------|
| Keep previous content | No spinner flash (08 D-10) | ✓ |
| Show spinner again | Like About | |

| Option (mid-stream error) | Description | Selected |
|--------|-------------|----------|
| Log, keep last content, stop | Strict parity with pinned debt | ✓ |
| Automatic retry | New behavior | |

---

## Bookmark & filter state

| Option | Description | Selected |
|--------|-------------|----------|
| Mirror, written through to the stores | Seeded from stores, updated synchronously, persisted in Kotlin | ✓ |
| Extra streams from the stores | Flow as only truth, one async hop, paused while detail pushed | |
| Extend the shared output | New Kotlin API, baseline changes | |

| Option (menu items) | Description | Selected |
|--------|-------------|----------|
| You decide | Identical items/order, source decided in research | ✓ |
| Kotlin enums only | | |

---

## Swift display model & use-case access

| Option | Description | Selected |
|--------|-------------|----------|
| Keep AgendaContent as thin display mapping | Formatting only, classification delegated | ✓ |
| Views use Kotlin types directly | Delete AgendaContent | |

| Option (access) | Description | Selected |
|--------|-------------|----------|
| IosDependencies accessors for all | One access path, moved to AppDependencies in Phase 10 | ✓ |
| Mixed | Swift instantiates stateless policies | |

---

## Update & parity UAT

| Option (old build) | Description | Selected |
|--------|-------------|----------|
| main on a simulator | simctl install over it, human checkpoint | ✓ |
| TestFlight / App Store on a device | Manual | |
| Both | | |

| Option (parity) | Description | Selected |
|--------|-------------|----------|
| Paired screenshots + checkpoint | 08 D-16 pattern | ✓ |
| Goldens/tests only | | |

| Option (scroll-now) | Description | Selected |
|--------|-------------|----------|
| Unit test with injected "now" | Pure extracted function | ✓ |
| Shifted-clock UAT | Fragile (SSL, host clock) | |
| Not covered | | |

---

## Claude's Discretion

- Filter menu option list source (identical to v1.0)
- Port/accessor names, mirror seeding, VM intent methods
- iOS day constants location (single-source)
- Plan slicing and deletion order
- Test list per VM, documentation updates

## Deferred Ideas

- Removing legacy UserDefaults keys
- Continuous observation for Speakers/About/SpeakerDetails
- Error UI / retry for agenda stream
- Data-driven conference days
