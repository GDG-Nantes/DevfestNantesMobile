# Agenda characterization test data

Shared inputs and golden outputs of the Phase 7 characterization tests. The same files are read by
Kotlin (JVM and iOS simulator), Android unit tests and Swift tests, so the formats below are a
contract: change a format only together with every reader and writer.

## Provenance and re-record

The data is the public DevFest Nantes 2025 agenda (conference `devfestnantes2025` on
`https://confetti-app.dev/graphql`), recorded on the date in
`core/data/testdata/graphql/snapshot-meta.json`.

1. `core/data/testdata/graphql/{GetSessions,GetSpeakers,GetRooms}.json` are the raw responses of
   the app's own three queries (recorded by `AgendaSnapshotRecorder`).
2. `sessions.snapshot.json`, `speakers.snapshot.json` and `rooms.snapshot.json` here are the
   neutral model snapshot, rebuilt from the raw responses through the real `GraphQLStore` over an
   Apollo `MockServer` by `AgendaSnapshotTest` (`:core:data` jvmTest).

Re-record everything (raw, then neutral) with:

```bash
bash scripts/record-agenda-snapshot.sh
```

The neutral files alone can be rewritten from the committed raw files with
`./gradlew :core:data:jvmTest -PrecordAgendaSnapshot --tests '*AgendaSnapshotTest*'`. Never edit
them by hand: the default `:core:data:jvmTest` run fails with `neutral snapshot drifted` when they
differ from what the raw responses produce.

Pinned facts of the current recording (D-17: the speaker set is what the app loads today):
94 sessions, 17 of them without a room, 8 rooms, 100 speakers from the `GetSpeakers` query. The
server reports 110 speakers through `speakersPage` (kept as `speakersPageTotal` in
`snapshot-meta.json`, metadata only); the gap is FUT-02 debt and is not fixed here.

## Snapshot formats

All three files are JSON arrays, pretty-printed with a 2-space indent and a trailing newline, in
store order.

`sessions.snapshot.json`, one object per session, keys in this order:

```json
{
  "id": "keynotedouverture",
  "title": "Opening Keynote",
  "type": "CONFERENCE",
  "language": "FRENCH",
  "complexity": "BEGINNER",
  "room": { "id": "Jules Verne", "name": "Jules Verne", "sortIndex": 0 },
  "startDate": "2025-10-16T09:00:00+02:00",
  "endDate": "2025-10-16T09:50:00+02:00",
  "speakerIds": ["speaker-id"]
}
```

- `type`, `language`, `complexity` are the `SessionType`, `SessionLanguage` and `Complexity` enum
  names, or `null` (for example the API type `party` maps to a null type).
- `room` is `null` for service sessions (breaks, lunches, openings, party).
- `startDate` and `endDate` are the strings the Android mapper produces
  (`yyyy-MM-dd'T'HH:mm:ss+02:00`).
- `speakerIds` lists speaker ids only.

`speakers.snapshot.json`: objects with keys `id`, `name`.
`rooms.snapshot.json`: objects with keys `id`, `name`, `sortIndex` (`Int.MAX_VALUE` for rooms
outside the known sort list). Rooms are in `sortIndex` order.

`:core:testing` decodes them with `AgendaSnapshot.sessions/speakers/rooms(json)`.

## Filter cases

`filter-cases.json`:

```json
{
  "bookmarkedSessionIds": ["session-id"],
  "cases": [
    { "name": "language_french", "filters": [{ "type": "LANGUAGE", "value": "FRENCH" }] }
  ]
}
```

`type` is a filter type (`BOOKMARK`, `COMPLEXITY`, `LANGUAGE`, `ROOM`, `TYPE`; the iOS tests map
them to the lowercase Swift raw values). `value` is the Kotlin enum name (`FRENCH`, `BEGINNER`,
`CONFERENCE`, ...) or the room id (`Titan`, `Tour Bretagne`), exactly what both apps store; it is
the empty string for `BOOKMARK`. The `bookmark*` cases apply with `bookmarkedSessionIds` as the
bookmark set. The 15 cases are in a fixed order that the filtering goldens follow.

## Golden formats

Goldens live under `golden/` (next to this README). They capture the v1.0 behavior of the Android
and iOS apps before the shared domain layer replaces it, and are written by plans 07-05 / 07-06
(Android), 07-08 (iOS) and read again by 07-12 / 07-13 (Kotlin).

Agenda goldens, `golden/android-agenda.json` and `golden/ios-agenda-<Area>_<City>.json` (the time
zone id with `/` replaced by `_`):

```json
{
  "source": "android-v1.0",
  "timeZone": "Europe/Paris",
  "days": [
    {
      "dayIndex": 1,
      "date": "<day id>",
      "slots": [
        { "start": "2025-10-16T06:00:00Z", "sessionIds": ["id-1", "id-2"] }
      ]
    }
  ]
}
```

`source` is `android-v1.0` or `ios-v1.0`; `timeZone` is the zone id or `UTC`; `start` is a UTC ISO
instant. Session ids inside a slot are canonicalized, because both v1.0 orders are unspecified
among ties (Android HashSet, iOS unstable sort): Android slots sort by (room `sortIndex`, room
name, id) with a null room first; iOS slots sort by (room name or the empty string, id).

Filtering goldens, `golden/android-filtering.json` and `golden/ios-filtering.json`:

```json
{ "source": "android-v1.0", "cases": [{ "name": "no_filter", "sessionIds": ["ascending", "ids"] }] }
```

`cases` follow the `filter-cases.json` order; `sessionIds` are sorted ascending.

Speaker goldens: `golden/android-speaker-index.json`
`{"source": "android-v1.0", "firstIndexes": [{"key": "a", "index": 0}]}` in first-appearance
order, and `golden/ios-speaker-index.json`
`{"source": "ios-v1.0", "letters": [{"letter": "A", "speakerIds": ["id"]}]}` in letter order.

### Golden rules (every golden)

- A default (CI) test run never creates or modifies a file under `core/domain/testdata`. An absent
  golden fails the test with `golden missing: <path>` and nothing is written, so parallel JVM,
  iOS-simulator and Swift test runs cannot race on these files.
- A golden is recorded only in explicit record mode: `-PrecordGoldens` on the Gradle test task, or
  `TEST_RUNNER_DEVFEST_RECORD_GOLDENS=true` in front of the Swift
  `xcodebuild ... test-without-building` command. In record mode an absent golden is written and
  the test fails with `golden recorded, re-run`.
- An existing golden is never overwritten, even in record mode. To re-record: delete the file,
  run once in record mode, then once in default mode.
- Goldens are never edited by hand and never rewritten to make new code pass.
- JSON is pretty-printed with a 2-space indent and a trailing newline.
