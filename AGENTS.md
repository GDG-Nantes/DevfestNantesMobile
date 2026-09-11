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
