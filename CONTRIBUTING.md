# Contributing to DevFest Nantes Mobile App

We welcome contributions from the community! This guide will help you get started with contributing to the DevFest Nantes mobile application.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Coding Standards](#coding-standards)
- [Pull Request Process](#pull-request-process)
- [Issue Reporting](#issue-reporting)
- [Feature Requests](#feature-requests)

## Code of Conduct

This project follows the [DevFest Code of Conduct](https://devfest.gdgnantes.com/code-of-conduct). By participating, you are expected to uphold this code.

## Getting Started

### Prerequisites

Before contributing, make sure you have:

1. **Development Environment:** Follow the [Development Setup Guide](documentation/DEVELOPMENT.md)
2. **Understanding:** Read the [Architecture Guide](documentation/ARCHITECTURE.md)
3. **Account:** GitHub account for submitting pull requests

### Your First Contribution

Looking for a good first issue? Check out:
- Issues labeled `good first issue`
- Issues labeled `documentation`
- Small bug fixes or improvements

## Development Workflow

### 1. Fork and Clone

```bash
# Fork the repository on GitHub
# Clone your fork
git clone https://github.com/YOUR_USERNAME/DevfestNantesMobile.git
cd DevfestNantesMobile

# Add upstream remote
git remote add upstream https://github.com/GDG-Nantes/DevfestNantesMobile.git
```

### 2. Create a Feature Branch

```bash
# Update your main branch
git checkout main
git pull upstream main

# Create a new branch for your feature
git checkout -b feature/your-feature-name
# or for bug fixes
git checkout -b fix/bug-description
```

### 3. Make Your Changes

- Follow the [coding standards](#coding-standards) below
- Write tests for new functionality
- Update documentation if needed
- Ensure your changes work on both Android and iOS

### 4. Test Your Changes

```bash
# Run all tests
./gradlew test

# Test Android build
./gradlew :androidApp:assembleDebug

# Test iOS build (macOS only)
xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp build
```

### 5. Commit Your Changes

Use conventional commit messages:

```bash
# Format: type(scope): description
git commit -m "feat(agenda): add session filtering by complexity"
git commit -m "fix(ios): resolve crash when loading speaker details"
git commit -m "docs: update setup instructions for Android"
```

**Commit Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build process or auxiliary tool changes

### 6. Push and Create Pull Request

```bash
# Push your branch
git push origin feature/your-feature-name

# Create a pull request on GitHub
```

## Coding Standards

### General Guidelines

1. **Follow existing patterns** in the codebase
2. **Write clear, self-documenting code**
3. **Add comments for complex logic**
4. **Prefer composition over inheritance**
5. **Use meaningful variable and function names**

### Kotlin (Shared Module)

#### Style Guidelines

- Use 4 spaces for indentation
- Use meaningful variable names
- Follow Kotlin coding conventions
- Use ktlint for formatting

```kotlin
// Good
class SessionRepository(
    private val graphqlStore: GraphQLStore,
    private val cacheManager: CacheManager
) {
    suspend fun getSession(sessionId: String): Session? {
        return try {
            graphqlStore.getSession(sessionId)
        } catch (exception: Exception) {
            logger.error("Failed to fetch session: $sessionId", exception)
            null
        }
    }
}

// Avoid
class repo(val store: GraphQLStore, val cache: CacheManager) {
    suspend fun get(id: String) = store.getSession(id)
}
```

#### Documentation

Use KDoc for public APIs:

```kotlin
/**
 * Retrieves a session by its unique identifier.
 *
 * @param sessionId The unique identifier of the session
 * @return The session if found, null otherwise
 * @throws NetworkException if there's a network connectivity issue
 */
suspend fun getSession(sessionId: String): Session?
```

### Android (Kotlin + Compose)

#### Architecture Patterns

- Use **MVVM** with ViewModels
- Inject dependencies with **Dagger Hilt**
- Use **StateFlow** for state management
- Follow **Compose** best practices

```kotlin
@HiltViewModel
class SessionViewModel @Inject constructor(
    private val sessionRepository: SessionRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SessionUiState.Loading)
    val uiState: StateFlow<SessionUiState> = _uiState.asStateFlow()
    
    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            _uiState.value = SessionUiState.Loading
            try {
                val session = sessionRepository.getSession(sessionId)
                _uiState.value = if (session != null) {
                    SessionUiState.Success(session)
                } else {
                    SessionUiState.Error("Session not found")
                }
            } catch (exception: Exception) {
                _uiState.value = SessionUiState.Error(exception.message ?: "Unknown error")
            }
        }
    }
}
```

#### Compose Guidelines

- Use `@Composable` functions for UI components
- Extract reusable components
- Use Material 3 design system
- Follow state hoisting principles

```kotlin
@Composable
fun SessionCard(
    session: Session,
    onSessionClick: (Session) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onSessionClick(session) },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = session.title,
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = session.abstract,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
```

### iOS (Swift + SwiftUI)

#### Style Guidelines

- Use 4 spaces for indentation
- Follow Swift API Design Guidelines
- Use Swift Package Manager for dependencies
- Prefer SwiftUI over UIKit

```swift
// Good
@MainActor
class SessionViewModel: BaseViewModel {
    @Published var session: Session_?
    @Published var isLoading = false
    
    func loadSession(sessionId: String) async {
        isLoading = true
        defer { isLoading = false }
        
        do {
            let fetchedSession = try await asyncFunction(for: store.getSession(id: sessionId))
            session = fetchedSession
        } catch {
            Logger(subsystem: Bundle.main.bundleIdentifier ?? "DevFestNantes", category: "Session")
                .error("Failed to load session: \(error.localizedDescription)")
        }
    }
}
```

#### SwiftUI Guidelines

- Use ViewModels for business logic
- Follow MVVM pattern
- Use proper state management

```swift
struct SessionView: View {
    @ObservedObject var viewModel: SessionViewModel
    let sessionId: String
    
    var body: some View {
        LoadingView(isShowing: $viewModel.isLoading) {
            if let session = viewModel.session {
                SessionDetailView(session: session)
            } else {
                Text("Session not found")
                    .foregroundColor(.secondary)
            }
        }
        .task {
            await viewModel.loadSession(sessionId: sessionId)
        }
    }
}
```

#### Logging Standards

Use the modern Logger API consistently:

```swift
Logger(subsystem: Bundle.main.bundleIdentifier ?? "DevFestNantes", category: "CategoryName")
    .error("Error message: \(error.localizedDescription)")
```

### Dependency Management

#### Always use libs.versions.toml

All dependencies must be defined in `gradle/libs.versions.toml`:

```toml
[versions]
kotlin = "2.2.0"
compose = "2025.07.00"

[libraries]
kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "compose" }

[plugins]
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
```

#### iOS Dependencies

- **Prefer Swift Package Manager** over CocoaPods
- Document dependency choices in pull requests
- Test dependencies on both simulator and device

## Pull Request Process

### Before Submitting

1. **Ensure your code follows the style guidelines**
2. **Add or update tests for your changes**
3. **Update documentation if needed**
4. **Test on both platforms (Android and iOS)**
5. **Rebase your branch on the latest main**

### Pull Request Template

Use this template for your PR description:

```markdown
## Description
Brief description of the changes made.

## Type of Change
- [ ] Bug fix (non-breaking change that fixes an issue)
- [ ] New feature (non-breaking change that adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work)
- [ ] Documentation update

## Testing
- [ ] Android build and tests pass
- [ ] iOS build and tests pass (if applicable)
- [ ] Manual testing completed

## Checklist
- [ ] My code follows the style guidelines
- [ ] I have performed a self-review of my code
- [ ] I have commented my code, particularly in hard-to-understand areas
- [ ] I have made corresponding changes to the documentation
- [ ] My changes generate no new warnings
- [ ] I have added tests that prove my fix is effective or that my feature works
```

### Review Process

1. **Automated checks** must pass (build, tests, linting)
2. **Code review** by at least one maintainer
3. **Manual testing** on both platforms (when applicable)
4. **Documentation review** if docs were changed

## Issue Reporting

### Bug Reports

Use the bug report template and include:

- **Description:** Clear description of the bug
- **Steps to reproduce:** Detailed steps
- **Expected behavior:** What should happen
- **Actual behavior:** What actually happens
- **Environment:** Platform, OS version, app version
- **Screenshots/logs:** If applicable

### Feature Requests

Use the feature request template and include:

- **Problem description:** What problem does this solve?
- **Proposed solution:** Describe your proposed solution
- **Alternatives considered:** Other solutions you've considered
- **Additional context:** Any other relevant information

## Feature Requests

We welcome feature requests! Please:

1. **Check existing issues** to avoid duplicates
2. **Clearly describe the use case**
3. **Consider the scope** - prefer smaller, focused features
4. **Think about both platforms** - how would this work on Android and iOS?

## Development Environment Notes

### OpenFeedback Integration

- OpenFeedback is **disabled by default** in development
- Secret credentials are required for full functionality
- Forms are hidden without proper configuration
- You can develop most features without OpenFeedback access

### Mock vs. Real Data

The app supports both mock and real server data:

```kotlin
// Configure in DevFestNantesStoreBuilder
val store = DevFestNantesStoreBuilder()
    .setUseMockServer(useMockServer = true) // for development
    .build()
```

## Questions or Need Help?

- **Documentation:** Check our comprehensive docs
- **Issues:** Search existing GitHub issues
- **Discussions:** Use GitHub Discussions for questions
- **Community:** Join our community channels

Thank you for contributing to DevFest Nantes! 🎉