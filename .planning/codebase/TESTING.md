# Testing Patterns

**Analysis Date:** 2026-09-11

## Test Framework

**Runner:**
- JUnit 4 (4.13.2) - Primary test runner
- Kotlin Test (2.2.0) - Kotlin stdlib test utilities
- AndroidJUnit4 - For instrumented Android tests
- Config files: `gradle/libs.versions.toml` defines test bundles and dependencies

**Assertion Library:**
- `kotlin.test` assertions: `assertNotNull()`, `assert()`
- `androidx.test.ext.truth` (Truth) - For Android-specific assertions
- Basic assertions used, no external assertion libraries like AssertJ

**Run Commands:**
```bash
./gradlew test                    # Run all unit tests
./gradlew connectedAndroidTest    # Run instrumentation tests
./gradlew testDebug               # Run tests for debug variant
./gradlew testRelease             # Run tests for release variant
```

## Test File Organization

**Location:**
- Unit tests co-located with source code
- Android unit tests: `androidApp/src/test/java/...`
- Instrumented tests (androidTest): `androidApp/src/androidTest/java/...`
- Shared module unit tests: `shared/src/commonTest/kotlin/...`
- Shared module JVM tests: `shared/src/jvmTest/kotlin/...`

**Naming:**
- Pattern: `[ClassName]Test.kt` or `[ClassName]AndroidTest.kt`
- Examples: `MainActivityTest.kt`, `GraphQLStoreJvmTest.kt`, `ScheduleSlotDateFormatAndroidTest.kt`

**Structure:**
```
androidApp/
├── src/
│   ├── androidTest/java/com/gdgnantes/devfest/android/
│   │   └── MainActivityTest.kt
│   ├── test/java/...
│   └── main/java/...

shared/
├── src/
│   ├── commonTest/kotlin/com/gdgnantes/devfest/store/
│   │   └── DevFestNantesStoreContractTest.kt
│   ├── jvmTest/kotlin/com/gdgnantes/devfest/store/graphql/
│   │   └── GraphQLStoreJvmTest.kt
│   └── commonMain/kotlin/...
```

## Test Structure

**Suite Organization:**
```kotlin
// From DevFestNantesStoreContractTest.kt
class DevFestNantesStoreContractTest {
    private val store: DevFestNantesStore = DevFestNantesStoreMocked()

    @Test
    fun agenda_flow_emits_agenda() = runTest {
        val agenda = store.agenda.first()
        assertNotNull(agenda)
    }

    @Test
    fun partners_flow_emits_partners() = runTest {
        val partners = store.partners.first()
        assertNotNull(partners)
    }
}
```

**Patterns:**
- Setup: `@BeforeTest` for fixture initialization (not @Before)
- Teardown: Not observed in current tests; no cleanup needed for immutable state
- Assertion: `assertNotNull()`, `assert()` with optional lambda for detailed messages
- One assertion per test preferred, but multiple assertions allowed for related checks

**Example with BeforeTest:**
```kotlin
class GraphQLStoreJvmTest {
    private lateinit var apolloClient: ApolloClient
    private lateinit var store: GraphQLStore

    @BeforeTest
    fun setUp() {
        apolloClient = ApolloClient.Builder()
            .serverUrl("https://confetti-app.dev/graphql")
            .httpHeaders(listOf(HttpHeader("conference", "devfestnantes2024")))
            .build()
        store = GraphQLStore(apolloClient)
    }
}
```

## Mocking

**Framework:** Hilt for dependency injection in tests

**Patterns:**
```kotlin
// From androidApp/src/androidTest/java/com/gdgnantes/devfest/android/MainActivityTest.kt
@RunWith(AndroidJUnit4ClassRunner::class)
class MainActivityTest {

    @get:Rule
    var activityTestRule = createAndroidComposeRule(MainActivity::class.java)

    @Test
    fun ensureHeaderIsDisplayed() {
        activityTestRule.waitForIdle() // Ensure Compose is idle before querying
        val toolbarTitle =
            activityTestRule.onNode(hasTestTag("topAppBar"), useUnmergedTree = true)
        toolbarTitle.assertIsDisplayed()
    }
}
```

**Hilt Test Setup:**
- `HiltAndroidTest` for Android instrumentation tests with dependency injection
- `HiltAndroidRule` or `createAndroidComposeRule()` for test rules
- Test implementations of interfaces injected via `@BindsInstance`

**What to Mock:**
- External API clients (Apollo GraphQL) - constructed with test URLs
- Analytics services - can be stubbed in test variants
- Database - use in-memory or test fixtures

**What NOT to Mock:**
- UI components (Compose, ViewModels) - test real implementations
- Business logic (Store classes) - test actual transformations
- Data classes - use real instances with test values

## Fixtures and Factories

**Test Data:**
```kotlin
// From shared/src/commonTest
private val store: DevFestNantesStore = DevFestNantesStoreMocked()

// From shared/src/jvmTest with setup in @BeforeTest
val apolloClient = ApolloClient.Builder()
    .serverUrl("https://confetti-app.dev/graphql")
    .httpHeaders(listOf(HttpHeader("conference", "devfestnantes2024")))
    .build()
```

**Location:**
- Test doubles co-located in same package as tests (e.g., `DevFestNantesStoreMocked`)
- Setup logic in `@BeforeTest` (Kotlin Test annotation, not JUnit)
- No separate fixtures directory; data created in setup or as test parameters

## Coverage

**Requirements:** Not enforced

**View Coverage:**
```bash
./gradlew testDebugUnitTestCoverage     # For JaCoCo reports (if configured)
./gradlew testDebug --info             # Run with info logging
```

Coverage not explicitly tracked in this project; focus on critical paths.

## Test Types

**Unit Tests:**
- Location: `shared/src/commonTest/kotlin/` and `androidApp/src/test/java/`
- Scope: Individual functions, Store classes, ViewModel logic
- Framework: JUnit 4 + Kotlin Test
- Example: `DevFestNantesStoreContractTest` - tests Store contract across implementations
- Run with: `./gradlew test`

**Integration Tests:**
- Location: `shared/src/jvmTest/kotlin/`
- Scope: GraphQL API integration with Apollo Client
- Framework: JUnit 4 + Kotlin Test + real Apollo client
- Example: `GraphQLStoreJvmTest` - hits live confetti-app.dev/graphql
- Tests real network calls, date parsing, data transformation
- Run with: `./gradlew jvmTest`

**Instrumentation Tests (E2E/UI):**
- Location: `androidApp/src/androidTest/java/`
- Scope: Activity lifecycle, Compose UI rendering, user interactions
- Frameworks: 
  - `androidx.compose.ui.test` - Compose UI testing DSL
  - `androidx.test.espresso` - Espresso for Views (if any legacy code)
  - `androidx.test.ext.junit` - JUnit rules for Android
- Example: `MainActivityTest` - verifies topAppBar displays via testTag
- Run with: `./gradlew connectedAndroidTest`

## Common Patterns

**Async Testing:**
```kotlin
// From DevFestNantesStoreContractTest.kt
@Test
fun agenda_flow_emits_agenda() = runTest {
    val agenda = store.agenda.first()  // Collects first value from Flow
    assertNotNull(agenda)
}
```
- Use `runTest` from `kotlinx.coroutines.test`
- Collect Flow values with `.first()` - cancels after first emission
- Implicit timeout managed by runTest (10 seconds default)
- Works for both Unit and Integration tests

**Error Testing:**
```kotlin
// From GraphQLStoreJvmTest.kt
@Test
fun scheduleSlot_dates_are_parseable_by_android_java() = runTest {
    val sessions = store.sessions.first()
    val format = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
    for (session in sessions) {
        try {
            format.parse(session.scheduleSlot.startDate)
            format.parse(session.scheduleSlot.endDate)
        } catch (e: Exception) {
            throw AssertionError("Unparseable date: ${session.scheduleSlot.startDate}", e)
        }
    }
}
```
- Throw `AssertionError` with descriptive message (includes context)
- Wrap actual exception as cause for full stack trace

**Compose UI Testing:**
```kotlin
// From MainActivityTest.kt
@get:Rule
var activityTestRule = createAndroidComposeRule(MainActivity::class.java)

@Test
fun ensureHeaderIsDisplayed() {
    activityTestRule.waitForIdle()  // Ensures Compose is idle before assertions
    val toolbarTitle = activityTestRule.onNode(hasTestTag("topAppBar"), useUnmergedTree = true)
    toolbarTitle.assertIsDisplayed()
}
```
- `createAndroidComposeRule()` sets up Compose environment
- `waitForIdle()` critical - waits for Compose recomposition to settle
- `hasTestTag()` matches UI elements by testTag modifier
- `useUnmergedTree = true` searches merged and unmerged composables

**Test Dependencies Configuration:**
```kotlin
// From androidApp/build.gradle.kts
testImplementation(libs.dagger.hilt.android.testing)
kspTest(libs.dagger.hilt.compiler)

androidTestImplementation(libs.androidx.compose.ui.test.junit4)
androidTestImplementation(libs.androidx.test.espresso)
androidTestImplementation(libs.androidx.test.ext.junit)
```

## Instrumentation Test Runner

**Default:** `testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"`

**Configuration:** `androidApp/build.gradle.kts`
```kotlin
android {
    defaultConfig {
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }
}
```

---

*Testing analysis: 2026-09-11*
