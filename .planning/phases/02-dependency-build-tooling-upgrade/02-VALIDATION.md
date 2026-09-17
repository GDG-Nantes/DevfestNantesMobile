---
phase: "02"
slug: "dependency-build-tooling-upgrade"
# status lifecycle: draft (seeded by plan-phase) → validated (set by validate-phase §6)
# audit-milestone §5.5 distinguishes NOT-VALIDATED (draft) from PARTIAL (validated + nyquist_compliant: false) (#2117)
status: draft
nyquist_compliant: true
wave_0_complete: true
created: "2026-09-17"
---

# Phase 02 — Validation Strategy

> Per-phase validation contract for feedback sampling during execution.

---

## Test Infrastructure

| Property | Value |
|----------|-------|
| **Framework** | Kotlin Test (`kotlin.test`) for `commonTest`/`jvmTest` in `shared`; JUnit 4 + Espresso 3.6.1 + Compose UI Test for `androidApp` unit and instrumented tests |
| **Config file** | None dedicated — `androidApp/build.gradle.kts`'s `testOptions { }` block and `gradle/libs.versions.toml`'s `test`/`android-test` bundles |
| **Quick run command** | `./gradlew --no-daemon :shared:jvmTest :androidApp:testDebugUnitTest` |
| **Full suite command** | `./gradlew --no-daemon detekt lint :shared:assemble :androidApp:assembleDebug :androidApp:assembleRelease :shared:jvmTest :androidApp:testDebugUnitTest` plus the three `:shared:compileKotlinIos*` targets; `connectedDebugAndroidTest` and the Xcode shared-framework build run in CI (`android.yml` `instrumentation-tests`, `ios.yml`) |
| **Estimated runtime** | ~180 s local (cold ~420 s); CI ~15 min for both workflows |

**Verify-command provenance:** the CI-green gate reuses the `gh run list --workflow … --json databaseId` / `gh run view … --json status,conclusion` idiom Phase 01 proved on a live run, extended to scope the lookup to the pushed commit's `headSha` and to assert `android.yml`'s job count. No new CI-polling mechanism was invented.

---

## Sampling Rate

- **After every task commit:** the quick run command, plus that task's catalog/source assertion gate and its CI-green gate on the pushed commit (D-02 makes CI-green part of the per-stage contract, not a wave-boundary check).
- **After every plan wave:** the full suite command. Each wave is one staging group, so this is also the D-02 "green before the next group starts" gate.
- **Before `/gsd-verify-work`:** full suite green plus both workflows green on the final commit.
- **Max feedback latency:** ~180 s local; ~15 min including the CI round-trip that D-02 requires per stage.

---

## Per-Task Verification Map

| Task ID | Plan | Wave | Requirement | Threat Ref | Secure Behavior | Test Type | Automated Command | File Exists | Status |
|---------|------|------|-------------|------------|-----------------|-----------|-------------------|-------------|--------|
| 02-01-01 | 01 | 1 | BUILD-01/02/04/05 (policy) | — | N/A — `checkpoint:decision`, no code produced | checkpoint | *(none — checkpoint task)* | N/A | ⬜ pending |
| 02-01-02 | 01 | 1 | BUILD-01 | T-02-01 / T-02-02 | Versions re-verified against registry `maven-metadata.xml` before the edit; no plugin id or artifact group authored | smoke + catalog assertion + CI | `grep … 'STAGE1-CATALOG-OK'` · `./gradlew :shared:assemble :androidApp:assembleDebug :shared:jvmTest :androidApp:testDebugUnitTest` · `gh run watch` both workflows · `android run` | ✅ | ⬜ pending |
| 02-02-01 | 02 | 2 | BUILD-03 | T-02-04 | `distributionSha256Sum` present (64 hex) and `distributionUrl` host is `services.gradle.org` | source assertion + smoke + CI | `grep … 'WRAPPER-PINNED-OK'` · `./gradlew --version \| grep 'Gradle 9.7.1'` + full build · `gh run watch` both workflows | ✅ | ⬜ pending |
| 02-02-02 | 02 | 2 | BUILD-02 (gate) | T-02-06 | Pre-1.0 `[SUS]` static-analysis artifact human-verified against its official registry page before CI is pinned to it; `gate="blocking-human"`, never auto-approved | checkpoint | *(none — `blocking-human` checkpoint)* | N/A | ⬜ pending |
| 02-02-03 | 02 | 2 | BUILD-02 | T-02-05 / T-02-07 / T-02-08 | New plugin coordinates verified first-party; legacy Detekt coordinate fully removed (no half-rename); release build exercised rather than keep-rules broadened pre-emptively | source assertion ×3 + smoke + CI + manual | `grep … 'STAGE2-CATALOG-OK'` · `'SHARED-MIGRATED-OK'` · `'APP-MIGRATED-OK'` · `./gradlew detekt lint … assembleRelease …` · `gh run view` 4 jobs + `ios.yml` · `android run` | ✅ | ⬜ pending |
| 02-03-01 | 03 | 3 | BUILD-04 | T-02-13 | No new artifact group; BOM verified against Google Maven metadata | catalog assertion + smoke + instrumented (CI) + CI | `grep … 'STAGE3-CATALOG-OK'` · `./gradlew detekt lint :androidApp:assemble{Debug,Release} :androidApp:testDebugUnitTest` · `gh run view` 4 jobs (incl. `instrumentation-tests`) | ✅ | ⬜ pending |
| 02-03-02 | 03 | 3 | BUILD-05 | T-02-09 / T-02-10 / T-02-11 / T-02-12 | New `com.apollographql.cache` group verified against official docs + Maven Central; cache-group entry count asserted exactly 2 so a partial migration cannot pass; cache-key strategy preserved | catalog + source assertion ×2 + smoke + CI + manual | `grep … 'STAGE4-CATALOG-OK'` · `'APOLLO-SRC-MIGRATED-OK'` · `'APOLLO-PLUGIN-REGISTERED-OK'` · `./gradlew clean :shared:generateApolloSources …` · `gh run view` 4 jobs + `ios.yml` | ✅ | ⬜ pending |
| 02-04-01 | 04 | 4 | BUILD-06 (prerequisite) | T-02-14 | Value-level assertions (absolute epoch millis, both `Z` and `+02:00` forms) authored and green on the **pre-bump** version, so a post-bump red is attributable | unit | `grep … 'DATE-TEST-AUTHORED-OK'` · `./gradlew :shared:jvmTest --tests 'com.gdgnantes.devfest.model.ScheduleSlotDateParsingTest'` | ❌ → created by this task | ⬜ pending |
| 02-04-02 | 04 | 4 | BUILD-06 | T-02-15 / T-02-16 / T-02-17 / T-02-18 | RC/beta/alpha suffix negative-matched on the JSON parser version; all three iOS targets compiled explicitly (platform-asymmetric failure mode); all four Firebase SDKs asserted still declared | catalog + source assertion + unit + smoke + CI + manual | `grep … 'STAGE5-CATALOG-OK'` · `'MODEL-MINIMAL-DIFF-OK'` · `'FIREBASE-SDKS-INTACT-OK'` · `./gradlew … --tests 'ScheduleSlotDateParsingTest'` + `clean :shared:compileKotlinIos{X64,Arm64,SimulatorArm64}` + full suite · `gh run view` 4 jobs + `ios.yml` | ✅ (created in 02-04-01) | ⬜ pending |
| 02-05-01 | 05 | 5 | BUILD-07 | T-02-19 / T-02-20 / T-02-21 | Exactly one settings file in effect; repository list and both `include()`s proven intact via `./gradlew projects` subproject count, so a redirected repo or dropped module cannot pass green | source assertion + CLI output + smoke + CI | `grep … 'SETTINGS-FORM-EXCLUSIVE-OK'` · `./gradlew projects \| … 'PROJECT-TOPOLOGY-INTACT-OK'` · `./gradlew clean … detekt lint` + `:shared:compileKotlinIosSimulatorArm64` · `'DCL-OUTCOME-DOCUMENTED-OK'` · `gh run view` 4 jobs + `ios.yml` | ✅ | ⬜ pending |
| 02-05-02 | 05 | 5 | BUILD-01 … BUILD-07 (record) | T-02-22 / T-02-23 | All seven requirement ids asserted present in the outcome record (no silently-skipped deviation); pre-existing doc landmarks asserted surviving so a whole-file overwrite fails rather than passing | source assertion ×2 + CI | `grep … 'STATE-OUTCOMES-RECORDED-OK'` · `'DOCS-APPENDED-NOT-OVERWRITTEN-OK'` · `gh run watch` both workflows | ✅ | ⬜ pending |

*Status: ⬜ pending · ✅ green · ❌ red · ⚠️ flaky*

**Sampling continuity:** every non-checkpoint task carries at least one runnable `<automated>` command with a stated `<fails_when>`. The two checkpoint tasks (02-01-01, 02-02-02) are non-adjacent and each is immediately followed by a task with automated verification, so there is no run of 3 consecutive tasks without an automated signal.

---

## Wave 0 Requirements

Existing infrastructure covers all phase requirements — no task carries a `MISSING — Wave 0 …` sentinel, because every gate is either an existing Gradle task, a source/catalog assertion, or the CI idiom Phase 01 proved.

One test file is authored inside the phase rather than pre-existing:

- [ ] `shared/src/commonTest/kotlin/com/gdgnantes/devfest/model/ScheduleSlotDateParsingTest.kt` — value-level coverage for BUILD-06's kotlinx-datetime bump. Created by **02-04 Task 1** and required green on the pre-bump version before **02-04 Task 2** consumes it. It is deliberately authored one task before its consumer rather than in Wave 1: its whole value is being green against the *old* datetime version, which only establishes a baseline if it is written immediately before the bump.

Closes `02-RESEARCH.md` § Wave 0 Gaps item 1. Gap item 2 (no Apollo cache-behavior test exists) is an **accepted** gap, not an oversight: D-07 explicitly scopes Apollo cache verification to a manual functional check with no automated before/after test.

---

## Manual-Only Verifications

| Behavior | Requirement | Why Manual | Test Instructions |
|----------|-------------|------------|-------------------|
| Agenda renders and bottom-nav moves after the Kotlin bump | BUILD-01 | D-02 asks for a local sanity build/run per group; visual parity is not machine-assertable beyond "the app launched and attached a window", which is automated | 02-01 Task 1 `<human-check>` — launch the debug app, confirm Agenda lists sessions and nav destinations switch |
| `dev.detekt` 2.0.0-alpha legitimacy and stability acceptance | BUILD-02 | Trust decision on a pre-1.0 `[SUS]` artifact; auto-approving it would defeat the gate's purpose | 02-02 Task 2 — `checkpoint:human-verify gate="blocking-human"`, verified against `plugins.gradle.org/plugin/dev.detekt`, the detekt GitHub releases page, and `repo1.maven.org/maven2/dev/detekt/detekt-formatting/` |
| Agenda / Speakers / Venue / Bookmarks render and navigate on AGP 9 | BUILD-02 | D-04 specifies a manual smoke pass via the `android` CLI; per-screen visual and navigation correctness has no automated equivalent in this repo (TEST-04 expansion is Phase 5) | 02-02 Task 3 `<human-check>` — drive all four screens; also confirm pre-existing bookmarks survived (the D-06 boundary) |
| Compose visual parity, especially the Agenda day pager | BUILD-04 | Layout, theming and pager-indicator appearance are visual judgments; the two instrumented tests cover launch and date formatting but not appearance | 02-03 Task 1 `<human-check>` — swipe the day pager, check indicator, Material 3 colors, no clipped or overlapped content |
| Apollo normalized cache serves a repeated query and an offline read | BUILD-05 | D-07 scopes this to functional-only verification with no automated test and explicitly no before/after data comparison (D-06) | 02-03 Task 2 `<human-check>` — populate online, navigate away and back (immediate re-render), then airplane-mode + force-stop + relaunch and confirm data still shows; then confirm bookmarks intact |
| Agenda day tabs and session times unchanged after the datetime bump | BUILD-06 | The automated test pins parsing semantics at value level; what it cannot assert is the *displayed* time strings, which go through Android formatting code | 02-04 Task 2 `<human-check>` — compare displayed start/end times and day-tab contents against pre-stage screenshots |

All six are harvested at end of phase: `workflow.human_verify_mode` is `end-of-phase`, so these are `<verify><human-check>` blocks rather than mid-flight `checkpoint:human-verify` tasks and flow into `{phase}-UAT.md`. The one exception is the Detekt gate, which is a genuine `blocking-human` checkpoint — harvesting it at end of phase would place the verification *after* the pin it exists to gate.

---

## Validation Sign-Off

- [x] All tasks have `<automated>` verify or Wave 0 dependencies — every non-checkpoint task has at least one runnable command; the two checkpoints are verification/decision gates that produce no code
- [x] Sampling continuity: no 3 consecutive tasks without automated verify
- [x] Wave 0 covers all MISSING references — no `MISSING` sentinel exists; the one new test file is created by 02-04 Task 1 before its consumer
- [x] No watch-mode flags — every Gradle invocation uses `--no-daemon` and no `--continuous`
- [x] Feedback latency < 180 s local (CI round-trip ~15 min is required by D-02, not incidental)
- [x] `nyquist_compliant: true` set in frontmatter

**Approval:** pending
