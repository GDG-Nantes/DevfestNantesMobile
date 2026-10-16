---
status: diagnosed
trigger: "UAT G-05-3 (Phase 05 test 3): crashed. I changed the flag on Firebase Remote Config and got the error FATAL EXCEPTION: main Process: com.gdgnantes.devfest.mobile.androidapp.dev, PID: 26012 java.lang.NoClassDefFoundError: Failed resolution of: Lkotlinx/datetime/Instant;"
created: 2026-10-16T13:30:00Z
updated: 2026-10-16T14:15:00Z
goal: find_root_cause_only
---

## Current Focus

bug_class: Bohrbug (deterministic link failure; reproduced on the JVM every run)
hypothesis: CONFIRMED - OpenFeedback 1.0.0-alpha.3 bytecode links against kotlinx.datetime.Instant/Clock (kotlinx-datetime 0.6.x), but the runtime classpath resolves kotlinx-datetime 0.8.0, which has no such classes; the R8 -dontwarn "unreachable" premise is false because local.properties enables OPEN_FEEDBACK_ENABLED through the secrets plugin
test: done - dependencyInsight, jar inspection, javap link check, differential JVM reproduction (0.8.0 fails; 0.6.2 and 0.8.0-0.6.x-compat pass)
expecting: n/a
next_action: return ROOT CAUSE FOUND to the caller (plan-phase --gaps handles the fix)

reasoning_checkpoint:
  hypothesis: "The NoClassDefFoundError happens because io.openfeedback 1.0.0-alpha.3 is compiled against kotlinx-datetime 0.6.x and references kotlinx.datetime.Instant directly. Gradle resolves kotlinx-datetime 0.8.0, where that class does not exist (typealias only)."
  confirming_evidence:
    - "dependencyInsight: kotlinx-datetime 0.6.0 -> 0.8.0 by conflict resolution, requested by openfeedback-android:1.0.0-alpha.3"
    - "kotlinx-datetime-jvm-0.8.0.jar has no kotlinx/datetime/Instant.class; 0.6.2 and 0.8.0-0.6.x-compat do"
    - "javap: timestampToInstant does getstatic kotlinx/datetime/Instant.Companion (the crash frame, line 8)"
    - "JVM repro: only swapping the datetime jar toggles the exact NoClassDefFoundError"
  falsification_test: "If the repro still threw with 0.6.2/compat on the classpath, or 0.8.0 contained Instant.class, the hypothesis would be false. Neither happened."
  fix_rationale: "Make kotlinx.datetime.Instant/Clock (and the kotlinx.datetime.Instant overloads) exist at runtime by resolving kotlinx-datetime to 0.8.0-0.6.x-compat. Downgrading to 0.6.2 is the alternative, but it would undo Phase 02 and break the kotlin.time.Clock/Instant call sites."
  blind_spots: "Not checked: whether swapping to compat compiles cleanly. The project mixes `import kotlinx.datetime.Instant` (core:model ScheduleSlot.kt/Agenda.kt, feature:agenda Agenda.kt, ScheduleSlotDateParsingTest.kt) with kotlin.time.Instant (IsoDate.kt, AgendaColumn.kt). Under compat these are distinct types, so expect deprecation warnings and possible type mismatches. Not checked: the gitlive/Firestore path beyond the mapping (e.g. comment posting, the Clock.System reference) on a device after a fix. Not checked: iOS consumption of the compat artifact (artifacts exist for all iOS targets)."
  candidate_causes:
    - "code/dependency: stale prebuilt third-party bytecode (openfeedback 0.6.x-linked) vs upgraded kotlinx-datetime 0.8.0 - CONFIRMED"
    - "config: -dontwarn rules + false 'OPEN_FEEDBACK_ENABLED=false' premise (the secrets plugin overrides it from local.properties) - CONFIRMED as the contributing reason it was not caught"
    - "environment: R8/minify stripping classes - ELIMINATED (debug build, JVM repro without R8)"
    - "data: Remote Config flag value - trigger only, not a cause (the flag correctly enabled the feature)"
  and_gate: "yes - the user-visible crash needs (a) the incompatible classpath AND (b) a build where OPEN_FEEDBACK_ENABLED resolves true (local.properties via secrets plugin) AND (c) Remote Config openfeedback_enabled=true. (a) is the defect; (b) is why the Phase 02 suppression was wrong; (c) is the trigger."

## Symptoms

expected: After answering the data-sharing consent dialog, the session detail feedback area shows or hides exactly as before the FeedbackFlags seam (05-08), driven by real Remote Config values.
actual: App crashes (FATAL EXCEPTION main) after the feedback flag is enabled on Firebase Remote Config and a session detail is opened on the dev debug build.
errors: |
  java.lang.NoClassDefFoundError: Failed resolution of: Lkotlinx/datetime/Instant;
    at io.openfeedback.mappers.FirestoreToModelMappers_androidKt.timestampToInstant(FirestoreToModelMappers.android.kt:8)
    at io.openfeedback.sources.OpenFeedbackFirestore.toComment(OpenFeedbackFirestore.kt:58)
    at io.openfeedback.sources.OpenFeedbackFirestore.toCommentsMap(OpenFeedbackFirestore.kt:74)
    at io.openfeedback.sources.OpenFeedbackFirestore$sessionThings$1$sessionThings$2.invoke(OpenFeedbackFirestore.kt:98)
    at dev.gitlive.firebase.SpecialValueSerializer.deserialize(serializers.kt:128)
    ... dev.gitlive.firebase.firestore.DocumentSnapshot.data(firestore.kt:453)
    ... io.openfeedback.sources.OpenFeedbackFirestore$sessionThings$$inlined$mapNotNull$1$2.emit
reproduction: Test 3 in .planning/phases/05-test-coverage-retrofit/05-UAT.md - enable the feedback flag in Firebase Remote Config, open a session detail on the dev debug build; crash when OpenFeedback Firestore comments are mapped.
started: Discovered during Phase 05 UAT (branch feature/reno_phase_5). Previously untestable because the feedback area was hidden by the flag.

## Eliminated

- hypothesis: R8/minification stripped kotlinx.datetime.Instant
  evidence: The crash is on the debug build (no minify). The plain-JVM reproduction with no shrinking fails the same way with kotlinx-datetime 0.8.0. The class is missing from the 0.8.0 artifact itself.
  timestamp: 2026-10-16T14:00:00Z

- hypothesis: A Phase 05 change (05-02 date parser swap to kotlin.time.Instant, or the 05-08 FeedbackFlags seam) introduced the crash
  evidence: The breaking version bump is 736c843 (Phase 02). The crash frame is entirely inside OpenFeedback/gitlive code reached after the feedback composable renders, which 05-08 only gates (enabled && remote flag, logic unchanged). 05-02 does not change which kotlinx-datetime OpenFeedback links against.
  timestamp: 2026-10-16T14:02:00Z

- hypothesis: Upgrading OpenFeedback to the latest release fixes the incompatibility
  evidence: The latest published release, 1.0.0-alpha.6 (2025-03-11), still declares kotlinx-datetime 0.6.2. Upstream main is on 0.7.1-0.6.x-compat (unreleased) and still uses kotlinx.datetime.Instant.
  timestamp: 2026-10-16T14:10:00Z

## Evidence

- timestamp: 2026-10-16T13:30:00Z
  checked: Knowledge base (.planning/debug/knowledge-base.md) and MemPalace
  found: .planning/debug/ was empty; no knowledge base file exists. No prior pattern.
  implication: No known-pattern candidate; proceed with open investigation.

- timestamp: 2026-10-16T13:30:00Z
  checked: gradle/libs.versions.toml and git log -G on version lines
  found: kotlinxDatetime = "0.8.0" (bumped from 0.6.2 in f42a027 "Phase 2: Dependency & Build Tooling Upgrade (#418)"); openfeedback = "1.0.0-alpha.3" (unchanged since abc6985, pre-GSD)
  implication: The datetime bump happened in Phase 02, not Phase 05. Openfeedback was not bumped alongside.

- timestamp: 2026-10-16T13:30:00Z
  checked: .planning/STATE.md Phase 02 decisions
  found: "Added three R8 -dontwarn rules (R8-generated, verbatim) for openfeedback's stale kotlinx-datetime 0.6.x Clock/Instant class references, safe because OPEN_FEEDBACK_ENABLED=false makes those code paths unreachable at runtime". Also: "kotlinx.datetime.Instant's typealias to kotlin.time.Instant survived (deprecated, not removed) into kotlinx-datetime 0.8.0"
  implication: The incompatibility was already detected by R8 in Phase 02 and suppressed on the premise that the feedback code path is unreachable. A typealias is source-level only and does not produce a kotlinx/datetime/Instant class file, so precompiled openfeedback bytecode cannot link.

- timestamp: 2026-10-16T13:35:00Z
  checked: androidApp/build.gradle.kts, androidApp/proguard-rules.pro, local.properties (keys only), FeedbackFormViewModel.kt
  found: build.gradle.kts default buildConfigField OPEN_FEEDBACK_ENABLED="false", but the secrets-gradle-plugin overrides BuildConfig fields from local.properties, which sets OPEN_FEEDBACK_ENABLED="true" (plus real project ids). FeedbackFormViewModel shows the form when openFeedbackConfig.enabled && remoteConfig "openfeedback_enabled". proguard-rules.pro lines 30-39 carry the 3 -dontwarn rules justified by "OPEN_FEEDBACK_ENABLED = false ... unreachable at runtime".
  implication: The Phase 02 "unreachable" premise is false for any build with a populated local.properties (this dev machine, and presumably the release/CI build that injects secrets). Flipping Remote Config is the only remaining gate, and the user did exactly that.

- timestamp: 2026-10-16T13:40:00Z
  checked: ./gradlew :androidApp:dependencyInsight --configuration debugRuntimeClasspath --dependency org.jetbrains.kotlinx:kotlinx-datetime
  found: Resolved org.jetbrains.kotlinx:kotlinx-datetime:0.8.0 "By conflict resolution: between versions 0.8.0 and 0.6.0". The 0.6.0 request comes from io.openfeedback:openfeedback-android:1.0.0-alpha.3 <- openfeedback:1.0.0-alpha.3 <- openfeedback-viewmodel-android <- openfeedback-viewmodel <- :feature:session-detail / :androidApp. The 0.8.0 request comes from the catalog (core:model api, feature:agenda, androidApp).
  implication: OpenFeedback was built for kotlinx-datetime 0.6.0, but Gradle silently upgrades it to 0.8.0 on the runtime classpath.

- timestamp: 2026-10-16T13:42:00Z
  checked: unzip -l of kotlinx-datetime-jvm-0.6.2.jar vs kotlinx-datetime-jvm-0.8.0.jar (Gradle cache)
  found: 0.6.2 contains kotlinx/datetime/Instant.class, Instant$Companion.class, Clock.class, Clock$System.class. 0.8.0 contains none of them (only InstantKt, InstantJvmKt, InstantTimeMark, ClockKt facades).
  implication: Direct proof that the class named in the NoClassDefFoundError does not exist in the resolved artifact.

- timestamp: 2026-10-16T13:45:00Z
  checked: javap -c on io.openfeedback.mappers.FirestoreToModelMappers_androidKt (openfeedback-android-1.0.0-alpha.3 AAR classes.jar), and javap -v constant-pool scan of every class in all 5 openfeedback AARs
  found: timestampToInstant returns kotlinx.datetime.Instant and does `getstatic kotlinx/datetime/Instant.Companion` then `invokevirtual kotlinx/datetime/Instant$Companion.fromEpochSeconds(JI)` - exactly the frame at FirestoreToModelMappers.android.kt:8 in the crash. Other references to removed classes: io/openfeedback/model/Comment (createdAt: kotlinx/datetime/Instant), sources/OpenFeedbackFirestore, extensions/CommentMap_extKt, extensions/SessionData_extKt (kotlinx/datetime/Clock, Clock$System, Instant - commitComment), mappers/FirestoreToModelMappersKt sort comparator; openfeedback-viewmodel-android SessionDataToUiModelsKt calls TimeZoneKt.toLocalDateTime(Lkotlinx/datetime/Instant;Lkotlinx/datetime/TimeZone;). openfeedback-m3/ui-models/resources reference no kotlinx.datetime classes.
  implication: The crash site is the first of several linkage failures; reading comments, mapping them to UI models, and posting a comment (commitComment -> Clock.System) would all fail too. Fixing only Instant would not be enough.

- timestamp: 2026-10-16T13:55:00Z
  checked: Member-level link check (javap -s) of all 19 kotlinx.datetime Methodref/Fieldref entries in openfeedback-android + openfeedback-viewmodel-android against kotlinx-datetime-jvm 0.6.2, 0.8.0 and 0.8.0-0.6.x-compat (downloaded from Maven Central)
  found: Against 0.8.0 the real breaks are Instant, Instant$Companion.fromEpochSeconds(JI), Instant.hashCode, Clock$System.INSTANCE/now, and TimeZoneKt.toLocalDateTime(Lkotlinx/datetime/Instant;Lkotlinx/datetime/TimeZone;). DateTimeFormatBuilder members resolve through superinterfaces (WithYearMonth/DateTimeFormatBuilder) in every version, so not a problem. Against 0.6.2 and 0.8.0-0.6.x-compat every reference resolves; compat also ships the kotlin.time.Instant overloads (toLocalDateTime(kotlin.time.Instant, TimeZone)).
  implication: The compat artifact covers the whole OpenFeedback surface, not just the crash frame.

- timestamp: 2026-10-16T14:00:00Z
  checked: Deterministic JVM reproduction (scratchpad Repro.java) calling io.openfeedback.mappers.FirestoreToModelMappers_androidKt.timestampToInstant(new com.google.firebase.Timestamp(...)) with the real openfeedback-android 1.0.0-alpha.3 classes.jar, firebase-common 22.2.1, kotlin-stdlib 2.4.20, android.jar stubs, swapping only the kotlinx-datetime jar
  found: 0.8.0 -> "java.lang.NoClassDefFoundError: kotlinx/datetime/Instant / Caused by: ClassNotFoundException: kotlinx.datetime.Instant" (same as device logcat). 0.6.2 -> OK, returns kotlinx.datetime.Instant 2025-10-16T09:00:00Z. 0.8.0-0.6.x-compat -> OK, same result.
  implication: Differential reproduction: the kotlinx-datetime jar version alone flips the failure on and off. Confirms the mechanism; R8 is not involved (plain JVM, no shrinking).

- timestamp: 2026-10-16T14:02:00Z
  checked: git show 736c843 (build(02-04), part of f42a027 / PR #418)
  found: kotlinxDatetime 0.6.2 -> 0.8.0, commit body records "io.openfeedback:openfeedback-viewmodel was compiled against kotlinx-datetime 0.6.x's real classes, now typealiases with no .class files; openfeedback is feature-flagged off (OPEN_FEEDBACK_ENABLED=false), so unreachable" and adds the three -dontwarn rules.
  implication: Introducing commit identified. Phase 05 (incl. 05-02 date parser, 05-08 FeedbackFlags seam) did not introduce it; the crash has existed on any feedback-enabled build since Phase 02 and was only surfaced now because UAT test 3 turned the Remote Config flag on.

- timestamp: 2026-10-16T14:05:00Z
  checked: .github/actions/android-setup/action.yml
  found: CI writes local.properties with only sdk.dir, so CI builds keep OPEN_FEEDBACK_ENABLED="false" and never render the OpenFeedback composable; androidTest SessionDetailSmokeTest uses OpenFeedbackConfig(enabled = false) and a fake FeedbackFlags.
  implication: No automated gate can reach the broken path; only a developer/release build with secrets in local.properties plus Remote Config openfeedback_enabled=true hits it.

- timestamp: 2026-10-16T14:10:00Z
  checked: Maven Central metadata for io.openfeedback (latest 1.0.0-alpha.6, 2025-03-11) + its POM; gh on paug/openfeedback-sdk-kotlin (releases, PR #52, code search); kotlinx-datetime metadata and README
  found: alpha.4 and alpha.6 POMs still declare kotlinx-datetime-jvm 0.6.2 (built before kotlinx-datetime 0.7.0). Upstream main switched to kotlinx-datetime "0.7.1-0.6.x-compat" in ddf0549 (PR #52, merged 2026-10-15, UNRELEASED) and its sources still import kotlinx.datetime.Instant (FirestoreModels.kt, FirestoreToModelMappers*.kt). kotlinx-datetime publishes 0.8.0-0.6.x-compat for jvm/iosarm64/iossimulatorarm64/iosx64. README "Deprecation of Instant": "If all else fails, use the compatibility release ... This artifact still contains kotlinx.datetime.Instant and kotlinx.datetime.Clock, ensuring that third-party libraries reliant on them can still be used" and "Compatibility releases will be published for versions up to 0.8.x ... but not longer"; with compat, "required kotlinx.datetime.Instant, found kotlin.time.Instant" errors may appear, fixed via kotlin.time imports or toDeprecatedInstant()/toStdlibInstant().
  implication: Bumping OpenFeedback does not help: no published release targets kotlin.time.Instant, and even a future release from current upstream main would still need the compat classes at runtime (and Gradle would still pick plain 0.8.0 over 0.7.1-0.6.x-compat). The app-side fix is to resolve kotlinx-datetime to 0.8.0-0.6.x-compat. It is a bridge that ends with 0.8.x.

## Resolution

root_cause: |
  Binary incompatibility between io.openfeedback 1.0.0-alpha.3 and kotlinx-datetime 0.8.0, combined with a wrong "unreachable code" premise.
  (1) Code/dependency: commit 736c843 (02-04, Phase 02 PR #418) bumped kotlinxDatetime 0.6.2 -> 0.8.0. OpenFeedback 1.0.0-alpha.3 (openfeedback-android + openfeedback-viewmodel-android) was compiled against kotlinx-datetime 0.6.x and its bytecode links directly against kotlinx.datetime.Instant, Instant$Companion, Clock$System and TimeZoneKt.toLocalDateTime(kotlinx.datetime.Instant, TimeZone). Gradle conflict resolution silently upgrades OpenFeedback's 0.6.0 request to 0.8.0, where those classes no longer exist (Instant/Clock are only source-level typealiases to kotlin.time). The first OpenFeedback Firestore comment mapping (FirestoreToModelMappers_androidKt.timestampToInstant, getstatic kotlinx/datetime/Instant.Companion) throws NoClassDefFoundError on the main thread.
  (2) Config (why it was not caught and shipped as "safe"): the same commit silenced R8's missing-class report with three -dontwarn rules on the premise that OPEN_FEEDBACK_ENABLED=false makes OpenFeedback unreachable. That holds only for the build.gradle.kts default. The secrets-gradle-plugin overrides it from local.properties, which on developer/release machines sets OPEN_FEEDBACK_ENABLED="true". After that, Remote Config openfeedback_enabled is the only gate, and UAT test 3 turned it on. CI (local.properties = sdk.dir only) and the smoke tests (enabled = false + fake flags) never reach the path.
  Not caused by Phase 05: 05-02 and 05-08 are not involved. R8/minify is not involved (debug build; reproduced on a plain JVM), though release builds would crash the same way because -keep io.openfeedback.** + -dontwarn keep the dangling references.
fix: (not applied - goal find_root_cause_only)
verification: Root cause reproduced deterministically on the JVM (0.8.0 fails with the same NoClassDefFoundError; 0.6.2 and 0.8.0-0.6.x-compat pass). No fix verification performed.
files_changed: []
