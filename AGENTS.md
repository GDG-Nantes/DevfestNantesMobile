# AGENTS.md

Instructions for AI coding agents working in this repository.

## Android CLI

This is a Kotlin Multiplatform project with an Android app module (`androidApp`) and a
shared module (`shared`). When performing Android-specific tasks, prefer the `android`
CLI over raw `adb`/`gradlew` invocations — it is faster and purpose-built for agent use.

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

## GitHub CLI

Prefer the `gh` CLI over raw GitHub web/API requests (curl, WebFetch, etc.) for anything
involving this repository's GitHub data or actions — issues, pull requests, checks,
releases, workflow runs. It's authenticated, faster, and avoids scraping HTML.

- Issues: `gh issue list|view|create|comment ...`
- Pull requests: `gh pr list|view|create|checkout|diff|review|comment ...`
- CI/checks: `gh run list|view|watch`, `gh pr checks`
- Releases: `gh release list|view|create`
- Arbitrary API access when no subcommand fits: `gh api repos/{owner}/{repo}/...`

Only fall back to web fetches or raw HTTP requests for things `gh` cannot do (e.g.
browsing content outside GitHub, or GitHub UI-only features with no API equivalent).
