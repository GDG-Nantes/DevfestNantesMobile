# Coding Conventions

**Analysis Date:** 2026-09-11

## Naming Patterns

**Files:**
- PascalCase for Kotlin classes: `BookmarksViewModel.kt`, `ScheduleSlot.kt`
- PascalCase for Composable screens: `Settings.kt`, `Home.kt`
- Snake_case suffixes for test files: `MainActivityTest.kt`, `GraphQLStoreJvmTest.kt`
- Utility files: `SessionExtensions.kt`, `DateUtils.kt`

**Functions:**
- camelCase for all functions and methods
- Extension functions use camelCase: `getDurationInMinutes()`, `getLanguageEmojiString()`
- @Composable functions use PascalCase: `Settings()`, `TopAppBar()`
- Test methods use descriptive snake_case with pattern `verb_noun_result`: `agenda_flow_emits_agenda()`, `getRoom_returns_room_or_null()`

**Variables:**
- camelCase for all variables and parameters
- Private properties prefixed with underscore: `_state`, `_event`
- Public properties without underscore: `startDate`, `endDate`
- Loop/iteration variables: standard camelCase `entry`, `session`

**Types:**
- PascalCase for classes, data classes, and interfaces: `ScheduleSlot`, `DevFestNantesStore`
- CONSTANT_CASE for module-level constants: `MILLISECONDS_IN_MINUTE`
- Generic type parameters: single uppercase letters when possible: `T`, `K`, `V`

## Code Style

**Formatting:**
- Tool: Detekt with auto-correct enabled
- Max line length: 120 characters
- Indentation: 4 spaces
- No trailing spaces, no semicolons, no wildcard imports
- Final newline required in all files

**Linting:**
- Tool: Detekt 1.23.8 (active formatting and style rules)
- Config: `linters/detekt-config.yml`
- Auto-correct enabled: `true`
- Key rules enabled:
  - `MaximumLineLength: 120` (ignores @Test annotated code)
  - `FunctionNaming: [a-zA-Z][a-zA-Z0-9]*` (ignores @Composable, @Test)
  - `PackageNaming: [a-z]+(\.[a-z][A-Za-z_0-9]*)*`
  - `NoUnitReturn`, `NoSemicolons`, `NoWildcardImports`, `NoTrailingSpaces`
  - `StringTemplate` (prefer string interpolation)
  - `LongParameterList: functionThreshold=16, constructorThreshold=7`
  - `LongMethod: threshold=160`

## Import Organization

**Order:**
1. `java.*` and `javax.*` imports
2. `kotlin.*` imports
3. `androidx.*` imports
4. `com.google.*` imports
5. `com.gdgnantes.*` (project-specific)
6. All other imports
7. Single underscore `^` for static imports (rarely used)

**Path Aliases:**
- No path aliases configured (full imports used)

**Pattern from actual code:**
```kotlin
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdgnantes.devfest.analytics.AnalyticsPage
import com.gdgnantes.devfest.store.BookmarksStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
```

## Error Handling

**Patterns:**
- No global try-catch blocks observed
- Errors propagated as exceptions (let caller handle via coroutine error handling)
- Assertion errors with descriptive messages in tests: `throw AssertionError("Unparseable date: ...", e)`
- Null-coalescing with default values in model: `val endDate: String = ""`

**Example from codebase:**
```kotlin
try {
    format.parse(session.scheduleSlot.startDate)
} catch (e: Exception) {
    throw AssertionError("Unparseable date: ${session.scheduleSlot.startDate}", e)
}
```

## Logging

**Framework:** Timber (5.0.1)

**Patterns:**
- Debug logs for informational messages: `Timber.d("Speakers loaded: ${speakersList.size} speakers")`
- Warning logs for recoverable issues: `Timber.w("Venue's image loading failed")`
- Timber is imported and used throughout ViewModels and Composables
- No log level filtering observed; Timber handles level-based output

**Example usage:**
```kotlin
import timber.log.Timber

Timber.d("Agenda loaded with ${days.values.sumOf { it.sessions.size }} sessions")
Timber.d("Session filters updated: ${filters.size} active filters")
Timber.w(state.result.throwable, "Venue's image loading failed")
```

## Comments

**When to Comment:**
- Inline comments for non-obvious logic: `// else don't care about letters that don't exist`
- Commented-out code blocks (rare): lines 49-51 in `Speakers.kt`
- TODO/FIXME comments: not observed; use Detekt rules to prevent

**JSDoc/TSDoc:**
- No KDoc comments observed in codebase
- Not enforced (Kdoc rule is disabled in detekt: `Kdoc: active: false`)
- Focus on code clarity over documentation comments

**Example inline comment:**
```kotlin
// Add spacing before the button
Spacer(modifier = Modifier.height(8.dp))

// Add the clear filters button
ClearFiltersButton(...)
```

## Function Design

**Size:**
- Max method length: 160 lines (enforced by Detekt `LongMethod`)
- Functions kept concise and focused on single responsibility
- Example: `BookmarksViewModel.setBookmarked()` is 2 lines, pure delegation

**Parameters:**
- Max 16 parameters for functions (Detekt rule: `functionThreshold: 16`)
- Max 7 parameters for constructors (Detekt rule: `constructorThreshold: 7`)
- Ignore default parameters in count
- @Composable functions exempt from parameter count limits
- Use data classes for grouped parameters when needed

**Return Values:**
- Single return value preferred
- No implicit returns observed; explicit `return` used
- Nullable returns use `?` syntax: `fun getRoom(id: String): Room?`
- Flow-based returns for reactive data: `fun subscribe(id: String): Flow<Boolean>`

## Module Design

**Exports:**
- No barrel files observed in project
- Each file contains single primary type
- Internal visibility not explicitly used; rely on package structure

**Dependency Injection:**
- Hilt annotations used: `@HiltViewModel`, `@Inject constructor`
- Dependencies injected into constructors, not properties
- Singleton pattern via `@Singleton` annotation (standard Android practice)

**Example DI pattern:**
```kotlin
@HiltViewModel
class BookmarksViewModel @Inject constructor(
    private val analyticsService: AnalyticsService,
    private val bookmarksStore: BookmarksStore,
) : ViewModel()
```

**Architectural Patterns:**
- MVVM for Android (ViewModel + State/Flow)
- Clean Architecture layers: Store (data), ViewModel (presentation logic), UI (Composables)
- Store pattern for shared business logic (KMP, used by both Android and shared modules)

---

*Convention analysis: 2026-09-11*
