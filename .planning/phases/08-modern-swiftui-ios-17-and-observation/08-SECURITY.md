---
phase: "8"
slug: "modern-swiftui-ios-17-and-observation"
status: verified
# threats_open = count of OPEN threats at or above workflow.security_block_on severity (the blocking gate)
threats_open: 0
asvs_level: 1
created: "2026-11-05"
---

# Phase 8 — Security

> Per-phase security contract: threat register, accepted risks, and audit trail.

---

## Trust Boundaries

| Boundary | Description | Data Crossing |
|----------|-------------|---------------|
| Developer edit -> Xcode project file | A hand edit of `project.pbxproj` can change unrelated build settings or corrupt the plist | Build settings, package pins (integrity) |
| Kotlin flow / suspend function -> Swift | KMP-NativeCoroutines hands Kotlin values to Swift through `KotlinStreamBridge` and `asyncFunction`; a missing cancellation keeps Kotlin work alive | Public conference data |
| App -> Crashlytics / Firebase Performance | `DevFestLogger` forwards `.error` logs; `trackDataLoad` sends traces | Error descriptions, trace names (no personal data) |
| Device storage (UserDefaults) -> app | v1.0 favorites and filters are read back after the update | Session ids, filter choices (local, non-sensitive) |
| SwiftUI environment -> agenda views | A non-optional `@Environment` read traps when no ancestor injected the object | — (availability) |
| Test process -> UserDefaults | Hosted tests run inside the app process | Test fixtures |
| Contributor change -> main / CI gate -> verdict | A later change could reintroduce Combine; a gate that scans nothing passes silently | Source code (integrity) |
| Local machine -> GitHub | The human pushes; CI builds with a mock `GoogleService-Info.plist` | Committed sources only |
| Gitignored secrets -> temporary worktree | `GoogleService-Info.plist` and `local.properties` copied into a worktree outside the repository for the parity builds | Firebase config, local paths (secret) |
| Host clock -> simulators and TLS | The shifted-clock check changes the Mac clock | — (availability) |

---

## Threat Register

| Threat ID | Category | Component | Severity | Disposition | Mitigation | Status |
|-----------|----------|-----------|----------|-------------|------------|--------|
| T-08-01 | Tampering | `project.pbxproj` (versions, bundle ids, package pins) | medium | mitigate | Targeted 2+/2- edit of `IPHONEOS_DEPLOYMENT_TARGET`; `plutil -lint`; KMP-NC gate. Verified: 6 entries, all `17.0` (08-01) | closed |
| T-08-02 | Denial of service | Users on iOS 15/16 | low | accept | App targets were already 17.0; only the project default aligned | closed |
| T-08-03 | Denial of service | `KotlinStreamBridge` upstream collection | medium | mitigate | `continuation.onTermination` cancels the native subscription (`KotlinStreamBridge.swift:51`); `terminationCount == 1` asserted in `AboutViewModelTests` | closed |
| T-08-04 | Information disclosure | About error logging | low | mitigate | Cancellation not logged; v1.0 error text with `localizedDescription` only (08-02) | closed |
| T-08-05 | Tampering | Test seams (`source:`, `store:` closures) | low | accept | Internal Swift API with live defaults; no new input surface | closed |
| T-08-06 | Tampering | swift-names baselines regenerated | medium | mitigate | Diff only adds `DevFestNantesStoreBuilder`; `swift-names-gate.sh` check + selftest pass (`SWIFT-GATE-SELFTEST-OK`) | closed |
| T-08-07 | Denial of service | v1.0 unstructured venue task outliving the view | low | mitigate | Load runs in the view's `.task`; `cancelled_first_load_is_silent` (`VenueViewModelTests.swift:87`) | closed |
| T-08-08 | Information disclosure | Venue error logging | low | mitigate | Cancellation not logged; v1.0 error text, no venue content (08-03) | closed |
| T-08-09 | Repudiation | CI replay claim | medium | mitigate | Run 41986925570 (#408) verified with `gh`: head SHA `c8aa2d3f`, conclusion success, `XCRESULT-OK`, 32/32, Xcode 26.6 (08-04 SUMMARY) | closed |
| T-08-10 | Information disclosure | Pushed branch | low | accept | Only committed sources pushed; real `GoogleService-Info.plist` gitignored, CI uses a mock | closed |
| T-08-11 | Denial of service | `SpeakerDetailsViewModel.init` repeated by eager `State(initialValue:)` | medium | mitigate | Init only stores; load in `.task`; `init_starts_no_load` (`SpeakerDetailsViewModelTests.swift:46`) | closed |
| T-08-12 | Information disclosure | Cancelled traced loads forwarded to Crashlytics | medium | mitigate | `trackDataLoad` catches `CancellationError` and logs at info level without the error (`PerformanceMonitoring.swift:99`) | closed |
| T-08-13 | Tampering | Performance trace names/data sources | low | mitigate | Constants reused; tracer test asserts `("speakers_load", "graphql")` (`SpeakersViewModelTests.swift:24`) | closed |
| T-08-14 | Tampering (data integrity) | Favorites / filters persistence | high | mitigate | Legacy keys `Favorites` / `AgendaSessionFilters` and encodings unchanged; init writes nothing; tests decode a literal v1.0 blob (08-07); 08-09 favorites-after-relaunch pair identical | closed |
| T-08-15 | Denial of service | `@Environment(AgendaViewModel.self)` with no injection | high | mitigate | Injected in `ContentView.swift:43`, re-injected on the navigation bar items (`AgendaView.swift:56`); launch check (08-07) and every 08-09 path including Speakers > speaker > session passed | closed |
| T-08-16 | Denial of service | Continuous rooms collection starving the sessions load | high | mitigate | Two separate `.task` modifiers in `AgendaView`; tracer test loads both; 08-09 agenda left its spinner on both days | closed |
| T-08-17 | Tampering | Hosted tests writing the app's standard defaults | low | mitigate | Per-test `UserDefaults(suiteName:)` (4 uses), no `.standard` in `AgendaViewModelTests.swift` | closed |
| T-08-18 | Tampering | iOS Swift sources (regression to Combine/legacy observation) | medium | mitigate | `scripts/swift-observation-gate.sh` runs in `ios.yml` (line 41), check + selftest | closed |
| T-08-19 | Repudiation | Gate false negative (empty scan set, broken regex) | medium | mitigate | `SWIFT-OBS-SCAN-EMPTY` fails the gate; selftest proves each pattern (`SWIFT-OBS-GATE-SELFTEST-OK`) | closed |
| T-08-20 | Denial of service | SPM product removal breaking resolution | low | mitigate | Only the Combine product removed (0 refs), `KMPNativeCoroutinesAsync` kept; `-resolvePackageDependencies`, KMP-NC gate, full suite green (08-08) | closed |
| T-08-21 | Repudiation / Tampering | iOS parity claim | high | mitigate | Blocking-human checkpoint on 21 agent-captured pairs from identical simulators; approved by the user 2026-11-05 (shifted-clock sub-check not performable on either build, logged as debt in `deferred-items.md`) | closed |
| T-08-22 | Information disclosure | Committed screenshots, temporary worktree | low | mitigate | Screens show public conference data only; no secret tracked (`git ls-files` finds no `GoogleService-Info.plist` / `local.properties`); the temp worktree is gone | closed |
| T-08-23 | Denial of service | Host clock shift (TLS failures) | low | mitigate | Only the human changed the clock; automatic time restored right after (the cached-data precondition did not make the agenda display, recorded as debt) | closed |
| T-08-24 | Tampering | `speaker_details_load` trace name and data source | low | mitigate | `PerformanceMonitoring.TRACE_SPEAKER_DETAILS_LOAD` and `"graphql"` reused (`SpeakerDetailsViewModel.swift:42`); sessions call untraced | closed |
| T-08-25 | Information disclosure | SpeakerDetails error logging | low | mitigate | No cancellation log; error lines keep the v1.0 text with `localizedDescription` (`SpeakerDetailsViewModel.swift:55,70`) | closed |
| T-08-SC | Tampering | Package / tool installs | low | accept | No package added or upgraded in any plan; 08-08 only removes the Combine product; Maestro 1.35.0 was already installed | closed |

*Status: open · closed · open — below high threshold (non-blocking)*
*Severity: critical > high > medium > low — only open threats at or above workflow.security_block_on count toward threats_open*
*Disposition: mitigate (implementation required) · accept (documented risk) · transfer (third-party)*

---

## Accepted Risks Log

| Risk ID | Threat Ref | Rationale | Accepted By | Date |
|---------|------------|-----------|-------------|------|
| AR-08-01 | T-08-02 | The shipped app targets were already iOS 17.0; the project default change does not drop any supported device | 08-01 plan threat model | 2026-11-04 |
| AR-08-02 | T-08-05 | Test seams are internal Swift initializer parameters with live defaults; no exported or external input surface | 08-02 plan threat model | 2026-11-04 |
| AR-08-03 | T-08-10 | Only committed sources leave the machine; the real Firebase config stays gitignored and CI generates a mock | 08-04 plan threat model | 2026-11-04 |
| AR-08-04 | T-08-SC | No dependency was added or upgraded during the phase | All phase 8 plan threat models | 2026-11-04 |

*Accepted risks do not resurface in future audit runs.*

---

## Security Audit Trail

| Audit Date | Threats Total | Closed | Open | Run By |
|------------|---------------|--------|------|--------|
| 2026-11-05 | 26 | 26 | 0 | /gsd-secure-phase 08 (orchestrator, ASVS L1 grep-depth; plan-time register, auditor not required) |

---

## Sign-Off

- [x] All threats have a disposition (mitigate / accept / transfer)
- [x] Accepted risks documented in Accepted Risks Log
- [x] `threats_open: 0` confirmed
- [x] `status: verified` set in frontmatter

**Approval:** verified 2026-11-05
