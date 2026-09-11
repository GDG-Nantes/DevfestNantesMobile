# Codebase Concerns

**Analysis Date:** 2026-09-11

## Tech Debt

**Incomplete UUID Implementation in Stubs:**
- Issue: Placeholder UUIDs in mock data with TODO comments indicating incomplete implementation
- Files: `shared/src/commonMain/kotlin/com/gdgnantes/devfest/model/stubs/StoreStubs.kt`
  - Line 29: `openFeedbackFormId = ""` marked with TODO to "Replaces with UUID implementation"
  - Line 61: `id = ""` marked with TODO to "Replace with UUID implementation"
- Impact: Mock/stub data is incomplete, may cause issues when transitioning from stubs to real data or testing UUID-dependent features
- Fix approach: Implement proper UUID generation in stub builders, consider using `java.util.UUID.randomUUID()` or similar UUID generation

**Incomplete Analytics Event Implementation:**
- Issue: `eventFilter()` method in Firebase Analytics service is unimplemented
- Files: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/services/FirebaseAnalyticsService.kt`
  - Line 31: `override fun eventFilter() { Timber.d("TODO: eventFilter") }`
- Impact: Filter events are not being tracked to Firebase Analytics, missing user engagement data
- Fix approach: Implement proper Firebase event logging for filter operations with relevant parameters (e.g., filter type, filters applied)

**Detekt Configuration Mismatch:**
- Issue: Detekt config has `autoCorrect: true` but build.gradle.kts has `autoCorrect: false`
- Files: 
  - `linters/detekt-config.yml` (line 37: `autoCorrect: true`)
  - `shared/build.gradle.kts` (line 16: `autoCorrect = false`)
  - `androidApp/build.gradle.kts` (line 21: `autoCorrect = false`)
- Impact: Inconsistent code formatting; auto-correction not applied despite being configured, meaning detekt violations are reported but not fixed automatically
- Fix approach: Unify configuration - either set both to `true` for automatic fixing or both to `false` for manual review

## Known Bugs

**Unsafe Null Assertions in Navigation:**
- Symptoms: Potential runtime crashes when navigating to Session or Speaker screens if arguments are missing
- Files: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt`
  - Line 87: `val sessionId = backStackEntry.arguments!!.getString("sessionId")!!`
  - Line 116: `val speakerId = backStackEntry.arguments!!.getString("speakerId")!!`
- Trigger: Navigation to session or speaker details without proper arguments, or argument bundle being null
- Workaround: Ensure navigation always provides required arguments from calling code; currently no null-safety handling
- Fix approach: Use safe navigation (`arguments?.getString(...)`), provide default values, or validate arguments before use

**Unsafe Icon Vector Assertions in BottomAppBar:**
- Symptoms: Potential null pointer exceptions when rendering bottom navigation icons
- Files: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/components/appbars/BottomAppBar.kt`
  - Line 31: `imageVector = Screen.Agenda.imageVector(...)!!`
  - Line 52: `imageVector = Screen.Speakers.imageVector(...)!!`
  - Line 73: `imageVector = Screen.Venue.imageVector(...)!!`
  - Line 94: `imageVector = Screen.About.imageVector(...)!!`
- Trigger: If `Screen.imageVector()` returns null for any enum value
- Workaround: Ensure all Screen enum values have valid icon vectors
- Fix approach: Make `imageVector()` return non-nullable type, provide fallback icons, or use safe casting

## Security Considerations

**OpenFeedback Configuration Exposed:**
- Risk: API keys and Firebase configuration exposed as build config fields with placeholder "SECRET" values
- Files: `androidApp/build.gradle.kts`
  - Lines 41-46: Hardcoded "SECRET" strings in buildConfigField definitions
  - Lines 42-46: `OPEN_FEEDBACK_FIREBASE_*` fields containing secrets
- Current mitigation: Using placeholder "SECRET" string, but proper values would be exposed in source
- Recommendations: 
  - Use Gradle Secrets Plugin (already imported on line 15) to read secrets from local properties
  - Move OpenFeedback credentials to `local.properties` or environment variables
  - Ensure `local.properties` is in `.gitignore` (verify this is the case)
  - Consider using BuildConfig.DEBUG to conditionally load/mock these values in debug builds

**Build Config Debug Flag:**
- Risk: `OPEN_FEEDBACK_ENABLED` hardcoded to "false", but full credentials still defined
- Files: `androidApp/build.gradle.kts`, lines 41-46
- Current mitigation: Feature is disabled, but code path still compiles with secrets
- Recommendations: Clean up unused secret fields if feature remains disabled; if enabling later, secure credential management becomes critical

## Performance Bottlenecks

**Large Composable Functions:**
- Problem: Several UI composition functions exceed recommended size, potentially causing recomposition overhead
- Files affected:
  - `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/agenda/SessionFiltersDrawer.kt` (405 lines)
  - `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/speakers/list/Speakers.kt` (341 lines)
  - `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/datacollection/DataCollectionSettingsScreen.kt` (296 lines)
  - `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/screens/Home.kt` (214 lines)
- Cause: Monolithic Composables combining layout, state management, and business logic
- Improvement path: 
  - Break into smaller, focused Composables
  - Extract reusable sub-components (e.g., filter items in SessionFiltersDrawer)
  - Use `@Composable` function extraction to isolate high-frequency recompositions
  - Consider lazy composition for large lists

**Generated Apollo Response Adapters:**
- Problem: Multiple generated response adapter files exceed typical class size, with some at 200+ lines
- Files: 
  - `shared/build/generated/source/apollo/service/com/gdgnantes/devfest/graphql/fragment/SessionDetailsImpl_ResponseAdapter.kt` (227 lines)
  - `shared/build/generated/source/apollo/service/com/gdgnantes/devfest/graphql/GetSessionsQuery.kt` (144 lines)
- Cause: Apollo code generation creates one adapter per query/fragment
- Improvement path: This is generated code; no action needed, but monitor for method count limits on Android

## Fragile Areas

**Stub Data Dependency:**
- Files: `shared/src/commonMain/kotlin/com/gdgnantes/devfest/model/stubs/StoreStubs.kt` (115 lines)
- Why fragile: 
  - Test code or preview code may depend on specific stub data structure
  - Empty IDs and placeholder UUIDs could break equality checks or lookups
  - Random data generation (Random.nextInt, Random.nextLong) makes tests non-deterministic
- Safe modification: 
  - Add seed parameter to builders for deterministic testing
  - Replace empty strings with proper UUIDs before using in production-critical paths
  - Document which stub fields are safe to mutate
- Test coverage: Stubs are used in JVM tests but not heavily tested themselves

**Date Format Parsing:**
- Files: Multiple files use SimpleDateFormat with timezone pattern "XXX"
  - `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/utils/DateUtils.kt` (line 16)
  - `androidApp/src/androidTest/java/com/gdgnantes/devfest/androidapp/ui/screens/agenda/ScheduleSlotDateFormatAndroidTest.kt` (line 19)
  - `shared/src/jvmTest/kotlin/com/gdgnantes/devfest/store/graphql/GraphQLStoreJvmTest.kt` (line 95)
- Why fragile: 
  - SimpleDateFormat is not thread-safe; if used concurrently, can corrupt date parsing
  - Format string "XXX" for timezone is Java-specific; not portable to Kotlin/JS or shared code
  - No error handling for malformed dates in some paths
- Safe modification: 
  - Use `java.time` API instead of SimpleDateFormat where possible (requires API 26+)
  - For shared code, use kotlinx-datetime (already imported in gradle dependencies)
  - Document timezone assumptions in API contracts

**Firebase Analytics Service Size:**
- Files: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/services/FirebaseAnalyticsService.kt` (113 lines, with @Suppress("TooManyFunctions"))
- Why fragile: 
  - Single class with many similar event methods (TooManyFunctions suppression indicates linter warning)
  - Each event handler is a separate method, making refactoring risky
  - If Firebase API changes, all methods must be updated
- Safe modification: 
  - Group related events into helper methods
  - Consider using a builder pattern for complex event logging
  - Test coverage: Not explicitly tested in main test suite
- Test coverage: No unit tests found for analytics service

## Test Coverage Gaps

**Minimal Unit Test Coverage:**
- What's not tested: 
  - No unit tests in `androidApp/src/test/` (directory exists but is empty)
  - Android UI layer largely untested (only 1 file in androidTest)
  - ViewModels have no unit tests
  - Services (Analytics, ExternalContent, DataCollection, SessionFilters) have no unit tests
- Files affected: Nearly all files in `androidApp/src/main/java/com/gdgnantes/devfest/androidapp`
- Risk: UI state changes, navigation logic, and business logic bugs go undetected until manual testing or production
- Priority: High - ViewModel and service logic is critical to app correctness
- Recommended test structure:
  - Unit tests for ViewModels using InstantTaskExecutorRule
  - Unit tests for services with mocked dependencies
  - Integration tests for end-to-end flows

**GraphQL Store Tests Only Hit Happy Path:**
- What's not tested: 
  - No tests for error conditions (network failures, malformed responses)
  - No tests for data consistency/caching behavior
  - No tests for concurrent request handling
  - Tests use `// Accept null or X, just check no crash` pattern, not validating behavior
- Files: `shared/src/jvmTest/kotlin/com/gdgnantes/devfest/store/graphql/GraphQLStoreJvmTest.kt`
- Risk: Silent failures in data layer, inconsistent state caching, race conditions
- Priority: Medium - data layer is critical but somewhat covered by integration tests with real API
- Recommended additions:
  - Mock Apollo client for error injection testing
  - Add tests for caching and refresh behavior
  - Add tests for concurrent access patterns

**No Compose UI Tests:**
- What's not tested: 
  - Compose component rendering (no @Composable preview tests found)
  - Component interactions (clicks, gestures)
  - State management in Composables
  - Navigation transitions
- Files: All UI components in `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/ui/`
- Risk: UI bugs (layout breaks, unresponsive controls, wrong data display) not caught until manual testing
- Priority: Medium - visual bugs are less critical than logic bugs but affect user experience

## Missing Critical Features

**Analytics Event Filtering:**
- Problem: `eventFilter()` method is unimplemented (currently just logs "TODO")
- Blocks: User analytics data for filter interactions is not collected
- Impact: Product team cannot measure which filters users prefer, limiting insights for feature prioritization

## Scaling Limits

**Single Activity Navigation Bottleneck:**
- Current capacity: All navigation routed through MainActivity via NavHost
- Limit: If navigation graph complexity grows further (more routes, deeper nesting), single-activity pattern becomes harder to test and maintain
- Scaling path: 
  - Consider multi-activity pattern for independent feature modules
  - Implement navigation deep linking for better modularity
  - Use Hilt with entry points for feature-specific dependency injection

**Stub Data Generation Memory:**
- Current capacity: `MAX_*` constants allow up to 100 sessions, 100 speakers, 10 partners (see `StoreStubs.kt`)
- Limit: Random generation of all data simultaneously could cause memory pressure on low-end devices
- Scaling path: 
  - Implement lazy initialization for stubs
  - Add configurable limits for test environment
  - Use object pooling for frequently created stubs

## Dependencies at Risk

**Deprecated Accompanist Libraries:**
- Risk: Accompanist libraries are experimental/soft-deprecated in favor of stable Jetpack alternatives
- Impact: May need migration if Accompanist stops receiving updates
- Files affected: `gradle/libs.versions.toml` includes accompanist 0.36.0
  - Used in: `androidApp/build.gradle.kts` for pager and system UI controller
- Migration plan: 
  - Pager functionality → migrate to Jetpack Compose Pager (when stable)
  - SystemUIController → migrate to EdgeToEdge API (already used in MainActivity, line 59)

**OpenFeedback Library - Early Alpha:**
- Risk: Library at `1.0.0-alpha.3` version; API may change, long-term support uncertain
- Impact: Updates may require code changes; if project is abandoned, custom feedback implementation needed
- Files: `gradle/libs.versions.toml`, imported in `androidApp/build.gradle.kts` (lines 138-139)
- Mitigation: Monitor releases; have contingency plan for custom feedback system

**Detekt Version Management:**
- Risk: Detekt 1.23.8 is several versions behind latest (as of 2026), potential security/performance issues in static analysis
- Impact: Code quality checks may miss new issues or false positives
- Fix approach: Regularly update detekt, test for new warnings in build

## Architecture Concerns

**Tight Coupling in Navigation:**
- Issue: MainActivity directly depends on specific ViewModel factories and screen components
- Files: `androidApp/src/main/java/com/gdgnantes/devfest/androidapp/MainActivity.kt`
- Impact: Hard to test navigation logic; changes to screen composition require MainActivity changes
- Fix approach: Use navigation graph declaration, extract navigation setup to separate module

---

*Concerns audit: 2026-09-11*
