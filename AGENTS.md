# AGENTS.md

Instructions for AI coding agents working in this repository. This is the single source of truth:
`CLAUDE.md` imports it and `.github/copilot-instructions.md` points to it, so edit this file rather
than those.

## Project

DevFest Nantes is a Kotlin Multiplatform conference app for Android and iOS. Data, network, domain
and analytics code is shared (KMP); the Android UI is Jetpack Compose, the iOS UI is SwiftUI.

Read before making changes:

- [README.md](README.md) - Overview, stack and quick start
- [documentation/ARCHITECTURE.md](documentation/ARCHITECTURE.md) - Modules, data flow, Koin DI,
  navigation, consent
- [documentation/DEVELOPMENT.md](documentation/DEVELOPMENT.md) - Build, test, code quality, common
  issues
- [CONTRIBUTING.md](CONTRIBUTING.md) - Coding standards and workflow
- `.planning/codebase/` - GSD codebase maps (stack, structure, conventions, testing, concerns)

## Architecture

Modules (declared in `settings.gradle.dcl`):

- `androidApp/` - Android app shell: `MainActivity` (Navigation Compose `NavHost`), application
  initializers, app-level Koin modules
- `feature/{agenda,speakers,session-detail,venue,about,settings}/` - one Android module per screen
  area: `<Name>Route.kt` entry composable, `<Name>ViewModel.kt`, `di/<Name>Module.kt`
- `core/model`, `core/network`, `core/data`, `core/analytics`, `core/testing` - KMP modules
- `core/ui/` - Android Compose theme and shared components
- `shared/` - KMP framework consumed by iOS; `initKoin()` is the single Koin composition root
- `iosApp/` - SwiftUI app (Xcode project)
- `build-logic/` - convention plugins (`devfest.android.feature`, `devfest.kmp.library`, ...)

Rules:

- Dependencies flow one way: `androidApp → feature:* → core:ui → core:data → core:network /
  core:model`. Feature modules never depend on each other; cross-feature side effects are passed
  to `*Route` composables as lambdas and handled in `androidApp`.
- Data access goes through the `DevFestNantesStore` interface. A new store method must be added to
  both `GraphQLStore` and `DevFestNantesStoreMocked` in the same change.
- Dependency injection is Koin. ViewModels use constructor injection and are resolved with
  `koinViewModel()`. Service-locator access is only allowed in `di/` packages, `*Module.kt` files,
  `DevFestNantesApplication`, `MainActivity` and `IosDependencies`. Never reintroduce Hilt or
  Dagger. `bash scripts/di-gate.sh` enforces both rules in CI.
- Koin overriding is disabled: do not declare the same binding in two modules.
- ViewModels expose `StateFlow`s; keep mutable state private.

## General Guidelines

- Every dependency used in a `build.gradle.kts` must be declared in `gradle/libs.versions.toml`.
- New Gradle modules apply a `devfest.*` convention plugin instead of configuring Android/Kotlin by
  hand.
- Prefer Swift Package Manager over CocoaPods for iOS dependencies.
- When bumping Kotlin, bump KSP to a compatible version. Do not bump kotlinx-datetime past the
  `0.6.x-compat` pin (OpenFeedback needs it; `OpenFeedbackDatetimeLinkageTest` guards it).
- Never change `versionName`, `versionCode` or `MARKETING_VERSION`: maintainers handle releases
  (see `documentation/VERSIONING.md`).
- Tests: JUnit 4 + `kotlin.test` + Turbine. Use the hand-written fakes in `core/testing` and the
  fixtures in `core/model/.../stubs/`; there is no mocking library. Test names are snake_case
  sentences.
- Write all GSD planning artifacts (`.planning/**`: PROJECT, REQUIREMENTS, ROADMAP, STATE, phase
  CONTEXT/RESEARCH/PLAN/SUMMARY, discussion logs, phase titles and slugs) in English, even when the
  conversation is in another language. `.planning/config.json` sets `response_language` to
  `English` for the same reason.
- When you change something the docs describe (a module, a command, a version), update
  `README.md` / `documentation/` in the same change.

Before considering a change done, run what CI runs:

```bash
./gradlew detekt lint
bash scripts/di-gate.sh
./gradlew testDebugUnitTest jvmTest testAndroidHostTest
```

## Android CLI

When performing Android-specific tasks, prefer the `android` CLI over raw `adb`/`gradlew`
invocations. It is faster and built for agent use.

- Build, install and launch the app: `android run` (from repo root; module `androidApp`,
  package `com.gdgnantes.devfest.androidapp`, `applicationId com.gdgnantes.devfest.mobile.androidapp`,
  main activity `.MainActivity`)
- Inspect the current UI tree (for locating elements, verifying a screen renders correctly):
  `android layout`
- Visually inspect the device / get bounding boxes for UI elements: `android screen capture`,
  `android screen resolve`
- Search official Android documentation (APIs, migration guides, best practices):
  `android docs search "<keywords>"`
- Analyze the project structure / locate build artifacts: `android describe`
- Manage emulators: `android emulator list|start|stop`

If `android` is not on PATH, install it per platform (see the `android-cli` skill /
https://dl.google.com/android/cli/latest/...). Run `android info` to check SDK location and
connected devices before running/building.

For device interaction (`android layout`, `android screen`) and journey/UI test evaluation,
follow the detailed guidance in the `android-cli` skill's `references/interact.md` and
`references/journeys.md` if available in the agent's environment.

## iOS

- The Xcode build phase builds the Kotlin framework (`./gradlew :shared:embedAndSignAppleFrameworkForXcode`).
  To only check that Kotlin compiles for iOS: `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`.
- Build check (pick an available iPhone simulator with `xcrun simctl list devices available`):
  `xcodebuild -project iosApp/iosApp.xcodeproj -scheme iosApp -configuration Debug -destination 'platform=iOS Simulator,name=<simulator name>' build`
- The iOS app needs SwiftGen (`brew install swiftgen`) and a `GoogleService-Info.plist` that is not
  committed. Test SwiftGen configuration changes manually before relying on the build phase.
- Logging: use `DevFestLogger` (`iosApp/iosApp/Analytics/DevFestLogger.swift`), which wraps the
  `Logger` API and forwards warnings and errors to Crashlytics. Do not use `os_log` or `print` in
  new code (one legacy `print` remains in `Extension/UIImage-extension.swift`).
  Use a category named after the ViewModel or feature area:
  ```swift
  private let logger = DevFestLogger(category: "Speakers")
  logger.log(.error, "Failed to load speakers", error: error)
  ```

## GitHub CLI

Prefer the `gh` CLI over raw GitHub web/API requests (curl, WebFetch, etc.) for anything
involving this repository's GitHub data or actions: issues, pull requests, checks,
releases, workflow runs. It's authenticated, faster, and avoids scraping HTML.

- Issues: `gh issue list|view|create|comment ...`
- Pull requests: `gh pr list|view|create|checkout|diff|review|comment ...`
- CI/checks: `gh run list|view|watch`, `gh pr checks`
- Releases: `gh release list|view|create`
- Arbitrary API access when no subcommand fits: `gh api repos/{owner}/{repo}/...`

Only fall back to web fetches or raw HTTP requests for things `gh` cannot do (e.g.
browsing content outside GitHub, or GitHub UI-only features with no API equivalent).

## Commits and Pull Requests

- Use conventional commit messages (`feat:`, `fix:`, `docs:`, `chore:`, ...).
- Make sure the branch is up to date with `main` before opening a pull request.
- Follow `.github/pull_request_template.md`, keeping only the relevant sections:
  - Always: Description, Type of Change, Changes Made
  - Platform Testing: only if the change affects Android or iOS behavior
  - Screenshots/Videos: only for UI changes
  - Breaking Changes: only if applicable

## Community Android skills (optional, not versioned)

This repo touches Clean Architecture, Compose UI/Navigation, Kotlin Coroutines, Retrofit and
Gradle build logic, which are areas covered by the community skill collection
[new-silvermoon/awesome-android-agent-skills](https://github.com/new-silvermoon/awesome-android-agent-skills).
These skills are **not part of this repo** and must be installed once per machine, outside
any git-tracked project, so use them if your agent environment has them available:

```bash
git clone https://github.com/new-silvermoon/awesome-android-agent-skills.git \
  ~/.claude/community-skills-src/awesome-android-agent-skills

src=~/.claude/community-skills-src/awesome-android-agent-skills/.github/skills
for dir in "$src"/*/*/; do
  name=$(basename "$dir")
  [ -e ~/.claude/skills/"$name" ] || ln -s "$dir" ~/.claude/skills/"$name"
done
```

This symlinks each skill (`android-architecture`, `android-viewmodel`, `android-data-layer`,
`compose-ui`, `compose-navigation`, `coil-compose`, `android-accessibility`,
`xml-to-compose-migration`, `rxjava-to-coroutines-migration`, `compose-performance-audit`,
`gradle-build-performance`, `kotlin-concurrency-expert`, `android-coroutines`,
`android-retrofit`, `android-testing`, `android-emulator-skill`, `android-gradle-logic`) into
`~/.claude/skills/`, so a later `git pull` in the cloned source keeps them up to date. Nothing
is written into this repo.
