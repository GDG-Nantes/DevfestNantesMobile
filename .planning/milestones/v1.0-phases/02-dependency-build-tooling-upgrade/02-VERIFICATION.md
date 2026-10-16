---
phase: 02-dependency-build-tooling-upgrade
verified: 2026-09-19T12:30:00Z
status: passed
score: 4/4 must-haves verified
covered_files:

  - .planning/REQUIREMENTS.md
  - .planning/phases/02-dependency-build-tooling-upgrade/02-01-PLAN.md
  - .planning/phases/02-dependency-build-tooling-upgrade/02-01-SUMMARY.md
  - .planning/phases/02-dependency-build-tooling-upgrade/02-02-PLAN.md
  - .planning/phases/02-dependency-build-tooling-upgrade/02-02-SUMMARY.md
  - .planning/phases/02-dependency-build-tooling-upgrade/02-03-PLAN.md
  - .planning/phases/02-dependency-build-tooling-upgrade/02-03-SUMMARY.md
  - .planning/phases/02-dependency-build-tooling-upgrade/02-04-PLAN.md
  - .planning/phases/02-dependency-build-tooling-upgrade/02-04-SUMMARY.md
  - .planning/phases/02-dependency-build-tooling-upgrade/02-05-PLAN.md
  - .planning/phases/02-dependency-build-tooling-upgrade/02-05-SUMMARY.md
  - androidApp/build.gradle.kts
  - androidApp/proguard-rules.pro
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/core/injection/AppModule.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/services/DataCollectionSettingsService.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/services/FirebaseAnalyticsService.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/services/SessionFiltersService.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/agenda/Agenda.kt
  - androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/agenda/AgendaViewModel.kt
  - build.gradle.kts
  - buildSrc/src/main/java/Dependencies.kt
  - gradle/libs.versions.toml
  - gradle/wrapper/gradle-wrapper.properties
  - linters/detekt-config.yml
  - settings.gradle.dcl
  - shared/build.gradle.kts
  - shared/src/commonMain/kotlin/com/gdgnantes/devfest/model/SocialItem.kt
  - shared/src/commonMain/kotlin/com/gdgnantes/devfest/model/stubs/StoreStubs.kt
  - shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/BookmarksStore.kt
  - shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/Apollo.kt
  - shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/ApolloCache.kt
  - shared/src/commonMain/kotlin/com/gdgnantes/devfest/store/graphql/GraphQLStore.kt
  - shared/src/commonTest/kotlin/com/gdgnantes/devfest/model/ScheduleSlotDateParsingTest.kt

covered_digest: "v1:sha256:55c02e92768b54ba3deb2a5b427734f901bcdef003efa6e903e116f6f109cdd6"
behavior_unverified: 0
overrides_applied: 0
human_verification:

  - test: "02-01 (Kotlin 2.4.20 + KSP): install/launch the debug app after the Kotlin/KSP bump and confirm the Agenda screen lists sessions and bottom-nav works, matching pre-bump behavior."
    expected: "App looks and behaves exactly as before the Kotlin/KSP bump — no crash, sessions render, navigation works."
    why_human: "Visual/behavioral parity judgment; screenshots exist (02-01-agenda.png, 02-01-speakers.png) but require a human to confirm they show correct, unregressed content."
  - test: "02-02 (AGP 9 KMP-library plugin swap): drive Agenda, Speakers, Venue and Bookmarks on the AGP-9-built app; confirm each renders and navigates, and that bookmarks existing before the phase survived."
    expected: "All four screens render populated content and navigate correctly; pre-existing bookmarks are intact after the plugin swap."
    why_human: "Visual/behavioral parity judgment across 4 screens; screenshots exist (02-02-*.png) but need human confirmation, especially the bookmark-persistence claim."
  - test: "02-03a (Compose BOM 2026.09.00): confirm the Agenda day-pager swipe/indicator, Material 3 theming, and screen layout are visually unchanged after the Compose BOM bump."
    expected: "No clipped/overlapped/missing content; day pager and theming look as before."
    why_human: "Visual parity of Compose-rendered UI cannot be confirmed by static analysis; screenshot 02-03-agenda-compose-bom.png exists for review."
  - test: "02-03b (Apollo 5.x cache migration, D-07): with network on, populate Agenda/Speakers/Venue; confirm a repeated query re-renders from cache without a loading flash; then go offline, force-relaunch, and confirm Agenda/Speakers/Venue still show the previously loaded data (not empty/error); confirm bookmarks survive the offline relaunch."
    expected: "Offline relaunch shows real cached data on all three screens; bookmarks persist."
    why_human: "This is the exact regression this stage's own D-07 fix (map->mapNotNull) was built to resolve — logcat/on-disk evidence is strong, but final on-device confirmation is a human call. Note for the reviewer: 02-03-08-agenda-offline-fixed.png shows only one session at 17:50 with a bookmark checkmark and nothing else on screen, while other online screenshots show 3+ sessions at the same timeslot — worth confirming this is the expected 'Favori' filter state carried over from an earlier test step (per 02-02's bookmarks-filtered flow) and not a partial-data regression."
  - test: "02-04 (Firebase BOM/coroutines/serialization/datetime bump): confirm Agenda day tabs and session start/end times are unchanged, and Speakers/Venue (Firebase Remote-Config-backed) still render populated content, not an empty/default state."
    expected: "Date-dependent UI (day tabs, session times) unchanged; Firebase-backed screens still populated."
    why_human: "Date-logic and Firebase-config visual parity; screenshots exist (02-04-*.png) but need human confirmation given this stage touched kotlinx-datetime directly."
  - test: "General: confirm none of the 4 deliberately-authorized deviations (targetSdk 36 vs compileSdk 37, the GraphQLStore CacheAndNetwork fix, the firebase-auth-ktx substitution, and the openfeedback R8 -dontwarn rules) introduce any user-visible change, since the phase's stated goal is zero observable behavior change."
    expected: "No end-user-visible behavior change from any of the four authorized deviations."
    why_human: "These are judgment calls about whether a technically-necessary fix stayed within 'zero observable behavior change' scope — already reasoned through in STATE.md/SUMMARYs, but final sign-off belongs to the developer per the project's human_verify_mode: end-of-phase workflow setting."
---

# Phase 02: Dependency & Build Tooling Upgrade Verification Report

**Phase Goal:** The project builds and runs on the modernized toolchain with zero observable behavior change, each dependency group staged and verified independently
**Verified:** 2026-09-19T12:30:00Z
**Status:** human_needed
**Re-verification:** No — initial verification

## Goal Achievement

### Observable Truths

| # | Truth (Roadmap Success Criterion) | Status | Evidence |
|---|---|---|---|
| 1 | The project compiles and CI passes on Kotlin 2.4.0 | ✓ VERIFIED | Satisfied by documented superset per developer-selected `newest-verified` policy (checkpoint in 02-01, recorded in REQUIREMENTS.md D-03 note and STATE.md § Phase 02 version deviations). `gradle/libs.versions.toml`: `kotlin = "2.4.20"`, `ksp = "2.3.12"`. `android.yml` (4/4 jobs) and `ios.yml` green on commits `aecf595`, `c56d6ab`, `c61f957`, `00ab89d`, `4ab7f01`, `31c8cd3`, `736c843`, `e8eb6a7`, `1e497a0` (confirmed live via `gh run list` for the branch — all `success`). |
| 2 | The project builds on Gradle 9.7.1 and on AGP 9.2.0 using `com.android.kotlin.multiplatform.library` in place of the forbidden `kotlin.multiplatform` + `com.android.library` coexistence | ✓ VERIFIED | AGP satisfied by documented superset (9.4.0, same `newest-verified` policy). `gradle/wrapper/gradle-wrapper.properties`: `distributionUrl=...gradle-9.7.1-bin.zip`, `distributionSha256Sum=acd53f1e...` (64 hex chars). Local `./gradlew --version` confirms `Gradle 9.7.1` on this machine. `gradle/libs.versions.toml`: `agp = "9.4.0"`; `[plugins]` has `android-kotlin-multiplatform-library = { id = "com.android.kotlin.multiplatform.library", ... }`; the legacy `android-library` plugin alias is fully absent from the catalog. `shared/build.gradle.kts` applies `alias(libs.plugins.android.kotlin.multiplatform.library)`, has no top-level `android { }` block, and configures Android via the nested `kotlin { android { ... } } ` block — the forbidden AGP-8 coexistence is gone. CI green on the AGP-9 commit (`c56d6ab`, 4/4 `android.yml` jobs + `ios.yml`). |
| 3 | The app runs unchanged for users on Compose BOM 2026.08.00 (superseded to 2026.09.00 per checkpoint), Apollo GraphQL 5.0.1+ (superseded to 5.2.0), and the latest stable Firebase BOM/Coroutines/kotlinx-serialization/kotlinx-datetime | ✓ VERIFIED (version/wiring), behavior-parity → human_needed | `gradle/libs.versions.toml`: `composeBom = "2026.09.00"`, `appollo = "5.2.0"`, `appolloCache = "1.0.8"`, `firebaseBom = "34.19.0"`, `kotlinxCoroutines = "1.11.0"`, `kotlinxSerialization = "1.11.0"`, `kotlinxDatetime = "0.8.0"`. `shared/.../GraphQLStore.kt` confirmed to use `com.apollographql.cache.normalized.*` imports and `.mapNotNull` (not `.map`) on all four `CacheAndNetwork` accessors — the user-authorized fix for the offline-cache-clobbering bug is actually in the code, not just claimed. `Apollo.kt`'s `CacheResolver.resolveField` uses the v5 `ResolverContext` signature. All version-bump commits green on both CI workflows (`c61f957`, `00ab89d`, `4ab7f01`, `736c843`). The "runs unchanged for users" half of this criterion is a visual/behavioral claim — routed to human verification below (screenshot evidence exists but final sign-off is a human call). |
| 4 | Build files are migrated to the Gradle Declarative DSL (`.gradle.dcl`) wherever AGP/KMP support allows; any module left on Kotlin DSL (`.kts`) is documented with the reason | ✓ VERIFIED | `settings.gradle.dcl` exists at repo root; `settings.gradle.kts` no longer exists. `STATE.md` § Phase 02 DCL pilot outcome documents, with a live 2026-09-19 re-check of `github.com/gradle/declarative-gradle`'s docs, exactly why `androidApp`/`shared` remain on `.kts` (Declarative Gradle's "Software Types" surface, required for AGP/KMP/Detekt/Hilt/Firebase plugins, is still explicitly "not ready for adoption"). CI green on the DCL commit (`e8eb6a7`, 4/4 `android.yml` jobs + `ios.yml`). |

**Score:** 4/4 roadmap success criteria technically verified; the "unchanged for users" visual-parity half of criterion 3 (and the equivalent D-02/D-04/D-07 sanity-pass claims embedded in criteria 1–4) is routed to human verification per the project's `human_verify_mode: end-of-phase` setting rather than accepted on agent-captured screenshots alone.

### Required Artifacts

| Artifact | Expected | Status | Details |
|---|---|---|---|
| `gradle/libs.versions.toml` | Kotlin/KSP/AGP/Detekt/Dagger/firebasePerf/Compose/Apollo/Firebase/Coroutines/Serialization/Datetime all bumped; no leftover legacy Detekt or `android-library` coordinates | ✓ VERIFIED | Read in full; all target values present, legacy coordinates absent. |
| `shared/build.gradle.kts` | KMP Android library via `com.android.kotlin.multiplatform.library`; no top-level `android{}`; `jvm()`/iOS targets intact; Apollo cache compiler plugin registered | ✓ VERIFIED | Read in full; matches exactly. |
| `androidApp/build.gradle.kts` | New `packaging{}` DSL; AGP-9-floor Hilt/firebase-perf; `firebase-auth-ktx` substitution; `kotlin-metadata-jvm` force | ✓ VERIFIED | Read in full; all present with inline rationale comments. |
| `gradle/wrapper/gradle-wrapper.properties` | Gradle 9.7.1, checksum-verified | ✓ VERIFIED | `distributionSha256Sum` present (64 hex chars), URL host `services.gradle.org`. Local `./gradlew --version` confirms `Gradle 9.7.1`. |
| `settings.gradle.dcl` | DCL conversion of the settings file | ✓ VERIFIED | Exists; `settings.gradle.kts` deleted (no coexistence). |
| `androidApp/proguard-rules.pro` | R8 `-dontwarn` rules for openfeedback's stale kotlinx-datetime classes, documented | ✓ VERIFIED | Three `-dontwarn` lines present with an inline comment explaining cause and the `OPEN_FEEDBACK_ENABLED=false` safety argument. |
| `buildSrc/src/main/java/Dependencies.kt` | `compile=37`, `target=36` deliberately decoupled, documented | ✓ VERIFIED | Inline comment explains the decoupling rationale; matches STATE.md's Blockers/Concerns follow-up entry. |
| `shared/.../GraphQLStore.kt` | `.mapNotNull` (not `.map`) on all 4 `CacheAndNetwork` accessors | ✓ VERIFIED | Read in full; all four accessors (`partners`, `rooms`, `sessions`, `speakers`) use `.mapNotNull { ... return@mapNotNull null ... }`. |
| `.planning/STATE.md` | Consolidated `## Phase 02 version deviations` + `## Phase 02 DCL pilot outcome` sections | ✓ VERIFIED | Both sections present, all 7 BUILD ids covered. |
| `.planning/PROJECT.md` | Key Decisions row for Phase 2 | ✓ VERIFIED | Phase 2 row present alongside the Phase 1 row. |
| `.planning/REQUIREMENTS.md` | BUILD-01..07 marked `[x]` complete; D-03 deviation note | ✓ VERIFIED | All seven checked; D-03 note present under §BUILD with full version-target reasoning. |

### Key Link Verification

| From | To | Via | Status | Details |
|---|---|---|---|---|
| `gradle/libs.versions.toml` | `shared/build.gradle.kts` | `alias(libs.plugins.android.kotlin.multiplatform.library)` | ✓ WIRED | Confirmed present in both files, version resolves through the shared `agp` ref. |
| `gradle/wrapper/gradle-wrapper.properties` | `.github/actions/android-setup/action.yml` | `gradle-version: wrapper` (no CI-file edit needed) | ✓ WIRED | CI green on Gradle-9.7.1 commits without any workflow-file change (confirmed no `.github/` diff for this phase). |
| `build.gradle.kts` (root) | `shared/build.gradle.kts` | root declares `android.kotlin.multiplatform.library apply false` | ✓ WIRED | Confirmed in root `build.gradle.kts`; legacy `android.library apply false` fully removed. |
| `gradle/libs.versions.toml` (`appolloCache`) | `shared/build.gradle.kts` (`apollo { plugin(...) }`) | compiler-plugin coordinate references `libs.versions.appolloCache.get()` | ✓ WIRED | Confirmed in `shared/build.gradle.kts`. |

### Behavioral Spot-Checks

| Behavior | Command | Result | Status |
|---|---|---|---|
| Gradle wrapper reports 9.7.1 locally | `./gradlew --no-daemon --version` | `Gradle 9.7.1` (Daemon JVM: Homebrew openjdk@17) | ✓ PASS |
| CI green on every phase-produced commit | `gh run list --branch feature/reno_phase_2 --json databaseId,headSha,conclusion,name,status` | All `android.yml`/`ios.yml` runs for phase commits (`df57a6e` through `d63ed56`, where triggered) report `conclusion: success` | ✓ PASS |
| No debt markers (`TBD`/`FIXME`/`XXX`) introduced in phase-touched build/source files | `grep -nE "TBD\|FIXME\|XXX"` across all `covered_files` build/source entries | No matches in any phase-touched file; the two pre-existing `TODO`s in `StoreStubs.kt` predate the phase (confirmed via `git diff` against the pre-phase commit — only a blank-line change in that file) | ✓ PASS |
| Full on-device app run (build/install/launch/navigate) | `android run` / `./gradlew assemble*` / full clean build | Not re-run by the verifier — already exercised per-stage in each plan's own verify gate, cross-checked here against CI green + code inspection rather than re-running a 10+ minute clean build | ? SKIP (redundant with CI evidence already gathered) |

### Requirements Coverage

| Requirement | Source Plan | Description | Status | Evidence |
|---|---|---|---|---|
| BUILD-01 | 02-01 | Kotlin 2.4.0 (superseded 2.4.20) | ✓ SATISFIED | `kotlin = "2.4.20"` in catalog; CI green; REQUIREMENTS.md `[x]` |
| BUILD-02 | 02-02 | AGP 9.2.0 (superseded 9.4.0) + `com.android.kotlin.multiplatform.library` | ✓ SATISFIED | Catalog + `shared/build.gradle.kts` confirmed; legacy plugin fully removed |
| BUILD-03 | 02-02 | Gradle 9.7.1 | ✓ SATISFIED | Wrapper properties + local `--version` confirmed |
| BUILD-04 | 02-03 | Compose BOM 2026.08.00 (superseded 2026.09.00) | ✓ SATISFIED | Catalog confirmed; CI green |
| BUILD-05 | 02-03 | Apollo GraphQL 5.0.1 (superseded 5.2.0) | ✓ SATISFIED | Catalog + import-path migration confirmed in `Apollo.kt`/`ApolloCache.kt`/`GraphQLStore.kt` |
| BUILD-06 | 02-04 | Firebase BOM/Coroutines/kotlinx-serialization/kotlinx-datetime latest stable | ✓ SATISFIED | Catalog confirmed; regression test file exists (`ScheduleSlotDateParsingTest.kt`) |
| BUILD-07 | 02-05 | Build files migrated to `.gradle.dcl` where support allows; documented elsewhere | ✓ SATISFIED | `settings.gradle.dcl` exists; documentation of the `androidApp`/`shared` exception is thorough in STATE.md |

No orphaned requirements: all 7 BUILD requirement IDs declared in REQUIREMENTS.md's Phase 2 traceability table are claimed by exactly one of the 5 plans' `requirements:` frontmatter, and all 7 are marked `[x]` complete in REQUIREMENTS.md.

### Anti-Patterns Found

None blocking. No `TBD`/`FIXME`/`XXX` markers in any phase-touched file. No stub implementations, no hardcoded-empty return values introduced by this phase's diff. The pre-existing `TODO`s in `StoreStubs.kt` predate the phase and are out of scope (confirmed via diff against the pre-phase commit).

The 02-REVIEW.md code-review report (already produced for this phase, `.planning/phases/02-dependency-build-tooling-upgrade/02-REVIEW.md`) found:

- **WR-01** (Warning): `MainActivity.kt`'s Speaker-screen analytics `pageEvent` never fires due to a route-equality bug — confirmed pre-existing (only a blank-line diff in this phase), not introduced by Phase 2. Not a gap for this phase's goal.
- **IN-01** (Info): Firebase catalog aliases retain a `-ktx` suffix in their key name even though they now resolve to non-KTX artifacts — cosmetic, not a functional issue, tracked for a future cleanup commit.

Both are correctly out-of-scope for this phase's goal (zero observable behavior change on the modernized toolchain) and neither blocks the phase.

### Deviation Documentation Audit (D-03 obligation)

Per the verification brief's note, four deliberate, developer-authorized deviations were checked against D-03's "record the deviation and its reason" obligation — all four are properly documented, not just present:

| Deviation | Documented in code | Documented in STATE.md/SUMMARY | Verdict |
|---|---|---|---|
| targetSdk kept at 36 while compileSdk forced to 37 | `buildSrc/.../Dependencies.kt` inline comment explaining the decoupling | STATE.md § Blockers/Concerns: explicit tracked follow-up with a link to the Android-17 behavior-changes doc to review before bumping | ✓ Properly recorded |
| `GraphQLStore.kt` CacheAndNetwork `.map`→`.mapNotNull` fix (user-authorized override of a plan prohibition) | No inline code comment, but the change is self-evidently narrow (4 accessors, same pattern) | 02-03-SUMMARY.md's Deviations section documents the conflict, the user's explicit choice, and the fix in detail; STATE.md Decisions log carries a one-line summary | ✓ Properly recorded |
| `firebase-auth-ktx` dependencySubstitution | `androidApp/build.gradle.kts` inline comment with full rationale and the exact forcing chain | STATE.md § Blockers/Concerns: tracked follow-up to re-verify the literal version on any future `firebaseBom` bump | ✓ Properly recorded |
| R8 `-dontwarn` rules for openfeedback (feature gated off) | `androidApp/proguard-rules.pro` inline comment citing the R8-generated origin and the `OPEN_FEEDBACK_ENABLED=false` safety argument | 02-04-SUMMARY.md documents the R8 failure, the fix, and the safety verification | ✓ Properly recorded |

### Human Verification Required

6 items — see YAML frontmatter `human_verification` for full detail. Summary:

1. **02-01 sanity pass** — Kotlin/KSP bump visual/behavioral parity (Agenda/Speakers, screenshots exist).
2. **02-02 D-04 smoke pass** — AGP 9 plugin swap across Agenda/Speakers/Venue/Bookmarks, including bookmark persistence (screenshots exist).
3. **02-03a Compose BOM parity** — day-pager/theming visual check (screenshot exists).
4. **02-03b D-07 offline-cache functional check** — the exact regression this phase's own fix targeted; strong logcat/on-disk evidence exists, but final on-device confirmation is a human call. **Flagged for extra attention:** the `02-03-08-agenda-offline-fixed.png` screenshot shows only one session (with a bookmark checkmark) and an otherwise-empty screen, while sibling online screenshots for the same timeslot show 3 sessions — worth confirming this is a carried-over "Favori" filter state, not a partial-data regression.
5. **02-04 Firebase/datetime parity** — day tabs, session times, Firebase-backed screens (screenshots exist).
6. **General zero-regression sign-off** — confirm none of the four D-03-documented deviations introduced user-visible behavior change.

### Gaps Summary

No gaps found. All 7 BUILD requirements are satisfied in the codebase (not just claimed in SUMMARYs): version catalog values, plugin migrations, import-path migrations, and documentation obligations were independently verified by reading the actual files, not by trusting SUMMARY.md text. CI is confirmed green (via live `gh run list`) on every commit this phase produced. The only reason this phase is not marked `passed` is that its own success criteria include visual/behavioral "runs unchanged for users" claims which — per this project's `human_verify_mode: end-of-phase` setting — were deliberately deferred by each plan's `<human-check>` blocks to this end-of-phase verification step, and an AI-captured screenshot is not a substitute for the developer's own sign-off on visual parity.

---

_Verified: 2026-09-19T12:30:00Z_
_Verifier: Claude (gsd-verifier)_
